package aman.project.remoteppt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

//class that handles message sending.
public class Send implements Runnable
{
	
	private ObjectOutputStream oos;
	private BufferedReader br;
	
	// Constructor for class
	Send(Socket sock)
	{
		try
		{
			this.oos = new ObjectOutputStream(sock.getOutputStream());
			this.br = new BufferedReader(new InputStreamReader(System.in));
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}
	
	Send(Socket sock, String message)
	{
		this(sock);
		sendMessage(message);
	}
	
	
	// Write messages to Network Stream
	public void run()
	{
	    String msg;
	    try 
		{
		    while((msg = br.readLine()) != null)
			{
		    	sendMessage(msg);
			}
		}
		
	    catch(UnknownHostException ex)
		{
		    ex.printStackTrace();
		}
		
	    catch (IOException e) 
		{
		    e.printStackTrace();
		}
	}
	public void sendMessage(String message)
	{
		try
		{
			oos.writeBytes(message+"\r\n");
			oos.flush();
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}
	
	public void sendlong(long data)
	{
		try 
		{
			oos.writeLong(data);
			oos.flush();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	public void sendPoints(PointHandler point)
	{
		try
		{
			this.sendMessage("$$SENDINGPOINTS$$");
			
			this.oos.writeFloat(point.getX());
			oos.flush();
			
			this.oos.writeFloat(point.getY());
			oos.flush();
			
			this.sendMessage(point.getColor());
		}
		catch(IOException exception)
		{
			exception.printStackTrace();
		}
	}
	
	public void sendFile(String path, long length, String fileName, int width, int height) throws IOException
	{
		try
		{
			oos.writeBytes("$$PROJECT$$\r\n");
			oos.flush();
			oos.writeInt(width);
			oos.flush();
			oos.writeInt(height);
			oos.flush();
			oos.writeBytes(fileName+"\r\n");
			oos.flush();
			oos.writeLong(length);
			oos.flush();
			
			Thread th = new Thread(new FileTransfer(path, oos));
			th.start();
			
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}
}