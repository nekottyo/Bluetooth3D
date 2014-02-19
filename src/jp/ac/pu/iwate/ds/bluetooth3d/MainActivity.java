package jp.ac.pu.iwate.ds.bluetooth3d;

import android.os.Bundle;
import android.app.Activity;
import android.app.DownloadManager.Request;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends Activity {
	private final static int REQUEST_ENABLE_BT = 1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		BluetoothAdapter Bt = BluetoothAdapter.getDefaultAdapter();
		if(!Bt.equals(null)) {
			Log.d("info" ,"Bluetoothがサポート");
		} else {
			Log.d("info", "Bluetoothがないよ");
		}
		
		boolean btEnable = Bt.isEnabled();
		if (btEnable) {
			
		} else {
			Intent btOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(btOn, REQUEST_ENABLE_BT);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	protected void onActivityResult(int requestCode, int ResultCode, Intent date) {
		
	}

}
