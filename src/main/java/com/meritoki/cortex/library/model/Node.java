package com.meritoki.cortex.library.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

public class Node<T> {

	@JsonIgnore
	private static Logger logger = LogManager.getLogger(Node.class.getName());
	@JsonIgnore
	private T data = null;
	@JsonIgnore
	private List<Node<T>> children = new ArrayList<>();
	@JsonIgnore
	private Node<T> parent = null;
	@JsonProperty
	public Coincidence coincidence = new Coincidence(6);
	@JsonIgnore
	public Coincidence brightnessCoincidence = new Coincidence(6);
	public Coincidence redCoincidence = new Coincidence(6);
	public Coincidence greenCoincidence = new Coincidence(6);
	public Coincidence blueCoincidence = new Coincidence(6);
	@JsonIgnore
	public static final int SIDES = 6;
	@JsonIgnore
	public Cone[] shortConeArray = new Cone[SIDES];
	@JsonIgnore
	public Cone[] mediumConeArray = new Cone[SIDES];
	@JsonIgnore
	public Cone[] longConeArray = new Cone[SIDES];

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
	public Coincidence getCoincidence(int type) {
//		logger.info("getCoincidence("+type+")");
		Coincidence coincidence = new Coincidence();
		int value = 0;
		for (int i = 0; i < SIDES; i++) {
			switch(type) {
			case Network.BRIGHTNESS: {
				value = (shortConeArray[i].blue + mediumConeArray[i].green + longConeArray[i].red) / 3;
				break;
			}
			case Network.RED: {
				value = longConeArray[i].red;
				break;
			}
			case Network.GREEN: {
				value = mediumConeArray[i].green;
				break;
			}
			case Network.BLUE: {
				value = shortConeArray[i].blue;
				break;
			}
			default: {
				value = 0;
				break;
			}
			}
			coincidence.addInteger(value);
		}
		return coincidence;
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