/*
 * Copyright 2020 Joaquin Osvaldo Rodriguez
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.meritoki.library.cortex.model.network;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.meritoki.library.cortex.model.Belief;
import com.meritoki.library.cortex.model.Binary;
import com.meritoki.library.cortex.model.Concept;
import com.meritoki.library.cortex.model.Mind;
import com.meritoki.library.cortex.model.Point;
import com.meritoki.library.cortex.model.group.Group;

@JsonTypeInfo(use = Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = Network.class), @Type(value = Group.class), })
public class Cortex {
	protected static Logger logger = LoggerFactory.getLogger(Cortex.class.getName());
	@JsonProperty
	public String uuid;
	@JsonProperty
	public ColorType type = ColorType.COMPOSITE;
	@JsonProperty
	public Configuration configuration = Configuration.HEXAGONAL;
	@JsonProperty
	public int size = 27;
	@JsonProperty
	public int radius = 1;
	@JsonProperty
	public int dimension = 13;
	@JsonProperty
	public int length = 2;
	@JsonProperty
	public int padding = 0;
	@JsonProperty
	public int depth = 0;
	@JsonProperty
	public Map<String, Shape> shapeMap = new HashMap<>();
	@JsonProperty
	public Point origin = new Point(0, 0);
	@JsonProperty
	public int index = 0;
	@JsonProperty
	public List<Belief> beliefList = new ArrayList<>();
	@JsonProperty
	public ColorType[] typeList = { ColorType.BRIGHTNESS, ColorType.RED, ColorType.GREEN, ColorType.BLUE };

	@JsonProperty
	public Map<String, String> conceptMap = new HashMap<>();
	@JsonIgnore
	public Mind mind;

	public Cortex() {
		this.uuid = UUID.randomUUID().toString();
//		this.pointList.add(this.root);
	}

	public List<Point> getPointList(Point origin, double scale) {
		List<Point> pList = new ArrayList<>();
		for (Belief belief : this.getBeliefList(32)) {
			for (Point p : belief.getGlobalPointList()) {
				p = new Point(p);
				p.x *= scale;
				p.y *= scale;
				p.x += origin.x;
				p.y += origin.y;
				p.round();
				pList.add(p);
			}
		}
		return pList;
	}

	public List<Belief> getBeliefList(int seconds) {
		List<Belief> beliefList = new ArrayList<>();
		Date now = new Date();
		for (Belief b : this.beliefList) {
			long millisecond = Math.abs(b.date.getTime() - now.getTime());
			long second = millisecond / 1000;
			if (seconds > second) {
				beliefList.add(b);
			}
		}
		return beliefList;
	}

	@JsonIgnore
	private Binary addRecursive(Binary current, Belief belief) {
		double value = belief.getRelativeRadius();
		if (current == null) {
			return new Mind(belief);
		}
		if (value < current.value) {
			current.left = addRecursive(current.left, belief);
		} else if (value > current.value) {
			current.right = addRecursive(current.right, belief);
		} else {
			// value already exists
			return current;
		}
		return current;
	}

	@JsonIgnore
	public void add(Belief belief) {
		boolean flag = this.containsMind(this.mind, belief.getRelativeRadius());
		if (flag) {
			this.mind = (Mind) this.getMind(this.mind, belief.getRelativeRadius());
			this.mind.beliefList.add(belief);
		} else {
			this.mind = (Mind) addRecursive(this.mind, belief);
		}
	}

	@JsonIgnore
	public boolean containsMind(double value) {
		return this.containsMind(this.mind, value);
	}

	@JsonIgnore
	public Mind getMind(double value) {
		return (Mind) this.getMind(this.mind, value);
	}

	@JsonIgnore
	private boolean containsMind(Binary current, double value) {
		if (current == null) {
			return false;
		}
		if (value == current.value) {
			return true;
		}
		return value < current.value ? containsMind(current.left, value) : containsMind(current.right, value);
	}

	@JsonIgnore
	private Binary getMind(Binary current, double value) {
		if (current == null) {
			return null;
		}
		if (value == current.value) {
			return current;
		}
		return value < current.value ? getMind(current.left, value) : getMind(current.right, value);
	}

	@JsonIgnore
	private int findSmallestValue(Binary root) {
		return (int) (root.left == null ? root.value : findSmallestValue(root.left));
	}

	@JsonIgnore
	public void traverseInOrder(Binary node) {
		if (node != null) {
			traverseInOrder(node.left);
			logger.info(" " + node.value);
			logger.info(" " + ((Mind) node).beliefList.size());
			traverseInOrder(node.right);
		}
	}

	@JsonIgnore
	public boolean setIndex(String uuid) {
		boolean flag = false;
		for (int i = 0; i < this.beliefList.size(); i++) {
			Belief input = this.beliefList.get(i);
			if (input.uuid.equals(uuid)) {
				this.index = i;
				flag = true;
				break;
			}
		}
		return flag;
	}

	@JsonIgnore
	public boolean setIndex(int index) {
		boolean flag = false;
		if (index >= 0 && index < this.beliefList.size()) {
			this.index = index;
			flag = true;
		}
		return flag;
	}

	@JsonIgnore
	public Belief getBelief() {
		int size = this.beliefList.size();
		Belief page = (this.index < size && size > 0) ? this.beliefList.get(this.index) : null;
		return page;
	}

	@JsonIgnore
	public Belief getBelief(int index) {
		int size = this.beliefList.size();
		Belief page = (index < size && size > 0) ? this.beliefList.get(index) : null;
		return page;
	}

	@JsonIgnore
	public Belief getLastBelief() {
		int size = this.beliefList.size();
		Belief page = (size > 0) ? this.beliefList.get(size - 1) : null;
		return page;
	}

	@JsonIgnore
	public void load() {
//		for (Belief belief : this.beliefList) {
//		
//			for (Point point : pointList) {
//				this.addPoint(this.root, point);
//			}
//		}
	}

//	public void addPoint(Point root, Point point) {
////		logger.info("addPoint("+root+", "+point+")");
//		if (point != null && root != null && !point.equals(root)) {
////			point.round()
//			List<Node> nodeList = root.getChildren();
//			double min = Point.getDistance(root, point);
//			Point minPoint = null;
//			Iterator<Node> iterator = nodeList.iterator();
//			while (iterator.hasNext()) {
//				Node n = iterator.next();
//				Point childPoint = (Point) n;
//				double distance = Point.getDistance(childPoint, point);
//				if (distance < min) {
//					min = distance;
//					minPoint = childPoint;
//				}
//			}
//			if (minPoint != null) {
//				// logger.info("addPoint("+root+", "+point+") minPoint="+minPoint);
//				this.addPoint(minPoint, point);
//			} else {
//				root.addChild(point);
////				Node.printTree(root, " ");
//				this.pointList.add(point);
//			}
//		}
//	}

	@JsonIgnore
	public void update() {
	}

	@JsonIgnore
	public void setOrigin(int x, int y) {
		logger.info("setOrigin(" + x + ", " + y + ")");
		this.origin = new Point(x, y);
//		this.x = x;
//		this.y = y;
	}

//	public int getX() {
//		return this.x;
//	}
//
//	public int getY() {
//		return this.y;
//	}

	@JsonIgnore
	public void process(Graphics2D graphics2D, BufferedImage image, Concept concept) {
	}

	@JsonIgnore
	public void process(Graphics2D graphics2D, BufferedImage image, Point origin, Concept concept) {
	}

//	@JsonIgnore
//	public int setPointMap(List<Point> pointList) {
////		logger.info("setPointMap(" + pointList.size() + ")");
//		int max = 0;
//		for (Point p : pointList) {
//			Belief belief = this.beliefMatrix[(int) p.x][(int) p.y];
//			if (belief == null) {
//				belief = new Belief();
//			}
//			if(p.belief != null) {
//				int size = p.belief.conceptList.size();
//				if(size > max) {
//					max = size;
//				}
//				belief.conceptList.addAll(p.belief.conceptList);
//			}
////			logger.info("setPointMap("+pointList.size()+") count="+count);
//			this.beliefMatrix[(int) p.x][(int) p.y] = belief;
//		}
//		return max;
//	}
	@JsonIgnore
	public double getRadius() {
		double max = 0;
		for (Entry<String, Shape> e : this.shapeMap.entrySet()) {
			String key = e.getKey();
			if (key.startsWith("0:")) {
//				logger.info("getSensorRedius() key="+key);
				Shape shape = e.getValue();
				shape.initCells();
//				shape.updatePoints();
				double[] xPoints = shape.xpoints;
				double[] yPoints = shape.ypoints;
				double nPoints = shape.npoints;
				for (int i = 0; i < nPoints; i++) {
					double x = xPoints[i] - this.origin.x;// e.getValue().getX();
					double y = yPoints[i] - this.origin.y;// e.getValue().getY();
					double r = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
					if (r > max) {
						max = r;
					}
				}
			}
		}
//		logger.info("getRadius() max="+max);
		return max;
	}

	@JsonIgnore
	public BufferedImage scaleBufferedImage(BufferedImage bufferedImage, double scale) {
		BufferedImage before = bufferedImage;
		int w = before.getWidth();
		int h = before.getHeight();
		BufferedImage after = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		AffineTransform at = new AffineTransform();
		at.scale(2.0, 2.0);
		AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		after = scaleOp.filter(before, after);
		return after;
	}
}

//public void addBelief(Belief belief) {
////if (belief != null) {
////belief.origin.scale(1 / this.scale);
////belief.origin.x -= this.origin.x;
////belief.origin.y -= this.origin.y;
////if (!previous.center) {
////	// Delta is a movement between two points.
////	// If "same" center, then delta is zero.
////	Point delta = this.origin.subtract(this.previous);
////	belief.origin.x += delta.x;
////	belief.origin.y += delta.y;
//////
////}
////this.cortex.addPoint(root, belief.origin);
//////belief.origin.subtract(this.origin);
////if (this.cortex != null && belief != null) {
////	
////	List<Point> pointList = belief.pointList;
////	for (Point point : pointList) {
////		// Scale divide makes points the same size in a domain.
////		point.x /= this.scale;
////		point.y /= this.scale;
////		// Without this code, Points appear where they are drawn
////		// With this code, points appear at root 0,0.
////
//////With this code the belief pointList is completly altered these transforms need to be applied to the belief center;
////
////		point.x -= this.origin.x;
////		point.y -= this.origin.y;
////
////		if (!previous.center) {
////			// Delta is a movement between two points.
////			// If "same" center, then delta is zero.
////			Point delta = this.origin.subtract(this.previous);
////			point.x += delta.x;
////			point.y += delta.y;
////
////		}
//////		this.cortex.addPoint(root, point);
////	}
////
////}
////}
//}

//@JsonProperty
//public Point root = new Point(0,0);
////@JsonProperty
////public Point global = new Point(0,0);
//@JsonIgnore
//public List<Point> pointList = new ArrayList<>();
