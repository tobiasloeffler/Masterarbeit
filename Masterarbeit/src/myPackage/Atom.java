package myPackage;

import java.util.Set;

public class Atom {
	String relation;
	Set<String> vars;

	public Atom(String relation, Set<String> vars) {
		this.relation = relation;
		this.vars = vars;
	}
}
