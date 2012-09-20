package edu.buffalo.cse.cse486_586.provider;

import java.util.HashMap;

import edu.buffalo.cse.cse486_586.Message;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class MessageProvider extends ContentProvider {

	private static UriMatcher UriCheck;
	private static final String dbName = "MessageDB";
	private static final int dbVersion = 12;
	private static final String tableName = "MessageT";
	private static final int messages = 1;
	
	// Content Provider URI
	public static final String AUTHORITY = "edu.buffalo.cse.cse486_586.provider.MessageProvider";
	public static HashMap<String, String> msgProjMap;	

	// SQL query to create the SQLite Database
	private static final String sqlCreateDB = "CREATE TABLE " + tableName + "("
			+ Message.MSG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ Message.provider_key + " INTEGER," + Message.provider_value
			+ " LONGTEXT );";
	
	
	
	private static class MyDB extends SQLiteOpenHelper {

		public MyDB(Context context) {
			super(context, dbName, null, dbVersion);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			db.execSQL(sqlCreateDB);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			db.execSQL("DROP TABLE IF EXISTS " + tableName);
			onCreate(db);

		}

	}

	MyDB createDB;

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		if (UriCheck.match(uri) != messages)
			throw new IllegalArgumentException("Arguments are wrong");
		ContentValues insertV;
		if (values != null)
			insertV = new ContentValues(values);
		else
			insertV = new ContentValues();

		SQLiteDatabase writeDB = createDB.getWritableDatabase();
		long rID = writeDB.insert(tableName, null, insertV);
		if (rID > 0) {
			Uri _uri = ContentUris.withAppendedId(uri, rID);
			getContext().getContentResolver().notifyChange(Message.CONTENT_URI,null);
			return _uri;
		}
		throw new SQLException("Failed to insert into " + uri);
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		createDB = new MyDB(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String keyToRead,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		SQLiteQueryBuilder queryDB = new SQLiteQueryBuilder();

		/*
		 * if (UriCheck.match(uri) == 1) queryDB.setTables(tableName); else
		 * return null;
		 */
		switch (UriCheck.match(uri)) {
		case messages:
			queryDB.setTables(tableName);
			queryDB.setProjectionMap(msgProjMap);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);

		}
		SQLiteDatabase readDB = createDB.getReadableDatabase();
		Cursor cursor = queryDB.query(readDB, projection, keyToRead,
				selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	static {
		UriCheck = new UriMatcher(UriMatcher.NO_MATCH);
		UriCheck.addURI(AUTHORITY, tableName, messages);

		msgProjMap = new HashMap<String, String>();
		msgProjMap.put(Message.MSG_ID, Message.MSG_ID);
		msgProjMap.put(Message.provider_key, Message.provider_key);
		msgProjMap.put(Message.provider_value, Message.provider_value);
	}

}