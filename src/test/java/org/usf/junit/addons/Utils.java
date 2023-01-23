package org.usf.junit.addons;

import static java.lang.String.join;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

final class Utils {
	
	private Utils() {}
	
	static Method classMethod(Class<?> clazz, String method, Class<?>... argClass) {
		try {
			return clazz.getDeclaredMethod(method, argClass);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	static Parameter methodParameter(Class<?> clazz, String method, Class<?> argClass) {
		try {
			return clazz.getDeclaredMethod(method, argClass).getParameters()[0];
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	static <T extends Annotation> T methodAnnotation(Class<?> clazz, String method, Class<T> annotationClass, Class<?>... argClass) {
		try {
			return clazz.getDeclaredMethod(method, argClass).getDeclaredAnnotation(annotationClass);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	static <T extends Annotation> T methodParameterAnnotation(Class<?> clazz, String method, Class<T> annotationClass, Class<?>... argClass) {
		try {
			var m = clazz.getDeclaredMethod(method, argClass);
			return m.getParameters()[0].getAnnotationsByType(annotationClass)[0];
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	static String readContent(Object o) throws IOException {
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
			return join("", (String[]) o);
		}
		if(o instanceof byte[]) {
			return new String((byte[]) o);
		}
		return null;
	}
}
