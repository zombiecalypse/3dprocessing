package assignment1;

import static helpers.StaticHelpers.iter;
import static helpers.StaticHelpers.len;

import java.io.IOException;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import meshes.HalfEdgeStructure;
import meshes.Vertex;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;
import openGL.MyDisplay;
import transformers.TrivialSmoother;
import transformers.Transformer;
import glWrapper.GLHalfEdgeStructure;
import helpers.Function;

public class Exercise3 {

	public static void main(String[] args) throws IOException {
		// Load a wireframe mesh
		WireframeMesh m = ObjReader.read("./objs/cat.obj", true);

		HalfEdgeStructure hs0 = new HalfEdgeStructure();

		MyDisplay disp = new MyDisplay();

		// create a half-edge structure out of the wireframe description.
		// As not every mesh can be represented as a half-edge structure
		// exceptions could occur.
		try {
			hs0.init(m);
		} catch (MeshNotOrientedException | DanglingTriangleException e) {
			e.printStackTrace();
			return;
		}

		Transformer smoother = new TrivialSmoother();
		HalfEdgeStructure hs = hs0;
		for (int i = 0; i < 20; i++) {
			if (i % 2 == 0) {
				hs.setTitle(String.format("smooth n=%d", i));
				GLHalfEdgeStructure glpot = new GLHalfEdgeStructure(hs);
				// choose the shader for the data
				glpot.configurePreferredShader("shaders/trimesh_flat.vert",
						"shaders/trimesh_flat.frag",
						"shaders/trimesh_flat.geom");
				disp.addToDisplay(glpot);
			}
			hs = smoother.call(hs);
		}
	}

}
