package transformers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import sparse.CSRMatrix;
import sparse.LinearSystem;
import sparse.solver.JMTSolver;
import sparse.solver.SciPySolver;
import sparse.solver.Solver;
import helpers.LMatrices;
import helpers.MyFunctions;
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
public class ImplicitSmoother implements
		Function<HalfEdgeStructure, HalfEdgeStructure> {
	public ImplicitSmoother(float lambda) {
		this.lambda = lambda;
	}

	final float lambda;
	ExecutorService tp = Executors.newCachedThreadPool();
	
	@Override
	public HalfEdgeStructure apply(HalfEdgeStructure hs) {
		CSRMatrix laplacian = LMatrices.mixedCotanLaplacian(hs);
		List<Vertex> vs = hs.getVertices();
		laplacian.scale(-lambda);
		CSRMatrix m = new CSRMatrix(0, 0);
		m.add(SSDMatrices.eye(laplacian.nRows, laplacian.nCols), laplacian);
		final List<Float> oxs = new ArrayList<>();
		final List<Float> oys = new ArrayList<>();
		final List<Float> ozs = new ArrayList<>();
		for (Vertex v : vs) {
			oxs.add(v.pos.x);
			oys.add(v.pos.y);
			ozs.add(v.pos.z);
		}
		final LinearSystem systemX = new LinearSystem(
				m, oxs);
		final LinearSystem systemY = new LinearSystem(
				m, oys);
		final LinearSystem systemZ = new LinearSystem(
						m, ozs);

		
		final ArrayList<Float> xs = solve(systemX, "x");
		final ArrayList<Float> ys = solve(systemY, "y");
		final ArrayList<Float> zs = solve(systemZ, "z");
		
		try {
			System.out.println("Python wait...");
			tp.shutdown();
			tp.awaitTermination(60, TimeUnit.SECONDS);
			System.out.println("Python done.");

			HalfEdgeStructure hs2 = new HalfEdgeStructure(hs);
			for (Vertex v : hs2.getVertices()) {
				v.pos.x = xs.get(v.index);
				v.pos.y = ys.get(v.index);
				v.pos.z = zs.get(v.index);
			}
			
			float volRelative = (float) Math.pow(hs.getVolume()/hs2.getVolume(), 1.0/3);
			
			for (Vertex v : hs2.getVertices()) {
				v.pos.x = v.pos.x * volRelative;
				v.pos.y = v.pos.y * volRelative;
				v.pos.z = v.pos.z * volRelative;
			}
			return hs2;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
		
	}

	private ArrayList<Float> solve(final LinearSystem systemX, final String ii) {
		final ArrayList<Float> xs = new ArrayList<>();
		tp.execute(new Runnable() {
			@Override
			public void run() {
				final Solver s = new SciPySolver("laplacian"+ii);
				s.solve(systemX, xs);
			}
		});
		return xs;
	}

}
