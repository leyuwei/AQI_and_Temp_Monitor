package com.leyuwei.locatecity;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.leyuwei.pmtestbt.R;

public class GetCityActivity extends Activity {

	private ListView lv_get_city;
	private Button btn_get_city;
	private TextView tv_getCity_provice;
	
	//�м�list
	ArrayList<String> list ;
	String provice; //ʡ
	GetCityAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_get_city);
		
		lv_get_city = (ListView) findViewById(R.id.lv_get_city);
		btn_get_city = (Button) findViewById(R.id.btn_get_city);
		tv_getCity_provice = (TextView) findViewById(R.id.tv_getCity_provice);
		
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		provice = bundle.getString("provice");
		list = bundle.getStringArrayList("city");
		
		//����ʡ����
		tv_getCity_provice.setText("ʡ��"+provice);
		adapter = new GetCityAdapter(getApplicationContext(), list);
		lv_get_city.setAdapter(adapter);
		
		//���ذ�ť
		btn_get_city.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intentN = new Intent(GetCityActivity.this,GetProviceActivity.class); 
				startActivity(intentN);
			}
		});
		
		//���item
		lv_get_city.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				//����item���м�����
				String cityName = list.get(position);
				//��ʡ��������item�����������¼�
				Intent intent2 = new Intent(GetCityActivity.this,CitySelActivity.class);
				Bundle bundle2 = new Bundle();
				bundle2.putString("provice", provice);
				bundle2.putString("cityName", cityName);
				intent2.putExtras(bundle2);
				setResult(RESULT_OK,intent2);
//				startActivity(intent2);
				finish();
			}
		});
		
	}

	

}
