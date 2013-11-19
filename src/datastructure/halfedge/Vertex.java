package datastructure.halfedge;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.vecmath.Point3f;

import static helpers.StaticHelpers.*;

/**
 * Implementation of a vertex for the {@link HalfEdgeStructure}
 */
public class Vertex extends HEElement implements Cloneable, Comparable<Vertex> {

	/** position */
	public Point3f pos;
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
	
	public float aMixed() {
		float aMixed = 0;
		for(Face f : iter(iteratorVF())) {
			final float mixedVoronoiCellArea = f.mixedVoronoiCellArea(this);
			aMixed += mixedVoronoiCellArea;
		}
		return aMixed;
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

	@Override
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
			if (this.current == null)
				this.current = first;
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

	public HalfEdge edgeBetween(Vertex a) {
		for (HalfEdge e : iter(this.iteratorVE())) {
			if ((e.start() == a && e.end() == this)
					|| (e.start() == this && e.end() == a))
				return e;
		}
		throw new AssertionError(String.format("No edge between %s and %s",
				this, a));
	}

	public boolean isOnBoundary() {
		for (HalfEdge e : iter(iteratorVE())) {
			if (e.isOnBorder())
				return true;
		}
		return false;
	}

	@Override
	public int compareTo(Vertex o) {
		return Integer.compare(index, o.index);
	}
}
