package stmatching;

import java.text.*;
import java.util.Date;

public class Time {
	public long epoch;
	public String formatTime;
	
	public Time(long epoch){
		this.setEpoch(epoch);
	}
	
	private void setEpoch(long epoch){
		this.epoch = epoch;
	}
	
	private void setFormatTime(long epoch){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		this.formatTime = sdf.format(new Date(epoch));
	}
	
	@Override
	public boolean equals(Object time){
		if(!(time instanceof Time)) return false;
		if(this.epoch == ((Time) time).epoch) return true;
		return false;
	}
	
	@Override
	public int hashCode(){
		return Long.toString(this.epoch).hashCode();
	}
	
	@Override
	public String toString(){
		this.setFormatTime(epoch);
		return Long.toString(this.epoch) + "," + this.formatTime;
	}
}
