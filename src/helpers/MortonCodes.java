package helpers;

/**
 * Implement the Morton Code manipulations here.
 * 
 */
public class MortonCodes {

	/** the three masks for dilated integer operations */
	public static final long d100100 = 0b100100100100100100100100100100100100100100100100100100100100100L, // X
			d010010 = 0b010010010010010010010010010010010010010010010010010010010010010L, // Y
			d001001 = 0b001001001001001001001001001001001001001001001001001001001001001L; // Z

	public static final long X = d100100, Y = d010010, Z = d001001;

	/**
	 * return the parent morton code
	 * 
	 * @param code
	 * @return
	 */
	public static long parentCode(long code) {
		return code >> 3;
	}

	/**
	 * return the (positive) neighbor code at the relative position encoded by
	 * 0bxyz using dilated addition
	 * 
	 * @param v
	 * @param level
	 * @param w
	 * @return
	 */
	public static long nbrCode(long v, int level, long w) {
		long ret = ((((v | ~X) + (w & X)) & X) | (((v | ~Y) + (w & Y)) & Y) | (((v | ~Z) + (w & Z)) & Z));

		if (overflowTest(ret, level)) {
			System.err.format("overflowed with %s\n", Long.toBinaryString(ret));
			return -1L;
		}
		return ret;
	}

	/**
	 * return the (negative) neighbor code at the relative position encoded by
	 * 0bxyz using dilated subtraction
	 * 
	 * @param v
	 * @param level
	 * @param w
	 * @return
	 */
	public static long nbrCodeMinus(long v, int level, long w) {
		long ret = ((((v & X) - (w & X)) & X) | (((v & Y) - (w & Y)) & Y) | (((v & Z) - (w & Z)) & Z));

		if (overflowTest(ret, level)) {
			System.err.format("overflowed with %s\n", Long.toBinaryString(ret));
			return -1L;
		}
		return ret;
	}

	/**
	 * A test to check if an overflow/underflow has occurred. it is enough to
	 * test if the delimiter bit is untouched and is the highest bit set.
	 * 
	 * @param code
	 * @param level
	 * @return
	 */
	public static boolean overflowTest(long code, int level) {
		// implement this
		return !isCellOnLevelXGrid(code, level);
	}

	/**
	 * Check if the cell_code is a morton code associated to the grid level
	 * given in the argument. A cell code is associated to a specific level
	 * 
	 * @param cell_code
	 * @param level
	 * @return
	 */
	public static boolean isCellOnLevelXGrid(long cell_code, int level) {
		return (cell_code >> (3 * level)) == 1l;
	}

	/**
	 * A test to check if the vertex_code (a morton code padded with zeros to
	 * have the length 3*tree_depth + 1) is associated to a vertex which is part
	 * of the level-grid.
	 * 
	 * This is determined by the number of trailing zeros, and if a vertex lies
	 * on some level k it will lie on the levels k+1,k+2... tree_depth too.
	 */
	public static boolean isVertexOnLevelXGrid(long vertex_code, int level,
			int tree_depth) {
		return (vertex_code & level_mask(level, tree_depth)) == 0;
	}

	private static long level_mask(int level, int tree_depth) {
		return ~(-1L << (tree_depth - level)*3);
	}

	/**
	 * A test to check if a vertex code is logically describing a boundary
	 * vertex.
	 */
	public static boolean isVertexOnBoundary(long vertex_code, int tree_depth) {
		boolean is = (vertex_code & (0b111 << 3 * (tree_depth - 1))) != 0 || // x==1,
																				// y==1
																				// or
																				// z==1
																				// in
																				// a
																				// unit
																				// cube
				(vertex_code & d100100) == 0 || // x==0
				(vertex_code & d010010) == 0 || // y==0
				(vertex_code & d001001) == (0b1 << 3 * tree_depth); // z==0
																	// (only the
																	// delimiter
																	// bit is
																	// set)

		return is;
	}

	public static int cellLevel(long cell_code) {
		for (int i = 0; i < Long.SIZE; i++) {
			if (isCellOnLevelXGrid(cell_code, i)) {
				return i;
			}
		}
		assert false : "Not any cell level.";
		return -1;
	}

	public static int minVertexLevel(long vertex_code, int tree_depth) {
		for (int i = 0; i <= tree_depth; i++) {
			if (isVertexOnLevelXGrid(vertex_code, i, tree_depth)) {
				return i;
			}
		}
		assert false : "Not any Vertex level";
		return -1;
	}

	public static int maxVertexLevel(long vertex_code, int tree_depth) {
		for (int i = tree_depth + 1; i >= 0; i--) {
			if (isVertexOnLevelXGrid(vertex_code, i, tree_depth)) {
				return i;
			}
		}
		assert false : "Not any Vertex level";
		return -1;
	}

}
