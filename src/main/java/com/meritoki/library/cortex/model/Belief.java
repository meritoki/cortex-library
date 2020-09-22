package com.meritoki.library.cortex.model;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.meritoki.library.controller.node.NodeController;
import com.meritoki.library.cortex.model.network.shape.Shape;

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
	public Point center;
	
	public double radius;
//	@JsonProperty
//	public double x;
//	@JsonProperty
//	public double y;
	@JsonIgnore
	public BufferedImage bufferedImage;
	@JsonIgnore
	public File file;
	@JsonProperty
	public String filePath;
	@JsonProperty
	public String fileName;


	public Belief() {
		this.uuid = UUID.randomUUID().toString();
	}
	
	public void containsPoint(Point point) {
		
	}
	
	public double getRadius() {
		double max = 0;
		for(Point point:this.pointList) {
			double distance = Point.getDistance(this.center,point);
			if(distance > max) {
				max = distance;
			}
		}
		return max;
	}
	
	public BufferedImage getBufferedImage() {
		if (this.bufferedImage == null) {
			System.out.println("getBufferedImage() filePath="+filePath+" fileName="+fileName);
			this.file = new File(filePath + NodeController.getSeperator() + fileName);
			if (this.file.exists()) {
				this.bufferedImage = NodeController.openBufferedImage(this.file);
			}
		}
		return this.bufferedImage;
	}
	
	
	/**
	 * Map is xyz -> object
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
			for(Concept c: conceptList) {
//				System.out.println("setConceptList("+map+", "+conceptList+") c="+c);	
				if(key.equals(c.toString())) {
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
		return this.center + " " + this.uuid;// + " pointList=" + this.pointList;
	}
}
