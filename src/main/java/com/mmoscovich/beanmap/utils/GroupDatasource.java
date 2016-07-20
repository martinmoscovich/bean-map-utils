package com.mmoscovich.beanmap.utils;

import java.util.HashMap;
import java.util.Map;

import lombok.ToString;

@ToString
public class GroupDatasource {
	private final Object defaultSource;
	private final Map<String, Object> sources = new HashMap<String, Object>();

	public GroupDatasource(Object defaultDS) {
		this.defaultSource = defaultDS;
	}
	
	public void add(String groupName, Object source) {
		sources.put(groupName, source);
	}
	
	public Object getDefault() {
		return this.defaultSource;
	}
	
	public Object get(String key) {
		Object source = sources.get(key);
		if(source != null) return source;
		
		return ObjectNavigator.getSimpleFieldValue(this.defaultSource, key);
	}
	
}
