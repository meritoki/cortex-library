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
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * In Network, hexagons are referenced by level and relative coordinates, i.e.
 * 0:00, 4:00
 * 
 * @author osvaldo.rodriguez
 *
 */
public class Network {

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
	private Map<String, Hexagon> hexagonMap = new HashMap<>();
	@JsonProperty
	private int type = 0;
	@JsonProperty
	public String uuid = null;
	@JsonIgnore
	private int x = 0;
	@JsonIgnore
	private int y = 0;
	@JsonProperty
	private int size = 13;
	@JsonProperty
	private int radius = 1;
	@JsonProperty
	private int padding = 0;
	@JsonProperty
	private int depth = 0;
	@JsonProperty
	private List<Belief> beliefList = new ArrayList<>();

	public Network() {
		this.uuid = UUID.randomUUID().toString();
	}

	public Network(int type, int x, int y, int size, int radius, int padding) {
		this.uuid = UUID.randomUUID().toString();
		this.type = type;
		this.x = x;
		this.y = y;
		this.size = size;
		this.radius = radius;
		this.padding = padding;
	}

	public Network(int size, int radius, int padding) {
		this.uuid = UUID.randomUUID().toString();
		this.size = size;
		this.radius = radius;
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
	public Level peekLevel() {
		Level level = this.levelList.peek();
		logger.info("peekLevel() level=" + level);
		return this.levelList.peek();
	}

	@JsonIgnore
	public void setConcept(Concept concept) {
		for (Level level : this.levelList) {
			level.propagate(0, concept, true);
		}
	}

	@JsonIgnore
	public void setHexagonMap(Map<String, Hexagon> hexagonMap) {
//		this.hexagonMap = hexagonMap;
		for (Map.Entry<String, Hexagon> entry : hexagonMap.entrySet()) {
			String key = entry.getKey();
			Hexagon value = entry.getValue();
			this.hexagonMap.put(key, value);
		}
	}

	@JsonIgnore
	public Map<String, Hexagon> getHexagonMap() {
		return this.hexagonMap;
	}

	/**
	 * Function uses hexagonMap initialized with loaded parameters, to generate keys
	 * that retrieve loaded hexagons from file and connects them in parent child
	 * relationships, forming a tree data structure that converges on a root not
	 */
	@JsonIgnore
	public void load() {
		logger.info("load()");
		logger.info("load() this.hexagonMap=" + this.hexagonMap);
		logger.info("load() this.size=" + this.size);
		logger.info("load() this.radius=" + this.radius);
		Map<String, Hexagon> hexagonMap = getHexgonMap(-1, new Point(this.x, this.y), this.size, this.radius,
				this.padding);
		int depth = (this.depth > 0) ? this.depth : this.getDepth(hexagonMap.size());
		if (this.depth == 0) {
			this.depth = depth;
		}
		Level level = new Level();
		List<Hexagon> hexagonList = this.getHexagonList(hexagonMap);
		Hexagon hexagon = null;
		for (Hexagon n : hexagonList) {
			hexagon = this.hexagonMap.get("0:" + n);
			if (hexagon == null) {
				hexagon = new Hexagon(n);
				this.hexagonMap.put("0:" + hexagon, hexagon);
			}
			hexagon.setData("0:" + hexagon);
			hexagon.initCells();
			level.addHexagon(hexagon);
		}
		this.addLevel(level);
		LinkedList<Hexagon> hexagonStack = null;
		int exponent = 0;
		for (int i = 1; i < depth; i++) {
			logger.debug("load() i=" + i);
			logger.debug("load() exponent=" + exponent);
			if (i % 2 == 0) {
				exponent++;
			}
			hexagonMap = this.peekLevel().getHexagonMap();
			level = new Level();
			hexagonList = new LinkedList<>();
			hexagonStack = new LinkedList<>();
			hexagonStack.push(hexagonMap.get(this.x + "," + this.y));
			while (!hexagonStack.isEmpty()) {
				hexagon = hexagonStack.pop();
				LinkedList<Hexagon> list = null;
				if (i % 2 == 1) {
					list = getOneHexagonList(hexagonMap, hexagon.getX(), hexagon.getY(), exponent); // get list for x, y
				} else {
					list = getZeroHexagonList(hexagonMap, hexagon.getX(), hexagon.getY(), exponent); // get list for x,
					// y
				}
				for (Hexagon h : list) {
					if (!hexagonList.contains(h)) {
						hexagonList.add(h);
						hexagonStack.push(h);
					}
				}
			}
			for (Hexagon m : hexagonList) {
				hexagon = this.hexagonMap.get(i + ":" + m);
				if (hexagon == null) {
					hexagon = new Hexagon(m);
					this.hexagonMap.put(i + ":" + hexagon, hexagon);
				}
				List<Hexagon> list = null;
				if (i % 2 == 1) {
					list = this.getZeroHexagonList(hexagonMap, hexagon.getX(), hexagon.getY(), exponent);
				} else {
					list = this.getOneHexagonList(hexagonMap, hexagon.getX(), hexagon.getY(), exponent - 1);
				}
				for (Hexagon n : list) {
					hexagon.addChild(n);
				}
				hexagon.setData(i + ":" + hexagon);
				level.addHexagon(hexagon);
			}
			this.addLevel(level);
		}
//		if (logger.isDebugEnabled()) {
//			level = this.peekLevel();
//			Hexagon h = level.getHexagonList().get(0);
//			Node.printTree(h, " ");
//		}
	}

	/**
	 * Function initializes the network of nodes, or hexagons, that converges into a
	 * single root node.
	 */

	public void input(Concept concept) {
		logger.info("input()");
		Level level = this.getInputLevel();
		List<Hexagon> hexagonList = level.getHexagonList();
		for (Hexagon hexagon : hexagonList) {
			hexagon.addCoincidence(hexagon.getCoincidence(this.type), concept, false);
		}
	}

	/**
	 * Need to modify so that it only moves the input level, not all levels this
	 * will improve the performance of the algorithm.
	 */
	@JsonIgnore
	public void update() {
		double radians = Math.toRadians(30);
		double xOff = Math.cos(radians) * (radius + padding);
		double yOff = Math.sin(radians) * (radius + padding);
		int half = size / 2;
		Level level = null;
		Hexagon hexagon = null;
		level = this.getInputLevel();
		for (int row = 0; row < size; row++) {
			int cols = size - java.lang.Math.abs(row - half);
			for (int col = 0; col < cols; col++) {
				int xPosition = row < half ? col - row : col - half;
				int yPosition = row - half;
				int x = (int) (this.x + xOff * (col * 2 + 1 - cols));
				int y = (int) (this.y + yOff * (row - half) * 3);
				hexagon = level.hexagonMap.get(xPosition + "," + yPosition);
				if (hexagon != null) {
					hexagon.setCenter(new Point(x, y));
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
				if(i == size-1) {
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
			for (Hexagon h : level.getHexagonList()) {
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
				h.addCoincidence(h.getCoincidence(this.type), concept ,false);
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
			for (Hexagon h : level.getHexagonList()) {
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
	public LinkedList<Hexagon> getHexagonList(Map<String, Hexagon> hexagonMap) {
		LinkedList<Hexagon> hexagonList = new LinkedList<Hexagon>();
		for (Map.Entry<String, Hexagon> entry : hexagonMap.entrySet()) {
			hexagonList.add((Hexagon) entry.getValue());
		}
		return hexagonList;
	}

	@JsonIgnore
	public LinkedList<Hexagon> getZeroHexagonList(Map<String, Hexagon> hexagonMap, int x, int y, int exponent) {
		LinkedList<Hexagon> hexagonList = new LinkedList<>();
		Hexagon h = null;
		int multiplier = (int) Math.pow(3, exponent);
		// (0,0)
		h = hexagonMap.get((x) + "," + (y));
		if (h != null) {
			hexagonList.push(h);
		}
		// (+3,-3)
		h = hexagonMap.get((x + (1 * multiplier)) + "," + (y - (1 * multiplier)));
		if (h != null) {
			hexagonList.push(h);
		}
		// (+3,0)
		h = hexagonMap.get((x + (1 * multiplier)) + "," + (y));
		if (h != null) {
			hexagonList.push(h);
		}
		// (0,+3)
		h = hexagonMap.get((x) + "," + (y + (1 * multiplier)));
		if (h != null) {
			hexagonList.push(h);
		}
		// (-3,+3)
		h = hexagonMap.get((x - (1 * multiplier)) + "," + (y + (1 * multiplier)));
		if (h != null) {
			hexagonList.push(h);
		}
		// (-3,0)
		h = hexagonMap.get((x - (1 * multiplier)) + "," + (y));
		if (h != null) {
			hexagonList.push(h);
		}
		// (0,-3)
		h = hexagonMap.get((x) + "," + (y - (1 * multiplier)));
		if (h != null) {
			hexagonList.push(h);
		}

		return hexagonList;
	}

	@JsonIgnore
	public LinkedList<Hexagon> getOneHexagonList(Map<String, Hexagon> hexagonMap, int x, int y, int exponent) {
		LinkedList<Hexagon> hexagonList = new LinkedList<>();
		Hexagon h = null;
		int multiplier = (int) Math.pow(3, exponent);
		// (0,0)
		h = hexagonMap.get((x) + "," + (y));
		if (h != null) {
			hexagonList.push(h);
		}
		// (+3,-6)
		h = hexagonMap.get((x + (1 * multiplier)) + "," + (y - (2 * multiplier)));
		if (h != null) {
			hexagonList.push(h);
		}
		// (+6,-3)
		h = hexagonMap.get((x + (2 * multiplier)) + "," + (y - (1 * multiplier)));
		if (h != null) {
			hexagonList.push(h);
		}
		// (+3,+3)
		h = hexagonMap.get((x + (1 * multiplier)) + "," + (y + (1 * multiplier)));
		if (h != null) {
			hexagonList.push(h);
		}
		// (-3,+6)
		h = hexagonMap.get((x - (1 * multiplier)) + "," + (y + (2 * multiplier)));
		if (h != null) {
			hexagonList.push(h);
		}
		// (-6,+3)
		h = hexagonMap.get((x - (2 * multiplier)) + "," + (y + (1 * multiplier)));
		if (h != null) {
			hexagonList.push(h);
		}
		// (-3,-3)
		h = hexagonMap.get((x - (1 * multiplier)) + "," + (y - (1 * multiplier)));
		if (h != null) {
			hexagonList.push(h);
		}
		return hexagonList;
	}

	@JsonIgnore
	public static Map<String, Hexagon> getHexgonMap(int level, Point origin, int size, int radius, int padding) {
		Map<String, Hexagon> hexagonMap = new HashMap<>();
		double radians = Math.toRadians(30);
		double xOff = Math.cos(radians) * (radius + padding);
		double yOff = Math.sin(radians) * (radius + padding);
		int half = size / 2;
		Hexagon hexagon = null;
		for (int row = 0; row < size; row++) {
			int cols = size - java.lang.Math.abs(row - half);
			for (int col = 0; col < cols; col++) {
				int xPosition = row < half ? col - row : col - half;
				int yPosition = row - half;
				int x = (int) (origin.x + xOff * (col * 2 + 1 - cols));
				int y = (int) (origin.y + yOff * (row - half) * 3);
				hexagon = new Hexagon(xPosition, yPosition, new Point(x, y), radius);
				if (level > -1)
					hexagonMap.put(level + ":" + xPosition + "," + yPosition, hexagon);
				else
					hexagonMap.put(xPosition + "," + yPosition, hexagon);
			}
		}
		return hexagonMap;
	}

	@JsonIgnore
	public int getDepth(int value) {
		boolean flag = true;
		int count = 1;
		do {
			value /= 3;
			if (value <= 1) {
				flag = false;
			}
			logger.info("getLevelCount(" + value + ")");
			count++;
		} while (flag);
		logger.info("getLevelCount(" + value + ") count=" + count);
		return count;
	}
}

//@JsonIgnore
//public void init() {
//	logger.info("init()");
//	this.setHexagonMap(this.getHexgonMap(new Point(this.x, this.y), this.size, this.radius, this.padding));
//	int count = this.getDepth(this.getHexagonMap().size());
//	Level level = new Level(this.getHexagonMap());
//	for (Node n : level.getHexagonList()) {
//		n.setData("0:" + n.getData());
//	}
//	this.pushLevel(level);
//	LinkedList<Hexagon> hexagonList = null;
//	LinkedList<Hexagon> hexagonStack = null;
//	Map<String, Hexagon> hexagonMap = null;
//	int exponent = 0;
//	for (int i = 1; i < count; i++) {
//		logger.debug("init() i=" + i);
//		logger.debug("init() exponent=" + exponent);
//		if (i % 2 == 0) {
//			exponent++;
//		}
//		hexagonMap = this.peekLevel().getHexagonMap();
//		level = new Level();
//		hexagonList = new LinkedList<>();
//		hexagonStack = new LinkedList<>();
//		hexagonStack.push(hexagonMap.get(this.x + "," + this.y));
//		while (!hexagonStack.isEmpty()) {
//			Hexagon hexagon = hexagonStack.pop();
//			LinkedList<Hexagon> list = null;
//			if (i % 2 == 1) {
//				list = getOneHexagonList(hexagonMap, hexagon.getX(), hexagon.getY(), exponent); // get list for x, y
//			} else {
//				list = getZeroHexagonList(hexagonMap, hexagon.getX(), hexagon.getY(), exponent); // get list for x,
//				// y
//			}
//			for (Hexagon h : list) {
//				if (!hexagonList.contains(h)) {
//					hexagonList.add(h);
//					hexagonStack.push(h);
//				}
//			}
//		}
//		Hexagon hexagon;
//		for (Hexagon m : hexagonList) {
//			hexagon = new Hexagon(m);
//			List<Hexagon> list = null;
//			if (i % 2 == 1) {
//				list = this.getZeroHexagonList(hexagonMap, m.getX(), m.getY(), exponent);
//			} else {
//				list = this.getOneHexagonList(hexagonMap, m.getX(), m.getY(), exponent - 1);
//			}
//			for (Hexagon n : list) {
//				hexagon.addChild(n);
//			}
//			hexagon.setData(i + ":" + (String) hexagon.getData());
//			level.addHexagon(hexagon);
//		}
//		this.pushLevel(level);
//	}
////	if (logger.isDebugEnabled()) {
////		level = this.peekLevel();
////		Hexagon h = level.getHexagonList().get(0);
////		Node.printTree(h, " ");
////	}
//}

//@JsonIgnore
//public void update() {
//	double radians = Math.toRadians(30);
//	double xOff = Math.cos(radians) * (radius + padding);
//	double yOff = Math.sin(radians) * (radius + padding);
//	int half = size / 2;
//	Hexagon hexagon = null;
//	for (int row = 0; row < size; row++) {
//		int cols = size - java.lang.Math.abs(row - half);
//		for (int col = 0; col < cols; col++) {
//			int xPosition = row < half ? col - row : col - half;
//			int yPosition = row - half;
//			int x = (int) (this.x + xOff * (col * 2 + 1 - cols));
//			int y = (int) (this.y + yOff * (row - half) * 3);
//			hexagon = this.hexagonMap.get(xPosition + "," + yPosition);
//			hexagon.setCenter(new Point(x, y));
//		}
//	}
//}


//public Belief process(Graphics2D graphics2D, BufferedImage image, double scale, Concept concept, int sleep) {
//	logger.info("processing...");
//	Belief belief = null;
//	int size = this.getLevelStack().size();
//	Level level = (size > 0) ? this.getLevelStack().get(size - 1) : null;
//	if (level != null && image != null) {
//		for (Hexagon h : level.getHexagonList()) {
//			if (sleep > 0) {
//				graphics2D.drawPolygon(h.doubleToIntArray(h.xpoints), h.doubleToIntArray(h.ypoints),
//						(int) h.npoints);
//			}
//			h.initCells();
//			for (int i = 0; i < h.SIDES; i++) {
//				if (h.shortConeArray[i] != null && h.shortConeArray[i].point != null && h.mediumConeArray[i] != null
//						&& h.mediumConeArray[i].point != null && h.longConeArray[i] != null
//						&& h.longConeArray[i].point != null && (int) h.shortConeArray[i].point.x > 0
//						&& (int) h.shortConeArray[i].point.x < (image.getWidth() * scale)
//						&& (int) h.shortConeArray[i].point.y > 0
//						&& (int) h.shortConeArray[i].point.y < (image.getHeight() * scale)) {
//					h.shortConeArray[i].input(image.getRGB((int) (h.shortConeArray[i].point.x * scale),
//							(int) (h.shortConeArray[i].point.y * scale)));
//					h.mediumConeArray[i].input(image.getRGB((int) (h.mediumConeArray[i].point.x * scale),
//							(int) (h.mediumConeArray[i].point.y * scale)));
//					h.longConeArray[i].input(image.getRGB((int) (h.longConeArray[i].point.x * scale),
//							(int) (h.longConeArray[i].point.y * scale)));
//				} else {
//					h.shortConeArray[i].input(Color.black.getRGB());
//					h.mediumConeArray[i].input(Color.black.getRGB());
//					h.longConeArray[i].input(Color.black.getRGB());
//				}
//			}
//			h.addCoincidence(h.getCoincidence(this.type), concept);
//		}
//
//		for (int i = size - 1; i >= 0; i--) {
//			level = this.getLevelStack().get(i);
//			level.propagate(concept);
//		}
//
//		if (concept == null) {
//			List<Concept> conceptList = this.getLevelStack().get(this.getIndex()).getCoincidenceConceptList();
//			concept = (conceptList.size() > 0) ? conceptList.get(0) : null;
//			if (concept != null) {// bConcept != null && aConcept.equals(bConcept)
//				belief = new Belief(concept, new Point(this.x, this.y));
////				this.beliefList.add(belief);
//			}
////			double width = 13;
////			double height = 13;
////			if (sleep > 0) {
////				for (Belief b : this.beliefList) {
////					if (b.concept.value.equals("white"))
////						graphics2D.setColor(Color.RED);
////					if (b.concept.value.equals("human"))
////						graphics2D.setColor(Color.GREEN);
////					if (b.concept.value.equals("ladder"))
////						graphics2D.setColor(Color.BLUE);
////					if (b.concept.value.equals("house"))
////						graphics2D.setColor(Color.YELLOW);
////					if (b.concept.value.equals("tree"))
////						graphics2D.setColor(Color.PINK);
////					double newX = b.point.x - width / 2.0;
////					double newY = b.point.y - height / 2.0;
////					Ellipse2D.Double ellipse = new Ellipse2D.Double(newX, newY, width, height);
////					graphics2D.draw(ellipse);
////				}
////			}
//		}
//	}
//	return belief;
//}
