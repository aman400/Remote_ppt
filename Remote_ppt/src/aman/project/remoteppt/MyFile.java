package aman.project.remoteppt;

public class MyFile
{
	private String name;
	private long size;
	MyFile(String name, long size)
	{
		this.name = name;
		this.size = size;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public long getSize()
	{
		return this.size;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
}
