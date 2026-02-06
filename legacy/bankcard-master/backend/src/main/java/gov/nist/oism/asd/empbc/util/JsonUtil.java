/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gov.nist.oism.asd.empbc.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.stream.JsonParsingException;

import static javax.json.JsonValue.ValueType.TRUE;

/**
 * Not recursive at the moment, i.e., one-level only
 */
public class JsonUtil {
    private static final Logger LOG = Logger.getLogger(JsonUtil.class.getSimpleName());
	
	/** Parse whole JSON stream */
	private static JsonObject streamToJsonObject(InputStream istream)
			throws IOException 
	{
            
		JsonObject jobj=null;
		
		try(var reader = Json.createReader(istream)) {
			jobj = reader.readObject();
		} catch (JsonParsingException pe) {
			LOG.log(Level.SEVERE, "Invalid JSON:", pe);
		} catch (JsonException je) {
			throw new IOException(je);
		}
		return jobj;
	}
	
	private static JsonObject stringToJsonObject(final String content)
			throws JsonException 
	{
		JsonObject jobj=null;
		
		try(var reader = Json.createReader(new StringReader(content))) {
			jobj = reader.readObject();
		} catch (JsonParsingException pe) {
			LOG.log(Level.SEVERE, "Invalid JSON:", pe);
		}
		
		return jobj;
	}
	
	private static Optional<List<?>> jsonArrayToList(JsonArray jarr) {
		if (jarr.isEmpty()) {
			return Optional.of(List.of());
		} 
		
		return switch (jarr.get(0).getValueType()) {
			case STRING ->
				Optional.of(jarr.getValuesAs(JsonString::getString));
			case NUMBER ->
				Optional.of(jarr.getValuesAs(JsonNumber::intValue));
			default ->
				Optional.empty();
		};
	}
	
	/**
	 * Creates an {@value modelClz} instance and populates it with the 
	 * content of the JSON {@value istream}.
	 * 
	 * Note: Does not support all value types
	 * 
	 * @see XMLUtil#read
	 */
	public static <T> T read(InputStream istream, Class<T> modelClz) 
			throws IOException, Exception
			
	{
		final JsonObject jobj = streamToJsonObject(istream);
		
		// Instantiate and populate model instance
		T rec = ReflectionUtil.createInstance(modelClz);
		
		jobj.forEach((name, jvalue) -> {
			// Ignore result = ignore unknown fields
			@SuppressWarnings("unused")
			boolean res = switch (jvalue.getValueType()) {
				case FALSE, TRUE -> ReflectionUtil.setInObject(rec, name,
						Boolean.class, jobj.getBoolean(name));
				case NUMBER -> ReflectionUtil.setInObject(rec, name,
						Integer.class, jobj.getInt(name));
				case STRING -> ReflectionUtil.setInObject(rec, name,
						String.class,jobj.getString(name));
				case ARRAY -> {
					var oList = jsonArrayToList(jobj.getJsonArray(name));

					if (oList.isPresent()) {
						// Cannot check list type (type erasure)
						yield ReflectionUtil.setInObject(rec, name,
							List.class, oList.get());
					}
					
					yield false;
				}
				default -> false;
			};
		});
		
		return rec;
	}
	
	/*
	 * Creates a map based on the content of the JSON {@value content}.
	 */
	public static Map<String, Object> stringToMap(final String content)
			throws  IOException
	{
		final JsonObject jobj = stringToJsonObject(content);

		final var map = new HashMap<String, Object>();
		
		jobj.forEach((name, jvalue) -> map.put(name, 
			switch (jvalue.getValueType()) {
				case FALSE, TRUE -> jobj.getBoolean(name);
				case NUMBER -> jobj.getInt(name);
				case STRING -> jobj.getString(name);
				case ARRAY ->
					jsonArrayToList(jobj.getJsonArray(name)).orElse(null);
				default -> null;
			})
		);
		
		return map;
	}
	
	/**
	 * Creates a JSON representation of {@value obj}.
	 * 
	 * Note: Only one level. String, boolean and int.
	 */
	public static <T> String objectToJson(T obj) {
		var job = Json.createObjectBuilder();
		
		ReflectionUtil.getFromObject(obj, String.class)
			.forEach(job::add);
		ReflectionUtil.getFromObject(obj, Integer.TYPE)
			.forEach(job::add);
		ReflectionUtil.getFromObject(obj, Boolean.TYPE)
			.forEach(job::add);
		
		final var strWriter = new StringWriter();
		
		final var jwriter = Json.createWriter(strWriter);
		jwriter.writeObject(job.build());
		jwriter.close();
		
		return strWriter.toString();
	}
}
