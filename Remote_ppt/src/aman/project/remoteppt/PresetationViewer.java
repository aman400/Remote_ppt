package aman.project.remoteppt;

import java.io.File;
import java.io.IOException;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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
	private int width, height;
	static boolean presentationRunning;
	static MyHandler updateGUI;
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		updateGUI = new MyHandler();
		
		// get Data from previous activity
		Intent in = getIntent();
		this.name = in.getStringExtra("item");
		
		presentationRunning = false;
		
		// set Screen orientation to Landscape
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		ActionBar bar = getActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
		// get width and height of Screen

		Display display = getWindowManager().getDefaultDisplay();
		
		this.width = display.getWidth();
		this.height = display.getHeight();
		
		
		// connect to the presentation server
		this.scanner = new Scanner(in.getStringExtra("IP"));
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
				presentationRunning = true;
				File file = new File(Environment.getExternalStorageDirectory().getPath() + 
						File.separatorChar + "Droid Drow" + File.separatorChar + "Files" + File.separatorChar + name);
		
				// send selected file to selected PC
				try 
				{
					scanner.send.sendFile(file.getAbsolutePath(), file.length(), name, width, height);
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
				// Extraction Directory path
				extractionDirectory = new File(Environment.getExternalStorageDirectory().getPath() + 
											File.separatorChar + "Droid Drow" + File.separatorChar + "Extracted Files");			
			
				this.board = new DrawingBoard(getBaseContext(), name, extractionDirectory, this.width, this.height, this.scanner);
				this.board.setOnTouchListener(board);
				setContentView(board);
				
			}
			catch(NullPointerException ex)
			{
				ex.printStackTrace();
				Toast.makeText(getBaseContext(), "Desktop Appliction not Running", Toast.LENGTH_SHORT).show();
				super.onBackPressed();
			}
		}
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
			}
			else
			{
				if(!menu.getItem(6).isChecked())
					menu.getItem(6).setChecked(true);
				menu.setGroupEnabled(R.id.drawing_group, false);
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
				else
				{
					board.setWhiteBoardEnabled();
				}
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
				catch(NullPointerException ex)
				{
					ex.printStackTrace();
				}
				
				invalidateOptionsMenu();
				return true;
			}
			
			case R.id.black:
			{
				board.setColor("black");
				item.setChecked(true);
				return true;
			}
			case R.id.red:
			{
				board.setColor("red");
				item.setChecked(true);
				return true;
			}
			
			case R.id.blue:
			{
				board.setColor("blue");
				item.setChecked(true);
				return true;
			}
			
			case R.id.green:
			{
				board.setColor("green");
				item.setChecked(true);
				return true;
			}
			
			case R.id.white:
			{
				board.setColor("white");
				item.setChecked(true);
				return true;
			}
			
			case R.id.yellow:
			{
				board.setColor("yellow");
				item.setChecked(true);
				return true;
			}
			
			case R.id.cyan:
			{
				board.setColor("cyan");
				item.setChecked(true);
				return true;
			}
			
			case R.id.gray:
			{
				board.setColor("gray");
				item.setChecked(true);
				return true;
			}
			
			case R.id.magenta:
			{
				board.setColor("magenta");
				item.setChecked(true);
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
		try
		{
			new Thread(new Cleanup(extractionDirectory.getAbsolutePath())).start();
		}
		catch(NullPointerException exception)
		{
			exception.printStackTrace();
		}
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
			super.onBackPressed();
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
}