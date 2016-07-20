package com.mmoscovich.beanmap.utils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import lombok.extern.slf4j.Slf4j;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
public class JsonTemplateTest {
	
	
	ObjectMapper mapper = new ObjectMapper();
	
	@Test
	public void a() throws IOException, URISyntaxException, InvalidTemplateException, InvalidInputException, ObjectTransformerException {
		String input = String.join("\n", Files.readAllLines(Paths.get(this.getClass().getResource("/json/input.json").toURI()), Charset.defaultCharset()));
		String template = String.join("", Files.readAllLines(Paths.get(this.getClass().getResource("/json/template.json").toURI()), Charset.defaultCharset()));
		String template2 = String.join("", Files.readAllLines(Paths.get(this.getClass().getResource("/json/template2.json").toURI()), Charset.defaultCharset()));
		
//		Object source = mapper.readValue(new File(this.getClass().getResource("/json/input.json").toURI()), Object.class);
		
		log.info("{}", JsonTransformer.transform(template2, input));
	}
	
	@Test
	public void b() throws IOException, URISyntaxException, InvalidTemplateException, InvalidInputException, ObjectTransformerException {
		String input = String.join("\n", Files.readAllLines(Paths.get(this.getClass().getResource("/json/input.json").toURI()), Charset.defaultCharset()));
		
//		Object source = mapper.readValue(new File(this.getClass().getResource("/json/input.json").toURI()), Object.class);
		
		log.info("{}", JsonTransformer.selectFields(input, Arrays.asList("respuesta.nombre", "respuesta.telefono", "respuesta.direccion.calle", "respuesta.contactos.nombre")));
		
	}

}
