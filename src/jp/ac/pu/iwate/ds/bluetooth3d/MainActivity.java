package jp.ac.pu.iwate.ds.bluetooth3d;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.BaseAdapter;
import android.graphics.Color;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothAdapter;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;


public class MainActivity extends Activity{
	private static final int REQUEST_ENABLE_BLUETOOTH = 1;

	private BluetoothAdapter adapter;
	private BroadcastReceiver receiver;
	//private ListView listView;
	//	private DeviceList deviceList;
	private TextView[] textviews;
	private CheckBox[] checkBoxs; 
	private List<DeviceInfo> deviceList;

	int[] textviewids ={
			R.id.textView1,
			R.id.textView2,
			R.id.textView3,
			R.id.textView4,
			R.id.textView5
	};
	int[] checkBoxids = {
			R.id.checkBox1,
			R.id.checkBox2,
			R.id.checkBox3,
			R.id.checkBox4,
			R.id.checkBox5
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.buttonlayout);

		textviews = new TextView[textviewids.length];
		for (int i = 0; i < textviewids.length; i++) {
			textviews[i]= (TextView)findViewById(textviewids[i]);
			textviews[i].setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
		}
		checkBoxs = new CheckBox[checkBoxids.length];
		for (int i = 0; i < checkBoxids.length; i++) {
			checkBoxs[i] = (CheckBox)findViewById(checkBoxids[i]); 
		}

		deviceList = new ArrayList<DeviceInfo>();
		
		Button button = (Button)findViewById(R.id.button1);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO 自動生成されたメソッド・スタブ
				List<Integer> list = new ArrayList<Integer>();
				
				for (int i = 0; i < checkBoxids.length; i++) {
					if(checkBoxs[i].isChecked()){
						list.add(deviceList.get(i).getRssi());
						Log.d("data", ""+deviceList.get(i).getRssi());
					}
					
				}
				
			}
		});
		
		//listView = new ListView(this);
//		deviceList = new DeviceList(this);

//		listView.setAdapter(deviceList);
//		listView.setBackgroundColor(Color.WHITE);

		if(createAdapter()){
			//デバイス探索用のBroadcastReceiverの準備
			setReceiver();

			//レシーバで受け取るメッセージの設定
			IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
			registerReceiver(receiver, filter);

			filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
			registerReceiver(receiver, filter);

			bluetoothEnable();
		}

//		setContentView(listView);
	}

	//アダプタの作成。成功したらtrueを、失敗したらfalseを返す
	private boolean  createAdapter(){
		adapter = BluetoothAdapter.getDefaultAdapter();

		if(adapter == null){
			return false;
		}

		return true;
	}

	private void bluetoothEnable(){
		if(adapter.isEnabled()){
			//Bluetoothが利用可能な状態ならデバイスの探索開始
			doDiscovery();
		}else{
			//Bluetoothの利用許可を求めるダイアログ表示。結果はonActibityResultで受け取る
			Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(intent, MainActivity.REQUEST_ENABLE_BLUETOOTH);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if(requestCode ==MainActivity.REQUEST_ENABLE_BLUETOOTH){
			if(resultCode == Activity.RESULT_OK){
				//Bluetoothの仕様許可が来たらデバイスの探索開始
				doDiscovery();
			}else{
				error("Bluetoothを利用出来ません");
			}
		}
	}

	//デバイスを探索を始める。結果はBroadcastReceiverで受け取る
	private void doDiscovery(){
		if(adapter.isDiscovering()){
			adapter.cancelDiscovery();
		}

		adapter.startDiscovery();
	}

	//
	private void setReceiver(){
		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();

				//デバイスを見つけたらアダプタに名前とアドレスを追加
				if(BluetoothDevice.ACTION_FOUND.equals(action)) {
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);

					if (device.getBondState() != BluetoothDevice.BOND_BONDED) {

//						deviceList.addDeviceInfo(device.getName(), device.getAddress(), rssi);
//						listView.invalidateViews();
						deviceList.add(new DeviceInfo(device.getName(), device.getAddress(), rssi));
						int i=0;
						for (Iterator<DeviceInfo> iterator = deviceList.iterator(); iterator.hasNext();) {
							if(i <= textviewids.length){
								textviews[i].setText(iterator.next().toString());
							} else {break;}
							i++;
							Log.d("hage", ""+i);
						}
					}
				}else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
					//デバイスを見つけられなければメッセージを表示
					if(deviceList.size() == 0){
						textviews[0].setText("そんなもんない");
					}
				}
			}
		};
	}

	//デバイスのリストを格納するアダプター
	private static class DeviceList extends BaseAdapter{
		private Context context;
		private List<DeviceInfo> infoList;

		private DeviceList(Context context){
			this.context = context;
			infoList = new ArrayList<DeviceInfo>();
		}

		public void addDeviceInfo(String name, String address, int rssi){
			infoList.add(new DeviceInfo(name, address, rssi));
		}

		@Override
		public int getCount(){
			return infoList.size();
		}

		@Override
		public Object getItem(int position){
			return infoList.get(position);
		}

		@Override
		public long getItemId(int position){
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent){
//			//デバイスの名前を取得してTextViewで表示
			DeviceInfo info = infoList.get(position);
			TextView deviceView = new TextView(null);
			//deviceView.setText(info.getName() +" \t "+ info.getAddress() +  "\n\t" + info.getRssi() + "dB");
			//deviceView.setTextSize(16f);
//			return parent;
			return deviceView;
			
		}

		//デバイスの名前とアドレスを持つクラス
		private static class DeviceInfo{
			private final String name;
			private final String address;
			private final int rssi;

			private DeviceInfo(String name, String address, int rssi){
				this.name = name;
				this.address = address;
				this.rssi = rssi;
			}

			private String getName(){
				return name;
			}

			private String getAddress(){
				return address;
			}

			private int getRssi(){
				return rssi;
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// デバイスの探索を止める
		if (adapter != null) {
			adapter.cancelDiscovery();
		}

		// ブロードキャストのレシーバを外す
		this.unregisterReceiver(receiver);
	}

	//エラーメッセージをアラートで表示
	private void error(String msg){
		AlertDialog.Builder builder = new AlertDialog.Builder (this);
		builder.setTitle("エラー");
		builder.setMessage(msg);
		builder.setCancelable(true);

		AlertDialog dialog = builder.create();
		dialog.show();

	}

	
	//デバイスの名前とアドレスを持つクラス
	private class DeviceInfo{
		private final String name;
		private final String address;
		private final int rssi;

		private DeviceInfo(String name, String address, int rssi){
			this.name = name;
			this.address = address;
			this.rssi = rssi;
		}

		private String getName(){
			return name;
		}

		private String getAddress(){
			return address;
		}

		private int getRssi(){
			return rssi;
		}
		
		@Override
		public String toString() {
			// TODO 自動生成されたメソッド・スタブ
			return getName() +" \t "+ getAddress() +  "\n\t" + getRssi() + "dB";
		}
	}
}

//package jp.ac.pu.iwate.ds.bluetooth3d;
//
//
//import android.os.Bundle;
//import android.os.Handler;
//import android.util.Log;
//import android.app.Activity;
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.view.Menu;
//import android.widget.TextView;
//
//public class MainActivity extends Activity {
//	private final static int REQUEST_ENABLE_BT = 1;
//	private BluetoothAdapter mBtAdapter; 
//	private String mResultString = "";
//	private TextView mScanResult;
//	private BroadcastReceiver mReceiver = new BroadcastReceiver(){
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			// TODO 自動生成されたメソッド・スタブ
//				String action = intent.getAction();
//				if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//					int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
//					mResultString += "Device:" + device.getName() + "\tRSSI=" + rssi + "\n";
//					mScanResult.setText(mResultString);
//				}
//		}	
//		
//	};
//	
//
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_main);
//		mScanResult = (TextView) findViewById(R.id.textView1); 
//		
////		_receiver = new BluetoothBroadcastReceiver();
////		_receiver.registerSelf(MainActivity.this);
//		
//		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//		registerReceiver(mReceiver, filter);
//
//		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
//		
////		final Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
//		
//		if(!mBtAdapter.equals(null)) {
//			Log.d("info" ,"Bluetoothがサポート");
//		} else {
//			Log.d("info", "Bluetoothがないよ");
//		}
//
//		boolean btEnable = mBtAdapter.isEnabled();
//		if (!btEnable) {
//			Intent btOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//			startActivityForResult(btOn, REQUEST_ENABLE_BT);
//		}
//		mBtAdapter.startDiscovery();
//	}
//
//
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
//		return true;
//	}
//
//	@Override
//	protected void onDestroy() {
//		// TODO 自動生成されたメソッド・スタブ
//		super.onDestroy();
//
//		if(mBtAdapter.isDiscovering()){
//			mBtAdapter.cancelDiscovery();
//		}
//		unregisterReceiver(mReceiver);
//	}
//	
//	
//}
//
//
