package aman.project.remoteppt;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewSwitcher.ViewFactory;

public class MakeConnection extends Activity implements DialogBox.NoticeDialogListener, OnTouchListener, ViewFactory
{
	private Scanner scanner;
	private String IP, name;
	private DialogFragment dialog;
	static boolean flag = false;
	private File extractionDirectory;
	private ImageSwitcher presentation;
	private int index = 0, height, width;
	private ArrayList<Uri> uri;
	private float xBeforeTouch, xAfterTouch;
	private VelocityTracker tracker;
	private boolean whiteBoardEnabled;
	private ArrayList<PointHandler> points;
	private String color;
	private Display display;
	
	protected void onCreate(Bundle savedInstanceState)	
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.make_connection);
		tracker = VelocityTracker.obtain();
		whiteBoardEnabled = false;
		presentation = (ImageSwitcher)(findViewById(R.id.presentation));
		presentation.setFactory(this);
		
		View v = (View)findViewById(R.id.View);
		v.setOnTouchListener(this);		
		
		display = getWindowManager().getDefaultDisplay();
		height = display.getHeight();
		width = display.getWidth();
	}
	
	protected void onStart()
	{
		try 
		{
			super.onStart();
		
			Intent in = getIntent();
			IP = in.getStringExtra("IP");
			name = in.getStringExtra("item");
			
			scanner = new Scanner(IP);
			Thread th = new Thread(scanner);
			th.start();
		
			th.join();		
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.make_connection, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case R.id.whiteboard:
				this.whiteBoardEnabled = !this.whiteBoardEnabled;
				
				if(whiteBoardEnabled)
				{
					this.points = new ArrayList<PointHandler>();
					this.setColor("black");
					scanner.send.sendMessage("$$OPENWHITEBOARD$$");
					Toast.makeText(getBaseContext(), "Drawing Enabled", Toast.LENGTH_SHORT).show();
				}
				else
				{
					scanner.send.sendMessage("$$CLOSEWHITEBOARD$$");
					Toast.makeText(getBaseContext(), "Swipe Enabled", Toast.LENGTH_SHORT).show();
				}
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		try
		{
			if(!flag)
			{
				MakeConnection.flag = true;
				
				File file = new File(Environment.getExternalStorageDirectory().getPath() + 
										File.separatorChar + "Droid Drow" + File.separatorChar + "Files" + File.separatorChar + name);
		
				scanner.send.sendFile(file.getAbsolutePath(), file.length(), name, width, height);
				
				// Extraction Directory path
				extractionDirectory = new File(Environment.getExternalStorageDirectory().getPath() + 
												File.separatorChar + "Droid Drow" + File.separatorChar + "Extracted Files");
				
				// if directory does not Exist then create it.
				if(!new File(extractionDirectory.getAbsolutePath()).exists())new File(extractionDirectory.getAbsolutePath()).mkdirs();
				
				// Extract zip files to a directory.
				Thread th = new Thread(new ZipExtractor(file.getAbsolutePath(), extractionDirectory.getAbsolutePath()));
				th.start();
				th.join();
				
				File f = new File(extractionDirectory.getAbsolutePath() + File.separatorChar + name);
				
				String[] fileNames = f.list();
				
				uri = new ArrayList<Uri>();
				for(int i = 0; i < fileNames.length; i++)
				{	
					File picture = new File(f.getAbsolutePath() + File.separatorChar + i);
					uri.add(Uri.parse(picture.getAbsolutePath()));
				}
				this.setImage();
				
			}
		}
		catch(InterruptedException ex)
		{
			scanner.send.sendMessage("$$CLOSEPROJECTION$$");
			super.onBackPressed();
			
		}
		catch(NullPointerException ex)
		{
			ex.printStackTrace();
			Toast.makeText(getBaseContext(), "Desktop Appliction not Running", Toast.LENGTH_LONG).show();
			super.onBackPressed();
		}
		catch(IOException ex)
		{
			scanner.send.sendMessage("$$CLOSEPROJECTION$$");
			super.onBackPressed();
		}
	}
	
	
	public void incrementIndex()
	{
		if(index < uri.size() - 1)
		{
			presentation.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_right));
			presentation.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_left));
			this.index++;
			this.scanner.send.sendMessage("$$NEXTSLIDE$$");
			this.setImage();
		}	
		
	}
	
	public void decrementIndex()
	{
		if(index > 0)
		{
			presentation.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_left));
			presentation.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_right));
			this.index--;
			this.scanner.send.sendMessage("$$PREVIOUSSLIDE$$");
			this.setImage();
		}
	}
	
	private void setImage()
	{
		try
		{
			presentation.setImageURI(uri.get(index));
		}
		catch(IndexOutOfBoundsException exception)
		{
			super.onBackPressed();
		}
	}
	
	@Override
	public void onBackPressed()
	{
		dialog = new DialogBox();
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
			MakeConnection.flag = false;	
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
	
	public void checkSwipeAction(float x1, float x2)
	{
		
		if (Math.abs(x2 - x1) > 100)
		{
			
			if(x1 < x2)
				this.decrementIndex();
			
			if(x2 < x1)
				this.incrementIndex();
		}
	}
	
	
	
	@Override
	public boolean onTouch(View v, MotionEvent event) 
	{
		PointHandler point;
		
		if(!whiteBoardEnabled)
		{
			switch(event.getAction())
			{
				case MotionEvent.ACTION_DOWN:
				{
					tracker.addMovement(event);
					xBeforeTouch = event.getX();
					return(true);
				}
				
				case MotionEvent.ACTION_MOVE:
				{
					tracker.addMovement(event);
					return(true);
				}
				
			
				case MotionEvent.ACTION_UP:
				{
					tracker.addMovement(event);
					tracker.computeCurrentVelocity(5);
					xAfterTouch = event.getX();
					if(Math.abs(tracker.getXVelocity()) > 5)
					{
						tracker.recycle();
						
						checkSwipeAction(xBeforeTouch, xAfterTouch);
					}
					return true;	
				}
				default:
					return super.onTouchEvent(event);
			}	
		}
		else
		{
			switch(event.getAction())
			{
				
				case MotionEvent.ACTION_DOWN :
				{
					point = new PointHandler(event.getX(), event.getY(), this.color);
					points.add(point);
					scanner.send.sendPoints(point);
					return true;
				}
				
				case MotionEvent.ACTION_MOVE :
				{
					point = new PointHandler(event.getX(), event.getY(), this.color);
					points.add(point);
					scanner.send.sendPoints(point);			
					
//					invalidate();
					
					return true;
				}
				case MotionEvent.ACTION_UP :
				{
					
					point = new PointHandler(0,0);
					scanner.send.sendPoints(point);

					points.add(point);
					
					return true;
				}
				
				default:
				{
					return super.onTouchEvent(event);
				}
			}
		}
	}
	
	public void setColor(String color)
	{
		this.color = color;
	}
	
	@Override
	public View makeView() 
	{
		ImageView image = new ImageView(this);
		image.setBackgroundColor(Color.BLACK);
		image.setScaleType(ImageView.ScaleType.FIT_CENTER);
		image.setLayoutParams(new ImageSwitcher.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		return image;
	}
}
