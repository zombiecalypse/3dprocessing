package assignment4;

import static helpers.StaticHelpers.*;
import glWrapper.GLHalfEdgeStructure;
import helpers.LMatrices;
import helpers.StaticHelpers.Indexed;
import helpers.StaticHelpers.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Logger;

import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import com.google.common.base.Function;

import datastructure.halfedge.HalfEdgeStructure;
import datastructure.halfedge.Vertex;
import meshes.WireframeMesh;
import openGL.MyDisplay;
import sparse.CSRMatrix;
import sparse.CSRMatrix.col_val;
import sparse.SparseDictMatrix;
import sparse.solver.JMTSolver;
import sparse.solver.SciPySolver;
import sparse.solver.Solver;
import assignment4.generatedMeshes.Bock;
import assignment4.generatedMeshes.Cylinder;

public class Assignment4_3_minimalSurfaces {
	static Logger log = Logger.getLogger("MinSurface");

	public static void main(String[] args) throws Exception {

		// generate example meshes
//		 WireframeMesh m = new Bock(1.3f,1.f,1.f).result;
		WireframeMesh m = new Cylinder(1.f, 1.5f).result;

		// generate he struture
		HalfEdgeStructure hs = new HalfEdgeStructure(m);

		Solver s = new JMTSolver();

		int i = 0;
		while (i++ < 50) {
			float oldArea = hs.surfaceArea();
			SparseDictMatrix laplacian = LMatrices.mixedCotanLaplacianBare(hs);
			ArrayList<Tuple3f> boundary = new ArrayList<>();
			for (Vertex v : hs.getVertices()) {
				if (v.isOnBoundary()) {
					boundary.add(new Point3f(v.pos));
					laplacian.add(v.index, v.index, -1);
				} else {
					boundary.add(new Vector3f());
				}
			}
			ArrayList<Tuple3f> x = new ArrayList<>();
			s.solveTuple(laplacian.toCsr(), boundary, x);
			for (Vertex v : hs.getVertices()) {
				v.pos = new Point3f(x.get(v.index));
			}
			float newArea = hs.surfaceArea();
			log.info(String.format("Old: %.3f New: %.3f ratio: %.3f", oldArea, newArea, newArea/oldArea));
			if (newArea / oldArea > 0.99f)
				break;
		}

		// collect and display the boundary
		display(hs);
	}

	/**
	 * Display the halfedge structure and highlight the set of vertices
	 * described by boundary
	 * 
	 * @param hs
	 * @param boundary
	 */
	public static void display(final HalfEdgeStructure hs) {
		MyDisplay disp = new MyDisplay();
		hs.putExtractor("color", new Function<Indexed<Vertex>, Float>() {
			final int[] border = collectBoundary(hs, 1);

			@Override
			public Float apply(Indexed<Vertex> input) {
				return (float) border[input.index()];
			}
		});

		GLHalfEdgeStructure glHE = new GLHalfEdgeStructure(hs);
		glHE.configurePreferredShader("shaders/trimesh_flatColor3f.vert",
				"shaders/trimesh_flatColor3f.frag",
				"shaders/trimesh_flatColor3f.geom");
		disp.addToDisplay(glHE);
	}

	/**
	 * Collect the boundary: this method returns a HEData1d object containing a
	 * 1 for each vertex that is maximally dist number of vertices away from the
	 * boundary
	 * 
	 * @param hs
	 * @param dist
	 * @return
	 */
	public static int[] collectBoundary(HalfEdgeStructure hs, int dist) {

		int[] has_jm1_dist = new int[hs.getVertices().size()];
		Stack<Pair<Vertex, Integer>> toMark = new Stack<>();
		for (Vertex v : hs.getVertices()) {
			if (v.isOnBoundary()) {
				toMark.add(pair(v, 0));
			}
		}

		while (!toMark.empty()) {
			Pair<Vertex, Integer> p = toMark.pop();
			if (p.b < dist) {
				for (Vertex vv : iter(p.a.iteratorVV())) {
					toMark.add(pair(vv, p.b + 1));
				}
			}
			has_jm1_dist[p.a.index] = 1;
		}

		return has_jm1_dist;
	}
}
