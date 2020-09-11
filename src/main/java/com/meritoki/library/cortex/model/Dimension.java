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

import java.io.IOException;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;


public class Dimension {

	@JsonIgnore
	protected Logger logger = Logger.getLogger(Dimension.class.getName());
	@JsonProperty
	public double width;
	@JsonProperty
	public double height;
	
	public Dimension() {
		
	}
	
	public Dimension(Dimension dimension) {
		this.width = dimension.width;
		this.height = dimension.height;
	}
	
	public Dimension(double width, double height) {
		this.width = width;
		this.height = height;
	}
	
	@JsonIgnore
	public boolean equals(Dimension dimension) {
		boolean flag = false;
		if(this.width == dimension.width && this.height == dimension.height) {
			flag = true;
		}
		return flag;
	}
	
	@JsonIgnore
	@Override
	public String toString() {
		String string = "";
		ObjectWriter ow = new ObjectMapper().writer();// .withDefaultPrettyPrinter();
		try {
			string = ow.writeValueAsString(this);
		} catch (IOException ex) {
			logger.severe("IOException " + ex.getMessage());
		}
		return string;
	}
}
