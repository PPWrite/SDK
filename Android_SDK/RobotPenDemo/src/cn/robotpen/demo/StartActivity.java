package cn.robotpen.demo;

import cn.robotpen.core.services.PenService;
import cn.robotpen.file.services.FileManageService;
import cn.robotpen.model.symbol.Keys;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

/**
 * 
 * @author Xiaoz
 * @date 2015年9月30日 下午4:38:45
 *
 *       Description
 */
public class StartActivity extends Activity implements OnClickListener {
	private Handler mHandler;
	private Button mBleBut;
	private Button mUsbBut;
	private ProgressDialog mProgressDialog;
	public static final String TAG = StartActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);

		
		mHandler = new Handler();
		mBleBut = (Button) findViewById(R.id.bleBut);
		mUsbBut = (Button) findViewById(R.id.usbBut);

		mBleBut.setOnClickListener(this);
		mUsbBut.setOnClickListener(this);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bleBut:
			mProgressDialog = ProgressDialog.show(this, "", getString(R.string.service_ble_start), true);
			// 绑定蓝牙笔服务
			RobotPenApplication.getInstance().bindPenService(Keys.APP_PEN_SERVICE_NAME);
			//绑定文件服务
			RobotPenApplication.getInstance().bindFileService();
			isServiceReady(Keys.APP_PEN_SERVICE_NAME);
			break;
		case R.id.usbBut:
			mProgressDialog = ProgressDialog.show(this, "", getString(R.string.service_usb_start), true);
			// 绑定USB笔服务
			RobotPenApplication.getInstance().bindPenService(Keys.APP_USB_SERVICE_NAME);
			//绑定文件服务
			RobotPenApplication.getInstance().bindFileService();
			isServiceReady(Keys.APP_USB_SERVICE_NAME);
			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		StartActivity.this.finish();
	}

	//确定服务启动
	private void isServiceReady(final String svrName) {
		final PenService service = RobotPenApplication.getInstance().getPenService();
		FileManageService fileService = RobotPenApplication.getInstance().getFileService();
		if (service != null && fileService != null) {
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					dismissProgressDialog();
					if (Keys.APP_PEN_SERVICE_NAME.equals(svrName)) {
						startActivity(new Intent(StartActivity.this, MainActivity.class));
					} else if (Keys.APP_USB_SERVICE_NAME.equals(svrName)) {
						Intent intent = new Intent(StartActivity.this, PenInfo.class);
						intent.putExtra(Keys.KEY_VALUE, svrName);
						startActivity(intent);
					}
				}
			}, 500);
		} else {
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					isServiceReady(svrName);
				}
			}, 1000);
		}
	}

	/** 释放progressDialog **/
	protected void dismissProgressDialog() {
		if (mProgressDialog != null) {
			if (mProgressDialog.isShowing())
				mProgressDialog.dismiss();

			mProgressDialog = null;
		}
	}

}
