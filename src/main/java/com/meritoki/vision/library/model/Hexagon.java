package com.meritoki.vision.library.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

public class Hexagon extends Node<Object> {

	@JsonIgnore
	private static Logger logger = LogManager.getLogger(Hexagon.class.getName());

	@JsonIgnore
	private static double Gamma = 0.80;
	@JsonIgnore
	private static double IntensityMax = 255;

	@JsonIgnore
	public List<Cell> cellList = new ArrayList<>();
	@JsonProperty
	private int x = 0;
	@JsonProperty
	private int y = 0;
	@JsonProperty
	private Point[] points = new Point[SIDES];
	@JsonProperty
	public Point center = new Point(0, 0);
	@JsonProperty
	public int radius;
	@JsonIgnore
	private int rotation = 90;
	@JsonProperty
	public double npoints;
	@JsonProperty
	public double[] xpoints = null;
	@JsonProperty
	public double[] ypoints = null;
	@JsonProperty
	public Coincidence previousPrediction = null;
	@JsonProperty
	public Coincidence prediction = null;
	@JsonProperty
	public Coincidence previousCoincidence = null;
	@JsonProperty
	protected List<Coincidence> coincidenceList = new LinkedList<>();
	@JsonProperty
	public Map<String, Integer> coincidenceCountMap = new HashMap<>();
	@JsonProperty
	public Map<String, Integer> coincidenceUnionCountMap = new HashMap<>();
	@JsonProperty
	public Map<String, Map<String, Double>> coincidenceConditionalMap = new HashMap<>();
	@JsonProperty
	protected Map<String, List<Concept>> conceptListMap = new HashMap<>();
	@JsonProperty
	protected LinkedList<Integer> correctList = new LinkedList<>();
	@JsonProperty
	private double threshold = 0.90;


	@JsonIgnore
	public int buffer = 32;

	public Hexagon() {
		super("");
	}

	public Hexagon(Hexagon hexagon) {
		super(hexagon.getX() + "," + hexagon.getY());
		this.x = hexagon.getX();
		this.y = hexagon.getY();
		this.npoints = hexagon.npoints;
		this.xpoints = hexagon.xpoints;
		this.ypoints = hexagon.ypoints;
		this.center = hexagon.center;
		this.radius = hexagon.radius;
		updatePoints();
		initCells();
	}

	public Hexagon(int x, int y, Point center, int radius) {
		super(x + "," + y);
		this.x = x;
		this.y = y;
		this.npoints = SIDES;
		this.xpoints = new double[SIDES];
		this.ypoints = new double[SIDES];
		this.center = center;
		this.radius = radius;
		updatePoints();
		initCells();
	}

	@Override
	public boolean equals(Object o) {
		boolean flag = false;
		if (o instanceof Hexagon) {
			Hexagon hexagon = (Hexagon) o;
			flag = hexagon.getX() == this.getX() && hexagon.getY() == this.getY();
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
	public int getRadius() {
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
	protected void updatePoints() {
		for (int i = 0; i < SIDES; i++) {
			double angle = findAngle((double) i / SIDES);
			Point point = findPoint(angle);
			xpoints[i] = point.x;
			ypoints[i] = point.y;
			points[i] = point;
		}
	}

	@JsonIgnore
	public void initCells() {
		for (int i = 0; i < SIDES; i++) {
			shortConeArray[i] = new Cone(Wavelength.SHORT);
			mediumConeArray[i] = new Cone(Wavelength.MEDIUM);
			longConeArray[i] = new Cone(Wavelength.LONG);
		}
	}
	
//	public void initCoincidence() {
//		this.brightnessCoincidence = this.getCoincidence(Network.BRIGHTNESS);
//		this.redCoincidence = this.getCoincidence(Network.RED);
//		this.greenCoincidence = this.getCoincidence(Network.GREEN);
//		this.blueCoincidence = this.getCoincidence(Network.BLUE);
//	}
	
	public Coincidence getCoincidence() {
		return this.coincidence;
	}

//	@JsonIgnore
//	public Coincidence getCoincidence(int type) {
//		logger.info("getCoincidence("+type+")");
//		Coincidence coincidence = new Coincidence();
//		int value = 0;
//		for (int i = 0; i < SIDES; i++) {
//			switch(type) {
//			case Network.BRIGHTNESS: {
//				value = (shortConeArray[i].blue + mediumConeArray[i].green + longConeArray[i].red) / 3;
//				break;
//			}
//			case Network.RED: {
//				value = longConeArray[i].red;
//				break;
//			}
//			case Network.GREEN: {
//				value = mediumConeArray[i].green;
//				break;
//			}
//			case Network.BLUE: {
//				value = shortConeArray[i].blue;
//				break;
//			}
//			}
//			coincidence.addInteger(value);
//		}
//		return coincidence;
//	}
	
	public void setCoincidence(Coincidence coincidence) {
		this.coincidence = coincidence;
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
	public void addCoincidence(Coincidence coincidence, Concept concept) {
//		logger.info(this.getData());
		Coincidence c = null;
		Integer count = 0;
		double max = 0;
		Coincidence buffer = null;
		if (coincidence.list.size() > 0) {
			for (int i = 0; i < this.coincidenceList.size(); i++) {
				c = this.coincidenceList.get(i);
				if (c.similar(coincidence, max)) {
					max = c.quotient;
					buffer = c;
				}
			}
			if (buffer != null) {
				count = this.coincidenceCountMap.get(buffer.toString());
				count = (count == null) ? 0 : count;
				this.coincidenceCountMap.put(buffer.toString(), count + 1);
				this.previousCoincidence = this.coincidence;
				this.coincidence = buffer;
				List<Concept> conceptList = this.conceptListMap.get(this.coincidence.toString());
				if (conceptList == null) {
					conceptList = new ArrayList<>();
				}
				if (concept != null) {
					conceptList.add(concept);
					this.conceptListMap.put(this.coincidence.toString(), conceptList);
				}
				// 20191216 attempting to allow a new value in the map that could be better than
				// the matched coincidence in the future.
				this.coincidenceList.add(coincidence);
			} else {
				this.previousCoincidence = this.coincidence;
				this.coincidence = coincidence;
				this.coincidenceList.add(this.coincidence);
			}

			if (this.previousPrediction != null) {
				if (this.previousPrediction.equals(this.coincidence)) {
					correctList.add(1);
				} else {
					correctList.add(0);
				}
			}
			this.previousPrediction = this.prediction;
			this.prediction = this.predictCoincidence(this.coincidence, this.previousCoincidence);
			this.purgeCorrectList();
		}
		if (this.coincidenceList.size() > this.buffer) {//this.getFrequencyMax() + this.buffer) {
			this.purgeCoincidenceList();
		}
	}

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
		return (this.correctList.size() > 0) ? (double)oneCount / (double)this.correctList.size() : 0;
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
		Integer abCount = (this.coincidenceUnionCountMap.get(ab) == null) ? 0
				: this.coincidenceUnionCountMap.get(ab);
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
		if (aGivenB > this.threshold) {
//			logger.info("predictCoincidence(aCoincidence, bCoincidence) P(A|B)=" + aGivenB);
			Map<String, Double> aMap = (this.coincidenceConditionalMap.get(b) == null) ? new HashMap<String, Double>()
					: this.coincidenceConditionalMap.get(b);
			aMap.put(a, aGivenB);
			this.coincidenceConditionalMap.put(b, aMap);
		}

		return this.getConditionalCoincidence(aCoincidence);
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
	public double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}

	@JsonIgnore
	public int[] doubleToIntArray(double[] array) {
		int[] intArray = new int[array.length];
		for (int i = 0; i < array.length; i++) {
			intArray[i] = (int) array[i];
		}
		return intArray;
	}

	/**
	 * Function converts wavelength to RGB value
	 */
	@JsonIgnore
	public static int[] waveLengthToRGB(double Wavelength) {
		double factor;
		double Red, Green, Blue;

		if ((Wavelength >= 380) && (Wavelength < 440)) {
			Red = -(Wavelength - 440) / (440 - 380);
			Green = 0.0;
			Blue = 1.0;
		} else if ((Wavelength >= 440) && (Wavelength < 490)) {
			Red = 0.0;
			Green = (Wavelength - 440) / (490 - 440);
			Blue = 1.0;
		} else if ((Wavelength >= 490) && (Wavelength < 510)) {
			Red = 0.0;
			Green = 1.0;
			Blue = -(Wavelength - 510) / (510 - 490);
		} else if ((Wavelength >= 510) && (Wavelength < 580)) {
			Red = (Wavelength - 510) / (580 - 510);
			Green = 1.0;
			Blue = 0.0;
		} else if ((Wavelength >= 580) && (Wavelength < 645)) {
			Red = 1.0;
			Green = -(Wavelength - 645) / (645 - 580);
			Blue = 0.0;
		} else if ((Wavelength >= 645) && (Wavelength < 781)) {
			Red = 1.0;
			Green = 0.0;
			Blue = 0.0;
		} else {
			Red = 0.0;
			Green = 0.0;
			Blue = 0.0;
		}
		;

		// Let the intensity fall off near the vision limits

		if ((Wavelength >= 380) && (Wavelength < 420)) {
			factor = 0.3 + 0.7 * (Wavelength - 380) / (420 - 380);
		} else if ((Wavelength >= 420) && (Wavelength < 701)) {
			factor = 1.0;
		} else if ((Wavelength >= 701) && (Wavelength < 781)) {
			factor = 0.3 + 0.7 * (780 - Wavelength) / (780 - 700);
		} else {
			factor = 0.0;
		}
		;

		int[] rgb = new int[3];

		// Don't want 0^x = 1 for x <> 0
		rgb[0] = Red == 0.0 ? 0 : (int) Math.round(IntensityMax * Math.pow(Red * factor, Gamma));
		rgb[1] = Green == 0.0 ? 0 : (int) Math.round(IntensityMax * Math.pow(Green * factor, Gamma));
		rgb[2] = Blue == 0.0 ? 0 : (int) Math.round(IntensityMax * Math.pow(Blue * factor, Gamma));

		return rgb;
	}

	@JsonIgnore
	public String toString() {
		return this.getX() + "," + this.getY();// (String)this.getData();//
	}
}