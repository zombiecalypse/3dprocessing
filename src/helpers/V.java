package helpers;

import java.io.IOException;
import java.util.ArrayList;

import javax.vecmath.Matrix3f;
import javax.vecmath.Point3f;
import javax.vecmath.Tuple4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import openGL.objects.Transformation;
import sparse.CSRMatrix;
import sparse.CSRMatrix.col_val;
import sparse.SCIPYEVD;
import sparse.SparseDictMatrix;

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
	

	/**
	 * This method is a hack to compute eigenvectors that will often work but
	 * will fail in various cases. it can't handle if the eigenspace is not 1
	 * dimensional, or if zero's pop up during the computations. But it will do
	 * for the simple bunny_ear.obj visualizations.
	 * 
	 * @param m
	 * @param eig
	 * @return
	 */
	public static Vector3f eigenVector(Matrix3f m, float eig) {
		Matrix3f mat = new Matrix3f(m);
		mat.m00 -= eig;
		mat.m11 -= eig;
		mat.m22 -= eig;

		if (mat.m00 != 0) {
			if (mat.m10 != 0) {
				float c = mat.m10 / mat.m00;
				mat.m10 = 0;
				mat.m11 -= c * mat.m01;
				mat.m12 -= c * mat.m02;
			}
			if (mat.m20 != 0) {
				float c = mat.m20 / mat.m00;
				mat.m20 = 0;
				mat.m21 -= c * mat.m01;
				mat.m22 -= c * mat.m02;
			}
		}
		if (mat.m11 != 0) {
			if (mat.m21 != 0) {
				float c = mat.m21 / mat.m11;
				mat.m21 = 0;
				mat.m22 -= c * mat.m12;
			}
			if (mat.m01 != 0) {
				float c = mat.m01 / mat.m11;
				mat.m01 = 0;
				mat.m02 -= c * mat.m12;
			}
		}

		Vector3f ev = new Vector3f();
		ev.z = 1;

		if (Math.abs(mat.m02) > 1e-4) {
			ev.x = -ev.z * mat.m02 / mat.m00;
		}

		if (Math.abs(mat.m12) > 1e-4) {
			ev.y = -ev.z * mat.m12 / mat.m11;
		}

		ev.normalize();
		return ev;
	}
	

	/**
	 * Compute the 3 Eigenvalues of a symmetric 3x3 matrix m. This nice
	 * algorithm is taken from wikipedia:
	 * http://en.wikipedia.org/wiki/Eigenvalue_algorithm
	 * 
	 * @param m
	 * @param evs
	 */
	public static float[] eigenValues(Matrix3f m) {
		float eig1, eig2, eig3;
		float p1 = m.m01 * m.m01 + m.m02 * m.m02 + m.m12 * m.m12;
		if (p1 == 0) {
			// A is diagonal.
			eig1 = m.m00;
			eig2 = m.m11;
			eig3 = m.m22;
		} else {
			float q = (m.m00 + m.m11 + m.m22) / 3;
			float p2 = (m.m00 - q) * (m.m00 - q) + (m.m11 - q) * (m.m11 - q)
					+ (m.m22 - q) * (m.m22 - q) + 2 * p1;
			float p = (float) Math.sqrt(p2 / 6);
			// B = (1 / p) * (A - q * I) // I is the identity matrix
			Matrix3f B = new Matrix3f(m);
			B.m00 -= q;
			B.m11 -= q;
			B.m22 -= q;
			B.mul(1.f / p);

			float r = B.determinant() / 2;

			// In exact arithmetic for a symmetric matrix -1 <= r <= 1
			// but computation error can leave it slightly outside this range.
			double phi;
			if (r <= -1)
				phi = Math.PI / 3;
			else if (r >= 1)
				phi = 0;
			else
				phi = Math.acos(r) / 3;

			// the eigenvalues satisfy eig3 <= eig2 <= eig1
			eig1 = q + 2 * p * (float) Math.cos(phi);
			eig3 = q - p
					* (float) (Math.cos(phi) + Math.sqrt(3) * Math.sin(phi));
			eig2 = 3 * q - eig1 - eig3; // since trace(A) = eig1 + eig2 + eig3
		}
		float[] evs = {eig1, eig2, eig3};
		return evs;
	}

	public static void assertSymmetric(SparseDictMatrix m) {
		assert m.cols == m.rows;
		for (int row = 0; row < m.rows; row++) {
			for (int col = 0; col < m.cols; col++) {
				Float rc = m.get(row, col);
				Float cr = m.get(col, row);
				float x = rc == null ? 0 : rc;
				float y = cr == null? 0 : cr;
				assert Math.abs(x-y) < 1e-5;
			}
		}
	}

	public static void assertPositiveDefinite(CSRMatrix mat) {
		ArrayList<Float> evs = new ArrayList<>();
		try {
			SCIPYEVD.doSVD(mat, "", 0, evs, new ArrayList<ArrayList<Float>>());
			for (Float e : evs) {
				assert e > 0;
			}
		} catch (IOException e) {
			assert e == null : e;
		}
	}
}
