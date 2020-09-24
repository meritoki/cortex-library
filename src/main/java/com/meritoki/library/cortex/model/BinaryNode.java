package com.meritoki.library.cortex.model;

public class BinaryNode {

	public int value;
	public BinaryNode left;
	public BinaryNode right;

	BinaryNode(int value) {
		this.value = value;
		right = null;
		left = null;
	}
	
	private BinaryNode addRecursive(BinaryNode current, int value) {
	    if (current == null) {
	        return new BinaryNode(value);
	    }
	 
	    if (value < current.value) {
	        current.left = addRecursive(current.left, value);
	    } else if (value > current.value) {
	        current.right = addRecursive(current.right, value);
	    } else {
	        // value already exists
	        return current;
	    }
	 
	    return current;
	}
}
