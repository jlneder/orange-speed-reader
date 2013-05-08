package com.duketek.orangespeedreader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.duketek.orangespeedreader.util.SystemUiHider;

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
	//private Handler mHandler = new Handler();
	private String[] mWords;
	private String word = "OSR";
	
	private TextView wordsTxtView;
	private TextView mProgressText; 
	private SeekBar mProgressBar;
	private SeekBar mSpeedBar;
	private TextView mSpeedText;
	private AtomicBoolean mNewLocation = new AtomicBoolean(false);
	private int mProgress;
	
	private String mLine;
	
	private String mSplitPattern = " ";
	private Pattern mSplitRegex = Pattern.compile(mSplitPattern);
	private int mLineIndex = 0;
	private Object mPauseLock = new Object();;
    private boolean mPaused = false;
    private boolean mPause = false;
    private boolean mLocked = false;
    private boolean mFinished = false;
    private BufferedReader mTextReader;
	private int mBookmark = 0;
	private int mWordCount;
	private int mWPM = 250;
	//default split is a blank space
	protected double pcnt;
	protected double ETA; 
	protected AtomicBoolean isRunning=new AtomicBoolean(false);
	protected AtomicBoolean stopFlag=new AtomicBoolean(false);
	protected UpdateTask updateTask = new UpdateTask();
	
	
	
	
	protected Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			wordsTxtView.setText((String)msg.obj);
			
			pcnt = ( (double)mProgress / (double)mWordCount) * 100.0d;
			ETA = ( (double)mWordCount - (double)mProgress ) / mWPM;
			mProgressText.setText( 	mProgress + "/" + mWordCount +
									" " + String.format("%.2f",pcnt) + "%" +
									" ETA: "+String.format("%.2f",ETA) + "min"  
									);
			//mProgressText.setText(mBookmark + "/" + mWordCount);
			mProgressBar.setProgress(mProgress);
			//Log.d(TAG,"mprogress out"+mProgress);
			
		}
	};


	
	
	
	
		
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//make sure to set content so a null pointer exception does not get thrown by the findViewById method
		setContentView(R.layout.activity_word_player);
		Log.d(TAG,"set content view: ");
		
		
		//this is the overlay pop up
		final View controlsView = findViewById(R.id.fullscreen_content_controls);
		//this one is always visible
		final View contentView = findViewById(R.id.fullscreen_content);
		
		//assign variable to the TextView
		wordsTxtView = (TextView) findViewById(R.id.fullscreen_content);
		Log.d(TAG,"set fullscreen content: ");
		
		//load the last file selected.
		mPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		mFilePath = mPrefs.getString("recent_book","");
		
		
		mBookmark = mPrefs.getInt(mFilePath+"bookmark",0);
		
		mWPM = mPrefs.getInt(mFilePath+"WPM",250);
		
		
		
		
		
		
		Log.d(TAG,"file path from settings: "+ mFilePath);
		Log.d(TAG,"wpm from settings: "+ mWPM);
		
		
		
		//player.run();
		//player.run();
		
		
		//setContentView(R.layout.activity_word_player);

		
		//mTextReader = loadFile();
		
		if (mBookmark >0){
			mProgress = mBookmark;
			// mTextReader =  loadLocation(mBookmark);
			// Log.d(TAG,"mBookmark starting #: "+mBookmark);
			 mNewLocation.set(true);
		 }
		
		
		setWordCount();			
		
		 
		
		 //setup progress bar and text
		 mProgressText = (TextView) findViewById(R.id.textViewPages);
		 mProgressText.setText(mBookmark + "/" + mWordCount);
		 mProgressBar = (SeekBar) findViewById(R.id.progressBar);
		 mProgressBar.setMax(mWordCount);
		 mProgressBar.setProgress(mBookmark);
		 
		 
		 

		 mProgressBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			 @Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				 // Log the progress
				 Log.d("DEBUG", "Progress is: "+progress);
				 //set textView's text
				 double pcnt = ((double)progress / (double)mProgressBar.getMax()*100.0d);
								
				//mProgressText.setText( progress + "/" + mProgressBar.getMax() +" "+  String.format("%.2f",pcnt) + "%" );
				 ETA = ( (double)mWordCount - (double)mProgress ) / mWPM;
				 mProgressText.setText( 	progress + "/" + mWordCount +
						" " + String.format("%.2f",pcnt) + "%" +
						" ETA: "+String.format("%.2f",ETA) + "min"  
						);
				//only run on updates (needed?)
				mProgress = progress;
			 }
		 
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			
				//restart thread after lifting finger off the bar
				//so as it wont freak out
			
				//mProgress = mProgressBar.getProgress();
				//stop the thread
				//stop(); 
				
				//stopFlag.set(true); 				
				mNewLocation.set(true);
				
				//start();

				//updateTask = new UpdateTask();
				//updateTask.start();
			}
		}	);
		
		
		
		
		//setup wpm speed bar (top)
		mSpeedText = (TextView) findViewById(R.id.textViewWPM);
		mSpeedBar = (SeekBar) findViewById(R.id.wpmBar);
		
		mSpeedText.setText( mWPM+ " WPM");
		mSpeedBar.setProgress(mWPM);
		mSpeedBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				// Log the progress
				Log.d("DEBUG", "WPM is: "+progress);
				//set textView's text
				mSpeedText.setText(progress + " WPM");
				mWPM = progress;
				ETA = ( (double)mWordCount - (double)mProgress ) / mWPM;
				mProgressText.setText( 	mProgress + "/" + mWordCount +
						" " + String.format("%.2f",pcnt) + "%" +
						" ETA: "+String.format("%.2f",ETA) + "min"  
						);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}

		}	);
		

		
		
		
		//setContentView(R.layout.activity_player);
		//wordsTxtView = new TextView(this);
	    //setContentView(wordsTxtView);
	    
//		if (mPaused == false){
//		mTextReader = loadFile();
//		 //run word filler logic
//	    updateTask.start();
//		}
		
		
		
		

		
		
		
		

		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.

		setupActionBar();
		
		
		
		mSystemUiHider = SystemUiHider.getInstance(this, contentView,HIDER_FLAGS);
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
						mShortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
					}
					controlsView
					.animate()
					.translationY(visible ? 0 : mControlsHeight)
					.setDuration(mShortAnimTime);
				} 
				else {
					// If the ViewPropertyAnimator APIs aren't
					// available, simply show or hide the in-layout UI
					// controls.
					controlsView.setVisibility(visible ? View.VISIBLE
							: View.GONE);
				}
				
				if (!visible/* && mPaused && mLocked*/){
					go();
				}
				else if(visible /*&& !mPaused && !mLocked*/ ){
					pause();
				}
					
				
				
//				if (visible && AUTO_HIDE) {
//					// Schedule a hide().
//					delayedHide(AUTO_HIDE_DELAY_MILLIS);
//				}
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
	
	
	
	
	
	@Override
	public void onStart() {
	    super.onStart();
	    //isRunning.set(true);
	    
	    //run word filler logic
	    //updateTask.start();
	    
	    if (mPaused == false){
	    	
	    	if (mBookmark >0){
				//mProgress = mBookmark;
	    		mTextReader =  loadLocation(mBookmark);
	    		Log.d(TAG,"mBookmark starting #: "+mBookmark);
	    		mNewLocation.set(true);
			}
	    	else{
	    		mTextReader =  loadLocation(0);
	    		Log.d(TAG,"mBookmark starting #: "+0);
	    		mNewLocation.set(true);
	    	}
			//mTextReader = loadFile();
			
			
			 
			//run word filler logic
		    updateTask.start();
			}
	    
//	    synchronized (mPauseLock) {
//            mPaused = false;
//            mPauseLock.notifyAll();
//        }
	    
	  }
	
	
	
	
	@Override
	public void onResume() {
	    super.onResume();
	    synchronized (mPauseLock) {
            mPaused = false;
            mPauseLock.notifyAll();
        }
	}
	
	
	
	
	@Override
	public void onPause() {
	    super.onPause();
	    
	    synchronized (mPauseLock) {
            mPaused = true;
        }
	    //isRunning.set(false);
	    
	        
	}
	
	
	
	
	@Override
	public void onStop() {
	    super.onStop();
	    
	    synchronized (mPauseLock) {
            mPaused = true;
        }
	    
	    
	    //set the prefs
	    SharedPreferences.Editor editor = mPrefs.edit();
	    editor.putInt(mFilePath+"bookmark",mProgress);
	    editor.putInt(mFilePath+"WPM",mWPM);
	    //commit the changes
	    editor.commit();

	    //isRunning.set(false);
	}

	  
	 
	  
	  private BufferedReader loadFile(){
			//load the last file selected.
			//mPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
			//load file path
			//mFilePath = mPrefs.getString("recent_book","");
			//load bookmark

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
	  
	  
	  
	  
	  
	  public int rewindMark(int bookmark, int rewindAmount){
		  
		  bookmark = bookmark - rewindAmount;
		  return bookmark;
	  }
	  
	  
	  
	  
	  
	  
	  public void setWordCount(){
		  				
		  BufferedReader wcReader = loadFile();

		  Pattern splitRegex = Pattern.compile(mSplitPattern);
		  int wordCount = 0;
		  String line ="";
		  String[] words;
		  try {
			do {
				 
				  line = wcReader.readLine();
				 //Log.d(TAG,line);
				  words = splitRegex.split(line);
				  wordCount =  wordCount + words.length;
			  } while( wcReader.readLine() != null) ;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  
		  mWordCount = wordCount;
	  }

	  
	  
	  
	  

	  private BufferedReader loadLocation(int mark){

		  BufferedReader seekReader = loadFile();

		  
		  String line ="";
		  
		  		  
		  try {
			  line = seekReader.readLine();
		  } catch (IOException e1) {
			  // TODO Auto-generated catch block
			  e1.printStackTrace();
		  }
		  
		  Pattern splitRegex = Pattern.compile(mSplitPattern);
		  mWords = splitRegex.split(line);
		  int b = 0;
		  
		  while (b + mWords.length < mark){
			  b = b+mWords.length;
			
			  try {
				  line = seekReader.readLine();
			  } catch (IOException e) {
				  // TODO Auto-generated catch block
				  e.printStackTrace();
			  }
			  
		  }
		  //last few
		  
		  mWords = splitRegex.split(line);
		  for (int i = b; i < mark; i++){
			  b = b++;
		  }
		  Log.d(TAG,"returned: " +b+"");
		  
		  //replace existing buffer with the new one
		  //mTextReader = seekReader;
		  mNewLocation.set(false);
		  return seekReader;
		  //return mWords;

	  }


	  
	  
	  
	  protected class UpdateTask extends Thread implements Runnable {
		  @Override
		public void run() {
			  int delay = 0;

			  //BufferedReader textReader = loadFile();
			  // String mLine = getNextLine(textReader);
			  // String word = getNextWord(line);


			  //	    		if(mFilePath != null){
			  //load the file
			  //Scanner scan = new Scanner(filePath);
			  //BufferedReader textReader = null;
			  //	    			try {
			  //	    				textReader = new BufferedReader(new FileReader(mFilePath));
			  //	    				Log.d(TAG, mFilePath+ " loaded");
			  //	    			}
			  //	    			catch (FileNotFoundException e) {
			  //	    				//popup the error
			  //	    				Toast.makeText(WordPlayerActivity.this,"Sorry, the file "+mFilePath+" could not be loaded",Toast.LENGTH_LONG).show();
			  //	    				e.printStackTrace();
			  //	    			}

			  try {

				  //if not starting the book at the beginning
				  //load up to the location
//				  if (mBookmark >0){
//
//					 mTextReader =  loadLocation(mProgress);
//
//				  }
//				  else if(mNewLocation.get()){
//					  loadLocation(mProgress);
//					  mNewLocation.set(false);

					  //	    					String line = mTextReader.readLine();
					  //	    					Pattern splitRegex = Pattern.compile(mSplitPattern);
					  //	    					mWords = splitRegex.split(line);
					  //	    					int b = 0;
					  //	    					while (b + mWords.length < mBookmark){
					  //	    						b = b+mWords.length;
					  //	    						line = mTextReader.readLine();
					  //	    					}
					  //	    					//last few
					  //	    					mWords = splitRegex.split(line);
					  //	    					for (int i = b; i < mBookmark; i++){
					  //	    						b = b++;
					  //	    					}

				//  }




				  String line = mTextReader.readLine();

				  //TODO make this regex changeable
				  Pattern splitRegex = Pattern.compile(mSplitPattern);

				  while(line != null){
					  if(!mNewLocation.get()){
						  //grab the words from the line split it into an array based on the pattern
						  if (mWords.length != 0 ){

							  mWords = splitRegex.split(line);
						  }

						  //TODO make impulse generator

						  for(int i = 0 ; i < mWords.length ; i++){
							  if(!mNewLocation.get()){
								  //hook to pause the thread if needed
								  synchronized (mPauseLock) {
									  while (mPaused) {
										  try {

											  mPauseLock.wait();

										  } catch (InterruptedException e) {
										  }
									  }
								  }

								  //get the chunk from the split and load it into word to be sent to TextView
								  word = mWords[i]; 

								  //store the current location
								  mProgress++;

								  //load the words into the text view

								  //Log.d(TAG, word);
								  //wordsTxtView.setText(word);

								  //wpm/60000 = wpm in millis
								  //int wpm = mWPM;
								  delay = 60000/mWPM;

								  try {
									  Thread.sleep(delay);
								  } catch (InterruptedException e) {
									  // TODO Auto-generated catch block
									  e.printStackTrace();
								  } 




								  Message message = handler.obtainMessage();
								  message.obj = word;
								  handler.sendMessage(message);
								  //updateTask.start();
								  //playWords.run();

								  //mHandler.postDelayed(this, 600000);

								  if ( i == (mWords.length-1) ){
									  //grab next line after the current one ran out
									  line = mTextReader.readLine();
								  }
							  }
//							  else{
//								  mTextReader =  loadLocation(mProgress);
//								  Log.d(TAG, "Loaded line, inner loop"+mProgress);
//								  line = mTextReader.readLine();
//								  
//							  }
						  }
					  }
					  else{
						  mTextReader =  loadLocation(mProgress);
						  line = mTextReader.readLine();
						  Log.d(TAG, "Loaded line, outer loop"+mProgress);
						 
					  }
						  

				  }
		  						  


			  } catch (IOException e) {
				  Toast.makeText(WordPlayerActivity.this,"Sorry, the file could not be read",Toast.LENGTH_LONG).show();
				  e.printStackTrace();
			  }


		  }


	  }

	  /*
	   * Stops the tread by setting an atomicBoolean stopFlag
	   * that gets checked inside every loop in the thread
	   */
	  public synchronized void stop() {
	        stopFlag.set(true);
	        
	        
	        /*try {
				updateTask.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
	        
	    }
	  
	  
	  public synchronized void pause() {
		    mPaused = true;
		}

		public void go() {
		    synchronized(mPauseLock){
		    	mPaused = false;
		    	mPauseLock.notifyAll();
		    	
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
			
			
			 int eventaction = motionEvent.getAction();

			 
			 switch (eventaction) {

			 // finger touches the screen
			 case MotionEvent.ACTION_DOWN: 

				 break;

				 // finger moves on the screen
			 case MotionEvent.ACTION_MOVE:

				 break;

				 // finger leaves the screen
			 case MotionEvent.ACTION_UP:   
				 
	            	Toast.makeText(WordPlayerActivity.this,"Finger up",Toast.LENGTH_LONG).show();

				 
				 if (isRunning.get() == false){ 
					 pause();
				 }
				 else{
					 go();
				 }
				 mPaused = true;
				 break;
			 }

			 // tell the system that we handled the event and no further processing is required
			 return true; 
			
			
			
			
			
//			if (AUTO_HIDE) {
//				delayedHide(AUTO_HIDE_DELAY_MILLIS);
//			}
//			return false;
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
