package datastructure.halfedge;

import static helpers.StaticHelpers.count;
import static helpers.StaticHelpers.zip;
import helpers.MyFunctions;
import helpers.StaticHelpers.Indexed;
import helpers.StaticHelpers.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import meshes.Point2i;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;

import com.google.common.base.Function;
import com.google.common.base.Functions;

/**
 * An implementation of a half-edge Structure. This particular implementation
 * can only handle orientable Manifold meshes with or without borders. This
 * structure explicitely stores a list of {@link HalfEdge}s, {@link Face}s and
 * {@link Vertex}s.
 *
 * <p>
 * Every half-edge has an opposite, next and prev, but on boundary edges one
 * edge of the half-edge pair will have a null face.
 * </p>
 * <p>
 * Initialize the HalfEdgeStructure with the {@link #init(WireframeMesh)} method
 * </p>
 *
 * @author bertholet
 *
 */

public class HalfEdgeStructure {

	ArrayList<HalfEdge> edges = new ArrayList<>();
	ArrayList<Face> faces = new ArrayList<>();
	ArrayList<Vertex> vertices = new ArrayList<>();
	HashMap<String, Function<Indexed<Vertex>, Float>> extractors1d = new HashMap<>();
	HashMap<String, Function<Indexed<Vertex>, Tuple3f>> extractors3d = new HashMap<>();
	HashMap<String, Function<Indexed<Face>, Tuple3f>> extractorsFace = new HashMap<>();
	String title;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public HashMap<String, Function<Indexed<Vertex>, Float>> getExtractors1d() {
		return extractors1d;
	}

	public void putExtractorPure(String name, Function<Vertex, Float> f) {
		extractors1d.put(name, Functions.compose(f, MyFunctions.<Vertex>value()));
	}

	public ArrayList<Vertex> getVertices() {
		return vertices;
	}

	public ArrayList<HalfEdge> getHalfEdges() {
		return edges;
	}

	public ArrayList<Face> getFaces() {
		return faces;
	}

	/**
	 * Get an Iterator over all Vertices
	 *
	 * @return
	 */
	public Iterator<Vertex> iteratorV() {
		return vertices.iterator();
	}

	/**
	 * Get an Iterator over all Edges
	 *
	 * @return
	 */
	public Iterator<HalfEdge> iteratorE() {
		return edges.iterator();
	}

	/**
	 * Get an Iterator over all Faces
	 *
	 * @return
	 */
	public Iterator<Face> iteratorF() {
		return faces.iterator();
	}
	
	public HalfEdgeStructure() { }

	public HalfEdgeStructure(WireframeMesh m) throws MeshNotOrientedException,
			DanglingTriangleException {
		this();
		init(m);
	}

	public HalfEdgeStructure(HalfEdgeStructure h) {
		this();
		init(h);
	}

	public void init(HalfEdgeStructure h) {
		for (Vertex v : h.vertices) {
			Vertex nv = new Vertex(new Point3f(v.pos.x, v.pos.y, v.pos.z));
			nv.index = v.index;
			vertices.add(nv);
		}
		for (Face f : h.faces) {
			Face nf = new Face();
			nf.index = f.index;
			faces.add(nf);
		}

		for (HalfEdge he : h.edges) {
			Face adj_face = null;
			if (he.incident_f != null) {
				adj_face = faces.get(he.incident_f.index);
			}
			Vertex adj_vertex = vertices.get(he.incident_v.index);
			HalfEdge new_he = new HalfEdge(adj_face, adj_vertex);
			new_he.index = he.index;
			edges.add(new_he);
		}

		this.enumerate();

		for (Pair<HalfEdge, HalfEdge> p : zip(h.edges, edges)) {
			p.b.opposite = edges.get(p.a.opposite.index);
			p.b.next = edges.get(p.a.next.index);
			p.b.prev = edges.get(p.a.prev.index);

			assert p.b.opposite != null;
			assert p.a.incident_f == null || p.b.incident_f != null;
			assert p.b.incident_v != null;
			assert p.b.next != null;
			assert p.b.prev != null;
		}

		for (Pair<Face, Face> p : zip(h.faces, faces)) {
			p.b.setHalfEdge(edges.get(p.a.getHalfEdge().index));
			assert p.a.getHalfEdge() == null || p.b.getHalfEdge() != null;
		}

		for (Pair<Vertex, Vertex> p : zip(h.vertices, vertices)) {
			p.b.anEdge = edges.get(p.a.anEdge.index);
			assert p.a.anEdge == null || p.b.anEdge != null;
			assert p.a.pos.epsilonEquals(p.b.pos, 1e-10f);
		}

		for (Pair<HalfEdge, HalfEdge> p : zip(h.edges, edges)) {
			assert p.a.index == p.b.index;
			assert p.a.incident_v.index == p.b.incident_v.index;
			assert p.a.incident_f == null || p.a.incident_f.index == p.b.incident_f.index;
			assert p.a.next == null || p.b.next.index == p.a.next.index;
			assert p.a.prev == null || p.b.prev.index == p.a.prev.index;
		}

		for (Pair<Vertex, Vertex> p : zip(h.vertices, vertices)) {
			assert p.a.index == p.b.index;
			assert p.a.anEdge.index == p.b.anEdge.index;
		}

		for (Pair<Face, Face> p : zip(h.faces, faces)) {
			assert p.a.index == p.b.index;
			assert p.a.getHalfEdge().index == p.b.getHalfEdge().index;
		}
	}

	/**
	 * Create a Halfedge Structure from a wireframe mesh described by the vertex
	 * Positions verts and fcs; this method throws a MeshNotOrientedException if
	 * the input mesh is not oriented consistently.
	 *
	 * Runs in O(n*m), where n is the number of faces and m is the average
	 * number of vertices per face
	 *
	 * @param verts
	 * @param fcs
	 * @param v_per_face
	 * @throws MeshNotOrientedException
	 * @throws DanglingTriangleException
	 *             when dangling Triangles are detected
	 */
	public void init(WireframeMesh m) throws MeshNotOrientedException,
			DanglingTriangleException {

		// add all vertices
		for (Point3f v : m.vertices) {
			vertices.add(new Vertex(v));

		}

		// local vars...
		Point2i key;
		HalfEdge he = null;
		HalfEdge first_he = null;
		HalfEdge prev_he = null;
		HalfEdge next_he;

		HashMap<Point2i, HalfEdge> edgeTable = new HashMap<Point2i, HalfEdge>(
				m.faces.size() + m.vertices.size());

		// for every face:
		int[] fc;
		for (int i = 0; i < m.faces.size(); i++) {

			// add the face
			faces.add(new Face());

			fc = m.faces.get(i);
			// add all adjascent edges
			for (int j = 0; j < fc.length; j++) {

				key = new Point2i(fc[j], fc[(j + 1) % fc.length]);

				// check well-orientedness:
				// every edge is found once if the mesh is oriented consistently
				if (edgeTable.containsKey(key)) {
					throw new MeshNotOrientedException();
				}

				// create a new half edge
				he = new HalfEdge(faces.get(faces.size() - 1), // the face that
																// just was
																// added
						vertices.get(fc[(j + 1) % fc.length])); // the vertex
																// the edge is
																// pointing to

				// link between previous halfedge and the new halfedge
				if (j == 0) {
					first_he = he;
				} else {
					prev_he.setNext(he);
					he.setPrev(prev_he);
				}
				// store the he for opposite he linkage
				edgeTable.put(key, he);

				// add the edge to vertex its outgoing,
				// every vertex has to know an arbitrary outgoing halfedge.
				vertices.get(fc[j]).setHalfEdge(he);

				// interlink the halfedge with its opposite halfedge if it
				// exists.
				key = new Point2i(key.y, key.x);
				if (edgeTable.containsKey(key)) {
					he.setOpposite(edgeTable.get(key));
					edgeTable.get(key).setOpposite(he);
				}
				// update prev_he
				prev_he = he;
			}

			// close the circle
			he.setNext(first_he);
			first_he.setPrev(he);
			// add a he to the face
			faces.get(faces.size() - 1).setHalfEdge(first_he);
		}

		edges.addAll(edgeTable.values());

		// finally: treat boundaries
		LinkedHashMap<Vertex, HalfEdge> boundaryEdges = new LinkedHashMap<Vertex, HalfEdge>();
		boolean dangling = false;

		// generate and interlink the opposite boundary halfEdges
		for (HalfEdge e : edges) {
			if (e.opposite == null) {
				he = new HalfEdge(null, e.getPrev().end());

				// throw an error later because the datastructure will be
				// disfunctional on boundaries:
				// multiple boundary edges start at this vertex, so
				if (boundaryEdges.containsKey(e.end())) {
					dangling = true;
				}
				// register them under the vertex 'he.getStart()'
				boundaryEdges.put(e.end(), he);
				he.setOpposite(e);
				e.setOpposite(he);

			}
		}

		// interlink previous and next boundary Edges
		for (HalfEdge e : boundaryEdges.values()) {
			next_he = boundaryEdges.get(e.end());
			e.setNext(next_he);
			next_he.setPrev(e);
		}

		// add all the boundary Edges
		edges.addAll(boundaryEdges.values());

		if (dangling) {
			throw new DanglingTriangleException();
		}

		this.enumerate();
	}

	/**
	 * Assign consecutive values 0...vertices.size()-1 to the Vertex.index
	 * fields.
	 */
	public void enumerate() {
		for (Pair<Integer, Vertex> p : zip(count(0), vertices)) {
			p.b.index = p.a;
		}
		for (Pair<Integer, Face> p : zip(count(0), faces)) {
			p.b.index = p.a;
		}
		for (Pair<Integer, HalfEdge> p : zip(count(0), edges)) {
			p.b.index = p.a;
		}


		Collections.sort(vertices, new Comparator<Vertex>() {
			@Override
			public int compare(Vertex o1, Vertex o2) {
				return Integer.compare(o1.index, o2.index);
			}
		});

		Collections.sort(faces, new Comparator<Face>() {
			@Override
			public int compare(Face o1, Face o2) {
				return Integer.compare(o1.index, o2.index);
			}
		});

		Collections.sort(edges, new Comparator<HalfEdge>() {
			@Override
			public int compare(HalfEdge o1, HalfEdge o2) {
				return Integer.compare(o1.index, o2.index);
			}
		});
	}

	public HashMap<String, Function<Indexed<Vertex>, Tuple3f>> getExtractors3d() {
		return extractors3d;
	}

	public void putExtractor3dPure(String string, Function<Vertex, Tuple3f> function) {
		extractors3d.put(string, Functions.compose(function, MyFunctions.<Vertex>value()));
	}

	public void putExtractor3d(String string, Function<Indexed<Vertex>, Tuple3f> function) {
		extractors3d.put(string, function);
	}


	public void putExtractor(String string, Function<Indexed<Vertex>, Float> function) {
		extractors1d.put(string, function);
	}

	public void putExtractorList(String string, final List<Float> function) {
		extractors1d.put(string, new Function<Indexed<Vertex>, Float>() {
			@Override
			public Float apply(Indexed<Vertex> input) {
				return function.get(input.index());
			}
		});
	}

	@Override
	public String toString() {
		return title == null ? super.toString() : title;
	}

	public float getVolume() {
        float sum = 0;
        for (Face f: getFaces()) {
                Iterator<Vertex> iter = f.iteratorFV();
                Vector3f p1 = new Vector3f(iter.next().getPos());
                Vector3f p2 = new Vector3f(iter.next().getPos());
                Vector3f p3 = new Vector3f(iter.next().getPos());
                Vector3f cross = new Vector3f();
                cross.cross(p2, p3);
                sum += p1.dot(cross);
        }
        return sum/6;
}

	public void putExtractor3dList(String string,
			final List<Tuple3f> function) {
		extractors3d.put(string, new Function<Indexed<Vertex>, Tuple3f>() {
			@Override
			public Tuple3f apply(Indexed<Vertex> input) {
				return function.get(input.index());
			}
		});
	}
	
	public void putExtractorFace(String name, final Function<Indexed<Face>, Tuple3f> function) {
		extractorsFace.put(name, function);
	}
	
	public HashMap<String, Function<Indexed<Face>, Tuple3f>> getExtractorFace() {
		return extractorsFace;
	}

	public float surfaceArea() {
		float sum = 0;
		for (Face f : getFaces()) {
			sum += f.getArea();
		}
		return sum;
	}
}
