package aman.project.remoteppt;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Formatter;
import android.util.Log;
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
	private static ArrayList<Server> serverList;
	private ServerListAdapter adapter;
	private ListView list;
	private UpdateGUI update;
	private ProgressDialog  dialog;
	private Thread scanningThread;

	protected void onCreate(Bundle savedInstance)
	{
		super.onCreate(savedInstance);
		this.setContentView(R.layout.activity_server_list);	
	}
	
	protected void onStart()
	{
		super.onStart();	
	}
	
	protected void onResume()
	{
		super.onResume();
		
		serverList = new ArrayList<Server>();
		this.update = new UpdateGUI();
		list = (ListView)(findViewById(R.id.list_of_servers));
		adapter = new ServerListAdapter(serverList);
		list.setAdapter(adapter);
		list.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position,
					long arg3) 
			{
				Toast.makeText(getBaseContext(), serverList.get(position).getServerName(), Toast.LENGTH_SHORT).show();	

					Intent viewFiles = new Intent(getBaseContext(), FileViewer.class);
					viewFiles.putExtra("IP",serverList.get(position).getServerIP());
					viewFiles.putExtra("port", port);
					startActivity(viewFiles);				
			}
			
		});
		
		dialog = new ProgressDialog(this);
		dialog.setIndeterminate(true);
		dialog.setMessage("Scanning...");
		dialog.setCancelable(true);
		dialog.setProgressStyle(ProgressDialog.THEME_HOLO_DARK);
		dialog.show();
		
		scanningThread = new Thread(new scanningThread());
		scanningThread.start();
	}
	
	class scanningThread implements Runnable
	{
		
		scanningThread()
		{
			// Get ip address of android device
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
							ip = "192.168.43.1";
						}
						else
						{
							ip = Formatter.formatIpAddress(wifi.getConnectionInfo().getIpAddress());
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
			
			network = ip.substring(0, ip.lastIndexOf("."));
		}
		
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
					scanners[j] = new Scanner(network+"."+ host, serverList, "$$IP&HOST$$", port, update);
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
						Thread.currentThread().interrupt();
					}
				}				
			}
				
			for(int i = 250; i < 256; i++)
			{
				Scanner scan = new Scanner(network+"." + i, serverList, "$$IP&HOST$$", port, update);
				Thread thread = new Thread(scan);
				thread.start();
				try 
				{
					thread.join();
				} 
				catch (InterruptedException e) 
				{
					Thread.currentThread().interrupt();
					return;
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
			}
			else if(msg.what == 999)
			{
				adapter.notifyDataSetChanged();
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
	protected void onPause() 
	{
		scanningThread.interrupt();
		serverList.clear();
		update.sendEmptyMessage(999);
		super.onPause();
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
				if(scanningThread.isAlive())
					scanningThread.interrupt();
				
				scanningThread = new Thread(new scanningThread());
				scanningThread.start();
				return true;
			}
			default :
				return super.onOptionsItemSelected(item);
		}
		
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