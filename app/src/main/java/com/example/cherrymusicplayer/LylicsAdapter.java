package com.example.cherrymusicplayer;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class LylicsAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	List<String> lylics;
	public LylicsAdapter(Context context,List<String> lylics) {
		mInflater = LayoutInflater.from(context);
		this.lylics = lylics;
	}
	@Override
	public int getCount() {
		return lylics.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView  == null){
			convertView = mInflater.inflate(R.layout.lylics_item, null);
		}
		TextView text = (TextView) convertView.findViewById(R.id.lylics_line);
		text.setText(lylics.get(position));
		return convertView;
	}

}
