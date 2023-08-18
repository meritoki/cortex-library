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
package com.meritoki.library.cortex.model.network.hexagon;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.meritoki.library.cortex.model.network.ColorType;
import com.meritoki.library.cortex.model.network.Level;
import com.meritoki.library.cortex.model.network.Network;
import com.meritoki.library.cortex.model.network.Shape;
import com.meritoki.library.cortex.model.unit.Point;

/**
 * In Network, hexagons are referenced by level and relative coordinates, i.e.
 * 0:00, 4:00
 * 
 * @author osvaldo.rodriguez
 *
 */
public class Hexagonal extends Network {

//	public static void main(String[] args) {
//		Hexagonal n = new Hexagonal(ColorType.BRIGHTNESS, 0, 0, 13, 1, 0);
//		n.load();
////		Map<String,Square> squareMap = Network.getSquareMap(-1, new Point(0,0), 3, 2, 0);
////		
////		for (Map.Entry<String, Square> entry : squareMap.entrySet()) {
////			String key = entry.getKey();
////			Square value = entry.getValue();
////			System.out.println(key+" "+value.getCenter()+" "+value.getRadius());
////		}
//	}

	@JsonIgnore
	protected static Logger logger = LoggerFactory.getLogger(Hexagonal.class.getName());


	public Hexagonal() {
		super(new ColorType[]{ ColorType.BRIGHTNESS, ColorType.RED, ColorType.GREEN, ColorType.BLUE }, 0, 0);
		this.length = 7;
	}

	public Hexagonal(int size, int radius, int padding) {
		super(new ColorType[]{ ColorType.BRIGHTNESS, ColorType.RED, ColorType.GREEN, ColorType.BLUE }, 0, 0);
		this.size = size;
		this.radius = radius;
		this.padding = padding;
		this.length = 7;
	}

	public Hexagonal(ColorType[] typeList, int x, int y, int size, int radius, int padding) {
		super(typeList, x, y);
		this.size = size;
		this.radius = radius;
		this.padding = padding;
		this.length = 7;
	}

	/**
	 * Need to modify so that it only moves the input level, not all levels this
	 * will improve the performance of the algorithm.
	 */
	@JsonIgnore
	@Override
	public void update() {
		double radians = Math.toRadians(30);
		double xOff = Math.cos(radians) * (radius + padding);
		double yOff = Math.sin(radians) * (radius + padding);
		int half = size / 2;
		Level level = null;
		Shape shape = null;
		level = this.getInputLevel();
		for (int row = 0; row < size; row++) {
			int cols = size - java.lang.Math.abs(row - half);
			for (int col = 0; col < cols; col++) {
				int xPosition = row < half ? col - row : col - half;
				int yPosition = row - half;
				int x = (int) (this.origin.x + xOff * (col * 2 + 1 - cols));
				int y = (int) (this.origin.y + yOff * (row - half) * 3);
				shape = level.shapeMap.get(xPosition + "," + yPosition);
				if (shape != null) {
					shape.setCenter(new Point(x, y));
				} else {
					logger.warn("update() shape == null");
				}
			}
		}
	}

	/**
	 * Function uses hexagonMap initialized with loaded parameters, to generate keys
	 * that retrieve loaded hexagons from file and connects them in parent child
	 * relationships, forming a tree data structure that converges on a root not
	 */
	@JsonIgnore
	@Override
	public void load() {
		super.load();
		logger.info("load()");
		logger.info("load() this.shapeMap.size()=" + ((this.shapeMap != null)?this.shapeMap.size()+"":0));
		logger.info("load() this.size=" + this.size);
		logger.info("load() this.radius=" + this.radius);
		logger.info("load() this.depth=" + this.depth);
		Map<String, Shape> shapeMap = getShapeMap(-1, new Point(this.origin.x, this.origin.y), this.size, this.radius,
				this.padding);
		int depth = (this.depth > 0) ? this.depth : this.getDepth(shapeMap.size());
		if (this.depth == 0) {
			this.depth = depth;
		}
		Level level = new Level();
		List<Shape> shapeList = this.getShapeList(shapeMap);
		Shape shape = null;
		logger.info("load() shapeList.size()="+shapeList.size());
		for (Shape s : shapeList) {
			shape = this.shapeMap.get("0:" + s);
			if (shape == null) {
				shape = new Hexagon(s);
				this.shapeMap.put("0:" + shape, shape);
			} 
			else {
				shape = new Hexagon(shape);
				this.shapeMap.put("0:" + shape, shape);
			}
//			hexagon.setData("0:" + hexagon);
//			System.out.println(shape);
//			System.out.println(shape.shortConeArray);
//			
			shape.initCells();
//			level.shapeMap.put("0:" + hexagon, hexagon);
			level.addShape(null,shape);
		}
		this.addLevel(level);
		LinkedList<Shape> shapeStack = null;
		int exponent = 0;
//		Map<String, Shape> shapeMap;
		for (int i = 1; i < depth; i++) {
			logger.trace("load() i=" + i);
			logger.trace("load() exponent=" + exponent);
			if (i % 2 == 0) {
				exponent++;
			}
			shapeMap = this.getLastLevel().getShapeMap();
			level = new Level();
			shapeList = new LinkedList<>();
			shapeStack = new LinkedList<>();
//			shapeStack.push(shapeMap.get(this.origin.x + "," + this.origin.y));
			shapeStack.push(shapeMap.get("0,0"));
//			shapeStack.push(shapeMap.get(new Point(0,0)));
			Shape s;
			while (!shapeStack.isEmpty()) {
				s = shapeStack.pop();
				LinkedList<Shape> list = null;
				if (i % 2 == 1) {
					list = getGroupOneHexagonList(shapeMap, s.getX(), s.getY(), exponent); // get list for x, y
				} else {
					list = getGroupZeroHexagonList(shapeMap, s.getX(), s.getY(), exponent); // get list for x,
					// y
				}
				for (Shape h : list) {
					if (!shapeList.contains(h)) {
						shapeList.add(h);
						shapeStack.push(h);
					}
				}
			}
			for (Shape m : shapeList) {
				Shape h = this.shapeMap.get(i + ":" + m);
				if (h == null) {
					h = new Hexagon(m);
					h.length = 7;
					this.shapeMap.put(i + ":" + h, h);
				} 
//				else {
//					h = new Hexagon(h);
//				}
				List<Shape> list = null;
				if (i % 2 == 1) {
					list = this.getGroupZeroHexagonList(shapeMap, h.getX(), h.getY(), exponent);
				} else {
					list = this.getGroupOneHexagonList(shapeMap, h.getX(), h.getY(), exponent - 1);
				}
//				logger.info("load() list.size()="+list.size());
				for (Shape n : list) {
//					n = new Hexagon(n);
					h.addChild(n);
				}
//				h.setData(i + ":" + h);
				level.addShape(null,h);
//				level.shapeMap.put("0:" + h, h);
			}
			this.addLevel(level);
		}
		// if (logger.isDebugEnabled()) {
		 level = this.getRootLevel();
		 Shape h = level.getShapeList().get(0);
//		 Node.printTree(h, " ");
		// }
	}

	@JsonIgnore
	public LinkedList<Shape> getGroupZeroHexagonList(Map<String, Shape> hexagonMap, int x, int y, int exponent) {
		LinkedList<Shape> hexagonList = new LinkedList<>();
		Shape h = null;
		int multiplier = (int) Math.pow(3, exponent);
		// (0,0)
		h = hexagonMap.get((x) + "," + (y));
		if (h != null) {
			hexagonList.push(h);
		}
		// (+3,-3)
		h = hexagonMap.get((x + (1 * multiplier)) + "," + (y - (1 * multiplier)));
		if (h != null) {
			hexagonList.push(h);
		}
		// (+3,0)
		h = hexagonMap.get((x + (1 * multiplier)) + "," + (y));
		if (h != null) {
			hexagonList.push(h);
		}
		// (0,+3)
		h = hexagonMap.get((x) + "," + (y + (1 * multiplier)));
		if (h != null) {
			hexagonList.push(h);
		}
		// (-3,+3)
		h = hexagonMap.get((x - (1 * multiplier)) + "," + (y + (1 * multiplier)));
		if (h != null) {
			hexagonList.push(h);
		}
		// (-3,0)
		h = hexagonMap.get((x - (1 * multiplier)) + "," + (y));
		if (h != null) {
			hexagonList.push(h);
		}
		// (0,-3)
		h = hexagonMap.get((x) + "," + (y - (1 * multiplier)));
		if (h != null) {
			hexagonList.push(h);
		}

		return hexagonList;
	}

	@JsonIgnore
	public LinkedList<Shape> getGroupOneHexagonList(Map<String, Shape> hexagonMap, int x, int y, int exponent) {
		LinkedList<Shape> hexagonList = new LinkedList<>();
		Shape h = null;
		int multiplier = (int) Math.pow(3, exponent);
		// (0,0)
		h = hexagonMap.get((x) + "," + (y));
		if (h != null) {
			hexagonList.push(h);
		}
		// (+3,-6)
		h = hexagonMap.get((x + (1 * multiplier)) + "," + (y - (2 * multiplier)));
		if (h != null) {
			hexagonList.push(h);
		}
		// (+6,-3)
		h = hexagonMap.get((x + (2 * multiplier)) + "," + (y - (1 * multiplier)));
		if (h != null) {
			hexagonList.push(h);
		}
		// (+3,+3)
		h = hexagonMap.get((x + (1 * multiplier)) + "," + (y + (1 * multiplier)));
		if (h != null) {
			hexagonList.push(h);
		}
		// (-3,+6)
		h = hexagonMap.get((x - (1 * multiplier)) + "," + (y + (2 * multiplier)));
		if (h != null) {
			hexagonList.push(h);
		}
		// (-6,+3)
		h = hexagonMap.get((x - (2 * multiplier)) + "," + (y + (1 * multiplier)));
		if (h != null) {
			hexagonList.push(h);
		}
		// (-3,-3)
		h = hexagonMap.get((x - (1 * multiplier)) + "," + (y - (1 * multiplier)));
		if (h != null) {
			hexagonList.push(h);
		}
		return hexagonList;
	}

	@JsonIgnore
	public static Map<String, Shape> getShapeMap(int level, Point origin, int size, int radius, int padding) {
		Map<String, Shape> hexagonMap = new HashMap<>();
		double radians = Math.toRadians(30);
		double xOff = Math.cos(radians) * (radius + padding);
		double yOff = Math.sin(radians) * (radius + padding);
		int half = size / 2;
		Hexagon hexagon = null;
		for (int row = 0; row < size; row++) {
			int cols = size - java.lang.Math.abs(row - half);
			for (int col = 0; col < cols; col++) {
				int xPosition = row < half ? col - row : col - half;
				int yPosition = row - half;
				int x = (int) (origin.x + xOff * (col * 2 + 1 - cols));
				int y = (int) (origin.y + yOff * (row - half) * 3);
				hexagon = new Hexagon(xPosition, yPosition, new Point(x, y), radius);
				hexagon.length = 7;
				if (level > -1)
					hexagonMap.put(level + ":" + xPosition + "," + yPosition, hexagon);
				else
					hexagonMap.put(xPosition + "," + yPosition, hexagon);
			}
		}
		return hexagonMap;
	}



	@JsonIgnore
	public int getDepth(int value) {
		boolean flag = true;
		int count = 1;
		do {
			value /= 3;
			if (value <= 1) {
				flag = false;
			}
			logger.info("getLevelCount(" + value + ")");
			count++;
		} while (flag);
		logger.info("getLevelCount(" + value + ") count=" + count);
		return count;
	}


}

//public static Map<String, Square> getSquareMap(int level, Point origin, int dimension, int length, int padding) {
//Map<String, Square> squareMap = new HashMap<>();
//int half = dimension/2;
////double radians = Math.toRadians(45);
////double xOff = Math.cos(radians) * (radius + padding);
////double yOff = Math.sin(radians) * (radius + padding);
//Square square = null;
//double xLeg = (length/2)-(padding/2);
//double yLeg = xLeg;
//double radius = Math.sqrt(Math.pow(xLeg, 2)+Math.pow(yLeg,2));
//for (int row = 0; row < dimension; row++) {
////	int cols = dimension - java.lang.Math.abs(row - half);
//	for (int column = 0; column < dimension; column++) {
//		int xPosition = column - half;
//		int yPosition = row - half;
//		System.out.println(xPosition+" "+yPosition);
//		double x = (origin.x + (xPosition * length) );
//		double y = (origin.y + (yPosition * length));
//		square = new Square(xPosition,yPosition, new Point(x,y),radius);
//		if (level > -1)
//			squareMap.put(level + ":" + xPosition + "," + yPosition, square);
//		else
//			squareMap.put(xPosition + "," + yPosition, square);
//	}
//}
//return squareMap;
//}
//@JsonIgnore
//public void init() {
//	logger.info("init()");
//	this.setHexagonMap(this.getHexgonMap(new Point(this.x, this.y), this.size, this.radius, this.padding));
//	int count = this.getDepth(this.getHexagonMap().size());
//	Level level = new Level(this.getHexagonMap());
//	for (Node n : level.getHexagonList()) {
//		n.setData("0:" + n.getData());
//	}
//	this.pushLevel(level);
//	LinkedList<Hexagon> hexagonList = null;
//	LinkedList<Hexagon> hexagonStack = null;
//	Map<String, Hexagon> hexagonMap = null;
//	int exponent = 0;
//	for (int i = 1; i < count; i++) {
//		logger.debug("init() i=" + i);
//		logger.debug("init() exponent=" + exponent);
//		if (i % 2 == 0) {
//			exponent++;
//		}
//		hexagonMap = this.peekLevel().getHexagonMap();
//		level = new Level();
//		hexagonList = new LinkedList<>();
//		hexagonStack = new LinkedList<>();
//		hexagonStack.push(hexagonMap.get(this.x + "," + this.y));
//		while (!hexagonStack.isEmpty()) {
//			Hexagon hexagon = hexagonStack.pop();
//			LinkedList<Hexagon> list = null;
//			if (i % 2 == 1) {
//				list = getOneHexagonList(hexagonMap, hexagon.getX(), hexagon.getY(), exponent); // get list for x, y
//			} else {
//				list = getZeroHexagonList(hexagonMap, hexagon.getX(), hexagon.getY(), exponent); // get list for x,
//				// y
//			}
//			for (Hexagon h : list) {
//				if (!hexagonList.contains(h)) {
//					hexagonList.add(h);
//					hexagonStack.push(h);
//				}
//			}
//		}
//		Hexagon hexagon;
//		for (Hexagon m : hexagonList) {
//			hexagon = new Hexagon(m);
//			List<Hexagon> list = null;
//			if (i % 2 == 1) {
//				list = this.getZeroHexagonList(hexagonMap, m.getX(), m.getY(), exponent);
//			} else {
//				list = this.getOneHexagonList(hexagonMap, m.getX(), m.getY(), exponent - 1);
//			}
//			for (Hexagon n : list) {
//				hexagon.addChild(n);
//			}
//			hexagon.setData(i + ":" + (String) hexagon.getData());
//			level.addHexagon(hexagon);
//		}
//		this.pushLevel(level);
//	}
////	if (logger.isDebugEnabled()) {
////		level = this.peekLevel();
////		Hexagon h = level.getHexagonList().get(0);
////		Node.printTree(h, " ");
////	}
//}

//@JsonIgnore
//public void update() {
//	double radians = Math.toRadians(30);
//	double xOff = Math.cos(radians) * (radius + padding);
//	double yOff = Math.sin(radians) * (radius + padding);
//	int half = size / 2;
//	Hexagon hexagon = null;
//	for (int row = 0; row < size; row++) {
//		int cols = size - java.lang.Math.abs(row - half);
//		for (int col = 0; col < cols; col++) {
//			int xPosition = row < half ? col - row : col - half;
//			int yPosition = row - half;
//			int x = (int) (this.x + xOff * (col * 2 + 1 - cols));
//			int y = (int) (this.y + yOff * (row - half) * 3);
//			hexagon = this.hexagonMap.get(xPosition + "," + yPosition);
//			hexagon.setCenter(new Point(x, y));
//		}
//	}
//}

//public Belief process(Graphics2D graphics2D, BufferedImage image, double scale, Concept concept, int sleep) {
//	logger.info("processing...");
//	Belief belief = null;
//	int size = this.getLevelStack().size();
//	Level level = (size > 0) ? this.getLevelStack().get(size - 1) : null;
//	if (level != null && image != null) {
//		for (Hexagon h : level.getHexagonList()) {
//			if (sleep > 0) {
//				graphics2D.drawPolygon(h.doubleToIntArray(h.xpoints), h.doubleToIntArray(h.ypoints),
//						(int) h.npoints);
//			}
//			h.initCells();
//			for (int i = 0; i < h.SIDES; i++) {
//				if (h.shortConeArray[i] != null && h.shortConeArray[i].point != null && h.mediumConeArray[i] != null
//						&& h.mediumConeArray[i].point != null && h.longConeArray[i] != null
//						&& h.longConeArray[i].point != null && (int) h.shortConeArray[i].point.x > 0
//						&& (int) h.shortConeArray[i].point.x < (image.getWidth() * scale)
//						&& (int) h.shortConeArray[i].point.y > 0
//						&& (int) h.shortConeArray[i].point.y < (image.getHeight() * scale)) {
//					h.shortConeArray[i].input(image.getRGB((int) (h.shortConeArray[i].point.x * scale),
//							(int) (h.shortConeArray[i].point.y * scale)));
//					h.mediumConeArray[i].input(image.getRGB((int) (h.mediumConeArray[i].point.x * scale),
//							(int) (h.mediumConeArray[i].point.y * scale)));
//					h.longConeArray[i].input(image.getRGB((int) (h.longConeArray[i].point.x * scale),
//							(int) (h.longConeArray[i].point.y * scale)));
//				} else {
//					h.shortConeArray[i].input(Color.black.getRGB());
//					h.mediumConeArray[i].input(Color.black.getRGB());
//					h.longConeArray[i].input(Color.black.getRGB());
//				}
//			}
//			h.addCoincidence(h.getCoincidence(this.type), concept);
//		}
//
//		for (int i = size - 1; i >= 0; i--) {
//			level = this.getLevelStack().get(i);
//			level.propagate(concept);
//		}
//
//		if (concept == null) {
//			List<Concept> conceptList = this.getLevelStack().get(this.getIndex()).getCoincidenceConceptList();
//			concept = (conceptList.size() > 0) ? conceptList.get(0) : null;
//			if (concept != null) {// bConcept != null && aConcept.equals(bConcept)
//				belief = new Belief(concept, new Point(this.x, this.y));
////				this.beliefList.add(belief);
//			}
////			double width = 13;
////			double height = 13;
////			if (sleep > 0) {
////				for (Belief b : this.beliefList) {
////					if (b.concept.value.equals("white"))
////						graphics2D.setColor(Color.RED);
////					if (b.concept.value.equals("human"))
////						graphics2D.setColor(Color.GREEN);
////					if (b.concept.value.equals("ladder"))
////						graphics2D.setColor(Color.BLUE);
////					if (b.concept.value.equals("house"))
////						graphics2D.setColor(Color.YELLOW);
////					if (b.concept.value.equals("tree"))
////						graphics2D.setColor(Color.PINK);
////					double newX = b.point.x - width / 2.0;
////					double newY = b.point.y - height / 2.0;
////					Ellipse2D.Double ellipse = new Ellipse2D.Double(newX, newY, width, height);
////					graphics2D.draw(ellipse);
////				}
////			}
//		}
//	}
//	return belief;
//}
//public List<Concept> process(BufferedImage image, double scale, Concept concept) {
////System.out.println("process(image,scale, "+concept+")");
//Belief belief = null;
//int size = this.getLevelList().size();
//Level level = this.getInputLevel();
//if (level != null && image != null) {
//for (Hexagon h : level.getHexagonList()) {
//	h.initCells();
//	for (int i = 0; i < h.SIDES; i++) {
//		if (h.shortConeArray[i] != null && h.mediumConeArray[i] != null && h.longConeArray[i] != null
//				&& (int) h.xpoints[i] > 0 && (int) h.xpoints[i] < (image.getWidth() * scale)
//				&& (int) h.ypoints[i] > 0 && (int) h.ypoints[i] < (image.getHeight() * scale)) {
//			h.shortConeArray[i]
//					.input(image.getRGB((int) (h.xpoints[i] * scale), (int) (h.ypoints[i] * scale)));
//			h.mediumConeArray[i]
//					.input(image.getRGB((int) (h.xpoints[i] * scale), (int) (h.ypoints[i] * scale)));
//			h.longConeArray[i]
//					.input(image.getRGB((int) (h.xpoints[i] * scale), (int) (h.ypoints[i] * scale)));
//		} else {
//			h.shortConeArray[i].input(Color.black.getRGB());
//			h.mediumConeArray[i].input(Color.black.getRGB());
//			h.longConeArray[i].input(Color.black.getRGB());
//		}
//	}
//	h.addCoincidence(h.getCoincidence(this.type), concept, false);
//}
//this.propagate(concept);
//}
//
////if (concept == null) {
////List<Concept> conceptList = this.getRootLevel().getCoincidenceConceptList();
////concept = (conceptList.size() > 0) ? conceptList.get(0) : null;
//////if (concept != null) {// bConcept != null && aConcept.equals(bConcept)
//////	belief = new Belief(concept, new Point(this.x, this.y));
//////}
//////System.out.println(concept);
////}
//
//return this.getRootLevel().getCoincidenceConceptList();
//}
//
//public void process(Graphics2D graphics2D, BufferedImage image, double scale, Concept concept, int sleep) {
//logger.info("processing...");
//Belief belief = null;
//int size = this.getLevelList().size();
//Level level = this.getInputLevel();
//if (level != null && image != null) {
//for (Hexagon h : level.getHexagonList()) {
//	if (sleep > 0) {
//		graphics2D.drawPolygon(h.doubleToIntArray(h.xpoints), h.doubleToIntArray(h.ypoints),
//				(int) h.npoints);
//	}
//	h.initCells();
//	for (int i = 0; i < h.SIDES; i++) {
//		if (h.shortConeArray[i] != null && h.mediumConeArray[i] != null && h.longConeArray[i] != null
//				&& (int) h.xpoints[i] > 0 && (int) h.xpoints[i] < (image.getWidth() * scale)
//				&& (int) h.ypoints[i] > 0 && (int) h.ypoints[i] < (image.getHeight() * scale)) {
//			h.shortConeArray[i]
//					.input(image.getRGB((int) (h.xpoints[i] * scale), (int) (h.ypoints[i] * scale)));
//			h.mediumConeArray[i]
//					.input(image.getRGB((int) (h.xpoints[i] * scale), (int) (h.ypoints[i] * scale)));
//			h.longConeArray[i]
//					.input(image.getRGB((int) (h.xpoints[i] * scale), (int) (h.ypoints[i] * scale)));
//		} else {
//			h.shortConeArray[i].input(Color.black.getRGB());
//			h.mediumConeArray[i].input(Color.black.getRGB());
//			h.longConeArray[i].input(Color.black.getRGB());
//		}
//	}
//	h.addCoincidence(h.getCoincidence(this.type), concept, false);
//}
//this.propagate(concept);
//}
//
//}
