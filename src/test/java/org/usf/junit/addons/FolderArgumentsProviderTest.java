package org.usf.junit.addons;

import static java.lang.Thread.currentThread;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.usf.junit.addons.AssertExt.assertThrowsWithMessage;
import static org.usf.junit.addons.FolderArgumentsProvider.typeResolver;
import static org.usf.junit.addons.Utils.methodAnnotation;
import static org.usf.junit.addons.Utils.readContent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Parameter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ArgumentConverter;
import org.junit.jupiter.params.converter.ConvertWith;
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
		assertEquals("dummy text", readContent(res));
	}

	@Test
	void testTypeResolver_Unsupported() {
		assertThrowsWithMessage("Unsupported type class java.lang.Object", 
				UnsupportedOperationException.class, ()-> typeResolver(Object.class));
	}

	@Test
	@FolderSource //used in test 
	void testAttachedResources() {
		var provider = new FolderArgumentsProvider();
		provider.accept(selfMethodAnnotation());
		var res = Stream.of(provider.attachedResources(assets.toFile()))
			.map(File.class::cast)
			.map(File::getName)
			.toArray(String[]::new);
		assertArrayEquals(new String[] {
				"JsonFile.json",
				"TEXT file.txt",
				"data.bin.dat",
				"file-separator",
				"file_separator",
				"temp"
		}, res); //files order + excludes folders
	}

	@Test
	@FolderSource(defaultType = String.class) //used in test 
	void testAttachedResources_stringType() {
		var provider = new FolderArgumentsProvider();
		provider.accept(selfMethodAnnotation());
		var res = provider.attachedResources(assets.toFile());
		assertArrayEquals(new String[] {
				"[\"JSON\"]",
				"",
				"",
				"",
				"",
				"dummy text"
		}, res); //files order + excludes folders
	}
	
	@Test
	@FolderSource() //used in test 
	void testAttachedResources_empty() {
		var provider = new FolderArgumentsProvider();
		provider.accept(selfMethodAnnotation());
		assertNull(provider.attachedResources(assets.resolve("conf").toFile()));
	}
	

	@Test
	@FolderSource() //used in test 
	void testAttachedResource() {
		var provider = new FolderArgumentsProvider();
		provider.accept(selfMethodAnnotation());
		var file = provider.attachedResource(assets.toFile(), methodArg("method1"));
		assertEquals(assets.resolve("temp").toFile(), file);
	}
	
	@Test
	@FolderSource() //used in test 
	void testAttachedResource_multiple() {
		var provider = new FolderArgumentsProvider();
		provider.accept(selfMethodAnnotation());
		AssertExt.assertThrowsWithMessage("fileSeparator : to many resources found", 
				IllegalArgumentException.class, ()-> provider.attachedResource(assets.toFile(), methodArg("multipleFiles")));
	}
	
	@Test
	@FolderSource() //used in test 
	void testAttachedResource_notExist() {
		var provider = new FolderArgumentsProvider();
		provider.accept(selfMethodAnnotation());
		assertNull(provider.attachedResource(assets.toFile(), methodArg("notExistFile")));
	}

	@Test
	@FolderSource(defaultType = Path.class) //used in test 
	void testAttachedResource_convertWith() {
		var provider = new FolderArgumentsProvider();
		provider.accept(selfMethodAnnotation());
		assertEquals(assets.resolve("temp"), provider.attachedResource(assets.toFile(), methodArg("convertWith")));
	}

	@Test
	@FolderSource //used in test 
	void testAttachedResource_sharedFile() {
		var provider = new FolderArgumentsProvider();
		provider.accept(selfMethodAnnotation());
		assertEquals(assets.getParent().resolve("shared-file").toFile(), 
				provider.attachedResource(assets.toFile(), methodArg("parentFile")));
	}
	
	void method1(File tempFile) { }

	void multipleFiles(File fileSeparator) { }
	
	void notExistFile(File noExist) { }

	void convertWith(@ConvertWith(value=ArgumentConverter.class) File temp) { }

	void parentFile(File sharedFile) { }
	
	static FolderSource selfMethodAnnotation() {
		var arr = currentThread().getStackTrace();
		int i = 0;
		while(i<arr.length && !arr[i].getMethodName().equals("selfMethodAnnotation")) {
			i++;
		}
		return methodAnnotation(FolderArgumentsProviderTest.class, arr[i+1].getMethodName(), FolderSource.class);
	}
	
	static Parameter methodArg(String method) {
		return Utils.methodParameter(FolderArgumentsProviderTest.class, method, File.class);
	}
}
