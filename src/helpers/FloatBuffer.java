package helpers;


import java.util.ArrayList;
import java.util.List;

import static helpers.StaticHelpers.*;

public class FloatBuffer {
	private List<Float> floats = new ArrayList<>();
	
	public void add(float... is) {
		for (float i: is) {
			floats.add(i);
		}
	}

	public float[] render() {
		float[] ret = new float[floats.size()];
		for (Pair<Integer, Float> p: zip(count(0), this.floats)) {
			ret[p.a] = p.b;
		}
		return ret;
	}
	
	public int size() {
		return floats.size();
	}
}
