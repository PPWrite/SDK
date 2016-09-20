package cn.robotpen.demo.usb;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import cn.robotpen.core.module.ImageRecordModule;
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
import cn.robotpen.utils.LogUtil;

public class NoteWithActivity extends Activity implements CanvasManageInterface, ImageRecordInterface {
	private PenService mPenService;
	private ProgressDialog mProgressDialog;
	private String mUserId;
	private RelativeLayout lineWindow;
	private MultipleCanvasView mPenCanvasView;
	private Handler mHandler = new Handler();

	private float penWeight;
	private Button changePenBut;
	private int penColor;
	private Button changePenColorBut;
	private ImageRecordModule mImageRecordModule;
	private Button saveScreenBut;// 截屏
	// 录制时间显示格式
	private SimpleDateFormat mTimeShowformat = new SimpleDateFormat("HH:mm:ss");
	// 录制时间显示日期对象
	private Date mTimeShowDate = new Date();
	private int butFlag = 0;// 区分录制状态控制按钮操控
	private Button recordBut;
	private Button mRecordStopBut;
	private Uri mInsertPhotoUri;
	private Button insertPhoto;
	private Uri mBgUri;
	private ScaleType scaleType;
	private Button changeBgBut;
	private Button changeBgScaleTypeBut;

	private final String IMAGE_TYPE = "image/*";
	private static final int SELECT_PICTURE = 1001;
	private static final int SELECT_BG = 1002;
	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 控制屏幕常亮
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_notewith);
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
			 // 判断是否设置了背景图片
			 if (mInsertPhotoUri != null) {
			 mPenCanvasView.insertPhoto(mInsertPhotoUri);
			 mInsertPhotoUri = null;
			 }
			 mPenCanvasView.refresh();			
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
			mPenService.setSceneType(SceneType.INCH_101);
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
			mInsertPhotoUri = null;
			if (requestCode == SELECT_PICTURE && data != null) {
				mInsertPhotoUri = data.getData();
			}
			if (requestCode == SELECT_BG && data != null) {
				mBgUri = data.getData();
			}
		}
	}

	/*
	 * 界面初始化
	 */
	void initUI() {
		lineWindow = (RelativeLayout) findViewById(R.id.lineWindow);
		changePenBut = (Button) findViewById(R.id.changePenBut);
		changePenBut.setOnClickListener(buttonClick);
		changePenColorBut = (Button) findViewById(R.id.changePenColorBut);
		changePenColorBut.setOnClickListener(buttonClick);
		saveScreenBut = (Button) findViewById(R.id.saveScreenBut);
		saveScreenBut.setOnClickListener(buttonClick);
		recordBut = (Button) findViewById(R.id.recordBut);
		recordBut.setOnClickListener(buttonClick);
		mRecordStopBut = (Button) findViewById(R.id.mRecordStopBut);
		mRecordStopBut.setOnClickListener(buttonClick);
		insertPhoto = (Button) findViewById(R.id.insertPhoto);
		insertPhoto.setOnClickListener(buttonClick);
		changeBgBut = (Button) findViewById(R.id.changeBgBut);
		changeBgBut.setOnClickListener(buttonClick);
		changeBgScaleTypeBut = (Button) findViewById(R.id.changeBgScaleTypeBut);
		changeBgScaleTypeBut.setOnClickListener(buttonClick);
	}

	void initCanvas() {
		mPenCanvasView = new MultipleCanvasView(NoteWithActivity.this, this);
		lineWindow.addView(mPenCanvasView);
		mPenCanvasView.setPenIcon(R.drawable.ic_pen);
		penColor = 0xFF000000;// 设置笔颜色
		penWeight = 2;
		mPenCanvasView.refresh();// 通过XML创建的画布在获取到笔服务后必须重新刷新一次
		mTimeShowformat.setTimeZone(TimeZone.getTimeZone("GMT0"));
		// 先判断文件夹是否创建
		if (ResUtils.isDirectory(ResUtils.DIR_NAME_BUFFER) && ResUtils.isDirectory(ResUtils.DIR_NAME_PHOTO)
				&& ResUtils.isDirectory(ResUtils.DIR_NAME_VIDEO) && ResUtils.isDirectory(ResUtils.DIR_NAME_DATA)) {
			// 获取压缩级别
			int level = RobotPenApplication.getInstance().getRecordLevel();
			// 初始化录制工具
			mImageRecordModule = new ImageRecordModule(NoteWithActivity.this);
			mImageRecordModule.setSavePhotoDir(ResUtils.getSavePath(ResUtils.DIR_NAME_PHOTO));
			mImageRecordModule.setSaveVideoDir(ResUtils.getSavePath(ResUtils.DIR_NAME_VIDEO));
			mImageRecordModule.setRecordLevel(level);
			mImageRecordModule.initImageRecord();
		}
	}

	private OnClickListener buttonClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.changePenBut:
				final String[] penWeightItems = { "2个像素", "3个像素", "10个像素", "50个像素" };
				new AlertDialog.Builder(NoteWithActivity.this).setTitle("修改笔粗细")
						.setItems(penWeightItems, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								switch (which) {
								case 0:
									penWeight = 2;
									break;
								case 1:
									penWeight = 3;
									break;
								case 2:
									penWeight = 10;
									break;
								case 3:
									penWeight = 50;
									break;
								default:
									// 刷新画布
									// mPenCanvasView.refresh();
								}
							}
						}).show();

				break;
			case R.id.changePenColorBut:
				final String[] penColorItems = { "红色", "绿色", "蓝色", "黑色" };
				new AlertDialog.Builder(NoteWithActivity.this).setTitle("修改笔颜色")
						.setItems(penColorItems, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								switch (which) {
								case 0:
									penColor = Color.RED;
									break;
								case 1:
									penColor = Color.GREEN;
									break;
								case 2:
									penColor = Color.BLUE;
									break;
								case 3:
									penColor = Color.BLACK;
									break;
								default:
									// 刷新画布
									// mPenCanvasView.refresh();
								}
							}
						}).show();
				break;
			case R.id.saveScreenBut: // 截屏
				AlertDialog.Builder builder = new AlertDialog.Builder(NoteWithActivity.this);
				builder.setMessage("确定要截屏吗？").setCancelable(false)
						.setPositiveButton("确定", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								mImageRecordModule.saveSnapshot();
								dialog.dismiss();
							}
						}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.dismiss();
							}
						});
				builder.create().show();
				break;
			case R.id.insertPhoto: // 插入图片
//				Intent getAlbum = new Intent(Intent.ACTION_GET_CONTENT);
//				getAlbum.setType(IMAGE_TYPE);
//				startActivityForResult(getAlbum, SELECT_PICTURE);
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		        intent.addCategory(Intent.CATEGORY_OPENABLE);
		        intent.setType("image/*");
		        startActivityForResult(Intent.createChooser(intent, "选择图片"), SELECT_PICTURE);
				break;
			case R.id.changeBgBut: // 插入背景
				Intent getAlbum_bg = new Intent(Intent.ACTION_GET_CONTENT);
				getAlbum_bg.setType(IMAGE_TYPE);
				startActivityForResult(getAlbum_bg, SELECT_BG);
				break;
			case R.id.changeBgScaleTypeBut: // 缩放背景
				final String[] items = { "居中", "平铺" };
				new AlertDialog.Builder(NoteWithActivity.this).setTitle("请点击选择")
						.setItems(items, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								switch (which) {
								case 0:
									scaleType = ScaleType.CENTER;
									mPenCanvasView.refresh();
									break;
								case 1:
									scaleType = ScaleType.FIT_XY;
									mPenCanvasView.refresh();
									break;
								default:
									// 刷新画布
									// mPenCanvasView.refresh();
								}
							}
						}).show();
				break;
			case R.id.recordBut: // 录制
				if (butFlag == 0) { // 点击开始录制按钮
					butFlag = 1;// 可以暂停
					((Button) v).setText("暂停");
					mRecordStopBut.setClickable(true);
					mRecordStopBut.setBackgroundColor(Color.LTGRAY);
					mImageRecordModule.startRecord();
				} else if (butFlag == 1) {// 点击暂停按钮
					butFlag = 2;// 可以继续
					((Button) v).setText("继续");
					mImageRecordModule.setIsPause(true);
				} else if (butFlag == 2) {// 点击继续按钮
					butFlag = 1;// 可以暂停
					((Button) v).setText("暂停");
					mImageRecordModule.setIsPause(false);
				}
				break;

			case R.id.mRecordStopBut:
				butFlag = 0;// 可以暂停
				recordBut.setText("开始录制");
				v.setBackgroundColor(Color.GRAY);
				mRecordStopBut.setClickable(false);
				mImageRecordModule.endRecord();
				break;
			default:
				break;
			}
		}
	};

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
		return penColor;
	}

	@Override
	public PenModel getPenModel() {
		return PenModel.WaterPen;
	}

	@Override
	public float getPenWeight() {
		return penWeight;
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
		return mBgUri;
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
		View view =  mPenCanvasView.getDrawAreaView();
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
		mTimeShowDate.setTime(arg0 * 1000);
		String time = mTimeShowformat.format(mTimeShowDate);
		recordBut.setText("暂停" + time);

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
	public void onCanvasSizeChanged(int w, int h, SceneType sceneType) {
		// TODO Auto-generated method stub
			mImageRecordModule.setInputSize(w,h);
		
	}
}
