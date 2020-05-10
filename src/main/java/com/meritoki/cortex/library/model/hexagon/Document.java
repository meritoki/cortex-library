package com.meritoki.cortex.library.model.hexagon;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

public class Document {

	@JsonProperty
	public Network network = null;
	@JsonProperty
	public Group group = null;
	
	public Document() {
		this.network = new Network(Network.BRIGHTNESS, 0, 0, 7, 1, 0);
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
