package com.meritoki.cortex.library.model.square;

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

import com.meritoki.cortex.library.model.Coincidence;
import com.meritoki.cortex.library.model.Concept;
import com.meritoki.cortex.library.model.Node;

/**
 * In a level, squares are always referenced by their relative coordinates, i.e. 0,0
 * @author osvaldo.rodriguez
 *
 */
public class Level {
	
	@JsonIgnore
	private static Logger logger = LogManager.getLogger(Level.class.getName());
	@JsonIgnore
	public Map<String, Square> squareMap = new HashMap<>();
	@JsonIgnore
	public Map<String, Integer> conceptCountMap = new HashMap<>();
	
	public Level() {
	}

	public Level(Map<String, Square> squareMap) {
		this.squareMap = squareMap;
	}

	@JsonIgnore
	public void addSquare(Square square) {
		this.squareMap.put(square.toString(), square);
	}

	@JsonIgnore
	public List<Square> getSquareList() {
		return this.getSquareList(this.squareMap);
	}

	@JsonIgnore
	public Map<String, Square> getSquareMap() {
		return this.squareMap;
	}

	@JsonIgnore
	public LinkedList<Square> getSquareList(Map<String, Square> squareMap) {
		LinkedList<Square> squareList = new LinkedList<Square>();
		for (Map.Entry<String, Square> entry : squareMap.entrySet()) {
			squareList.add((Square) entry.getValue());
		}
		return squareList;
	}
	
	@JsonIgnore
	public void input(int type, Concept concept) {
//		logger.info("propagate("+type+","+concept+")");
		Square h = null;
		Coincidence coincidence = null;
		List<Node<Object>> nodeList = null;
		for (Map.Entry<String,Square> entry : this.squareMap.entrySet()) {
            h = entry.getValue();
            h.coincidence = h.getCoincidence(type);
		}
	}
	
	@JsonIgnore
	public void propagate(int type, Concept concept, boolean flag) {
		Square h = null;
		Coincidence coincidence = null;
		List<Node<Object>> nodeList = null;
		for (Map.Entry<String,Square> entry : this.squareMap.entrySet()) {
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
		Square square = null;
		List<Concept> cList = null;
		Integer count = 0;
		this.conceptCountMap = new HashMap<>();
		for (Map.Entry<String,Square> entry : this.squareMap.entrySet()) {
			square = entry.getValue();
			if(square.coincidence != null) {
				cList = square.conceptListMap.get(square.coincidence.toString());
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
		return this.getSquareList()+"";
	}
}
