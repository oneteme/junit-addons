package org.usf.junit.addons;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.usf.junit.addons.AssertExt.assertThrowsWithMessage;
import static org.usf.junit.addons.Utils.methodParameter;
import static org.usf.junit.addons.Utils.methodParameterAnnotation;
import static org.usf.junit.addons.Utils.parameterContext;

import java.lang.reflect.Parameter;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.converter.ArgumentConversionException;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author u$f
 *
 */
class JsonParserTest {

	private static Path asserts;

	@BeforeAll
	public static void setup() throws URISyntaxException {
		asserts = Path.of(FolderSourceTest.class.getResource("assets").toURI());
	}

	@Test
	void testConvert() {
		var jp = new JsonParser();
		jp.accept(argAnnotation("validMapper"));
		var file = asserts.resolve("JsonFile.json").toFile();
		var obj = jp.convert(file, parameterContext(parameter("validMapper")));
		assertArrayEquals(new String[] {"JSON"}, (String[])obj);
	}

	@Test
	void testConvert_badContent() {
		var jp = new JsonParser();
		jp.accept(argAnnotation("validMapper"));
		var file = asserts.resolve("temp").toFile();
		assertThrowsWithMessage("error while reading file " + file, 
				ArgumentConversionException.class, ()-> jp.convert(file, parameterContext(parameter("validMapper"))));
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
		return methodParameterAnnotation(JsonParserTest.class, method, String[].class, ConvertWithJsonParser.class);
	}

	Parameter parameter(String method) {
		return methodParameter(JsonParserTest.class, method, String[].class);
	}
}
