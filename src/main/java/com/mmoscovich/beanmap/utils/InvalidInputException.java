package com.mmoscovich.beanmap.utils;

/**
 * Exception thrown by {@link ObjectTransformer} when the String input source is not a valid JSON and could not be parsed.
 * 
 * @author Martin Moscovich
 *
 */
public class InvalidInputException extends ObjectTransformerException {

	private static final long serialVersionUID = 1L;

	public InvalidInputException(String message, Throwable e) {
		super(message, e);
	}

}
