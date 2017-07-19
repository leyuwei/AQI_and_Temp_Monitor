package com.leyuwei.locatecity;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.leyuwei.pmtestbt.R;

public class GetCityAdapter extends BaseAdapter {

	private Context context;
	private ArrayList<String> list;

	private TextView tv_city;
	
	public GetCityAdapter(Context context, ArrayList<String> list) {
		super();
		this.context = context;
		this.list = list;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		convertView = LayoutInflater.from(context).inflate(R.layout.city_item, null);
		
		tv_city = (TextView) convertView.findViewById(R.id.tv_city);
		
		String city = list.get(position);
		
		tv_city.setText(city);
		return convertView;
	}
	
	

}
