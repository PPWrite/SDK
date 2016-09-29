package cn.robotpen.demo.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

import cn.robotpen.core.module.PenDataUtil;
import cn.robotpen.core.services.PenService;
import cn.robotpen.core.services.SmartPenService;
import cn.robotpen.demo.Ant;
import cn.robotpen.demo.R;
import cn.robotpen.demo.RobotPenApplication;
import cn.robotpen.demo.bluetooth.adapter.PenAdapter;
import cn.robotpen.demo.utils.ResUtils;
import cn.robotpen.model.DeviceObject;
import cn.robotpen.model.handler.OnReceiveDataHandler;
import cn.robotpen.model.interfaces.Listeners;
import cn.robotpen.model.symbol.ConnectState;
import cn.robotpen.model.symbol.Keys;

public class BleListActivity extends Ant {
    private PageItem mPageItem;
    private PenAdapter mPenAdapter;
    private PenService mPenService;
    private RobotPenApplication mApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_list);
        mApplication = RobotPenApplication.getInstance();
        mPageItem = new PageItem();
    }


    @Override
    public void penSvrInitComplete() {
        mPenService = mApplication.getPenService();
        //判断USB是否连上,如果连上则断开
        String tag = mApplication.getPenService().getSvrTag();
        if(tag.equals(Keys.APP_USB_SERVICE_NAME)){
            //解除绑定
            mApplication.unBindPenService();
            isUnBindPenService(Keys.APP_PEN_SERVICE_NAME);
        }else{
            //检测当前设备
            checkDevice();
        }
    }

    /**
     * 将USB服务切换为蓝牙服务
     * @param bindTag
     */
    private void isUnBindPenService(final String bindTag){
        if(mPenService == null || !mPenService.getIsBind()){
            mApplication.setConnectDeviceType(bindTag);
            initPenService();
        }else{
            showBindSvrDialog(bindTag);
            getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isUnBindPenService(bindTag);
                }
            }, 500);
        }
    }

    private void checkDevice() {
        //检查设备是否支持ble
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
                && ((SmartPenService) mPenService).isBluetoothAdapterNormal()) {
            //检查是否已连接设备
            DeviceObject device = mPenService.getConnectDevice();
            if (device != null) {
                mPenService.setOnReceiveDataListener(onReceiveDataHandler);
                mPageItem.statusText.setText("已连接设备: "+device.type.getValue());
                mPageItem.disconnectBut.setVisibility(View.VISIBLE);
            }
        } else {
            Toast.makeText(this,"当前设备不支持蓝牙",Toast.LENGTH_LONG);
            this.finish();

        }
    }

    private OnReceiveDataHandler onReceiveDataHandler = new OnReceiveDataHandler(){
        @Override
        public void version(int hw, int sw) {
            //hw为硬件版本  sw为固件版本
           Toast.makeText(BleListActivity.this,PenDataUtil.getFormatHardwareVersion(hw)+"/"+PenDataUtil.getFormatFirmwareVersion(sw),Toast.LENGTH_LONG).show();
        }
    };

    private Listeners.OnScanDeviceListener onScanDeviceListener = new Listeners.OnScanDeviceListener() {
        @Override
        public void find(DeviceObject device) {
            mPenAdapter.addItem(device);
            mPenAdapter.notifyDataSetChanged();
        }

        @Override
        public void complete(HashMap<String, DeviceObject> list) {
           // mPageItem.emptytext.setText(R.string.error_find_device);
            //mPageItem.scanBut.setText(R.string.title_find_ble);
            mPageItem.scanBut.setEnabled(true);
        }
    };
    private Listeners.OnConnectStateListener onConnectStateListener = new Listeners.OnConnectStateListener() {
        @Override
        public void stateChange(String address, ConnectState state) {

            Log.v(TAG, "onConnectStateListener state:" + state);

            switch (state) {
                case PEN_INIT_COMPLETE: //蓝牙连接成功
                    //检查是否已连接设备
                    DeviceObject device = mPenService.getConnectDevice();
                    if (device != null) {
                        mPageItem.statusText.setText("已连接设备: "+device.name);
                        mPageItem.disconnectBut.setVisibility(View.VISIBLE);
                    }
                    break;
                default:

                    break;
            }
        }
    };
    class PageItem {
        Button scanBut;
        TextView statusText;
        Button disconnectBut;
        TextView emptytext;
        ListView deviceList;
        LinearLayout listFrame;


        public PageItem() {
            scanBut = (Button) findViewById(R.id.scanBut);
            scanBut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPenService.setScanTime(5000);
                    if(mPenService.scanDevice(onScanDeviceListener, ResUtils.getPackagePath())) {
                        mPageItem.scanBut.setEnabled(false);
                        mPageItem.emptytext.setText("正在搜索");
                        mPenAdapter.clearItems();
                        mPenAdapter.notifyDataSetChanged();
                    }else {
                        //请求打开蓝牙
                        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableIntent, Keys.REQUEST_ENABLE_BT);
                    }
                }
            });
            disconnectBut = (Button) findViewById(R.id.disconnectBut);
            disconnectBut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPenService.disconnectDevice();
                    v.setVisibility(View.GONE);
                    statusText.setText(R.string.disconnected);
                    mPageItem.emptytext.setText("请重新搜索");
                    mPenAdapter.clearItems();
                    mPenAdapter.notifyDataSetChanged();
                }
            });
            statusText = (TextView) findViewById(R.id.statusText);
            emptytext = (TextView) findViewById(R.id.emptytext);
            deviceList = (ListView) findViewById(R.id.listview);
            listFrame = (LinearLayout) findViewById(R.id.listFrame);
            deviceList.setEmptyView(emptytext);
            mPenAdapter = new PenAdapter(BleListActivity.this);
            deviceList.setAdapter(mPenAdapter);
            deviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
                    //停止搜索
                    mPenService.stopScanDevice();
                    DeviceObject device = mPenAdapter.getItem(arg2);
                    ConnectState state = ((SmartPenService) mPenService).connectDevice(onConnectStateListener, device.address);

                }
            });
        }
    }

}
