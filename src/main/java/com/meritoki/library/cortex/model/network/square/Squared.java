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
package com.meritoki.library.cortex.model.network.square;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.meritoki.library.cortex.model.Node;
import com.meritoki.library.cortex.model.Point;
import com.meritoki.library.cortex.model.network.Color;
import com.meritoki.library.cortex.model.network.Level;
import com.meritoki.library.cortex.model.network.Network;
import com.meritoki.library.cortex.model.network.shape.Shape;

/**
 * In Network, squares are referenced by level and relative coordinates, i.e.
 * 0:00, 4:00
 * 
 * @author osvaldo.rodriguez
 *
 */
public class Squared extends Network {

	public static void main(String[] args) {
		Squared n = new Squared(Color.BRIGHTNESS, 0, 0, 5, 1, 0);
		n.load();
	}
	@JsonIgnore
	protected Logger logger = Logger.getLogger(Squared.class.getName());

	public Squared() {
		super(Color.BRIGHTNESS, 0, 0);
		this.length = 9;
	}

	public Squared(int dimension, int length, int padding) {
		super(Color.BRIGHTNESS, 0, 0);
		this.dimension = dimension;
		this.length = length;
		this.padding = padding;
//		this.length = 9;//
	}

	public Squared(Color type, int x, int y, int dimension, int length, int padding) {
		super(type, x, y);
		this.dimension = dimension;
		this.length = length;
		this.padding = padding;
//		this.length = 9;
	}

	/**
	 * Need to modify so that it only moves the input level, not all levels this
	 * will improve the performance of the algorithm.
	 */
	@JsonIgnore
	public void update() {
		Map<String, Square> squareMap = new HashMap<>();
		int half = dimension / 2;
		Square square = null;
		Level level = this.getInputLevel();
		double xLeg = (length / 2) - (padding / 2);
		double yLeg = xLeg;
		double radius = Math.sqrt(Math.pow(xLeg, 2) + Math.pow(yLeg, 2));
		for (int row = 0; row < dimension; row++) {
			for (int column = 0; column < dimension; column++) {
				int xPosition = column - half;
				int yPosition = row - half;
				double x = (this.origin.x + (xPosition * length));
				double y = (this.origin.y + (yPosition * length));
				square = (Square) level.shapeMap.get(xPosition + "," + yPosition);
				if (square != null) {
					square.setCenter(new Point(x, y));
				}
			}
		}
	}

	/**
	 * Function uses squareMap initialized with loaded parameters, to generate keys
	 * that retrieve loaded squares from file and connects them in parent child
	 * relationships, forming a tree data structure that converges on a root not
	 */
	@JsonIgnore
	@Override
	public void load() {
		logger.info("load()");
		logger.info("load() this.shapeMap=" + this.shapeMap);
		logger.info("load() this.dimension=" + this.dimension);
		logger.info("load() this.length=" + this.length);
		Map<String, Shape> squareMap = getShapeMap(-1, new Point(this.origin.x, this.origin.y), this.dimension, this.length,
				this.padding);
		int depth = (this.depth > 0) ? this.depth : this.getDepth(squareMap.size());
		if (this.depth == 0) {
			this.depth = depth;
		}
		Level level = new Level();
		List<Shape> squareList = this.getShapeList(squareMap);
		Shape square = null;
		for (Shape n : squareList) {
			square = this.shapeMap.get("0:" + n);
			if (square == null) {
				square = new Square(n);
				this.shapeMap.put("0:" + square, square);
			}
			square.setData("0:" + square);
			square.initCells();
			level.addShape(null,square);
		}
		this.addLevel(level);
		LinkedList<Shape> squareStack = null;
		int exponent = 0;
		for (int i = 1; i < depth; i++) {
			logger.fine("load() i=" + i);
			exponent = i;
			logger.fine("load() exponent=" + exponent);
			squareMap = this.getLastLevel().getShapeMap();
			level = new Level();
			squareList = new LinkedList<>();
			squareStack = new LinkedList<>();
			squareStack.push(squareMap.get(this.origin.x + "," + this.origin.y));
			Shape shape;
			while (!squareStack.isEmpty()) {
				shape = squareStack.pop();
				LinkedList<Shape> list = getGroupZeroSquareList(squareMap, shape.getX(), shape.getY(), exponent);
				for (Shape s : list) {
					if (!squareList.contains(s)) {
						squareList.add(s);
						squareStack.push(s);
					}
				}
			}
			for (Shape m : squareList) {
				Shape s = this.shapeMap.get(i + ":" + m);
				if (s == null) {
					s = new Square(m);
					s.length = 9;
					this.shapeMap.put(i + ":" + s, s);
				}
				List<Shape> list = this.getGroupZeroSquareList(squareMap, s.getX(), s.getY(), exponent - 1);
				for (Shape n : list) {
					s.addChild(n);
				}
				s.setData(i + ":" + s);
				level.addShape(null,s);
			}
			this.addLevel(level);
		}
		 level = this.getRootLevel();
		 Shape h = level.getShapeList().get(0);
//		 Node.printTree(h, " ");
	}

	@JsonIgnore
	public LinkedList<Shape> getGroupZeroSquareList(Map<String, Shape> squareMap, int x, int y, int exponent) {
		// System.out.println("getGroupZeroSquareList("+squareMap.size()+", "+x+",
		// "+y+", "+exponent+")");
		LinkedList<Shape> squareList = new LinkedList<>();
		Shape h = null;
		int multiplier = (int) Math.pow(2, exponent);
		// System.out.println("multiplier="+multiplier);
		// (0,0)
		h = squareMap.get((x) + "," + (y));
		if (h != null) {
			squareList.push(h);
		}
		// (0,+3)
		h = squareMap.get((x) + "," + (y + (1 * multiplier)));
		if (h != null) {
			squareList.push(h);
		}
		// (+3,+3)
		h = squareMap.get((x + (1 * multiplier)) + "," + (y + (1 * multiplier)));
		if (h != null) {
			squareList.push(h);
		}
		// (+3,0)
		h = squareMap.get((x + (1 * multiplier)) + "," + (y));
		if (h != null) {
			squareList.push(h);
		}
		// (+3,-3)
		h = squareMap.get((x + (1 * multiplier)) + "," + (y - (1 * multiplier)));
		if (h != null) {
			squareList.push(h);
		}
		// (0,-3)
		h = squareMap.get((x) + "," + (y - (1 * multiplier)));
		if (h != null) {
			squareList.push(h);
		}
		// (-3,-3)
		h = squareMap.get((x - (1 * multiplier)) + "," + (y - (1 * multiplier)));
		if (h != null) {
			squareList.push(h);
		}
		// (-3,0)
		h = squareMap.get((x - (1 * multiplier)) + "," + (y));
		if (h != null) {
			squareList.push(h);
		}
		// (-3,+3)
		h = squareMap.get((x - (1 * multiplier)) + "," + (y + (1 * multiplier)));
		if (h != null) {
			squareList.push(h);
		}
		// System.out.println("getGroupZeroSquareList(squareMap,"+x+", "+y+",
		// "+exponent+") squareList="+squareList);
		return squareList;
	}

	public static Map<String, Shape> getShapeMap(int level, Point origin, int dimension, int length, int padding) {
		Map<String, Shape> shapeMap = new HashMap<>();
		int half = dimension / 2;
		Square square = null;
		double xLeg = (length / 2) - (padding / 2);
		double yLeg = xLeg;
		double radius = Math.sqrt(Math.pow(xLeg, 2) + Math.pow(yLeg, 2));
		for (int row = 0; row < dimension; row++) {
			for (int column = 0; column < dimension; column++) {
				int xPosition = column - half;
				int yPosition = row - half;
				// System.out.println(xPosition+" "+yPosition);
				double x = (origin.x + (xPosition * length));
				double y = (origin.y + (yPosition * length));
				square = new Square(xPosition, yPosition, new Point(x, y), radius);
				square.length = 9;
				if (level > -1)
					shapeMap.put(level + ":" + xPosition + "," + yPosition, square);
				else
					shapeMap.put(xPosition + "," + yPosition, square);
			}
		}
		return shapeMap;
	}

	@JsonIgnore
	public int getDepth(int value) {
		boolean flag = true;
		int count = 1;
		do {
			value /= 4;
			if (value <= 1) {
				flag = false;
			}
			logger.info("getDepth(" + value + ")");
			count++;
		} while (flag);
		logger.info("getDepth(" + value + ") count=" + count);
		return count;
	}
}

//public void process(Graphics2D graphics2D, BufferedImage image, double scale, Concept concept, int sleep) {
//logger.info("processing...");
//Belief belief = null;
//int size = this.getLevelList().size();
//Level level = this.getInputLevel();
//if (level != null && image != null) {
//	for (Square h : level.getSquareList()) {
//		if (sleep > 0) {
//			graphics2D.drawPolygon(h.doubleToIntArray(h.xpoints), h.doubleToIntArray(h.ypoints),
//					(int) h.npoints);
//		}
//		h.initCells();
//		for (int i = 0; i < h.SIDES; i++) {
//			if (h.shortConeArray[i] != null && h.mediumConeArray[i] != null && h.longConeArray[i] != null
//					&& (int) h.xpoints[i] > 0 && (int) h.xpoints[i] < (image.getWidth() * scale)
//					&& (int) h.ypoints[i] > 0 && (int) h.ypoints[i] < (image.getHeight() * scale)) {
//				h.shortConeArray[i]
//						.input(image.getRGB((int) (h.xpoints[i] * scale), (int) (h.ypoints[i] * scale)));
//				h.mediumConeArray[i]
//						.input(image.getRGB((int) (h.xpoints[i] * scale), (int) (h.ypoints[i] * scale)));
//				h.longConeArray[i]
//						.input(image.getRGB((int) (h.xpoints[i] * scale), (int) (h.ypoints[i] * scale)));
//			} else {
//				h.shortConeArray[i].input(Color.black.getRGB());
//				h.mediumConeArray[i].input(Color.black.getRGB());
//				h.longConeArray[i].input(Color.black.getRGB());
//			}
//		}
//		h.addCoincidence(h.getCoincidence(this.type), concept, false);
//	}
//	this.propagate(concept);
//}
//
//}
