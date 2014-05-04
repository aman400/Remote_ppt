package aman.project.remoteppt;

import java.io.File;

public class Cleanup implements Runnable
{	
	private String path;
	
	Cleanup(String path)
	{
		this.path = path;
	}
	public void delete(String path)
	{
		File file = new File(path);
		if(file.isFile())
		{
			file.delete();
		}
		
		if(file.isDirectory())
		{
			String[] files = file.list();
			for(String f : files)
			{
				delete(file.getAbsolutePath() + File.separatorChar + f);
			}
			file.delete();
		}
	}

	@Override
	public void run() 
	{
		delete(this.path);
	}
}