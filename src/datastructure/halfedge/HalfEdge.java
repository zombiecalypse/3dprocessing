package datastructure.halfedge;


import javax.vecmath.Vector3f;

/**
 * Implementation of a half-edge for the {@link HalfEdgeStructure}
 * @author Alf
 *
 */
public class HalfEdge extends HEElement{
//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		result = prime * result + opposite.incident_v.index;
//		result = prime * result + incident_v.index;
//		return result;
//	}
//
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (obj == null)
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//		HalfEdge other = (HalfEdge) obj;
//		
//		if (incident_v.index != other.incident_v.index) {
//			return false;
//		} 
//		if (opposite.incident_v.index != other.opposite.incident_v.index) {
//			return false;
//		} else {
//			return true;
//		}
//	}

	private static int ids = 0;
	public final int id = ids++;
	
	/** The end vertex of this edge*/
	public Vertex incident_v;
	
	/**The face this half edge belongs to, which is the face
	* this edge is positively oriented for. This can be null if
	* the half edge lies on a boundary*/
	Face incident_f;
	
	/**the opposite, next and previous edge*/
	HalfEdge opposite, next;

	public HalfEdge prev;

	int index;
	
	/**
	 * Initialize a half-edge with the Face it belongs to (the face it is
	 * positively oriented for) and the vertex it points to 
	 */
	public HalfEdge(Face f, Vertex v) {
		incident_v = v;
		incident_f = f;
		opposite = null;
	}
	
	/**
	 * Initialize a half-edge with the Face it belongs to (the face it is
	 * positively oriented for), the vertex it points to and its opposite
	 * half-edge.
	 * @param f
	 * @param v
	 * @param oppos
	 */
	public HalfEdge(Face f, Vertex v, HalfEdge oppos) {
		incident_v = v;
		incident_f = f;
		opposite = oppos;
	}

	
	/**
	 * If this is the edge (a->b) this returns the edge (b->a).
	 * @return
	 */
	public HalfEdge getOpposite() {
		return opposite;
	}
	
	public void setOpposite(HalfEdge opp) {
		this.opposite = opp;
	}

	/**
	 * will return the next edge on the face this half-edge belongs to. 
	 * (If this is the edge (a->b) on the triangle (a,b,c) this will be (b->c).
	 * @return
	 */
	public HalfEdge getNext() {
		return next;
	}
	
	public void setNext(HalfEdge he) {
		this.next = he;
	}
	
	/**
	 * Returns the face this half-edge belongs to. If this is the edge (b->a) lying
	 * on the faces (a,b,c) and (b,a,d) this will be the face (b,a,d). If the
	 * half-edge lies on a boundary this can be null.
	 * @return
	 */
	public Face getFace() {
		return incident_f;
	}

	/**
	 * will return the previous edge on the face this half-edge belongs to. 
	 * (If this is the edge (a->b) on the triangle (a,b,c) this will be (c->a).
	 * @return
	 */
	public HalfEdge getPrev() {
		return prev;
	}
	
	public void setPrev(HalfEdge he) {
		this.prev = he;
	}
	
	
	public void setEnd(Vertex v){
		this.incident_v = v;
	}

	
	public Vertex start(){
		return opposite.incident_v;
	}
	
	public Vertex end(){
		return incident_v;
	}
	
	
	public boolean hasFace(){
		return this.incident_f != null;
	}
	
	/**
	 * Returns true if this edge and its opposite have a face only on one side.
	 */
	public boolean isOnBorder(){
		return this.incident_f == null || opposite.incident_f == null;
	}
	
	@Override
	public String toString(){
		return String.format("[%x](%s --> %s)", id, start(), end());
	}

	public Vector3f asVector() {
		Vector3f vec = new Vector3f();
		vec.add(this.incident_v.pos);
		vec.sub(this.opposite.incident_v.pos);
		return vec;
	}

	public float opposingAngle() {
		if (this.incident_f == null) throw new AssertionError("No face to opposing angle");
		return next.asVector().angle(next.next.asVector());
	}
	
	public float incidentAngle() {
		return opposite.asVector().angle(next.asVector());
	}

	public boolean splitsFaces() {
		return hasFace() && opposite.hasFace();
	}

	public void setStart(Vertex v) {
		opposite.incident_v = v;
	}
}
