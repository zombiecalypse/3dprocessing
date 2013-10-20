package helpers;


import java.util.ArrayList;
import java.util.List;

import static helpers.StaticHelpers.*;

public class IntBuffer {
	private List<Integer> ints = new ArrayList<>();
	
	public void add(int... is) {
		for (int i: is) {
			ints.add(i);
		}
	}

	public int[] render() {
		int[] ret = new int[ints.size()];
		for (Pair<Integer, Integer> p: zip(count(0), this.ints)) {
			ret[p.a] = p.b;
		}
		return ret;
	}
	
	public int size() {
		return ints.size();
	}

	public void clear() {
		ints.clear();
	}
}
