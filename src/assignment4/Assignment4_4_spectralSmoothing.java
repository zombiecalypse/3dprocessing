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
import transformers.ImplicitSmoother;
import datastructure.halfedge.HalfEdgeStructure;



/**
 * You can implement the spectral smoothing application here....
 * @author Alf
 *
 */
public class Assignment4_4_spectralSmoothing {

	public static void main(String[] args) throws IOException, MeshNotOrientedException, DanglingTriangleException{
		WireframeMesh m = ObjReader.read("./objs/sphere.obj", true);
		
		final HalfEdgeStructure hs = new HalfEdgeStructure(m);
		
		CSRMatrix laplacian = LMatrices.symmetricCotanLaplacian(hs);
		
		int numEVs = 20;
		ArrayList<Float> eigenValues = new ArrayList<>();
		ArrayList<ArrayList<Float>> eigenVectors = new ArrayList<>();
		SCIPYEVD.doSVD(laplacian, "eigen", numEVs, eigenValues , eigenVectors);
		
		final HalfEdgeStructure[] harmonics = new HalfEdgeStructure[numEVs];
		final GLHalfEdgeStructure[] displs = new GLHalfEdgeStructure[numEVs];
		for (int i = 0; i < numEVs; i++) {
			harmonics[i] = new HalfEdgeStructure(hs);
			ArrayList<Float> harmony = new ArrayList<>(eigenVectors.get(i));
			float min = Collections.min(harmony), max = Collections.max(harmony);
			harmonics[i].putExtractorList("color", map(MyFunctions.spread(min, max), harmony));
			displs[i] = new GLHalfEdgeStructure(harmonics[i]);
			displs[i].setTitle(String.format("Harmony %d", i));
			displs[i].configurePreferredShader("shaders/default.vert", 
					"shaders/default.frag", null);
		}
		
		MyDisplay disp = new MyDisplay();

		GLHalfEdgeStructure glwf = new GLHalfEdgeStructure(hs);
		glwf.setTitle("Original");
		glwf.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");
		disp.addToDisplay(glwf);
		
		for (int i = 0; i < numEVs; i++) {
			disp.addToDisplay(displs[i]);
		}
	}
}
