package com.mmoscovich.beanmap.utils;

import lombok.AllArgsConstructor;

import org.apache.commons.lang3.text.StrLookup;
import org.apache.commons.lang3.text.StrSubstitutor;

/**
 * Class that takes a String template and a data source (bean or map) and returns the processed String result.
 * <br>For more details, see {@link #replace(String, Object)}
 * 
 * @author Martin Moscovich
 *
 */
public class StringTemplate {
	
	/**
	 * Creates a String from a data source (bean or map) using the given template.
	 * <p>
	 * The tokens to be replaced can refer to simple or nested attributes and must be wrapped with <code>${...}</code>. 
	 * <br>Example: <code>${name}</code> or <code>${person.name}</code>
	 * </p>
	 * <p>
	 * If the attribute cannot be found on the source, an exception is thrown, 
	 * <b>except</b> the attribute is marked as optional with a '?'. 
	 * In that case, a default value (if specified) or nothing is inserted when the attribute is missing. 
	 * <br>Example: <code>${name?}</code> or <code>${name?Peter}</code>.
	 * </p>
	 * <p>Full Example:
	 * <ul>
	 * <li>template: <code>"Hello ${person.title?Mr.} ${person.name}"</code></li>
	 * <li>source: <code>{person: {name: John, age:20}, id: 3}</code></li>
	 * <li><b>Result:</b> <code>"Hello Mr. John"</code></li>
	 * </ul> 
	 * @param template
	 * @param source a bean or map with the data
	 * @return the processed String
	 * @throws IllegalArgumentException if the template is incorrect.
	 * @throws MissingAttributeException if an attribute cannot be found on the source and it is not optional
	 */
	public static String replace(String template, Object source) throws IllegalArgumentException, MissingAttributeException {
		StrSubstitutor replacer = new StrSubstitutor(new HierarchicalStrLookup(source));
		return replacer.replace(template);
	}
	
	@AllArgsConstructor
	private static class HierarchicalStrLookup extends StrLookup<String> {
		
		private Object source;

		@Override
		public String lookup(String key) {
			if(key == null || key.isEmpty()) throw new IllegalArgumentException("The templates contain an empty key");
			
			Object result = EnhancedObjectNavigator.findValue(source, key);
			
			if(result == null) return "";
			
			return result.toString();
	    	
//			boolean isOptional = key.contains("?");
//			String[] parts = key.split("\\?");
//			
//			if(source != null && (parts[0].equals("ENTITY") || parts[0].equals("="))) {
//				if(source instanceof GroupDatasource) source = ((GroupDatasource)source).getDefault();
//
//				return source.toString();
//			}
//						
//			Object value = ObjectNavigator.findValue(source, parts[0]);
//	    	if(value == null) {
//	    		if(!isOptional) throw new MissingAttributeException(key);
//	    		return (parts.length > 1)?parts[1]:"";
//	    	}
//	    	return value.toString();
		}
	}

}
