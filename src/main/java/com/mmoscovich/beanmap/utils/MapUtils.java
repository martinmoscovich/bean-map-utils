package com.mmoscovich.beanmap.utils;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ClassUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Useful functions to work with {@link Map}s.
 * 
 * @author Martin Moscovich
 *
 */
public class MapUtils {
	
	private static ObjectMapper mapper = new ObjectMapper();
	
	/**
	 * Builds a Properties from a Map (the value is converted to String using  {@link #toString()}).
	 * 
	 * @param map map to convert
	 * @return the properties object
	 */
	public static Properties mapToProperties(Map<String, Object> map) {
		Map<String, Object> result = flattenMap(map);
		Properties p = new Properties();
		for(Entry<String, Object> entry : result.entrySet()) {
			p.setProperty(entry.getKey(), entry.getValue().toString());
		}
		return p;
	}
	
	/**
	 * Builds a nested map from a bean.
	 * <p>Each entry of the map will correspond to one of the bean's properties, using the property name as key and its value as value.
	 * <br>If the value is another complex bean, a nested Map will be created for that entry's value.</p>
	 *  
	 * 
	 * @param bean the bean to convert
	 * @return a nested Map that represents the bean
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> beanToMap(Object bean) {
		return mapper.convertValue(bean, Map.class);
	}
	
	/**
	 * Builds a flat map from a bean.
	 * <p>Each entry of the map will correspond to one of the bean's properties, using the property name as key and its value as value.
	 * <br>If the value is another complex bean, each of the sub-bean's properties will be mapped to one entry of the same map, 
	 * but the key will have the root's property name and the sub-bean property name, separated with a dot, similar to a properties file</p>
	 * For example, if the original bean has a property name person and that property is a bean with two attributes, name and age, the map will
	 * contain two keys: "person.name" and "person.age"
	 * 
	 * @param bean the bean to convert
	 * @return a flat map that represents the bean.
	 */
	public static Map<String, Object> beanToFlatMap(Object bean) {
		return flattenMap(beanToMap(bean), "", true);
	}
	
	/**
	 * Creates a flat map from a nested map, using the dot notation for nested keys (similar to properties file)
	 * <p>The simple entries will be copied as is to the new map.
	 * <br>If the value of an entry is another map, however, each of the submap's entries will be mapped to one entry of the root map, 
	 * but the key will have the root's property name and the submap's entry key, separated with a dot, similar to a properties file</p>
	 * For example, if the original map has a entry key "person" and the value is a map bean with two entries, name and age, the map will
	 * contain two keys: "person.name" and "person.age".
	 * <p>If the value is a complex bean, it will be used as value in the new map without any conversion</p>
	 * 
	 * @param nestedMap the complex nested map to flatten
	 * @return the flat map
	 */
	public static Map<String, Object> flattenMap(Map<String, Object> nestedMap) {
		return flattenMap(nestedMap, false);
	}
	
	/**
	 * Creates a flat map from a nested map, using the dot notation for nested keys (similar to properties file)
	 * <p>The simple entries will be copied as is to the new map.
	 * <br>If the value of an entry is another map, however, each of the submap's entries will be mapped to one entry of the root map, 
	 * but the key will have the root's property name and the submap's entry key, separated with a dot, similar to a properties file</p>
	 * For example, if the original map has a entry key "person" and the value is a map bean with two entries, name and age, the map will
	 * contain two keys: "person.name" and "person.age".
	 * <p>If the value is a complex bean, its properties will be converted to flat entries only if the parameter <code>covertBeans</code> is set.</p>
	 * 
	 * @param nestedMap the complex nested map to flatten
	 * @param convertBeans if <code>true</code>, the complex beans will be also flatten as map entries. Otherwise, the complex bean will be 
	 * inserted in the new map as is
	 * @return the flat map.
	 */
	public static Map<String, Object> flattenMap(Map<String, Object> nestedMap, boolean convertBeans) {
		return flattenMap(nestedMap, "", convertBeans);
	}
		
	@SuppressWarnings("unchecked")
	private static Map<String, Object> flattenMap(Map<String, Object> map, String prefix, boolean convertBeans) {
		Map<String, Object> result = new HashMap<>();
		if(prefix == null) prefix = "";
		for(Entry<String, Object> entry : map.entrySet()) {
			String key = prefix + (prefix.isEmpty()?"":".") + entry.getKey();
			if(entry.getValue() instanceof Map) {
				result.putAll(flattenMap((Map<String, Object>) entry.getValue(), key, convertBeans));
			} else if(convertBeans && isComplex(entry.getValue())) {
				result.putAll(flattenMap(beanToMap(entry.getValue()), key, convertBeans));
			} else {
				result.put(key, entry.getValue());
			}
		}
		return result;
	}
	
	/**
	 * Creates a Map where each entry has one item of the passed collection as value and the key is built using the specified function. 
	 * 
	 * @param collection the list of items to use as values
	 * @param keyBuilderFn a function that receives an item and returns the key it must be associated with
	 * @return the map
	 */
	public static <K, V>  Map<K, V> collectionToMapValues(Collection<V> collection, Function<? super V, ? extends K> keyBuilderFn) {
		return collection.stream().collect(Collectors.toMap(keyBuilderFn, Function.identity()));
	}
	
	/**
	 * Creates a Map where each entry has one item of the passed collection as key and the value is built using the specified function. 
	 * 
	 * @param collection the list of items to use as keys
	 * @param keyBuilderFn a function that receives an item and returns the value it must be associated with
	 * @return the map
	 */
	public static <K, V>  Map<K, V> collectionToMapKeys(Collection<K> collection, Function<? super K, ? extends V> valueBuilderFn) {
		return collection.stream().collect(Collectors.toMap(Function.identity(), valueBuilderFn));
	}
	
	/**
	 * Creates a Map where each entry has one item of the passed list as value and the key is built using the specified function.
	 * <br>The Map will respect the list's order. 
	 * 
	 * @param list the list of items to use as values
	 * @param keyBuilderFn a function that receives an item and returns the key it must be associated with
	 * @return the map
	 */
	public static <K, V>  Map<K, V> listToMapValues(List<V> list, Function<? super V, ? extends K> keyBuilderFn) {
		return list.stream().collect(Collectors.toMap(keyBuilderFn, Function.identity(), throwingMerger(), LinkedHashMap::new));
	}
	
	/**
	 * Creates a Map where each entry has one item of the passed collection as key and the value is built using the specified function. 
	 * <br>The Map will respect the list's order. 
	 * 
	 * @param list the list of items to use as keys
	 * @param keyBuilderFn a function that receives an item and returns the value it must be associated with
	 * @return the map
	 */
	public static <K, V>  Map<K, V> listToMapKeys(List<K> list, Function<? super K, ? extends V> valueBuilderFn) {
		return list.stream().collect(Collectors.toMap(Function.identity(), valueBuilderFn, throwingMerger(), LinkedHashMap::new));
	}
	
	/**
	 * Filters the specified map using the filterFunction and returns a map containing only the entries that match.
	 * 
	 * @param original the original map
	 * @param filterFunction function that receives an entry and returns a <code>boolean</code> indicating if it should be included
	 * in the output
	 * @return the filtered map
	 */
	public static <K, V> Map<K, V> filter(Map<K, V> original, Predicate<? super Entry<K, V>> filterFunction) {
		//return original.entrySet().stream().filter(filterFunction).collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
		return filter(original, filterFunction, false);
	}
	
	/**
	 * Filters the specified map using the filterFunction and returns a map containing only the entries that match.
	 * 
	 * @param original the original map
	 * @param filterFunction function that receives an entry and returns a <code>boolean</code> indicating if it should be included
	 * @param keepOrder specifies whether the new map should respect the original's order or no.
	 * in the output
	 * @return the filtered map
	 */
	public static <K, V> Map<K, V> filter(Map<K, V> original, Predicate<? super Entry<K, V>> filterFunction, boolean keepOrder) {
		//return original.entrySet().stream().filter(filterFunction).collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
		Supplier<? extends Map<K, V>> mapSupplier = (keepOrder?LinkedHashMap::new:HashMap::new);
		
		return original.entrySet().stream().filter(filterFunction).collect(entryCollector(mapSupplier));
	}
	
	/**
	 * Takes a map and creates a new one by transforming each entry using the provided keymapper and valuemapper
	 * 
	 * @param original the original map
	 * @param keyMapper the function that maps each key to the new key
	 * @param valueMapper the function that maps each value to the new value
	 * @return the transformed map
	 */
	public static <K,V,K2,V2> Map<K2,V2> transformMap(Map<K, V> original, Function<? super K, ? extends K2> keyMapper, Function<? super V, ? extends V2> valueMapper) {
		return original.entrySet().stream().collect(Collectors.toMap(entry -> keyMapper.apply(entry.getKey()), entry -> valueMapper.apply(entry.getValue())));
	}
	
	/**
	 * Takes a map and creates a new one by transforming the value of each entry using the provided valuemapper.
	 * <br>The keys are not changed.
	 * 
	 * @param original the original map
	 * @param valueMapper the function that maps each value to the new value
	 * @return the transformed map
	 */
	public static <K,V,V2> Map<K,V2> transformMapValues(Map<K, V> original, Function<? super V, ? extends V2> valueMapper) {
		return transformMap(original, Function.identity(), valueMapper);
	}
	
	/**
	 * Takes a map and creates a new one by transforming the key of each entry using the provided keymapper.
	 * <br>The values are not changed.
	 * 
	 * @param original the original map
	 * @param keyMapper the function that maps each key to the new key
	 * @return the transformed map
	 */
	public static <K,V,K2> Map<K2,V> transformMapKeys(Map<K, V> original, Function<? super K, ? extends K2> keyMapper) {
		return transformMap(original, keyMapper, Function.identity());
	}
	
	/**
	 * Shortcut method to create a {@link HashMap} from a stream of entries without any conversion.
	 * 
	 * @return A collector that builds a map using the streams entries without any conversion.
	 */
	public static <K,V> Collector<? super Entry<K, V>, ?, Map<K, V>> entryCollector() {
		return entryCollector(HashMap::new);
    }
	
	/**
	 * Shortcut method to create a Map from a stream of entries without any conversion.
	 * <br>The type of map is defined by the mapSupplier.
	 * 
	 * @param mapSupplier the supplier that creates an instance of {@link Map}.
	 * @return A collector that builds a map using the streams entries without any conversion.
	 */
	private static <K,V, M extends Map<K, V>> Collector<? super Entry<K, V>, ?, M> entryCollector(Supplier<M> mapSupplier) {
		return Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue(), throwingMerger(), mapSupplier);
    }
	
	private static <T> BinaryOperator<T> throwingMerger() {
        return (u,v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); };
    }
	
	private static boolean isComplex(Object o) {
		return (o != null && !(o instanceof String) && !(o instanceof Date) && !ClassUtils.isPrimitiveOrWrapper(o.getClass()));
	}
}
