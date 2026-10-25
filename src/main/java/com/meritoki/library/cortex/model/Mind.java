package com.meritoki.library.cortex.model;

import java.util.ArrayList;
import java.util.List;

public class Mind extends Binary {
	
	public List<Belief> beliefList = new ArrayList<>();

	public Mind(Belief belief) {
		super(belief.getRelativeRadius());
		this.beliefList.add(belief);
	}
}
