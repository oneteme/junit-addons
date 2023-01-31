package org.usf.junit.addons;

import static java.lang.Class.forName;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

//not final 
public class ThrowableMessage {
	
	private final Class<? extends Throwable> type;
	private final String message;
	
	@JsonCreator
	public ThrowableMessage(@JsonProperty("type") String type, @JsonProperty("message") String message) {
		this.type = parseType(type);
		this.message = message;
	}

	public Class<? extends Throwable> getType() {
		return type;
	}
	
	public String getMessage() {
		return message;
	}
	
	@SuppressWarnings("unchecked")
	static Class<? extends Throwable> parseType(String className) {
		Class<?> c;
		try {
			c = forName(className);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(className + " class not found", e);
		}
		if(Throwable.class.isAssignableFrom(c)) {
			return (Class<? extends Throwable>) c;
		}
		throw new IllegalArgumentException(className + " class is not throwable");
	}
	
	@Override
	public String toString() {
		return type.getName() + " : " + message;
	}
}