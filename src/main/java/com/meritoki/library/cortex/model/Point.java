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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Point extends Node {

	@JsonProperty
	public double x = 0;
	@JsonProperty
	public double y = 0;

	public Point() {
	}

	public Point(Point p) {
//		super(p.x+","+p.y);
		this.x = p.x;
		this.y = p.y;
//		this.belief = p.belief;
	}

	public Point(double x, double y) {
		super(x+","+y);
		this.x = x;
		this.y = y;
	}
	
	public void scale(double scale) {
		this.x *= scale;
		this.y *= scale;
	}
	
	@JsonIgnore
	public static double getDistance(Point a, Point b) {
		double value = Math.sqrt(Math.pow(b.x - a.x, 2) + Math.pow(b.y - a.y, 2));
//		System.out.println("getDistance("+a+", "+b+") value="+value);
		return value;
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
