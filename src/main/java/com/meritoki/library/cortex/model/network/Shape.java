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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.meritoki.library.cortex.model.cell.Cone;
import com.meritoki.library.cortex.model.cell.Rod;
import com.meritoki.library.cortex.model.unit.Coincidence;
import com.meritoki.library.cortex.model.unit.Concept;
import com.meritoki.library.cortex.model.unit.Point;

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
	public List<Point> pointList;
	@JsonProperty
	public double npoints;
	@JsonProperty
	public double[] xpoints = null;
	@JsonProperty
	public double[] ypoints = null;
	@JsonProperty
	public Coincidence coincidence;
	@JsonProperty
	public Coincidence previousCoincidence;
	@JsonProperty
	public Coincidence predictionCoincidence;
	@JsonProperty
	public Map<Type, Coincidence> typeCoincidenceMap = new HashMap<>();
	@JsonProperty
	public Map<Type, Coincidence> typePreviousCoincidenceMap = new HashMap<>();
	@JsonProperty
	public Map<Type, Coincidence> typePredictionCoincidenceMap = new HashMap<>();
	@JsonIgnore
	public Cone[] coneArray;
	@JsonIgnore
	public Rod[] rodArray;
	@JsonProperty
	protected List<Coincidence> coincidenceList = new LinkedList<>();
	@JsonProperty
	public Map<Type, List<Coincidence>> typeCoincidenceListMap = new HashMap<>();
	@JsonProperty
	public Map<String, Integer> coincidenceCountMap = new HashMap<>();
	@JsonProperty
	public Map<String, Integer> correctCountMap = new HashMap<>();
	@JsonProperty
	public Map<String, Integer> coincidenceUnionCountMap = new HashMap<>();
	@JsonProperty
	public Map<String, Map<String, Double>> coincidenceConditionalMap = new HashMap<>();
	@JsonProperty
	public Map<String, List<Concept>> coincidenceConceptListMap = new HashMap<>();
	@JsonProperty
	protected LinkedList<Integer> correctList = new LinkedList<>();
	@JsonIgnore
	public static final int MEMORY = 4096;

	public Shape() {
	}

	public Shape(int sides, int rotation, int x, int y, Point center, double radius) {
		super(x + "," + y);
//		logger.info("Shape("+sides+", "+rotation+", "+x+", "+y+", "+center+", "+radius+")");
		this.sides = sides;
		switch (this.sides) {
		case 4: {
			this.coincidence = new Coincidence(9);
			this.typeCoincidenceMap.put(Type.BRIGHTNESS, new Coincidence(9));
			this.typeCoincidenceMap.put(Type.RED, new Coincidence(9));
			this.typeCoincidenceMap.put(Type.GREEN, new Coincidence(9));
			this.typeCoincidenceMap.put(Type.BLUE, new Coincidence(9));
			this.typeCoincidenceListMap.put(Type.BRIGHTNESS, new ArrayList<>());
			this.typeCoincidenceListMap.put(Type.RED, new ArrayList<>());
			this.typeCoincidenceListMap.put(Type.GREEN, new ArrayList<>());
			this.typeCoincidenceListMap.put(Type.BLUE, new ArrayList<>());
			break;
		}
		case 6: {
			this.coincidence = new Coincidence(7);
			this.typeCoincidenceMap.put(Type.BRIGHTNESS, new Coincidence(7));
			this.typeCoincidenceMap.put(Type.RED, new Coincidence(7));
			this.typeCoincidenceMap.put(Type.GREEN, new Coincidence(7));
			this.typeCoincidenceMap.put(Type.BLUE, new Coincidence(7));
			this.typeCoincidenceListMap.put(Type.BRIGHTNESS, new ArrayList<>());
			this.typeCoincidenceListMap.put(Type.RED, new ArrayList<>());
			this.typeCoincidenceListMap.put(Type.GREEN, new ArrayList<>());
			this.typeCoincidenceListMap.put(Type.BLUE, new ArrayList<>());
			break;
		}
		}
		this.rotation = rotation;
		this.x = x;
		this.y = y;
		this.center = center;
		this.radius = radius;
		this.updatePoints();
		this.initCells();
	}

	public double[] getXPoints() {
		double[] array = new double[this.pointList.size() - 1];
		for (int i = 1; i < this.pointList.size(); i++) {
			Point point = this.pointList.get(i);
			array[i - 1] = point.x;
		}
		return array;
	}

	public double[] getYPoints() {
		double[] array = new double[this.pointList.size() - 1];
		for (int i = 1; i < this.pointList.size(); i++) {
			Point point = this.pointList.get(i);
			array[i - 1] = point.y;
		}
		return array;
	}

	public int getNPoints() {
		return this.pointList.size() - 1;
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
		return this.coincidenceConceptListMap.get(c.toString());
	}

	public Coincidence getCoincidence() {
		return this.coincidence;
	}

	public void setCoincidence(Coincidence coincidence) {
		this.coincidence = coincidence;
	}

	@JsonIgnore
	public void updatePoints() {
		this.pointList = new ArrayList<>();
		this.pointList.add(this.center);
		for (int i = 0; i < this.sides; i++) {
			double angle = findAngle((double) i / this.sides);
			Point point = findPoint(angle);
			this.pointList.add(point);
		}
	}

	@JsonIgnore
	public void initCells() {

		this.coneArray = new Cone[this.pointList.size()];
		this.rodArray = new Rod[this.pointList.size()];
		for (int i = 0; i < this.pointList.size(); i++) {
			coneArray[i] = new Cone();
			rodArray[i] = new Rod();
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

	@JsonIgnore
	public Coincidence getCoincidence(Type type) {
		// logger.info("getCoincidence("+type+")");
		Coincidence coincidence = new Coincidence();
		int value = 0;
		for (int i = 0; i < this.sides + 1; i++) {
			switch (type) {
			case BRIGHTNESS: {
				value = rodArray[i].brightness;
				break;
			}
			case RED: {
				value = coneArray[i].red;
				break;
			}
			case GREEN: {
				value = coneArray[i].green;
				break;
			}
			case BLUE: {
				value = coneArray[i].blue;
				break;
			}
			default: {
				value = 0;
				break;
			}
			}
			coincidence.addInteger(value);
		}
		// logger.info("getCoincidence("+type+") coincidence="+coincidence);
		return coincidence;
	}

	public Coincidence getCoincidence(Map<Type, Coincidence> coincidenceMap) {
		Coincidence coincidence = new Coincidence();
		for (Map.Entry<Type, Coincidence> entry : coincidenceMap.entrySet()) {
			Coincidence c = entry.getValue();
			coincidence.list.addAll(c.list);
		}
		return coincidence;
	}

	/**
	 * Function has a lot of responsibility. It matches a input coincidence by
	 * minimum and maximum similarity with a list of coincidences already input If a
	 * similarity is found
	 * 
	 * @param type
	 * @param coincidence
	 * @param concept
	 * @param flag        - Used to differentiate b/t propagate and feedback
	 */
	@JsonIgnore
	public void addCoincidence(Type type, Coincidence coincidence, Concept concept) { // , boolean flag) {
//		logger.info("addCoincidence("+type+", "+coincidence+", "+concept+")");
		Coincidence c = null;
		double max = 0;
		Coincidence inferredCoincidence = null;
		if (coincidence != null && coincidence.list.size() > 0) {
			List<Coincidence> coincidenceList = this.typeCoincidenceListMap.get(type);//this.coincidenceList;// this.typeCoincidenceListMap.get(type);//this.coincidenceList;//
			coincidenceList = (coincidenceList == null) ? new ArrayList<>() : coincidenceList;
			for (int i = 0; i < coincidenceList.size(); i++) {
				c = coincidenceList.get(i);
				if (concept == null) {
					c.setThreshold(0.99);
				} else {
					c.setThreshold(0.99);
				}
				if (c.similar(coincidence, max)) {
					max = c.quotient;
					inferredCoincidence = c;
				}
			}
			// Test Prediction Correct Before Overridding

			if (inferredCoincidence != null) {
				coincidence = inferredCoincidence;
//				if (coincidence != null) {

//				}
				if (this.typePredictionCoincidenceMap.get(type) != null) {
					coincidence.setThreshold(0.75);
					if (coincidence.similar(this.typePredictionCoincidenceMap.get(type), 0.75)) {
//						logger.info("addCoincidence(...) correct");
						this.correctList.add(1);
						Integer count = this.correctCountMap
								.get(this.typePredictionCoincidenceMap.get(type).toString());
						count = (count == null) ? 0 : count;
						if (count > 0) {
							count -= 1;
						}
						this.correctCountMap.put(this.typePredictionCoincidenceMap.get(type).toString(), count);
					} else {
						this.correctList.add(0);
						Integer count = this.correctCountMap
								.get(this.typePredictionCoincidenceMap.get(type).toString());
						count = (count == null) ? 0 : count;
						count += 1;
						this.correctCountMap.put(this.typePredictionCoincidenceMap.get(type).toString(), count);
					}
				}
				Integer count = this.coincidenceCountMap.get(coincidence.toString());
				count = (count == null) ? 0 : count;
				this.coincidenceCountMap.put(coincidence.toString(), count + 1);
				this.addCoincidenceUnionCountMap(coincidence, this.typePreviousCoincidenceMap.get(type));

			} else {
				coincidenceList.add(coincidence);
				this.typeCoincidenceListMap.put(type,coincidenceList);
			}
			if (concept != null) {
				List<Concept> conceptList = this.coincidenceConceptListMap.get(coincidence.toString());
				conceptList.add(concept);
				this.coincidenceConceptListMap.put(coincidence.toString(), conceptList);
			}
			this.typePredictionCoincidenceMap.put(type,
					this.predictCoincidence(coincidence, this.typePreviousCoincidenceMap.get(type)));
			this.typePreviousCoincidenceMap.put(type, this.typeCoincidenceMap.get(type));
			this.typeCoincidenceMap.put(type, coincidence);

		}
//		if (this.colorTypeCoincidenceListMap.get(type).size() > MEMORY) {// this.getFrequencyMax() + this.buffer) {
//			this.purgeCoincidenceList(type);
//		}
	}

	public void addCoincidenceUnionCountMap(Coincidence aCoincidence, Coincidence bCoincidence) {
		String a = (aCoincidence != null) ? aCoincidence.toString() : "[]";
		String b = (bCoincidence != null) ? bCoincidence.toString() : "[]";
		String ab = a + "," + b;
		Integer abCount = (this.coincidenceUnionCountMap.get(ab) == null) ? 0 : this.coincidenceUnionCountMap.get(ab);
		abCount += 1;
		this.coincidenceUnionCountMap.put(ab, abCount);
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
//		logger.info("predictCoincidence(aCoincidence, bCoincidence) aCount=" + aCount);
//		logger.info("predictCoincidence(aCoincidence, bCoincidence) bCount=" + bCount);
//		logger.info("predictCoincidence(aCoincidence, bCoincidence) abCount=" + abCount);
//		aCount += 1;
//		this.coincidenceFrequencyMap.put(a, aCount);
//		abCount += 1;
//		this.coincidenceUnionCountMap.put(ab, abCount);
		double countSum = (double) this.getMapIntegerSum(this.coincidenceCountMap);
		double unionCountSum = (double) this.getMapIntegerSum(this.coincidenceUnionCountMap);
//		logger.info("predictCoincidence(aCoincidence, bCoincidence) countSum=" + countSum);
//		logger.info("predictCoincidence(aCoincidence, bCoincidence) unionCountSum=" + unionCountSum);
		double aProbability = (countSum > 0) ? (double) aCount / countSum : 0;
		double bProbability = (countSum > 0) ? (double) bCount / countSum : 0;
		double abUnionProbability = (unionCountSum > 0) ? (double) (abCount) / unionCountSum : 0;
		Coincidence prediction;
		if (bCoincidence != null) {

//			double abIntersectCoefficient = (aCoincidence.getCG() > bCoincidence.getCG())
//					? (bCoincidence.getCG() / aCoincidence.getCG())
//					: (aCoincidence.getCG() / bCoincidence.getCG());// (sum > 0) ? (double) abCount / sum : 0;
//			logger.info("predictCoincidence(aCoincidence, bCoincidence) C(A AND B)=" + abIntersectCoefficient);
			double abIntersectProbability = abUnionProbability;//aProbability + bProbability - abUnionProbability;// (((double)(aCount+bCount))*abIntersectCoefficient)/countSum;
			// double abIntersectProbability = (aCoincidence.getCG() > bCoincidence.getCG())
//					? (((double) bCount)*2) / sum
//					: (((double) aCount)*2) / sum;
//			double abIntersectProbability = (aCoincidence.getCG() > bCoincidence.getCG())
//					? ((double) aCount + ((double) bCount * abIntersectCoefficient)) / sum
//					: ((double) bCount + ((double) aCount * abIntersectCoefficient)) / sum;
//		double aGivenB = (bProbability > 0) ? (double) abIntersectProbability / (double) bProbability : 0;
//			logger.info("predictCoincidence(aCoincidence, bCoincidence) P(A)=" + aProbability);
//			logger.info("predictCoincidence(aCoincidence, bCoincidence) P(B)=" + bProbability);
//			logger.info("predictCoincidence(aCoincidence, bCoincidence) P(A AND B)=" + abIntersectProbability);
			double bGivenA = (aProbability > 0) ? (double) abIntersectProbability / (double) aProbability : 0;
//			logger.info("predictCoincidence(aCoincidence, bCoincidence) P(B|A)=" + bGivenA);
			if (bGivenA > 0.75) {
				logger.info("predictCoincidence(aCoincidence, bCoincidence) P(B|A)=" + bGivenA);
				Map<String, Double> bMap = (this.coincidenceConditionalMap.get(b) == null)
						? new HashMap<String, Double>()
						: this.coincidenceConditionalMap.get(b);
				bMap.put(b, bGivenA);
				this.coincidenceConditionalMap.put(a, bMap);
			}
			prediction = this.getConditionalCoincidence(aCoincidence);
//		logger.info("predictCoincidence("+aCoincidence+", "+bCoincidence+") prediction="+prediction);
		} else {
			prediction = aCoincidence;
		}
		return prediction;
	}

	@JsonIgnore
	public Coincidence getConditionalCoincidence(Coincidence a) {
		Coincidence coincidence = null;
		if (a != null) {
			Map<String, Double> aMap = this.coincidenceConditionalMap.get(a.toString());
			if (aMap != null) {
				double max = 0;
				for (Map.Entry<String, Double> entry : aMap.entrySet()) {
					Coincidence b = new Coincidence(entry.getKey());
					int correct = (this.correctCountMap.get(b.toString()) != null)
							? this.correctCountMap.get(b.toString())
							: 0;
//					logger.info("getConditionalCoincidence(...) correct="+correct);
					double p = entry.getValue();
					p /= Math.pow(2, correct);
					if (p > max) {
						max = p;
						coincidence = new Coincidence(entry.getKey());
					}
				}
			}
		}
		return coincidence;
	}

	// this.conceptListMap.put(this.coincidence.toString(), conceptList);
	// this.coincidence = coincidence;
	// if (this.previousPrediction != null) {
	// if (this.previousPrediction.equals(this.coincidence)) {
	// correctList.add(1);
	// } else {
	// correctList.add(0);
	// }
	// }
	// this.previousPrediction = this.prediction;
	// this.prediction = this.predictCoincidence(this.coincidence,
	// this.previousCoincidence);
	// this.purgeCorrectList();
	// }
	// /**
	// * Function uses a map of coincidence frequency to determine if it should be
	// * remove from the coincidenceList. A coincidence with a frequency greater
	// than
	// * a threshold value are kept.
	// */
	// @JsonIgnore
	// public void purgeCoincidenceList(Type type) {
	// Coincidence c = null;
	// List<Coincidence> cList = new LinkedList<>();
	// for (int i = 0; i < this.typeCoincidenceListMap.get(type).size(); i++) {
	// c = this.typeCoincidenceListMap.get(type).get(i);
	// if (this.coincidenceCountMap.get(c.toString()) == null) {
	// cList.add(c);
	// }
	// }
	// for (Coincidence coincidence : cList) {
	// this.coincidenceConceptListMap.remove(coincidence);
	// }
	// this.typeCoincidenceListMap.get(type).removeAll(cList);
	// }

	// @JsonIgnore
	// public void purgeCorrectList() {
	// while (this.correctList.size() > 7) {
	// this.correctList.pop();
	// }
	// }
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
		// logger.info("getCorrectPercentage() oneCount="+oneCount);
		// logger.info("getCorrectPercentage()
		// this.correctList.size()="+this.correctList.size());
		return (this.correctList.size() > 0) ? (double) oneCount / (double) this.correctList.size() : 0;
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
	public int getMapIntegerSum(Map<String, Integer> map) {
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
//this.conceptListMap.put(this.coincidence.toString(), conceptList);
//this.coincidence = coincidence;
//if (this.previousPrediction != null) {
//if (this.previousPrediction.equals(this.coincidence)) {
//	correctList.add(1);
//} else {
//	correctList.add(0);
//}
//}
//this.previousPrediction = this.prediction;
//this.prediction = this.predictCoincidence(this.coincidence, this.previousCoincidence);
//this.purgeCorrectList();
//}
///**
// * Function uses a map of coincidence frequency to determine if it should be
// * remove from the coincidenceList. A coincidence with a frequency greater than
// * a threshold value are kept.
// */
//@JsonIgnore
//public void purgeCoincidenceList(Type type) {
//	Coincidence c = null;
//	List<Coincidence> cList = new LinkedList<>();
//	for (int i = 0; i < this.typeCoincidenceListMap.get(type).size(); i++) {
//		c = this.typeCoincidenceListMap.get(type).get(i);
//		if (this.coincidenceCountMap.get(c.toString()) == null) {
//			cList.add(c);
//		}
//	}
//	for (Coincidence coincidence : cList) {
//		this.coincidenceConceptListMap.remove(coincidence);
//	}
//	this.typeCoincidenceListMap.get(type).removeAll(cList);
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

//conceptList = this.coincidenceConceptListMap.get(inferredCoincidence.toString());
//this.coincidenceConceptListMap.put(inferredCoincidence.toString(), conceptList);
//if (flag) {
//	List<Concept> conceptList = null;
//	if (inferredCoincidence != null) {
//		count = this.coincidenceCountMap.get(inferredCoincidence.toString());
//		count = (count == null) ? 0 : count;
//		this.coincidenceCountMap.put(inferredCoincidence.toString(), count + 1);
////		conceptList = this.coincidenceConceptListMap.get(inferredCoincidence.toString());
////		this.coincidenceConceptListMap.put(inferredCoincidence.toString(), conceptList);
//	}
//	if (conceptList == null) {
//		conceptList = new ArrayList<>();
//	}
//	if (concept != null) {
//		conceptList.add(concept);
//	}
//	this.colorTypeCoincidenceMap.put(type, coincidence);
//	coincidenceList.add(coincidence);
//	this.colorTypeCoincidenceListMap.put(type, coincidenceList);
//} else {
//
//	this.colorTypeCoincidenceMap.put(type, coincidence);
//}
//this.points = new Point[sides + 1];
//this.npoints = sides;//+1;
//this.xpoints = new double[sides];
//this.ypoints = new double[sides];
///**
//* Function has a lot of responsibility. It matches a input coincidence by
//* minimum and maximum similarity with a list of coincidences already input If a
//* similarity is found
//* 
//* @param coincidence
//* @param concept
//* @param threshold
//*/
//@JsonIgnore
//public void addCoincidence(Coincidence coincidence, Concept concept, boolean flag) {
//	Coincidence c = null;
//	Integer count = 0;
//	double max = 0;
//	Coincidence inferredCoincidence = null;
//	if (coincidence != null && coincidence.list.size() > 0) {
//		for (int i = 0; i < this.coincidenceList.size(); i++) {
//			c = this.coincidenceList.get(i);
//			if (c.similar(coincidence, max)) {
//				max = c.quotient;
//				inferredCoincidence = c;
//			}
//		}
//		this.previousCoincidence = this.coincidence;
//		if (flag) {
//			List<Concept> conceptList = null;
//			if (inferredCoincidence != null) {
//				count = this.coincidenceCountMap.get(inferredCoincidence.toString());
//				count = (count == null) ? 0 : count;
//				this.coincidenceCountMap.put(inferredCoincidence.toString(), count + 1);
//				conceptList = this.coincidenceConceptListMap.get(inferredCoincidence.toString());
//				this.coincidenceConceptListMap.put(inferredCoincidence.toString(), conceptList);
//			}
//			if (conceptList == null) {
//				conceptList = new ArrayList<>();
//			}
//			if (concept != null) {
//				conceptList.add(concept);
//			}
//			this.coincidence = coincidence;
//			this.coincidenceConceptListMap.put(this.coincidence.toString(), conceptList);
//			this.coincidenceList.add(this.coincidence);
//		} else {
//			this.coincidence = coincidence;
//		}
////		if (this.previousPrediction != null) {
////			if (this.previousPrediction.equals(this.coincidence)) {
////				correctList.add(1);
////			} else {
////				correctList.add(0);
////			}
////		}
////		this.previousPrediction = this.prediction;
////		this.prediction = this.predictCoincidence(this.coincidence, this.previousCoincidence);
////		this.purgeCorrectList();
//	}
//	if (this.coincidenceList.size() > MEMORY) {// this.getFrequencyMax() + this.buffer) {
//		this.purgeCoincidenceList();
//	}
//}
//if(concept == null) {
//c.setThreshold(0.95);
//} else {
//c.setThreshold(0.99);
//}
//this.shortConeArray = new Cone[this.pointList.size()];
//this.mediumConeArray = new Cone[this.pointList.size()];
//this.longConeArray = new Cone[this.pointList.size()];
//shortConeArray[i] = new Cone(Wavelength.SHORT);
//mediumConeArray[i] = new Cone(Wavelength.MEDIUM);
//longConeArray[i] = new Cone(Wavelength.LONG);
//@JsonIgnore
//public void updatePoints() {
////	Point point = new Point(center.x, center.y);
////	xpoints[0] = point.x;
////	ypoints[0] = point.y;
////	points[0] = point;
//	for (int i = 0; i < this.sides; i++) {
//		double angle = findAngle((double) i / this.sides);
//		Point point = findPoint(angle);
//		xpoints[i] = point.x;
//		ypoints[i] = point.y;
////		points[i] = point;
//	}
//}
//@JsonIgnore
//public Cone[] shortConeArray;
//@JsonIgnore
//public Cone[] mediumConeArray;
//@JsonIgnore
//public Cone[] longConeArray;
//this.brightnessCoincidence = new Coincidence(9);
//this.redCoincidence = new Coincidence(9);
//this.greenCoincidence = new Coincidence(9);
//this.blueCoincidence = new Coincidence(9);
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
//this.brightnessCoincidence = new Coincidence(7);
//this.redCoincidence = new Coincidence(7);
//this.greenCoincidence = new Coincidence(7);
//this.blueCoincidence = new Coincidence(7);
//(shortConeArray[i].red + mediumConeArray[i].green +
// longConeArray[i].blue) / 3;
//@JsonIgnore
//public int red;
//@JsonIgnore
//public int green;
//@JsonIgnore 
//public int blue;
//@JsonIgnore
//public Coincidence brightnessCoincidence;
//@JsonIgnore
//public Coincidence redCoincidence;
//@JsonIgnore
//public Coincidence greenCoincidence;
//@JsonIgnore
//public Coincidence blueCoincidence;
//@JsonIgnore
//public Coincidence defaultCoincidence = null;
