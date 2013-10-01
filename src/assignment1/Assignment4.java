package assignment1;

import glWrapper.GLHalfEdgeStructure;
import glWrapper.GLWireframeMesh;
import helpers.Function;
import static helpers.StaticHelpers.*;

import java.io.IOException;
import java.util.List;

import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import openGL.MyDisplay;
import meshes.Face;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;

/**
 * 
 * @author Alf
 * 
 */
public class Assignment4 {

	public static void main(String[] args) throws IOException {
		// Load a wireframe mesh
		WireframeMesh m = ObjReader.read("./objs/teapot.obj", true);

		HalfEdgeStructure hs = new HalfEdgeStructure();
		hs.setTitle("Normals");
		final Tuple3f normalizer = new Vector3f(0.5f, 0.5f, 0.5f);
		hs.putExtractor3d("color", new Function<Vertex, Tuple3f>() {

			@Override
			public Tuple3f call(Vertex v) {
				List<Face> faces = list(v.iteratorVF());
				Tuple3f normal = new Vector3f();
				for (Face f : faces) {
					Vector3f normal_f = f.normal();
					normal_f.scale(f.angle_in(v));
					normal.add(normal_f);
				}
				normal.scale(1/norm(normal)/2);
				normal.add(normalizer);
				return normal;
			}});

		MyDisplay disp = new MyDisplay();

		// create a half-edge structure out of the wireframe description.
		// As not every mesh can be represented as a half-edge structure
		// exceptions could occur.
		try {
			hs.init(m);
		} catch (MeshNotOrientedException | DanglingTriangleException e) {
			e.printStackTrace();
			return;
		}

		GLHalfEdgeStructure glpot = new GLHalfEdgeStructure(hs);
		// choose the shader for the data
		glpot.configurePreferredShader("shaders/default.vert",
				"shaders/default.frag", null);
		disp.addToDisplay(glpot);
	}

}
