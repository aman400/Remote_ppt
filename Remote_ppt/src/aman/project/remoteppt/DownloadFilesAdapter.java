package aman.project.remoteppt;

import java.util.ArrayList;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class DownloadFilesAdapter extends MyAdapter 
{

	private ArrayList<MyFile> myFiles;
	DownloadFilesAdapter(ArrayList<MyFile> myFiles)
	{
		this.myFiles = myFiles;
	}
	
	public void setList(ArrayList<MyFile> myFiles)
	{
		this.myFiles = myFiles;	
	}
	
	@Override
	public int getCount() 
	{
		return myFiles.size();
	}

	@Override
	public Object getItem(int position) 
	{
		return myFiles.get(position);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		if(convertView == null)
		{
			LayoutInflater inflater = LayoutInflater.from(parent.getContext());
			convertView = inflater.inflate(R.layout.single_row, parent, false);
		}
			
		TextView name = (TextView)(convertView.findViewById(R.id.name_field));
		TextView ip = (TextView)(convertView.findViewById(R.id.ip_field));
		ImageView iv = (ImageView)(convertView.findViewById(R.id.image));
		
		name.setText(myFiles.get(position).getName());
		double size =(double)((double) (myFiles.get(position).getSize() / 1000 )/ 1024);
		ip.setText( String.format("%.2f", size) + " MB");
		iv.setImageResource(R.drawable.zip);
		
		return convertView;
	}
}
