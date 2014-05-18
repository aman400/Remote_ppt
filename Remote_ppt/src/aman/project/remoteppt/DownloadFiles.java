package aman.project.remoteppt;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;


public class DownloadFiles extends Activity implements DialogBox.NoticeDialogListener
{
	
	private ListView list;
	private static ArrayList<MyFile> myFiles;
	private DownloadFilesAdapter adapter;
	private int selectedIndex;
	private DialogBox dialog;
	private String ip;
	private int port;
	private MyHandler handler;
	private Scanner scanner;
	private ProgressDialog pDialog, downloadDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_download_files);
		handler = new MyHandler();

		Intent in = getIntent();
		this.ip = in.getStringExtra("ip");
		this.port = in.getIntExtra("port", 5678);
				
		myFiles = new ArrayList<MyFile>();
		list = (ListView)(findViewById(R.id.download_files_list));
		
		pDialog = new ProgressDialog(this);
		pDialog.setIndeterminate(true);
		pDialog.setIcon(R.drawable.download_blue);
		pDialog.setProgressStyle(ProgressDialog.THEME_TRADITIONAL);
		pDialog.setMessage(getString(R.string.fetching) +"...");
		pDialog.setCancelable(false);
		pDialog.show();
		pDialog.setTitle(getString(R.string.loading));
		pDialog.show();
		
		scanner = new Scanner(ip, port, handler);
		Thread th = new Thread(scanner);
		th.start();
		
		try 
		{
			th.join();
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}

		scanner.send.sendMessage("$$DOWNLOAD$$");

	}
	
	@Override
	protected void onResume() 
	{
		super.onResume();
		
		adapter = new DownloadFilesAdapter(myFiles);
		list.setAdapter(adapter);
		
		list.setOnItemLongClickListener(new OnItemLongClickListener() 
		{

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int index, long arg3)
			{
				
				scanner.receive.setFilePath(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Droid Drow" + File.separator + "Files" + File.separator + myFiles.get(index).getName());
				scanner.receive.setSize(myFiles.get(index).getSize());
				
				selectedIndex = index;
				dialog = new DialogBox();
				Bundle bundle = new Bundle();
				
				bundle.putString("Title", getString(R.string.dialog_title));
				bundle.putString("Message", getString(R.string.download_message));
				bundle.putString("YES", getString(R.string.download));
				bundle.putString("NO", getString(R.string.cancel));
				
				dialog.setArguments(bundle);
				dialog.show(getFragmentManager(), "message");
				return true;
			}			
		});
	}
	
	@Override
	public void onDialogPositiveClick(DialogFragment dialog) 
	{
		downloadDialog = new ProgressDialog(this);
		downloadDialog.setTitle(getString(R.string.downloading));
		downloadDialog.setIcon(R.drawable.download_blue);
		downloadDialog.setProgress(scanner.receive.getDownloadedLength());
		downloadDialog.setProgressStyle(ProgressDialog.THEME_TRADITIONAL);
		downloadDialog.setCanceledOnTouchOutside(false);
		downloadDialog.show();
		
		scanner.send.sendMessage("$$SENDFILE$$");
		scanner.send.sendMessage(myFiles.get(selectedIndex).getName());
	}
	
	public void setProgress()
	{
		downloadDialog.setProgress(scanner.receive.getDownloadedLength());
		
		if(scanner.receive.getDownloadedLength() == 100)
			downloadDialog.dismiss();
	}
	@Override
	public void onDialogNegetiveClick(DialogFragment dialog) 
	{
		dialog.dismiss();
	}
	
	class MyHandler extends Handler
	{
		@Override
		public void handleMessage(Message message)
		{
			if(message.what == 1)
			{
				myFiles = scanner.receive.getFiles();
				adapter.setList(myFiles);
				adapter.notifyDataSetChanged();
				pDialog.dismiss();
			}
			
			if(message.what == 100)
			{
				setProgress();
			}
		}	
	}
	
	@Override
	public void onBackPressed()
	{
		Intent in = new Intent(this, FileViewer.class);
		in.putExtra("IP", this.ip);
		in.putExtra("port", this.port);
		startActivity(in);
	}

}
