package com.zephyr.migration.utils;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/**
 * <p>JSON utility class</p>
 * 
 * @author harsh
 *
 */
public class JsonUtil {
	
	private static final ObjectMapper mapper = new ObjectMapper();
	
	public static String toPrettyJsonString(Object obj) throws IOException {
		mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
        StringWriter sw = new StringWriter();
        mapper.writeValue(sw, obj);
        return sw.toString();
    }

	public static String mapToPrettyJsonString(Map<String, Object> map) throws IOException {		
		mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
		return mapper.writeValueAsString(map);
    }
	
	public static <T> T readValue(String content, TypeReference<T> reference) throws IOException {
		mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, false);
        return mapper.readValue(content,reference);
    }

    public static String writeValueAsString(Object object) throws IOException {
    	mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, false);
        return mapper.writeValueAsString(object);
    }

}
