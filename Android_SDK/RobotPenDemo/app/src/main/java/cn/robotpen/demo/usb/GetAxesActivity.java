package cn.robotpen.demo.usb;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import cn.robotpen.core.PenManage;
import cn.robotpen.demo.R;
import cn.robotpen.model.DeviceObject;
import cn.robotpen.model.PointObject;
import cn.robotpen.model.interfaces.Listeners;
import cn.robotpen.model.symbol.ConnectState;
import cn.robotpen.model.symbol.Keys;
import cn.robotpen.model.symbol.SceneType;

public class GetAxesActivity extends Activity {


    @BindView(R.id.deviceBut)
    Button deviceBut;
    @BindView(R.id.deviceStatus)
    TextView deviceStatus;
    @BindView(R.id.deviceInfo)
    LinearLayout deviceInfo;
    @BindView(R.id.sceneType)
    Spinner sceneType;
    @BindView(R.id.isRouteTxt)
    TextView isRouteTxt;
    @BindView(R.id.isRoute)
    TextView isRoute;
    @BindView(R.id.pressureTxt)
    TextView pressureTxt;
    @BindView(R.id.pressure)
    TextView pressure;
    @BindView(R.id.originalXTxt)
    TextView originalXTxt;
    @BindView(R.id.originalX)
    TextView originalX;
    @BindView(R.id.originalYTxt)
    TextView originalYTxt;
    @BindView(R.id.originalY)
    TextView originalY;
    @BindView(R.id.penInfo)
    RelativeLayout penInfo;
    @BindView(R.id.sceneTypeTxt)
    TextView sceneTypeTxt;
    @BindView(R.id.sceneType1)
    TextView sceneType1;
    @BindView(R.id.sceneWidthTxt)
    TextView sceneWidthTxt;
    @BindView(R.id.sceneWidth)
    TextView sceneWidth;
    @BindView(R.id.sceneHeightTxt)
    TextView sceneHeightTxt;
    @BindView(R.id.sceneHeight)
    TextView sceneHeight;
    @BindView(R.id.sceneOffsetXTXT)
    TextView sceneOffsetXTXT;
    @BindView(R.id.sceneOffsetX)
    TextView sceneOffsetX;
    @BindView(R.id.sceneOffsetYTXT)
    TextView sceneOffsetYTXT;
    @BindView(R.id.sceneOffsetY)
    TextView sceneOffsetY;
    @BindView(R.id.deviceInfo2)
    RelativeLayout deviceInfo2;

    private String[] mItems = {"10.1寸竖屏", "10.1寸横屏"};
    private Handler mHandler = new Handler();
    private ProgressDialog mProgressDialog;
    private PenManage mPenManage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        // 控制屏幕常亮
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_getaxes);
        butterknife.ButterKnife.bind(this);
        mPenManage = new PenManage(this);
        mPenManage.setServiceType(this, Keys.APP_USB_SERVICE_NAME);
        mPenManage.setSceneObject(SceneType.P1);//将场景设置为P1设备的竖屏
        mPenManage.setOnConnectStateListener(onConnectStateListener);
        mPenManage.setOnPointChangeListener(onPointChangeListener);
        mPenManage.startService();
        mProgressDialog = ProgressDialog.show(this, "", getString(R.string.service_usb_start), true);
    }


    /*
    * 此处监听是为了弹出授权
    */
    private Listeners.OnConnectStateListener onConnectStateListener = new Listeners.OnConnectStateListener() {
        @Override
        public void stateChange(String arg0, ConnectState arg1) {
            // TODO Auto-generated method stub
            if (arg1 == ConnectState.CONNECTED) {
                dismissProgressDialog();
            }
        }
    };
    /*
     * 手动执行设备扫描，确保设备保持连接状态
     */
    private Listeners.OnScanDeviceListener onScanDeviceListener = new Listeners.OnScanDeviceListener() {
        @Override
        public void complete(HashMap<String, DeviceObject> arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void status(int i) {

        }

        @Override
        public void find(DeviceObject arg0) {
            // TODO Auto-generated method stub

        }

    };

    /*
        * 通过监听方式完成业务处理 这里接收笔的信息建议通过监听，通过广播方式也是可以的，但是广播方式效率较低
        */
    private Listeners.OnPointChangeListener onPointChangeListener = new Listeners.OnPointChangeListener() {
        @Override
        public void change(PointObject point) {
            // TODO Auto-generated method stub
            // 设置看坐标中的各个字段
            originalX.setText(String.valueOf(point.originalX));
            originalY.setText(String.valueOf(point.originalY));
            isRoute.setText(String.valueOf(point.isRoute));
            pressure.setText(String.valueOf(point.pressure) + "(" + String.valueOf(point.pressureValue) + ")");

            sceneType1.setText(String.valueOf(mPenManage.getConnectDeviceType()));
            //sceneWidth.setText(String.valueOf(point.getWidth(mPenManage.getConnectDeviceType())));
            //sceneHeight.setText(String.valueOf(point.getHeight(mPenService.getSceneType())));
            sceneOffsetX.setText(String.valueOf(point.getOffsetX()));
            sceneOffsetY.setText(String.valueOf(point.getOffsetY()));
        }

        @Override
        public void change(List<PointObject> arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onButClick(int i) {

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

}
