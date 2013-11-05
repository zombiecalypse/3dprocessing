package assignment1;

import glWrapper.GLHalfEdgeStructure;
import com.google.common.base.Function;
import helpers.MyFunctions;
import static helpers.StaticHelpers.*;

import java.io.IOException;
import datastructure.halfedge.HalfEdgeStructure;
import datastructure.halfedge.Vertex;
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
public class Exercise5 {

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

		Function<Vertex, Float> f = comp(MyFunctions.logNormalize(10), MyFunctions.laplacian());
		
		hs.putExtractor3d("color", MyFunctions.asColor(MyFunctions.pure(f)));

		GLHalfEdgeStructure glpot = new GLHalfEdgeStructure(hs);
		// choose the shader for the data
		glpot.configurePreferredShader("shaders/default.vert",
				"shaders/default.frag", null);
		disp.addToDisplay(glpot);
	}

}
