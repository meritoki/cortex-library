package com.meritoki.library.cortex.model.retina;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import com.meritoki.library.cortex.model.Point;

public class Observation {

	public BufferedImage bufferedImage;
	public double radius = 0;
	public BufferedImage object;

	public Observation(BufferedImage bufferedImage) {
		this.bufferedImage = bufferedImage;
		this.radius = this.getMaxRadius();
		this.object = this.getDefaultObject();
	}
	
	public List<Point> getCornerList() {
		List<Point> cornerList = new ArrayList<>();
		Point topLeft = new Point(0,0);
		Point topRight = new Point(this.bufferedImage.getWidth(),0);
		Point bottomLeft = new Point(0,this.bufferedImage.getHeight());
		Point bottomRight = new Point(this.bufferedImage.getWidth(),this.bufferedImage.getHeight());
		cornerList.add(topLeft);
		cornerList.add(topRight);
		cornerList.add(bottomLeft);
		cornerList.add(bottomRight);
		return cornerList;
	}
	
	public List<Point> getCenteredCornerList() {
		List<Point> cornerList = this.getCornerList();
		Point center = this.getCenter(this.bufferedImage);
		for(Point corner:cornerList) {
			corner.x -= center.x;
			corner.y -= center.y;
		}
		return cornerList;
	}
	
	public double getMaxRadius() {
		List<Point> cornerList = this.getCenteredCornerList();
		double max = 0;
		for(Point p: cornerList) {
			double radius = Math.sqrt(Math.pow(p.x, 2)+Math.pow(p.y, 2));
			if(radius > max) {
				max = radius;
			}
		}
		return max;
	}
	
	public double getWidth() {
		return 2*this.radius;
	}
	
	public double getHeight() {
		return 2*this.radius;
	}
	
	public Point getCenter() {
		Point center = new Point(this.getWidth()/2,this.getHeight()/2);
		return center;
	}
	
	public Point getCenter(BufferedImage bufferedImage) {
		Point center = new Point(bufferedImage.getWidth()/2,bufferedImage.getHeight()/2);
//		System.out.println("getCenter() center="+center);
		return center;
	}
	
	public BufferedImage getObject() {
		return this.object;
	}
	
	public BufferedImage getDefaultObject() {
		int width = (int)this.getWidth();
		int height = (int)this.getHeight();
		Point center = this.getCenter();
		BufferedImage b = new BufferedImage((int)(width), (int)(height),BufferedImage.TYPE_INT_RGB);
		Graphics g = b.createGraphics();
		Point bufferedImageCenter = this.getCenter(this.bufferedImage);
//		System.out.println("bufferedImageCenter="+bufferedImageCenter);
		int x = (int)(center.x-bufferedImageCenter.x);//(int)((width/2)-(center.x-bufferedImageCenter.x));
		int y = (int)(center.y-bufferedImageCenter.y);// ;//(int)((height/2)-
//		System.out.println("difference x="+x+" y="+y);
//		System.out.println("this.bufferedImage.getWidth()="+this.bufferedImage.getWidth()+" this.bufferedImage.getHeight()="+this.bufferedImage.getHeight());
		g.drawImage(this.bufferedImage,x,y,this.bufferedImage.getWidth(),this.bufferedImage.getHeight(),null);
//		g.drawRect(x, y, this.bufferedImage.getWidth(), this.bufferedImage.getHeight());
//		System.out.println("getObject() b.getWidth()="+b.getWidth()+" b.getHeight()="+b.getHeight());
		return b;
	}
}
