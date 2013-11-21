package assignment5;

import glWrapper.GLHalfEdgeFaces;
import glWrapper.GLHalfEdgeStructure;
import helpers.MyFunctions;

import java.util.ArrayList;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.vecmath.Matrix3f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import algorithms.collapse.HalfEdgeCollapse;

import com.google.common.base.Function;

import openGL.MyDisplay;
import meshes.WireframeMesh;
import meshes.reader.ObjReader;
import datastructure.halfedge.Face;
import datastructure.halfedge.HalfEdgeStructure;
import datastructure.halfedge.Vertex;
import static helpers.StaticHelpers.*;

/**
 * Some convenience methods for the visualization of epsilon-isosurfaces of
 * quadratic forms.
 * 
 * @author Alf
 * 
 */
public class Assignment5SmallEdges {
	private static Logger log = Logger.getLogger("Main");

	public static void main(String[] args) throws Exception {
		// Eliminated:      42132 halfedges      14044 faces       7022 vertices
		// Takes forever and two days
//		WireframeMesh wf = ObjReader.read("objs/dragon.obj", true);
		WireframeMesh wf = ObjReader.read("objs/dragon_10000.obj", true);
		HalfEdgeStructure hs = new HalfEdgeStructure(wf);
		HalfEdgeStructure hs2 = new HalfEdgeStructure(wf);

		final HalfEdgeCollapse hec = new HalfEdgeCollapse(hs);
		hec.collapseSmall(0.01f);
		log.info(String.format("Eliminated: %10s halfedges %10s faces %10s vertices",
				hec.deadEdges.size(), hec.deadFaces.size(), hec.deadVertices.size()));

		hs2.putExtractorFace("color", MyFunctions.asColor(MyFunctions
				.pure(new Function<Face, Float>() {
					@Override
					public Float apply(Face a) {
						if (hec.isFaceDead(a)) {
							return 1f;
						} else {
							return 0f;
						}
					}
				})));

		// visualize the isosurfaces of this bunny_ear
		// to compute the eigenvalues of some 3x3 matrix m:
		// eigs = new float[3];
		// eigenValues(m, eigs);
		//
		// to compute the eigenvector for an eigenvalue eigs[i]
		// (yes, THE eigenvector, the method will fail if an eigenspace
		// has higher dimension than 1. This does not happen on the bunny ear.
		// Feel free to improve/use a different method :- ) )
		// eigenVector(m, eigs[i]);

		MyDisplay disp = new MyDisplay();

		GLHalfEdgeStructure glear = new GLHalfEdgeStructure(hs);
		glear.configurePreferredShader("shaders/trimesh_flatColor3f.vert", 
				"shaders/trimesh_flatColor3f.frag", "shaders/trimesh_flatColor3f.geom");
		glear.setTitle("Mesh Simple");
		disp.addToDisplay(glear);
		
		GLHalfEdgeFaces glear2 = new GLHalfEdgeFaces(hs2);
		glear2.configurePreferredShader("shaders/trimesh_flatColor3f.vert", 
				"shaders/trimesh_flatColor3f.frag", "shaders/trimesh_flatColor3f.geom");
		glear2.setTitle("Mesh marked");
		disp.addToDisplay(glear2);
	}
}
