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
			assertThrowsWithMessage(Exception.class, msg, ()-> {throw new Exception(msg);});
		});
		assertThrows(AssertionError.class, ()->{
			assertThrowsWithMessage(Exception.class, msg, ()-> {});
		});
		assertThrows(AssertionError.class, ()->{
			assertThrowsWithMessage(Exception.class, msg, ()-> {throw new Exception("another msg");});
		});
	}
	
	@Test
	void testAssertThrowsWithCause() {
		var msg = "dummy message";
		var cause = new Exception(msg);
		assertDoesNotThrow(()->{
			assertThrowsWithCause(RuntimeException.class, cause, ()-> {throw new RuntimeException(cause);});
		});
		assertThrows(AssertionError.class, ()->{
			assertThrowsWithCause(RuntimeException.class, cause, ()-> {}); //no exception
		});
		assertThrows(AssertionError.class, ()->{
			assertThrowsWithCause(RuntimeException.class, cause, ()-> {throw new RuntimeException(new Exception(msg));}); // equals but not same
		});
	}
	

	@Test
	void testAssertThrowsWithCause_ThrowableMessage() {
		var msg = "dummy message";
		var tm = new ThrowableMessage(Exception.class.getName(), msg);
		assertDoesNotThrow(()->{
			assertThrowsWithMessage(tm, ()-> {throw new Exception(msg);});
		});
		assertThrows(AssertionError.class, ()->{
			assertThrowsWithMessage(tm, ()-> {});
		});
		assertThrows(AssertionError.class, ()->{
			assertThrowsWithMessage(tm, ()-> {throw new Exception("another msg");});
		});
	}

}
