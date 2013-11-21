package algorithms.collapse;

import helpers.V;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

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

	public static final Logger log = Logger.getLogger("Collapse");

	// collect the obsolete elements
	public HashSet<HalfEdge> deadEdges = new HashSet<>();
	public TreeSet<Vertex> deadVertices = new TreeSet<>();
	public HashSet<Face> deadFaces;

	// the half-edge structure we work on
	private HalfEdgeStructure hs;

	// store the original face normals for fold-over prevention
	public HashMap<Face, Vector3f> oldFaceNormals;

	// how strongly the normals are constrained to prevent flips.
	// A flip will be detected, if after a collapse oldNormal.dot(newNormal) <
	// flipConst.
	private static final float flipConst = 0.1f;// -0.8f;

	private static interface Executor {
		public void add(Instruction i);

		public void run();

		public void addAll(List<Instruction> removeFace);
	}

	private static class Interpreter implements Executor {
		@Override
		public void add(Instruction i) {
			log.fine(String.format("%s", i));
			i.execute();
		}

		@Override
		public void run() {
		}

		@Override
		public void addAll(List<Instruction> a) {
			for (Instruction i : a) {
				add(i);
			}
		}
	}

	private static class Collector implements Executor {
		List<Instruction> inst = new ArrayList<>();

		@Override
		public void add(Instruction a) {
			inst.add(a);
		}

		@Override
		public void run() {
			for (Instruction i : inst) {
				log.finer(String.format("%s", i));
				i.execute();
			}
			inst.clear();
		}

		@Override
		public void addAll(List<Instruction> a) {
			inst.addAll(a);
		}
	}

	/**
	 * 
	 * Initiate class variables...
	 * 
	 * @param hs
	 */
	public HalfEdgeCollapse(HalfEdgeStructure hs) {
		this.hs = hs;
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
		Point3f newPos = V.scaled_add(e.start().pos, 0.5f, e.end().pos, 0.5f);
		if (!this.isCollapseMeshInv(e, newPos)) {
			collapseEdge(e, newPos);
		}
	}

	void collapseEdge(HalfEdge e, Point3f newPos) {
		if (isEdgeDead(e))
			return;
		log.entering(HalfEdgeCollapse.class.toString(), "collapseEdge", e);
		log.finer(String.format("next: %s prev: %s", e.getNext(), e.prev));
		assert isEdgeCollapsable(e);
		assert !isCollapseMeshInv(e, newPos);

		// We will use deferred actions. No changes are executed
		// until after we constructed all changes. This simplifies
		// the fiddling with references.

		makeV2ERefSafe(e);
		Executor ins = new Collector();

		final Vertex end = e.end();
		ins.add(updateVertexPosition(end, newPos));

		Vertex deletedVertex = e.start();
		ins.add(delete(deletedVertex));

		log.finer(String.format("del: %s repl: %s", deletedVertex, end));

		HalfEdge eo = e.getOpposite();
		assert eo.end() == deletedVertex;

		for (HalfEdge outflux : iter(deletedVertex.iteratorVE())) {
			assert outflux.start() == deletedVertex;
			final HalfEdge influx = outflux.getOpposite();
			assert influx.end() == deletedVertex;
			if (influx == eo)
				continue;
			ins.add(updateEdgeVertexReference(influx, end, deletedVertex));
		}

		if (e.hasFace()) {
			ins.addAll(removeFace(e));
		} else {
			ins.addAll(removeFaceAtEdge(e));
		}

		if (eo.hasFace()) {
			ins.addAll(removeFace(eo));
		} else {
			ins.addAll(removeFaceAtEdge(eo));
		}

		ins.run();

		// Do a lot of assertions while debugging, either here
		// or in the calling method... ;-)
		// If something is wrong in the half-edge structure it is awful
		// to detect what it is that is wrong...

		assertEdgesOk(hs);
		assertVerticesOk(hs);
	}

	private List<Instruction> removeFaceAtEdge(HalfEdge e) {
		return Arrays.asList(stitchLength(e.getPrev(), e.getNext()), delete(e));
	}

	class StitchLength implements Instruction {
		private HalfEdge prev;
		private HalfEdge next;

		public StitchLength(HalfEdge prev, HalfEdge next) {
			this.prev = prev;
			this.next = next;
		}

		@Override
		public void execute() {
			prev.setNext(next);
			next.setPrev(prev);
			log.finer(String.format("Stitched: %s %s", prev, next));
		}

		public String toString() {
			return String.format("Stitch(%s, %s)", prev, next);
		}
	}

	private Instruction stitchLength(final HalfEdge prev, final HalfEdge next) {
		return new StitchLength(prev, next);
	}

	private List<Instruction> removeFace(HalfEdge e) {
		List<Instruction> ret = new ArrayList<>(Arrays.asList(
				glueEdge(e.getPrev(), e.getNext()), delete(e.getFace())));
		for (HalfEdge ee : iter(e.getFace().iteratorFE())) {
			ret.add(delete(ee));
		}
		return ret;
	}

	class UpdatePos implements Instruction {
		private Vertex v;
		private Point3f newPos;

		public UpdatePos(Vertex v, Point3f newPos) {
			this.v = v;
			this.newPos = newPos;
		}

		@Override
		public void execute() {
			v.pos = newPos;
		}

		public String toString() {
			return String.format("Move(%s => %s)", v, newPos);
		}
	}

	private Instruction updateVertexPosition(final Vertex v,
			final Point3f newPos) {
		return new UpdatePos(v, newPos);
	}

	class GlueEdge implements Instruction {
		private HalfEdge from_o;
		private HalfEdge to_o;
		private String from;
		private String to;

		public GlueEdge(HalfEdge from, HalfEdge to) {
			assert from.getOpposite() != to;
			assert to.getOpposite() != from;
			assert to.start() != from.end() || from.start() != to.end();
			this.from_o = from.getOpposite();
			this.to_o = to.getOpposite();
			this.from = from.toString();
			this.to = to.toString();
		}

		@Override
		public void execute() {
			from_o.setOpposite(to_o);
			to_o.setOpposite(from_o);
			assert from_o.start() == to_o.end();
			assert from_o.end() == to_o.start();
			log.finer(String.format("Glued: %s %s", from_o, to_o));
		}

		public String toString() {
			return String.format("Glue(%s => %s)", from, to);
		}
	}

	private Instruction glueEdge(final HalfEdge from, final HalfEdge to) {
		return new GlueEdge(from, to);
	}

	private Instruction delete(final Face face) {
		return new Instruction() {
			@Override
			public void execute() {
				deadFaces.add(face);
			}

			public String toString() {
				return String.format("del(%s)", face);
			}
		};
	}

	class UpdateEdgeVertexRef implements Instruction {
		private HalfEdge e;
		private Vertex v;

		public UpdateEdgeVertexRef(HalfEdge e, Vertex v) {
			assert e.start() != v;
			assert e.end() != v;
			this.e = e;
			this.v = v;
		}

		@Override
		public void execute() {
			e.setEnd(v);
			e.getNext().setStart(v);
			log.finer(String.format("Updated edge: %s, next: %s", e,
					e.getNext()));
		}

		public String toString() {
			return String.format("Link(%s => %s)", e, v);
		}
	}

	private Instruction updateEdgeVertexReference(final HalfEdge e,
			final Vertex v, Vertex deletedVertex) {
		assert e.end() == deletedVertex;
		assert e.start() != v;
		return new UpdateEdgeVertexRef(e, v);

	}

	private Instruction delete(final Vertex v) {
		return new Instruction() {
			@Override
			public void execute() {
				deadVertices.add(v);
			}

			public String toString() {
				return String.format("del(%s)", v);
			}
		};
	}

	private Instruction delete(final HalfEdge e) {
		return new Instruction() {
			@Override
			public void execute() {
				deadEdges.add(e);
			}

			public String toString() {
				return String.format("del(%s)", e);
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

		assertEdgesOk(hs);
		assertVerticesOk(hs);
		hs.enumerate();
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
		log.entering(HalfEdgeCollapse.class.toString(), "isEdgeCollapsable", e);

		HashSet<Vertex> set_a = new HashSet<>(set(e.start().iteratorVV()));
		HashSet<Vertex> set_b = new HashSet<>(set(e.end().iteratorVV()));
		set_a.retainAll(set_b);
		int commonNeighbors = set_a.size();

		// dont produce dangling edges!
		if (e.start().isOnBoundary() && e.end().isOnBoundary()
				&& !e.isOnBorder()) {
			log.finer(String.format(
					"Can't remove %s, because it'd produce dangling edges.", e));
			return false;
		}
		// don't delete the last triangle
		if (!e.hasFace() && e.getNext().getNext() == e.getPrev()) {
			log.finer(String.format(
					"Can't remove %s, because it's the last triag. 1", e));
			return false;
		}
		if (!e.getOpposite().hasFace()
				&& e.getOpposite().getNext().getNext() == e.getOpposite()
						.getPrev()) {
			log.finer(String.format(
					"Can't remove %s, because it's the last triag. 2", e));
			return false;
		}

		boolean ret = commonNeighbors == (e.isOnBorder() ? 1 : 2);
		if (!ret)
			log.finer(String
					.format("Can't remove %s, because it has the wrong number of common neighbors: %d",
							e, commonNeighbors));

		return ret;
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
		HalfEdge current, next, first;
		Vector3f e1 = new Vector3f(), e2 = new Vector3f(), n1 = new Vector3f(), n2 = new Vector3f();

		first = current = e.start().getHalfEdge();
		next = null;
		while (next != first) {
			// current = it.next();
			next = current.getPrev().getOpposite();
			if (next == e || current == e || !current.hasFace()) {
				current = next;
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
			current = next;
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
				assert (e.start() != e.end()) : e;
				assert (e.getOpposite().getOpposite() == e) : String.format(
						"(%s <> %s)", e, e.getOpposite());
				final HalfEdge prev = e.getPrev();
				assert (prev.end() == e.start()) : String.format("(%s => %s)",
						prev, e);
				final HalfEdge next = e.getNext();
				assert (next.start() == e.end()) : String.format("(%s => %s)",
						e, next);
				assert (prev.end() != next.start());

				assert (!deadEdges.contains(next.getPrev()));
				assert (!deadEdges.contains(prev.getNext()));

				assert (e == next.getPrev()) : String.format(
						"e: %s next.prev: %s", e, next.getPrev());
				assert (e == prev.getNext()) : String.format(
						"e: %s prev.next: %s", e, prev.getNext());

				assert (e.getFace() == next.getFace());
				assert (e.getFace() == prev.getFace());

				assert (!deadEdges.contains(prev));
				assert (!deadEdges.contains(next));
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
		Set<Edge> edges = getEdges();
		int toRemove = edges.size() - remainding;
		collapseNEdgesRandomly(toRemove);
	}

	public void collapseNEdgesRandomly(int toRemove) {
		Set<Edge> edges = getEdges();
		List<Edge> l = list(edges);
		Collections.shuffle(l);
		Iterator<Edge> it = l.iterator();
		while (toRemove > 0 && it.hasNext()) {
			Edge e = it.next();
			it.remove();
			if (isEdgeDead(e.h1) || isEdgeDead(e.h2))
				continue;
			if (isEdgeCollapsable(e.h1)) {
				collapseEdge(e.h1);
			}
			// Refill if necessary
			if (!it.hasNext()) {
				Collections.shuffle(l);
				it = l.iterator();
			}
			toRemove--;
		}
		finish();
	}

	private Set<Edge> getEdges() {
		Set<Edge> edges = new HashSet<>();
		for (Vertex v : hs.getVertices()) {
			for (HalfEdge h : iter(v.iteratorVE())) {
				edges.add(new Edge(h));
			}
		}
		return edges;
	}

	/** Edges are equal if their halfedges as a set are equal */
	static class Edge implements Comparable<Edge> {
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

		@Override
		public int compareTo(Edge o) {
			int key = Integer.compare(h1.id, o.h1.id);
			if (key != 0) {
				return key;
			}
			return Integer.compare(h2.id, o.h2.id);
		}
	}

	/** Abstract action that changes the topology of the mesh. */
	static interface Instruction {
		void execute();
	}

	public void collapseSmall(float f) {
		Set<Edge> edges = getEdges();
		Iterator<Edge> it = edges.iterator();
		boolean found = false;
		int index = 0;
		System.err.format("Checking %s edges\n", edges.size());
		while (it.hasNext()) {
			if (index++ % 1000 == 0) {
				System.err.print('.');
			}
			Edge e = it.next();
			it.remove();
			if (isEdgeDead(e.h1) || isEdgeDead(e.h2))
				continue;
			if (e.h1.asVector().length() <= f && isEdgeCollapsable(e.h1)) {
				collapseEdge(e.h1);
				found = true;
			}
//			if (!it.hasNext() && found) {
//				it = edges.iterator();
//			}
		}
		System.err.println();

		finish();
	}
}
