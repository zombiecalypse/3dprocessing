package assignment4;

import glWrapper.GLHalfEdgeStructure;
import helpers.LMatrices;
import helpers.MyFunctions;
import helpers.StaticHelpers.Indexed;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
public class Assignment4_1_visual {

	public static void main(String[] arg) throws IOException{
		headDemoSmoothing();
	}

	private static void headDemoSmoothing() throws IOException {
		WireframeMesh m = ObjReader.read("./objs/uglySphere.obj", true);
		
		final HalfEdgeStructure hs = new HalfEdgeStructure();
		try {
			hs.init(m);
		} catch (MeshNotOrientedException | DanglingTriangleException e) {
			e.printStackTrace();
			return;
		}
		final HalfEdgeStructure hs2 = new HalfEdgeStructure(hs);
		MyDisplay disp = new MyDisplay();
		
		CSRMatrix laplacian = LMatrices.mixedCotanLaplacian(hs, false);
		showLaplacian(hs, disp, laplacian);
	}

	private static void showLaplacian(final HalfEdgeStructure hs,
			MyDisplay disp, CSRMatrix laplacian) {

		ArrayList<Tuple3f> xs = new ArrayList<>(map(MyFunctions.pos, hs.getVertices()));

		HalfEdgeStructure hsCurv = new HalfEdgeStructure(hs);
		HalfEdgeStructure hsNorm = new HalfEdgeStructure(hs);
		
		final List<Tuple3f> laplacianEval = laplacian.multComponentwise(xs);
		final List<Float> coords = chain(
				map(MyFunctions.x, laplacianEval),
				map(MyFunctions.y, laplacianEval),
				map(MyFunctions.z, laplacianEval));
		float min = Collections.min(coords), max = Collections.max(coords);
		
		List<Float> lengths = map(MyFunctions.length, laplacianEval);
		float minl = Collections.min(lengths), maxl = Collections.max(lengths);
		hsCurv.putExtractorList("color", 
				map(Functions.compose(MyFunctions.spread(minl, maxl), MyFunctions.length), 
				laplacianEval));
		hsNorm.putExtractor3dList("color", map(MyFunctions.spread3d(min, max), laplacianEval));

		GLHalfEdgeStructure glwf = new GLHalfEdgeStructure(hsCurv);
		glwf.setTitle("Sphere Curv");
		glwf.configurePreferredShader("shaders/default.vert", 
				"shaders/default.frag", null);
		disp.addToDisplay(glwf);
		GLHalfEdgeStructure glwf2 = new GLHalfEdgeStructure(hsNorm);
		glwf2.setTitle("Sphere Norm");
		glwf2.configurePreferredShader("shaders/default.vert", 
				"shaders/default.frag", null);
		disp.addToDisplay(glwf2);
	}
}
