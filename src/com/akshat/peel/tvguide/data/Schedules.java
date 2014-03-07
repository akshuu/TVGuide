package com.akshat.peel.tvguide.data;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.text.format.Time;

/**
 * 15 - channel name
 * 
 * 16 - channel number
 * 
 * 21 - channel logo
 * 
 * 18 - start date
 * 
 * 19 - start time
 * 
 * 20 - duration
 * 
 * @author Akshat_Jain
 * 
 */
public class Schedules implements Serializable {

	private String sChannelName;
	private String sChannelNumber;
	private String sLogoUrl;
	private List<Schedule> schedules;
	
	
	public String getChannelName() {
		return sChannelName;
	}

	public void setChannelName(String sChannelName) {
		this.sChannelName = sChannelName;
	}

	public String getChannelNumber() {
		return sChannelNumber;
	}

	public void setChannelNumber(String sChannelNumber) {
		this.sChannelNumber = sChannelNumber;
	}

	public String getLogoUrl() {
		return sLogoUrl;
	}

	public void setLogoUrl(String sLogoUrl) {
		this.sLogoUrl = sLogoUrl;
	}

	public List<Schedule> getSchedules() {
		return schedules;
	}
	
	public void setSchedules(List<Schedule> schedules) {
		this.schedules = schedules;
	}

	public void addSchedule(Schedule schedule) {
		if(schedules == null){
			schedules = new LinkedList<Schedule>();
		}
		schedules.add(schedule);
	}

	@Override
	public String toString(){
		return "Channel " + sChannelName + " on " + sChannelNumber + " has  : " + (schedules != null ? schedules.size() : 0) + " programs " ;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((sChannelName == null) ? 0 : sChannelName.hashCode());
		result = prime * result
				+ ((sChannelNumber == null) ? 0 : sChannelNumber.hashCode());
		result = prime * result
				+ ((sLogoUrl == null) ? 0 : sLogoUrl.hashCode());
		result = prime * result
				+ ((schedules == null) ? 0 : schedules.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Schedules other = (Schedules) obj;
		if (sChannelName == null) {
			if (other.sChannelName != null)
				return false;
		} else if (!sChannelName.equals(other.sChannelName))
			return false;
		if (sChannelNumber == null) {
			if (other.sChannelNumber != null)
				return false;
		} else if (!sChannelNumber.equals(other.sChannelNumber))
			return false;
		if (sLogoUrl == null) {
			if (other.sLogoUrl != null)
				return false;
		} else if (!sLogoUrl.equals(other.sLogoUrl))
			return false;
		if (schedules == null) {
			if (other.schedules != null)
				return false;
		} else if (!schedules.equals(other.schedules))
			return false;
		return true;
	}
	
	
}
