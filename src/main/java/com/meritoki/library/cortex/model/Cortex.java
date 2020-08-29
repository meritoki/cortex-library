package com.meritoki.library.cortex.model;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonTypeInfo(use = Id.CLASS,
include = JsonTypeInfo.As.PROPERTY,
property = "type")
@JsonSubTypes({
@Type(value = Network.class),
@Type(value = Group.class),
})
public class Cortex {
	
	@JsonIgnore
	public static final int BRIGHTNESS = 1;
	@JsonIgnore
	public static final int RED = 2;
	@JsonIgnore
	public static final int GREEN = 3;
	@JsonIgnore
	public static final int BLUE = 4;
	@JsonIgnore
	public static final int HEXAGONAL = 1;
	@JsonIgnore
	public static final int SQUARED = 2;
	@JsonProperty
	public String uuid = null;
	@JsonProperty
	public int type = 0;
	@JsonProperty
	public int size = 13;
	@JsonProperty
	public int radius = 1;
	@JsonProperty
	public int dimension = 13;
	@JsonProperty
	public int length = 2;
	@JsonProperty
	public int padding = 0;
	@JsonProperty
	public int depth = 0;
	@JsonProperty
	public Map<String, Shape> shapeMap = new HashMap<>();
	@JsonIgnore
	public int x = 0;
	@JsonIgnore
	public int y = 0;
	@JsonProperty
	public List<Belief> beliefList = new ArrayList<>();
	
	@JsonIgnore
	public void load() {}
	
	@JsonIgnore
	public void update() {}
	
	@JsonIgnore
	public void setOrigin(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public List<Concept> process(BufferedImage image, Concept concept) {
		return null;
	}
	
	public BufferedImage scaleBufferedImage(BufferedImage bufferedImage, double scale) {
		BufferedImage before = bufferedImage;
		int w = before.getWidth();
		int h = before.getHeight();
		BufferedImage after = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		AffineTransform at = new AffineTransform();
		at.scale(2.0, 2.0);
		AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		after = scaleOp.filter(before, after);
		return after;
	}
}
