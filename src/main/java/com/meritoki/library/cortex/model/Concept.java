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
