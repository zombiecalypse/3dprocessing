package glWrapper;

import static helpers.StaticHelpers.*;

import java.util.ArrayList;
import java.util.Arrays;

import helpers.Function;
import helpers.IndexBuffer;

import javax.media.opengl.GL;
import javax.vecmath.Point3f;

import datastructure.HashOctree;
import datastructure.HashOctreeCell;
import openGL.gl.GLRenderer;
import openGL.gl.GLDisplayable.Semantic;
import openGL.objects.Transformation;

public class GlOctreeNeighbors extends GLVertexDisplayer {
	private static Function<HashOctreeCell, Point3f> cellCenter = new Function<HashOctreeCell, Point3f>() {
		@Override
		public Point3f call(HashOctreeCell a) {
			return a.center;
		}
	};

	public GlOctreeNeighbors(HashOctree oct) {
		super(oct.numberOfCells());
		ArrayList<HashOctreeCell> cells = new ArrayList<>(oct.getCells());
		float[] verts = packPoints(map(cellCenter, cells));
		IndexBuffer ind = new IndexBuffer();
		for (HashOctreeCell c : oct.getCells()) {
			for (int i = 0b001; i != 0b1000; i = i << 1) {
				HashOctreeCell other = oct.getNbr_c2c(c, i);
				if (other != null) {
					ind.add(cells.indexOf(c), cells.indexOf(other));
				}
			}
		}

		this.addElement(verts, Semantic.POSITION, 3);
		this.addElement(verts, Semantic.USERSPECIFIED, 3, "color");
		this.addIndices(ind.render());

		System.out.format("total: %s\n", verts.length / 3);

		this.configurePreferredShader("shaders/default.vert",
				"shaders/default.frag", null);
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
