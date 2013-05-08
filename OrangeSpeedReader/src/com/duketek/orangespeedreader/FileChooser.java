package com.duketek.orangespeedreader;

import java.io.File;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.ipaulpro.afilechooser.utils.FileUtils;


public class FileChooser extends Activity {

	private static final int REQUEST_CODE = 6384; // onActivityResult request code

	public static final String PREFS_NAME = "OrangeSettings";
	private String filePath;
	private SharedPreferences mPrefs;
	private int mRecentBookSlot;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		mRecentBookSlot = mPrefs.getInt("recent_book_slot",0);
		
		
		showChooser();
		
	}

	private void showChooser() {
		// Use the GET_CONTENT intent from the utility class
		Intent target = FileUtils.createGetContentIntent();
		// Create the chooser Intent
		Intent fileOpenIntent = Intent.createChooser(target, getString(R.string.title_activity_file_chooser));
		//Intent fileOpenIntent = new Intent(getApplicationContext(), FileChooser.class);
		
		try {
			startActivityForResult(fileOpenIntent, REQUEST_CODE);
			target.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			//finish();
			
			
		} catch (ActivityNotFoundException e) {
			// The reason for the existence of aFileChooser
		}


	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CODE:
			// If the file selection was successful
			if (resultCode == RESULT_OK) {
				if (data != null) {
					// Get the URI of the selected file
					final Uri uri = data.getData();

					try {
						// Create a file instance from the URI
						final File file = FileUtils.getFile(uri);
						
						//save the selected file path
						filePath =  file.getAbsolutePath();
						
						Log.d("file",filePath);
						Toast.makeText(FileChooser.this,"File Selected: "+file.getAbsolutePath(), Toast.LENGTH_LONG).show();
						
						
						
						//Toast.makeText(FileChooser.this,"File Selected: " + file.getAbsolutePath(),Toast.LENGTH_LONG).show();
						
						//start the word player 
						//Intent wordPlayerIntent = new Intent(getApplicationContext(), WordPlayerActivity.class);
						
						
						
						//startActivity(wordPlayerIntent);
						
					} catch (Exception e) {
						Log.e("FileSelectorTestActivity", "File select error",	e);
					}
				}
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	protected void onStop(){
		super.onStop();
       	//load up shared prefs for storage
		//SharedPreferences mPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		SharedPreferences.Editor editor = mPrefs.edit();
       
		boolean wasAdded = mPrefs.getBoolean(filePath+"added",false);
		//set the prefs
		
		//if it wasnt added already
		if (!wasAdded){
			
			//mark the filepath saved so it does not get addeed 2x
			editor.putBoolean(filePath+"added", true);
			
			//add slot0, filepath for first save slot1,fp for second etc
			editor.putString("slot"+mRecentBookSlot, filePath);
			Log.d("file", "slot: "+mRecentBookSlot);
			
			//store the next slot to use
			int nextSlot = mRecentBookSlot + 1;
						
			editor.putInt("recent_book_slot", nextSlot );
			Log.d("file", "nxtslot: "+nextSlot);
			
		}

		editor.putString("recent_book", filePath );

		//commit the changes
		editor.commit();

      
    }

}