package com.meritoki.library.cortex.model.motor;

import com.meritoki.library.cortex.model.Point;
import com.meritoki.library.cortex.model.cortex.Cortex;

/**
 * Motor is a class used to move Retina.
 * @author jorodriguez
 *
 */
public class Motor {

	public Cortex cortex;
	
	public Motor(Cortex cortex) {
		this.cortex = cortex;
	}
	
	public Delta getDelta() {
		return null;
	}
}
