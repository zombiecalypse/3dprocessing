package glWrapper;

import static helpers.StaticHelpers.iter;
import helpers.StaticHelpers.Indexed;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.media.opengl.GL;
import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;

import com.google.common.base.Function;

import datastructure.halfedge.Face;
import datastructure.halfedge.HalfEdgeStructure;
import datastructure.halfedge.Vertex;
import openGL.gl.GLDisplayable;
import openGL.gl.GLRenderer;
import openGL.gl.GLDisplayable.Semantic;
import openGL.objects.Transformation;
import static helpers.StaticHelpers.*;

public class GLHalfEdgeFaces extends GLDisplayable {

	private HalfEdgeStructure structure;

	public GLHalfEdgeFaces(HalfEdgeStructure s) {
		super(s.getFaces().size() * 3);

		this.structure = s;
		// Add Vertices

		flatten(s);
		
		for (Entry<String, Function<Indexed<Face>, Tuple3f>> e : s.getExtractorFace().entrySet()) {
			float[] vals = new float[s.getFaces().size() * 9];
			List<Tuple3f> vs = map(e.getValue(), withIndex(s.getFaces()));
			int index = 0;
			for (Indexed<Face> f : withIndex(s.getFaces())) {
				Tuple3f v = vs.get(f.index());
				for (Vertex _ : iter(f.value().iteratorFV())) {
					vals[index * 3 + 0] = v.x;
					vals[index * 3 + 1] = v.y;
					vals[index * 3 + 2] = v.z;
					index++;
				}
			}
			this.addElement(vals, Semantic.USERSPECIFIED, 3, e.getKey());
		}

	}

	private void flatten(HalfEdgeStructure s) {
		float[] verts = new float[s.getFaces().size() * 9];
		int[] triags = new int[s.getFaces().size() * 9];
		int index = 0;
		for (Face f : s.getFaces()) {
			for (Vertex v : iter(f.iteratorFV())) {
				Point3f p = v.getPos();
				verts[index * 3 + 0] = p.x;
				verts[index * 3 + 1] = p.y;
				verts[index * 3 + 2] = p.z;
				triags[index * 3 + 0] = index * 3 + 0;
				triags[index * 3 + 1] = index * 3 + 1;
				triags[index * 3 + 2] = index * 3 + 2;
				index++;
			}
		}
		assert index == s.getFaces().size() * 3 : String.format("index: %s expected: %s", index, s.getFaces().size() * 3);
		this.addElement(verts, Semantic.POSITION, 3);
		this.addIndices(triags);
	}

	@Override
	public int glRenderFlag() {
		return GL.GL_TRIANGLES;
	}

	@Override
	public void loadAdditionalUniforms(GLRenderer glRenderContext,
			Transformation mvMat) {
		// additional uniforms can be loaded using the function
		// glRenderContext.setUniform(name, val);

		// Such uniforms can be accessed in the shader by declaring them as
		// uniform <type> name;
		// where type is the appropriate type, e.g. float / vec3 / mat4 etc.
		// this method is called at every rendering pass.

	}

	@Override
	public String toString() {
		return title == null ? structure.toString() : title;
	}
}
