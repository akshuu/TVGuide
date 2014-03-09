package com.akshat.peel.tvguide;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.akshat.peel.tvguide.data.Program;
import com.akshat.peel.tvguide.data.Schedule;
import com.akshat.peel.tvguide.data.Schedules;
import com.akshat.peel.tvguide.utils.Utils;

public class GuideActivity extends Activity {

	private static final int HEIGHT_PX = 75;

	private static final String TAG = "PeelGuide";

	private Map<String,Schedules> mSchedules;
	private WeakHashMap<String, Bitmap> mChannelImage;
	
	
	private HandlerThread hThread = new HandlerThread("guide");
	private Handler handler;

	private TextView mStatusMessageView;
	private TextView mErrorMessageView;
	private View mStatusView;
//	private ListView lvChannels;
//	private ListView lvPrograms;
	
	private ScrollView mScrollView;
	private LinearLayout mCompleteList ;
	
	private HorizontalScrollView mHorizontalView;
	
	private LinearLayout channelLayout;
	private LinearLayout programLayout;
	
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_guide);
		mSchedules = new TreeMap<String, Schedules>();
		mChannelImage = new WeakHashMap<String, Bitmap>();
		
		hThread.start();
		handler = new Handler(hThread.getLooper());
		
		mStatusView = findViewById(R.id.fetchingstatus);
		mStatusMessageView = (TextView) findViewById(R.id.status_message);
		mErrorMessageView  = (TextView) findViewById(R.id.error_status_message);
		
		mScrollView = (ScrollView) findViewById(R.id.channel_guide_layout);
		mCompleteList = (LinearLayout) findViewById(R.id.channel_program_layout);
//		
		channelLayout = (LinearLayout) findViewById(R.id.channels_layout);
		mHorizontalView = (HorizontalScrollView) findViewById(R.id.programs_scroll_layout);
		programLayout = (LinearLayout) findViewById(R.id.program_layout);

		
		mStatusMessageView.setText(R.string.progress);
		getDataFromServer();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
	}

	private void getDataFromServer() {
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				String guideData = Utils.getJSON();
				
				if(mSchedules == null)
					mSchedules = new TreeMap<String, Schedules>();
				mSchedules.clear();
				
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
	protected void onDestroy() {
		super.onDestroy();
		Utils.stopThreadPool();
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
			programLayout.removeAllViews();
			channelLayout.removeAllViews();
			getDataFromServer();
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	/// Utility functions 

	private void parse(String guideData){
		try {
			JSONObject obj = new JSONObject(guideData);
			JSONArray array = obj.getJSONArray("schedules");
			
			for(int i=0;i<array.length();i++){
				JSONObject scheduleJSON = array.getJSONObject(i);
				Schedules schedule = parseSchedule(scheduleJSON);
			}
			
			
			Log.d(TAG, "mSchedules size == " + mSchedules.size());
			
			createUI();
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	
	// Optimize : Create UI few layouts at  a time...
	// probably we need to follow the adapter approach
	private void createUI() {
		Set<String> keys = mSchedules.keySet();
		
		channelLayout.setOrientation(LinearLayout.VERTICAL);

		for(String key : keys){
			
			LinearLayout layout2 = new LinearLayout(getApplicationContext());
			layout2.setOrientation(LinearLayout.HORIZONTAL);
	        int ht_dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, HEIGHT_PX, getResources().getDisplayMetrics());

			android.widget.LinearLayout.LayoutParams paramsChannel = new LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, ht_dp);
			paramsChannel.setMargins(15, 15, 15,15);
			
			android.widget.LinearLayout.LayoutParams paramsPrograms = new LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, ht_dp);
			paramsPrograms.setMargins(15, 15, 15,15);
			Bitmap channelLogo;
			
			// If we have the image in the WeakReference?
			if(mChannelImage.containsKey(key) && mChannelImage.get(key) != null){
				channelLogo = mChannelImage.get(key);
			}else{
				try {			// This method will check for disk cache , else will fetch it from the URL.
					channelLogo = Utils.getBitmaps(getApplicationContext(),key,mSchedules.get(key).getLogoUrl());
					mChannelImage.put(key, channelLogo);
				} catch (InterruptedException e) {
					e.printStackTrace();
					channelLogo = null; 
				} catch (ExecutionException e) {
					e.printStackTrace();
					channelLogo = null;
				}
				
			}
			View channelDetail = createChannelUI(key,channelLogo);
			channelLayout.addView(channelDetail,paramsChannel);
			
			List<Schedule> tempSchedule = new LinkedList<Schedule>(mSchedules.get(key).getSchedules());

			createProgramUI(tempSchedule,layout2);
			programLayout.addView(layout2,paramsPrograms);
		}
		
	}

	// Responsible for creating the Horizontal scroll for a channel.
	private void createProgramUI(List<Schedule> tempSchedule,
			LinearLayout layout2) {
		
        int ht_dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, HEIGHT_PX, getResources().getDisplayMetrics());
        boolean overflow = false;
        boolean prog1Overflow = false;
        boolean prog2Overflow = false;
        
        
		android.widget.LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(800, ht_dp);
		params.setMargins(0, 0, 15, 0);
		ListIterator<Schedule> iter = tempSchedule.listIterator();
		
		// This logic deteremines what needs to be added to the current view
		// How the overflow happens
		// We stop after adding 4 items or when the list is empty.
		while(true){
			Schedule schedule;
			// If there was overflow, we need to use the same previous object.
			if(overflow && iter.previousIndex() != -1){
				schedule = iter.previous();
				overflow = false;
				prog1Overflow = true;
				prog2Overflow = false;
			}else{
				if(!iter.hasNext())
					break;
				schedule = iter.next();
				overflow = false;
			}
			
			int minutes = schedule.getRemainingDuration();
			String prog1 = schedule.getProgram().getTitle();
			String prog2 = null;
			
			if(minutes <= 30){			// Show is only 30 mins, so we can add another to the same view
				// add another to the same view
				iter.remove();			// remove the current node
				if(iter.hasNext()){
					schedule = iter.next();
					minutes = schedule.getRemainingDuration();
					if(minutes <= 30){
						prog2 = schedule.getProgram().getTitle();
						overflow = false;
						iter.remove();
					}else if(minutes > 30 && minutes <= 60){
						overflow = true;
						prog2Overflow = true;
						prog2 = schedule.getProgram().getTitle();
						schedule.setRemainingDuration(schedule.getRemainingDuration() - 30);
					}else if(minutes > 60){
						prog2Overflow = true;
						schedule.setRemainingDuration(schedule.getRemainingDuration() - 60);
						overflow = true;
					}
				}
			}else if(minutes > 30 && minutes <= 60){		// Show is only 1 hr,so we add nothing else
				overflow = false;
				prog2 = null;
				iter.remove();
			}else if(minutes > 60){							// Show is > 1 hr, so we need to carry it forward
				schedule.setRemainingDuration(schedule.getRemainingDuration() - 60);
				prog2 = null;
				overflow = true;
			}
			
//			Log.d(TAG, "Setting the following : " + prog1 + " , prg2 = " + prog2 + ", is overflow = ? " + overflow);
			View programDetail = createProgramUI(prog1,prog2,overflow,prog1Overflow,prog2Overflow );
			layout2.addView(programDetail,params);
			// Not adding more than 4 hrs of data
			if(layout2.getChildCount() == 4)
				break;
		}
		
		// If we have less info for the program, just show an empty area...
		if(layout2.getChildCount() < 4){
			for(int i=layout2.getChildCount();i<4;i++){
				View programNoInfoDetail = createProgramUI("No Info",null,false,true,false);
				layout2.addView(programNoInfoDetail,params);
			}
		}
	}

	// This creates the individual program layout.
	private View createProgramUI(String prog1,String prog2,boolean overflow, boolean prog1Overflow, boolean prog2Overflow) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
	    View rowView = inflater.inflate(R.layout.program_list_item, mHorizontalView, false);
	    TextView textViewProgTime1 = (TextView) rowView.findViewById(R.id.program1_time);
	    TextView textViewProgName1 = (TextView)  rowView.findViewById(R.id.program1_name);
	    TextView textViewProgTime2 = (TextView) rowView.findViewById(R.id.program2_time);
	    TextView textViewProgName2 = (TextView)  rowView.findViewById(R.id.program2_name);
	    
	    if(prog1 != null){
		    textViewProgName1.setText(prog1);
		    textViewProgTime1.setText("00");
	    }else{
		    textViewProgTime1.setVisibility(View.GONE);
		    textViewProgName1.setVisibility(View.GONE);

	    }
	    
	    if(prog2 != null){
	    	textViewProgTime2.setText("30");
	    	 textViewProgName2.setText(prog2);
	    }else{
		    textViewProgTime2.setVisibility(View.GONE);
		    textViewProgName2.setVisibility(View.GONE);
	    }
	    
	    if(prog1Overflow){
	    	textViewProgTime1.setText(">");
	    }
	    else if(prog2Overflow)
	    	textViewProgTime2.setText(">");
	    
	    return rowView;
	}

	private View createChannelUI(String channelNumber, Bitmap channelLogo) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	
	    View rowView = inflater.inflate(R.layout.channel_list_item, channelLayout, false);
	    TextView textView = (TextView) rowView.findViewById(R.id.channelNumber);
	    ImageView imageView = (ImageView) rowView.findViewById(R.id.channelImage);
	    
	    textView.setText(channelNumber);
	    if(channelLogo == null)
	    	imageView.setBackgroundResource(R.drawable.blanktv);
	    else
	    	imageView.setImageBitmap(channelLogo);
	    return rowView;
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
		
		// Submit the fetch Logo request here.
		if(!mChannelImage.containsKey(schedules.getChannelNumber())){
			Bitmap channelLogo;
			channelLogo = Utils.fetchBitmaps(getApplicationContext(),schedules.getChannelNumber(),mSchedules.get(schedules.getChannelNumber()).getLogoUrl());
			mChannelImage.put(schedules.getChannelNumber(), channelLogo);
		}
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
