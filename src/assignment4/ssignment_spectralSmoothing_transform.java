package assignment4;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import com.google.common.base.Function;

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
public class ssignment_spectralSmoothing_transform {

	public static void main(String[] args) throws IOException, MeshNotOrientedException, DanglingTriangleException{
		WireframeMesh m = ObjReader.read("./objs/bunny.obj", true);
		
		final HalfEdgeStructure hs = new HalfEdgeStructure(m);
		
		
		MyDisplay disp = new MyDisplay();

		GLHalfEdgeStructure glwf = new GLHalfEdgeStructure(hs);
		glwf.setTitle("Original");
		glwf.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");
		disp.addToDisplay(glwf);
		
		transform("harmonic", hs, disp, new Function<Integer, Float>() {
			@Override
			public Float apply(Integer input) {
				return 1f/(5+input);
			}
		});
		
		final float nVerts = hs.getVertices().size(); 
		
		transform("Gaussian 10", hs, disp, new Function<Integer, Float>() {
			@Override
			public Float apply(Integer input) {
				return gaussian(input, 10, 10f);
			}
		});
		transform("Gaussian End", hs, disp, new Function<Integer, Float>() {
			@Override
			public Float apply(Integer input) {
				return gaussian(input, nVerts, 50f);
			}
		});
		transform("iGaussian Middle", hs, disp, new Function<Integer, Float>() {
			@Override
			public Float apply(Integer input) {
				return 1-gaussian(input, nVerts/2, 30f);
			}
		});
		
	}
	
	static float gaussian(float x, float m, float s) {
		return (float) Math.exp(-Math.pow(x-m, 2)/(s*s)/(s*Math.sqrt(2*Math.PI)));
	}

	private static void transform(String name, final HalfEdgeStructure hs, MyDisplay disp, Function<Integer, Float> f) {
		HarmonicTransformer trans = new HarmonicTransformer(f);
		
		HalfEdgeStructure hs2 = trans.apply(hs);

		GLHalfEdgeStructure glwf2 = new GLHalfEdgeStructure(hs2);
		glwf2.setTitle(name);
		glwf2.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");
		disp.addToDisplay(glwf2);
	}
}
