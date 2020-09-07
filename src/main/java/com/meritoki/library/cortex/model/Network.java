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
package com.meritoki.library.cortex.model;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.meritoki.library.cortex.model.hexagon.Hexagonal;
import com.meritoki.library.cortex.model.square.Square;
import com.meritoki.library.cortex.model.square.Squared;
@JsonTypeInfo(use = Id.CLASS,
include = JsonTypeInfo.As.PROPERTY,
property = "type")
@JsonSubTypes({
@Type(value = Hexagonal.class),
@Type(value = Squared.class),
})
public class Network extends Cortex {
	
	protected Logger logger = Logger.getLogger(Network.class.getName());
	@JsonIgnore
	protected LinkedList<Level> levelList = new LinkedList<>();
	
	public Network() {
		this.uuid = UUID.randomUUID().toString();
	}
	
	public Network(int type, int x, int y) {
		this.type = type;
		this.x = x;
		this.y = y;
		this.uuid = UUID.randomUUID().toString();
	}

	@JsonIgnore
	public int getX() {
		return this.x;
	}

	@JsonIgnore
	public int getY() {
		return this.y;
	}
	

	@JsonIgnore
	public Map<String, Shape> getShapeMap() {
		return this.shapeMap;
	}

	@JsonIgnore
	public List<Level> getLevelList() {
		return this.levelList;
	}

	@JsonIgnore
	public void addLevel(Level level) {
		logger.info("addLevel(" + level + ")");
		this.levelList.add(level);
	}

	@JsonIgnore
	public Level getLastLevel() {
		Level level = this.levelList.get(this.levelList.size()-1);
		logger.info("getLastLevel() level=" + level);
		return level;
	}

	@JsonIgnore
	public void setConcept(Concept concept) {
		for (Level level : this.levelList) {
			level.propagate(0, concept, true);
		}
	}
	
	@JsonIgnore
	public void setShapeMap(Map<String, Shape> shapeMap) {
		// this.hexagonMap = hexagonMap;
		for (Map.Entry<String, Shape> entry : shapeMap.entrySet()) {
			String key = entry.getKey();
			Shape value = entry.getValue();
			this.shapeMap.put(key, value);
		}
	}
	
	@JsonIgnore
	public static LinkedList<Shape> getShapeList(Map<String, Shape> shapeMap) {
		LinkedList<Shape> shapeList = new LinkedList<>();
		for (Map.Entry<String, Shape> entry : shapeMap.entrySet()) {
			shapeList.add(entry.getValue());
		}
		return shapeList;
	}
	
	@JsonIgnore
	public Level getRootLevel() {
		int size = this.getLevelList().size();
		Level level = (size > 0) ? this.getLevelList().get(size - 1) : null;
		return level;
	}

	@JsonIgnore
	public Level getInputLevel() {
		int size = this.getLevelList().size();
		Level level = (size > 0) ? this.getLevelList().get(0) : null;
		return level;
	}
	
	@JsonIgnore
	public void load() {
		logger.info("load()");
	}
	
	@JsonIgnore
	public void update() {
//		switch (this.type) {
//		case HEXAGONAL: {
//			double radians = Math.toRadians(30);
//			double xOff = Math.cos(radians) * (this.radius + this.padding);
//			double yOff = Math.sin(radians) * (this.radius + this.padding);
//			int half = this.size / 2;
//			Shape hexagon = null;
//			for (int row = 0; row < this.size; row++) {
//				int cols = this.size - java.lang.Math.abs(row - half);
//				for (int col = 0; col < cols; col++) {
//					int xPosition = row < half ? col - row : col - half;
//					int yPosition = row - half;
//					int x = (int) (this.x + xOff * (col * 2 + 1 - cols));
//					int y = (int) (this.y + yOff * (row - half) * 3);
//					hexagon = this.shapeMap.get("0:" + xPosition + "," + yPosition);
//					if (hexagon != null) {
//						hexagon.setCenter(new Point(x, y));
//					}
//				}
//			}
//			break;
//		}
//		case SQUARED: {
//			int half = dimension / 2;
//			Square square = null;
//			double xLeg = (length / 2) - (padding / 2);
//			double yLeg = xLeg;
////			double radius = Math.sqrt(Math.pow(xLeg, 2)+Math.pow(yLeg,2));
//			for (int row = 0; row < dimension; row++) {
//				for (int column = 0; column < dimension; column++) {
//					int xPosition = column - half;
//					int yPosition = row - half;
////					System.out.println(xPosition+" "+yPosition);
//					double x = (this.x + (xPosition * length));
//					double y = (this.y + (yPosition * length));
//					square = (Square) this.shapeMap.get("0:" + xPosition + "," + yPosition);
//					if (square != null) {
//						square.setCenter(new Point(x, y));
//					}
//				}
//			}
//			break;
//		}
//		}

	}
	
	/**
	 * Function initializes the network of nodes, or squares, that converges into a
	 * single root node.
	 */
	@JsonIgnore
	public void input(Concept concept) {
		logger.info("input()");
		Level level = this.getInputLevel();
		List<Shape> shapeList = level.getShapeList();
		for (Shape shape : shapeList) {
			shape.addCoincidence(shape.getCoincidence(this.type), concept, false);
		}
	}
	
	@JsonIgnore
	public void propagate(Concept concept) {
//		logger.info("propogate(" + concept + ")");
		Level level = null;
		int size = this.getLevelList().size();
		for (int i = 0; i < size; i++) {
			level = this.getLevelList().get(i);
			if (i == 0) {
				level.input(this.type, concept);
			} else {
				if (i == size - 1) {
					level.propagate(0, concept, true);
				} else {
					level.propagate(0, concept, false);
				}
			}
		}
	}
	
	@Override
	@JsonIgnore
	public void process(Graphics2D graphics2D, BufferedImage image, Concept concept) {
		System.out.println("process("+image+", "+concept+")");
		Level level = this.getInputLevel();
		if (level != null) {
			for (Shape shape : level.getShapeList()) {
				if(graphics2D != null) {
					graphics2D.setColor(Color.YELLOW);
					graphics2D.drawPolygon(shape.doubleToIntArray(shape.xpoints), shape.doubleToIntArray(shape.ypoints),(int) shape.npoints);
				}
				shape.initCells();
				for (int i = 0; i < shape.sides; i++) {
					if (image != null && shape.shortConeArray[i] != null && shape.mediumConeArray[i] != null && shape.longConeArray[i] != null
							&& (int) shape.xpoints[i] > 0 && (int) shape.xpoints[i] < (image.getWidth())
							&& (int) shape.ypoints[i] > 0 && (int) shape.ypoints[i] < (image.getHeight())) {
						shape.shortConeArray[i]
								.input(image.getRGB((int) (shape.xpoints[i]), (int) (shape.ypoints[i])));
						shape.mediumConeArray[i]
								.input(image.getRGB((int) (shape.xpoints[i]), (int) (shape.ypoints[i])));
						shape.longConeArray[i]
								.input(image.getRGB((int) (shape.xpoints[i]), (int) (shape.ypoints[i])));
					} else {
						shape.shortConeArray[i].input(Color.black.getRGB());
						shape.mediumConeArray[i].input(Color.black.getRGB());
						shape.longConeArray[i].input(Color.black.getRGB());
					}
				}
				shape.addCoincidence(shape.getCoincidence(this.type), concept, false);
			}
			this.propagate(concept);
		}
	}
}
