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

import static java.time.format.DateTimeFormatter.ISO_INSTANT;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * TODO: {@linkplain io.vertx.core.json.Json}'s mapper miss serialization | deserialization for EventBus Proxy value. so we extend Json
 * 
 * @author <a href="mailto:takeseem@gmail.com">杨浩</a>
 */
public class UtilJson {
	public static ObjectMapper mapper = new ObjectMapper();
	private static final Base64.Encoder base64Encoder = Base64.getEncoder();
	private static final Base64.Decoder base64Decoder = Base64.getDecoder();

	static {
		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		SimpleModule module = new SimpleModule();
		// custom types
		module.addSerializer(JsonObject.class, new JsonObjectSerializer());
		module.addDeserializer(JsonObject.class, new JsonObjectDeserializer());

		module.addSerializer(JsonArray.class, new JsonArraySerializer());
		module.addDeserializer(JsonArray.class, new JsonArrayDeserializer());

		// he have 2 extensions: RFC-7493
		module.addSerializer(Instant.class, new InstantSerializer());

		module.addSerializer(byte[].class, new ByteArraySerializer());
		module.addDeserializer(byte[].class, new ByteArrayDeserializer());

		mapper.registerModule(module);
	}

	public static <T> T convertValue(Object value, Type type) {
		return mapper.convertValue(value, mapper.constructType(type));
	}
	
	@SuppressWarnings("unchecked")
  public static JsonObject toJsonObject(Object value) {
    return new JsonObject(mapper.convertValue(value, Map.class));
  }

	private static class JsonObjectSerializer extends JsonSerializer<JsonObject> {
		@Override
		public void serialize(JsonObject value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
			jgen.writeObject(value.getMap());
		}
	}

	private static class JsonObjectDeserializer extends JsonDeserializer<JsonObject> {
		@SuppressWarnings("unchecked")
		@Override
		public JsonObject deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
			Map<String, Object> map = p.readValueAs(Map.class);
			return map == null ? null : new JsonObject(map);
		}
	}

	private static class JsonArraySerializer extends JsonSerializer<JsonArray> {
		@Override
		public void serialize(JsonArray value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
			jgen.writeObject(value.getList());
		}
	}

	private static class JsonArrayDeserializer extends JsonDeserializer<JsonArray> {
		@Override
		public JsonArray deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
			List<?> list = p.readValueAs(List.class);
			return list == null ? null : new JsonArray(list);
		}
	}

	private static class InstantSerializer extends JsonSerializer<Instant> {
		@Override
		public void serialize(Instant value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
			jgen.writeString(ISO_INSTANT.format(value));
		}
	}
	// private class InstantDeserializer extends JsonDeserializer<Instant> {
	// @Override
	// public Instant deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
	// p.readValueAs(String.class);
	// return null;
	// }
	// }

	private static class ByteArraySerializer extends JsonSerializer<byte[]> {
		@Override
		public void serialize(byte[] value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
			jgen.writeString(base64Encoder.encodeToString(value));
		}
	}

	private static class ByteArrayDeserializer extends JsonDeserializer<byte[]> {
		@Override
		public byte[] deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
			String value = p.readValueAs(String.class);
			return value == null ? null : base64Decoder.decode(value);
		}
	}
}
