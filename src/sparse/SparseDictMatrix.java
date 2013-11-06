package sparse;

import helpers.StaticHelpers;
import helpers.StaticHelpers.Pair;
import static helpers.StaticHelpers.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import sparse.CSRMatrix.col_val;

public class SparseDictMatrix {
	private Map<Pair<Integer, Integer>, Float> values = new HashMap<>();
	int rows = 0, cols = 0;
	
	public SparseDictMatrix putOnce(int row, int col, float val) {
		assert !values.containsKey(pair(row, col));
		values.put(pair(row, col), val);
		rows = Math.max(rows, row);
		cols = Math.max(cols, col);
		return this;
	}
	
	public Float get(int row, int col) {
		return values.get(pair(row, col));
	}
	
	public SparseDictMatrix add(int row, int col, float val) {
		final Pair<Integer, Integer> key = pair(row, col);
		if (!values.containsKey(key)) {
			putOnce(row, col, val);
		} else {
			values.put(key, values.get(key) + val);
		}
		return this;
	}
	
	public CSRMatrix toCsr() {
		CSRMatrix m = new CSRMatrix(rows, cols);
		for (Entry<Pair<Integer, Integer>, Float> e : values.entrySet()) {
			m.put(e.getKey().a, e.getKey().b, e.getValue());
		}
		for (ArrayList<col_val> r : m.rows) {
			Collections.sort(r);
		}
		return m;
	}
}
