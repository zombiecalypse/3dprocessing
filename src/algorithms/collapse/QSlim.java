package algorithms.collapse;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector4f;

import meshes.Ellipsoid;
import meshes.WireframeMesh;
import openGL.objects.Transformation;
import datastructure.halfedge.Face;
import datastructure.halfedge.HalfEdgeStructure;
import datastructure.halfedge.Vertex;
import static helpers.StaticHelpers.*;

/**
 * Implement the QSlim algorithm here
 * 
 * @author Alf
 * 
 */
public class QSlim {

	HalfEdgeStructure hs;
	Map<Vertex, Matrix4f> vertexQs = new HashMap<>();
	
	public QSlim(HalfEdgeStructure hs) {
		this.hs = hs;
		init();
	}
	
	public Matrix4f q(Vertex v) {
		return vertexQs.get(v);
	}

	/********************************************
	 * Use or discard the skeleton, as you like.
	 ********************************************/

	/**
	 * Compute per vertex matrices
	 * Compute edge collapse costs, 
	 * Fill up the Priority queue/heap or similar
	 */
	private void init() {
		for (Vertex v : hs.getVertices()) {
			Matrix4f t = new Matrix4f();
			for (Face f : iter(v.iteratorVF())) {
				t.add(f.errorQuadric());
			}
			this.vertexQs.put(v, t);
		}
	}

	/**
	 * The actual QSlim algorithm, collapse edges until the target number of
	 * vertices is reached.
	 * 
	 * @param target
	 */
	public void simplify(int target) {

	}

	/**
	 * Collapse the next cheapest eligible edge. ; this method can be called
	 * until some target number of vertices is reached.
	 */
	public void collapseEdge() {

	}

	/**
	 * Represent a potential collapse
	 * 
	 * @author Alf
	 * 
	 */
	protected class PotentialCollapse implements Comparable<PotentialCollapse> {

		@Override
		public int compareTo(PotentialCollapse arg1) {
			return -1;
		}
	}

}
