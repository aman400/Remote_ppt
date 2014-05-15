package aman.project.remoteppt;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import android.app.ProgressDialog;

public class FileTransfer implements Runnable
{
	private byte[] buffer;
	private FileInputStream fis;
	private ObjectOutputStream oos;
	private ProgressDialog pd;
	
	FileTransfer(String path, ObjectOutputStream oos, ProgressDialog pd)
	{
		try
		{
			this.pd = pd;
			this.buffer = new byte[102400];
			this.oos = oos;
			this.fis = new FileInputStream(path);
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}
	
	@Override
	public void run() 
	{
		try
		{
			try
			{
				int count;
				while((count = fis.read(buffer, 0, 102400)) != -1)
				{
					oos.write(buffer, 0, count);
					oos.flush();
				}
				fis.close();
				pd.dismiss();
			}
			catch(EOFException exception)
			{
				fis.close();
			}
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}
}
