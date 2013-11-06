package sparse.solver;

import java.util.ArrayList;

import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import sparse.CSRMatrix;
import sparse.LinearSystem;

public abstract class Solver {
	
	/**
	 * x will be used as an initial guess, the result will be stored in x
	 * @param mat
	 * @param b
	 * @param x
	 */
	public abstract void solve(CSRMatrix mat, ArrayList<Float> b,ArrayList<Float> x);
	
	
	public void solve(LinearSystem l, ArrayList<Float> x){
		if(l.mat.nCols == l.mat.nRows){
			solve(l.mat, l.b, x);
		}
		else{
			throw new UnsupportedOperationException("can solve only square mats");
		}
	}
	
	public <T extends Tuple3f> void solveTuple(CSRMatrix m, ArrayList<T> b, ArrayList<T> x) {
		ArrayList<Float> bX = new ArrayList<Float>(b.size());
		ArrayList<Float> xX = new ArrayList<Float>(x.size());
		ArrayList<Float> bY = new ArrayList<Float>(b.size());
		ArrayList<Float> xY = new ArrayList<Float>(x.size());
		ArrayList<Float> bZ = new ArrayList<Float>(b.size());
		ArrayList<Float> xZ = new ArrayList<Float>(x.size());
		for (Tuple3f t: b) {
			bX.add(t.x);
			bY.add(t.y);
			bZ.add(t.z);
		}
		solve(m, bX, xX);
		solve(m, bY, xY);
		solve(m, bZ, xZ);
		for (int i = 0; i < b.size(); i++){
			x.add((T) new Vector3f(xX.get(i),xY.get(i),xZ.get(i)));
		}
	}

}
