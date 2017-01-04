package cn.robotpen.demo.connect;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.robotpen.core.PenManage;
import cn.robotpen.core.services.PenService;
import cn.robotpen.core.services.SmartPenService;
import cn.robotpen.demo.R;
import cn.robotpen.model.entity.DeviceEntity;
import cn.robotpen.model.handler.OnReceiveDataHandler;
import cn.robotpen.model.interfaces.Listeners;
import cn.robotpen.model.symbol.ConnectState;
import cn.robotpen.model.symbol.DeviceType;
import cn.robotpen.model.symbol.Keys;
import cn.robotpen.model.symbol.ResultState;
import cn.robotpen.model.symbol.SceneType;
import cn.robotpen.utils.StringUtil;
import cz.msebera.android.httpclient.Header;

import static cn.robotpen.demo.R.id;

public class BleConnectActivity extends Activity {

    @BindView(id.statusText)
    TextView statusText;
    @BindView(id.deviceDetail)
    Button deviceDetail;
    @BindView(id.listview)
    ListView listview;
    @BindView(id.scanBut)
    Button scanBut;
    @BindView(id.disconnectBut)
    Button disconnectBut;
    @BindView(id.listFrame)
    LinearLayout listFrame;


    private PenAdapter mPenAdapter;
    private PenManage mPenManage;
    private Handler mHandler;
    private AsyncHttpClient mHttpClient;
    private int mFirmwareVer;
    //int  stateFlag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_connect);
        ButterKnife.bind(this);
        // mPenManage = new PenManage(this, SmartPenService.TAG);
        mHandler = new Handler();
        mHttpClient = new AsyncHttpClient();
        mPenAdapter = new PenAdapter(BleConnectActivity.this);
        listview.setAdapter(mPenAdapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                //停止搜索
                mPenManage.stopScanDevice();
                DeviceEntity device = mPenAdapter.getItem(arg2);
                connectDevice(device.getAddress());//定向连接设备
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        CheckDevice();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mPenManage != null)
            mPenManage.disconnectDevice();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPenManage != null)
            mPenManage.shutdown(); //退出Activity时将服务释放，方便其他地方继续使用
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        boolean returnFlag = false;
//        if (keyCode == KeyEvent.KEYCODE_BACK ) {
//            if(stateFlag == 1){
//                returnFlag = true;
//                AlertDialog.Builder alert = new AlertDialog.Builder(this);
//                alert.setTitle("提示");
//                alert.setMessage("正在连接设备，禁止退出！");
//                alert.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                });
//                alert.setCancelable(false);
//                alert.show();
//            }
//        }
//        if(!returnFlag){
//            return super.onKeyDown(keyCode, event);
//        }else{
//            return false;
//        }
//    }


    /*
     * 检测设备连接
     */
    private void CheckDevice() {
        if (null == mPenManage) {
            PenManage.setServiceType(BleConnectActivity.this, SmartPenService.TAG);//这样新建服务会记住连接方式
            mPenManage = new PenManage(this, SmartPenService.TAG); //这样新建服务会记住连接方式
            mPenManage.setScanTime(5000);
            mPenManage.scanDevice(onScanDeviceListener);
        } else {
            //判断蓝牙还是USB服务
            if (SmartPenService.TAG.equals(mPenManage.getSvrTag())) {
                //检查以前是否有连接过设备
                DeviceEntity lastDevice = PenManage.getLastDevice(BleConnectActivity.this);
                if (lastDevice == null || TextUtils.isEmpty(lastDevice.getAddress())) {

                } else {
                    mPenManage.scanDevice(onScanDeviceListener);
                }
            } else {
                //非蓝牙连接
                Toast.makeText(BleConnectActivity.this, "之前是USB连接", Toast.LENGTH_LONG);
                //停止原来的服务 重新开始
            }
        }
    }


    @OnClick({id.scanBut, id.disconnectBut,id.deviceDetail})
    void OnClick(View view) {
        switch (view.getId()) {
            case id.scanBut:
                mPenAdapter.clearItems();
                mPenAdapter.notifyDataSetChanged();
                mPenManage.scanDevice(onScanDeviceListener);
                break;
            case id.disconnectBut:
                mPenManage.disconnectDevice();
                scanBut.setVisibility(View.VISIBLE);
                disconnectBut.setVisibility(View.GONE);
                statusText.setText("未连接设备!");
                break;
            case id.deviceDetail:
                Toast.makeText(BleConnectActivity.this,"持续完善中……",Toast.LENGTH_SHORT).show();
                deviceDetail.setClickable(false);

                break;
        }
    }

    private PenService.OnScanDeviceListener onScanDeviceListener = new PenService.OnScanDeviceListener() {
        @Override
        public void find(DeviceEntity device) {
            mPenAdapter.addItem(device);
            mPenAdapter.notifyDataSetChanged();
            DeviceEntity lastDevice = mPenManage.getLastDevice(BleConnectActivity.this);
            if (lastDevice!=null&&!StringUtil.isEmpty(lastDevice.getAddress())) {
                if (device.getAddress().equals(lastDevice.getAddress())) {
                    mPenManage.stopScanDevice();
                    connectDevice(lastDevice.getAddress());
                }
            }
        }

        @Override
        public void complete(HashMap<String, DeviceEntity> list) {
            if (!mPenManage.getIsStartConnect()) {
                Toast.makeText(BleConnectActivity.this, "没有可连接的设备！", Toast.LENGTH_SHORT).show();
                scanBut.setEnabled(true);
            }
        }

        @Override
        public void status(int i) {
            switch (i) {
                case Keys.REQUEST_ENABLE_BT:
                    Toast.makeText(BleConnectActivity.this, "蓝牙未打开", Toast.LENGTH_SHORT).show();
                    //请求打开蓝牙
                    Intent req_ble = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(req_ble, Keys.REQUEST_ENABLE_BT);
                    break;
                case Keys.BT_ENABLE_ERROR:
                    Toast.makeText(BleConnectActivity.this, "设备不支持BLE协议", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private PenService.OnConnectStateListener onConnectStateListener = new PenService.OnConnectStateListener() {
        @Override
        public void stateChange(String address, ConnectState state) {
                switch (state) {
                    case CONNECTING:
                        //stateFlag = 1;
                        break;
                    case PEN_INIT_COMPLETE: //蓝牙连接成功
                        //stateFlag = 0;
                        mPenManage.getDeviceVersion(); //获取版本号的同时会存储设备类型
                    break;
                default: //另外有连接中、连接错误等多个状态可以判断使用
                        //stateFlag = 0;
                    break;
            }
        }
    };

    private OnReceiveDataHandler onReceiveDataHandler = new OnReceiveDataHandler() {
        @Override
        public void version(int hw, int sw) {
            mHandler.postDelayed(new Runnable() {
                 @Override
                 public void run() {
                     //检查是否已连接设备
                     DeviceEntity device = mPenManage.getConnectDevice();
                     if (device != null) {
                         statusText.setText("已连接设备: " + device.getName());
                         scanBut.setVisibility(View.GONE);
                         disconnectBut.setVisibility(View.VISIBLE);
                         DeviceType dp = device.getDeviceType();
                         SceneType sceneType = SceneType.getSceneType(false, dp);//根据设备型号获取场景模式 false为竖屏
                         mPenManage.setSceneObject(sceneType);
                     }
                 }
             }, 1000
            );
            mFirmwareVer = sw;
            String url = mPenManage.getFirmwareInfoUrl() + "?t=" + System.currentTimeMillis();
            if (!TextUtils.isEmpty(url)) {
                //检查是否有新版本
                mHttpClient.get(url, onGetVersionInfoHandler);
            }
            //检查是否有离线笔记
            checkStorageNoteNum();
        }
    };

    private Listeners.OnUploadCallback onUploadFirmwareCallback = new Listeners.OnUploadCallback() {
        @Override
        public void progress(int progresses) {
           // mView.showRetry(String.format(mContext.getString(R.string.wait_update_progress), progresses), true);
        }

        @Override
        public void complete(ResultState state) {
            //取消屏幕常亮控制
            BleConnectActivity.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            switch (state) {
                case OK:
                   // BleConnectActivity.this.alertMsg(mContext.getString(R.string.title_update_complete));
                    break;
                case ERROR_CHECKSUM:
                   // mFragment.getBaseActivity().alertMsg(mContext.getString(R.string.error_update_checksum));
                    break;
                default:
                  //  mFragment.getBaseActivity().alertMsg(mContext.getString(R.string.error_update));
                    break;
            }
        }
    };

    private AsyncHttpResponseHandler onGetVersionInfoHandler = new AsyncHttpResponseHandler() {

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            String ver = new String(responseBody);
            if (StringUtil.isNotEmpty(ver)) {
                int newVer = Integer.parseInt(ver.replace(".", ""));
                if (newVer >= mFirmwareVer) {
                    deviceDetail.setText("固件版本："+ver+"，可升级！");
                    deviceDetail.setVisibility(View.VISIBLE);
                } else {
                    deviceDetail.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            //mView.isNewFirmware(0);
            deviceDetail.setVisibility(View.GONE);
        }

    };

    /**
     * 连接设备
     *
     * @param address 蓝牙设备地址
     */
    public void connectDevice(String address) {
        mPenManage.setOnReceiveDataListener(onReceiveDataHandler);
        mPenManage.setOnUploadFirmwareCallback(onUploadFirmwareCallback);
        mPenManage.connectDevice(onConnectStateListener, address);
    }

    /**
     * 检查存储笔记数
     */
    private void checkStorageNoteNum() {
        int num = mPenManage.getStorageNoteNum();
        //S.i("StorageNoteNum:" + num);

        if (num > 0) {
            AlertDialog.Builder alert = new AlertDialog.Builder(BleConnectActivity.this);
            alert.setTitle("提示");
            alert.setMessage("共有"+num+"条数据可以同步！");
            alert.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mPenManage.startSyncNote();
                }
            });
            alert.setNegativeButton("取消", null);
            alert.show();
        }
    }

}
