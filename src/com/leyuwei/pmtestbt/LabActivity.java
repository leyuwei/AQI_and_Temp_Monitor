package com.leyuwei.pmtestbt;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MarkerOptions.MarkerAnimateType;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;
import com.leyuwei.locatecity.GetProviceActivity;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * 
 * Code below is specially designed for author's graduation design. 
 * Anyone who quote this must be authorized by the author.
 * Any actions without authorization is strictly prohibited.
 * 
 * @author leyuwei
 * @category Application for graduation design 2017
 * @version 1.3.0
 * @since 2017/05/10 19:27
 * @description LabView Controller
 *
 */
public class LabActivity extends FragmentActivity implements OnClickListener,OnGetGeoCoderResultListener{
	private static final int BEGIN_CODE = 0;
	private String username = "";
	private String usercity = "";
	private String usercitycode = "";
	private double latitude,lontitude;
	private TextView tv_userstate,tv_datepicker1,tv_datepicker2,tv_city,tv_bigdata;
	private boolean hasGotPos = false, isFirstLoc = true, hasGotBigdataLoc = false;
	private Dao<AQIData, Date> simpleDao = null;
	private DatabaseHelper databaseHelper = null;
	private MapView mMapView = null;
	private BaiduMap mBaiduMap = null;
	private String aqi4Show = "";
	private String temp4Show = "";
	GeoCoder mSearch = null; // 搜索模块，也可去掉地图模块独立使用
	private Map<String,String> markerList = new HashMap<String,String>();
	private List<Marker> markerObjList = new ArrayList<Marker>();
	private Overlay circleMarker = null;
	public LocationClient mLocationClient = null;
	private ProgressDialog pd = null;
	public BDLocationListener myListener = new BDLocationListener() {
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null || mMapView == null) {
				tv_userstate.setText(username+",请点此重新获取位置");
				mLocationClient.stop();
                return;
            }
			//获取定位结果
			DecimalFormat def = new DecimalFormat(".000");
	        latitude = Double.valueOf(def.format(location.getLatitude()));    //获取纬度信息
	        lontitude = Double.valueOf(def.format(location.getLongitude()));    //获取经度信息
	        usercity = location.getCity();
	        usercitycode = location.getCityCode();
	        hasGotPos = true;
	        tv_userstate.setText("欢迎！"+usercity+location.getDistrict()+"的"+username);
	        tv_city.setText(location.getCity());
	        hasGotBigdataLoc = true;
	        
	        //处理地图数据
		    // 设置定位数据  
		    MyLocationData locData = new MyLocationData.Builder()  
		    .accuracy(location.getRadius())  
		    .latitude(location.getLatitude())  
		    .longitude(location.getLongitude()).build();  
		    mBaiduMap.setMyLocationData(locData);
		    BitmapDescriptor bitmap = BitmapDescriptorFactory  
		    	    .fromResource(R.drawable.indicator_bdmap);  
		    MyLocationConfiguration config = new MyLocationConfiguration(com.baidu.mapapi.map.MyLocationConfiguration.LocationMode.FOLLOWING, false, bitmap);  
		    mBaiduMap.setMyLocationConfiguration(config);
		    if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(12.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
		    mLocationClient.stop();
		}
		@Override
		public void onConnectHotSpotMessage(String arg0, int arg1) {
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_lab);
		
		// Set result CANCELED incase the user backs out  
        setResult(Activity.RESULT_OK);
		
		//绑定视图
		tv_userstate = (TextView) findViewById(R.id.tv_userstate);
		tv_datepicker1 = (TextView) findViewById(R.id.tv_datepicker_internet);
		tv_datepicker2 = (TextView) findViewById(R.id.tv_datepicker_bigdata);
		tv_city = (TextView) findViewById(R.id.tv_city);
		tv_bigdata = (TextView) findViewById(R.id.tv_bigdata);
		tv_datepicker1.setOnClickListener(this);
		tv_datepicker2.setOnClickListener(this);
		findViewById(R.id.btn_backtohome).setOnClickListener(this);
		findViewById(R.id.btn_backup).setOnClickListener(this);
		findViewById(R.id.btn_recover).setOnClickListener(this);
		findViewById(R.id.btn_bigdata).setOnClickListener(this);
		tv_city.setOnClickListener(this);
		tv_userstate.setOnClickListener(this);
		mMapView = (MapView) findViewById(R.id.mapView);
		mBaiduMap = mMapView.getMap(); 
		mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL); 
		mBaiduMap.setMyLocationEnabled(true);
		
		//初始化定位SDK并启动
		mLocationClient = new LocationClient(this);
	    mLocationClient.registerLocationListener( myListener );
	    initLocation();
	    mLocationClient.start();
	    
	    // 初始化搜索模块，注册事件监听
        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);
        
        // 初始化标记点击事件监听
        mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker marker) {
				if(marker.getTitle()==null || marker.getTitle().equals("")){
					return false;
				}
				String msg = markerList.get(marker.getTitle());
				String msgArr[] = msg.split("-");
				aqi4Show = msgArr[0];
				temp4Show = msgArr[1];
				LatLng ptCenter = new LatLng((Float.valueOf(msgArr[2])), (Float.valueOf(msgArr[3])));
				if(circleMarker!=null){
					circleMarker.remove();
				}
				OverlayOptions ooCircle = new CircleOptions().fillColor(0x384d73b3)
                        .center(ptCenter).stroke(new Stroke(3, 0x784d73b3))
                        .radius(78);
                circleMarker  = (Overlay)mBaiduMap.addOverlay(ooCircle);
				mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(ptCenter));
				return false;
			}
		});
	    
	    //初始化一些变量
	    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	    tv_datepicker1.setText(df.format(Calendar.getInstance().getTime()));
	    tv_datepicker2.setText(df.format(Calendar.getInstance().getTime()));
		username = this.getIntent().getStringExtra("username");
		tv_userstate.setText("稍等呀，"+username);
		pd = new ProgressDialog(this,ProgressDialog.THEME_HOLO_LIGHT);
		
		//初始化ormlite数据库，获取数据库操作对象
		try {
			simpleDao = getHelper().getDataDao();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @category get the helper from the manager once per class.
	 */
	private DatabaseHelper getHelper() {
		if (databaseHelper == null) {
			databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
		}
		return databaseHelper;
	}
	
	private void initLocation(){
	    LocationClientOption option = new LocationClientOption();
	    option.setLocationMode(LocationMode.Hight_Accuracy);
	    //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
	    option.setCoorType("bd09ll");
	    //可选，默认gcj02，设置返回的定位结果坐标系
	    int span=1000;
	    option.setScanSpan(span);
	    //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
	    option.setIsNeedAddress(true);
	    //可选，设置是否需要地址信息，默认不需要
	    option.setOpenGps(true);
	    //可选，默认false,设置是否使用gps
	    option.setIsNeedLocationDescribe(true);
	    //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
	    option.setIsNeedLocationPoiList(true);
	    //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
	    option.setIgnoreKillProcess(false);
	    //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死  
	    option.SetIgnoreCacheException(false);
	    //可选，默认false，设置是否收集CRASH信息，默认收集
	    mLocationClient.setLocOption(option);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		//关闭数据库
		if (databaseHelper != null) {
			OpenHelperManager.releaseHelper();
			databaseHelper = null;
		}
		// 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        for(Marker m : markerObjList){
			m.remove();
		}
		markerObjList.clear();
		markerList.clear();
		mBaiduMap.clear();
        mMapView.onDestroy();
        mMapView = null;
		mLocationClient.stop();
	}
	
	@Override  
    protected void onResume() {  
        super.onResume();
        mMapView.onResume();
	}  
	
    @Override  
    protected void onPause() {  
        super.onPause();
        mMapView.onPause();
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_datepicker_internet:
			SimpleCalendarDialogFragment dialog = new SimpleCalendarDialogFragment(tv_datepicker1);
			dialog.setIsLab(true);
			dialog.show(getSupportFragmentManager(), "calendardialog");
		break;
		case R.id.tv_datepicker_bigdata:
			SimpleCalendarDialogFragment dialog2 = new SimpleCalendarDialogFragment(tv_datepicker2);
			dialog2.setIsLab(true);
			dialog2.show(getSupportFragmentManager(), "calendardialog2");
		break;
		case R.id.btn_backup:
			if(!hasGotPos){
				Toast.makeText(LabActivity.this, "请先等待定位结束", Toast.LENGTH_SHORT).show();
				return;
			}
			pd.setMessage("正在处理并备份，请稍候...");
			pd.setCancelable(false);
			pd.show();
			List<AQIData> aqidataList = new ArrayList<AQIData>();
		    QueryBuilder<AQIData, Date> queryBuilder = simpleDao.queryBuilder();
		    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		    Date querydate = null;
		    String curDateStr = tv_datepicker1.getText().toString()+" 00:00:00";
		    JSONObject sendPackage = new JSONObject();
		    try {
				querydate = df.parse(curDateStr);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		    final Date queryDateFinal = querydate;
			try {
				queryBuilder.where().eq(AQIData.FIELDNAME_VAGUE_DATE, querydate);
				PreparedQuery<AQIData> preparedQuery = queryBuilder.prepare();
			    aqidataList = simpleDao.query(preparedQuery);
			    if(aqidataList.size()==0){
			    	Toast.makeText(LabActivity.this, "当天没有采集数据", Toast.LENGTH_SHORT).show();
			    	pd.cancel();
			    	return;
			    }
			    int upedCount = 0;
			    for(AQIData aqidata : aqidataList){
			    	if(aqidata.getIsUploaded()){
			    		upedCount++;
			    		continue;
			    	}
			    	JSONObject jsonObj = aqidata.toJSON(username,usercity,latitude,lontitude,curDateStr);
			    	try {
						sendPackage.accumulate("data", jsonObj);
					} catch (JSONException e) {
						e.printStackTrace();
					}
			    }
			    if(upedCount == aqidataList.size()){
			    	Toast.makeText(LabActivity.this, "当天数据已全部备份", Toast.LENGTH_SHORT).show();
			    	pd.cancel();
			    	return;
			    }
			}catch (SQLException e) {
				e.printStackTrace();
			}
			//Log.d("sendmsg", sendPackage.toString());
			RequestParams params = new RequestParams();
			params.add("json", sendPackage.toString());
			PMHttpClient.post(LabActivity.this, PMHttpClient.URL_BACKUP, params, new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
					String result = new String(arg2);
					if(result.trim().equals("2")){
						Toast.makeText(LabActivity.this, "JSON数据格式出错", Toast.LENGTH_SHORT).show();
					}else if(result.trim().equals("1")){
						Toast.makeText(LabActivity.this, "服务器备份时出错", Toast.LENGTH_SHORT).show();
					}else{
						Toast.makeText(LabActivity.this, "备份成功", Toast.LENGTH_SHORT).show();
						// TODO 将列表所有元素isUploaded设为true
						UpdateBuilder<AQIData, Date> updateBuilder = simpleDao.updateBuilder();
						try {
							updateBuilder.updateColumnValue(AQIData.FIELDNAME_ISUPLOADED, true);
							Where<AQIData, Date> where = updateBuilder.where();
							where.eq(AQIData.FIELDNAME_VAGUE_DATE, queryDateFinal);
							updateBuilder.setWhere(where);
							updateBuilder.update();
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
					pd.cancel();
				}
				@Override
				public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
					Toast.makeText(LabActivity.this, "网络连接失败，请重试", Toast.LENGTH_SHORT).show();
					pd.cancel();
				}
			});
		break;
		case R.id.btn_recover:
			SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat df3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String todayDate = df2.format(Calendar.getInstance().getTime());
			if(todayDate.equals(tv_datepicker1.getText().toString())){
				Toast.makeText(LabActivity.this, "不可恢复当天日期", Toast.LENGTH_SHORT).show();
		    	return;
			}
			Date querydate2 = null;
			try {
				querydate2 = df3.parse(tv_datepicker1.getText().toString()+" 00:00:00");
			} catch (ParseException e) {
				e.printStackTrace();
			}
			List<AQIData> aqidataList2 = new ArrayList<AQIData>();
		    try {
		    	QueryBuilder<AQIData, Date> queryBuilder2 = simpleDao.queryBuilder();
				queryBuilder2.where().eq(AQIData.FIELDNAME_VAGUE_DATE, querydate2);
				PreparedQuery<AQIData> preparedQuery2 = queryBuilder2.prepare();
				aqidataList2 = simpleDao.query(preparedQuery2);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			if(aqidataList2.size()!=0){
				Toast.makeText(LabActivity.this, "选择日数据无需恢复", Toast.LENGTH_SHORT).show();
				return;
			}
			pd.setMessage("正在从云端恢复数据，请稍候...");
			pd.setCancelable(false);
			pd.show();
			RequestParams params2 = new RequestParams();
			params2.put("vaguedate", tv_datepicker1.getText().toString()+" 00:00:00");
			params2.put("username", username);
			PMHttpClient.post(LabActivity.this, PMHttpClient.URL_RECOVER, params2 , new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
					String result = new String(arg2);
					if(result.trim().equals("1")){
						Toast.makeText(LabActivity.this, "云端不存在选择日数据", Toast.LENGTH_SHORT).show();
					}else if(result.trim().equals("2")){
						Toast.makeText(LabActivity.this, "取回数据时出错，恢复失败", Toast.LENGTH_SHORT).show();
					}else{
						Toast.makeText(LabActivity.this, "数据取回成功", Toast.LENGTH_SHORT).show();
						try {
							SimpleDateFormat df4 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							JSONObject jsonObject = new JSONObject(result);
							JSONArray jsonArray = jsonObject.getJSONArray("data");
							for(int i=0; i<jsonArray.length(); i++){
								JSONObject object = jsonArray.getJSONObject(i);
								Date dateInCase=null, dateVague=null;
								try {
									dateInCase = df4.parse(object.getString("date"));
									dateVague  = df4.parse(object.getString("vaguedate"));
								} catch (ParseException e) {
									e.printStackTrace();
								}
								AQIData queryData;
								try {
									queryData = simpleDao.queryForId(dateInCase);
									if(queryData==null){ //防止服务器有重复数据
										AQIData newdata = new AQIData(dateInCase, dateVague, Integer.valueOf(object.getString("aqi")),  Integer.valueOf(object.getString("temp")), true);
										simpleDao.create(newdata);
									}
								} catch (SQLException e) {
									e.printStackTrace();
								}
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
					pd.cancel();
				}
				@Override
				public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
					Toast.makeText(LabActivity.this, "网络连接失败，请重试", Toast.LENGTH_SHORT).show();
					pd.cancel();
				}
			});
		break;
		case R.id.btn_bigdata:
			if(!hasGotPos){
				Toast.makeText(LabActivity.this, "请先选择要查看的城市", Toast.LENGTH_SHORT).show();
				return;
			}
			pd.setMessage("正在从云端分析大数据...\n该操作耗时较长");
			pd.setCancelable(false);
			pd.show();
			RequestParams params3 = new RequestParams();
			params3.put("vaguedate", tv_datepicker2.getText().toString()+" 00:00:00");
			params3.put("city", tv_city.getText().toString());
			PMHttpClient.post(LabActivity.this, PMHttpClient.URL_BIGDATA, params3 , new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
					String result = new String(arg2);
					double totalAqi = 0.0, totalTemp = 0.0;
					int totalC = 0, totalPos = 1;
					if(result.trim().equals("1")){
						Toast.makeText(LabActivity.this, "该地区暂没有人共享这天的数据", Toast.LENGTH_SHORT).show();
					}else if(result.trim().equals("2")){
						Toast.makeText(LabActivity.this, "云端分析数据出错，请重试", Toast.LENGTH_SHORT).show();
					}else{
						mBaiduMap.setMyLocationEnabled(false);
						for(Marker m : markerObjList){
							m.remove();
						}
						markerObjList.clear();
						markerList.clear();
						mBaiduMap.clear();
						int maxAQI=0,minAQI=5000;
						try {
							SimpleDateFormat df4 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							JSONObject jsonObject = new JSONObject(result);
							JSONArray jsonArray = jsonObject.getJSONArray("data");
							double sumAqi = 0.0, sumTemp = 0.0;
							int tmpC = 0;
							double lastLat = 0.0, lastLon = 0.0;
							boolean isFirst = true;
							totalC = jsonArray.length();
							for(int i=0; i<jsonArray.length(); i++){
								JSONObject object = jsonArray.getJSONObject(i);
								DecimalFormat defll = new DecimalFormat(".000");
								Double thisLat = Double.valueOf(defll.format(Double.valueOf(object.get("lat").toString())));
								Double thisLon = Double.valueOf(defll.format(Double.valueOf(object.get("lon").toString())));
								if(!isFirst && (thisLon!=lastLon || thisLat!=lastLat)){
									LatLng point = new LatLng(lastLat, lastLon);  
									sumAqi /= tmpC;
									sumTemp /= tmpC;
									DecimalFormat def = new DecimalFormat(".0");
									Bitmap imgMarker = BitmapFactory.decodeResource(getResources(), R.drawable.icon_tag);
									BitmapDescriptor bitmap = BitmapDescriptorFactory  
											    .fromBitmap(createBitmap(def.format(sumAqi)+"/"+def.format(sumTemp)+"℃",imgMarker)); 
									OverlayOptions option = new MarkerOptions()  
									    .position(point)  
									    .icon(bitmap)
									    .animateType(MarkerAnimateType.drop)
									    .title(String.valueOf(totalPos));
									String markerText = sumAqi+"-"+sumTemp+"-"+lastLat+"-"+lastLon;
									Marker newMark = (Marker)mBaiduMap.addOverlay(option);
									markerObjList.add(newMark);
									markerList.put(String.valueOf(totalPos), markerText);
									sumAqi=0.0; sumTemp=0.0; tmpC=0; totalPos++;
								}
								tmpC++;
								isFirst=false;
								sumAqi+=Integer.valueOf(object.get("aqi").toString());
								sumTemp+=Integer.valueOf(object.get("temp").toString());
								totalAqi+=Integer.valueOf(object.get("aqi").toString());
								totalTemp+=Integer.valueOf(object.get("temp").toString());
								lastLat = thisLat;
								lastLon = thisLon;
								if(Integer.valueOf(object.get("aqi").toString())>=maxAQI){
									maxAQI = Integer.valueOf(object.get("aqi").toString());
								}
								if(Integer.valueOf(object.get("aqi").toString())<=minAQI){
									minAQI = Integer.valueOf(object.get("aqi").toString());
								}
								if(i==jsonArray.length()-1){
									LatLng point = new LatLng(lastLat, lastLon);  
									sumAqi /= tmpC;
									sumTemp /= tmpC;
									DecimalFormat def = new DecimalFormat(".0");
									Bitmap imgMarker = BitmapFactory.decodeResource(getResources(), R.drawable.icon_tag);
									BitmapDescriptor bitmap = BitmapDescriptorFactory  
											    .fromBitmap(createBitmap(def.format(sumAqi)+"/"+def.format(sumTemp)+"℃",imgMarker)); 
									OverlayOptions option = new MarkerOptions()  
									    .position(point)  
									    .icon(bitmap)
									    .animateType(MarkerAnimateType.drop)
									    .title(String.valueOf(totalPos));
									String markerText = sumAqi+"-"+sumTemp+"-"+lastLat+"-"+lastLon;
									Marker newMark = (Marker)mBaiduMap.addOverlay(option);
									markerObjList.add(newMark);
									markerList.put(String.valueOf(totalPos), markerText);
									//移动地图
									LatLng ll = new LatLng(lastLat,lastLon);
					                MapStatus.Builder builder = new MapStatus.Builder();
					                builder.target(ll).zoom(14.0f);
					                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
								}
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
						totalAqi/=totalC; totalTemp/=totalC;
						DecimalFormat decimalf = new DecimalFormat(".00");
						StringBuilder sb = new StringBuilder();
						sb.append(tv_city.getText().toString()+"这一天，AQI平均值");
						sb.append(decimalf.format(totalAqi)+"，气温平均值："+decimalf.format(totalTemp)+"℃\n");
						sb.append("AQI最大值："+maxAQI+"  AQI最小值："+minAQI);
						sb.append("\n共计"+totalPos+"个地点共享了这一天的数据\n");
						sb.append("您可以点击地图上的标记来查看精确信息\n用户密度较大区域需要您放大查看\n点击后出现的蓝圈代表该点数据描述范围");
						tv_bigdata.setText(sb.toString());
					}
					pd.cancel();
				}
				@Override
				public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
					Toast.makeText(LabActivity.this, "网络连接失败，请重试", Toast.LENGTH_SHORT).show();
					pd.cancel();
				}
			});
			
		break;
		case R.id.tv_city:
			Intent intent = new Intent(LabActivity.this,
					GetProviceActivity.class);
			startActivityForResult(intent, BEGIN_CODE);
		break;
		case R.id.tv_userstate:
			if(!mLocationClient.isStarted() && !hasGotPos){
				Toast.makeText(LabActivity.this, "开始重新定位", Toast.LENGTH_SHORT).show();
				mLocationClient.start();
			}
		break;
		case R.id.btn_backtohome:
			finish();
		break;
		default:
		break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case BEGIN_CODE:
			if(data==null)
				return;
			Bundle bundle = data.getExtras();
			String cityName = bundle.getString("cityName");
			tv_city.setText(cityName);
			hasGotBigdataLoc = true;
			break;
		default:
			break;
		}
	}
	
	@Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
		StringBuilder sb = new StringBuilder();
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
        	sb.append("地点周边无准确地名描述\n");
        }else{
        	sb.append(result.getAddress()+"\n");
        }
        DecimalFormat decimalf = new DecimalFormat(".000");
        sb.append("AQI指数："+decimalf.format(Double.valueOf(aqi4Show))+"\n");
        sb.append("气      温："+decimalf.format(Double.valueOf(temp4Show))+"℃");
        AlertDialog.Builder builder = new AlertDialog.Builder(LabActivity.this,AlertDialog.THEME_HOLO_LIGHT);
        builder.setMessage(sb.toString())
        .setPositiveButton("关闭", null)
        .setCancelable(true);
        builder.show();
    }
	
	private Bitmap createBitmap(String letter, Bitmap orgImgMarker) {
		Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG
				| Paint.DEV_KERN_TEXT_FLAG);
		textPaint.setTextSize(DensityUtil.sp2px(LabActivity.this, 14.0f));
		textPaint.setTypeface(Typeface.DEFAULT); // 采用默认的宽度
		textPaint.setColor(Color.RED);
		Rect rect = new Rect();
		textPaint.getTextBounds(letter, 0, letter.length(), rect);
		int tw = rect.width();  
		int th = rect.height(); 
		//tw = DensityUtil.dip2px(LabActivity.this, tw);
		//th = DensityUtil.dip2px(LabActivity.this, th);
		
		/*int width = orgImgMarker.getWidth();
		int height = orgImgMarker.getHeight();
	    float scaleWidth = ((float) tw+8) / width;  
	    float scaleHeight = ((float) th+8) / height;
	    Matrix matrix = new Matrix();  
	    matrix.postScale(scaleWidth, scaleHeight);*/
	    //Bitmap imgMarker = Bitmap.createBitmap(orgImgMarker, 0, 0, width, height, matrix,true);  
	    int width = tw+8;
		int height =(int) (th*2);
		Bitmap imgMarker = Bitmap.createScaledBitmap(orgImgMarker, width, height, true);
	    
		Log.d("sendmsg", (width)+" width");
		Log.d("sendmsg", (height)+" height");
		Bitmap imgTemp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(imgTemp); 

		Paint paint = new Paint(); // 建立画笔
		paint.setDither(true);
		paint.setFilterBitmap(true);
		Rect src = new Rect(0, 0, width, height);
		Rect dst = new Rect(0, 0, width, height);
		canvas.drawBitmap(imgMarker, src, dst, paint);

		float baseline = height/2 + textPaint.getTextSize()/2 - textPaint.getFontMetrics().descent;
		//FontMetricsInt fontMetrics = textPaint.getFontMetricsInt();
		//int baseline = (height - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top;  
		
		canvas.drawText(letter, 4, baseline, textPaint);
		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();
		return (new BitmapDrawable(getResources(),imgTemp)).getBitmap();

	}

	@Override
	public void onGetGeoCodeResult(GeoCodeResult arg0) {}
}
