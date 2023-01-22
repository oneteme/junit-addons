package org.usf.junit.addons;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.usf.junit.addons.AssertExt.assertThrowsWithCause;
import static org.usf.junit.addons.AssertExt.assertThrowsWithMessage;

import org.junit.jupiter.api.Test;

class AssertExtTest {

	@Test
	void testAassertThrowsWithMessage() {
		var msg = "dummy message";
		assertDoesNotThrow(()->{
			assertThrowsWithMessage(msg, Exception.class, ()-> {throw new Exception(msg);});
		});
		assertThrows(AssertionError.class, ()->{
			assertThrowsWithMessage(msg, Exception.class, ()-> {});
		});
		assertThrows(AssertionError.class, ()->{
			assertThrowsWithMessage("another msg", Exception.class, ()-> {throw new Exception(msg);});
		});
	}
	
	@Test
	void testAssertThrowsWithCause() {
		var msg = "dummy message";
		var cause = new Exception(msg);
		assertDoesNotThrow(()->{
			assertThrowsWithCause(cause, RuntimeException.class, ()-> {throw new RuntimeException(cause);});
		});
		assertThrows(AssertionError.class, ()->{
			assertThrowsWithCause(cause, RuntimeException.class, ()-> {}); //no exception
		});
		assertThrows(AssertionError.class, ()->{
			assertThrowsWithCause(cause, RuntimeException.class, ()-> {throw new RuntimeException(new Exception(msg));}); // equals but not same
		});
	}

}
