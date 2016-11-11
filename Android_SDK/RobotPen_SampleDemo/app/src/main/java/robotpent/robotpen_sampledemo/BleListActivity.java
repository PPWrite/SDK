package robotpent.robotpen_sampledemo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.robotpen.core.PenManage;
import cn.robotpen.core.services.SmartPenService;
import cn.robotpen.model.entity.DeviceEntity;
import cn.robotpen.model.interfaces.Listeners;
import cn.robotpen.model.symbol.ConnectState;
import cn.robotpen.model.symbol.Keys;
import robotpent.robotpen_sampledemo.adapter.PenAdapter;

public class BleListActivity extends Activity {
    @BindView(R.id.statusText)
    TextView statusText;
    @BindView(R.id.listview)
    ListView listview;
    @BindView(R.id.emptytext)
    TextView emptytext;
    @BindView(R.id.scanBut)
    Button scanBut;
    @BindView(R.id.disconnectBut)
    Button disconnectBut;
    @BindView(R.id.listFrame)
    LinearLayout listFrame;

    private PenAdapter mPenAdapter;
    private PenManage mPenManage;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_list);
        ButterKnife.bind(this);
        mPenManage  = new PenManage(this, SmartPenService.TAG);
        mHandler = new Handler();
        listview.setEmptyView(emptytext);
        mPenAdapter = new PenAdapter(BleListActivity.this);
        listview.setAdapter(mPenAdapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
                //停止搜索
                mPenManage.stopScanDevice();
                DeviceEntity device = mPenAdapter.getItem(arg2);
                mPenManage.connectDevice(onConnectStateListener, device.getAddress());//定向连接设备
            }
        });
    }

    @Override
    protected void onPause() {
        if (mPenManage != null)
            mPenManage.shutdown();
        super.onPause();
    }


    @OnClick({R.id.scanBut,
        R.id.disconnectBut})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.scanBut:
                mPenAdapter.clearItems();
                mPenAdapter.notifyDataSetChanged();
                mPenManage.scanDevice(onScanDeviceListener);
                break;
            case R.id.disconnectBut:
                mPenManage.disconnectDevice();
                scanBut.setVisibility(View.VISIBLE);
                disconnectBut.setVisibility(View.GONE);
                break;
        }
    }

    private Listeners.OnScanDeviceListener onScanDeviceListener = new Listeners.OnScanDeviceListener() {
        @Override
        public void find(DeviceEntity device) {
            mPenAdapter.addItem(device);
            mPenAdapter.notifyDataSetChanged();
        }
        @Override
        public void complete(HashMap<String, DeviceEntity> list) {
            scanBut.setEnabled(true);
        }
        @Override
        public void status(int i) {
            switch (i){
                case Keys.REQUEST_ENABLE_BT:
                    Toast.makeText(BleListActivity.this,"蓝牙未打开",Toast.LENGTH_SHORT).show();
                    Intent req_ble = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(req_ble, Keys.REQUEST_ENABLE_BT);
                    break;
                case  Keys.BT_ENABLE_ERROR:
                    Toast.makeText(BleListActivity.this,"设备不支持BLE协议",Toast.LENGTH_SHORT).show();
                    break;
            }


        }
    };

    private Listeners.OnConnectStateListener onConnectStateListener = new Listeners.OnConnectStateListener() {
        @Override
        public void stateChange(String address, ConnectState state) {
            switch (state) {
                case PEN_INIT_COMPLETE: //蓝牙连接成功
                    //检查是否已连接设备
                    DeviceEntity device = mPenManage.getConnectDevice();
                    if (device != null) {
                        statusText.setText("已连接设备: "+device.getName());
                        scanBut.setVisibility(View.GONE);
                        disconnectBut.setVisibility(View.VISIBLE);
                        mPenManage.saveLastDevice(BleListActivity.this,device);
                        //mPenManage.setSceneObject(SceneType.P7);
                       // mPenManage.setOnPointChangeListener(onPointChangeListener);
                        //mPenManage.startService();
                    }
                    break;
                default: //另外有连接中、连接错误等多个状态可以判断使用
                    break;
            }
        }
    };

//    private Listeners.OnPointChangeListener onPointChangeListener = new Listeners.OnPointChangeListener() {
//
//        @Override
//        public void change(final PointObject point) {
//            // 设置看坐标中的各个字段
////            connectSenceType.setText(point.getSceneType().name());
////            connectDeviceSize.setText(point.getWidth()+"/"+point.getHeight());
////            penBattery.setText(String.valueOf(point.battery.getValue()));
////            penIsRoute.setText(String.valueOf(point.isRoute));
////            penWeight.setText(String.valueOf(point.weight));
////            penColor.setText(String.valueOf(point.color));
////            penPress.setText(point.pressure+"/"+point.pressureValue);
////            penOriginal.setText(point.originalX+"/"+point.originalY);
////            connectOffest.setText(point.getOffsetX()+"/"+point.getOffsetY());
//            //Toast.makeText(BleListActivity.this,point.pressureValue+"",Toast.LENGTH_SHORT).show();
//
//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    statusText.setText("压力值: "+point.pressureValue);
//                }
//            });
//
//        }
//
//        @Override
//        public void onButClick(int i) {
//
//        }
//    };






}
