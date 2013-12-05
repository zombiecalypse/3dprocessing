package assignment6;

import helpers.LMatrices;
import helpers.MyFunctions;
import helpers.StaticHelpers.Pair;
import helpers.V;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import datastructure.halfedge.HalfEdge;
import datastructure.halfedge.HalfEdgeStructure;
import datastructure.halfedge.Vertex;
import sparse.CSRMatrix;
import sparse.CSRMatrix.col_val;
import sparse.Linalg3x3;
import sparse.solver.Cholesky;
import sparse.solver.JMTSolver;
import sparse.solver.Solver;
import static helpers.StaticHelpers.*;

/**
 * As rigid as possible deformations.
 * 
 * @author Alf
 * 
 */
public class RAPS_modelling {
	private final static Logger log = Logger.getLogger("RAPS");
	private final static float weight = 100f;
	private final static Linalg3x3 svd = new Linalg3x3(3);

	// ArrayList containing all optimized rotations,
	// keyed by vertex.index
	ArrayList<Matrix3f> rotations;

	// A copy of the original half-edge structure. This is needed to compute the
	// correct
	// rotation matrices.
	private HalfEdgeStructure hs_original;
	// The halfedge structure being deformed
	private HalfEdgeStructure hs_deformed;

	// The unnormalized cotan weight matrix, with zero rows for
	// boundary vertices.
	// It can be computed once at setup time and then be reused
	// to compute the matrix needed for position optimization
	CSRMatrix ltl;
	// The matrix used when solving for optimal positions
	CSRMatrix L_deform;

	// allocate righthand sides and x only once.
	ArrayList<Tuple3f> b;
	ArrayList<Tuple3f> x;

	// sets of vertex indices that are constrained.
	private HashSet<Integer> keepFixed;
	private HashSet<Integer> deform;

	private Solver solver;
	private CSRMatrix laplacian_transposed;
	private CSRMatrix constraints;
	private ArrayList<Tuple3f> ltb = new ArrayList<>();

	/**
	 * The mesh to be deformed
	 * 
	 * @param hs
	 */
	public RAPS_modelling(HalfEdgeStructure hs) {
		this.hs_original = new HalfEdgeStructure(hs); // deep copy of the original mesh
		this.hs_deformed = hs;

		this.keepFixed = new HashSet<>();
		this.deform = new HashSet<>();

		init_b_x(hs);

	}

	/**
	 * Set which vertices should be kept fixed.
	 * 
	 * @param verts_idx
	 */
	public void keep(Collection<Integer> verts_idx) {
		this.keepFixed.clear();
		this.keepFixed.addAll(verts_idx);
		for (Vertex v : hs_original.getVertices()) {
			keepFixed.add(v.index);
		}

	}

	/**
	 * constrain these vertices to the new target position
	 */
	public void target(Collection<Integer> vert_idx) {
		this.deform.clear();
		this.deform.addAll(vert_idx);
	}

	/**
	 * update the linear system used to find optimal positions for the currently
	 * constrained vertices. Good place to do the cholesky decomposition
	 */
	public void updateL() {
		// on lifting (um... lifting off a 3d table in a 4d space or something),
		// we can precompute the cholesky decomposition to solve
		// the positioning step easier.
		final int nverts = hs_original.getVertices().size();
		CSRMatrix laplacian = LMatrices.mixedCotanLaplacian(hs_original, false);
		constraints = getConstraints();
		L_deform = new CSRMatrix(0, nverts);
		ltl = new CSRMatrix(0, nverts);
		this.laplacian_transposed = laplacian.transposed();
		laplacian_transposed.multParallel(laplacian, ltl);
		L_deform.add(ltl, constraints);
//		V.assertSymmetric(L_deform.toSDM());
		V.assertPositiveDefinite(L_deform);

		solver = new Cholesky(L_deform);

		resetRotations();
	}

	/** Constraints set by user */
	private CSRMatrix getConstraints() {
		int nverts = hs_original.getVertices().size();
		CSRMatrix constraints = new CSRMatrix(nverts, nverts);
		for (int index = 0; index < nverts; index++) {
			if (isUserConstrained(index)) {
				constraints.rows.get(index).add(
						new col_val(index, weight * weight)); // squared because
																// L is
																// "squared"
			}
		}
		return constraints;
	}

	private boolean isUserConstrained(int index) {
		return deform.contains(index) || keepFixed.contains(index);
	}

	private void resetRotations() {
		rotations = new ArrayList<Matrix3f>();
		final int nvert = hs_original.getVertices().size();
		for (int i = 0; i < nvert; i++) {
			Matrix3f id = new Matrix3f();
			id.setIdentity();
			rotations.add(id);
		}
	}

	/**
	 * The RAPS modelling algorithm.
	 * 
	 * @param t
	 * @param nRefinements
	 */
	public void deform(Matrix4f t, int nRefinements) {
		this.transformTarget(t);

		for (int i = 0; i < nRefinements; i++) {
			optimalPositions();
			optimalRotations();
			log.fine(String.format("RAPS iteration: %02d", i));
		}
	}

	/**
	 * Method to transform the target positions and do nothing else.
	 * 
	 * @param t
	 */
	public void transformTarget(Matrix4f t) {
		for (Vertex v : hs_deformed.getVertices()) {
			if (deform.contains(v.index)) {
				t.transform(v.getPos());
			}
		}
	}

	/** @return ArrayList keyed with the vertex indices. */
	public ArrayList<Matrix3f> getRotations() {
		return rotations;
	}

	/** @return undeformed version of the mesh. */
	public HalfEdgeStructure getOriginalCopy() {
		return hs_original;
	}

	private void init_b_x(HalfEdgeStructure hs) {
		b = new ArrayList<>(hs.getVertices().size());
		for (int j = 0; j < hs.getVertices().size(); j++) {
			b.add(new Vector3f());
		}
		x = new ArrayList<>(hs.getVertices().size());
		for (int j = 0; j < hs.getVertices().size(); j++) {
			x.add(new Vector3f());
		}
	}

	/**
	 * Compute optimal positions for the current rotations.
	 */
	public void optimalPositions() {
		compute_b();
		solver.solveTuple(L_deform, new ArrayList<>(ltb), x);
		hs_deformed.setPos(x);
	}

	/**
	 * compute the righthand side for the position optimization
	 */
	private void compute_b() {
		reset_b();

		for (Vertex v : hs_original.getVertices()) {
			if (!this.isUserConstrained(v.index) && !v.isOnBoundary()) {
				for (HalfEdge e : iter(v.iteratorVE())) {
					// rot ≃ -cotanWeights/2 (rot_begin + rot_end)
					Matrix3f rot = new Matrix3f(rotations.get(e.start().index));
					rot.add(rotations.get(e.end().index));
					rot.mul(-.5f * e.cotanWeight());
					Vector3f vector = e.asVector();
					rot.transform(vector);
					b.get(v.index).add(vector);
				}
			}
		}

		// We prefer to solve L^T L x = L^T b
		laplacian_transposed.multTuple(b, ltb);
		ArrayList<Tuple3f> positions = new ArrayList<>(map(MyFunctions.pos,
				hs_deformed.getVertices()));
		ArrayList<Tuple3f> newPositions = new ArrayList<>();
		constraints.multTuple(positions, newPositions);
		for (Pair<Tuple3f, Tuple3f> p : zip(ltb, newPositions)) {
			p.a.add(p.b);
		}
	}

	private void reset_b() {
		for (Pair<Tuple3f, Tuple3f> v : zip(b, map(MyFunctions.pos, hs_deformed.getVertices()))) {
			v.a.set(v.b);
		}
	}

	/**
	 * Compute the optimal rotations for 1-neighborhoods, given the original and
	 * deformed positions.
	 */
	public void optimalRotations() {
		// for the svd.
		Linalg3x3 l = new Linalg3x3(3);// argument controls number of
										// iterations for ed/svd decompositions
										// 3 = very low precision but high
										// speed. 3 seems to be good enough

		for (Indexed<Matrix3f> irot : withIndex(rotations)) {
			irot.value().set(0);
			Vertex v_old = hs_original.getVertices().get(irot.index());
			Vertex v_new = hs_deformed.getVertices().get(irot.index());
			for (Pair<HalfEdge, HalfEdge> ee : zip(iter(v_old.iteratorVE()),
					iter(v_new.iteratorVE()))) {
				// outer product of vecs.
				Matrix3f ppT = compute_ppT(ee.a.asVector(), ee.b.asVector());

				// Note: slightly better results are achieved when the absolute
				// of cotangent weights w_ij are used instead of plain cotangent
				// weights.
				float cotanWeight = Math.abs(ee.b.cotanWeight());
				ppT.mul(cotanWeight);
				irot.value().add(ppT);
			}
			makeRotationMatrix(irot.value());
		}
	}

	// Reuse, because garbage collector
	Matrix3f MU = new Matrix3f();
	Matrix3f MV = new Matrix3f();
	Matrix3f MD = new Matrix3f();

	private void makeRotationMatrix(Matrix3f rot) {
		svd.svd(rot, MU, MD, MV);
		if (MU.determinant() < 0) {
			Vector3f col = new Vector3f();
			// because f- the third column. There is probably a joke in there
			// which I'm not going to make.
			MU.getColumn(2, col);
			col.negate();
			MU.setColumn(2, col);
		}
		MU.transpose();
		MV.mul(MU);
		rot.set(MV);
	}

	private Matrix3f compute_ppT(Vector3f p, Vector3f p2) {
		assert (p.x * 0 == 0);
		assert (p.y * 0 == 0);
		assert (p.z * 0 == 0);

		Matrix3f pp2T = new Matrix3f();
		pp2T.m00 = p.x * p2.x;
		pp2T.m01 = p.x * p2.y;
		pp2T.m02 = p.x * p2.z;
		pp2T.m10 = p.y * p2.x;
		pp2T.m11 = p.y * p2.y;
		pp2T.m12 = p.y * p2.z;
		pp2T.m20 = p.z * p2.x;
		pp2T.m21 = p.z * p2.y;
		pp2T.m22 = p.z * p2.z;

		return pp2T;
	}

}
