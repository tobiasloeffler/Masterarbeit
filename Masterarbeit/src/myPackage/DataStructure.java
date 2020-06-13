package myPackage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DataStructure {
	
	static Query query;
	static MyList startList;
	static QTree qTree;
	static Map<String,Item>[] itemStorage;
	
	public static double[][] copyMatrix(double[][] src) {
		
		double[][] dest = new double[src.length][src.length];
		
		for (int i=0; i<dest.length; i++) {
			for (int j=0; j<dest.length; j++) {
				dest[i][j] = src[i][j];
			}
		}
		
		return dest;
	}
	
	public static void printMatrix(String name, double[][] matrix) {
		
		System.out.println("\n" + name + ":");
		
		for (int k=0; k<matrix[0].length; k++) {
			for (int l=0; l<matrix[0].length; l++) {
				System.out.print(matrix[k][l] + "  ");
			}
			System.out.println("");
		}
	}
	
	//mode: true = insert, false = delete
	public static void update(boolean mode, String relation, double[] constants) {
		
		outerloop:
		for (int i=0; i<query.atoms.length; i++) {
			Atom atom = query.atoms[i];
			Map<Integer, Integer> map = new HashMap<>();
			
			if ((!relation.equals(atom.relation)) || (atom.tuple.length != constants.length)) {
				continue;
			}
			
			for (int j=0; j<atom.tuple.length; j++) {
				if (map.containsKey((Integer) atom.tuple[j])) {
					if (constants[map.get(atom.tuple[j])] != constants[j]) {
						continue outerloop;
					}
				} else {
					map.put((Integer) atom.tuple[j], (Integer) j);
				}
			}
			
			if (atom.tuple.length == 3) {															//needs to be redone
				System.out.println("\nUpdate: " + relation + "(" + constants[0] + ", " + constants[1] + ", " + constants[2] + ")");
			} else {
				System.out.println("\nUpdate: " + relation + "(" + constants[0] + ", " + constants[1] + ", " + constants[2] + ", " + constants[3] + ")");
			}
			
			//create items
			
			String valuation = "";																	//replace with map?
			Set<Integer> vars = new HashSet<>();
			
			for (int j=0; j<atom.tuple.length; j++) {
				vars.add((Integer) atom.tuple[j]);
			}
			
			Item[] items = new Item[vars.size()];
			int[] path = qTree.getPath(qTree.root, vars, 1);
			
			for (int j=0; j<path.length; j++) {
				int var = path[j];
				double cons = constants[map.get((Integer) var)];
				
				valuation = valuation + "/" + Double.toString(cons);
				if (itemStorage[var].containsKey(valuation)) {
					items[j] = (itemStorage[var]).get(valuation);
				} else {
					if (j > 0) {
						items[j] = new Item(var, cons, valuation, items[j-1]);
					} else {
						items[j] = new Item(var, cons, valuation, null);
					}
					(itemStorage[var]).put(valuation, items[j]);
				}
			}
			
			for (int j=(items.length-1); j>(-1); j--) {
				System.out.println("\nd: " + j);
				
				int var = path[j];
				
				//1st step
				if (mode) {
					items[j].weightAtoms[i] += 1;
				} else {
					items[j].weightAtoms[i] -= 1;
				}
				
				//2nd step
				int weight_old = items[j].weight;
				double[][] matrix_old = copyMatrix(items[j].matrix);
				
				items[j].weight = 1;
				items[j].matrix = copyMatrix(items[j].baseMatrix);
				
				int weightRep = 1;
				
				for (int k=0; k<query.atoms.length; k++) {
					if (qTree.inRep(query.atoms[k], var)) {
						weightRep *= items[j].weightAtoms[k];
					}
				}
				
				items[j].weight *= weightRep;
				
				if (items[j].children != null) {
					for (int k=0; k<items[j].children.length; k++) {
						items[j].weight *= items[j].children[k].weight;
						for (int l=0; l<query.mFree; l++) {
							for (int m=0; m<query.mFree; m++) {
								items[j].matrix[l][m] *= (weightRep * items[j].children[k].matrix[l][m]);
							}
						}
					}
				}
				
				//2.a)
				int weightFree_old = 0;
				double[][] matrixFree_old = null;
				
				if (query.isFree(items[j].var)) {
					weightFree_old = items[j].weightFree;
					matrixFree_old = copyMatrix(items[j].matrixFree);
					
					if (items[j].weight == 0) {
						items[j].weightFree = 0;
						for (int k=0; k<(query.mFree-1); k++) {
							for (int l=0; l<(query.mFree-1); l++) {
								items[j].matrixFree[k][l] = 0;
							}
						}
					} else {
						items[j].weightFree = 1;
						for (int k=0; k<(query.mFree-1); k++) {
							for (int l=0; l<(query.mFree-1); l++) {
								if ((k == 3) && (l == 3)) {
									items[j].matrixFree[k][l] = items[j].baseMatrix[4][4];
								} else if (k==3) {
									items[j].matrixFree[k][l] = items[j].baseMatrix[4][l];
								} else if (l==3) {
									items[j].matrixFree[k][l] = items[j].baseMatrix[k][4];
								} else {
									items[j].matrixFree[k][l] = items[j].baseMatrix[k][l];
								}
							}
						}
						
						if (items[j].children != null) {
							for (int k=0; k<items[j].children.length; k++) {
								if (query.isFree(items[j].children[k].var)) {
									items[j].weightFree *= items[j].children[k].weightFree;
									for (int l=0; l<(query.mFree-1); l++) {
										for (int m=0; m<(query.mFree-1); m++) {
											items[j].matrixFree[l][m] *= (items[j].children[k].matrixFree[l][m]);
										}
									}
								}
							}
						}
					}
				}
				
				//3rd step
				MyList list = null;
				
				if (items[j].parent != null) {
					for (int k=0; k<items[j].parent.children.length; k++) {
						if (items[j].parent.children[k].var == items[j].var) {
							list = items[j].parent.children[k];
						}
					}
				} else {
					list = startList;
				}
						
				if ((items[j].weight > 0) && (weight_old == 0)) {
					if (list.first == null) {
						list.first = items[j];
						list.last = list.first;
					} else {
						items[j].prev = list.last;
						list.last.next = items[j];
						list.last = items[j];
					}
				} else if ((items[j].weight == 0) && (weight_old > 0)) {
					if (list.first == list.last) {
						list.first = null;
						list.last = null;
					} else {
						if (list.first == items[j]) {
							list.first = items[j].next;
						} else if (list.last == items[j]) {
							list.last = items[j].prev;
						}
						if (items[j].prev != null) {
							items[j].prev.next = items[j].next;
						}
						if (items[j].next != null) {
							items[j].next.prev = items[j].prev;
						}
						items[j].prev = null;
						items[j].next = null;
					}
				}
				
				//4th step
				list.weight -= weight_old;
				list.weight += items[j].weight;
				
				for (int k=0; k<query.mFree; k++) {
					for (int l=0; l<query.mFree; l++) {
						list.matrix[k][l] -= matrix_old[k][l];
						list.matrix[k][l] += items[j].matrix[k][l];
					}
				}
				
				//4.a)
				if (query.isFree(items[j].var)) {
					list.weightFree -= weightFree_old;
					list.weightFree += items[j].weightFree;
					
					for (int k=0; k<(query.mFree-1); k++) {
						for (int l=0; l<(query.mFree-1); l++) {
							list.matrixFree[k][l] -= matrixFree_old[k][l];
							list.matrixFree[k][l] += items[j].matrixFree[k][l];
						}
					}
				}
				
				//5th step
				boolean delete = true;
				
				if (!mode) {
					for (int k=0; k<query.atoms.length; k++) {
						if (items[j].weightAtoms[k] > 0) {
							delete = false;
						}
					}
					if (delete) {
						itemStorage[var].remove(items[j].valuation);
					}
				}
			}
		}
	}
	
	public static void main(String[] args) {
		
		query = Query.parseQuery(args);
		
		qTree = QTree.generateQTree(query);
		
		System.exit(0);
		
		
		
		
		startList = new MyList(qTree.root.var);
		itemStorage = (HashMap<String,Item>[]) new HashMap[query.mFree];
		
		for (int i=0; i<query.mFree; i++) {
			itemStorage[i] = new HashMap<String, Item>();
		}
		
		String url = "jdbc:postgresql://localhost/masterarbeit";
		String user = "tobi";
		String password = "password";
		
		try (Connection conn = DriverManager.getConnection(url, user, password)) {
			
			System.out.println("Connection successful\n");
			
			Statement statement = conn.createStatement();
			ResultSet result = statement.executeQuery(query.sql);
			
			while (result.next()) {
				System.out.println(result.getDouble(1) + " " + result.getDouble(2) + " " + result.getDouble(3) + " " + result.getDouble(4) + " " + result.getDouble(5));
			}
			
			result = statement.executeQuery("SELECT * FROM R1;");
			double[] cons1 = new double[3];
			
			while (result.next()) {
				for (int i=0; i<3; i++) {
					cons1[i] = result.getDouble(i+1);
				}
				update(true, "R1", cons1);
			}
			
			System.out.println("\nAll tuples from R1 succesfully inserted.");
			
			result = statement.executeQuery("SELECT * FROM R2;");
			double[] cons2 = new double[4];
			
			while (result.next()) {
				for (int i=0; i<4; i++) {
					cons2[i] = result.getDouble(i+1);
				}
				update(true, "R2", cons2);
			}
			
			System.out.println("All tuples from R2 succesfully inserted.");
			
			result = statement.executeQuery("SELECT * FROM R3;");
			double[] cons3 = new double[3];
			
			while (result.next()) {
				for (int i=0; i<3; i++) {
					cons3[i] = result.getDouble(i+1);
				}
				update(true, "R3", cons3);
			}
			
			System.out.println("All tuples from R3 succesfully inserted.\n");
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		printMatrix("Cofactor", startList.matrix);
		
		printMatrix("CofactorFree", startList.matrixFree);
		
		Item item = startList.first;
		
		while (item != null) {
			printMatrix(item.valuation, item.matrix);
			item = item.next;
		}
	}
}
