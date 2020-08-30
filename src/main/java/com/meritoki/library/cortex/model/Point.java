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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

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
