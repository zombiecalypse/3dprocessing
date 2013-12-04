package tests;

import static org.junit.Assert.*;

import sparse.Linalg3x3;

import java.util.Random;

import javax.vecmath.Matrix3f;

import org.junit.Before;
import org.junit.Test;

public class SVDTests {

	Linalg3x3 l;
	Matrix3f symm, unsymm;
	
	@Before
	public void setUp(){
		l = new Linalg3x3(10);
		symm = new Matrix3f(0.1f,0.2f,0.05f,
				0.2f, -1.f,  0.8f, 
				0.05f, 0.8f, -0.3f);
		unsymm = new Matrix3f(0.1f,0.1f,0.05f,
				-0.2f, -1.f,  0.3f, 
				-0.05f, 0.2f, -0.3f);
	}
	
	@Test
	public void testSvd() {
		Matrix3f U = new Matrix3f();
		Matrix3f sigma = new Matrix3f();
		Matrix3f V = new Matrix3f();
		
		int numTests = 1000000;
		Random r = new Random(1);
		long time = System.currentTimeMillis();
		for(int i = 0; i < numTests; i++){
			unsymm = new Matrix3f(r.nextFloat()-0.5f,r.nextFloat()-0.5f,r.nextFloat()-0.5f,
					r.nextFloat()-0.5f,r.nextFloat()-0.5f,r.nextFloat()-0.5f,
					r.nextFloat()-0.5f,r.nextFloat()-0.5f,r.nextFloat()-0.5f);
			l.svd(unsymm, U, sigma, V);
			
			//System.out.println(U.determinant());
			assertTrue(Math.abs(Math.abs(U.determinant())- 1)< 1e-3);
			
			//System.out.println(V.determinant());
			assertTrue(Math.abs(V.determinant()-1)< 1e-3);
			
			assertTrue(Math.abs(sigma.m00) >= Math.abs(sigma.m11) && 
					Math.abs(sigma.m11) > Math.abs(sigma.m22));
			
			sigma.mulTransposeRight(sigma, V);
			sigma.mul(U, sigma);
			
			assertTrue(sigma.epsilonEquals(unsymm, 1e-5f));
		}
		System.out.println((1.0*System.currentTimeMillis() - time)/numTests + " ms per svd");
		
	}
	
	@Test
	public void svdTest2(){
		Matrix3f m = new Matrix3f();
		
		Matrix3f U = new Matrix3f();
		Matrix3f sigma = new Matrix3f();
		Matrix3f V = new Matrix3f();
		
		l.svd(m, U, sigma, V);
		
		assertTrue(Math.abs(V.determinant()-1)< 1e-3);
		
		assertTrue(Math.abs(sigma.m00) >= Math.abs(sigma.m11) && 
				Math.abs(sigma.m11) >= Math.abs(sigma.m22));
		
		sigma.mulTransposeRight(sigma, V);
		sigma.mul(U, sigma);
		
		assertTrue(sigma.epsilonEquals(m, 1e-5f));
	}
	
	@Test
	public void svdTest3(){
		Matrix3f m = new Matrix3f(6.6667e-03f, -2.9141e-10f, 0.0000e+00f,
				-2.9141e-10f, 5.0838e+04f, 0.0000e+00f,
				0.0000e+00f, 0.0000e+00f, 0.0000e+00f);
		
		Matrix3f U = new Matrix3f();
		Matrix3f sigma = new Matrix3f();
		Matrix3f V = new Matrix3f();
		
		l.svd(m, U, sigma, V);
		
		assertTrue(Math.abs(V.determinant()-1)< 1e-3);
		
		assertTrue(Math.abs(sigma.m00) >= Math.abs(sigma.m11) && 
				Math.abs(sigma.m11) >= Math.abs(sigma.m22));
		
		sigma.mulTransposeRight(sigma, V);
		sigma.mul(U, sigma);
		
		assertTrue(sigma.epsilonEquals(m, 1e-5f));
	}

	@Test
	public void testJacobiEVD() {
		Matrix3f q = new Matrix3f(), sigma = new Matrix3f();
		l.jacobiEVD(symm, q, sigma);
		
		Matrix3f res = new Matrix3f();
		res.mul(q, sigma);
		res.mulTransposeRight(res, q);
		
		assertTrue(res.epsilonEquals(symm, 1e-3f));
	}
	
	@Test
	public void testJacobiEVD2() {
		Matrix3f q = new Matrix3f(), sigma = new Matrix3f();
		Matrix3f m = new Matrix3f();
		l.jacobiEVD(m, q, sigma);
		
		Matrix3f res = new Matrix3f();
		res.mul(q, sigma);
		res.mulTransposeRight(res, q);
		
		assertTrue(res.epsilonEquals(m, 1e-3f));
	}

	@Test
	public void testQR() {
		Matrix3f q = new Matrix3f(), r = new Matrix3f();
		l.givensQr(unsymm, q, r);
		
		Matrix3f res = new Matrix3f();
		res.mul(q, r);
				
		assertTrue(res.epsilonEquals(unsymm, 1e-3f));
	}
}
