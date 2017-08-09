package io.vertx.serviceproxy.clustered;

import com.jayway.awaitility.Awaitility;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceException;
import io.vertx.serviceproxy.ServiceExceptionMessageCodec;
import io.vertx.serviceproxy.testmodel.MyServiceException;
import io.vertx.serviceproxy.testmodel.MyServiceExceptionMessageCodec;
import io.vertx.serviceproxy.testmodel.SomeEnum;
import io.vertx.serviceproxy.testmodel.SomeVertxEnum;
import io.vertx.serviceproxy.testmodel.TestDataObject;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class ClusteredTest {

  AtomicReference<Vertx> providerNode = new AtomicReference<>();
  AtomicReference<Vertx> consumerNode = new AtomicReference<>();

  @Before
  public void setUp() {
    Vertx.clusteredVertx(new VertxOptions().setClustered(true).setClusterHost("127.0.0.1"), ar -> {
      Vertx vertx = ar.result();
      vertx.eventBus().registerDefaultCodec(MyServiceException.class,
          new MyServiceExceptionMessageCodec());
      providerNode.set(vertx);
      vertx.deployVerticle(ServiceProviderVerticle.class.getName());
    });

    Vertx.clusteredVertx(new VertxOptions().setClustered(true).setClusterHost("127.0.0.1"), ar -> {
      Vertx vertx = ar.result();
      vertx.eventBus().registerDefaultCodec(MyServiceException.class,
          new MyServiceExceptionMessageCodec());
      consumerNode.set(vertx);
    });

    Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> providerNode.get() != null);
    Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> consumerNode.get() != null);
  }


  @Test
  public void testHello() {
    AtomicReference<String> result = new AtomicReference<>();
    Service service = createProxy();
    service.hello("vert.x", ar -> {
      result.set(ar.result());
    });

    Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() ->
      "hello vert.x".equalsIgnoreCase(result.get()));
  }

  @Test
  public void testEnumAsParameter() {
    AtomicReference<Boolean> result = new AtomicReference<>();
    Service service = createProxy();
    service.methodUsingEnum(SomeEnum.WIBBLE, ar -> {
      result.set(ar.result());
    });

    Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> result.get() != null && result.get());
  }

  @Test
  public void testEnumAsResult() {
    AtomicReference<SomeEnum> result = new AtomicReference<>();
    Service service = createProxy();
    service.methodReturningEnum(ar -> {
      result.set(ar.result());
    });

    Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> result.get() == SomeEnum.WIBBLE);
  }

  @Test
  public void testVertxEnumAsResult() {
    AtomicReference<SomeVertxEnum> result = new AtomicReference<>();
    Service service = createProxy();
    service.methodReturningVertxEnum(ar -> {
      result.set(ar.result());
    });

    Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> result.get() == SomeVertxEnum.BAR);
  }


  @Test
  public void testWithDataObject() {
    AtomicReference<TestDataObject> result = new AtomicReference<>();
    Service service = createProxy();
    TestDataObject data = new TestDataObject().setBool(true).setNumber(25).setString("vert.x");
    service.methodWithDataObject(data, ar -> {
      result.set(ar.result());
    });

    Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> result.get() != null);
    TestDataObject out = result.get();
    assertThat(out.getNumber()).isEqualTo(25);
    assertThat(out.isBool()).isTrue();
    assertThat(out.getString()).isEqualTo("vert.x");
  }

  @Test
  public void testWithListOfDataObject() {
    AtomicReference<List<TestDataObject>> result = new AtomicReference<>();
    Service service = createProxy();
    TestDataObject data = new TestDataObject().setBool(true).setNumber(25).setString("vert.x");
    TestDataObject data2 = new TestDataObject().setBool(true).setNumber(26).setString("vert.x");
    service.methodWithListOfDataObject(Arrays.asList(data, data2), ar -> {
      if (ar.failed()) {
        ar.cause().printStackTrace();
      }
      result.set(ar.result());
    });

    Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> result.get() != null);
    List<TestDataObject> out = result.get();
    assertThat(out.get(0).getNumber()).isEqualTo(25);
    assertThat(out.get(0).isBool()).isTrue();
    assertThat(out.get(0).getString()).isEqualTo("vert.x");
    assertThat(out.get(1).getNumber()).isEqualTo(26);
    assertThat(out.get(1).isBool()).isTrue();
    assertThat(out.get(1).getString()).isEqualTo("vert.x");
  }

  @Test
  public void testWithListOfJsonObject() {
    AtomicReference<List<JsonObject>> result = new AtomicReference<>();
    Service service = createProxy();
    TestDataObject data = new TestDataObject().setBool(true).setNumber(25).setString("vert.x");
    TestDataObject data2 = new TestDataObject().setBool(true).setNumber(26).setString("vert.x");
    service.methodWithListOfJsonObject(Arrays.asList(data.toJson(), data2.toJson()), ar -> {
      if (ar.failed()) {
        ar.cause().printStackTrace();
      }
      result.set(ar.result());
    });

    Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> result.get() != null);
    List<JsonObject> out = result.get();

    TestDataObject out0 = new TestDataObject(out.get(0));
    TestDataObject out1 = new TestDataObject(out.get(1));
    assertThat(out0.getNumber()).isEqualTo(25);
    assertThat(out0.isBool()).isTrue();
    assertThat(out0.getString()).isEqualTo("vert.x");
    assertThat(out1.getNumber()).isEqualTo(26);
    assertThat(out1.isBool()).isTrue();
    assertThat(out1.getString()).isEqualTo("vert.x");
  }

  @Test
  public void testWithJsonObject() {
    AtomicReference<TestDataObject> result = new AtomicReference<>();
    Service service = createProxy();
    TestDataObject data = new TestDataObject().setBool(true).setNumber(25).setString("vert.x");
    service.methodWithJsonObject(data.toJson(), ar -> {
      result.set(new TestDataObject(ar.result()));
    });

    Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> result.get() != null);
    TestDataObject out = result.get();
    assertThat(out.getNumber()).isEqualTo(25);
    assertThat(out.isBool()).isTrue();
    assertThat(out.getString()).isEqualTo("vert.x");
  }

  @Test
  public void testWithJsonArray() {
    AtomicReference<JsonArray> result = new AtomicReference<>();
    Service service = createProxy();
    TestDataObject data = new TestDataObject().setBool(true).setNumber(25).setString("vert.x");
    JsonArray array = new JsonArray();
    array.add("vert.x").add(data.toJson());

    service.methodWithJsonArray(array, ar -> {
      result.set(ar.result());
    });

    Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> result.get() != null);
    TestDataObject out = new TestDataObject(result.get().getJsonObject(1));
    assertThat(array.getString(0)).isEqualToIgnoringCase("vert.x");
    assertThat(out.getNumber()).isEqualTo(25);
    assertThat(out.isBool()).isTrue();
    assertThat(out.getString()).isEqualTo("vert.x");
  }

  @Test
  public void testWithFailingResult() {
    AtomicReference<Throwable> result = new AtomicReference<>();
    Service service = createProxy();
    service.methodWthFailingResult("Fail", ar -> {
      assertThat(ar.cause() instanceof ServiceException).isTrue();
      assertThat(((ServiceException)ar.cause()).failureCode()).isEqualTo(30);
      assertThat(((ServiceException)ar.cause()).getDebugInfo()).isEqualTo(new JsonObject().put("test", "val"));
      result.set(ar.cause());
    });
    Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> result.get() != null);
  }

  @Test
  public void testWithFailingResultServiceExceptionSubclass() {
    AtomicReference<Throwable> result = new AtomicReference<>();
    Service service = createProxy();
    service.methodWthFailingResult("cluster Fail", ar -> {
      assertThat(ar.cause() instanceof MyServiceException).isTrue();
      assertThat(((MyServiceException)ar.cause()).failureCode()).isEqualTo(30);
      assertThat(((MyServiceException)ar.cause()).getExtra()).isEqualTo("some extra");
      result.set(ar.cause());
    });
    Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> result.get() != null);
  }
  
  protected Service createProxy() {
    return Service.createProxy(consumerNode.get(), "my.service");
  }
}
