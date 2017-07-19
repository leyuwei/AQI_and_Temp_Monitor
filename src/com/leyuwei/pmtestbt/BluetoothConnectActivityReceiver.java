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
				//1.ȷ�����  
                ClsUtils.setPairingConfirmation(btDevice.getClass(), btDevice, true);  
                //2.��ֹ����㲥  
                abortBroadcast();//���û�н��㲥��ֹ��������һ��һ����������Կ�  
                //3.����setPin�����������...  
                boolean ret = ClsUtils.setPin(btDevice.getClass(), btDevice, strPsw);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}


	}
}
