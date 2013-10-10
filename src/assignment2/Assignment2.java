package assignment2;

import glWrapper.GLHashtree;
import glWrapper.GLHashtree_Vertices;
import glWrapper.GLPointCloud;

import java.io.IOException;
import java.util.Arrays;

import datastructure.HashOctree;
import openGL.MyDisplay;
import openGL.gl.GLDisplayable;
import meshes.PointCloud;
import meshes.reader.ObjReader;
import meshes.reader.PlyReader;

public class Assignment2 {

	public static void main(String[] args) throws IOException {
		MyDisplay display = new MyDisplay();

		PointCloud pointCloud = ObjReader.readAsPointCloud("./objs/dragon.obj",
				true);

		PointCloud pointCloud2 = PlyReader.readPointCloud(
				"./objs/octreeTest2.ply", true);

		

		// these demos will run once all methods in the MortonCodes class are
		// implemented.

		for (GLDisplayable d: hashTreeDemo("Dragon", pointCloud)) {
			display.addToDisplay(d);
		}
		
		for (GLDisplayable d: hashTreeDemo("Alt", pointCloud2)) {
			display.addToDisplay(d);
		}
	}

	private static Iterable<GLDisplayable> hashTreeDemo(String name, PointCloud pointCloud) {
		GLPointCloud glPointCloud = new GLPointCloud(pointCloud);
		glPointCloud.setName(name + " PC");
		glPointCloud.configurePreferredShader("shaders/default.vert",
				"shaders/default.frag", null);
		
		HashOctree oct = new HashOctree(pointCloud, 4, 1, 1f);
		GLHashtree glOct = new GLHashtree(oct);
		glOct.setName(name + " Oct");
		
		glOct.configurePreferredShader("shaders/octree.vert", "shaders/octree.frag", "shaders/octree.geom");
		
		GLHashtree_Vertices glVerts = new GLHashtree_Vertices(oct);
		glVerts.setName(name + " Verts");
		
		return Arrays.asList(glPointCloud, glOct, glVerts);
	}
}
