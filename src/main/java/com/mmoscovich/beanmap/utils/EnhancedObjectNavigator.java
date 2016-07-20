package com.mmoscovich.beanmap.utils;

public class EnhancedObjectNavigator  {
	
	/**
	 * Find the specified attribute in the source by delegating to {@link ObjectNavigator#findValue(Object, String)}.
	 * <p>The difference is that is method adds two features:
	 * <ul>
	 * <li>If the path is "<code>ENTITY</code>" or "<code>=</code>", the source is returned.</li>
	 * <li>The path can be declared optional by ending it with "<code>?</code>". Instead of throwing an exception, if an optional attribute is not found,
	 * either the default value or <code>null</code> is returned.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param src the object to retrieve the data from (POJO or Map)
	 * @param path the path to the property
	 * @return the value of the property. If the attribute is not found and it's optional, then the default value is returned (or <code>null</code> if there is no default value). 
	 * @throws IllegalArgumentException if the path is <code>null</code> or empty.
	 * @throws MissingAttributeException if the attribute is not found and is not marked as optional.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T findValue(Object source, String path) throws IllegalArgumentException, MissingAttributeException {
		if(path == null || path.isEmpty()) throw new IllegalArgumentException("The path cannot be empty");
    	
		// Optional logic
		boolean isOptional = path.contains("?");
		String[] parts = path.split("\\?");
		
		if(source != null && (parts[0].equals("ENTITY") || parts[0].equals("="))) {
			if(source instanceof GroupDatasource) source = ((GroupDatasource)source).getDefault();

			return (T)source;
		}
					
		Object value = ObjectNavigator.findValue(source, parts[0]);
    	if(value == null) {
    		if(!isOptional) throw new MissingAttributeException(path);
    		return (T)((parts.length > 1)?parts[1]:null);
    	}
    	return (T)value;
	}
}
