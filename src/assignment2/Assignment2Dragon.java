package assignment2;

import java.io.IOException;
import openGL.MyDisplay;
import openGL.gl.GLDisplayable;
import meshes.PointCloud;
import meshes.reader.ObjReader;

public class Assignment2Dragon extends Template {

	public static void main(String[] args) throws IOException {
		MyDisplay display = new MyDisplay();

		PointCloud pointCloud = ObjReader.readAsPointCloud("./objs/dragon.obj",
				true);


		// these demos will run once all methods in the MortonCodes class are
		// implemented.

		for (GLDisplayable d: hashTreeDemo("Dragon", pointCloud)) {
			display.addToDisplay(d);
		}
		
	}
}
