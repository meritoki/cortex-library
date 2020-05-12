package com.meritoki.cortex.library.model;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import com.meritoki.cortex.library.model.hexagon.Hexagon;
import com.meritoki.cortex.library.model.hexagon.Hexagonal;
import com.meritoki.cortex.library.model.square.Square;
import com.meritoki.cortex.library.model.square.Squared;

public class Group {

	private static Logger logger = LogManager.getLogger(Group.class.getName());
	public static final int HEXAGONAL = 1;
	public static final int SQUARED = 2;
	public int type = 0;
	private Network brightness = null;
	private Network red = null;
	private Network green = null;
	private Network blue = null;
	private Level level = new Level();
	private Hexagon root = new Hexagon();
	@JsonProperty
	private int size = 13;
	@JsonProperty
	private int radius = 1;
	@JsonProperty
	private int padding = 0;
	@JsonProperty
	private int depth = 0;
//	@JsonProperty
//	private Map<String, Hexagon> hexagonMap = new HashMap<>();
	@JsonProperty
	private Map<String, Shape> shapeMap = new HashMap<>();
	@JsonIgnore
	private int x = 0;
	@JsonIgnore
	private int y = 0;
	@JsonProperty
	private List<Belief> beliefList = new ArrayList<>();
	
	/**
	 * Checked 202001191442 Good
	 */
	public Group(int type) {
		this.type = type;
		switch(this.type) {
		case HEXAGONAL: {
			this.brightness = new Hexagonal(Network.BRIGHTNESS, x,y,size,radius,padding);
			this.red = new Hexagonal(Network.RED, x,y,size,radius,padding);
			this.green = new Hexagonal(Network.GREEN, x,y,size,radius,padding);
			this.blue = new Hexagonal(Network.BLUE, x,y,size,radius,padding);
			this.shapeMap = Hexagonal.getShapeMap(0,new Point(this.x, this.y), size, radius, padding);
			this.brightness.setShapeMap(this.shapeMap);
			this.red.setShapeMap(this.shapeMap);
			this.green.setShapeMap(this.shapeMap);
			this.blue.setShapeMap(this.shapeMap);
			break;
		}
		case SQUARED: {
			this.brightness = new Squared(Network.BRIGHTNESS, x,y,size,radius,padding);
			this.red = new Squared(Hexagonal.RED, x,y,size,radius,padding);
			this.green = new Squared(Hexagonal.GREEN, x,y,size,radius,padding);
			this.blue = new Squared(Hexagonal.BLUE, x,y,size,radius,padding);
			this.shapeMap = Squared.getShapeMap(0,new Point(this.x, this.y), size, radius, padding);
			this.brightness.setShapeMap(this.shapeMap);
			this.red.setShapeMap(this.shapeMap);
			this.green.setShapeMap(this.shapeMap);
			this.blue.setShapeMap(this.shapeMap);
			break;
		}
		}
	}
	
	@JsonIgnore
	public void setOrigin(int x, int y) {
		this.x = x;
		this.y = y;
	}	
	
	public int getX() {
		return this.x;
	}
	
	public int getY() {
		return this.y;
	}

	
	public Level getLevel() {
		return level;
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
	
	public void load() {
		this.brightness.load();
		this.red.load();
		this.green.load();
		this.blue.load();
		Shape brightnessHexagon = this.brightness.getRootLevel().getShapeList().get(0);
		Shape redHexagon = this.red.getRootLevel().getShapeList().get(0);
		Shape greenHexagon = this.green.getRootLevel().getShapeList().get(0);
		Shape blueHexagon = this.blue.getRootLevel().getShapeList().get(0);
		this.root.addChild(brightnessHexagon);
		this.root.addChild(redHexagon);
		this.root.addChild(greenHexagon);
		this.root.addChild(blueHexagon);
		this.level.addShape(this.root);
	}
	
	@JsonIgnore
	public void update() {
		logger.info("update()");
		switch(this.type) {
		case HEXAGONAL: {
			double radians = Math.toRadians(30);
			double xOff = Math.cos(radians) * (this.radius + this.padding);
			double yOff = Math.sin(radians) * (this.radius + this.padding);
			int half = this.size / 2;
			Hexagon hexagon = null;
			for (int row = 0; row < this.size; row++) {
				int cols = this.size - java.lang.Math.abs(row - half);
				for (int col = 0; col < cols; col++) {
					int xPosition = row < half ? col - row : col - half;
					int yPosition = row - half;
					int x = (int) (this.x + xOff * (col * 2 + 1 - cols));
					int y = (int) (this.y + yOff * (row - half) * 3);
					hexagon = (Hexagon)this.shapeMap.get("0:"+xPosition + "," + yPosition);
					if (hexagon != null) {
						hexagon.setCenter(new Point(x, y));
					}
				}
			}
			break;
		}
		case SQUARED: {
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
					double x = (this.x + (xPosition * length));
					double y = (this.y + (yPosition * length));
					square = (Square) this.shapeMap.get("0:"+xPosition + "," + yPosition);
					if (square != null) {
						square.setCenter(new Point(x, y));
					}
				}
			}
			
			break;
		}
		}

	}
	
	public void scan(BufferedImage image, double scale, Concept concept) {
		logger.info("processing...");
		int width = image.getWidth();
		int height = image.getHeight();
		for(int w = 0; w< width;w++) {
			for(int n=0;n < height;n++) {
				this.setOrigin(w, n);
				this.update();
				this.process(image, scale, concept);
			}
		}
	}
	
	public void process(BufferedImage image, double scale, Concept concept) {
		Belief belief = null;
		List<Shape> hexagonList = Network.getShapeList(this.shapeMap);
		for (Shape h : hexagonList) {
			for (int i = 0; i < h.sides; i++) {
				if (h.shortConeArray[i] != null 
						&& h.mediumConeArray[i] != null
						&& h.longConeArray[i] != null
						&& (int) h.xpoints[i] > 0
						&& (int) h.xpoints[i] < (image.getWidth() * scale)
						&& (int) h.ypoints[i] > 0
						&& (int) h.ypoints[i] < (image.getHeight() * scale)) {
					h.shortConeArray[i].input(image.getRGB((int) (h.xpoints[i] * scale),
							(int) (h.ypoints[i] * scale)));
					h.mediumConeArray[i].input(image.getRGB((int) (h.xpoints[i] * scale),
							(int) (h.ypoints[i] * scale)));
					h.longConeArray[i].input(image.getRGB((int) (h.xpoints[i] * scale),
							(int) (h.ypoints[i] * scale)));
				} else {
					h.shortConeArray[i].input(Color.black.getRGB());
					h.mediumConeArray[i].input(Color.black.getRGB());
					h.longConeArray[i].input(Color.black.getRGB());
				}
			}
		}
		this.brightness.propagate(concept);
		this.red.propagate(concept);
		this.green.propagate(concept);
		this.blue.propagate(concept);
		this.level.propagate(0,concept,true);
		if (concept == null) {
			List<Concept> conceptList = this.level.getCoincidenceConceptList();
			concept = (conceptList.size() > 0) ? conceptList.get(0) : null;
			if (concept != null) {// bConcept != null && aConcept.equals(bConcept)
				belief = new Belief(concept, new Point(this.x, this.y));
				this.beliefList.add(belief);
			}
		}
	}

	public void process(Graphics2D graphics2D, BufferedImage image, double scale, Concept concept, int sleep) {
		logger.info("processing...");
		Belief belief = null;
		List<Shape> hexagonList = Network.getShapeList(this.shapeMap);
		for (Shape h : hexagonList) {
			if (sleep > 0) {
				graphics2D.drawPolygon(h.doubleToIntArray(h.xpoints), h.doubleToIntArray(h.ypoints),
						(int) h.npoints);
			}
			for (int i = 0; i < h.sides; i++) {
				if (h.shortConeArray[i] != null 
						&& h.mediumConeArray[i] != null
						&& h.longConeArray[i] != null
						&& (int) h.xpoints[i] > 0
						&& (int) h.xpoints[i] < (image.getWidth() * scale)
						&& (int) h.ypoints[i] > 0
						&& (int) h.ypoints[i] < (image.getHeight() * scale)) {
					h.shortConeArray[i].input(image.getRGB((int) (h.xpoints[i] * scale),
							(int) (h.ypoints[i] * scale)));
					h.mediumConeArray[i].input(image.getRGB((int) (h.xpoints[i] * scale),
							(int) (h.ypoints[i] * scale)));
					h.longConeArray[i].input(image.getRGB((int) (h.xpoints[i] * scale),
							(int) (h.ypoints[i] * scale)));
				} else {
					h.shortConeArray[i].input(Color.black.getRGB());
					h.mediumConeArray[i].input(Color.black.getRGB());
					h.longConeArray[i].input(Color.black.getRGB());
				}
			}
		}
		this.brightness.propagate(concept);
		this.red.propagate(concept);
		this.green.propagate(concept);
		this.blue.propagate(concept);
		this.level.propagate(0,concept, true);
		if (concept == null) {
			List<Concept> conceptList = this.level.getCoincidenceConceptList();
			concept = (conceptList.size() > 0) ? conceptList.get(0) : null;
			if (concept != null) {// bConcept != null && aConcept.equals(bConcept)
				belief = new Belief(concept, new Point(this.x, this.y));
				this.beliefList.add(belief);
			}
			double width = 13;
			double height = 13;
			if (sleep > 0) {
				for (Belief b : this.beliefList) {
					graphics2D.setColor(Color.BLUE);
					double newX = b.point.x - width / 2.0;
					double newY = b.point.y - height / 2.0;
					Ellipse2D.Double ellipse = new Ellipse2D.Double(newX, newY, width, height);
					graphics2D.draw(ellipse);
				}
			}
		}
	}
}
