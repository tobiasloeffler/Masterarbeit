package myPackage;

public class Atom {
	String relation;
	int[] tuple;

	public Atom(String relation, int[] tuple) {
		this.relation = relation;
		this.tuple = tuple;
	}
	
	public int firstIndexOf(int var) {
		
		for (int i=0; i<this.tuple.length; i++) {
			if (tuple[i] == var) {
				return i;
			}
		}
		
		return -1;
	}
}
