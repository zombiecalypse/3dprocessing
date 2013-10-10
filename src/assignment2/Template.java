package assignment2;

import glWrapper.GLHashtree;
import glWrapper.GLHashtree_Vertices;
import glWrapper.GLPointCloud;
import glWrapper.GlOctreeNeighbors;
import glWrapper.GlOctreeParenthood;
import glWrapper.GlOctreeVertexToCell;

import java.util.Arrays;

import meshes.PointCloud;
import openGL.gl.GLDisplayable;
import datastructure.HashOctree;

public class Template {

	protected static Iterable<GLDisplayable> hashTreeDemo(String name, PointCloud pointCloud) {
		GLPointCloud glPointCloud = new GLPointCloud(pointCloud);
		glPointCloud.setName(name + " PC");
		glPointCloud.configurePreferredShader("shaders/default.vert",
				"shaders/default.frag", null);
		
		HashOctree oct = new HashOctree(pointCloud, 4, 1, 1f);
		GLHashtree glOct = new GLHashtree(oct);
		glOct.setName(name + " Oct");
		
		GlOctreeParenthood parenthood = new GlOctreeParenthood(oct);
		parenthood.setName(name + " parenthood");
		GlOctreeNeighbors neighbors = new GlOctreeNeighbors(oct);
		neighbors.setName(name + " neighbors");
		
		GlOctreeVertexToCell vertex_to_cell = GlOctreeVertexToCell.make(oct);
		vertex_to_cell.setName(name + " Vertices");
		
		glOct.configurePreferredShader("shaders/octree.vert", "shaders/octree.frag", "shaders/octree.geom");
		
		GLHashtree_Vertices glVerts = new GLHashtree_Vertices(oct);
		glVerts.setName(name + " Verts");

		return Arrays.asList(glPointCloud, glOct, glVerts, parenthood, neighbors, vertex_to_cell);
	}
}
