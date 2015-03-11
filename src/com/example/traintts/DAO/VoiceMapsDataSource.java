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
			  MySQLiteHelper.COLUMN_SEGMENT,
			  MySQLiteHelper.COLUMN_SIGNAL,
			  MySQLiteHelper.COLUMN_DISTANCE,
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

	  public VoiceMap createVoiceMap(int segment, int signal, double distance, String voice) {
	    ContentValues values = new ContentValues();
	    values.put(MySQLiteHelper.COLUMN_SEGMENT, segment);
	    values.put(MySQLiteHelper.COLUMN_SIGNAL, signal);
	    values.put(MySQLiteHelper.COLUMN_DISTANCE, distance);
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
	  
	  public VoiceMap queryVoiceMapByArgs(int segment, int signal, double distance, String voice){
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_SEGMENT, segment);
		values.put(MySQLiteHelper.COLUMN_SIGNAL, signal);
		values.put(MySQLiteHelper.COLUMN_DISTANCE, distance);
		values.put(MySQLiteHelper.COLUMN_VOICE, voice);
	    Cursor cursor = database.query(MySQLiteHelper.TABLE_VOICEMAPS,
		        allColumns, 
		        MySQLiteHelper.COLUMN_SEGMENT + " = " + segment + " AND " + 
		        MySQLiteHelper.COLUMN_SIGNAL + " = " + signal + " AND " + 
		        MySQLiteHelper.COLUMN_DISTANCE + " >= " + (distance-2) + " AND " + 
		        MySQLiteHelper.COLUMN_DISTANCE + " <= " + (distance+2),
		        null, null, null, null);
	    cursor.moveToFirst();
	    VoiceMap newVoiceMap = null;
	    if (cursor.getCount() > 0){
	    	// More than 0 rows and the query is available
	    	newVoiceMap = cursorToVoiceMap(cursor);
	    }
	    cursor.close();
	    return newVoiceMap; 
	  }
	  
	  public void updateVoiceMapByArgs(int segment, int signal, double distance, String voice){
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_SEGMENT, segment);
		values.put(MySQLiteHelper.COLUMN_SIGNAL, signal);
		values.put(MySQLiteHelper.COLUMN_DISTANCE, distance);
		values.put(MySQLiteHelper.COLUMN_VOICE, voice);
		ContentValues args = new ContentValues();
		args.put(MySQLiteHelper.COLUMN_VOICE, voice);
	    database.update(MySQLiteHelper.TABLE_VOICEMAPS,
	    		args, 
		        MySQLiteHelper.COLUMN_SEGMENT + " = " + segment + " AND " + 
		        MySQLiteHelper.COLUMN_SIGNAL + " = " + signal + " AND " + 
		        MySQLiteHelper.COLUMN_DISTANCE + " = " + distance ,
		        null);
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
		  voiceMap.setSegment(cursor.getInt(1));
		  voiceMap.setSignal(cursor.getInt(2));
		  voiceMap.setDistance(cursor.getDouble(3));
		  voiceMap.setVoice(cursor.getString(4));
		  return voiceMap;
	  }
}
