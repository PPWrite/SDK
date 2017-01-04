package cn.robotpen.demo.show;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.robotpen.core.PenManage;
import cn.robotpen.core.services.PenService;
import cn.robotpen.core.services.SmartPenService;
import cn.robotpen.demo.R;
import cn.robotpen.demo.connect.DeviceActivity;
import cn.robotpen.model.PointObject;
import cn.robotpen.model.entity.DeviceEntity;
import cn.robotpen.model.symbol.ConnectState;
import cn.robotpen.model.symbol.Keys;
import cn.robotpen.model.symbol.SceneType;
import cn.robotpen.utils.StringUtil;

public class ShowPointActivity extends Activity {

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
    @BindView(R.id.activity_usb)
    LinearLayout activityUsb;

    PenManage mPenManage;
    ProgressDialog mProgressDialog;
    Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_point);
        ButterKnife.bind(this);
        mHandler = new Handler();
        //获取关键服务
        mPenManage = new PenManage(this);
        //设置状态监听
        mPenManage.setOnConnectStateListener(onConnectStateListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkDeviceConnStatus(); //检查设备连接状态
    }
    @Override
    protected void onPause() {
        if (mPenManage != null)
            mPenManage.disconnectDevice(); //退出Activity时将服务释放，方便其他地方继续使用
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPenManage != null)
            mPenManage.shutdown(); //退出Activity时将服务释放，方便其他地方继续使用
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
                DeviceEntity lastDevice = PenManage.getLastDevice(ShowPointActivity.this);
                if (lastDevice == null || TextUtils.isEmpty(lastDevice.getAddress())) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(ShowPointActivity.this);
                    alert.setTitle("提示");
                    alert.setMessage("暂未连接设备，请先连接设备！");
                    alert.setPositiveButton(R.string.canceled, null);
                    alert.setNegativeButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Intent intent = new Intent(ShowPointActivity.this, DeviceActivity.class);
                            ShowPointActivity.this.startActivity(intent);
                            ShowPointActivity.this.finish();
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
            Toast.makeText(ShowPointActivity.this,"设备连接成功",Toast.LENGTH_LONG).show();
        }
    }

    /*
    *扫描监听
     */
    PenService.OnScanDeviceListener onScanDeviceListener = new PenService.OnScanDeviceListener() {
        @Override
        public void find(DeviceEntity deviceObject) {
            DeviceEntity lastDevice = mPenManage.getLastDevice(ShowPointActivity.this);
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
                Toast.makeText(ShowPointActivity.this, "暂未发现设备", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void status(int i) {
            switch (i) {
                case Keys.REQUEST_ENABLE_BT:
                    Toast.makeText(ShowPointActivity.this, "蓝牙未打开", Toast.LENGTH_SHORT).show();
                    Intent req_ble = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(req_ble, Keys.REQUEST_ENABLE_BT);
                    break;
                case Keys.BT_ENABLE_ERROR:
                    Toast.makeText(ShowPointActivity.this, "设备不支持BLE协议", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(ShowPointActivity.this, "设备已连接且连接成功！", Toast.LENGTH_SHORT).show();
                mPenManage.setSceneObject(SceneType.getSceneType(false,mPenManage.getConnectDeviceType()));
                //刷新当前临时笔记
                mPenManage.setOnPointChangeListener(onPointChangeListener);
            } else if (arg1 == ConnectState.DISCONNECTED) {
                Toast.makeText(ShowPointActivity.this, "设备已断开", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private PenService.OnPointChangeListener onPointChangeListener = new PenService.OnPointChangeListener() {

        @Override
        public void change(final PointObject point) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    // 设置看坐标中的各个字段
                    connectDeviceType.setText(point.getDeviceType().name());
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
