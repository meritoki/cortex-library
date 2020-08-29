package com.meritoki.library.cortex.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * In a level, shapes are always referenced by their relative coordinates, i.e. 0,0
 * @author osvaldo.rodriguez
 *
 */
public class Level {
	
	@JsonIgnore
	private static Logger logger = LogManager.getLogger(Level.class.getName());
	@JsonIgnore
	public Map<String, Shape> shapeMap = new HashMap<>();
	@JsonIgnore
	public Map<String, Integer> conceptCountMap = new HashMap<>();
	
	public Level() {
	}

	public Level(Map<String, Shape> shapeMap) {
		this.shapeMap = shapeMap;
	}

	@JsonIgnore
	public void addShape(Shape shape) {
		this.shapeMap.put(shape.toString(), shape);
	}

	@JsonIgnore
	public List<Shape> getShapeList() {
		return this.getShapeList(this.shapeMap);
	}

	@JsonIgnore
	public Map<String, Shape> getShapeMap() {
		return this.shapeMap;
	}

	@JsonIgnore
	public LinkedList<Shape> getShapeList(Map<String, Shape> shapeMap) {
		LinkedList<Shape> shapeList = new LinkedList<Shape>();
		for (Map.Entry<String, Shape> entry : shapeMap.entrySet()) {
			shapeList.add((Shape) entry.getValue());
		}
		return shapeList;
	}
	
	@JsonIgnore
	public void input(int type, Concept concept) {
//		logger.info("propagate("+type+","+concept+")");
		Shape h = null;
		Coincidence coincidence = null;
		List<Node<Object>> nodeList = null;
		for (Map.Entry<String,Shape> entry : this.shapeMap.entrySet()) {
            h = entry.getValue();
            h.coincidence = h.getCoincidence(type);
		}
	}
	
	@JsonIgnore
	public void propagate(int type, Concept concept, boolean flag) {
//		logger.info("propagate("+type+", "+concept+", "+flag+")");
		Shape s = null;
		Coincidence coincidence = null;
		List<Node<Object>> nodeList = null;
		for (Map.Entry<String,Shape> entry : this.shapeMap.entrySet()) {
			coincidence = new Coincidence();
            s = entry.getValue();
            nodeList = s.getChildren();
            for(Node n : nodeList) {
            	coincidence.list.addAll(((Shape)n).coincidence.list);
            }
            s.addCoincidence(coincidence, concept,flag);
		}
	}
	
	@JsonIgnore
	public List<Concept> getCoincidenceConceptList() {
		List<Concept> conceptList = new ArrayList<>();
		Shape shape = null;
		List<Concept> cList = null;
		Integer count = 0;
		this.conceptCountMap = new HashMap<>();
		for (Map.Entry<String,Shape> entry : this.shapeMap.entrySet()) {
			shape = entry.getValue();
			if(shape.coincidence != null) {
				cList = shape.conceptListMap.get(shape.coincidence.toString());
			} else {
				cList = null;
			}
			if(cList != null) {
				for(Concept c: cList) {
					count = this.conceptCountMap.get(c.toString());
					count = (count != null)?count:0;
					this.conceptCountMap.put(c.toString(), count+1);
				}
			}
		}
		Integer total = this.getTotal(this.conceptCountMap);
		String value;
		Integer dividend;
		Double quotient;
		Concept concept = null;
		for (Map.Entry<String,Integer> entry : this.conceptCountMap.entrySet()) {
			value = entry.getKey();
			dividend = entry.getValue();
			quotient = (total>0)?(double)dividend/(double)total:0;
			concept = new Concept(value);
			concept.rank = quotient;
			conceptList.add(concept);
		}
		this.sortDescendingList(conceptList);
//		logger.debug("getConceptList() conceptList="+conceptList);
		return conceptList;
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
    private void sortAscendingList(List<Concept> list) {
        Collections.sort(list, new Comparator<Concept>() {
            public int compare(Concept ideaVal1, Concept ideaVal2) {
            	Double idea1 = ideaVal1.rank;
            	Double idea2 = ideaVal2.rank;
                return idea1.compareTo(idea2);
            }
        });
    }

	@JsonIgnore
    private void sortDescendingList(List<Concept> list) {
        Collections.sort(list, new Comparator<Concept>() {
            public int compare(Concept ideaVal1, Concept ideaVal2) {
            	Double idea1 = ideaVal1.rank;
            	Double idea2 = ideaVal2.rank;
                return idea2.compareTo(idea1);
            }
        });
    }
	
	public String toString() {
		return this.getShapeList()+"";
	}
}
