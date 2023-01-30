package org.usf.junit.addons;

//not final 
public class ThrowableMessage {
	
	private final Class<? extends Throwable> type;
	private final String message;
	
	public ThrowableMessage(String type, String message) {
		this.type = parse(type);
		this.message = message;
	}

	public Class<? extends Throwable> getType() {
		return type;
	}
	
	public String getMessage() {
		return message;
	}
	
	@SuppressWarnings("unchecked")
	static Class<? extends Throwable> parse(String className) {
		Class<?> c;
		try {
			c = Class.forName(className);
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