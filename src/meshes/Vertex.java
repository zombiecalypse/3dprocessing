package meshes;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;

import static helpers.StaticHelpers.*;

/**
 * Implementation of a vertex for the {@link HalfEdgeStructure}
 */
public class Vertex extends HEElement {

	/** position */
	Point3f pos;
	/** adjacent edge: this vertex is startVertex of anEdge */
	HalfEdge anEdge;

	/** The index of the vertex, mainly used for toString() */
	public int index;

	public Vertex(Point3f v) {
		pos = v;
		anEdge = null;
	}

	public Point3f getPos() {
		return pos;
	}

	public void setHalfEdge(HalfEdge he) {
		anEdge = he;
	}

	public HalfEdge getHalfEdge() {
		return anEdge;
	}

	/**
	 * Get an iterator which iterates over the 1-neighbouhood
	 * 
	 * @return
	 */
	public Iterator<Vertex> iteratorVV() {
		return new IteratorVV();
	}

	/**
	 * Iterate over the incident edges
	 * 
	 * @return
	 */
	public Iterator<HalfEdge> iteratorVE() {
		return new IteratorVE();
	}

	/**
	 * Iterate over the neighboring faces
	 * 
	 * @return
	 */
	public Iterator<Face> iteratorVF() {
		return new IteratorVF();
	}

	public String toString() {
		return "" + index;
	}

	public boolean isAdjascent(Vertex w) {
		for (Vertex v : iter(iteratorVV())) {
			if (v == w) {
				return true;
			}
		}
		return false;
	}

	public final class IteratorVE implements Iterator<HalfEdge> {

		private HalfEdge first;
		private HalfEdge current;

		public IteratorVE() {
			this.first = Vertex.this.anEdge;
			this.current = null;
		}
		
		@Override
		public boolean hasNext() {
			return first != current;
		}

		@Override
		public HalfEdge next() {
			if (this.current == null) this.current = first;
			HalfEdge returned = this.current;
			this.current = current.opposite.next;
			return returned;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	public final class IteratorVV implements Iterator<Vertex> {
		private Iterator<HalfEdge> iter;
		
		public IteratorVV() {
			this.iter = new IteratorVE();
		}

		@Override
		public boolean hasNext() {
			return iter.hasNext();
		}

		@Override
		public Vertex next() {
			HalfEdge next = iter.next();
			return next.incident_v;
		}

		@Override
		public void remove() {
			iter.remove();
		}
	}
	
	public final class IteratorVF implements Iterator<Face> {
		private Iterator<HalfEdge> iter;
		private Face next = null;
		public IteratorVF() {
			this.iter = new IteratorVE();
		}

		@Override
		public boolean hasNext() {
			while (next == null) {
				if (!iter.hasNext()) {
					return false;
				}
				next = iter.next().getFace();
			}
			return true;
			
		}

		@Override
		public Face next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			Face returned = next;
			next = null;
			return returned;
		}

		@Override
		public void remove() {
			iter.remove();
		}
	}
}
