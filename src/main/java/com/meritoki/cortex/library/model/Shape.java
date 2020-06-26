package com.meritoki.cortex.library.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import com.meritoki.cortex.library.model.hexagon.Hexagonal;

public class Shape extends Node<Object> {

	private static Logger logger = LogManager.getLogger(Shape.class.getName());
	@JsonProperty
	private int x = 0;
	@JsonProperty
	private int y = 0;
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
	@JsonIgnore
	public Coincidence brightnessCoincidence;
	@JsonIgnore
	public Coincidence redCoincidence;
	@JsonIgnore
	public Coincidence greenCoincidence;
	@JsonIgnore
	public Coincidence blueCoincidence;
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
	protected Map<String, List<Concept>> conceptListMap = new HashMap<>();
	@JsonProperty
	protected LinkedList<Integer> correctList = new LinkedList<>();
	@JsonIgnore
	public static final int LIMIT = 4096;
	
	public Shape() {
		
	}

	public Shape(int sides, int rotation, int x, int y, Point center, double radius) {
		super(x + "," + y);
		this.sides = sides;
		switch(this.sides) {
		case 4: {
			this.coincidence = new Coincidence(9);
			this.brightnessCoincidence = new Coincidence(9);
			this.redCoincidence = new Coincidence(9);
			this.greenCoincidence = new Coincidence(9);
			this.blueCoincidence = new Coincidence(9);
			break;
		}
		case 6: {
			this.coincidence = new Coincidence(7);
			this.brightnessCoincidence = new Coincidence(7);
			this.redCoincidence = new Coincidence(7);
			this.greenCoincidence = new Coincidence(7);
			this.blueCoincidence = new Coincidence(7);
			break;
		}
		}
		this.rotation = rotation;
		this.x = x;
		this.y = y;
		this.center = center;
		this.radius = radius;
		this.points = new Point[sides];
		this.npoints = sides;
		this.xpoints = new double[sides];
		this.ypoints = new double[sides];
		this.shortConeArray = new Cone[sides];
		this.mediumConeArray = new Cone[sides];
		this.longConeArray = new Cone[sides];
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

	public Coincidence getCoincidence() {
		return this.coincidence;
	}

	public void setCoincidence(Coincidence coincidence) {
		this.coincidence = coincidence;
	}

	@JsonIgnore
	protected void updatePoints() {
		for (int i = 0; i < this.sides; i++) {
			double angle = findAngle((double) i / this.sides);
			Point point = findPoint(angle);
			xpoints[i] = point.x;
			ypoints[i] = point.y;
			points[i] = point;
		}
	}

	@JsonIgnore
	public void initCells() {
		for (int i = 0; i < this.sides; i++) {
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
		Coincidence buffer = null;
		if (coincidence.list.size() > 0) {
			for (int i = 0; i < this.coincidenceList.size(); i++) {
				c = this.coincidenceList.get(i);
				if (c.similar(coincidence, max)) {
					max = c.quotient;
					buffer = c;
				}
			}
			if (flag && buffer != null) {
				count = this.coincidenceCountMap.get(buffer.toString());
				count = (count == null) ? 0 : count;
				this.coincidenceCountMap.put(buffer.toString(), count + 1);
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
				this.coincidence = coincidence;
				this.coincidenceList.add(this.coincidence);
			}
		}
		if (this.coincidenceList.size() > LIMIT) {// this.getFrequencyMax() + this.buffer) {
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

	@JsonIgnore
	public void purgeCorrectList() {
		while (this.correctList.size() > 7) {
			this.correctList.pop();
		}
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
	public Coincidence getCoincidence(int type) {
//		logger.info("getCoincidence("+type+")");
		Coincidence coincidence = new Coincidence();
		int value = 0;
		for (int i = 0; i < this.sides; i++) {
			switch (type) {
			case Hexagonal.BRIGHTNESS: {
				value = (shortConeArray[i].blue + mediumConeArray[i].green + longConeArray[i].red) / 3;
				break;
			}
			case Hexagonal.RED: {
				value = longConeArray[i].red;
				break;
			}
			case Hexagonal.GREEN: {
				value = mediumConeArray[i].green;
				break;
			}
			case Hexagonal.BLUE: {
				value = shortConeArray[i].blue;
				break;
			}
			default: {
				value = 0;
				break;
			}
			}
			coincidence.addInteger(value);
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
}
