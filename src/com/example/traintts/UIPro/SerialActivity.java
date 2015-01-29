package com.example.traintts.UIPro;


import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.example.traintts.R;
import com.example.traintts.utils.YcApi;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;

public class SerialActivity extends Activity{
	
	YcApi ycapi;
	private Spinner					m_SpinnerSerialNumber;
	private Spinner					m_SpinnerSerialBaute;
	private Spinner					m_SpinnerSerialParityBit;
	private Spinner					m_SpinnerSerialDataBit;
	private Spinner					m_SpinnerSerialStopBit;
	
	private static final String[]	m_serialNumber	  = { "ttySAC0", "ttySAC1", "ttySAC2", "ttySAC3" }; 
	
	private static final String[]	m_serialBaute	  = { "110", "300", "600", "1200",
		"2400", "4800", "9600", "14400","19200", "38400", "43000", "56000","57600", "115200",
		"128000", "256000"};
	private static final int[] m_serialBauteInt={ 110, 300, 600, 1200,
		2400, 4800, 9600, 14400,19200, 38400, 43000, 56000,57600, 115200,128000,256000};
	
	private static final String[]	m_serialParityBit = { "None", "Even", "Odd", "Space"};
	private static final int[] m_serialParityBitInt={ 0, 1, 2, 3};
	
	private static final String[]	m_serialDataBit	  = { "5", "6", "7", "8" };
	private static final int[] m_serialDataBitInt={ 5, 6, 7, 8};
	
	private static final String[]	m_serialStopBit	  = { "1", "2"};
	private static final int[] m_serialStopBitInt={1,2};
	
	protected static final String   TAG = "SerialActivity.java :";
	
	private ArrayAdapter<String>	adapterSerialNumber;
	private ArrayAdapter<String>	adapterSerialBaute;
	private ArrayAdapter<String>	adapterSerialParityBit;
	private ArrayAdapter<String>	adapterSerialDataBit;
	private ArrayAdapter<String>	adapterSerialStopBit;
	
	private EditText mSendTex;
	
	private FileDescriptor fp;	
	private FileInputStream mInputStream;
	private FileOutputStream mOutputStream;
	
	private ReadThread mReadThread;
	
	EditText mReception;
	
	private boolean gSerialOpenFlag=false;

	
	private class ReadThread extends Thread {

		@Override
		public void run() {
			super.run();
			while(!isInterrupted()) 
			{
				int size;
				if(gSerialOpenFlag)
				{
					try 
					{
						byte[] buffer = new byte[64];
						if (mInputStream == null) return;					
						size = mInputStream.read(buffer);
						if (size > 0) 
						{
							if(gSerialOpenFlag)
								onDataReceived(buffer, size);
						}						
					} 
					catch (IOException e)
					{
						e.printStackTrace();
						return;
					}
			   }
		  }
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_serial);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		mReception = (EditText) findViewById(R.id.editTextRece);
		
		mSendTex = (EditText)findViewById(R.id.editTextSend);
		
		mSendTex.setText("www.yctek.com");
		
		ycapi = new YcApi();
		
		SerialInit();
		
		//返回按钮的操作
/*        Button mButtonReturn=(Button)findViewById(R.id.buttonreturn);
        mButtonReturn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SerialActivity.this.finish();
			}
		});*/
        SharedPreferences settings = getPreferences(Activity.MODE_PRIVATE);
	}
	private void SerialInit()
    {				
		//初始化Spinner
		SerialNumberSpinner();
		SerialBauteSpinner();
		SerialParityBitSpinner();
		SerialDataBitSpinner();
		SerialStopBitSpinner();
		
		m_SpinnerSerialParityBit.setEnabled(false);//暂时屏蔽掉校验位
		
		
		
		final Button clearButton = (Button)findViewById(R.id.buttonClearReceText);
		clearButton.setOnClickListener(new View.OnClickListener() 
		{			
			public void onClick(View arg0) 
			{
				mReception.setText("");
			}
		});
		
		//打开和关闭串口
		final Button openButton = (Button)findViewById(R.id.buttonOpen);
		openButton.setOnClickListener(new View.OnClickListener()
		{			
			public void onClick(View arg0) 
			{
				
				String path = ycapi.ttySAC0;
				if(gSerialOpenFlag)
				{
					if (mReadThread != null)
						mReadThread.interrupt();
					openButton.setText(getResources().getString(R.string.main2serialopenbutton));
					gSerialOpenFlag=false;
					ycapi.closeCom();					
					m_SpinnerSerialNumber.setEnabled(true);
					m_SpinnerSerialBaute.setEnabled(true);
					//m_SpinnerSerialParityBit.setEnabled(true);
					m_SpinnerSerialDataBit.setEnabled(true);
					m_SpinnerSerialStopBit.setEnabled(true);
				}
				else
				{
					switch(m_SpinnerSerialNumber.getSelectedItemPosition())
					{
					case 0:
						path=ycapi.ttySAC0;
						break;
					case 1:
						path=ycapi.ttySAC1;
						break;
					case 2:
						path=ycapi.ttySAC2;
						break;
					case 3:
						path=ycapi.ttySAC3;
						break;
					default:
						path=ycapi.ttySAC0;
						break;
					}
					//openCom(String path, int baudrate ,int databit,int paritybit,int stopbit);
					fp = ycapi.openCom(path, m_serialBauteInt[m_SpinnerSerialBaute.getSelectedItemPosition()],
							m_serialDataBitInt[m_SpinnerSerialDataBit.getSelectedItemPosition()],
							m_serialParityBitInt[m_SpinnerSerialParityBit.getSelectedItemPosition()],
							m_serialStopBitInt[m_SpinnerSerialStopBit.getSelectedItemPosition()]
							); 
					if (fp == null) 
					{
						Log.e(TAG, "native open returns null");
						try 
						{
							throw new IOException();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						gSerialOpenFlag=false;
					}
					else
					{
						mInputStream = new FileInputStream(fp);
						mOutputStream = new FileOutputStream(fp);
						openButton.setText(getResources().getString(R.string.main2serialclosebutton));
						gSerialOpenFlag=true;
						
						m_SpinnerSerialNumber.setEnabled(false);
						m_SpinnerSerialBaute.setEnabled(false);
						m_SpinnerSerialParityBit.setEnabled(false);
						m_SpinnerSerialDataBit.setEnabled(false);
						m_SpinnerSerialStopBit.setEnabled(false);
					}
					
					mReadThread = new ReadThread();
					mReadThread.start();
				}
			}
		});
		
		Button sendButton = (Button)findViewById(R.id.buttonSend);
		sendButton.setOnClickListener(new View.OnClickListener() 
		{			
			public void onClick(View arg0) 
			{	
				if(gSerialOpenFlag)
				{
				try {
					
						mOutputStream.write(mSendTex.getText().toString().getBytes());
					} catch (IOException e) 
					{
					// TODO Auto-generated catch block
					e.printStackTrace();
				    }
			    }				
			}
		});
    }
	
	private void SerialNumberSpinner()
	{
		m_SpinnerSerialNumber = (Spinner) findViewById(R.id.spinnerComNumber);
		
		

		//将可选内容与ArrayAdapter连接
		adapterSerialNumber = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, m_serialNumber);

		//设置下拉列表的风格
		adapterSerialNumber.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		//将adapter添加到m_Spinner中
		m_SpinnerSerialNumber.setAdapter(adapterSerialNumber);
		
		m_SpinnerSerialNumber.setSelection(0);
		//添加Spinner事件监听
		m_SpinnerSerialNumber.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{			
				//设置显示当前选择的项
				arg0.setVisibility(View.VISIBLE);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0)
			{
				// TODO Auto-generated method stub
			}

		});
	}
	private void SerialBauteSpinner()
	{
		m_SpinnerSerialBaute = (Spinner) findViewById(R.id.spinnerbaute);

		//将可选内容与ArrayAdapter连接
		adapterSerialBaute = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, m_serialBaute);

		//设置下拉列表的风格
		adapterSerialBaute.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		//将adapter添加到m_Spinner中
		m_SpinnerSerialBaute.setAdapter(adapterSerialBaute);
		
		m_SpinnerSerialBaute.setSelection(13);

		//添加Spinner事件监听
		m_SpinnerSerialBaute.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{			
				//设置显示当前选择的项
				arg0.setVisibility(View.VISIBLE);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0)
			{
				// TODO Auto-generated method stub
			}

		});
		
		
	}
	private void SerialParityBitSpinner()
	{
		m_SpinnerSerialParityBit = (Spinner) findViewById(R.id.spinnerparitybit);

		//将可选内容与ArrayAdapter连接
		adapterSerialParityBit = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, m_serialParityBit);

		//设置下拉列表的风格
		adapterSerialParityBit.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		//将adapter添加到m_Spinner中
		m_SpinnerSerialParityBit.setAdapter(adapterSerialParityBit);
		
		

		//添加Spinner事件监听
		m_SpinnerSerialParityBit.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{			
				//设置显示当前选择的项
				arg0.setVisibility(View.VISIBLE);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0)
			{
				// TODO Auto-generated method stub
			}

		});
	}
	private void SerialDataBitSpinner()
	{
		m_SpinnerSerialDataBit = (Spinner) findViewById(R.id.spinnerdatabit);

		//将可选内容与ArrayAdapter连接
		adapterSerialDataBit = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, m_serialDataBit);

		//设置下拉列表的风格
		adapterSerialDataBit.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		//将adapter添加到m_Spinner中
		m_SpinnerSerialDataBit.setAdapter(adapterSerialDataBit);
		
		m_SpinnerSerialDataBit.setSelection(3);

		//添加Spinner事件监听
		m_SpinnerSerialDataBit.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{			
				//设置显示当前选择的项
				arg0.setVisibility(View.VISIBLE);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0)
			{
				// TODO Auto-generated method stub
			}

		});
	}
	private void SerialStopBitSpinner()
	{
		m_SpinnerSerialStopBit = (Spinner) findViewById(R.id.spinnerstopbit);

		//将可选内容与ArrayAdapter连接
		adapterSerialStopBit = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, m_serialStopBit);

		//设置下拉列表的风格
		adapterSerialStopBit.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		//将adapter添加到m_Spinner中
		m_SpinnerSerialStopBit.setAdapter(adapterSerialStopBit);
	

		//添加Spinner事件监听
		m_SpinnerSerialStopBit.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{			
				//设置显示当前选择的项
				arg0.setVisibility(View.VISIBLE);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0)
			{
				// TODO Auto-generated method stub
			}

		});
	}
	
	protected void onDataReceived(final byte[] buffer, final int size) {
		runOnUiThread(new Runnable() {
			public void run() {
				if (mReception != null) {
					mReception.append(new String(buffer, 0, size));
				}
			}
		});
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		//ycapi.closeCom();	
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		/*try {
			Log.e(TAG, "initUart xxxxxxxxxxx");
			initUart();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		mReadThread = new ReadThread();
		mReadThread.start();*/ 
	}

	@Override
	protected void onDestroy() {
		if (mReadThread != null)
			mReadThread.interrupt();
		
		super.onDestroy();
	}
}
