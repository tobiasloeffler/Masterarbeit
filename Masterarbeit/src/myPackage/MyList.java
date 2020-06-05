package myPackage;

public class MyList extends DataStructure {
	int var;
	int weight;
	double[][] matrix;
	Item first;
	Item last;
	
	int weightFree;
	double[][] matrixFree;
	
	public MyList(int var) {
		this.var = var;
		this.weight = 0;
		this.matrix = new double[query.mFree][query.mFree];
		this.first = null;
		this.last = null;
		
		this.weightFree = 0;
		this.matrixFree = new double[query.mFree-1][query.mFree-1];
		
		for (int i=0; i<query.mFree; i++) {
			for (int j=0; j<query.mFree; j++) {
				this.matrix[i][j] = 0.0;
			}
		}
		
		for (int i=0; i<(query.mFree-1); i++) {
			for (int j=0; j<(query.mFree-1); j++) {
				this.matrixFree[i][j] = 0.0;
			}
		}
	}
}
