package glWrapper;

import static helpers.StaticHelpers.*;
import helpers.FloatBuffer;
import com.google.common.base.Function;
import helpers.IndexBuffer;

import java.util.ArrayList;

import javax.media.opengl.GL;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import openGL.gl.GLRenderer;
import openGL.objects.Transformation;
import datastructure.octree.HashOctree;
import datastructure.octree.HashOctreeVertex;

public class GLOctreeVertexNeighbors extends GLVertexDisplayer {
	private static Function<HashOctreeVertex, Point3f> position = new Function<HashOctreeVertex, Point3f>() {
		@Override
		public Point3f apply(HashOctreeVertex a) {
			return a.position;
		}
	};

	static public GLOctreeVertexNeighbors make(HashOctree oct) {
		ArrayList<HashOctreeVertex> vertices = new ArrayList<>(
				oct.getVertices());
		FloatBuffer verts = new FloatBuffer();
		for (HashOctreeVertex v : vertices) {
			verts.add(v.position.x, v.position.y, v.position.z);
		}
		int index = vertices.size();
		IndexBuffer ind = new IndexBuffer();
		for (Pair<Integer, HashOctreeVertex> p : zip(count(0), vertices)) {
			HashOctreeVertex v = p.b;
			for (int i = 0b100; i != 0; i >>= 1) {
				HashOctreeVertex other = oct.getNbr_v2vMinus(v, i);
				if (other != null) {
					Vector3f other_dir = new Vector3f(other.position);
					other_dir.sub(v.position);
					other_dir.scale(0.3f);
					other_dir.add(v.position);
					verts.add(other_dir.x, other_dir.y, other_dir.z);

					ind.add(p.a, index++);
				}
				other = oct.getNbr_v2v(v, i);
				if (other != null) {
					Vector3f other_dir = new Vector3f(other.position);
					other_dir.sub(v.position);
					other_dir.scale(0.3f);
					other_dir.add(v.position);
					verts.add(other_dir.x, other_dir.y, other_dir.z);

					ind.add(p.a, index++);
				}
			}
		}
		return new GLOctreeVertexNeighbors(verts.render(), ind.render());
	}
		
	public GLOctreeVertexNeighbors(float[] verts, int[] ind) {
		super(verts.length/3);
		this.addElement(verts, Semantic.POSITION, 3);
		this.addElement(verts, Semantic.USERSPECIFIED, 3, "color");
		this.addIndices(ind);

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
