package com.example.cherrymusicplayer;

public class Music {
	private int _ID;
	private int postion;
	private String TITLE;
	private String ALBUM;
	private long DURATION;
	private String ARTIST;
	private String URL;
	public int get_ID() {
		return _ID;
	}
	public void set_ID(int _ID) {
		this._ID = _ID;
	}
	public String getTITLE() {
		return TITLE;
	}
	public void setTITLE(String tITLE) {
		TITLE = tITLE;
	}
	public String getALBUM() {
		return ALBUM;
	}
	public void setALBUM(String aLBUM) {
		ALBUM = aLBUM;
	}
	public long getDURATION() {
		return DURATION;
	}
	public void setDURATION(long dURATION) {
		DURATION = dURATION;
	}
	public String getARTIST() {
		return ARTIST;
	}
	public void setARTIST(String aRTIST) {
		ARTIST = aRTIST;
	}
	public String getURL() {
		return URL;
	}
	public void setURL(String uRL) {
		URL = uRL;
	}
	public int getPostion() {
		return postion;
	}
	public void setPostion(int postion) {
		this.postion = postion;
	}
}
