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
import java.awt.geom.AffineTransform;
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
import com.meritoki.library.controller.node.NodeController;
import com.meritoki.library.cortex.model.hexagon.Hexagonal;
import com.meritoki.library.cortex.model.retina.Retina;
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
		logger.info("Network("+type+", "+x+", "+y+")");
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
			level.propagate(concept, true, false);
		}
	}
	
	@JsonIgnore
	public void setShapeMap(Map<String, Shape> shapeMap) {
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
		logger.info("update()");
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
			shape.addCoincidence(shape.getCoincidence(this.type), concept, true);
		}
	}
	
	@JsonIgnore
	public void propagate(Concept concept, boolean flag) {
//		logger.info("propogate(" + concept + ")");
		Level level = null;
		int size = this.getLevelList().size();
		for (int i = 0; i < size; i++) {
			level = this.getLevelList().get(i);
			if (flag && i == 0) {
				level.input(this.type, concept);
			} else {
//				if(i == 0) {
//					level.propagate(concept, true, true);
//				} else 
				if (i == size - 1) {
					level.propagate(concept, true, true);
				} else {
					level.propagate(concept, true, false);
				}
			}
		}
	}
	
	@JsonIgnore
	public void feedback(Concept concept) {
		Level level = null;
		int size = this.getLevelList().size();
		for (int i = size-1; 0 < i; i--) {
			level = this.getLevelList().get(i);
			level.feedback(concept, false);
		}
	}
	
	@Override
	@JsonIgnore
	public void process(Graphics2D graphics2D, BufferedImage bufferedImage, Concept concept) {
		Level level = this.getInputLevel();
		if (level != null) {
			for (Shape shape : level.getShapeList()) {
				shape.initCells();
				for (int i = 0; i < shape.sides+1; i++) {
					if (bufferedImage != null && shape.shortConeArray[i] != null && shape.mediumConeArray[i] != null && shape.longConeArray[i] != null
							&& (int) shape.xpoints[i] > 0 && (int) shape.xpoints[i] < (bufferedImage.getWidth())
							&& (int) shape.ypoints[i] > 0 && (int) shape.ypoints[i] < (bufferedImage.getHeight())) {
						shape.shortConeArray[i]
								.input(bufferedImage.getRGB((int) (shape.xpoints[i]), (int) (shape.ypoints[i])));
						shape.mediumConeArray[i]
								.input(bufferedImage.getRGB((int) (shape.xpoints[i]), (int) (shape.ypoints[i])));
						shape.longConeArray[i]
								.input(bufferedImage.getRGB((int) (shape.xpoints[i]), (int) (shape.ypoints[i])));
					} else {
						shape.shortConeArray[i].input(Color.black.getRGB());
						shape.mediumConeArray[i].input(Color.black.getRGB());
						shape.longConeArray[i].input(Color.black.getRGB());
					}
				}
				shape.addCoincidence(shape.getCoincidence(this.type), concept, false);
			}
			this.propagate(concept,true);
			this.feedback(concept);
			for (Shape shape : level.getShapeList()) {
				shape.initCells();
				for (int i = 1; i < shape.sides+1; i++) {
					int brightness = shape.coincidence.list.get(0);
					Color color = new Color(brightness, brightness, brightness);
					graphics2D.setColor(color);
					graphics2D.drawPolygon(shape.doubleToIntArray(shape.xpoints), shape.doubleToIntArray(shape.ypoints),(int) shape.npoints);
				}
			}
		}
	}
}
