package org.usf.junit.addons;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.usf.junit.addons.AssertExt.assertThrowsWithMessage;
import static org.usf.junit.addons.FolderArgumentsProvider.typeResolver;
import static org.usf.junit.addons.Utils.methodParameterAnnotation;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.provider.ValueSource;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author u$f
 *
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
class JsonParserTest {

	private static Path asserts;

	@BeforeAll
	public static void setup() throws URISyntaxException {
		asserts = Path.of(FolderSourceTest.class.getResource("assets").toURI());
	}

	@ParameterizedTest
	@ValueSource(classes = {File.class, URI.class, Path.class, InputStream.class, String.class, byte[].class})
	void testConvert(Class<?> clazz) {
		var res = typeResolver(clazz).apply(asserts.resolve("JsonFile.json").toFile()); //FolderArgumentsProvider usage 
		assertArrayEquals(new String[] {"JSON"}, initJsonParser("defaultMapper").convert(res, String[].class));
	}
	
	@Test
	void testConvert_null() {
		assertNull(initJsonParser("defaultMapper").convert(null, String[].class));
	}
	
	@Test
	void testConvert_unsuported() {
		assertThrowsWithMessage("unsupported type : java.lang.Object", 
				UnsupportedOperationException.class, ()-> initJsonParser("defaultMapper").convert(new Object(), String[].class));
	}

	@Test
	void testConvert_badContent() {
		var file = asserts.resolve("temp").toFile();
		assertThrowsWithMessage("error while reading file : " + file, 
				ArgumentConversionException.class, ()-> initJsonParser("defaultMapper").convert(file, String[].class));
	}

	@Test
	void testDefinedMapper() {
		assertNotNull(initJsonParser("defaultMapper").definedMapper());
	}
	
	@Test
	void testDefinedMapper_badMethodReturn() {
		assertThrowsWithMessage("JsonParserTest.badMethodReturn method must return an instance of ObjectMapper", 
				IllegalArgumentException.class, initJsonParser("badMethodReturn")::definedMapper);
	}

	@Test
	void testDefinedMapper_invokeException() {
		assertThrowsWithMessage("JsonParserTest.invokeException method invoke throws exception", 
				ResourceAccesException.class, initJsonParser("invokeException")::definedMapper);
	}
	
	@Test
	void testDefinedMapper_missingMethod() {
		assertThrowsWithMessage("JsonParserTest.missingMethod method not found", 
				NoSuchElementException.class, initJsonParser("missingMethod")::definedMapper);
	}
	
	@Test
	void testDefinedMapper_privateMethod() {
		assertThrowsWithMessage("JsonParserTest.privateMethod method is not accessibe", 
				ResourceAccesException.class, initJsonParser("privateMethod")::definedMapper);
	}
	
	void defaultMapper(@ConvertWithJsonParser String[] arr){ }
	
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

	static JsonParser initJsonParser(String method) {
		var jp = new JsonParser();
		jp.accept(methodParameterAnnotation(JsonParserTest.class, method, ConvertWithJsonParser.class, String[].class));
		return jp;
	}
}
