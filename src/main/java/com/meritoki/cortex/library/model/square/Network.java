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
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import com.meritoki.cortex.library.model.Belief;
import com.meritoki.cortex.library.model.Concept;
import com.meritoki.cortex.library.model.Node;
import com.meritoki.cortex.library.model.Point;
import com.meritoki.cortex.library.model.square.Level;
import com.meritoki.cortex.library.model.square.Square;

/**
 * In Network, squares are referenced by level and relative coordinates, i.e.
 * 0:00, 4:00
 * 
 * @author osvaldo.rodriguez
 *
 */
public class Network {
	
	public static void main(String[] args) {
//		Map<String,Square> squareMap = Network.getSquareMap(-1, new Point(0,0), 3, 2, 0);
		Network n = new Network(Network.BRIGHTNESS, 0,0,9,1,0);
		n.load();
//		for (Map.Entry<String, Square> entry : squareMap.entrySet()) {
//			String key = entry.getKey();
//			Square value = entry.getValue();
//			System.out.println(key+" "+value.getCenter()+" "+value.getRadius());
//		}
	}

	@JsonIgnore
	private static Logger logger = LogManager.getLogger(Network.class.getName());
	public static final int BRIGHTNESS = 1;
	public static final int RED = 2;
	public static final int GREEN = 3;
	public static final int BLUE = 4;
	@JsonIgnore
	private LinkedList<Level> levelList = new LinkedList<>();
	@JsonIgnore
	public int index = 0;
	@JsonProperty
	private Map<String, Square> squareMap = new HashMap<>();
	@JsonProperty
	private int type = 0;
	@JsonProperty
	public String uuid = null;
	@JsonIgnore
	private int x = 0;
	@JsonIgnore
	private int y = 0;
	@JsonProperty
	private int dimension = 13;
	@JsonProperty
	private int length = 1;
	@JsonProperty
	private int padding = 0;
	@JsonProperty
	private int depth = 0;
	@JsonProperty
	private List<Belief> beliefList = new ArrayList<>();

	public Network() {
		this.uuid = UUID.randomUUID().toString();
	}

	public Network(int type, int x, int y, int dimension, int length, int padding) {
		this.uuid = UUID.randomUUID().toString();
		this.type = type;
		this.x = x;
		this.y = y;
		this.dimension = dimension;
		this.length = length;
		this.padding = padding;
	}

	public Network(int size, int radius, int padding) {
		this.uuid = UUID.randomUUID().toString();
		this.dimension = size;
		this.length = radius;
		this.padding = padding;
	}

	@JsonIgnore
	public int getIndex() {
		return this.index;
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
	public void setSquareMap(Map<String, Square> squareMap) {
//		this.squareMap = squareMap;
		for (Map.Entry<String, Square> entry : squareMap.entrySet()) {
			String key = entry.getKey();
			Square value = entry.getValue();
			this.squareMap.put(key, value);
		}
	}

	@JsonIgnore
	public Map<String, Square> getSquareMap() {
		return this.squareMap;
	}

	/**
	 * Function uses squareMap initialized with loaded parameters, to generate keys
	 * that retrieve loaded squares from file and connects them in parent child
	 * relationships, forming a tree data structure that converges on a root not
	 */
	@JsonIgnore
	public void load() {
		logger.info("load()");
		logger.info("load() this.squareMap=" + this.squareMap);
		logger.info("load() this.dimension=" + this.dimension);
		logger.info("load() this.length=" + this.length);
		Map<String, Square> squareMap = getSquareMap(-1, new Point(this.x, this.y), this.dimension, this.length,
				this.padding);
		System.out.println(squareMap);
		int depth = (this.depth > 0) ? this.depth : this.getDepth(squareMap.size());
		if (this.depth == 0) {
			this.depth = depth;
		}
		Level level = new Level();
		List<Square> squareList = this.getSquareList(squareMap);
		Square square = null;
		for (Square n : squareList) {
			square = this.squareMap.get("0:" + n);
			if (square == null) {
				square = new Square(n);
				this.squareMap.put("0:" + square, square);
			}
			square.setData("0:" + square);
			square.initCells();
			level.addSquare(square);
		}
		this.addLevel(level);
		LinkedList<Square> squareStack = null;
		int exponent = 0;
		for (int i = 1; i < depth; i++) {
			logger.debug("load() i=" + i);
			if (i % 3 == 0) {
				exponent+=2;
			}
			logger.debug("load() %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% exponent=" + exponent);
			squareMap = this.getLastLevel().getSquareMap();
			level = new Level();
			squareList = new LinkedList<>();
			squareStack = new LinkedList<>();
			squareStack.push(squareMap.get(this.x + "," + this.y));
			while (!squareStack.isEmpty()) {
				square = squareStack.pop();
				LinkedList<Square> list = null;
				if (i % 2 == 1) {
					list = getGroupOneSquareList(squareMap, square.getX(), square.getY(), exponent); // get list for x, y
				} else {
					list = getGroupZeroSquareList(squareMap, square.getX(), square.getY(), exponent); // get list for x,
					// y
				}
				for (Square s : list) {
					if (!squareList.contains(s)) {
						squareList.add(s);
						squareStack.push(s);
					}
				}
			}
			System.out.println("*****************************************");
			for (Square m : squareList) {
				square = this.squareMap.get(i + ":" + m);
				if (square == null) {
					square = new Square(m);
					this.squareMap.put(i + ":" + square, square);
				}
				List<Square> list = null;
				if (i % 2 == 1) {
					list = this.getGroupZeroSquareList(squareMap, square.getX(), square.getY(), exponent);
				} else {
					list = this.getGroupOneSquareList(squareMap, square.getX(), square.getY(), exponent);
				}
				System.out.println("square="+square);
				for (Square n : list) {
					System.out.println("children="+n);
					square.addChild(n);
				}
				square.setData(i + ":" + square);
				level.addSquare(square);
			}
			this.addLevel(level);
			System.out.println("=========================================================");
		}
////		if (logger.isDebugEnabled()) {
			level = this.getLastLevel();
			Square h = level.getSquareList().get(0);
			Node.printTree(h, " ");
//			
//			for(Level l: this.levelList) {
//				System.out.println(l);
//			}
////		}
	}

	@JsonIgnore
	public LinkedList<Square> getGroupZeroSquareList(Map<String, Square> squareMap, int x, int y, int exponent) {
		System.out.println("getGroupZeroSquareList("+squareMap+","+x+", "+y+", "+exponent+")");
		LinkedList<Square> squareList = new LinkedList<>();
		Square h = null;
		int multiplier = (int) Math.pow(2, exponent);
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
		System.out.println("getGroupZeroSquareList(squareMap,"+x+", "+y+", "+exponent+") squareList="+squareList);
		return squareList;
	}

	@JsonIgnore
	public LinkedList<Square> getGroupOneSquareList(Map<String, Square> squareMap, int x, int y, int exponent) {
		System.out.println("getGroupOneSquareList("+squareMap+","+x+", "+y+", "+exponent+")");
		LinkedList<Square> squareList = new LinkedList<>();
		Square h = null;
		int multiplier = (int) Math.pow(2, exponent);
		h = squareMap.get((x) + "," + (y));
		if (h != null) {
			squareList.push(h);
		}
		h = squareMap.get((x) + "," + (y + (2 * multiplier)));
		if (h != null) {
			squareList.push(h);
		}
		h = squareMap.get((x + (2 * multiplier)) + "," + (y + (2 * multiplier)));
		if (h != null) {
			squareList.push(h);
		}
		h = squareMap.get((x + (2 * multiplier)) + "," + (y));
		if (h != null) {
			squareList.push(h);
		}
		h = squareMap.get((x + (2 * multiplier)) + "," + (y - (2 * multiplier)));
		if (h != null) {
			squareList.push(h);
		}
		h = squareMap.get((x) + "," + (y - (2 * multiplier)));
		if (h != null) {
			squareList.push(h);
		}
		h = squareMap.get((x - (2 * multiplier)) + "," + (y - (2 * multiplier)));
		if (h != null) {
			squareList.push(h);
		}
		h = squareMap.get((x - (2 * multiplier)) + "," + (y));
		if (h != null) {
			squareList.push(h);
		}

		h = squareMap.get((x - (2 * multiplier)) + "," + (y + (2 * multiplier)));
		if (h != null) {
			squareList.push(h);
		}
		System.out.println("getGroupOneSquareList(squareMap,"+x+", "+y+", "+exponent+") squareList="+squareList);
		return squareList;
	}

	/**
	 * Function initializes the network of nodes, or squares, that converges into a
	 * single root node.
	 */

	public void input(Concept concept) {
		logger.info("input()");
		Level level = this.getInputLevel();
		List<Square> squareList = level.getSquareList();
		for (Square square : squareList) {
			square.addCoincidence(square.getCoincidence(this.type), concept, false);
		}
	}

	/**
	 * Need to modify so that it only moves the input level, not all levels this
	 * will improve the performance of the algorithm.
	 */
	@JsonIgnore
	public void update() {
		Map<String, Square> squareMap = new HashMap<>();
		int half = dimension/2;
		Square square = null;
		Level level = this.getInputLevel();
		double xLeg = (length/2)-(padding/2);
		double yLeg = xLeg;
		double radius = Math.sqrt(Math.pow(xLeg, 2)+Math.pow(yLeg,2));
		for (int row = 0; row < dimension; row++) {
			for (int column = 0; column < dimension; column++) {
				int xPosition = column - half;
				int yPosition = row - half;
				double x = (this.x + (xPosition * length) );
				double y = (this.y + (yPosition * length));
				square = level.squareMap.get(xPosition + "," + yPosition);
				if (square != null) {
					square.setCenter(new Point(x, y));
				}
			}
		}
	}

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

	public Level getRootLevel() {
		int size = this.getLevelList().size();
		Level level = (size > 0) ? this.getLevelList().get(size - 1) : null;
		return level;
	}

	public Level getInputLevel() {
		int size = this.getLevelList().size();
		Level level = (size > 0) ? this.getLevelList().get(0) : null;
		return level;
	}

//	public Concept scan(BufferedImage image, double scale, int interval, Concept concept) {
////		logger.info("processing...");
//		int width = image.getWidth();
//		int height = image.getHeight();
////		System.out.println(width+" "+height);
//		Map<Concept,Integer> conceptMap = new HashMap<>();
//		for(int w = 0; w< width;w+=interval) {
//			for(int n=0;n < height;n+=interval) {
//				this.setOrigin(w, n);
//				this.update();
////				System.out.println(w+" "+n);
//				concept = this.process(image, scale, concept);
//				Integer integer = conceptMap.get(concept);
//				if(integer == null) {
//					integer = 0;
//				}
//				conceptMap.put(concept, integer+1);
//			}
//		}
//		
//		int max = 0;
//		for (Map.Entry<Concept, Integer> entry : conceptMap.entrySet()) {
//		    Concept key = entry.getKey();
//		    Integer value = entry.getValue();
//		    if(value > max) {
//		    	max = value;
//		    	concept = key;
//		    }
//		}
//		return concept;
//	}

	public List<Concept> process(BufferedImage image, double scale, Concept concept) {
		Belief belief = null;
		int size = this.getLevelList().size();
		Level level = this.getInputLevel();
		if (level != null && image != null) {
			for (Square h : level.getSquareList()) {
				h.initCells();
				for (int i = 0; i < h.SIDES; i++) {
					if (h.shortConeArray[i] != null && h.mediumConeArray[i] != null && h.longConeArray[i] != null
							&& (int) h.xpoints[i] > 0 && (int) h.xpoints[i] < (image.getWidth() * scale)
							&& (int) h.ypoints[i] > 0 && (int) h.ypoints[i] < (image.getHeight() * scale)) {
						h.shortConeArray[i]
								.input(image.getRGB((int) (h.xpoints[i] * scale), (int) (h.ypoints[i] * scale)));
						h.mediumConeArray[i]
								.input(image.getRGB((int) (h.xpoints[i] * scale), (int) (h.ypoints[i] * scale)));
						h.longConeArray[i]
								.input(image.getRGB((int) (h.xpoints[i] * scale), (int) (h.ypoints[i] * scale)));
					} else {
						h.shortConeArray[i].input(Color.black.getRGB());
						h.mediumConeArray[i].input(Color.black.getRGB());
						h.longConeArray[i].input(Color.black.getRGB());
					}
				}
				h.addCoincidence(h.getCoincidence(this.type), concept, false);
			}
			this.propagate(concept);
		}

//		if (concept == null) {
//			List<Concept> conceptList = this.getRootLevel().getCoincidenceConceptList();
//			concept = (conceptList.size() > 0) ? conceptList.get(0) : null;
////			if (concept != null) {// bConcept != null && aConcept.equals(bConcept)
////				belief = new Belief(concept, new Point(this.x, this.y));
////			}
////			System.out.println(concept);
//		}

		return this.getRootLevel().getCoincidenceConceptList();
	}

	public void process(Graphics2D graphics2D, BufferedImage image, double scale, Concept concept, int sleep) {
		logger.info("processing...");
		Belief belief = null;
		int size = this.getLevelList().size();
		Level level = this.getInputLevel();
		if (level != null && image != null) {
			for (Square h : level.getSquareList()) {
				if (sleep > 0) {
					graphics2D.drawPolygon(h.doubleToIntArray(h.xpoints), h.doubleToIntArray(h.ypoints),
							(int) h.npoints);
				}
				h.initCells();
				for (int i = 0; i < h.SIDES; i++) {
					if (h.shortConeArray[i] != null && h.mediumConeArray[i] != null && h.longConeArray[i] != null
							&& (int) h.xpoints[i] > 0 && (int) h.xpoints[i] < (image.getWidth() * scale)
							&& (int) h.ypoints[i] > 0 && (int) h.ypoints[i] < (image.getHeight() * scale)) {
						h.shortConeArray[i]
								.input(image.getRGB((int) (h.xpoints[i] * scale), (int) (h.ypoints[i] * scale)));
						h.mediumConeArray[i]
								.input(image.getRGB((int) (h.xpoints[i] * scale), (int) (h.ypoints[i] * scale)));
						h.longConeArray[i]
								.input(image.getRGB((int) (h.xpoints[i] * scale), (int) (h.ypoints[i] * scale)));
					} else {
						h.shortConeArray[i].input(Color.black.getRGB());
						h.mediumConeArray[i].input(Color.black.getRGB());
						h.longConeArray[i].input(Color.black.getRGB());
					}
				}
				h.addCoincidence(h.getCoincidence(this.type), concept, false);
			}
			this.propagate(concept);
		}

	}

	@JsonIgnore
	public LinkedList<Square> getSquareList(Map<String, Square> squareMap) {
		LinkedList<Square> squareList = new LinkedList<Square>();
		for (Map.Entry<String, Square> entry : squareMap.entrySet()) {
			squareList.add((Square) entry.getValue());
		}
		return squareList;
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

	public static Map<String, Square> getSquareMap(int level, Point origin, int dimension, int length, int padding) {
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
				double x = (origin.x + (xPosition * length) );
				double y = (origin.y + (yPosition * length));
				square = new Square(xPosition,yPosition, new Point(x,y),radius);
				if (level > -1)
					squareMap.put(level + ":" + xPosition + "," + yPosition, square);
				else
					squareMap.put(xPosition + "," + yPosition, square);
			}
		}
		return squareMap;
	}
}
