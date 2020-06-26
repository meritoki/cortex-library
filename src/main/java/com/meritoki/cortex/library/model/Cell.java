package com.meritoki.cortex.library.model;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Cell {
	private static Logger logger = LogManager.getLogger(Cell.class.getName());
	public int x;
	public int y;
	public Integer red = 0;
	public Integer green = 0;
	public Integer blue = 0;

	public String rgbToString(Integer red, Integer green, Integer blue) {
		if (red == null) {
			red = 0;
		}
		if (green == null) {
			green = 0;
		}
		if (blue == null) {
			blue = 0;
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