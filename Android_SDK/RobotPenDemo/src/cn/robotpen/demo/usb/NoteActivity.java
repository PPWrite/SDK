package cn.robotpen.demo.usb;

import java.util.HashMap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView.ScaleType;
import cn.robotpen.core.services.PenService;
import cn.robotpen.core.views.MultipleCanvasView;
import cn.robotpen.core.views.MultipleCanvasView.CanvasManageInterface;
import cn.robotpen.core.views.MultipleCanvasView.PenModel;
import cn.robotpen.demo.R;
import cn.robotpen.demo.RobotPenApplication;
import cn.robotpen.file.services.FileManageService;
import cn.robotpen.model.DeviceObject;
import cn.robotpen.model.interfaces.Listeners.OnConnectStateListener;
import cn.robotpen.model.interfaces.Listeners.OnScanDeviceListener;
import cn.robotpen.model.symbol.ConnectState;
import cn.robotpen.model.symbol.SceneType;

public class NoteActivity extends Activity implements CanvasManageInterface {
	private PenService mPenService;
	private ProgressDialog mProgressDialog;
	private String mUserId;
	private FrameLayout.LayoutParams mDrawAreaParams;
	private MultipleCanvasView mPenCanvasView;
	private int mDisplayWidth;
	private int mDisplayHeight;
	private Handler mHandler = new Handler();

	private ScaleType scaleType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 控制屏幕常亮
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_note);
		// 创建画布，创建画布是必须设置宽度和高度
		mPenCanvasView = (MultipleCanvasView) findViewById(R.id.penCanvasView);
		// 示例以根视图显示比例为例，实际代码中可以根据自己需要进行设置
		DisplayMetrics metric = new DisplayMetrics(); //
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		mDisplayWidth = metric.widthPixels; // 屏幕宽度（像素）
		mDisplayHeight = metric.heightPixels; // 屏幕高度（像素）
		mDrawAreaParams = new LayoutParams(mDisplayWidth, mDisplayHeight - 30);
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
			// dismissProgressDialog();
			// 如果要弹出确认则必须设置连接监听
			mPenService.setSceneType(SceneType.INCH_101);// 设置场景值，用于坐标转化
			mPenService.setOnConnectStateListener(onConnectStateListener);
			mPenService.scanDevice(onScanDeviceListener);
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
			if (arg1 == ConnectState.CONNECTED) {
				dismissProgressDialog();
				mPenCanvasView.setPenIcon(R.drawable.ic_pen);
				mPenCanvasView.refresh();// 通过XML创建的画布在获取到笔服务后必须重新刷新一次
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

	@Override
	public int getBgColor() {
		// TODO Auto-generated method stub
		return 0xFFFFFFFF;
	}

	@Override
	public int getBgResId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getCurrUserId() {
		return mUserId;
	}

	@Override
	public float getIsRubber() {
		return 0.0f;
	}

	@Override
	public int getPenColor() {
		return 0xFF000000;
	}

	@Override
	public PenModel getPenModel() {
		return PenModel.WaterPen;
	}

	@Override
	public float getPenWeight() {
		return 2;
	}

	@Override
	public void penRouteStatus(boolean arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public ScaleType getBgScaleType() {
		// TODO Auto-generated method stub
		return scaleType;
	}

	@Override
	public LayoutParams getDrawAreaParams() {
		// TODO Auto-generated method stub
		return mDrawAreaParams;
	}

	@Override
	public FileManageService getFileService() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PenService getPenService() {
		// TODO Auto-generated method stub
		return mPenService;
	}

	@Override
	public Uri getBgPhoto() {
		// TODO Auto-generated method stub
		return null;
	}

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
