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
package com.meritoki.library.cortex.model;

import java.awt.Graphics2D;
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
	
	public int getX() {
		return this.x;
	}

	public int getY() {
		return this.y;
	}
	
	public void process(Graphics2D graphics2D, BufferedImage image, Concept concept) {}
	
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
