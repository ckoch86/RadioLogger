package com.comtec.radiologger.adapter;

import java.util.ArrayList;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

public class GridViewAdapter extends BaseAdapter {

	private ArrayList<Button> mButtons = null;

	public GridViewAdapter(ArrayList<Button> b) {
		mButtons = b;
	}

	public int getCount() {
		return mButtons.size();
	}

	public Object getItem(int position) {
		return (Object) mButtons.get(position);
	}

	public long getItemId(int position) {
		// in our case position and id are synonymous
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		Button button;
		button = (Button) mButtons.get(position);
		return button;
	}
}