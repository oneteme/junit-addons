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
class ObjectMapperConvertorTest {

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
		assertThrowsWithMessage(UnsupportedOperationException.class, "unsupported type : java.lang.Object", 
				()-> initJsonParser("defaultMapper").convert(new Object(), String[].class));
	}

	@Test
	void testConvert_badContent() {
		var file = asserts.resolve("temp").toFile();
		assertThrowsWithMessage(ArgumentConversionException.class, 
				"error while reading file : " + file, 
				()-> initJsonParser("defaultMapper").convert(file, String[].class));
	}

	@Test
	void testDefinedMapper() {
		assertNotNull(initJsonParser("defaultMapper").definedMapper());
	}
	
	@Test
	void testDefinedMapper_badMethodReturn() {
		assertThrowsWithMessage(IllegalArgumentException.class, 
				"ObjectMapperConvertorTest.badMethodReturn method must return an instance of ObjectMapper", 
				initJsonParser("badMethodReturn")::definedMapper);
	}

	@Test
	void testDefinedMapper_invokeException() {
		assertThrowsWithMessage(ResourceAccessException.class, 
				"ObjectMapperConvertorTest.invokeException method invoke throws exception", 
				initJsonParser("invokeException")::definedMapper);
	}
	
	@Test
	void testDefinedMapper_missingMethod() {
		assertThrowsWithMessage(NoSuchElementException.class, 
				"ObjectMapperConvertorTest.missingMethod method not found", 
				initJsonParser("missingMethod")::definedMapper);
	}
	
	@Test
	void testDefinedMapper_privateMethod() {
		assertThrowsWithMessage(ResourceAccessException.class, 
				"ObjectMapperConvertorTest.privateMethod method is not accessibe", 
				initJsonParser("privateMethod")::definedMapper);
	}
	
	void defaultMapper(@ConvertWithObjectMapper String[] arr){ }
	
	void badMethodReturn(@ConvertWithObjectMapper(clazz = ObjectMapperConvertorTest.class, method = "badMethodReturn") String[] arr){ }

	void invokeException(@ConvertWithObjectMapper(clazz = ObjectMapperConvertorTest.class, method = "invokeException") String[] arr){ }

	void missingMethod(@ConvertWithObjectMapper(clazz = ObjectMapperConvertorTest.class, method = "missingMethod") String[] arr){ }

	void privateMethod(@ConvertWithObjectMapper(clazz = ObjectMapperConvertorTest.class, method = "privateMethod") String[] arr){ }

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

	static ObjectMapperConvertor initJsonParser(String method) {
		var jp = new ObjectMapperConvertor();
		jp.accept(methodParameterAnnotation(ObjectMapperConvertorTest.class, method, ConvertWithObjectMapper.class, String[].class));
		return jp;
	}
}
