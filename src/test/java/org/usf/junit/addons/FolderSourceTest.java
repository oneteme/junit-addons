package org.usf.junit.addons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.usf.junit.addons.FolderSource.FileMatchingMode.SMART;
import static org.usf.junit.addons.FolderSource.FileMatchingMode.STRICT;

import java.net.URISyntaxException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FolderSourceTest {
	
	private static Path assets;

	@BeforeAll
	public static void setup() throws URISyntaxException {
		assets = Path.of(FolderSourceTest.class.getResource("assets").toURI());
	}
	
	@ParameterizedTest(name = "[STRICT] {0} => JsonFile.json")
	@ValueSource(strings = {"jsonFile", "JSONFILE", "Jsonfile"}) //filename pattern
	void testMatchingFiles_strict_pattern(String argName) {
		var files = STRICT.matchingFiles(argName, assets.toFile());
		assertEquals(1, files.length);
		assertEquals("JsonFile.json", files[0].getName());
	}
	
	@ParameterizedTest(name = "[STRICT] {0} => data.bin.dat")
	@ValueSource(strings = {"data", "DATA", "Data"}) // dot separator
	void testMatchingFiles_strict_dotPattern(String argName) {
		var files = STRICT.matchingFiles(argName, assets.toFile());
		assertEquals(1, files.length);
		assertEquals("data.bin.dat", files[0].getName());
	}
	
	@ParameterizedTest(name = "[STRICT] {0} => temp")
	@ValueSource(strings = {"temp", "TEMP", "Temp"}) // no file extension
	void testMatchingFiles_strict_noExt(String argName) {
		var files = STRICT.matchingFiles(argName, assets.toFile());
		assertEquals(1, files.length);
		assertEquals("temp", files[0].getName());
	}

	@ParameterizedTest(name = "[STRICT] {0} => []")
	@ValueSource(strings = {"json_file", "JSON_FILE", "data_bin", "DATA_BIN", "tempFile", "refTemp"}) //not match
	void testMatchingFiles_strict_nonematch(String argName) {
		var files = STRICT.matchingFiles(argName, assets.toFile());
		assertEquals(0, files.length);
	}

	@ParameterizedTest(name = "{0} => !file")
	@ValueSource(strings = {"bin", "Bin", "BIN"}) //folder
	void testMatchingFiles_notFile(String argName) {
		assertEquals(0, STRICT.matchingFiles(argName, assets.toFile()).length);
		assertEquals(0, SMART.matchingFiles(argName, assets.toFile()).length);
	}
	
	@ParameterizedTest(name = "[SMART] {0} => JsonFile.json")
	@ValueSource(strings = {"jsonFile", "JSONFILE", "Json_File", "jsonFilePath", "refJsonFile"}) //filename pattern + suffix/prefix
	void testMatchingFiles_smart_pattern(String argName) {
		var files = SMART.matchingFiles(argName, assets.toFile());
		assertEquals(1, files.length);
		assertEquals("JsonFile.json", files[0].getName());
	}

	@ParameterizedTest(name = "[SMART] {0} => data.bin.dat")
	@ValueSource(strings = {"dataBin", "DATABIN", "Data_Bin", "dataBinPath", "refDataBin"}) //filename pattern + suffix/prefix
	void testMatchingFiles_smart_dotPattern(String argName) {
		var files = SMART.matchingFiles(argName, assets.toFile());
		assertEquals(1, files.length);
		assertEquals("data.bin.dat", files[0].getName());
	}

	@ParameterizedTest(name = "[SMART] {0} => TEXT file.txt")
	@ValueSource(strings = {"textFile", "TEXTFILE", "Text_File", "textFilePath", "refTextFile"}) //filename pattern + suffix/prefix
	void testMatchingFiles_smart_blankPattern(String argName) {
		var files = SMART.matchingFiles(argName, assets.toFile());
		assertEquals(1, files.length);
		assertEquals("TEXT file.txt", files[0].getName());
	}
	
	@ParameterizedTest(name = "[SMART] {0} => temp")
	@ValueSource(strings = {"temp", "TEMP", "Temp", "temp_Path", "ref_Temp"}) //filename pattern + suffix/prefix
	void testMatchingFiles_smart_noExt(String argName) {
		var files = SMART.matchingFiles(argName, assets.toFile());
		assertEquals(1, files.length);
		assertEquals("temp", files[0].getName());
	}
	
	@Test
	void testMatchingFiles_smart_noExt() {
		var files = SMART.matchingFiles("fileSeparator", assets.toFile());
		assertEquals(2, files.length);
	}
}
