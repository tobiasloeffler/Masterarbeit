package myPackage;

import java.util.HashSet;
import java.util.Set;

public class QTree extends DataStructure {
	QTreeNode root;
	
	public QTree() {
		QTreeNode node3 = new QTreeNode(3, null);
		QTreeNode node4 = new QTreeNode(4, null);
		QTreeNode[] children2 = {node3};
		QTreeNode node2 = new QTreeNode(2, children2);
		QTreeNode[] children1 = {node2, node4};
		QTreeNode node1 = new QTreeNode(1, children1);
		QTreeNode[] children0 = {node1};
		this.root = new QTreeNode(0, children0);
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
					int[] result = qTree.getPath(node.children[i], vars, length+1);
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
		
		for (int i=0; i<atom.vars.length; i++) {
			vars.add((Integer) atom.vars[i]);
		}
		
		int[] path = qTree.getPath(qTree.root, vars, 1);
		return ((path != null) && (path[path.length-1] == var));
	}
}
