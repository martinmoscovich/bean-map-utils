package com.mmoscovich.beanmap.utils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Subclass of {@link ObjectTransformer} that allows the use of a JSON input String instead of an Object.
 * <br>This class will take care of transforming the input to object. 
 * 
 * @author Martin Moscovich
 *
 */
public class JsonTransformer extends ObjectTransformer {
	
	private static ObjectMapper mapper = new ObjectMapper();
	
	/**
	 * Creates an JSON string output containing only the specified fields from the JSON string input.
	 * <p>Simple and nested attributes can be used (eg. "person" or "person.name")</p>
	 * 
	 * @param fieldNames the list of field names (can be nested)
	 * @param json the payload to use as input. It must be a parseable (aka valid) JSON String. 
	 * @return The result JSON String after applying the template transformation to the input.
	 * @throws ObjectTransformerException if there is any other error while processing (eg. a required field not found on the data source)
	 */
	public static String selectFields(String json, List<String> fieldNames) throws ObjectTransformerException {
		Object result;
		try {
			result = selectFields(mapper.readValue(json, Object.class), fieldNames);
		} catch (IOException e) {
			throw new InvalidInputException("The input could not be parsed", e);
		}
		
		try {
			return mapper.writeValueAsString(result);
		} catch (JsonProcessingException e) {
			throw new ObjectTransformerException("There was an internal problem while creating the JSON String", e);
		}
	}
	
	/**
	 * Transforms the JSON String input using the given template and returns the result as a JSON String
	 * 
	 * @param template JSON Template used to apply the transformation
	 * @param json the payload to use as input. It must be a parseable (aka valid) JSON String. 
	 * @return The result (as a JSON String) of applying the template transformation to the input 
	 * @throws InvalidTemplateException if the template String is empty, null, contains no properties or it is not a valid JSON.
	 * @throws InvalidInputException if the input String cannot be parsed as JSON.
	 * @throws ObjectTransformerException if there is any other error while processing (eg. a required field not found on the data source)
	 */
	public static String transform(String template, String json) throws InvalidTemplateException, InvalidInputException, ObjectTransformerException {
		try {
			return transform(template, mapper.readValue(json, Object.class));
		} catch (IOException e) {
			throw new InvalidInputException("The input could not be parsed", e);
		}
	}
	
	/**
	 * Transforms the input object (collection, bean or map) using the given template and returns the result as a JSON String
	 * 
	 * @param template JSON Template used to apply the transformation
	 * @param input the payload to use as input. It can be any kind of object (beans, maps, collections). 
	 * @return The result (as a JSON String) of applying the template transformation to the input 
	 * @throws InvalidTemplateException if the template String is empty, null, contains no properties or it is not a valid JSON.
	 * @throws ObjectTransformerException if there is any other error while processing (eg. a required field not found on the data source)
	 */
	public static String transform(String template, Object input) throws InvalidTemplateException, ObjectTransformerException {
		try {
			return mapper.writeValueAsString(transformToObject(template, input));
		} catch (JsonProcessingException e) {
			throw new ObjectTransformerException("There was an internal problem while creating the JSON String", e);
		}
	}
	
	/**
	 * Transforms the JSON String input using the given template and returns the result,
	 * which may be a Map or a List (depending on the input).
	 * 
	 * @param template JSON Template used to apply the transformation.
	 * @param json the payload to use as input. It must be a parseable (aka valid) JSON String. 
	 * @return The result object (Map or List) of applying the template transformation to the input.
	 * @throws InvalidTemplateException if the template String is empty, null, contains no properties or it is not a valid JSON.
	 * @throws InvalidInputException if the input String cannot be parsed as JSON.
	 * @throws ObjectTransformerException if there is any other error while processing (eg. a required field not found on the data source)
	 */
	public static Object transformToObject(String template, String json) throws InvalidTemplateException, InvalidInputException, ObjectTransformerException {
		try {
			return transformToObject(template, mapper.readValue(json, Object.class));
		} catch (IOException e) {
			throw new InvalidInputException("The input could not be parsed", e);
		}
	}
	
	/**
	 * Transforms the input object (collection, bean or map) using the given template and returns the result,
	 * which may be a Map or a List (depending on the input).
	 * <p>The template must be a valid JSON String and it will be parsed.</p>
	 * 
	 * @param template JSON Template used to apply the transformation.
	 * @param input the payload to use as input. It can be any kind of object (beans, maps, collections).  
	 * @return The result object (Map or List) of applying the template transformation to the input.
	 * @throws InvalidTemplateException if the template String is empty, null, contains no properties or it is not a valid JSON.
	 * @throws ObjectTransformerException if there is any other error while processing (eg. a required field not found on the data source)
	 */
	@SuppressWarnings("unchecked")
	public static Object transformToObject(String template, Object input) throws InvalidTemplateException, ObjectTransformerException {
		if(StringUtils.isEmpty(template)) throw new InvalidTemplateException("The template cannot be null");

		try {
			Map<String, Object> json = mapper.readValue(template, Map.class);
			return transform(json, input);
			
		} catch (IOException e) {
			// Error while parsing the template
			throw new InvalidTemplateException("The template could not be parsed as a Map", e);
		}
	}

}
