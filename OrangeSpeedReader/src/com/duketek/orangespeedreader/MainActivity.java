package com.duketek.orangespeedreader;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import com.duketek.orangespeedreader.R;




public class MainActivity extends Activity {

	
	public static final String PREFS_NAME = "OrangeSettings";
	private SharedPreferences mPrefs;
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString("recent_book", "" );
        editor.commit();
        
        
        //show that wonderful gridview we created
        GridView gridview = (GridView) findViewById(R.id.gridViewMain);
        gridview.setAdapter(new ImageAdapter(this));

        gridview.setOnItemClickListener(new OnItemClickListener() {
            @Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            	
            	if (position == 0){
		        	Intent wordPlayer = new Intent(getApplicationContext(), WordPlayerActivity.class);
		        	startActivity(wordPlayer);
            	}
            	else if (position == 1){
		        	Intent fileChooser = new Intent(getApplicationContext(), FileChooser.class);
		        	startActivity(fileChooser);
            	}
            	else if (position == 2){
		        	Intent recentBooks = new Intent(getApplicationContext(), RecentBooksActivity.class);
		        	startActivity(recentBooks);
            	}else if (position == 3){
		        	Intent settings = new Intent(getApplicationContext(), SettingsActivity.class);
		        	startActivity(settings);
            	}
            	else if (position == 4){
		        	Intent about = new Intent(getApplicationContext(), AboutActivity.class);
		        	startActivity(about);
            	}
            	else if (position == 5){ //exit
		        	//Intent wordPlayer = new Intent(getApplicationContext(), WordPlayerActivity.class);
		        	 finish();
		             System.exit(0);
            	}
            	
            	//Toast.makeText(MainActivity.this, "" + position, Toast.LENGTH_SHORT).show();
            }
        });
        
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        
       
        
        return true;
    }
    



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	
    	case R.id.action_settings:
    		Intent settings = new Intent(getApplicationContext(), SettingsActivity.class);
        	startActivity(settings);
    	    return true;
    	
    	case R.id.help:
    		Intent help = new Intent(getApplicationContext(), HelpActivity.class);
        	startActivity(help);
        	return true;
    	
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }
}






