package meshes.generated;

import glWrapper.GLHalfEdgeStructure;
import glWrapper.GLWireframeMesh;

import javax.vecmath.Point3f;

import datastructure.halfedge.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import openGL.MyDisplay;
import assignment4.Assignment4_3_minimalSurfaces;

public class Bock {
	
	public WireframeMesh result;
	private float width;
	//public HEData1d boundary;
	private float height;
	private float length;

	
	public Bock(float width, float length, float height){
		this.length = length;
		this.height = height;
		this.width = width;
		this.setUp();
	}
	
	
	public void setUp(){
		this.result = new WireframeMesh();
		
		int num1 = 40;
		
		
		for(int i = 0; i < num1 ; i++){
			for(int j = 0; j < num1 ; j++){
				 this.result.vertices.add(new Point3f(width/num1 * j -width/2, height/num1*i, -length/2));
			}
		}
		for(int i = 0; i < num1 ; i++){
			for(int j = 0; j < num1 ; j++){
				 this.result.vertices.add(new Point3f(width/num1 *j -width/2, height, -length/2 + length/num1 * i));
			}
		}
		
		for(int i = 0; i < num1 +1 ; i++){
			for(int j = 0; j < num1 ; j++){
				 this.result.vertices.add(new Point3f(width/num1*j -width/2, height - height/num1 * i, length/2));
			}
		}
		
		
		for(int i = 0; i < 3*num1; i++){
			for(int j = 0 ; j < num1-1; j++){
				int[] fc1 = {i*num1 +j + 1,i*num1 + j, (i+1)*num1 +j + 1};
				int[] fc2 = {(i+1)*num1 +j+1 , i*num1 + j,  (i+1)*num1 +j};
				this.result.faces.add(fc1);
				this.result.faces.add(fc2);
			}
		}
		
	}

}
