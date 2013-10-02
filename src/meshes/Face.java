package meshes;


import static helpers.StaticHelpers.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.vecmath.Vector3f;

/**
 * Implementation of a face for the {@link HalfEdgeStructure}
 *
 */
public class Face extends HEElement {

	//an adjacent edge, which is positively oriented with respect to the face.
	private HalfEdge anEdge;
	int index;
	
	public Face(){
		anEdge = null;
	}
	
	public Face(HalfEdge he) {
		anEdge = he;
	}

	public void setHalfEdge(HalfEdge he) {
		this.anEdge = he;
	}

	public HalfEdge getHalfEdge() {
		return anEdge;
	}
	
	
	/**
	 * Iterate over the vertices on the face.
	 * @return
	 */
	public Iterator<Vertex> iteratorFV(){
		return new IteratorFV(anEdge);
	}
	
	/**
	 * Iterate over the adjacent edges
	 * @return
	 */
	public Iterator<HalfEdge> iteratorFE(){
		return new IteratorFE(anEdge);
	}
	
	public String toString(){
		if(anEdge == null){
			return "f: not initialized";
		}
		String s = "f: [";
		Iterator<Vertex> it = this.iteratorFV();
		while(it.hasNext()){
			s += it.next().toString() + " , ";
		}
		s+= "]";
		return s;
		
	}
	
	

	/**
	 * Iterator to iterate over the edges on a face
	 * @author Aaron
	 *
	 */
	public final class IteratorFE implements Iterator<HalfEdge> {
		
		
		private HalfEdge first, actual;

		public IteratorFE(HalfEdge anEdge) {
			assert anEdge != null;
			first = anEdge;
			actual = null;
		}

		@Override
		public boolean hasNext() {
			return actual == null || actual.next != first;
		}

		@Override
		public HalfEdge next() {
			//make sure eternam iteration is impossible
			if(!hasNext()){
				throw new NoSuchElementException();
			}

			//update what edge was returned last
			actual = (actual == null?
						first:
						actual.next);
			assert actual != null;
			return actual;
		}

		
		@Override
		public void remove() {
			//we don't support removing through the iterator.
			throw new UnsupportedOperationException();
		}

		/**
		 * return the face this iterator iterates around
		 * @return
		 */
		public Face face() {
			return first.incident_f;
		}
	}
	
	/**
	 * Iterator to iterate over the vertices on a face
	 * @author Aaron
	 *
	 */
	public final class IteratorFV implements Iterator<Vertex> {
		private IteratorFE iterator;

		public IteratorFV(HalfEdge anEdge) {
			this.iterator = new IteratorFE(anEdge);
		}

		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public Vertex next() {
			HalfEdge n = iterator.next();
			assert n != null;
			return n.incident_v;
		}

		@Override
		public void remove() {
			iterator.remove();
		}
	}

	public Vector3f normal() {
		assert len(this.iteratorFV()) == 3;
		Iterator<HalfEdge> edges = iteratorFE();
		Vector3f edge1 = edges.next().asVector();
		Vector3f edge2 = edges.next().asVector();
		Vector3f normal = new Vector3f();
		normal.cross(edge1, edge2);
		return normal;
	}

	public float angleIn(Vertex v) {
		List<Vector3f> adj_edges = new ArrayList<Vector3f>();
		for (HalfEdge e: iter(iteratorFE())) {
			if (e.end() == v || e.start() == v) {
				adj_edges.add(e.asVector());
			}
		}
		assert len(adj_edges) == 2;
		Vector3f e1 = adj_edges.get(0);
		Vector3f e2 = adj_edges.get(1);
		return e1.angle(e2);
	}

	public boolean obtuse() {
		for (Vertex v: iter(this.iteratorFV())) {
			float angle = angleIn(v);
			if (angle > Math.PI/2 && angle < Math.PI)
				return true;
		}
		return false;
	}

	public float area() {
		Iterator<HalfEdge> adj_edges = iteratorFE();
		Vector3f e1 = adj_edges.next().asVector();
		Vector3f e2 = adj_edges.next().asVector();
		Vector3f normal = new Vector3f();
		normal.cross(e1, e2);
		return norm(normal)/2;
	}

}
