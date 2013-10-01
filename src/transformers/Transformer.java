package transformers;

import meshes.HalfEdgeStructure;

public interface Transformer {
	HalfEdgeStructure call(HalfEdgeStructure hs);
}
