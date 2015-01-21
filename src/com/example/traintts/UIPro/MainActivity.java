package com.example.traintts.UIPro;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.example.TrainTTS.R;
import com.example.traintts.DAO.VoiceMap;
import com.example.traintts.DAO.VoiceMapsDataSource;
import com.example.traintts.utils.FileHelper;
import com.ipaulpro.afilechooser.utils.FileUtils;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	// TTS objects
	private TextToSpeech TTSObject;
	private VoiceMapsDataSource datasource;
	private final String NORECORD = "没有记录";
	
	// CVS file reader
	private FileHelper CVSHelper = new FileHelper();
	
	// Variables for Clock on the top
	TextView timeDateText;
	String TAG = "TimeDate";
	String UPDATE = "updateTime";

	// TextViews objects
	private TextView duan;
	private TextView jie;
	private TextView distance;
	private TextView operation;

	// The uiHander to update duan, jie, distance
    private Handler uiHandler = new UIHandler();

    class UIHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            // a message is received; update UI text view
        	String[] msgParts = msg.obj.toString().split(",");
        	VoiceMap voiceMap = datasource.queryVoiceMapByArgs(
        			Integer.parseInt(msgParts[0]), 
        			Integer.parseInt(msgParts[1]),
        			Double.parseDouble(msgParts[2]), "");
        	speakText(voiceMap != null ? voiceMap.getVoice() : NORECORD);
            duan.setText(msgParts[0]);
            jie.setText(msgParts[1]);
            distance.setText(msgParts[2]);
            if (voiceMap != null && !voiceMap.getVoice().equals("")){
            	operation.setText(voiceMap.getVoice());
            } else {
            	operation.setText(NORECORD);
            }
            
            super.handleMessage(msg);
        }
    }

    // Serial query thread 
    private Thread serialQueryThread;
    
	private BroadcastReceiver myReceiver = new BroadcastReceiver(){
	
		@Override
		public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
			if(intent.getAction().equals(UPDATE))
			{
//				Log.d(TAG, "receive update time message");
		        Date date = new Date();
		        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");    
		        timeDateText.setText(dateFormat.format(date));
			}
		}    	
	};
	
    private Thread myThread = new Thread(){
    	public void run()
    	{
    		while(true)
    		{
 //      		  Log.d(TAG, "running");
        		try {
    				Thread.sleep(1000);
    				Intent intent = new Intent();
    				intent.setAction(UPDATE);
    				sendBroadcast(intent);
    			} catch (InterruptedException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    		}
    	}
    };

    
    private static final int REQUEST_CODE = 9898; // onActivityResult request
    // code
    

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
				
		// Initialize the TTS object
		TTSObject = new TextToSpeech(getApplicationContext(), 
		new TextToSpeech.OnInitListener() {
			@Override
			public void onInit(int status) {
			   if(status != TextToSpeech.ERROR){
				   TTSObject.setLanguage(Locale.CHINA);
			   }			
			}
		});
		
		// Initialize the data source
	    datasource = new VoiceMapsDataSource(this);
	    datasource.open();
	    
	    // Save CSV file to datasource
//	    try {
//			CVSHelper.saveCVSFileToDatasource(
//					new InputStreamReader(getAssets().open("data.csv")), 
//					datasource);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	    List<VoiceMap> values = datasource.getAllVoiceMaps();
	    
		// Initialize clock on the top 
		timeDateText = (TextView)findViewById(R.id.timedate);
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");    
        timeDateText.setText(dateFormat.format(date));
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UPDATE);
        this.registerReceiver(myReceiver, intentFilter);
        myThread.start();
        
        // Initialize textviews
        duan = (TextView)findViewById(R.id.Duan);
        jie = (TextView)findViewById(R.id.Jie);
        distance = (TextView)findViewById(R.id.Distance);
        operation = (TextView)findViewById(R.id.Operation);
        
        // Initialize serial query thread
        serialQueryThread = getSerialQueryThread();
        serialQueryThread.start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuItem addData = menu.add(0, 1, 1, "载入数据");
		addData.setOnMenuItemClickListener(new OnMenuItemClickListener() {

	        @Override
	        public boolean onMenuItemClick(MenuItem item) {
                // Display the file chooser dialog
                showChooser();
	            return true;
	        }
	    });
		MenuItem exit = menu.add(0, 2, 2, "退出");
		exit.setOnMenuItemClickListener(new OnMenuItemClickListener() {

	        @Override
	        public boolean onMenuItemClick(MenuItem item) {
                // Display the file chooser dialog
                System.exit(0);
	            return true;
	        }
	    });
		addData.setIcon(R.drawable.add);
		exit.setIcon(R.drawable.exit);
		return true;
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		this.unregisterReceiver(myReceiver);
		super.onDestroy();
	}


	// Useful method to speak a sentence
	public void speakText(String toSpeak){
		if (toSpeak == null || toSpeak.equals("")) {
			toSpeak = NORECORD;
		}
		Toast.makeText(getApplicationContext(), toSpeak, 
		Toast.LENGTH_SHORT).show();
		TTSObject.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
	}

	// Serial query thread methods to create and restart the thread
	private Thread getSerialQueryThread(){
		 return new Thread() {
			public void run() {
				for (;;){
					// create message which will be send to handler

					BufferedReader reader = null;
					try {
					    reader = new BufferedReader(
					        new InputStreamReader(getAssets().open("tty")));

					    // do reading, usually loop until end of file reading  
					    String mLine = reader.readLine();
					    while (mLine != null) {
					       //process line
						   Message msg = Message.obtain(uiHandler);
					       Log.d("", mLine);
					       msg.obj = mLine;
					       uiHandler.sendMessage(msg);
					       mLine = reader.readLine(); 
					       try {
							Thread.sleep(6000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
					    }
					} catch (IOException e) {
					    //log the exception
					} finally {
					    if (reader != null) {
					         try {
					             reader.close();
					         } catch (IOException e) {
					             //log the exception
					         }
					    }
					}
				}
			}
		};
	}
	
	// Universe method to restart any thread
	private void restartThread(Thread thread){
		thread.interrupt();
		thread = null;
		thread = getSerialQueryThread();
		thread.start();
	}
	
	// File chooser for the CVS file reader
    private void showChooser() {
        // Use the GET_CONTENT intent from the utility class
        Intent target = FileUtils.createGetContentIntent();
        // Create the chooser Intent
        Intent intent = Intent.createChooser(
                target, ("Choose a CVS file"));
        try {
            startActivityForResult(intent, REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            // The reason for the existence of aFileChooser
        }
    }
    
    // File uri for CVS reader
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE:
                // If the file selection was successful
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        // Get the URI of the selected file
                        final Uri uri = data.getData();
                        Log.i(TAG, "Uri = " + uri.toString());
                        try {
                            // Get the file path from the URI
                            final String path = FileUtils.getPath(this, uri);
                            Toast.makeText(MainActivity.this,
                                    "File Selected: " + path, Toast.LENGTH_LONG).show();
                    	    try {
                    	    	InputStream is = new FileInputStream(path);
                    	    	InputStreamReader reader = new InputStreamReader(is);
                    			CVSHelper.saveCVSFileToDatasource(reader, datasource);
                    		} catch (IOException e) {
                    			// TODO Auto-generated catch block
                    			e.printStackTrace();
                    		}
                        } catch (Exception e) {
                            Log.e("FileSelectorTestActivity", "File select error", e);
                        }
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
