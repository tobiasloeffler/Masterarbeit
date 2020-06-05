package myPackage;

public class Item extends DataStructure{
	final int var;
	final double cons;
	final String valuation;
	int[] weightAtoms;
	int weight;
	final double[][] baseMatrix;
	double[][] matrix;
	Item parent;
	Item prev;
	Item next;
	MyList[] children;
	
	int weightFree;
	double[][] matrixFree;
	
	public Item(int var, double cons, String valuation, Item parent) {
		this.var = var;
		this.cons = cons;
		this.valuation = valuation;
		this.weightAtoms = new int[query.atoms.length];
		this.baseMatrix = new double[query.mFree][query.mFree];
		this.matrix = new double[query.mFree][query.mFree];
		this.parent = parent;
		this.prev = null;
		this.next = null;
		this.children = null;
		
		this.weightFree = 0;
		this.matrixFree = new double[query.mFree-1][query.mFree-1];
		
		for (int i=0; i<query.atoms.length; i++) {
			this.weightAtoms[i] = 0;
		}
		
		QTreeNode[] children = qTree.getChildren(qTree.root, var);
		
		if (children != null) {
			this.children = new MyList[qTree.getChildrenCount(qTree.root, var)];
			for (int i=0; i<children.length; i++) {
				this.children[i] = new MyList(children[i].var);
			}
		}
		
		for (int i=0; i<query.mFree; i++) {
			for (int j=0; j<query.mFree; j++) {
				this.matrix[i][j] = 0.0;
				if ((i == this.var) || (j == this.var)) {
					if ((i == this.var) && (j == this.var)) {
						this.baseMatrix[i][j] = cons * cons;
					} else {
						this.baseMatrix[i][j] = cons;
					}
				} else {
					this.baseMatrix[i][j] = 1.0;
				}
			}
		}
		
		for (int i=0; i<(query.mFree-1); i++) {
			for (int j=0; j<(query.mFree-1); j++) {
				this.matrixFree[i][j] = 0.0;
			}
		}
	}
}
