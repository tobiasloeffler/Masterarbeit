package myPackage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

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
	
	public static void printUpdate(boolean mode, String relation, double[] constants) {
		
		if (mode) {
			System.out.print("insert: " + relation + "(");
		} else {
			System.out.print("delete: " + relation + "(");
		}
		
		for (int i=0; i<constants.length-1; i++) {
			System.out.print(constants[i] + ", ");
		}
		
		System.out.println(constants[constants.length-1] + ")");
	}
	
	public static boolean isAtomCompatible(Atom atom, String relation, double[] constants) {
		
		Map<Integer, Integer> map = new HashMap<>();
		
		if ((!relation.equals(atom.relation)) || (atom.tuple.length != constants.length)) {
			return false;
		}
		
		for (int j=0; j<atom.tuple.length; j++) {
			if (map.containsKey((Integer) atom.tuple[j])) {
				if (constants[map.get(atom.tuple[j])] != constants[j]) {
					return false;
				}
			} else {
				map.put((Integer) atom.tuple[j], (Integer) j);
			}
		}
		
		return true;
	}
	
	public static Item[] createItems(Atom atom, double[] constants) {
		
		String valuation = "";
		int[] path = qTree.getPath(atom);
		Item[] items = new Item[path.length];
		
		for (int j=0; j<path.length; j++) {
			int var = path[j];
			double cons = constants[atom.firstIndexOf(var)];
			
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
		
		return items;
	}
	
	public static void step1(boolean mode, Item item, int atomIndex) {
		
		if (mode) {
			item.weightAtoms[atomIndex] += 1;
		} else {
			item.weightAtoms[atomIndex] -= 1;
		}
	}
	
	public static void step2(Item item) {
		
		int var = item.var;
		
		item.weight = 1;
		item.matrix = copyMatrix(item.baseMatrix);
		
		int weightRep = 1;
		
		for (int k=0; k<query.atoms.length; k++) {
			if (qTree.inRep(query.atoms[k], var)) {
				weightRep *= item.weightAtoms[k];
			}
		}
		
		item.weight *= weightRep;
		
		if (item.children != null) {
			for (int k=0; k<item.children.length; k++) {
				item.weight *= item.children[k].weight;
				for (int l=0; l<query.mFree; l++) {
					for (int m=0; m<query.mFree; m++) {
						item.matrix[l][m] *= (weightRep * item.children[k].matrix[l][m]);
					}
				}
			}
		}
	}
	
	public static void step2a(Item item) {
		
		if (query.isFree(item.var)) {
			if (item.weight == 0) {
				item.weightFree = 0;
				for (int k=0; k<(query.mFree-1); k++) {
					for (int l=0; l<(query.mFree-1); l++) {
						item.matrixFree[k][l] = 0;
					}
				}
			} else {
				item.weightFree = 1;
				for (int k=0; k<(query.mFree-1); k++) {
					for (int l=0; l<(query.mFree-1); l++) {
						if ((k == 3) && (l == 3)) {
							item.matrixFree[k][l] = item.baseMatrix[4][4];
						} else if (k==3) {
							item.matrixFree[k][l] = item.baseMatrix[4][l];
						} else if (l==3) {
							item.matrixFree[k][l] = item.baseMatrix[k][4];
						} else {
							item.matrixFree[k][l] = item.baseMatrix[k][l];
						}
					}
				}
				
				if (item.children != null) {
					for (int k=0; k<item.children.length; k++) {
						if (query.isFree(item.children[k].var)) {
							item.weightFree *= item.children[k].weightFree;
							for (int l=0; l<(query.mFree-1); l++) {
								for (int m=0; m<(query.mFree-1); m++) {
									item.matrixFree[l][m] *= (item.children[k].matrixFree[l][m]);
								}
							}
						}
					}
				}
			}
		}
	}
	
	public static MyList step3(Item item, int weight_old) {
		
		MyList list = null;
		
		if (item.parent != null) {
			for (int k=0; k<item.parent.children.length; k++) {
				if (item.parent.children[k].var == item.var) {
					list = item.parent.children[k];
				}
			}
		} else {
			list = startList;
		}
				
		if ((item.weight > 0) && (weight_old == 0)) {
			if (list.first == null) {
				list.first = item;
				list.last = list.first;
			} else {
				item.prev = list.last;
				list.last.next = item;
				list.last = item;
			}
		} else if ((item.weight == 0) && (weight_old > 0)) {
			if (list.first == list.last) {
				list.first = null;
				list.last = null;
			} else {
				if (list.first == item) {
					list.first = item.next;
				} else if (list.last == item) {
					list.last = item.prev;
				}
				if (item.prev != null) {
					item.prev.next = item.next;
				}
				if (item.next != null) {
					item.next.prev = item.prev;
				}
				item.prev = null;
				item.next = null;
			}
		}
		
		return list;
	}
	
	public static void step4(Item item, MyList list, int weight_old, double[][] matrix_old) {
		
		list.weight -= weight_old;
		list.weight += item.weight;
		
		for (int k=0; k<query.mFree; k++) {
			for (int l=0; l<query.mFree; l++) {
				list.matrix[k][l] -= matrix_old[k][l];
				list.matrix[k][l] += item.matrix[k][l];
			}
		}
	}
	
	public static void step4a(Item item, MyList list, int weightFree_old, double[][] matrixFree_old) {
		
		if (query.isFree(item.var)) {
			list.weightFree -= weightFree_old;
			list.weightFree += item.weightFree;
			
			for (int k=0; k<(query.mFree-1); k++) {
				for (int l=0; l<(query.mFree-1); l++) {
					list.matrixFree[k][l] -= matrixFree_old[k][l];
					list.matrixFree[k][l] += item.matrixFree[k][l];
				}
			}
		}
	}
	
	public static void step5(boolean mode, Item item) {
		
		boolean delete = true;
		int var = item.var;
		
		if (!mode) {
			for (int k=0; k<query.atoms.length; k++) {
				if (item.weightAtoms[k] > 0) {
					delete = false;
				}
			}
			if (delete) {
				itemStorage[var].remove(item.valuation);
			}
		}
	}
	
	//mode: true = insert, false = delete
	public static void update(boolean mode, String relation, double[] constants) {
		
		for (int i=0; i<query.atoms.length; i++) {
			Atom atom = query.atoms[i];
			
			if (!isAtomCompatible(atom, relation, constants)) {
				continue;
			}
			
			printUpdate(mode, relation, constants);
			
			Item[] items = createItems(atom, constants);
			
			for (int j=(items.length-1); j>(-1); j--) {
				
				int weight_old = items[j].weight;
				double[][] matrix_old = copyMatrix(items[j].matrix);
				
				int weightFree_old = 0;
				double[][] matrixFree_old = null;
				
				if (query.isFree(items[j].var)) {
					weightFree_old = items[j].weightFree;
					matrixFree_old = copyMatrix(items[j].matrixFree);
				}
				
				MyList list = null;
				
				step1(mode, items[j], i);
				
				step2(items[j]);
				
				step2a(items[j]);
				
				list = step3(items[j], weight_old);
				
				step4(items[j], list, weight_old, matrix_old);
				
				step4a(items[j], list, weightFree_old, matrixFree_old);
				
				step5(mode, items[j]);
			}
		}
	}
	
	public static void main(String[] args) {
		
		Properties properties = new Properties();
		
		try (FileInputStream inputStream = new FileInputStream("resources\\config.properties")) {
			properties.load(inputStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String url = properties.getProperty("url");
		String user = properties.getProperty("user");
		String password = properties.getProperty("password");
		
		query = Query.parseQuery(args);
		qTree = QTree.generateQTree(query);
		
		startList = new MyList(qTree.root.var);
		itemStorage = (HashMap<String,Item>[]) new HashMap[query.mFree];
		
		for (int i=0; i<query.mFree; i++) {
			itemStorage[i] = new HashMap<String, Item>();
		}
		
		try (Connection conn = DriverManager.getConnection(url, user, password)) {
			
			System.out.println("Connection successful\n");
			
			Statement statement = conn.createStatement();
			ResultSet result = statement.executeQuery(query.sql);
			
			while (result.next()) {
				System.out.println(result.getDouble(1) + " "
						+ result.getDouble(2) + " "
						+ result.getDouble(3) + " "
						+ result.getDouble(4) + " "
						+ result.getDouble(5));
			}
			
			result = statement.executeQuery("SELECT * FROM r1;");
			double[] cons1 = new double[3];
			
			while (result.next()) {
				for (int i=0; i<3; i++) {
					cons1[i] = result.getDouble(i+1);
				}
				update(true, "r1", cons1);
			}
			
			System.out.println("\nAll tuples from r1 succesfully inserted.");
			
			result = statement.executeQuery("SELECT * FROM r2;");
			double[] cons2 = new double[4];
			
			while (result.next()) {
				for (int i=0; i<4; i++) {
					cons2[i] = result.getDouble(i+1);
				}
				update(true, "r2", cons2);
			}
			
			System.out.println("All tuples from r2 succesfully inserted.");
			
			result = statement.executeQuery("SELECT * FROM r3;");
			double[] cons3 = new double[3];
			
			while (result.next()) {
				for (int i=0; i<3; i++) {
					cons3[i] = result.getDouble(i+1);
				}
				update(true, "r3", cons3);
			}
			
			System.out.println("All tuples from r3 succesfully inserted.\n");
			
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
