package cn.robotpen.demo.usb;

import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
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

public class GetAxesActivity extends Activity {
	private String[] mItems = { "10.1寸竖屏", "10.1寸横屏" };
	private ProgressDialog mProgressDialog;
	private PenService mPenService;
	private Handler mHandler = new Handler();
	private Button deviceBut;
	private Spinner mSceneType;
	private TextView isRoute; // 是否写入状态
	private TextView pressure; // 压感
	private TextView originalX;
	private TextView originalY;

	private TextView sceneType1;
	private TextView sceneWidth;
	private TextView sceneHeight;
	private TextView sceneOffsetX;
	private TextView sceneOffsetY;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// 控制屏幕常亮
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_getaxes);
		// 启动USB服务
		RobotPenApplication.getInstance().bindPenService();
		initUI();// 初始化界面
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
//		mProgressDialog = ProgressDialog.show(this, "", getString(R.string.service_usb_start), true);
//		// 启动笔服务
//		initPenService();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (mPenService == null) {
			mProgressDialog = ProgressDialog.show(this, "", getString(R.string.service_usb_start), true);
			// 启动笔服务
			initPenService();
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		// 如果有多个应用或者多个Activity使用笔服务的话，结束前必须断开设备
		if (mPenService != null) {
			RobotPenApplication.getInstance().unBindPenService();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/*
	 * 界面初始化
	 */
	void initUI() {
		deviceBut = (Button) findViewById(R.id.deviceBut);
		deviceBut.setOnClickListener(buttonClick);
		mSceneType = (Spinner) findViewById(R.id.sceneType);
		// 笔的信息
		isRoute = (TextView) findViewById(R.id.isRoute);
		pressure = (TextView) findViewById(R.id.pressure);
		originalX = (TextView) findViewById(R.id.originalX);
		originalY = (TextView) findViewById(R.id.originalY);
		// 设备板信息
		sceneType1 = (TextView) findViewById(R.id.sceneType1);
		sceneWidth = (TextView) findViewById(R.id.sceneWidth);
		sceneHeight = (TextView) findViewById(R.id.sceneHeight);
		sceneOffsetX = (TextView) findViewById(R.id.sceneOffsetX);
		sceneOffsetY = (TextView) findViewById(R.id.sceneOffsetY);

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
	}

	/*
	 * 此处一定要确保笔服务是启动的
	 */
	private void initPenService() {
		mPenService = RobotPenApplication.getInstance().getPenService();
		if (mPenService == null)
			RobotPenApplication.getInstance().bindPenService();
		isPenServiceReady();
	}

	private void isPenServiceReady() {
		mPenService = RobotPenApplication.getInstance().getPenService();
		if (mPenService != null) {
			dismissProgressDialog();
			mPenService.setSceneType(SceneType.INCH_101);// 设置场景值，用于坐标转化
			// 如果要弹出确认则必须设置连接监听
			mPenService.setOnConnectStateListener(onConnectStateListener);
			mPenService.setOnPointChangeListener(onPointChangeListener);
		} else {
			mHandler.postDelayed(new Runnable() {
				public void run() {
					// execute the task
					isPenServiceReady();
				}
			}, 500);
		}
	}

	/*
	 * 手动执行设备扫描
	 */
	private OnClickListener buttonClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			mProgressDialog = ProgressDialog.show(GetAxesActivity.this, "", "正在扫描设备……", true);
			mPenService.scanDevice(onScanDeviceListener);
		}
	};
	/*
	 * 此处监听是为了弹出授权
	 */
	private OnConnectStateListener onConnectStateListener = new OnConnectStateListener() {
		@Override
		public void stateChange(String arg0, ConnectState arg1) {
			// TODO Auto-generated method stub
			if (arg1 == ConnectState.CONNECTED) {
				dismissProgressDialog();
			}
		}
	};
	/*
	 * 手动执行设备扫描，确保设备保持连接状态
	 */
	private OnScanDeviceListener onScanDeviceListener = new OnScanDeviceListener() {
		@Override
		public void complete(HashMap<String, DeviceObject> arg0) {
			// TODO Auto-generated method stub

		}
		@Override
		public void find(DeviceObject arg0) {
			// TODO Auto-generated method stub

		}

	};
	/*
	 * 通过监听方式完成业务处理 这里接收笔的信息建议通过监听，通过广播方式也是可以的，但是广播方式效率较低
	 */
	private OnPointChangeListener onPointChangeListener = new OnPointChangeListener() {
		@Override
		public void change(PointObject point) {
			// TODO Auto-generated method stub
			// 设置看坐标中的各个字段
			originalX.setText(String.valueOf(point.originalX));
			originalY.setText(String.valueOf(point.originalY));
			isRoute.setText(String.valueOf(point.isRoute));
			pressure.setText(String.valueOf(point.pressure) + "(" + String.valueOf(point.pressureValue) + ")");

			sceneType1.setText(String.valueOf(point.getSceneType()));
			sceneWidth.setText(String.valueOf(point.getWidth(mPenService.getSceneType())));
			sceneHeight.setText(String.valueOf(point.getHeight(mPenService.getSceneType())));
			sceneOffsetX.setText(String.valueOf(point.getSceneX()));
			sceneOffsetY.setText(String.valueOf(point.getSceneY()));
		}

		@Override
		public void change(List<PointObject> arg0) {
			// TODO Auto-generated method stub

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
