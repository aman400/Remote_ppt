package aman.project.remoteppt;

import java.util.ArrayList;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ServerListAdapter extends MyAdapter 
{
	ArrayList<Server> serverList;
	ServerListAdapter(ArrayList<Server> serverList)
	{
		this.serverList = serverList;
	}
	@Override
	public int getCount() 
	{
		return serverList.size();
	}

	@Override
	public Object getItem(int position) 
	{
		return serverList.get(position);
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
		iv.setImageResource(R.drawable.server);
		
		
		name.setText(serverList.get(position).getServerName());
		ip.setText(serverList.get(position).getServerIP());
		
		return convertView;
	}
}
