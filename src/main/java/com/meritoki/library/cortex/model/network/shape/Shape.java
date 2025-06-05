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
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.meritoki.library.cortex.model.Concept;
import com.meritoki.library.cortex.model.Point;
import com.meritoki.library.cortex.model.cell.Cell;
import com.meritoki.library.cortex.model.cell.Wavelength;

public class Shape extends Node<Object> {

	protected Logger logger = Logger.getLogger(Shape.class.getName());
	@JsonProperty
	private int x = 0;
	@JsonProperty
	private int y = 0;
	@JsonProperty
	public int size;// The size of default instantiated Coincidence
	@JsonIgnore
	public int sides;
	@JsonIgnore
	public int rotation;
	@JsonProperty
	public Point center = new Point(0, 0);
	@JsonProperty
	public double radius;
//	@JsonProperty
//	private Point[] points;
	@JsonProperty
	public int n;// represents the number of sensors
	@JsonProperty
	public double[] xDimension = null;
	@JsonProperty
	public double[] yDimension = null;
	@JsonProperty
	public Coincidence coincidence;
	@JsonProperty
	public Coincidence previousCoincidence = null;
	@JsonProperty
	public Coincidence predictionCoincidence = null;
	@JsonProperty
	public Coincidence previousPredictionCoincidence = null;
	@JsonIgnore
	public Coincidence defaultCoincidence = null;
	@JsonIgnore
	public Cell[] cellArray;
	@JsonProperty
	protected List<Coincidence> coincidenceList = new LinkedList<>();
	@JsonProperty
	public List<Integer> coincidenceIndexList = new ArrayList<>();
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
	public static final Double THRESHOLD = 1 / 1.618;

	public Shape() {
//		logger.info("Shape()");
	}

	public Shape(int sides, int size, int rotation, int x, int y, Point center, double radius) {
		super(x + "," + y);
//		logger.info("Shape("+sides+", "+rotation+", "+x+", "+y+", "+center+", "+radius+")");
		this.sides = sides;
		this.size = size;
		this.rotation = rotation;
		this.x = x;
		this.y = y;
		this.center = center;
		this.radius = radius;
		this.n = sides + 1;
		this.xDimension = new double[this.n];
		this.yDimension = new double[this.n];
		this.coincidence = new Coincidence(this.n);
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

	@JsonIgnore
	public Coincidence getCoincidence(Wavelength wavelength) {
		Coincidence coincidence = new Coincidence();
		int value = 0;
		for (int i = 0; i < this.n; i++) {
			switch (wavelength) {
			case ROD_GRAY: {
				value = cellArray[i].getWavelength(Wavelength.ROD_GRAY);
				break;
			}
			case CONE_SHORT: {
				value = cellArray[i].getWavelength(Wavelength.CONE_SHORT);
				break;
			}
			case CONE_MEDIUM: {
				value = cellArray[i].getWavelength(Wavelength.CONE_MEDIUM);
				break;
			}
			case CONE_LONG: {
				value = cellArray[i].getWavelength(Wavelength.CONE_LONG);
				break;
			}
			default: {
				value = 0;
				break;
			}
			}
			coincidence.addInteger(value);
		}
//		logger.info("getCoincidence(" + wavelength + ") coincidence=" + coincidence);
		return coincidence;
	}

	public void setCoincidence(Coincidence coincidence) {
//		logger.info("setCoincidence("+coincidence.list.size()+")");
		this.coincidence = coincidence;
	}

	/**
	 * Function has a lot of responsibility. Check if Parameter Coincidence is
	 * Similar to Any Coincidence in List Increment Counter for
	 * 
	 * @param coincidence
	 * @param concept
	 * @param threshold
	 */
	@JsonIgnore
	public void addCoincidence(Coincidence coincidence, Concept concept) {// , boolean flag) {
		Coincidence c = null;
		if (coincidence != null && coincidence.list.size() > 0) {
			this.previousCoincidence = this.coincidence;
			this.previousPredictionCoincidence = this.predictionCoincidence;
			for (int i = 0; i < this.coincidenceList.size(); i++) {
				c = this.coincidenceList.get(i);
				if (c.similar(coincidence)) {
					this.coincidenceCountIncrement(c);
					this.coincidenceCountIncrement(coincidence);
					this.predictionCoincidence = this.predictCoincidence(coincidence, this.previousCoincidence);
					if (this.predictionCoincidence.similar(coincidence)) {
						this.coincidenceCountIncrement(this.predictionCoincidence);
						this.addCoincidenceConcept(c, concept);
						this.addCoincidenceConcept(this.predictionCoincidence, concept);
					}
				}
			}
			this.setCoincidence(coincidence);
			this.coincidenceList.add(coincidence);
		}
		if (this.coincidenceList.size() > MEMORY) {
			this.purgeCoincidenceList();
		}
	}

	public void addCoincidenceConcept(Coincidence c, Concept concept) {
		if (concept != null) {
			List<Concept> conceptList = this.conceptListMap.get(c.toString());
			if (conceptList == null) {
				conceptList = new ArrayList<>();
			}
			conceptList.add(concept);
			this.conceptListMap.put(c.toString(), conceptList);
		}
	}

	/**
	 * 
	 * @param c
	 * @param w
	 * @return
	 */
	@JsonIgnore
	public void setCellArray(Coincidence c, Wavelength w) {
//		logger.info("setCellArray(" + c + ", " + w + ")");
		int size = this.n;//(this.n > c.list.size()) ? c.list.size() : this.n;
		for (int i = 0; i < size; i++) {
			switch (w) {
			case ROD_GRAY: {
				// Must be set because c value is already an average, no way to assume red,
				// green, or blue
				cellArray[i].red = c.list.get(i);
				cellArray[i].green = c.list.get(i);
				cellArray[i].blue = c.list.get(i);
				break;
			}
			case CONE_SHORT: {
				cellArray[i].red = c.list.get(i);
				break;
			}
			case CONE_MEDIUM: {
				cellArray[i].green = c.list.get(i);
				break;
			}
			case CONE_LONG: {
				cellArray[i].blue = c.list.get(i);
				break;
			}
			default: {
				cellArray[i].red = 0;
				cellArray[i].green = 0;
				cellArray[i].blue = 0;
				break;
			}
			}
		}
		// logger.info("getCoincidence("+Wavelength+") coincidence="+coincidence);
	}

	@JsonIgnore
	public void updatePoints() {
		Point point = new Point(center.x, center.y);
		xDimension[0] = point.x;
		yDimension[0] = point.y;
//		points[0] = point;
		for (int i = 1; i < this.n; i++) {
			double angle = findAngle((double) i / this.sides);
			point = findPoint(angle);
			xDimension[i] = point.x;
			yDimension[i] = point.y;
//			points[i] = point;
		}
	}

	@JsonIgnore
	public void initCells() {
		this.cellArray = new Cell[this.n];
		for (int i = 0; i < this.n; i++) {
			this.cellArray[i] = new Cell();
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

	public void coincidenceCountIncrement(Coincidence c) {
		Integer count = 0;
		count = this.coincidenceCountMap.get(c.toString());
		count = (count == null) ? 0 : count;
//		logger.info("coincidencCountIncrement(...) count="+count);
		this.coincidenceCountMap.put(c.toString(), count + 1);
	}

	/**
	 * Function uses a map of coincidence frequency to determine if it should be
	 * remove from the coincidenceList. A coincidence with a frequency greater than
	 * a threshold value are kept.
	 */
	@JsonIgnore
	public void purgeCoincidenceList() {
		logger.info("purgeCoincidencList()");
		Coincidence c = null;
		List<Coincidence> cList = new LinkedList<>();
		for (int i = 0; i < this.coincidenceList.size(); i++) {
			c = this.coincidenceList.get(i);
			Integer count = this.coincidenceCountMap.get(c.toString());
			if (count == null || count <= 1) {
				cList.add(c);
			}
		}
		for (Coincidence coincidence : cList) {
			this.conceptListMap.remove(coincidence);
		}
		this.coincidenceList.removeAll(cList);
	}

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
		if (aGivenB > THRESHOLD) {
//			logger.info("predictCoincidence(aCoincidence, bCoincidence) P(A|B)=" + aGivenB);
			Map<String, Double> aMap = (this.coincidenceConditionalMap.get(b) == null) ? new HashMap<String, Double>()
					: this.coincidenceConditionalMap.get(b);
			aMap.put(a, aGivenB);
			this.coincidenceConditionalMap.put(b, aMap);
		}
		Coincidence conditionalCoincidence = this.getConditionalCoincidence(aCoincidence);
		return (conditionalCoincidence != null)?conditionalCoincidence:aCoincidence;
	}

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

	public static <T> void printTree(Shape node, String appender) {
//		System.out.println(appender + node.getData() + ":" + node.coincidence);
		node.getChildren().forEach(each -> printTree(each, appender + appender));
	}
}
//@JsonIgnore
//public void purgeCorrectList() {
//	while (this.correctList.size() > 7) {
//		this.correctList.pop();
//	}
//}
//
//@JsonIgnore
//public double getCorrectPercentage() {
//	int oneCount = 0;
//	for (Integer i : this.correctList) {
//		oneCount += i;
//	}
////	logger.info("getCorrectPercentage() oneCount="+oneCount);
////	logger.info("getCorrectPercentage() this.correctList.size()="+this.correctList.size());
//	return (this.correctList.size() > 0) ? (double) oneCount / (double) this.correctList.size() : 0;
//}

//@JsonIgnore
//public Coincidence getConditionalCoincidence(Coincidence b) {
//	Coincidence coincidence = null;
//	if (b != null) {
//		Map<String, Double> aMap = this.coincidenceConditionalMap.get(b.toString());
//		if (aMap != null) {
//			double max = 0;
//			for (Map.Entry<String, Double> entry : aMap.entrySet()) {
//				if (entry.getValue() > max) {
//					max = entry.getValue();
//					coincidence = new Coincidence(entry.getKey());
//				}
//			}
//		}
//	}
//	return coincidence;
//}
//@JsonIgnore
//public int getTotal(Map<String, Integer> map) {
//	int sum = 0;
//	if (map != null) {
//		for (Integer i : map.values()) {
//			sum += i;
//		}
//	}
//	return sum;
//}
//@JsonIgnore
//public Rod[] rodArray;
//@JsonIgnore
//public Cone[] shortConeArray;
//@JsonIgnore
//public Cone[] mediumConeArray;
//@JsonIgnore
//public Cone[] longConeArray;
