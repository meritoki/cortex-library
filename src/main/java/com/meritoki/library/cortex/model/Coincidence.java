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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Coincidence {

	@JsonIgnore
	protected Logger logger = Logger.getLogger(Coincidence.class.getName());
	@JsonProperty
	public List<Integer> list = new ArrayList<>();
	@JsonProperty
	public double threshold = 0.95;
	@JsonProperty
	public double quotient = 0;

	public Coincidence() {
	}

	public Coincidence(String list) {
		String commaList = list.replace("[", "").replace("]", "");
		String[] integerArray = commaList.split(",");
		for (String s : integerArray) {
			this.addInteger(Integer.parseInt(s.trim()));
		}
	}

	public Coincidence(int size) {
		for (int i = 0; i < size; i++) {
			this.addInteger(0);
		}
	}

	@Override
	@JsonIgnore
	public boolean equals(Object o) {
		boolean flag = false;
		if (o instanceof Coincidence) {
			Coincidence hexagon = (Coincidence) o;
			flag = hexagon.toString().equals(this.toString());
		}
		return flag;
	}

	@JsonIgnore
	public void addInteger(Integer object) {
		list.add(object);
	}

	@JsonIgnore
	public List<Integer> getList() {
		return this.list;
	}

	@JsonIgnore
	public boolean minimum(Coincidence c) {
		boolean flag = false;
		double a = calculateDCG(this.list);
		double b = calculateDCG(c.list);
		this.quotient = (a == b) ? 1 : (a > b) ? ((a > 0) ? b / a : 0) : ((b > 0) ? a / b : 0);
		if (this.quotient > this.threshold) {
			flag = true;
		}
		return flag;
	}

	/**
	 * Function returns a boolean indicating the calculated quotient is greater than
	 * max
	 * 
	 * @param max - highest quotient//
	 * @return flag - max is greater than quotient
	 */
	@JsonIgnore
	public boolean maximum(double max) {
		boolean flag = false;
		if (this.quotient > max) {
			flag = true;
		}
		return flag;
	}
	
	@JsonIgnore
	public boolean similar(Coincidence c, double max) {
		return this.minimum(c) && this.maximum(max);
	}

	@JsonIgnore
	public double calculateDCG(List<Integer> list) {
		int integer;
		double sum = 0;
		double iteration;
		for (int i = 0; i < list.size(); i++) {
			integer = list.get(i);
			iteration = (integer / (log2(i + 2)));
			sum += iteration;
		}
		return sum;
	}

	@JsonIgnore
	public static double log2(int x) {
		return Math.log(x) / Math.log(2);
	}

	@JsonIgnore
	public String toString() {
		return this.list.toString();
	}
}
