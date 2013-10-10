package glWrapper;

import static helpers.StaticHelpers.*;
import helpers.Function;
import helpers.IndexBuffer;

import javax.media.opengl.GL;
import javax.vecmath.Point3f;

import datastructure.HashOctree;
import datastructure.HashOctreeCell;
import openGL.gl.GLRenderer;
import openGL.objects.Transformation;

public class GlOctreeParenthood extends GLVertexDisplayer {
	private static Function<HashOctreeCell, Point3f> cellCenter = new Function<HashOctreeCell, Point3f>() {
		@Override
		public Point3f call(HashOctreeCell a) {
			return a.center;
		}
	};

	public GlOctreeParenthood(HashOctree oct) {
		super(oct.numberOfCells()*2);
		float[] verts = packPoints(map(cellCenter, oct.getCells()));
		IndexBuffer ind = new IndexBuffer();
		for (HashOctreeCell c : oct.getCells()) {
			ind.add(c.leafIndex, oct.getParent(c).leafIndex);
		}
		
		this.addElement(verts, Semantic.POSITION, 3);
		this.addIndices(ind.render());
	}

	@Override
	public int glRenderFlag() {
		return GL.GL_LINES;
	}

	@Override
	public void loadAdditionalUniforms(GLRenderer glRenderContext,
			Transformation mvMat) {
		// TODO Auto-generated method stub

	}

}
