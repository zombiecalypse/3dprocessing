package meshes;

public class Point2i {
	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}

	public int x;
	public int y;

	public Point2i(int i, int j) {
		x = i;
		y = j;
		
	}
	
	@Override
	public int hashCode(){
		return (x ^ ~y << 16) | (x ^ y) ;
	}
	
	@Override
	public boolean equals(Object o){
		return o instanceof Point2i && 
				((Point2i) o).x == this.x &&
				((Point2i) o).y == this.y;
	}

}
