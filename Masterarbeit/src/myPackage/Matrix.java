package myPackage;

public class Matrix {

	int dimension;
	double[][] data;
	
	public Matrix(int dimension) {
		
		this.dimension = dimension;
		this.data = new double[dimension][dimension];
	}
	
	public void print(String name) {
		
		System.out.println("\n" + name + ":");
		
		for (int i=0; i<this.data[0].length; i++) {
			for (int j=0; j<this.data[0].length; j++) {
				System.out.print(this.data[i][j] + "  ");
			}
			System.out.println("");
		}
	}
	
	public static Matrix copy(Matrix src) {
		
		Matrix dest = new Matrix(src.dimension);
		
		for (int i=0; i<dest.dimension; i++) {
			for (int j=0; j<dest.dimension; j++) {
				dest.data[i][j] = src.data[i][j];
			}
		}
		
		return dest;
	}
	
	public void set(double value) {
		
		for (int i=0; i<this.dimension; i++) {
			for (int j=0; j<this.dimension; j++) {
				this.data[i][j] = value;
			}
		}
	}
	
	public void scalarMult(double scalar) {
		
		for (int i=0; i<this.dimension; i++) {
			for (int j=0; j<this.dimension; j++) {
				this.data[i][j] *= scalar;
			}
		}
	}
	
	public int add(Matrix m) {
		
		if (this.dimension == m.dimension) {
			for (int i=0; i<this.dimension; i++) {
				for (int j=0; j<this.dimension; j++) {
					this.data[i][j] += m.data[i][j];
				}
			}
			
			return 0;
		}
		
		return -1;
	}
	
	public int subtract(Matrix m) {
		
		if (this.dimension == m.dimension) {
			for (int i=0; i<this.dimension; i++) {
				for (int j=0; j<this.dimension; j++) {
					this.data[i][j] -= m.data[i][j];
				}
			}
			
			return 0;
		}
		
		return -1;
	}
	
	public int hadamard(Matrix m) {
		
		if (this.dimension == m.dimension) {
			for (int i=0; i<this.dimension; i++) {
				for (int j=0; j<this.dimension; j++) {
					this.data[i][j] *= m.data[i][j];
				}
			}
			
			return 0;
		}
		
		return -1;
	}
}
