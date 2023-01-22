package org.usf.junit.addons;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;

import com.fasterxml.jackson.databind.ObjectMapper;

class JsonParserTest {

	@Test
	void testConvert() {
		var jp = new JsonParser();
		jp.accept(annotation("method1"));
		assertArrayEquals(new String[] {"JSON"}, (String[])jp.convert(jsonFile("Json-FILE.json"), context(parameter("method1"))));
	}

	@Test
	void testConvert_IOException() {
		var jp = new JsonParser();
		jp.accept(annotation("method1"));
		assertThrows(ArgumentConversionException.class, ()->
			jp.convert(jsonFile("TEXT_file.txt"), context(parameter("method1"))));
	}

	@Test
	void testDefinedMapper_default() {
		var jp = new JsonParser();
		jp.accept(annotation("method1"));
		assertDoesNotThrow(jp::definedMapper);
	}
	
	@Test
	void testDefinedMapper_IllegalArgumentException() {
		var jp = new JsonParser();
		jp.accept(annotation("method2"));
		assertThrows(IllegalArgumentException.class, jp::definedMapper);
	}

	@Test
	void testDefinedMapper_InvocationTargetException() {
		var jp = new JsonParser();
		jp.accept(annotation("method3"));
		assertThrows(IllegalArgumentException.class, jp::definedMapper);
	}
	
	@Test
	void testDefinedMapper_NoSuchMethodException() {
		var jp = new JsonParser();
		jp.accept(annotation("method4"));
		assertThrows(NoSuchElementException.class, jp::definedMapper);
	}
	
	@Test
	void testDefinedMapper_IllegalAccessException() {
		var jp = new JsonParser();
		jp.accept(annotation("method5"));
		assertThrows(ResourceAccesException.class, jp::definedMapper);
	}

	void method1(@ConvertWithJsonParser String[] arr){	}
	
	void method2(@ConvertWithJsonParser(clazz = JsonParserTest.class, method = "notMapper") String[] arr){ }

	void method3(@ConvertWithJsonParser(clazz = JsonParserTest.class, method = "throwException") String[] arr){ }

	void method4(@ConvertWithJsonParser(clazz = JsonParserTest.class, method = "noMethod") String[] arr){	}

	void method5(@ConvertWithJsonParser(clazz = JsonParserTest.class, method = "privateMapper") String[] arr){ }

	public static Object notMapper() {
		return null;
	}
	
	public static ObjectMapper throwException() {
		throw new RuntimeException();
	}
	
	private static ObjectMapper privateMapper() {
		return null;
	}
	
	File jsonFile(String filename) {
		try {
			return Path.of(getClass().getResource("single/case-1").toURI())
					.resolve(filename).toFile();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	ConvertWithJsonParser annotation(String method) {
		try {
			var m = getClass().getDeclaredMethod(method, String[].class);
			return m.getParameters()[0].getAnnotationsByType(ConvertWithJsonParser.class)[0];
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	Parameter parameter(String method) {
		try {
			return getClass().getDeclaredMethod(method, String[].class).getParameters()[0];
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	static ParameterContext context(Parameter p) {
		
		return new ParameterContext() {
			
			@Override
			public boolean isAnnotated(Class<? extends Annotation> annotationType) {return false; }
			
			@Override
			public Optional<Object> getTarget() {return Optional.empty(); }
			
			@Override
			public Parameter getParameter() { return p; }
			
			@Override
			public int getIndex() { return 0; }
			
			@Override
			public <A extends Annotation> List<A> findRepeatableAnnotations(Class<A> annotationType) { return null; }
			
			@Override
			public <A extends Annotation> Optional<A> findAnnotation(Class<A> annotationType) { return Optional.empty(); }
		};
	}
}
