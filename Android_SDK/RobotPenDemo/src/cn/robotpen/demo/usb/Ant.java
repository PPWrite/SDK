package cn.robotpen.demo.usb;

import java.util.HashMap;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import cn.robotpen.core.services.PenService;
import cn.robotpen.demo.R;
import cn.robotpen.demo.RobotPenApplication;
import cn.robotpen.model.DeviceObject;
import cn.robotpen.model.interfaces.Listeners.OnConnectStateListener;
import cn.robotpen.model.interfaces.Listeners.OnScanDeviceListener;
import cn.robotpen.model.symbol.ConnectState;
import cn.robotpen.model.symbol.SceneType;

public class Ant extends Activity{
	public static final String TAG = GetAxesActivity.class.getSimpleName();
	private ProgressDialog mProgressDialog;
	private PenService mPenService;
	private Handler mHandler = new Handler();

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
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		mProgressDialog = ProgressDialog.show(this, "", getString(R.string.service_usb_start), true);
		// 启动笔服务
		initPenService();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
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
		// 断开设备
		if (mPenService != null) {
			RobotPenApplication.getInstance().unBindPenService();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
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
