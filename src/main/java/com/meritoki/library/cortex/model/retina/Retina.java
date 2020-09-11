package com.meritoki.library.cortex.model.retina;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.meritoki.library.cortex.model.Cortex;
import com.meritoki.library.cortex.model.Dimension;

public class Retina {
	
	
	//Process the output of the cortexMap into input for the shapeMap
	//Has some configuration involved.
	
	//Must determine how to process default size of cortex into size of the larger cortex. 
	
	//default one cortex per shape, then two, three.
	
	public BufferedImage bufferedImage;
	public BufferedImage scaledBufferedImage;
	public double maxDistance;
	public double minDistance = 16;
	public double distance = 8;
	public Cortex cortex;
	public double focalLength = 8;
	public final double  MILLIMETER = 0.2645833333;
	public double scale = 0;
	public double sensorRadius;
	public int defaultDimension = 100;
	
	public Retina(BufferedImage bufferedImage, Cortex cortex) {
		this.bufferedImage = bufferedImage;
		if(this.bufferedImage == null) {
			this.bufferedImage = new BufferedImage(defaultDimension, defaultDimension, BufferedImage.TYPE_INT_RGB);
		}
		this.cortex = cortex;
		this.sensorRadius = this.cortex.getSensorRadius();
//		this.maxDistance = (int)this.getMaxDistance();
		this.setDistance(this.getMaxDistance());
//		this.setDistance(16);
//		System.out.println("scale="+scale);
//		System.out.println(this.getObjectHeightMM());
//		System.out.println(this.getObjectWidthMM());
//		System.out.println(this.getObjectHeight());
//		System.out.println(this.getObjectWidth());
//		this.dimension  = new Dimension(this.getObjectWidth(),this.getObjectHeight());
//		this.scale = (this.getObjectHeight()<this.bufferedImage.getHeight())?this.getObjectHeight()/this.bufferedImage.getHeight():this.bufferedImage.getHeight()/this.getObjectHeight();
//		this.scale = (this.getObjectHeight()/this.bufferedImage.getHeight());
//		System.out.println("this.scale="+this.scale);
		
//		this.cortex.setOrigin(this.scaledBufferedImage.getWidth()/2, this.scaledBufferedImage.getHeight()/2);
//		this.cortex.update();
	}
	
	public void setBufferedImage(BufferedImage bufferedImage) {
		this.bufferedImage = bufferedImage;
	}
	
	public void setDistance(double distance) {
		this.distance = distance;
		double scale = (this.getObjectHeight()/this.bufferedImage.getHeight());
		if(scale != this.scale) {
			this.scale = scale;
			this.scaledBufferedImage = this.getScaledBufferedImage();
		}
	}
	
	
	public double getMaxDistance() {
		return (this.bufferedImage.getHeight() * this.focalLength)/this.getSensorWidthMM();
	}
	
	public double fieldWidth() {
		return (this.getSensorWidth()*distance)/focalLength;
	}
	
	public double fieldHeight() {
		return (this.getSensorHeight()*distance)/focalLength;
	}
	
	public double getSensorRadius() {
		return this.sensorRadius;
	}
	
	public double getSensorWidth() {
		return this.sensorRadius*Math.sqrt(2);
	}
	
	public double getSensorHeight() {
		return this.sensorRadius*Math.sqrt(2);
	}
	
	public double getSensorWidthMM() {
		
		return getSensorWidth()*MILLIMETER;
	}
	
	public double getSensorHeightMM() {
		return getSensorHeight() *MILLIMETER;
	}
	
	public double getObjectHeightMM() {
		return (this.focalLength*this.bufferedImage.getHeight())/this.distance;
	}
	
	public double getObjectWidthMM() {
		return (this.focalLength*this.bufferedImage.getWidth())/this.distance;
	}
	
	public double getObjectHeight() {
		return this.getObjectHeightMM()/MILLIMETER;
	}
	
	public double getObjectWidth() {
		return this.getObjectWidthMM()/MILLIMETER;
	}
	
	public boolean equivalence() {
		return this.getObjectHeightMM()/this.focalLength == (this.bufferedImage.getHeight())/this.distance;
	}
	
	public double getMagnification() {
		return this.distance/this.focalLength;
	}
	
	public boolean magnificationEquivalence() {
		return this.getMagnification() == this.getObjectHeightMM();
	}
	
	@JsonIgnore
	public BufferedImage getScaledBufferedImage() {
		BufferedImage before = this.bufferedImage;
		BufferedImage after = new BufferedImage((int)(before.getWidth()*this.scale), (int)(before.getHeight()*this.scale), BufferedImage.TYPE_INT_RGB);//new BufferedImage(before.getWidth(), before.getHeight(), BufferedImage.TYPE_INT_RGB);
		AffineTransform affineTransform = new AffineTransform();
		affineTransform.scale(this.scale, this.scale);// this handles scaling the bufferedImage
		AffineTransformOp affineTransformOp = new AffineTransformOp(affineTransform, AffineTransformOp.TYPE_BILINEAR);
		after = affineTransformOp.filter(before, after);
		return after;
	}
}
