package com.meritoki.library.cortex.model.motor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.meritoki.library.cortex.model.Belief;
import com.meritoki.library.cortex.model.Matrix;
import com.meritoki.library.cortex.model.Mind;
import com.meritoki.library.cortex.model.Point;
import com.meritoki.library.cortex.model.cortex.Cortex;

/**
 * Motor is a class used to move Retina.
 * 
 * @author jorodriguez
 *
 */
//Does an accounting that the point it solicited is the input;
public class Motor {

	public Cortex cortex;
	public Delta delta = null;
	public LinkedList<Delta> deltaStack = new LinkedList<>();
	public Point point;
	public List<Point> pointList = new ArrayList<Point>();
	public Direction vertical = Direction.CENTER;
	public Direction horizontal = Direction.CENTER;
	public Matrix matrix;

	public Motor() {
	}

	public void setCortex(Cortex cortex) {
		this.cortex = cortex;
	}

	// The absolute point, where
	// Have input Point
	/**
	 * First imlementation of input is based on left to right, top to bottom
	 * movement The Matrix is used in conjunction with radius // We are going to
	 * apply the left to right, top to bottom method, relative to Cortex size. //
	 * Starting direction CENTER. // The start position is the center. // Move to
	 * the beginning of a new line that is perpendicular to and less than or equal
	 * to the belief radius away // Move to the max point on the line that is less
	 * than or equal to belief radius. (Repeats until end of line is reached). //
	 * Move to center if possible direction DOWN // Move to the beginning of a new
	 * line that is less than or equal to the belief radius away // Then we move to
	 * the max point on the line that is less than or equal to // belief radius.
	 * (Repeats until end of line is reached). // At some point I will want to visit
	 * where I have been before // So I have these two apparent functions in
	 * conflict with one another. // How can they be resolved? // In one case we
	 * have the concept of Training and the other Inference. // If we are in
	 * training mode then we are searching for far away or new // concepts. // When
	 * we are inferring we are looking for what we already know in relation to //
	 * itself // Therefore if concept is null, we want to visit where we have been
	 * before. // If concept is not null, we want to visit farthest away. // Min/Max
	 * distance from what is "known" or believed // This can be used to find
	 * beliefs, what is the purpose of mind? // Mind was intended to be a meter from
	 * 0,0 that registers any belief by radius. // One or more beliefs can be found
	 * that are at a point we are interested in // going to. // Each of these
	 * beliefs has concepts. // With this data it is possible to do the following:
	 * // 1) If I am at a specific location, I can check if I've been there before
	 * // and choose a different point. // If I choose the different point, How
	 * often will this work? // 2) Based on the prediction of Cortex, I can choose a
	 * belief with the same // concept. // 3) Choose a point in the matrix rowlist.
	 * 
	 * @param center
	 * @param input
	 * @param scale
	 */
	public void input(Point center, Point input, double scale) {
		System.out.println("input(" + center + ", " + input + ", " + scale + ")");
		int size = this.pointList.size();
		if (size > 0) {
			Point previous = this.pointList.get(size - 1);
		}
		this.pointList.add(input);
		double cortexRadius = this.cortex.getRadius();// Size of sensor
		Belief belief = this.cortex.getBelief();
		double beliefRadius = belief.getRadius() * scale;// Size of curret belief
		if(beliefRadius == 0) {
			beliefRadius = cortexRadius;
		}
		cortexRadius = this.round(cortexRadius);
		beliefRadius = this.round(beliefRadius);
		Point origin = new Point(belief.origin); // Origin is the position from the Mouse x, y;
		Point global = new Point(belief.global); // Global is the position translated to 0,0 using the Input Image
													// center Point
		Point relative = new Point(belief.relative); // Relative is the position translated to 0,0 using Input Image
														// Point
		origin.scale(scale);
		global.scale(scale);
		relative.scale(scale);
		origin.round();
		global.round();
		relative.round();
		System.out.println("beliefRadius=" + beliefRadius);
		System.out.println("cortexRadius=" + cortexRadius);
		System.out.println("origin=" + origin);
		System.out.println("global=" + global);
		System.out.println("relative=" + relative);
		// Point list is updated with the points from the current belief
		List<Point> pointList = this.cortex.getPointList(center, scale);// list has origin equal to input image center
		this.matrix = new Matrix(pointList, 4 * scale);
//		Mind mind = this.cortex.getMind(beliefRadius);//Mind returns list of Beliefs @ Value equal to Relative Point Radius
//		if(mind != null) {
//			for(Belief b: mind.beliefList) {
//				System.out.println(b);
//			}
//		}
		Point move = null;
		// The idea was that when Direction is center, there is no output.
		if (input.equals(center)) {
			// Switch directions when input is equal to center.
			// There is a problem here that I predict.
			// Problem has to do with where vertical starts.
			// We want to start at CENTER, because it converts to UP in the first iteration.
			System.out.println("input(...) this.vertical=" + this.vertical);
			switch (this.vertical) {
			case UP: {
				List<Point> line = matrix.getPerpendicularLine(origin, beliefRadius, this.vertical);
				if (line != null)
					move = line.get(0);
				this.vertical = Direction.DOWN;
				break;
			}
			case DOWN: {
				List<Point> line = matrix.getPerpendicularLine(origin, beliefRadius, this.vertical);
				if (line != null)
					move = line.get(0);
				this.vertical = Direction.CENTER;
				break;
			}
			case CENTER: {
				this.vertical = Direction.UP;
				break;
			}
			default: {
				System.err.println("this.vertical=" + this.vertical);
			}
			}

		} else {
			Point point = matrix.getNextPoint(origin, beliefRadius, Direction.RIGHT);
			if (point != null) {
				move = point;
			} else {
				// Move to center or find new perpendicular line
				// Can center be reached from the current point?
				//
//				I am working on something complicated.
//				Use history of movement to center to move to center.
//				If cannot reach center, keep going up or down. Belief has origin and radius, center is known. If distance is greater than radius, cannot reach center.

				// Hardest part
				// If Mind is working correctly, then there should exist a way to "jump"
				// from the last point of the current line to center.
				// This ability is mapped to the last point of the current line.
				// It can be checked for. In some cases we will want to ignore the jump, if
				// there is more than one jump possible
				// When do we jump and not jump?

				// When a point returns to center from where it was, it creates a relative
				// belief
//				If find belief at radius equal to center in Mind.
				// then I have a candidate to return to center

				double distance = Point.getDistance(belief.origin, center);
				if (beliefRadius >= distance) {
					System.out.println("distance=" + distance);
					move = center;
					this.vertical = Direction.CENTER;
				} else {
					System.out.println("cannot reach center");
					// else find next perpendicular line
					// For now, the easiest solution to for this algorithm is to fail to return a
					// line and go to center.
					List<Point> line = matrix.getPerpendicularLine(origin, beliefRadius, this.vertical);
					if (line != null) {
						move = line.get(0);
					} else {
						System.out.println("move to center");
						move = center;
						this.vertical = Direction.CENTER;
					}
				}

			}
		}

		// We are finally returning a move. Now must figure out how to not send a move.

		if (move != null && !move.equals(input)) {
			Delta delta = new Delta(input, move);
			System.out.println("delta=" + delta);
			this.deltaStack.push(delta);
		}
	}

	public void paint(Graphics2D graphics2D) {
		if (graphics2D != null) {
			if (this.matrix != null) {
				graphics2D.setColor(Color.PINK);
				for (int i = 0; i < this.matrix.rowList.size(); i++) {
					List<Point> pointList = this.matrix.rowList.get(i);
					Point previous = null;
					Point current;
					for (int j = 0; j < pointList.size(); j++) {
						current = pointList.get(j);
						if (previous != null) {
							graphics2D.drawLine((int) (current.x), (int) (current.y), (int) (previous.x),
									(int) (previous.y));
						}
						previous = current;
					}
				}
			}
		}
	}

	public double round(double value) {
		DecimalFormat decimalFormat = new DecimalFormat("#.##");
		return Double.parseDouble(decimalFormat.format(value));
	}

	/**
	 * Returning null will break a loop and allow the next scale to produce deltas.
	 * 
	 * @return
	 */
	public Delta getDelta() {
		// This is where the answer to the question goes.
		// Delta is the the returned object, at some point delta is null;
		// Points of a belief indicate locations where the same belief could apply.
		// Start with current Point, point can be found at end of
//		double cortexRadius = this.cortex.getRadius();//Size of sensor
//		Belief belief = this.cortex.getBelief();
//		double beliefRadius = belief.getRadius();//Size of curret belief
		Delta delta = (this.deltaStack.size() > 0) ? this.deltaStack.pop() : null;
		return delta;
	}

	// Will return absolute point with origin equal to input center;
	public List<ArrayList<Point>> getRowList(List<Point> pointList, double scale) {
//		for (Point p : this.cortex.pointList) {
//			p = new Point(p);
//			p.x *= scale;
//			p.y *= scale;
//			p.x += origin.x;
//			p.y += origin.y;
//			pList.add(p);
//		}
//		this.getPointList(origin, scale)
		Matrix matrix = new Matrix(pointList, 4 * scale);
		return matrix.getRowList();
	}
}

//System.out.println("line="+line);
////global has an index in line.
////line  
//boolean contains = false;
////Remove from data list if have been there before
////Using Mind, find Beliefs are Radius,
////If data Point equals global of belief from list, then remove from dataList.
//for(Point data:dataList) {
//	//Data list used to generate deltas. Deltas are added to stack.
//	System.out.println("data="+data);
//	if(line.contains(data)) {
//		contains = true;
//	}
//}
//
//if(contains) {
//	//if contains is true then the point we can move to is on the line.
//}
//
//
////Can get start and stop of the line
//if(line.size() > 0) {
//	Point start = line.get(0);
//	Point stop = line.get(line.size()-1);
//}

// This code shows that there is a max radius and at least one point that
// corresponds to that radius
// Technically this code should always return at least one value.
//List<Point> list = belief.getRadiusPointList(beliefRadius,scale);//This works because belief origin and pointList have been processed
//for(Point p: list) {
//	System.out.println("*******"+p);//Point is relative to 0,0
//}

// There is an inequality here that is vital, I have to figure it out.
//However, if the cortexRadius is greater than the beliefRadius, then we must choose a point that is at least the beliefRadius away.
//if(cortexRadius > beliefRadius) {
// Point is at least beliefRadius away.

// this algorithm is the hardest I have ever written
// I do not want the system to follow lines I have drawn.
// I want it to follow lines it sees.
// But to get to that point, it must "see" something in some way.
// This is the complete point list, must somehow get a point to visit.
// Premise is that where ever cortex is currently pointing, it will want to find
// new concept, so we are interested in points far away.
// Points "far away" are points at the boundary of the cortex radius.
// However, if the cortexRadius is greater than the beliefRadius, then we must
// choose a point that is at least the beliefRadius away.
//Find a generalized way to arrive at Matrix.get(0,0);
// Meaning, that beliefRadius helps determine the line we move to the start of
// from where ever we are.
// if data is in line go to that data.
// Programming the robot.
// Want robot to go to beginning of line
// Want robot to go to new line
// Want robot to find new concepts
// Move is the point that is put into a delta.

//If I do this correctly, an image can also be scanned in reverse order. This
// can be achieved with a global direction variable?
// Figure out how Im going to step through moves or if we really use deltaStack
// with multiple moves in order.
// Knowing one is at the center is important because it changes the processing.
// Center is like an algorithmic blink.

// We have the following:
// 1) Beliefs with Global Point, indicating position relative to origin equals to input image center. 
// 2) Beliefs with Relative Point, indicating position relative to origin equal to input, i.e. mouse click. 
// 3) Mind returns list of Beliefs @ Value equal to Relative Point Radius
// - Each Belief has three positions origin, relative, global
// 
// 
// 
// 4) The Lines, if you have a point on a line, following the line.
// When choosing a point from a line, we choose using the belief radius.
// When we reach the end of a line, we must choose what to do.
//List<Point> currentLine = matrix.getCurrentLine(origin);
//List<Point> dataList = matrix.getRadiusPointList(input, beliefRadius);
//System.out.println("currentLine=" + currentLine);
//if (currentLine.size() > 0) {
//	Point start = currentLine.get(0);
//	Point stop = currentLine.get(currentLine.size() - 1);
//}
