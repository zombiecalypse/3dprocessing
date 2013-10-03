package assignment1;

import glWrapper.GLHalfEdgeStructure;
import glWrapper.GLWireframeMesh;
import helpers.Function;
import helpers.Functions;
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
public class Assignment5 {

	public static void main(String[] args) throws IOException {
		// Load a wireframe mesh
		WireframeMesh m = ObjReader.read("./objs/cat.obj", true);

		HalfEdgeStructure hs = new HalfEdgeStructure();
		hs.setTitle("Curvature");
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

		Function<Vertex, Float> f = comp(Functions.logNormalize(10), Functions.laplacian());
		
		float min_trans = minimize(f, hs.getVertices());
		float max_trans = maximize(f, hs.getVertices());
		
		hs.putExtractor3d("color", Functions.asColor(f));


		GLHalfEdgeStructure glpot = new GLHalfEdgeStructure(hs);
		// choose the shader for the data
		glpot.configurePreferredShader("shaders/default.vert",
				"shaders/default.frag", null);
		disp.addToDisplay(glpot);
	}

}
