package com.example.traintts.DAO;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {

	  public static final String TABLE_VOICEMAPS = "VOICEMAPS";
	  public static final String COLUMN_ID = "_id";
	  public static final String COLUMN_ARG0 = "ARG0";
	  public static final String COLUMN_ARG1 = "ARG1";
	  public static final String COLUMN_ARG2 = "ARG2";
	  public static final String COLUMN_VOICE = "VOICE";

	  private static final String DATABASE_NAME = "VOICEMAPS.db";
	  private static final int DATABASE_VERSION = 1;

	  // Database creation sql statement
	  private static final String DATABASE_CREATE = "create table "
	      + TABLE_VOICEMAPS + "(" 
		      + COLUMN_ID + " integer primary key autoincrement, "
		      + COLUMN_ARG0 + " int not null, "
		      + COLUMN_ARG1 + " int not null, "
		      + COLUMN_ARG2 + " double not null, "
		      + COLUMN_VOICE + " text not null" + ");";

	  public MySQLiteHelper(Context context) {
	    super(context, DATABASE_NAME, null, DATABASE_VERSION);
	  }

	  @Override
	  public void onCreate(SQLiteDatabase database) {
	    database.execSQL(DATABASE_CREATE);
	  }

	  @Override
	  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    Log.w(MySQLiteHelper.class.getName(),
	        "Upgrading database from version " + oldVersion + " to "
	            + newVersion + ", which will destroy all old data");
	    db.execSQL("DROP TABLE IF EXISTS " + TABLE_VOICEMAPS);
	    onCreate(db);
	  }

	} 