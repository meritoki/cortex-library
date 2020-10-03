package com.meritoki.library.cortex.model.motor;

import com.meritoki.library.cortex.model.Point;
import com.meritoki.library.cortex.model.retina.State;

public class Delta {
	public Point start;
	public Point stop;
	public int index;
	private int interval = 8;
	private int size;
	public int step = 8;
	public State state = State.NEW;
	
	public Delta(Point start, Point stop) {
		this.start = start;
		this.stop = stop;
	}
	
	public void iterate() {
		
	}
	
	public double getSlope() {
		return (stop.y-start.y)/(stop.x-start.y);
	}
	
	public double getYIntercept(Point point) {
		return (point.y - (this.getSlope()*point.x));
	}
	
	public String toString() {
		return start+":"+stop;
	}
}
