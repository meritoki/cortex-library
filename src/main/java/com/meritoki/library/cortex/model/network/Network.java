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

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
//import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.meritoki.library.cortex.model.network.hexagon.Hexagonal;
import com.meritoki.library.cortex.model.network.square.Squared;
import com.meritoki.library.cortex.model.unit.Concept;
import com.meritoki.library.cortex.model.unit.Point;

@JsonTypeInfo(use = Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @com.fasterxml.jackson.annotation.JsonSubTypes.Type(value = Hexagonal.class),
		@com.fasterxml.jackson.annotation.JsonSubTypes.Type(value = Squared.class), })
public class Network extends Cortex {

	protected static Logger logger = LoggerFactory.getLogger(Network.class.getName());
	@JsonIgnore
	protected LinkedList<Level> levelList = new LinkedList<>();

	public Network() {
		this.uuid = UUID.randomUUID().toString();
	}

	public Network(Type[] typeList, int x, int y) {
		logger.info("Network(" + typeList + ", " + x + ", " + y + ")");
		this.typeArray = typeList;
		this.origin = new Point(x, y);
		this.uuid = UUID.randomUUID().toString();
	}

	public Network(int x, int y) {
		logger.info("Network(" + x + ", " + y + ")");
		this.origin = new Point(x, y);
		this.uuid = UUID.randomUUID().toString();
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
		logger.debug("addLevel(" + level + ")");
		this.levelList.add(level);
	}

	@JsonIgnore
	public Level getLastLevel() {
		Level level = this.levelList.get(this.levelList.size() - 1);
		logger.debug("getLastLevel() level=" + level);
		return level;
	}

	@JsonIgnore
	public void setConcept(Concept concept) {
		for (Level level : this.levelList) {
			for (Type type : this.typeArray) {
				level.propagate(type, concept);
			}
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
	public LinkedList<Shape> getShapeList(Map<String, Shape> shapeMap) {
		LinkedList<Shape> shapeList = new LinkedList<>();
		for (Map.Entry<String, Shape> entry : shapeMap.entrySet()) {
			shapeList.add(entry.getValue());
		}
		return shapeList;
	}

	@JsonIgnore
	public Level getRoot() {
		int size = this.getLevelList().size();
		Level level = (size > 0) ? this.getLevelList().get(size - 1) : null;
		return level;
	}

	@JsonIgnore
	public Level getInput() {
		int size = this.getLevelList().size();
		Level level = (size > 0) ? this.getLevelList().get(0) : null;
		return level;
	}

	@JsonIgnore
	public void load() {
		super.load();
		logger.info("load()");
	}

	@JsonIgnore
	public void update() {
		logger.info("update()");
	}

	@JsonIgnore
	@Override
	public void process(BufferedImage bufferedImage, Point origin, Concept concept) {
		logger.info("process(bufferedImage=" + String.valueOf(bufferedImage != null) + ", " + origin + ", " + concept
				+ ")");
		this.setOrigin((int) (origin.x), (int) (origin.y));// Origin is used;
		this.update();
		Level input = this.getInput();
		if (input != null) {
			for (Shape shape : input.getShapeList()) {
				shape.initCells();
				for (int i = 0; i < shape.pointList.size(); i++) {
					if (bufferedImage != null && shape.coneArray[i] != null && shape.rodArray[i] != null
							&& (int) shape.pointList.get(i).x > 0
							&& (int) shape.pointList.get(i).x < (bufferedImage.getWidth())
							&& (int) shape.pointList.get(i).y > 0
							&& (int) shape.pointList.get(i).y < (bufferedImage.getHeight())) {

						shape.coneArray[i].input(bufferedImage.getRGB((int) (shape.pointList.get(i).x),
								(int) (shape.pointList.get(i).y)));
						shape.rodArray[i].input(bufferedImage.getRGB((int) (shape.pointList.get(i).x),
								(int) (shape.pointList.get(i).y)));
					} else {
						shape.coneArray[i].input(Color.black.getRGB());
						shape.rodArray[i].input(Color.black.getRGB());
					}
				}
				for (Type type : this.typeArray) {
					shape.addCoincidence(type, shape.getCoincidence(type), concept);
				}
			}
			for (Type type : this.typeArray) {
				this.propagate(type, concept);
			}
			Level root = this.getRoot();
			List<Shape> shapeList = root.getShapeList();
			if (shapeList.size() > 0) {
				Shape shape = shapeList.get(0);
				for (Type type : this.typeArray) {
					logger.info("process(...) type=" + shape.typeCoincidenceMap.get(type).list.size());
				}
			}
			for (Type type : this.typeArray) {
				this.feedback(type, concept);
			}
		}
	}

	@JsonIgnore
	public void propagate(Type type, Concept concept) { // , boolean inputFlag) {
		logger.info("propogate(" + type + ", " + concept + ")");// + inputFlag + ")");
		Level level;
		int size = this.getLevelList().size();
		for (int i = 0; i < size; i++) {
			level = this.getLevelList().get(i);
			level.propagate(type, concept);

		}
	}

	@JsonIgnore
	public void feedback(Type type, Concept concept) {
		logger.info("feedback(" + type + ", " + concept + ")");
		Level level = null;
		int size = this.getLevelList().size();
		for (int i = size - 1; 0 < i; i--) {
			level = this.getLevelList().get(i);
			level.feedback(type, concept, false);
		}
	}
}
//for (ColorType type : this.typeArray) {
//shape.addCoincidence(type, shape.getCoincidence(type), concept, false);
//}
//if (inputFlag && i == 0) {
//level.input(type, concept);
//} else 
//{
//if (i == size - 1) {
//level.propagate(type, concept, true);
//} else {
//level.propagate(type, concept, true);
//}
//}
/**
 * Function initializes the network of nodes, or squares, that converges into a
 * single root node.
 */
//@JsonIgnore
//public void input(Concept concept) {
//	logger.info("input()");
//	Level level = this.getInputLevel();
//	List<Shape> shapeList = level.getShapeList();
//	for (Shape shape : shapeList) {
//		shape.addCoincidence(shape.getCoincidence(this.type), concept, true);
//	}
//}
//
//@JsonIgnore
//public void propagate(Concept concept, boolean flag) {
////	logger.info("propogate(" + concept + ")");
//	Level level = null;
//	int size = this.getLevelList().size();
//	for (int i = 0; i < size; i++) {
//		level = this.getLevelList().get(i);
//		if (flag && i == 0) {
//			level.input(this.type, concept);
//		} else {
////			if(i == 0) {
////				level.propagate(concept, true, true);
////			} else 
//			if (i == size - 1) {
//				level.propagate(concept, true, true);
//			} else {
//				level.propagate(concept, true, false);
//			}
//		}
//	}
//}
//if(i == 0) {
//level.propagate(concept, true, true);
//} else 
//shape.shortConeArray[i]
//.input(bufferedImage.getRGB((int) (shape.pointList.get(i).x), (int) (shape.pointList.get(i).y)));
//shape.mediumConeArray[i]
//.input(bufferedImage.getRGB((int) (shape.pointList.get(i).x), (int) (shape.pointList.get(i).y)));
//shape.longConeArray[i]
//.input(bufferedImage.getRGB((int) (shape.pointList.get(i).x), (int) (shape.pointList.get(i).y)));
//shape.shortConeArray[i].input(Color.black.getRGB());
//shape.mediumConeArray[i].input(Color.black.getRGB());
//if (graphics2D != null) {
////Belief belief = new Belief();
//for (Shape shape : level.getShapeList()) {
////int brightness = shape.coincidenceMap.get(ColorType.BRIGHTNESS).list.get(0);
////int red = shape.coincidenceMap.get(ColorType.RED).list.get(0);
////int green = shape.coincidenceMap.get(ColorType.GREEN).list.get(0);
////int blue = shape.coincidenceMap.get(ColorType.BLUE).list.get(0);
//Color c = Color.YELLOW;//new Color(red, green, blue);
//graphics2D.setColor(c);
//graphics2D.drawPolygon(shape.doubleToIntArray(shape.getXPoints()), shape.doubleToIntArray(shape.getYPoints()),
//		(int) shape.getNPoints());
//}
//
//}
//@JsonIgnore
//public int getX() {
//	return this.x;
//}
//
//@JsonIgnore
//public int getY() {
//	return this.y;
//}
//if (bufferedImage != null && shape.shortConeArray[i] != null && shape.mediumConeArray[i] != null
//&& shape.longConeArray[i] != null && (int) shape.xpoints[i] > 0
//&& (int) shape.xpoints[i] < (bufferedImage.getWidth()) && (int) shape.ypoints[i] > 0
//&& (int) shape.ypoints[i] < (bufferedImage.getHeight())) {
//shape.shortConeArray[i]
//	.input(bufferedImage.getRGB((int) (shape.xpoints[i]), (int) (shape.ypoints[i])));
//shape.mediumConeArray[i]
//	.input(bufferedImage.getRGB((int) (shape.xpoints[i]), (int) (shape.ypoints[i])));
//shape.longConeArray[i]
//	.input(bufferedImage.getRGB((int) (shape.xpoints[i]), (int) (shape.ypoints[i])));
//shape.rodArray[i]
//	.input(bufferedImage.getRGB((int) (shape.xpoints[i]), (int) (shape.ypoints[i])));
//} else {
//shape.shortConeArray[i].input(Color.black.getRGB());
//shape.mediumConeArray[i].input(Color.black.getRGB());
//shape.longConeArray[i].input(Color.black.getRGB());
//shape.rodArray[i].input(Color.black.getRGB());
//}
////Same as loop above
//@Override
//public List<Point> getPointList() {
//	List<Point> pointList = null;
//	Level level = this.getInputLevel();
//	if (level != null) {
//		pointList = new ArrayList<>();
//		for (Shape shape : level.getShapeList()) {
//			shape.initCells();
//			Point point = new Point(shape.xpoints[0],shape.ypoints[0]);
//			int brightness = shape.coincidence.list.get(0);
//			if(255 > brightness && brightness > 0 ) {
//				point.conceptList = shape.getConceptList(shape.coincidence);
//				pointList.add(point);
//			}
//		}
//	}
//	return pointList;
//}
//@JsonIgnore
//public void feedback(Concept concept) {
//	Level level = null;
//	int size = this.getLevelList().size();
//	for (int i = size - 1; 0 < i; i--) {
//		level = this.getLevelList().get(i);
//		level.feedback(concept, false);
//	}
//}
//@Override
//@JsonIgnore
//public void process(Graphics2D graphics2D, BufferedImage bufferedImage, Concept concept) {
//	logger.info("process(" + String.valueOf(graphics2D!=null) + ", "+String.valueOf(bufferedImage != null)+", " + concept + ")");
//	Level level = this.getInputLevel();
//	if (level != null) {
//		for (Shape shape : level.getShapeList()) {
//			shape.initCells();
//			for (int i = 0; i < shape.sides + 1; i++) {
//				if (bufferedImage != null && shape.shortConeArray[i] != null && shape.mediumConeArray[i] != null
//						&& shape.longConeArray[i] != null && (int) shape.xpoints[i] > 0
//						&& (int) shape.xpoints[i] < (bufferedImage.getWidth()) && (int) shape.ypoints[i] > 0
//						&& (int) shape.ypoints[i] < (bufferedImage.getHeight())) {
//					shape.shortConeArray[i]
//							.input(bufferedImage.getRGB((int) (shape.xpoints[i]), (int) (shape.ypoints[i])));
//					shape.mediumConeArray[i]
//							.input(bufferedImage.getRGB((int) (shape.xpoints[i]), (int) (shape.ypoints[i])));
//					shape.longConeArray[i]
//							.input(bufferedImage.getRGB((int) (shape.xpoints[i]), (int) (shape.ypoints[i])));
//				} else {
//					shape.shortConeArray[i].input(Color.black.getRGB());
//					shape.mediumConeArray[i].input(Color.black.getRGB());
//					shape.longConeArray[i].input(Color.black.getRGB());
//				}
//			}
//			shape.addCoincidence(shape.getCoincidence(this.type), concept, false);
//		}
//		this.propagate(concept, true);
////		this.feedback(concept);
//
//		//Here we make a coincidence
//		//Can draw on image
//		if (graphics2D != null) {
////			System.out.println("graphics2D != null");
//			int dimension = (int)(this.getRadius()*2);
//			BufferedImage beliefBufferedImage = new BufferedImage(dimension, dimension, BufferedImage.TYPE_INT_RGB);
//			List<Point> pointList = new ArrayList<>();
//			List<Concept> conceptList = this.getRootLevel().getCoincidenceConceptList();
//			Belief belief = new Belief();
//			for (Shape shape : level.getShapeList()) {
////				Point point = new Point(shape.xpoints[0],shape.ypoints[0]);//Implement w/ Center
//				int brightness = shape.coincidence.list.get(0);
////				if(255 > brightness && brightness > 0 ) {
////					pointList.add(point);
////				}
//				Color color = new Color(brightness, brightness, brightness);
//				graphics2D.setColor(color);
//				graphics2D.drawPolygon(shape.doubleToIntArray(shape.xpoints), shape.doubleToIntArray(shape.ypoints),
//						(int) shape.npoints);
//				
//				for (int i = 0; i < shape.sides; i++) {
//					color = new Color(brightness, brightness, brightness);
//					double x = (shape.xpoints[i]-this.origin.x);
//					double y = (shape.ypoints[i]-this.origin.y);
//					beliefBufferedImage.setRGB((int)x+dimension/2,(int)y+dimension/2, color.getRGB());
//				}
//			}
//			//When belief is initialized, it has the global coordinates of where Cortex was
//			//on a plane.
//			//We actually want this information
//			//Beliefs are drawn where they are found in a plane.
//			//Point List consists of points that are centered around 
//			//belief origin. @ least one point in Point List is equal to origin.
//			belief.setConceptList(this.conceptMap, conceptList);
//			belief.coincidence = this.getRootLevel().getCoincidenceList().get(0);
//			belief.pointList = new ArrayList<>(pointList);
//			belief.bufferedImage = (beliefBufferedImage);
//			belief.origin = new Point(this.origin);
//			belief.date = new Date();
////			this.addBelief(belief);
//			this.beliefList.add(belief);
//			//Normailization ruins this information, but we still
//			//want the result of normalization.
//			//We want beliefs represented relative to origin.
//			//The same implementation we have now, for the most part.
//			//
//			
////			this.add(belief);
////			System.out.println("this.setIndex(...) flag="+);
//			this.setIndex(this.beliefList.size()-1);
//		}
//	}
//}
