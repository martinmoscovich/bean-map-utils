package com.mmoscovich.beanmap.utils;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.junit.Test;

@Slf4j
public class StringTemplateTest {
	
	@Test
	public void a() {
		Map<String, Object> m =  new HashMap<String, Object>();
		Person p = new Person();
		p.setId(2L);
		p.setStatus(Person.Status.ERROR);
		m.put("nombre", p);
		
		log.info(StringTemplate.replace("Probando ${=}!", m));
	}
	
	

}
