package assignment4;

import glWrapper.GLHalfEdgeStructure;
import glWrapper.GLWireframeMesh;

import java.io.IOException;

import datastructure.halfedge.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;
import openGL.MyDisplay;
import transformers.ImplicitSmoother;
import transformers.UnsharpMasker;

public class Assignment4_2_uMasking {

	public static void main(String[] arg) throws IOException{
		headDemo();
					
	}

	private static void headDemo() throws IOException {
WireframeMesh m = ObjReader.read("./objs/head.obj", true);
		
		final HalfEdgeStructure hs = new HalfEdgeStructure();
		try {
			hs.init(m);
		} catch (MeshNotOrientedException | DanglingTriangleException e) {
			e.printStackTrace();
			return;
		}
		
		//do your unsharp masking thing...

		HalfEdgeStructure hs2 = UnsharpMasker.unsharp(0.1f, .1f, hs);
		HalfEdgeStructure hs3 = UnsharpMasker.unsharp(0.1f, 1f, hs);
		HalfEdgeStructure hs4 = UnsharpMasker.unsharp(0.1f, 2f, hs);
		

		MyDisplay disp = new MyDisplay();
		GLHalfEdgeStructure glwf = new GLHalfEdgeStructure(hs);
		glwf.setTitle("Unsmoothed");
		glwf.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");
		disp.addToDisplay(glwf);
		show(hs2, disp, .1f);
		show(hs3, disp, 1);
		show(hs4, disp, 2f);
	}

	private static void show(HalfEdgeStructure hs, MyDisplay disp, float l) {
		GLHalfEdgeStructure glwf2 = new GLHalfEdgeStructure(hs);
		glwf2.setTitle(String.format("Unsharp λ=0.1 w=%s", l));
		glwf2.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");
		disp.addToDisplay(glwf2);
	}

}
