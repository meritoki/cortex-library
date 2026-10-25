package com.meritoki.library.cortex.model;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.meritoki.library.controller.node.NodeController;

public class Belief {

	@JsonProperty
	public String uuid;
	@JsonProperty
	public Coincidence coincidence;
	@JsonProperty
	public List<Concept> conceptList = new ArrayList<>();
	@JsonProperty
	public List<Point> pointList = new ArrayList<>();
	@JsonProperty
	public Point origin;
	@JsonProperty
	public Point global;
	@JsonProperty
	public Point relative;
	@JsonProperty
	public double radius;
	@JsonIgnore
	public BufferedImage bufferedImage;
	@JsonIgnore
	public File file;
	@JsonProperty
	public String filePath;
	@JsonProperty
	public String fileName;
	@JsonProperty
	public Date date;
	@JsonProperty
	public String retinaUUID;

	public Belief() {
		this.uuid = UUID.randomUUID().toString();
	}
	
	public boolean contains(Concept concept) {
		return this.conceptList.contains(concept);
	}
	
	@JsonIgnore
	@Override
	public boolean equals(Object belief) {
		boolean flag = false;
		if (this.uuid.equals(((Belief)belief).uuid)) {
			flag = true;
		}
		return flag;
	}
	
	//Do I need to scale the origin?
	public void normalize(double scale) {
		this.origin.scale(1/scale);
		for(Point point:pointList) {
			point.x /= scale;
			point.y /= scale;
			point.x -= origin.x;
			point.y -= origin.y;
		}
	}

	/**
	 * Normalize is a complicated function that produced beliefs that 
	 * can be drawn relative to origin, where origin is specified
	 * as the cortex origin.
	 * @param scale
	 * @param origin
	 * @param previous
	 */
	public void setRelative(double scale, Point origin, Point previous) {
//		System.out.println("setRelative("+scale+", "+origin+", "+previous+")");
		this.relative = new Point(this.origin);
//		this.relative.scale(1 / scale);
		this.relative.x -= origin.x/scale;
		this.relative.y -= origin.y/scale;
//		System.out.println("normalize("+scale+", "+origin+", "+previous+") this.origin="+this.origin);		
		if (!previous.center) {
//			System.out.println("normalize(...) !previous.center");
			// Delta is a movement between two points.
			// If "same" center, then delta is zero.
			Point delta = origin.subtract(previous);
			this.relative.x += delta.x/scale;
			this.relative.y += delta.y/scale;
		}
		System.out.println("setRelative("+scale+", "+origin+", "+previous+") this.relative="+relative);
	}
	
	/**
	 * Scale is used to bring all points to one domain where all points are in the same scale
	 * 
	 * @param scale
	 */
	public void scale(double scale) {
		this.origin.scale(1 / scale);
		for (Point point : pointList) {
			point.x /= scale;
			point.y /= scale;
		}
	}
	
	//This transformation makes the belief relative to origin.
	//
	public void setGlobal(Point origin, double scale) {
//		System.out.println("setGlobal("+scale+", "+origin+")");
		this.global = new Point(this.origin);
		this.global.x -= origin.x/scale;
		this.global.y -= origin.y/scale;
		System.out.println("setGlobal("+scale+", "+origin+") this.global="+this.global);
	}

	@JsonIgnore
	public double getRelativeRadius() {
		return this.round(Math.sqrt(Math.pow(relative.x, 2) + Math.pow(relative.y, 2)));
	}

	@JsonIgnore
	public double getRadius() {
		double max = 0;
		for (Point point : this.pointList) {
			double distance = Point.getDistance(new Point(0,0), point);
			if (distance > max) {
				max = distance;
			}
		}
		return max;
	}
	
	@JsonIgnore
	public List<Point> getGlobalPointList() {
		List<Point> pointList = new ArrayList<>();
		for(Point point: this.pointList) {
			Point p = new Point(point);
			p.x += this.global.x;
			p.y += this.global.y;
			p.round();
			pointList.add(p);
		}
		return pointList;
	}
	
	@JsonIgnore
	public List<Point> getRelativePointList() {
		List<Point> pointList = new ArrayList<>();
		for(Point point: this.pointList) {
			Point p = new Point(point);
			p.x += this.relative.x;
			p.y += this.relative.y;
			pointList.add(p);
		}
		return pointList;
	}
	
	
	@JsonIgnore
	public List<Point> getRadiusPointList(double radius, double scale) {
		System.out.println("getRadiusPointList("+radius+")");
		List<Point> pointList = new ArrayList<>();
		for(Point point: this.pointList) {
			double distance = Point.getDistance(new Point(0,0), point)*scale;
			distance = this.round(distance);
//			System.out.println("distance="+distance);
			if(distance == radius) {
				pointList.add(point);
			}
		}
		return pointList;
	}
	
	public double round(double value) {
		DecimalFormat decimalFormat = new DecimalFormat("#.##");
		return Double.parseDouble(decimalFormat.format(value));
	}


	@JsonIgnore
	public BufferedImage getBufferedImage() {
		if (this.bufferedImage == null) {
			System.out.println("getBufferedImage() filePath=" + filePath + " fileName=" + fileName);
			this.file = new File(filePath + NodeController.getSeperator() + fileName);
			if (this.file.exists()) {
				this.bufferedImage = NodeController.openBufferedImage(this.file);
			}
		}
		return this.bufferedImage;
	}

	/**
	 * Map is xyz -> object
	 * 
	 * @param map
	 * @param conceptList
	 * @return
	 */
	@JsonIgnore
	public void setConceptList(Map<String, String> map, List<Concept> conceptList) {
//		System.out.println("setConceptList("+map+", "+conceptList+")");
		conceptList = new ArrayList<>(conceptList);
		Set<String> keySet = map.keySet();
		for (String key : keySet) {
			String value = map.get(key);
//			System.out.println("setConceptList("+map+", "+conceptList+") key="+key);
//			System.out.println("setConceptList("+map+", "+conceptList+") value="+value);			
			for (Concept c : conceptList) {
//				System.out.println("setConceptList("+map+", "+conceptList+") c="+c);	
				if (key.equals(c.toString())) {
					c.value = value;
				}
			}

//			for (Concept k : keyList) {
//				if (!conceptList.contains(k)) {// order does not matter;
//					contains = false;
//				}
//			}
//			if (contains) {
//				if (!conceptList.contains(concept)) {
//					conceptList.add(0, concept);
//				}
//				for (Concept k : keyList) {
//					conceptList.remove(k);
//				}
//			}
		}

		//
		Map<String, Integer> conceptCountMap = new HashMap<>();
		conceptCountMap = new HashMap<>();
		if (conceptList != null) {
//			System.out.println("getConceptList() conceptList=" + conceptList);
			for (Concept c : conceptList) {
				Integer count = conceptCountMap.get(c.toString());
				count = (count != null) ? count : 0;
				conceptCountMap.put(c.toString(), count + 1);
			}
		}
		Integer total = this.getTotal(conceptCountMap);
		String value;
		Integer dividend;
		Double quotient;
		Concept concept = null;
		List<Concept> cList = new ArrayList<>();
		for (Map.Entry<String, Integer> entry : conceptCountMap.entrySet()) {
			value = entry.getKey();
			dividend = entry.getValue();
			quotient = (total > 0) ? (double) dividend / (double) total : 0;
			concept = new Concept(value);
			concept.rank = quotient;
			cList.add(concept);
		}
		Collections.sort(cList, new ConceptComparator());
		Collections.reverse(cList);
		this.conceptList = cList;
//		System.out.println("getConceptList() cList=" + cList);
	}

//	@JsonIgnore
//	public List<Concept> setConceptList(Map<String, String> map, List<Concept> conceptList) {
//		System.out.println("setConceptList("+map+", "+conceptList+")");
//		conceptList = new ArrayList<>(conceptList);
//		Set<String> keySet = map.keySet();
//		for (String key : keySet) {
//			String concept = map.get(key);
//			boolean contains = true;
//			for (Concept k : keyList) {
//				if (!conceptList.contains(k)) {// order does not matter;
//					contains = false;
//				}
//			}
//			if (contains) {
//				if (!conceptList.contains(concept)) {
//					conceptList.add(0, concept);
//				}
//				for (Concept k : keyList) {
//					conceptList.remove(k);
//				}
//			}
//		}
//
//		this.conceptCountMap = new HashMap<>();
//		if (conceptList != null) {
//			System.out.println("getConceptList() conceptList=" + conceptList);
//			for (Concept c : conceptList) {
//				Integer count = this.conceptCountMap.get(c.toString());
//				count = (count != null) ? count : 0;
//				this.conceptCountMap.put(c.toString(), count + 1);
//			}
//		}
//		Integer total = this.getTotal(this.conceptCountMap);
//		String value;
//		Integer dividend;
//		Double quotient;
//		Concept concept = null;
//		List<Concept> cList = new ArrayList<>();
//		for (Map.Entry<String, Integer> entry : this.conceptCountMap.entrySet()) {
//			value = entry.getKey();
//			dividend = entry.getValue();
//			quotient = (total > 0) ? (double) dividend / (double) total : 0;
//			concept = new Concept(value);
//			concept.rank = quotient;
//			cList.add(concept);
//		}
//		this.conceptList = cList;
//		System.out.println("getConceptList() cList=" + cList);
//		return cList;
//	}

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

	public String toString() {
		return this.origin + " " + this.uuid;// + " pointList=" + this.pointList;
	}
}

//System.out.println("normalize("+scale+", "+origin+", "+previous+") this.origin="+this.origin);		
//if (!previous.center) {
////	System.out.println("normalize(...) !previous.center");
//	// Delta is a movement between two points.
//	// If "same" center, then delta is zero.
//	Point delta = origin.subtract(previous);
//	this.origin.x += delta.x/scale;
//	this.origin.y += delta.y/scale;
//}
//origin.subtract(this.origin);
//List<Point> pointList = this.pointList;
//for (Point point : pointList) {
//	// Scale divide makes points the same size in a domain.
//	point.x /= scale;
//	point.y /= scale;
//	// Without this code, Points appear where they are drawn
//	// With this code, points appear at root 0,0.
//
////With this code the belief pointList is completly altered these transforms need to be applied to the belief center;
//
//	point.x -= origin.x/scale;
//	point.y -= origin.y/scale;
////	if (!previous.center) {
////		// Delta is a movement between two points.
////		// If "same" center, then delta is zero.
////		Point delta = origin.subtract(previous);
////		point.x += delta.x/scale;
////		point.y += delta.y/scale;
////
////	}
//}
