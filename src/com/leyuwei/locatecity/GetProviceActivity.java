package com.leyuwei.locatecity;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

import com.leyuwei.pmtestbt.R;

public class GetProviceActivity extends Activity {

	private ListView lv_getProvice;
	private Button btn_get_provice;

	GetProviceAdapter adapter;
	List<String> mProvices;// 所有的省
	// 省+对应的市
	private Map<String, List<String>> mCityMap = new HashMap<String, List<String>>();
	/**
	 * 把全国的省市区的信息以json的格式保存，解析完成后赋值为null
	 */
	private JSONObject mJsonObj;
	private JSONArray jsonArray;

	//item 的省名
	private String proviceI;
	//item 的市集合
	private ArrayList<String> cityI;
	private List<String> cityO;//获取map中的市list

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_get_provice);
		mProvices = new ArrayList<String>();
		// 获取json数据
		initJsonDatas();
		// 解析json
		initDatas();

		// 获取布局文件
		lv_getProvice = (ListView) findViewById(R.id.lv_get_provice);
		btn_get_provice = (Button) findViewById(R.id.btn_get_provice);

		adapter = new GetProviceAdapter(getApplicationContext(), mProvices);
		lv_getProvice.setAdapter(adapter);

		// 点击listview的item
		lv_getProvice.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				//获取点击的item的省份
				proviceI = mProvices.get(position);
//				Toast.makeText(getApplicationContext(), proviceI,
//						Toast.LENGTH_LONG).show();
				System.out.println(proviceI);
				cityI = new ArrayList<String>();
				//获取点击的item的省份对应的市list
				 cityO= mCityMap.get(proviceI);
				
				for (int i = 0; i < cityO.size(); i++) {
					cityI.add(cityO.get(i));
				}
				
				Intent intent = new Intent(GetProviceActivity.this,
						GetCityActivity.class);

				Bundle bundle = new Bundle();
				bundle.putString("provice", proviceI);
				bundle.putStringArrayList("city", cityI);
				intent.putExtras(bundle);
				startActivityForResult(intent, 1);
			}
		});

		// 点击返回按钮
		btn_get_provice.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			
			Bundle bundle = data.getExtras();
			String provice = bundle.getString("provice");
			String cityName = bundle.getString("cityName");
			Intent intentSet = new Intent(GetProviceActivity.this,CitySelActivity.class);
			Bundle bundleSet = new Bundle();
			bundleSet.putString("provice", provice);
			bundleSet.putString("cityName", cityName);
			intentSet.putExtras(bundleSet);
			setResult(RESULT_OK,intentSet);
			finish();
		}
		
	}
	
	/**
	 * 从assert文件夹中读取省市区的json文件，然后转化为json对象
	 */
	private void initJsonDatas() {
		try {
			StringBuffer sb = new StringBuffer();
			InputStream is = getAssets().open("city.json");
			int len = -1;
			byte[] buf = new byte[1024];
			while ((len = is.read(buf)) != -1) {
				sb.append(new String(buf, 0, len, "gbk"));
			}
			is.close();
			mJsonObj = new JSONObject(sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	// 解析json对象
	private void initDatas() {
		try {
			jsonArray = mJsonObj.getJSONArray("data");
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonP = (JSONObject) jsonArray.get(i);
				String proviceJson = jsonP.getString("name");// jso文件中省份的标签为"name"
				// 添加到省份的list中
				mProvices.add(proviceJson);

				String city = jsonP.getString("cities");
				// 去除字符串中的特殊符号
				city = city.replaceAll("\"", "");
				city = city.replace("[", "");
				city = city.replace("]", "");
				// 获取市list
				List<String> listCity = Arrays.asList(city.split(","));
				// 将省及对应的市放到map中；
				mCityMap.put(proviceJson, listCity);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		mJsonObj = null;
	}

}
