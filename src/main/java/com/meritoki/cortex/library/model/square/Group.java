package com.meritoki.cortex.library.model.square;

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

import com.meritoki.cortex.library.model.Belief;
import com.meritoki.cortex.library.model.Concept;
import com.meritoki.cortex.library.model.Point;

public class Group {

	private static Logger logger = LogManager.getLogger(Group.class.getName());
	private Network brightness = null;
	private Network red = null;
	private Network green = null;
	private Network blue = null;
	private Level level = new Level();
	private Square root = new Square();
	@JsonProperty
	private int dimension = 13;
	@JsonProperty
	private int length = 10;
	@JsonProperty
	private int padding = 0;
	@JsonProperty
	private int depth = 0;
	@JsonProperty
	private Map<String, Square> squareMap = new HashMap<>();
	@JsonIgnore
	private int x = 0;
	@JsonIgnore
	private int y = 0;
	@JsonProperty
	private List<Belief> beliefList = new ArrayList<>();
	
	/**
	 * Checked 202001191442 Good
	 */
	public Group() {
		this.brightness = new Network(Network.BRIGHTNESS, x,y,dimension,length,padding);
		this.red = new Network(Network.RED, x,y,dimension,length,padding);
		this.green = new Network(Network.GREEN, x,y,dimension,length,padding);
		this.blue = new Network(Network.BLUE, x,y,dimension,length,padding);
		this.squareMap = Network.getSquareMap(0,new Point(this.x, this.y), dimension, length, padding);
		this.brightness.setSquareMap(this.squareMap);
		this.red.setSquareMap(this.squareMap);
		this.green.setSquareMap(this.squareMap);
		this.blue.setSquareMap(this.squareMap);
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
	public LinkedList<Square> getSquareList(Map<String, Square> squareMap) {
		LinkedList<Square> squareList = new LinkedList<Square>();
		for (Map.Entry<String, Square> entry : squareMap.entrySet()) {
			squareList.add((Square) entry.getValue());
		}
		return squareList;
	}

	public void setLevel(Level level) {
		this.level = level;
	}
	
	public void load() {
		this.brightness.load();
		this.red.load();
		this.green.load();
		this.blue.load();
		Square brightnessSquare = this.brightness.getRootLevel().getSquareList().get(0);
		Square redSquare = this.red.getRootLevel().getSquareList().get(0);
		Square greenSquare = this.green.getRootLevel().getSquareList().get(0);
		Square blueSquare = this.blue.getRootLevel().getSquareList().get(0);
		this.root.addChild(brightnessSquare);
		this.root.addChild(redSquare);
		this.root.addChild(greenSquare);
		this.root.addChild(blueSquare);
		this.level.addSquare(this.root);
	}
	
	@JsonIgnore
	public void update() {
		logger.info("update()");
		Map<String, Square> squareMap = new HashMap<>();
		int half = dimension/2;
		Square square = null;
		double xLeg = (length/2)-(padding/2);
		double yLeg = xLeg;
		double radius = Math.sqrt(Math.pow(xLeg, 2)+Math.pow(yLeg,2));
		for (int row = 0; row < dimension; row++) {
			for (int column = 0; column < dimension; column++) {
				int xPosition = column - half;
				int yPosition = row - half;
//				System.out.println(xPosition+" "+yPosition);
				double x = (this.x + (xPosition * length) );
				double y = (this.y + (yPosition * length));
				square = this.squareMap.get("0:"+xPosition + "," + yPosition);
				if (square != null) {
					square.setCenter(new Point(x, y));
				}
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
		List<Square> squareList = this.getSquareList(this.squareMap);
		for (Square h : squareList) {
			for (int i = 0; i < h.SIDES; i++) {
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
		List<Square> squareList = this.getSquareList(this.squareMap);
		for (Square h : squareList) {
			if (sleep > 0) {
				graphics2D.drawPolygon(h.doubleToIntArray(h.xpoints), h.doubleToIntArray(h.ypoints),
						(int) h.npoints);
			}
			for (int i = 0; i < h.SIDES; i++) {
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
//		this.brightness.propagate(concept);
//		this.red.propagate(concept);
//		this.green.propagate(concept);
//		this.blue.propagate(concept);
//		this.level.propagate(0,concept, true);
//		if (concept == null) {
//			List<Concept> conceptList = this.level.getCoincidenceConceptList();
//			concept = (conceptList.size() > 0) ? conceptList.get(0) : null;
//			if (concept != null) {// bConcept != null && aConcept.equals(bConcept)
//				belief = new Belief(concept, new Point(this.x, this.y));
//				this.beliefList.add(belief);
//			}
//			double width = 13;
//			double height = 13;
//			if (sleep > 0) {
//				for (Belief b : this.beliefList) {
//					graphics2D.setColor(Color.BLUE);
//					double newX = b.point.x - width / 2.0;
//					double newY = b.point.y - height / 2.0;
//					Ellipse2D.Double ellipse = new Ellipse2D.Double(newX, newY, width, height);
//					graphics2D.draw(ellipse);
//				}
//			}
//		}
	}
}
