package aman.project.remoteppt;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity 
{
	private Button connect;
	private EditText network_address, port;
	private String network;
	final int PORT = 5678;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		this.connect = (Button)this.findViewById(R.id.connect);
		network_address = (EditText)(this.findViewById(R.id.address));
		port = (EditText)(this.findViewById(R.id.port));
	}
		
	@Override
	protected void onStart()
	{
		super.onStart();
		this.connect.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0) 
			{
				try
				{
					
					network = network_address.getText().toString();
					
					Toast.makeText(getBaseContext(), "Scanning for Servers", Toast.LENGTH_LONG).show();
					
					Intent in = new Intent();
					in.putExtra("network", network);
					in.putExtra("port", PORT);
					in.setClass(getApplicationContext(), ServerScanner.class);
					
					startActivity(in);
				}
				catch(NumberFormatException ex)
				{
					Toast.makeText(getBaseContext(), "Enter valid port and Network address", Toast.LENGTH_LONG).show();
				}
			}			
		});		
	}
	
	public String getNetwork()
	{
		return this.network;
	}
}