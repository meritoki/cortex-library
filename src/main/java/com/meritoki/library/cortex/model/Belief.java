package com.meritoki.library.cortex.model;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.meritoki.library.cortex.model.network.shape.Shape;

public class Belief {

	public String uuid;
	@JsonProperty
	public Coincidence coincidence;
	@JsonProperty
	public List<Concept> conceptList = new ArrayList<>();
	@JsonProperty
	public List<Point> pointList = new ArrayList<>();
	@JsonProperty
	public double x;
	@JsonProperty
	public double y;
	@JsonIgnore
	public BufferedImage bufferedImage;
	@JsonProperty
	public Map<List<Concept>, Concept> map = new HashMap<>();
	@JsonIgnore
	public Map<String, Integer> conceptCountMap = new HashMap<>();

	public Belief() {
		this.uuid = UUID.randomUUID().toString();
	}

	public List<Concept> getConceptList() {
		System.out.println("getConceptList() this.conceptList="+this.conceptList);
		List<Concept> conceptList = new ArrayList<>(this.conceptList);
		Set<List<Concept>> key = map.keySet();
		for (List<Concept> keyList : key) {
			Concept concept = map.get(keyList);
			boolean contains = true;
			for (Concept k : keyList) {
				if (!conceptList.contains(k)) {// order does not matter;
					contains = false;
				}
			}
			if (contains) {
				if (!conceptList.contains(concept)) {
					conceptList.add(0, concept);
				}
				for (Concept k : keyList) {
					conceptList.remove(k);
				}
			}
		}

		this.conceptCountMap = new HashMap<>();
		if (conceptList != null) {
			System.out.println("getConceptList() conceptList="+conceptList);
			for (Concept c : conceptList) {
				Integer count = this.conceptCountMap.get(c.toString());
				count = (count != null) ? count : 0;
				this.conceptCountMap.put(c.toString(), count + 1);
			}
		}
		Integer total = this.getTotal(this.conceptCountMap);
		String value;
		Integer dividend;
		Double quotient;
		Concept concept = null;
		List<Concept> cList = new ArrayList<>();
		for (Map.Entry<String, Integer> entry : this.conceptCountMap.entrySet()) {
			value = entry.getKey();
			dividend = entry.getValue();
			quotient = (total > 0) ? (double) dividend / (double) total : 0;
			concept = new Concept(value);
			concept.rank = quotient;
			cList.add(concept);
		}
		this.conceptList = cList;
		System.out.println("getConceptList() cList="+cList);
		return cList;
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

	public String toString() {
		return this.x + " " + this.y + " " + this.uuid;// + " pointList=" + this.pointList;
	}
}
