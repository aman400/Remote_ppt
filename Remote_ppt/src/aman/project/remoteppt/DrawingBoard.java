package aman.project.remoteppt;

import java.io.File;
import java.util.ArrayList;

import android.app.Presentation;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;

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
	private ArrayList<Integer> undoPoints;

	public DrawingBoard(Context context, String name, File extractionDirectory, int width, int height, Scanner scanner) 
	{
		super(context);
		this.context = context;
		index = 0;
		this.setColor("black");
		
		undoPoints = new ArrayList<Integer>();
		
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
					PresetationViewer.updateGUI.sendEmptyMessage(10);
					this.undoPoints.add(points.size());
					for(Integer s : this.undoPoints)
						Log.d("undo", s+"");
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
		undoPoints.clear();
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
		this.setColor("black");
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
	
	public void sublist(int start, int end)
	{
		this.points = new ArrayList<PointHandler>(points.subList(start, end));
		invalidate();
	}
	
	public void undo()throws NullPointerException
	{
		sublist(0, undoPoints.get(undoPoints.size() - 1));
		undoPoints.remove(undoPoints.size() - 1);
		invalidate();
	}
	
	public int getUndoPointsSize()
	{
		return undoPoints.size();
	}
	
	public int getLastUndoPoint()
	{
		return undoPoints.get(undoPoints.size() - 1);
	}
}