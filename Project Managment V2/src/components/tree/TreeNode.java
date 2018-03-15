package components.tree;

import java.util.ArrayList;
import java.util.Comparator;

import components.tree.table.TableRow;

public class TreeNode<T> {
	private TreeNode<T> root;
	private TreeNode<T> prev, next;
	private TreeNode<T> first, last;
	
	private T value;
	private boolean expanded;
	private boolean neverEmpty;
	
	private TableRow<T> attachedRow;
	
	public TreeNode(T value) { this(value, false); }
	public TreeNode(T value, boolean neverEmpty) { 
		this.value = value; 
		this.neverEmpty = neverEmpty;
		if(neverEmpty) append(new EmptyNode<>()); 
	}
	
	public void append(TreeNode<T> node) {
		if(node.root != null) node.root.remove(node);
		node.root = this;

		if(last != null) last.next = node;
		node.prev = last == null ? first : last;

		if(first == null) first = node;		
		last = node;
		
		if(neverEmpty && !(node instanceof EmptyNode) && first instanceof EmptyNode) remove(first);

		node.fireStructureChanged(node, this, true);
	}
	
	public void remove(TreeNode<T> node) {
		if(node.root != this) throw new IllegalArgumentException("Node is not attached to this Node");

		if(node.prev == null) // First Node
			 node.root.first = node.next;
		else node.prev.next  = node.next;
		
		if(node.next == null) // Last Node
			 node.root.last = node.prev;
		else node.next.prev = node.prev;

		node.root = null;
		node.prev = null;
		node.next = null;
		
		if(neverEmpty && first == null) append(new EmptyNode<>());
		
		node.fireStructureChanged(node, this, false);
	}
	
	public void insertBefore(TreeNode<T> node) {
		if(root == null) throw new IllegalStateException("Cannot insert before this Node! This Node is not attach to a Root");
		if(prev != null) { prev.insertAfter(node); return; } // Not First

		if(node.root != null) node.root.remove(node);
		node.root = root;
		
		prev = node;
		node.next = this;
		root.first = node;
		
		node.fireStructureChanged(node, node.root, true);
	}
	
	public void insertAfter(TreeNode<T> node) {
		if(root == null) throw new IllegalStateException("Cannot insert after this Node! This Node is not attach to a Root");
		
		if(node.root != null) node.root.remove(node);
		node.root = root;
		
		node.next = next;
		node.prev = this;
		next = node;
		
		if(node.next == null) root.last = node; // Is Last
		else node.next.prev = node;
		
		node.fireStructureChanged(node, node.root, true);
	}
	
	public void set(T t) { 
		T old = this.value;
		this.value = t;
		
		fireNodeChanged(this, root, old);
	}
	
	public void makeVisable() {
		if(root != null)
			root.makeVisable();
		expanded = true;
	}
	
	public static interface TreeWalker<T> { public void moveTo(int level, TreeNode<T> node); }

	public void walk(TreeWalker<T> walker) { walk(false, walker, 0); }
	public void walk(boolean onlyOpen, TreeWalker<T> walker) { walk(onlyOpen, walker, 0); }
	
	private void walk(boolean onlyOpen, TreeWalker<T> walker, int level) { 
		walker.moveTo(level, this);
		
		if(first != null && (!onlyOpen || expanded)) first.walk(onlyOpen, walker, level + 1);
		if(next  != null)  next.walk(onlyOpen, walker, level);
	}
	
	public void sort(Comparator<TreeNode<T>> comparator) {
		TreeNode<T> tail = first, head = first;
		TreeNode<T> current = first.next;
		tail.next = tail.prev = null;
		
		while(current != null) {
			TreeNode<T> next = current.next;
			current.next = current.prev = null;
			
			TreeNode<T> compare = tail, prev = null;
			while(compare != null && comparator.compare(current, compare) < 0) {
				prev = compare;
				compare = compare.prev;
			}
			
			current.next = prev;
			current.prev = compare;
			
			if(prev != null) prev.prev = current;
			else tail = current;
			
			if(compare != null) compare.next = current;
			else head = current;
			
			current = next;
		}
		
		this.first = head;
		this.last = tail;
	}

	public TreeNode<T> getRoot() { return root; }

	public TreeNode<T> getPrev() { return prev; }
	public TreeNode<T> getNext() { return next; }

	public TreeNode<T> getFirst() { return first; }
	public TreeNode<T> getLast() { return last; }

	public T get() { return value; }
	
	public boolean isExpanded() { return expanded; }
	public void setExpanded(boolean expanded) { this.expanded = expanded; }
	
	public void setNeverEmpty(boolean never) {
		this.neverEmpty = never;
		if(neverEmpty && first == null) append(new EmptyNode<>());
	}
	
	private TreeNodeMetadata metadata;
	public void setMetadata(TreeNodeMetadata metadata) { 
		this.metadata = metadata; 
		setNeverEmpty(metadata.isContainer());
	}
	public TreeNodeMetadata getMetadata() {
		return metadata != null ? metadata : new TreeNodeMetadata(value.toString(), first != null);
	}
	
	public TableRow<T> getAttachedRow() { return attachedRow; }
	public void attachTableRow(TableRow<T> row) { 
		if(this.attachedRow != null) throw new IllegalStateException("Cannot reassign TableRow");
		this.attachedRow = row; 
	}

//	--------------------------------------------------------------------------------------------------- \\
	
	public static interface TreeListener<T> {
		public void nodeChanged(TreeNode<T> source, TreeNode<T> root, T oldValue);
		public void structureChanged(TreeNode<T> source, TreeNode<T> root, boolean added);
	}
	
	private ArrayList<TreeListener<T>> listeners = new ArrayList<>();
	
	public void addTreeListener(TreeListener<T> listener) { listeners.add(listener); }
	public void removeTreeListener(TreeListener<T> listener) { listeners.remove(listener); }
	
	protected void fireNodeChanged(TreeNode<T> source, TreeNode<T> root, T oldValue) {
		listeners.forEach(l -> l.nodeChanged(source, root, oldValue));
		if(root != null) root.fireNodeChanged(source, root, oldValue);
	}
	
	protected void fireStructureChanged(TreeNode<T> source, TreeNode<T> root, boolean added) {
		listeners.forEach(l -> l.structureChanged(source, root, added));
		if(this.root != null) this.root.fireStructureChanged(source, root, added);
		else if(!added && this == source && root != null) root.fireStructureChanged(source, root, added);
	}
}
