package com.meritoki.library.cortex.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Document {

	@JsonProperty
	public Cortex cortex;
	
	public Document() {
//		this.cortex = new Hexagonal(Hexagonal.BRIGHTNESS, 0, 0, 7, 1, 0);
//		this.cortex = new Group(Group.SQUARED);
	}
}
