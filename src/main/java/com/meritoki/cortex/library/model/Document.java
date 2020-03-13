package com.meritoki.cortex.library.model;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import com.meritoki.cortex.library.model.Group;
import com.meritoki.cortex.library.model.Network;

public class Document {

	@JsonProperty
	public Network network = null;
	@JsonProperty
	public Group group = null;
	
	public Document() {
		this.network = new Network();
		this.group = new Group();
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
