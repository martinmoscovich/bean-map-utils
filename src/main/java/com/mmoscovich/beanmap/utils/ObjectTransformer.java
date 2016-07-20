package com.mmoscovich.beanmap.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Object transformer/adapter.
 * <p>It takes an input object (list, bean or map) and applies a transformation using a JSON template.</p>
 * <p>It allows to adapt the format declaratively, using data instead of custom code.</p> 
 * 
 * @author Martin Moscovich
 *
 */
public class ObjectTransformer {
	
	private static final List<String> KEYWORDS = Arrays.asList("_root", "_include", "_exclude"); 
	
	@Data
	@AllArgsConstructor
	private static class Reference {
		private Object scope;
		private Object value; 
	}
	
	/**
	 * Creates an object containing only the specified fields from the input
	 * 
	 * @param fieldNames the list of field names (can be nested)
	 * @param input the payload to use as input. It can be any kind of object (beans, maps, collections). 
	 * @return The result object (Map or List) of applying the template transformation to the input.
	 * @throws ObjectTransformerException if there is any other error while processing (eg. a required field not found on the data source)
	 */
	public static Object selectFields(Object input, List<String> fieldNames) throws ObjectTransformerException {
		Map<String, Object> inclusionTemplate = new HashMap<>();
		inclusionTemplate.put("_include", fieldNames);
		
		return transform(inclusionTemplate, input);
	}
	
	/**
	 * Transforms the input object (collection, bean or map) using the given template Map and returns the result,
	 * which may be a Map or a List (depending on the input).
	 * <p>The template map must include one entry per property using the name as key and the wanted value.
	 * The value can be a literal, a reference to a property of the input, a template string ({@link StringTemplate} will be used)
	 * or a sub map a nested output is desired. The submap must follow the same rules.</p>
	 * 
	 * @param template the template Map used to apply the transformation.
	 * @param input the payload to use as input. It can be any kind of object (beans, maps, collections).  
	 * @return The result object (Map or List) of applying the template transformation to the input.
	 * @throws InvalidTemplateException if the template map is invalid (empty or null).
	 * @throws ObjectTransformerException if there is any other error while processing (eg. a required field not found on the data source)
	 */
	public static Object transform(Map<String, Object> templateMap, Object input) throws InvalidTemplateException, ObjectTransformerException {
		if(templateMap == null || templateMap.isEmpty()) throw new InvalidTemplateException("The template cannot be null or empty");

		try {
			return transformFromTemplate(templateMap, input, null);
			
		} catch(Exception e) {
			throw new ObjectTransformerException(e);
		}
	}
	
	/**
	 * Builds the output from the specified root template until the end (recursively).
	 * 
	 * @param template The root template
	 * @param input the input object for this level
	 * @return The result (Map or List) of the transformation for the level 
	 */
	private static Object transformFromTemplate(Map<String,Object> template, Object input, String attributeName) {
		
		if(template.containsKey("_root")) {
			// if the template contains a "_root", use the path to get the new input
			input = ObjectNavigator.findValue(input, template.get("_root").toString());
		} else if(attributeName != null) {
			// if there's an attribute name, use it as path to find the new input
			input = ObjectNavigator.findValue(input, attributeName);
		}
		// otherwise, use the current input. 
		
		final Map<String,Object> processedTemplate = preprocessTemplateLevel(template, input);
		
		// if the source is null, there's no data to extract
		if(input == null) return null;
		
		if(input instanceof Collection<?>) {
			// if the source is a collection, we template should be applied to each item 
			// and the transformed list must be returned
			return ((Collection<?>)input).stream().map(i -> processEntries(processedTemplate, i)).collect(Collectors.toList());
		} else {
			// if the source is not a collection, apply the template to the element and return the single result.
			return processEntries(processedTemplate, input);
		}
	}
	
	/**
	 * Process the template to handle special cases (include, exclude and nested fields).
	 * 
	 * @param template the template to process
	 * @param input the input object for this level
	 * @return the processed template 
	 */
	private static Map<String,Object> preprocessTemplateLevel(Map<String,Object> template, Object input) {
		template = preprocessInclusions(template, input);
		preprocessNestedNames(template, input);
		
		// Remove nested fields (with .) and return
		return MapUtils.filter(template, entry -> ( !entry.getKey().contains(".") && !KEYWORDS.contains(entry.getKey()) ), true );
	}
	
	/**
	 * Analyze the _include and _exclude fields and then add the appropiate fields to the template.  
	 * 
	 * @param template the template to process
	 * @param source the input object for this level
	 */
	private static Map<String,Object> preprocessInclusions(final Map<String,Object> template, Object source) {
		Collection<String> exclusions = buildFieldList(template, "exclude");
		Collection<String> inclusions = buildFieldList(template, "include");
		
		// If no inclusions are specified and there are exclusions, we add all the fields except the ones excluded
		if( inclusions.contains("_all") || (inclusions.isEmpty() && !exclusions.isEmpty()) ) {
			inclusions = ObjectNavigator.getProperties(source); 
		}
//		Collection<String> fieldsToAdd = CollectionUtils.subtract(inclusions, exclusions);
		
		Map<String,Object> newTemplate = new LinkedHashMap<>();
		for(String name : inclusions) {
			if(!exclusions.contains(name)) newTemplate.put(name, "=");
		}
		newTemplate.putAll(template);
		
		return newTemplate;
		
//		for(String name : fieldsToAdd) {
//			template.put(name, "=");
//		}
	}
	
	/**
	 * Processes the nested fields (names with ".").
	 * <p> Creates a new nested field named after the first level. The value will be a Map including the rest of the nested field name.</p> 
	 * <p> The value of the the nested field will be relative to this level's input, <b>NOT</b> to the nested level. The only exception is the keyword '=' which is
	 * translated to the exact value the field name represents. (If the template is <code>person.name: '='</code>, then the result will be two levels and the value will be the input's <code>person.name</code>)</p>
	 * <b>Example</b>: 
	 * <code><pre>
	 * { 
	 *   person.address.street: '=' 
	 * }
	 * </pre></code> 
	 * gets translated to 
	 *  <code><pre>
	 * { 
	 *    person: { 
	 *       address.street: '=' 
	 *    } 
	 * }
	 * </pre></code>
	 * When the next level is processed, the same logic is applied until no nested field names remain. ie: In the end, the template will be 
	 * <code><pre>
	 * { 
	 *    person: { 
	 *       address: { 
	 *          street: '=' 
	 *       }
	 *    }
	 * }
	 * </pre></code>
	 * @param template
	 * @param input
	 */
	@SuppressWarnings("unchecked")
	private static void preprocessNestedNames(final Map<String,Object> template, Object input) {
		
		// Get the list of nested field names
		Collection<String> nestedKeys = template.keySet().stream().filter(k -> k.contains(".")).collect(Collectors.toList());
		
		for(String key : nestedKeys) {
			String[] parts = key.split("\\.");
			if(parts.length > 1) {
				
				// Get the entry value
				Object value = template.get(key);
				
				// Get the first level attribute name: person.address.street -> person
				String baseKey = parts[0];
				
				// Get the rest of the nested field name: person.address.street -> address.street
				String subKey = key.replace(parts[0] + ".", "");
				
				// If there's already a field with that name (eg. person) in the template and it is a map, add the new property name to it
				Object nested = template.get(baseKey);
				if(nested == null || !(nested instanceof Map)) {
					// If the field doesnt exist, create a new Map
					// If the field exists but it isnt a Map, overwrite it with a new Map
					nested = new LinkedHashMap<String, Reference>();
					template.put(baseKey, nested);
				}
				
				// If the value is "=", the source must be the nested property, so set the scope to null (eg: use person.address.street as scope)
				// Otherwise, use this input as scope
				Object scope = ("=".equals(value))?null:input;
				
				// If the value is already a reference, it's been processed in a higher level, use that reference.
				// Otherwise, create a reference with the scope
				if(!(value instanceof Reference)) value = new Reference(scope, value);
				
				// Add the reference to the new map, using the subKey (address.street)
				((Map<String,Object>)nested).put(subKey, value);
			}
		}
	}
	
	
	/**
	 * Process all the properties of the template map from this level down (recursively).
	 * 
	 * @param level The root level
	 * @param source the input object for this level
	 * @return The result Map for this element
	 */
	private static Map<String, Object> processEntries(Map<String,Object> level, Object source) {
//		return level.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e->processEntry(e.getKey(), e.getValue(), source)));

		Map<String, Object> result = new LinkedHashMap<String, Object>();
		
		// process all the entries of this map
		for(Entry<String, Object> entry : level.entrySet()) {
			result.put(entry.getKey(), processEntry(entry.getKey(), entry.getValue(), source));
		}
		return result;
	}
	
	/**
	 * Build the list of field names specified in the passed special field
	 * 
	 * @param template the template where the keywords are
	 * @param keyword the name of the keyword to find
	 * @return the list of field names if found. Otherwise an empty list.
	 */
	private static List<String> buildFieldList(Map<String,Object> template, String keyword) {
		Object result = template.get("_" + keyword);
		
		// If the special field is not found, return an empty list
		if(result == null) return Collections.emptyList();
		
		// If the value is a string, return a list with that single item
		if(!(result instanceof Collection<?>)) result = Arrays.asList(result.toString());
	
		// Otherwise return the full list
		return ((Collection<?>)result).stream().map(o -> o.toString()).collect(Collectors.toList());
	}
	
	/**
	 * Process a particular property recursively (all the way down if it is complex).
	 * @param key name of the property
	 * @param value the template value
	 * @param input the input object to extract the data from
	 * @return The value of the property (primitive, complex, list, etc)
	 */
	@SuppressWarnings("unchecked")
	private static Object processEntry(String key, Object value, Object input) {
		// the KEYWORDS should not be processed
		if(KEYWORDS.contains(key)) return null;
		
		if(value instanceof String) {
			// the value is a String, it will be either a literal or the name of the property to extract from the input
			// We just need the actual value, no further transformation is required
			
			String sVal = (String)value;
			if(sVal.charAt(0) == '=') {
				// the value is a propery name or "=" (meaning the same name)
				// the actual value must be extracted from the input
				sVal = ("=".equals(value))? key: sVal.substring(1);
				
				return EnhancedObjectNavigator.findValue(input, sVal.toString());
				//return StringTemplate.replace("${" + sVal + "}", input);
			} else if(sVal.contains("${")){
				sVal = sVal.replace("${=}", "${" + key + "}").replace("${=?}", "${" + key + "?}");
				return StringTemplate.replace(sVal, input);
			} else {
				// the value is a literal, write it as is.
				return sVal; 
			}

		} else if(value instanceof Map) {
			// the value is a map, ie. a transformation must be apply to the input data
			Map<String, Object> mapValue = (Map<String, Object>) value;
			
			// call the original method recursively to apply the same logic from this property down.
			return transformFromTemplate(mapValue, input, key);
			
		} else if(value instanceof Reference) {
			// If the value is a reference, it's the end of a nested field name.
			Reference ref = (Reference)value;
			
			// If the reference scope is null, use the current (nested) source as scope (see line 228)
			// Otherwise, use the saved scope.
			Object scope = (ref.getScope() == null)?input:ref.getScope();
			
			// Process the entry using the reference value and the scope
			return processEntry(key, ref.getValue(), scope);
		}
	
		return value;
	}

}
