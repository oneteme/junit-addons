package org.usf.junit.addons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.usf.junit.addons.AssertExt.assertThrowsWithMessage;
import static org.usf.junit.addons.FolderArgumentsProvider.typeResolver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FolderArgumentsProviderTest {
	
	private static File file;

	@BeforeAll
	public static void setup() throws URISyntaxException {
		file = Path.of(FolderSourceTest.class.getResource("assets/temp").toURI()).toFile();
	}

	@ParameterizedTest
	@ValueSource(classes = {File.class, URI.class, Path.class, InputStream.class, String.class, String[].class})
	void testTypeResolver(Class<?> clazz) throws IOException, URISyntaxException {
		assertEquals("dummy text", readString(typeResolver(clazz).apply(file)));
	}

	@Test
	void testTypeResolver_Unsupported() {
		assertThrowsWithMessage("Unsupported type " + Object.class, 
				UnsupportedOperationException.class, ()-> typeResolver(Object.class));
	}

	private static String readString(Object o) throws IOException {
		Path path = null;
		if(o instanceof File) {
			path = Path.of(((File)o).getPath());
		}
		else if(o instanceof Path) {
			path = (Path) o;
		}
		else if(o instanceof URI) {
			path = Paths.get((URI) o);
		}
		if(path != null) {
			return Files.readString(path);
		}
		if(o instanceof InputStream) {
			var is = (InputStream) o;
			try{
				return new String(is.readNBytes(10));
			}
			finally {
				is.close();
			}
		}
		if(o instanceof String) {
			return (String) o;
		}
		if(o instanceof String[]) {
			return String.join("", (String[]) o);
		}
		return null;
	}
}
