package aman.project.remoteppt;

import java.io.File;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class FileViewer extends Activity implements DialogBox.NoticeDialogListener
{
	private DialogFragment dialog;
	private ListView lv;
	private File file;
	private String[] files;
	private String ip;
	private  int itemClicked, port;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_viewer);
		
		// Get ip address from previous Activity
		Intent in = getIntent();
		this.ip = in.getStringExtra("IP");
		this.port = in.getIntExtra("port", 5678);
		
		try
		{
			file = new File(Environment.getExternalStorageDirectory().getPath()+File.separatorChar+"Droid Drow"+File.separatorChar+"Files");

			files = file.list();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		// create adapter for listing files
		ListFilesAdapter myAdapter = new ListFilesAdapter(files);
		
		// get list view and add adapter to it
		lv = (ListView)(findViewById(R.id.ziplist));
		lv.setAdapter(myAdapter);
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
		lv.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int index,
					long arg3) 
			{
				dialog = new DialogBox();
				FileViewer.this.itemClicked = index;
				Bundle bundle = new Bundle();
				bundle.putString("Title", getString(R.string.dialog_title));
				bundle.putString("Message", getString(R.string.dialog_message));
				bundle.putString("YES", getString(R.string.positive_button));
				bundle.putString("NO", getString(R.string.negetive_button));
				
				dialog.setArguments(bundle);
				dialog.show(getFragmentManager(), "message");
			}

		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.file_viewer, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		try
		{
			Scanner scanner = new Scanner(this.ip, this.port);
			Thread th = new Thread(scanner);
			th.start();
			th.join();
			
			// get selected item from menu and take action accordingly
			switch(item.getItemId())
			{
				case R.id.stop_desktop_app:
					scanner.send.sendMessage("$$STOPDESKTOPAPP$$");
					super.onBackPressed();
					return true;
					
				case R.id.shutdown:
					scanner.send.sendMessage("$$SHUTDOWN$$");
					return true;
					
				case R.id.restart:
					scanner.send.sendMessage("$$RESTART$$");
					return true;
					
				case R.id.logoff:
					scanner.send.sendMessage("$$LOGOFF$$");
					return true;
					
				case R.id.hibernate:
					scanner.send.sendMessage("$$HIBERNATE$$");
					return true;
					
				default:
					return super.onOptionsItemSelected(item);
			}
		}
		catch(NullPointerException exception)
		{
			Toast.makeText(getApplicationContext(), "Desktop Application Not Running", Toast.LENGTH_SHORT).show();
		}
		catch(InterruptedException exception)
		{
			Toast.makeText(getApplicationContext(), "Desktop Application Not Running", Toast.LENGTH_SHORT).show();
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	// This function is called upon error in network connection
	public void connectionError()
	{
		Toast.makeText(getBaseContext(), "Desktop application not Running", Toast.LENGTH_SHORT).show();
		super.onBackPressed();
	}
	
	
	@Override
	public void onDialogPositiveClick(DialogFragment dialog) 
	{	
		try
		{
			Intent in = new Intent(getBaseContext(), PresetationViewer.class);
			in.putExtra("IP", this.ip);
			in.putExtra("port", this.port);
			in.putExtra("item", files[itemClicked]);
			extract(files[itemClicked]);
			startActivity(in);
		} 
		catch (InterruptedException e) 
		{
			Toast.makeText(this, "File corrupted", Toast.LENGTH_SHORT).show();
		}
		
	}

	@Override
	public void onDialogNegetiveClick(DialogFragment dialog) 
	{
		dialog.dismiss();
	}
	
	
	// This function is used to extract files to a fixed directory
	public void extract(String name) throws InterruptedException
	{
		File file = new File(Environment.getExternalStorageDirectory().getPath() + 
				File.separatorChar + "Droid Drow" + File.separatorChar + "Files" + File.separatorChar + name);
		
		// Extraction Directory path
		File extractionDirectory = new File(Environment.getExternalStorageDirectory().getPath() + 
									File.separatorChar + "Droid Drow" + File.separatorChar + "Extracted Files");
			
		// if directory does not Exist then create it.
		if(!new File(extractionDirectory.getAbsolutePath()).exists())new File(extractionDirectory.getAbsolutePath()).mkdirs();
			
		// Extract zip files to a directory.
		Thread thread = new Thread(new ZipExtractor(file.getAbsolutePath(), extractionDirectory.getAbsolutePath()));
		thread.start();
		thread.join();
	}
}
