package components.tree;

public class EmptyNode<T> extends TreeNode<T> {

	EmptyNode() { super(null); }
	
	public void append(TreeNode<T> node) { throw new UnsupportedOperationException("Cannot append to an EmptyNode"); }
	public void remove(TreeNode<T> node) { throw new UnsupportedOperationException("Cannot remove from an EmptyNode"); }

	public void insertBefore(TreeNode<T> node) { super.insertBefore(node); getRoot().remove(this); }
	public void insertAfter(TreeNode<T> node) { super.insertAfter(node); getRoot().remove(this); }
	
	public void set(T val) { throw new UnsupportedOperationException("Cannot set the value of an EmptyNode"); }
	public void setMetadata(TreeNodeMetadata m) { throw new UnsupportedOperationException("Cannot add Metadata to EmptyNode"); }
	public TreeNodeMetadata getMetadata() { return null; }
	
//	protected void fireNodeChanged(TreeNode<T> source, TreeNode<T> root, T oldValue) { }
//	protected void fireStructureChanged(TreeNode<T> source, TreeNode<T> root, boolean added) { }
}
