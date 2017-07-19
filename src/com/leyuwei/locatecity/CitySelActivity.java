package com.leyuwei.locatecity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

import com.leyuwei.pmtestbt.R;

public class CitySelActivity extends Activity {
	private TextView city, city_end;
	private static final int BEGIN_CODE = 0;
	private static final int END_CODE = 1;
	private static final int NOTHING_CODE = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_citysel);
		setTitle("选择查看城市");
        ActionBar actionBar=getActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        
		city = (TextView) findViewById(R.id.city);
		city_end = (TextView) findViewById(R.id.city_end);

		//出发地
		city.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(CitySelActivity.this,
						GetProviceActivity.class);
				startActivityForResult(intent, BEGIN_CODE);
			}
		});

		//目的地
		city_end.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(CitySelActivity.this,
						GetProviceActivity.class);
				startActivityForResult(intent, END_CODE);
			}
		});

	}

	// 获取传会的数据
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case BEGIN_CODE:
			if(data==null)
				return;
			Bundle bundle = data.getExtras();
			String provice = bundle.getString("provice");
			String cityName = bundle.getString("cityName");
			// 出发地
			city.setText(provice + " " + cityName);
			break;

		case END_CODE:
			if(data==null)
				return;
			Bundle bundle2 = data.getExtras();
			String provice2 = bundle2.getString("provice");
			String cityName2 = bundle2.getString("cityName");
			// 目的地
			city_end.setText(provice2 + " " + cityName2);
			break;
			
		case NOTHING_CODE:
			
			break;
			
		default:
			break;
		}

	}

}
