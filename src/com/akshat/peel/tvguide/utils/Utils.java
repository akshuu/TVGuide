package com.akshat.peel.tvguide.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class Utils {

	private static final String TAG = "PeelGuide";
	
	private static ExecutorService pool ;
	private static HashMap<String, Future<Bitmap>> mResult;

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
		
		minutes = part1 * 60 + part2;
		
		}catch(Exception ex){
			Log.e(TAG, "Invalid string.",ex);
			return 0;
		}
//		Log.d(TAG,"Converted : " + duration + " to minutes = " + minutes);
		return minutes;
	}
	
	public static String getJSON(){
		StringBuilder builder = new StringBuilder();
		String url = "http://peelapp.zelfy.com/epg/schedules/stillrunning/DITV807?start=" + Utils.getDate() + "&window=4&userid=83073892&roomid=1";
		InputStream content = getData(url);
		if(content != null){
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(content));
			String line;
			try {
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
			} catch (IOException e) {
				Log.e(TAG, "There was some error reading the data from server.",e);
				return null;
			}
		} else {
			Log.e(TAG, "Failed to download file");
			return null;
		}
		return builder.toString();
	}
	
	private static Bitmap getBitmap(String url){
		InputStream content = getData(url);
		Bitmap bmp = null;
		if(content != null){
			bmp = BitmapFactory.decodeStream(content);
		} else {
			Log.e(TAG, "Failed to download file");
			bmp = null;
		}
		return bmp;
	}
	
	// This method will place fetch requests to the network and queue them on the Future task.
	
	public static Bitmap fetchBitmaps(final Context mContext,final String key,final String url){
		if(pool == null)
			pool = Executors.newFixedThreadPool(30);
		if(url == null || "".equals(url))
			return null;
		Bitmap bmp = readBitmapFromDisk(key, mContext);
		
		if(bmp == null){			// if we don't have it on disk, get it from the network
			Log.d(TAG, "Image not found on disk...");
			if(!pool.isShutdown()){
				Future<Bitmap> results = pool.submit(new Callable<Bitmap>() {
					@Override
					public Bitmap call() throws Exception {
						Bitmap bmp =getBitmap(url);
						saveBitmapToDisk(mContext,key, bmp);
						return bmp;
					}
				}); 
				if(mResult == null)
					mResult = new HashMap<String, Future<Bitmap>>();					
				mResult.put(key, results);
			}
			else 
				return null;
		}else{
			Log.d(TAG, "Image was found on disk...");
			return bmp;
		}
		return bmp;
		
	}

	// This method will get result from disk or else the result of Future<Bitmap>
	
	public static Bitmap getBitmaps(final Context mContext,final String key,final String url) throws InterruptedException, ExecutionException{
		if(pool == null)
			pool = Executors.newFixedThreadPool(15);
		if(url == null || "".equals(url))
			return null;
		
		Bitmap bmp = readBitmapFromDisk(key, mContext);
		
		if(bmp == null){			// if we don't have it on disk, get it from the network
			Log.d(TAG, "Image not found on disk...");
			if(!pool.isShutdown()){
				Future<Bitmap> result = mResult.get(key);
				try{
					bmp = result.get(30,TimeUnit.SECONDS);		// Timeout in 30 sec
					saveBitmapToDisk(mContext,key, bmp);
					return bmp;
				}catch(Exception ex){
					return null;
				}
			}
			else 
				return null;
		}else{
			Log.d(TAG, "Image was found on disk...");
			return bmp;
		}
		
	}
	
	// Saves bitmaps to disk
	public static void saveBitmapToDisk(Context mContext,String key,Bitmap bitmap){
		if (bitmap == null) {
			return;
		}
		String filename = getAbsoluteFileLocation(key, mContext);
    	File destFile = new File(filename);
		FileOutputStream fos = null;
		try {
			if (destFile.exists()) {
				destFile.delete();
			}
			fos = new FileOutputStream(destFile);
			boolean status = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
			Log.d(TAG,"image saved to disk ? " + status);
		} catch (FileNotFoundException fnfe) {
			return ;
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException ioe) {
				}
			}
		}
		return;
	}
	
	 /**
		 * @param filename
		 * @return
		 */
		public static String getAbsoluteFileLocation(String filename,Context context) {

			File picturePath = context.getFilesDir();
			filename = picturePath + "/"+ filename;
			Log.d(TAG,"image path:  " + filename);
			return filename;
		}

	
	public static Bitmap readBitmapFromDisk(String key, Context mContext){
		String filename = getAbsoluteFileLocation(key, mContext);
		FileInputStream fis = null;
		Bitmap img = null;
		{
			Log.d(TAG,"Reading file:  " + filename);
			try {
				fis = new FileInputStream(filename);
				img = BitmapFactory.decodeStream(fis);
				return img;
			} catch (FileNotFoundException e) {
			}finally{
				if(fis!=null){
					try {
						fis.close();
					} catch (IOException e) {
					}
					fis = null;
				}
			}

		}
		return null;
	}
	
	public static InputStream getData(String url){
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		
		try {
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				return content;
			} else {
				Log.e(TAG, "Failed to download file");
				return null;
			}
		} catch (ClientProtocolException e) {
			Log.e(TAG, "There was some error reading the data from server.",e);
			return null;
		} catch (IOException e) {
			Log.e(TAG, "There was some error reading the data from server.",e);
			return null;
		}
	}
	
	public static void stopThreadPool(){
		if(pool != null)
			pool.shutdown();
	}
}
