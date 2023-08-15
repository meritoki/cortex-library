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
package com.meritoki.library.cortex.model.network.hexagon;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.meritoki.library.cortex.model.Point;
import com.meritoki.library.cortex.model.network.Shape;

public class Hexagon extends Shape {

	@JsonIgnore
	protected static Logger logger = LoggerFactory.getLogger(Hexagon.class.getName());
	
	public Hexagon() {
		super(6,90,0,0,new Point(0,0),1);
	}

	public Hexagon(Hexagon hexagon) {
		super(6,90,hexagon.getX(), hexagon.getY(), hexagon.getCenter(), hexagon.getRadius());
	}
	
	public Hexagon(Shape shape) {
		super(6,90,shape.getX(), shape.getY(), shape.getCenter(), shape.getRadius());
	}

	public Hexagon(int x, int y, Point center, int radius) {
		super(6,90,x,y,center,radius);
	}
}