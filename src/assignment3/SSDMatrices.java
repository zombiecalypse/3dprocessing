package assignment3;

import java.util.Arrays;

import javax.vecmath.Point3f;

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
					v *= (ithBit(x, i) - 0.5) * coord(pt, x);
				}
				m.put(row, col,  v);
			}
		}

		return m;
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
		// find the vertices surrounding it
		// and set their coordinates in the matrix the value that would make the difference of normals 0.
		// TODO

		return m;
	}

	/** Smoothness energy: norm of Hessian. Should get close to 0, has n_vertices * 9 entries. */
	public static CSRMatrix RTerm(HashOctree tree) {

		CSRMatrix m = new CSRMatrix(tree.numberOfVertices(), 9);

		// foreach cell
		// find its neighbor cells
		// find 
		// TODO:
		return m;
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

		// TODO
		LinearSystem system = new LinearSystem();
		system.mat = null;
		system.b = null;
		return system;
	}

}
