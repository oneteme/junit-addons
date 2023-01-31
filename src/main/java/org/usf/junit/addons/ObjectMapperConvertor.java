package org.usf.junit.addons;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.nio.file.Path;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;
import org.junit.jupiter.params.support.AnnotationConsumer;

import com.fasterxml.jackson.databind.ObjectMapper;

final class ObjectMapperConvertor implements ArgumentConverter, AnnotationConsumer<ConvertWithObjectMapper> {
	
	private ConvertWithObjectMapper annotation;

	@Override
	public void accept(ConvertWithObjectMapper annotation) {
		this.annotation = annotation;
	}
	
	@Override
	public Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
		return convert(source, context.getParameter().getType());
	}

	<T> T convert(Object source, Class<T> type) throws ArgumentConversionException {
		if(source == null) {
			return null;
		}
		var mapper = definedMapper();
		try {
			if(source instanceof File) {
				return mapper.readValue((File)source, type);
			}
			if(source instanceof Path) {
				return mapper.readValue(((Path)source).toFile(), type);
			}
			if(source instanceof URI) {
				return mapper.readValue(Path.of((URI)source).toFile(), type);
			}
			if(source instanceof InputStream) {
				return mapper.readValue((InputStream)source, type);
			}
			if(source instanceof String) {
				return mapper.readValue((String)source, type);
			}
			if(source instanceof byte[]) {
				return mapper.readValue((byte[])source, type);
			}
			throw new UnsupportedOperationException("unsupported type : " + source.getClass().getCanonicalName());
		} catch (IOException e) {
			throw new ArgumentConversionException("error while reading file : " + source, e);
		}
	}
	
	ObjectMapper definedMapper() {
		try {
			var method = annotation.clazz().getDeclaredMethod(annotation.method());
			if(ObjectMapper.class.isAssignableFrom(method.getReturnType())) {
				return (ObjectMapper) method.invoke(null);
			}
			throw new IllegalArgumentException(methodFullName() + " method must return an instance of ObjectMapper");
		} catch (InvocationTargetException e) {
			throw new ResourceAccessException(methodFullName() + " method invoke throws exception", e);
		} catch (NoSuchMethodException e) {
			throw new NoSuchElementException(methodFullName() + " method not found");
		} catch (SecurityException | IllegalAccessException e) {
			throw new ResourceAccessException(methodFullName() + " method is not accessibe", e);
		}
	}
	
	private String methodFullName() {
		return annotation.clazz().getSimpleName() + "." + annotation.method();
	}
	
	/**
	 * do not delete or rename this method
	 * 
	 * @see org.usf.ConvertWithObjectMapper.core.junit.ConvertWithJsonParser
	 * 
	 */
	static ObjectMapper defaultMapper() {
		return new ObjectMapper();
	}
}