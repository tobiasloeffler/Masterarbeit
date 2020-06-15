package myPackage;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DataStructureTest {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testCopyMatrix() {
		Matrix testMatrix = new Matrix(20);
		
		for (int i=0; i<20; i++ ) {
			for (int j=0; j<20; j++) {
				testMatrix.data[i][j] = Math.random();
			}
		}
		
		Matrix resultMatrix = Matrix.copy(testMatrix);
		
		for (int i=0; i<20; i++ ) {
			for (int j=0; j<20; j++) {
				assertEquals(testMatrix.data[i][j], resultMatrix.data[i][j], "All matrix values should be correct");
			}
		}
	}

}
