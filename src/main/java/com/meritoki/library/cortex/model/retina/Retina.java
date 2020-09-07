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
package com.meritoki.library.cortex.model.retina;

import java.util.HashMap;
import java.util.Map;

import com.meritoki.library.cortex.model.Cortex;

public class Retina extends Cortex {
	
	Map<String, Cortex> cortexMap = new HashMap<>();
	
	
	//Process the output of the cortexMap into input for the shapeMap
	//Has some configuration involved.
	
	//Must determine how to process default size of cortex into size of the larger cortex. 
	
	//default one cortex per shape, then two, three.
	
}
