package cn.robotpen.demo.show;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.robotpen.core.PenManage;
import cn.robotpen.core.module.NoteManageModule;
import cn.robotpen.core.services.PenService;
import cn.robotpen.core.services.SmartPenService;
import cn.robotpen.core.widget.PenDrawView;
import cn.robotpen.core.widget.RecordBoardView;
import cn.robotpen.core.widget.WhiteBoardView;
import cn.robotpen.demo.R;
import cn.robotpen.demo.RobotPenApplication;
import cn.robotpen.demo.connect.DeviceActivity;
import cn.robotpen.demo.utils.ResUtils;
import cn.robotpen.model.entity.DeviceEntity;
import cn.robotpen.model.entity.NoteEntity;
import cn.robotpen.model.entity.SettingEntity;
import cn.robotpen.model.symbol.ConnectState;
import cn.robotpen.model.symbol.DeviceType;
import cn.robotpen.model.symbol.Keys;
import cn.robotpen.model.symbol.RecordState;
import cn.robotpen.model.symbol.SceneType;
import cn.robotpen.utils.FileUtils;
import cn.robotpen.utils.StringUtil;

public class SingleWithMethodActivity extends Activity
        implements
        RecordBoardView.CanvasManageInterface,
        RecordBoardView.RecordManageListener {

    @BindView(R.id.recordBoardView)
    RecordBoardView recordBoardView;
    @BindView(R.id.viewWindow)
    RelativeLayout viewWindow;

    @BindView(R.id.changePenBut)
    Button changePenBut;
    @BindView(R.id.changePenColorBut)
    Button changePenColorBut;
    @BindView(R.id.cleanLineBut)
    Button cleanLineBut;
    @BindView(R.id.cleanPhotoBut)
    Button cleanPhotoBut;
    @BindView(R.id.cleanScreenBut)
    Button cleanScreenBut;
    @BindView(R.id.innerPhotoBut)
    Button innerPhotoBut;
    @BindView(R.id.photoScaleTypeBut)
    Button photoScaleTypeBut;
    @BindView(R.id.removePhotoBut)
    Button removePhotoBut;
    @BindView(R.id.innerbgBut)
    Button innerbgBut;
    @BindView(R.id.bgScaleTypeBut)
    Button bgScaleTypeBut;
    @BindView(R.id.removeBgBut)
    Button removeBgBut;
    @BindView(R.id.saveScreenBut)
    Button saveScreenBut;
    @BindView(R.id.recordBut)
    Button recordBut;
    @BindView(R.id.recordStopBut)
    Button recordStopBut;

    SimpleDateFormat mTimeShowformat = new SimpleDateFormat("HH:mm:ss");
    ProgressDialog mProgressDialog;
    Handler mHandler;
    PenManage mPenManage;
    PenDrawView.PenModel mPenModel = PenDrawView.PenModel.Pen; //只能默认为pen模式
    float mPenWeight = 2;//默认笔宽度是2像素
    int mPenColor = Color.BLUE;//默认为蓝色
    String mNoteKey = NoteEntity.KEY_NOTEKEY_TMP;//默认为临时笔记
    final static String KEY_NOTEKEY = "NoteKey";
    static final int SELECT_PICTURE = 1001;
    static final int SELECT_BG = 1002;
    Uri mInsertPhotoUri = null;
    Uri mBgUri = null;
    int butFlag = 0;
    NoteManageModule mNoteManageModule;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_withmethod);
        ButterKnife.bind(this);
        mHandler = new Handler();
        mNoteManageModule = new NoteManageModule(this);
        initView();
    }

    @Override
    public void onStart() {
        super.onStart();
        String noteKey = getIntent().getStringExtra(KEY_NOTEKEY);
        if (!TextUtils.isEmpty(noteKey)) {
            mNoteKey = noteKey;
        }
        recordBoardView.beginPage();//xml创建的画布必须刷新一次
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkDeviceConnStatus(); //检查设备连接状态
        checkIntentInsertPhoto();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (recordBoardView != null)
            recordBoardView.dispose(); //退出Activity时将服务释放，方便其他地方继续使用
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            mInsertPhotoUri = null;
            mBgUri = null;
            if (requestCode == SELECT_PICTURE && data != null) {
                mInsertPhotoUri = data.getData();
            }
            if (requestCode == SELECT_BG && data != null) {
                mBgUri = data.getData();
            }
        }
    }

    /**
     * 检查是否有Intent传入需要插入的图片
     */
    public void checkIntentInsertPhoto() {
        //检查是否有需要插入的图片uri
        if (null != mInsertPhotoUri) {
            recordBoardView.insertPhoto(mInsertPhotoUri);
            mInsertPhotoUri = null;
        }
        if(null != mBgUri){
            recordBoardView.setBgPhoto(mBgUri);
            mBgUri = null;
        }
    }


    @OnClick({ R.id.changePenBut, R.id.changePenColorBut
    ,R.id.cleanLineBut,R.id.cleanPhotoBut,R.id.cleanScreenBut
    ,R.id.innerPhotoBut,R.id.photoScaleTypeBut,R.id.removePhotoBut
    ,R.id.innerbgBut,R.id.bgScaleTypeBut,R.id.removeBgBut
    ,R.id.saveScreenBut
    ,R.id.recordBut,R.id.recordStopBut
    })
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.changePenBut: //更改笔粗细
                final String[] penWeightItems = {"2个像素", "3个像素", "10个像素", "50个像素"};
                new AlertDialog.Builder(SingleWithMethodActivity.this).setTitle("修改笔粗细")
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
                                }
                            }
                        }).show();

                break;
            case R.id.changePenColorBut:
                final String[] penColorItems = {"红色", "绿色", "蓝色", "黑色"};
                new AlertDialog.Builder(SingleWithMethodActivity.this).setTitle("修改笔颜色")
                        .setItems(penColorItems, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
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
                                }
                            }
                        }).show();
                break;
            case R.id.cleanLineBut:
                recordBoardView.cleanTrail();
                break;
            case R.id.cleanPhotoBut:
                recordBoardView.cleanPhoto();
                break;
            case R.id.cleanScreenBut:
                recordBoardView.cleanScreen();
                break;
            case R.id.innerPhotoBut:
                recordBoardView.setDataSaveDir(ResUtils.getSavePath(ResUtils.DIR_NAME_DATA));
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(Intent.createChooser(intent, "选择图片"), SELECT_PICTURE);
                //支持多个图片的插入 所以插入图片成功后需要改变序号
                break;
            case R.id.photoScaleTypeBut:
                recordBoardView.setPhotoScaleType(ImageView.ScaleType.CENTER_CROP);
                break;
            case R.id.removePhotoBut:
                recordBoardView.delCurrEditPhoto();
                break;
            case R.id.innerbgBut:
                recordBoardView.setDataSaveDir(ResUtils.getSavePath(ResUtils.DIR_NAME_DATA));
                recordBoardView.startPhotoEdit(true); //设置图片可以编辑状态
                Intent intent2 = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(Intent.createChooser(intent2, "选择背景"), SELECT_BG);
                break;
            case R.id.bgScaleTypeBut:
                recordBoardView.setBgScaleType(ImageView.ScaleType.CENTER_CROP);
                break;
            case R.id.removeBgBut:
                mBgUri=null;
                recordBoardView.setBgPhoto(null);
                break;
            case R.id.saveScreenBut:
                recordBoardView.setSaveSnapshotDir(ResUtils.getSavePath(ResUtils.DIR_NAME_PHOTO));//设置存储路径
                recordBoardView.saveSnapshot();
                break;
            case R.id.recordBut:
                recordBoardView.setSaveVideoDir(ResUtils.getSavePath(ResUtils.DIR_NAME_VIDEO));//设置存储路径
                if (butFlag == 0) { // 点击开始录制按钮
                    butFlag = 1;// 可以暂停
                    ((Button) v).setText("暂停");
                    recordStopBut.setClickable(true);
                    recordStopBut.setBackgroundColor(Color.DKGRAY);
                    recordBoardView.startRecord();
                } else if (butFlag == 1) {// 点击暂停按钮
                    butFlag = 2;// 可以继续
                    ((Button) v).setText("继续");
                    recordBoardView.setIsPause(true);
                } else if (butFlag == 2) {// 点击继续按钮
                    butFlag = 1;// 可以暂停
                    ((Button) v).setText("暂停");
                    recordBoardView.setIsPause(false);
                }
                break;
            case R.id.recordStopBut:
                butFlag = 0;// 可以暂停
                recordBut.setText("录制");
                v.setBackgroundColor(Color.GRAY);
                ((Button) v).setClickable(false);
                recordBoardView.endRecord();
                break;

        }
    }


    /*
     * 初始化画布
     */
    protected void initView() {
        recordBoardView.setDaoSession(RobotPenApplication.getInstance().getDaoSession());
        recordBoardView.setIsTouchWrite(true);
        //获取关键服务
        mPenManage = recordBoardView.getPenManage();
        //设置状态监听
        mPenManage.setOnConnectStateListener(onConnectStateListener);
    }

    /*
    * 检测设备连接
    */
    public void checkDeviceConnStatus() {
        DeviceEntity deviceEntity = mPenManage.getConnectDevice();
        if (null == deviceEntity) {
            //判断蓝牙还是USB服务
            if (SmartPenService.TAG.equals(mPenManage.getSvrTag())) {
                //检查以前是否有连接过设备
                DeviceEntity lastDevice = PenManage.getLastDevice(SingleWithMethodActivity.this);
                if (lastDevice == null || TextUtils.isEmpty(lastDevice.getAddress())) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(SingleWithMethodActivity.this);
                    alert.setTitle("提示");
                    alert.setMessage("暂未连接设备，请先连接设备！");
                    alert.setPositiveButton(R.string.canceled, null);
                    alert.setNegativeButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Intent intent = new Intent(SingleWithMethodActivity.this, DeviceActivity.class);
                            SingleWithMethodActivity.this.startActivity(intent);
                            SingleWithMethodActivity.this.finish();
                        }
                    });
                    alert.show();
                } else {
                    mPenManage.scanDevice(onScanDeviceListener);
                }
            } else {
                mPenManage.scanDevice(null);
            }
        } else { //已成功连接设备
            Toast.makeText(SingleWithMethodActivity.this, "设备连接成功", Toast.LENGTH_LONG).show();
        }
    }

    /*
    *扫描监听
     */
    PenService.OnScanDeviceListener onScanDeviceListener = new PenService.OnScanDeviceListener() {
        @Override
        public void find(DeviceEntity deviceObject) {
            DeviceEntity lastDevice = mPenManage.getLastDevice(SingleWithMethodActivity.this);
            if (!StringUtil.isEmpty(lastDevice.getAddress())) {
                if (deviceObject.getAddress().equals(lastDevice.getAddress())) {
                    mPenManage.stopScanDevice();
                    mPenManage.connectDevice(onConnectStateListener, lastDevice.getAddress());
                }
            }
        }

        @Override
        public void complete(HashMap<String, DeviceEntity> hashMap) {
            if (!mPenManage.getIsStartConnect()) {
                Toast.makeText(SingleWithMethodActivity.this, "暂未发现设备", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void status(int i) {
            switch (i) {
                case Keys.REQUEST_ENABLE_BT:
                    Toast.makeText(SingleWithMethodActivity.this, "蓝牙未打开", Toast.LENGTH_SHORT).show();
                    Intent req_ble = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(req_ble, Keys.REQUEST_ENABLE_BT);
                    break;
                case Keys.BT_ENABLE_ERROR:
                    Toast.makeText(SingleWithMethodActivity.this, "设备不支持BLE协议", Toast.LENGTH_SHORT).show();
                    break;
            }

        }
    };
    /*
    * 此处监听是为了弹出授权
    */
    private PenService.OnConnectStateListener onConnectStateListener = new PenService.OnConnectStateListener() {
        @Override
        public void stateChange(String arg0, ConnectState arg1) {
            if (arg1 == ConnectState.CONNECTED) {
                Toast.makeText(SingleWithMethodActivity.this, "设备已连接且连接成功！", Toast.LENGTH_SHORT).show();
                mPenManage.setSceneObject(SceneType.getSceneType(false, mPenManage.getConnectDeviceType()));
                //刷新当前临时笔记
                recordBoardView.refresh();
            } else if (arg1 == ConnectState.DISCONNECTED) {
                Toast.makeText(SingleWithMethodActivity.this, "设备已断开", Toast.LENGTH_SHORT).show();
            }
        }
    };

    /*
    *最基本的画布必须实现的方法
     */
    @Override
    public PenDrawView.PenModel getPenModel() {
        return mPenModel;
    }

    @Override
    public float getPenWeight() {
        return mPenWeight;
    }

    @Override
    public int getPenColor() {
        return mPenColor;
    }

    @Override
    public float getIsRubber() {
        return 0;
    }

    @Override
    public boolean getIsPressure() {
        return false;
    }

    @Override
    public boolean getIsHorizontal() {
        return false;
    }

    @Override
    public long getCurrUserId() {
        return 0;
    }

    @Override
    public String getNoteKey() {
        return mNoteKey;
    }

    @Override
    public void onEvent(WhiteBoardView.BoardEvent boardEvent) {
        switch (boardEvent) {
            case PEN_DOWN:
                break;
            case PEN_UP:
                break;
            case TRAILS_LOADING:
                mProgressDialog = ProgressDialog.show(this, "", "加载中……", true);
                break;
            case TRAILS_COMPLETE:
                dismissProgressDialog();
                break;
            case WRITE_BAN:
                break;
            case ERROR_BOARD_PEN_VIEW:
                break;
            case ERROR_DEVICE_TYPE:
                //检查当前是否是临时笔记
                if (recordBoardView.getNoteEntity() != null
                        && NoteEntity.KEY_NOTEKEY_TMP.equals(recordBoardView.getNoteEntity().getNoteKey())) {
                    //如果是，那么将临时笔记另存，并标识设备类型
                    mNoteKey = DeviceType.toDeviceType(recordBoardView.getNoteEntity().getDeviceType()).getDeviceIdent()
                            + FileUtils.getDateFormatName("yyyyMMdd_HHmmss");
                    mNoteManageModule.tmpSaveToNote(mNoteKey,getCurrUserId(), ResUtils.DIR_NAME_DATA);
                    //刷新当前临时笔记
                    recordBoardView.refresh();
                } else {
                    Toast.makeText(SingleWithMethodActivity.this, "设备错误", Toast.LENGTH_LONG).show();
                }
                break;
            case ERROR_SCENE_TYPE:
                break;
        }

    }

    @Override
    public boolean onMessage(String s) {
        return false;
    }


    /*
    *录制时必须实现的方法
     */
    @Override
    public int getRecordLevel() {
        SettingEntity mSettingEntity = new SettingEntity(this);
        return mSettingEntity.getVideoQualityValue(); //录制质量
    }

    @Override
    public void onRecordButClick(int i) {
        switch (i) {
            case RecordBoardView.EVENT_CONFIRM_EXT_CLICK:
                //MyLocalVideoActivity.launch(getContext());
                break;
        }

    }

    @Override
    public void onRecordError(int i) {

    }

    @Override
    public boolean onRecordState(RecordState recordState, String s) {
        switch (recordState) {
            case START:
                break;
            case END:
                break;
            case PAUSE:
                break;
            case CONTINUE:
                break;
            case SAVING:
                break;
            case CODING:
                break;
            case COMPLETE:
                break;
            case ERROR:
                break;
            case RESISTANCE:
                break;
        }
        return true;
    }

    @Override
    public boolean onRecordTimeChange(Date date) {
       // String time = mTimeShowformat.format(date);
        //recordBut.setText("暂停" + time);
        return false;//自动显示时间
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
