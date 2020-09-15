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

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.meritoki.library.cortex.model.Concept;
import com.meritoki.library.cortex.model.Point;
import com.meritoki.library.cortex.model.group.Group;
import com.meritoki.library.cortex.model.network.shape.Shape;

@JsonTypeInfo(use = Id.CLASS,
include = JsonTypeInfo.As.PROPERTY,
property = "type")
@JsonSubTypes({
@Type(value = Network.class),
@Type(value = Group.class),
})
public class Cortex {
	@JsonProperty
	public String uuid = null;
	@JsonProperty
	public Color type = Color.BRIGHTNESS ;//int type;//com.meritoki.library.cortex.model.Type type = com.meritoki.library.cortex.model.Type.HEXAGONAL;
	@JsonProperty
	public Configuration configuration = Configuration.HEXAGONAL;
	@JsonProperty
	public int size = 27;
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
	@JsonIgnore
	public List<Point> pointList;
	
	@JsonIgnore
	public void load() {}
	
	@JsonIgnore
	public void update() {}
	
	@JsonIgnore
	public void setOrigin(int x, int y) {
		System.out.println("setOrigin("+x+", "+y+")");
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
	
	public double getSensorRadius() {
		double max = 0;
		for(Entry<String, Shape> e: this.shapeMap.entrySet()) {
//			double x = e.getValue().getX();
//			double y = e.getValue().getY();
//			double r = Math.sqrt(Math.pow(x,2)+Math.pow(y, 2));
//			if(r > max) {
//				max = r;
//			}
			String key = e.getKey();
			if(key.startsWith("0:")) {
//				System.out.println("getSensorRedius() key="+key);
				Shape shape = e.getValue();
				double[] xPoints = shape.xpoints;
				double[] yPoints = shape.ypoints;
				double nPoints = shape.npoints;
				for(int i=0;i<nPoints; i++) {
					double x = xPoints[i]-this.x;//e.getValue().getX();
					double y = yPoints[i]-this.y;//e.getValue().getY();
					double r = Math.sqrt(Math.pow(x,2)+Math.pow(y, 2));
					if(r > max) {
						max = r;
					}	
				}
			}
		}
		
//		System.out.println("getSensorRedius() max="+max);
		return max;
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
	
	public List<Point> getPointList() {
		return null;
	}
}
