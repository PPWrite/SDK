package cn.robotpen.demo.show;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.robotpen.core.PenManage;
import cn.robotpen.core.module.NoteManageModule;
import cn.robotpen.core.services.PenService;
import cn.robotpen.core.services.SmartPenService;
import cn.robotpen.core.widget.PenDrawView;
import cn.robotpen.core.widget.WhiteBoardView;
import cn.robotpen.demo.R;
import cn.robotpen.demo.RobotPenApplication;
import cn.robotpen.demo.connect.DeviceActivity;
import cn.robotpen.demo.utils.ResUtils;
import cn.robotpen.model.entity.DeviceEntity;
import cn.robotpen.model.entity.NoteEntity;
import cn.robotpen.model.symbol.ConnectState;
import cn.robotpen.model.symbol.DeviceType;
import cn.robotpen.model.symbol.Keys;
import cn.robotpen.model.symbol.SceneType;
import cn.robotpen.utils.FileUtils;
import cn.robotpen.utils.StringUtil;

public class SingleCanvasActivity extends Activity implements WhiteBoardView.CanvasManageInterface {

    @BindView(R.id.activity_single_canvas)
    RelativeLayout activitySingleCanvas;
    @BindView(R.id.whiteBoardView)
    WhiteBoardView whiteBoardView;
    @BindView(R.id.clearnScreen)
    Button clearnScreen;
    PenManage mPenManage;
    PenDrawView.PenModel mPenModel = PenDrawView.PenModel.Pen;
    String mNoteKey = NoteEntity.KEY_NOTEKEY_TMP;
    final static String KEY_NOTEKEY = "NoteKey";
    ProgressDialog mProgressDialog;
    Handler mHandler;
    NoteManageModule mNoteManageModule;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_canvas);
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
        whiteBoardView.beginPage();//xml 创建的画布必须刷新一次
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkDeviceConnStatus(); //检查设备连接状态
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (whiteBoardView != null)
            whiteBoardView.dispose(); //退出Activity时将服务释放，方便其他地方继续使用
    }

    @OnClick(R.id.clearnScreen)
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clearnScreen:
                whiteBoardView.cleanScreen();
                break;
        }
    }

    /*
     * 初始化画布
     */
    protected void initView() {
        whiteBoardView.setDaoSession(RobotPenApplication.getInstance().getDaoSession());
        whiteBoardView.setIsTouchWrite(true);
        //获取关键服务
        mPenManage = whiteBoardView.getPenManage();
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
                DeviceEntity lastDevice = PenManage.getLastDevice(SingleCanvasActivity.this);
                if (lastDevice == null || TextUtils.isEmpty(lastDevice.getAddress())) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(SingleCanvasActivity.this);
                    alert.setTitle("提示");
                    alert.setMessage("暂未连接设备，请先连接设备！");
                    alert.setPositiveButton(R.string.canceled, null);
                    alert.setNegativeButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Intent intent = new Intent(SingleCanvasActivity.this, DeviceActivity.class);
                            SingleCanvasActivity.this.startActivity(intent);
                            SingleCanvasActivity.this.finish();
                        }
                    });
                    alert.show();
                } else {
                    mPenManage.scanDevice(onScanDeviceListener);
                }
            } else {
                mPenManage.scanDevice(null);
            }
        }else{ //已成功连接设备
            Toast.makeText(SingleCanvasActivity.this,"设备连接成功",Toast.LENGTH_LONG).show();
        }
    }

    /*
    *扫描监听
     */
    PenService.OnScanDeviceListener onScanDeviceListener = new PenService.OnScanDeviceListener() {
        @Override
        public void find(DeviceEntity deviceObject) {
            DeviceEntity lastDevice = mPenManage.getLastDevice(SingleCanvasActivity.this);
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
                Toast.makeText(SingleCanvasActivity.this, "暂未发现设备", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void status(int i) {
            switch (i) {
                case Keys.REQUEST_ENABLE_BT:
                    Toast.makeText(SingleCanvasActivity.this, "蓝牙未打开", Toast.LENGTH_SHORT).show();
                    Intent req_ble = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(req_ble, Keys.REQUEST_ENABLE_BT);
                    break;
                case Keys.BT_ENABLE_ERROR:
                    Toast.makeText(SingleCanvasActivity.this, "设备不支持BLE协议", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(SingleCanvasActivity.this, "设备已连接且连接成功！", Toast.LENGTH_SHORT).show();
                mPenManage.setSceneObject(SceneType.getSceneType(false,mPenManage.getConnectDeviceType()));
                //刷新当前临时笔记
                whiteBoardView.refresh();
            } else if (arg1 == ConnectState.DISCONNECTED) {
                Toast.makeText(SingleCanvasActivity.this, "设备已断开", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    public PenDrawView.PenModel getPenModel() {
        return mPenModel;
    }

    @Override
    public float getPenWeight() {
        return 2;
    }

    @Override
    public int getPenColor() {
        return Color.BLUE;
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
                if (whiteBoardView.getNoteEntity() != null
                        && NoteEntity.KEY_NOTEKEY_TMP.equals(whiteBoardView.getNoteEntity().getNoteKey())) {
                    //如果是，那么将临时笔记另存，并标识设备类型
                    mNoteKey = DeviceType.toDeviceType(whiteBoardView.getNoteEntity().getDeviceType()).getDeviceIdent()
                            + FileUtils.getDateFormatName("yyyyMMdd_HHmmss");
                    mNoteManageModule.tmpSaveToNote(mNoteKey,getCurrUserId(), ResUtils.DIR_NAME_DATA);
                    //刷新当前临时笔记
                    whiteBoardView.refresh();
                } else {
                    Toast.makeText(SingleCanvasActivity.this,"设备错误",Toast.LENGTH_LONG).show();
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
