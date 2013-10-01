package tests;

import static org.junit.Assert.*;
import static helpers.StaticHelpers.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import junit.framework.TestCase;
import meshes.Face;
import meshes.HalfEdge;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;

import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Parameterized.class)
public class HalfEdgeIterators extends TestCase {

	private HalfEdgeStructure hs;
	
	public int index;
	public int nEdges, nFaces, nVertices;

	private Vertex vertex;
	
	public HalfEdgeIterators(int a, int b, int c, int d) {
		index = a;
		nEdges = b;
		nFaces = c;
		nVertices = d;
	}

	@Before
	public void setUp() throws IOException, MeshNotOrientedException,
			DanglingTriangleException {
		WireframeMesh m = ObjReader.read("./objs/oneNeighborhood.obj", true);
		this.hs = new HalfEdgeStructure(m);
		this.hs = new HalfEdgeStructure(hs);

		this.vertex = hs.getVertices().get(index);
	}
	
	@Parameterized.Parameters
	public static Collection Vertices() {
		return Arrays.asList(new Object[][] {
				{ 0, 5, 5, 5 },
				{ 1, 3, 2, 3 },
				{ 2, 3, 2, 3 },
				{ 3, 3, 2, 3 },
				{ 4, 3, 2, 3 },
				{ 5, 3, 2, 3 }
		});
	}

	@Test
	public void indexCorrect() {
		assertEquals(index, vertex.index);
	}
	
	@Test
	public void nEdgesFits() {
		assertEquals(nEdges, len(set(vertex.iteratorVE())));
	}
	
	@Test
	public void nFacesFits() {
		assertEquals(nFaces, len(set(vertex.iteratorVF())));
	}
	
	@Test
	public void nVerticesFits() {
		assertEquals(nVertices, len(set(vertex.iteratorVV())));
	}
	
	@Test
	public void NotInOwnNeighborhood() {
		for (Vertex x: iter(vertex.iteratorVV())) {
			assertNotSame(x, vertex);
		}
	}

	@Test
	public void NoEmptyFaces() {
		for (Face x: iter(vertex.iteratorVF())) {
			assertNotNull(x);
		}
	}
	
	@Test
	public void FacesHaveVertices() {
		for (Face x: iter(vertex.iteratorVF())) {
			assertEquals(3, len(x.iteratorFV()));
			assertEquals(3, len(x.iteratorFE()));
		}
	}
	
	@Test
	public void ManualTest() {
		System.out.format("Vertex(%d):\n", index);
		System.out.print("  Vertices: ");
		for (Vertex v: iter(vertex.iteratorVV())) {
			System.out.format("%d, ", v.index);
		}

		System.out.println();
		System.out.print("  Edges:\n");
		for (HalfEdge v: iter(vertex.iteratorVE())) {
			System.out.format("      %s\n", v);
		}
		System.out.print("  Faces:\n");
		for (Face v: iter(vertex.iteratorVF())) {
			System.out.format("      %s\n", v);
		}
		System.out.println();
	}
}
