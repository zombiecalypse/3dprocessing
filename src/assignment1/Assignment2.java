package assignment1;

import glWrapper.GLHalfEdgeStructure;
import glWrapper.GLWireframeMesh;
import helpers.Function;
import static helpers.StaticHelpers.*;

import java.io.IOException;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import openGL.MyDisplay;
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
public class Assignment2 {

	public static void main(String[] args) throws IOException {
		// Load a wireframe mesh
		WireframeMesh m = ObjReader.read("./objs/teapot.obj", true);

		HalfEdgeStructure hs = new HalfEdgeStructure();
		hs.putExtractor("valence", new Function<Vertex, Float>() {
			@Override
			public Float call(Vertex a) {
				return (float) len(a.iteratorVE());
			}
		});

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
		glpot.configurePreferredShader("shaders/valence.vert",
				"shaders/valence.frag", "shaders/valence.geom");
		disp.addToDisplay(glpot);
	}

}
