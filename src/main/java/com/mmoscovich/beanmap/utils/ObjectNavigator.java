package com.mmoscovich.beanmap.utils;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.ClassUtils;

/**
 * Class that allows to navigate an object's properties.
 * <p>
 * It works with both POJOs and Maps. 
 * <ul>
 * <li>When passed a POJO, it uses reflection to access the actual properties.</li>
 * <li>When passed a Map, it uses the entries' keys instead of the actual properties.</li>
 * </ul>
 * </p>
 * 
 * @author Martin Moscovich
 *
 */
public class ObjectNavigator {
	
	/**
	 * Find the specified attribute in the source.
	 * <p>The path can be simple or nested using the dot notation (eg. <code>person</code> or <code>person.address.street</code>)
	 * <p>
	 * It works with both POJOs and Maps. 
	 * <ul>
	 * <li>When passed a POJO, it uses reflection to access the actual properties.</li>
	 * <li>When passed a Map, it uses the entries' keys instead of the actual properties.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param src the object to retrieve the data from (POJO or Map)
	 * @param path the path to the property
	 * @return the value of the property or <code>null</code> if the property is not found or the source is <code>null</code>. 
	 * @throws IllegalArgumentException if the path is <code>null</code> or empty.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T findValue(Object src, String path) throws IllegalArgumentException {
		if(src == null) return null;
		if(path == null || path.isEmpty()) throw new IllegalArgumentException("The path cannot be null or empty");
		return (T) findValueRecursive(path, src);
	}
	
	/**
	 * Retrieves the value of a single-level property (nested not allowed) from the object. 
	 * <p>
	 * It works with both POJOs and Maps. 
	 * <ul>
	 * <li>When passed a POJO, it uses reflection to access the actual properties.</li>
	 * <li>When passed a Map, it uses the entries' keys instead of the actual properties.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param src the object to retrieve the data from (POJO or Map)
	 * @param fieldName name of the property (nested not allowed)
	 * @return the value
	 */
	@SuppressWarnings("unchecked")
	public static Object getSimpleFieldValue(Object src, String fieldName) {
    	if(src instanceof Map) return ((Map<Object,Object>)src).get(fieldName);
    	if(src instanceof GroupDatasource) return ((GroupDatasource)src).get(fieldName);
    	
    	try {
    		return PropertyUtils.getProperty(src, fieldName);
		} catch (Exception e) {
			return null;
		}
    }
	
	/**
	 * Retrieves all the available properties from an object.
	 * <p>
	 * It works with both POJOs and Maps. 
	 * <ul>
	 * <li>When passed a POJO, it uses reflection to access the actual properties.</li>
	 * <li>When passed a Map, it uses the entries' keys instead of the actual properties.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param o the object to retrieve the properties from (POJO or Map)
	 * @return the list of property names.
	 */
	@SuppressWarnings("unchecked")
	public static Set<String> getProperties(Object o) {
		if(o instanceof Map) return ((Map<String,Object>)o).keySet();
		
		return Arrays.stream(PropertyUtils.getPropertyDescriptors(o)).map(p -> p.getName()).collect(Collectors.toSet());
	}
	
	private static Object findValueRecursive(String key, Object parent) {
		String[] keys = key.split("\\.");
		
		Object value = getSimpleFieldValue(parent, keys[0]);
		if(keys.length == 1 || value == null) return value;
					
		if(ClassUtils.isPrimitiveOrWrapper(value.getClass())) return null;
		
		String[] newKeys = Arrays.copyOfRange(keys, 1, keys.length);
		return findValueRecursive(String.join(".",newKeys), value);
	}
}
