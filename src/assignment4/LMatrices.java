package assignment4;

import static helpers.StaticHelpers.cot;
import static helpers.StaticHelpers.iter;
import static helpers.StaticHelpers.list;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector3f;

import sparse.CSRMatrix;
import sparse.SparseDictMatrix;
import datastructure.halfedge.Face;
import datastructure.halfedge.HalfEdge;
import datastructure.halfedge.HalfEdgeStructure;
import datastructure.halfedge.Vertex;

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
		  // In each vertex...
		  float a_mixed4 = 4 * Math.abs(aMixed(v));
			for (HalfEdge e1 : iter(v.iteratorVE())) {
			  // take every edge weighted by cotangens of the adjacent angles
				HalfEdge e2 = e1.getOpposite();
				float alpha = (float) Math.max(e1.opposingAngle(), 1e-2);
				float beta = (float) Math.max(e2.opposingAngle(), 1e-2);
				float weight = cot(alpha) + cot(beta);
				m.putOnce(v.index, e1.incident_v.index, -1.f/weight/a_mixed4);
				m.add(v.index, v.index, 1.f/weight/a_mixed4);
			}
		}
		return m.toCsr();
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
