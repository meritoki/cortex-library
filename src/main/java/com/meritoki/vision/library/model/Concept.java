package com.meritoki.vision.library.model;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

public class Concept {

	@JsonProperty
	public String value = null;
	@JsonProperty
	public double rank = 0;
	
	public Concept() {
	}
	
	public Concept(String value) {
		this.value = value;
	}
	
	@Override
	@JsonIgnore
	public boolean equals(Object o) {
		boolean flag = false;
		if (o instanceof Concept) {
			Concept concept = (Concept) o;
			flag = (concept.value != null)?concept.value.equals(this.value):false;
		}
		return flag;
	}
	
	@JsonIgnore
	public String toString() {
		return this.value;
	}
}
