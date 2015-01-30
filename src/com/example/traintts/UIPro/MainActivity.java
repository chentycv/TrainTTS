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

import com.example.traintts.R;
import com.example.traintts.DAO.VoiceMap;
import com.example.traintts.DAO.VoiceMapsDataSource;
import com.example.traintts.media.RecMicToMp3;
import com.example.traintts.utils.FileHelper;
import com.ipaulpro.afilechooser.utils.FileUtils;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
	
	/*
	 * A tag to filter logs in logcat
	 */
	String TAG = "TrainTTSMainActivity";
	
	/*
	 *  TTS objects to store the entity of TTS engine 
	 *  Voice map data source is map from "duan" "jie" "distance" to a "sentence"
	 */
	private TextToSpeech TTSObject;
	private VoiceMapsDataSource datasource;
	private final String NORECORD = "没有记录";
	
	/*
	 *  CVS file reader
	 */
	private FileHelper CVSHelper = new FileHelper();
	
	/*
	 *  Variables for Clock on the top
	 *  A textview to display time
	 */
	TextView timeDateText;
	String UPDATE = "updateTime";

	private BroadcastReceiver myReceiver = new BroadcastReceiver(){
		
		@Override
		public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
			if(intent.getAction().equals(UPDATE))
			{
		        Date date = new Date();
		        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");    
		        timeDateText.setText(dateFormat.format(date));
			}
		}    	
	};
	
	/*
	 *  TextViews objects for duan jie distance operation
	 */
	private TextView duan;
	private TextView jie;
	private TextView distance;
	private TextView operation;

	/*
	 *  The uiHander to update duan, jie, distance
	 */
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
        	speakText(voiceMap != null ? voiceMap.getVoice() : "");
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

    /*
     *  Serial query thread 
     */
    private Thread serialQueryThread;

    
    /*
     * Test unit to emulate the serial signal to query
     */
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

    /*
     * A Request code for file chooser to identity the result that the file chooser activity returned
     */
    private static final int REQUEST_CODE = 9898; // onActivityResult request code
    
    
    
	/*
	 *  The object to record mic input to the /sdcard/mezzo.mp3 with 8000hz sample rate
	 */
	private RecMicToMp3 mRecMicToMp3 = new RecMicToMp3(
			Environment.getExternalStorageDirectory() + "/mezzo.mp3", 8000);


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
        
        // Initialize textviews of duan jie distance operation
        duan = (TextView)findViewById(R.id.Duan);
        jie = (TextView)findViewById(R.id.Jie);
        distance = (TextView)findViewById(R.id.Distance);
        operation = (TextView)findViewById(R.id.Operation);
        
        // Initialize serial query thread
        serialQueryThread = getSerialQueryThread();
        serialQueryThread.start();
        
        // Binder the handler to record thread 
        mRecMicToMp3.setHandle(new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case RecMicToMp3.MSG_REC_STARTED:
//					statusTextView.setText("録音中");
					break;
				case RecMicToMp3.MSG_REC_STOPPED:
//					statusTextView.setText("");
					break;
				case RecMicToMp3.MSG_ERROR_GET_MIN_BUFFERSIZE:
//					statusTextView.setText("");
					Toast.makeText(MainActivity.this,
							"録音が開始できませんでした。この端末が録音をサポートしていない可能性があります。",
							Toast.LENGTH_LONG).show();
					break;
				case RecMicToMp3.MSG_ERROR_CREATE_FILE:
//					statusTextView.setText("");
					Toast.makeText(MainActivity.this, "ファイルが生成できませんでした",
							Toast.LENGTH_LONG).show();
					break;
				case RecMicToMp3.MSG_ERROR_REC_START:
//					statusTextView.setText("");
					Toast.makeText(MainActivity.this, "録音が開始できませんでした",
							Toast.LENGTH_LONG).show();
					break;
				case RecMicToMp3.MSG_ERROR_AUDIO_RECORD:
//					statusTextView.setText("");
					Toast.makeText(MainActivity.this, "録音ができませんでした",
							Toast.LENGTH_LONG).show();
					break;
				case RecMicToMp3.MSG_ERROR_AUDIO_ENCODE:
//					statusTextView.setText("");
					Toast.makeText(MainActivity.this, "エンコードに失敗しました",
							Toast.LENGTH_LONG).show();
					break;
				case RecMicToMp3.MSG_ERROR_WRITE_FILE:
//					statusTextView.setText("");
					Toast.makeText(MainActivity.this, "ファイルの書き込みに失敗しました",
							Toast.LENGTH_LONG).show();
					break;
				case RecMicToMp3.MSG_ERROR_CLOSE_FILE:
//					statusTextView.setText("");
					Toast.makeText(MainActivity.this, "ファイルの書き込みに失敗しました",
							Toast.LENGTH_LONG).show();
					break;
				default:
					break;
				}
			}
		});
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
		
		MenuItem appSetting = menu.add(0, 2, 2, "设置");
		appSetting.setOnMenuItemClickListener(new OnMenuItemClickListener() {

	        @Override
	        public boolean onMenuItemClick(MenuItem item) {
                // Display the file chooser dialog
	        	Intent myIntent = new Intent(MainActivity.this, SerialActivity.class);
//	        	myIntent.putExtra("key", value); //Optional parameters
	        	MainActivity.this.startActivity(myIntent);
	            return true;
	        }
	    });
	
		MenuItem recordStart = menu.add(0, 3, 3, "开始录音");
		recordStart.setOnMenuItemClickListener(new OnMenuItemClickListener() {

	        @Override
	        public boolean onMenuItemClick(MenuItem item) {
	        	mRecMicToMp3.start();
	            return true;
	        }
	    });

		MenuItem recordStop = menu.add(0, 4, 4, "停止录音");
		recordStop.setOnMenuItemClickListener(new OnMenuItemClickListener() {

	        @Override
	        public boolean onMenuItemClick(MenuItem item) {
	        	mRecMicToMp3.stop();
	            return true;
	        }
	    });
		
		MenuItem exit = menu.add(0, 5, 5, "退出");
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


	/*
	 *  Useful method to speak a sentence
	 */
	public void speakText(String toSpeak){
		if (toSpeak == null || toSpeak.equals("")) {
//			toSpeak = NORECORD;
		}
//		Toast.makeText(getApplicationContext(), toSpeak, 
//		Toast.LENGTH_SHORT).show();
		TTSObject.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
	}

	/*
	 *  Serial query thread methods to create and restart the thread
	 */
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
							Thread.sleep(1200);
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
	
	/*
	 *  Universe method to restart any thread
	 */
	private void restartThread(Thread thread){
		thread.interrupt();
		thread = null;
		thread = getSerialQueryThread();
		thread.start();
	}
	
	/*
	 *  File chooser for the CVS file reader
	 */
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
    
    /*
     *  File uri for CVS reader
     */
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
