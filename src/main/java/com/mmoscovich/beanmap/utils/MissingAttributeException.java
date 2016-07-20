package com.mmoscovich.beanmap.utils;

import lombok.Getter;

public class MissingAttributeException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	@Getter
	private String attributeName;
	
	public MissingAttributeException(String attributeName) {
		super("The attribute '" + attributeName + "' was not found on the data source");
		this.attributeName = attributeName;
	}
}
