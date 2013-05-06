package com.duketek.orangespeedreader;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class RecentBooksActivity extends ListActivity {

	
	private SharedPreferences mPrefs;
	private ArrayAdapter <String> mAdapter;
	private ListView recentList;
	private String pathToBook;
	public static final String PREFS_NAME = "OrangeSettings";


	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		//setContentView(R.layout.activity_recent_books);
		//setContentView(R.layout.recent_books_list_item);
		//recentList = (ListView) findViewById(R.id.recentBooksListView);
		
		final ArrayList<String> bookList = new ArrayList<String>();
		
		mPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		//get the index of the 1st empty slot		
		int slots = mPrefs.getInt("recent_book_slot", 0 );
		
		String[] recentBooks = new String[slots];
		
	    for ( int i = 0; i< slots; i++){
	            recentBooks[i] = "";
	    }
		
		//Boolean(filePath+"added", true);
		
		//fill up array list with strings
		for (int i = 0; i<slots;i++){
			Log.d("recent","slots: "+slots);
			recentBooks[i] = mPrefs.getString("slot"+i, "");
			Log.d("recent","recentbook: "+recentBooks[i]);
			bookList.add(mPrefs.getString("slot"+i, ""));
		}
		
		//setup arrary adaptor (converts array list to list view
		
		setListAdapter(new ArrayAdapter<String>(this, R.layout.recent_books_list_item, recentBooks));
		//recentList.setAdapter(mAdapter);
		
		recentList = getListView();
		recentList.setTextFilterEnabled(true);
		
		//setContentView(recentList);
		
		
		//get recent books from shared prefs
        
         
        
		recentList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				  pathToBook = (String)( recentList.getItemAtPosition(position) );
				  SharedPreferences.Editor editor = mPrefs.edit();
				  
				  //set the most recent book to the one selected then save it
				  editor.putString("recent_book", pathToBook );	
				  editor.commit();
				  //launce the player app after selection
				  
				  Intent wordPlayerIntent = new Intent(getApplicationContext(), WordPlayerActivity.class);
				  startActivity(wordPlayerIntent);
				
			}                 
		} );
	}
		
	public View getView (int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.recent_books_list_item, null);
		}
		return v;
	}
	

}
