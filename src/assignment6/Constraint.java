package assignment6;

import datastructure.halfedge.Vertex;

/**
 * Interface for constrained boundary selection, used in {@link Assignment6_examples} 
 *
 */
public interface Constraint {
 public boolean isEligible(Vertex v);
}
