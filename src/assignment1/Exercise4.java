package assignment1;

import glWrapper.GLHalfEdgeStructure;
import com.google.common.base.Function;
import helpers.MyFunctions;
import java.io.IOException;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import datastructure.halfedge.HalfEdgeStructure;
import openGL.MyDisplay;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;

/**
 * 
 * @author Alf
 * 
 */
public class Exercise4 {

	public static void main(String[] args) throws IOException {
		// Load a wireframe mesh
		WireframeMesh m = ObjReader.read("./objs/dragon.obj", true);

		HalfEdgeStructure hs = new HalfEdgeStructure();
		hs.setTitle("Normals");
		final Tuple3f normalizer = new Vector3f(0.5f, 0.5f, 0.5f);
		hs.putExtractor3dPure("color", MyFunctions.centered_normals());

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
