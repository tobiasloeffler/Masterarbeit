package myPackage;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class QTree {
	QTreeNode root;
	
	public QTree(QTreeNode root) {
		
		this.root = root;
	}
	
	public QTreeNode[] getChildren(QTreeNode node, int var) {
		
		if (node.var == var) {
			return node.children;
		} else if (node.children != null){
			for (int i=0; i<node.children.length; i++) {
				QTreeNode[] children = getChildren(node.children[i], var);
				if (children != null) {
					return children;
				}
			}
		}
		return null;
	}
	
	public int getChildrenCount(QTreeNode node, int var) {
		
		if (node.var == var) {
			if (node.children != null) {
				return node.children.length;
			} else {
				return 0;
			}
		} else if (node.children != null) {
			for (int i=0; i<node.children.length; i++) {
				int childrenCount = getChildrenCount(node.children[i], var);
				if (childrenCount != -1) {
					return childrenCount;
				}
			}
		}
		return -1;
	}
	
	public int[] getPath(QTreeNode node, Set<Integer> vars, int length) {
		
		if (vars.contains((Integer) node.var)) {
			vars.remove((Integer) node.var);
			if (vars.isEmpty()) {
				int[] result = new int[length];
				result[length-1] = node.var;
				return result;
			} else if (node.children != null) {
				for (int i=0; i<node.children.length; i++) {
					int[] result = this.getPath(node.children[i], vars, length+1);
					if (result != null) {
						result[length-1] = node.var;
						return result;
					}
				}
			}
		}
		return null;
	}
	
	public boolean inRep(Atom atom, int var) {
		
		Set<Integer> vars = new HashSet<Integer>();
		
		for (int i=0; i<atom.tuple.length; i++) {
			vars.add((Integer) atom.tuple[i]);
		}
		
		int[] path = this.getPath(this.root, vars, 1);
		return ((path != null) && (path[path.length-1] == var));
	}
	
public static QTree generateQTree(Query q) {
		
		Set<Set<Integer>> varSets = new HashSet<>();
		
		for (int i=0; i<q.atoms.length; i++) {
			Set<Integer> varSet = IntStream.of(q.atoms[i].tuple)
					.boxed()
					.collect(Collectors.toSet());
			varSets.add(varSet);
		}
		
		QTreeNode[] rootNodes = generateQTree(q, varSets);
		
		return new QTree(rootNodes[0]);
	}
	
	public static QTreeNode[] generateQTree(Query q, Set<Set<Integer>> varSets) {
		
		List<Set<Set<Integer>>> connectedComponents = new LinkedList<>();
		
		outerloop:
		for (Set<Integer> varSet : varSets) {
			
			Iterator<Set<Set<Integer>>> it = connectedComponents.iterator();
			
			while (it.hasNext()) {
				
				Set<Set<Integer>> component = (Set<Set<Integer>>) it.next();
				
				for (Set<Integer> atom : component) {
					Set<Integer> intersection = new HashSet<>(varSet);
					intersection.retainAll(atom);
					
					if (!intersection.isEmpty()) {
						component.add(varSet);
						continue outerloop;
					}
				}
			}
			
			Set<Set<Integer>> newComponent = new HashSet<>();
			newComponent.add(varSet);
			connectedComponents.add(newComponent);
		}
		
		QTreeNode[] result = new QTreeNode[connectedComponents.size()];
		int count = 0;
		
		for (Set<Set<Integer>> component : connectedComponents) {
			int rootVar = getRootVar(q, component);
			
			for (Iterator<Set<Integer>> it = component.iterator(); it.hasNext();) {
				
				Set<Integer> atom = it.next();
				
				if (atom.size() > 1) {
					atom.remove(rootVar);
				} else {
					it.remove();
				}
			}
			
			QTreeNode[] children = generateQTree(q, component);
			result[count] = new QTreeNode(rootVar, children);
			count++;
		}
		
		return result;
	}
	
	public static int getRootVar(Query q, Set<Set<Integer>> component) {
		
		Set<Integer> commonVars = intersectAll(component);
		
		Set<Integer> freeCommonVars = new HashSet<>(commonVars);
		freeCommonVars.retainAll(q.freeVars);
		
		int v;
		
		if (!freeCommonVars.isEmpty()) {
			v = freeCommonVars.iterator().next();
		} else {
			v = commonVars.iterator().next();
		}
		
		return v;
	}
	
	public static Set<Integer> intersectAll(Set<Set<Integer>> component) {
		
		Iterator<Set<Integer>> it = component.iterator();
		Set<Integer> first = new HashSet<>(it.next());
		
		while (it.hasNext()) {
			first.retainAll(it.next());
		}
		
		return first;
	}
}
