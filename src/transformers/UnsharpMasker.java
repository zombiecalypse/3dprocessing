package transformers;

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

public class UnsharpMasker implements
		Function<HalfEdgeStructure, HalfEdgeStructure> {
	private float weight;

	public UnsharpMasker(Function<HalfEdgeStructure, HalfEdgeStructure> t, float weight) {
		this.smoother = t;
		this.weight = weight;
	}

	final Function<HalfEdgeStructure, HalfEdgeStructure> smoother;
	
	@Override
	public HalfEdgeStructure apply(HalfEdgeStructure hs) {
		HalfEdgeStructure transformed = smoother.apply(hs);

		for (Vertex v : transformed.getVertices()) {
			Point3f otherPos = hs.getVertices().get(v.index).pos;
			Point3f movement = V.sub(v.pos, otherPos);
			v.pos = V.scaled_add(otherPos, 1, movement, -weight);
		}

		float volRelative = (float) Math.pow(hs.getVolume()/transformed.getVolume(), 1.0/3);
		for (Vertex v : transformed.getVertices()) {
			v.pos.x = v.pos.x * volRelative;
			v.pos.y = v.pos.y * volRelative;
			v.pos.z = v.pos.z * volRelative;
		}
		return transformed;
	}
	
	public static HalfEdgeStructure unsharp(float lambda, float weight, HalfEdgeStructure hs) {
		return new UnsharpMasker(ImplicitSmoother.mixed(lambda), weight).apply(hs);
	}
}
