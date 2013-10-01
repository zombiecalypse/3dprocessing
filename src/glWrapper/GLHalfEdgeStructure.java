package glWrapper;

import helpers.Function;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

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

		this.structure = s;
		// Add Vertices

		float[] vert = flatVertices(s);
		this.addElement(vert, Semantic.POSITION, 3);

		this.addElement(vert, Semantic.USERSPECIFIED, 3, "color");
		int[] trigs = flatTriags(s);
		this.addIndices(trigs);

		addExtracted1d(s);

		addExtracted3d(s);
	}

	private void addExtracted3d(HalfEdgeStructure s) {
		HashMap<String, Function<Vertex, Point3f>> extractors3d = s
				.getExtractors3d();
		for (String name : extractors3d.keySet()) {
			System.out.print(name + " ");
			Function<Vertex, Point3f> f = extractors3d.get(name);
			float[] values = new float[s.getVertices().size() * 3];
			int index = 0;
			for (Vertex v : s.getVertices()) {
				Point3f p = f.call(v);
				values[index++] = p.x;
				values[index++] = p.y;
				values[index++] = p.z;
			}
			this.addElement(values, Semantic.USERSPECIFIED, 3, name);
			for (int i = 0; i < 100; i++) {
				System.out.format("%.2f ", values[i]);
			}
			System.out.println();
		}
	}

	private void addExtracted1d(HalfEdgeStructure s) {
		HashMap<String, Function<Vertex, Float>> extractors = s
				.getExtractors1d();
		for (String name : extractors.keySet()) {
			System.out.print(name + " ");
			Function<Vertex, Float> f = extractors.get(name);
			float[] values = new float[s.getVertices().size()];
			int index = 0;
			for (Vertex v : s.getVertices()) {
				values[index++] = f.call(v);
			}
			this.addElement(values, Semantic.USERSPECIFIED, 1, name);

			for (int i = 0; i < 100; i++) {
				System.out.format("%.2f ", values[i]);
			}
			System.out.println();
		}
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
