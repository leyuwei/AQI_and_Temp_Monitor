package com.leyuwei.pmtestbt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BluetoothConnectActivityReceiver extends BroadcastReceiver
{

	String strPsw = "021101";

	@Override
	public void onReceive(Context context, Intent intent)
	{
		// TODO Auto-generated method stub
		if (intent.getAction().equals("android.bluetooth.device.action.PAIRING_REQUEST")){
			BluetoothDevice btDevice = intent
					.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			try
			{
				//1.确认配对  
                ClsUtils.setPairingConfirmation(btDevice.getClass(), btDevice, true);  
                //2.终止有序广播  
                abortBroadcast();//如果没有将广播终止，则会出现一个一闪而过的配对框。  
                //3.调用setPin方法进行配对...  
                boolean ret = ClsUtils.setPin(btDevice.getClass(), btDevice, strPsw);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}


	}
}
