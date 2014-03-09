package com.akshat.peel.tvguide;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
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

public class GuideActivity extends Activity {

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
			getDataFromServer();
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	/// Utility functions 
	
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
			
			/*Schedules test = mSchedules.get("371");
			Log.d(TAG, "Schedules for channel : 002 == "  +test.getSchedules().size());
			List<Schedule> temp = test.getSchedules();
			Collections.sort(temp);
			for(Schedule sch : temp){
				Log.d(TAG, "Schedule : " + sch.getStartDate() +  " :: " +sch.getStartTime() + " , prog = " + sch.getProgram().getTitle() + " , genre == " + sch.getProgram().getGenre());;
			}*/
			
//			channelAdapter = new ChannelAdapter(getApplicationContext(), mSchedules.keySet());
//			lvChannels.setAdapter(channelAdapter);
			
			// Uncomment this for old approach
			createUI();
			
//			channelAdapter.updateData(mSchedules.keySet());
//			channelAdapter.notifyDataSetChanged();
			
//			adapter.updateData(mSchedules);
//			adapter.notifyDataSetChanged();
			///createUI_TwoWay()
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	
	// Optimize : Create UI few layouts at  a time...
	private void createUI() {
		Set<String> keys = mSchedules.keySet();
		// Sorting the keys
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
//		mHorizontalView = new HorizontalScrollView(getApplicationContext());
		android.widget.LinearLayout.LayoutParams paramsRow = new LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
				android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
		paramsRow.setMargins(0, 0, 0, 5);
		android.widget.LinearLayout.LayoutParams paramsHorizontalScroll = new LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
//		mHorizontalView.setLayoutParams(paramsHorizontalScroll);
		
		channelLayout.setOrientation(LinearLayout.VERTICAL);

//		LinearLayout layoutPrograms = new LinearLayout(getApplicationContext());
//		layoutPrograms.setOrientation(LinearLayout.VERTICAL);
		
		int i=0;
		for(String key : sortedKeys){
			// For testing
			i++;
			if(i==30)
				break;
			// End for testing
			
			LinearLayout layout = new LinearLayout(getApplicationContext());
			LinearLayout layout2 = new LinearLayout(getApplicationContext());
	        int ht_dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());


			android.widget.LinearLayout.LayoutParams paramsChannel = new LinearLayout.LayoutParams(ht_dp, ht_dp);
			paramsChannel.setMargins(0, 0, 5,5);

			android.widget.LinearLayout.LayoutParams paramsPrograms = new LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, ht_dp);
			paramsPrograms.setMargins(0, 0, 5,5);

			View channelDetail = createChannelUI(key);
			channelLayout.addView(channelDetail,paramsChannel);
			
			for(Schedule schedule : mSchedules.get(key).getSchedules()){
				View programDetail = createProgramUI(schedule);
				android.widget.LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(800, ht_dp);
				params.setMargins(0, 0, 5, 5);
				layout2.setOrientation(LinearLayout.HORIZONTAL);
				layout2.addView(programDetail,params);
			}
			
//			android.widget.LinearLayout.LayoutParams paramsPrograms = new LinearLayout.LayoutParams(channelDetail.getLayoutParams());
			
//			layoutPrograms.addView(layout2,paramsPrograms);
			
			programLayout.addView(layout2,paramsPrograms);
			
		}
		
//		mHorizontalView.addView(layoutPrograms);
//		mCompleteList.addView(channelLayout,paramsRow);
//		mCompleteList.addView(mHorizontalView,paramsHorizontalScroll);
	}

	private View createProgramUI(Schedule schedule) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
	    View rowView = inflater.inflate(R.layout.program_list_item, mHorizontalView, false);
	    TextView textViewProgTime1 = (TextView) rowView.findViewById(R.id.program1_time);
	    TextView textViewProgName1 = (TextView)  rowView.findViewById(R.id.program1_name);
	    TextView textViewProgTime2 = (TextView) rowView.findViewById(R.id.program2_time);
	    TextView textViewProgName2 = (TextView)  rowView.findViewById(R.id.program2_name);
	    
	    textViewProgName1.setText(schedule.getProgram().getTitle());
	    textViewProgTime1.setText(schedule.getDuration());

	    textViewProgTime2.setVisibility(View.GONE);
	    textViewProgName2.setVisibility(View.GONE);
	    
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
