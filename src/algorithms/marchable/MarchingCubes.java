package algorithms.marchable;

import helpers.FloatBuffer;

import java.util.ArrayList;
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
			if (e1.equals(e2) || e1.equals(e3) || e2.equals(e3)) {
				throw new AssertionError("Can't use same edge multiple times.");
			}
			Point3f p1 = lookup(e1, n);
		}
	}
	
	private Point3f lookup(Point2i edge, MarchableCube n) {
		
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
