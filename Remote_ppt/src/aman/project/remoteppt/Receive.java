package aman.project.remoteppt;

import java.io.ObjectInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import android.util.Log;

//class to Receive messages
public class Receive implements Runnable
{
	private ObjectInputStream ois;	
	private ArrayList<Server> serverList;
	private String IP;
	private Send send;
	
	Receive(Socket sock)
	{
		try 
		{
			this.ois = new ObjectInputStream(sock.getInputStream());

		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	Receive(Socket sock, ArrayList<Server> serverList, String IP, Send send)
	{
		this(sock);
		this.send = send;
		this.IP = IP;
		this.serverList = serverList;
	}
	
	
	// Read messages from network
	public void run()
	{
		String msg;
		while((msg = this.receiveMessage())!=null)
		{
			// this code runs only when it gets a new connection
			if(msg.equals("$$IP&HOST$$"))
			{
			
				String serverAddress = this.receiveMessage();
				String serverName = this.receiveMessage();
				serverList.add(new Server(this.IP, serverName));
				ServerScanner.update.sendEmptyMessage(999);			
			}
			else
			{
				Log.d("mymessage", msg);
			}
		}
	}
	
	public String receiveMessage()
	{
		try
		{
			return ois.readLine();
		}
		catch(IOException ex)
		{
			 ex.printStackTrace();
		}
		return null;
	}
}