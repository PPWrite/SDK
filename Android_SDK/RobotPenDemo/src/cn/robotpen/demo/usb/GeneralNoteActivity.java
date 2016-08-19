package cn.robotpen.demo.usb;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.ImageView.ScaleType;
import cn.robotpen.core.module.ImageRecordModule.ImageRecordInterface;
import cn.robotpen.core.services.PenService;
import cn.robotpen.core.views.MultipleCanvasView;
import cn.robotpen.core.views.MultipleCanvasView.CanvasManageInterface;
import cn.robotpen.core.views.MultipleCanvasView.PenModel;
import cn.robotpen.demo.R;
import cn.robotpen.demo.RobotPenApplication;
import cn.robotpen.demo.utils.ResUtils;
import cn.robotpen.file.services.FileManageService;
import cn.robotpen.model.interfaces.Listeners.OnConnectStateListener;
import cn.robotpen.model.symbol.ConnectState;
import cn.robotpen.model.symbol.RecordState;
import cn.robotpen.model.symbol.SceneType;

public class GeneralNoteActivity extends Activity implements CanvasManageInterface, ImageRecordInterface {
	private PenService mPenService;
	private ProgressDialog mProgressDialog;
	private String mUserId;
	private RelativeLayout lineWindow;
	private MultipleCanvasView mPenCanvasView;
	private Handler mHandler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 控制屏幕常亮
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
		setContentView(R.layout.activity_generalnote);
		// 初始化UI
		initUI();
		// 启动USB服务
		RobotPenApplication.getInstance().bindPenService();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		if (mPenService == null) {
			mProgressDialog = ProgressDialog.show(this, "", getString(R.string.service_usb_start), true);
			// 启动笔服务
			initPenService();
		} else {
			// // 判断是否设置了背景图片
			// if (mInsertPhotoUri != null) {
			// mPenCanvasView.insertPhoto(mInsertPhotoUri);
			// mInsertPhotoUri = null;
			// }
			// mPenCanvasView.refresh();
		}
		super.onResume();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub

		// 断开设备
		if (mPenService != null) {
			RobotPenApplication.getInstance().unBindPenService();
		}
		super.onStop();
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
			dismissProgressDialog();
			// 初始化画布和录制对象
			initCanvas();
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
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			// mInsertPhotoUri = null;
			// if (requestCode == SELECT_PICTURE && data != null) {
			// mInsertPhotoUri = data.getData();
			// }
			// if (requestCode == SELECT_BG && data != null) {
			// mBgUri = data.getData();
			// }
		}
	}

	/*
	 * 界面初始化
	 */
	void initUI() {
		lineWindow = (RelativeLayout) findViewById(R.id.lineWindow);

	}

	void initCanvas() {
		mPenCanvasView = new MultipleCanvasView(GeneralNoteActivity.this, this);
		lineWindow.addView(mPenCanvasView);
		mPenCanvasView.setPenIcon(R.drawable.ic_pen);
		// penColor = 0xFF000000;// 设置笔颜色
		// penWeight = 2;
		mPenCanvasView.refresh();// 通过XML创建的画布在获取到笔服务后必须重新刷新一次
		// mTimeShowformat.setTimeZone(TimeZone.getTimeZone("GMT0"));
		// 先判断文件夹是否创建
		if (ResUtils.isDirectory(ResUtils.DIR_NAME_BUFFER) && ResUtils.isDirectory(ResUtils.DIR_NAME_PHOTO)
				&& ResUtils.isDirectory(ResUtils.DIR_NAME_VIDEO) && ResUtils.isDirectory(ResUtils.DIR_NAME_DATA)) {
			// 获取压缩级别
			int level = RobotPenApplication.getInstance().getRecordLevel();
			// 初始化录制工具
			// mImageRecordModule = new
			// ImageRecordModule(GeneralNoteActivity.this);
			// mImageRecordModule.setSavePhotoDir(ResUtils.getSavePath(ResUtils.DIR_NAME_PHOTO));
			// mImageRecordModule.setSaveVideoDir(ResUtils.getSavePath(ResUtils.DIR_NAME_VIDEO));
			// mImageRecordModule.setRecordLevel(level);
			// mImageRecordModule.initImageRecord();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.robotpen.core.views.MultipleCanvasView.CanvasManageInterface#
	 * getBgColor()
	 */
	@Override
	public int getBgColor() {
		// TODO Auto-generated method stub
		return 0xFFFFFFF0;
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
		return 0;
	}

	@Override
	public PenModel getPenModel() {
		return PenModel.WaterPen;
	}

	@Override
	public float getPenWeight() {
		return 0.0f;
	}

	@Override
	public void penRouteStatus(boolean arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public ScaleType getBgScaleType() {
		// TODO Auto-generated method stub
		return null;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.robotpen.core.module.ImageRecordModule.ImageRecordInterface#
	 * fillImageBuffer(java.nio.ByteBuffer)
	 */
	@Override
	public int fillImageBuffer(ByteBuffer buffer) {
		// TODO Auto-generated method stub
		int result = 0;
		// 启用DrawingCache并创建位图
		View view = mPenCanvasView.getDrawAreaView();
		if (view != null) {
			view.setDrawingCacheEnabled(true);
			view.buildDrawingCache();
			Bitmap imageCache = view.getDrawingCache();
			if (imageCache != null) {
				result = imageCache.getByteCount();
				imageCache.copyPixelsToBuffer(buffer);
			}
			view.setDrawingCacheEnabled(false);
		}
		return result;
	}

	@Override
	public void recordTimeChange(int arg0) {
		// TODO Auto-generated method stub
		// mTimeShowDate.setTime(arg0 * 1000);
		// String time = mTimeShowformat.format(mTimeShowDate);
		// recordBut.setText("暂停" + time);

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
	public void onCanvasSizeChanged(int w, int h) {
		// TODO Auto-generated method stub
		// mImageRecordModule.setInputSize(w, h);
	}
}
