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

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import java.util.logging.Logger;

public class Cell {
	protected static Logger logger = LoggerFactory.getLogger(Cell.class.getName());
	public int x;
	public int y;
	public Integer red = 0;
	public Integer green = 0;
	public Integer blue = 0;

	public String rgbToString(Long red, Long green, Long blue) {
		if (red == null) {
			red = (long) 0;
		}
		if (green == null) {
			green = (long) 0;
		}
		if (blue == null) {
			blue = (long) 0;
		}
		return "(" + red + "," + green + "," + blue + ")";
	}

	public int getTotal(Map<String, Integer> map) {
		int sum = 0;
		if (map != null) {
			for (Integer i : map.values()) {
				sum += i;
			}
		}
		return sum;
	}

	public void input(Integer red, Integer green, Integer blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
	}
}