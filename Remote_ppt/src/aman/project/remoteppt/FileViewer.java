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
	private File f;
	private String[] files;
	private String ip;
	private  int itemClicked;
	private Scanner scanner;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_viewer);
		Intent in = getIntent();
		this.ip = in.getStringExtra("IP");
		
		try
		{
			f = new File(Environment.getExternalStorageDirectory().getPath()+File.separatorChar+"Droid Drow"+File.separatorChar+"Files");
			Log.d("path", f.getAbsolutePath());
			files = f.list();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		ListFilesAdapter myAdapter = new ListFilesAdapter(files);
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
				Bundle b = new Bundle();
				b.putString("Title", getString(R.string.dialog_title));
				b.putString("Message", getString(R.string.dialog_message));
				b.putString("YES", getString(R.string.positive_button));
				b.putString("NO", getString(R.string.negetive_button));
				
				dialog.setArguments(b);
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
			switch(item.getItemId())
			{
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
			connectionError();
		}
		return super.onOptionsItemSelected(item);
	}
	
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
