package algorithms.marchable;

import helpers.FloatBuffer;
import helpers.V;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point3f;

import com.google.common.collect.Range;

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
			this.triags[i] = new Point2i(-1,-1);
		}
	}

	/**
	 * Perform primary Marching cubes on the tree.
	 */
	public void primaryMC(List<Float> x) {
		this.val = x;
		this.result = new WireframeMesh();

		for (HashOctreeCell c : tree.getCells()) {
			pushCube(c);
		}
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
		for (int i = 0; i < 8; i++) {
			fb.add(evaluateFunction(n.getCornerElement(i, tree).getIndex()));
		}
		
		MCTable.resolve(fb.render(), triags);

		for (int i = 0; i < 15/3; i++) {
			Point2i e1 = triags[i+0];
			Point2i e2 = triags[i+1];
			Point2i e3 = triags[i+2];
			if (isEmpty(e1) || isEmpty(e2) || isEmpty(e3)) break;
			assert !(e1.equals(e2) || e1.equals(e3) || e2.equals(e3)) : "Can't use same edge multiple times.";
			int p1 = lookup(e1, n);
			int p2 = lookup(e2, n);
			int p3 = lookup(e3, n);
			this.result.faces.add(asArray(p1, p2, p3));
		}
	}
	
	Map<Point2i, Integer> cache = new HashMap<>();
		
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
		float w = v1/(v2-v1);
		Point3f ret = V.scaled_add(p1, 1.0f - w, p2, w);
		
		int new_index = result.vertices.size();
		
		cache.put(theoretical_cache_key, new_index);
		result.vertices.add(ret);
		return new_index;
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
