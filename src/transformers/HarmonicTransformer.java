package transformers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class HarmonicTransformer implements
		Function<HalfEdgeStructure, HalfEdgeStructure> {
	private Function<Integer, Float> dampening;
	
	static Map<HalfEdgeStructure, ArrayList<ArrayList<Float>>> cache = new HashMap<>();

	public HarmonicTransformer(Function<Integer, Float> dampening) {
		this.dampening = dampening;
	}

	@Override
	public HalfEdgeStructure apply(HalfEdgeStructure hs) {
		HalfEdgeStructure smooth = new HalfEdgeStructure(hs);

		ArrayList<ArrayList<Float>> eigenVectors = eigenfrequencies(hs);

		SparseDictMatrix m = new SparseDictMatrix();
		SparseDictMatrix mT = new SparseDictMatrix();
		
		for (Indexed<ArrayList<Float>> p : withIndex(eigenVectors)) {
			float coeff = this.dampening.apply(p.index());
			for (Indexed<Float> q: withIndex(p.value())) {
				m.add(p.index(), q.index(), q.value() * coeff);
				mT.add(q.index(), p.index(), q.value() * coeff);
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

	private ArrayList<ArrayList<Float>> eigenfrequencies(HalfEdgeStructure hs) {
		if (cache.containsKey(hs)){
			System.out.println("Cache hit");
			return cache.get(hs);
		}
		CSRMatrix laplacian = LMatrices.symmetricCotanLaplacian(hs);
		
		ArrayList<Float> eigenValues = new ArrayList<>();
		ArrayList<ArrayList<Float>> eigenVectors = new ArrayList<>();
		try {
			SCIPYEVD.doSVD(laplacian, "eigen", hs.getVertices().size(), eigenValues , eigenVectors);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
		cache.put(hs, eigenVectors);
		return eigenVectors;
	}
	
}
