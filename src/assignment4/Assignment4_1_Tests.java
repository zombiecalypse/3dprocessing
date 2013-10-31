package assignment4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

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
	public void setUp() throws IOException, MeshNotOrientedException, DanglingTriangleException {
		WireframeMesh m = ObjReader.read("objs/sphere.obj", false);
		hs = new HalfEdgeStructure(m);

		m = ObjReader.read("objs/uglySphere.obj", false);
		hs2 = new HalfEdgeStructure(m);

	}

	public static void laplacianCheck(HalfEdgeStructure hs, CSRMatrix l) {
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
			assertEquals(0, sum, 1e-2);
		}
	}

	@Test
	public void uniformLaplacianOnSphereRuns() {
		laplacianCheck(hs, LMatrices.uniformLaplacian(hs));
	}

  @Test
  public void uniformLaplacianOnUglySphereRuns() {
    laplacianCheck(hs2, LMatrices.uniformLaplacian(hs2));
  }

  @Test
  public void cotanLaplacianOnUglySphereRuns() {
    laplacianCheck(hs2, LMatrices.mixedCotanLaplacian(hs2));
  }
  @Test
  public void cotanLaplacianOnSphereRuns() {
    laplacianCheck(hs, LMatrices.mixedCotanLaplacian(hs));
  }
}
