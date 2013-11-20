package helpers;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import sparse.CSRMatrix;
import sparse.CSRMatrix.col_val;

/**
 * Class that makes vector arithmetic functional.
 * @author aaron
 *
 */
public final class V {
	public static Point3f add(Point3f v1, Vector3f v2) {
		Point3f p = new Point3f(v1);
		p.add(v2);
		return p;
	}
	
	public static Point3f scaled_add(Point3f v1, float w1, Point3f v2, float w2) {
		Point3f p = new Point3f();
		Point3f vec1 = new Point3f(v1);
		Point3f vec2 = new Point3f(v2);
		// You'd think scaleAdd would do this. And you would be wrong. And ask yourself
		// why your ball is so spiky. And cry a little.
		vec1.scale(w1);
		vec2.scale(w2);
		p.add(vec1);
		p.add(vec2);
		return p;
	}

	public static Point3f sub(Point3f x, Point3f y) {
		Point3f n = new Point3f(x);
		n.sub(y);
		return n;
	}

	public static CSRMatrix I(int nRows) {
		CSRMatrix i = new CSRMatrix(nRows, nRows);
		for (int j = 0; j < nRows; j++) {
			i.addRow().add(new col_val(j, 1));
		}
		return i;
	}
}
