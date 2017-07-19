package com.leyuwei.pmtestbt;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.R.integer;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

public class SimpleCalendarDialogFragment extends DialogFragment implements OnDateSelectedListener {

	private int cata = 0;
	private boolean isLab = false;
	private TextView textView;
	private RadioButton rbAQI,rbTEMP;
	private RadioGroup rg;
	private onCalendarSelectedListener listener = null;
	
	public void setListener(onCalendarSelectedListener listener){
		this.listener = listener;
	}
	
	public void setIsLab(boolean isLab){
		this.isLab = isLab;
	}
	
	/**
	 * 构造函数，使用需要先实例化！
	 */
	public SimpleCalendarDialogFragment(TextView textView) {
		this.textView = textView;
	}
	
	public void setChecked(int which){
		this.cata = which;
	}
	
	/**
	 * 初始化函数
	 * @param savedInstanceState
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setCancelable(true);
		setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog);
		
	}
	
    /**
     * 绑定视图，返回view对象
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_calendar, container, false);
    	rbAQI = (RadioButton) view.findViewById(R.id.radio0);
    	rbTEMP = (RadioButton) view.findViewById(R.id.radio1);
    	rg = (RadioGroup) view.findViewById(R.id.radioGroup1);
    	if(cata==0){
    		rbAQI.setChecked(true);rbTEMP.setChecked(false);
    	}else{
    		rbAQI.setChecked(false);rbTEMP.setChecked(true);
    	}
    	if(isLab)
    		rg.setVisibility(View.GONE);
    	return view;
    }

    /**
     * 初始化view里面的日历控件，及控件方法绑定
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
    	super.onViewCreated(view, savedInstanceState);
        MaterialCalendarView widget = (MaterialCalendarView) view.findViewById(R.id.calendarView);
        widget.setOnDateChangedListener(this);
        widget.setCurrentDate(CalendarDay.from(Calendar.getInstance().getTime()), true);
        widget.setDateSelected(CalendarDay.from(Calendar.getInstance().getTime()), true);
    }

    /**
     * 重写监听方法
     * @param widget the view associated with this listener
     * @param date   the new date. May be null if selection was cleared
     */
	@SuppressLint("SimpleDateFormat")
	@Override
	public void onDateSelected(MaterialCalendarView widget, CalendarDay date,
			boolean selected) {
		String which = null;
		int mode = 0;
		if(rbAQI.isChecked()){
			which = "AQI"; mode = 0;
		}else if(rbTEMP.isChecked()){
			which = "TEMP"; mode = 1;
		}
		SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd");
		if(isLab)
			textView.setText(formatter.format(date.getDate()));
		else
			textView.setText(formatter.format(date.getDate()));
		if(listener!=null)
			listener.onCalendarSelected(mode, formatter.format(date.getDate())+" 00:00:00");
		this.dismiss();
	}
}