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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.Map;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * @author <a href="https://github.com/takeseem">杨浩</a>
 */
public class JdkProxy implements InvocationHandler {
	protected final Logger logger;
	private final Class<?> clazz;
	private final Vertx vertx;
	private final String address;
	private final DeliveryOptions options;
	
	private final Map<Method, MethodModel> methodMap;
	private Object jdkProxy;
	private boolean closed;
	private Future<Object> closedFuture;

	public JdkProxy(Class<?> clazz, Vertx vertx, String address, DeliveryOptions options) {
	  logger = LoggerFactory.getLogger(clazz);
		this.clazz = clazz;
		this.vertx = vertx;
		this.address = address;
		this.options = options;
		methodMap = UtilProxy.createMethodModel(clazz);
	}

	@SuppressWarnings("unchecked")
	public <T> T getProxy() {
		UtilProxy.registerDefaultCodec(vertx);
		return (T) (jdkProxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[] { clazz }, this));
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		MethodModel model = methodMap.get(method);
		if (model == null) throw new IllegalStateException("method not support: " + method);
		
		logger.trace("EventBus.proxy address={}, {}, args={}", address, model, args);
    JsonObject json = new JsonObject();
    DeliveryOptions deliveryOptions = options == null ? new DeliveryOptions() : new DeliveryOptions(options);
    deliveryOptions.addHeader("action", model.getMethodName());

    int resultHandlerPos = model.getAsyncResultHandlerParameterPos();
    Parameter[] parameters = model.getParameters();
    for (int i = 0, len = parameters.length; i < len; i++) {
      if (i == resultHandlerPos) continue;

      Parameter parameter = parameters[i];
      Object arg = args[i];
      json.put(parameter.getName(), UtilProxy.encodeToMsgValue(arg, parameter));
    }

    Future<Object> resultFuture = null;
    if (model.isProxyIgnore()) {
      resultFuture = Future.succeededFuture();
    } else {
      if (closed) {
        if (model.isProxyClose()) {
          resultFuture = closedFuture;
        } else {
          resultFuture = Future.failedFuture(new IllegalStateException("Proxy is closed: " + model));
        }
      } else {
        logger.trace("EventBus.send address={}, json={}", address, json);
        Future<Message<Object>> replyFuture = UtilProxy.handleAsyncResult(handler -> vertx.eventBus().send(address, json, deliveryOptions, handler));
        resultFuture = replyFuture.map(msg -> {
          String proxyaddr = msg.headers().get("proxyaddr");
          if (proxyaddr != null && !proxyaddr.isEmpty()) return UtilProxy.createJdkProxy(model.getAsyncResultClass(), vertx, proxyaddr);

          Object body = msg.body();
          if (body == null) return null;

          return UtilJson.convertValue(body, model.getAsyncResultJavaType());
        });
        if (model.isProxyClose()) {
          closed = true;
          closedFuture = resultFuture;
        }
      }
    }

    Handler<AsyncResult<Object>> resultHandler = model.getAsyncResultHandler(args);
    if (resultHandler != null) resultFuture.setHandler(resultHandler);
    if (model.isReturnVoid()) return null;
    return jdkProxy;
	}
}

