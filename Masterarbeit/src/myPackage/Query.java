package myPackage;

import java.util.HashSet;

public class Query {
	int mFree;
	Atom[] atoms;
	String queryString;
	HashSet<Integer> free;
	
	public boolean isFree(int var) {
		if (this.free.contains((Integer) var)) {
			return true;
		}
		return false;
	}
	
	public Query(int mFree, Atom[] atoms, String queryString) {
		this.mFree = mFree;
		this.atoms = atoms;
		this.queryString = queryString;
		this.free = new HashSet<Integer>();
		
		this.free.add((Integer) 0);
		this.free.add((Integer) 1);
		this.free.add((Integer) 2);
		this.free.add((Integer) 4);
	}
}
