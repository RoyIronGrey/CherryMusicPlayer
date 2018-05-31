package com.example.cherrymusicplayer;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class MusicProvider extends ContentProvider {
	private static final String authority = "com.example.cherrymusicplayer";

	private static final int QUERY_ALL_CODE = 1;
	private static final int QUERY_BY_ID = 2;
	private static final int INSERT_CODE = 3;
	private static final int DELETE_CODE = 4;
	private static final int MODIFY_CODE = 5;

	private static final UriMatcher URI_MATCHER = new UriMatcher(
			UriMatcher.NO_MATCH);

	static {
		URI_MATCHER.addURI(authority, "query", QUERY_ALL_CODE);
		URI_MATCHER.addURI(authority, "query/#", QUERY_BY_ID);
		URI_MATCHER.addURI(authority, "insert", INSERT_CODE);
		URI_MATCHER.addURI(authority, "delete", DELETE_CODE);
		URI_MATCHER.addURI(authority, "modify", MODIFY_CODE);
	}

	private MusicHelper helper;

	@Override
	public boolean onCreate() {
		helper = new MusicHelper(getContext());
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		Cursor result = null;
		int code = URI_MATCHER.match(uri);
		SQLiteDatabase database = helper.getReadableDatabase();
		switch (code) {
		case QUERY_ALL_CODE: {
			result = database.query(MusicHelper.TABLE_NAME, null, null, null,
					null, null, sortOrder);
			break;
		}
		case QUERY_BY_ID: {
			result = database.query(MusicHelper.TABLE_NAME, null,
					MusicHelper.COLUMN_ID + "=?", new String[] { uri
							.getPathSegments().get(1) }, null, null, sortOrder);
			break;
		}
		default:
			break;
		}
		return result;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int code = URI_MATCHER.match(uri);
		SQLiteDatabase database = helper.getWritableDatabase();
		switch (code) {
		case INSERT_CODE:
			database.insert(MusicHelper.TABLE_NAME, null, values);
			break;
		default:
			break;
		}
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int code = URI_MATCHER.match(uri);
		int count = 0;
		SQLiteDatabase database = helper.getWritableDatabase();
		switch (code) {
		case DELETE_CODE:
			count = database.delete(MusicHelper.TABLE_NAME, null, null);
			break;

		default:
			break;
		}
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return 0;
	}

}
