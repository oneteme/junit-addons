package org.usf.junit.addons;

import static java.lang.Thread.currentThread;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.usf.junit.addons.AssertExt.assertThrowsWithMessage;
import static org.usf.junit.addons.FolderArgumentsProvider.typeResolver;
import static org.usf.junit.addons.Utils.classMethod;
import static org.usf.junit.addons.Utils.methodAnnotation;
import static org.usf.junit.addons.Utils.methodParameter;
import static org.usf.junit.addons.Utils.readContent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Parameter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.converter.ArgumentConverter;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * 
 * @author u$f
 *
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
class FolderArgumentsProviderTest {
	
	private static Path assets;

	@BeforeAll
	public static void setup() throws URISyntaxException {
		assets = Path.of(FolderSourceTest.class.getResource("assets").toURI());
	}

	@Test
	@FolderSource(pattern = "") //used in test 
	void testAccept(){
		var provider = initFolderArgumentsProvider();
		assertNotNull(provider.fs);
		assertNotNull(provider.filter);
	}
	
	@Test
	@FolderSource(pattern = "\\w+") //used in test 
	void testAccept_pattern(){
		var provider = initFolderArgumentsProvider();
		assertNotNull(provider.fs);
		assertNotNull(provider.filter);
	}
	
	@Test
	@FolderSource(pattern = "*") //used in test 
	void testAccept_badPattern(){
		var provider = new FolderArgumentsProvider();
		var annotation = selfMethodAnnotation(1);
		assertThrows(PatternSyntaxException.class, ()-> provider.accept(annotation)); //pre-compile regex
	}
	
	@Test
	@FolderSource(path = "conf") //used in test 
	void testProvideArguments_badPath() throws URISyntaxException {
		assertEquals(0, initFolderArgumentsProvider().provideArguments(getClass(), classMethod(getClass(), "testProvideArguments_badPath")).count());
	}

	@Test
	@FolderSource(path = "provider") //used in test 
	void testProvideArguments_noParameter() throws URISyntaxException {
		var list = initFolderArgumentsProvider().provideArguments(getClass(), classMethod(getClass(), "testProvideArguments_noParameter"))
				.collect(toList());
		assertEquals(3, list.size());
		list.forEach(c-> assertEquals(0, c.get().length)); //method has no parameter
	}

	@Test
	@FolderSource(path = "provider") //used in test 
	void testProvideArguments_oneParameter() throws URISyntaxException {
		var list = initFolderArgumentsProvider().provideArguments(getClass(), classMethod(getClass(), "oneParameter", File.class))
				.collect(toList());
		assertEquals(3, list.size());
		list.forEach(c-> {
			assertEquals(1, c.get().length);
			assertInstanceOf(File.class, c.get()[0]);
		}); //method has no parameter
	}

	@Test
	@FolderSource(path = "provider") //used in test 
	void testProvideArguments_ArgumentsAccessor() throws URISyntaxException {
		var list = initFolderArgumentsProvider().provideArguments(getClass(), classMethod(getClass(), "oneParameter", ArgumentsAccessor.class))
				.collect(toList());
		assertEquals(3, list.size());
		list.forEach(c-> {
			assertEquals(1, c.get().length);
			assertInstanceOf(File.class, c.get()[0]);
		}); //method has no parameter
	}
	
	@Test
	@FolderSource(path = "provider", defaultType = URI.class) //used in test 
	void testProvideArguments_ArgumentsAccessor_stringType() throws URISyntaxException {
		var list = initFolderArgumentsProvider().provideArguments(getClass(), classMethod(getClass(), "oneParameter", ArgumentsAccessor.class))
				.collect(toList());
		assertEquals(3, list.size());
		list.forEach(c-> {
			assertEquals(1, c.get().length);
			assertInstanceOf(URI.class, c.get()[0]);
		}); //method has no parameter
	}
	
	@Test
	@FolderSource(path = "provider") //used in test 
	void testProvideArguments_nParameter() throws URISyntaxException {
		var list = initFolderArgumentsProvider().provideArguments(getClass(), classMethod(getClass(), "nParameter", File.class, Path.class, byte[].class))
				.collect(toList());
		assertEquals(3, list.size());
		list.forEach(c-> {
			assertEquals(3, c.get().length);
			assertInstanceOf(File.class, c.get()[0]);
			assertInstanceOf(Path.class, c.get()[1]);
			assertInstanceOf(byte[].class, c.get()[2]);
		}); //method has no parameter
	}
	
	@Test
	@FolderSource //used in test 
	void testAttachedResources() {
		var res = Stream.of(initFolderArgumentsProvider().attachedResources(assets.toFile()))
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
		assertArrayEquals(new String[] {
				"[\"JSON\"]",
				"",
				"",
				"",
				"",
				"dummy text"
		}, initFolderArgumentsProvider().attachedResources(assets.toFile())); //files order + excludes folders
	}
	
	@Test
	@FolderSource() //used in test 
	void testAttachedResources_empty() {
		assertNull(initFolderArgumentsProvider().attachedResources(assets.resolve("conf").toFile()));
	}
	

	@Test
	@FolderSource() //used in test 
	void testAttachedResource() {
		assertEquals(assets.resolve("temp").toFile(), 
				initFolderArgumentsProvider().attachedResource(assets.toFile(), methodArg("tempFile")));
	}
	
	@Test
	@FolderSource() //used in test 
	void testAttachedResource_multiple() {
		var provider = initFolderArgumentsProvider();
		AssertExt.assertThrowsWithMessage("fileSeparator : to many resources found", 
				IllegalArgumentException.class, ()-> provider.attachedResource(assets.toFile(), methodArg("multipleFiles")));
	}
	
	@Test
	@FolderSource() //used in test 
	void testAttachedResource_notExist() {
		assertNull(initFolderArgumentsProvider().attachedResource(assets.toFile(), methodArg("notExistFile")));
	}

	@Test
	@FolderSource(defaultType = Path.class) //used in test 
	void testAttachedResource_convertWith() {
		assertEquals(assets.resolve("temp"), 
				initFolderArgumentsProvider().attachedResource(assets.toFile(), methodArg("convertWith")));
	}

	@Test
	@FolderSource //used in test 
	void testAttachedResource_sharedFile() {
		assertEquals(assets.getParent().resolve("shared-file").toFile(), 
				initFolderArgumentsProvider().attachedResource(assets.toFile(), methodArg("parentFile")));
	}

	@ParameterizedTest
	@ValueSource(classes = {File.class, URI.class, Path.class, InputStream.class, String.class, String[].class, byte[].class})
	void testTypeResolver(Class<?> clazz) throws IOException, URISyntaxException {
		var res = typeResolver(clazz).apply(assets.resolve("temp").toFile());
		assertEquals("dummy text", readContent(res));
	}

	@Test
	void testTypeResolver_unsupported() {
		assertThrowsWithMessage("Unsupported type class java.lang.Object", 
				UnsupportedOperationException.class, ()-> typeResolver(Object.class));
	}
	
	/* provideArguments methods */

	void oneParameter(File file) { }

	void oneParameter(ArgumentsAccessor arg) { }

	void nParameter(File file, Path filePath, byte[] fileContent) { }
	
	/* typeResolver methods */
	
	void tempFile(File tempFile) { }

	void multipleFiles(File fileSeparator) { }
	
	void notExistFile(File noExist) { }

	void convertWith(@ConvertWith(value=ArgumentConverter.class) File temp) { }

	void parentFile(File sharedFile) { }

	
	
	static Parameter methodArg(String method) {
		return methodParameter(FolderArgumentsProviderTest.class, method, File.class);
	}
	
	static FolderArgumentsProvider initFolderArgumentsProvider() {
		var provider = new FolderArgumentsProvider();
		provider.accept(selfMethodAnnotation(2));
		return provider;
	}
	
	static FolderSource selfMethodAnnotation(int deep) {
		var arr = currentThread().getStackTrace();
		int i = 0;
		while(i<arr.length && !arr[i].getMethodName().equals("selfMethodAnnotation")) {
			i++;
		}
		if(i+deep >= arr.length) {
			throw new IllegalArgumentException();
		}
		return methodAnnotation(FolderArgumentsProviderTest.class, arr[i+deep].getMethodName(), FolderSource.class);
	}
}
