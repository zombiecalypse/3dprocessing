package assignment2;

import glWrapper.GLPointCloud;

import java.io.IOException;

import openGL.MyDisplay;
import meshes.PointCloud;
import meshes.reader.ObjReader;
import meshes.reader.PlyReader;

public class Assignment2 {

	public static void main(String[] args) throws IOException{
		MyDisplay display = new MyDisplay();
		
		PointCloud pointCloud = ObjReader.readAsPointCloud("./objs/dragon.obj", true);
		
		GLPointCloud glPointCloud = new GLPointCloud(pointCloud);
		glPointCloud.setName("Dragon PC");
		glPointCloud.configurePreferredShader("shaders/default.vert", "shaders/default.frag", null);
		
		//these demos will run once all methods in the MortonCodes class are
		//implemented.
//		hashTreeDemo(pointCloud);
		
		PointCloud pointCloud2 = PlyReader.readPointCloud("./objs/octreeTest2.ply", true);
		GLPointCloud glPointCloud2 = new GLPointCloud(pointCloud2);
		glPointCloud2.configurePreferredShader("shaders/default.vert", "shaders/default.frag", null);
//		hashTreeDemo(PlyReader.readPointCloud("./objs/octreeTest2.ply", true));
		
		display.addToDisplay(glPointCloud);
		display.addToDisplay(glPointCloud2);
	}
}
