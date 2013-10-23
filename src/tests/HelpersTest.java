package tests;

import static org.junit.Assert.*;
import static helpers.StaticHelpers.*;

import org.junit.Before;
import org.junit.Test;

public class HelpersTest {
	@Test
	public void testIthBit() {
		assertEquals(1, ithBit(0, 0b1));
		assertEquals(1, ithBit(0, 0b11));
		assertEquals(0, ithBit(0, 0b10));
		assertEquals(0, ithBit(0, 0b0));
		assertEquals(0, ithBit(1, 0b1));
		assertEquals(1, ithBit(1, 0b11));
		assertEquals(1, ithBit(1, 0b10));
		assertEquals(0, ithBit(1, 0b0));
	}

}
