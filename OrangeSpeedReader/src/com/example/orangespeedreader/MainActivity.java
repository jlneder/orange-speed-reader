package com.example.orangespeedreader;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        
        //show that wonderful gridview we created
        GridView gridview = (GridView) findViewById(R.id.gridViewMain);
        gridview.setAdapter(new ImageAdapter(this));

        gridview.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            	
            	if (position == 0){
		        	Intent wordPlayer = new Intent(getApplicationContext(), WordPlayerActivity.class);
		        	startActivity(wordPlayer);
            	}
            	else if (position == 1){
		        	Intent wordPlayer = new Intent(getApplicationContext(), WordPlayerActivity.class);
		        	startActivity(wordPlayer);
            	}
            	else if (position == 2){
		        	Intent wordPlayer = new Intent(getApplicationContext(), WordPlayerActivity.class);
		        	startActivity(wordPlayer);
            	}else if (position == 3){
		        	Intent wordPlayer = new Intent(getApplicationContext(), WordPlayerActivity.class);
		        	startActivity(wordPlayer);
            	}
            	else if (position == 4){
		        	Intent wordPlayer = new Intent(getApplicationContext(), WordPlayerActivity.class);
		        	startActivity(wordPlayer);
            	}
            	
            	Toast.makeText(MainActivity.this, "" + position, Toast.LENGTH_SHORT).show();
            }
        });
        
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}










