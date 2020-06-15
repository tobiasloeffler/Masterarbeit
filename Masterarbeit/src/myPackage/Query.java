package myPackage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class Query {
	int mFree;
	int m;
	Atom[] atoms;
	String sql;
	int[] freeTuple;
	SortedSet<Integer> freeVars;
	private Map<Integer, String> varToString;
	
	public Query(int mFree, int m, Atom[] atoms, String sql, int[] freeTuple, SortedSet<Integer> freeVars, Map<Integer, String> varToString) {
		this.mFree = mFree;
		this.m = m;
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
	
	public static Query parseQuery(String[] cq) {
		
		Map<Integer, String> varToString = new HashMap<>();
		Map<String, Integer> varToInt = new HashMap<>();
		
		String freeTupleString;
		String[] freeVarsArray;
		int[] freeTuple;
		SortedSet<Integer> freeVars = new TreeSet<>();
		
		if (cq[0].matches("[a-zA-Z]\\w*[(]([a-zA-Z]\\w*((,[a-zA-Z]\\w*)?)*)?[)]")) {
			freeTupleString = cq[0].substring(cq[0].indexOf('(')+1,cq[0].length()-1);
			freeVarsArray = freeTupleString.split(",");
			
			freeTuple = new int[freeVarsArray.length];
			
			for (int i=0; i<freeVarsArray.length; i++) {
				if (!varToInt.containsKey(freeVarsArray[i])) {
					varToInt.put(freeVarsArray[i], varToInt.keySet().size());
					varToString.put(varToInt.get(freeVarsArray[i]), freeVarsArray[i]);
				}
				freeTuple[i] = varToInt.get(freeVarsArray[i]);
				freeVars.add(varToInt.get(freeVarsArray[i]));
			}
		} else {
			System.err.println("Invalid query format");
			return null;
		}
		
		if (!cq[1].equals("=")) {
			System.err.println("Invalid query format");
			return null;
		}
		
		Atom[] atoms = new Atom[cq.length-2];
		Set<String> relationSet = new HashSet<>();
		Map<Integer,Set<String>> varsInRelations = new HashMap<>();
		
		for (int i=2; i<cq.length; i++) {
			if (cq[i].matches("[a-zA-Z]\\w*[(]([a-zA-Z]\\w*((,[a-zA-Z]\\w*)?)*)?[)]")) {
				String relation = cq[i].substring(0,cq[i].indexOf('('));
				relationSet.add(relation);
				
				String tupleString = cq[i].substring(cq[i].indexOf('(')+1,cq[i].length()-1);
				String[] varsArray = tupleString.split(",");
				
				int[] atomTuple = new int[varsArray.length];
				
				for (int j=0; j<varsArray.length; j++) {
					if (!varToInt.containsKey(varsArray[j])) {
						varToInt.put(varsArray[j], varToInt.keySet().size());
						varToString.put(varToInt.get(varsArray[j]), varsArray[j]);
					}
					atomTuple[j] = varToInt.get(varsArray[j]);
				}
				
				atoms[i-2] = new Atom(relation, atomTuple);
				
				for (int j=0; j<atomTuple.length; j++) {
					if (!varsInRelations.containsKey(atomTuple[j])) {
						varsInRelations.put(atomTuple[j], new HashSet<String>());
					}
					varsInRelations.get(atomTuple[j]).add(relation);
				}
			} else {
				System.err.println("Invalid query format");
				return null;
			}
		}
		
		String sql;
		
		if ((sql = convertCqToSql(freeTuple, relationSet, varsInRelations, varToString)) != null) {
			return new Query(freeVars.size(), varToString.keySet().size(), atoms, sql, freeTuple, freeVars, varToString);
		} else {
			return null;
		}
	}
	
	public static String convertCqToSql(int[] freeTuple, Set<String> relations, Map<Integer,Set<String>> varsInRelations, Map<Integer, String> varToString) {
		
		StringBuilder builder = new StringBuilder().append("SELECT ");
		
		for (int i=0; i<freeTuple.length; i++) {
			String r = varsInRelations.get(freeTuple[i]).iterator().next();
			builder.append(r + "." + varToString.get(freeTuple[i]));
			if (i<freeTuple.length-1) {
				builder.append(", ");
			}
		}
		
		builder.append(" FROM ");
		builder.append(String.join(", ", relations));								//does not support self joins
		
		boolean where = false;
		Iterator<Integer> itKeySet = varsInRelations.keySet().iterator();
		
		while (itKeySet.hasNext()) {
			Integer v = itKeySet.next();
			String varString = varToString.get(v);
			Iterator<String> itRelations = varsInRelations.get(v).iterator();
			String first = itRelations.next();
			
			while (itRelations.hasNext()) {
				if (where) {
					builder.append(" AND ");
				} else {
					builder.append(" WHERE ");
					where = true;
				}
				builder.append(first + "." + varString + " = " + itRelations.next() + "." + varString);
			}
			
			if (!itKeySet.hasNext()) {
				builder.append(";");
			}
		}
		
		return builder.toString();
	}
}
