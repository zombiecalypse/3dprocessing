package assignment4;

import glWrapper.GLHalfEdgeStructure;
import helpers.LMatrices;
import helpers.MyFunctions;
import helpers.StaticHelpers.Indexed;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import com.google.common.base.Function;
import com.google.common.base.Functions;

import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;
import openGL.MyDisplay;
import sparse.CSRMatrix;
import transformers.ImplicitSmoother;
import datastructure.halfedge.HalfEdgeStructure;
import datastructure.halfedge.Vertex;
import static helpers.StaticHelpers.*;


/**
 * Smoothing
 * @author Alf
 *
 */
public class Assignment4_2_smoothing {

	public static void main(String[] arg) throws IOException{
		headDemoSmoothing();
	}

	private static void headDemoSmoothing() throws IOException {
		WireframeMesh m = ObjReader.read("./objs/head.obj", true);
		
		final HalfEdgeStructure hs = new HalfEdgeStructure();
		try {
			hs.init(m);
		} catch (MeshNotOrientedException | DanglingTriangleException e) {
			e.printStackTrace();
			return;
		}
		//do your smoothing thing...

		HalfEdgeStructure hs2 = ImplicitSmoother.mixed(0.01f, hs);
		HalfEdgeStructure hs3 = ImplicitSmoother.mixed(0.1f, hs);
		HalfEdgeStructure hs4 = ImplicitSmoother.mixed(1, hs);
		

		MyDisplay disp = new MyDisplay();
		GLHalfEdgeStructure glwf = new GLHalfEdgeStructure(hs);
		glwf.setTitle("Unsmoothed");
		glwf.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");
		disp.addToDisplay(glwf);
		show(hs2, disp, 0.01f);
		show(hs3, disp, 0.1f);
	}

	private static void show(HalfEdgeStructure hs, MyDisplay disp, float l) {
		GLHalfEdgeStructure glwf2 = new GLHalfEdgeStructure(hs);
		glwf2.setTitle(String.format("Smoothed λ=%.3f", l));
		glwf2.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");
		disp.addToDisplay(glwf2);
	}
}
