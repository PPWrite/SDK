package cn.robotpen.demo.usb;


import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.robotpen.core.PenManage;
import cn.robotpen.core.views.PenDrawView;
import cn.robotpen.core.views.WhiteBoardView;
import cn.robotpen.demo.R;
import cn.robotpen.model.interfaces.Listeners.OnConnectStateListener;
import cn.robotpen.model.symbol.ConnectState;
import cn.robotpen.model.symbol.SceneType;

public class NoteActivity extends Activity implements WhiteBoardView.CanvasManageInterface {

    @BindView(R.id.lineWindow)
    RelativeLayout lineWindow;

    private WhiteBoardView mWhiteBoardView;
    private PenManage mPenManage;
    // private ProgressDialog mProgressDialog;
    private Handler mHandler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 控制屏幕常亮
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_note);
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mWhiteBoardView==null){
            mWhiteBoardView = new WhiteBoardView(this,this);
            lineWindow.addView(mWhiteBoardView);
            mWhiteBoardView.getPenManage().setOnConnectStateListener(onConnectStateListener);
        }
        mWhiteBoardView.getPenManage().scanDevice(null);
    }

    @Override
    public void onPause() {
        super.onPause();
        //断开设备
        if (mWhiteBoardView != null) {
            mWhiteBoardView.getPenManage().disconnectDevice();
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mWhiteBoardView != null) {
            mWhiteBoardView.dispose();
            mWhiteBoardView = null;
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
    public PenDrawView.PenModel getPenModel() {
        return PenDrawView.PenModel.WaterPen;
    }

    @Override
    public float getPenWeight() {
        return 2;
    }

    @Override
    public int getPenColor() {
        return Color.BLACK;
    }

    @Override
    public float getIsRubber() {
        return 0;
    }

    @Override
    public long getCurrUserId() {
        return 0;
    }

    @Override
    public String getNoteKey() {
        return null;
    }

    @Override
    public void onCanvasSizeChanged(int i, int i1, SceneType sceneType) {

    }

    @Override
    public void onPenRouteStatus(boolean b) {

    }

    @Override
    public void onTrailsLoading(boolean b) {

    }
}
