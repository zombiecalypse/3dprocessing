package glWrapper;

import java.util.ArrayList;
import static helpers.StaticHelpers.*;

import javax.vecmath.Point3f;

import openGL.gl.GLDisplayable;

public abstract class GLVertexDisplayer extends GLDisplayable {
	
	public GLVertexDisplayer(int n) {
		super(n);
	}

	private String name;

	public void setName(String string) {
		this.name = string;
	}

	@Override
	public String toString() {
		return name == null ? super.toString() : name;
	}
	
	public static float[] packPoints(Iterable<Point3f> points) {
		ArrayList<Float> f = new ArrayList<>();
		for (Point3f p: points) {
			f.add(p.x);
			f.add(p.y);
			f.add(p.z);
		}
		float[] array = new float[f.size()];
		for (Pair<Integer, Float> p: zip(count(0), f)) {
			array[p.a] = p.b;
		}
		return array;
	}
}
