package com.akshat.peel.tvguide;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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
import android.content.Context;
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

import com.akshat.peel.tvguide.adapters.ChannelAdapter;
import com.akshat.peel.tvguide.adapters.SimpleListAdapter;
import com.akshat.peel.tvguide.data.Program;
import com.akshat.peel.tvguide.data.Schedule;
import com.akshat.peel.tvguide.data.Schedules;
import com.akshat.peel.tvguide.utils.Utils;

public class GuideActivity extends Activity {

	private static final int HEIGHT_PX = 75;

	private static final String TAG = "PeelGuide";

	private Map<String,Schedules> mSchedules;
	
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
	
	private ChannelAdapter channelAdapter;
	private LinearLayout channelLayout;
	private LinearLayout programLayout;
	
	private SimpleListAdapter adapter;
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_guide);
		mSchedules = new TreeMap<String, Schedules>();
		
		hThread.start();
		handler = new Handler(hThread.getLooper());
		
		mStatusView = findViewById(R.id.fetchingstatus);
		mStatusMessageView = (TextView) findViewById(R.id.status_message);
		mErrorMessageView  = (TextView) findViewById(R.id.error_status_message);
		
//		lvChannels = (ListView) findViewById(R.id.channel_list);
//		lvPrograms = (ListView) findViewById(R.id.guide_list);
		
		mScrollView = (ScrollView) findViewById(R.id.channel_guide_layout);
		mCompleteList = (LinearLayout) findViewById(R.id.channel_program_layout);
//		
		channelLayout = (LinearLayout) findViewById(R.id.channels_layout);
		mHorizontalView = (HorizontalScrollView) findViewById(R.id.programs_scroll_layout);
		programLayout = (LinearLayout) findViewById(R.id.program_layout);

		//		mListView = (TwoWayView) findViewById(R.id.list);
//        mListView.setItemMargin(10);
        
//        mChannelList = (TwoWayView) findViewById(R.id.channellist);
//        mChannelList.setItemMargin(10);
        
        
      /*  mListView.setOnScrollListener(new TwoWayView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(TwoWayView view, int scrollState) {
                String stateName = "Undefined";
                switch(scrollState) {
                case SCROLL_STATE_IDLE:
                    stateName = "Idle";
                    break;

                case SCROLL_STATE_TOUCH_SCROLL:
                    stateName = "Dragging";
                    break;

                case SCROLL_STATE_FLING:
                    stateName = "Flinging";
                    break;
                }

//                mStateMessage = "Scroll state changed: " + stateName;
//                refreshToast();
            }

            @Override
            public void onScroll(TwoWayView view, int firstVisibleItem,
                    int visibleItemCount, int totalItemCount) {
//                mScrollMessage = "Scroll (first: " + firstVisibleItem + ", count = " + visibleItemCount + ")";
//                refreshToast();
            }
        });

        mListView.setRecyclerListener(new TwoWayView.RecyclerListener() {
            @Override
            public void onMovedToScrapHeap(View view) {
                Log.d(TAG, "View moved to scrap heap");
            }
        });
        */
        
//        adapter = new SimpleListAdapter(GuideActivity.this);
//        mListView.setAdapter(adapter);
        
//        channelAdapter = new ChannelAdapter(getApplicationContext());
//		mChannelList.setAdapter(channelAdapter);
		
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
				String guideData = getGuideJSON();
				
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

	
	private String getGuideJSON(){
		StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(
				"http://peelapp.zelfy.com/epg/schedules/stillrunning/DITV807?start=" + Utils.getDate() + "&window=4&userid=83073892&roomid=1");
		
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
	private void createUI() {
		Set<String> keys = mSchedules.keySet();
		
		channelLayout.setOrientation(LinearLayout.VERTICAL);

		
//		int i=0;
		for(String key : keys){
//		{
//			String key = "751";
			
			// For testing
//			i++;
//			if(i==5)
//				break;
			// End for testing
			
			LinearLayout layout2 = new LinearLayout(getApplicationContext());
			layout2.setOrientation(LinearLayout.HORIZONTAL);
	        int ht_dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, HEIGHT_PX, getResources().getDisplayMetrics());

			android.widget.LinearLayout.LayoutParams paramsChannel = new LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, ht_dp);
			paramsChannel.setMargins(15, 15, 15,15);
			
			android.widget.LinearLayout.LayoutParams paramsPrograms = new LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, ht_dp);
			paramsPrograms.setMargins(15, 15, 15,15);

			View channelDetail = createChannelUI(key);
			channelLayout.addView(channelDetail,paramsChannel);
			
			if(key.equals("216")){
				Log.d(TAG, "At channel 216");
			}
			List<Schedule> tempSchedule = new LinkedList<Schedule>(mSchedules.get(key).getSchedules());

			createProgramUI(tempSchedule,layout2);
			programLayout.addView(layout2,paramsPrograms);
		}
		
	}

	private void createProgramUI(List<Schedule> tempSchedule,
			LinearLayout layout2) {
		
        int ht_dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, HEIGHT_PX, getResources().getDisplayMetrics());
        boolean overflow = false;
        boolean prog1Overflow = false;
        boolean prog2Overflow = false;
        
        
		android.widget.LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(800, ht_dp);
		params.setMargins(0, 0, 15, 0);
		ListIterator<Schedule> iter = tempSchedule.listIterator();
		while(true){
			Schedule schedule;
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
			
			if(minutes <= 30){
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
			}else if(minutes > 30 && minutes <= 60){
				overflow = false;
				prog2 = null;
				iter.remove();
			}else if(minutes > 60){
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
		
		if(layout2.getChildCount() < 4){
			for(int i=layout2.getChildCount();i<4;i++){
				View programNoInfoDetail = createProgramUI("No Info",null,false,true,false);
				layout2.addView(programNoInfoDetail,params);
			}
		}
	}

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
//	    if(overflow && !bigOverFlow){
//	    	if(prog1 == null)
//	    		textViewProgTime1.setText(">");
//	    	else
//	    		textViewProgTime2.setText(">");
//	    }
	    
	    return rowView;
	}

	private View createChannelUI(String channelNumber) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	
	    View rowView = inflater.inflate(R.layout.channel_list_item, channelLayout, false);
	    TextView textView = (TextView) rowView.findViewById(R.id.channelNumber);
	    ImageView imageView = (ImageView) rowView.findViewById(R.id.channelImage);
	    
	    textView.setText(channelNumber);

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
//		Log.d(TAG, "Schedule created : " + (temp != null ? temp.toString() : schedules.toString()));
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
