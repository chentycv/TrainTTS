package com.example.traintts.UI;

import java.util.List;
import java.util.Locale;
import java.util.Random;

import com.example.traintts.R;
import com.example.traintts.DAO.VoiceMap;
import com.example.traintts.DAO.VoiceMapsDataSource;

import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.Toast;

public class MainActivity extends ListActivity {
	
    /**
     * This field should be made private, so it is hidden from the SDK.
     * {@hide}
     */
    protected ListAdapter mAdapter;
	
	private TextToSpeech TTSObject;
	private VoiceMapsDataSource datasource;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Initlizate the TTS object
		TTSObject = new TextToSpeech(getApplicationContext(), 
		new TextToSpeech.OnInitListener() {
			@Override
			public void onInit(int status) {
			   if(status != TextToSpeech.ERROR){
				   TTSObject.setLanguage(Locale.CHINA);
			   }			
			}
		});
		
		// Initlizate the data source
	    datasource = new VoiceMapsDataSource(this);
	    datasource.open();
	    List<VoiceMap> values = datasource.getAllVoiceMaps();
	    
	    // use the SimpleCursorAdapter to show the
	    // elements in a ListView
	    ArrayAdapter<VoiceMap> adapter = new ArrayAdapter<VoiceMap>(this,
	        android.R.layout.simple_list_item_1, values);
	    setListAdapter(adapter);
	}

	// Will be called via the onClick attribute
	// of the buttons in main.xml
	public void onClick(View view) {
    @SuppressWarnings("unchecked")
    	ArrayAdapter<VoiceMap> adapter = (ArrayAdapter<VoiceMap>) getListAdapter();
    	VoiceMap voiceMap = null;
    	switch (view.getId()) {
    		case R.id.add:
    			String[] comments = new String[] { "很好", "测试", "长一点的文字" };
    			int nextInt = new Random().nextInt(comments.length);
    			// save the new comment to the database
    			voiceMap = datasource.createVoiceMap(1, 2, 3.1f, comments[nextInt]);
    			speakText("添加" + comments[nextInt]);
    			adapter.add(voiceMap);
    			break;
    		case R.id.delete:
    			if (getListAdapter().getCount() > 0) {
    				voiceMap = (VoiceMap) getListAdapter().getItem(0);
    				speakText("删除" + voiceMap.getVoice());
    				datasource.deleteVoiceMap(voiceMap);
    				adapter.remove(voiceMap);
    			}
    			break;
    	}
    	adapter.notifyDataSetChanged();
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void speakText(String toSpeak){
		Toast.makeText(getApplicationContext(), toSpeak, 
		Toast.LENGTH_SHORT).show();
		TTSObject.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
	}
}
