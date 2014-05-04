package aman.project.remoteppt;


public class Server 
{
	private String ip;
	private String name;
	
	Server(String ip, String name)
	{
		this.ip = ip;
		this.name = name;
	}
	
	Server()
	{}
	
	public String getServerIP()
	{
		return this.ip;
	}
	
	public String getServerName()
	{
		return this.name;
	}
	
	public void setServerIP(String ip)
	{
		this.ip = ip;
		
	}
	public void setServerName(String name)
	{
		this.name = name;
	}
	
}