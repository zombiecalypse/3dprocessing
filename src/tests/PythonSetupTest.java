package tests;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.*;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;

import junit.framework.TestCase;
import sparse.SCIPY;

public class PythonSetupTest extends TestCase {

	@Test
	public void testExternalCall() throws IOException{
		
		ArrayList<Float> x = new ArrayList<>();
		SCIPY.runLeastSquaresPy("",x);
		assertEquals(x.get(0).doubleValue(), 1, 0.0001);

		assertEquals(x.get(1).doubleValue(), 0.3333, 0.0001);
		
		assertEquals(x.get(2), 0.2, 0.0001);
	}
}
