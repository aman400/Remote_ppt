package aman.project.remoteppt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.ProgressDialog;
import android.util.Log;

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
	
	public void sendLong(long data)
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
	
	public void sendInt(int number)
	{
		try
		{
			oos.writeInt(number);
			oos.flush();
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}
	
	public void sendFile(String path, long length, String fileName, int width, int height, ProgressDialog pd) throws IOException, InterruptedException
	{
		Log.d("debug", "sending file");
		this.sendInt(width);
		this.sendInt(height);
		this.sendMessage(fileName);
		this.sendLong(length);
		
		Thread th = new Thread(new FileTransfer(path, oos, pd));
		th.start();
	}
}