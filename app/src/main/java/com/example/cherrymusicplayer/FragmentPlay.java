package com.example.cherrymusicplayer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import cn.sharesdk.onekeyshare.OnekeyShare;

import com.example.cherrymusicplayer.PlayService.MusicBinder;

public class FragmentPlay extends Fragment implements OnSeekBarChangeListener,
		OnClickListener {
	TextView timeViewTotal;
	TextView timeViewCurrent;
	TextView title;
	TextView artist;
	SeekBar seekBar;
	ImageButton playOrPause;
	ImageButton pre;
	ImageButton next;
	ImageButton random;
	ImageButton one_circle;
	ImageButton share;
	MusicBinder musicService;
	OnekeyShare oks;
	ListView lylicsView;
	private ServiceConnection connection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			if (service instanceof MusicBinder) {
				musicService = (MusicBinder) service;
				// initButton();
				initEvents(musicService);
				flashLylics();
			}
		}
	};
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if(musicService.getMusicList().size() == 0){
				return;
			}
			seekBar.setProgress(musicService.getCurrentPosition());
			SimpleDateFormat format = new SimpleDateFormat("mm:ss");
			String allTime = format
					.format(new Date(musicService.getDuration()));
			String current = format.format(new Date(musicService
					.getCurrentPosition()));
			seekBar.setMax(musicService.getDuration());
			timeViewCurrent.setText(current);
			timeViewTotal.setText(allTime);
			title.setText(musicService.currentMusic().getTITLE());
			artist.setText(musicService.currentMusic().getARTIST());
			if (!handler.hasMessages(0)) {
				handler.sendEmptyMessageDelayed(0, 200);
			}
			if (musicService.isPlaying()) {
				playOrPause.setBackgroundResource(R.drawable.pause_img_btn);
			} else {
				playOrPause.setBackgroundResource(R.drawable.play_img_btn);
				handler.removeMessages(0);
				if (Math.abs(musicService.getDuration()
						- musicService.getCurrentPosition()) < 2000) {
					handler.sendEmptyMessage(0);
				}
			}
		};
	};

	public Handler getHandler() {
		return handler;
	}
	private void initOnekeyShare() {
		oks = new OnekeyShare();
		oks.setTitle(getString(R.string.default_share_title));
		oks.setTitleUrl(getString(R.string.default_share_titleUrl));
		oks.setText(getString(R.string.default_share_text));
		oks.setComment(getString(R.string.default_share_comment));
		oks.setSite(getString(R.string.default_share_site));
		oks.setSiteUrl(getString(R.string.default_share_siteUrl));
		oks.disableSSOWhenAuthorize();
	}

	private void publishOnkeyShare() {
		oks.setTitle(musicService.currentMusic().getTITLE() + " - "
				+ musicService.currentMusic().getARTIST());
		// oks.setTitleUrl(getString(R.string.default_share_titleUrl));
		oks.setText(getString(R.string.publish_share_text));
		// oks.setComment(getString(R.string.default_share_comment));
		// oks.setSite(getString(R.string.default_share_site));
		// oks.setSiteUrl(getString(R.string.default_share_siteUrl));
		oks.show(this.getActivity().getApplicationContext());
	}

	public void flashLylics() {
		if(musicService.getMusicList().size() == 0){
			return;
		}
		BufferedReader reader = null;
		FileReader in = null;
		List<String> lylics = new ArrayList<String>();
		for(int i=0;i<6;i++){
			lylics.add("");
		}
		
		File lylicsFile = new File(Environment.getExternalStorageDirectory()
				.getPath()
				+ "/lrcStorage/"
				+ musicService.currentMusic().getTITLE() + ".txt");
		if (lylicsFile.exists()) {
			try {
				in = new FileReader(lylicsFile);
				reader = new BufferedReader(in);
				String tempLine = "";
				while ((tempLine = reader.readLine()) != null) {
					lylics.add(tempLine);
				}
				reader.close();
				in.close();

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		} else {
			lylics.add(getString(R.string.default_lyrics));
		}
		LylicsAdapter lylicsAdapter = new LylicsAdapter(this.getActivity(),
				lylics);
		lylicsView.setAdapter(lylicsAdapter);
		scrollLylics();
	}

	private void scrollLylics() {
		lylicsHandler.sendEmptyMessage(1);
	}

	private Handler lylicsHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			lylicsView.smoothScrollBy(40, 1000);
			if (!lylicsHandler.hasMessages(1)) {
				//Log.d("cherry", "lyrics roll one times");
				lylicsHandler.sendEmptyMessageDelayed(1, 1000);
			}
			if (lylicsView.getLastVisiblePosition() == lylicsView.getCount() - 1) {
				lylicsHandler.removeMessages(1);
			}
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_play, null);
		timeViewTotal = (TextView) view.findViewById(R.id.end_time_view);
		timeViewCurrent = (TextView) view.findViewById(R.id.start_time_view);
		title = (TextView) view.findViewById(R.id.playing_title);
		artist = (TextView) view.findViewById(R.id.playing_artist);
		seekBar = (SeekBar) view.findViewById(R.id.seekbar);
		playOrPause = (ImageButton) view
				.findViewById(R.id.play_play_or_pause_img_btn);
		pre = (ImageButton) view.findViewById(R.id.play_pre_img_btn);
		next = (ImageButton) view.findViewById(R.id.play_next_img_btn);
		random = (ImageButton) view.findViewById(R.id.play_random_img_btn);
		one_circle = (ImageButton) view
				.findViewById(R.id.play_one_circle_img_btn);
		share = (ImageButton) view.findViewById(R.id.play_share_img_btn);
		lylicsView = (ListView) view.findViewById(R.id.lyrics_view);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Intent service = new Intent(this.getActivity(), PlayService.class);
		this.getActivity().bindService(service, connection,
				this.getActivity().BIND_AUTO_CREATE);
	}

	@Override
	public void onDestroy() {
		this.getActivity().unbindService(connection);
		super.onDestroy();
	}

	public void initEvents(MusicBinder musicService) {

		if (musicService.getMusicList().size() != 0) {
			seekBar.setOnSeekBarChangeListener(this);
			pre.setOnClickListener(this);
			next.setOnClickListener(this);
			playOrPause.setOnClickListener(this);
			share.setOnClickListener(this);
			SimpleDateFormat format = new SimpleDateFormat("mm:ss");
			String allTime = format
					.format(new Date(musicService.getDuration()));
			String current = format.format(new Date(musicService
					.getCurrentPosition()));
			timeViewCurrent.setText(current);
			timeViewTotal.setText(allTime);
			title.setText(musicService.currentMusic().getTITLE());
			artist.setText(musicService.currentMusic().getARTIST());
			seekBar.setMax(musicService.getDuration());
			seekBar.setProgress(musicService.getCurrentPosition());
			initOnekeyShare();
			if (musicService.isPlaying()) {
				if (!handler.hasMessages(0)) {
					handler.removeMessages(0);
					handler.sendEmptyMessage(0);
				}
			}
		}
	}

	public void playOrPause() {
		if (musicService.isPlaying()) {
			musicService.pause();
			handler.removeMessages(0);
			handler.sendEmptyMessage(0);
			lylicsHandler.removeMessages(1);
		} else {
			musicService.play();
			handler.removeMessages(0);
			handler.sendEmptyMessage(0);
			lylicsHandler.sendEmptyMessage(1);
		}
	}
	@Override
	public void onStop() {
		super.onStop();
		lylicsHandler.removeMessages(1);
	}
	public void playmusic(int order) {
		musicService.playMusic(order);
	}

	public void next() {
		musicService.next();
		handler.removeMessages(0);
		handler.sendEmptyMessage(0);
		flashLylics();
	}

	public void pre() {
		musicService.pre();
		handler.removeMessages(0);
		handler.sendEmptyMessage(0);
		flashLylics();
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
	}

	boolean startAgain = false;

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

		if (musicService.isPlaying()) {
			musicService.pause();
			startAgain = true;
		}
		handler.removeMessages(0);
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		Log.d("cherry", "seekbarprogress");
		musicService.seekTo(seekBar.getProgress());
		handler.sendEmptyMessage(0);
		if (!musicService.isPlaying() && startAgain) {
			musicService.play();
		}
		startAgain = false;
	}

	@Override
	public void onClick(View arg0) {

		switch (arg0.getId()) {
		case R.id.play_pre_img_btn:
			pre();
			break;
		case R.id.play_next_img_btn:
			next();
			break;
		case R.id.play_play_or_pause_img_btn:
			playOrPause();
			break;
		case R.id.play_share_img_btn:
			publishOnkeyShare();
			break;
		default:
			break;
		}
	}
}
