package org.usf.junit.addons;

import static java.lang.Thread.currentThread;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.usf.junit.addons.AssertExt.assertThrowsWithMessage;
import static org.usf.junit.addons.FolderArgumentsProvider.typeResolver;
import static org.usf.junit.addons.Utils.methodAnnotation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FolderArgumentsProviderTest {
	
	private static Path assets;

	@BeforeAll
	public static void setup() throws URISyntaxException {
		assets = Path.of(FolderSourceTest.class.getResource("assets").toURI());
	}

	@ParameterizedTest
	@ValueSource(classes = {File.class, URI.class, Path.class, InputStream.class, String.class, String[].class})
	void testTypeResolver(Class<?> clazz) throws IOException, URISyntaxException {
		var res = typeResolver(clazz).apply(assets.resolve("temp").toFile());
		assertEquals("dummy text", Utils.readContent(res));
	}

	@Test
	void testTypeResolver_Unsupported() {
		assertThrowsWithMessage("Unsupported type " + Object.class, 
				UnsupportedOperationException.class, ()-> typeResolver(Object.class));
	}

	@Test
	@FolderSource //used in test 
	void testAttachedResource() {
		var provider = new FolderArgumentsProvider();
		provider.accept(selfMethodAnnotation());
		var res = Stream.of(provider.attachedResource(assets.toFile()))
			.map(File.class::cast)
			.map(File::getName)
			.toArray(String[]::new);
		assertArrayEquals(new String[] {
				"data.bin.dat",
				"file-separator",
				"file_separator",
				"JsonFile.json",
				"temp",
				"TEXT file.txt"
		}, res); //files order + excludes folders
	}

	@Test
	@FolderSource(defaultType = String.class) //used in test 
	void testAttachedResource_stringType() {
		var provider = new FolderArgumentsProvider();
		provider.accept(selfMethodAnnotation());
		var res = provider.attachedResource(assets.toFile());
		assertArrayEquals(new String[] {
				"",
				"",
				"",
				"[\"JSON\"]",
				"dummy text",
				""
		}, res); //files order + excludes folders
	}
	
	@Test
	@FolderSource() //used in test 
	void testAttachedResource_empty() {
		var provider = new FolderArgumentsProvider();
		provider.accept(selfMethodAnnotation());
		assertNull(provider.attachedResource(assets.resolve("conf").toFile()));
	}
	
	static FolderSource selfMethodAnnotation() {
		var arr = currentThread().getStackTrace();
		int i = 0;
		while(i<arr.length && !arr[i].getMethodName().equals("selfMethodAnnotation")) {
			i++;
		}
		return methodAnnotation(FolderArgumentsProviderTest.class, arr[i+1].getMethodName(), FolderSource.class);
	}
}
