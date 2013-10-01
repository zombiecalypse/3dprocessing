package helpers;

import static helpers.StaticHelpers.*;

import java.util.List;

import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import meshes.Face;
import meshes.Vertex;

public final class Functions {
	private static final Vector3f grey = new Vector3f(0.5f, 0.5f, 0.5f);
	
	public static Function<Tuple3f, Tuple3f> add(final Tuple3f x) {
		return new Function<Tuple3f, Tuple3f>() {
			@Override
			public Tuple3f call(Tuple3f a) {
				Vector3f n = new Vector3f(x);
				n.add(a);
				return n;
			}
		};
	}
	
	public static Function<Vertex, Tuple3f> normals() {
		return new Function<Vertex, Tuple3f>() {

			@Override
			public Tuple3f call(Vertex v) {
				List<Face> faces = list(v.iteratorVF());
				Tuple3f normal = new Vector3f();
				for (Face f : faces) {
					Vector3f normal_f = f.normal();
					normal_f.scale(f.angle_in(v));
					normal.add(normal_f);
				}
				normal.scale(1/norm(normal));
				return normal;
			}
		};
	}
	
	public static Function<Vertex, Float> valence() {
		return new Function<Vertex, Float>() {
			@Override
			public Float call(Vertex a) {
				return (float) len(a.iteratorVE());
			}
		};
	}
	
	public static Function<Float, Float> spread(final float min, final float max) {
		return new Function<Float, Float>() {
			@Override
			public Float call(Float a) {
				return (a-min)/(max-min);
			}};
	}
	
	public static Function<Vertex, Tuple3f> asColor(final Function<Vertex, Float> f) {
		return new Function<Vertex, Tuple3f>() {
			@Override
			public Tuple3f call(Vertex a) {
				float v = f.call(a);
				return new Vector3f(v, v, v);
			}
		};
	}
	
	public static Function<Vertex, Tuple3f> centered_normals() {
		return comp(add(grey), normals());
	}
}
