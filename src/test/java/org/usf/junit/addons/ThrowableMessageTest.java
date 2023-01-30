package org.usf.junit.addons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.usf.junit.addons.AssertExt.assertThrowsWithMessage;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ThrowableMessageTest {

	@Test
	void testThrowableMessage() {
		var c = new ThrowableMessage(Exception.class.getName(), "dummy message");
		assertEquals(Exception.class, c.getType());
		assertEquals("dummy message", c.getMessage());
	}
	
	@ParameterizedTest
	@ValueSource(classes = {Exception.class, IOException.class, SQLException.class})
	void testParse(Class<?> type){
		assertEquals(type, ThrowableMessage.parse(type.getName()));
	}

	@Test
	void testParse_badClass() {
		assertThrowsWithMessage(IllegalArgumentException.class, 
				"org.usf.assertapi.core.ApiRequest class not found", 
				()-> ThrowableMessage.parse("org.usf.assertapi.core.ApiRequest"));

		assertThrowsWithMessage(IllegalArgumentException.class, 
				"java.lang.Object class is not throwable", 
				()-> ThrowableMessage.parse("java.lang.Object"));
	}

	@Test
	void testToString() {
		assertEquals("java.lang.Exception : dummy message", 
				new ThrowableMessage(Exception.class.getName(), "dummy message").toString());
	}
	
}
