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
package com.meritoki.library.cortex.model;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.meritoki.library.cortex.model.motor.Direction;

public class Matrix {

	@JsonIgnore
	public double threshold = 16;
	@JsonIgnore
	public double max;
	@JsonIgnore
	public double min;

	@JsonIgnore
	public List<Point> pointList;

	@JsonIgnore
	public List<ArrayList<Point>> rowList;

	public Matrix(List<Point> pointList, double threshold) {
		this.pointList = new ArrayList(pointList);
		this.threshold = threshold;
		this.rowList = this.getRowList();
	}

	public List<Point> getRadiusPointList(Point input, double radius) {
		List<Point> radiusList = null;
		for (int i = 0; i < this.size(); i++) {
			for (int j = 0; j < this.getPointList(i).size(); j++) {
				Point data = this.getPoint(i, j);
				if (data != null) {
					// We must tell Point the cortex origin.
					double dataRadius = data.getRadius(input);
					dataRadius = this.round(dataRadius);
					if (dataRadius == radius) {// It will always be able to find points, because the belief added its
												// own points
						if (radiusList == null)
							radiusList = new ArrayList<>();
						System.out.println(i + ":" + j);
						System.out.println("dataRadius=" + dataRadius);
						radiusList.add(data);
					}
				}
			}
		}
		return radiusList;
	}
	
	

	public double round(double value) {
		DecimalFormat decimalFormat = new DecimalFormat("#.##");
		return Double.parseDouble(decimalFormat.format(value));
	}

	public List<Point> getCurrentLine(Point center) {
		center = new Point(center);
		center.round();
		List<Point> line = null;
		for (int i = 0; i < this.size(); i++) {
			for (int j = 0; j < this.getPointList(i).size(); j++) {
				Point data = new Point(this.getPoint(i, j));
				if (data != null) {
					// We must tell Point the cortex origin.
					// Will always need to find the current line
					// For the current belief, we can get the line that its origin currently
					// intersects
					data.round();
					if (data.equals(center)) {// All beliefs are on a line.
						line = this.getPointList(i);// only one line will ever be returned
						// b/c the belief has only one origin.
					}
				}
			}
		}
//		System.out.println("getCurrentLine("+center+") line="+line);
		return line;
	}

//	public Point getNextPoint(Point origin, double radius, Direction direction) {
//		Point point = null;
//		List<Point> line = this.getCurrentLine(origin);
//		double min = Double.MAX_VALUE;//;input.y;
//		double max = Double.MIN_VALUE;//input.y;
//		if (line != null) {
//			double distance = Point.getDistance(origin, line.get(line.size() - 1));
//			if (radius < distance) {
//				switch (direction) {
//				case LEFT: {
//					for (Point p : line) {
//						distance = this.round(Point.getDistance(origin, p));
//						if (p.x < origin.x && distance == radius) {
//							point = p;
//						}
//					}
//					break;
//				}
//				case RIGHT: {
//					for (Point p : line) {
//						distance = this.round(Point.getDistance(origin, p));
//						if (p.x > origin.x && distance >= radius && distance < min) {//list is in order, so latest is max
//							min = distance;
////							System.out.println("getNextPoint("+origin+", "+radius+", "+direction+") distance="+distance);
//							point = p;
//						}
//					}
//					break;
//				}
//				default: {
//					System.err.println("direction=" + direction);
//				}
//				}
//			} else {
//				point = line.get(line.size() - 1);
//				if(point.equals(origin)) {
//					point = null;
//				}
//			}
//		}
//		System.out.println("getNextPoint("+origin+", "+radius+", "+direction+") point="+point);
//		if(point != null)
//			point.round();
//		return point;
//	}
	
	public Point getNextPoint(Point input, double radius, Direction direction) {
		System.out.println("getNextPoint(" + input + ", " + radius + ", " + direction + ")");
		// Get line
		Point point = null;
		// Direction UP/DOWN
		// if direction UP, get point closet to input, which minimizes y
		// if direction DOWN, get point closet to input, which maximizes y
		double min = Double.MAX_VALUE;//;input.y;
		double max = Double.MIN_VALUE;//input.y;

		for (int i = 0; i < this.size(); i++) {
			for (int j = 0; j < this.getPointList(i).size(); j++) {
				Point data = this.getPoint(i, j);
				if (data != null) {
					if (input.y - 2 < data.y && data.y < input.y + 2) {// found points that line on the same vertical line.
						// To the algorithm, must add current input.y and radius.
						switch (direction) {
						case LEFT: {
							if (data.x < (input.x - radius) && data.x > max) {
								max = data.x;
								point = data;
							}
							break;
						}
						case RIGHT: {
							if (data.x > (input.x + radius) && data.x < min) {
								min = data.x;
								point = data;
							}
							break;
						}
						}
					}
				}
			}
		}
//		System.out.println("getPerpendicularLine(" + input + ", " + radius + ", " + direction + ") line=" + line);
		return point;

	}


	/**
	 * Must incorporate radius into algorithm
	 * 
	 * @param input
	 * @param radius
	 * @param direction
	 * @return
	 */
	public List<Point> getPerpendicularLine(Point input, double radius, Direction direction) {
		System.out.println("getPerpendicularLine(" + input + ", " + radius + ", " + direction + ")");
		// Get line
		List<Point> line = null;
		double threshold = 2.0;
		// Direction UP/DOWN
		// if direction UP, get point closet to input, which minimizes y
		// if direction DOWN, get point closet to input, which maximizes y
		double min = Double.MAX_VALUE;//;input.y;
		double max = Double.MIN_VALUE;//input.y;

		for (int i = 0; i < this.size(); i++) {
			for (int j = 0; j < this.getPointList(i).size(); j++) {
				Point data = this.getPoint(i, j);
				if (data != null) {
					if (input.x -5 < data.x && data.x < input.x + 5) {// found points that line on the same vertical line.
						// To the algorithm, must add current input.y and radius.
						switch (direction) {
						case UP: {
//							if (data.y < min && data.y > (input.y - radius)) {
							if (data.y < (input.y - radius) && data.y > max) {
								max = data.y;
								line = this.getPointList(i);
							}
							break;
						}
						case DOWN: {
							if (data.y > (input.y + radius) && data.y < min) {
//							if (data.y > max && data.y < (input.y + radius)) {
//							if (data.y >= (input.y + radius)) {
								min = data.y;
								line = this.getPointList(i);
							}
							break;
						}
						}
					}
				}
			}
		}
//		System.out.println("getPerpendicularLine(" + input + ", " + radius + ", " + direction + ") line=" + line);
		return line;

	}

	public int size() {
		return this.rowList.size();
	}

	public Point getPoint(int a, int b) {
		return rowList.get(a).get(b);
	}

	public List<Point> getPointList(int a) {
		return rowList.get(a);
	}

	@JsonIgnore
	public List<ArrayList<Point>> getRowList() {
		List<Point> pointList = this.pointList;
		List<ArrayList<Point>> list = new ArrayList<>();
		if (pointList != null && pointList.size() > 0) {
			Point point = null;
			boolean flag = true;
			for (int i = 0; i < pointList.size(); i++) {
				point = pointList.get(i);
//				if(point.getChildren().size()==0) {
				for (List<Point> rowList : list) {
					if (this.isPointListYInThreshold(rowList, point)) {
						rowList.add(point);
						flag = false;
					}
				}
				if (flag) {
					ArrayList<Point> rowList = new ArrayList<>();
					rowList.add(point);
					list.add(rowList);
				} else {
					flag = true;
				}
				this.sortShapeMatrix(list);
//				}
			}
//			this.print(list);
		}
		return list;
	}

	@JsonIgnore
	public List<Point> getPointList() {
		List<Point> pointList = null;
		List<ArrayList<Point>> shapeMatrix = this.getRowList();
		if (shapeMatrix != null) {
			pointList = new LinkedList<>();
			for (int i = 0; i < shapeMatrix.size(); i++) {
				for (int j = 0; j < shapeMatrix.get(i).size(); j++) {
					pointList.add(shapeMatrix.get(i).get(j));
				}
			}
		}
		return pointList;
	}

	@JsonIgnore
	public void print(List<ArrayList<Point>> list) {
		String string = null;
		if (list != null && list.size() > 0) {
			string = "\n";
			for (int i = 0; i < list.size(); i++) {
				for (int j = 0; j < list.get(i).size(); j++) {
					if (list.get(i).get(j) != null) {
						string += "*";
					}
				}
				if (i < (list.size() - 1)) {
					string += "\n";
				}
			}
		}
		if (string != null) {
			System.out.println(string);
		}
	}

	@JsonIgnore
	public double getpointListYAverage(List<Point> pointList, Point point) {
		double average = 0;
		int count = 0;
		double sum = 0;
		for (Point s : pointList) {
			sum += s.y;
			count += 1;
		}
		sum += point.y;
		count += 1;
		average = sum / count;
		return average;
	}

	@JsonIgnore
	public boolean isPointListYInThreshold(List<Point> pointList, Point shape) {
		boolean flag = true;
		double a = 0;
		double average = this.getpointListYAverage(pointList, shape);
		a = shape.y;
		a = Math.abs(average - a);
		if (a > (threshold)) {
			flag = false;
		}
		return flag;
	}

	@JsonIgnore
	public void sortShapeMatrix(List<ArrayList<Point>> list) {
		for (List<Point> pointList : list) {
			this.sortRowList(pointList);
		}
		Collections.sort(list, new Comparator<List<Point>>() {
			public int compare(List<Point> ideaVal1, List<Point> ideaVal2) {
				Double idea1 = ideaVal1.get(0).y;
				Double idea2 = ideaVal2.get(0).y;
				return idea1.compareTo(idea2);
			}
		});
	}

	@JsonIgnore
	public void sortRowList(List<Point> pointList) {
		Collections.sort(pointList, new Comparator<Point>() {
			public int compare(Point ideaVal1, Point ideaVal2) {
				Double idea1 = ideaVal1.x;// pointList.get(0).x;
				Double idea2 = ideaVal2.x;// pointList.get(0).x;
				return idea1.compareTo(idea2);
			}
		});
	}

	@JsonIgnore
	public boolean columnListContains(List<Point> pointList, Point shape) {
		boolean flag = false;
		for (Point s : pointList) {
			if (s.equals(shape)) {
				flag = true;
			}
		}
		return flag;
	}

	@JsonIgnore
	public boolean rowListContains(List<List<Point>> pointList, Point shape) {
		boolean flag = false;
		for (List<Point> s : pointList) {
			flag = this.columnListContains(s, shape);
			if (flag) {
				break;
			}
		}
		return flag;
	}

	@JsonIgnore
	@Override
	public String toString() {
		String string = "";
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		try {
			string = ow.writeValueAsString(this);
		} catch (IOException ex) {
			System.err.println("IOException " + ex.getMessage());
		}

		return string;
	}
}
