package sparse;

import helpers.Function;
import helpers.StaticHelpers.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sparse.LinearSystem.Builder;
import static helpers.StaticHelpers.*;

public class LinearSystem{
	private static Function<Pair<Float, CSRMatrix>, Integer> ncols = new Function<Pair<Float, CSRMatrix>, Integer>() {
		@Override
		public Integer call(Pair<Float, CSRMatrix> a) {
			return a.b.nCols;
		}
	};
	
	public CSRMatrix mat;
	public ArrayList<Float> b;
	
	static public class Builder {
		List<Pair<Float, CSRMatrix>> mats = new ArrayList<>();
		List<Float> bs = new ArrayList<>();
		private CSRMatrix buffer;
		
		public LinearSystem render() {
			CSRMatrix m = new CSRMatrix(0, maximize(ncols, mats));
			for (Pair<Float, CSRMatrix> p : mats) {
				m.append(p.b, p.a);
			}
			LinearSystem s = new LinearSystem();
			s.mat = m;
			s.b = new ArrayList<>(bs);
			return s;
		}
		
		public Builder mat(CSRMatrix mat) {
			buffer = mat;
			return this;
		}
		
		public Builder weight(float f) {
			mats.add(pair(f, buffer));
			return this;
		}
		
		public Builder b(Iterable<Float> b) {
			bs.addAll(list(b));
			return this;
		}

		public Builder b(float f) {
			return b(Collections.nCopies(buffer.nRows, f));
		}

		public Builder weight(double x) {
			return weight((float) x);
		}
	}
}