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
				// TODO �����������ꂽ���\�b�h�E�X�^�u
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
			//�f�o�C�X�T���p��BroadcastReceiver�̏���
			setReceiver();

			//���V�[�o�Ŏ󂯎�郁�b�Z�[�W�̐ݒ�
			IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
			registerReceiver(receiver, filter);

			filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
			registerReceiver(receiver, filter);

			bluetoothEnable();
		}

//		setContentView(listView);
	}

	//�A�_�v�^�̍쐬�B����������true���A���s������false��Ԃ�
	private boolean  createAdapter(){
		adapter = BluetoothAdapter.getDefaultAdapter();

		if(adapter == null){
			return false;
		}

		return true;
	}

	private void bluetoothEnable(){
		if(adapter.isEnabled()){
			//Bluetooth�����p�\�ȏ�ԂȂ�f�o�C�X�̒T���J�n
			doDiscovery();
		}else{
			//Bluetooth�̗��p�������߂�_�C�A���O�\���B���ʂ�onActibityResult�Ŏ󂯎��
			Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(intent, MainActivity.REQUEST_ENABLE_BLUETOOTH);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if(requestCode ==MainActivity.REQUEST_ENABLE_BLUETOOTH){
			if(resultCode == Activity.RESULT_OK){
				//Bluetooth�̎d�l����������f�o�C�X�̒T���J�n
				doDiscovery();
			}else{
				error("Bluetooth�𗘗p�o���܂���");
			}
		}
	}

	//�f�o�C�X��T�����n�߂�B���ʂ�BroadcastReceiver�Ŏ󂯎��
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

				//�f�o�C�X����������A�_�v�^�ɖ��O�ƃA�h���X��ǉ�
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
					//�f�o�C�X���������Ȃ���΃��b�Z�[�W��\��
					if(deviceList.size() == 0){
						textviews[0].setText("����Ȃ���Ȃ�");
					}
				}
			}
		};
	}

	//�f�o�C�X�̃��X�g���i�[����A�_�v�^�[
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
//			//�f�o�C�X�̖��O���擾����TextView�ŕ\��
			DeviceInfo info = infoList.get(position);
			TextView deviceView = new TextView(null);
			//deviceView.setText(info.getName() +" \t "+ info.getAddress() +  "\n\t" + info.getRssi() + "dB");
			//deviceView.setTextSize(16f);
//			return parent;
			return deviceView;
			
		}

		//�f�o�C�X�̖��O�ƃA�h���X�����N���X
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

		// �f�o�C�X�̒T�����~�߂�
		if (adapter != null) {
			adapter.cancelDiscovery();
		}

		// �u���[�h�L���X�g�̃��V�[�o���O��
		this.unregisterReceiver(receiver);
	}

	//�G���[���b�Z�[�W���A���[�g�ŕ\��
	private void error(String msg){
		AlertDialog.Builder builder = new AlertDialog.Builder (this);
		builder.setTitle("�G���[");
		builder.setMessage(msg);
		builder.setCancelable(true);

		AlertDialog dialog = builder.create();
		dialog.show();

	}

	
	//�f�o�C�X�̖��O�ƃA�h���X�����N���X
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
			// TODO �����������ꂽ���\�b�h�E�X�^�u
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
//			// TODO �����������ꂽ���\�b�h�E�X�^�u
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
//			Log.d("info" ,"Bluetooth���T�|�[�g");
//		} else {
//			Log.d("info", "Bluetooth���Ȃ���");
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
//		// TODO �����������ꂽ���\�b�h�E�X�^�u
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
