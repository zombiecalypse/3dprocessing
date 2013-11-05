package assignment1;

import glWrapper.GLHalfEdgeStructure;
import com.google.common.base.Function;
import static helpers.StaticHelpers.*;
import helpers.MyFunctions;

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
public class Exercise2 {

	public static void main(String[] args) throws IOException {
		// Load a wireframe mesh
		WireframeMesh m = ObjReader.read("./objs/teapot.obj", true);

		HalfEdgeStructure hs = new HalfEdgeStructure();
		hs.setTitle("Valence");

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
		
		float max_valence_ = Float.MIN_NORMAL;
		float min_valence_ = Float.MAX_VALUE;
		for (Vertex a: hs.getVertices()) {
			float val = len(a.iteratorVE());
			max_valence_ = Math.max(val, max_valence_);
			min_valence_ = Math.min(val, min_valence_);
		}
		final float max_valence = max_valence_;
		final float min_valence = min_valence_;
		
		hs.putExtractor3d("color", MyFunctions.asColor(MyFunctions.pure(comp(MyFunctions.spread(min_valence, max_valence), MyFunctions.valence()))));

		GLHalfEdgeStructure glpot = new GLHalfEdgeStructure(hs);
		// choose the shader for the data
		glpot.configurePreferredShader("shaders/default.vert",
				"shaders/default.frag", null);
		disp.addToDisplay(glpot);
	}

}
