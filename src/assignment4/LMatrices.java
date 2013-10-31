package assignment4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.vecmath.Vector3f;

import datastructure.halfedge.Face;
import datastructure.halfedge.HalfEdge;
import datastructure.halfedge.HalfEdgeStructure;
import datastructure.halfedge.Vertex;
import sparse.CSRMatrix;
import sparse.CSRMatrix.col_val;
import sparse.SparseDictMatrix;
import static helpers.StaticHelpers.*;

/**
 * Methods to create different flavours of the cotangent and uniform laplacian.
 * 
 * @author Alf
 * 
 */
public class LMatrices {

	/**
	 * The uniform Laplacian
	 * 
	 * @param hs
	 * @return
	 */
	public static CSRMatrix uniformLaplacian(HalfEdgeStructure hs) {
		SparseDictMatrix m = new SparseDictMatrix();
		int nVertices = hs.getVertices().size();
		for (Vertex v : hs.getVertices()) {
			m.putOnce(v.index, v.index, 1);
			assert v.index < nVertices;
			List<Vertex> neighbors = list(v.iteratorVV());
			for (Vertex n : neighbors) {
				assert n.index < nVertices;
				m.putOnce(v.index, n.index, -1f / neighbors.size());
			}
		}
		return m.toCsr();
	}

	/**
	 * The cotangent Laplacian
	 * 
	 * @param hs
	 * @return
	 */
	public static CSRMatrix mixedCotanLaplacian(HalfEdgeStructure hs) {
		SparseDictMatrix m = new SparseDictMatrix();
		for (Vertex v : hs.getVertices()) {
			Vector3f sum = new Vector3f();
			for (HalfEdge e1 : iter(v.iteratorVE())) {
				HalfEdge e2 = e1.getOpposite();
				float alpha = e1.opposingAngle();
				float beta = e2.opposingAngle();
				Vector3f sub = e1.asVector();
				sub.scale(cot(alpha) + cot(beta));
				sum.add(sub);
			}
			assert !Float.isNaN(sum.length());
			assert !Float.isInfinite(sum.length());
			float ret = sum.length() / (4 * Math.abs(aMixed(v)));
			assert !Float.isInfinite(ret);
			assert !Float.isNaN(ret);
			assert ret >= 0;
		}
		return null;
	}

	private static float aMixed(Vertex a) {
		float A_mixed = 0f;
		for (Face f : iter(a.iteratorVF())) {
			if (!f.obtuse()) {
				float mini_triags = 0;
				for (HalfEdge e : iter(f.iteratorFE())) {
					if (e.getFace() != f)
						e = e.getOpposite();
					assert e.getFace() == f;
					if (a == e.incident_v)
						continue;
					float n = e.asVector().lengthSquared();
					mini_triags += n / Math.tan(e.opposingAngle());
				}
				A_mixed += mini_triags / 8;
			} else if (f.angleIn(a) > Math.PI / 2) {
				A_mixed += f.area() / 2;
			} else {

				A_mixed += f.area() / 4;
			}
		}
		assert !Float.isNaN(A_mixed);
		return A_mixed;
	}

	/**
	 * A symmetric cotangent Laplacian, cf Assignment 4, exercise 4.
	 * 
	 * @param hs
	 * @return
	 */
	public static CSRMatrix symmetricCotanLaplacian(HalfEdgeStructure hs) {
		return null;
	}

	/**
	 * helper method to multiply x,y and z coordinates of the halfedge structure
	 * at once
	 * 
	 * @param m
	 * @param s
	 * @param res
	 */
	public static void mult(CSRMatrix m, HalfEdgeStructure s,
			ArrayList<Vector3f> res) {
		ArrayList<Float> x = new ArrayList<>(), b = new ArrayList<>(s
				.getVertices().size());
		x.ensureCapacity(s.getVertices().size());

		res.clear();
		res.ensureCapacity(s.getVertices().size());
		for (Vertex v : s.getVertices()) {
			x.add(0.f);
			res.add(new Vector3f());
		}

		for (int i = 0; i < 3; i++) {

			// setup x
			for (Vertex v : s.getVertices()) {
				switch (i) {
				case 0:
					x.set(v.index, v.getPos().x);
					break;
				case 1:
					x.set(v.index, v.getPos().y);
					break;
				case 2:
					x.set(v.index, v.getPos().z);
					break;
				}

			}

			m.mult(x, b);

			for (Vertex v : s.getVertices()) {
				switch (i) {
				case 0:
					res.get(v.index).x = b.get(v.index);
					break;
				case 1:
					res.get(v.index).y = b.get(v.index);
					break;
				case 2:
					res.get(v.index).z = b.get(v.index);
					break;
				}

			}
		}
	}
}
