package com.meritoki.vision.library.model;

public class Saccade {

	private double width;
	private double height;
	private double scale;
	private int i = 0;
	private int j = 0;
	
	public Saccade(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
	public void setScale(double scale) {
		this.scale = scale;
	}
	
	public Point getPoint() {
		Point point = null;
//		for(i ; i < width; i++) {
//			for(j = 0; j < height; j++) {
//			  point = new Point(i,j);
//			  break;
//			}
//		}
//		double w = (Math.random()*((this.width*this.scale-0)+1)); 
//		double h = (Math.random()*((this.height*this.scale-0)+1));
		return point;
	}
}
