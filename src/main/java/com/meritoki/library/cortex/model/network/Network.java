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
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Date;
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
import com.meritoki.library.cortex.model.Belief;
import com.meritoki.library.cortex.model.Concept;
import com.meritoki.library.cortex.model.Point;
import com.meritoki.library.cortex.model.cell.Wavelength;
import com.meritoki.library.cortex.model.cortex.Cortex;
import com.meritoki.library.cortex.model.network.hexagon.Hexagonal;
import com.meritoki.library.cortex.model.network.shape.Shape;
import com.meritoki.library.cortex.model.network.square.Squared;

/**
 * A Network Retains a List of Levels, Each Containing a Map of Shapes
 */
@JsonTypeInfo(use = Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = Hexagonal.class), @Type(value = Squared.class), })
public class Network extends Cortex {

	protected Logger logger = Logger.getLogger(Network.class.getName());
	@JsonIgnore
	protected LinkedList<Level> levelList = new LinkedList<>();

	public Network() {
		this.uuid = UUID.randomUUID().toString();
	}

	/**
	 * X & Y Are the initial origin of the Network
	 * 
	 * @param wavelength
	 * @param x
	 * @param y
	 */
	public Network(Wavelength[] wavelength, int x, int y) {
		logger.info("Network(" + wavelength + ", " + x + ", " + y + ")");
		this.wavelength = wavelength;
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
		logger.info("addLevel(" + level + ")");
		this.levelList.add(level);
	}

	@JsonIgnore
	public Level getLastLevel() {
		int size = this.getLevelList().size();
		Level level = (size > 0) ? this.getLevelList().get(size - 1) : null;
		logger.info("getLastLevel() level=" + level);
		return level;
	}

	@JsonIgnore
	public void setConcept(Concept concept) {
		for (Level level : this.levelList) {
			level.propagate(concept);
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
		super.load();
		logger.info("load()");
	}

	@JsonIgnore
	public void update() {
		logger.info("update()");
	}

//	/**
//	 * Function initializes the network of nodes, or squares, that converges into a
//	 * single root node.
//	 */
//	@JsonIgnore
//	public void input(Concept concept) {
//		logger.info("input(" + concept + ")");
//		Level level = this.getInputLevel();
//		List<Shape> shapeList = level.getShapeList();
//		for (Shape shape : shapeList) {
//			for(Wavelength w: this.wavelength) {
//				shape.addCoincidence(shape.getCoincidence(w), concept);
//			}
//		}
//	}

	@JsonIgnore
	public void propagate(Wavelength wavelength, Concept concept) {
//		logger.info("propogate(" + concept + ")");
		Level level = null;
		int size = this.getLevelList().size();
		for (int i = 0; i < size; i++) {
			level = this.getLevelList().get(i);
			if (i == 0) {
				level.input(wavelength, concept);
			} else {
				level.propagate(concept);
			}
		}
	}

	/**
	 * Invokes Level Feedback from Root to Children
	 * 
	 * @param concept
	 */
	@JsonIgnore
	public void feedback(Wavelength wavelength, Concept concept) {
		Level level = null;
		int size = this.getLevelList().size();
		for (int i = size - 1; 0 <= i; i--) {
			level = this.getLevelList().get(i);
			if (i > 0) {
				level.feedback(concept);
			} else {
				level.output(wavelength);
			}
		}
	}

	/**
		 * Function Checked 20250604, will work if Shape Cell Array Has Values.
		 * Used to make image of input data to be retained for the purpose of subsequent
		 * potential input. It retains the state of the cells as sensors. If Red, Only
		 * Red If Blue, Only Blue If Green, Only Green If Color, Only Color If Gray,
		 * Only Gray
		 * 
		 * @param bufferedImage
		 * @param dimension
		 * @param shape
		 */
		public void bufferedImageSetRGB(BufferedImage bufferedImage, int dimension, Shape shape) {
	//		logger.info("bufferedImageSetRGB("+(bufferedImage!=null)+","+dimension+", "+shape+")");
			for (int i = 0; i < shape.n; i++) {
				// Must always use CONE SHORT, MEDIUM, LONG BECAUSE ROD_GRAY OUTPUTS TO RED,
				// GREEN, BLUE
				double x = shape.xDimension[i] - this.origin.x;
				double y = shape.yDimension[i] - this.origin.y;
	//			if (bufferedImage != null && shape.cellArray[i] != null
	//					&& (int) shape.xDimension[i] > 0
	//					&& (int) shape.xDimension[i] < (bufferedImage.getWidth()) && (int) shape.yDimension[i] > 0
	//					&& (int) shape.yDimension[i] < (bufferedImage.getHeight())) {
				Color color = new Color(shape.cellArray[i].getWavelength(Wavelength.CONE_SHORT),
						shape.cellArray[i].getWavelength(Wavelength.CONE_MEDIUM),
						shape.cellArray[i].getWavelength(Wavelength.CONE_LONG));
	//			logger.info("bufferedImageSetRGB("+(bufferedImage!=null)+","+dimension+", "+shape+") color="+color);
				bufferedImage.setRGB((int) x + dimension / 2, (int) y + dimension / 2, color.getRGB());
	//			}
			}
		}

	/**
	 * Network has a position, specified by Origin Network has Levels, including
	 * Root and Input
	 */
	@Override
	@JsonIgnore
	public void process(Graphics2D graphics2D, BufferedImage bufferedImage, Concept concept) {
		logger.info("process(" + String.valueOf(graphics2D != null) + ", " + String.valueOf(bufferedImage != null)
				+ ", " + concept + ")");
		Level level = this.getInputLevel();
		if (level != null) {
			/**
			 * Initialize and Set the Shape Cell Values: Red, Green, Blue
			 */
			for (Shape shape : level.getShapeList()) {
				shape.initCells();
				for (int i = 0; i < shape.n; i++) {
					if (bufferedImage != null && shape.cellArray[i] != null && (int) shape.xDimension[i] > 0
							&& (int) shape.xDimension[i] < (bufferedImage.getWidth()) && (int) shape.yDimension[i] > 0
							&& (int) shape.yDimension[i] < (bufferedImage.getHeight())) {
						shape.cellArray[i]
								.input(bufferedImage.getRGB((int) (shape.xDimension[i]), (int) (shape.yDimension[i])));
					} else {
						shape.cellArray[i].input(Color.black.getRGB());
					}
				}
			}
			/**
			 * For All Input Shapes, Set Short, Medium, & Long
			 */
			for(Wavelength w: this.wavelength) {
				for (Shape shape : level.getShapeList()) {
					shape.addCoincidence(shape.getCoincidence(w), concept);
				}
				this.propagate(w,concept);
				this.feedback(w,concept);
				for (Shape shape : level.getShapeList()) {
					shape.setCellArray(shape.coincidence, w);
				}
			}
//			shape.addCoincidence(shape.getCoincidence(this.wavelength), concept);
			int dimension = (int) (this.getRadius() * 2);
			BufferedImage beliefBufferedImage = new BufferedImage(dimension, dimension, BufferedImage.TYPE_INT_RGB);
			for (Shape shape : level.getShapeList()) {
				this.bufferedImageSetRGB(beliefBufferedImage, dimension, shape);
			}

			Belief belief = new Belief();
			List<Point> pointList = new ArrayList<>();
			List<Concept> conceptList = this.getRootLevel().getCoincidenceConceptList();
			for (Shape shape : level.getShapeList()) {
				Point point = new Point(shape.xDimension[0], shape.yDimension[0]);
				int brightness = shape.coincidence.list.get(0);
				if (255 > brightness && brightness > 0) {
					pointList.add(point);
				}
//					Color color = new Color(brightness, brightness, brightness);
//					graphics2D.setColor(color);
//					graphics2D.drawPolygon(shape.doubleToIntArray(shape.xDimension),
//							shape.doubleToIntArray(shape.yDimension), (int) shape.n);
			}
			// When belief is initialized, it has the global coordinates of where Cortex was
			// on a plane.
			// We actually want this information
			// Beliefs are drawn where they are found in a plane.
			// Point List consists of points that are centered around
			// belief origin. @ least one point in Point List is equal to origin.
			belief.setConceptList(this.conceptMap, conceptList);
			belief.coincidence = this.getRootLevel().getCoincidenceList().get(0);
			belief.pointList = new ArrayList<>(pointList);
			belief.bufferedImage = (beliefBufferedImage);
			belief.origin = new Point(this.origin);
			belief.date = new Date();
			this.beliefList.add(belief);
			this.setIndex(this.beliefList.size() - 1);
		}
	}

//	//Same as loop above
//	@Override
//	public List<Point> getPointList() {
//		List<Point> pointList = null;
//		Level level = this.getInputLevel();
//		if (level != null) {
//			pointList = new ArrayList<>();
//			for (Shape shape : level.getShapeList()) {
//				shape.initCells();
//				Point point = new Point(shape.xpoints[0],shape.ypoints[0]);
//				int brightness = shape.coincidence.list.get(0);
//				if(255 > brightness && brightness > 0 ) {
//					point.conceptList = shape.getConceptList(shape.coincidence);
//					pointList.add(point);
//				}
//			}
//		}
//		return pointList;
//	}
}
//color = new Color(shape.cellArray[i].getWavelength(Wavelength.ROD_GRAY)
//graphics2D.setColor(color);
//graphics2D.drawPolygon(shape.doubleToIntArray(shape.xDimension), shape.doubleToIntArray(shape.yDimension),
//		(int) shape.n);

//for (int i = 0; i < shape.n; i++) {
//	//Must always use CONE SHORT, MEDIUM, LONG BECAUSE ROD_GRAY OUTPUTS TO RED, GREEN, BLUE
//	Color color = new Color(shape.cellArray[i].getWavelength(Wavelength.CONE_SHORT), shape.cellArray[i].getWavelength(Wavelength.CONE_MEDIUM), shape.cellArray[i].getWavelength(Wavelength.CONE_LONG));
//	double x = (shape.xDimension[i]-this.origin.x);
//	double y = (shape.yDimension[i]-this.origin.y);
//	beliefBufferedImage.setRGB((int)x+dimension/2,(int)y+dimension/2, color.getRGB());
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