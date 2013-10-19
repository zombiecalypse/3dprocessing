package algorithms.marchable;

import helpers.FloatBuffer;
import helpers.V;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Point3f;

import meshes.Point2i;
import meshes.WireframeMesh;
import datastructure.octree.HashOctree;
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
	private Map<Long, Float> val;

	/**
	 * Implementation of the marching cube algorithm. pass the tree and either
	 * the primary values associated to the trees edges
	 * 
	 * @param tree
	 * @param byLeaf
	 */
	public MarchingCubes(HashOctree tree) {
		this.tree = tree;
	}

	/**
	 * Perform primary Marching cubes on the tree.
	 */
	public void primaryMC(Map<Long, Float> byVertex) {
		this.val = byVertex;
		this.result = new WireframeMesh();

		// TODO

	}

	/**
	 * Perform dual marchingCubes on the tree
	 */
	public void dualMC(Map<Long, Float> byVertex) {

		// TODO
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
		fb.add(evaluateFunction(n.getIndex()));
		for (final Integer i : directions0bXYZ()) {
			fb.add(evaluateFunction(n.getCornerElement(i, tree).getIndex()));
		}
		
		MCTable.resolve(fb.render(), triags);

		for (int i = 0; i < 15/3; i++) {
			Point2i e1 = triags[i+0];
			Point2i e2 = triags[i+1];
			Point2i e3 = triags[i+2];
			if (isEmpty(e1) || isEmpty(e2) || isEmpty(e3)) break;
			assert !(e1.equals(e2) || e1.equals(e3) || e2.equals(e3)) : "Can't use same edge multiple times.";
			Point3f p1 = lookup(e1, n);
			Point3f p2 = lookup(e2, n);
			Point3f p3 = lookup(e3, n);
		}
	}
	
	Map<Long, Point3f> cache = new HashMap<>();
	
	private static long key(int x, int y) {
		assert Long.SIZE >= 2*Integer.SIZE;
		return x << Integer.SIZE | y;
	}
	
	private Point3f lookup(Point2i edge, MarchableCube n) {
		MarchableCube m1 = n.getCornerElement(edge.x, tree);
		MarchableCube m2 = n.getCornerElement(edge.y, tree);
		
		long theoretical_cache_key = key(m1.getIndex(), m2.getIndex());
		
		if (cache.containsKey(theoretical_cache_key)) {
			return cache.get(theoretical_cache_key);
		}
		float v1 = val.get(m1.getIndex());
		float v2 = val.get(m1.getIndex());
		assert (v1 <= 0 && v2 >= 0) || (v1 >= 0 && v2 <= 0);
		
		// add positions weighted by value
		Point3f p1 = m1.getPosition();
		Point3f p2 = m2.getPosition();
		float w = v1/(v2-v1);
		Point3f ret = V.scaled_add(p1, 1.0f - w, p2, w);
		cache.put(theoretical_cache_key, ret);
		return ret;
	}
	
	private boolean isEmpty(Point2i p) {
		return p.x == -1 && p.y == -1;
	}

	private float evaluateFunction(final int i) {
		return val.get(i);
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
