package aman.project.remoteppt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;

import android.os.Handler;
import android.util.Log;

//class to Receive messages
public class Receive implements Runnable
{
	private ObjectInputStream ois;	
	private ArrayList<Server> serverList;
	private String IP, path;
	private Send send;
	private ArrayList<MyFile> myFiles;
	private Handler handler;
	private long size, receivedLength;
	private boolean isReceving;
	
	Receive(Socket sock)
	{
		myFiles = new ArrayList<MyFile>();
		try 
		{
			this.ois = new ObjectInputStream(sock.getInputStream());
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	Receive(Socket sock, ArrayList<Server> serverList, String IP, Send send, Handler handler)
	{
		this(sock, handler);
		this.send = send;
		this.IP = IP;
		this.serverList = serverList;
	}
	
	
	public Receive(Socket sock, Handler handler) 
	{
		this(sock);
		this.handler = handler;
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
				handler.sendEmptyMessage(999);
				break;
			}
			
			else if(msg.equals("$$DOWNLOAD$$"))
			{
				handler.sendEmptyMessage(1);
			}
			
			else if(msg.equals("$$FILE$$"))
			{
				this.myFiles.add(new MyFile(this.receiveMessage(), this.readLong()));
			}
			
			else if(msg.equals("$$SENDINGFILE$$"))
			{
				Log.d("file", "receiving");
				receiveFile(path, size);
			}
			
			else
			{
				Log.d("mymessage", msg);
			}
		}
	}
	
	public void receiveFile(String path, long length)
	{
		isReceving = true;
		receivedLength = 0;
		int count = 0;
		byte[] buffer = new byte[1000];
		
		try
		{	
			// Delete existing directories
			if(new File(path).exists())new File(path).delete();
			
			FileOutputStream fos = new FileOutputStream(path);
			
			// Read data from network and write to file
			while((count = ois.read(buffer)) != -1)
			{		
				fos.write(buffer, 0, count);
				receivedLength += count;
				handler.sendEmptyMessage(100);

				if(length == receivedLength || !isReceving)
					break;
			}
			fos.close();
		}
		
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public int getDownloadedLength()
	{
		return (int)((this.receivedLength * 100) / size);
	}
	
	public ArrayList<MyFile> getFiles()
	{
		return this.myFiles;
	}
	
	public void setFilePath(String path)
	{
		this.path = path;
	}
	
	public void setSize(long size)
	{
		this.size = size;
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
	
	public int readInt()
	{
		try
		{
			return ois.readInt();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		return -1;
	}
	
	public long readLong()
	{
		try 
		{
			return ois.readLong();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		return -1;
	}	
	
	public void interruptReceiving()
	{
		isReceving = false;
	}
}