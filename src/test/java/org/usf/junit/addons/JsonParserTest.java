package org.usf.junit.addons;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.usf.junit.addons.AssertExt.assertThrowsWithMessage;

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

/**
 * 
 * @author u$f
 *
 */
class JsonParserTest {

	@Test
	void testConvert() {
		var jp = new JsonParser();
		jp.accept(argAnnotation("validMapper"));
		var obj = jp.convert(jsonFile("Json-FILE.json"), context(methodParameter("validMapper")));
		assertArrayEquals(new String[] {"JSON"}, (String[])obj);
	}

	@Test
	void testConvert_badContent() {
		var jp = new JsonParser();
		jp.accept(argAnnotation("validMapper"));
		var file = jsonFile("TEXT_file.txt");
		assertThrowsWithMessage("error while reading file " + file, 
				ArgumentConversionException.class, ()-> jp.convert(file, context(methodParameter("validMapper"))));
	}

	@Test
	void testDefinedMapper_ok() {
		var jp = new JsonParser();
		jp.accept(argAnnotation("validMapper"));
		assertDoesNotThrow(jp::definedMapper);
	}
	
	@Test
	void testDefinedMapper_badMethodReturn() {
		var jp = new JsonParser();
		jp.accept(argAnnotation("badMethodReturn"));
		assertThrowsWithMessage("JsonParserTest.badMethodReturn method must return an instance of ObjectMapper", 
				IllegalArgumentException.class, jp::definedMapper);
	}

	@Test
	void testDefinedMapper_invokeException() {
		var jp = new JsonParser();
		jp.accept(argAnnotation("invokeException"));
		assertThrowsWithMessage("JsonParserTest.invokeException method invoke throws exception", 
				ResourceAccesException.class, jp::definedMapper);
	}
	
	@Test
	void testDefinedMapper_missingMethod() {
		var jp = new JsonParser();
		jp.accept(argAnnotation("missingMethod"));
		assertThrowsWithMessage("JsonParserTest.missingMethod method not found", 
				NoSuchElementException.class, jp::definedMapper);
	}
	
	@Test
	void testDefinedMapper_privateMethod() {
		var jp = new JsonParser();
		jp.accept(argAnnotation("privateMethod"));
		assertThrowsWithMessage("JsonParserTest.privateMethod method is not accessibe", 
				ResourceAccesException.class, jp::definedMapper);
	}

	void validMapper(@ConvertWithJsonParser String[] arr){ }
	
	void badMethodReturn(@ConvertWithJsonParser(clazz = JsonParserTest.class, method = "badMethodReturn") String[] arr){ }

	void invokeException(@ConvertWithJsonParser(clazz = JsonParserTest.class, method = "invokeException") String[] arr){ }

	void missingMethod(@ConvertWithJsonParser(clazz = JsonParserTest.class, method = "missingMethod") String[] arr){ }

	void privateMethod(@ConvertWithJsonParser(clazz = JsonParserTest.class, method = "privateMethod") String[] arr){ }

	public static Object badMethodReturn() {
		return null;
	}
	
	public static ObjectMapper invokeException() {
		throw new RuntimeException();
	}
	
	@SuppressWarnings("unused")
	private static ObjectMapper privateMethod() {
		return null;
	}

	ConvertWithJsonParser argAnnotation(String method) {
		try {
			var m = getClass().getDeclaredMethod(method, String[].class);
			return m.getParameters()[0].getAnnotationsByType(ConvertWithJsonParser.class)[0];
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	Parameter methodParameter(String method) {
		try {
			return getClass().getDeclaredMethod(method, String[].class).getParameters()[0];
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	File jsonFile(String filename) {
		try {
			return Path.of(getClass().getResource("single/case-1").toURI())
					.resolve(filename).toFile();
		} catch (URISyntaxException e) {
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
