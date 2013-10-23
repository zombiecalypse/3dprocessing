package algorithms.marchable;

import helpers.FloatBuffer;
import helpers.IntBuffer;
import helpers.V;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point3f;

import meshes.Point2i;
import meshes.WireframeMesh;
import datastructure.octree.HashOctree;
import datastructure.octree.HashOctreeCell;
import datastructure.octree.HashOctreeVertex;
import static helpers.StaticHelpers.*;

/**
 * Implwmwnr your Marching cubes algorithms here.
 * 
 * @author bertholet
 * 
 */
public class MarchingCubes {

	// the reconstructed surface
	public WireframeMesh result;

	// the tree to march
	private HashOctree tree;
	// per marchable cube values
	private List<Float> val;

	/**
	 * Implementation of the marching cube algorithm. pass the tree and either
	 * the primary values associated to the trees edges
	 * 
	 * @param tree
	 * @param byLeaf
	 */
	public MarchingCubes(HashOctree tree) {
		this.tree = tree;
		for (int i = 0; i < 15; i++) {
			this.triags[i] = new Point2i(-1, -1);
		}
	}

	/**
	 * Perform primary Marching cubes on the tree.
	 */
	public void primaryMC(List<Float> x) {
		this.val = x;
		this.result = new WireframeMesh();

		for (HashOctreeCell c : tree.getLeafs()) {
			pushCube(c);
		}
	}

	/**
	 * Perform dual marchingCubes on the tree
	 */
	public void dualMC(List<Float> byVertex) {
		float[] byCell = new float[byVertex.size()];
		for (HashOctreeCell c : tree.getLeafs()) {
			for (int i = 0; i < 8; i++) {
				HashOctreeVertex v = tree.getNbr_c2v(c, i);
				byCell[c.getIndex()] += byVertex.get(v.getIndex());
			}
			byCell[c.getIndex()] /= 8;
		}
		this.val = list(byCell);
		this.result = new WireframeMesh();
		for (HashOctreeVertex v : tree.getVertices()) {
			if (!tree.isOnBoundary(v)) {
				pushCube(v);
			}
		}
	}

	private final FloatBuffer fb = new FloatBuffer();
	private final Point2i[] triags = new Point2i[15];

	/**
	 * March a single cube: compute the triangles and add them to the wireframe
	 * model
	 * 
	 * @param n
	 */
	private void pushCube(final MarchableCube n) {
		fb.clear();
		for (int i = 0; i < 8; i++) {
			fb.add(evaluateFunction(n.getCornerElement(i, tree)));
		}

		MCTable.resolve(fb.render(), triags);

		for (Point2i e : triags) {
			if (isEmpty(e))
				break;
			int p = lookup(e, n);
			faceConstructionSide.add(p);
			if (faceConstructionSide.size() == 3) {
				int[] rendered = faceConstructionSide.render();
				faceConstructionSide.clear();
				if (rendered[0] == rendered[1] || 
						rendered[0] == rendered[2] || 
						rendered[1] == rendered[2]) continue;
				this.result.faces.add(rendered);
			}
		}
	}

	Map<Point2i, Integer> cache = new HashMap<>();
	IntBuffer faceConstructionSide = new IntBuffer();

	private int lookup(Point2i edge, MarchableCube n) {
		Point2i theoretical_cache_key = key(n, edge);

		if (cache.containsKey(theoretical_cache_key)) {
			return cache.get(theoretical_cache_key);
		}
		MarchableCube m1 = n.getCornerElement(edge.x, tree);
		MarchableCube m2 = n.getCornerElement(edge.y, tree);
		int i1 = m1.getIndex(), i2 = m2.getIndex();

		float v1 = val.get(i1);
		float v2 = val.get(i2);
		assert (v1 <= 0 && v2 >= 0) || (v1 >= 0 && v2 <= 0);

		// add positions weighted by value
		Point3f p1 = m1.getPosition();
		Point3f p2 = m2.getPosition();
		float w = v1 / (v1 - v2);
		Point3f ret = V.scaled_add(p1, 1.0f - w, p2, w);

		int new_index = result.vertices.size();

		cache.put(theoretical_cache_key, new_index);
		result.vertices.add(ret);
		return new_index;
	}

	private boolean isEmpty(Point2i p) {
		return p.x == -1 && p.y == -1;
	}

	private float evaluateFunction(MarchableCube marchableCube) {
		return val.get(marchableCube.getIndex());
	}

	/**
	 * Get a nicely marched wireframe mesh...
	 * 
	 * @return
	 */
	public WireframeMesh getResult() {
		return this.result;
	}

	/**
	 * compute a key from the edge description e, that can be used to uniquely
	 * identify the edge e of the cube n. See Assignment 3 Exerise 1-5
	 * 
	 * @param n
	 * @param e
	 * @return
	 */
	private Point2i key(MarchableCube n, Point2i e) {
		Point2i p = new Point2i(n.getCornerElement(e.x, tree).getIndex(), n
				.getCornerElement(e.y, tree).getIndex());
		if (p.x > p.y) {
			int temp = p.x;
			p.x = p.y;
			p.y = temp;
		}
		return p;
	}

}
