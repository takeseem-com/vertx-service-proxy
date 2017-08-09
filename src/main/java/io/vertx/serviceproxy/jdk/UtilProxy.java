/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertx.serviceproxy.jdk;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.ProxyClose;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.ProxyIgnore;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.Json;
import io.vertx.core.shareddata.impl.ClusterSerializable;
import io.vertx.serviceproxy.ServiceException;
import io.vertx.serviceproxy.ServiceExceptionMessageCodec;

/**
 * @author <a href="https://github.com/takeseem">杨浩</a>
 */
public class UtilProxy {
  private static final Set<String> registerDefaultCodecs = new HashSet<>();
  
  public static void registerDefaultCodec(Vertx vertx) {
    registerDefaultCodec(vertx, ServiceException.class, new ServiceExceptionMessageCodec());
  }
  private static <T> void registerDefaultCodec(Vertx vertx, Class<T> clazz, MessageCodec<T, ?> codec) {
    String key = clazz.getName();
    if (registerDefaultCodecs.contains(key)) return;
    
    try {
      vertx.eventBus().registerDefaultCodec(clazz, codec);
    } catch (IllegalStateException ex) {
    }
    registerDefaultCodecs.add(key);
  }
  
  public static <T> T createJdkProxy(Class<T> clazz, Vertx vertx, String address) {
    return createJdkProxy(clazz, vertx, address, null);
  }
  public static <T> T createJdkProxy(Class<T> clazz, Vertx vertx, String address, DeliveryOptions options) {
    
    return new JdkProxy(clazz, vertx, address, options).getProxy();
  }
  
  /** just convert {@linkplain AsyncResult}&lt;T&gt; to {@linkplain Future}&lt;T&gt; */
  public static <T> Future<T> handleAsyncResult(Consumer<Handler<AsyncResult<T>>> handler) {
    Future<T> future = Future.future();
    handler.accept(asyncResult -> {
      if (asyncResult.succeeded()) {
        future.complete(asyncResult.result());
      } else {
        future.fail(asyncResult.cause());
      }
    });
    return future;
  }
  
  public static Map<Method, MethodModel> createMethodModel(Class<?> clazz) {
    Map<Method, MethodModel> result = new HashMap<>();
    for (Method method : getMethods(clazz)) {
      if (isToString(method) || isHashCode(method) || isEquals(method)) continue;
      result.put(method, new MethodModel(method));
    }
    return result;
  }
  private static List<Method> getMethods(Class<?> clazz) {
    List<Method> result = new ArrayList<>();
    if (clazz == null || clazz == Object.class) {
      try {
        result.add(Object.class.getMethod("toString", new Class<?>[0]));
        result.add(Object.class.getMethod("hashCode", new Class<?>[0]));
        result.add(Object.class.getMethod("equals", new Class<?>[] {Object.class}));
      } catch (NoSuchMethodException | SecurityException e) {
        throw new IllegalArgumentException(e);
      }
      return result;
    }
    
    result.addAll(Arrays.asList(clazz.getMethods()));
    result.addAll(getMethods(clazz.getSuperclass()));
    return result;
  }
  
  public static boolean isToString(Method method) {
    return isNameArgSize(method, "toString", 0);
  }
  public static boolean isHashCode(Method method) {
    return isNameArgSize(method, "hashCode", 0);
  }
  public static boolean isEquals(Method method) {
    return isNameArgSize(method, "equals", 0);
  }
  /** args.length == argSize && method.name == name */
  private static boolean isNameArgSize(Method method, String name, int argSize) {
    return method.getParameterCount() == argSize && method.getName().equals(name);
  }
  public static boolean isProxyClose(Method method) {
    if (isNameArgSize(method, "close", 0) && AutoCloseable.class.isAssignableFrom(method.getDeclaringClass())) return true;
    return method.getAnnotation(ProxyClose.class) != null;
  }
  public static boolean isProxyIgnore(Method method) {
    return method.getAnnotation(ProxyIgnore.class) != null;
  }
  public static boolean isProxyGen(Class<?> clazz) {
    return clazz != null && clazz.getAnnotation(ProxyGen.class) != null;
  }
  /** param's type: Collection<T> or T, must be T hash annotation {@linkplain DataObject}  */
  public static boolean isDataObject(Type type) {
    if (type == null) return false;
    if (type instanceof Class) return ((Class<?>) type).getAnnotation(DataObject.class) != null;
    if (type instanceof ParameterizedType) {
      if (isDataObject(((ParameterizedType) type).getRawType())) return true;
      
      Type[] typeArgs = ((ParameterizedType) type).getActualTypeArguments();
      for (Type tmp : typeArgs) {
        if (isDataObject(tmp)) return true;
      }
      return false;
    }
    
    throw new IllegalArgumentException("[BUG] getClass(Type) not support this type: " + type);
  }
  
  /**
   * @param parameter must be {@linkplain Handler}&lt;{@linkplain AsyncResult}&gt;&lt;T&gt;
   * @return T in the AsyncResult (may be null)
   */
  public static Type getHandlerAsyncResultTargetType(Parameter parameter) {
    ParameterizedType handlerType = (ParameterizedType) parameter.getParameterizedType();
    Type asyncResultType = handlerType.getActualTypeArguments()[0];
    if (asyncResultType instanceof ParameterizedType) {
      return ((ParameterizedType) asyncResultType).getActualTypeArguments()[0];
    } else {
      return null;
    }
  }
  
  public static Class<?> getClass(Type type) {
    if (type == null) return null;
    if (type instanceof Class) return (Class<?>) type;
    if (type instanceof ParameterizedType) return (Class<?>) ((ParameterizedType) type).getRawType();
    throw new IllegalArgumentException("[BUG] getClass(Type) not support this type: " + type);
  }
  
  /** your must convert when arg serializable to Json not implement in {@linkplain Json#checkAndCopy(Object, boolean)} */
  public static Object encodeToMsgValue(Object arg, Parameter parameter) {
    if (arg == null) return arg;
    
    Class<?> type = parameter.getType();
    if (ClusterSerializable.class.isAssignableFrom(type)) return arg;
    if (type == char.class || type == Character.class) return (int) (char) arg;
    if (type.isPrimitive()) return arg;
    if (Enum.class.isAssignableFrom(type)) return arg.toString();
    if (Collection.class.isAssignableFrom(type)) {
      if (isDataObject(parameter.getParameterizedType())) {
        return ((Collection<?>) arg).stream().map(UtilJson::toJsonObject).collect(Collectors.toList());
      } else if (Set.class.isAssignableFrom(type)) {
        return new ArrayList<>((Set<?>) arg);
      } else {
        return arg;
      }
    }
    
    if (type.getName().startsWith("java.")) return arg;
    return UtilJson.toJsonObject(arg);
  }
  
  public static Object decodeAsyncResult(Object body, MethodModel model) {
    if (body == null) return null;
    Type type = model.getAsyncResultType();
    if (type == null) return body;
    if (type instanceof Class && ((Class<?>) type).isInstance(body)) return body;
    return UtilJson.convertValue(body, model.getAsyncResultJavaType());
  }
}
