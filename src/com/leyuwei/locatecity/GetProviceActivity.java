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
	List<String> mProvices;// ���е�ʡ
	// ʡ+��Ӧ����
	private Map<String, List<String>> mCityMap = new HashMap<String, List<String>>();
	/**
	 * ��ȫ����ʡ��������Ϣ��json�ĸ�ʽ���棬������ɺ�ֵΪnull
	 */
	private JSONObject mJsonObj;
	private JSONArray jsonArray;

	//item ��ʡ��
	private String proviceI;
	//item ���м���
	private ArrayList<String> cityI;
	private List<String> cityO;//��ȡmap�е���list

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_get_provice);
		mProvices = new ArrayList<String>();
		// ��ȡjson����
		initJsonDatas();
		// ����json
		initDatas();

		// ��ȡ�����ļ�
		lv_getProvice = (ListView) findViewById(R.id.lv_get_provice);
		btn_get_provice = (Button) findViewById(R.id.btn_get_provice);

		adapter = new GetProviceAdapter(getApplicationContext(), mProvices);
		lv_getProvice.setAdapter(adapter);

		// ���listview��item
		lv_getProvice.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				//��ȡ�����item��ʡ��
				proviceI = mProvices.get(position);
//				Toast.makeText(getApplicationContext(), proviceI,
//						Toast.LENGTH_LONG).show();
				System.out.println(proviceI);
				cityI = new ArrayList<String>();
				//��ȡ�����item��ʡ�ݶ�Ӧ����list
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

		// ������ذ�ť
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
	 * ��assert�ļ����ж�ȡʡ������json�ļ���Ȼ��ת��Ϊjson����
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

	// ����json����
	private void initDatas() {
		try {
			jsonArray = mJsonObj.getJSONArray("data");
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonP = (JSONObject) jsonArray.get(i);
				String proviceJson = jsonP.getString("name");// jso�ļ���ʡ�ݵı�ǩΪ"name"
				// ��ӵ�ʡ�ݵ�list��
				mProvices.add(proviceJson);

				String city = jsonP.getString("cities");
				// ȥ���ַ����е��������
				city = city.replaceAll("\"", "");
				city = city.replace("[", "");
				city = city.replace("]", "");
				// ��ȡ��list
				List<String> listCity = Arrays.asList(city.split(","));
				// ��ʡ����Ӧ���зŵ�map�У�
				mCityMap.put(proviceJson, listCity);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		mJsonObj = null;
	}

}
