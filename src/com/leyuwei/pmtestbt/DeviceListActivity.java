package com.leyuwei.pmtestbt;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.http.Header;

import com.baidu.mapapi.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DeviceListActivity extends Activity {  
    // Debugging  
    private static final String TAG = "DeviceListActivity";  
    private static final boolean D = true;  
    private List<String> deviceMAC = new ArrayList<String>();
  
    // Return Intent extra  
    public static String EXTRA_DEVICE_ADDRESS = "device_address";  
  
    // Member fields  
    private SharedPreferences sp;
	private Editor editor;
    private BluetoothAdapter mBtAdapter;  
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;  
    private ArrayAdapter<String> mNewDevicesArrayAdapter;  
    private List<BluetoothDevice> mDeviceList;
    private boolean isExistsPairedDevices;
  
    @Override  
    protected void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);  
        setContentView(R.layout.device_list);  
        setTitle("空指监测器匹配");
        ActionBar actionBar=getActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        
        //init data keeper
      	sp = this.getSharedPreferences("pmtester", Activity.MODE_PRIVATE);
      	editor = sp.edit();
      	String macArr[] = (sp.getString("macList", "")).toString().split("-");
      	for(int i=0; i<macArr.length; i++)
      		deviceMAC.add(macArr[i]);
        
        // Set result CANCELED incase the user backs out  
        setResult(Activity.RESULT_CANCELED);  
        mDeviceList = new ArrayList<BluetoothDevice>();
        
        // Initialize the button
        Button scanButton = (Button) findViewById(R.id.button_scan);  
        scanButton.setOnClickListener(new OnClickListener() {  
            public void onClick(View v) {  
            	mDeviceList.clear();
                doDiscovery();  
                v.setVisibility(View.GONE);  
            }  
        });  
        Button updateButton = (Button) findViewById(R.id.button_updatemac);  
        updateButton.setOnClickListener(new OnClickListener() {  
            public void onClick(View v) {  
            	PMHttpClient.post(DeviceListActivity.this, PMHttpClient.URL_MACADDR, null, new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
						deviceMAC.clear();
						String result = new String(arg2);
						editor.putString("macList", result);
						editor.commit();
						String macArr[] = result.split("-");
						for(int i = 0; i<macArr.length; i++){
							deviceMAC.add(macArr[i]);
						}
						Toast.makeText(DeviceListActivity.this, "支持设备列表更新成功", Toast.LENGTH_SHORT).show();
					}
					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
						Toast.makeText(DeviceListActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
					}
				});
            }  
        });
  
        // Initialize array adapters. One for already paired devices and  
        // one for newly discovered devices  
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);  
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name); 
  
        // Find and set up the ListView for paired devices  
        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);  
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);  
        pairedListView.setOnItemClickListener(mDeviceClickListener);  
  
        // Find and set up the ListView for newly discovered devices  
        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);  
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);  
        newDevicesListView.setOnItemClickListener(mNewDeviceClickListener);  
  
        // Register for broadcasts when a device is discovered  
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);  
        this.registerReceiver(mReceiver, filter);  
  
        // Register for broadcasts when discovery has finished  
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);  
        this.registerReceiver(mReceiver, filter);  
  
        // Get the local Bluetooth adapter  
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();  
  
        // Get a set of currently paired devices  
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();  
  
        // If there are paired devices, add each one to the ArrayAdapter  
        if (pairedDevices.size() > 0) {  
        	isExistsPairedDevices = true;
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);  
            for (BluetoothDevice device : pairedDevices) {  
            	if(deviceMAC.contains(device.getAddress().toUpperCase())){
            		mPairedDevicesArrayAdapter.add("PM_Mon\n" + device.getAddress());  
            	}else{
            		mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());  
            	}
            }
        } else {  
        	isExistsPairedDevices = false;
            String noDevices = "没有发现匹配设备\n请点击设备搜索进行蓝牙匹配";
            mPairedDevicesArrayAdapter.add(noDevices);  
        }  
    }  
  
    @Override  
    protected void onDestroy() {  
        super.onDestroy();
        // Make sure we're not doing discovery anymore  
        if (mBtAdapter != null) {  
            mBtAdapter.cancelDiscovery();  
        }
        // Unregister broadcast listeners  
        this.unregisterReceiver(mReceiver);  
    }  
  
    /** 
     * Start device discover with the BluetoothAdapter 
     */  
    private void doDiscovery() {
        // Indicate scanning in the title  
        setProgressBarIndeterminateVisibility(true);  
        setTitle("正在搜索");  
  
        // Turn on sub-title for new devices  
        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);  
  
        // If we're already discovering, stop it  
        if (mBtAdapter.isDiscovering()) {  
            mBtAdapter.cancelDiscovery();  
        }  
  
        // Request discover from BluetoothAdapter  
        mBtAdapter.startDiscovery();  
    }  
  
    // The on-click listener for all devices in the ListViews of paired devices
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {  
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {  
            // Cancel discovery because it's costly and we're about to connect  
            mBtAdapter.cancelDiscovery();
            // Get the device MAC address, which is the last 17 chars in the View  
            String info = ((TextView) v).getText().toString();  
            String address = info.substring(info.length() - 17);
            if(!deviceMAC.contains(address.toUpperCase())){
            	Toast.makeText(DeviceListActivity.this, "这个设备不是PM检测仪\n请检查后重连", Toast.LENGTH_SHORT).show();
            	return;
            }
            // Create the result Intent and include the MAC address  
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address); 
            // Set result and finish this Activity  
            setResult(Activity.RESULT_OK, intent);  
            finish();  
        }  
    };  
    
    // The on-click listener for all devices in the ListViews of new devices 
    private OnItemClickListener mNewDeviceClickListener = new OnItemClickListener() {  
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {  
            // Cancel discovery because it's costly and we're about to connect  
            mBtAdapter.cancelDiscovery();
            // Get the device MAC address, which is the last 17 chars in the View  
            String info = ((TextView) v).getText().toString();  
            String address = info.substring(info.length() - 17);
            if(!deviceMAC.contains(address.toUpperCase())){
            	Toast.makeText(DeviceListActivity.this, "这个设备不是PM检测仪\n请检查后重选", Toast.LENGTH_SHORT).show();
            	return;
            }else{
            	//try connecting with pin automatically
            	//Toast.makeText(DeviceListActivity.this, ""+arg2, Toast.LENGTH_LONG).show();
            	BluetoothDevice device = mDeviceList.get(arg2);
        		try {
        			//ClsUtils.createBond(device.getClass(), device);
    				ClsUtils.setPin(device.getClass(), device, "021101"); // 手机和蓝牙采集器配对
    				ClsUtils.createBond(device.getClass(), device);
        		} catch (Exception e) {
        			Toast.makeText(DeviceListActivity.this, "设备连接不成功，请重试", Toast.LENGTH_SHORT).show();
        			return;
        		} 
        		if(!isExistsPairedDevices){
        			mPairedDevicesArrayAdapter.clear();
        		}
        		mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
        		Toast.makeText(DeviceListActivity.this, "设备连接完成，请在上方列表确认连接", Toast.LENGTH_SHORT).show();
            }
        }  
    };  
    
  
    // The BroadcastReceiver that listens for discovered devices and  
    // changes the title when discovery is finished  
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {  
        @Override  
        public void onReceive(Context context, Intent intent) {  
            String action = intent.getAction();  
  
            // When discovery finds a device  
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {  
                // Get the BluetoothDevice object from the Intent  
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);  
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    mDeviceList.add(device);
                }  
            // When discovery is finished, change the Activity title  
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {  
                setProgressBarIndeterminateVisibility(false);  
                setTitle("PM监测器匹配");  
                if (mNewDevicesArrayAdapter.getCount() == 0) {  
                    String noDevices = "没有发现设备\n请检查是否开启电源并重开该界面";
                    mNewDevicesArrayAdapter.add(noDevices);
                }
            }  
        }  
    };  
  
}  