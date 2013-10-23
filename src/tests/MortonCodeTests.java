package tests;

import static helpers.MortonCodes.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;

import org.junit.Test;

public class MortonCodeTests {
	// example of a level 4 (cell) morton code
	long cellCode = 0b1000101000100;

	// the hashes of its parent and neighbors
	long parent = 0b1000101000;

	long nbr_plus_x = 0b1000101100000;
	long nbr_plus_y = 0b1000101000110;
	long nbr_plus_z = 0b1000101000101;

	long nbr_minus_x = 0b1000101000000;
	long nbr_minus_y = -1; // invalid: the vertex lies on the boundary and an
							// underflow should occur
	long nbr_minus_z = 0b1000100001101;

	// example of a a vertex morton code in a multigrid of
	// depth 4. It lies on the level 3 and 4 grids
	long vertexHash = 0b1000110100000;

	@Test
	public void testLevel() {
		assertThat(cellLevel(cellCode), is(4));
		assertThat(cellLevel(nbr_plus_x), is(4));
		assertThat(cellLevel(nbr_plus_y), is(4));
		assertThat(cellLevel(nbr_plus_z), is(4));
		assertThat(cellLevel(nbr_minus_x), is(4));
		assertThat(cellLevel(nbr_minus_x), is(4));
		assertThat(cellLevel(nbr_minus_x), is(4));

		for (Integer i : Arrays.asList(0, 1, 2, 3, 5, 6, 7)) {
			assertThat(isCellOnLevelXGrid(cellCode, i), is(false));
		}

		assertThat(isCellOnLevelXGrid(cellCode, 4), is(true));
	}

	@Test
	public void testVertexOnGridLevel() {
		for (Integer i : Arrays.asList(0, 1, 2)) {
			assertThat(isVertexOnLevelXGrid(vertexHash, i, 4), is(false));
		}
		for (Integer i : Arrays.asList(3, 4)) {
			assertThat(isVertexOnLevelXGrid(vertexHash, i, 4), is(true));
		}
		assertThat(minVertexLevel(vertexHash, 4), is(3));
		assertThat(maxVertexLevel(vertexHash, 4), is(4));
	}

	@Test
	public void testParent() {
		assertThat(cellLevel(parentCode(cellCode)), is(3));
		assertThat(parentCode(cellCode), is(parent));
	}

	@Test
	public void testNeighborPlus() {
		assertThat(nbrCode(cellCode, 4, 0b100), is(nbr_plus_x));
		assertThat(nbrCode(cellCode, 4, 0b010), is(nbr_plus_y));
		assertThat(nbrCode(cellCode, 4, 0b001), is(nbr_plus_z));
		
		assertThat(cellLevel(nbrCode(cellCode, 4, 0b100)), is(4));
		assertThat(cellLevel(nbrCode(cellCode, 4, 0b010)), is(4));
		assertThat(cellLevel(nbrCode(cellCode, 4, 0b001)), is(4));
	}

	@Test
	public void testNeighborMinus() {
		assertThat(nbrCodeMinus(cellCode, 4, 0b100), is(nbr_minus_x));
		assertThat(nbrCodeMinus(nbr_plus_y, 4, 0b010), is(cellCode));
		assertThat(nbrCodeMinus(cellCode, 4, 0b001), is(nbr_minus_z));
		assertThat(cellLevel(nbrCodeMinus(cellCode, 4, 0b100)), is(4));
		assertThat(cellLevel(nbrCodeMinus(nbr_plus_y, 4, 0b010)), is(4));
		assertThat(cellLevel(nbrCodeMinus(cellCode, 4, 0b001)), is(4));
	}
	
	@Test
	public void testOverflow() {
		assertThat(isVertexOnBoundary(cellCode, 4), is(true));
		assertThat(nbrCodeMinus(cellCode, 4, 0b010), is(-1l));
	}
}
