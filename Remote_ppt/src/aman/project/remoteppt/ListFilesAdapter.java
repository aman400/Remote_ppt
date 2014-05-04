package aman.project.remoteppt;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ListFilesAdapter extends MyAdapter 
{
	private String[] tv1;
	ListFilesAdapter(String[] text)
	{
		this.tv1 = text;
	}
	
	@Override
	public int getCount() 
	{
		return tv1.length;
	}

	@Override
	public Object getItem(int position) 
	{
		return tv1[position];
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
		
		name.setText(tv1[position]);
		ip.setText("");
		iv.setImageResource(R.drawable.zip);
		
		return convertView;
	}
}
