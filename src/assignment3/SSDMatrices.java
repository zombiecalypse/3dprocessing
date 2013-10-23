package assignment3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import meshes.PointCloud;
import sparse.CSRMatrix;
import sparse.CSRMatrix.col_val;
import sparse.LinearSystem;
import datastructure.octree.HashOctree;
import datastructure.octree.HashOctreeCell;
import datastructure.octree.HashOctreeVertex;
import helpers.MortonCodes;
import helpers.StaticHelpers.Indexed;
import helpers.V;
import static helpers.StaticHelpers.*;

public class SSDMatrices {

	/**
	 * Example Matrix creation: Create an identity matrix, clamped to the
	 * provided format.
	 */
	public static CSRMatrix eye(int nRows, int nCols) {
		CSRMatrix eye = new CSRMatrix(0, nCols);

		// initialize the identity matrix part
		for (int i = 0; i < Math.min(nRows, nCols); i++) {
			eye.addRow();
			eye.lastRow().add(
			// column i, vlue 1
					new col_val(i, 1));
		}
		// fill up the matrix with empt rows.
		for (int i = Math.min(nRows, nCols); i < nRows; i++) {
			eye.addRow();
		}

		return eye;
	}

	/**
	 * Example matrix creation: Identity matrix restricted to boundary per
	 * vertex values.
	 */
	public static CSRMatrix Eye_octree_boundary(HashOctree tree) {

		CSRMatrix result = new CSRMatrix(0, tree.numberOfVertices());

		for (HashOctreeVertex v : tree.getVertices()) {
			if (MortonCodes.isVertexOnBoundary(v.code, tree.getDepth())) {
				result.addRow();
				result.lastRow().add(new col_val(v.index, 1));
			}
		}

		return result;
	}

	/**
	 * One line per point, One column per vertex, enforcing that the
	 * interpolation of the Octree vertex values is zero at the point position.
	 * 
	 */
	public static CSRMatrix D0Term(HashOctree tree, PointCloud cloud) {
		CSRMatrix m = new CSRMatrix(cloud.points.size(),
				tree.numberOfVertices());
		// foreach point
		for (Indexed<Point3f> ip : withIndex(cloud.points)) {
			int row = ip.index();
			HashOctreeCell ps_cell = tree.getCell(ip.value());
			// The center designates (0.5,0.5,0.5) in trilinear interpolation
			Point3f pt = V.sub(ip.value(), ps_cell.center);
			pt.scale(1.f/ps_cell.side);
			// find the vertices surrounding it
			for (int i = 0; i < 8; i++) {
				// and put to their coordinates in the matrix the value that would make the trilinear interpolation 0.
				int col = ps_cell.getCornerElement(i, tree).getIndex();
				float v = 1;
				for (int x = 0; x < 3 /*u*/; x++) {
					v *= 0.5 * bitAsSign(i, x) * coord(pt, x);
				}
				m.put(row, col,  v);
			}
		}

		return m;
	}
	
	private static int bitAsSign(int x, int i) {
		return ithBit(i, x) == 1 ? 1 : -1;
	}

	/**
	 * matrix with three rows per point and 1 column per octree vertex. rows
	 * with i%3 = 0 cover x gradients, =1 y-gradients, =2 z gradients; The row
	 * i, i+1, i+2 correxponds to the point/normal i/3. Three consecutant rows
	 * belong to the same gradient, the gradient in the cell of
	 * pointcloud.point[row/3];
	 */
	public static CSRMatrix D1Term(HashOctree tree, PointCloud cloud) {

		CSRMatrix m = new CSRMatrix(3 * cloud.points.size(),
				tree.numberOfVertices());
		// foreach point
		for (Indexed<Point3f> ip : withIndex(cloud.points)) {
			int row = ip.index()*3;
			HashOctreeCell c = tree.getCell(ip.value());
			float weight = 1f/(4*c.side);

			// find the vertices surrounding it
			for (int i = 0; i < 8; i++) {
				for (int coord = 0; coord < 3; coord++) {
					// and set their coordinates in the matrix the value that would make the difference of normals 0.
					m.put(row+coord, c.getCornerElement(i, tree).getIndex(), -bitAsSign(i, coord)*weight);
				}
			}
		}

		return m;
	}

	/** Smoothness energy: norm of Hessian. Should get close to 0, has n_vertices². */
	public static CSRMatrix RTerm(HashOctree tree) {

		CSRMatrix m = new CSRMatrix(tree.numberOfVertices(), tree.numberOfVertices());

		float total_weight = 0;
		// foreach vertex
		for ( Indexed<HashOctreeVertex> iv : withIndex(tree.getVertices())) {
			HashOctreeVertex v = iv.value();
			int row = iv.index();
			// foreach neighbor pair
			for (Pair<HashOctreeVertex, HashOctreeVertex> p : vertexNeighborPairs(v, tree)) {
				float dist_north = v.position.distance(p.b.position);
				float dist_south = v.position.distance(p.a.position);
				float dist_traverse = dist_north+dist_south;
				m.put(row, row, 1);
				m.put(row, p.b.getIndex(), -dist_north/dist_traverse);
				m.put(row, p.a.getIndex(), -dist_south/dist_traverse);
				total_weight += dist_north* dist_south;
			}
		}
		m.scale(1f/total_weight);
		return m;
	}

	private static Iterable<Pair<HashOctreeVertex, HashOctreeVertex>> vertexNeighborPairs(final HashOctreeVertex v, HashOctree tree) {
		List<Pair<HashOctreeVertex, HashOctreeVertex>> neighbors = new ArrayList<>();
		for (int dir = 0b100; dir > 0; dir >>= 1) {
			HashOctreeVertex n1 = tree.getNbr_v2v(v, dir);
			HashOctreeVertex n2 = tree.getNbr_v2vMinus(v, dir);
			if (n1 == null || n2 == null) continue;
			neighbors.add(pair(n1, n2));
		}
		return neighbors;
	}

	/**
	 * Set up the linear system for ssd: append the three matrices,
	 * appropriately scaled. And set up the appropriate right hand side, i.e.
	 * the b in Ax = b
	 * 
	 * @param tree
	 * @param pc
	 * @param lambda0
	 * @param lambda1
	 * @param lambda2
	 * @return
	 */
	public static LinearSystem ssdSystem(HashOctree tree, PointCloud pc,
			float lambda0, float lambda1, float lambda2) {

		int N_vertices = tree.numberOfVertices();
		LinearSystem system = (new LinearSystem.Builder())
				.mat(D0Term(tree, pc)).b(0f).weight(Math.sqrt(lambda0/N_vertices))
				.mat(RTerm(tree)).b(0f).weight(Math.sqrt(lambda2/N_vertices))
				.mat(D1Term(tree, pc)).weight(Math.sqrt(lambda1/N_vertices)).b(
						flatten(pc.normals)).render();
		return system;
	}

}
