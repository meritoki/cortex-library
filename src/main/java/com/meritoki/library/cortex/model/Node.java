/*
 * Copyright 2020 Joaquin Osvaldo Rodriguez
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.meritoki.library.cortex.model;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Node<T> {

	@JsonIgnore
	protected Logger logger = Logger.getLogger(Node.class.getName());
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