package com.example.orangespeedreader;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import com.example.orangespeedreader.R;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;


public class ImageAdapter extends BaseAdapter {

	@Override
	public int getCount() {
		return buttonID.length;
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	
	

	

	    private Context mContext;

	    public ImageAdapter(Context c) {
	        mContext = c;
	    }


	    @Override
	    // create a new ImageView for each item referenced by the Adapter
	    public View getView(int position, View convertView, ViewGroup parent) {
	        ImageView imageView;
	        if (convertView == null) {  // if it's not recycled, initialize some attributes
	            imageView = new ImageView(mContext);
	            imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
	            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
	            imageView.setPadding(8, 8, 8, 8);
	        } else {
	            imageView = (ImageView) convertView;
	        }

	        imageView.setImageResource(buttonID[position]);
	        return imageView;
	    }

	    // references to our images
	    private Integer[] buttonID = {
	            R.drawable.ic_launcher, R.drawable.ic_launcher,
	            R.drawable.ic_launcher, R.drawable.ic_launcher,
	            R.drawable.ic_launcher, R.drawable.ic_launcher,
	            
	    };
	}
	
	

