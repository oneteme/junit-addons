package org.usf.assertapi.test;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.params.converter.ConvertWith;

@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ConvertWith(JsonParser.class)
public @interface ConvertWithJsonParser {
	
	Class<?> clazz() default JsonParser.class;
	
	String method() default "defaultMapper";
	
}