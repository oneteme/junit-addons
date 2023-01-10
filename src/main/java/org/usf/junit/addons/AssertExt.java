package org.usf.junit.addons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.function.Executable;

public final class AssertExt {
	
	private AssertExt() {}
	
	public static void assertThrowsMessage(String expectedMessage, Class<? extends Throwable> expectedType, Executable executable) {
		assertEquals(expectedMessage, assertThrows(expectedType, executable).getMessage());
	}

}