/**
 * 
 */
package org.mindhaus.bdb.browser.ui.model;

import java.util.ArrayList;

import org.eclipse.core.runtime.IAdaptable;

/*
 * The content provider class is responsible for
 * providing objects to the view. It can wrap
 * existing objects in adapters or simply return
 * objects as-is. These objects may be sensitive
 * to the current input of the view, or ignore
 * it and always show the same content 
 * (like Task List, for example).
 */
public class Node implements IAdaptable {
	private String name;
	private Node parent;
	private ArrayList<Node> children;
	
	
	public Node(String name) {
		this.name = name;
		children = new ArrayList<Node>();
	}
	public String getName() {
		return name;
	}
	public void setParent(Node parent) {
		this.parent = parent;
	}
	public Node getParent() {
		return parent;
	}
	
	public void addChild(Node child) {
		children.add(child);
		child.setParent(this);
	}
	public void removeChild(Node child) {
		children.remove(child);
		child.setParent(null);
	}
	public Node[] getChildren() {
		return (Node[]) children.toArray(new Node[children.size()]);
	}
	public boolean hasChildren() {
		return children.size()>0;
	}
	
	public String toString() {
		return getName();
	}
	public Object getAdapter(Class adapter) {
		return null;
	}

}