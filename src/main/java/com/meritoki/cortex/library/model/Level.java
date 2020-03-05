package com.meritoki.cortex.library.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * In a level, hexagons are always referenced by their relative coordinates, i.e. 0,0
 * @author osvaldo.rodriguez
 *
 */
public class Level {
	
	@JsonIgnore
	private static Logger logger = LogManager.getLogger(Level.class.getName());
	@JsonIgnore
	public Map<String, Hexagon> hexagonMap = new HashMap<>();
	@JsonIgnore
	public Map<String, Integer> conceptCountMap = new HashMap<>();
	
	public Level() {
	}

	public Level(Map<String, Hexagon> hexagonMap) {
		this.hexagonMap = hexagonMap;
	}

	@JsonIgnore
	public void addHexagon(Hexagon hexagon) {
		this.hexagonMap.put(hexagon.toString(), hexagon);
	}

	@JsonIgnore
	public List<Hexagon> getHexagonList() {
		return this.getHexagonList(this.hexagonMap);
	}

	@JsonIgnore
	public Map<String, Hexagon> getHexagonMap() {
		return this.hexagonMap;
	}

	@JsonIgnore
	public LinkedList<Hexagon> getHexagonList(Map<String, Hexagon> hexagonMap) {
		LinkedList<Hexagon> hexagonList = new LinkedList<Hexagon>();
		for (Map.Entry<String, Hexagon> entry : hexagonMap.entrySet()) {
			hexagonList.add((Hexagon) entry.getValue());
		}
		return hexagonList;
	}
	
	@JsonIgnore
	public void input(int type, Concept concept) {
//		logger.info("propagate("+type+","+concept+")");
		Hexagon h = null;
		Coincidence coincidence = null;
		List<Node<Object>> nodeList = null;
		for (Map.Entry<String,Hexagon> entry : this.hexagonMap.entrySet()) {
            h = entry.getValue();
            h.coincidence = h.getCoincidence(type);
		}
	}
	
	@JsonIgnore
	public void propagate(int type, Concept concept, boolean flag) {
		Hexagon h = null;
		Coincidence coincidence = null;
		List<Node<Object>> nodeList = null;
		for (Map.Entry<String,Hexagon> entry : this.hexagonMap.entrySet()) {
			coincidence = new Coincidence();
            h = entry.getValue();
            nodeList = h.getChildren();
            for(Node n : nodeList) {
            	coincidence.list.addAll(n.coincidence.list);
            }
            h.addCoincidence(coincidence, concept,flag);
		}
	}
	
	@JsonIgnore
	public List<Concept> getCoincidenceConceptList() {
		List<Concept> conceptList = new ArrayList<>();
		Hexagon hexagon = null;
		List<Concept> cList = null;
		Integer count = 0;
		this.conceptCountMap = new HashMap<>();
		for (Map.Entry<String,Hexagon> entry : this.hexagonMap.entrySet()) {
			hexagon = entry.getValue();
			if(hexagon.coincidence != null) {
				cList = hexagon.conceptListMap.get(hexagon.coincidence.toString());
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
		return this.getHexagonList()+"";
	}
}
