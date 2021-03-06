package aman.project.remoteppt;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import android.os.Handler;
import android.util.Log;

public class Scanner implements Runnable
{
	private Socket sock;
	private String ip, message = "";
	int port;
	Send send;
	Receive receive;
	private ArrayList<Server> serverList;
	private Handler handler;
	private int timeout;
	
	Scanner(String ip, int port)
	{
		this.ip = ip;
		this.port = port;
		this.timeout = 4000;
	}
	
	Scanner(String ip, ArrayList<Server> serverList, String message, int port, Handler handler)
	{
		this(ip, port, handler);
		this.serverList = serverList;
		this.message = message;
	}
	
	Scanner(String ip, int port, Handler handler)
	{
		this(ip, port);
		this.handler = handler;
	}
	
	@Override
	public void run()
	{
		
		try 
		{			
			sock = new Socket();
			sock.connect(new InetSocketAddress(this.ip, this.port), timeout);
		
			if(handler != null)
			{
				Log.d("debug", "message sent");
				if(message.equals("$$IP&HOST$$"))
				{
					send = new Send(sock, handler);
					receive = new Receive(sock, serverList, ip, send, handler);	
					new Thread(receive).start();
					send.sendMessage("$$IP&HOST$$");
				}
				
				else
				{
					receive = new Receive(sock, handler);
					send = new Send(sock, handler);
					new Thread(send).start();
					new Thread(receive).start();
				}
			}
			else
			{
				receive = new Receive(sock);		
				send = new Send(sock);
				new Thread(send).start();
				new Thread(receive).start();
			}
						
		}
		catch(SocketException ex)
		{
			ex.printStackTrace();
		}
		catch (IOException e) 
		{
			Log.d("mymessage", "server not found at "+ip);
		}
	}
	
	public ArrayList<Server> getServerList()
	{
		return this.serverList;
	}
}

