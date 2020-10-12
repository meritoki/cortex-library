package com.meritoki.library.cortex.model.retina;

import java.awt.Color;
import java.awt.Dimension;
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
import com.meritoki.library.cortex.model.Matrix;
import com.meritoki.library.cortex.model.Point;
import com.meritoki.library.cortex.model.cortex.Cortex;
import com.meritoki.library.cortex.model.motor.Delta;
import com.meritoki.library.cortex.model.motor.Motor;
import com.meritoki.library.cortex.model.Node;

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

	public final int DIMENSION = 100;
	public Dimension dimension;

	public Cortex cortex;
	public Motor motor = new Motor();

	public BufferedImage object;
	public BufferedImage bufferedImage;
	public BufferedImage inputBufferedImage;// Object should be what is input into cortex

	public double focalLength = 8; // mm
	public double minDistance = 8;// mm
	public double distance = minDistance;
	public double maxDistance;

	public double scale = 1;
	public double radius = 0;
	public double cortexRadius;

	public Point origin = new Point(0, 0);
	public Point previous;

	public int index;
	private int interval = 8;
	private int size;
	public int step = 16;
	public State state = State.NEW;

	public Retina() {
	}

	public void setDimension(Dimension dimension) {
		System.out.println("setDimension(" + dimension + ")");
		this.dimension = dimension;
		this.inputBufferedImage = this.getInputBufferedImage();
		Point origin = this.getInputCenter();
		this.setOrigin(origin);
	}

	// Called multiple times
	public void setCortex(Cortex cortex) {
		this.cortex = cortex;
		this.cortexRadius = this.cortex.getRadius();
		this.maxDistance = this.getMaxDistance();
		this.motor.setCortex(this.cortex);
	}

	// Called Multiple times
	public void setBufferedImage(BufferedImage bufferedImage) {
		if (bufferedImage != null) {
			if (this.bufferedImage != bufferedImage) {
				this.bufferedImage = bufferedImage;
				this.radius = this.getMaxRadius();
				this.object = this.getObject();
			}
		} else {
			this.bufferedImage = new BufferedImage(this.DIMENSION, this.DIMENSION, BufferedImage.TYPE_INT_RGB);
			this.radius = this.getMaxRadius();
			this.object = this.getObject();
		}
	}

	public void setOrigin(Point origin) {
//		System.out.println("setOrigin(" + origin + ")");
		this.previous = this.origin;
		if (this.previous == null) {
			this.previous = this.getInputCenter();
			this.previous.center = true;
		}
		this.origin = origin;
//		System.out.println("setOrigin(" + origin + ") origin.center=" + this.origin.center);
	}

	public void setDistance(double distance) {
		System.out.println("setDistance(" + distance + ")");
		// this.previousDistance = this.distance;
		this.distance = distance;// millimeter
		if (this.distance > 0) {
			this.scale = (this.getObjectHeight() / this.object.getHeight());
			// System.out.println("this.scale=" + this.scale);
			this.inputBufferedImage = this.getInputBufferedImage();
		}
	}

	public void scan(Graphics2D graphics2D, BufferedImage bufferedImage, Cortex cortex, Concept concept) {
		MemoryController.log();
		TimeController.start();
		while (this.state != State.COMPLETE) {
			this.iterate(graphics2D, bufferedImage, cortex, concept);
		}
		TimeController.stop();
		MemoryController.log();
	}

	/**
	 * iterate
	 * 
	 * @param graphics2D
	 * @param bufferedImage
	 * @param cortex
	 * @param concept
	 */
	public void iterate(Graphics2D graphics2D, BufferedImage bufferedImage, Cortex cortex, Concept concept) {
//		System.out.println("iterate(" + String.valueOf(graphics2D != null) + ", "
//				+ String.valueOf(bufferedImage != null) + ", " + String.valueOf(cortex != null) + ")");
		Delta delta = this.motor.getDelta();
		if (delta != null) {
			System.out.println("iterate(...) delta=" + delta);
			this.setOrigin(delta.stop);
			this.input(graphics2D, bufferedImage, cortex, concept);
		} else {
			this.setBufferedImage(bufferedImage);// DEFECT
			this.setCortex(cortex);// DEFECT
			System.out.println("this.distance=" + this.distance);
			if (this.distance == 0) {
				this.maxDistance = this.getMaxDistance();
				this.distance = this.maxDistance;
				this.size = (int) (this.getMaxDistance() - this.focalLength - this.minDistance);
				this.index = 0;
				Point origin = this.getInputCenter();
				this.setOrigin(origin);
				this.input(graphics2D, bufferedImage, cortex, concept);
				this.state = State.PENDING;
			} else {
				this.interval = this.size / this.step;
				if ((index * this.interval) < this.size) {
					System.out.println("index=" + index);
					this.distance = size;
					this.distance -= index * this.interval;
					this.index++;
					Point origin = this.getInputCenter();
					this.setOrigin(origin);
					this.input(graphics2D, bufferedImage, cortex, concept);
					this.state = State.PENDING;
				} else {
					this.state = State.COMPLETE;
				}
			}
		}
	}

	/**
	 * Usually when you have a function, you pass the objects it needs and cannot
	 * get globally. Retina is a strange object, with bad design.
	 * 
	 * @param graphics2D
	 * @param bufferedImage
	 * @param cortex
	 * @param concept
	 */
	public void input(Graphics2D graphics2D, BufferedImage bufferedImage, Cortex cortex, Concept concept) {
//		System.out.println("input(" + String.valueOf(graphics2D != null) + ", " + concept + ")");
		this.setBufferedImage(bufferedImage);// DEFECT
		this.setCortex(cortex);// DEFECT
		this.setDistance(this.distance);
		this.drawInputBufferedImage(graphics2D);
		this.cortex.setOrigin((int) (origin.x), (int) (origin.y));// Origin is used;
		this.cortex.update();
		this.inputBufferedImage = this.getInputBufferedImage();
		this.cortex.process(graphics2D, this.inputBufferedImage, concept);
		this.processBelief();
		this.motor.input(this.getInputCenter(), origin, this.scale);
		concept = (this.cortex.getBelief().conceptList.size() > 0) ? this.cortex.getBelief().conceptList.get(0) : null;
		this.drawGlobalBeliefList(graphics2D, concept);
//			this.drawRelativeBeliefList(graphics2D);
		this.drawInputCenter(graphics2D);
//			this.drawCortexPointList(graphics2D);
		this.drawMotor(graphics2D);
		this.drawCortex(graphics2D);
		this.drawOrigin(graphics2D);

	}

	/**
	 * Consider moving this implementation into Motor and calling Motor input(...)
	 * instead of processBelief() from input(...) Deltas are still handled in
	 * iterate(...)
	 */
	public void processBelief() {
		Belief belief = this.cortex.getBelief();
		if (belief != null) {

			belief.normalize(this.scale);// Point List is centered around 0,0
			belief.setGlobal(this.getInputCenter(), this.scale);
			belief.setRelative(this.scale, this.origin, this.previous);
			// Now we have a belief in global coordinates that can be scaled
			// and visualized.
			// We lost the function to view a belief relative to the center
			// Here the belief is now around 0,0;
			// What we had before was the the belief would be mapped relative to this.origin
			// which can be anywhere,
			// The easiest solution to this problem is the following:
			// 1) Make Point List relative to 0,0 so that any origin can be applied.
			// 2) Give belief two origins, one is the global origin, the other is the
			// relative origin.
			// To draw global and points must be translated
			// to the center of the screen.
			// Now we are focused on the root Point.
			belief.global.round();
//			if (belief.pointList.size() > 0) {
//				this.cortex.addPoint(this.cortex.root, belief.global);
				for (Point point : belief.getGlobalPointList()) {
//					this.cortex.addPoint(belief.global, point);
					this.cortex.addPoint(this.cortex.root, point);
				}
//			}
//			this.cortex.addRelativePoint(this.cortex.relative, belief.global);
//			for (Point point : belief.getGlobalPointList()) {
//				this.cortex.addRelativePoint(belief.global, point);
//			}
//			this.cortex.add(belief);//Entry into Mind radius system.
//			this.cortex.traverseInOrder(this.cortex.mind);
		}
	}

	public void drawCortexPointList(Graphics2D graphics2D) {
		if (graphics2D != null) {
			graphics2D.setColor(Color.WHITE);
			int size = this.cortex.pointList.size();
			int count = 0;
			double x = this.getInputCenterX();// this.origin.x;
			double y = this.getInputCenterY();// this.origin.y;
			for (Point point : this.cortex.pointList) {
				List<Node> nodeList = point.getChildren();
				point = new Point(point);
				point.x *= this.scale;
				point.y *= this.scale;
				point.x += x;
				point.y += y;
				for (Node n : nodeList) {
					Point child = (Point) n;
					child = new Point(child);
					child.x *= this.scale;
					child.y *= this.scale;
					child.x += x;
					child.y += y;
					graphics2D.setColor(this.getColor(0.8, count, size));
					graphics2D.drawLine((int) (point.x), (int) (point.y), (int) (child.x), (int) (child.y));
				}
				count++;
			}
		}
	}

	public void drawMotor(Graphics2D graphics2D) {
		if (graphics2D != null) {
			this.motor.paint(graphics2D);
//			int size = this.cortex.pointList.size();
//			int count = 0;
//			double x = this.getInputCenterX();//this.origin.x;
//			double y = this.getInputCenterY();//this.origin.y;
//			for (Point point : this.cortex.pointList) {
//				List<Node> nodeList = point.getChildren();
//				point = new Point(point);
//				point.x *= this.scale;
//				point.y *= this.scale;
//				point.x += x;
//				point.y += y;
//				for (Node n : nodeList) {
//					Point child = (Point) n;
//					child = new Point(child);
//					child.x *= this.scale;
//					child.y *= this.scale;
//					child.x += x;
//					child.y += y;
//					graphics2D.setColor(this.getColor(0.8, count, size));
//					graphics2D.drawLine((int) (point.x), (int) (point.y), (int) (child.x), (int) (child.y));
//				}
//				count++;
//			}
//
//			graphics2D.setColor(Color.BLACK);
//			List<Point> pList = new ArrayList<>();
//			for (Point p : this.cortex.pointList) {
//				p = new Point(p);
//				p.x *= this.scale;
//				p.y *= this.scale;
//				p.x += this.getInputCenterX();
//				p.y += this.getInputCenterY();
////				ellipse = new Ellipse2D.Double(p.x, p.y, 2, 2);
////				graphics2D.draw(ellipse);
//				pList.add(p);
//			}
//
//			Matrix matrix = new Matrix(pList, 4 * this.scale);
//			List<ArrayList<Point>> rowList = matrix.getRowList();
//			for (int i = 0; i < rowList.size(); i++) {
//				List<Point> pointList = rowList.get(i);
//				Point previous = null;
//				Point current;
//				for (int j = 0; j < pointList.size(); j++) {
//					current = pointList.get(j);
////					current.x *= this.scale;
////					current.y *= this.scale;
////					current.x += x;
////					current.y += y;
//					if (previous != null) {
//						graphics2D.drawLine((int) (current.x), (int) (current.y), (int) (previous.x),
//								(int) (previous.y));
//					}
//					previous = current;
//				}
//			}
		}
	}

	public void drawGlobalBeliefList(Graphics2D graphics2D, Concept concept) {
//		System.out.println("drawGlobalBeliefList(...)");
		if (graphics2D != null) {

			double x = this.getInputCenterX();// this.origin.x;
			double y = this.getInputCenterY();// 0;//this.origin.y;
			for (int i = 0; i < this.cortex.beliefList.size(); i++) {// Belief belief : this.cortex.beliefList) {
				Belief belief = this.cortex.beliefList.get(i);
//				System.out.println(belief);
				if (belief.contains(concept)) {
					graphics2D.setColor(Color.BLACK);
				} else {
					graphics2D.setColor(this.getColor(0.8, i, this.cortex.beliefList.size()));
				}
				Point point = new Point(belief.global);
				double r = belief.getRadius() * this.scale;
				point.x *= this.scale;
				point.y *= this.scale;
				point.x += x;
				point.y += y;
//				double x = belief.origin.x * this.scale;
//				double y = belief.origin.y * this.scale;
				double newX = point.x - r;
				double newY = point.y - r;
				Ellipse2D.Double ellipse = new Ellipse2D.Double(newX, newY, r * 2, r * 2);
				graphics2D.draw(ellipse);

			}
		}
	}

	public void drawRelativeBeliefList(Graphics2D graphics2D) {
//		System.out.println("drawRelativeBeliefList(...)");
		if (graphics2D != null) {
			graphics2D.setColor(Color.YELLOW);
			double x = this.origin.x;
			double y = this.origin.y;
			for (Belief belief : this.cortex.beliefList) {
//				System.out.println(belief);
				Point point = new Point(belief.relative);
				double r = belief.getRadius() * this.scale;
				point.x *= this.scale;
				point.y *= this.scale;
				point.x += x;
				point.y += y;
//				double x = belief.origin.x * this.scale;
//				double y = belief.origin.y * this.scale;
				double newX = point.x - r;
				double newY = point.y - r;
				Ellipse2D.Double ellipse = new Ellipse2D.Double(newX, newY, r * 2, r * 2);
				graphics2D.draw(ellipse);

			}
		}
	}

	public void drawInputBufferedImage(Graphics2D graphics2D) {
		if (graphics2D != null) {
			graphics2D.drawImage(this.inputBufferedImage, null, null);
		}
	}

	public void drawCortex(Graphics2D graphics2D) {
		if (graphics2D != null) {
			graphics2D.setColor(Color.BLUE);
			double r = this.cortexRadius;
			double x = this.cortex.origin.x;
			double y = this.cortex.origin.y;
			double newX = x - r;
			double newY = y - r;
			Ellipse2D.Double ellipse = new Ellipse2D.Double(newX, newY, r * 2, r * 2);
			graphics2D.draw(ellipse);
		}
	}

	public void drawOrigin(Graphics2D graphics2D) {
		if (graphics2D != null) {
			graphics2D.setColor(Color.CYAN);
			double r = 8;
			double x = this.origin.x;
			double y = this.origin.y;
			double newX = x - r;
			double newY = y - r;
			Ellipse2D.Double ellipse = new Ellipse2D.Double(newX, newY, r * 2, r * 2);
			graphics2D.draw(ellipse);
		}
	}

	public void drawInputCenter(Graphics2D graphics2D) {
		if (graphics2D != null) {
			Point point = new Point(this.getInputCenterX(), this.getInputCenterY());
			graphics2D.setColor(Color.white);
			graphics2D.drawLine((int) point.x, 0, (int) point.x, (int) point.y);
			graphics2D.drawLine(0, (int) point.y, (int) point.x, (int) point.y);
		}
	}

	public Color getColor(double factor, double value, double size) {
		double power;
//		if (inverted) {
//			power = (size - value) * factor / size;
//		} else {
		power = value * factor / size; // 0.9
//		}
		double H = power;// * 0.4; // Hue (note 0.4 = Green, see huge chart below)
		double S = 0.9; // Saturation
		double B = 0.9; // Brightness
		Color color = Color.getHSBColor((float) H, (float) S, (float) B);
		return color;
	}

	public void deltaDistance(double factor) {
		this.setDistance(this.distance + factor);
	}

	public double round(double value) {
		DecimalFormat decimalFormat = new DecimalFormat("#.##");
		return Double.parseDouble(decimalFormat.format(value));
	}

	// When distance is maxDistance, angle is formed with the point that is left,
	// right, top, bottom corners from center of object;
	// Field dimension is
	public double getAngle() {
		return 2 * Math.atan(this.toMillimeter(this.getSensorWidth()) / (2 * this.focalLength));
	}

	public Point getInputCenter() {
		Point point = new Point(this.getInputCenterX(), this.getInputCenterY());
		point.center = true;
		return point;
	}

	public double getInputCenterX() {
		return (this.inputBufferedImage != null) ? this.inputBufferedImage.getWidth() / 2 : 0;
	}

	public double getInputCenterY() {
		return (this.inputBufferedImage != null) ? this.inputBufferedImage.getHeight() / 2 : 0;
	}

	public double getCenterX() {
		return this.getObjectWidth() / 2;
	}

	public double getCenterY() {
		return this.getObjectHeight() / 2;
	}

	public double getCenterX(double distance) {
		return this.getObjectWidth(distance) / 2;
	}

	public double getCenterY(double distance) {
		return this.getObjectHeight(distance) / 2;
	}

	/**
	 * 
	 * @return
	 */
	public double getMaxDistance() {
		return (this.toMillimeter(this.object.getHeight()) * this.focalLength)
				/ this.toMillimeter(this.getSensorWidth());
	}

	public double getFieldWidth() {
		return (this.getSensorWidth() * distance) / focalLength;
	}

	public double getFieldHeight() {
		return (this.getSensorHeight() * distance) / focalLength;
	}

//	public double getSensorRadius() {
////		System.out.println("getSensorRadius() this.sensorRadius="+this.sensorRadius);
//		return this.cortexRadius;
//	}

	public double getSensorWidth() {
		return this.cortexRadius * Math.sqrt(2);
	}

	public double getSensorHeight() {
		return this.cortexRadius * Math.sqrt(2);
	}

	public double getObjectHeight(double distance) {
		return this.toPixel((this.focalLength * this.toMillimeter(this.object.getHeight())) / distance);
	}

	public double getObjectWidth(double distance) {
		return this.toPixel((this.focalLength * this.toMillimeter(this.object.getWidth())) / distance);
	}

	// Object width and height are dependant on distance. All objects have their own
	// maxDistance and the same minDistance
	// If I get the center of the Object at minDistance, then it will remain
	// constant.
	public double getObjectHeight() {
		return this.toPixel((this.focalLength * this.toMillimeter(this.object.getHeight())) / this.distance);
	}

	public double getObjectWidth() {
		return this.toPixel((this.focalLength * this.toMillimeter(this.object.getWidth())) / this.distance);
	}

	public boolean lensEquivalence() {
		double a = (this.getSensorWidth()) / this.focalLength;
		double b = this.getFieldHeight() / this.distance;
//		System.out.println("lenEquivalence() a="+a+" b="+b);
		return a == b;
	}

	public boolean objectEquivalence() {
		return this.toMillimeter(this.getObjectHeight())
				/ this.focalLength == (this.toMillimeter(this.object.getHeight())) / this.distance;
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
	public BufferedImage getInputBufferedImage() {
		BufferedImage after = null;
		if (this.object != null) {
			BufferedImage before = this.object;// this.bufferedImage;
			after = new BufferedImage((int) (before.getWidth() * this.scale), (int) (before.getHeight() * this.scale),
					BufferedImage.TYPE_INT_RGB);// new
												// BufferedImage(before.getWidth(),
												// before.getHeight(),
												// BufferedImage.TYPE_INT_RGB);
			AffineTransform affineTransform = new AffineTransform();
			affineTransform.scale(this.scale, this.scale);// this handles scaling the bufferedImage
			AffineTransformOp affineTransformOp = new AffineTransformOp(affineTransform,
					AffineTransformOp.TYPE_BILINEAR);
			after = affineTransformOp.filter(before, after);
			if (this.dimension != null) {
				BufferedImage screen = new BufferedImage((int) (this.dimension.width), (int) (this.dimension.height),
						BufferedImage.TYPE_INT_RGB);
				Graphics2D graphics2D = screen.createGraphics();
				graphics2D.setPaint(Color.white);
				if (graphics2D != null) {
					int width = after.getWidth();
					int height = after.getHeight();
					int centerX = width / 2;
					int centerY = height / 2;
					int x = 0;
					int y = 0;
					x = ((int) (this.dimension.getWidth() / 2) - centerX);
					y = ((int) (this.dimension.getHeight() / 2) - centerY);
					graphics2D.drawImage(after, x, y, null);
					after = screen;
				}
			}
		}
		return after;
	}

	@JsonIgnore
	public BufferedImage getBufferedImage() {
		return bufferedImage;
	}

	public BufferedImage getObject() {
		int width = (int) this.getWidth();
		int height = (int) this.getHeight();
		Point center = this.getCenter();
		BufferedImage b = new BufferedImage((int) (width), (int) (height), BufferedImage.TYPE_INT_RGB);
		Graphics g = b.createGraphics();
		Point bufferedImageCenter = this.getBufferedImageCenter(this.bufferedImage);
		// System.out.println("bufferedImageCenter="+bufferedImageCenter);
		int x = (int) (center.x - bufferedImageCenter.x);// (int)((width/2)-(center.x-bufferedImageCenter.x));
		int y = (int) (center.y - bufferedImageCenter.y);// ;//(int)((height/2)-
		// System.out.println("difference x="+x+" y="+y);
		// System.out.println("this.bufferedImage.getWidth()="+this.bufferedImage.getWidth()+"
		// this.bufferedImage.getHeight()="+this.bufferedImage.getHeight());
		g.drawImage(this.bufferedImage, x, y, this.bufferedImage.getWidth(), this.bufferedImage.getHeight(), null);
		// g.drawRect(x, y, this.bufferedImage.getWidth(),
		// this.bufferedImage.getHeight());
		// System.out.println("getObject() b.getWidth()="+b.getWidth()+"
		// b.getHeight()="+b.getHeight());

		return b;
	}

	public double toMillimeter(double value) {
		return value * 0.2645833333;
	}

	public double toPixel(double value) {
		return value / 0.2645833333;
	}

	public List<Point> getCornerList() {
		List<Point> cornerList = new ArrayList<>();
		Point topLeft = new Point(0, 0);
		Point topRight = new Point(this.bufferedImage.getWidth(), 0);
		Point bottomLeft = new Point(0, this.bufferedImage.getHeight());
		Point bottomRight = new Point(this.bufferedImage.getWidth(), this.bufferedImage.getHeight());
		cornerList.add(topLeft);
		cornerList.add(topRight);
		cornerList.add(bottomLeft);
		cornerList.add(bottomRight);
		return cornerList;
	}

	public List<Point> getCenteredCornerList() {
		List<Point> cornerList = this.getCornerList();
		Point center = this.getBufferedImageCenter(this.bufferedImage);
		for (Point corner : cornerList) {
			corner.x -= center.x;
			corner.y -= center.y;
		}
		return cornerList;
	}

	public double getMaxRadius() {
		List<Point> cornerList = this.getCenteredCornerList();
		double max = 0;
		for (Point p : cornerList) {
			double radius = Math.sqrt(Math.pow(p.x, 2) + Math.pow(p.y, 2));
			if (radius > max) {
				max = radius;
			}
		}
		return max;
	}

	public double getWidth() {
		return 2 * this.radius;
	}

	public double getHeight() {
		return 2 * this.radius;
	}

	public Point getCenter() {
		Point center = new Point(this.getWidth() / 2, this.getHeight() / 2);
		center.center = true;
		return center;
	}

	public Point getBufferedImageCenter(BufferedImage bufferedImage) {
		Point center = new Point(bufferedImage.getWidth() / 2, bufferedImage.getHeight() / 2);
//		System.out.println("getCenter() center="+center);
		return center;
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
//	if (Point.getDistance(point, p) > minDistance) {
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

//public void iterate(Graphics2D graphics2D, BufferedImage bufferedImage, Cortex cortex, Concept concept) {
////System.out.println("iterate(" + String.valueOf(graphics2D != null) + ", "
////	+ String.valueOf(bufferedImage != null) + ", " + String.valueOf(cortex != null) + ")");
//if (this.loop) {
//if (this.pointStack.size() > 0) {
////	System.out.println("this.pointStack=" + this.pointStack);
//	Point point = this.pointStack.pop();
//	System.out.println(point);
//	this.setOrigin(point.x, point.y);
//	this.input(graphics2D, concept);
//	List<Point> pointList = this.traverse(point);
//	for (Point p : pointList) {
//		this.pointStack.push(p);
//	}
//	System.out.println("this.pointStack.size()=" + this.pointStack.size());
//} else {
//	this.setBufferedImage(bufferedImage);
//	this.setCortex(cortex);
//	System.out.println("this.getMagnification()=" + this.getMagnification());
//	System.out.println("this.distance=" + this.distance);
//	if (this.distance == 0) {
//		System.out.println("this.distance == 0");//	public void centerBeliefList() {
//System.out.println("centerBeliefList()");
//if(this.cortex != null && this.cortex.root!=null) { 
//	Point center = new Point(this.getCenterX(),this.getCenterY());
//	double deltaX = center.x - this.cortex.root.x;
//	double deltaY = center.y - this.cortex.root.y;
//	this.cortex.root.x += deltaX;
//	this.cortex.root.y += deltaY;
////	for(Point p: this.cortex.pointList) {
////		p.x += deltaX;
////		p.y += deltaY;
////	}
//}
//}
//		this.maxDistance = this.getMaxDistance();
//		this.distance = this.maxDistance;
//		this.size = (int) (this.getMaxDistance() - this.focalLength - this.minDistance);
//		System.out.println("size=" + size);
//		this.index = 0;
//		this.setDistance(this.distance);
//		this.input(graphics2D, concept);
////		this.pointStack.push(this.root);
//		System.out.println("this.pointStack.size()=" + this.pointStack.size());
//		this.state = State.NEW;
//	} else {
//		this.interval = this.size / this.step;
//		if ((index * this.interval) < this.size) {
//			System.out.println("index=" + index);
//			this.distance = size;
//			this.distance -= index * this.interval;
//			this.index++;
//			this.setDistance(this.distance);
//			this.input(graphics2D, concept);
////			this.pointStack.push(this.root);
//			System.out.println("this.pointStack.size()=" + this.pointStack.size());
//			this.state = State.PENDING;
//		} else {
//			this.state = State.COMPLETE;
//			this.loop = false;
//		}
//	}
//}
//} else {
//this.setBufferedImage(bufferedImage);
//this.setCortex(cortex);
//if (bufferedImage != null && this.distance == 0) {
//	this.setDistance(this.minDistance);
//} else {
//	this.setDistance(this.distance);
//}
//if (this.manual) {
//	this.input(graphics2D, concept);
//	this.drawPointList(graphics2D);
//	this.drawBeliefList(graphics2D);
////	this.drawPointMatrix(graphics2D);
//} else {
//	this.drawScaledBufferedImage(graphics2D);
//	this.drawPointList(graphics2D);
////	this.drawPointMatrix(graphics2D);
//}
//}
//}

//public void input(Graphics2D graphics2D, BufferedImage bufferedImage, Cortex cortex,Concept concept) {
//System.out.println("input(" + String.valueOf(graphics2D != null) + ", " + concept + ")");
////System.out.println("angle="+this.getAngle());
////System.out.println("lensEquivalence="+this.lensEquivalence());
////System.out.println("objectEquivalence="+this.objectEquivalence());
////System.out.println("magnificationEquivalence="+this.magnificationEquivalence());
//this.setBufferedImage(bufferedImage);
//this.setCortex(cortex);
//this.drawScaledBufferedImage(graphics2D);
////concept = new Concept(UUID.randomUUID().toString());
//if (!manual && this.cortex.root == null) {
//	x = this.getCenterX();// this.observation.getObject().getWidth()/2;////*this.scale;
//	y = this.getCenterY();// this.observation.getObject().getHeight()/2;////*this.scale;
//	if (x > 0 && y > 0) {
//		this.cortex.root = new Point(x / this.scale, y / this.scale);// Save root
//		this.cortex.setOrigin((int) (x), (int) (y));
//		this.cortex.update();
//		this.cortex.process(graphics2D, scaledBufferedImage, concept);
//		this.addPoint(this.cortex.root);
//	}
//
//} else {
//	if (input) {
//		input = false;
//	} else {
//		x = this.getCenterX();
//		y = this.getCenterY();
//	}
//	if (this.cortex.root == null) {
//		this.cortex.root = new Point(x / this.scale, y / this.scale);
//	}
//	this.cortex.setOrigin((int) (x), (int) (y));
//	this.cortex.update();
//	this.cortex.process(graphics2D, scaledBufferedImage, concept);
//	this.addPoint(this.cortex.root);
//}
//this.drawSensor(graphics2D);
//}

//if (this.cortex.root == null) {
////this.cortex.root = new Point(origin.x / this.scale, origin.y / this.scale);
//this.cortex.root = new Point(this.getCenterX(), this.getCenterY());
//}

//public void centerBeliefList() {
//System.out.println("centerBeliefList()");
//if(this.cortex != null && this.cortex.root!=null) { 
//	Point center = new Point(this.getCenterX(),this.getCenterY());
//	double deltaX = center.x - this.cortex.root.x;
//	double deltaY = center.y - this.cortex.root.y;
//	this.cortex.root.x += deltaX;
//	this.cortex.root.y += deltaY;
////	for(Point p: this.cortex.pointList) {
////		p.x += deltaX;
////		p.y += deltaY;
////	}
//}
//}

//public void drawRoot(Graphics2D graphics2D) {
//	System.out.println("drawRoot(...)");
//	if (graphics2D != null) {
//		graphics2D.setColor(Color.CYAN);
//		double x = this.cortex.root.x;
//		double y = this.cortex.root.y;
//		System.out.println(this.cortex.root);
//		Ellipse2D.Double ellipse = new Ellipse2D.Double(x, y, 8, 8);
//		graphics2D.draw(ellipse);
//	}
//}

//int width = this.scaledBufferedImage.getWidth();
//int height = this.scaledBufferedImage.getHeight();
//int centerX = width/2;
//int centerY = height/2;
//int x = 0;
//int y = 0;
////int width = this.scaledBufferedImage.getWidth();
////int height = this.scaledBufferedImage.getHeight();
////int centerX = width/2;
////int centerY = height/2;
//if(this.dimension != null) {
//	x = ((int)(this.dimension.getWidth()/2)-centerX);
//	y = ((int)(this.dimension.getHeight()/2)-centerY);
////	graphics2D.setColor(Color.white);
////	graphics2D.fillRect(0, 0, dimension.getWidth(), dimension.getHeight());
////	graphics2D.translate((int) (dimension.getWidth() / 2.0), (int) (dimension.getHeight() / 2.0));
////	dimension.getWidth();
////	dimension.getHeight();
//}

//belief.origin.scale(1 / this.scale);
//belief.origin.x -= this.origin.x;
//belief.origin.y -= this.origin.y;
//if (!previous.center) {
//	// Delta is a movement between two points.
//	// If "same" center, then delta is zero.
//	Point delta = this.origin.subtract(this.previous);
//	belief.origin.x += delta.x;
//	belief.origin.y += delta.y;
////
//}
//this.cortex.addPoint(root, belief.origin);
////belief.origin.subtract(this.origin);
//if (this.cortex != null && belief != null) {
//	
//	List<Point> pointList = belief.pointList;
//	for (Point point : pointList) {
//		// Scale divide makes points the same size in a domain.
//		point.x /= this.scale;
//		point.y /= this.scale;
//		// Without this code, Points appear where they are drawn
//		// With this code, points appear at root 0,0.
//
////With this code the belief pointList is completly altered these transforms need to be applied to the belief center;
//
//		point.x -= this.origin.x;
//		point.y -= this.origin.y;
//
//		if (!previous.center) {
//			// Delta is a movement between two points.
//			// If "same" center, then delta is zero.
//			Point delta = this.origin.subtract(this.previous);
//			point.x += delta.x;
//			point.y += delta.y;
//
//		}
////		this.cortex.addPoint(root, point);
//	}
//
//}

//System.out.println("angle="+this.getAngle());
//System.out.println("lensEquivalence="+this.lensEquivalence());
//System.out.println("objectEquivalence="+this.objectEquivalence());
//System.out.println("magnificationEquivalence="+this.magnificationEquivalence());

//public List<Point> traverse(Point point) {
//List<Point> pointList = new ArrayList<>();
//List<Node> nodeList = point.getChildren();
//for (Node n : nodeList) {
//	Point p = (Point) n;
//	if (Point.getDistance(point, p) * this.scale >= this.getSensorRadius()) {
//		pointList.add(p);
//	}
//}
//return pointList;
//}

//graphics2D.setColor(Color.MAGENTA);
//List<Point> pList = new ArrayList<>();
//for (Point p : belief.pointList) {
//	p = new Point(p);
//	p.x *= this.scale;
//	p.y *= this.scale;
//	p.x += x;
//	p.y += y;
////	ellipse = new Ellipse2D.Double(p.x, p.y, 2, 2);
////	graphics2D.draw(ellipse);
//	pList.add(p);
//}

//int size = belief.pointList.size();
//int count = 0;
//for (Point child : belief.pointList) {
//	child = new Point(child);
//	child.x *= this.scale;
//	child.y *= this.scale;
//	child.x += x;
//	child.y += y;
//	graphics2D.setColor(this.getColor(0.8, count, size));
////	graphics2D.drawLine((int) (point.x * this.scale), (int) (point.y * this.scale),
////			(int) (child.x * this.scale), (int) (child.y * this.scale));
//	graphics2D.drawLine((int) (point.x), (int) (point.y), (int) (child.x), (int) (child.y));
//}
//count++;

//List<Point> pList = new ArrayList<>();
//for (Point p : this.cortex.pointList) {
//	p = new Point(p);
//	p.x *= this.scale;
//	p.y *= this.scale;
//	p.x += this.getInputCenterX();
//	p.y += this.getInputCenterY();
////	ellipse = new Ellipse2D.Double(p.x, p.y, 2, 2);
////	graphics2D.draw(ellipse);
//	pList.add(p);
//}
//
//Matrix matrix = new Matrix(pList, 4 * this.scale);
//List<ArrayList<Point>> rowList = matrix.getRowList();
//for (int i = 0; i < rowList.size(); i++) {
//	List<Point> pointList = rowList.get(i);
//	Point previous = null;
//	Point current;
//	for (int j = 0; j < pointList.size(); j++) {
//		current = pointList.get(j);
////		current.x *= this.scale;
////		current.y *= this.scale;
////		current.x += x;
////		current.y += y;
//		if (previous != null) {
//			graphics2D.drawLine((int) (current.x), (int) (current.y), (int) (previous.x),
//					(int) (previous.y));
//		}
//		previous = current;
//	}
//}

//Up to this point, belief has global
//coordinates where input center is regarded as the origin
//need two representations, 
//belief.scale(this.scale);//, this.previous);
//Whatever belief and origin are normalize();