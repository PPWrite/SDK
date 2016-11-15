package robotpent.robotpen_sampledemo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.robotpen.core.PenManage;
import cn.robotpen.core.services.SmartPenService;
import cn.robotpen.model.PointObject;
import cn.robotpen.model.entity.DeviceEntity;
import cn.robotpen.model.interfaces.Listeners;
import cn.robotpen.model.symbol.ConnectState;
import cn.robotpen.model.symbol.Keys;
import cn.robotpen.model.symbol.SceneType;
import cn.robotpen.utils.StringUtil;

public class BleActivity extends Activity {

    PenManage mPenManage;
    @BindView(R.id.connectDevice)
    Button connectDevice;
    @BindView(R.id.device_info)
    TextView deviceInfo;
    @BindView(R.id.connect_deviceType)
    TextView connectDeviceType;
    @BindView(R.id.connect_senceType)
    TextView connectSenceType;
    @BindView(R.id.connect_deviceSize)
    TextView connectDeviceSize;
    @BindView(R.id.pen_battery)
    TextView penBattery;
    @BindView(R.id.pen_isRoute)
    TextView penIsRoute;
    @BindView(R.id.pen_weight)
    TextView penWeight;
    @BindView(R.id.pen_color)
    TextView penColor;
    @BindView(R.id.pen_press)
    TextView penPress;
    @BindView(R.id.pen_original)
    TextView penOriginal;
    @BindView(R.id.connect_offest)
    TextView connectOffest;
    @BindView(R.id.activity_ble)
    LinearLayout activityBle;

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 控制屏幕常亮
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_ble);
        ButterKnife.bind(this);
        mHandler = new Handler();
        connectDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(BleActivity.this,BleListActivity.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //mPenManage  = new PenManage(this, SmartPenService.TAG); //此种方式创建的PenManage对象将记住服务类型
                mPenManage = new PenManage(BleActivity.this);
                mPenManage.setServiceType(BleActivity.this, SmartPenService.TAG);
                mPenManage.setSceneObject(SceneType.P7);
                DeviceEntity connectedDevice = mPenManage.getConnectDevice();
                if (connectedDevice != null) {
                    deviceInfo.setText("已连接设备: " + connectedDevice.getName());
                    mPenManage.setSceneObject(SceneType.P7);
                } else {
                    DeviceEntity lastDevice = mPenManage.getLastDevice(BleActivity.this); //在BleList中已经将连接的设备进行了保存
                    if (lastDevice != null && !StringUtil.isEmpty(lastDevice.getAddress())) {
                        mPenManage.scanDevice(onScanDeviceListener);
                    }
                }
            }
        }, 500); //服务的关闭和解绑需要时间这里增加延时，在实际处理时可以通过状态判定来处理
    }

    @Override
    protected void onPause() {
        if (mPenManage != null)
            mPenManage.shutdown(); //退出Activity时将服务释放，方便其他地方继续使用
        super.onPause();
    }
    private Listeners.OnPointChangeListener onPointChangeListener = new Listeners.OnPointChangeListener() {

        @Override
        public void change(final PointObject point) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    // 设置看坐标中的各个字段
                    connectSenceType.setText(point.getSceneType().name());
                    connectDeviceSize.setText(point.getWidth()+"/"+point.getHeight());
                    penBattery.setText(String.valueOf(point.battery.getValue()));
                    penIsRoute.setText(String.valueOf(point.isRoute));
                    penWeight.setText(String.valueOf(point.weight));
                    penColor.setText(String.valueOf(point.color));
                    penPress.setText(point.pressure+"/"+point.pressureValue);
                    penOriginal.setText(point.originalX+"/"+point.originalY);
                    connectOffest.setText(point.getOffsetX()+"/"+point.getOffsetY());
                }
            });
        }
        @Override
        public void onButClick(int i) {

        }
    };

    Listeners.OnConnectStateListener connectStateListener = new Listeners.OnConnectStateListener() {
        @Override
        public void stateChange(String s, ConnectState connectState) {
            switch (connectState) {
                case PEN_INIT_COMPLETE: //蓝牙连接成功
                    //检查是否已连接设备
                    DeviceEntity device = mPenManage.getConnectDevice();
                    if (device != null) {
                        deviceInfo.setText("已连接设备: "+device.getName());
                    }
                    mPenManage.setSceneObject(SceneType.P7);
                    mPenManage.setOnPointChangeListener(onPointChangeListener);
                    mPenManage.startService();
                    break;
                default: //另外有连接中、连接错误等多个状态可以判断使用
                    break;
            }

        }
    };

    Listeners.OnScanDeviceListener onScanDeviceListener = new Listeners.OnScanDeviceListener() {
        @Override
        public void find(DeviceEntity deviceObject) {
            DeviceEntity lastDevice = mPenManage.getLastDevice(BleActivity.this);
            if(!StringUtil.isEmpty(lastDevice.getAddress())){
                if(deviceObject.getAddress().equals(lastDevice.getAddress())){
                    mPenManage.stopScanDevice();
                    mPenManage.connectDevice(connectStateListener,lastDevice.getAddress());
                }
            }
        }
        @Override
        public void complete(HashMap<String, DeviceEntity> hashMap) {

        }

        @Override
        public void status(int i) {
            switch (i){
                case Keys.REQUEST_ENABLE_BT:
                    Toast.makeText(BleActivity.this,"蓝牙未打开",Toast.LENGTH_SHORT).show();
                    Intent req_ble = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(req_ble, Keys.REQUEST_ENABLE_BT);
                    break;
                case  Keys.BT_ENABLE_ERROR:
                    Toast.makeText(BleActivity.this,"设备不支持BLE协议",Toast.LENGTH_SHORT).show();
                    break;
            }

        }
    };
}
