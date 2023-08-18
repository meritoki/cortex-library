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
package com.meritoki.library.cortex.model.unit;

import java.io.IOException;
import java.text.DecimalFormat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class Point  { //extends Node {

	@JsonProperty
	public double x = 0;
	@JsonProperty
	public double y = 0;
//	@JsonIgnore
//	public boolean center;

	public Point() {
	}

	public Point(Point p) {
//		super(p.x+","+p.y);
		this.x = p.x;
		this.y = p.y;
//		this.center = p.center;
//		this.belief = p.belief;
	}

	public Point(double x, double y) {
//		super(x + "," + y);
		this.x = this.round(x);
		this.y = this.round(y);
	}
	
	public void round() {
		this.x = this.round(this.x);
		this.y = this.round(this.y);
	}
	
	public double round(double value) {
		DecimalFormat decimalFormat = new DecimalFormat("#.##");
		return Double.parseDouble(decimalFormat.format(value));
	}
	
	public double getRadius(Point p) {
		return Math.sqrt(Math.pow(this.x-p.x,2)+Math.pow(this.y-p.y, 2));
	}

	public Point subtract(Point point) {
		return (point != null) ? new Point(this.x - point.x, this.y - point.y) : null;
	}

	public Point add(Point point) {
		return (point != null) ? new Point(this.x + point.x, this.y + point.y) : null;
	}

	public void scale(double scale) {
		this.x *= scale;
		this.y *= scale;
//		this.x = this.round(this.x);
//		this.y = this.round(this.y);
	}

	@JsonIgnore
	public static double getDistance(Point a, Point b) {
		double value = Math.sqrt(Math.pow(b.x - a.x, 2) + Math.pow(b.y - a.y, 2));
//		System.out.println("getDistance("+a+", "+b+") value="+value);
		return value;
	}

	@JsonIgnore
	public boolean equals(Point point) {
//		System.out.println(this+".equals("+point+")");
		boolean flag = false;
		if (this.x == point.x && this.y == point.y) {
			flag = true;
		}
		return flag;
	}

	@JsonIgnore
	@Override
	public String toString() {
		String string = "";
		ObjectWriter ow = new ObjectMapper().writer();
		try {
			string = ow.writeValueAsString(this);
		} catch (IOException ex) {
			System.err.println("IOException " + ex.getMessage());
		}
		return string;
	}
}
