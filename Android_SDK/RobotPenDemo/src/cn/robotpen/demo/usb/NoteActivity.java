package cn.robotpen.demo.usb;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Random;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView.ScaleType;
import cn.robotpen.core.module.ImageRecordModule.ImageRecordInterface;
import cn.robotpen.core.services.PenService;
import cn.robotpen.core.views.MultipleCanvasView;
import cn.robotpen.core.views.MultipleCanvasView.CanvasManageInterface;
import cn.robotpen.core.views.MultipleCanvasView.PenModel;
import cn.robotpen.demo.R;
import cn.robotpen.demo.RobotPenApplication;
import cn.robotpen.file.services.FileManageService;
import cn.robotpen.model.DeviceObject;
import cn.robotpen.model.interfaces.Listeners.OnScanDeviceListener;
import cn.robotpen.model.symbol.RecordState;

public class NoteActivity extends Activity implements CanvasManageInterface, ImageRecordInterface {
	private PenService mPenService;
	private ProgressDialog mProgressDialog;
	private String mUserId;
	private FrameLayout.LayoutParams mDrawAreaParams;
	private MultipleCanvasView mPenCanvasView;
	private int mDisplayWidth;
	private int mDisplayHeight;
	private Handler mHandler = new Handler();

	private ScaleType scaleType;
	/** 笔画布 **/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 控制屏幕常亮
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_note);
		// 创建画布
		mPenCanvasView = (MultipleCanvasView) findViewById(R.id.penCanvasView);
		DisplayMetrics metric = new DisplayMetrics();
	    getWindowManager().getDefaultDisplay().getMetrics(metric);
	    mDisplayWidth = metric.widthPixels;  // 屏幕宽度（像素）
	    mDisplayHeight = metric.heightPixels;  // 屏幕高度（像素）
		mDrawAreaParams = new LayoutParams(mDisplayWidth, mDisplayHeight);
		//启动USB服务
		RobotPenApplication.getInstance().bindPenService();
		
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mProgressDialog = ProgressDialog.show(this, "", getString(R.string.service_usb_start), true);
		initPenService();

	}

	@Override
	public void onPause() {
		super.onPause();
		RobotPenApplication.getInstance().unBindPenService();
		RobotPenApplication.getInstance().unBindFileService();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	/*
	 * 一定要确保笔服务是启动的
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
			mPenService.scanDevice(onScanDeviceListener);
			dismissProgressDialog();
			Random rd = new Random();
	        mUserId = "u"+ String.valueOf(rd.nextInt(10000));
			mPenService.setUserId(mUserId);
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
	 * 主动扫描监听
	 * 
	 */
	private OnScanDeviceListener onScanDeviceListener = new OnScanDeviceListener() {
		
		@Override
		public void find(DeviceObject arg0) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void complete(HashMap<String, DeviceObject> arg0) {
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

	// FIXME
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
	public int fillImageBuffer(ByteBuffer buffer) {
		// TODO Auto-generated method stub
		int result = 0;
		// mPenCanvasView.setDrawingCacheEnabled(true);
		// mPenCanvasView.buildDrawingCache();
		// Bitmap imageCache = mPenCanvasView.getDrawingCache();
		// if (imageCache != null) {
		// result = imageCache.getByteCount();
		// imageCache.copyPixelsToBuffer(buffer);
		// }
		// mPenCanvasView.setDrawingCacheEnabled(false);
		return result;
	}

	@Override
	public void recordTimeChange(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void recordWarning(RecordState arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void videoCodeState(int progress) {
		// TODO Auto-generated method stub
		if (progress > 100) { // progress=100 表示正在压制 progress>100 表示录制成功
			// 保存
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle("录制完成");
			alert.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					try {
						Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
						field.setAccessible(true);
						field.set(dialog, true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			alert.show();

		}

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
}
