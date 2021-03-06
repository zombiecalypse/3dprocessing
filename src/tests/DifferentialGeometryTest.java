package tests;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static helpers.StaticHelpers.*;
import helpers.MyFunctions;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.google.common.base.Function;

import datastructure.halfedge.Face;
import datastructure.halfedge.HalfEdge;
import datastructure.halfedge.HalfEdgeStructure;
import datastructure.halfedge.Vertex;

@RunWith(Parameterized.class)
public class DifferentialGeometryTest {
	
	@Parameterized.Parameters
	public static Collection Vertices() throws IOException, MeshNotOrientedException, DanglingTriangleException {
		WireframeMesh mPot = ObjReader.read("./objs/teapot.obj", true);
		WireframeMesh mDragon = ObjReader.read("./objs/dragon.obj", true);

		HalfEdgeStructure teapot = new HalfEdgeStructure(mPot);
		HalfEdgeStructure dragon = new HalfEdgeStructure(mDragon);
		
		return Arrays.asList(new Object[][] {
				{teapot}, {dragon} 
		});
	}

	private HalfEdgeStructure mesh;
	
	public DifferentialGeometryTest(HalfEdgeStructure mesh) {
		this.mesh = mesh;
	}

	@Test
	public void testTesselationAnglesAddUp() {
		for (Face f: mesh.getFaces()) {
			float angleSum = 0;
			for (HalfEdge e: iter(f.iteratorFE())) {
				angleSum += e.opposingAngle();
			}
			assertEquals(2*Math.PI, angleSum, 0.001);
		}
	}
	
	@Test
	public void testCurvatureSanity() {
		Function<Vertex, Float> laplacian = MyFunctions.laplacian();
		for (Vertex v: mesh.getVertices()) {
			assertThat(0f, is(lessThanOrEqualTo(laplacian.apply(v))));
		}
	}


	@Ignore("We don't have the gaussian curvature yet")
	@Test
	public void testCurvatureIntegral() {
		Function<Vertex, Float> curvature = MyFunctions.laplacian();
		float sum = 0;
		for (Vertex v: mesh.getVertices()) {
			sum += curvature.apply(v);
		}
		assertEquals(2*Math.PI, sum, 0.001);
	}
}
