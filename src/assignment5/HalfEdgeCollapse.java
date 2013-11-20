package assignment5;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import datastructure.halfedge.Face;
import datastructure.halfedge.HalfEdge;
import datastructure.halfedge.HalfEdgeStructure;
import datastructure.halfedge.Vertex;
import static helpers.StaticHelpers.*;

/**
 * This class handles efficient half-edge collapsing on a half-edge structure.
 * 
 * @author Alf
 * 
 */
public class HalfEdgeCollapse {

	// collect the obsolete elements
	public HashSet<HalfEdge> deadEdges;
	public HashSet<Vertex> deadVertices;
	public HashSet<Face> deadFaces;

	// the half-edge structure we work on
	private HalfEdgeStructure hs;

	// store the original face normals for fold-over prevention
	public HashMap<Face, Vector3f> oldFaceNormals;

	// how strongly the normals are constrained to prevent flips.
	// A flip will be detected, if after a collapse oldNormal.dot(newNormal) <
	// flipConst.
	private static final float flipConst = 0.1f;// -0.8f;

	/**
	 * 
	 * Initiate class variables...
	 * 
	 * @param hs
	 */
	public HalfEdgeCollapse(HalfEdgeStructure hs) {
		this.hs = hs;
		this.deadEdges = new HashSet<>();
		this.deadVertices = new HashSet<>();
		this.deadFaces = new HashSet<>();

		this.oldFaceNormals = new HashMap<>();
		for (Face f : hs.getFaces()) {

			// NAN/infinite Normals.
			// f.normal should be the face normal,
			// simply computed by cross(edge1, edge2).normalize
			if (f.normal().length() * 0 != 0) {
				oldFaceNormals.put(f, new Vector3f(0, 0, 0));
			} else {
				// f.normal should be the face normal,
				// simply computed by cross(edge1, edge2).normalize
				oldFaceNormals.put(f, f.normal());
			}
		}

		for (Face f : hs.getFaces()) {
			assert (oldFaceNormals.get(f).length() * 0 == 0);
		}

	}

	/**
	 * collapse a single halfedge, but don't remove the dead halfedges faces and
	 * vertices from the halfedges structure.
	 * 
	 * @param e
	 * @param hs
	 */
	void collapseEdge(HalfEdge e) {
		Point3f newPos = new Point3f(e.incident_v.pos);
		if (this.isCollapseMeshInv(e, newPos)) {
			collapseEdge(e, e.incident_v.pos);
		}
	}

	void collapseEdge(HalfEdge e, Point3f newPos) {
		assert isEdgeCollapsable(e);
		assert isCollapseMeshInv(e, newPos);

		// We will use deferred actions. No changes are executed
		// until after we constructed all changes. This simplifies
		// the fiddling with references.
		List<Instruction> ins = new ArrayList<>();

		// First step:
		// relink the vertices to safe edges. don't iterate
		// around e.end() before the collapse is finished.

		// Standard case:
		if (!e.isOnBorder()) {
			// e, e.opposite and e.start() are deleted.
			Vertex deletedVertex = e.incident_v;
			HalfEdge eo = e.getOpposite();
			ins.add(delete(e));
			ins.add(delete(eo));
			ins.add(delete(deletedVertex));
			// References to e.start() go to e.end()
			// References to e and its opposite die.
			for (HalfEdge influx : iter(deletedVertex.iteratorVE())) {
				ins.add(updateEdgeVertexReference(influx.getOpposite(),
						e.start(), deletedVertex));
			}
			// e.face, e.opposite.face are deleted.
			// References to the faces die.
			ins.add(delete(e.getFace()));
			ins.add(delete(e.getOpposite().getFace()));
			// The edge circles around e.face and e.opposite.face die
			// Only e(.opposite).next and e(.opposite).prev are alive at this
			// point

			ins.add(delete(e.prev));
			ins.add(delete(e.getNext()));
			// e.next.opposite is stitched to e.prev.opposite
			ins.add(stitchEdge(e.prev, e.getNext()));
			// e.next.v references e.prev.opposite now
			if (e.incident_v.anEdge == e.getNext()) {
				ins.add(updateVertexEdge(e.incident_v, e.prev.getOpposite(),
						e.getNext()));
			}
			if (e.getNext().incident_v.anEdge == e.prev) {
				ins.add(updateVertexEdge(e.getNext().incident_v, e.getNext()
						.getOpposite(), e.prev));
			}

			ins.add(delete(eo.prev));
			ins.add(delete(eo.getNext()));
			// e.next.opposite is stitched to e.prev.opposite
			ins.add(stitchEdge(eo.prev, eo.getNext()));
			// e.next.v references e.prev.opposite now
			if (eo.incident_v.anEdge == eo.getNext()) {
				ins.add(updateVertexEdge(eo.incident_v, eo.prev.getOpposite(),
						eo.getNext()));
			}
			if (eo.getNext().incident_v.anEdge == e.prev) {
				ins.add(updateVertexEdge(eo.getNext().incident_v, eo.getNext()
						.getOpposite(), eo.prev));
			}
			ins.add(updateVertexPosition(e.getNext().incident_v, newPos));
		} else if (e.hasFace()) {
		}
		
		 makeV2ERefSafe(e);

		for (Instruction i : ins) {
			i.execute();
		}

		// Do a lot of assertions while debugging, either here
		// or in the calling method... ;-)
		// If something is wrong in the half-edge structure it is awful
		// to detect what it is that is wrong...

		assertEdgesOk(hs);
		assertVerticesOk(hs);
	}

	private Instruction updateVertexPosition(final Vertex v,
			final Point3f newPos) {
		return new Instruction() {
			@Override
			public void execute() {
				v.pos = newPos;
			}
		};
	}

	private Instruction updateVertexEdge(final Vertex v, final HalfEdge e,
			HalfEdge deleted) {
		assert v.anEdge == deleted : String.format("Expected: %s was %s",
				deleted, v.anEdge);
		return new Instruction() {
			@Override
			public void execute() {
				v.anEdge = e;
			}
		};
	}

	private Instruction stitchEdge(final HalfEdge from, final HalfEdge to) {
		if (to.incident_v.anEdge == from) {
			System.out.println("bla");
		}
		return new Instruction() {
			HalfEdge from_o = from.getOpposite();
			HalfEdge to_o = to.getOpposite();

			@Override
			public void execute() {
				from_o.setOpposite(to_o);
				to_o.setOpposite(from_o);
			}
		};
	}

	private Instruction delete(final Face face) {
		return new Instruction() {
			@Override
			public void execute() {
				deadFaces.add(face);
			}
		};
	}

	private Instruction updateEdgeVertexReference(final HalfEdge e,
			final Vertex v, Vertex deletedVertex) {
		assert e.incident_v == deletedVertex;
		return new Instruction() {
			@Override
			public void execute() {
				e.incident_v = v;
			}
		};

	}

	private Instruction delete(final Vertex v) {
		return new Instruction() {
			@Override
			public void execute() {
				deadVertices.add(v);
			}
		};
	}

	private Instruction delete(final HalfEdge e) {
		return new Instruction() {
			@Override
			public void execute() {
				deadEdges.add(e);
			}
		};
	}

	/**
	 * collapse a single halfedge, remove all obsolete elements and re-enumerate
	 * the remaining vertices. Inefficient method.
	 * 
	 * @param e
	 * @param hs
	 */
	public void collapseEdgeAndDelete(HalfEdge e) {
		collapseEdge(e);
		finish();
	}

	/**
	 * Delete the collected dead vertices, faces, edges and tidy up the Halfedge
	 * structure.
	 */
	void finish() {

		hs.getFaces().removeAll(deadFaces);
		hs.getVertices().removeAll(deadVertices);
		hs.getHalfEdges().removeAll(deadEdges);
		hs.enumerate();

		assertEdgesOk(hs);
		assertVerticesOk(hs);
	}

	/**
	 * Tests if an edge is collapsable without producing a invalid mesh or a
	 * mesh with a different topology, as discussed in the exercise session.
	 * 
	 * @param e
	 * @return
	 */
	public static boolean isEdgeCollapsable(HalfEdge e) {
		// 1-neighborhood(e.start) \cap 1-neighborhood(e.end)
		int commonNeighbors = 0;

		Iterator<Vertex> it_a = e.start().iteratorVV();
		Iterator<Vertex> it_b;
		while (it_a.hasNext()) {
			Vertex nb_a = it_a.next();
			Vertex nb_b;
			it_b = e.end().iteratorVV();
			while (it_b.hasNext()) {
				nb_b = it_b.next();
				commonNeighbors += (nb_b == nb_a ? 1 : 0);
			}
		}

		// dont produce dangling edges!
		if (e.start().isOnBoundary() && e.end().isOnBoundary()
				&& !e.isOnBorder()) {
			return false;
		}
		// don't delete the last triangle
		if (!e.hasFace() && e.getNext().getNext() == e.getPrev()) {
			return false;
		}
		if (!e.getOpposite().hasFace()
				&& e.getOpposite().getNext().getNext() == e.getOpposite()
						.getPrev()) {
			return false;
		}

		return commonNeighbors == (e.isOnBorder() ? 1 : 2);

	}

	/**
	 * Simple, unrefined, heuristic mesh inversion detection
	 * 
	 * @param e
	 * @param newPos
	 * @return
	 */
	public boolean isCollapseMeshInv(HalfEdge e, Point3f newPos) {
		return isFaceFlipStart(e, newPos)
				|| isFaceFlipStart(e.getOpposite(), newPos);
	}

	/**
	 * Helper method, will check for face flips around e.start().
	 * 
	 * @param e
	 * @param newPos
	 * @return
	 */
	private boolean isFaceFlipStart(HalfEdge e, Point3f newPos) {
		Iterator<HalfEdge> it = e.start().iteratorVE();
		HalfEdge current, next;

		Vector3f e1 = new Vector3f(), e2 = new Vector3f(), n1 = new Vector3f(), n2 = new Vector3f();

		while (it.hasNext()) {
			current = it.next();
			next = current.getPrev().getOpposite();
			if (next == e || current == e || !current.hasFace()) {

				continue;
			}
			n1.set(oldFaceNormals.get(current.getFace()));

			e1.set(newPos);
			e1.negate();
			e1.add(current.end().getPos());
			e2.set(newPos);
			e2.negate();
			e2.add(next.end().getPos());
			n2.cross(e1, e2);

			// Degenerated faces have 0 normals and flips on
			// them will not be detected.
			if (n1.length() > 0 && n2.length() > 0
					&& n1.dot(n2) / n1.length() / n2.length() < flipConst) {
				return true;
			}

		}

		return false;
	}

	/**
	 * This helper method will make sure that none of the vertices reference an
	 * edge that becomes obsolete during the (half) edge collapse e.start() ->
	 * e.end().
	 * <p>
	 * Note that in some cases there exists no non-obsolete-becoming edge at the
	 * vertex e.end() that points away from e.end(), namely in the case of an
	 * edge collapse on a boundary triangle: __/\__ In that case, e.end() is set
	 * to reference an appropriate edge from e.start(); this means that after
	 * calling this method, until the edge is completely collapsed, iterators
	 * around e.end() will behave irregularly.
	 * </p>
	 * 
	 * @param e
	 */
	private void makeV2ERefSafe(HalfEdge e) {

		HalfEdge e_opp = e.getOpposite();
		Vertex b = e.end();
		Vertex c = e.getNext().end();
		Vertex d = e_opp.getNext().end();

		// find an edge at vertex b which does not become obsolete,
		// if this is impossible, an edge from a is taken!
		if (e.hasFace()) {
			b.setHalfEdge(e.getNext().getOpposite().getNext());
			if (e.getNext().getOpposite().getNext() == e.getOpposite()) {
				b.setHalfEdge(e_opp.getNext().getOpposite().getNext());
			}
		}
		// handle the case of e being on the outer boundary
		else {
			b.setHalfEdge(e.getNext());
		}

		// find non-obsolete edges at the vertices c and d
		if (e.hasFace()) {
			c.setHalfEdge(e.getNext().getOpposite());
		}
		if (e_opp.hasFace()) {
			d.setHalfEdge(e.getOpposite().getNext().getOpposite());
		}
	}

	/**
	 * is v obsolete?
	 * 
	 * @param v
	 * @return
	 */
	public boolean isVertexDead(Vertex v) {
		return deadVertices.contains(v);
	}

	/**
	 * is e obsolete?
	 * 
	 * @param e
	 * @return
	 */
	public boolean isEdgeDead(HalfEdge e) {
		return deadEdges.contains(e);
	}

	/**
	 * is f obsolete?
	 * 
	 * @param f
	 * @return
	 */
	public boolean isFaceDead(Face f) {
		return deadFaces.contains(f);
	}

	/**
	 * Valueable assertion
	 * 
	 * @param vs
	 */
	private void assertVerticesDontRefZombies(Vertex... vs) {
		for (Vertex v : vs) {
			assert (!deadEdges.contains(v.getHalfEdge()));
		}
	}

	/**
	 * Valueable assertions.
	 * 
	 * @param h
	 */
	public void assertEdgesOk(HalfEdgeStructure h) {
		for (HalfEdge e : h.getHalfEdges()) {
			if (!isEdgeDead(e)) {
				assert (e.start() != e.end());
				assert (e.getOpposite().getOpposite() == e);
				assert (e.getPrev().end() == e.start());
				assert (e.getNext().start() == e.end());
				assert (e.getPrev().end() != e.getNext().start());

				assert (e == e.getNext().getPrev());
				assert (e == e.getPrev().getNext());

				assert (e.getFace() == e.getNext().getFace());
				assert (e.getFace() == e.getPrev().getFace());

				assert (!deadEdges.contains(e.getPrev()));
				assert (!deadEdges.contains(e.getNext()));
				assert (!deadEdges.contains(e.getOpposite()));

				assert (e.getFace() == null || !isFaceDead(e.getFace()));
				assert (!isVertexDead(e.end()));
				assert (!isVertexDead(e.start()));
			}
		}
	}

	/**
	 * Valueable assertions.
	 * 
	 * @param h
	 */
	public void assertVerticesOk(HalfEdgeStructure h) {
		for (Vertex v : h.getVertices()) {
			if (!isVertexDead(v)) {
				assert (!isEdgeDead(v.getHalfEdge()));
				assert (v.getHalfEdge().start() == v);
			}
		}
	}

	public void collapseEdgesRandomly(int remainding) {
		Set<Edge> edges = new HashSet<>();
		for (Vertex v : hs.getVertices()) {
			for (HalfEdge h : iter(v.iteratorVE())) {
				edges.add(new Edge(h));
			}
		}
		int removed = edges.size() - remainding;
		List<Edge> toDelete = sample(list(edges), removed);
		for (Edge e : toDelete) {
			if (isEdgeCollapsable(e.h1)) {
				collapseEdge(e.h1);
			}
		}
		finish();
	}

	/** Edges are equal if their halfedges as a set are equal */
	static class Edge {
		public Edge(HalfEdge h1) {
			this.h1 = Objects.requireNonNull(h1);
			this.h2 = Objects.requireNonNull(h1.getOpposite());
		}

		HalfEdge h1;
		HalfEdge h2;

		@Override
		public int hashCode() {
			// Note that it commutes.
			return h1.hashCode() ^ h2.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Edge other = (Edge) obj;
			if (!(h1.equals(other.h1) || h1.equals(other.h2))) {
				return false;
			} else if (!(h2.equals(other.h2) || h2.equals(other.h1))) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return String.format("Edge%s",
					sorted(Arrays.asList(h1.incident_v, h2.incident_v)));
		}
	}

	/** Abstract action that changes the topology of the mesh. */
	static interface Instruction {
		void execute();
	}

	static class SetVertexReference implements Instruction {
		final Vertex v;
		final HalfEdge goal;

		public SetVertexReference(Vertex v, HalfEdge goal, HalfEdge toBeDeleted) {
			assert v.anEdge == toBeDeleted;
			this.v = v;
			this.goal = goal;
		}

		@Override
		public void execute() {
			v.anEdge = goal;
		}
	}
}
