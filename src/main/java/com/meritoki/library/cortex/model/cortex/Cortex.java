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
package com.meritoki.library.cortex.model.cortex;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.meritoki.library.cortex.model.Belief;
import com.meritoki.library.cortex.model.BinaryNode;
import com.meritoki.library.cortex.model.Concept;
import com.meritoki.library.cortex.model.Node;
import com.meritoki.library.cortex.model.Point;
import com.meritoki.library.cortex.model.group.Group;
import com.meritoki.library.cortex.model.network.Color;
import com.meritoki.library.cortex.model.network.Configuration;
import com.meritoki.library.cortex.model.network.Network;
import com.meritoki.library.cortex.model.network.shape.Shape;

@JsonTypeInfo(use = Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = Network.class), @Type(value = Group.class), })
public class Cortex {
	@JsonProperty
	public String uuid = null;
	@JsonProperty
	public Color type = Color.BRIGHTNESS;// int type;//com.meritoki.library.cortex.model.Type type =
											// com.meritoki.library.cortex.model.Type.HEXAGONAL;
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
	public Point origin = new Point(0,0);
	@JsonProperty
	public int index = 0;
	@JsonProperty
	public List<Belief> beliefList = new ArrayList<>();
	@JsonProperty
	public Point root = new Point(0,0);
	@JsonIgnore
	public BinaryNode binaryNode;
	@JsonIgnore
	public List<Point> pointList = new ArrayList<>();
	@JsonIgnore
	public Belief[][] beliefMatrix;
	@JsonIgnore
	public List<Belief[][]> beliefMatrixList;
	@JsonProperty
	public Map<String, String> conceptMap = new HashMap<>();

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
		Belief page = (size > 0)?this.beliefList.get(size-1):null;
		return page;
	}

	@JsonIgnore
	public void load() {
		for (Belief belief : this.beliefList) {
			List<Point> pointList = belief.pointList;
			for (Point point : pointList) {
				this.addPoint(this.root, point);
			}
		}
	}

	public void addPoint(Point root, Point point) {
//		System.out.println("addPoint("+root+", "+point+")");
		if (point != null && !root.equals(point)) {
			List<Node> nodeList = root.getChildren();
			double min = Point.getDistance(root, point);
			Point minPoint = null;
			Iterator<Node> iterator = nodeList.iterator();
			while (iterator.hasNext()) {
				Node n = iterator.next();
				Point childPoint = (Point) n;
				double distance = Point.getDistance(childPoint, point);
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
				this.pointList.add(point);
			}
		}
	}

	@JsonIgnore
	public void update() {
	}

	@JsonIgnore
	public void setOrigin(int x, int y) {
		System.out.println("setOrigin("+x+", "+y+")");
		this.origin = new Point(x,y);
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

//	@JsonIgnore
//	public int setPointMap(List<Point> pointList) {
////		System.out.println("setPointMap(" + pointList.size() + ")");
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
////			System.out.println("setPointMap("+pointList.size()+") count="+count);
//			this.beliefMatrix[(int) p.x][(int) p.y] = belief;
//		}
//		return max;
//	}
	@JsonIgnore
	public double getSensorRadius() {
		double max = 0;
		for (Entry<String, Shape> e : this.shapeMap.entrySet()) {
			String key = e.getKey();
			if (key.startsWith("0:")) {
//				System.out.println("getSensorRedius() key="+key);
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
		System.out.println("getSensorRedius() max="+max);
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
