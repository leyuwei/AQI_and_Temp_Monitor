package com.leyuwei.pmtestbt;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "AQIData")
public class AQIData {
	public static final String FIELDNAME_DATE = "date";
	public static final String FIELDNAME_VAGUE_DATE = "vaguedate";
	public static final String FIELDNAME_AQI = "aqi";
	public static final String FIELDNAME_TEMP = "temp";
	public static final String FIELDNAME_ISUPLOADED = "isuploaded";
	
	@DatabaseField(dataType = DataType.DATE, id = true, canBeNull = false, unique = true, useGetSet = true, columnName = FIELDNAME_DATE)
	Date date;
	
	@DatabaseField(dataType = DataType.DATE, canBeNull = false, index = true, useGetSet = true, columnName = FIELDNAME_VAGUE_DATE)
	Date vagueDate;
	
	@DatabaseField(dataType = DataType.INTEGER, canBeNull = false, useGetSet = true, columnName = FIELDNAME_AQI)
	int aqi;
	
	@DatabaseField(dataType = DataType.INTEGER, canBeNull = false, useGetSet = true, columnName = FIELDNAME_TEMP)
	int temp;
	
	@DatabaseField(dataType = DataType.BOOLEAN, canBeNull = false, useGetSet = true, defaultValue = "false", columnName = FIELDNAME_ISUPLOADED)
	boolean isUploaded;
	
	public Date getDate(){
		return date;
	}
	
	public void setDate(Date date){
		this.date = date;
	}
	
	public Date getVagueDate(){
		return vagueDate;
	}
	
	public void setVagueDate(Date vagueDate){
		this.vagueDate = vagueDate;
	}
	
	public int getAqi(){
		return aqi;
	}
	
	public void setAqi(int aqi){
		this.aqi = aqi;
	}
	
	public int getTemp(){
		return temp;
	}
	
	public void setTemp(int temp){
		this.temp = temp;
	}
	
	public boolean getIsUploaded(){
		return isUploaded;
	}
	
	public void setIsUploaded(boolean isUploaded){
		this.isUploaded = isUploaded;
	}

	AQIData() {
		// needed by ormlite
	}

	public AQIData(Date date, Date vagueDate, int aqi, int temp, boolean isUploaded) {
		this.date = date;
		this.vagueDate = vagueDate;
		this.aqi = aqi;
		this.temp = temp;
		this.isUploaded = isUploaded;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		sb.append("exact_date=").append(dateFormatter.format(date));
		sb.append(", ").append("vague_date=").append(dateFormatter.format(vagueDate));
		sb.append(", ").append("aqi=").append(""+aqi);
		sb.append(", ").append("temp=").append(""+temp);
		sb.append(", ").append("temp=").append(""+isUploaded);
		return sb.toString();
	}
	
	public JSONObject toJSON(String username, String usercity, double latitude, double lontitude, String vaguedate){
		JSONObject jsonObj = new JSONObject();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			jsonObj.put("aq", this.aqi);
			jsonObj.put("tp", this.temp);
			jsonObj.put("na", username);
			jsonObj.put("ci", usercity);
			jsonObj.put("lt", latitude);
			jsonObj.put("ln", lontitude);
			jsonObj.put("vd", vaguedate);
			jsonObj.put("ed", df.format(this.date));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObj;
	}
}
