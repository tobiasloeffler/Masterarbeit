package myPackage;

import java.util.SortedSet;

public class Query {
	int mFree;
	Atom[] atoms;
	String queryString;
	SortedSet<String> free;
	
	public Query(int mFree, Atom[] atoms, String queryString, SortedSet<String> free) {
		this.mFree = mFree;
		this.atoms = atoms;
		this.queryString = queryString;
		this.free = free;
	}
	
	public boolean isFree(String var) {
		if (this.free.contains(var)) {
			return true;
		}
		return false;
	}
}
