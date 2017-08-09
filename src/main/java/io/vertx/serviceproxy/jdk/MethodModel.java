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
import java.lang.reflect.Type;

import com.fasterxml.jackson.databind.JavaType;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;

/**
 * @author <a href="mailto:takeseem@gmail.com">杨浩</a>
 */
public class MethodModel {
	private final Method method;
	private final String info, methodName;
	private final Parameter[] parameters;
	
	private final boolean proxyIgnore;
	private final boolean proxyClose;
	private final boolean returnVoid;

	private int asyncResultHandlerParameterPos = -1;
	private Parameter asyncResultHandlerParameter;
	private Type asyncResultType;
	private Class<?> asyncResultClass;
	private JavaType asyncResultJavaType;
	private boolean asyncResultClassProxyGen;
	
	
	public Method getMethod() {
		return method;
	}
	public String getMethodName() {
		return methodName;
	}
	public Parameter[] getParameters() {
		return parameters;
	}

	public boolean isProxyIgnore() {
		return proxyIgnore;
	}
	public boolean isProxyClose() {
		return proxyClose;
	}
	public boolean isReturnVoid() {
		return returnVoid;
	}

	public int getAsyncResultHandlerParameterPos() {
		return asyncResultHandlerParameterPos;
	}
	public Parameter getAsyncResultHandlerParameter() {
		return asyncResultHandlerParameter;
	}
	public Type getAsyncResultType() {
		return asyncResultType;
	}
	public Class<?> getAsyncResultClass() {
		return asyncResultClass;
	}
	public JavaType getAsyncResultJavaType() {
		return asyncResultJavaType;
	}
	public boolean isAsyncResultClassProxyGen() {
		return asyncResultClassProxyGen;
	}

	
	public MethodModel(Method method) {
		this.method = method;
		proxyIgnore = UtilProxy.isProxyIgnore(method);
		proxyClose = UtilProxy.isProxyClose(method);
		methodName = method.getName();
		returnVoid = method.getReturnType() == Void.class;
		parameters = method.getParameters();
		for (int i = 0, len = parameters.length; i < len; i++) {
			Parameter parameter = parameters[i];
			if (Handler.class.isAssignableFrom(parameter.getType())) {
				asyncResultHandlerParameterPos = i;
				asyncResultHandlerParameter = parameter;
				asyncResultType = UtilProxy.getHandlerAsyncResultTargetType(parameter);
				asyncResultClass = UtilProxy.getClass(asyncResultType);
				asyncResultJavaType = Json.mapper.constructType(asyncResultType);
			} else {
			}
		}
		asyncResultClassProxyGen = UtilProxy.isProxyGen(asyncResultClass);
		info = (asyncResultClassProxyGen ? "@ProxyGen" : "")
				+ ' ' + method.getDeclaringClass().getSimpleName() + '.' + method.getName()
				+ (proxyIgnore ? "@ProxyIgnore" : "") + (proxyClose ? "@ProxyClose" : "");
	}
	
	
	@Override
	public String toString() {
		return info;
	}
	
	@SuppressWarnings("unchecked")
	public <T> Handler<AsyncResult<T>> getAsyncResultHandler(Object[] args) {
		return asyncResultHandlerParameterPos == -1 ? null : (Handler<AsyncResult<T>>) args[asyncResultHandlerParameterPos];
	}
}
