/*
* Copyright 2014 Red Hat, Inc.
*
* Red Hat licenses this file to you under the Apache License, version 2.0
* (the "License"); you may not use this file except in compliance with the
* License. You may obtain a copy of the License at:
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
* License for the specific language governing permissions and limitations
* under the License.
*/

package io.vertx.serviceproxy.clustered;

import io.vertx.serviceproxy.clustered.Service;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.Vertx;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.function.Function;
import io.vertx.serviceproxy.ProxyHelper;
import io.vertx.serviceproxy.ServiceException;
import io.vertx.serviceproxy.ServiceExceptionMessageCodec;
import io.vertx.serviceproxy.testmodel.SomeEnum;
import io.vertx.serviceproxy.testmodel.SomeVertxEnum;
import io.vertx.core.json.JsonArray;
import java.util.List;
import io.vertx.serviceproxy.testmodel.TestDataObject;
import io.vertx.serviceproxy.clustered.Service;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/*
  Generated Proxy code - DO NOT EDIT
  @author Roger the Robot
*/
@SuppressWarnings({"unchecked", "rawtypes"})
public class ServiceVertxEBProxy implements Service {

  private Vertx _vertx;
  private String _address;
  private DeliveryOptions _options;
  private boolean closed;

  public ServiceVertxEBProxy(Vertx vertx, String address) {
    this(vertx, address, null);
  }

  public ServiceVertxEBProxy(Vertx vertx, String address, DeliveryOptions options) {
    this._vertx = vertx;
    this._address = address;
    this._options = options;
    try {
      this._vertx.eventBus().registerDefaultCodec(ServiceException.class,
          new ServiceExceptionMessageCodec());
    } catch (IllegalStateException ex) {}
  }

  public Service hello(String name, Handler<AsyncResult<String>> result) {
    if (closed) {
    result.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return this;
    }
    JsonObject _json = new JsonObject();
    _json.put("name", name);
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "hello");
    _vertx.eventBus().<String>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        result.handle(Future.failedFuture(res.cause()));
      } else {
        result.handle(Future.succeededFuture(res.result().body()));
      }
    });
    return this;
  }

  public Service methodUsingEnum(SomeEnum e, Handler<AsyncResult<Boolean>> result) {
    if (closed) {
    result.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return this;
    }
    JsonObject _json = new JsonObject();
    _json.put("e", e == null ? null : e.toString());
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "methodUsingEnum");
    _vertx.eventBus().<Boolean>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        result.handle(Future.failedFuture(res.cause()));
      } else {
        result.handle(Future.succeededFuture(res.result().body()));
      }
    });
    return this;
  }

  public Service methodReturningEnum(Handler<AsyncResult<SomeEnum>> result) {
    if (closed) {
    result.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return this;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "methodReturningEnum");
    _vertx.eventBus().<String>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        result.handle(Future.failedFuture(res.cause()));
      } else {
        result.handle(Future.succeededFuture(res.result().body() == null ? null : SomeEnum.valueOf(res.result().body())));
      }
    });
    return this;
  }

  public Service methodReturningVertxEnum(Handler<AsyncResult<SomeVertxEnum>> result) {
    if (closed) {
    result.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return this;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "methodReturningVertxEnum");
    _vertx.eventBus().<String>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        result.handle(Future.failedFuture(res.cause()));
      } else {
        result.handle(Future.succeededFuture(res.result().body() == null ? null : SomeVertxEnum.valueOf(res.result().body())));
      }
    });
    return this;
  }

  public Service methodWithJsonObject(JsonObject json, Handler<AsyncResult<JsonObject>> result) {
    if (closed) {
    result.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return this;
    }
    JsonObject _json = new JsonObject();
    _json.put("json", json);
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "methodWithJsonObject");
    _vertx.eventBus().<JsonObject>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        result.handle(Future.failedFuture(res.cause()));
      } else {
        result.handle(Future.succeededFuture(res.result().body()));
      }
    });
    return this;
  }

  public Service methodWithJsonArray(JsonArray json, Handler<AsyncResult<JsonArray>> result) {
    if (closed) {
    result.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return this;
    }
    JsonObject _json = new JsonObject();
    _json.put("json", json);
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "methodWithJsonArray");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        result.handle(Future.failedFuture(res.cause()));
      } else {
        result.handle(Future.succeededFuture(res.result().body()));
      }
    });
    return this;
  }

  public Service methodWithList(List<String> list, Handler<AsyncResult<List<String>>> result) {
    if (closed) {
    result.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return this;
    }
    JsonObject _json = new JsonObject();
    _json.put("list", new JsonArray(list));
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "methodWithList");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        result.handle(Future.failedFuture(res.cause()));
      } else {
        result.handle(Future.succeededFuture(convertList(res.result().body().getList())));
      }
    });
    return this;
  }

  public Service methodWithDataObject(TestDataObject data, Handler<AsyncResult<TestDataObject>> result) {
    if (closed) {
    result.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return this;
    }
    JsonObject _json = new JsonObject();
    _json.put("data", data == null ? null : data.toJson());
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "methodWithDataObject");
    _vertx.eventBus().<JsonObject>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        result.handle(Future.failedFuture(res.cause()));
      } else {
        result.handle(Future.succeededFuture(res.result().body() == null ? null : new TestDataObject(res.result().body())));
                      }
    });
    return this;
  }

  public Service methodWithListOfDataObject(List<TestDataObject> list, Handler<AsyncResult<List<TestDataObject>>> result) {
    if (closed) {
    result.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return this;
    }
    JsonObject _json = new JsonObject();
    _json.put("list", new JsonArray(list.stream().map(r -> r == null ? null : r.toJson()).collect(Collectors.toList())));
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "methodWithListOfDataObject");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        result.handle(Future.failedFuture(res.cause()));
      } else {
        result.handle(Future.succeededFuture(res.result().body().stream()
            .map(o -> { if (o == null) return null;
                        return o instanceof Map ? new TestDataObject(new JsonObject((Map) o)) : new TestDataObject((JsonObject) o);
                 })
            .collect(Collectors.toList())));
      }
    });
    return this;
  }

  public Service methodWithListOfJsonObject(List<JsonObject> list, Handler<AsyncResult<List<JsonObject>>> result) {
    if (closed) {
    result.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return this;
    }
    JsonObject _json = new JsonObject();
    _json.put("list", new JsonArray(list));
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "methodWithListOfJsonObject");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        result.handle(Future.failedFuture(res.cause()));
      } else {
        result.handle(Future.succeededFuture(convertList(res.result().body().getList())));
      }
    });
    return this;
  }

  public Service methodWthFailingResult(String input, Handler<AsyncResult<JsonObject>> result) {
    if (closed) {
    result.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return this;
    }
    JsonObject _json = new JsonObject();
    _json.put("input", input);
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "methodWthFailingResult");
    _vertx.eventBus().<JsonObject>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        result.handle(Future.failedFuture(res.cause()));
      } else {
        result.handle(Future.succeededFuture(res.result().body()));
      }
    });
    return this;
  }


  private List<Character> convertToListChar(JsonArray arr) {
    List<Character> list = new ArrayList<>();
    for (Object obj: arr) {
      Integer jobj = (Integer)obj;
      list.add((char)(int)jobj);
    }
    return list;
  }

  private Set<Character> convertToSetChar(JsonArray arr) {
    Set<Character> set = new HashSet<>();
    for (Object obj: arr) {
      Integer jobj = (Integer)obj;
      set.add((char)(int)jobj);
    }
    return set;
  }

  private <T> Map<String, T> convertMap(Map map) {
    if (map.isEmpty()) { 
      return (Map<String, T>) map; 
    } 
     
    Object elem = map.values().stream().findFirst().get(); 
    if (!(elem instanceof Map) && !(elem instanceof List)) { 
      return (Map<String, T>) map; 
    } else { 
      Function<Object, T> converter; 
      if (elem instanceof List) { 
        converter = object -> (T) new JsonArray((List) object); 
      } else { 
        converter = object -> (T) new JsonObject((Map) object); 
      } 
      return ((Map<String, T>) map).entrySet() 
       .stream() 
       .collect(Collectors.toMap(Map.Entry::getKey, converter::apply)); 
    } 
  }
  private <T> List<T> convertList(List list) {
    if (list.isEmpty()) { 
          return (List<T>) list; 
        } 
     
    Object elem = list.get(0); 
    if (!(elem instanceof Map) && !(elem instanceof List)) { 
      return (List<T>) list; 
    } else { 
      Function<Object, T> converter; 
      if (elem instanceof List) { 
        converter = object -> (T) new JsonArray((List) object); 
      } else { 
        converter = object -> (T) new JsonObject((Map) object); 
      } 
      return (List<T>) list.stream().map(converter).collect(Collectors.toList()); 
    } 
  }
  private <T> Set<T> convertSet(List list) {
    return new HashSet<T>(convertList(list));
  }
}