package transformers;

import datastructure.halfedge.HalfEdgeStructure;

public interface Transformer {
	HalfEdgeStructure call(HalfEdgeStructure hs);
}
