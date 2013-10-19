package helpers;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

/**
 * Class that makes vector arithmetic functional.
 * @author aaron
 *
 */
public final class V {
	public static Point3f add(Point3f v1, Vector3f v2) {
		Point3f p = new Point3f(v1);
		p.add(v2);
		return p;
	}
	
	public static Point3f scaled_add(Point3f v1, float w1, Point3f v2, float w2) {
		Point3f p = new Point3f();
		p.scaleAdd(w1, v1);
		p.scaleAdd(w2, v2);
		return p;
	}
}
