package com.meritoki.library.cortex.model;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.meritoki.library.cortex.model.hexagon.Hexagonal;
import com.meritoki.library.cortex.model.square.Squared;
@JsonTypeInfo(use = Id.CLASS,
include = JsonTypeInfo.As.PROPERTY,
property = "type")
@JsonSubTypes({
@Type(value = Hexagonal.class),
@Type(value = Squared.class),

})
public class Network {
	
	private static Logger logger = LogManager.getLogger(Network.class.getName());
	@JsonIgnore
	public static final int BRIGHTNESS = 1;
	@JsonIgnore
	public static final int RED = 2;
	@JsonIgnore
	public static final int GREEN = 3;
	@JsonIgnore
	public static final int BLUE = 4;
	@JsonProperty
	private int type = 0;
	@JsonIgnore
	protected int x = 0;
	@JsonIgnore
	protected int y = 0;
	@JsonProperty
	public String uuid = null;
	@JsonIgnore
	protected LinkedList<Level> levelList = new LinkedList<>();
	@JsonProperty
	protected Map<String, Shape> shapeMap = new HashMap<>();
	@JsonProperty
	private List<Belief> beliefList = new ArrayList<>();
	
	@JsonProperty
	public int size = 13;
	@JsonProperty
	public int radius = 1;
	@JsonProperty
	public int dimension = 13;
	@JsonProperty
	public int length = 1;
	@JsonProperty
	public int padding = 0;
	@JsonProperty
	public int depth = 0;
	
	public Network() {
		this.type = BRIGHTNESS;
		this.uuid = UUID.randomUUID().toString();
	}
	
	public Network(int type, int x, int y) {
		this.type = type;
		this.x = x;
		this.y = y;
		this.uuid = UUID.randomUUID().toString();
	}
	
	@JsonIgnore
	public void setOrigin(int x, int y) {
		this.x = x;
		this.y = y;
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
		logger.info("propogate(" + concept + ")");
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
	
	@JsonIgnore
	public List<Concept> process(BufferedImage image, double scale, Concept concept) {
		Level level = this.getInputLevel();
		if (level != null && image != null) {
			for (Shape s : level.getShapeList()) {
				s.initCells();
				for (int i = 0; i < s.sides; i++) {
					if (s.shortConeArray[i] != null && s.mediumConeArray[i] != null && s.longConeArray[i] != null
							&& (int) s.xpoints[i] > 0 && (int) s.xpoints[i] < (image.getWidth() * scale)
							&& (int) s.ypoints[i] > 0 && (int) s.ypoints[i] < (image.getHeight() * scale)) {
						s.shortConeArray[i]
								.input(image.getRGB((int) (s.xpoints[i] * scale), (int) (s.ypoints[i] * scale)));
						s.mediumConeArray[i]
								.input(image.getRGB((int) (s.xpoints[i] * scale), (int) (s.ypoints[i] * scale)));
						s.longConeArray[i]
								.input(image.getRGB((int) (s.xpoints[i] * scale), (int) (s.ypoints[i] * scale)));
					} else {
						s.shortConeArray[i].input(Color.black.getRGB());
						s.mediumConeArray[i].input(Color.black.getRGB());
						s.longConeArray[i].input(Color.black.getRGB());
					}
				}
				s.addCoincidence(s.getCoincidence(this.type), concept, false);
			}
			this.propagate(concept);
		}
		return this.getRootLevel().getCoincidenceConceptList();
	}
}
