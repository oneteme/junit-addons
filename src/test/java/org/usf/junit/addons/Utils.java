package org.usf.junit.addons;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.extension.ParameterContext;


final class Utils {
	
	private Utils() {}

	static Parameter methodParameter(Class<?> clazz, String method, Class<?> argClass) {
		try {
			return clazz.getDeclaredMethod(method, argClass).getParameters()[0];
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	static <T extends Annotation> T  methodAnnotation(Class<?> clazz, String method, Class<T> annotationClass) {
		try {
			return clazz.getDeclaredMethod(method).getDeclaredAnnotation(annotationClass);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	static <T extends Annotation> T methodParameterAnnotation(Class<?> clazz, String method, Class<?> argClass, Class<T> annotationClass) {
		try {
			var m = clazz.getDeclaredMethod(method, argClass);
			return m.getParameters()[0].getAnnotationsByType(annotationClass)[0];
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	static ParameterContext parameterContext(Parameter p) {
		
		return new ParameterContext() {
			
			@Override
			public boolean isAnnotated(Class<? extends Annotation> annotationType) {return false; }
			
			@Override
			public Optional<Object> getTarget() {return Optional.empty(); }
			
			@Override
			public Parameter getParameter() { return p; }
			
			@Override
			public int getIndex() { return 0; }
			
			@Override
			public <A extends Annotation> List<A> findRepeatableAnnotations(Class<A> annotationType) { return null; }
			
			@Override
			public <A extends Annotation> Optional<A> findAnnotation(Class<A> annotationType) { return Optional.empty(); }
		};
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
			return String.join("", (String[]) o);
		}
		return null;
	}

}
