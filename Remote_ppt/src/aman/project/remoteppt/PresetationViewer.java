package aman.project.remoteppt;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
import android.widget.Toast;

public class PresetationViewer extends Activity implements DialogBox.NoticeDialogListener
{
	private DrawingBoard board;
	private String name;
	private File extractionDirectory;
	private Scanner scanner;
	private int width, height;
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		// get Data from previous activity
		Intent in = getIntent();
		this.name = in.getStringExtra("item");
		
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
	
	public void onStart()
	{
		super.onStart();
		try
		{
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
			}
				
			// Extraction Directory path
			extractionDirectory = new File(Environment.getExternalStorageDirectory().getPath() + 
										File.separatorChar + "Droid Drow" + File.separatorChar + "Extracted Files");
				
			// if directory does not Exist then create it.
			if(!new File(extractionDirectory.getAbsolutePath()).exists())new File(extractionDirectory.getAbsolutePath()).mkdirs();
				
			// Extract zip files to a directory.
			Thread thread = new Thread(new ZipExtractor(file.getAbsolutePath(), extractionDirectory.getAbsolutePath()));
			thread.start();
			thread.join();
		 
		
		
			this.board = new DrawingBoard(getBaseContext(), name, extractionDirectory, this.width, this.height, this.scanner);
			this.board.setOnTouchListener(board);
			setContentView(board);
			
		}
		catch(InterruptedException ex)
		{
			scanner.send.sendMessage("$$CLOSEPROJECTION$$");
			super.onBackPressed();
			
		}
		catch(NullPointerException ex)
		{
			ex.printStackTrace();
			Toast.makeText(getBaseContext(), "Desktop Appliction not Running", Toast.LENGTH_SHORT).show();
			super.onBackPressed();
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
			
			case R.id.eraser:
			{
				board.setColor("white");
				item.setChecked(true);
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
	
}

class DrawingBoard extends View implements OnTouchListener
{
	private File[] slides;
	private ArrayList<String> slidesPath;
	private Bitmap bitmap;
	private Paint paint;
	private int width, height, index;
	private float xBeforeTouch, xAfterTouch;
	private VelocityTracker tracker;
	private boolean whiteBoardEnabled;
	private String color;
	private Scanner scanner;
	private ArrayList<PointHandler> points;
	private Context context;

	public DrawingBoard(Context context, String name, File extractionDirectory, int width, int height, Scanner scanner) 
	{
		super(context);
		this.context = context;
		index = 0;
		this.setColor("black");
		
		this.scanner = scanner;
		
		slidesPath = new ArrayList<String>();
		slides = (new File(extractionDirectory.getAbsolutePath() + File.separatorChar + name)).listFiles();
		
		
		// setting paint Brush for drawing on canvas
		paint = new Paint();
		paint.setStrokeWidth(3);
		
		// Drawing list
		points = new ArrayList<PointHandler>();

		// Getting canvas Width and Height
		this.width = width;
		this.height = height;
		
		this.whiteBoardEnabled = false;
		
		// Velocity tracker for calculating sliding velocity
		tracker = VelocityTracker.obtain();
		
		// creating slides ArrayList
		for(File slide : slides)
		{
			slidesPath.add(slide.getAbsolutePath());
		}
		
		// Bitmap for drawing images on canvas
		bitmap = BitmapFactory.decodeFile(slidesPath.get(index));
		bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
		invalidate();
	}
	
	@Override
	public void onDraw(Canvas canvas)
	{
		canvas.drawBitmap(bitmap, 0, 0, paint);
		if(whiteBoardEnabled)
		{
			for( int i = 0;  i < points.size() - 1; i++ )
			{
				if((points.get(i).getX() == 0.0f && points.get(i).getY() == 0.0f) || (points.get(i + 1).getX() == 0.0f && points.get(i + 1).getY() == 0.0f))
						continue;					
			
				else
				{
					paint.setColor(Color.parseColor(points.get(i).getColor()));
					canvas.drawLine(points.get(i).getX(), points.get(i).getY(), points.get(i + 1).getX(), points.get(i + 1).getY(), paint);	
				}
			}
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
					
					invalidate();
					
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
	
	public void incrementIndex()
	{
		if(index < slidesPath.size() - 1)
		{
//			presentation.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_right));
//			presentation.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_left));
			this.index++;
			this.scanner.send.sendMessage("$$NEXTSLIDE$$");
			bitmap = BitmapFactory.decodeFile(slidesPath.get(index));
			bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
			invalidate();
		}	
		
	}
	
	public void decrementIndex()
	{
		if(index > 0)
		{
//			presentation.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_left));
//			presentation.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_right));
			this.index--;
			this.scanner.send.sendMessage("$$PREVIOUSSLIDE$$");
			bitmap = BitmapFactory.decodeFile(slidesPath.get(index));
			bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
			invalidate();
		}
	}
	
	public void clearPointsList()
	{
		points.clear();
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
	
	public void setWhiteBoardEnabled()
	{
		whiteBoardEnabled = true;
		scanner.send.sendMessage("$$OPENWHITEBOARD$$");
		Toast.makeText(context, "Drawing Enabled", Toast.LENGTH_SHORT).show();
		invalidate();
	}
	
	public void setWhiteBoardDisabled()
	{
		whiteBoardEnabled = false;
		this.clearPointsList();
		scanner.send.sendMessage("$$CLOSEWHITEBOARD$$");
		Toast.makeText(context, "Swipe Enabled", Toast.LENGTH_SHORT).show();
		invalidate();
	}
	
	public void setColor(String color)
	{
		this.color = color;
	}
	
	public boolean isWhiteBoardEnabled()
	{
		return whiteBoardEnabled;
	}
}