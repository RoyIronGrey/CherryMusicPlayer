package com.example.cherrymusicplayer;

public interface PlayServiceInterface {
	public void play();
	public void pause();
	public void stop();
	public int getDuration();
	public boolean isPlaying();
	public void next();
	public void pre();
	public void setModel(int model);
	public void seekTo(int where);
	public int getCurrentPosition();
	public Music currentMusic();
	public void prepareMusic(int order);
	public void playMusic(int order);
}
