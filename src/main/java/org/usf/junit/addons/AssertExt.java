package org.usf.junit.addons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.function.Executable;

public final class AssertExt {
	
	private AssertExt() {}

	public static void assertThrowsWithMessage(ThrowableMessage expected, Executable executable) {
		assertThrowsWithMessage(expected.getType(), expected.getMessage(), executable);
	}
	
	public static void assertThrowsWithMessage(Class<? extends Throwable> expectedType, String expectedMessage, Executable executable) {
		assertEquals(expectedMessage, assertThrows(expectedType, executable).getMessage());
	}
	
	public static void assertThrowsWithCause(Class<? extends Throwable> expectedType, Throwable expectedCause, Executable executable) {
		assertSame(expectedCause, assertThrows(expectedType, executable).getCause());
	}

}
