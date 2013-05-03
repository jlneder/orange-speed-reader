package com.duketek.orangespeedreader;

import com.duketek.orangespeedreader.R;
import com.duketek.orangespeedreader.PlayerActivity.UpdateTask;
import com.duketek.orangespeedreader.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class WordPlayerActivity extends Activity {
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = false;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	/**
	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
	 * will show the system UI visibility upon interaction.
	 */
	private static final boolean TOGGLE_ON_CLICK = true;

	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;

	private static final String TAG = "WordPlayer";
	public static final String PREFS_NAME = "OrangeSettings";

	private SharedPreferences mPrefs;
	private String mFilePath;
	private Handler mHandler = new Handler();
	private String[] mWords;
	private String word = "OSR";
	
	private TextView wordsTxtView;
	private String mLine;
	
	private Pattern splitRegex = Pattern.compile(" ");
	private int mLineIndex = 0;
	
	protected AtomicBoolean isRunning=new AtomicBoolean(false);
	protected UpdateTask updateTask = new UpdateTask();
	protected Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			wordsTxtView.setText((String)msg.obj);
		}
	};


	
	
	
	
		
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//make sure to set content so a null pointer exception does not get thrown by the findViewById method
		setContentView(R.layout.activity_word_player);
		
		//assign variable to the TextView
		wordsTxtView = (TextView) findViewById(R.id.fullscreen_content);
		
		//load the last file selected.
		mPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		mFilePath = mPrefs.getString("recent_book","");
		
		Log.d(TAG,mFilePath+" file path from settings");
		
		
		//player.run();
		//player.run();
		
		
		//setContentView(R.layout.activity_word_player);
		setupActionBar();

		//setContentView(R.layout.activity_player);
		//wordsTxtView = new TextView(this);
	    //setContentView(wordsTxtView);
	    
		
		
		
		
		
		
		//this is the overlay pop up
		final View controlsView = findViewById(R.id.fullscreen_content_controls);
		//this one is always visible
		final View contentView = findViewById(R.id.fullscreen_content);

		
		
		
		
		
		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, contentView,
				HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
					// Cached values.
					int mControlsHeight;
					int mShortAnimTime;

					@Override
					@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
					public void onVisibilityChange(boolean visible) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
							// If the ViewPropertyAnimator API is available
							// (Honeycomb MR2 and later), use it to animate the
							// in-layout UI controls at the bottom of the
							// screen.
							if (mControlsHeight == 0) {
								mControlsHeight = controlsView.getHeight();
							}
							if (mShortAnimTime == 0) {
								mShortAnimTime = getResources().getInteger(
										android.R.integer.config_shortAnimTime);
							}
							controlsView
									.animate()
									.translationY(visible ? 0 : mControlsHeight)
									.setDuration(mShortAnimTime);
						} else {
							// If the ViewPropertyAnimator APIs aren't
							// available, simply show or hide the in-layout UI
							// controls.
							controlsView.setVisibility(visible ? View.VISIBLE
									: View.GONE);
						}

						if (visible && AUTO_HIDE) {
							// Schedule a hide().
							delayedHide(AUTO_HIDE_DELAY_MILLIS);
						}
					}
				});

		
	   
		
		
		// Set up the user interaction to manually show or hide the system UI.
		contentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (TOGGLE_ON_CLICK) {
					
					//add code to resume text stream 
					
					mSystemUiHider.toggle();
				} else {
					
					//add code to pause text stream
					
					mSystemUiHider.show();
				}
			}
		});

		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
//		findViewById(R.id.dummy_button).setOnTouchListener(
//				mDelayHideTouchListener);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(100);
		
	}
	
	
	
	
	
	public void onStart() {
	    super.onStart();
	    isRunning.set(true);
	    
	    //run word filler logic
	    updateTask.start();
	    
	    
	  }
	
	public void onResume() {
	    super.onResume();
	    isRunning.set(true);
	}
	 
	public void onPause() {
	    super.onPause();
	    isRunning.set(false);
	    
	        
	}
	
	public void onStop() {
	    super.onStop();
	    isRunning.set(false);
	  }
	  
	  
	 
	  
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
	            	Toast.makeText(WordPlayerActivity.this,"Sorry, the file "+mFilePath+" could not be loaded",Toast.LENGTH_LONG).show();
					e.printStackTrace();
				}
			}
			return textReader;

		}
	  
	  	  
	  protected class UpdateTask extends Thread implements Runnable {
	    public void run() {
	    	
	    	while(isRunning.get()){

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
	    				Toast.makeText(WordPlayerActivity.this,"Sorry, the file "+mFilePath+" could not be loaded",Toast.LENGTH_LONG).show();
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
	    				Toast.makeText(WordPlayerActivity.this,"Sorry, the file could not be read",Toast.LENGTH_LONG).show();
	    				e.printStackTrace();
	    			}
	    		}


	    	}


	    }
	  }


	
	
	
	

	 	
	
	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			// Show the Up button in the action bar.
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			// TODO: If Settings has multiple levels, Up should navigate up
			// that hierarchy.
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}
}
