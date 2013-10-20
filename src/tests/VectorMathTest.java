package tests;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import helpers.V;

import javax.vecmath.Point3f;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class VectorMathTest {
	static Object[] asArray(Object...objects) {
		return objects;
	}
	
	@Parameterized.Parameters
	public static Collection vectors() {
		return Arrays.asList(
				asArray(new Point3f(1,1,1), new Point3f(0,1,1), 0.5f, new Point3f(0.5f, 1, 1)),
				asArray(new Point3f(1,1,1), new Point3f(0,1,1), 0.25f, new Point3f(0.25f, 1, 1))
				);
	}

	private Point3f x;
	private Point3f y;
	private float w;
	private Point3f result;
	
	public VectorMathTest(Point3f x, Point3f y, float w, Point3f result) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.result = result;
	}

	@Test
	public void testScaleAdd() {
		Point3f newPt = V.scaled_add(x, w, y, 1-w);
		assertEquals(newPt.x, result.x, 1e-10f);
		assertEquals(newPt.y, result.y, 1e-10f);
		assertEquals(newPt.z, result.z, 1e-10f);
	}
}
