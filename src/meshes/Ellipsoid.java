package meshes;

import java.util.ArrayList;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

public class Ellipsoid {
	/**
	 * Constructs an ellipsoid with the given axes and the given radia.
	 * 
	 * @param center
	 * @param v0
	 * @param l0
	 * @param v1
	 * @param l1
	 * @param v2
	 * @param l2
	 * @return
	 */
	public static WireframeMesh make(Point3f center, Vector3f v0,
			float l0, Vector3f v1, float l1, Vector3f v2, float l2) {

		int numPhi = 10;
		ArrayList<Point3f> sphr = new ArrayList<>();
		float Pi = (float) Math.PI;
		int numPsi = 10;
		for (float psi = -Pi / 2; psi < Pi / 2 + Pi / (2 * numPsi); psi += Pi
				/ numPsi) {
			for (float phi = 0; phi < 2 * Pi - Pi / (2 * numPhi); phi += Pi
					/ numPhi) {

				Point3f p = new Point3f(
						(float) (Math.cos(phi) * Math.cos(psi)),
						(float) (Math.sin(phi) * Math.cos(psi)),
						(float) (Math.sin(psi)));

				sphr.add(new Point3f(center.x + l0 * p.x * v0.x + l1 * p.y
						* v1.x + l2 * p.z * v2.x, center.y + l0 * p.x * v0.y
						+ l1 * p.y * v1.y + l2 * p.z * v2.y, center.z + l0
						* p.x * v0.z + l1 * p.y * v1.z + l2 * p.z * v2.z));

			}
		}

		WireframeMesh wf = new WireframeMesh();
		wf.vertices = sphr;
		int nphi = 2 * numPhi;
		int npsi = numPsi + 1;
		for (int i = 0; i < npsi; i++) {
			for (int j = 0; j < nphi; j++) {
				int[] fc = { i * nphi + j, i * nphi + (j + 1) % (nphi),
						(i + 1) * nphi + j };
				int[] fc2 = { (i + 1) * nphi + j, i * nphi + (j + 1) % (nphi),
						(i + 1) * nphi + (j + 1) % (nphi) };
				wf.faces.add(fc);
				wf.faces.add(fc2);
			}
		}

		return wf;
	}
}
