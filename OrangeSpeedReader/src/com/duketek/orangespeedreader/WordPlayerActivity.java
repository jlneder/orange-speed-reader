package com.duketek.orangespeedreader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.duketek.orangespeedreader.R.color;
import com.duketek.orangespeedreader.util.SystemUiHider;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class WordPlayerActivity extends Activity {

	
	
	
	
	private final static int ROBOTO_THIN = 0;
	private final static int ROBOTO_THIN_ITALIC = 1;
	private final static int ROBOTO_LIGHT = 2;
	private final static int ROBOTO_LIGHT_ITALIC = 3;
	private final static int ROBOTO_REGULAR = 4;
	private final static int ROBOTO_ITALIC = 5;
	private final static int ROBOTO_MEDIUM = 6;
	private final static int ROBOTO_MEDIUM_ITALIC = 7;
	private final static int ROBOTO_BOLD = 8;
	private final static int ROBOTO_BOLD_ITALIC = 9;
	private final static int ROBOTO_BLACK = 10;
	private final static int ROBOTO_BLACK_ITALIC = 11;
	private final static int ROBOTO_CONDENSED = 12;
	private final static int ROBOTO_CONDENSED_ITALIC = 13;
	private final static int ROBOTO_CONDENSED_BOLD = 14;
	private final static int ROBOTO_CONDENSED_BOLD_ITALIC = 15;
	
	private final static int TOP_LEFT = 1;
	private final static int TOP_MIDDLE = 2;
	private final static int TOP_RIGHT = 3;
	private final static int CENTER_LEFT = 4;
	private final static int CENTER = 5;
	private final static int CENTER_RIGHT = 6;
	private final static int BOTTOM_LEFT = 7;
	private final static int BOTTOM_MIDDLE = 8;
	private final static int BOTTOM_RIGHT = 9;
	
	
	
	
	
	
	
	
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
	
	private boolean variable_speed;
	private boolean slow_down;
	private boolean random_speed;
	private boolean skip_stopwords;
	private int chunk_size;
	boolean skip_vowels;
	
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
		//final View contentView = findViewById(R.id.fullscreen_content);
		
		//assign variable to the TextView
		wordsTxtView = (TextView) findViewById(R.id.fullscreen_content);
		Log.d(TAG,"set fullscreen content: ");
		
		//load the defaults without having to initialize
		PreferenceManager.setDefaultValues(this, R.xml.pref_font, false);
		PreferenceManager.setDefaultValues(this, R.xml.pref_player, false);
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		
		
		
		//assign seekbars and seek text to variables
		mSpeedText = (TextView) findViewById(R.id.textViewWPM);
		mSpeedBar = (SeekBar) findViewById(R.id.wpmBar);
		
		
		
		//setup progress bar and text
		 mProgressText = (TextView) findViewById(R.id.textViewPages);
		 mProgressText.setText(mBookmark + "/" + mWordCount);
		 //mProgressBar = (SeekBar) findViewById(R.id.progressBar);
		 //mProgressBar.setMax(mWordCount);
		
		
		//load the last file selected.
		mPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		mFilePath = mPrefs.getString("recent_book","");
		Log.d(TAG,"recent_book:"+mFilePath);
		
		if(mFilePath.equals("")){
        	Toast.makeText(WordPlayerActivity.this,"Please Open a book first",Toast.LENGTH_LONG).show();
        	this.finish();
		}
				
		//check if day or night colors should be loaded
		boolean day_mode_checkbox = settings.getBoolean("day_mode_checkbox", true);
		if(day_mode_checkbox){
			//load day colors
			int day_font_color_picker = settings.getInt("day_font_color_picker", color.OrangeRed);
			int day_background_color_picker = settings.getInt("day_background_color_picker", color.White);
			int day_osd_color_picker = settings.getInt("day_osd_color_picker", color.Black);
			wordsTxtView.setTextColor(day_font_color_picker);
			wordsTxtView.setBackgroundColor(day_background_color_picker);
			mSpeedText.setTextColor(day_osd_color_picker);
			mProgressText.setTextColor(day_osd_color_picker);
			Log.d(TAG,"daytxt "+day_font_color_picker);
			Log.d(TAG,"daytxt "+day_background_color_picker);
			Log.d(TAG,"daytxt "+day_osd_color_picker);
			
		}
		else{
			//load night colors
			int night_font_color_picker = settings.getInt("night_font_color_picker", color.OrangeRed);
			int night_background_color_picker = settings.getInt("night_background_color_picker", color.OrangeRed);
			int night_osd_color_picker = settings.getInt("night_osd_color_picker", color.OrangeRed);
			wordsTxtView.setTextColor(night_font_color_picker);
			wordsTxtView.setBackgroundColor(night_background_color_picker);
			mSpeedText.setTextColor(night_osd_color_picker);
			mProgressText.setTextColor(night_osd_color_picker);

			
		}
		int font_list = Integer.parseInt( settings.getString("font_list","8") );
		
		
		
		Typeface font;
	    switch (font_list) {
	        case ROBOTO_THIN:
	            font = Typeface.createFromAsset(getBaseContext().getAssets(), "fonts/Roboto-Thin.ttf");
	            break;
	        case ROBOTO_THIN_ITALIC:
	            font = Typeface.createFromAsset(getBaseContext().getAssets(), "fonts/Roboto-ThinItalic.ttf");
	            break;
	        case ROBOTO_LIGHT:
	            font = Typeface.createFromAsset(getBaseContext().getAssets(), "fonts/Roboto-Light.ttf");
	            break;
	        case ROBOTO_LIGHT_ITALIC:
	            font = Typeface.createFromAsset(getBaseContext().getAssets(), "fonts/Roboto-LightItalic.ttf");
	            break;
	        case ROBOTO_REGULAR:
	            font = Typeface.createFromAsset(getBaseContext().getAssets(), "fonts/Roboto-Regular.ttf");
	            break;
	        case ROBOTO_ITALIC:
	            font = Typeface.createFromAsset(getBaseContext().getAssets(), "fonts/Roboto-Italic.ttf");
	            break;
	        case ROBOTO_MEDIUM:
	            font = Typeface.createFromAsset(getBaseContext().getAssets(), "fonts/Roboto-Medium.ttf");
	            break;
	        case ROBOTO_MEDIUM_ITALIC:
	            font = Typeface.createFromAsset(getBaseContext().getAssets(), "fonts/Roboto-MediumItalic.ttf");
	            break;
	        case ROBOTO_BOLD:
	            font = Typeface.createFromAsset(getBaseContext().getAssets(), "fonts/Roboto-Bold.ttf");
	            break;
	        case ROBOTO_BOLD_ITALIC:
	            font = Typeface.createFromAsset(getBaseContext().getAssets(), "fonts/Roboto-BoldItalic.ttf");
	            break;
	        case ROBOTO_BLACK:
	            font = Typeface.createFromAsset(getBaseContext().getAssets(), "fonts/Roboto-Black.ttf");
	            break;
	        case ROBOTO_BLACK_ITALIC:
	            font = Typeface.createFromAsset(getBaseContext().getAssets(), "fonts/Roboto-BlackItalic.ttf");
	            break;
	        case ROBOTO_CONDENSED:
	            font = Typeface.createFromAsset(getBaseContext().getAssets(), "fonts/Roboto-Condensed.ttf");
	            break;
	        case ROBOTO_CONDENSED_ITALIC:
	            font = Typeface.createFromAsset(getBaseContext().getAssets(), "fonts/Roboto-CondensedItalic.ttf");
	            break;
	        case ROBOTO_CONDENSED_BOLD:
	            font = Typeface.createFromAsset(getBaseContext().getAssets(), "fonts/Roboto-BoldCondensed.ttf");
	            break;
	        case ROBOTO_CONDENSED_BOLD_ITALIC:
	            font = Typeface.createFromAsset(getBaseContext().getAssets(), "fonts/Roboto-BoldCondensedItalic.ttf");
	            break;
	        default:
	            throw new IllegalArgumentException("Unknown `typeface` attribute value " + font_list);
	    }
		//apply the font setting to the Text View
	    wordsTxtView.setTypeface(font);
		
	    int font_size = Integer.parseInt( settings.getString("font_size_list","50") );
	    wordsTxtView.setTextSize(font_size);
	    
	    int text_alignment = Integer.parseInt(settings.getString("text_alignment_list","5") );
	    
	    
	    switch (text_alignment) {
	        case TOP_LEFT:
	            wordsTxtView.setGravity(Gravity.TOP | Gravity.LEFT);
	            break;
	        case TOP_MIDDLE:
	        	 wordsTxtView.setGravity(Gravity.TOP | Gravity.CENTER);
	        	 break;
	        case TOP_RIGHT:
	        	 wordsTxtView.setGravity(Gravity.TOP | Gravity.RIGHT);
	        	 break;
	        case CENTER_LEFT:
	        	 wordsTxtView.setGravity(Gravity.CENTER | Gravity.LEFT);
	        	 break;
	        case CENTER:
	        	 wordsTxtView.setGravity(Gravity.CENTER);
	        	 break;
	        case CENTER_RIGHT:
	        	 wordsTxtView.setGravity(Gravity.CENTER | Gravity.RIGHT);
	        	 break;
	        case BOTTOM_LEFT:
	        	 wordsTxtView.setGravity(Gravity.BOTTOM | Gravity.LEFT);
	        	 break;
	        case BOTTOM_MIDDLE:
	        	 wordsTxtView.setGravity(Gravity.BOTTOM | Gravity.CENTER);
	        	 break;
	        case BOTTOM_RIGHT:
	        	 wordsTxtView.setGravity(Gravity.BOTTOM | Gravity.RIGHT);
	        	 break;
	        default:
	            break;
	    }
	    
	    
	    int default_wpm = Integer.parseInt( settings.getString("default_wpm_edit_text","250") );
	    
	    variable_speed = settings.getBoolean("variable_speed_checkbox", false);
	    slow_down = settings.getBoolean("slow_down_checkbox", false);
	    random_speed = settings.getBoolean("random_speed_checkbox", false);
	    skip_stopwords = settings.getBoolean("skip_stopwords_checkbox", false);
	    chunk_size = Integer.parseInt( settings.getString("chunk_size_edit_text", "1") );
	    skip_vowels = settings.getBoolean("skip_vowels_checkbox", false);
	    
		
		mBookmark = mPrefs.getInt(mFilePath+"bookmark",0);
		
		//make the value to load if there is no saved setting the default set in the settings menu
		mWPM = mPrefs.getInt(mFilePath+"WPM",default_wpm);
		
		
		
		mSpeedText.setText( mWPM+ " WPM");
		mSpeedBar.setProgress(mWPM);
		

		
		
		
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
		
		 
		
//		 //setup progress bar and text
//		 mProgressText = (TextView) findViewById(R.id.textViewPages);
//		 mProgressText.setText(mBookmark + "/" + mWordCount);
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
//		mSpeedText = (TextView) findViewById(R.id.textViewWPM);
//		mSpeedBar = (SeekBar) findViewById(R.id.wpmBar);
//		
//		mSpeedText.setText( mWPM+ " WPM");
//		mSpeedBar.setProgress(mWPM);
		mSpeedBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				// Log the progress
				Log.d("DEBUG", "WPM is: "+progress);
				//set textView's text
				
				
				mSpeedText.setText( Integer.toString(progress + 50) + " WPM");
				mWPM = progress+50;
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
		
		
		
		mSystemUiHider = SystemUiHider.getInstance(this, wordsTxtView,HIDER_FLAGS);
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
		wordsTxtView.setOnClickListener(new View.OnClickListener() {
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

	 public static ArrayList<String> split2(String line, int n){
		    line+=" ";
		    Pattern pattern = Pattern.compile("\\w*\\s");
		    Matcher matcher = pattern.matcher(line);
		    ArrayList<String> list = new ArrayList<String>();
		    int i = 0;
		    while (matcher.find()){
		        if(i!=n)
		            list.add(matcher.group());
		        else
		            break;
		        i++;
		    }
		    return list;
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
		  
		  
		  //add a space at end of line
		  
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
				  
				  

				  

				  Pattern splitRegex = Pattern.compile(mSplitPattern);

				  while(line != null){
					  if(!mNewLocation.get()){
						  //grab the words from the line split it into an array based on the pattern
						  if (mWords.length != 0 ){

							  if(skip_stopwords){
								  Pattern stopWords = Pattern.compile("\\b(?:i|a|and|about|an|are|the|about|across|after|afterwards|again|against|all|almost|alone|along|already|also|although|always|am|among|an|and|any|are|as|at|be|because|been|but|by|can|cannot|could|dear|did|do|does|either|else|ever|every|for|from|get|got|had|has|have|he|her|hers|him|his|how|however|i|if|in|into|is|it|its|just|least|let|like|likely|may|me|might|most|must|my|neither|no|nor|not|of|off|often|on|only|or|other|our|own|rather|said|say|says|she|should|since|so|some|than|that|the|their|them|then|there|these|they|this|tis|to|too|twas|us|wants|was|we|were|what|when|where|which|while|who|whom|why|will|with|would|yet|you|your )\\b\\s*", Pattern.CASE_INSENSITIVE);
								  Matcher match = stopWords.matcher(line);
								  line = match.replaceAll(" ");
							  }
							
							  
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
								  //delay = 60000/mWPM;
								  delay = 60000/mWPM;
								  double percentAdjust = .5;
								  double adjust = (delay * percentAdjust);
								  Log.d(TAG,"initial delay: "+delay);

								  //if variable speed is set, 
								  if(variable_speed){
									  
									  //speed up if less then 5 the avg eng word length
									  //slow down if over 5 in length
									  if(word.length() >8){

									  Log.d(TAG,"adjust: "+adjust);
									  
									  delay = delay + (int)adjust;
									  Log.d(TAG,"delay: "+delay);
									  }
									  else if (word.length() <4){
										  Log.d(TAG,"adjust: "+adjust);

										  delay = delay - (int)adjust;
										  Log.d(TAG,"delay: "+delay);

									  }
								  }
								  //when slow down is checked in the settings, slow down on puncuation
								  if(slow_down){
									  Pattern punctuation = Pattern.compile( "[,-.\\!;:&\'\"]" );
									  Matcher match = punctuation.matcher(word);
									  	if (match.find()){
									  		delay = delay + (int)adjust;
									  	}
								  }
								  
								  if(skip_vowels){
									  if(word.length() > 4){
									  Pattern vowels = Pattern.compile( "(a|e|i|o|u)",Pattern.CASE_INSENSITIVE );
									  Matcher match = vowels.matcher(word);
									  word = match.replaceAll("");
									  }
								  }
								  //if random speed down is checked in the settings, randomly speed up or slow down
								  if(random_speed){
									  double randomPercent = .2 + (double)(Math.random()*.50);
									  //get a randrom boolean
									  boolean add = Math.random() < 0.5;
									  if(add){
										  delay = delay + (int)(delay * randomPercent);
									  }
									  else{//subtract
										  delay = delay - (int)(delay * randomPercent);
									  }
									  
								  }
								  
								  
								  
								  
								  
								  
								  
								  

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
