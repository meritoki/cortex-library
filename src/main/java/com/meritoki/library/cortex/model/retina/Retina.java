package com.meritoki.library.cortex.model.retina;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.meritoki.library.controller.memory.MemoryController;
import com.meritoki.library.controller.time.TimeController;
import com.meritoki.library.cortex.model.Belief;
import com.meritoki.library.cortex.model.Concept;
import com.meritoki.library.cortex.model.Point;
import com.meritoki.library.cortex.model.Node;
import com.meritoki.library.cortex.model.network.Cortex;

/**
 * Retina is a class that combines all the functions to perform a scan of an
 * image for the purpose of training and inference Retina uses a 4 dimensional
 * space to represent information in points. Each point contains a concept
 * 
 * @author jorodriguez
 *
 */
public class Retina {

	// I want bufferedimage to appear in "center" of screen;
	// has width and height, which can be used to obtain top right and left and
	// bottom right and left corners.
	// center can also be calculated.
	// by subtracting center from points, can get a new origin.
	// Can find the longest radius, sqrt(x^2 + y^2)
	// Longest radius is half of width and height that represents square containing
	// image.
	// Draw bufferedImage image with dimension from calculation
	// Draw object in bufferedImage in position centered.

	// I want points to relate to the "center",
	// the center can be set to any value and all points will be relative to it.
	// Distance is calculated between points.

	// For a given distance, which translates to a new scale, a point is modified
	// The modification includes three transformations.
	// 1. Sensor Point -> the points are pixel based and mapped to global position
	// on image @ a given scale.
	// This transformation converts the Point to be relative to the Object center.
	// 2.

	// When a new point is added it connect itself to the closet point.
	// This done through a network instead of a list to decrease runtime.
	// The root node is queried, then its children
	// Eventually a child will win that has or does not have children, but cannot
	// decrease the minDistance.
	// This is where we connect the point and add it to the list.

	public BufferedImage bufferedImage;
	public BufferedImage scaledBufferedImage;
	public double distance = 0;
	public double maxDistance;
	public double minDistance = 8;
	public Cortex cortex;
	public double focalLength = 8;
	public final double MILLIMETER = 0.2645833333;
	public double scale = 1;
	public double sensorRadius;
	public int defaultDimension = 100;
	public double x = 0;
	public double y = 0;
	public double objectRadius;
	public Point root = null;
	public Observation observation;
	public int index;
	public boolean loop = false;
	public boolean input = false;
	public boolean manual = false;

	public boolean inverted = false;
	public int maxDepth = 0;
	private LinkedList<Point> pointStack = new LinkedList<>();
	private int interval = 8;
	private int size;
	public State state;
	public List<Double> scaleList = new ArrayList<>();
	public int step = 20;

	public Retina() {
	}

	public void scan(Graphics2D graphics2D, BufferedImage bufferedImage, Cortex cortex, Concept concept) {
		MemoryController.log();
		TimeController.start();
		while (this.loop) {
			switch (this.state) {
			case NEW: {
				this.iterate(graphics2D, bufferedImage, cortex, concept);
			}
			case PENDING: {
				this.iterate(graphics2D, bufferedImage, cortex, concept);
			}
			case COMPLETE: {
				loop = false;
			}
			}
		}
		TimeController.stop();
		MemoryController.log();
	}

	public void iterate(Graphics2D graphics2D, BufferedImage bufferedImage, Cortex cortex, Concept concept) {
//		System.out.println("iterate(" + String.valueOf(graphics2D != null) + ", "
//				+ String.valueOf(bufferedImage != null) + ", " + String.valueOf(cortex != null) + ")");
		if (this.loop) {
			if (this.pointStack.size() > 0) {
//				System.out.println("this.pointStack=" + this.pointStack);
				Point point = this.pointStack.pop();
				System.out.println(point);
				this.setOrigin(point.x, point.y);
				this.input(graphics2D, concept);
				List<Point> pointList = this.traverse(point);
				for (Point p : pointList) {
					this.pointStack.push(p);
				}
				System.out.println("this.pointStack.size()=" + this.pointStack.size());
			} else {
				this.setBufferedImage(bufferedImage);
				this.setCortex(cortex);
				System.out.println("this.getMagnification()=" + this.getMagnification());
				System.out.println("this.distance=" + this.distance);
				if (this.distance == 0) {
					System.out.println("this.distance == 0");
					this.maxDistance = this.getMaxDistance();
					this.distance = this.maxDistance;
					this.size = (int) (this.getMaxDistance() - this.focalLength - this.minDistance);
					System.out.println("size=" + size);
					this.index = 0;
					this.setDistance(this.distance);
					this.input(graphics2D, concept);
//					this.pointStack.push(this.root);
					System.out.println("this.pointStack.size()=" + this.pointStack.size());
					this.state = State.NEW;
				} else {
					this.interval = this.size / this.step;
					if ((index * this.interval) < this.size) {
						System.out.println("index=" + index);
						this.distance = size;
						this.distance -= index * this.interval;
						this.index++;
						this.setDistance(this.distance);
						this.input(graphics2D, concept);
						this.pointStack.push(this.root);
						System.out.println("this.pointStack.size()=" + this.pointStack.size());
						this.state = State.PENDING;
					} else {
						this.state = State.COMPLETE;
						this.loop = false;
					}
				}
			}
		} else {
			this.setBufferedImage(bufferedImage);
			this.setCortex(cortex);
			if (bufferedImage != null && this.distance == 0) {
				this.setDistance(this.minDistance);
			} else {
				this.setDistance(this.distance);
			}
			if (this.manual) {
				this.input(graphics2D, concept);
//				this.drawPointList(graphics2D);
				this.drawPointMatrix(graphics2D);
			} else {
				this.drawScaledBufferedImage(graphics2D);
//				this.drawPointList(graphics2D);
				this.drawPointMatrix(graphics2D);
			}
		}
	}

	public List<Point> traverse(Point point) {
		List<Point> pointList = new ArrayList<>();
		List<Node> nodeList = point.getChildren();
		for (Node n : nodeList) {
			Point p = (Point) n;
			if (this.getDistance(point, p) * this.scale >= this.getSensorRadius()) {
				pointList.add(p);
			}
		}
		return pointList;
	}

	public void input(Graphics2D graphics2D, Concept concept) {
		System.out.println("input(" + String.valueOf(graphics2D != null) + ", " + concept + ")");
//		System.out.println("angle="+this.getAngle());
//		System.out.println("lensEquivalence="+this.lensEquivalence());
//		System.out.println("objectEquivalence="+this.objectEquivalence());
//		System.out.println("magnificationEquivalence="+this.magnificationEquivalence());
		this.drawScaledBufferedImage(graphics2D);
//		concept = new Concept(UUID.randomUUID().toString());
		if (!manual && this.root == null) {
			x = this.getCenterX();// this.observation.getObject().getWidth()/2;////*this.scale;
			y = this.getCenterY();// this.observation.getObject().getHeight()/2;////*this.scale;
			if (x > 0 && y > 0) {
				this.root = new Point(x / this.scale, y / this.scale);// Save root
				this.cortex.setOrigin((int) (x), (int) (y));
				this.cortex.update();
				this.cortex.process(graphics2D, scaledBufferedImage, concept);
				this.cortex.pointList.add(this.root);
				this.addPoint(this.root);
			}

		} else {
			if (input) {
				input = false;
			} else {
				x = this.getCenterX();
				y = this.getCenterY();
			}
			if (this.root == null) {
				this.root = new Point(x / this.scale, y / this.scale);
				this.cortex.pointList.add(this.root);
			}
			this.cortex.setOrigin((int) (x), (int) (y));
			this.cortex.update();
			this.cortex.process(graphics2D, scaledBufferedImage, concept);
			this.addPoint(this.root);
		}
		this.drawSensor(graphics2D);
	}

	public void drawScaledBufferedImage(Graphics2D graphics2D) {
		if (graphics2D != null)
			graphics2D.drawImage(this.scaledBufferedImage, null, null);
	}

	public void drawSensor(Graphics2D graphics2D) {
		if (graphics2D != null) {
			graphics2D.setColor(Color.BLUE);
			double r = this.getSensorRadius();
			double x = this.cortex.getX();
			double y = this.cortex.getY();
			double newX = x - r;
			double newY = y - r;
			Ellipse2D.Double ellipse = new Ellipse2D.Double(newX, newY, r * 2, r * 2);
			graphics2D.draw(ellipse);
		}
	}

	public void drawPointList(Graphics2D graphics2D) {
		if (graphics2D != null) {
			graphics2D.setColor(Color.WHITE);
			int size = this.cortex.pointList.size();
			int count = 0;
			for (Point point : this.cortex.pointList) {
				List<Node> nodeList = point.getChildren();
				for (Node n : nodeList) {
					Point child = (Point) n;
					graphics2D.setColor(this.getColor(0.8, count, size));
					graphics2D.drawLine((int) (point.x * this.scale), (int) (point.y * this.scale),
							(int) (child.x * this.scale), (int) (child.y * this.scale));
				}
				count++;
			}
		}
	}

	public void drawPointMatrix(Graphics2D graphics2D) {
		if (graphics2D != null) {
			int max = this.cortex.setPointMap(this.cortex.pointList);
//			if ((this.getMagnification() <= 1)) {
			for (int i = 0; i < this.cortex.beliefMatrix.length; i++) {
				for (int j = 0; j < this.cortex.beliefMatrix[i].length; j++) {
					Belief belief = this.cortex.beliefMatrix[i][j];
					if(belief != null) {
						int count = belief.conceptList.size();
						if (count > 0) {
							graphics2D.setColor(this.getColor(0.8, count, max));
							graphics2D.drawOval((int) (i * this.scale), (int) (j * this.scale), 2, 2);
						}
					}
				}
			}
//			}
		}
	}

	public Color getColor(double factor, double value, double size) {
		double power;
		if (inverted) {
			power = (size - value) * factor / size;
		} else {
			power = value * factor / size; // 0.9
		}
		double H = power;// * 0.4; // Hue (note 0.4 = Green, see huge chart below)
		double S = 0.9; // Saturation
		double B = 0.9; // Brightness
		Color color = Color.getHSBColor((float) H, (float) S, (float) B);
		return color;
	}

	public void setOrigin(double x, double y) {
		System.out.println("setOrigin(" + x + ", " + y + ")");
		this.x = x * this.scale;
		this.y = y * this.scale;
		this.input = true;
	}

	public void addPoint(Point root) {
		if (this.cortex != null && this.cortex.getBelief() != null) {
			List<Point> pointList = this.cortex.getBelief().pointList;
			for (Point point : pointList) {
				point.x /= this.scale;
				point.y /= this.scale;
				this.addPoint(root, point);
			}
		}
	}

	public void addPoint(Point root, Point point) {
//		System.out.println("addPoint("+root+", "+point+")");
		if (!root.equals(point)) {
			List<Node> nodeList = root.getChildren();
			double min = this.getDistance(root, point);
			Point minPoint = null;
			Iterator<Node> iterator = nodeList.iterator();
			while (iterator.hasNext()) {
				Node n = iterator.next();
				Point childPoint = (Point) n;
				double distance = this.getDistance(childPoint, point);
				if (distance < min) {
					min = distance;
					minPoint = childPoint;
				}
			}
			if (minPoint != null) {
				// System.out.println("addPoint("+root+", "+point+") minPoint="+minPoint);
				this.addPoint(minPoint, point);
			} else {
				root.addChild(point);
//				Node.printTree(root, " ");
				this.cortex.pointList.add(point);
			}
		}
	}

	public double getDistance(Point a, Point b) {
		double value = Math.sqrt(Math.pow(b.x - a.x, 2) + Math.pow(b.y - a.y, 2));
//		System.out.println("getDistance("+a+", "+b+") value="+value);
		return value;
	}

	public void setCortex(Cortex cortex) {
		this.cortex = cortex;
		this.sensorRadius = this.cortex.getSensorRadius();
		this.maxDistance = this.getMaxDistance();
		this.cortex.beliefMatrix = new Belief[this.observation.getObject().getWidth()][this.observation.getObject()
		                                                           					.getHeight()];
	}

	public void setBufferedImage(BufferedImage bufferedImage) {
		if (bufferedImage != null) {
			this.bufferedImage = bufferedImage;
			this.observation = new Observation(this.bufferedImage);
			
		} else {
			if (this.bufferedImage == null) {
				this.bufferedImage = new BufferedImage(this.defaultDimension, this.defaultDimension,
						BufferedImage.TYPE_INT_RGB);
				this.observation = new Observation(this.bufferedImage);
			}
		}
	}

	public void deltaDistance(double factor) {
		this.setDistance(this.distance + factor);
	}

	public void setDistance(double distance) {
		System.out.println("setDistance(" + distance + ")");
		this.distance = distance;// millimeter
		if (this.distance > 0) {
			this.scale = (this.getObjectHeight() / this.observation.getObject().getHeight());
			System.out.println("this.scale=" + this.scale);
			this.scaledBufferedImage = this.getScaledBufferedImage();
		}
	}

	// When distance is maxDistance, angle is formed with the point that is left,
	// right, top, bottom corners from center of object;
	// Field dimension is
	public double getAngle() {
		return 2 * Math.atan(this.toMillimeter(this.getSensorWidth()) / (2 * this.focalLength));
	}

	public double getCenterX() {
		return this.getObjectWidth() / 2;
	}

	public double getCenterY() {
		return this.getObjectHeight() / 2;
	}

	/**
	 * 
	 * @return
	 */
	public double getMaxDistance() {
		return (this.toMillimeter(this.observation.getObject().getHeight()) * this.focalLength)
				/ this.toMillimeter(this.getSensorWidth());
	}

	public double getFieldWidth() {
		return (this.getSensorWidth() * distance) / focalLength;
	}

	public double getFieldHeight() {
		return (this.getSensorHeight() * distance) / focalLength;
	}

	public double getSensorRadius() {
//		System.out.println("getSensorRadius() this.sensorRadius="+this.sensorRadius);
		return this.sensorRadius;
	}

	public double getSensorWidth() {
		return this.sensorRadius * Math.sqrt(2);
	}

	public double getSensorHeight() {
		return this.sensorRadius * Math.sqrt(2);
	}

	public double getObjectHeight() {
		return (this.observation != null) ? this.toPixel(
				(this.focalLength * this.toMillimeter(this.observation.getObject().getHeight())) / this.distance) : 0;
	}

	public double getObjectWidth() {
		return (this.observation != null)
				? this.toPixel(
						(this.focalLength * this.toMillimeter(this.observation.getObject().getWidth())) / this.distance)
				: 0;
	}

	public boolean lensEquivalence() {
		double a = (this.getSensorWidth()) / this.focalLength;
		double b = this.getFieldHeight() / this.distance;
//		System.out.println("lenEquivalence() a="+a+" b="+b);
		return a == b;
	}

	public boolean objectEquivalence() {
		return this.toMillimeter(this.getObjectHeight())
				/ this.focalLength == (this.toMillimeter(this.observation.getObject().getHeight())) / this.distance;
	}

	public boolean magnificationEquivalence() {
		NumberFormat formatter = new DecimalFormat("#0.00");
		double a = this.getMagnification();
		double b = this.getObjectHeight() / this.getSensorObjectHeight();
//		System.out.println("magnificationEquivalence() a="+a+" b="+b);
		return formatter.format(a).equals(formatter.format(b));
	}

	public double getMagnification() {
		return this.distance / this.focalLength;
	}

	public double getSensorObjectWidth() {
		return this.toPixel(this.toMillimeter(this.getObjectWidth()) / this.getMagnification());
	}

	public double getSensorObjectHeight() {
		return this.toPixel(this.toMillimeter(this.getObjectHeight()) / this.getMagnification());
	}

	@JsonIgnore
	public BufferedImage getScaledBufferedImage() {
		BufferedImage before = this.observation.getObject();// this.bufferedImage;
		BufferedImage after = new BufferedImage((int) (before.getWidth() * this.scale),
				(int) (before.getHeight() * this.scale), BufferedImage.TYPE_INT_RGB);// new
																						// BufferedImage(before.getWidth(),
																						// before.getHeight(),
																						// BufferedImage.TYPE_INT_RGB);
		AffineTransform affineTransform = new AffineTransform();
		affineTransform.scale(this.scale, this.scale);// this handles scaling the bufferedImage
		AffineTransformOp affineTransformOp = new AffineTransformOp(affineTransform, AffineTransformOp.TYPE_BILINEAR);
		after = affineTransformOp.filter(before, after);
		return after;
	}

	public double toMillimeter(double value) {
		return value * MILLIMETER;
	}

	public double toPixel(double value) {
		return value / MILLIMETER;
	}
}

//this.setDistance(16);
//System.out.println("scale="+scale);
//System.out.println(this.getObjectHeightMM());
//System.out.println(this.getObjectWidthMM());
//System.out.println(this.getObjectHeight());
//System.out.println(this.getObjectWidth());
//this.dimension  = new Dimension(this.getObjectWidth(),this.getObjectHeight());
//this.scale = (this.getObjectHeight()<this.bufferedImage.getHeight())?this.getObjectHeight()/this.bufferedImage.getHeight():this.bufferedImage.getHeight()/this.getObjectHeight();
//this.scale = (this.getObjectHeight()/this.bufferedImage.getHeight());
//System.out.println("this.scale="+this.scale);

//this.cortex.setOrigin(this.scaledBufferedImage.getWidth()/2, this.scaledBufferedImage.getHeight()/2);
//this.cortex.update();

//public void scan(Graphics graphics) {
//Graphics2D graphics2D = (Graphics2D) graphics.create();
//if (this.distance == 0) {
//	this.distance = this.getMaxDistance();
//	this.setDistance(this.distance);
//} else { 
//	this.setDistance(this.distance);
//}
//System.out.println("angle="+this.getAngle());
//System.out.println("lensEquivalence="+this.lensEquivalence());
//System.out.println("objectEquivalence="+this.objectEquivalence());
//System.out.println("magnificationEquivalence="+this.magnificationEquivalence());
//graphics2D.drawImage(this.scaledBufferedImage, null, null);
//Concept concept = new Concept(UUID.randomUUID().toString());
//
//if(this.root == null) {
//	x = this.getCenterX();///this.scale;
//	y = this.getCenterY();///this.scale;
//	this.root = new Point(x,y);//Save root 
//	this.pointList.add(this.root);
//	this.cortex.setOrigin((int)(x), (int)(y));
//	this.cortex.update();
//	this.cortex.process(graphics2D, scaledBufferedImage, concept);
//
//} else {
//	x = this.getCenterX();
//	y = this.getCenterY();
//	this.cortex.setOrigin((int)(x), (int)(y));
//	this.cortex.update();
//	this.cortex.process(graphics2D, scaledBufferedImage, concept);
//}
//List<Point> pointList = this.cortex.getPointList();
////this.root.x*=this.scale;
////this.root.y*=this.scale;
//for(Point p: pointList) {
//	p.x /= this.scale;
//	p.y /= this.scale;
//	Point point = new Point(p.x,p.y);
//	this.addPoint(this.root, point);
//}
//graphics2D.setColor(Color.BLUE);
//double r = this.getSensorRadius();
//double x = this.cortex.getX();
//double y = this.cortex.getY();
//double newX = x - r;
//double newY = y - r;
//System.out.println("r="+r+" x="+x+" y="+y);
//Ellipse2D.Double ellipse = new Ellipse2D.Double(newX, newY, r * 2, r * 2);
//graphics2D.draw(ellipse);
//
//graphics2D.setColor(Color.RED);
//for(Point point: this.pointList) {
//	List<Node> nodeList = point.getChildren();
//	for(Node n: nodeList) {
//		Point child = (Point) n;
//		graphics2D.drawLine((int)(point.x*this.scale), (int)(point.y*this.scale), (int)(child.x*this.scale), (int)(child.y*this.scale));	
//	}
//	
//}
//}

//Iterator<Point> iterator = new ArrayList(this.pointList).iterator();
//System.out.println(this.pointList.size());
//while(iterator.hasNext()) {
//	Point p = iterator.next();
//	this.cortex.setOrigin((int)(p.x), (int)(p.y));
//	this.cortex.update();
//	this.cortex.process(graphics2D, scaledBufferedImage, concept);
//	this.addPoint(this.root);
//}

//if (point.addScale < this.scale) {
//System.out.println(point);
//this.setOrigin(point.x, point.y);
//this.input(graphics2D,concept);
//List<Node> nodeList = point.getChildren();
//double minDistance = this.getSensorRadius();
//for (Node n : nodeList) {
//	Point p = (Point) n;
//	if (this.getDistance(point, p) > minDistance) {
//		System.out.println("node point" + p);
//		pointStack.push(p);
//	}
//}
//}
//if(point.getChildren().size() == 0) {
//
//} else {
//this.drawScaledBufferedImage(graphics2D);
//}

//public int getDepth() {
//int max = 0;
////for()
//}

//public double getRandom() {
//int max = 2;
//int min = 0;
//int value = 2;
//int random;
////random = min + (int)(Math.random() * ((max - min) + 1));
////if(random == value) {
////	Point p = (Point) n;
////	System.out.println("node point" + p);
////	pointStack.push(p);
////}
//}

//public LinkedList<Point> getPointList() {
//	LinkedList<Point> pointList = new LinkedList<>();
//	List<Node> nodeList = this.root.getChildren();
//	for (Node n : nodeList) {
//		Point p = (Point) n;
//		pointList.add(p);
//	}
//	return pointList;
//}
//public LinkedList<Point> getPointStack(int factor) {
//	LinkedList<Point> pointList = new LinkedList<>();
//	for (Point p : this.pointList) {
//		if (p.getChildren().size() == factor) {
//			pointList.add(p);
//		}
//	}
//	return pointList;
//}
