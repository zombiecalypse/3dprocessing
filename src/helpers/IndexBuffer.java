package helpers;

import java.util.ArrayList;
import static helpers.StaticHelpers.*;

public class IndexBuffer {
	private ArrayList<Integer> indices;
	
	public void add(int... is) {
		for (int i: is) {
			indices.add(i);
		}
	}

	public int[] render() {
		int[] ret = new int[indices.size()];
		for (Pair<Integer, Integer> p: zip(count(0), this.indices)) {
			ret[p.a] = p.b;
		}
		return ret;
	}
}
