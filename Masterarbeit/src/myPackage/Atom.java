package myPackage;

import java.util.HashMap;
import java.util.Map;

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
	
	public boolean isCompatible(String relation, double[] constants) {
		
		Map<Integer, Integer> map = new HashMap<>();
		
		if ((!relation.equals(this.relation)) || (this.tuple.length != constants.length)) {
			return false;
		}
		
		for (int j=0; j<this.tuple.length; j++) {
			if (map.containsKey((Integer) this.tuple[j])) {
				if (constants[map.get(this.tuple[j])] != constants[j]) {
					return false;
				}
			} else {
				map.put((Integer) this.tuple[j], (Integer) j);
			}
		}
		
		return true;
	}
}
