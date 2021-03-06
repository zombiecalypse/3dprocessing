package helpers;

import static helpers.StaticHelpers.cot;
import static helpers.StaticHelpers.iter;
import static helpers.StaticHelpers.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.vecmath.Vector3f;

import sparse.CSRMatrix;
import sparse.CSRMatrix.col_val;
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
			m.putOnce(v.index, v.index, -1);
			assert v.index < nVertices;
			List<Vertex> neighbors = list(v.iteratorVV());
			for (Vertex n : neighbors) {
				assert n.index < nVertices;
				m.putOnce(v.index, n.index, 1f / neighbors.size());
			}
		}
		final CSRMatrix csr = m.toCsr();
		return csr;
	}

	/**
	 * The unnormalized cotangent Laplacian
	 * 
	 * @param hs
	 * @param keepFixed 
	 * @param deform 
	 * @return
	 */
	public static SparseDictMatrix unnormalizedCotanLaplacianBare(HalfEdgeStructure hs, Set<Integer> keepFixed, Set<Integer> deform) {
		SparseDictMatrix m = new SparseDictMatrix();
		for (Vertex v : hs.getVertices()) {
			// In each vertex...
			if (!keepFixed.contains(v) && !v.isOnBoundary()) {
				for (HalfEdge e1 : iter(v.iteratorVE())) {
					// take every edge weighted by cotangens of the adjacent
					// angles
					HalfEdge e2 = e1.getOpposite();
					float alpha = (float) Math.max(0.2, e1.opposingAngle());
					float beta = (float) Math.max(0.2, e2.opposingAngle());
					float weight = cot(alpha) + cot(beta);
					m.putOnce(v.index, e2.start().index, -weight);
					m.add(v.index, v.index, weight);
				}
			} else if (deform.contains(v)){
				m.add(v.index, v.index, 1);
			} else {
				m.add(v.index, v.index, 0);
			}
		}
		return m;
	}
	
	public static CSRMatrix unnormalizedCotanLaplacian(HalfEdgeStructure hs, Set<Integer> keepFixed, Set<Integer> deform) {
		return unnormalizedCotanLaplacianBare(hs, keepFixed, deform).toCsr();
	}
	/**
	 * The cotangent Laplacian
	 * 
	 * @param hs
	 * @return
	 */
	public static SparseDictMatrix mixedCotanLaplacianBare(HalfEdgeStructure hs) {
		// doesn't seem to be wrong.
		SparseDictMatrix m = new SparseDictMatrix();
		for (Vertex v : hs.getVertices()) {
			// In each vertex...
			if (!v.isOnBoundary()) {
				float a_mixed2 = 2 * v.aMixed();
				for (HalfEdge e1 : iter(v.iteratorVE())) {
					// take every edge weighted by cotangens of the adjacent
					// angles
					HalfEdge e2 = e1.getOpposite();
					float alpha = (float) Math.max(0.2, e1.opposingAngle());
					float beta = (float) Math.max(0.2, e2.opposingAngle());
					float weight = cot(alpha) + cot(beta);
					float val = weight / a_mixed2;
					// cut off if too small
					m.putOnce(v.index, e2.start().index, -val);
					m.add(v.index, v.index, val);
				}
			} else {
				m.add(v.index, v.index, 0);
			}
		}
		return m;
	}
	/**
	 * The cotangent Laplacian
	 * @param hs
	 * @return
	 */
	public static CSRMatrix mixedCotanLaplacian(HalfEdgeStructure hs, boolean normalized){
		CSRMatrix m = new CSRMatrix(0, hs.getVertices().size());
		for(Vertex v: hs.getVertices()) {
			ArrayList<col_val> row = m.addRow();
			if (v.isOnBoundary()) 
				continue; //leave row empty
			float aMixed;
			if (normalized)
				aMixed = v.aMixed();
			else
				aMixed = 1/2f;
			Iterator<HalfEdge> iter = v.iteratorVE();
			float sum = 0;
			while(iter.hasNext()) {
				HalfEdge current = iter.next();
				float alpha = (float) Math.max(0.1, current.opposingAngle());
				float beta = (float) Math.max(0.1, current.getOpposite().opposingAngle());
				float cot_alpha = cot(alpha);
				float cot_beta = cot(beta);
				float entry = (cot_alpha + cot_beta)/(2*aMixed);
				sum += entry;
				row.add(new col_val(current.end().index, entry));
			}		
			row.add(new col_val(v.index, -sum));
			
			Collections.sort(row);
		}
		return m;
	}
	
	public static CSRMatrix mixedCotanLaplacian(HalfEdgeStructure hs){
		return mixedCotanLaplacian(hs, true);
	}

	/**
	 * A symmetric cotangent Laplacian, cf Assignment 4, exercise 4.
	 * 
	 * @param hs
	 * @return
	 */
	public static CSRMatrix symmetricCotanLaplacian(HalfEdgeStructure hs) {
		SparseDictMatrix m = new SparseDictMatrix();
		for (Vertex v : hs.getVertices()) {
			// In each vertex...
			if (!v.isOnBoundary()) {
				float a_mixed = v.aMixed();
				for (HalfEdge e1 : iter(v.iteratorVE())) {
					// take every edge weighted by cotangens of the adjacent
					// angles
					HalfEdge e2 = e1.getOpposite();
					float alpha = (float) Math.max(0.2, e1.opposingAngle());
					float beta = (float) Math.max(0.2, e2.opposingAngle());
					float weight = cot(alpha) + cot(beta);
					float a_mixed_other = e1.end().aMixed();
					float val = (float) (weight / (2* Math.sqrt(a_mixed * a_mixed_other)));
					// cut off if too small
					m.putOnce(v.index, e2.start().index, -val);
					m.add(v.index, v.index, val);
				}
			} else {
				m.add(v.index, v.index, 0);
			}
		}
		return m.toCsr();
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
