package cn.robotpen.demo.usb;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Random;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.robotpen.core.module.ImageRecordModule;
import cn.robotpen.core.module.ImageRecordModule.ImageRecordInterface;
import cn.robotpen.core.services.PenService;
import cn.robotpen.core.services.SmartPenService;
import cn.robotpen.core.services.UsbPenService;
import cn.robotpen.core.views.MultipleCanvasView;
import cn.robotpen.core.views.MultipleCanvasView.CanvasManageInterface;
import cn.robotpen.core.views.MultipleCanvasView.PenModel;
import cn.robotpen.demo.PenView;
import cn.robotpen.demo.R;
import cn.robotpen.demo.RobotPenApplication;
import cn.robotpen.demo.R.id;
import cn.robotpen.demo.R.layout;
import cn.robotpen.demo.R.menu;
import cn.robotpen.demo.R.string;
import cn.robotpen.file.services.FileManageService;
import cn.robotpen.model.interfaces.Listeners.OnConnectStateListener;
import cn.robotpen.model.symbol.ConnectState;
import cn.robotpen.model.symbol.Keys;
import cn.robotpen.model.symbol.RecordState;
import cn.robotpen.model.symbol.SceneType;
import cn.robotpen.utils.SystemUtil;

/**
 * 笔信息显示
 * @author Xiaoz
 * @date 2015年6月12日 下午3:34:58
 *
 * Description
 */
public class PenInfo extends Activity implements CanvasManageInterface,ImageRecordInterface{
	public static final String TAG = PenInfo.class.getSimpleName();
	public static final int REQUEST_SETTING_SIZE = 1000;
	 private static final int SELECT_PICTURE = 1001;
	    private static final int SELECT_CAMERA = 1002;
	
	/**笔服务广播处理**/
	private PenServiceReceiver mPenServiceReceiver;
	private PenService mPenService;
	private FrameLayout.LayoutParams mDrawAreaParams;
	private ProgressDialog mProgressDialog;
	private int mShowType = 0;
	private Button mXYBut;
	private Button mLineBut;

	private LinearLayout mXYFrame;
	private RelativeLayout mLineFrame;
	private RelativeLayout mLineWindow;//装载画布
	
	//笔的实际坐标
	private TextView mOriginalX;
	private TextView mOriginalY;
	
	//是否是写入状态
	private TextView mIsRoute;
	private TextView mIsSw1;
	private TextView mPenPressure;
	
	//纸张尺寸
	private TextView mSceneWidth;
	private TextView mSceneHeight;
	private TextView mSceneType;
	
	//纸张坐标
	private TextView mSceneX;
	private TextView mSceneY;
	
	//屏幕坐标
	private TextView mWindowX;
	private TextView mWindowY;

	private Button mStartP2PBut;
	private Button mStartGroupBut;
	private Button mStopLiveBut;
	private Button mRecordStartBut;
	private Button mRecordStopBut;
	private ImageRecordModule mImageRecordModule;
	private int butFlag = 0; //1为可暂停／停止  2为可继续／停止  
	private final String IMAGE_TYPE = "image/*"; 
	private final int IMAGE_CODE = 0; 
	private String path = "/storage/sdcard0/Movies/";
	//更换面板背景
	private Button changeBgBut;
	private Uri bgUri;//接收选择的背景图片
	private Button changeBgScaleTypeBut;
	private ScaleType scaleType;
	private Button saveScreenBut;
	private Button insertPhoto;
	private Uri mInsertPhotoUri;
	/**虚拟用户ID**/
	private String mUserId;
	
	/** 笔画布 **/
	private MultipleCanvasView mPenCanvasView;

	//笔视图
	private PenView mPenView;
	
	//当前设备屏幕宽度
	private int mDisplayWidth;
	//屏幕高度
	private int mDisplayHeight;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//控制屏幕常亮
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		DisplayMetrics metric = new DisplayMetrics();
	    getWindowManager().getDefaultDisplay().getMetrics(metric);
	    mDisplayWidth = metric.widthPixels;  // 屏幕宽度（像素）
	    mDisplayHeight = metric.heightPixels;  // 屏幕高度（像素）
	    
	    mPenService = RobotPenApplication.getInstance().getPenService();
	    mPenService.setSceneType(SceneType.INCH_101);
	    
	    //状态栏+ActionBar+菜单按钮高
		Rect frame = new Rect();  
		getWindow().getDecorView().getWindowVisibleDisplayFrame(frame); 
		int menuHeight = SystemUtil.dip2px(PenInfo.this,120) + frame.top;
		
		Log.v(TAG, "menuHeight:"+menuHeight);
		
		//设置显示区域宽和高
		int frameWidth = mDisplayWidth;
		int frameHeight = mDisplayHeight - menuHeight;

		
		//随机一个用户ID，正式环境上替换为用户ID
		Random rd = new Random();
        mUserId = "u"+ String.valueOf(rd.nextInt(10000));
		mPenService.setUserId(mUserId);
		
		mDrawAreaParams = new FrameLayout.LayoutParams(frameWidth,frameHeight); 
	    
		setContentView(R.layout.activity_info);

        ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
	    
		mXYBut = (Button) findViewById(R.id.xyBut);
		mLineBut = (Button) findViewById(R.id.lineBut);
		
		mXYFrame = (LinearLayout) findViewById(R.id.xyFrame);
		mLineFrame = (RelativeLayout) findViewById(R.id.lineFrame);
		//父节点
		mLineWindow = (RelativeLayout) findViewById(R.id.lineWindow);
		
		mOriginalX = (TextView) findViewById(R.id.originalX);
		mOriginalY = (TextView) findViewById(R.id.originalY);
		mIsRoute = (TextView) findViewById(R.id.isRoute);
		mIsSw1 = (TextView) findViewById(R.id.isSw1);
		mPenPressure = (TextView) findViewById(R.id.penPressure);
		mSceneWidth = (TextView) findViewById(R.id.sceneWidth);
		mSceneHeight = (TextView) findViewById(R.id.sceneHeight);
		mSceneType = (TextView) findViewById(R.id.sceneType);
		mSceneX = (TextView) findViewById(R.id.sceneX);
		mSceneY = (TextView) findViewById(R.id.sceneY);
		mWindowX = (TextView) findViewById(R.id.windowX);
		mWindowY = (TextView) findViewById(R.id.windowY);
		
		mStartP2PBut = (Button) findViewById(R.id.startP2PBut);
		mStartP2PBut.setOnClickListener(startP2P_click);
		
		mStartGroupBut = (Button) findViewById(R.id.startGroupBut);
		mStartGroupBut.setOnClickListener(startGroup_click);
		
		mStopLiveBut = (Button) findViewById(R.id.stopLiveBut);
		mStopLiveBut.setOnClickListener(stopLive_click);
		
		mRecordStartBut = (Button) findViewById(R.id.recordStartBut);
		mRecordStartBut.setOnClickListener(recordStart_click);
		mRecordStopBut = (Button) findViewById(R.id.recordStopBut);
		mRecordStopBut.setOnClickListener(recordStop_click);
		//
		changeBgBut = (Button) findViewById(R.id.changeBgBut);
		changeBgBut.setOnClickListener(bgChange_click);
		changeBgScaleTypeBut = (Button) findViewById(R.id.changeBgScaleTypeBut);
		changeBgScaleTypeBut.setOnClickListener(bgScaleType_click);
		
		saveScreenBut = (Button) findViewById(R.id.saveScreenBut);
		saveScreenBut.setOnClickListener(saveScreen_click);
		
		insertPhoto = (Button) findViewById(R.id.insertPhoto);
		insertPhoto.setOnClickListener(insertPhoto_click);

		//添加笔跟随图标
		mPenView = new PenView(this);
		mLineWindow.addView(mPenView);
		
		//看坐标
		mXYBut.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mShowType = 0;
				initPage();
			}
		});
		
		//看实际画线
		mLineBut.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mShowType = 1;
				initPage();
			}
		});
		
		String address = getIntent().getStringExtra(Keys.KEY_DEVICE_ADDRESS);
		if(address != null && !address.isEmpty()){
			connectDevice(address);
		}else{
			String isUsbSvr = getIntent().getStringExtra(Keys.KEY_VALUE);
			if(isUsbSvr != null && !isUsbSvr.isEmpty() && isUsbSvr.equals(Keys.APP_USB_SERVICE_NAME)){
				((UsbPenService)mPenService).checkDeviceConnect();
				initPage();
			}else{
				alertError("IP address error.");
			}
		}
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pen_info, menu);
        return true;
    }

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
        case android.R.id.home:
            PenInfo.this.finish();
            break;
    	case R.id.action_settings:
    		initPage();
    		break;
    	}
    	return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
    	if(resultCode == RESULT_OK){
    		if(requestCode == REQUEST_SETTING_SIZE){
    			Log.v(TAG, "onActivityResult:"+REQUEST_SETTING_SIZE);
    			initPage();
    		}
    		if(requestCode == 0){
    			bgUri = data.getData();
    		}
    		 mInsertPhotoUri = null;
             if (requestCode == SELECT_PICTURE && data != null) {
                 mInsertPhotoUri = data.getData();
             } else if (requestCode == SELECT_CAMERA) {
                 String photoPath = path + "temp00_00.jpg";
                 mInsertPhotoUri = Uri.fromFile(new File(photoPath));
             }
    	}
	}
    
    @Override
	public void onResume() {
		super.onResume();
		
		if(mPenService != null){
			//设置笔坐标监听
			//mPenService.setOnPointChangeListener(onPointChangeListener);
		}else{
			//注册笔服务通过广播方式发送的笔迹坐标信息
			mPenServiceReceiver = new PenServiceReceiver();
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(Keys.ACTION_SERVICE_SEND_POINT);
			registerReceiver(mPenServiceReceiver, intentFilter);
		}
		if(mPenCanvasView!=null){
		mPenCanvasView.removeAllViews();
		}
		//创建画布
		mPenCanvasView = new MultipleCanvasView(PenInfo.this, this);
		mPenCanvasView.setBackgroundColor(Color.WHITE);
		mLineWindow.addView(mPenCanvasView);
		//获取压缩级别
        int level = RobotPenApplication.getInstance().getRecordLevel();
        //初始化录制工具
        mImageRecordModule = new ImageRecordModule(PenInfo.this);
        //存路径
        mImageRecordModule.setSavePhotoDir(path);
        mImageRecordModule.setSaveVideoDir(path);
        mImageRecordModule.setRecordLevel(level);
        mImageRecordModule.setInputSize(mPenCanvasView.getDrawAreaWidth(), mPenCanvasView.getDrawAreaHeight());
        mImageRecordModule.initImageRecord();
		if(null!=mInsertPhotoUri){
		     mPenCanvasView.insertPhoto(mInsertPhotoUri);
		     mInsertPhotoUri = null;
		}
		if(bgUri!=null){
			mPenCanvasView.refresh();
			bgUri=null;
		}
	}
	
	@Override
	public void onPause(){
		if(mPenService != null){
			//mPenService.setOnPointChangeListener(null);
		}else{
			unregisterReceiver(mPenServiceReceiver);
		}
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		//断开设备
		if(mPenService != null){
			mPenService.disconnectDevice();
		}
		
		super.onDestroy();
	}
	
	private void initPage(){
		if(mShowType == 0){
			mXYBut.setEnabled(false);
			mLineBut.setEnabled(true);
			mXYFrame.setVisibility(View.VISIBLE);
			mLineFrame.setVisibility(View.GONE);
		}else{
			mXYBut.setEnabled(true);
			mLineBut.setEnabled(false);
			mXYFrame.setVisibility(View.GONE);
			mLineFrame.setVisibility(View.VISIBLE);
		}
	}
	
	private void alertError(String msg){
		Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Warning");
        alert.setMessage(msg);
        alert.setPositiveButton("OK",new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				PenInfo.this.finish();
			}
        });
        alert.show();
	}
	
	/**释放progressDialog**/
	protected void dismissProgressDialog(){
		if(mProgressDialog != null){
			if(mProgressDialog.isShowing())
				mProgressDialog.dismiss();
			
			mProgressDialog = null;
		}
	}
	
	private void connectDevice(String address){
		PenService service = RobotPenApplication.getInstance().getPenService();
		if(service != null){
			ConnectState state = ((SmartPenService)service).connectDevice(onConnectStateListener, address);
			if(state != ConnectState.CONNECTING){
				alertError("The pen service connection failure.");
			}else{
				mProgressDialog = ProgressDialog.show(PenInfo.this, "", getString(R.string.connecting), true);
			}
		}
	}
	
	//处理笔服务通过广播方式发送的笔迹坐标信息
	//示例仅用作演示有这个功能，没有特殊需求可删除以下代码
	private class PenServiceReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Keys.ACTION_SERVICE_SEND_POINT)){
				//广播的形式接收笔迹信息
				String pointJson = intent.getStringExtra(Keys.KEY_PEN_POINT);
				if(pointJson != null && !pointJson.isEmpty()){

					Toast.makeText(PenInfo.this, pointJson, Toast.LENGTH_SHORT).show();
				}
				return;
			}
		}
	}
	
	private OnClickListener startGroup_click = new OnClickListener(){
		@Override
		public void onClick(View v) {
			View view = LayoutInflater.from(PenInfo.this).inflate(R.layout.alert_live_group, null);
            final EditText groupIdText = (EditText) view.findViewById(R.id.groupIdText);
            
            AlertDialog.Builder alert = new AlertDialog.Builder(PenInfo.this);
            alert.setTitle("请输入组ID");
            alert.setView(view);
            alert.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(mPenService != null && groupIdText.getText().length() > 0){
						String groupId = groupIdText.getText().toString();
						if(groupId != null && !groupId.isEmpty()){
							//设置组ID，需要在startLive之前设置
							mPenService.setLiveGroupId(groupId);
							mPenService.startLive();
							//设置轨迹接收监听,要在startLive之后设置
					        //mPenService.setOnTrailsClientChangeListener(onTrailsClientChangeListener);
						}
					}
				}
            });
            alert.setNegativeButton(R.string.canceled,null);
            alert.show();
		}
	};
	
	private OnClickListener startP2P_click = new OnClickListener(){
		@Override
		public void onClick(View v) {
			View view = LayoutInflater.from(PenInfo.this).inflate(R.layout.alert_live_p2p, null);
            TextView myIdText = (TextView) view.findViewById(R.id.myIdText);
            final EditText targetIdText = (EditText) view.findViewById(R.id.targetIdText);
            
            myIdText.setText(mUserId);
            
            AlertDialog.Builder alert = new AlertDialog.Builder(PenInfo.this);
            alert.setTitle("请输入对方ID");
            alert.setView(view);
            alert.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(mPenService != null && targetIdText.getText().length() > 0){
						String targetId = targetIdText.getText().toString();
						if(targetId != null && !targetId.isEmpty()){
							//设置目标ID，需要在startLive之前设置
							mPenService.setLiveTargetId(targetId);
							mPenService.startLive();
						}
					}
				}
            });
            alert.setNegativeButton(R.string.canceled,null);
            alert.show();
		}
	};

	private OnClickListener stopLive_click = new OnClickListener(){
		@Override
		public void onClick(View v) {
			if(mPenService != null){
				mPenService.stopLive();
			}
		}
	};
	
	private OnConnectStateListener onConnectStateListener = new OnConnectStateListener(){
		@Override
		public void stateChange(String address,ConnectState state) {
			switch(state){
			case PEN_READY:
				
				break;
			case PEN_INIT_COMPLETE:
				dismissProgressDialog();
				Toast.makeText(PenInfo.this, R.string.connected, Toast.LENGTH_SHORT).show();
				initPage();
				break;
			case CONNECTED:
				
				break;
			case SERVICES_FAIL:
				dismissProgressDialog();
				alertError("The pen service discovery failed.");
				break;
			case CONNECT_FAIL:
				dismissProgressDialog();
				alertError("The pen service connection failure.");
				break;
			case DISCONNECTED:
				dismissProgressDialog();
				Toast.makeText(PenInfo.this, R.string.disconnected, Toast.LENGTH_SHORT).show();
				
				mXYBut.setEnabled(false);
				mLineBut.setEnabled(false);
				break;
			default:
				
				break;
			}
		}
	};
	
	

	/*
	 * (non-Javadoc)
	 * @see cn.robotpen.core.views.MultipleCanvasView.CanvasManageInterface#getBgBitmap()
	 * 录制功能演示
	 */
	private OnClickListener recordStart_click = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			// TODO Auto-generated method stub
			if(butFlag==0){ //点击开始录制按钮
				butFlag = 1;//可以暂停
				((Button)v).setText("暂停");
				mRecordStopBut.setClickable(true);
				mRecordStopBut.setBackgroundColor(Color.LTGRAY);
		        mImageRecordModule.startRecord();
			}else if(butFlag==1){//点击暂停按钮
				butFlag = 2;//可以继续
				((Button)v).setText("继续");
				mImageRecordModule.setIsPause(true);
			}else if(butFlag==2){//点击继续按钮
				butFlag = 1;//可以暂停
				((Button)v).setText("暂停");
				mImageRecordModule.setIsPause(false);
			}
			
		}
	};
	
	private OnClickListener recordStop_click = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				butFlag = 0;//可以暂停
				mRecordStartBut.setText("结束录制");
				v.setBackgroundColor(Color.GRAY);
				mRecordStopBut.setClickable(false);
				mImageRecordModule.endRecord();
			}
		};
		/**
		 * 更换背景
		 * **/
		private OnClickListener bgChange_click = new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent getAlbum = new Intent(Intent.ACTION_GET_CONTENT); 
				getAlbum.setType(IMAGE_TYPE); 
				startActivityForResult(getAlbum, IMAGE_CODE); 
				
			}
		};
		/**
		 * 缩放背景
		 * **/
		private OnClickListener bgScaleType_click = new OnClickListener() {
			@Override
			public void onClick(View v) {
				final String[] items = {"居中","平铺"};  
                new AlertDialog.Builder(PenInfo.this)  
                        .setTitle("请点击选择")  
                        .setItems(items, new DialogInterface.OnClickListener() {

							@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							switch (which) {
							case 0:
								scaleType = ScaleType.CENTER;
								//刷新画布
				    			mPenCanvasView.refresh();
								break;
							case 1:
								scaleType = ScaleType.FIT_XY;
								//刷新画布
				    			mPenCanvasView.refresh();
								break;
							default:
								//刷新画布
				    			mPenCanvasView.refresh();
							}
						}
					}).show();
            
			}
		};
		
		/**
		 * 截屏saveScreen_click
		 * */
		private OnClickListener saveScreen_click = new OnClickListener() {
			@Override
			public void onClick(View v) {
				 mImageRecordModule.saveSnapshot();
			}
		};
		
		/***
		 * insertPhoto_click
		 */
		private OnClickListener insertPhoto_click = new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent getAlbum = new Intent(Intent.ACTION_GET_CONTENT); 
				getAlbum.setType(IMAGE_TYPE); 
				startActivityForResult(getAlbum,SELECT_PICTURE); 
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
		//FIXME
		

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
			 mPenCanvasView.setDrawingCacheEnabled(true);
			 mPenCanvasView.buildDrawingCache();
	            Bitmap imageCache = mPenCanvasView.getDrawingCache();
	            if (imageCache != null) {
	                result = imageCache.getByteCount();
	                imageCache.copyPixelsToBuffer(buffer);
	            }
	            mPenCanvasView.setDrawingCacheEnabled(false);
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
			if (progress > 100) { //progress=100 表示正在压制  progress>100 表示录制成功
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
			return  RobotPenApplication.getInstance().getFileService();
		}

		@Override
		public PenService getPenService() {
			// TODO Auto-generated method stub
			return mPenService;
		}

		@Override
		public Uri getBgPhoto() {
			// TODO Auto-generated method stub
			return bgUri;
		}

		@Override
		public float getIsRubber() {
			// TODO Auto-generated method stub
			return 0.0f;
		}
}
	 
