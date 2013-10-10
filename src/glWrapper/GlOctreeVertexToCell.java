package glWrapper;

import static helpers.StaticHelpers.*;

import java.util.ArrayList;
import java.util.Arrays;

import helpers.FloatBuffer;
import helpers.Function;
import helpers.IndexBuffer;

import javax.media.opengl.GL;
import javax.vecmath.Point3f;

import datastructure.octree.HashOctree;
import datastructure.octree.HashOctreeCell;
import datastructure.octree.HashOctreeVertex;
import openGL.gl.GLRenderer;
import openGL.objects.Transformation;

public class GlOctreeVertexToCell extends GLVertexDisplayer {
	private static Function<HashOctreeVertex, Point3f> vertexPosition = new Function<HashOctreeVertex, Point3f>() {
		@Override
		public Point3f call(HashOctreeVertex a) {
			return a.position;
		}
	};
	private static Function<HashOctreeCell, Point3f> cellCenter = new Function<HashOctreeCell, Point3f>() {
		@Override
		public Point3f call(HashOctreeCell a) {
			return a.center;
		}
	};
	
	public static GlOctreeVertexToCell make(HashOctree oct) {
		ArrayList<HashOctreeCell> cells = new ArrayList<>(oct.getCells());
		float[] cell_pos = packPoints(map(cellCenter, cells));
		
		FloatBuffer vert_pos = new FloatBuffer();
		IndexBuffer ind = new IndexBuffer();
		int index = cell_pos.length;
		for (Pair<Integer, HashOctreeCell> p : zip(count(0), cells)) {
			if (p.b.points == null) continue;
			for (Point3f pt: p.b.points) {
				vert_pos.add(pt.x, pt.y, pt.z);
				ind.add(p.a, index++);	
			}
		}

		float[] verts = concat(cell_pos, vert_pos.render());

		

		// this.configurePreferredShader("shaders/default.vert",
		// "shaders/default.frag", null);
		
		return new GlOctreeVertexToCell(verts, ind.render());
	}

	private GlOctreeVertexToCell(float[] verts, int[] ind) {
		super(verts.length/3);
		this.addElement(verts, Semantic.POSITION, 3);
		this.addIndices(ind);
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
