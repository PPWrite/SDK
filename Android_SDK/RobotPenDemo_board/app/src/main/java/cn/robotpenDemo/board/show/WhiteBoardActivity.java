package cn.robotpenDemo.board.show;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.robotpen.core.widget.WhiteBoardView;
import cn.robotpen.model.DevicePoint;
import cn.robotpen.model.entity.note.NoteEntity;
import cn.robotpen.model.symbol.DeviceType;
import cn.robotpen.pen.callback.PenPositionAndEventCallback;
import cn.robotpen.pen.callback.RobotPenActivity;
import cn.robotpen.pen.model.RemoteState;
import cn.robotpen.pen.model.RobotDevice;
import cn.robotpenDemo.board.MyApplication;
import cn.robotpenDemo.board.R;
import cn.robotpenDemo.board.common.BaseConnectPenServiceActivity;

public class WhiteBoardActivity extends RobotPenActivity
        implements WhiteBoardView.CanvasManageInterface {//BaseConnectPenServiceActivity<PenPositionAndEventCallback>

    @BindView(R.id.clearnScreen)
    Button clearnScreen;
    @BindView(R.id.whiteBoardView)
    WhiteBoardView whiteBoardView;

    DeviceType mDeDeviceType = DeviceType.P1;//默认连接设备为P1 当与连接设备有冲突时则需要进行切换
    boolean isRubber;//是否是橡皮
    boolean isTouchWrite;//是否可以通过屏幕书写
    float mPenWeight = 2;//笔粗细
    int mPenColor = Color.BLACK;//笔颜色
    String mNoteKey = NoteEntity.KEY_NOTEKEY_TMP;
    ProgressDialog mProgressDialog;
    Handler mHandler;
    private WeakReference<Activity> weakReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_white_board);
        ButterKnife.bind(this);
        mHandler = new Handler();
        whiteBoardView.setIsTouchWrite(true);//允许在屏幕上直接绘制
        whiteBoardView.setDaoSession(MyApplication.getInstance().getDaoSession());
    }
    @Override
    protected void onResume() {
        super.onResume();
        whiteBoardView.initDrawArea();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (whiteBoardView != null) {
            whiteBoardView.dispose();
            whiteBoardView = null;
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
                    if (whiteBoardView.getFrameSizeObject().getDeviceType() != type) {
                        mDeDeviceType = type;
                        mNoteKey = NoteEntity.KEY_NOTEKEY_TMP + "_" + mDeDeviceType.name();
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        //都需要刷新白板
        whiteBoardView.initDrawArea();
    }
    @OnClick(R.id.clearnScreen)
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clearnScreen:
                whiteBoardView.cleanScreen();
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
    public String getNewNoteName() {
        return null;
    }

    @Override
    public boolean onEvent(WhiteBoardView.BoardEvent boardEvent, Object o) {
        switch (boardEvent) {
            case BOARD_AREA_COMPLETE: //白板区域加载完成
                whiteBoardView.beginBlock();
                break;
            case ERROR_DEVICE_TYPE: //检测到连接设备更换
                checkDeviceConn();
                break;
            case ERROR_SCENE_TYPE: //横竖屏更换
                break;
        }
        return true;
    }

    @Override
    public boolean onMessage(String s, Object o) {
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
        whiteBoardView.drawLine(p);//白板的绘制必须手动执行
    }
}
