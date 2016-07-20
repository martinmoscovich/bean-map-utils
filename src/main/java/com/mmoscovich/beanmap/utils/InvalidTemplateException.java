package com.mmoscovich.beanmap.utils;

/**
 * Exception thrown by {@link ObjectTransformer} when the template specified is not valid.
 * 
 * @author Martin Moscovich
 *
 */
public class InvalidTemplateException extends ObjectTransformerException {

	private static final long serialVersionUID = 1L;

	public InvalidTemplateException(String message) {
		super(message);
	}

	public InvalidTemplateException(String message, Throwable e) {
		super(message, e);
	}

}
