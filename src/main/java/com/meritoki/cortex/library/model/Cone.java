package com.meritoki.cortex.library.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Cone extends Cell {
	private static Logger logger = LogManager.getLogger(Cone.class.getName());
	public Wavelength wavelength = Wavelength.SHORT;
	
	public Cone(Wavelength wavelength) {
		this.wavelength = wavelength;
	}
	
	public void input(int color) {
//		logger.info("input("+color+")");
		int blue = color & 0xff;
		int green = (color & 0xff00) >> 8;
		int red = (color & 0xff0000) >> 16;		
		switch(wavelength) {
		case SHORT: {
			this.input(0, 0, blue);
			break;
		}
		case MEDIUM: {
			this.input(0,green,0);
			break;
		}
		case LONG: {
			this.input(0,0,blue);
			break;
		}
		default: {
			
		}
		}
	}
}
