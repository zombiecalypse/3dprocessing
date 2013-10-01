package transformers;

import static helpers.StaticHelpers.*;

import javax.vecmath.Point3f;

import meshes.HalfEdgeStructure;
import meshes.Vertex;

public class TrivialSmoother implements Transformer {

	@Override
	public HalfEdgeStructure call(HalfEdgeStructure hs0) {
		HalfEdgeStructure hs = new HalfEdgeStructure(hs0);
		for (Pair<Vertex, Vertex> p: zip(hs.getVertices(), hs0.getVertices())) {
			Point3f avg = new Point3f();
			float length = len(p.b.iteratorVV());
			for (Vertex v: iter(p.b.iteratorVV())) {
				avg.add(v.pos);
			}
			avg.scale(1.0f/length);
			p.a.pos = avg;
		}
		return hs;
	}

}
