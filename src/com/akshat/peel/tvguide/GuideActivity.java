package com.akshat.peel.tvguide;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.format.Time;
import android.transition.Scene;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.akshat.peel.tvguide.data.Program;
import com.akshat.peel.tvguide.data.Schedule;
import com.akshat.peel.tvguide.data.Schedules;

public class GuideActivity extends Activity {

	private static final String TAG = "PeelGuide";

	private Map<String,Schedules> mSchedules;
	
	private HandlerThread hThread = new HandlerThread("guide");
	private Handler handler;

	private TextView mStatusMessageView;
	private TextView mErrorMessageView;
	private View mStatusView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_guide);
		mSchedules = new HashMap<String, Schedules>();
		
		hThread.start();
		handler = new Handler(hThread.getLooper());
		
		mStatusView = findViewById(R.id.fetchingstatus);
		mStatusMessageView = (TextView) findViewById(R.id.status_message);
		mErrorMessageView  = (TextView) findViewById(R.id.error_status_message);
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		mStatusMessageView.setText(R.string.progress);
		getDataFromAPI();
	}

	private void getDataFromAPI() {
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				String guideData = getGuideJSON();
				mSchedules = new HashMap<String, Schedules>();
				updateResults(guideData);
				Log.d(TAG, "JSON == " + guideData);
				
			}
		});
	}
	
	protected void updateResults(final String guideData) {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				if(guideData == null){
					mStatusView.setVisibility(View.GONE);
					mErrorMessageView.setVisibility(View.VISIBLE);
					mErrorMessageView.setText(R.string.error_getting_data);
				}else{
					mStatusView.setVisibility(View.GONE);
					parse(guideData);
				}
	
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.splash, menu);
		return true;
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.action_refresh){
			mStatusView.setVisibility(View.VISIBLE);
			mErrorMessageView.setVisibility(View.GONE);
			getDataFromAPI();
		}
		return super.onOptionsItemSelected(item);
	}
	
	private String getDate(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddkkmm00",Locale.US);
		String date = sdf.format(new Date());
		Log.d(TAG, "The date is == " + date);
		return date;
	}
	
	private String getGuideJSON(){
		StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(
				"http://peelapp.zelfy.com/epg/schedules/stillrunning/DITV807?start=" + getDate() + "&window=4&userid=83073892&roomid=1");
		
		try {
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(content));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
			} else {
				Log.e(TAG, "Failed to download file");
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return builder.toString();
	}
	
	private void parse(String guideData){
		try {
			JSONObject obj = new JSONObject(guideData);
			JSONArray array = obj.getJSONArray("schedules");
			
			Log.d(TAG, "Array == " + array.length());
			for(int i=0;i<array.length();i++){
				JSONObject scheduleJSON = array.getJSONObject(i);
				Schedules schedule = parseSchedule(scheduleJSON);
				//mSchedules.put(schedule.getChannelNumber(), schedule);
			}
			
			
			Log.d(TAG, "mSchedules size == " + mSchedules.size());
			
			Schedules test = mSchedules.get("002");
			Log.d(TAG, "Schedules for channel : 002 == ");
			for(Schedule sch : test.getSchedules()){
				Log.d(TAG, "Schedule : " + sch.getStartDate() +  " :: " +sch.getStartTime() + " , prog = " + sch.getProgram().getTitle() + " , genre == " + sch.getProgram().getGenre());;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private Schedules parseSchedule(JSONObject scheduleJSON) throws JSONException {
		Iterator<String> keys = scheduleJSON.keys();
		Schedules schedules = new Schedules();
		Schedule schedule = new Schedule();
		while(keys.hasNext()){
			String key = keys.next();
			String value = scheduleJSON.getString(key);
			if("15".equals(key)){
				schedules.setChannelName(value);
			}else if("16".equals(key)){
				schedules.setChannelNumber(value);
			}else if("19".equals(key)){
				schedule.setStartTime(value);
			}else if("18".equals(key)){
				schedule.setStartDate(value);
			}else if("20".equals(key)){
				schedule.setDuration(value);
			}else if("21".equals(key)){
				schedules.setLogoUrl(value);
			}else if("program".equals(key)){
				JSONObject programJSON = scheduleJSON.getJSONObject(key);
				Program program = parseProgram(programJSON);
				schedule.setPrograms(program);
			}
		}
		
		Schedules temp = mSchedules.get(schedules.getChannelNumber());
		if(temp == null){
			schedules.addSchedule(schedule);
			mSchedules.put(schedules.getChannelNumber(), schedules);
		}else{
			temp.addSchedule(schedule);
			mSchedules.put(temp.getChannelNumber(), temp);
		}
		Log.d(TAG, "Schedule created : " + (temp != null ? temp.toString() : schedules.toString()));
		return schedules;
	}

	
	private Program parseProgram(JSONObject programJSON) {
		try {
			Program prog = new Program();
			Iterator<String> keys = programJSON.keys();
			while(keys.hasNext()){
				String key = keys.next();
				String value;
				value = programJSON.getString(key);
				if("1".equals(key)){
					prog.setID(value);
				}else if("6".equals(key)){
					prog.setID2(value);
				}else if("2".equals(key)){
					prog.setType(value);
				}else if("11".equals(key)){
					prog.setTitle(value);
				}else if("12".equals(key)){
					prog.setDescription(value);
				}else if("13".equals(key)){
					prog.setGenre(value);
				}else if("14".equals(key)){
					prog.setImageUrl(value);
				}
			}
			return prog;
		} catch (JSONException e) {
			Log.d(TAG, "Some error occurred parsing the program data",e);
			return null;
		}
		
	}
}
