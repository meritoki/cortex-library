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
package com.meritoki.library.cortex.model.network.shape;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.meritoki.library.cortex.model.Coincidence;
import com.meritoki.library.cortex.model.Concept;
import com.meritoki.library.cortex.model.Node;
import com.meritoki.library.cortex.model.Point;
import com.meritoki.library.cortex.model.cell.Cone;
import com.meritoki.library.cortex.model.cell.Wavelength;
import com.meritoki.library.cortex.model.network.Color;

public class Shape extends Node<Object> {

	protected static Logger logger = LoggerFactory.getLogger(Shape.class.getName());
	@JsonProperty
	private int x = 0;
	@JsonProperty
	private int y = 0;
	@JsonProperty
	public int length;
	@JsonIgnore
	public int sides;
	@JsonIgnore
	public int rotation;
	@JsonProperty
	public Point center = new Point(0, 0);
	@JsonProperty
	public double radius;
	@JsonProperty
	private Point[] points;
	@JsonProperty
	public double npoints;
	@JsonProperty
	public double[] xpoints = null;
	@JsonProperty
	public double[] ypoints = null;
	@JsonProperty
	public Coincidence coincidence;
//	@JsonIgnore
//	public Coincidence brightnessCoincidence;
//	@JsonIgnore
//	public Coincidence redCoincidence;
//	@JsonIgnore
//	public Coincidence greenCoincidence;
//	@JsonIgnore
//	public Coincidence blueCoincidence;
	@JsonProperty
	public Coincidence previousPrediction = null;
	@JsonProperty
	public Coincidence prediction = null;
	@JsonProperty
	public Coincidence previousCoincidence = null;
	@JsonIgnore
	public Coincidence defaultCoincidence = null;
	@JsonIgnore
	public Cone[] shortConeArray;
	@JsonIgnore
	public Cone[] mediumConeArray;
	@JsonIgnore
	public Cone[] longConeArray;
	@JsonProperty
	protected List<Coincidence> coincidenceList = new LinkedList<>();
	@JsonProperty
	public Map<String, Integer> coincidenceCountMap = new HashMap<>();
	@JsonProperty
	public Map<String, Integer> coincidenceUnionCountMap = new HashMap<>();
	@JsonProperty
	public Map<String, Map<String, Double>> coincidenceConditionalMap = new HashMap<>();
	@JsonProperty
	public Map<String, List<Concept>> conceptListMap = new HashMap<>();
	@JsonProperty
	protected LinkedList<Integer> correctList = new LinkedList<>();
	@JsonIgnore
	public static final int MEMORY = 4096;
	@JsonIgnore
	public int red;
	@JsonIgnore
	public int green;
	@JsonIgnore 
	public int blue;

	public Shape() {
//		logger.info("Shape()");
	}

	public Shape(int sides, int rotation, int x, int y, Point center, double radius) {
		super(x + "," + y);
//		logger.info("Shape("+sides+", "+rotation+", "+x+", "+y+", "+center+", "+radius+")");
		this.sides = sides;
		switch (this.sides) {
		case 4: {
			this.coincidence = new Coincidence(9);
//			this.brightnessCoincidence = new Coincidence(9);
//			this.redCoincidence = new Coincidence(9);
//			this.greenCoincidence = new Coincidence(9);
//			this.blueCoincidence = new Coincidence(9);
			break;
		}
		case 6: {
			this.coincidence = new Coincidence(7);
//			this.brightnessCoincidence = new Coincidence(7);
//			this.redCoincidence = new Coincidence(7);
//			this.greenCoincidence = new Coincidence(7);
//			this.blueCoincidence = new Coincidence(7);
			break;
		}
		}
		this.rotation = rotation;
		this.x = x;
		this.y = y;
		this.center = center;
		this.radius = radius;
		this.points = new Point[sides + 1];
		this.npoints = sides;
		this.xpoints = new double[sides + 1];
		this.ypoints = new double[sides + 1];

		this.updatePoints();
		this.initCells();
	}

	@Override
	public boolean equals(Object o) {
		boolean flag = false;
		if (o instanceof Shape) {
			Shape shape = (Shape) o;
			flag = shape.getX() == this.getX() && shape.getY() == this.getY();
		}
		return flag;
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
	public double getRadius() {
		return radius;
	}

	@JsonIgnore
	public void setRadius(int radius) {
		this.radius = radius;
		updatePoints();
	}

	@JsonIgnore
	public int getRotation() {
		return rotation;
	}

	@JsonIgnore
	public void setRotation(int rotation) {
		this.rotation = rotation;
		updatePoints();
	}

	@JsonIgnore
	public Point getCenter() {
		return this.center;
	}

	@JsonIgnore
	public void setCenter(Point center) {
		this.center = center;
		updatePoints();
		initCells();
	}

	@JsonIgnore
	public void setCenter(int x, int y) {
		setCenter(new Point(x, y));
	}
	
	public List<Concept> getConceptList(Coincidence c) {
		return this.conceptListMap.get(c.toString());
	}

	public Coincidence getCoincidence() {
		return this.coincidence;
	}

	public void setCoincidence(Coincidence coincidence) {
		this.coincidence = coincidence;
	}

	@JsonIgnore
	public void updatePoints() {
		Point point = new Point(center.x, center.y);
		xpoints[0] = point.x;
		ypoints[0] = point.y;
		points[0] = point;
		for (int i = 1; i < this.sides + 1; i++) {
			double angle = findAngle((double) i / this.sides);
			point = findPoint(angle);
			xpoints[i] = point.x;
			ypoints[i] = point.y;
			points[i] = point;
		}
	}

	@JsonIgnore
	public void initCells() {
		this.shortConeArray = new Cone[sides + 1];
		this.mediumConeArray = new Cone[sides + 1];
		this.longConeArray = new Cone[sides + 1];
		for (int i = 0; i < this.sides + 1; i++) {
			shortConeArray[i] = new Cone(Wavelength.SHORT);
			mediumConeArray[i] = new Cone(Wavelength.MEDIUM);
			longConeArray[i] = new Cone(Wavelength.LONG);
		}
	}

	@JsonIgnore
	private double findAngle(double fraction) {
		return fraction * Math.PI * 2 + Math.toRadians((rotation + 180) % 360);
	}

	@JsonIgnore
	private Point findPoint(double angle) {
		double x = round((double) (center.x + Math.cos(angle) * radius), 3);
		double y = round((double) (center.y + Math.sin(angle) * radius), 3);
		return new Point(x, y);
	}

	@JsonIgnore
	public double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}

	/**
	 * Function has a lot of responsibility. It matches a input coincidence by
	 * minimum and maximum similarity with a list of coincidences already input If a
	 * similarity is found
	 * 
	 * @param coincidence
	 * @param concept
	 * @param threshold
	 */
	@JsonIgnore
	public void addCoincidence(Coincidence coincidence, Concept concept, boolean flag) {
		Coincidence c = null;
		Integer count = 0;
		double max = 0;
		Coincidence inferredCoincidence = null;
		if (coincidence != null && coincidence.list.size() > 0) {
			for (int i = 0; i < this.coincidenceList.size(); i++) {
				c = this.coincidenceList.get(i);
//				if(concept == null) {
//					c.setThreshold(0.95);
//				} else {
//					c.setThreshold(0.99);
//				}
				if (c.similar(coincidence, max)) {
					max = c.quotient;
					inferredCoincidence = c;
				}
			}
			this.previousCoincidence = this.coincidence;
			if(flag) {
				List<Concept> conceptList = null;
				if (inferredCoincidence != null) {
					count = this.coincidenceCountMap.get(inferredCoincidence.toString());
					count = (count == null) ? 0 : count;
					this.coincidenceCountMap.put(inferredCoincidence.toString(), count + 1);
					conceptList = this.conceptListMap.get(inferredCoincidence.toString());
					this.conceptListMap.put(inferredCoincidence.toString(), conceptList);
				}
				if (conceptList == null) {
					conceptList = new ArrayList<>();
				}
				if (concept != null) {
					conceptList.add(concept);
				}
				this.coincidence = coincidence;
				this.conceptListMap.put(this.coincidence.toString(), conceptList);
				this.coincidenceList.add(this.coincidence);
			} else {
				this.coincidence = coincidence;
			}
//			if (this.previousPrediction != null) {
//				if (this.previousPrediction.equals(this.coincidence)) {
//					correctList.add(1);
//				} else {
//					correctList.add(0);
//				}
//			}
//			this.previousPrediction = this.prediction;
//			this.prediction = this.predictCoincidence(this.coincidence, this.previousCoincidence);
//			this.purgeCorrectList();
		}
		if (this.coincidenceList.size() > MEMORY) {// this.getFrequencyMax() + this.buffer) {
			this.purgeCoincidenceList();
		}
	}

	/**
	 * Function uses a map of coincidence frequency to determine if it should be
	 * remove from the coincidenceList. A coincidence with a frequency greater than
	 * a threshold value are kept.
	 */
	@JsonIgnore
	public void purgeCoincidenceList() {
		Coincidence c = null;
		List<Coincidence> cList = new LinkedList<>();
		for (int i = 0; i < this.coincidenceList.size(); i++) {
			c = this.coincidenceList.get(i);
			if (this.coincidenceCountMap.get(c.toString()) == null) {
				cList.add(c);
			}
		}
		for (Coincidence coincidence : cList) {
			this.conceptListMap.remove(coincidence);
		}
		this.coincidenceList.removeAll(cList);
	}

//	@JsonIgnore
//	public void purgeCorrectList() {
//		while (this.correctList.size() > 7) {
//			this.correctList.pop();
//		}
//	}
//	
	@JsonIgnore
	public void purgeCorrectList() {
		while (this.correctList.size() > 7) {
			this.correctList.pop();
		}
	}

	@JsonIgnore
	public double getCorrectPercentage() {
		int oneCount = 0;
		for (Integer i : this.correctList) {
			oneCount += i;
		}
//		logger.info("getCorrectPercentage() oneCount="+oneCount);
//		logger.info("getCorrectPercentage() this.correctList.size()="+this.correctList.size());
		return (this.correctList.size() > 0) ? (double) oneCount / (double) this.correctList.size() : 0;
	}

//	@JsonIgnore
//	public Coincidence getConditionalCoincidence(Coincidence b) {
//		Coincidence coincidence = null;
//		if (b != null) {
//			Map<String, Double> aMap = this.coincidenceConditionalMap.get(b.toString());
//			if (aMap != null) {
//				double max = 0;
//				for (Map.Entry<String, Double> entry : aMap.entrySet()) {
//					if (entry.getValue() > max) {
//						max = entry.getValue();
//						coincidence = new Coincidence(entry.getKey());
//					}
//				}
//			}
//		}
//		return coincidence;
//	}

	/**
	 * Function builds a map of maps. Two
	 * 
	 * @param aCoincidence
	 * @param bCoincidence
	 */
	@JsonIgnore
	public Coincidence predictCoincidence(Coincidence aCoincidence, Coincidence bCoincidence) {
		String a = (aCoincidence != null) ? aCoincidence.toString() : "[]";
		String b = (bCoincidence != null) ? bCoincidence.toString() : "[]";
		String ab = a + "," + b;
		Integer bCount = (coincidenceCountMap.get(b) == null) ? 0 : coincidenceCountMap.get(b);
		Integer aCount = (coincidenceCountMap.get(a) == null) ? 0 : coincidenceCountMap.get(a);
		Integer abCount = (this.coincidenceUnionCountMap.get(ab) == null) ? 0 : this.coincidenceUnionCountMap.get(ab);
//		aCount += 1;
//		this.coincidenceFrequencyMap.put(a, aCount);
		abCount += 1;
		this.coincidenceUnionCountMap.put(ab, abCount);
		double total = (double) this.getTotal(this.coincidenceCountMap);
		double aProbability = (total > 0) ? (double) aCount / total : 0;
		double bProbability = (total > 0) ? (double) bCount / total : 0;
		total = (double) this.getTotal(this.coincidenceUnionCountMap);
		double abProbability = (total > 0) ? (double) abCount / total : 0;
		double aGivenB = (bProbability > 0) ? (double) abProbability / (double) bProbability : 0;
		if (aGivenB > MEMORY) {
//			logger.info("predictCoincidence(aCoincidence, bCoincidence) P(A|B)=" + aGivenB);
			Map<String, Double> aMap = (this.coincidenceConditionalMap.get(b) == null) ? new HashMap<String, Double>()
					: this.coincidenceConditionalMap.get(b);
			aMap.put(a, aGivenB);
			this.coincidenceConditionalMap.put(b, aMap);
		}

		return this.getConditionalCoincidence(aCoincidence);
	}

//	@JsonIgnore
//	public int getTotal(Map<String, Integer> map) {
//		int sum = 0;
//		if (map != null) {
//			for (Integer i : map.values()) {
//				sum += i;
//			}
//		}
//		return sum;
//	}

	@JsonIgnore
	public Coincidence getConditionalCoincidence(Coincidence b) {
		Coincidence coincidence = null;
		if (b != null) {
			Map<String, Double> aMap = this.coincidenceConditionalMap.get(b.toString());
			if (aMap != null) {
				double max = 0;
				for (Map.Entry<String, Double> entry : aMap.entrySet()) {
					if (entry.getValue() > max) {
						max = entry.getValue();
						coincidence = new Coincidence(entry.getKey());
					}
				}
			}
		}
		return coincidence;
	}

	@JsonIgnore
	public Coincidence getCoincidence(Color type) {
//		logger.info("getCoincidence("+type+")");
		Coincidence coincidence = new Coincidence();
		int value = 0;
		for (int i = 0; i < this.sides + 1; i++) {
			switch (type) {
			case BRIGHTNESS: {
				value = (shortConeArray[i].red + mediumConeArray[i].green + longConeArray[i].blue) / 3;
				break;
			}
			case RED: {
				value = shortConeArray[i].red;
				break;
			}
			case GREEN: {
				value = mediumConeArray[i].green;
				break;
			}
			case BLUE: {
				value = longConeArray[i].blue;
				break;
			}
			default: {
				value = 0;
				break;
			}
			}
			coincidence.addInteger(value);
		}
//		logger.info("getCoincidence("+type+") coincidence="+coincidence);
		return coincidence;
	}

	@JsonIgnore
	public int getFrequencyMax() {
		int count = 0;
		for (Map.Entry<String, Integer> entry : this.coincidenceCountMap.entrySet()) {
			if (entry.getValue() > 0) {
				count++;
			}
		}
		return count;
	}

	@JsonIgnore
	public int[] doubleToIntArray(double[] array) {
		int[] intArray = new int[array.length];
		for (int i = 0; i < array.length; i++) {
			intArray[i] = (int) array[i];
		}
		return intArray;
	}

	@JsonIgnore
	public int getTotal(Map<String, Integer> map) {
		int sum = 0;
		if (map != null) {
			for (Integer i : map.values()) {
				sum += i;
			}
		}
		return sum;
	}

	@JsonIgnore
	public String toString() {
		return this.getX() + "," + this.getY();// (String)this.getData();//
	}
}
