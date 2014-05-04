/* This is a white board activity*/

package aman.project.remoteppt;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;


public class Board extends Activity
{
	private ActionBar actionBar;
	private BoardActivity board;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
//		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
//		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		board = new BoardActivity(getApplicationContext());
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		setContentView(board);
		
		board.setOnTouchListener(board);
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.board, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		item.setChecked(true);
		switch(item.getItemId())
		{
			case R.id.eraser:
				board.setColor("white");
				return true;
				
			case R.id.red:
				board.setColor("red");
				return true;
				
			case R.id.blue:
				board.setColor("blue");
				return true;
				
			case R.id.black:
				board.setColor("black");
				return true;
				
			case R.id.green:
				board.setColor("green");
				return true;
				
			case R.id.yellow:
				board.setColor("yellow");
				return true;
				
			case R.id.gray:
				board.setColor("gray");
				return true;
				
			case R.id.cyan:
				board.setColor("cyan");
				return true;
				
			case R.id.white:
				board.setColor("white");
				return true;
				
			case R.id.magenta:
				board.setColor("magenta");
				return true;
				
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}

class BoardActivity extends View implements OnTouchListener
{
	ArrayList<PointHandler> points;
	Paint paint;
	PointF p;
	String color;
	
	public BoardActivity(Context context) 
	{
		super(context);
		
		this.color = "black";
		this.setBackgroundColor(Color.WHITE);
		
		paint = new Paint();
		paint.setStrokeWidth(3);
		points = new ArrayList<PointHandler>();
	}
	
	protected void onDraw(Canvas canvas)
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
	
	@Override
	public boolean onTouch(View view, MotionEvent event) 
	{
		switch(event.getAction())
		{
			
			case MotionEvent.ACTION_DOWN :
			{
				points.add(new PointHandler(event.getX(), event.getY(), this.color));
				return true;
			}
			
			case MotionEvent.ACTION_MOVE :
			{
				points.add(new PointHandler(event.getX(), event.getY(), this.color));
				invalidate();
				return true;
			}
			case MotionEvent.ACTION_UP :
			{
				points.add(new PointHandler(0,0));
				return true;
			}
			
			default:
			{
				return super.onTouchEvent(event);
			}
		}
	}
	
	public void setColor(String color)
	{
		this.color = color;
	}
}