package assignment5;

import static helpers.StaticHelpers.withIndex;
import glWrapper.GLHalfEdgeStructure;
import glWrapper.GLWireframeMesh;
import helpers.StaticHelpers.Indexed;
import helpers.V;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import meshes.Ellipsoid;
import meshes.WireframeMesh;
import meshes.reader.ObjReader;
import openGL.MyDisplay;
import algorithms.collapse.QSlim;
import datastructure.halfedge.HalfEdgeStructure;
import datastructure.halfedge.Vertex;

/**
 * Some convenience methods for the visualization of epsilon-isosurfaces of
 * quadratic forms.
 *
 * @author Alf
 *
 */
public class Assignment5QslimVis {
	private static Logger log = Logger.getLogger("Main");

	static final float E = 0.01f;

	static float evToEl(float v) {
		return (float) (E/Math.sqrt(Math.abs(v)));
	}

	public static void main(String[] args) throws Exception {
		WireframeMesh wf = ObjReader.read("objs/bunny_ear.obj", true);
		HalfEdgeStructure hs = new HalfEdgeStructure(wf);

		QSlim qs = new QSlim(hs);

		List<WireframeMesh> ellipsoids = new ArrayList<>();

		for (Vertex v : hs.getVertices()) {
      Matrix3f q = new Matrix3f();
			qs.q(v).getRotationScale(q);
			float[] evs = V.eigenValues(q);
			Vector3f[] evecs = new Vector3f[3];
			for (int i = 0; i < 3; i++) {
				evecs[i] = V.eigenVector(q, evs[i]);
			}
			ellipsoids.add(Ellipsoid.make(v.pos, evecs[0], evToEl(evs[0]), evecs[1], evToEl(evs[1]), evecs[2], evToEl(evs[2])));
		}

		MyDisplay disp = new MyDisplay();

		GLHalfEdgeStructure glear = new GLHalfEdgeStructure(hs);
		glear.configurePreferredShader("shaders/trimesh_flatColor3f.vert",
				"shaders/trimesh_flatColor3f.frag", "shaders/trimesh_flatColor3f.geom");
		glear.setTitle("Bunny ear");
		disp.addToDisplay(glear);

		for (Indexed<WireframeMesh> e : withIndex(ellipsoids)) {
			GLWireframeMesh gwf = new GLWireframeMesh(e.value());
			gwf.configurePreferredShader("shaders/trimesh_flatColor3f.vert",
				"shaders/trimesh_flatColor3f.frag", "shaders/trimesh_flatColor3f.geom");
			gwf.setTitle(String.format("el%s", e.index()));
			disp.addToDisplay(gwf);
		}
	}
}
