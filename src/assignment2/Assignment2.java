package assignment2;

import glWrapper.GLHashtree;
import glWrapper.GLHashtree_Vertices;
import glWrapper.GLPointCloud;
import glWrapper.GlOctreeParenthood;
import glWrapper.GlOctreeVertexToCell;

import java.io.IOException;
import java.util.Arrays;

import datastructure.octree.HashOctree;
import openGL.MyDisplay;
import openGL.gl.GLDisplayable;
import meshes.PointCloud;
import meshes.reader.ObjReader;
import meshes.reader.PlyReader;

public class Assignment2 extends Template {

	public static void main(String[] args) throws IOException {
		MyDisplay display = new MyDisplay();

		PointCloud pointCloud2 = PlyReader.readPointCloud(
				"./objs/octreeTest2.ply", true);


		for (GLDisplayable d: hashTreeDemo("Simple", pointCloud2)) {
			display.addToDisplay(d);
		}
	}
}
