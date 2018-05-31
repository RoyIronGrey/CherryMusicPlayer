package com.example.cherrymusicplayer;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class MusicListItem extends LinearLayout {
	private Music music;
	public MusicListItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		
	}
	public Music getMusic() {
		return music;
	}
	public void setMusic(Music music) {
		this.music = music;
	}

}
