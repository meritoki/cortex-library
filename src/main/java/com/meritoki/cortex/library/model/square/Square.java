package com.meritoki.cortex.library.model.square;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.meritoki.cortex.library.model.Point;
import com.meritoki.cortex.library.model.Shape;

public class Square extends Shape {
	@JsonIgnore
	private static Logger logger = LogManager.getLogger(Square.class.getName());

	public Square() {
		super(4,45,0,0,new Point(0,0),1);
	}

	public Square(Square square) {
		super(4,45,square.getX(), square.getY(), square.getCenter(), square.getRadius());
	}
	
	public Square(Shape shape) {
		super(4,45,shape.getX(), shape.getY(), shape.getCenter(), shape.getRadius());
	}

	public Square(int x, int y, Point center, double radius) {
		super(4,45,x,y,center,radius);
	}
}
