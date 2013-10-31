package sparse;

import helpers.StaticHelpers.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class SparseDictMatrix {
	private Map<Pair<Integer, Integer>, Float> values = new HashMap<>();
	int rows = 0, cols = 0;
	
	public SparseDictMatrix put(int row, int col, float val) {
		values.put(new Pair<Integer, Integer>(row, col), val);
		rows = Math.max(rows, row);
		cols = Math.max(cols, col);
		return this;
	}
	
	public CSRMatrix toCsr() {
		CSRMatrix m = new CSRMatrix(rows, cols);
		for (Entry<Pair<Integer, Integer>, Float> e : values.entrySet()) {
			m.put(e.getKey().a, e.getKey().b, e.getValue());
		}
		return m;
	}
}
