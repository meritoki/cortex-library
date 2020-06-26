package com.meritoki.cortex.library.model;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

public class Point {

	@JsonProperty
	public double x = 0;
	@JsonProperty
	public double y = 0;

	public Point() {
	}

	public Point(Point p) {
		this.x = p.x;
		this.y = p.y;
	}

	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}

	@JsonIgnore
	public boolean equals(Point point) {
		boolean flag = false;
		if (this.x == point.x && this.y == point.y) {
			flag = true;
		}
		return flag;
	}
	
	@JsonIgnore
	public String toString() {
		return "{"+this.x+","+this.y+"}";
	}
}
