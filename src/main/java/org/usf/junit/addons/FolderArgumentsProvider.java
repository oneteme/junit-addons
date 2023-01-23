package org.usf.junit.addons;

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.readAllLines;
import static java.nio.file.Files.readString;
import static java.util.Comparator.comparing;
import static java.util.regex.Pattern.compile;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;

public final class FolderArgumentsProvider implements ArgumentsProvider, AnnotationConsumer<FolderSource> {
	
	FolderSource fs; //relative | absolute
	Predicate<String> filter; //default => U.T

	@Override
	public void accept(FolderSource ds) {
		this.fs = ds;
		this.filter = fs.pattern().isEmpty()
				? s-> true
				: compile(ds.pattern()).asMatchPredicate(); //validate regex parameter
	}

	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
		return provideArguments(
				context.getTestClass().orElseThrow(), 
				context.getTestMethod().orElseThrow());
	}

	Stream<Arguments> provideArguments(Class<?> testClass, Method method) throws URISyntaxException {
		var root = testClass.getResource(fs.path());
		if(root == null) {
			return Stream.empty();
		}
		var folders = new File(root.toURI()).listFiles(filter());
		if(method.getParameterCount() == 0) {
			return Stream.of(folders).map(f-> arguments()); //no args
		}
		if(method.getParameterCount() == 1) {
			var c = method.getParameters()[0].getType();
			if(ArgumentsAccessor.class.isAssignableFrom(c)){
				return Stream.of(folders).map(f-> arguments(attachedResources(f))); //all files
			}
		}
		return Stream.of(folders)
				.map(f-> arguments(Stream.of(method.getParameters()).map(p-> attachedResource(f, p)).toArray()));
	}

	Object[] attachedResources(File folder) { //ArgumentsAccessor
		var files = folder.listFiles(File::isFile);
		return files == null ? null : Stream.of(files)
			.sorted(comparing(File::getName)) //not same order window/unix
			.map(typeResolver(fs.defaultType()))
			.toArray();
	}
	
	Object attachedResource(File folder, Parameter arg) {
		File[] res = fs.mode().matchingFiles(arg.getName(), folder);
		if(res.length == 0) {
			res = fs.mode().matchingFiles(arg.getName(), folder.getParentFile()); //search in parent (shared resources)
		}
		if(res.length == 0) {
			return null; //TD primitive types ? 
		}
		if(res.length == 1) {
			var type = findAnnotation(arg, ConvertWith.class).isEmpty() ? arg.getType() : fs.defaultType();
			return typeResolver(type).apply(res[0]);
		}
		throw new IllegalArgumentException(arg.getName() + " : to many resources found");
	}
	
	static Function<File, Object> typeResolver(Class<?> c) {
		if(c.equals(File.class)) {
			return f-> f;
		}
		if(c.equals(Path.class)) {
			return File::toPath;
		}
		if(c.equals(URI.class)) {
			return File::toURI;
		}
		if(c.equals(InputStream.class)) {
			return FolderArgumentsProvider::readStream;
		}
		if(c.equals(String.class)) {
			return FolderArgumentsProvider::readContent;
		}
		if(c.equals(String[].class)) {
			return FolderArgumentsProvider::readLines;
		}
		if(c.equals(byte[].class)) {
			return FolderArgumentsProvider::readBytes;
		}
		throw new UnsupportedOperationException("Unsupported type " + c );
	}

	private static InputStream readStream(File f) {
		try {
			return f.toURI().toURL().openStream();
		} catch (IOException e) {
			throw new ResourceAccesException(e);
		}
	}
	
	private static String readContent(File f) {
		try {
			return readString(f.toPath());
		} catch (IOException e) {
			throw new ResourceAccesException(e);
		}
	}
	
	private static String[] readLines(File f) {
		try {
			return readAllLines(f.toPath()).toArray(String[]::new);
		} catch (IOException e) {
			throw new ResourceAccesException(e);
		}
	}	
	
	private static byte[] readBytes(File f) {
		try {
			return readAllBytes(f.toPath());
		} catch (IOException e) {
			throw new ResourceAccesException(e);
		}
	}
	
	private FileFilter filter(){
    	return f-> f.isDirectory() && filter.test(f.getName());
	}
}