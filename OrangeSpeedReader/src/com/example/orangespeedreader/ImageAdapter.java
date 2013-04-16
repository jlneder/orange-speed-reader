package com.example.orangespeedreader;

import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import com.example.orangespeedreader.R;
import com.example.orangespeedreader.R.id;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class ImageAdapter extends BaseAdapter {

	Context mContext;
	
	// Constructor
	public ImageAdapter(Context c){
		mContext = c;
	 
	}

	
	
	@Override
	public int getCount() {
		return btnPics.length;
	}

	@Override
	public Object getItem(int position) {
		return btnPics[position];
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

//	public void LinearLayout(Context c) {
//		        mContext = c;
//	}
		
		
	@Override
	// create a new ImageView for each item referenced by the Adapter
	
	public View getView(int position, View convertView, ViewGroup parent) {
		
		//get the item corresponding to position and store in a BtnBox

		View v = convertView; 
		//LinearLayout mLinearLayout = findViewById(R.layout.grid_item);
		ImageView mImageView = null;
		TextView mTextView = null;
		
	    if (convertView == null) { // if it's not recycled, initialize some
	                                // attributes
	    LayoutInflater vi = 
	            (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            v = vi.inflate(R.layout.grid_item, null);
	     } 
	        
	    mImageView =(ImageView)v.findViewById(R.id.btnPic);
	    mTextView = (TextView)v.findViewById(R.id.btnText);
	    
	    mImageView.setImageResource(btnPics[position]);
	    mTextView.setText(btnText[position]);
	    
	    return v;		
		
		
		
		
//		BtnBox mBtnBox;
//		
//		if (convertView == null){
//			convertView = LayoutInflater.from(mContext).inflate(R.layout.grid_item, parent, false);
//			mBtnBox = new BtnBox();
//			mBtnBox.mImageView=(ImageView)convertView.findViewById(R.id.btnPic);
//			mBtnBox.mTextView=(TextView)convertView.findViewById(R.id.btnText);
//			mBtnBox.mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//			mBtnBox.mImageView.setPadding(8,8,8,8);
//			convertView.setTag(mBtnBox);
//		}
//		else{
//			mBtnBox = (BtnBox)convertView.getTag();
//		}
//		
//		mBtnBox.mImageView.setImageResource(btnPics[position]);
//		mBtnBox.mTextView.setText(btnText[position]);
//		
//		return convertView;
		    
	}
		    	
	    	
	        //ImageView imageView;
//	        if (convertView == null) {  // if it's not recycled, initialize some attributes
//	            
//	        	imageView = new ImageView(mContext);
//	            imageView.setLayoutParams(new GridView.LayoutParams(85, 185));
//	            imageView.setScaleType(ImageView.ScaleType.CENTER);
//	            imageView.setPadding(8, 8, 8, 8);
//	        } else {
//	            imageView = (ImageView) convertView;
//	        }
//
//	        imageView.setImageResource(buttonID[position]);
//	        return imageView;
//}
	    
	   
	    
	    // add pictures to gridshow pretty pictures on the main activity
	private Integer[] btnPics = { 	R.drawable.play, R.drawable.sd_card, 
									R.drawable.collection, android.R.drawable.ic_menu_preferences, 
									android.R.drawable.ic_menu_more, android.R.drawable.ic_menu_close_clear_cancel };

	    // add text to layout
	static final String[] btnText = new String[] { 	"Resume Reading", "Open Book", 
													"Recent Books", "Settings", 
													"About", "Exit"};

	    
}

	

	

