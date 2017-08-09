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
package io.vertx.serviceproxy.test;

import io.vertx.serviceproxy.jdk.JdkProxy;
import io.vertx.serviceproxy.jdk.UtilProxy;
import io.vertx.serviceproxy.testmodel.TestService;

/**
 * service's {@linkplain JdkProxy} call service's VertxProxyHandler
 * @author <a href="https://github.com/takeseem">杨浩</a>
 */
public class ServiceJdkProxyTest extends ServiceProxyTest {
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    Class<TestService> clazz = TestService.class;
    proxy = UtilProxy.createJdkProxy(clazz, vertx, SERVICE_ADDRESS);
  }
  
}
