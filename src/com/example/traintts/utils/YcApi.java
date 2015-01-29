package com.example.traintts.utils;

import java.io.FileDescriptor;
//import android.util.Log;

public class YcApi
{
	public int IO_POLLING_MODE = 0;
	public int IO_INTR_MODE = 1;

	public int IO_INTR_LOW_LEVEL_TRIGGERED = 0;
	public int IO_INTR_HIGH_LEVEL_TRIGGERED = 1;
	public int IO_INTR_FALLING_EDGE_TRIGGERED = 2;
	public int IO_INTR_RISING_EDGE_TRIGGERED = 3;
	public int IO_INTR_BOTH_EDGE_TRIGGERED = 4;
	
	public String ttySAC0 = "dev/ttySAC0";//����0
	public String ttySAC1 = "dev/ttySAC1";//����1
	public String ttySAC2 = "dev/ttySAC2";//����2
	public String ttySAC3 = "dev/ttySAC3";//����3
	
    public native int 		SetLed(boolean flag);
    
    public native int 		SetBeep(boolean flag);
    
    public native int 		StartWDog();
    public native int 		SetWDog(byte timeInterval);
    public native int 		FeedWDog();
    public native int 		StopWDog();
    
    public native boolean 	SetIO(int level , int ioNum);
    public native boolean 	SetIoMode(int ioNum,int ioMode ,int triggeredMode);
    public native int    	GetIO(int ioNum,int flag);
    
    public native boolean   SetBackLight(int dx);
    public native int    	GetBackLight();
    
    public native FileDescriptor openCom(String path, int baudrate ,int databit,int paritybit,int stopbit);
   	public native void           closeCom();
   	
    public native int OpenI2C(String nodeName); 
    public native int ReadI2C(int fileHander , int slaveAddr, int subaddr, int bufArr[], int len);
    public native int WriteI2C(int fileHander, int slaveAddr, int subaddr, int bufArr[], int len); 
    public native void CloseI2C(int fileHander);
    
    public native int WriteE2PROM(int subaddr, String buf, int len);
    public native String ReadE2PROM(int subaddr, int len);

    static
    {
        System.loadLibrary("ycapi");
    }
}