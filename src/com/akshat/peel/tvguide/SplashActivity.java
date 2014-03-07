package com.akshat.peel.tvguide;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class SplashActivity extends Activity {

	private static final String TAG = "PeelGuide";
	
	private HandlerThread hThread = new HandlerThread("guide");
	private Handler handler;

	private TextView mStatusMessageView;
	private TextView mErrorMessageView;
	
	private View mStatusView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		hThread.start();
		handler = new Handler(hThread.getLooper());
		
		mStatusView = findViewById(R.id.fetchingstatus);
		mStatusMessageView = (TextView) findViewById(R.id.status_message);
		mErrorMessageView  = (TextView) findViewById(R.id.error_status_message);
		
	}

	@Override
	protected void onResume() {
		super.onResume();
//		mStatusMessageView.setText(R.string.progress);
//		getData();
	}

	private void getData() {
//		handler.post(new Runnable() {
//			
//			@Override
//			public void run() {
//				String guideData = getGuideJSON();
//				updateResults(guideData);
//				Log.d(TAG, "JSON == " + guideData);
//				
//			}
//		});
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
					Intent guideActivity = new Intent(getApplicationContext(), GuideActivity.class);
					guideActivity.putExtra("JSON", guideData);
					startActivity(guideActivity);
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
			getData();
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

}
