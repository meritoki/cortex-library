package com.meritoki.library.cortex.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Node<T> {

	@JsonIgnore
	private static Logger logger = LogManager.getLogger(Node.class.getName());
	@JsonIgnore
	private T data = null;
	@JsonIgnore
	private List<Node<T>> children = new ArrayList<>();
	@JsonIgnore
	private Node<T> parent = null;

	public Node() {
	}
	
	public Node(T data) {
		this.data = data;
	}

	@JsonIgnore
	public Node<T> addChild(Node<T> child) {
		child.setParent(this);
		this.children.add(child);
		return child;
	}

	@JsonIgnore
	public void addChildren(List<Node<T>> children) {
		children.forEach(each -> each.setParent(this));
		this.children.addAll(children);
	}

	@JsonIgnore
	public List<Node<T>> getChildren() {
		return children;
	}

	@JsonIgnore
	public T getData() {
		return data;
	}

	@JsonIgnore
	public void setData(T data) {
		this.data = data;
	}

	@JsonIgnore
	private void setParent(Node<T> parent) {
		this.parent = parent;
	}

	@JsonIgnore
	public Node<T> getParent() {
		return parent;
	}

	@JsonIgnore
	public static <T> void printTree(Node<T> node, String appender) {
		System.out.println(appender + node.getData());
		node.getChildren().forEach(each -> printTree(each, appender + appender));
	}
}