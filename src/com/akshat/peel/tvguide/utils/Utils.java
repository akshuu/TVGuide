package com.akshat.peel.tvguide.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.util.Log;

public class Utils {

	private static final String TAG = "PeelGuide";

	public static String getDate(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddkkmm00",Locale.US);
		String date = sdf.format(new Date());
		Log.d(TAG, "The date is == " + date);
		return date;
	}
	
	public static int convertDurationToMinutes(String duration){
		int minutes = 0;
		if(duration == null || "".equals(duration))
			return minutes;
		try{
		String[] times = duration.split(":");
		if(times.length != 3 )
			return minutes;
		
		int part1 = Integer.parseInt(times[0]);
		int part2 = Integer.parseInt(times[1]);
		int part3 = Integer.parseInt(times[2]);
		
		minutes = part1 * 60 + part2;
		
		}catch(Exception ex){
			Log.e(TAG, "Invalid string.",ex);
			return 0;
		}
//		Log.d(TAG,"Converted : " + duration + " to minutes = " + minutes);
		return minutes;
	}
	
	public static void getStartTime(String date, String time, String duration){
		
	}
}
