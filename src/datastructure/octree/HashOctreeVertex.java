package datastructure.octree;

import javax.vecmath.Point3f;

import algorithms.marchable.MarchableCube;


/**
 * 
 * @author Alf
 *
 */
public class HashOctreeVertex implements MarchableCube {
	/** The morton code of this vertex*/
	public long code;
	/** Its position*/
	public Point3f position;
	/** The maximal grid layer on which this vertex has an adjacent cell*/
	public int maxLvl;
	/** The minimal grid layer on which this vertex has an adjacent cell*/
	public int minLvl;
	
	/** During hashoctree creation the vertices are
	 * enumerated consicutively and every vertex has a unique index assigned
	 * the {@link HashOctree} class allows a vertex to be retrieved by its index
	 * as an alternative to the morton code*/
	public int index;
	
	/**
	 * Initialize a vertex with dummy values
	 */
	public HashOctreeVertex() {
		code = -1; //illegal
		maxLvl = -1; //illegal
		minLvl = -1; //illegal
		position = new Point3f();
	}
	
	@Override
	public boolean equals(Object o){
		assert(code >= 0);
		if(o instanceof HashOctreeVertex){
			return ((HashOctreeVertex)o).code == this.code;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return Long.toBinaryString(code);
	}

	@Override
	public Point3f getPosition() {
		return position;
	}

	@Override
	public MarchableCube getCornerElement(int Obxyz, HashOctree tree) {
		return tree.getNbr_v2c(this, Obxyz);
	}

	@Override
	public int getIndex() {
		return this.index;
	}
	
}
