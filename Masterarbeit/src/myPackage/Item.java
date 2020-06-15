package myPackage;

import static myPackage.DataStructure.query;
import static myPackage.DataStructure.qTree;
import static myPackage.DataStructure.startList;

public class Item {
	final int var;
	final double cons;
	final String valuation;
	int[] weightAtoms;
	int weight;
	final Matrix baseMatrix;
	Matrix matrix;
	Item parent;
	Item prev;
	Item next;
	MyList[] children;
	
	int weightFree;
	Matrix matrixFree;
	
	public Item(int var, double cons, String valuation, Item parent) {
		this.var = var;
		this.cons = cons;
		this.valuation = valuation;
		this.weightAtoms = new int[query.atoms.length];
		this.baseMatrix = new Matrix(query.m);
		this.matrix = new Matrix(query.m);
		this.parent = parent;
		this.prev = null;
		this.next = null;
		this.children = null;
		
		this.weightFree = 0;
		this.matrixFree = new Matrix(query.mFree);							//FIX
		
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
		
		for (int i=0; i<query.m; i++) {
			for (int j=0; j<query.m; j++) {
				if ((i == this.var) || (j == this.var)) {
					if ((i == this.var) && (j == this.var)) {
						this.baseMatrix.data[i][j] = cons * cons;
					} else {
						this.baseMatrix.data[i][j] = cons;
					}
				} else {
					this.baseMatrix.data[i][j] = 1.0;
				}
			}
		}
		
		this.matrix.set(0.0);
		this.matrixFree.set(0.0);
	}
	
	public void updateWeightAtoms(boolean mode, int atomIndex) {
		
		if (mode) {
			this.weightAtoms[atomIndex] += 1;
		} else {
			this.weightAtoms[atomIndex] -= 1;
		}
	}
	
	public void updateWeightAndMatrix() {
		
		int var = this.var;
		int weightRep = 1;
		
		for (int k=0; k<query.atoms.length; k++) {
			if (qTree.inRep(query.atoms[k], var)) {
				weightRep *= this.weightAtoms[k];
			}
		}
		
		this.weight = weightRep;
		this.matrix = Matrix.copy(this.baseMatrix);
		
		if (this.children != null) {
			for (int k=0; k<this.children.length; k++) {
				this.weight *= this.children[k].weight;
				this.matrix.scalarMult(weightRep);
				this.matrix.hadamard(this.children[k].matrix);
			}
		}
	}
	
	public void updateWeightFreeAndMatrixFree() {
		
		if (query.isFree(this.var)) {
			
			if (this.weight == 0) {
				
				this.weightFree = 0;
				this.matrixFree.set(0.0);
				
			} else {
				
				this.weightFree = 1;
				
				for (int k=0; k<(query.mFree); k++) {
					for (int l=0; l<(query.mFree); l++) {
						this.matrixFree.data[k][l] = this.baseMatrix.data[k][l];
					}
				}
				
				if (this.children != null) {
					
					for (int k=0; k<this.children.length; k++) {
						
						if (query.isFree(this.children[k].var)) {
							this.weightFree *= this.children[k].weightFree;
							this.matrixFree.hadamard(this.children[k].matrixFree);
						}
					}
				}
			}
		}
	}
	
	public MyList updateList(int weight_old) {
		
		MyList list = null;
		
		if (this.parent != null) {
			for (int k=0; k<this.parent.children.length; k++) {
				if (this.parent.children[k].var == this.var) {
					list = this.parent.children[k];
				}
			}
		} else {
			list = startList;
		}
				
		if ((this.weight > 0) && (weight_old == 0)) {
			if (list.first == null) {
				list.first = this;
				list.last = list.first;
			} else {
				this.prev = list.last;
				list.last.next = this;
				list.last = this;
			}
		} else if ((this.weight == 0) && (weight_old > 0)) {
			if (list.first == list.last) {
				list.first = null;
				list.last = null;
			} else {
				if (list.first == this) {
					list.first = this.next;
				} else if (list.last == this) {
					list.last = this.prev;
				}
				if (this.prev != null) {
					this.prev.next = this.next;
				}
				if (this.next != null) {
					this.next.prev = this.prev;
				}
				this.prev = null;
				this.next = null;
			}
		}
		
		return list;
	}
}
