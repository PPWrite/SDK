package cn.robotpenDemo.board.show;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.robotpen.core.widget.RecordBoardView;
import cn.robotpen.core.widget.WhiteBoardView;
import cn.robotpen.model.DevicePoint;
import cn.robotpen.model.entity.SettingEntity;
import cn.robotpen.model.entity.note.NoteEntity;
import cn.robotpen.model.symbol.DeviceType;
import cn.robotpen.model.symbol.RecordState;
import cn.robotpen.pen.callback.PenPositionAndEventCallback;
import cn.robotpen.pen.callback.RobotPenActivity;
import cn.robotpen.pen.model.RemoteState;
import cn.robotpen.pen.model.RobotDevice;
import cn.robotpenDemo.board.MyApplication;
import cn.robotpenDemo.board.R;
import cn.robotpenDemo.board.common.BaseConnectPenServiceActivity;
import cn.robotpenDemo.board.common.ResUtils;

public class SettingExampleActivity extends RobotPenActivity
        implements WhiteBoardView.CanvasManageInterface,
        RecordBoardView.RecordManageListener {

    DeviceType mDeDeviceType = DeviceType.P1;//默认连接设备为P1 当与连接设备有冲突时则需要进行切换
    float isRubber = 0;//是否是橡皮
    ProgressDialog mProgressDialog;
    SettingEntity mSettingEntity;
    Handler mHandler;
    float mPenWeight = 2;//默认笔宽度是2像素
    int mPenColor = Color.BLACK;//默认为黑色
    String mNoteKey = NoteEntity.KEY_NOTEKEY_TMP;//默认为临时笔记
    int butFlag = 0;
    boolean mDirection;
    @BindView(R.id.recordBoardView)
    RecordBoardView recordBoardView;
    @BindView(R.id.viewWindow)
    RelativeLayout viewWindow;
    @BindView(R.id.saveScreenBut)
    Button saveScreenBut;
    @BindView(R.id.recordBut)
    Button recordBut;
    @BindView(R.id.recordStopBut)
    Button recordStopBut;
    @BindView(R.id.delPageBut)
    Button delPageBut;
    @BindView(R.id.gotoProBut)
    Button gotoProBut;
    @BindView(R.id.gotoNextBut)
    Button gotoNextBut;
    @BindView(R.id.choseScreen)
    Button choseScreen;
    @BindView(R.id.choseLevel)
    Button choseLevel;
    @BindView(R.id.addNote)
    Button addNote;
    @BindView(R.id.choseNote)
    Button choseNote;
    @BindView(R.id.cleanScreenBut)
    Button cleanScreenBut;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettingEntity = new SettingEntity(this);
        setContentView(R.layout.activity_setting_example);
        ButterKnife.bind(this);
        mHandler = new Handler();
        recordBoardView.setIsTouchWrite(true);//允许在屏幕上直接绘制
        recordBoardView.setDaoSession(MyApplication.getInstance().getDaoSession());
    }


    @Override
    protected void onResume() {
        super.onResume();
        recordBoardView.initDrawArea();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (recordBoardView != null) {
            recordBoardView.dispose();
            recordBoardView = null;
        }
    }

    /**
     * 当服务服务连接成功后进行
     *
     * @param name
     * @param service
     */
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        super.onServiceConnected(name, service);
        checkDeviceConn();
    }

    public void checkDeviceConn() {
        if (robotService != null) {
            try {
                RobotDevice device = robotService.getConnectedDevice();
                if (device != null) {
                    DeviceType type = DeviceType.toDeviceType(device.getDeviceVersion());
                    //判断当前设备与笔记设备是否一致
                    if (recordBoardView.getFrameSizeObject().getDeviceType() != type) {
                        mDeDeviceType = type;
                        mNoteKey = NoteEntity.KEY_NOTEKEY_TMP + "_" + mDeDeviceType.name();
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        //都需要刷新白板
        recordBoardView.initDrawArea();
    }

    @OnClick({R.id.cleanScreenBut
            , R.id.choseScreen, R.id.choseLevel
            , R.id.saveScreenBut
            , R.id.addNote, R.id.choseNote
            , R.id.delPageBut, R.id.gotoProBut, R.id.gotoNextBut
            , R.id.recordBut, R.id.recordStopBut})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cleanScreenBut:
                recordBoardView.cleanScreen();
                break;
            case R.id.choseScreen: //横竖屏切换
                final String[] screenItems = {"横屏", "竖屏"};
                new AlertDialog.Builder(SettingExampleActivity.this).setTitle("横竖屏切换")
                        .setItems(screenItems, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0: //横屏为true
                                        mSettingEntity.setDirection(true);
                                        mDirection = true;
                                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                                        mNoteKey = NoteEntity.KEY_NOTEKEY_TMP + "_" + mDeDeviceType.name()+"_H";
                                        break;
                                    case 1:
                                        mSettingEntity.setDirection(false);
                                        mDirection = false;
                                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                                        mNoteKey = NoteEntity.KEY_NOTEKEY_TMP + "_" + mDeDeviceType.name();
                                        break;
                                }
                                recordBoardView.updateNoteIsHorizontal(getIsHorizontal());
                                recordBoardView.initPenDrawView();
                            }
                        }).show();

                break;
            case R.id.choseLevel: //录制质量 22, 12, 2
                final String[] levelItems = {"超清", "高清","标清"};
                new AlertDialog.Builder(SettingExampleActivity.this).setTitle("录像质量")
                        .setItems(levelItems, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0: //超清22
                                        mSettingEntity.setVideoQuality(22);
                                        break;
                                    case 1:
                                        mSettingEntity.setVideoQuality(12);
                                        break;
                                    case 2:
                                        mSettingEntity.setVideoQuality(2);
                                        break;
                                }
                            }
                        }).show();

                break;
            case R.id.saveScreenBut:
                recordBoardView.setSaveSnapshotDir(ResUtils.getSavePath(ResUtils.DIR_NAME_PHOTO));//设置存储路径
                recordBoardView.saveSnapshot();
                break;
            case R.id.delPageBut:
                recordBoardView.delCurrBlock();
                break;
            case R.id.gotoProBut:
                recordBoardView.frontBlock();
                break;
            case R.id.gotoNextBut:
                recordBoardView.nextBlock();
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


    @Override
    public DeviceType getDeviceType() {
        return mDeDeviceType;
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
        return isRubber;
    } //非0时即为橡皮擦

    @Override
    public boolean getIsPressure() {
        return false;
    }

    @Override
    public boolean getIsHorizontal() {
        return mDirection;
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
    public String getNewNoteName() {
        return null;
    }

    @Override
    public boolean onEvent(WhiteBoardView.BoardEvent boardEvent, Object o) {
        switch (boardEvent) {
            case BOARD_AREA_COMPLETE: //白板区域加载完成
                recordBoardView.beginBlock();
                break;
            case ERROR_DEVICE_TYPE: //检测到连接设备更换
                checkDeviceConn();
                break;
            case ERROR_SCENE_TYPE: //横竖屏更换
                //recordBoardView.updateNoteIsHorizontal(getIsHorizontal());
                break;
        }
        return true;
    }

    @Override
    public boolean onMessage(String s, Object o) {
        return false;
    }

    /*
    *录制时必须实现的方法
     */
    @Override
    public int getRecordLevel() {
        return mSettingEntity.getVideoQualityValue(); //录制质量
    }

    @Override
    public void onRecordButClick(int i) {
        switch (i) {
            case RecordBoardView.EVENT_CONFIRM_EXT_CLICK:
                break;
        }

    }

    @Override
    public void onRecordError(int i) {

    }

    /**
     * 接收录制中的各种状态进行处理
     */
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
        return false;
    }

    @Override
    public void onStateChanged(int i, String s) {
        switch (i) {
            case RemoteState.STATE_CONNECTED:
                break;
            case RemoteState.STATE_DEVICE_INFO: //当出现设备切换时获取到新设备信息后执行的
                //whiteBoardView.initDrawArea();
                checkDeviceConn();
                break;
            case RemoteState.STATE_DISCONNECTED://设备断开
                break;
        }
    }

    @Override
    public void onPenServiceError(String s) {

    }

    @Override
    public void onPenPositionChanged(int deviceType, int x, int y, int presure, byte state) {
        super.onPenPositionChanged(deviceType, x, y, presure, state);
        DevicePoint p = DevicePoint.obtain(deviceType, x, y, presure, state);
        recordBoardView.drawLine(p);
    }
}
