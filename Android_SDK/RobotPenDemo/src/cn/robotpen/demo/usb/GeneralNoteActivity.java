package cn.robotpen.demo.usb;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import android.widget.Toast;

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

public class GeneralNoteActivity extends Activity implements CanvasManageInterface, ImageRecordInterface {
	private ProgressDialog mProgressDialog;
	private PenService mPenService;
	private Handler mHandler = new Handler();

	// 页面元素
	private RelativeLayout lineWindow;
	private MultipleCanvasView mPenCanvasView;
	private TextView penB;
	private TextView penWeightB;
	private TextView penColorB;
	private TextView eraserB;
	private TextView cleanB;
	private TextView innerB;
	private ImageView penStatusImg;
	private ImageButton backB;
	private ImageButton saveB;
	private float mPenWeight = 2;
	private int mPenColor = 0xFF000000;
	private float mRubberStatus = 0;
	private static final int SELECT_PICTURE = 1001;
	private static final int SELECT_CAMERA = 1002;
	private Uri mInsertPhotoUri;
	public static final String TMP_CAMERA_IMAGE_NAME = "tmp_camera_img.jpg";
	private ImageRecordModule mImageRecordModule;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 控制屏幕常亮
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
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
			mPenService.setOnConnectStateListener(null);
            mPenService.setOnUploadFirmwareCallback(null);
            mPenService.setOnReceiveDataListener(null);
            mPenService.setOnPointChangeListener(null);
		}
		RobotPenApplication.getInstance().unBindPenService();
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
			//mPenService.setSceneType(SceneType.INCH_101_horizontal);// 设置场景值，用于坐标转化
			mPenService.setIsHorizontal(true);
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
			if (arg1 == ConnectState.CONNECTED) {
				Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pan_ic_down);
				penStatusImg.setImageBitmap(bitmap);
				// if (bitmap != null && !bitmap.isRecycled())
				// {
				// bitmap.recycle();
				// }
				penStatusImg.setClickable(false);
			} else {
				Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pan_ic_up);
				penStatusImg.setImageBitmap(bitmap);
				// if (bitmap != null && !bitmap.isRecycled())
				// {
				// bitmap.recycle();
				// }
				penStatusImg.setClickable(true);
			}
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			innerB.setTextColor(Color.BLACK);
			System.gc();
			mInsertPhotoUri = null;
			if (requestCode == SELECT_PICTURE && data != null) {
				mInsertPhotoUri = data.getData();
			} else if (requestCode == SELECT_CAMERA) {
				String photoPath = ResUtils.getSavePath(ResUtils.DIR_NAME_BUFFER) + TMP_CAMERA_IMAGE_NAME;
				mInsertPhotoUri = Uri.fromFile(new File(photoPath));
			}
			if (mInsertPhotoUri != null) {
				if (mPenService != null) {
					// 如果penService不为null,那么说明activity没有执行onStop.
					mPenCanvasView.insertPhoto(mInsertPhotoUri);
					mInsertPhotoUri = null;
				}
			}
		}
	}

	/*
	 * 界面初始化
	 */
	void initUI() {
		lineWindow = (RelativeLayout) findViewById(R.id.lineWindow);
		penB = (TextView) findViewById(R.id.penB);
		penB.setTextColor(Color.RED);
		penB.setOnClickListener(butClick);
		penWeightB = (TextView) findViewById(R.id.penWeightB);
		penWeightB.setOnClickListener(butClick);
		penColorB = (TextView) findViewById(R.id.penColorB);
		penColorB.setOnClickListener(butClick);
		eraserB = (TextView) findViewById(R.id.eraserB);
		eraserB.setOnClickListener(butClick);
		cleanB = (TextView) findViewById(R.id.cleanB);
		cleanB.setOnClickListener(butClick);
		innerB = (TextView) findViewById(R.id.innerB);
		innerB.setOnClickListener(butClick);
		penStatusImg = (ImageView) findViewById(R.id.penStatusImg);
		penStatusImg.setOnClickListener(butClick);
		backB = (ImageButton) findViewById(R.id.backB);
		backB.setOnClickListener(butClick);
		saveB = (ImageButton) findViewById(R.id.saveB);
		saveB.setOnClickListener(butClick);
	}

	void initCanvas() {
		mPenCanvasView = new MultipleCanvasView(GeneralNoteActivity.this, this);
		lineWindow.addView(mPenCanvasView);
		mPenCanvasView.setPenIcon(R.drawable.ic_pen);
		mPenCanvasView.refresh();
		penWeightB.setText("笔粗细" + "_" + mPenWeight);
		penColorB.setText("笔颜色" + "_" + mPenColor);
		// 先判断文件夹是否创建
		if (ResUtils.isDirectory(ResUtils.DIR_NAME_BUFFER) && ResUtils.isDirectory(ResUtils.DIR_NAME_PHOTO)
				&& ResUtils.isDirectory(ResUtils.DIR_NAME_VIDEO) && ResUtils.isDirectory(ResUtils.DIR_NAME_DATA)) {
			// 获取压缩级别
			int level = RobotPenApplication.getInstance().getRecordLevel();
			// 初始化录制工具
			mImageRecordModule = new ImageRecordModule(GeneralNoteActivity.this);
			mImageRecordModule.setSavePhotoDir(ResUtils.getSavePath(ResUtils.DIR_NAME_PHOTO));
			mImageRecordModule.setSaveVideoDir(ResUtils.getSavePath(ResUtils.DIR_NAME_VIDEO));
			mImageRecordModule.setRecordLevel(level);
			mImageRecordModule.initImageRecord();
		}
	}

	private View.OnClickListener butClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.penB:
				penB.setTextColor(Color.RED);
				eraserB.setTextColor(Color.BLACK);
				mRubberStatus = 0;
				break;
			case R.id.eraserB:
				penB.setTextColor(Color.BLACK);
				eraserB.setTextColor(Color.RED);
				mRubberStatus = mPenWeight * 2;
				break;
			case R.id.cleanB:
				cleanB.setTextColor(Color.RED);
				AlertDialog.Builder builder = new AlertDialog.Builder(GeneralNoteActivity.this);
				builder.setMessage("确定要清屏吗？").setCancelable(false)
						.setPositiveButton("确定", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								mPenCanvasView.cleanScreen();
								cleanB.setTextColor(Color.BLACK);
								dialog.dismiss();
							}
						}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								cleanB.setTextColor(Color.BLACK);
								dialog.dismiss();
							}
						});
				builder.create().show();
				break;
			case R.id.innerB: // 插入图片
				innerB.setTextColor(Color.RED);
				final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				intent.setType("image/*");
				final String[] innerFrom = { "从图片中选择", "拍摄一张" };
				new AlertDialog.Builder(GeneralNoteActivity.this).setTitle("插入图片")
						.setItems(innerFrom, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								switch (which) {
								case 0:
									startActivityForResult(Intent.createChooser(intent, "选择图片"), SELECT_PICTURE);
									break;
								case 1:
									String outPath = ResUtils.getSavePath(ResUtils.DIR_NAME_BUFFER)
											+ TMP_CAMERA_IMAGE_NAME;
									File outFile = new File(outPath);
									if (!outFile.exists()) {
										try {
											outFile.createNewFile();
										} catch (IOException e) {
											e.printStackTrace();
										}
									}
									Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
									intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(outFile));
									startActivityForResult(intent, SELECT_CAMERA);
									break;
								default:
									break;
								}
							}
						}).show();

				break;
			case R.id.penWeightB:
				penWeightB.setTextColor(Color.YELLOW);
				final String[] penWeightItems = { "2个像素", "3个像素", "10个像素", "50个像素" };
				new AlertDialog.Builder(GeneralNoteActivity.this).setTitle("修改笔粗细")
						.setItems(penWeightItems, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								switch (which) {
								case 0:
									mPenWeight = 2;
									break;
								case 1:
									mPenWeight = 3;
									break;
								case 2:
									mPenWeight = 10;
									break;
								case 3:
									mPenWeight = 50;
									break;
								default:
									break;
								}
								penWeightB.setText("笔粗细" + "_" + mPenWeight);
							}
						}).show();

				break;
			case R.id.penColorB:
				penColorB.setTextColor(Color.YELLOW);
				final String[] penColorItems = { "红色", "绿色", "蓝色", "黑色" };
				new AlertDialog.Builder(GeneralNoteActivity.this).setTitle("修改笔颜色")
						.setItems(penColorItems, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								switch (which) {
								case 0:
									mPenColor = Color.RED;
									break;
								case 1:
									mPenColor = Color.GREEN;
									break;
								case 2:
									mPenColor = Color.BLUE;
									break;
								case 3:
									mPenColor = Color.BLACK;
									break;
								default:
									break;
								}
								penColorB.setText("笔颜色" + "_" + mPenColor);
							}
						}).show();
				break;
			case R.id.saveB: // 截屏
				AlertDialog.Builder builderSave = new AlertDialog.Builder(GeneralNoteActivity.this);
				builderSave.setMessage("确定要截屏吗？").setCancelable(false)
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
				builderSave.create().show();
				break;
			case R.id.backB:
				GeneralNoteActivity.this.finish();
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
		return 0xFFFFFFFF;
	}

	@Override
	public int getBgResId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getCurrUserId() {
		return "";
	}

	@Override
	public float getIsRubber() {
		return mRubberStatus;
	}

	@Override
	public int getPenColor() {
		return mPenColor;
	}

	@Override
	public PenModel getPenModel() {
		return PenModel.WaterPen;
	}

	@Override
	public float getPenWeight() {
		return mPenWeight;
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

	}

	@Override
	public void recordWarning(RecordState arg0) {

	}

	@Override
	public void videoCodeState(int progress) {
		if (progress > 100) {
			Toast.makeText(GeneralNoteActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
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
		mImageRecordModule.setInputSize(w, h);
	}
}
