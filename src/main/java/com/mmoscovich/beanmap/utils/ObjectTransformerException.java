package com.mmoscovich.beanmap.utils;

/**
 * General Exception thrown by {@link ObjectTransformer}
 * 
 * @author Martin Moscovich
 *
 */
public class ObjectTransformerException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public ObjectTransformerException(String message) {
		super(message);
	}
	
	public ObjectTransformerException(String message, Throwable e) {
		super(message, e);
	}
	
	public ObjectTransformerException(Throwable e) {
		super("There was an error while using the template", e);
	}
}
