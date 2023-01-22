package org.usf.junit.addons;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.extension.ParameterContext;

public class Utils {

	static Parameter methodParameter(Class<?> clazz, String method, Class<?> argClass) {
		try {
			return clazz.getDeclaredMethod(method, argClass).getParameters()[0];
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

}
