package com.example.cherrymusicplayer;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MusicListAdapter extends BaseAdapter {
	List<Music> musicList;
	private LayoutInflater mInflater;

	public MusicListAdapter(Context context,List<Music> musicList) {
		this.mInflater = LayoutInflater.from(context);
		this.musicList = musicList;
	}

	@Override
	public int getCount() {
		return musicList.size();
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
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.music_list_item, null);
		}
		MusicListItem view = (MusicListItem) convertView;
		view.setMusic(musicList.get(position));
		TextView title = (TextView) view
				.findViewById(R.id.list_text_music_title);
		TextView artistAndAlbum = (TextView) view
				.findViewById(R.id.list_text_artist_album);
		title.setText(musicList.get(position).getTITLE());
		artistAndAlbum.setText(musicList.get(position).getARTIST() + " - "
				+ musicList.get(position).getALBUM());
		return view;
	}

}
