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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.meritoki.library.cortex.model.unit.Coincidence;
import com.meritoki.library.cortex.model.unit.Concept;
import com.meritoki.library.cortex.model.unit.ConceptComparator;

/**
 * In a level, shapes are always referenced by their relative coordinates, i.e.
 * 0,0
 * 
 * @author osvaldo.rodriguez
 *
 */
public class Level {

	@JsonIgnore
	protected static Logger logger = LoggerFactory.getLogger(Level.class.getName());
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
	public void addShape(String key, Shape shape) {
//		logger.info("addShape(shape) shape.toString()="+shape.toString());
		if (key == null)
			this.shapeMap.put(shape.toString(), shape);
		else
			this.shapeMap.put(key, shape);
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
	public void input(ColorType type, Concept concept) {
//		logger.info("propagate("+type+","+concept+")");
		Shape shape = null;
		Coincidence coincidence = null;
		List<Node<Object>> nodeList = null;
		for (Map.Entry<String, Shape> entry : this.shapeMap.entrySet()) {
			shape = entry.getValue();
//			shape.coincidence = shape.getCoincidence(type);
			shape.colorTypeCoincidenceMap.put(type, shape.getCoincidence(type));
		}
	}

//	@JsonIgnore
//	public void propagate(Concept concept, boolean flag, boolean nodeFlag) {
////		logger.info("propagate("+type+", "+concept+", "+flag+")");
//		Shape s = null;
//		Coincidence coincidence = null;
//		List<Node<Object>> nodeList = null;
//		for (Map.Entry<String, Shape> entry : this.shapeMap.entrySet()) {
//			coincidence = new Coincidence();
//			s = entry.getValue();
//			nodeList = s.getChildren();
//			if (nodeFlag) {
////				logger.info("propogate(...) nodeList.size()=" + nodeList.size());
//				for (int i = 0; i < nodeList.size(); i++) {
//					Node n = nodeList.get(i);
//					Shape shape = (Shape) n;
//					coincidence.list.addAll(shape.coincidence.list);
//				}
//				s.addCoincidence(coincidence, concept, flag);
//			} else {
//				int size = s.length;
//				if (size > 0) {
////					logger.info("propogate(...) size=" + size);
//					for (int i = 0; i < s.length; i++) {
//						if (i < nodeList.size()) {
//							Node n = nodeList.get(i);
//							Shape shape = (Shape) n;
//							size = shape.coincidence.list.size();
//							coincidence.list.addAll(shape.coincidence.list);
//						} else {
//							coincidence.list.addAll(new Coincidence(size).list);
//						}
//					}
//					s.addCoincidence(coincidence, concept, flag);
//				}
//			}
//		}
//	}

	@JsonIgnore
	public void propagate(ColorType type, Concept concept, boolean flag, boolean nodeFlag) {
		logger.debug("propagate("+type+", "+concept+", "+flag+", "+nodeFlag+")");
		Shape s = null;
		Coincidence coincidence = null;
		List<Node<Object>> nodeList = null;
		for (Map.Entry<String, Shape> entry : this.shapeMap.entrySet()) {
			coincidence = new Coincidence();
			s = entry.getValue();
			nodeList = s.getChildren();
			if (nodeFlag) {
//				logger.info("propogate(...) nodeList.size()=" + nodeList.size());
				for (int i = 0; i < nodeList.size(); i++) {
					Node n = nodeList.get(i);
					Shape shape = (Shape) n;
					coincidence.list.addAll(shape.colorTypeCoincidenceMap.get(type).list);
				}
				s.addCoincidence(type, coincidence, concept, flag);
			} else {
				int size = s.length;
				if (size > 0) {
//					logger.info("propogate(...) size=" + size);
					for (int i = 0; i < s.length; i++) {
						if (i < nodeList.size()) {
							Node n = nodeList.get(i);
							Shape shape = (Shape) n;
							size = shape.colorTypeCoincidenceMap.get(type).list.size();
							coincidence.list.addAll(shape.colorTypeCoincidenceMap.get(type).list);
						} else {
							coincidence.list.addAll(new Coincidence(size).list);
						}
					}
					s.addCoincidence(type, coincidence, concept, flag);
				}
			}
		}
	}

//	@JsonIgnore
//	public void feedback(Concept concept, boolean nodeFlag) {
//		Shape s = null;
//		Coincidence coincidence = null;
//		List<Node<Object>> nodeList = null;
//		for (Map.Entry<String, Shape> entry : this.shapeMap.entrySet()) {
//			s = entry.getValue();
//			nodeList = s.getChildren();
//			int length = 0;
//			for (int i = 0; i < nodeList.size(); i++) {
//				Node<?> n = nodeList.get(i);
//				if (n instanceof Shape) {
//					Shape shape = (Shape) n;
//					length = (nodeFlag) ? nodeList.size() : shape.length;
//					if (length > 0) {
////						logger.info("feedback(...) length=" + length);
//						List<Integer> list = s.coincidence.getSublist(length, i);
//						if (list != null) {
//							shape.addCoincidence(new Coincidence(list), concept, false);
//
//						}
//					}
//				}
//			}
//		}
//	}

	@JsonIgnore
	public void feedback(ColorType type, Concept concept, boolean nodeFlag) {
		Shape s = null;
//		Coincidence coincidence = null;
		List<Node<Object>> nodeList = null;
		for (Map.Entry<String, Shape> entry : this.shapeMap.entrySet()) {
			s = entry.getValue();
			nodeList = s.getChildren();
			int length = 0;
			for (int i = 0; i < nodeList.size(); i++) {
				Node<?> n = nodeList.get(i);
				if (n instanceof Shape) {
					Shape shape = (Shape) n;
					length = (nodeFlag) ? nodeList.size() : shape.length;
					if (length > 0) {
//						logger.info("feedback(...) length=" + length);
						List<Integer> list = s.colorTypeCoincidenceMap.get(type).getSublist(length, i);
						if (list != null) {
							shape.addCoincidence(type, new Coincidence(list), concept, false);

						}
					}
				}
			}
		}
	}

	public void addCoincidenceConceptList(Coincidence coincidence, List<Concept> conceptList) {
		List<Concept> cList = null;
		for (Map.Entry<String, Shape> entry : this.shapeMap.entrySet()) {
			Shape shape = entry.getValue();
			cList = shape.coincidenceConceptListMap.get(coincidence.toString());
			if (cList != null) {
				cList.addAll(conceptList);
				shape.coincidenceConceptListMap.put(coincidence.toString(), cList);
			}
		}
	}

	public void removeCoincidenceConceptList(Coincidence coincidence, List<Concept> conceptList) {
		List<Concept> cList = null;
		for (Map.Entry<String, Shape> entry : this.shapeMap.entrySet()) {
			Shape shape = entry.getValue();
			cList = shape.coincidenceConceptListMap.get(coincidence.toString());
			if (cList != null) {
				cList.removeAll(conceptList);
				shape.coincidenceConceptListMap.put(coincidence.toString(), cList);
			}
		}
	}

	@JsonIgnore
	public List<Concept> getCoincidenceConceptList() {
		List<Concept> conceptList = new ArrayList<>();
		Shape shape = null;
		List<Concept> cList = null;
		Integer count = 0;
		this.conceptCountMap = new HashMap<>();
		for (Map.Entry<String, Shape> entry : this.shapeMap.entrySet()) {
			shape = entry.getValue();
			if (shape.coincidence != null) {
				cList = shape.coincidenceConceptListMap.get(shape.coincidence.toString());
			} else {
				cList = null;
			}
			if (cList != null) {
				for (Concept c : cList) {
					count = this.conceptCountMap.get(c.toString());
					count = (count != null) ? count : 0;
					this.conceptCountMap.put(c.toString(), count + 1);
				}
			}
		}
		Integer total = this.getTotal(this.conceptCountMap);
		String value;
		Integer dividend;
		Double quotient;
		Concept concept = null;
		for (Map.Entry<String, Integer> entry : this.conceptCountMap.entrySet()) {
			value = entry.getKey();
			dividend = entry.getValue();
			quotient = (total > 0) ? (double) dividend / (double) total : 0;
//			concept = new Concept(value);
//			concept.rank = quotient;
//			conceptList.add(concept);
		}
		Collections.sort(conceptList, new ConceptComparator());
//		logger.info("getCoincidenceConceptList() conceptList="+conceptList);
		return conceptList;
	}

	@JsonIgnore
	public List<Concept> getPredictionConceptList() {
		List<Concept> conceptList = new ArrayList<>();
		Shape shape = null;
		List<Concept> cList = null;
		Integer count = 0;
		this.conceptCountMap = new HashMap<>();
		for (Map.Entry<String, Shape> entry : this.shapeMap.entrySet()) {
			shape = entry.getValue();
			if (shape.prediction != null) {
				cList = shape.coincidenceConceptListMap.get(shape.prediction.toString());
			} else {
				cList = null;
			}
			if (cList != null) {
				for (Concept c : cList) {
					count = this.conceptCountMap.get(c.toString());
					count = (count != null) ? count : 0;
					this.conceptCountMap.put(c.toString(), count + 1);
				}
			}
		}
		Integer total = this.getTotal(this.conceptCountMap);
		String value;
		Integer dividend;
		Double quotient;
		Concept concept = null;
		for (Map.Entry<String, Integer> entry : this.conceptCountMap.entrySet()) {
			value = entry.getKey();
			dividend = entry.getValue();
			quotient = (total > 0) ? (double) dividend / (double) total : 0;
//			concept = new Concept(value);
//			concept.rank = quotient;
//			conceptList.add(concept);
		}
		logger.info("getPredictionConceptList() conceptList=" + conceptList);
		return conceptList;
	}

	public List<Coincidence> getCoincidenceList() {
		List<Coincidence> coincidenceList = new ArrayList<>();
		for (Map.Entry<String, Shape> entry : this.shapeMap.entrySet()) {
			Shape shape = entry.getValue();
			coincidenceList.add(shape.coincidence);
		}
		return coincidenceList;
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
		return this.getShapeList() + "";
	}
}

//List<Concept> conceptList = shape.conceptListMap.get(new Coincidence(list).toString());
//if(conceptList != null) {
//	shape.addCoincidence(coincidence, conceptList.get(c), true);
//} else {
//	shape.setCoincidence(new Coincidence(list));
//}
