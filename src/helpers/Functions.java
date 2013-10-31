package helpers;

import static helpers.StaticHelpers.*;

import java.util.List;

import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import datastructure.halfedge.Face;
import datastructure.halfedge.HalfEdge;
import datastructure.halfedge.Vertex;

public final class Functions {
	private static final Vector3f grey = new Vector3f(0.5f, 0.5f, 0.5f);

	public static <A> Function<A, A> id() {
		return new Function<A, A>() {
			@Override
			public A call(A a) {
				return a;
			}
		};
	}

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
					normal_f.scale(f.angleIn(v));
					normal.add(normal_f);
				}
				normal.scale(1 / norm(normal));
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
				return (a - min) / (max - min);
			}
		};
	}

	public static Function<Vertex, Tuple3f> asColor(
			final Function<Vertex, Float> f) {
		return new Function<Vertex, Tuple3f>() {
			@Override
			public Tuple3f call(Vertex a) {
				float v = f.call(a);
				return new Vector3f(v, 1 - v, 1 - v);
			}
		};
	}

	public static Function<Tuple3f, Float> x() {
		return new Function<Tuple3f, Float>() {
			@Override
			public Float call(Tuple3f a) {
				return a.x;
			}
		};
	}

	public static Function<Tuple3f, Float> y() {
		return new Function<Tuple3f, Float>() {
			@Override
			public Float call(Tuple3f a) {
				return a.y;
			}
		};
	}

	public static Function<Tuple3f, Float> z() {
		return new Function<Tuple3f, Float>() {
			@Override
			public Float call(Tuple3f a) {
				return a.z;
			}
		};
	}

	public static Function<Vertex, Tuple3f> centered_normals() {
		return comp(add(grey), normals());
	}

	public static Function<Vertex, Float> laplacian() {
		return new Function<Vertex, Float>() {
			@Override
			public Float call(Vertex a) {

				Vector3f sum = new Vector3f();
				for (HalfEdge e1: iter(a.iteratorVE())) {
					HalfEdge e2 = e1.getOpposite();
					float alpha = e1.opposingAngle();
					float beta = e2.opposingAngle();
					Vector3f sub = e1.asVector();
					sub.scale(cot(alpha) + cot(beta));
					sum.add(sub);
				}
				assert !Float.isNaN(sum.length());
				assert !Float.isInfinite(sum.length());
				float ret = sum.length() / (4 * Math.abs(aMixed(a)));
				assert !Float.isInfinite(ret);
				assert !Float.isNaN(ret);
				assert ret >= 0;
				return ret;
			}

			private float aMixed(Vertex a) {
				float A_mixed = 0f;
				for (Face f : iter(a.iteratorVF())) {
					if (!f.obtuse()) {
						float mini_triags = 0;
						for (HalfEdge e : iter(f.iteratorFE())) {
							if (e.getFace() != f)
								e = e.getOpposite();
							assert e.getFace() == f;
							if (a == e.incident_v)
								continue;
							float n = e.asVector().lengthSquared();
							mini_triags += n / Math.tan(e.opposingAngle());
						}
						A_mixed += mini_triags / 8;
					} else if (f.angleIn(a) > Math.PI / 2) {
						A_mixed += f.area() / 2;
					} else {

						A_mixed += f.area() / 4;
					}
				}
				assert !Float.isNaN(A_mixed);
				return A_mixed;
			}
		};
	}

	public static Function<Float, Float> logNormalize(final float C) {
		return new Function<Float, Float>() {
			@Override
			public Float call(Float a) {
				return (float) Math.log(1 + a / C);
			}
		};
	}
}
