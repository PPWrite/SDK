package cn.robotpen.demo;

import java.util.HashMap;

import cn.robotpen.model.interfaces.Listeners.OnScanDeviceListener;
import cn.robotpen.model.DeviceObject;
import cn.robotpen.core.services.PenService;
import cn.robotpen.model.symbol.Keys;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity {
	
	private Button mScanBut;
	private ListView mDeviceList;
	private TextView mEmptytext;
	
	private PenAdapter mPenAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mPenAdapter = new PenAdapter(this);
		
		mEmptytext = (TextView) findViewById(R.id.emptytext);
		mDeviceList = (ListView) findViewById(R.id.listview);
		mDeviceList.setEmptyView(mEmptytext);
		mDeviceList.setAdapter(mPenAdapter);
		mDeviceList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				//停止搜索
				PenService service = RobotPenApplication.getInstance().getPenService();
				if(service != null){
					service.stopScanDevice();
				}
				
				DeviceObject item = mPenAdapter.getItem(arg2);
				
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, PenInfo.class);
				intent.putExtra(Keys.KEY_DEVICE_ADDRESS, item.address);
				startActivity(intent);
			}
		});

		mScanBut = (Button) findViewById(R.id.scanBut);
		mScanBut.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mScanBut.setText("Scanning...");
				mScanBut.setEnabled(false);
				mPenAdapter.clearItems();
				mPenAdapter.notifyDataSetChanged();
				
				PenService service = RobotPenApplication.getInstance().getPenService();
				if(service != null){
					service.scanDevice(onScanDeviceListener);
				}
			}
		});
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
//		return true;
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		// Handle action bar item clicks here. The action bar will
//		// automatically handle clicks on the Home/Up button, so long
//		// as you specify a parent activity in AndroidManifest.xml.
//		int id = item.getItemId();
//		if (id == R.id.action_settings) {
//			return true;
//		}
//		return super.onOptionsItemSelected(item);
//	}
	
	private OnScanDeviceListener onScanDeviceListener = new OnScanDeviceListener(){
		@Override
		public void find(DeviceObject device) {
			mPenAdapter.addItem(device);
			mPenAdapter.notifyDataSetChanged();
		}

		@Override
		public void complete(HashMap<String, DeviceObject> list) {
			mScanBut.setText("Start Scan");
			mScanBut.setEnabled(true);
		}
	};
}
