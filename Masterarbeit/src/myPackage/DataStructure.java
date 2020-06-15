package myPackage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DataStructure {
	
	static Query query;
	static MyList startList;
	static QTree qTree;
	static Map<String,Item>[] itemStorage;
	
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
	
	public static void deleteItem(boolean mode, Item item) {
		
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
			
			if (!atom.isCompatible(relation, constants)) {
				continue;
			}
			
			printUpdate(mode, relation, constants);
			
			Item[] items = createItems(atom, constants);
			
			for (int j=(items.length-1); j>(-1); j--) {
				
				Item item = items[j];
				
				int weight_old = item.weight;
				Matrix matrix_old = Matrix.copy(item.matrix);
				
				int weightFree_old = 0;
				Matrix matrixFree_old = null;
				
				if (query.isFree(items[j].var)) {
					weightFree_old = item.weightFree;
					matrixFree_old = Matrix.copy(item.matrixFree);
				}
				
				MyList list;
				
				item.updateWeightAtoms(mode, i);
				
				item.updateWeightAndMatrix();
				
				item.updateWeightFreeAndMatrixFree();
				
				list = item.updateList(weight_old);
				
				list.updateWeightAndMatrix(item, weight_old, matrix_old);
				
				list.updateWeightFreeAndMatrixFree(item, weightFree_old, matrixFree_old);
				
				deleteItem(mode, item);
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
		itemStorage = (HashMap<String,Item>[]) new HashMap[query.m];
		
		for (int i=0; i<query.m; i++) {
			itemStorage[i] = new HashMap<String, Item>();
		}
		
		try (Connection conn = DriverManager.getConnection(url, user, password)) {
			
			System.out.println("Connection successful\n");
			
			Statement statement = conn.createStatement();
			ResultSet result = statement.executeQuery(query.sql);
			
			/*while (result.next()) {
				System.out.println(result.getDouble(1) + " "
						+ result.getDouble(2) + " "
						+ result.getDouble(3) + " "
						+ result.getDouble(4) + " "
						+ result.getDouble(5));
			}*/
			
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
		
		startList.matrix.print("Cofactor");
		
		startList.matrixFree.print("CofactorFree");
	}
}
