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
import android.widget.RelativeLayout;
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
	private RelativeLayout lineWindow;
	private MultipleCanvasView mPenCanvasView;
	private Handler mHandler = new Handler();

	private ScaleType scaleType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 控制屏幕常亮
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_note);
		lineWindow = (RelativeLayout) findViewById(R.id.lineWindow);
		// 启动USB服务
		RobotPenApplication.getInstance().bindPenService();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (mPenService == null) {
			mProgressDialog = ProgressDialog.show(this, "", getString(R.string.service_usb_start), true);
			// 启动笔服务
			initPenService();
		}else{
			mPenCanvasView.refresh();//重新回到界面时必须刷新一次画布
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
			// 如果要弹出确认则必须设置连接监听
			mPenService.setSceneType(SceneType.INCH_101);// 设置场景值，用于坐标转化
			mPenService.setOnConnectStateListener(onConnectStateListener);
			mPenService.scanDevice(null);
			dismissProgressDialog(); //此处写法要求必须连接设备才可以使用画布
			mPenCanvasView = new MultipleCanvasView(NoteActivity.this, this);//画布只能通过new的方式创建
			lineWindow.addView(mPenCanvasView);
			mPenCanvasView.setPenIcon(R.drawable.ic_pen);
			mPenCanvasView.refresh();	
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
//				dismissProgressDialog(); //此处写法要求必须连接设备才可以使用画布
//				mPenCanvasView.setPenIcon(R.drawable.ic_pen);
//				mPenCanvasView.refresh();// 通过XML创建的画布在获取到笔服务后必须重新刷新一次
			}
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
	
	@Override
	public void onCanvasSizeChanged(int arg0, int arg1) {
		// TODO Auto-generated method stub
		
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
