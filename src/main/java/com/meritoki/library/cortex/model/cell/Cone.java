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

import com.meritoki.library.cortex.model.Wavelength;

public class Cone extends Cell {
	
	public Wavelength wavelength = Wavelength.SHORT;
	
	public Cone(Wavelength wavelength) {
		this.wavelength = wavelength;
	}
	
	public void input(long color) {
//		logger.info("input("+color+")");
		long blue = color & 0xff;
		long green = (color & 0xff00) >> 8;
		long red = (color & 0xff0000) >> 16;		
		switch(wavelength) {
		case SHORT: {
			this.input((int)red, 0, 0);
			break;
		}
		case MEDIUM: {
			this.input(0,(int)green,0);
			break;
		}
		case LONG: {
			this.input(0,0,(int)blue);
			break;
		}
		default: {
			
		}
		}
	}
}
