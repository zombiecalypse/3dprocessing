package transformers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import sparse.CSRMatrix;
import sparse.LinearSystem;
import sparse.SCIPYEVD;
import sparse.SparseDictMatrix;
import sparse.solver.JMTSolver;
import sparse.solver.SciPySolver;
import sparse.solver.Solver;
import helpers.LMatrices;
import helpers.MyFunctions;
import helpers.StaticHelpers.Indexed;
import helpers.StaticHelpers.Pair;
import helpers.V;
import algorithms.energy.SSDMatrices;
import static helpers.StaticHelpers.*;

import com.google.common.base.Function;
import com.google.common.base.Functions;

import datastructure.halfedge.HalfEdgeStructure;
import datastructure.halfedge.Vertex;

/**
 * solves (I-lambda*laplacian) * p' = p.
 * 
 * @author aaron
 * 
 */
public class CutHarmonicSmoother implements
		Function<HalfEdgeStructure, HalfEdgeStructure> {
	private int n;

	public CutHarmonicSmoother(int n) {
		this.n = n;
	}

	@Override
	public HalfEdgeStructure apply(HalfEdgeStructure hs) {
		HalfEdgeStructure smooth = new HalfEdgeStructure(hs);

		CSRMatrix laplacian = LMatrices.symmetricCotanLaplacian(hs);
		
		ArrayList<Float> eigenValues = new ArrayList<>();
		ArrayList<ArrayList<Float>> eigenVectors = new ArrayList<>();
		try {
			SCIPYEVD.doSVD(laplacian, "eigen", n, eigenValues , eigenVectors);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}

		SparseDictMatrix m = new SparseDictMatrix();
		SparseDictMatrix mT = new SparseDictMatrix();
		
		for (Indexed<ArrayList<Float>> p : withIndex(eigenVectors)) {
			for (Indexed<Float> q: withIndex(p.value())) {
				m.add(p.index(), q.index(), q.value());
				mT.add(q.index(), p.index(), q.value());
			}
		}
		
		CSRMatrix mTm = new CSRMatrix(0,0);
		mT.toCsr().multParallel(m.toCsr(), mTm);
		
		List<Tuple3f> newPos = mTm.multComponentwise(new ArrayList<>(map(MyFunctions.pos, hs.getVertices())));
		
		for (Vertex v : smooth.getVertices()) {
			v.pos = new Point3f(newPos.get(v.index));
		}

		float correction = (float) Math.pow(hs.getVolume()/smooth.getVolume(), 1/3.0);
		for (Vertex v : smooth.getVertices()) {
			v.pos.scale(correction);
		}
		
		return smooth;
	}
	
}
