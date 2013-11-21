package helpers;

import javax.vecmath.Point3f;
import javax.vecmath.Tuple4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import openGL.objects.Transformation;
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
	
	/**
	 * helper method that might be useful..
	 * 
	 * @param p
	 * @param ppT
	 */
	public static Transformation ppT(Tuple4f p) {
		assert (p.x * 0 == 0);
		assert (p.y * 0 == 0);
		assert (p.z * 0 == 0);
		assert (p.w * 0 == 0);
		Transformation ppT = new Transformation();
		ppT.m00 = p.x * p.x;
		ppT.m01 = p.x * p.y;
		ppT.m02 = p.x * p.z;
		ppT.m03 = p.x * p.w;
		ppT.m10 = p.y * p.x;
		ppT.m11 = p.y * p.y;
		ppT.m12 = p.y * p.z;
		ppT.m13 = p.y * p.w;
		ppT.m20 = p.z * p.x;
		ppT.m21 = p.z * p.y;
		ppT.m22 = p.z * p.z;
		ppT.m23 = p.z * p.w;
		ppT.m30 = p.w * p.x;
		ppT.m31 = p.w * p.y;
		ppT.m32 = p.w * p.z;
		ppT.m33 = p.w * p.w;
		return ppT;

	}
}
