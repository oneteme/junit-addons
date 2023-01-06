package org.usf.junit.addons;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;
import org.junit.jupiter.params.support.AnnotationConsumer;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonParser implements ArgumentConverter, AnnotationConsumer<ConvertWithJsonParser> {
	
	private ConvertWithJsonParser annotation;

	@Override
	public void accept(ConvertWithJsonParser annotation) {
		this.annotation = annotation;
	}
	
	@Override
	public Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
		var mapper = annotation == null 
				? defaultMapper()
				: definedMapper();
		try {
			return mapper.readValue((File)source, context.getParameter().getType());
		} catch (IOException e) {
			throw new ArgumentConversionException("error while reading file " + source, e);
		}
	}
	
	private ObjectMapper definedMapper() {
		try {
			var method = annotation.clazz().getDeclaredMethod(annotation.method());
			if(ObjectMapper.class.isAssignableFrom(method.getReturnType())) {
				return (ObjectMapper) method.invoke(null);
			}
			throw new IllegalArgumentException(methodFullName() + " method must return an instance of ObjectMapper");
		} catch (InvocationTargetException e) {
			throw new IllegalArgumentException(methodFullName() + " method should have no parameters", e);
		} catch (NoSuchMethodException e) {
			throw new NoSuchElementException(methodFullName() + " method not found");
		} catch (SecurityException | IllegalAccessException e) {
			throw new ResourceAccesException(methodFullName() + " method is not accessibe", e);
		}
	}
	
	private String methodFullName() {
		return annotation.clazz().getSimpleName() + "." + annotation.method();
	}
	
	/**
	 * do not rename this method
	 * 
	 * @see org.usf.assertapi.core.junit.ConvertWithJsonParser
	 * 
	 */
	static ObjectMapper defaultMapper() {
		return new ObjectMapper();
	}
}