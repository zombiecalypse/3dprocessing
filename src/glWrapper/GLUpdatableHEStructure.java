package glWrapper;

import static helpers.StaticHelpers.map;
import static helpers.StaticHelpers.withIndex;
import helpers.StaticHelpers.Indexed;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.media.opengl.GL;
import javax.vecmath.Tuple3f;

import com.google.common.base.Function;

import datastructure.halfedge.Face;
import datastructure.halfedge.HalfEdgeStructure;
import datastructure.halfedge.Vertex;
import openGL.gl.GLRenderer;
import openGL.gl.GLDisplayable.Semantic;
import openGL.gl.interactive.GLUpdateable;
import openGL.objects.Transformation;

public class GLUpdatableHEStructure extends GLUpdateable {

	private HalfEdgeStructure hs;

	public GLUpdatableHEStructure(HalfEdgeStructure s) {
		super(s.getVertices().size());
		assert s.getExtractorFace().isEmpty();
		hs = s;

		// add vertices
		float[] verts = new float[hs.getVertices().size() * 3];
		int[] ind = new int[hs.getFaces().size() * 3];

		copyToArray(hs.getVertices(), verts);
		copyToArray(hs.getFaces(), ind);
		this.addElement(verts, Semantic.POSITION, 3, "position");

		this.addIndices(ind);
		this.configurePreferredShader("shaders/trimesh_flatColor3f.vert",
				"shaders/trimesh_flatColor3f.frag",
				"shaders/trimesh_flatColor3f.geom");

		for (Entry<String, Function<Indexed<Vertex>, Float>> e : s.getExtractors1d().entrySet()) {
			float[] initial = new float[3 * hs.getVertices().size()];
			String name = e.getKey();
			fill(e.getValue(), initial);
			
			addElement(initial, Semantic.USERSPECIFIED, 1, name);
		}
		
		for (Entry<String, Function<Indexed<Vertex>, Tuple3f>> e : s.getExtractors3d().entrySet()) {
			float[] initial = new float[3 * hs.getVertices().size()];
			String name = e.getKey();
			Function<Indexed<Vertex>, Tuple3f> f = e.getValue();
			fill3d(e.getValue(), initial);
			
			addElement(initial, Semantic.USERSPECIFIED, 3, name);
		}
	}

	private void fill(Function<Indexed<Vertex>, Float> f,
			float[] initial) {
		for (Indexed<Vertex> iv: withIndex(hs.getVertices())) {
			initial[iv.index()] = f.apply(iv);
		}
	}
	
	private void fill3d(Function<Indexed<Vertex>, Tuple3f> f,
			float[] initial) {
		for (Indexed<Vertex> iv: withIndex(hs.getVertices())) {
			final Tuple3f result = f.apply(iv);
			initial[3 * iv.index() + 0] = result.x;
			initial[3 * iv.index() + 1] = result.y;
			initial[3 * iv.index() + 2] = result.z;
		}
	}
	
	/**
	 * The position buffer will be updated in the next pass
	 */
	public void updatePosition() {
		float[] data = getDataBuffer("position");
		copyToArray(hs.getVertices(), data);
		scheduleUpdate("position");
		/***/
	}

	/**
	 * The gpu buffer associated to the name glName will be updated in the next
	 * render pass.
	 * 
	 * @param glName
	 */
	public void update(String glName) {
		float[] data = getDataBuffer(glName);
		fill3d(hs.getExtractors3d().get(glName), data);
		scheduleUpdate(glName);
		/***/
	}

	private void copyToArray(ArrayList<Face> faces, int[] ind) {
		Iterator<Vertex> it;
		int i = 0;
		for (Face f : faces) {
			it = f.iteratorFV();
			while (it.hasNext()) {
				ind[i++] = it.next().index;
			}
		}
	}

	private void copyToArray(ArrayList<Vertex> vertices, float[] verts) {
		int i = 0;
		for (Vertex v : vertices) {
			v.index = i / 3;
			verts[i++] = v.getPos().x;
			verts[i++] = v.getPos().y;
			verts[i++] = v.getPos().z;

		}
	}

	@Override
	public int glRenderFlag() {
		return GL.GL_TRIANGLES;
	}

	@Override
	public void loadAdditionalUniforms(GLRenderer glRenderContext,
			Transformation mvMat) {
		// no additional uniforms
	}

}
