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
package com.meritoki.library.cortex.model.cell;

import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Cell {
	@JsonIgnore
	protected Logger logger = Logger.getLogger(Cell.class.getName());
	public int x;
	public int y;
	public Integer red = 0;
	public Integer green = 0;
	public Integer blue = 0;
	
	public void input(long color, Wavelength[] wavelength) {
		logger.fine("input("+color+")");
		long blue = color & 0xff;
		long green = (color & 0xff00) >> 8;
		long red = (color & 0xff0000) >> 16;
		for(Wavelength w: wavelength) {
			this.setWavelength(w,(int) red,(int) green,(int) blue);
		}
	}
	
	public boolean containsWavelength(Wavelength w, Wavelength[] wArray) {
		for(Wavelength wavelength: wArray) {
			if(wavelength == w) {
				return true;
			}
		}
		return false;
	}
	
	public void input(Integer red, Integer green, Integer blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
	}
	
	public void setWavelength(Wavelength w, Integer red, Integer green, Integer blue) {
		switch (w) {
		case CONE_SHORT: {
			this.red = red;
			break;
		}
		case CONE_MEDIUM: {
			this.green = green;
			break;
		}
		case CONE_LONG: {
			this.blue = blue;
			break;
		}
		case ROD_GRAY: {
			this.red = (int)(red+green+blue)/3;
			this.green = (int)(red+green+blue)/3;
			this.blue = (int)(red+green+blue)/3;
			break;
		}
		default: {
			
		}
		}
	}
	
	public Integer getWavelength(Wavelength w) {
		Integer value = 0;
		switch (w) {
		case CONE_SHORT: {
			value = (int) red;
			break;
		}
		case CONE_MEDIUM: {
			value = (int) green;
			break;
		}
		case CONE_LONG: {
			value = (int) blue;
			break;
		}
		case ROD_GRAY: {
			value = (int)(red+green+blue)/3;
			break;
		}
		default: {
			value = 0;
		}
		}
		return value;
	}
}