package com.akshat.peel.tvguide.adapters;

import java.util.Set;
import java.util.TreeSet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.akshat.peel.tvguide.R;

public class ChannelAdapter extends BaseAdapter {

	private Set<String> mChannelNumbers;
	private Context context;
	private Object[] channelArray;
	
	public ChannelAdapter(Context ctx,Set<String> channels) {
		this.context = ctx;
		updateData(channels);
	}
	
	public ChannelAdapter(Context applicationContext) {
		this.context = applicationContext;
		mChannelNumbers = new TreeSet<String>();
		channelArray = mChannelNumbers.toArray();
	}

	@Override
	public int getCount() {
		return mChannelNumbers.size();
	}

	@Override
	public Object getItem(int position) {
		return channelArray[position];
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	public void updateData(Set<String> channels){
		if(mChannelNumbers == null)
			mChannelNumbers = new TreeSet<String>();
		mChannelNumbers.clear();
		mChannelNumbers.addAll(channels);
		channelArray = mChannelNumbers.toArray();
//		Arrays.sort(channelArray);
		
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			
			LayoutInflater inflater = (LayoutInflater) context
		        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
			convertView = inflater.inflate(R.layout.channel_list_item, parent, false);
		    
			holder = new ViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.channelNumber);
			holder.imageView = (ImageView) convertView.findViewById(R.id.channelImage);

			convertView.setTag(holder);
		} else {
		    holder = (ViewHolder) convertView.getTag();
		}
		
		holder.title.setText(channelArray[position].toString());
	    return convertView;
	}
	
	@Override
	public boolean isEnabled(int position) {
		return false;
	}

	class ViewHolder {
	    public TextView title;
	    public ImageView imageView;
	}
}
