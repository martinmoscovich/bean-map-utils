package com.mmoscovich.beanmap.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Person {
	private long id;
	private String name;
	private int age;
	private Status status;
	
	private Address address;
	
	@AllArgsConstructor
	@NoArgsConstructor
	@Data
	public static class Address {
		private long id;
		private String street;
		
	}
	
	public static enum Status {
		OK,ERROR;
	}

}
