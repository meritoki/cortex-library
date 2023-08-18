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
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.meritoki.library.cortex.model.network.Configuration;
import com.meritoki.library.cortex.model.network.Cortex;
import com.meritoki.library.cortex.model.network.Level;
import com.meritoki.library.cortex.model.network.Network;
import com.meritoki.library.cortex.model.network.Shape;
import com.meritoki.library.cortex.model.network.hexagon.Hexagon;
import com.meritoki.library.cortex.model.network.hexagon.Hexagonal;
import com.meritoki.library.cortex.model.network.square.Square;
import com.meritoki.library.cortex.model.network.square.Squared;
import com.meritoki.library.cortex.model.unit.Concept;
import com.meritoki.library.cortex.model.unit.Point;

public class Group extends Cortex {

	protected static Logger logger = LoggerFactory.getLogger(Group.class.getName());
	@JsonProperty
	private Network brightness = null;
	@JsonProperty
	private Network red = null;
	@JsonProperty
	private Network green = null;
	@JsonProperty
	private Network blue = null;
	@JsonProperty
	private Level level = new Level();
	@JsonProperty
	private Shape root = new Shape();
	@JsonProperty
	private Network color = new Network();

	public Group() {
		this.uuid = UUID.randomUUID().toString();
		this.setConfiguration(Configuration.HEXAGONAL);
	}

	/**
	 * Checked 202001191442 Good
	 */
	public Group(Configuration configuration) {
		this.setConfiguration(configuration);
	}
	
	public void setConfiguration(Configuration configuration) {
//		this.configuration = configuration;
//		switch (this.configuration) {
//		case HEXAGONAL: {
//			logger.info("HEXAGONAL");
//			this.brightness = new Hexagonal(com.meritoki.library.cortex.model.network.ColorType.BRIGHTNESS, (int)this.origin.x, (int)this.origin.y, size, radius, padding);
//			this.red = new Hexagonal(com.meritoki.library.cortex.model.network.ColorType.RED, (int)this.origin.x, (int)this.origin.y, size, radius, padding);
//			this.green = new Hexagonal(com.meritoki.library.cortex.model.network.ColorType.GREEN, (int)this.origin.x, (int)this.origin.y, size, radius, padding);
//			this.blue = new Hexagonal(com.meritoki.library.cortex.model.network.ColorType.BLUE, (int)this.origin.x, (int)this.origin.y, size, radius, padding);
//			this.shapeMap = Hexagonal.getShapeMap(0, new Point(this.origin.x, this.origin.y), size, radius, padding);
//			this.brightness.setShapeMap(this.shapeMap);
//			this.red.setShapeMap(this.shapeMap);
//			this.green.setShapeMap(this.shapeMap);
//			this.blue.setShapeMap(this.shapeMap);
//			break;
//		}
//		case SQUARED: {
//			logger.info("SQUARED");
//			this.brightness = new Squared(com.meritoki.library.cortex.model.network.ColorType.BRIGHTNESS, (int)this.origin.x, (int)this.origin.y, dimension, length, padding);
//			this.red = new Squared(com.meritoki.library.cortex.model.network.ColorType.RED, (int)this.origin.x, (int)this.origin.y, dimension, length, padding);
//			this.green = new Squared(com.meritoki.library.cortex.model.network.ColorType.GREEN, (int)this.origin.x, (int)this.origin.y, dimension, length, padding);
//			this.blue = new Squared(com.meritoki.library.cortex.model.network.ColorType.BLUE,(int)this.origin.x, (int)this.origin.y, dimension, length, padding);
//			this.shapeMap = Squared.getShapeMap(0, new Point(this.origin.x, this.origin.y), dimension, length, padding);
//			this.brightness.setShapeMap(this.shapeMap);
//			this.red.setShapeMap(this.shapeMap);
//			this.green.setShapeMap(this.shapeMap);
//			this.blue.setShapeMap(this.shapeMap);
//			break;
//		}
//		}
	}

//	@JsonIgnore
//	public void setOrigin(int x, int y) {
//		this.x = x;
//		this.y = y;
//	}

	public Level getLevel() {
		return this.level;
	}

	@JsonIgnore
	public LinkedList<Hexagon> getHexagonList(Map<String, Hexagon> hexagonMap) {
		LinkedList<Hexagon> hexagonList = new LinkedList<Hexagon>();
		for (Map.Entry<String, Hexagon> entry : hexagonMap.entrySet()) {
			hexagonList.add((Hexagon) entry.getValue());
		}
		return hexagonList;
	}

	public void setLevel(Level level) {
		this.level = level;
	}

	@Override
	public void load() {
		this.brightness.load();
		this.red.load();
		this.green.load();
		this.blue.load();
		Shape brightnessShape = this.brightness.getRootLevel().getShapeList().get(0);
		Shape redShape = this.red.getRootLevel().getShapeList().get(0);
		Shape greenShape = this.green.getRootLevel().getShapeList().get(0);
		Shape blueShape = this.blue.getRootLevel().getShapeList().get(0);
		brightnessShape.setData("brightness");
		redShape.setData("red");
		greenShape.setData("green");
		blueShape.setData("blue");
		this.level.addShape("brightness",brightnessShape);
		this.level.addShape("red",redShape);
		this.level.addShape("green",greenShape);
		this.level.addShape("blue",blueShape);
		this.color.addLevel(this.level);
		this.level = new Level();
		this.root.setData("root");
		this.root.length = 4;
		this.root.addChild(brightnessShape);
		this.root.addChild(redShape);
		this.root.addChild(greenShape);
		this.root.addChild(blueShape);
		this.level.addShape("root",this.root);
		this.color.addLevel(this.level);
		for(Shape shape: Network.getShapeList(this.shapeMap)) { 
			if(shape.length == 0) {
				System.out.println(shape+" "+shape.length);
			}
		}
	}

	@JsonIgnore
	public void update() {
////		logger.info("update()");
//		switch (this.configuration) {
//		case HEXAGONAL: {
//			double radians = Math.toRadians(30);
//			double xOff = Math.cos(radians) * (this.radius + this.padding);
//			double yOff = Math.sin(radians) * (this.radius + this.padding);
//			int half = this.size / 2;
//			Shape shape = null;
//			for (int row = 0; row < this.size; row++) {
//				int cols = this.size - java.lang.Math.abs(row - half);
//				for (int col = 0; col < cols; col++) {
//					int xPosition = row < half ? col - row : col - half;
//					int yPosition = row - half;
//					int x = (int) (this.origin.x + xOff * (col * 2 + 1 - cols));
//					int y = (int) (this.origin.y + yOff * (row - half) * 3);
//					shape = this.shapeMap.get("0:"+xPosition + "," + yPosition);
//					if (shape != null) {
//						shape.setCenter(new Point(x, y));
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
//					double x = (this.origin.x + (xPosition * length));
//					double y = (this.origin.y + (yPosition * length));
//					square = (Square) this.shapeMap.get("0:"+xPosition + "," + yPosition);
//					if (square != null) {
//						square.setCenter(new Point(x, y));
//					}
//				}
//			}
//			break;
//		}
//		}

	}

	@Override
	public void process(Graphics2D graphics2D, BufferedImage image, Concept concept) {
//		List<Shape> shapeList = Network.getShapeList(this.shapeMap);
//		for (Shape shape : shapeList) {
//			shape.initCells();
//			for (int i = 0; i < shape.sides; i++) {
//				if (shape.shortConeArray[i] != null && shape.mediumConeArray[i] != null
//						&& shape.longConeArray[i] != null && (int) shape.xpoints[i] > 0
//						&& (int) shape.xpoints[i] < (image.getWidth()) && (int) shape.ypoints[i] > 0
//						&& (int) shape.ypoints[i] < (image.getHeight())) {
//					shape.shortConeArray[i].input(image.getRGB((int) (shape.xpoints[i]), (int) (shape.ypoints[i])));
//					shape.mediumConeArray[i].input(image.getRGB((int) (shape.xpoints[i]), (int) (shape.ypoints[i])));
//					shape.longConeArray[i].input(image.getRGB((int) (shape.xpoints[i]), (int) (shape.ypoints[i])));
//				} else {
//					shape.shortConeArray[i].input(Color.black.getRGB());
//					shape.mediumConeArray[i].input(Color.black.getRGB());
//					shape.longConeArray[i].input(Color.black.getRGB());
//				}
//			}
//		}

//		this.red.propagate(concept,true);
//		System.out.println("process(...) this.root.coincidence="+this.root.coincidence);
//		System.out.println("process(...) this.red.getRootLevel().getShapeList().get(0).coincidence.list.size()="+this.red.getRootLevel().getShapeList().get(0).coincidence);
//		for (int i = 0; i < this.red.getInputLevel().getShapeList().size(); i++) {
//			Shape red = this.red.getInputLevel().getShapeList().get(i);
//			red.red = red.coincidence.list.get(0);
//		}
//		this.green.propagate(concept,true);
//		System.out.println("process(...) this.green.getRootLevel().getShapeList().get(0).coincidence.list.size()="+this.green.getRootLevel().getShapeList().get(0).coincidence);
//		for (int i = 0; i < this.green.getInputLevel().getShapeList().size(); i++) {
//			Shape red = this.green.getInputLevel().getShapeList().get(i);
//			red.green = red.coincidence.list.get(0);
//		}
//		this.blue.propagate(concept,true);
//		System.out.println("process(...) this.blue.getRootLevel().getShapeList().get(0).coincidence.list.size()="+this.blue.getRootLevel().getShapeList().get(0).coincidence);
//		for (int i = 0; i < this.blue.getInputLevel().getShapeList().size(); i++) {
//			Shape red = this.blue.getInputLevel().getShapeList().get(i);
//			red.blue = red.coincidence.list.get(0);
//		}
//		for (int i = 0; i < this.brightness.getInputLevel().getShapeList().size(); i++) {
//			Shape shape = this.brightness.getInputLevel().getShapeList().get(i);
//			Color color = new Color(shape.red, shape.green, shape.blue);
//			System.out.println(color);
//			graphics2D.setColor(color);
//			graphics2D.drawPolygon(shape.doubleToIntArray(shape.xpoints), shape.doubleToIntArray(shape.ypoints),
//					(int) shape.npoints);
//
//		}
		
		
//		this.brightness.propagate(concept,true);
//		this.color.propagate(concept,false);
//		System.out.println("process(...) this.root.coincidence="+this.color.getRootLevel().getShapeList().get(0).coincidence);
//		System.out.println("process(...) this.root.coincidence.list.size()="+this.color.getRootLevel().getShapeList().get(0).coincidence.list.size());
		this.level.feedback(concept,true);
//		this.red.feedback(concept);
//		for (int i = 0; i < this.red.getInputLevel().getShapeList().size(); i++) {
//			Shape red = this.red.getInputLevel().getShapeList().get(i);
//			red.red = red.coincidence.list.get(0);
//		}
//		this.green.feedback(concept);
//		for (int i = 0; i < this.green.getInputLevel().getShapeList().size(); i++) {
//			Shape green = this.green.getInputLevel().getShapeList().get(i);
//			green.green = green.coincidence.list.get(0);
//		}
//		this.blue.feedback(concept);
//		for (int i = 0; i < this.blue.getInputLevel().getShapeList().size(); i++) {
//			Shape blue = this.blue.getInputLevel().getShapeList().get(i);
//			blue.blue = blue.coincidence.list.get(0);
//		}
//		for (int i = 0; i < this.brightness.getInputLevel().getShapeList().size(); i++) {
//			Shape shape = this.brightness.getInputLevel().getShapeList().get(i);
//			Color color = new Color(shape.red, shape.green, shape.blue);
////			System.out.println(color);
//			graphics2D.setColor(color);
//			graphics2D.drawPolygon(shape.doubleToIntArray(shape.xpoints), shape.doubleToIntArray(shape.ypoints),
//					(int) shape.npoints);
//		}
	}
}

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
