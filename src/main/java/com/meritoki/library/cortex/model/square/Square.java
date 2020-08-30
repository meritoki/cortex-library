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
package com.meritoki.library.cortex.model.square;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.meritoki.library.cortex.model.Point;
import com.meritoki.library.cortex.model.Shape;

public class Square extends Shape {
	@JsonIgnore
	private static Logger logger = LogManager.getLogger(Square.class.getName());

	public Square() {
		super(4,45,0,0,new Point(0,0),1);
	}

	public Square(Square square) {
		super(4,45,square.getX(), square.getY(), square.getCenter(), square.getRadius());
	}
	
	public Square(Shape shape) {
		super(4,45,shape.getX(), shape.getY(), shape.getCenter(), shape.getRadius());
	}

	public Square(int x, int y, Point center, double radius) {
		super(4,45,x,y,center,radius);
	}
}
