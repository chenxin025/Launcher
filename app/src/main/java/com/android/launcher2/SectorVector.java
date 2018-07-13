package com.android.launcher2;

public class SectorVector {
	
	public static final float VECTOR_LENGTH_MIN = 40.0f;
	
	public double length ;
	public double angle ;
	
	public SectorVector(double length, double angle) {
		super();
		this.length = length;
		this.angle = angle;
	}
	
	public boolean lengthIsValid()
	{
		if (length >= VECTOR_LENGTH_MIN) {
			return true;
		}
		return false;
	}
	
	public boolean isFirstQuadrant()
	{
		if (angle > 0.0 && angle < 90.0) {
			return true;
		}
		return false;
	}
	
	public boolean isSecondQuadrant()
	{
		if (angle >= 90.0 && angle <= 180.0) {
			return true;
		}
		return false;
	}
	
	public boolean isThirdQuadrant()
	{
		if (angle > 180.0 && angle < 270.0) {
			return true;
		}
		return false;
	}
	
	public boolean isFourthQuadrant()
	{
		if (angle >= 270.0 && angle <= 360.0) {
			return true;
		}
		return false;
	}

}
