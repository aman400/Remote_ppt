package aman.project.remoteppt;

import java.io.File;
import java.io.IOException;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class PresetationViewer extends Activity implements DialogBox.NoticeDialogListener
{
	private DrawingBoard board;
	private String name;
	private File extractionDirectory;
	private Scanner scanner;
	private int width, height, port;
	static boolean presentationRunning;
	static MyHandler updateGUI;
	private static int colorindex;
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		updateGUI = new MyHandler();
		colorindex = 6;
		// get Data from previous activity
		Intent in = getIntent();
		this.name = in.getStringExtra("item");
		this.port = in.getIntExtra("port", 5678);
		
		presentationRunning = false;
		
		ActionBar bar = getActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
		// get width and height of Screen

		Display display = getWindowManager().getDefaultDisplay();
		
		this.width = display.getWidth();
		this.height = display.getHeight();
		
		
		// connect to the presentation server
		this.scanner = new Scanner(in.getStringExtra("IP"), port);
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
	}
	
	public void onResume()
	{
		super.onResume();
		if(!presentationRunning)
		{
			try
			{			
				// Extraction Directory path
				extractionDirectory = new File(Environment.getExternalStorageDirectory().getPath() + 
											File.separatorChar + "Droid Drow" + File.separatorChar + "Extracted Files");
				
				ProgressDialog pd = new ProgressDialog(this);
				pd.setCancelable(false);
				pd.setProgressStyle(ProgressDialog.THEME_HOLO_DARK);
				pd.setIndeterminate(true);
				pd.setMessage("Preparing desktop...");
				pd.setButton(ProgressDialog.BUTTON_NEGATIVE, "Cancel", new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) 
					{
						scanner.send.interruptFileTransfer();
						PresetationViewer.this.cleanupAndExit();
					}
				});
				
				File file = new File(Environment.getExternalStorageDirectory().getPath() + 
						File.separatorChar + "Droid Drow" + File.separatorChar + "Files" + File.separatorChar + name);
		
				// send selected file to selected PC
				try 
				{
					if(!presentationRunning)
					{
						presentationRunning = true;
						scanner.send.sendFile(file.getAbsolutePath(), file.length(), name, width, height, pd);
					}
					pd.show();
				}
				catch (IOException e) 
				{
					e.printStackTrace();
					super.onBackPressed();
				}
				catch(InterruptedException exception)
				{
					exception.printStackTrace();
					super.onBackPressed();
				}			
			
				this.board = new DrawingBoard(getBaseContext(), name, extractionDirectory, this.width, this.height, this.scanner);
				this.board.setOnTouchListener(board);
				setContentView(board);
				
			}
			catch(NullPointerException ex)
			{
				this.connectionError();
			}
		}
	}
	
	public void connectionError()
	{
		Toast.makeText(getBaseContext(), "Desktop application not Running", Toast.LENGTH_SHORT).show();
		new Thread(new Cleanup(extractionDirectory.getAbsolutePath())).start();
		super.onBackPressed();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.presentation_viewer, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		try
		{			
			if(board.isWhiteBoardEnabled())
			{
				menu.setGroupEnabled(R.id.drawing_group, true);
				menu.getItem(colorindex).setChecked(true);
			}
			
			else
			{
				if(!menu.getItem(6).isChecked() && !board.isWhiteBoardEnabled())
				{
					menu.getItem(6).setChecked(true);
				}
				menu.setGroupEnabled(R.id.drawing_group, false);
				menu.getItem(1).setEnabled(false);
			}
			
			if(board.getUndoPointsSize() > 0)
			{
				menu.getItem(1).setEnabled(true);
				menu.getItem(1).setIcon(R.drawable.eraser);
			}
			
			else
			{
				menu.getItem(1).setEnabled(false);
				menu.getItem(1).setIcon(R.drawable.eraser_black);
				
			}
		}
		catch(NullPointerException exception)
		{
			super.onBackPressed();
		}
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case R.id.whiteboard:
			{
				if(board.isWhiteBoardEnabled())
				{
					board.setWhiteBoardDisabled();
				}
				else if(!board.isWhiteBoardEnabled())
				{
					board.setWhiteBoardEnabled();
					colorindex = 6;
				}
				invalidateOptionsMenu();
				return true;
			}
			
			case R.id.undo:
			{
				try
				{
					scanner.send.sendMessage("$$UNDO$$");
					scanner.send.sendInt(board.getLastUndoPoint());
					board.undo();
				}
				catch(ArrayIndexOutOfBoundsException exception)
				{
					invalidateOptionsMenu();
				}
				catch(NullPointerException ex)
				{
					invalidateOptionsMenu();
				}
				
				invalidateOptionsMenu();
				return true;
			}
			
			case R.id.black:
			{
				board.setColor("black");
				item.setChecked(true);
				colorindex = 6;
				return true;
			}
			case R.id.red:
			{
				board.setColor("red");
				item.setChecked(true);
				colorindex = 2;
				return true;
			}
			
			case R.id.blue:
			{
				board.setColor("blue");
				item.setChecked(true);
				colorindex = 3;
				return true;
			}
			
			case R.id.green:
			{
				board.setColor("green");
				item.setChecked(true);
				colorindex = 4;
				return true;
			}
			
			case R.id.white:
			{
				board.setColor("white");
				item.setChecked(true);
				colorindex = 5;
				return true;
			}
			
			case R.id.yellow:
			{
				board.setColor("yellow");
				item.setChecked(true);
				colorindex = 7;
				return true;
			}
			
			case R.id.cyan:
			{
				board.setColor("cyan");
				item.setChecked(true);
				colorindex = 8;
				return true;
			}
			
			case R.id.gray:
			{
				board.setColor("gray");
				item.setChecked(true);
				colorindex = 9;
				return true;
			}
			
			case R.id.magenta:
			{
				board.setColor("magenta");
				item.setChecked(true);
				colorindex = 10;
				return true;
			}
			default:
				return super.onOptionsItemSelected(item);	
		}
	}	
	
	@Override
	public void onBackPressed()
	{
		DialogBox dialog = new DialogBox();

		Bundle bundle = new Bundle();
		bundle.putString("Message", getString(R.string.confirm_exit));
		bundle.putString("Title", getString(R.string.dialog_title));
		bundle.putString("YES", getString(R.string.positive_button));
		bundle.putString("NO", getString(R.string.negetive_button));
		
		dialog.setArguments(bundle);
		dialog.show(getFragmentManager(), "dialog");
	}
	
	@Override
	public void onDialogPositiveClick(DialogFragment dialog) 
	{
		try
		{
			presentationRunning = false;
			scanner.send.sendMessage("$$CLOSEPROJECTION$$");
			this.cleanupAndExit();
		} 
		catch(NullPointerException ex)
		{
			super.onBackPressed();
		}
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
			if(message.what == 10)
			{
				invalidateOptionsMenu();
			}
		}
	}
	
	public void cleanupAndExit()
	{
		new Thread(new Cleanup(extractionDirectory.getAbsolutePath())).start();
		super.onBackPressed();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
		{
			presentationRunning = true;
		}
		if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
			presentationRunning = true;
	}
}