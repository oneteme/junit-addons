package org.usf.junit.addons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;
import org.junit.jupiter.params.converter.ConvertWith;
import org.usf.junit.addons.FolderSource.FileMatchingMode;

class DemoTest {

	@ParameterizedTest
	@FolderSource(path = "single") //assert does not throw, existing sub (folder) + not existing dummy
	void testSmartNullArgInjection(File subFile, URI subUri, Path subPath, InputStream dummyStream, String dummyContent) {
		assertNull(subFile);
		assertNull(subUri);
		assertNull(subPath);
		assertNull(dummyStream);
		assertNull(dummyContent);
	}
	
	@ParameterizedTest
	@FolderSource(path = "single", pattern = "case-[0-9]") // multiple arguments + matching names
	void testSmartArgInjection(File jsonfile, URI textFileUri, Path xml_file, InputStream binaryarrstream, String json_file_content) {
		try {
			assertEquals("Json-FILE.json", jsonfile.getName());
			assertEquals("TEXT_file.txt", Paths.get(textFileUri).toFile().getName());
			assertEquals("xml.file.txt", xml_file.toFile().getName());
			assertNotNull(binaryarrstream);
			assertEquals("[\"JSON\"]", json_file_content);
		}
		finally {
			try {
				binaryarrstream.close();
			} catch (IOException e) {}
		}
	}

	@ParameterizedTest
	@FolderSource(path = "single") // file order
	void testArgumentsAccessorInjection_default(ArgumentsAccessor arg) throws URISyntaxException {
		var parent = Paths.get(getClass().getResource("single/case-1").toURI());
		assertNotNull(arg);
		assertEquals(4, arg.size());
		assertEquals(parent.resolve("Json-FILE.json").toFile(), arg.get(0));
		assertEquals(parent.resolve("TEXT_file.txt").toFile(), arg.get(1));
		assertEquals(parent.resolve("binary_arr").toFile(), arg.get(2));
		assertEquals(parent.resolve("xml.file.txt").toFile(), arg.get(3));
	}

	@ParameterizedTest
	@FolderSource(path = "single", defaultType = String.class) // file order
	void testArgumentsAccessorInjection_string(ArgumentsAccessor arg) {
		assertNotNull(arg);
		assertEquals(4, arg.size());
		assertEquals("[\"JSON\"]", arg.get(0));
		assertEquals("TXT", arg.get(1));
		assertEquals("BIN", arg.get(2));
		assertEquals("<xml>", arg.get(3));
	}

	@ParameterizedTest
	@FolderSource(path = "single")
	void testSmartArgConvert_default(@ConvertWith(value = TypeCodeConvertor.class) int jsonFile) {
		assertEquals(1, jsonFile);
	}

	@ParameterizedTest
	@FolderSource(path = "single", defaultType = Path.class)
	void testSmartArgConvert_path(@ConvertWith(value = TypeCodeConvertor.class) byte jsonFile) {
		assertEquals(2, jsonFile);
	}

	@ParameterizedTest
	@FolderSource(path = "single", defaultType = String.class)
	void testSmartArgConvert_path(@ConvertWith(value = TypeCodeConvertor.class) String jsonFile) {
		assertEquals("[\"JSON\"]", jsonFile);
	}
	

	@ParameterizedTest
	@FolderSource(path = "single", mode = FileMatchingMode.STRICT) // multiple arguments + matching names
	void testStrictArgInjection(File binary_arr, String binaryArr, InputStream binaryarrstream, File xml, String xmlFile, Path xml_file) {
		assertEquals("binary_arr", binary_arr.getName());
		assertNull(binaryArr);
		assertNull(binaryarrstream);
		assertEquals("xml.file.txt", xml.getName());
		assertNull(xmlFile);
		assertNull(xml_file);
	}

	
	static class TypeCodeConvertor implements ArgumentConverter {
		
		@Override
		public Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
			if(source instanceof File) {
				return 1;
			}
			if(source instanceof Path) {
				return (byte)2;
			}
			if(source instanceof String) {
				return source;
			}
			return null;
		}
	}
}
