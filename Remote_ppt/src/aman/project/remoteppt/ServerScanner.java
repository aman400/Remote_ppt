package aman.project.remoteppt;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Formatter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class ServerScanner extends Activity implements DialogBox.NoticeDialogListener
{
	private String network, ip;
	private final int port = 5678;
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
		
		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		Method[] wmMethods = wifi.getClass().getDeclaredMethods();
		for(Method method: wmMethods)
		{
			if(method.getName().equals("isWifiApEnabled")) 
			{

				try 
				{
					if((Boolean)method.invoke(wifi))
					{
						this.ip = "192.168.43.1";
					}
					else
					{
						this.ip = Formatter.formatIpAddress(wifi.getConnectionInfo().getIpAddress());
					}
		
				}
				catch (IllegalArgumentException e) 
				{
					e.printStackTrace();
				} 
				catch (IllegalAccessException e) 
				{
					e.printStackTrace();
				} 
				catch (InvocationTargetException e) 
				{
					e.printStackTrace();
				}

	     	}
		}
		
		this.network = ip.substring(0, ip.lastIndexOf("."));
		
		ServerScanner.update = new UpdateGUI();
		serverList = new ArrayList<Server>();	
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
				Toast.makeText(getBaseContext(), serverList.get(position).getServerName(), Toast.LENGTH_SHORT).show();	

					Intent viewFiles = new Intent(getBaseContext(), FileViewer.class);
					viewFiles.putExtra("IP",serverList.get(position).getServerIP());
					startActivity(viewFiles);				
			}
			
		});
		
		dialog = new ProgressDialog(this);
		dialog.setIndeterminate(true);
		dialog.setMessage("Scanning...");
		dialog.setCancelable(true);
		dialog.show();
		
		serverList.clear();
		new Thread(new scanningThread()).start();
	}
	
	protected void onResume()
	{
		super.onResume();
		setProgressBarIndeterminateVisibility(true);
	}
	
	class scanningThread implements Runnable
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
				update.sendEmptyMessage(12);
			}	
		}
	
	// Handler to update GUI
	class UpdateGUI extends Handler
	{
		@Override
		public void handleMessage(Message msg)
		{
			
			if(msg.what == 99)
			{
				Toast.makeText(getBaseContext(), serverList.size()+" Servers Found", Toast.LENGTH_SHORT).show();
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
			else if(msg.what == 11)
			{
				if(!dialog.isShowing())
					dialog.show();
			}
			else if(msg.what == 12)
			{
				if(dialog.isShowing())
					dialog.dismiss();
			}

		}
	}

	@Override
	public void onBackPressed()
	{
		DialogBox dialogBox = new DialogBox();

		Bundle bundle = new Bundle();
		bundle.putString("Title", getString(R.string.dialog_title));
		bundle.putString("Message", getString(R.string.activity_destroy_message));
		bundle.putString("YES", getString(R.string.positive_button));
		bundle.putString("NO", getString(R.string.negetive_button));
		
		dialogBox.setArguments(bundle);
		dialogBox.show(getFragmentManager(), "message");
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.server_list, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		switch(item.getItemId())
		{
			case R.id.scan:
			{
				dialog.show();
				serverList.clear();
				new Thread(new scanningThread()).start();
				return true;
			}
			default :
				return super.onOptionsItemSelected(item);
		}
		
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) 
	{
		super.onConfigurationChanged(newConfig);
	}
	
	@Override
	public void onDialogPositiveClick(DialogFragment dialog)
	{
		super.onDestroy();
		this.finish();
	}

	@Override
	public void onDialogNegetiveClick(DialogFragment dialog) 
	{
		dialog.dismiss();
	}	
}