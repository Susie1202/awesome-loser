package com.loserico.pattern.bridge;

public class Circle implements Shape {

	private int x;
	private int y;
	private int r;

	public Circle(int x, int y, int r) {
		this.x = x;
		this.y = y;
		this.r = r;
	}

	@Override
	public void draw() {
		//invoke the draw method  
	}

}