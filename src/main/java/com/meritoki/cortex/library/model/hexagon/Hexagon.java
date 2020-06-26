package com.meritoki.cortex.library.model.hexagon;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.meritoki.cortex.library.model.Point;
import com.meritoki.cortex.library.model.Shape;

public class Hexagon extends Shape {

	@JsonIgnore
	private static Logger logger = LogManager.getLogger(Hexagon.class.getName());
	
	public Hexagon() {
		super(6,90,0,0,new Point(0,0),1);
	}

	public Hexagon(Hexagon hexagon) {
		super(6,90,hexagon.getX(), hexagon.getY(), hexagon.getCenter(), hexagon.getRadius());
	}
	
	public Hexagon(Shape shape) {
		super(6,90,shape.getX(), shape.getY(), shape.getCenter(), shape.getRadius());
	}

	public Hexagon(int x, int y, Point center, int radius) {
		super(6,90,x,y,center,radius);
	}
}