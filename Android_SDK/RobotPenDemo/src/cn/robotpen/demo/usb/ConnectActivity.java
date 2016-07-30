package cn.robotpen.demo.usb;

import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import cn.robotpen.core.services.PenService;
import cn.robotpen.demo.R;
import cn.robotpen.demo.RobotPenApplication;
import cn.robotpen.model.DeviceObject;
import cn.robotpen.model.PointObject;
import cn.robotpen.model.interfaces.Listeners.OnConnectStateListener;
import cn.robotpen.model.interfaces.Listeners.OnPointChangeListener;
import cn.robotpen.model.interfaces.Listeners.OnScanDeviceListener;
import cn.robotpen.model.symbol.ConnectState;
import cn.robotpen.model.symbol.SceneType;
import cn.robotpen.utils.LogUtil;

public class ConnectActivity extends Activity {
	public static final String TAG = GetAxesActivity.class.getSimpleName();
	private String[] mItems = { "10.1寸竖屏", "10.1寸横屏" };
	private ProgressDialog mProgressDialog;
	private ProgressDialog mProgressDialog2;
	private PenService mPenService;
	private Button deviceBut;
	private Spinner mSceneType;
	private TextView isRoute; // 是否写入状态
	private TextView pressure;
	private TextView originalX;
	private TextView originalY;

	private TextView sceneType1;
	private TextView sceneWidth;
	private TextView sceneHeight;
	private TextView sceneOffsetX;
	private TextView sceneOffsetY;

	// private

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_getaxes);
		initUI();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mProgressDialog = ProgressDialog.show(this, "", getString(R.string.service_usb_start), true);
		initPenService();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		// 断开设备
		if (mPenService != null) {
            mPenService.setOnConnectStateListener(null);
			RobotPenApplication.getInstance().unBindPenService();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 断开设备
		if (mPenService != null) {
            mPenService.setOnConnectStateListener(null);
			RobotPenApplication.getInstance().unBindPenService();
			mPenService.disconnectDevice();
		}
		ConnectActivity.this.finish();
	}

	void initUI() {
		deviceBut = (Button) findViewById(R.id.deviceBut);
		mSceneType = (Spinner) findViewById(R.id.sceneType);
		// 笔的信息
		isRoute = (TextView) findViewById(R.id.isRoute);
		pressure = (TextView) findViewById(R.id.pressure);
		originalX = (TextView) findViewById(R.id.originalX);
		originalY = (TextView) findViewById(R.id.originalY);
//		// 设备板信息
//		sceneType1 = (TextView) findViewById(R.id.sceneType1);
//		sceneWidth = (TextView) findViewById(R.id.sceneWidth);
//		sceneHeight = (TextView) findViewById(R.id.sceneHeight);
//		sceneOffsetX = (TextView) findViewById(R.id.sceneOffsetX);
//		sceneOffsetY = (TextView) findViewById(R.id.sceneOffsetY);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mItems);
		mSceneType.setAdapter(adapter);
		mSceneType.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				switch (position) {
				case 0:
					if (null != mPenService)
						mPenService.setSceneType(SceneType.INCH_101);
					break;
				case 1:
					if (null != mPenService)
						mPenService.setSceneType(SceneType.INCH_101_horizontal);
					break;
				default:
					break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub

			}
		});
		deviceBut.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mPenService.scanDevice(new OnScanDeviceListener() {
					@Override
					public void find(DeviceObject arg0) {
						// TODO Auto-generated method stub

					}

					@Override
					public void complete(HashMap<String, DeviceObject> arg0) {
						// TODO Auto-generated method stub

					}
				});
			}
		});
	}

	/*
	 * 
	 * 一定要确保笔服务是启动的
	 */
	private void initPenService() {
		if (mPenService == null)
			mPenService = RobotPenApplication.getInstance().getPenService();
		if (mPenService == null)
			RobotPenApplication.getInstance().bindPenService();
		isPenServiceReady();
	}

	private void isPenServiceReady() {
		LogUtil.addLog(TAG, "isPenServiceReady");
		mPenService = RobotPenApplication.getInstance().getPenService();
		if (mPenService != null) {
			LogUtil.addLog(TAG + "///连接成功");
			dismissProgressDialog();
			mPenService.setOnConnectStateListener(onConnectStateListener);
			mPenService.setSceneType(SceneType.INCH_101);
			mPenService.setOnPointChangeListener(onPointChangeListener);
		} else {
			new Handler().postDelayed(new Runnable() {
				public void run() {
					// execute the task
					isPenServiceReady();
				}
			}, 500);
		}
	}

	/*
	 * 通过监听方式完成业务处理 这里接收笔的信息建议通过监听，通过广播方式也是可以的，但是广播方式效率较低
	 */
	private OnPointChangeListener onPointChangeListener = new OnPointChangeListener() {

		@Override
		public void change(PointObject point) {
			LogUtil.addLog(TAG + "///" + point.originalX + point.originalY + point.pressure);
			// TODO Auto-generated method stub
			// 设置看坐标中的各个字段
			originalX.setText(String.valueOf(point.originalX));
			originalY.setText(String.valueOf(point.originalY));
			isRoute.setText(String.valueOf(point.isRoute));
			pressure.setText(String.valueOf(point.pressure) + "(" + String.valueOf(point.pressureValue) + ")");

//			sceneType1.setText(String.valueOf(point.getSceneType()));
//			sceneWidth.setText(String.valueOf(point.getWidth(mPenService.getSceneType())));
//			sceneHeight.setText(String.valueOf(point.getHeight(mPenService.getSceneType())));
//			sceneOffsetX.setText(String.valueOf(point.getSceneX()));
//			sceneOffsetY.setText(String.valueOf(point.getSceneY()));
		}

		@Override
		public void change(List<PointObject> arg0) {
			// TODO Auto-generated method stub

		}
	};
	/**
	 * 此监听由后台服务进行处理，所以此处可以基本忽略
	 */
	OnScanDeviceListener onScanDeviceListener = new OnScanDeviceListener() {

		@Override
		public void find(DeviceObject arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void complete(HashMap<String, DeviceObject> arg0) {
			// TODO Auto-generated method stub

		}
	};
	/*
	 * 检测设备是否连接成功
	 */
	private OnConnectStateListener onConnectStateListener = new OnConnectStateListener() {
		@Override
		public void stateChange(String address, ConnectState state) {
			// state.getValue 2为连接成功 5为断开连接
			if (state.getValue() == 2) {
				LogUtil.addLog(TAG, "2为连接成功");
				if (mProgressDialog2 != null) {
					if (mProgressDialog2.isShowing())
						mProgressDialog2.dismiss();
					mProgressDialog2 = null;

				}
			} else if (state.getValue() == 5) {
				LogUtil.addLog(TAG, "5为断开");
				AlertDialog.Builder alert = new AlertDialog.Builder(ConnectActivity.this);
				alert.setTitle("设备状态");
				alert.setMessage("设备已断开");
				alert.setPositiveButton("重新连接", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (null != mPenService) {
							mPenService.scanDevice(onScanDeviceListener);
							mProgressDialog2 = ProgressDialog.show(ConnectActivity.this, "", "连接中", true);
							dialog.dismiss();
							mProgressDialog2.show();
						
						}
					}
				});
				alert.show();
			}
		}

	};

	/**
	 * 释放progressDialog
	 **/
	private void dismissProgressDialog() {
		if (mProgressDialog != null) {
			if (mProgressDialog.isShowing())
				mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}

}
