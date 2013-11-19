package transformers;

import static helpers.StaticHelpers.withIndex;
import helpers.Function;
import helpers.Functions;
import helpers.StaticHelpers.Indexed;
import helpers.V;

import java.io.IOException;
import java.util.ArrayList;

import javax.vecmath.Tuple3f;

import sparse.CSRMatrix;
import sparse.LinearSystem;
import sparse.SCIPY;
import assignment4.LMatrices;
import datastructure.halfedge.HalfEdgeStructure;
import datastructure.halfedge.Vertex;

public class ImplicitUniformSmoother implements Transformer {
  private final float lambda;
  public ImplicitUniformSmoother(float lambda) {
    super();
    this.lambda = lambda;
  }

  @Override
  public HalfEdgeStructure call(HalfEdgeStructure hs0) {
    HalfEdgeStructure hs = new HalfEdgeStructure(hs0);
    int nVerts = hs.getVertices().size();
    Function<Vertex, Tuple3f> pos = Functions.position();
    CSRMatrix m = V.add(V.mul(-lambda, LMatrices.uniformLaplacian(hs)), CSRMatrix.eye(nVerts));
    assert m.nCols == m.nRows;
    LinearSystem s = (new LinearSystem.Builder())
        .mat(m).b(Functions.map(Functions.circ(Functions.x(), pos), hs.getVertices()))
        .mat(m).b(Functions.map(Functions.circ(Functions.y(), pos), hs.getVertices()))
        .mat(m).b(Functions.map(Functions.circ(Functions.z(), pos), hs.getVertices()))
            .render();
    ArrayList<Float> out = new ArrayList<>();
    try {
      SCIPY.solve(s, "", out);
    } catch (IOException e) {
      e.printStackTrace();
    }
    assert out.size() == nVerts * 3;
    for (Indexed<Vertex> p: withIndex(hs.getVertices())) {
      p.value().pos.x = out.get(p.index());
      p.value().pos.y = out.get(p.index()+nVerts);
      p.value().pos.z = out.get(p.index()+2*nVerts);
    }
    return hs;
  }

}
