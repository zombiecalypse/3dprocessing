package glWrapper;

import java.util.ArrayList;
import java.util.Collection;

import javax.media.opengl.GL;

import datastructure.octree.HashOctree;
import datastructure.octree.HashOctreeVertex;
import openGL.gl.GLDisplayable;
import openGL.gl.GLRenderer;
import openGL.objects.Transformation;

/**
 * GLWrapper which will send the HashOctree vertex positions to the GPU
 * @author Alf
 *
 */
public class GLHashtree_Vertices extends GLDisplayable {

	private HashOctree myTree;
	private String name;
	public GLHashtree_Vertices(HashOctree tree) {
		
		super(tree.numberOfVertices());
		this.myTree = tree;
		//Add Vertices
		//float[] verts = new float[myTree.getNumberOfPoints()*3];
		float[] verts = new float[myTree.numberOfVertices()*3];
		
		
		int idx = 0;
		Collection<HashOctreeVertex> temp = tree.getVertices();
		for(HashOctreeVertex v : temp){
			verts[idx++] = v.position.x;
			verts[idx++] = v.position.y;
			verts[idx++] = v.position.z;
		}
		
		int[] ind = new int[myTree.numberOfVertices()];
		for(int i = 0; i < ind.length; i++)	{
			ind[i]=i;
		}
		this.addElement(verts, Semantic.POSITION , 3);
		this.addIndices(ind);
		
	}
	
	/**
	 * values are given by OctreeVertex
	 * @param values
	 */
	public void addFunctionValues(ArrayList<Float> values){
		float[] vals = new float[myTree.numberOfVertices()];
		
		for(HashOctreeVertex v: myTree.getVertices()){
			vals[v.index] = values.get(v.index);//*/Math.signum(values.get(myTree.getVertex(n, i).index));
		}
		
		this.addElement(vals, Semantic.USERSPECIFIED , 1, "func");
	}

	@Override
	public int glRenderFlag() {
		return GL.GL_POINTS;
	}

	@Override
	public void loadAdditionalUniforms(GLRenderer glRenderContext,
			Transformation mvMat) {
		// TODO Auto-generated method stub
		
	}
}
