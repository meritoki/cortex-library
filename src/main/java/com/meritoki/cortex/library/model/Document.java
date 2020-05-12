package com.meritoki.cortex.library.model;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import com.meritoki.cortex.library.model.hexagon.Hexagonal;

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
