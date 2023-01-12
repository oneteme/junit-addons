package org.usf.junit.addons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.function.Executable;

public final class AssertExt {
	
	private AssertExt() {}
	
	public static void assertThrowsWithMessage(String expectedMessage, Class<? extends Throwable> expectedType, Executable executable) {
		assertEquals(expectedMessage, assertThrows(expectedType, executable).getMessage());
	}
	
	public static void assertThrowsWithCause(Throwable throwable, Class<? extends Throwable> expectedType, Executable executable) {
		assertSame(throwable, assertThrows(expectedType, executable).getCause());
	}

}
