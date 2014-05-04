package aman.project.remoteppt;

import java.io.File;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class FileViewer extends Activity implements DialogBox.NoticeDialogListener
{
	private DialogFragment dialog;
	private ListView lv;
	private File f;
	private String[] files;
	private String ip;
	private  int itemClicked;
	
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
	public void onDialogPositiveClick(DialogFragment dialog) 
	{
		Intent in = new Intent(getBaseContext(), MakeConnection.class);
		in.putExtra("IP", this.ip);
		in.putExtra("item", files[itemClicked]);
		startActivity(in);
	}

	@Override
	public void onDialogNegetiveClick(DialogFragment dialog) 
	{
		dialog.dismiss();
	}
	
}
