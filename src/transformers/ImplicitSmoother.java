package transformers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

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
public abstract class ImplicitSmoother implements
		Function<HalfEdgeStructure, HalfEdgeStructure> {
	public ImplicitSmoother(float lambda) {
		this.lambda = lambda;
	}

	final float lambda;
	ExecutorService tp = Executors.newCachedThreadPool();

	abstract CSRMatrix laplacian(HalfEdgeStructure hs);

	@Override
	public HalfEdgeStructure apply(HalfEdgeStructure hs) {
		CSRMatrix laplacian = laplacian(hs);
		List<Vertex> vs = hs.getVertices();
		laplacian.scale(-lambda);
		CSRMatrix m = new CSRMatrix(0, 0);
		m.add(SSDMatrices.eye(laplacian.nRows, laplacian.nCols), laplacian);
		ArrayList<Vector3f> pos = new ArrayList<>();
		for (Vertex v : hs.getVertices()) {
			pos.add(new Vector3f(v.getPos()));
		}
		Solver s = new JMTSolver();
		ArrayList<Vector3f> x = new ArrayList<>();
		s.solveTuple(m, pos, x);

		HalfEdgeStructure hs2 = new HalfEdgeStructure(hs);
		for (Vertex v : hs2.getVertices()) {
			v.pos = new Point3f(x.get(v.index));
		}

		float volRelative = (float) Math.pow(hs.getVolume() / hs2.getVolume(),
				1.0 / 3);

		for (Vertex v : hs2.getVertices()) {
			v.pos.x = v.pos.x * volRelative;
			v.pos.y = v.pos.y * volRelative;
			v.pos.z = v.pos.z * volRelative;
		}
		return hs2;

	}

	private ArrayList<Float> solve(final LinearSystem systemX, final String ii) {
		final ArrayList<Float> xs = new ArrayList<>();
		tp.execute(new Runnable() {
			@Override
			public void run() {
				final Solver s = new JMTSolver();// SciPySolver("laplacian"+ii);
				s.solve(systemX, xs);
			}
		});
		return xs;
	}

	private static class Uniform extends ImplicitSmoother {
		public Uniform(float lambda) {
			super(lambda);
		}

		@Override
		CSRMatrix laplacian(HalfEdgeStructure hs) {
			return LMatrices.uniformLaplacian(hs);
		}

	}

	private static class Mixed extends ImplicitSmoother {
		public Mixed(float lambda) {
			super(lambda);
		}

		@Override
		CSRMatrix laplacian(HalfEdgeStructure hs) {
			return LMatrices.mixedCotanLaplacian(hs);
		}

	}

	public static HalfEdgeStructure mixed(float l, HalfEdgeStructure hs) {
		return new Mixed(l).apply(hs);
	}

	public static HalfEdgeStructure uniform(float l, HalfEdgeStructure hs) {
		return new Uniform(l).apply(hs);
	}

	public static Mixed mixed(float l) {
		return new Mixed(l);
	}

	public static Uniform uniform(float l) {
		return new Uniform(l);
	}
}
