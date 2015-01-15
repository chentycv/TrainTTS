package com.example.traintts.DAO;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class VoiceMapsDataSource {
	
	  // Database fields
	  private SQLiteDatabase database;
	  private MySQLiteHelper dbHelper;
	  private String[] allColumns = { 
			  MySQLiteHelper.COLUMN_ID,
			  MySQLiteHelper.COLUMN_ARG0,
			  MySQLiteHelper.COLUMN_ARG1,
			  MySQLiteHelper.COLUMN_ARG2,
			  MySQLiteHelper.COLUMN_VOICE };

	  public VoiceMapsDataSource(Context context) {
	    dbHelper = new MySQLiteHelper(context);
	  }

	  public void open() throws SQLException {
	    database = dbHelper.getWritableDatabase();
	  }

	  public void close() {
	    dbHelper.close();
	  }

	  public VoiceMap createVoiceMap(int arg0, int arg1, double arg2, String voice) {
	    ContentValues values = new ContentValues();
	    values.put(MySQLiteHelper.COLUMN_ARG0, arg0);
	    values.put(MySQLiteHelper.COLUMN_ARG1, arg1);
	    values.put(MySQLiteHelper.COLUMN_ARG2, arg2);
	    values.put(MySQLiteHelper.COLUMN_VOICE, voice);
	    long insertId = database.insert(MySQLiteHelper.TABLE_VOICEMAPS, null,
	        values);
	    Cursor cursor = database.query(MySQLiteHelper.TABLE_VOICEMAPS,
	        allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
	        null, null, null);
	    cursor.moveToFirst();
	    VoiceMap newVoiceMap = cursorToVoiceMap(cursor);
	    cursor.close();
	    return newVoiceMap;
	  }

	  public void deleteVoiceMap(VoiceMap voiceMap) {
	    long id = voiceMap.getId();
	    System.out.println("Voice map deleted with id: " + id);
	    database.delete(MySQLiteHelper.TABLE_VOICEMAPS, MySQLiteHelper.COLUMN_ID
	        + " = " + id, null);
	  }

	  public List<VoiceMap> getAllVoiceMaps() {
	    List<VoiceMap> voiceMaps = new ArrayList<VoiceMap>();

	    Cursor cursor = database.query(MySQLiteHelper.TABLE_VOICEMAPS,
	        allColumns, null, null, null, null, null);

	    cursor.moveToFirst();
	    while (!cursor.isAfterLast()) {
	      VoiceMap voiceMap = cursorToVoiceMap(cursor);
	      voiceMaps.add(voiceMap);
	      cursor.moveToNext();
	    }
	    
	    // make sure to close the cursor
	    cursor.close();
	    return voiceMaps;
	  }

	  private VoiceMap cursorToVoiceMap(Cursor cursor) {
		  VoiceMap voiceMap = new VoiceMap();
		  voiceMap.setId(cursor.getLong(0));
		  voiceMap.setArg0(cursor.getInt(1));
		  voiceMap.setArg1(cursor.getInt(2));
		  voiceMap.setArg2(cursor.getDouble(3));
		  voiceMap.setVoice(cursor.getString(4));
		  return voiceMap;
	  }
}
