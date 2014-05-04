package aman.project.remoteppt;

import java.io.Serializable;

public class PointHandler implements Serializable
{

	private static final long serialVersionUID = 1L;
	private float pointX, pointY;
	private String color;
	
	PointHandler(float pointX, float pointY)
	{
		this.pointX = pointX;
		this.pointY = pointY;
		this.color = "black";
	}

	PointHandler(float pointX, float pointY, String color)
	{
		this.pointX = pointX;
		this.pointY = pointY;
		this.color = color;
	}
	
	public float getX()
	{
		return this.pointX;
	}
	
	public float getY()
	{
		return this.pointY;
	}
	
	public void setX(float pointX)
	{
		this.pointX = pointX;
	}
	
	public void setY(float pointY)
	{
		this.pointY = pointY;
	}
	
	public int getIntX()
	{
		return (int)pointX;
	}
	
	public int getIntY()
	{
		return (int)pointY;
	}
	
	public void setColor(String color)
	{
		this.color = color;
	}
	
	public String getColor()
	{
		return this.color;
	}
}
