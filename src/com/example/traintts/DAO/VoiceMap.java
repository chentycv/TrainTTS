package com.example.traintts.DAO;

public class VoiceMap {
	
	private long id;
	private int arg0;
	private int arg1;
	private double arg2;
	private String voice;
	
	// Setters and gettes
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public int getArg0() {
		return arg0;
	}
	public void setArg0(int arg0) {
		this.arg0 = arg0;
	}
	public int getArg1() {
		return arg1;
	}
	public void setArg1(int arg1) {
		this.arg1 = arg1;
	}
	public double getArg2() {
		return arg2;
	}
	public void setArg2(double arg2) {
		this.arg2 = arg2;
	}
	public String getVoice() {
		return voice;
	}
	public void setVoice(String voice) {
		this.voice = voice;
	}
	
	// Will be used by the ArrayAdapter in the ListView
	@Override
	public String toString() {
		return 
			Integer.toString(this.arg0) + ", " +
			Integer.toString(this.arg1) + ", " + 
			Double.toString(this.arg2) + ", " + 
			this.voice;
	}
}
