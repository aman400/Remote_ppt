package aman.project.remoteppt;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class DialogBox extends DialogFragment 
{
	
	public interface NoticeDialogListener
	{
		public void onDialogPositiveClick(DialogFragment dialog);
		public void onDialogNegetiveClick(DialogFragment dialog);
	}
	
	NoticeDialogListener myListener;
	
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		Bundle bundle = getArguments();
		String message = bundle.getString("Message");
		String title = bundle.getString("Title");
		String positive = bundle.getString("YES");
		String negetive = bundle.getString("NO");
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(title).setMessage(message).setPositiveButton(positive, new DialogInterface.OnClickListener() 
		{
			
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				myListener.onDialogPositiveClick(DialogBox.this);
			}
		}).setNegativeButton(negetive, new DialogInterface.OnClickListener() 
		{
			
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				myListener.onDialogNegetiveClick(DialogBox.this);
			}
		});
		return builder.create();
	}
	
	
	
	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		try
		{
			myListener = (NoticeDialogListener)activity;
		}
		catch(ClassCastException ex)
		{
			throw new ClassCastException(activity.toString() + " must implement NoticeDialogListener");
		}
	}
	
}
