package myPackage;

import static myPackage.DataStructure.query;

public class MyList {
	int var;
	int weight;
	Matrix matrix;
	Item first;
	Item last;
	
	int weightFree;
	Matrix matrixFree;
	
	public MyList(int var) {
		this.var = var;
		this.weight = 0;
		this.matrix = new Matrix(query.m);
		this.first = null;
		this.last = null;
		
		this.weightFree = 0;
		this.matrixFree = new Matrix(query.mFree);
		
		this.matrix.set(0.0);
		this.matrixFree.set(0.0);
	}
	
	public void updateWeightAndMatrix(Item item, int weight_old, Matrix matrix_old) {
		
		this.weight -= weight_old;
		this.weight += item.weight;
		
		this.matrix.subtract(matrix_old);
		this.matrix.add(item.matrix);
	}
	
	public void updateWeightFreeAndMatrixFree(Item item, int weightFree_old, Matrix matrixFree_old) {
		
		if (query.isFree(item.var)) {
			this.weightFree -= weightFree_old;
			this.weightFree += item.weightFree;
			
			this.matrixFree.subtract(matrixFree_old);
			this.matrixFree.add(item.matrixFree);
		}
	}
}
