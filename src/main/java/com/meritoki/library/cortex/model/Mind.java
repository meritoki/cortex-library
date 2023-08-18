package com.meritoki.library.cortex.model;

import java.util.ArrayList;
import java.util.List;
//https://www.baeldung.com/java-range-search
public class Mind {
	
	public List<Belief> beliefList = new ArrayList<>();

	public Mind(Belief belief) {
		super();
	}
	
	public void add(Belief belief) {
		this.beliefList.add(belief);
	}
}
