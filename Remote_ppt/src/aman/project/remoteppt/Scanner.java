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
	
	Scanner(String ip, int port)
	{
		this.ip = ip;
		this.port = port;
	}
	
	Scanner(String ip, ArrayList<Server> serverList, String message, int port, Handler handler)
	{
		this(ip, port, handler);
		this.serverList = serverList;
		this.message = message;
	}
	
	Scanner(String ip, int port, Handler handler)
	{
		this.ip = ip;
		this.port = port;
		this.handler = handler;
	}
	
	@Override
	public void run()
	{
		
		try 
		{			
			sock = new Socket();
			sock.connect(new InetSocketAddress(this.ip, this.port), 3000);
		
			if(message.equals("$$IP&HOST$$"))
			{
				receive = new Receive(sock, serverList, ip, send, handler);	
				send = new Send(sock, message, handler);
				new Thread(receive).start();

			}
			else if(handler != null)
			{
				receive = new Receive(sock, handler);
				send = new Send(sock, handler);
				new Thread(send).start();
				new Thread(receive).start();
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

