package cn.robotpen.demo.usb;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import cn.robotpen.core.services.PenService;
import cn.robotpen.demo.R;
import cn.robotpen.demo.RobotPenApplication;
import cn.robotpen.model.PointObject;
import cn.robotpen.model.interfaces.Listeners.OnConnectStateListener;
import cn.robotpen.model.interfaces.Listeners.OnPointChangeListener;
import cn.robotpen.model.symbol.ConnectState;
import cn.robotpen.model.symbol.SceneType;
import cn.robotpen.utils.LogUtil;

public class GetAxesActivity extends Activity {
	public static final String TAG = GetAxesActivity.class.getSimpleName();
	private ProgressDialog mProgressDialog;
	private ProgressDialog mProgressDialog2;
	private PenService mPenService;

	// private

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_getaxes);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		initPenService();
	}
	
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
		GetAxesActivity.this.finish();
	}

	/*
	 * 
	 * 一定要确保笔服务是启动的
	 */
	private void initPenService() {
		LogUtil.addLog(TAG + "///isPenServiceReady");
		if (mPenService == null)
			mPenService = RobotPenApplication.getInstance().getPenService();
		if (mPenService == null)
			RobotPenApplication.getInstance().bindPenService();
		isPenServiceReady();
	}

	private void isPenServiceReady() {
		LogUtil.addLog(TAG + "///isPenServiceReady");
		mPenService = RobotPenApplication.getInstance().getPenService();
		if (mPenService != null) {
			LogUtil.addLog(TAG + "///连接成功");
			mPenService.setOnConnectStateListener(onConnectStateListener);
			mPenService.setSceneType(SceneType.INCH_101);
			mPenService.setOnPointChangeListener(onPointChangeListener);
			LogUtil.addLog(TAG + "///监听成功");
		} else {
			new Handler().postDelayed(new Runnable() {
				public void run() {
					LogUtil.addLog(TAG + "///再执行一次");
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
		}

		@Override
		public void change(List<PointObject> arg0) {
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
				LogUtil.addLog(TAG+"2为连接成功");
				
			} else if (state.getValue() == 5) {
				LogUtil.addLog(TAG+"5为断开");
				AlertDialog.Builder alert = new AlertDialog.Builder(GetAxesActivity.this);
				alert.setTitle("设备状态");
				alert.setMessage("设备已断开");
				alert.setPositiveButton("重新连接", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (null != mPenService) {
							mPenService.setOnConnectStateListener(onConnectStateListener);
							dialog.dismiss();						
						}
					}
				});
				alert.show();
			}
		}

	};

}
