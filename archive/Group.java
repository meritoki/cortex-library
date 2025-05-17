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
package com.meritoki.library.cortex.model.group;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.meritoki.library.cortex.model.Belief;
import com.meritoki.library.cortex.model.Concept;
import com.meritoki.library.cortex.model.Point;
import com.meritoki.library.cortex.model.cell.Wavelength;
import com.meritoki.library.cortex.model.cortex.Cortex;
import com.meritoki.library.cortex.model.network.Configuration;
import com.meritoki.library.cortex.model.network.Level;
import com.meritoki.library.cortex.model.network.Network;
import com.meritoki.library.cortex.model.network.hexagon.Hexagon;
import com.meritoki.library.cortex.model.network.hexagon.Hexagonal;
import com.meritoki.library.cortex.model.network.shape.Shape;
import com.meritoki.library.cortex.model.network.square.Square;
import com.meritoki.library.cortex.model.network.square.Squared;

public class Group extends Network {

	protected Logger logger = Logger.getLogger(Group.class.getName());
	@JsonProperty
	private Network brightness = null;
	@JsonProperty
	private Network red = null;
	@JsonProperty
	private Network green = null;
	@JsonProperty
	private Network blue = null;
	@JsonProperty
	private Shape root = new Shape();
	/**
	 * Goal is to provide a network where Root and Input Levels Can Can Be Read
	 */
	@JsonProperty
	private Network color = new Network();

	public Group() {
		this.uuid = UUID.randomUUID().toString();
	}

	/**
	 * Checked 202001191442 Good
	 */
	public Group(Configuration configuration) {
		this.configuration = configuration;
		switch (this.configuration) {
		case HEXAGONAL: {
			logger.info("HEXAGONAL");
			this.brightness = new Hexagonal(Wavelength.ROD_GRAY, (int) this.origin.x, (int) this.origin.y);
			this.red = new Hexagonal(Wavelength.CONE_SHORT, (int) this.origin.x, (int) this.origin.y);
			this.green = new Hexagonal(Wavelength.CONE_MEDIUM, (int) this.origin.x, (int) this.origin.y);
			this.blue = new Hexagonal(Wavelength.CONE_LONG, (int) this.origin.x, (int) this.origin.y);
			this.shapeMap = Hexagonal.getShapeMap(0, new Point(this.origin.x, this.origin.y), size, radius, padding);
			this.brightness.setShapeMap(this.shapeMap);
			this.red.setShapeMap(this.shapeMap);
			this.green.setShapeMap(this.shapeMap);
			this.blue.setShapeMap(this.shapeMap);
			break;
		}
		case SQUARED: {
			logger.info("SQUARED");
			this.brightness = new Squared(Wavelength.ROD_GRAY, (int) this.origin.x, (int) this.origin.y);
			this.red = new Squared(Wavelength.CONE_SHORT, (int) this.origin.x, (int) this.origin.y);
			this.green = new Squared(Wavelength.CONE_MEDIUM, (int) this.origin.x, (int) this.origin.y);
			this.blue = new Squared(Wavelength.CONE_LONG, (int) this.origin.x, (int) this.origin.y);
			this.shapeMap = Squared.getShapeMap(0, new Point(this.origin.x, this.origin.y), dimension, length, padding);
			this.brightness.setShapeMap(this.shapeMap);
			this.red.setShapeMap(this.shapeMap);
			this.green.setShapeMap(this.shapeMap);
			this.blue.setShapeMap(this.shapeMap);
			break;
		}
		}
	}

	@JsonIgnore
	public LinkedList<Hexagon> getHexagonList(Map<String, Hexagon> hexagonMap) {
		LinkedList<Hexagon> hexagonList = new LinkedList<Hexagon>();
		for (Map.Entry<String, Hexagon> entry : hexagonMap.entrySet()) {
			hexagonList.add((Hexagon) entry.getValue());
		}
		return hexagonList;
	}

//	public void setLevel(Level level) {
//		this.rootLevel = level;
//	}

	@Override
	public void load() {
		this.brightness.load();
		this.red.load();
		this.green.load();
		this.blue.load();
		// Get Root Level of Network to Input into New Level
		Shape brightnessShape = this.brightness.getRootLevel().getShapeList().get(0);
		Shape redShape = this.red.getRootLevel().getShapeList().get(0);
		Shape greenShape = this.green.getRootLevel().getShapeList().get(0);
		Shape blueShape = this.blue.getRootLevel().getShapeList().get(0);
		//
		brightnessShape.setData("brightness");
		redShape.setData("red");
		greenShape.setData("green");
		blueShape.setData("blue");
		//
		Level colorLevel = new Level();
		colorLevel.addShape("brightness", brightnessShape);
		colorLevel.addShape("red", redShape);
		colorLevel.addShape("green", greenShape);
		colorLevel.addShape("blue", blueShape);
		//
		this.color.addLevel(colorLevel);
		Level rootLevel = new Level();
		this.root.setData("root");
		this.root.size = 4;
		this.root.addChild(brightnessShape);
		this.root.addChild(redShape);
		this.root.addChild(greenShape);
		this.root.addChild(blueShape);
		rootLevel.addShape("root", this.root);
		Level inputLevel = new Level();
		inputLevel.shapeMap = this.shapeMap;
		this.color.addLevel(inputLevel);
		this.color.addLevel(colorLevel);
		this.color.addLevel(rootLevel);
	}

	@JsonIgnore
	public void update() {
//		logger.info("update()");
		switch (this.configuration) {
		case HEXAGONAL: {
			double radians = Math.toRadians(30);
			double xOff = Math.cos(radians) * (this.radius + this.padding);
			double yOff = Math.sin(radians) * (this.radius + this.padding);
			int half = this.size / 2;
			Shape shape = null;
			for (int row = 0; row < this.size; row++) {
				int cols = this.size - java.lang.Math.abs(row - half);
				for (int col = 0; col < cols; col++) {
					int xPosition = row < half ? col - row : col - half;
					int yPosition = row - half;
					int x = (int) (this.origin.x + xOff * (col * 2 + 1 - cols));
					int y = (int) (this.origin.y + yOff * (row - half) * 3);
					shape = this.shapeMap.get("0:" + xPosition + "," + yPosition);
					if (shape != null) {
						shape.setCenter(new Point(x, y));
					}
				}
			}
			break;
		}
		case SQUARED: {
			int half = dimension / 2;
			Square square = null;
			double xLeg = (length / 2) - (padding / 2);
			double yLeg = xLeg;
//			double radius = Math.sqrt(Math.pow(xLeg, 2)+Math.pow(yLeg,2));
			for (int row = 0; row < dimension; row++) {
				for (int column = 0; column < dimension; column++) {
					int xPosition = column - half;
					int yPosition = row - half;
//					System.out.println(xPosition+" "+yPosition);
					double x = (this.origin.x + (xPosition * length));
					double y = (this.origin.y + (yPosition * length));
					square = (Square) this.shapeMap.get("0:" + xPosition + "," + yPosition);
					if (square != null) {
						square.setCenter(new Point(x, y));
					}
				}
			}
			break;
		}
		}

	}
	
	@Override
	public void propagate(Concept concept) {
		this.color.propagate(concept);
		this.red.propagate(concept);
		this.green.propagate(concept);
		this.blue.propagate(concept);
		this.brightness.propagate(concept);
	}
	
	@Override
	public void feedback(Concept concept) {
		this.color.feedback(concept);
		this.red.feedback(concept);
		this.green.feedback(concept);
		this.blue.feedback(concept);
		this.brightness.feedback(concept);
	}

	@Override
	public void process(Graphics2D graphics2D, BufferedImage image, Concept concept) {
		Level level = this.getInputLevel();
		if (level != null) {
			List<Shape> shapeList = level.getShapeList();// .getShapeList(this.shapeMap);
			for (Shape shape : shapeList) {
				shape.initCells();
				for (int i = 0; i < shape.sides; i++) {
					if (shape.cellArray[i] != null && (int) shape.xDimension[i] > 0
							&& (int) shape.xDimension[i] < (image.getWidth()) && (int) shape.yDimension[i] > 0
							&& (int) shape.yDimension[i] < (image.getHeight())) {
						shape.cellArray[i]
								.input(image.getRGB((int) (shape.xDimension[i]), (int) (shape.yDimension[i])));
					} else {
						shape.cellArray[i].input(Color.black.getRGB());
					}
				}
				shape.addCoincidence(shape.getCoincidence(this.wavelength[0]), concept);
			}

			

			if (graphics2D != null) {
				int dimension = (int) (this.getRadius() * 2);
				BufferedImage beliefBufferedImage = new BufferedImage(dimension, dimension, BufferedImage.TYPE_INT_RGB);
				List<Point> pointList = new ArrayList<>();
				List<Concept> conceptList = this.getRootLevel().getCoincidenceConceptList();
				Belief belief = new Belief();
				for (Shape shape : shapeList) {
					Point point = new Point(shape.xDimension[0], shape.yDimension[0]);
					int brightness = shape.coincidence.list.get(0);
					if (255 > brightness && brightness > 0) {
						pointList.add(point);
					}
					Color color = new Color(brightness, brightness, brightness);
					graphics2D.setColor(color);
					graphics2D.drawPolygon(shape.doubleToIntArray(shape.xDimension),
							shape.doubleToIntArray(shape.yDimension), (int) shape.n);
					this.bufferedImageSetRGB(beliefBufferedImage, dimension, shape);
				}
				// When belief is initialized, it has the global coordinates of where Cortex was
				// on a plane.
				// We actually want this information
				// Beliefs are drawn where they are found in a plane.
				// Point List consists of points that are centered around
				// belief origin. @ least one point in Point List is equal to origin.
				belief.setConceptList(this.conceptMap, conceptList);
				belief.coincidence = this.getRootLevel().getCoincidenceList().get(0);
				;
				belief.pointList = new ArrayList<>(pointList);
				belief.bufferedImage = (beliefBufferedImage);
				belief.origin = new Point(this.origin);
				belief.date = new Date();
//			this.addBelief(belief);
				this.beliefList.add(belief);
				// Normailization ruins this information, but we still
				// want the result of normalization.
				// We want beliefs represented relative to origin.
				// The same implementation we have now, for the most part.
				//

//			this.add(belief);
//			System.out.println("this.setIndex(...) flag="+);
				this.setIndex(this.beliefList.size() - 1);
			}

		}
	}
}
//@JsonIgnore
//public void setOrigin(int x, int y) {
//	this.x = x;
//	this.y = y;
//}

//public Level getLevel() {
//	return this.rootLevel;
//}
//public void scan(BufferedImage image, double scale, Concept concept) {
//	logger.info("processing...");
//	int width = image.getWidth();
//	int height = image.getHeight();
//	for(int w = 0; w< width;w++) {
//		for(int n=0;n < height;n++) {
//			this.setOrigin(w, n);
//			this.update();
//			this.process(image, scale, concept);
//		}
//	}
//}

//public void process(Graphics2D graphics2D, BufferedImage image, double scale, Concept concept, int sleep) {
//logger.info("processing...");
//Belief belief = null;
//List<Shape> shapeList = Network.getShapeList(this.shapeMap);
//for (Shape h : shapeList) {
//	if (sleep > 0) {
//		graphics2D.drawPolygon(h.doubleToIntArray(h.xpoints), h.doubleToIntArray(h.ypoints), (int) h.npoints);
//	}
//	for (int i = 0; i < h.sides; i++) {
//		if (h.shortConeArray[i] != null && h.mediumConeArray[i] != null && h.longConeArray[i] != null
//				&& (int) h.xpoints[i] > 0 && (int) h.xpoints[i] < (image.getWidth() * scale)
//				&& (int) h.ypoints[i] > 0 && (int) h.ypoints[i] < (image.getHeight() * scale)) {
//			h.shortConeArray[i].input(image.getRGB((int) (h.xpoints[i] * scale), (int) (h.ypoints[i] * scale)));
//			h.mediumConeArray[i]
//					.input(image.getRGB((int) (h.xpoints[i] * scale), (int) (h.ypoints[i] * scale)));
//			h.longConeArray[i].input(image.getRGB((int) (h.xpoints[i] * scale), (int) (h.ypoints[i] * scale)));
//		} else {
//			h.shortConeArray[i].input(Color.black.getRGB());
//			h.mediumConeArray[i].input(Color.black.getRGB());
//			h.longConeArray[i].input(Color.black.getRGB());
//		}
//	}
//}
//this.brightness.propagate(concept);
//this.red.propagate(concept);
//this.green.propagate(concept);
//this.blue.propagate(concept);
//this.level.propagate(0, concept, true);
//if (concept == null) {
//	List<Concept> conceptList = this.level.getCoincidenceConceptList();
//	concept = (conceptList.size() > 0) ? conceptList.get(0) : null;
//	if (concept != null) {// bConcept != null && aConcept.equals(bConcept)
//		belief = new Belief(concept, new Point(this.x, this.y));
//		this.beliefList.add(belief);
//	}
//	double width = 13;
//	double height = 13;
//	if (sleep > 0) {
//		for (Belief b : this.beliefList) {
//			graphics2D.setColor(Color.BLUE);
//			double newX = b.point.x - width / 2.0;
//			double newY = b.point.y - height / 2.0;
//			Ellipse2D.Double ellipse = new Ellipse2D.Double(newX, newY, width, height);
//			graphics2D.draw(ellipse);
//		}
//	}
//}
//}
