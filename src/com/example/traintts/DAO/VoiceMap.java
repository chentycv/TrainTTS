package com.example.traintts.DAO;

public class VoiceMap {
	
	private long id;
	private int segment;
	private int signal;
	private double distance;
	private String voice;
	
	// Setters and gettes
	public long getId() {
		return id;
	}
	public void setId(long id){
		this.id = id;
	}
	public int getSegment() {
		return segment;
	}
	public void setSegment(int segment) {
		this.segment = segment;
	}
	public int getSignal() {
		return signal;
	}
	public void setSignal(int signal) {
		this.signal = signal;
	}
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
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
			Integer.toString(this.segment) + ", " +
			Integer.toString(this.signal) + ", " + 
			Double.toString(this.distance) + ", " + 
			this.voice;
	}
}
