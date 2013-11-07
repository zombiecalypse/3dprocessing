package tests;

import static helpers.StaticHelpers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import helpers.LMatrices;
import helpers.MyFunctions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Tuple3f;

import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;

import org.junit.Before;
import org.junit.Test;

import sparse.CSRMatrix;
import sparse.CSRMatrix.col_val;
import datastructure.halfedge.HalfEdgeStructure;

public class Assignment4_1_Tests {

	// A sphere of radius 2.
	private HalfEdgeStructure hs;
	// An ugly sphere of radius 1, don't expect the Laplacians
	// to perform accurately on this mesh.
	private HalfEdgeStructure hs2;

	@Before
	public void setUp() throws IOException, MeshNotOrientedException,
			DanglingTriangleException {
		WireframeMesh m = ObjReader.read("objs/sphere.obj", false);
		hs = new HalfEdgeStructure(m);

		m = ObjReader.read("objs/uglySphere.obj", false);
		hs2 = new HalfEdgeStructure(m);

	}

	public static void laplacianCheck(HalfEdgeStructure hs, CSRMatrix l,
			float expectedError) {
		assertNotNull(l);
		assertEquals(l.nRows, l.nCols);
		assertEquals(hs.getVertices().size(), l.nCols);
		for (int row = 0; row < l.nRows; row++) {
			List<col_val> t = l.rows.get(row);
			boolean anyNotZero = false;
			float sum = 0;
			for (col_val c : t) {
				anyNotZero |= c.val > 0.001;
				sum += c.val;
			}
			assertTrue(anyNotZero);
			assertEquals(0, sum, expectedError);
		}
	}

	@Test
	public void uniformLaplacianOnSphereRuns() {
		laplacianCheck(hs, LMatrices.uniformLaplacian(hs), 1e-6f);
	}

	@Test
	public void uniformLaplacianOnUglySphereRuns() {
		laplacianCheck(hs2, LMatrices.uniformLaplacian(hs2), 1e-6f);
	}

	@Test
	public void cotanLaplacianOnUglySphereRuns() { // quite messy
		laplacianCheck(hs2, LMatrices.mixedCotanLaplacian(hs2), 1e-2f);
	}

	@Test
	public void cotanLaplacianOnSphereRuns() { // many float computations
		laplacianCheck(hs, LMatrices.mixedCotanLaplacian(hs), 1e-5f); 
	}

	@Test
	public void cotanLaplacianOnSphereGivesCurvature() {
		CSRMatrix m = LMatrices.mixedCotanLaplacian(hs);
		List<Tuple3f> l = m.multComponentwise(new ArrayList<>(map(
				MyFunctions.pos, hs.getVertices())));
		for (Float f : map(MyFunctions.length, l)) {
			assertEquals(1.0f, f, 1e-4); // curvature * 2
		}
	}
}
