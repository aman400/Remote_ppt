package aman.project.remoteppt;

import android.widget.BaseAdapter;
abstract class MyAdapter extends BaseAdapter 
{
	@Override
	public long getItemId(int id) 
	{
		return id * 87;
	}
}