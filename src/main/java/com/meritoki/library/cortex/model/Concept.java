package com.meritoki.library.cortex.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

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
