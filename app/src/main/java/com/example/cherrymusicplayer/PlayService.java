package com.example.cherrymusicplayer;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

public class PlayService extends Service implements PlayServiceInterface,
		OnCompletionListener {

	public NotificationManager mNotificationManager;
	public ButtonBroadcastReceiver bReceiver;

	public final static String ACTION_BUTTON = "com.notification.intent.action.ButtonClick";

	public final static String INTENT_BUTTONID_TAG = "ButtonId";

	public final static int BUTTON_PREV_ID = 1;
	public final static int BUTTON_PALY_ID = 2;
	public final static int BUTTON_NEXT_ID = 3;
	public final static int BUTTON_CLEAN = 4;
	public final static int BUTTON_TO_MAIN = 5;

	public static final int ORDER = 0;
	public static final int SINGLE = 1;
	public static final int RANDOM = 2;
	
	public static boolean isActivityOn = false;

	RemoteViews mRemoteViews;

	private MediaPlayer mediaPlayer;
	private MusicBinder binder;
	public List<Music> musicList;
	int current = 0;
	int model;
	SharedPreferences preference;

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			//Log.d("cherry", currentMusic().getTITLE());
			if(musicList.size() == 0){
				return;
			}
			mRemoteViews.setTextViewText(R.id.notify_title, currentMusic()
					.getTITLE());
			reShowButtonNotify();
			handler.removeMessages(0);
		};
	};

	public static void collapseStatusBar(Context context) {
		try {
			Object statusBarManager = context.getSystemService("statusbar");
			Method collapse;

			if (Build.VERSION.SDK_INT <= 16) {
				collapse = statusBarManager.getClass().getMethod("collapse");
			} else {
				collapse = statusBarManager.getClass().getMethod(
						"collapsePanels");
			}
			collapse.invoke(statusBarManager);
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	public class ButtonBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			if (action.equals(ACTION_BUTTON)) {

				int buttonId = intent.getIntExtra(INTENT_BUTTONID_TAG, 0);
				switch (buttonId) {
				case BUTTON_PREV_ID:
					pre();
					mRemoteViews.setImageViewResource(
							R.id.notify_playORpause_img_btn,
							R.drawable.notify_pause_img_btn);
//					handler.removeMessages(0);
//					handler.sendEmptyMessage(0);
					break;
				case BUTTON_PALY_ID:
					if (isPlaying()) {
						pause();
						mRemoteViews.setImageViewResource(
								R.id.notify_playORpause_img_btn,
								R.drawable.notify_play_img_btn);
					} else {
						play();
						mRemoteViews.setImageViewResource(
								R.id.notify_playORpause_img_btn,
								R.drawable.notify_pause_img_btn);
					}
//					handler.removeMessages(0);
					handler.sendEmptyMessage(0);
//					handler.removeMessages(0);
					break;
				case BUTTON_NEXT_ID:
					next();
					mRemoteViews.setImageViewResource(
							R.id.notify_playORpause_img_btn,
							R.drawable.notify_pause_img_btn);
//					handler.removeMessages(0);
//					handler.sendEmptyMessage(0);
					break;
				case BUTTON_CLEAN:
					// Toast.LENGTH_SHORT).show();
					PageControler.instance.finish();
					mediaPlayer.stop();
					mediaPlayer = null;
					if (bReceiver != null) {
						unregisterReceiver(bReceiver);
					}
					PlayService.this.stopSelf();
					break;
				case BUTTON_TO_MAIN:
					Toast.makeText(getApplicationContext(),
							getString(R.string.toMain), Toast.LENGTH_SHORT)
							.show();
					Intent activity_intent = new Intent(getBaseContext(),
							PageControler.class);
					activity_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(activity_intent);
					collapseStatusBar(getBaseContext());
				default:
					break;
				}
			}
		}
	}

	public class MusicBinder extends Binder {
		public void play() {
			PlayService.this.play();
		}

		public void pause() {
			PlayService.this.pause();
		}

		public void stop() {
			PlayService.this.stop();
		}

		public void seekTo(int where) {
			PlayService.this.seekTo(where);
		}

		public int getDuration() {
			return PlayService.this.getDuration();
		}

		public int getCurrentPosition() {
			return PlayService.this.getCurrentPosition();
		}

		public boolean isPlaying() {
			return PlayService.this.isPlaying();
		}

		public void playMusic(int order) {
			PlayService.this.playMusic(order);
		}

		public void setModel(int model) {
			PlayService.this.setModel(model);
		}

		public void next() {
			PlayService.this.next();
		}

		public void pre() {
			PlayService.this.pre();
		}

		public Music currentMusic() {
			return PlayService.this.currentMusic();
		}

		public List<Music> getMusicList() {
			return PlayService.this.musicList;
		}

		public void showButtonNotify() {
			PlayService.this.showButtonNotify();
		}

		public void closeButtonNotify() {
			PlayService.this.closeButtonNotify();
		}

		public void flashList() {
			PlayService.this.flashList();
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		if (binder == null) {
			binder = new MusicBinder();
		}
		return binder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return true;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d("cherry", "=====create service=====");
		init();
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		closeButtonNotify();
	}

	private void init() {
		preference = PreferenceManager.getDefaultSharedPreferences(this);

		bReceiver = new ButtonBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ACTION_BUTTON);
		registerReceiver(bReceiver, intentFilter);

		flashList();
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setOnCompletionListener(this);
		current = preference.getInt("lastMusic", 0);
		model = preference.getInt("model", ORDER);
		if (musicList.size() != 0) {
			prepareMusic(current);
		}
	}

	public void flashList() {
		musicList = new ArrayList<Music>();
		musicList.clear();
		Uri uri = Uri.parse("content://com.example.cherrymusicplayer/query");
		Cursor cursor = getContentResolver().query(uri, null, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				Music music = new Music();
				int _id = cursor.getInt(cursor
						.getColumnIndex(MusicHelper.COLUMN_ID));
				String title = cursor.getString(cursor
						.getColumnIndex(MusicHelper.COLUMN_TITLE));
				String artist = cursor.getString(cursor
						.getColumnIndex(MusicHelper.COLUMN_ARTIST));
				String album = cursor.getString(cursor
						.getColumnIndex(MusicHelper.COLUMN_ALBUM));
				String url = cursor.getString(cursor
						.getColumnIndex(MusicHelper.COLUMN_URL));
				int duration = cursor.getInt(cursor
						.getColumnIndex(MusicHelper.COLUMN_DURATION));
				music.set_ID(_id);
				music.setARTIST(artist);
				music.setALBUM(album);
				music.setDURATION(duration);
				music.setTITLE(title);
				music.setURL(url);
				music.setPostion(musicList.size());
				musicList.add(music);
			} while (cursor.moveToNext());
		}
		cursor.close();
	}

	public void play() {
		if (!mediaPlayer.isPlaying()) {
			mediaPlayer.start();
		}
	}

	public void pause() {
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.pause();
		}
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		if (mediaPlayer != null && mediaPlayer.isPlaying()) {
			mediaPlayer.stop();
			mediaPlayer = null;
		}
	}

	@Override
	public void seekTo(int where) {
		if (mediaPlayer != null) {
			mediaPlayer.seekTo(where);
		}
	}

	@Override
	public void onDestroy() {
		Log.d("cherry", "serviceDestory-------------------------------------");
		super.onDestroy();
		closeButtonNotify();
		Editor edit = preference.edit();
		edit.putInt("lastMusic", current);
		edit.putInt("model", model);
		edit.commit();
	}

	@Override
	public int getDuration() {
		int result = 0;
		if (mediaPlayer != null) {
			result = mediaPlayer.getDuration();
		}
		return result;
	}

	@Override
	public int getCurrentPosition() {
		int result = 0;
		if (mediaPlayer != null) {
			result = mediaPlayer.getCurrentPosition();
		}
		return result;
	}

	@Override
	public boolean isPlaying() {
		// TODO Auto-generated method stub
		boolean result = false;
		if (mediaPlayer != null) {
			result = mediaPlayer.isPlaying();
		}
		return result;
	}

	@Override
	public void next() {
		if(musicList.size() == 0){
			return;
		}
		if (model == RANDOM) {
			current = new Random().nextInt(musicList.size());
		} else if (current + 1 >= musicList.size()) {
			current = 0;
		} else {
			current++;
		}
		playMusic(current);
	}

	@Override
	public void pre() {
		if(musicList.size() == 0){
			return;
		}
		if (model == RANDOM) {
			current = new Random().nextInt(musicList.size());
		} else if (current - 1 < 0) {
			current = musicList.size() - 1;
		} else {
			current--;
		}
		playMusic(current);
	}

	@Override
	public void setModel(int model) {
		this.model = model;
	}

	@Override
	public void prepareMusic(int order) {
		mediaPlayer.reset();
		try {
			mediaPlayer.setDataSource(musicList.get(order).getURL());
			mediaPlayer.prepare();
			mediaPlayer.setLooping(false);
			int currentPostion = mediaPlayer.getCurrentPosition();
			mediaPlayer.seekTo(currentPostion);
			current = order;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void playMusic(int order) {
		prepareMusic(order);
		mediaPlayer.start();
		if(isActivityOn == false){
			handler.sendEmptyMessage(0);
//			handler.removeMessages(0);
		}
	}

	@Override
	public Music currentMusic() {
		// Log.d("cherry",
		// "currentMusic--------------------------------------------");
		return musicList.get(current);
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		switch (model) {
		case ORDER: {
			if (musicList.size() != 0) {
				next();
			}
			break;
		}
		case SINGLE: {
			playMusic(current);
			break;
		}
		case RANDOM: {
			current = new Random().nextInt(musicList.size());
			playMusic(current);
			break;
		}
		default:
			break;
		}
	}

	public void closeButtonNotify() {
//		handler.removeMessages(0);
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.cancel(200);
	}

	public void initButtonNotify() {
		mRemoteViews = new RemoteViews(getPackageName(), R.layout.notification);

		Intent buttonIntent = new Intent(ACTION_BUTTON);

		buttonIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_PREV_ID);

		PendingIntent intent_prev = PendingIntent.getBroadcast(this, 1,
				buttonIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		mRemoteViews.setOnClickPendingIntent(R.id.notify_pre_img_btn,
				intent_prev);

		buttonIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_PALY_ID);
		PendingIntent intent_paly = PendingIntent.getBroadcast(this, 2,
				buttonIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		mRemoteViews.setOnClickPendingIntent(R.id.notify_playORpause_img_btn,
				intent_paly);

		buttonIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_NEXT_ID);
		PendingIntent intent_next = PendingIntent.getBroadcast(this, 3,
				buttonIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		mRemoteViews.setOnClickPendingIntent(R.id.notify_next_img_btn,
				intent_next);

		buttonIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_CLEAN);
		PendingIntent clean = PendingIntent.getBroadcast(this, 4, buttonIntent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		mRemoteViews.setOnClickPendingIntent(R.id.notify_close_img_btn, clean);

		buttonIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_TO_MAIN);
		PendingIntent tomain = PendingIntent.getBroadcast(this, 5,
				buttonIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		mRemoteViews.setOnClickPendingIntent(R.id.notify_title, tomain);

		if (isPlaying()) {
			mRemoteViews.setImageViewResource(R.id.notify_playORpause_img_btn,
					R.drawable.notify_pause_img_btn);
		} else {
			mRemoteViews.setImageViewResource(R.id.notify_playORpause_img_btn,
					R.drawable.notify_play_img_btn);
		}
	}

	public void reShowButtonNotify() {
		if (BaseTools.getSystemVersion() <= 9) {
			mRemoteViews.setViewVisibility(R.id.notify, View.GONE);
		} else {
			mRemoteViews.setViewVisibility(R.id.notify, View.VISIBLE);
		}
		NotificationCompat.Builder mBuilder = new Builder(this);
		mBuilder.setContent(mRemoteViews)
				.setContentIntent(
						getDefalutIntent(Notification.FLAG_ONGOING_EVENT))
				.setWhen(System.currentTimeMillis())
				.setTicker(getString(R.string.notify))
				.setPriority(Notification.PRIORITY_DEFAULT).setOngoing(true)
				.setAutoCancel(true).setSmallIcon(R.drawable.logo);

		Notification notify = mBuilder.build();
		// notify.flags = Notification.FLAG_ONGOING_EVENT;

		mNotificationManager.notify(200, notify);
	}

	public void showButtonNotify() {
		initButtonNotify();
		handler.sendEmptyMessage(0);
		reShowButtonNotify();
	}

	public PendingIntent getDefalutIntent(int flags) {
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 1,
				new Intent(), flags);
		return pendingIntent;
	}

}
