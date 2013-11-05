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
		WireframeMesh m = ObjReader.read("./objs/cat.obj", true);
		
		MyDisplay disp = new MyDisplay();
				
		final HalfEdgeStructure hs = new HalfEdgeStructure();
		try {
			hs.init(m);
		} catch (MeshNotOrientedException | DanglingTriangleException e) {
			e.printStackTrace();
			return;
		}
		
		//do your smoothing thing...
		
		CSRMatrix laplacian_m = LMatrices.uniformLaplacian(hs);
		final ArrayList<Vector3f> res = new ArrayList<>();
		LMatrices.mult(laplacian_m, hs, res);
		Function<Indexed<Vertex>, Float> laplacian = new Function<Indexed<Vertex>, Float>() {
			@Override
			public Float apply(Indexed<Vertex> input) {
				return res.get(input.index()).lengthSquared();
			}
		};
		Function<Indexed<Vertex>, Float> absoluteLaplacian = Functions.compose( 
				MyFunctions.logNormalize(10),
				Functions.compose(MyFunctions.abs, laplacian));

		List<Float> vals = map(absoluteLaplacian, withIndex(hs.getVertices()));
		float p95 = percentile(0.95f, vals);
		float p05 = percentile(0.05f, vals);
		
		hs.putExtractor3d("color", MyFunctions.asColor(Functions.compose(
				MyFunctions.spread(p05, p95),
				absoluteLaplacian)));
		
		GLHalfEdgeStructure glwf = new GLHalfEdgeStructure(hs);
		glwf.configurePreferredShader("shaders/default.vert",
				"shaders/default.frag", null);
		disp.addToDisplay(glwf);
	}
}
