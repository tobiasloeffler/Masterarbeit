package myPackage;

import java.util.Map;
import java.util.SortedSet;

public class Query {
	int mFree;
	Atom[] atoms;
	String sql;
	int[] freeTuple;
	private SortedSet<Integer> freeVars;
	private Map<Integer, String> varToString;
	
	public Query(int mFree, Atom[] atoms, String sql, int[] freeTuple, SortedSet<Integer> freeVars, Map<Integer, String> varToString) {
		this.mFree = mFree;
		this.atoms = atoms;
		this.sql = sql;
		this.freeTuple = freeTuple;
		this.freeVars = freeVars;
		this.varToString = varToString;
	}
	
	public boolean isFree(Integer var) {
		if (this.freeVars.contains(var)) {
			return true;
		}
		return false;
	}
	
	public String mapVarToString(Integer var) {
		return this.varToString.get(var);
	}
}
