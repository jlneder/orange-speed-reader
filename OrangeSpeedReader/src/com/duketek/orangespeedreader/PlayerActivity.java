package com.duketek.orangespeedreader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

public class PlayerActivity extends Activity {
	  protected TextView textView;
	  private static final String TAG = "Player";
		public static final String PREFS_NAME = "OrangeSettings";

		private SharedPreferences mPrefs;
		private String mLine;
		private String mFilePath;
		private Handler mHandler = new Handler();
		private String[] mWords;
		private String word = "OSR";
		private Pattern splitRegex = Pattern.compile(" ");
		private TextView wordsTxtView;
		private int mLineIndex = 0;
		
		protected AtomicBoolean isRunning=new AtomicBoolean(false);
		protected UpdateTask updateTask = new UpdateTask();
		protected Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				wordsTxtView.setText((String)msg.obj);
			}
		};


		private BufferedReader loadFile(){
			//load the last file selected.
			mPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
			mFilePath = mPrefs.getString("recent_book","");
			Log.d(TAG,mFilePath+" file path from settings");
			
			BufferedReader textReader = null;
			
			if(!mFilePath.equals(" ")){
				//load the file
				//Scanner scan = new Scanner(filePath);
				
				try {
					textReader = new BufferedReader(new FileReader(mFilePath));
					Log.d(TAG, mFilePath+ " loaded");
				}
				catch (FileNotFoundException e) {
					//popup the error
	            	Toast.makeText(PlayerActivity.this,"Sorry, the file "+mFilePath+" could not be loaded",Toast.LENGTH_LONG).show();
					e.printStackTrace();
				}
			}
			return textReader;

		}
		
		private void getNextLine(BufferedReader text){
			String line = null;
			
			try {
				line = text.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mLine = line;
		
		
//		Private String getNextWord(String line){
//			//Pattern splitRegex = Pattern.compile(" ");
//			if (line == null){
//				return "END";
//			}
//			else{
//				mWords = splitRegex.split(line);
//
//				if ( mLineIndex == (mWords.length-1) ){
//					//grab next line after the current one ran out
//					mLine = getNextLine();
//				}
//
//				for(int mLineIndex = 0 ; mLineIndex < mWords.length ; mLineIndex++){
//
//					//get the chunk from the split and load it into word to be sent to TextView
//					word = mWords[mLineIndex]; 
//
//					Log.d(TAG, word);
//
//
//					if ( i == (mWords.length-1) ){
//						//grab next line after the current one ran out
//						line = textReader.readLine();
//					}
//					 
//				 }
//			}
//		}
		}
		
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_player);
		wordsTxtView = new TextView(this);
	    setContentView(wordsTxtView);
	    
	    
	   
	    
	    
	    
	    
	    
	    
	    
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.player, menu);
		return true;
	}
	
	
	
	 
	 
	  
	  
	 
	  
	 
	  public void onStart() {
	    super.onStart();
	    isRunning.set(true);
	    
	    //run word filler logic
	    updateTask.start();
	    
	    
	    
	    
	  }
	 
	  public void onStop() {
	    super.onStop();
	    isRunning.set(false);
	  }
	 
	  	  
	  protected class UpdateTask extends Thread implements Runnable {
	    public void run() {
	    	
	    	 BufferedReader textReader = loadFile();
			 // String mLine = getNextLine(textReader);
			 // String word = getNextWord(line);
			  
			  
			  if(mFilePath != null){
					//load the file
					//Scanner scan = new Scanner(filePath);
					//BufferedReader textReader = null;
					try {
						textReader = new BufferedReader(new FileReader(mFilePath));
						Log.d(TAG, mFilePath+ " loaded");
					}
					catch (FileNotFoundException e) {
						//popup the error
			        	Toast.makeText(PlayerActivity.this,"Sorry, the file "+mFilePath+" could not be loaded",Toast.LENGTH_LONG).show();
						e.printStackTrace();
					}

					try {
						String line = textReader.readLine();

						//TODO make this regex changeable
						Pattern splitRegex = Pattern.compile(" ");
						while(line != null){

							//grab the words from the line split it into an array based on the pattern
							 mWords = splitRegex.split(line);

							//TODO make impulse generator

							 for(int i = 0 ; i < mWords.length ; i++){

								 //get the chunk from the split and load it into word to be sent to TextView
								 word = mWords[i]; 
								 
								 //load the words into the text view
								 
								 Log.d(TAG, word);
								 //wordsTxtView.setText(word);

								 //wpm/60000 = wpm in millis
								 long wpm = 60;
								 long delay = 60000/wpm;
								 
								 try {
										Thread.sleep(1000);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} //This could be something computationally intensive.
									Message message = handler.obtainMessage();
									//message.obj = Double.toString(Math.random());
									message.obj = word;
									handler.sendMessage(message);
								//updateTask.start();
								 //playWords.run();
								 
								 //mHandler.postDelayed(this, 600000);
								 
								 if ( i == (mWords.length-1) ){
									 //grab next line after the current one ran out
									 line = textReader.readLine();
								 }
								 
							 }
							 
						}
						
					} catch (IOException e) {
			        	Toast.makeText(PlayerActivity.this,"Sorry, the file could not be read",Toast.LENGTH_LONG).show();
						e.printStackTrace();
					}



			  }

      
		  
		  
	    
	     }
	    }
	  }

	
	
	


