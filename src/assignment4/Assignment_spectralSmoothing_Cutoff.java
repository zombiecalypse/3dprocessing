package assignment4;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import static helpers.StaticHelpers.*;
import glWrapper.GLHalfEdgeStructure;
import helpers.LMatrices;
import helpers.MyFunctions;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;
import openGL.MyDisplay;
import sparse.CSRMatrix;
import sparse.SCIPYEVD;
import transformers.CutHarmonicSmoother;
import transformers.HarmonicTransformer;
import transformers.ImplicitSmoother;
import datastructure.halfedge.HalfEdgeStructure;



/**
 * You can implement the spectral smoothing application here....
 * @author Alf
 *
 */
public class Assignment_spectralSmoothing_Cutoff {

	public static void main(String[] args) throws IOException, MeshNotOrientedException, DanglingTriangleException{
		WireframeMesh m = ObjReader.read("./objs/teapot.obj", true);
		
		final HalfEdgeStructure hs = new HalfEdgeStructure(m);
		
		CutHarmonicSmoother smoother = new CutHarmonicSmoother(200);
		
		HalfEdgeStructure hs2 = smoother.apply(hs);
		
		MyDisplay disp = new MyDisplay();

		GLHalfEdgeStructure glwf = new GLHalfEdgeStructure(hs);
		glwf.setTitle("Original");
		glwf.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");
		disp.addToDisplay(glwf);
		
		GLHalfEdgeStructure glwf2 = new GLHalfEdgeStructure(hs2);
		glwf2.setTitle("Smoothed");
		glwf2.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");
		disp.addToDisplay(glwf2);
	}
}
