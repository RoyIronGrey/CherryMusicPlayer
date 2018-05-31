package com.example.cherrymusicplayer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.Toast;

import com.example.cherrymusicplayer.PlayService.MusicBinder;

public class PageControler extends FragmentActivity implements
		OnPageChangeListener {
	ViewPager viewPager;
	ListView musicListView;
	FragmentPlay fragmentPlay;
	FragmentList fragmentList;
	private long mExitTime;
	FragmentPagerAdapter fragmentPagerAdapter;
	SharedPreferences preferences;
	ArrayList<Fragment> fragments = new ArrayList<Fragment>();
	Intent service;
	MusicBinder musicService;
	int currentModel = 0;
	public static PageControler instance = null;

	private ServiceConnection connection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			if (service instanceof MusicBinder) {
				musicService = (MusicBinder) service;
				musicService.closeButtonNotify();
			}
		}
	};
	// build a filepath on SD
		public void createSDCardDir() {
			if (Environment.MEDIA_MOUNTED.equals(Environment
					.getExternalStorageState())) {
				File sdcardDir = Environment.getExternalStorageDirectory();
				String path = sdcardDir.getPath() + "/lrcStorage";
				File path1 = new File(path);
				if (!path1.exists()) {
					path1.mkdirs();
				}
			} else {
				return;
			}
		}	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		service = new Intent(this, PlayService.class);
		setContentView(R.layout.page_controler);
		fragmentList = new FragmentList();
		fragmentPlay = new FragmentPlay();
		fragments.add(fragmentList);
		fragments.add(fragmentPlay);
		initViews();
		initEvents();
		initDatabase();
		startService(service);
		bindService(service, connection, BIND_AUTO_CREATE);
		instance = this;
		createSDCardDir();
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		PlayService.isActivityOn = true;
		super.onStart();
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		musicService.closeButtonNotify();
		PlayService.isActivityOn = true;
		//Log.d("cherry","Restart----------------------------------------------");
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		//Log.d("cherry","Resume----------------------------------------------");
		super.onResume();
	}

	protected void onStop() {
		super.onStop();
		//Log.d("cherry","Stop----------------------------------------------");
		musicService.showButtonNotify();
		PlayService.isActivityOn = false;
	};

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		// stopService(service);
		super.onDestroy();
		Log.d("cherry","Destroy----------------------------------------------");
		unbindService(connection);
	}

	private void initDatabase() {
		if (preferences.getBoolean("hasDatabase", false)) {
			return;
		}
		flashDatabase();
		Editor hasDatabase = preferences.edit();
		hasDatabase.putBoolean("hasDatabase", true);
		hasDatabase.commit();
	}

	public void flashDatabase() {
		Uri insertUri = Uri
				.parse("content://com.example.cherrymusicplayer/insert");
		Uri deleteUri = Uri
				.parse("content://com.example.cherrymusicplayer/delete");
		ContentResolver cr = this.getContentResolver();
		cr.delete(deleteUri, null, null);
		Cursor musics = cr.query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				new String[] {
						MediaStore.Audio.Media._ID, // int
						MediaStore.Audio.Media.TITLE,
						MediaStore.Audio.Media.ARTIST,
						MediaStore.Audio.Media.ALBUM,
						MediaStore.Audio.Media.DURATION,
						MediaStore.Audio.Media.DATA, // String
						MediaStore.Audio.Media.DISPLAY_NAME, // String
						MediaStore.Audio.Media.MIME_TYPE // String
				}, MediaStore.Audio.Media.IS_MUSIC + " = 1 AND "
						+ MediaStore.Audio.Media.DURATION + " > 10000", null,
				MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		musics.moveToFirst();
		while (!musics.isAfterLast()) {
			ContentValues values = new ContentValues();
			values.put(MusicHelper.COLUMN_TITLE, musics.getString(musics
					.getColumnIndex(MediaStore.Audio.Media.TITLE)));
			values.put(MusicHelper.COLUMN_ALBUM, musics.getString(musics
					.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
			values.put(MusicHelper.COLUMN_DURATION, musics.getLong(musics
					.getColumnIndex(MediaStore.Audio.Media.DURATION)));
			values.put(MusicHelper.COLUMN_ARTIST, musics.getString(musics
					.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
			values.put(MusicHelper.COLUMN_URL, musics.getString(musics
					.getColumnIndex(MediaStore.Audio.Media.DATA)));
			cr.insert(insertUri, values);
			musics.moveToNext();
		}
		musics.close();
	}

	private void initViews() {
		viewPager = (ViewPager) findViewById(R.id.view_pager);
		fragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
			@Override
			public int getCount() {
				return fragments.size();
			}

			@Override
			public Fragment getItem(int arg0) {
				return fragments.get(arg0);
			}
		};
		viewPager.setAdapter(fragmentPagerAdapter);
		viewPager.setCurrentItem(1);
	}

	private void initEvents() {
		viewPager.setOnPageChangeListener(this);
	}
	public void buttonClick(View v) {
		switch (v.getId()) {
		case R.id.play_left_img_btn:
			Log.d("cherry", "play_left_img_btn_clicked");
			viewPager.setCurrentItem(0);
			break;
		case R.id.play_random_img_btn:
			if (currentModel != PlayService.RANDOM) {
				fragmentPlay.musicService.setModel(PlayService.RANDOM);
				currentModel = PlayService.RANDOM;
				fragmentPlay.random
						.setBackgroundResource(R.drawable.random_active);
				fragmentPlay.one_circle
						.setBackgroundResource(R.drawable.one_circle);
				Toast.makeText(this, getString(R.string.model_random), Toast.LENGTH_SHORT).show();
			} else {
				fragmentPlay.musicService.setModel(PlayService.ORDER);
				currentModel = PlayService.ORDER;
				fragmentPlay.random.setBackgroundResource(R.drawable.random);
				Toast.makeText(this, getString(R.string.model_order), Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.play_one_circle_img_btn:
			if (currentModel != PlayService.SINGLE) {
				fragmentPlay.musicService.setModel(PlayService.SINGLE);
				currentModel = PlayService.SINGLE;
				fragmentPlay.one_circle
						.setBackgroundResource(R.drawable.one_circle_active);
				fragmentPlay.random.setBackgroundResource(R.drawable.random);
				Toast.makeText(this, getString(R.string.model_single), Toast.LENGTH_SHORT).show();
			} else {
				fragmentPlay.musicService.setModel(PlayService.ORDER);
				currentModel = PlayService.ORDER;
				fragmentPlay.one_circle
						.setBackgroundResource(R.drawable.one_circle);
				Toast.makeText(this, getString(R.string.model_order), Toast.LENGTH_SHORT).show();
			}
			break;

		case R.id.list_right_img_btn:
			Log.d("cherry", "right_button_clicked");
			viewPager.setCurrentItem(1);
			break;
		}
	}

	List<Music> musicList = new ArrayList<Music>();

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK: {
			if ((System.currentTimeMillis() - mExitTime) > 2000) {
				Object mHelperUtils;
				Toast.makeText(this, getString(R.string.back), Toast.LENGTH_SHORT).show();
				mExitTime = System.currentTimeMillis();
			} else {
				finish();
			}
			return true;
		}
		default:
			break;
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	@Override
	public void onPageSelected(int arg0) {
		switch (arg0) {
		case 0: {
			break;
		}
		case 1: {
			InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			if(getCurrentFocus() != null){
			imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
			}
			fragmentPlay.getHandler().sendEmptyMessage(0);
			fragmentPlay.flashLylics();
			break;
		}
		default:
			break;
		}
	}

}
