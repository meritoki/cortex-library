package com.meritoki.library.cortex.model;

public class Binary {

	public double value;
	public Binary left;
	public Binary right;

	public Binary(double value) {
		this.value = value;
		right = null;
		left = null;
	}
}
