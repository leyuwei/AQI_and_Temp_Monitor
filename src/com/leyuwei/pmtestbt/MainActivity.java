package com.leyuwei.pmtestbt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import lecho.lib.hellocharts.formatter.SimpleAxisValueFormatter;
import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.listener.ViewportChangeListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;
import lecho.lib.hellocharts.view.PreviewLineChartView;

import org.apache.http.Header;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
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
 * @since 2017/05/12 18:52
 * @description MainView Controller
 *
 */
public class MainActivity extends FragmentActivity implements OnClickListener,onCalendarSelectedListener{
	/*
	 * constants' and variables' declarations
	 */
	private static final int REQUEST_ENABLE_BT = 0;
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_OPEN_LAB = 6;
	private static final int CONSTAT_OK = 0;
	private static final int CONSTAT_FAIL = 1;
	private static final int CONSTAT_UNKNOWN = -1;
	private static final int CATA_AQI = 0;
	private static final int CATA_TEMP = 1;
	private static int conStat = CONSTAT_UNKNOWN;
	private static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
	private static final String comBuf1 = "BB 01 01 01 01 01 01 01 01 01 00 09 FF";
	private static final String comBuf2 = "BB 02 02 02 02 02 02 02 02 02 00 12 FF";
	private static final String comBuf3 = "BB 03 03 03 03 03 03 03 03 03 00 1B FF";
	private static final String comBuf4 = "BB 04 04 04 04 04 04 04 04 04 00 24 FF";
	private static final String comBuf5 = "BB 05 05 05 05 05 05 05 05 05 00 2D FF";
	
	private int comi = 0;
	private int comInd = 0;
	private int bufBT[];
	private boolean isFirstExe = false;
	private boolean isPause = false;
	private List<Integer> mBuffer;
	private String strDataBuf;
	private BluetoothAdapter mBluetoothAdapter;
	private ConnectThread mConnectThread;
	public ConnectedThread mConnectedThread;
	private static final int MSG_NEW_DATA = 3;
	
	private SharedPreferences sp;
	private Editor editor;
	private Dao<AQIData, Date> simpleDao = null;
	private DatabaseHelper databaseHelper = null;
	private List<Integer> mAQIData = null;
	private List<String> mTimeData = null;
	private List<Integer> mTempData = null;
	private List<Integer> mAQIDispData = null;
	private List<String> mTimeDispData = null;
	private List<Integer> mTempDispData = null;
	private static int mDisplayCata = CATA_AQI;
	
	private LineChartView chart;
    private PreviewLineChartView previewChart;
    private LineChartData data,data2,previewData;
	private LinearLayout ll2;
	private ScrollView scrollView;
	private Button btn_save;
	private ImageView img_nodata,img_modesel;
	private EditText ed_th,ed_th_danger,ed_usr,ed_pwd;
	private TextView tv_stat,tv_advice,tv_device,tv_datepicker;
	
	private ProgressDialog pd;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getApplicationContext());
		requestWindowFeature(Window.FEATURE_NO_TITLE); //remove actionbar
		setContentView(R.layout.activity_main);
		
		//init data keeper
		sp = this.getSharedPreferences("pmtester", Activity.MODE_PRIVATE);
		editor = sp.edit();
		
		//init Bluetooth BroadcastReceiver
		registerReceiver(mReceiver, makeFilter());
		
		//show welcome tips diag
		if(sp.getBoolean("isFirstInit", true)){
			AlertDialog.Builder builder = new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_LIGHT);
			builder.setCancelable(false)
			.setMessage(this.getResources().getString(R.string.str_hint_first_open))
			.setPositiveButton("我知道了", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//initate bluetooth connection
					mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
					if (mBluetoothAdapter == null) {
						Toast.makeText(MainActivity.this, "蓝牙不可用",
								Toast.LENGTH_LONG).show();
						finish();
						return;
					}
					// If BT is not on, request that it be enabled.
					// setupChat() will then be called during onActivityResult
					if (!mBluetoothAdapter.isEnabled()) {
						Intent enableIntent = new Intent(
								BluetoothAdapter.ACTION_REQUEST_ENABLE);
						MainActivity.this.startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
						// Otherwise, setup the chat session
					}
				}
			})
			.show();
			editor.putBoolean("isFirstInit", false);
			editor.commit();
		}else{
			AlertDialog.Builder builder = new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_LIGHT);
			builder.setCancelable(false)
			.setMessage(this.getResources().getString(R.string.str_hint_open))
			.setPositiveButton("打开应用", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//initate bluetooth connection
					mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
					if (mBluetoothAdapter == null) {
						Toast.makeText(MainActivity.this, "蓝牙不可用",
								Toast.LENGTH_LONG).show();
						finish();
						return;
					}
					// If BT is not on, request that it be enabled.
					// setupChat() will then be called during onActivityResult
					if (!mBluetoothAdapter.isEnabled()) {
						Intent enableIntent = new Intent(
								BluetoothAdapter.ACTION_REQUEST_ENABLE);
						MainActivity.this.startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
						// Otherwise, setup the chat session
					}
				}
			})
			.show();
		}
		Toast.makeText(this.getBaseContext(), "中部可以上下滚动查看", Toast.LENGTH_SHORT).show();
		
		//initiate those variables
		bufBT = new int[13];
		mBuffer = new ArrayList<Integer>();
		comInd = 0;
		isFirstExe = true;
		pd = new ProgressDialog(MainActivity.this,ProgressDialog.THEME_HOLO_LIGHT);
		mAQIData = new ArrayList<Integer>();
		mTimeData = new ArrayList<String>();
		mTempData = new ArrayList<Integer>();
		mAQIDispData = new ArrayList<Integer>();
		mTimeDispData = new ArrayList<String>();
		mTempDispData = new ArrayList<Integer>();
		
		//connect the variables to the elements of corresponding view
		//binding buttons with the OnClickListener
		//binding edittexts with its watcher
		chart = (LineChartView) findViewById(R.id.dotchart);
        previewChart = (PreviewLineChartView) findViewById(R.id.dotchart_preview);
		findViewById(R.id.btn_exit).setOnClickListener(this);
		btn_save = (Button) findViewById(R.id.btn_save);
		btn_save.setOnClickListener(this);
		findViewById(R.id.btn_update).setOnClickListener(this);
		findViewById(R.id.btn_usr_bak).setOnClickListener(this);
		findViewById(R.id.btn_usr_rec).setOnClickListener(this);
		findViewById(R.id.btn_reset).setOnClickListener(this);
		findViewById(R.id.btn_synctime).setOnClickListener(this);
		findViewById(R.id.btn_shutdown).setOnClickListener(this);
		findViewById(R.id.btn_transback).setOnClickListener(this);
		findViewById(R.id.btn_transthre).setOnClickListener(this);
		findViewById(R.id.tv_datepicker).setOnClickListener(this);
		ed_pwd = (EditText) findViewById(R.id.ed_password);
		ed_usr = (EditText) findViewById(R.id.ed_username);
		ed_usr.setText(sp.getString("username", ""));
		ed_pwd.setText(sp.getString("password", ""));
		ed_th = (EditText) findViewById(R.id.ed_threshold);
		ed_th_danger = (EditText) findViewById(R.id.ed_threshold_danger);
		img_nodata = (ImageView) findViewById(R.id.img_nodata);
		img_modesel = (ImageView) findViewById(R.id.img_modesel);
		ll2 = (LinearLayout) findViewById(R.id.ll2);
		scrollView = (ScrollView) findViewById(R.id.scrollView1);
		tv_stat = (TextView) findViewById(R.id.tv_statistics);
		tv_device = (TextView) findViewById(R.id.tv_device);
		tv_advice = (TextView) findViewById(R.id.tv_advices);
		tv_datepicker = (TextView) findViewById(R.id.tv_datepicker);
		img_nodata.setVisibility(View.GONE);
		previewChart.setVisibility(View.VISIBLE);
		chart.setVisibility(View.VISIBLE);
		img_modesel.setOnClickListener(this);
		ed_th.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			@Override
			public void afterTextChanged(Editable s) {
				if(s.toString().equals("")){
					return;
				}
				if(Integer.valueOf(s.toString())<=0 || Integer.valueOf(s.toString())>500){
					ed_th.setText("100");
					Toast.makeText(MainActivity.this.getBaseContext(), "数值范围只允许在1~500之间", Toast.LENGTH_LONG).show();
				}
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}
		});
		ed_th_danger.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			@Override
			public void afterTextChanged(Editable s) {
				if(s.toString().equals("")){
					return;
				}
				if(Integer.valueOf(s.toString())<=0 || Integer.valueOf(s.toString())>500 ){
					ed_th_danger.setText("200");
					Toast.makeText(MainActivity.this.getBaseContext(), "数值范围只允许在1~500之间且必须比雾霾线高", Toast.LENGTH_LONG).show();
				}
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}
		});
		
		//初始化ormlite数据库，获取数据库操作对象
		try {
			simpleDao = getHelper().getDataDao();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		//初始化图表（测试阶段）
		mDisplayCata = CATA_AQI; //设置AQI显示模式
		Date date = Calendar.getInstance().getTime();
		SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
		String curDate = df2.format(date) + " 00:00:00";
		tv_datepicker.setText(df2.format(date));
        showDataOnChart(curDate, CATA_AQI);
        
        //scroll scrollview to top
        scrollView.scrollTo(0, 0);
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
	
	/**
	 * @category 在图上显示指定天的数据
	 * @param date 显示日期
	 * @param which 显示AQI还是温度 
	 */
	private void showDataOnChart(String dateStr, int which) {
		final String dateStr2 = dateStr;
		final int which2 = which;
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				Message msg = new Message();
		        msg.obj = dateStr2;
		        msg.what = which2;
		        mDataHandler.sendMessage(msg); //耗时操作交给异步处理！
			}
		});
		thread.run();
    }
	
	/**
	 * @category 评价指定天的所有数据并给出建议方案的异步手柄
	 */
	private Handler mDataHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			int which = msg.what;
			String dateStr = (String) msg.obj;
			// TODO 获取数据
			List<PointValue> values = new ArrayList<PointValue>();
			List<AQIData> aqidataList = new ArrayList<AQIData>();
		    QueryBuilder<AQIData, Date> queryBuilder = simpleDao.queryBuilder();
		    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		    Date querydate = null;
		    try {
				querydate = df.parse(dateStr);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			try {
				queryBuilder.where().eq(AQIData.FIELDNAME_VAGUE_DATE, querydate);
				PreparedQuery<AQIData> preparedQuery = queryBuilder.prepare();
			    aqidataList = simpleDao.query(preparedQuery);
			    // TODO 读取来的选择的日期的所有数据 循环进行加工处理
			    if(which == CATA_AQI){
				    for (AQIData simple : aqidataList) {
				    	values.add(new PointValue(formatDateToFloat(simple.getDate()),simple.getAqi()));
				    	//Log.d("showData", simple.toString()+" "+formatDateToFloat(simple.getDate())+" "+simple.getAqi());
					}
				    chart.setOnValueTouchListener(new LineChartOnValueSelectListener() {
						@Override
						public void onValueDeselected() {}
						@Override
						public void onValueSelected(int lineIndex, int pointIndex, PointValue value) {
							 Toast.makeText(MainActivity.this, "时间："+formatMinutes(value.getX(),false)+"\nAQI："+value.getY(), Toast.LENGTH_SHORT).show();
						}
					});
			    }else{
			    	for (AQIData simple : aqidataList) {
				    	values.add(new PointValue(formatDateToFloat(simple.getDate()),simple.getTemp()));
				    	//Log.d("showData", simple.toString()+" "+formatDateToFloat(simple.getDate())+" "+simple.getTemp());
			    	}
			    	chart.setOnValueTouchListener(new LineChartOnValueSelectListener() {
						@Override
						public void onValueDeselected() {}
						@Override
						public void onValueSelected(int lineIndex, int pointIndex, PointValue value) {
							 Toast.makeText(MainActivity.this, "时间："+formatMinutes(value.getX(),false)+"\n温度："+(int)value.getY()+"℃", Toast.LENGTH_SHORT).show();
						}
					});
			    }
			    if(aqidataList.size()==0){
			    	img_nodata.setVisibility(View.VISIBLE);
					previewChart.setVisibility(View.INVISIBLE);
					chart.setVisibility(View.INVISIBLE);
					tv_advice.setText("N/A");
					tv_stat.setText("N/A");
					return;
			    }else{
			    	img_nodata.setVisibility(View.GONE);
					previewChart.setVisibility(View.VISIBLE);
					chart.setVisibility(View.VISIBLE);
			    }
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			// TODO 显示数据
			Line line = new Line(values);
	        line.setColor(ChartUtils.COLOR_BLUE);
	        line.setCubic(false);
	        line.setHasPoints(false);
	        List<Line> lines = new ArrayList<Line>();
	        lines.add(line);
	        
	        Line line2 = new Line(values);
	        line2.setColor(ChartUtils.COLOR_BLUE);
	        line2.setCubic(false);
	        line2.setShape(ValueShape.CIRCLE);
	        line2.setHasPoints(true);
	        line2.setStrokeWidth(1);
	        List<Line> lines2 = new ArrayList<Line>();
	        lines2.add(line2);
		    
	        List<AxisValue> axisValues = new ArrayList<AxisValue>();
	        for (float i = 0; i < 24; i += 0.0833f) {
	            axisValues.add(new AxisValue(i).setLabel(formatMinutes(i,true)));
	        }
	        
	        Axis axisX = new Axis(axisValues);
	        axisX.setMaxLabelChars(5);
	        axisX.setHasTiltedLabels(true);
	        axisX.setTextColor(Color.BLACK);
	        axisX.setLineColor(Color.GRAY);
	        axisX.setInside(true);
	        Axis axisY = new Axis().setHasLines(true);
	        if(which == CATA_TEMP){
	        	axisY.setFormatter(new SimpleAxisValueFormatter().setAppendedText("℃".toCharArray()));
	        	axisY.setMaxLabelChars(5);
	        }else{
	        	axisY.setMaxLabelChars(4);
	        }
	        data = new LineChartData(lines2);
	        data.setAxisXBottom(axisX);
	        data.setAxisYLeft(axisY);
	        
	        data2 = new LineChartData(lines);
	        previewData = new LineChartData(data2);
	        previewData.setAxisXBottom(null);
	        previewData.setAxisYLeft(null);
	        previewData.getLines().get(0).setColor(ChartUtils.DEFAULT_DARKEN_COLOR);
	        
	        chart.resetViewports();
	        previewChart.resetViewports();
	        
	        if(which == CATA_TEMP){
	        	chart.setViewportCalculationEnabled(false);
	        }else{
	        	chart.setViewportCalculationEnabled(true);
	        }
	        chart.setLineChartData(data);
	        if(which == CATA_TEMP){
		        Viewport v = new Viewport(chart.getMaximumViewport());
		        v.top =50f;
		        v.bottom = -20f; 
		        chart.setMaximumViewport(v);
		        chart.setCurrentViewport(v);
	        }
	        chart.setZoomEnabled(true);
	        chart.setScrollEnabled(true);
	        chart.setZoomType(ZoomType.HORIZONTAL);
	        chart.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
	        
	        previewChart.setLineChartData(previewData);
	        if(which == CATA_TEMP){
	        	Viewport v = new Viewport(chart.getMaximumViewport());
	        	v.top =50f;
	        	v.bottom = -20f;
	        	previewChart.setMaximumViewport(v);
	        	previewChart.setCurrentViewport(v);
	        }
	        previewChart.setViewportChangeListener(new ViewportListener());
	        previewX(true);
	        
			// TODO 评价数据并显示
			if(aqidataList.size()==0)
				return;
			float sum_aqi = 0.0f , sum_temp = 0.0f;
			float avg_aqi=0.0f, avg_temp = 0.0f;
			AQIData lastDate = null;
			boolean isFullyUploaded = true, isContainAfternoon = false;
			int count = 0, countsection = 1;
			int max_aqi = 0, max_temp = -100, min_aqi = 5000, min_temp = 200;
			List<String> startTimeList = new ArrayList<String>();
			List<String> maxAQIList = new ArrayList<String>();
			List<String> minAQIList = new ArrayList<String>();
			List<String> maxTempList = new ArrayList<String>();
			List<String> minTempList = new ArrayList<String>();
			SimpleDateFormat df2 = new SimpleDateFormat("HH:mm:ss");
			SimpleDateFormat df3 = new SimpleDateFormat("MM月dd号");
			for(AQIData aqidata: aqidataList){
				sum_aqi+=aqidata.getAqi();
				sum_temp+=aqidata.getTemp();
				if(aqidata.getIsUploaded()==false)
					isFullyUploaded = false;
				int containTime = Integer.valueOf(df2.format(aqidata.getDate()).split(":")[0]);
				if(containTime>12 && containTime<18)
					isContainAfternoon = true;
				if(aqidata.getAqi()>=max_aqi){
					if(aqidata.getAqi()==max_aqi)
						maxAQIList.add(df2.format(aqidata.getDate()));
					else{
						maxAQIList.clear();
						max_aqi = aqidata.getAqi();
						maxAQIList.add(df2.format(aqidata.getDate()));
					}
				}
				if(aqidata.getAqi()<=min_aqi){
					if(aqidata.getAqi()==min_aqi)
						minAQIList.add(df2.format(aqidata.getDate()));
					else{
						minAQIList.clear();
						min_aqi = aqidata.getAqi();
						minAQIList.add(df2.format(aqidata.getDate()));
					}
				}
				if(aqidata.getTemp()>=max_temp){
					if(aqidata.getTemp()==max_temp)
						maxTempList.add(df2.format(aqidata.getDate()));
					else{
						maxTempList.clear();
						max_temp = aqidata.getTemp();
						maxTempList.add(df2.format(aqidata.getDate()));
					}
				}
				if(aqidata.getTemp()<=min_temp){
					if(aqidata.getTemp()==min_temp)
						minTempList.add(df2.format(aqidata.getDate()));
					else{
						minTempList.clear();
						min_temp = aqidata.getTemp();
						minTempList.add(df2.format(aqidata.getDate()));
					}
				}
				if(count==0){
					lastDate = aqidata;
					startTimeList.add(df2.format(aqidata.getDate()));
				}else{
					if(TimeUtil.getTimeDifference(df.format(lastDate.getDate()), df.format(aqidata.getDate()))>=120){
						//如果时间差大于2分钟 判断为两段数据
						countsection++;
						startTimeList.add(df2.format(aqidata.getDate()));
					}
					lastDate = aqidata;
				}
				count++;
			}
			avg_aqi = sum_aqi / (float)count;
			avg_temp = sum_temp / (float)count;
			StringBuilder strStat = new StringBuilder();
			StringBuilder strAdvice = new StringBuilder();
			strStat.append(df3.format(querydate)+"，您在");
			strStat.append(df2.format(aqidataList.get(0).getDate()));
			strStat.append("到");
			strStat.append(df2.format(aqidataList.get(aqidataList.size()-1).getDate()));
			strStat.append("之间，进行了"+countsection+"次连续采集工作。采集时间在：\n");
			
			if(countsection>10)
				strAdvice.append("· 采集次数过多，建议您在早中晚各分别采集一次以获得准确分析数据\n");
			if(!isFullyUploaded)
				strAdvice.append("· 今日数据仍有部分没有备份完整\n");
			int headtime = Integer.valueOf(df2.format(aqidataList.get(0).getDate()).split(":")[0]);
			int tailtime = Integer.valueOf(df2.format(aqidataList.get(aqidataList.size()-1).getDate()).split(":")[0]);
			if(headtime<=12&&tailtime<=12){
				strAdvice.append("· 缺少下午和晚间数据，数据完整性严重欠缺\n");
			}else if(headtime>=18&&tailtime>=18){
				strAdvice.append("· 缺少晨间和下午数据，数据完整性严重欠缺\n");
			}else if(headtime>12&&headtime<18&&tailtime>12&&tailtime<18){
				strAdvice.append("· 缺少晨间和晚间数据，数据完整性严重欠缺\n");
			}else if(headtime>12&&headtime<18&&tailtime>=18){
				strAdvice.append("· 缺少晨间数据，数据完整性欠缺\n");
			}else if(headtime>=0&&headtime<=12&&tailtime>12&&tailtime<18){
				strAdvice.append("· 缺少晚间数据，数据完整性欠缺\n");
			}else if(headtime<=12&&tailtime>=18&&(!isContainAfternoon)){
				strAdvice.append("· 缺少下午数据，数据完整性欠缺\n");
			}
			
			int i = 0;
			for(String s : startTimeList){
				i++;
				strStat.append(s);
				if(i>10){
					strStat.append("等时间");
					break;
				}
				if(i<countsection)
					strStat.append("、");
			}
			i=0;
			strStat.append("\n------------------------\nAQI最大值： "+max_aqi+"\n@ ");
			for(String s : maxAQIList){
				i++;
				strStat.append(s);
				if(i>2){
					strStat.append(" 等"+maxAQIList.size()+"个时间点");
					break;
				}
				if(i<maxAQIList.size())
					strStat.append("、");
			}
			i=0;
			strStat.append("\n------------------------\nAQI最小值： "+min_aqi+"\n@ ");
			for(String s : minAQIList){
				i++;
				strStat.append(s);
				if(i>2){
					strStat.append(" 等"+minAQIList.size()+"个时间点");
					break;
				}
				if(i<minAQIList.size())
					strStat.append("、");
			}
			i=0;
			strStat.append("\n------------------------\n气温最大值： "+max_temp+"℃\n@ ");
			for(String s : maxTempList){
				i++;
				strStat.append(s);
				if(i>2){
					strStat.append(" 等"+maxTempList.size()+"个时间点");
					break;
				}
				if(i<maxTempList.size())
					strStat.append("、");
			}
			i=0;
			strStat.append("\n------------------------\n气温最小值： "+min_temp+"℃ \n@ ");
			for(String s : minTempList){
				i++;
				strStat.append(s);
				if(i>2){
					strStat.append(" 等"+minTempList.size()+"个时间点");
					break;
				}
				if(i<minTempList.size())
					strStat.append("、");
			}
			if(avg_temp>30){
				strAdvice.append("· 天气非常炎热，出门注意防晒\n");
			}else if(max_temp>=25 && avg_temp>=25){
				strAdvice.append("· 天气炎热，出门记得涂防晒哦\n");
			}else if(max_temp>=25 && avg_temp<=15){
				strAdvice.append("· 天气有时较热，注意衣物增减\n");
			}else if(max_temp<=10&&max_temp>0){
				strAdvice.append("· 天气较凉，出门注意保暖\n");
			}else if(max_temp<=0){
				strAdvice.append("· 零下的天气，出门前一定要注意保暖哦\n");
			}else if(avg_temp>12&&avg_temp<=23&&max_aqi<=200){
				strAdvice.append("· 温度较为适宜，适合踏青出行\n");
			}else if(avg_temp>12&&avg_temp<=23&&max_aqi>500){
				strAdvice.append("· 温度较为适宜，但空气质量较差，不建议踏青出行\n");
			}else if(avg_temp>12&&avg_temp<=23&&max_aqi<=500&&max_aqi>200){
				strAdvice.append("· 温度较为适宜，但空气质量较差，踏青出行带上口罩\n");
			}
			if(max_temp-min_temp>10){
				strAdvice.append("· 温差较大，注意适量增减衣物\n");
			}
			if(max_aqi>500 && avg_aqi<200){
				strAdvice.append("· 环境灰尘有时较大，带只口罩注意身体\n");
			}else if(max_aqi>500 && avg_aqi>500){
				strAdvice.append("· 环境污染严重，请携带口罩出行\n");
			}else if(max_aqi<200 && max_aqi>=100){
				strAdvice.append("· 空气较为清新，请放心出行\n");
			}else if(max_aqi<100){
				strAdvice.append("· 空气非常清新，请放心出行\n");
			}
			if(max_aqi-min_aqi>200){
				strAdvice.append("· 空气质量时好时坏，请多留意\n");
			}
			strAdvice.deleteCharAt(strAdvice.length()-1); 
			DecimalFormat decimalf = new DecimalFormat(".00");
			float avg_temp_F = 32.0f + avg_temp*1.8f;
			strStat.append("\n------------------------\nAQI的平均值："+decimalf.format(avg_aqi)+"\n气温的平均值："+decimalf.format(avg_temp)+"℃ 或 "+decimalf.format(avg_temp_F)+"℉");
			tv_stat.setText(strStat.toString());
			tv_advice.setText(strAdvice.toString());
		}
	};
	

	/**
	 * @category override onDestroy() to release the helper when done.
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (databaseHelper != null) {
			OpenHelperManager.releaseHelper();
			databaseHelper = null;
		}
		PMHttpClient.stopAllConnection(this);
	}
	
	/**
	 * @category 图的横轴转换为时间 由float转换成时间格式
	 * @param value 浮点值
	 * @return 时间字符串
	 */
	private String formatMinutes(float value, boolean isShort) {
        StringBuilder sb = new StringBuilder();

        int valueInSecs = (int) (value * 3600);
        int hours = (int) Math.floor(valueInSecs / 3600);
        int minutes = (int) Math.floor((valueInSecs-3600*hours) / 60);
        int secs = (int) valueInSecs-3600*hours-60*minutes;

        sb.append(String.valueOf(hours)).append(':');
        if (minutes < 10) {
            sb.append('0');
        }
        sb.append(String.valueOf(minutes));
        if(isShort)
        	return sb.toString();
        
        sb.append(':');
        if (secs < 10) {
            sb.append('0');
        }
        sb.append(String.valueOf(secs));
        
        return sb.toString();
    }
	
	/**
	 * @category 将Date转换成最大24.0的浮点数
	 * @param date 日期
	 * @return 浮点数
	 */
	private float formatDateToFloat(Date date){
		SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
		String dateStr = df.format(date);
		String build[] = dateStr.split(":");
		float actualvalue = Integer.valueOf(build[0])*3600+Integer.valueOf(build[1])*60+Integer.valueOf(build[2]);
		return (float)actualvalue/(float)86400.0*(float)24.0;
	}
	
	/**
	 * @category 横着滚动看数据
	 * @param animate 是否播放动画
	 */
	private void previewX(boolean animate) {
        Viewport tempViewport = new Viewport(chart.getMaximumViewport());
        float dx = tempViewport.width() / 2.2f;
        tempViewport.inset(dx, 0);
        tempViewport.offset(-dx, 0);
        if (animate) {
            previewChart.setCurrentViewportWithAnimation(tempViewport);
        } else {
            previewChart.setCurrentViewport(tempViewport);
        }
        previewChart.setZoomType(ZoomType.HORIZONTAL);
    }
	
	/**
     * @category Viewport listener for preview chart(lower one). in {@link #onViewportChanged(Viewport)} method change
     * @category viewport of upper chart.
     */
    private class ViewportListener implements ViewportChangeListener {

        @Override
        public void onViewportChanged(Viewport newViewport) {
            // don't use animation, it is unnecessary when using preview chart.
            chart.setCurrentViewport(newViewport);
        }

    }
	
	/**
	 * @category 退出应用时候的提示函数
	 */
	public void activateQuit(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_LIGHT);
		builder.setMessage("您确定要退出应用吗？");
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				System.exit(0);
			}
		});
		builder.setNegativeButton("取消", null);
		builder.setCancelable(false);
		builder.show();
	}
	
	/**
	 * @category 重载的活动间返回值处理函数，用于处理 打开蓝牙 和 连接蓝牙设备 这两个返回值
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		switch (requestCode) {
		case REQUEST_ENABLE_BT:
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled Launch the DeviceListActivity
				Intent serverIntent = new Intent(this, DeviceListActivity.class);
				startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			} else {
				// User did not enable Bluetooth or an error occured
				Toast.makeText(this, "蓝牙没有打开", Toast.LENGTH_SHORT).show();
				return;
			}
			break;
		case REQUEST_CONNECT_DEVICE:
			if (resultCode != Activity.RESULT_OK) {
				tv_device.setText("没有连接设备");
				return;
			} else {
				String address = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				// Get the BLuetoothDevice object
				BluetoothDevice device = mBluetoothAdapter
						.getRemoteDevice(address);
				// Attempt to connect to the device
				connect(device);
			}
			break;
		case REQUEST_OPEN_LAB:
			//重新初始化ormlite数据库，获取数据库操作对象
			try {
				simpleDao = getHelper().getDataDao();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			break;
		}
	}
	
	/**
	 * @category 注册蓝牙状态改变的广播时所需要的过滤器
	 * @return 意图过滤器
	 */
	private IntentFilter makeFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        return filter;
    }
	
	/**
	 * @category 蓝牙状态改变时，实时改变连接指示变量的广播接收器，为了增强程序整体稳定性
	 * @category 与mHandler联合使用，能够使系统变的更加稳定
	 */
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_TURNING_ON:
                            conStat = CONSTAT_UNKNOWN;
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                        	conStat = CONSTAT_FAIL;
                        	Toast.makeText(MainActivity.this, "系统蓝牙关闭，应用功能不可用", Toast.LENGTH_SHORT).show();
                            break;
                    }
                    break;
            }
        }
    };
	
	/**
	 * @category 负责蓝牙设备的连接，与ConnectThread线程协同工作
	 * @param device 蓝牙设备
	 */
	public void connect(BluetoothDevice device) {
		// Start the thread to connect with the given device
		mConnectThread = new ConnectThread(device);
		mConnectThread.start();
	}
	
	/**
	 * @category 蓝牙实际连接后，成功与否的处理Handler
	 */
	private Handler conStatHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case CONSTAT_OK:
				//Toast.makeText(MainActivity.this, "连接设备成功", Toast.LENGTH_SHORT).show();
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,AlertDialog.THEME_HOLO_LIGHT);
				builder.setMessage("请您注意：请务必确保您的监测仪处在蓝牙透传模式，否则可能会出现不可预料的错误\n如果您在使用中不小心断开了蓝牙，或者监测仪电源不小心被关闭，请一定重新连接设备后继续使用\n使用中有更多问题，您可以电邮leyuwei126@126.com，我们会随时给您解答。")
				.setPositiveButton("好的", null)
				.setCancelable(false)
				.show();
				tv_device.setText("设备已连接");
				conStat=CONSTAT_OK;
				break;
			case CONSTAT_FAIL:
				Toast.makeText(MainActivity.this, "连接设备失败\n请检查蓝牙是否开启，监测器电源是否打开", Toast.LENGTH_SHORT).show();
				tv_device.setText("设备连接失败");
				conStat=CONSTAT_FAIL;
				break;
			}
		};
	};
	
	/**
	 * @category 该线程负责与蓝牙设备进行连接，建立套接字通讯，以及断开连接的操作
	 */
	private class ConnectThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;

		public ConnectThread(BluetoothDevice device) {
			mmDevice = device;
			BluetoothSocket tmp = null;

			// Get a BluetoothSocket for a connection with the
			// given BluetoothDevice
			try {
				tmp = device.createRfcommSocketToServiceRecord(UUID
						.fromString(SPP_UUID));
			} catch (IOException e) {
				e.printStackTrace();
			}
			mmSocket = tmp;
		}

		public void run() {
			setName("ConnectThread");

			// Always cancel discovery because it will slow down a connection
			mBluetoothAdapter.cancelDiscovery();

			// Make a connection to the BluetoothSocket
			try {
				// This is a blocking call and will only return on a
				// successful connection or an exception
				mmSocket.connect();
				conStatHandler.sendEmptyMessage(CONSTAT_OK);
			} catch (IOException e) {
				// Close the socket
				try {
					mmSocket.close();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
				conStatHandler.sendEmptyMessage(CONSTAT_FAIL);
				return;
			}

			mConnectThread = null;

			// Start the connected thread
			// Start the thread to manage the connection and perform
			// transmissions
			mConnectedThread = new ConnectedThread(mmSocket);
			mConnectedThread.start();

		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @category 这个线程在设备连接成功时运行
	 * @category 负责处理所有输入输出流消息并交由handler处理
	 */
	private class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		public ConnectedThread(BluetoothSocket socket) {
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the BluetoothSocket input and output streams
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				e.printStackTrace();
			}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run() {
			byte[] buffer = new byte[256];
			int bytes;

			// Keep listening to the InputStream while connected
			while (true) {
				try {
					// Read from the InputStream
					bytes = mmInStream.read(buffer);
					synchronized (mBuffer) {
						for (int i = 0; i < bytes; i++) {
							mBuffer.add(buffer[i] & 0xFF);
						}
					}
					mHandler.sendEmptyMessage(MSG_NEW_DATA);
				} catch (IOException e) {
					e.printStackTrace();
					break;
				}
			}
		}

		/**
		 * Write to the connected OutStream.
		 * 
		 * @param buffer
		 *            The bytes to write
		 */
		public void write(byte[] buffer) {
			if(!mmSocket.isConnected()){ //写的时候发现出错，说明监测器断连，用异步状态手柄修正状态
				conStatHandler.sendEmptyMessage(CONSTAT_FAIL);
				return;
			}
			try {
				mmOutStream.write(buffer);
			} catch (IOException e) {
				//写的时候发现出错，说明监测器断连，用异步状态手柄修正状态
				conStatHandler.sendEmptyMessage(CONSTAT_FAIL);
				e.printStackTrace();
			}
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @param a 256进制双字节高位
	 * @param b 256进制双字节低位
	 * @return 十进制结果
	 */
	public Integer combiner(int a, int b){
		return a*256+b;
	}
	
	/**
	 * @category 给不足2位的时间补0
	 * @return 补0后的两位数时间字符串
	 */
	public static String frontCompWithZore(int sourceDate,int formatLength)  
	{
	  String newString = String.format("%0"+formatLength+"d", sourceDate);  
	  return newString;  
	}  
	
	/**
	 * @param command 一字节传入数据
	 * @param dir 校验起始位的编号
	 * @return 是否符合协议标准 2：还在检查中 1：不符合校验出错 0：校验成功请处理
	 */
	int checkCom(int command, int dir){
		int sum=0,checksum=0;
		int startbit = 0;
		if(dir==0) //指令 0xBB
			startbit = 0xBB;
		else if(dir==1) //数据 0xAA
			startbit = 0xAA;
		else if(dir==2) //ACK 0xCC
			startbit = 0xCC;
		if(command==startbit) //起始位判断
		{
				comi=0;
				bufBT[comi]=command;
		}else{
				comi++;
				bufBT[comi]=command;
				if(comi==12){
					sum=bufBT[1]+bufBT[2]+bufBT[3]+bufBT[4]+bufBT[5]+bufBT[6]+bufBT[7]+bufBT[8]+bufBT[9];
					checksum=combiner(bufBT[10],bufBT[11]);
					if(sum == checksum && bufBT[12]==0xFF){
						comi=0;
						return 0; //指令/数据 校验正确
					}else{
						comi=0;
						return 1; //指令/数据 校验错误
					}
				}
		}
		return 2; //指令/数据 正在校验中，不要急
	}
	
	/**
	 * @category 向socket中写入流数据
	 * @param input 将输入的一串字符串解析成16进制发送到输出流，字符串在这里应符合自定义协议
	 */
	private void btSend(String input){
		if (input != null && !"".equals(input)) {
			String[] data = input.split(" ");
			byte[] tmp = new byte[data.length];
			for (int i = 0; i < data.length; i++) {
				tmp[i] = (byte) Integer.parseInt(data[i], 16);
			}
			
			//判断数据传输线程是否开启，设备蓝牙是否开启，再进行下一步操作
			//否则进行警告，提示用户连接好再操作
			if(mConnectedThread!=null && conStat==CONSTAT_OK)
				mConnectedThread.write(tmp);
			else
				conStatHandler.sendEmptyMessage(CONSTAT_FAIL);
		}
	}
	
	/**
	 * @category 向发送缓冲字符串内写入当前系统时间的协议格式数据
	 */
	private void parseSystemTime(){
		Calendar c = Calendar.getInstance();
		String year = Integer.toString(Integer.valueOf(new SimpleDateFormat("yy").format(Calendar.getInstance().getTime())),16);
		String month = Integer.toString(c.get(Calendar.MONTH)+1, 16);
		String day = Integer.toString(c.get(Calendar.DAY_OF_MONTH), 16);
		String hour = Integer.toString(c.get(Calendar.HOUR_OF_DAY), 16);
		String min = Integer.toString(c.get(Calendar.MINUTE), 16);
		String sec = Integer.toString(c.get(Calendar.SECOND), 16);
		int sum = c.get(Calendar.MONTH)+1+c.get(Calendar.DAY_OF_MONTH)+c.get(Calendar.HOUR_OF_DAY)+c.get(Calendar.MINUTE)+c.get(Calendar.SECOND)+Integer.valueOf(new SimpleDateFormat("yy").format(Calendar.getInstance().getTime()));
		String checkh = Integer.toHexString(sum/256);
		String checkl = Integer.toHexString(sum%256);
		strDataBuf = "AA 00 00 00 " + year + " "+ month + " " + day + " " + hour + " " + min + " " + sec + " " + checkh + " " + checkl + " FF";
	}
	
	/**
	 * @category 向发送缓冲字符串内写入门限值的协议格式数据
	 */
	private void parseThre(){
		int foggy = Integer.valueOf(ed_th.getEditableText().toString());
		String foggyH = Integer.toHexString(foggy/256);
		String foggyL = Integer.toHexString(foggy%256);
		int danger = Integer.valueOf(ed_th_danger.getEditableText().toString());
		String dangerH = Integer.toHexString(danger/256);
		String dangerL = Integer.toHexString(danger%256);
		int threchecksum = danger/256 + danger%256 + foggy/256 + foggy%256 + 1275;
		String threchecksumH = Integer.toHexString(threchecksum/256);
		String threchecksumL = Integer.toHexString(threchecksum%256);
		strDataBuf = "AA FF FF FF FF FF " + foggyH + " " + foggyL + " " + dangerH + " " + dangerL + " " + threchecksumH + " " + threchecksumL + " FF";
	}
	
	/**
	 * @category 处理回传数据，由Handler里回传成功后的代码调用，主要是处理数据库保存部分以及反转List
	 */
	private void processRevData(){
		if(pd.isShowing())
			pd.cancel();
		pd.setMessage("正在保存回传的数据\n时间较长，请耐心等候");
		pd.setCancelable(false);
		pd.show();
		
		Collections.reverse(mAQIData);
		Collections.reverse(mTimeData);
		Collections.reverse(mTempData);
		
		String tmpTimeArr[] = null;
		String tmpTime = null;
		Date tmpTime1 = null, tmpTime2 = null, newTime = null;
		Integer tmpAQI = null;
		Integer tmpTemp = null;
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		for(int i=0; i<mAQIData.size(); i++){
			//TODO 处理一下数据保存
			boolean isSuccessParse = false;
			tmpTimeArr = mTimeData.get(i).split(" ");
			tmpTime = tmpTimeArr[0]+" 00:00:00";
			try {
				tmpTime1 = df.parse(mTimeData.get(i));
				tmpTime2 = df.parse(tmpTime);
				isSuccessParse = true;
			} catch (ParseException e) {
				e.printStackTrace();
				isSuccessParse = false;
			}
			if(isSuccessParse){
				try {
					AQIData queryData = simpleDao.queryForId(tmpTime1);
					if(queryData==null){
						AQIData newdata = new AQIData(tmpTime1, tmpTime2, mAQIData.get(i), mTempData.get(i),false);
						simpleDao.create(newdata);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		
		tv_device.setText("设备连接正常 数据保存完成");
		pd.cancel();
		mDisplayCata = CATA_AQI; //设置AQI显示模式
		img_modesel.setImageDrawable(MainActivity.this.getResources().getDrawable(R.drawable.icon_aqi));
		Date date = Calendar.getInstance().getTime();
		SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
		String curDate = df2.format(date) + " 00:00:00";
		tv_datepicker.setText(df2.format(date));
		showDataOnChart(curDate, CATA_AQI);
	}
	
	/**
	 * @category 处理接收消息字节的函数，根据发出的指令代码进行分类处理
	 */
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case MSG_NEW_DATA:
				if (isPause) {
					break;
				} else {
					synchronized (mBuffer) {
							for (int i : mBuffer) {
								//处理输入数据
								int re = -1;
								switch(comInd){
								case 1:
									if(isFirstExe){
										re = checkCom(i, 2);
										if(re==0){
											if(bufBT[1]==0xFF){
												tv_device.setText("进入状态 - 数据回传");
												pd.setMessage("正在回传数据\n请不要退出或切出应用");
												pd.setCancelable(false);
												pd.show();
												isFirstExe = false;
											}else{
												tv_device.setText("监测仪未响应回传指令，请重发");
												comInd = 0; isFirstExe = true;
											}
										}else if (re==1) {
											tv_device.setText("连接不稳定，确认稳定连接后，请重发回传指令");
											comInd = 0; isFirstExe = true;
										}
									}else{
										re = checkCom(i, 1);
										if(re==0){
											if(bufBT[10]==0x08 && bufBT[11]==0xF7){
												pd.cancel();
												tv_device.setText("设备连接正常 数据回传成功");
												// TODO 这里需要对数据进行处理！
												processRevData();
												comInd = 0; isFirstExe = true;
											}else{
												mAQIData.add(combiner(bufBT[1], bufBT[2]));
												mTempData.add(bufBT[3]);
												mTimeData.add("20"+frontCompWithZore(bufBT[4],2)+"-"+frontCompWithZore(bufBT[5],2)+"-"+frontCompWithZore(bufBT[6],2)+" "+frontCompWithZore(bufBT[7],2)+":"+frontCompWithZore(bufBT[8],2)+":"+frontCompWithZore(bufBT[9],2));
											}
										}else if(re==1){
											// 节点数据损坏，不作任何处理
										}
									}
									break;
								case 2:
									re = checkCom(i, 2);
									if(re==0){
										if(bufBT[1]==0xFF){
											tv_device.setText("设备连接正常 退出透传成功");
											comInd = 0;
										}else{
											tv_device.setText("监测仪未响应退出指令，请重发");
											comInd = 0; 
										}
									}else if(re==1){
										tv_device.setText("连接不稳定，确认稳定连接后，请重发退出指令");
										comInd = 0;
									}
									break;
								case 3:
									re = checkCom(i, 2);
									if(re==0){
										if(bufBT[1]==0xFF){
											tv_device.setText("设备连接正常 成功重置系统并退出透传");
											comInd = 0;
										}else{
											tv_device.setText("监测仪未响应重置指令，请重发");
											comInd = 0; 
										}
									}else if(re==1){
										tv_device.setText("连接不稳定，确认稳定连接后，请重发重置指令");
										comInd = 0;
									}
									break;
								case 4:
									if(isFirstExe){
										re = checkCom(i, 2);
										if(re==0){
											if(bufBT[1]==0xFF){
												pd.setMessage("正在设置门限\n请不要退出或切出应用");
												pd.setCancelable(false);
												pd.show();
												tv_device.setText("进入状态 - 设置设备报警门限");
												isFirstExe = false;
												try {
													Thread.sleep(1000);
												} catch (InterruptedException e) {
													e.printStackTrace();
												}
												parseThre();
												btSend(strDataBuf);
											}else{
												tv_device.setText("监测仪未响应门限设置指令，请重发");
												comInd = 0; isFirstExe = true;
											}
										}else if (re==1) {
											tv_device.setText("连接不稳定，确认稳定连接后，请重发门限设置指令");
											comInd = 0; isFirstExe = true;
										}
									}else{
										re = checkCom(i, 2);
										if(re==0){
											if(bufBT[1]==0xFF){
												pd.cancel();
												tv_device.setText("报警门限设置成功 下次开机生效");
												comInd = 0; isFirstExe = true;
											}else{
												pd.cancel();
												tv_device.setText("设备连接正常 报警门限设置失败");
												comInd = 0; isFirstExe = true;
											}
										}else if(re==1){
											pd.cancel();
											tv_device.setText("连接不稳定，操作结果未知，请重发门限设置指令");
											comInd = 0; isFirstExe = true;
										}
									}
									break;
								case 5:
									if(isFirstExe){
										re = checkCom(i, 2);
										if(re==0){
											if(bufBT[1]==0xFF){
												pd.setMessage("正在同步时间\n请不要退出或切出应用");
												pd.setCancelable(false);
												pd.show();
												tv_device.setText("进入状态 - 同步监测器时间");
												isFirstExe = false;
												try {
													Thread.sleep(1000);
												} catch (InterruptedException e) {
													e.printStackTrace();
												}
												parseSystemTime();
												btSend(strDataBuf);
											}else{
												tv_device.setText("监测仪未响应同步时间指令，请重发");
												comInd = 0; isFirstExe = true;
											}
										}else if (re==1) {
											tv_device.setText("连接不稳定，确认稳定连接后，请重发同步时间指令");
											comInd = 0; isFirstExe = true;
										}
									}else{
										re = checkCom(i, 2);
										if(re==0){
											if(bufBT[1]==0xFF){
												pd.cancel();
												tv_device.setText("设备连接正常 同步时间成功");
												comInd = 0; isFirstExe = true;
											}else{
												pd.cancel();
												tv_device.setText("设备连接正常 同步时间失败");
												comInd = 0; isFirstExe = true;
											}
										}else if(re==1){
											pd.cancel();
											tv_device.setText("连接不稳定，操作结果未知，请重发同步时间指令");
											comInd = 0; isFirstExe = true;
										}
									}
									break;
								}
							}
							mBuffer.clear();
					}
				}
				break;
			default:
				break;
			}
		}
	};

	/**
	 * @category 实现的OnClickListener接口
	 */
	@Override
	public void onClick(View v) {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,AlertDialog.THEME_HOLO_LIGHT);
		switch(v.getId()){
		case R.id.btn_update:
			if (mConnectThread != null) {
				mConnectThread.cancel();
				mConnectThread = null;
			}
			Intent serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			break;
		case R.id.btn_save:
			if(ll2.isShown()){
				ll2.setVisibility(View.GONE);
				btn_save.setText("功能 ↑");
			}else{
				ll2.setVisibility(View.VISIBLE);
				btn_save.setText("功能 ↓");
			}
			break;
		case R.id.btn_exit:
			activateQuit();
			break;
		case R.id.btn_usr_bak:
			if(ed_usr.getEditableText().toString().equals("")||ed_pwd.getEditableText().toString().equals("")){
				Toast.makeText(MainActivity.this, "账号密码请填写完整", Toast.LENGTH_SHORT).show();
				return;
			}
			RequestParams params = new RequestParams();
			params.add("username", ed_usr.getEditableText().toString());
			params.add("password", ed_pwd.getEditableText().toString());
			PMHttpClient.post(MainActivity.this, PMHttpClient.URL_LOGIN, params , new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
					String result = new String(arg2);
					if(result.trim().equals("0")){ //成功注册
						Toast.makeText(MainActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
						editor.putString("username", ed_usr.getEditableText().toString());
						editor.putString("password", ed_pwd.getEditableText().toString());
						editor.commit();
						if (databaseHelper != null) {
							OpenHelperManager.releaseHelper();
							databaseHelper = null;
						}
						Intent intent = new Intent();
						intent.putExtra("username", ed_usr.getEditableText().toString());
						intent.setClass(MainActivity.this, LabActivity.class);
						MainActivity.this.startActivityForResult(intent, REQUEST_OPEN_LAB);
					}else if(result.trim().equals("1")){
						Toast.makeText(MainActivity.this, "用户名或密码错误，请重试", Toast.LENGTH_SHORT).show();
					}
				}
				@Override
				public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
					Toast.makeText(MainActivity.this, "网络连接失败，请重试", Toast.LENGTH_SHORT).show();
				}
			});
			break;
		case R.id.btn_usr_rec:
			if(ed_usr.getEditableText().toString().equals("")||ed_pwd.getEditableText().toString().equals("")){
				Toast.makeText(MainActivity.this, "账号密码请填写完整", Toast.LENGTH_SHORT).show();
				return;
			}
			if(ed_usr.getEditableText().length()<6||ed_pwd.getEditableText().length()<6){
				Toast.makeText(MainActivity.this, "账号密码均不可以小于6位", Toast.LENGTH_SHORT).show();
				return;
			}
			RequestParams params2 = new RequestParams();
			params2.add("username", ed_usr.getEditableText().toString());
			params2.add("password", ed_pwd.getEditableText().toString());
			PMHttpClient.post(MainActivity.this, PMHttpClient.URL_REGISTER, params2 , new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
					String result = new String(arg2);
					if(result.trim().equals("0")){ //成功注册
						Toast.makeText(MainActivity.this, "注册成功，快去登录吧", Toast.LENGTH_SHORT).show();
					}else if(result.trim().equals("1")){
						Toast.makeText(MainActivity.this, "注册失败，请重试", Toast.LENGTH_SHORT).show();
					}else if(result.trim().equals("2")){
						Toast.makeText(MainActivity.this, "用户名已注册，换一个吧", Toast.LENGTH_SHORT).show();
						ed_usr.setText("");
					}
				}
				@Override
				public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
					Toast.makeText(MainActivity.this, "网络连接失败，请重试", Toast.LENGTH_SHORT).show();
				}
			});
			break;
		case R.id.btn_shutdown:
			builder.setMessage("您确定要执行以下操作吗？确认后操作将不可回退。\n监测仪退出蓝牙传输模式");
			builder.setPositiveButton("确认执行", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					comInd = 2; isFirstExe = true;
					btSend(comBuf2);
				}
			});
			builder.setNegativeButton("取消", null);
			builder.show();
			break;
		case R.id.btn_reset:
			builder.setMessage("您确定要执行以下操作吗？确认后操作将不可回退。\n监测仪系统重置");
			builder.setPositiveButton("确认执行", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					comInd = 3; isFirstExe = true;
					btSend(comBuf3);
				}
			});
			builder.setNegativeButton("取消", null);
			builder.show();
			break;
		case R.id.btn_synctime:
			comInd = 5; isFirstExe = true;
			btSend(comBuf5);
			break;
		case R.id.img_modesel:
			if(mDisplayCata == CATA_AQI){
				mDisplayCata = CATA_TEMP;
				img_modesel.setImageDrawable(MainActivity.this.getResources().getDrawable(R.drawable.icon_temp));
				showDataOnChart(tv_datepicker.getText().toString()+" 00:00:00", CATA_TEMP);
			}else{
				mDisplayCata = CATA_AQI;
				img_modesel.setImageDrawable(MainActivity.this.getResources().getDrawable(R.drawable.icon_aqi));
				showDataOnChart(tv_datepicker.getText().toString()+" 00:00:00", CATA_AQI);
			}
			break;
		case R.id.btn_transback:
			builder.setMessage("您确定要执行以下操作吗？确认后操作将不可回退。\n回传监测仪记录数据，操作可能耗时较长，您需要稍作等候！");
			builder.setPositiveButton("确认执行", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					comInd = 1; isFirstExe = true;
					btSend(comBuf1);
				}
			});
			builder.setNegativeButton("取消", null);
			builder.show();
			break;
		case R.id.btn_transthre:
			if(Integer.valueOf(ed_th_danger.getEditableText().toString())>Integer.valueOf(ed_th.getEditableText().toString())){
				builder.setMessage("您确定要执行以下操作吗？确认后操作将不可回退。\n修改监测仪报警门限");
				builder.setPositiveButton("确认执行", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						comInd = 4; isFirstExe = true;
						btSend(comBuf4);
					}
				});
				builder.setNegativeButton("取消", null);
				builder.show();
			}else{
				Toast.makeText(getBaseContext(), "人体危害线应比雾霾线高", Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.tv_datepicker:
			SimpleCalendarDialogFragment dialog = new SimpleCalendarDialogFragment(tv_datepicker);
			dialog.setListener(MainActivity.this);
			dialog.setChecked(mDisplayCata);
			dialog.show(getSupportFragmentManager(), "calendardialog");
			break;
		}
	}
	
	/**
	 * @category 重载的按键响应函数，负责处理按下返回键的时候的操作
	 */
	@Override 
	public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if(keyCode == KeyEvent.KEYCODE_BACK){
    		activateQuit();
    		return true; 
    	}else{        
    		return super.onKeyDown(keyCode, event); 
    	} 
	}

	@Override
	public void onCalendarSelected(int which, String dateStr) {
		mDisplayCata = which;
		if(mDisplayCata == CATA_AQI){
			img_modesel.setImageDrawable(MainActivity.this.getResources().getDrawable(R.drawable.icon_aqi));
		}else{
			img_modesel.setImageDrawable(MainActivity.this.getResources().getDrawable(R.drawable.icon_temp));
		}
		showDataOnChart(dateStr, which);
	}

}
