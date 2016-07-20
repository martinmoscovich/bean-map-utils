package com.mmoscovich.beanmap.utils;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.junit.Test;

import com.mmoscovich.beanmap.utils.Person.Address;

@Slf4j
public class MapUtilsTest {
	
	@Test
	public void a() {
		Map<String, Object> m =  new HashMap<String, Object>();
		Map<String, Object> m2 =  new HashMap<String, Object>();
		Person p = new Person();
		p.setId(2L);
		Address address = new Address(5L, "Cool St");
		p.setStatus(Person.Status.OK);
		p.setAddress(address);
		m.put("property", 1);
		m.put("job", p);
		m2.put("map", m);
		m2.put("attribute", 2);
		
		log.info("{}",MapUtils.beanToMap(p));
		log.info("{}",MapUtils.beanToFlatMap(p));
		log.info("{}",MapUtils.flattenMap(m2));
		log.info("{}",MapUtils.flattenMap(m2, true));
	}

}
