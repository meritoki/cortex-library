package com.meritoki.cortex.library.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

public class Belief {
	
	@JsonIgnore
	private static Logger logger = LogManager.getLogger(Belief.class.getName());
	@JsonProperty
	public Point point;
	@JsonProperty
	public Concept concept;
	
	public Belief() {
		
	}
	
	public Belief(Concept concept, Point point) {
		logger.info("Belief("+concept+","+point+")");
		this.concept = concept;
		this.point = point;
	}
}
