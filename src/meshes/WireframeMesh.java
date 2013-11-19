package meshes;

import java.util.ArrayList;
import java.util.Arrays;

import javax.vecmath.Point3f;

import static helpers.StaticHelpers.*;

/**
 * A Wireframe Mesh represents a mesh as a list of vertices and a list of faces.
 * Very lightweight representation.
 * @author bertholet
 *
 */
public class WireframeMesh {

	public ArrayList<Point3f> vertices;
	public ArrayList<int[]> faces;
	
	public WireframeMesh(){
		vertices = new ArrayList<>();
		faces = new ArrayList<>();
	}
	
	public WireframeMesh join(WireframeMesh other) {
		WireframeMesh ret = new WireframeMesh();
		ret.vertices.addAll(vertices);
		ret.faces.addAll(faces);
		int offset = ret.vertices.size();
		ret.vertices.addAll(other.vertices);
		for (int[] f : other.faces) {
			int[] fnew = Arrays.copyOf(f, f.length);
			for (int i = 0; i < fnew.length; i++) {
				fnew[i] += offset;
			}
			ret.faces.add(fnew);
		}
		
		return ret;
	}
}
