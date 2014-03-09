package com.akshat.peel.tvguide.adapters;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.akshat.peel.tvguide.R;
import com.akshat.peel.tvguide.data.Schedule;
import com.akshat.peel.tvguide.data.Schedules;

public class SimpleListAdapter extends BaseAdapter {

	private final Context mContext;
	private List<Schedule> channels;
	private Object[] channelArray;
	
	public SimpleListAdapter(Context context) {
		mContext = context;
		channels = new ArrayList<Schedule>();
	}

	public void updateData(List<Schedule> list){
		if(channels == null)
			channels = new ArrayList<Schedule>();
		this.channels.clear();
		this.channels.addAll(list);
	}
	
	@Override
	public int getCount() {
	    return channels.size();
	}

	@Override
	public Object getItem(int position) {
		return channels.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	    ViewHolder holder = null;

		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.program_list_item, parent, false);

			holder = new ViewHolder();
			holder.prog1 = (TextView) convertView.findViewById(R.id.program1_name);
			holder.time1 = (TextView) convertView.findViewById(R.id.program1_time);

			holder.prog2 = (TextView) convertView.findViewById(R.id.program2_name);
			holder.time2 = (TextView) convertView.findViewById(R.id.program2_time);

			convertView.setTag(holder);
		} else {
		    holder = (ViewHolder) convertView.getTag();
		}

	    Schedule schedule = channels.get(position);
	    
	    holder.prog1.setText(schedule.getProgram().getTitle());
	    holder.time1.setText(schedule.getDuration());
		holder.time2.setVisibility(View.GONE);
		holder.prog2.setVisibility(View.GONE);
		
		return convertView;
	}

	class ViewHolder {
	    public TextView prog1;
	    public TextView time1;

	    public TextView prog2;
	    public TextView time2;

	}

}
