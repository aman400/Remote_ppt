package aman.project.remoteppt;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class ServerScanner extends Activity
{
	private String network;
	private int port;
	private ArrayList<Server> serverList;
	private ServerListAdapter adapter;
	private ListView list;
	static UpdateGUI update;
	ProgressDialog  dialog;

	protected void onCreate(Bundle savedInstance)
	{
		super.onCreate(savedInstance);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);  
		setProgressBarIndeterminateVisibility(true); 
		
		this.setContentView(R.layout.activity_server_list);
		
		ServerScanner.update = new UpdateGUI();
		serverList = new ArrayList<Server>();
		Intent in = getIntent();
		this.network = in.getStringExtra("network");
		this.port = in.getIntExtra("port", 5678);		
	}
	
	protected void onStart()
	{
		super.onStart();

		list = (ListView)(findViewById(R.id.list_of_servers));
		adapter = new ServerListAdapter(serverList);
		list.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position,
					long arg3) 
			{
				Toast.makeText(getBaseContext(), serverList.get(position).getServerName(), Toast.LENGTH_LONG).show();	

					Intent viewFiles = new Intent(getBaseContext(), FileViewer.class);
					viewFiles.putExtra("IP",serverList.get(position).getServerIP());
					startActivity(viewFiles);
					
			}

		});
		
		dialog = new ProgressDialog(this);
		dialog.setIndeterminate(true);
		dialog.setMessage("Scanning");
		dialog.setCancelable(true);
		dialog.show();
		new Thread(new Runnable()
		{
			public void run()
			{
				// Different threads that run to check for availability of any server 
				Scanner[] scanners = new Scanner[25];
				Thread[] threads = new Thread[25];
					for(int i = 0; i < 10; i++)
					{
						for( int j = 0; j < 25; j++)
						{
							int host = (i * 25) + j;
							scanners[j] = new Scanner(network+"."+ host, serverList, "$$IP&HOST$$");
							threads[j] = new Thread(scanners[j]);
							threads[j].start();
						}
						for(int j = 0; j < 25; j++)
						{
							try 
							{
								threads[j].join();
							} 
							catch (InterruptedException e) 
							{
								e.printStackTrace();
							}
						}
					}
					
					for(int i = 250; i < 256; i++)
					{
						Scanner scan = new Scanner(network+"." + i, serverList, "$$IP&HOST$$");
						Thread thread = new Thread(scan);
						thread.start();
						try 
						{
							thread.join();
						} 
						catch (InterruptedException e) 
						{
							e.printStackTrace();
						}
					}
					update.sendEmptyMessage(99);
					dialog.dismiss();
				}
		}).start();
	}
	
	protected void onResume()
	{
		super.onResume();
		setProgressBarIndeterminateVisibility(true);
	}
	
	
	// Handler to update GUI
	class UpdateGUI extends Handler
	{
		@Override
		public void handleMessage(Message msg)
		{
			
			if(msg.what == 99)
			{
				Toast.makeText(getBaseContext(), serverList.size()+" Servers Found", Toast.LENGTH_LONG).show();
				setProgressBarIndeterminateVisibility(false);
			}
			else if(msg.what == 999)
			{
				if(list.getAdapter() == null)
				{
					list.setAdapter(adapter);
				}
				
				adapter.notifyDataSetInvalidated();
			}

		}
	}	
}