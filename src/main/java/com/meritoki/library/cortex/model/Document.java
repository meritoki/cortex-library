package com.meritoki.library.cortex.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.meritoki.library.cortex.model.hexagon.Hexagonal;

public class Document {

	@JsonProperty
	public Network network = null;
	@JsonProperty
	public Group group = null;
	
	public Document() {
		this.network = new Hexagonal(Hexagonal.BRIGHTNESS, 0, 0, 7, 1, 0);
		this.group = new Group(Group.SQUARED);
	}
	
	@JsonIgnore
	public Network getNetwork() {
		return this.network;
	}
	
	@JsonIgnore
	public Group getGroup() {
		return this.group;
	}
}
