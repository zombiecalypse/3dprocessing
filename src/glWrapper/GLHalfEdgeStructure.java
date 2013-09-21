package glWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import javax.media.opengl.GL;
import javax.vecmath.Point3f;

import static helpers.StaticHelpers.*;
import meshes.Face;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import openGL.gl.GLDisplayable;
import openGL.gl.GLRenderer;
import openGL.objects.Transformation;

public class GLHalfEdgeStructure extends GLDisplayable {

	private HalfEdgeStructure structure;

	public GLHalfEdgeStructure(HalfEdgeStructure s) {
		super(s.getVertices().size());
		System.out.format("n: %d\n", s.getVertices().size());

		this.structure = s;
		// Add Vertices

		float[] vert = flatVertices(s);
		this.addElement(vert, Semantic.POSITION, 3);

		this.addElement(vert, Semantic.USERSPECIFIED, 3, "color");
		int[] trigs = flatTriags(s);
		this.addIndices(trigs);
	}

	private static float[] flatVertices(HalfEdgeStructure s) {
		float[] verts = new float[s.getVertices().size() * 3];
		int index = 0;
		for (Vertex v : s.getVertices()) {
			Point3f p = v.getPos();
			verts[index * 3 + 0] = p.x;
			verts[index * 3 + 1] = p.y;
			verts[index * 3 + 2] = p.z;
			index++;
		}
		return verts;
	}

	private static int[] flatTriags(HalfEdgeStructure s) {
		ArrayList<Integer> ind = new ArrayList<Integer>();
		for (Face f : s.getFaces()) {
			Iterator<Vertex> iterator = f.iteratorFV();
			Vertex first = iterator.next();
			Vertex last = iterator.next();
			for (Vertex v : iter(iterator)) {
				ind.add(first.index);
				ind.add(v.index);
				ind.add(last.index);
				last = v;
			}
		}

		// Fun fact: Java hates primitive types so much that it won't allow
		// anything like
		// converting ArrayList<Integer> to int[] or creating an ArrayList<int>.
		// If you
		// don't die a little seeing this, you have no heart...
		int[] arr = new int[ind.size()];
		int index = 0;
		for (Integer i : ind) {
			arr[index++] = i;
		}

		return arr;

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

}
