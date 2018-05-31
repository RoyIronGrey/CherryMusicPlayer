package com.example.cherrymusicplayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MusicHelper extends SQLiteOpenHelper {
	private static final String DB_NAME = "music.db";
	private static final int DB_VERSION = 1;

	public static final String TABLE_NAME = "music";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_ALBUM = "album";
	public static final String COLUMN_DURATION = "duration";
	public static final String COLUMN_ARTIST = "artist";
	public static final String COLUMN_URL = "url";

	public static final String[] COLUMNS = { COLUMN_ID, COLUMN_TITLE,
			COLUMN_ALBUM, COLUMN_DURATION, COLUMN_ARTIST, COLUMN_URL };

	public MusicHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		//Log.d("cherry", "init music database");
		db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + COLUMN_ID
				+ " INTEGER PRIMARY KEY," + COLUMN_TITLE + " TEXT,"
				+ COLUMN_ALBUM + " TEXT," + COLUMN_DURATION + " INTEGER,"
				+ COLUMN_ARTIST + " TEXT," + COLUMN_URL + " TEXT)");
		init(db);
	}

	private void init(SQLiteDatabase db) {
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

}
