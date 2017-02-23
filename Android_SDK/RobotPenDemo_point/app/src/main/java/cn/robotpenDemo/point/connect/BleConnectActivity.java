package cn.robotpenDemo.point.connect;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.codingmaster.slib.S;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.robotpen.model.entity.DeviceEntity;
import cn.robotpen.model.symbol.DeviceType;
import cn.robotpen.pen.callback.RobotPenActivity;
import cn.robotpen.pen.model.RemoteState;
import cn.robotpen.pen.model.RobotDevice;
import cn.robotpen.pen.scan.RobotScanCallback;
import cn.robotpen.pen.scan.RobotScannerCompat;
import cn.robotpenDemo.point.R;


public class BleConnectActivity extends RobotPenActivity {

    private final UUID SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");

    @BindView(R.id.statusText)
    TextView statusText;
    @BindView(R.id.listview)
    ListView listview;
    @BindView(R.id.scanBut)
    Button scanBut;
    @BindView(R.id.disconnectBut)
    Button disconnectBut;
    @BindView(R.id.listFrame)
    LinearLayout listFrame;
    @BindView(R.id.deviceUpdate)
    Button deviceUpdate;
    @BindView(R.id.deviceSync)
    Button deviceSync;


    private PenAdapter mPenAdapter;
    //    SharedPreferences lastSp;
    SharedPreferences pairedSp;
    ProgressDialog mProgressDialog;
    RobotDevice mRobotDevice;//连接上的设备
    String mNewVersion; //从网络获取的最新版本号
    /**
     * 上次配对信息
     */
    public static final String SP_LAST_PAIRED = "last_paired_device";
    /**
     * 记录配对信息
     */
    public static final String SP_PAIRED_DEVICE = "sp_paird";
    /**
     * 关键字
     */
    public static final String SP_PAIRED_KEY = "address";
    /**
     * 固件升级URL
     */
    public static final String FIREWARE_FILE_HOST = "http://dl.robotpen.cn/fw/";
    public static final int SUCCESS = 0;
    public static final int ERRORCODE = 1;
    public static final int FAILURE = 2;
    public static final int UPDATESUCCESS = 3;
    public static final int UPDATEFAILURE = 4;
    /**
     * 当有扫描结果时的回调
     */
    RobotScannerCompat robotScannerCompat;
    RobotScanCallback scanCallback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_connect);
        ButterKnife.bind(this);
        mPenAdapter = new PenAdapter(BleConnectActivity.this);
        //获取存储存储
//        lastSp = this.getSharedPreferences(SP_LAST_PAIRED, MODE_PRIVATE);
        pairedSp = this.getSharedPreferences(SP_PAIRED_DEVICE, MODE_PRIVATE);

        listview.setAdapter(mPenAdapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {
                //停止搜索
                stopScan();
                DeviceEntity device = mPenAdapter.getItem(index);
                String addr = device.getAddress();
                try {
                    if (getPenService().getConnectedDevice() == null) {
                        getPenService().connectDevice(addr);//通过监听获取连接状态
                    } else {
                        Toast.makeText(BleConnectActivity.this, "先断开当前设备", Toast.LENGTH_SHORT).show();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
        scanCallback = new MyScanCallback(this);
        robotScannerCompat = new RobotScannerCompat(scanCallback);
    }

    public void addRobotDevice2list(BluetoothDevice bluetoothDevice) {
        DeviceEntity device = new DeviceEntity(bluetoothDevice);
        mPenAdapter.addItem(device);
        mPenAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        stopScan();
        robotScannerCompat = null;
        scanCallback = null;
        super.onDestroy();
    }

    @OnClick({R.id.scanBut, R.id.disconnectBut, R.id.deviceSync, R.id.deviceUpdate})
    void OnClick(View view) {
        switch (view.getId()) {
            case R.id.scanBut:
                checkPermission();
                break;
            case R.id.disconnectBut:
                try {
                    getPenService().disconnectDevice();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                scanBut.setVisibility(View.VISIBLE);
                disconnectBut.setVisibility(View.GONE);
                statusText.setText("未连接设备!");
                break;
            case R.id.deviceSync:
                showProgress("同步中");
                checkStorageNoteNum(mRobotDevice);//同步笔记
                break;
            case R.id.deviceUpdate:
                showProgress("升级中");
                updateDevice(mRobotDevice);
                break;
        }
    }

    /**
     * 蓝牙未开启请求
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 0xb) {
        }
    }

    @Override
    public void onUpdateFirmwareFinished() {
        deviceUpdate.setVisibility(View.GONE);
        Toast.makeText(BleConnectActivity.this, "固件升级完毕", Toast.LENGTH_SHORT).show();

    }

    /**--------------
     * 设备连接部分
     -----------------*/
    /**
     * 校验蓝牙是否打开
     * 6.0以上使用蓝牙的相关权限是否具备
     * ACCESS_COARSE_LOCATION 必须校验
     */
    public void checkPermission() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "对不起，您的设备不支持蓝牙,即将退出", Toast.LENGTH_SHORT).show();
            finish();
        } else if (!mBluetoothAdapter.isEnabled()) {//蓝牙未开启
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            this.startActivityForResult(enableBtIntent, 0xb);
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mPenAdapter.clearItems();
            mPenAdapter.notifyDataSetChanged();
            S.i("");
            startScan();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }
    }

    /**
     * 服务连接成功后需要实现
     */
    public void onServiceConnected(ComponentName name, IBinder service) {
        super.onServiceConnected(name, service);
        S.i("");
        checkDevice();//检测设备如果连接过则自动连接
    }

    /**
     * 检测设备连接 如果本次连接的是P1则禁止使用如果本次连接的是蓝牙设备则不处理
     * 如果本次未连接但上次已连接蓝牙设备则直接连接
     * 只有在onServiceConnected之后robotService才可以正常使用
     **/
    private void checkDevice() {
        try {
            RobotDevice robotDevice = getPenService().getConnectedDevice(); //获取目前连接的设备
            if (robotDevice != null) {//已连接设备
                statusText.setText("已连接设备: " + robotDevice.getProductName());
                if (robotDevice.getDeviceType() == DeviceType.P1.getValue()) { //已连接设备
                    Toast.makeText(this, "请先断开USB设备再进行蓝牙设备连接", Toast.LENGTH_SHORT).show();
                    scanBut.setVisibility(View.GONE);
                } else {
                    disconnectBut.setVisibility(View.VISIBLE);
                    scanBut.setVisibility(View.GONE);
                }
            } else {
                //获取上次连接设备
//                if (!pairedSp.getString(SP_PAIRED_KEY, "").isEmpty()) {
//                    //已经连接过蓝牙设备 从pairedSp中获取
//                    String laseDeviceAddress = pairedSp.getString(SP_PAIRED_KEY, "");
//                    getPenService().connectDevice(laseDeviceAddress);
//                    showProgress("正在检测上次连接的设备");
//                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    /**
     * 开始扫描Ble设备--带过滤
     */
    public void startScan() {
        S.i("");
        robotScannerCompat.startScan();
    }

    /**
     * 停止扫描Ble设备
     */
    public void stopScan() {
        robotScannerCompat.stopScan();
    }

    /**
     * 保存设备的连接信息
     *
     * @param device
     * @param name
     * @param addr
     */
    private void saveConnectInfo(RobotDevice device, String name, String addr) {
//        SharedPreferences.Editor edit = lastSp.edit().clear();
        if (!TextUtils.isEmpty(addr)) {
            pairedSp.edit()
                    .putString(SP_PAIRED_KEY, addr)
                    .apply();
//            edit.putString(String.valueOf(device.getDeviceType()), addr);
        }
//        edit.apply();
    }
    /**--------------
     * 笔迹同步部分
     -----------------*/
    /**
     * 检查存储笔记数
     */
    private void checkStorageNoteNum(RobotDevice device) {
        int num = device.getOfflineNoteNum();
        if (num > 0) {
            deviceSync.setVisibility(View.VISIBLE);
            AlertDialog.Builder alert = new AlertDialog.Builder(BleConnectActivity.this);
            alert.setTitle("提示");
            alert.setMessage("共有" + num + "条数据可以同步！");
            alert.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        getPenService().startSyncOffLineNote();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    dialog.dismiss();
                }
            });
            alert.setNegativeButton("取消", null);
            alert.show();
        }
    }
    /**--------------
     * 设备升级部分
     -----------------*/
    /**
     * 固件升级的相关回调
     */
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SUCCESS:
                    if (mRobotDevice != null) {
                        String device_firmwareVer = mRobotDevice.getFirmwareVerStr();
                        String newVersion = msg.obj.toString();
                        if (device_firmwareVer.compareTo(newVersion) > 0) { //存在新版
                            deviceUpdate.setVisibility(View.VISIBLE);
                            mNewVersion = newVersion;
                        } else {
                            Toast.makeText(BleConnectActivity.this, "不需要更新固件", Toast.LENGTH_SHORT).show();
                            deviceUpdate.setVisibility(View.GONE);
                        }
                    }
                    break;
                case FAILURE:
                    Toast.makeText(BleConnectActivity.this, "获取数据失败", Toast.LENGTH_SHORT)
                            .show();
                    break;
                case ERRORCODE:
                    Toast.makeText(BleConnectActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show();
                    break;
                case UPDATESUCCESS:
                    if (getPenService() != null) {
                        byte[] newFirmwareVer = (byte[]) msg.obj;
                        try {
                            getPenService().startUpdateFirmware(mNewVersion, newFirmwareVer);
                            //升级结果可以通过RemoteCallback 进行展示
                            //此时注意观察设备为紫灯常亮，直到设备升级完毕将自动进行重启
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case UPDATEFAILURE:
                    Toast.makeText(BleConnectActivity.this, "升级失败！", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }

        ;
    };

    /**
     * 检查设备固件版本
     */
    private void checkDeviceVersion(RobotDevice device) {
        final String otaFileName = device.getProductName() + "_svrupdate.txt";
        new Thread() {
            public void run() {
                int code;
                try {
                    URL url = new URL(FIREWARE_FILE_HOST + otaFileName);
                    HttpURLConnection conn = (HttpURLConnection) url
                            .openConnection();
                    conn.setRequestMethod("GET");//使用GET方法获取
                    conn.setConnectTimeout(5000);
                    code = conn.getResponseCode();
                    if (code == 200) {
                        InputStream is = conn.getInputStream();
                        String result = readMyInputStream(is);
                        Message msg = new Message();
                        msg.obj = result;
                        msg.what = SUCCESS;
                        mHandler.sendMessage(msg);
                    } else {
                        Message msg = new Message();
                        msg.what = ERRORCODE;
                        mHandler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Message msg = new Message();
                    msg.what = FAILURE;
                    mHandler.sendMessage(msg);
                }
            }
        }.start();
    }

    /**
     * 升级固件版本
     */
    private void updateDevice(RobotDevice device) {
        final String path = generatorFirmwareUrl(device, mNewVersion);
        new Thread() {
            public void run() {
                try {
                    URL url = new URL(path);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setDoOutput(false);
                    urlConnection.setConnectTimeout(10 * 1000);
                    urlConnection.setReadTimeout(10 * 1000);
                    urlConnection.setRequestProperty("Connection", "Keep-Alive");
                    urlConnection.setRequestProperty("Charset", "UTF-8");
                    urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");
                    urlConnection.connect();
                    InputStream in = urlConnection.getInputStream();
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    int bytetotal = urlConnection.getContentLength();
                    int bytesum = 0;
                    int byteread;
                    byte[] buffer = new byte[1024];
                    while ((byteread = in.read(buffer)) != -1) {
                        bytesum += byteread;
                        outputStream.write(buffer, 0, byteread);
                    }
                    outputStream.flush();
                    outputStream.close();
                    in.close();
                    byte[] result = outputStream.toByteArray();
                    outputStream.close();
                    Message msg = new Message();
                    msg.obj = result;
                    msg.what = UPDATESUCCESS;
                    mHandler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                    Message msg = new Message();
                    msg.what = UPDATEFAILURE;
                    mHandler.sendMessage(msg);
                }
            }
        }.start();
    }

    /**
     * 生成固件升级url
     *
     * @param device
     * @return
     */
    private String generatorFirmwareUrl(RobotDevice device, String lastFirmwareVer) {
        return FIREWARE_FILE_HOST + device.getProductName() + "_" + lastFirmwareVer + ".bin";
    }

    /**
     * Stream转String
     *
     * @param is
     * @return
     */
    public static String readMyInputStream(InputStream is) {
        byte[] result;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            is.close();
            baos.close();
            result = baos.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
            String errorStr = "获取数据失败。";
            return errorStr;
        }
        return new String(result);
    }


    /**
     * 显示ProgressDialog
     **/
    private void showProgress(String flag) {
        mProgressDialog = ProgressDialog.show(this, "", flag + "……", true);
    }

    /**
     * 释放progressDialog
     **/
    private void closeProgress() {
        if (mProgressDialog != null) {
            if (mProgressDialog.isShowing())
                mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    @Override
    public void onStateChanged(int i, String s) {
        switch (i) {
            case RemoteState.STATE_CONNECTED:
                break;
            case RemoteState.STATE_CONNECTING:
                break;
            case RemoteState.STATE_DISCONNECTED: //设备断开
                closeProgress();
                statusText.setText("未连接设备！");
                scanBut.setVisibility(View.VISIBLE);
                disconnectBut.setVisibility(View.GONE);
                break;
            case RemoteState.STATE_DEVICE_INFO: //设备连接成功状态
                try {
                    RobotDevice robotDevice = getPenService().getConnectedDevice();
                    if (null != robotDevice) {
                        closeProgress();
                        mRobotDevice = robotDevice;
                        if (robotDevice.getDeviceType() > 0) {//针对固件bug进行解决 STATE_DEVICE_INFO 返回两次首次无设备信息第二次会上报设备信息
                            statusText.setText("已连接设备: " + robotDevice.getProductName());
                            if (robotDevice.getDeviceType() == DeviceType.P1.getValue()) { //如果连接上的是usb设备
                                Toast.makeText(BleConnectActivity.this, "请先断开USB设备再进行蓝牙设备连接", Toast.LENGTH_SHORT).show();
                                scanBut.setVisibility(View.GONE);
                                disconnectBut.setVisibility(View.GONE);
                            } else {//如果连接的是蓝牙设备
                                saveConnectInfo(robotDevice, robotDevice.getName(), robotDevice.getAddress());
                                scanBut.setVisibility(View.GONE);
                                disconnectBut.setVisibility(View.VISIBLE);
                                //如果有离线笔记则同步离线笔记
                                //checkStorageNoteNum(robotDevice);
                                if (robotDevice.getOfflineNoteNum() > 0) {
                                    deviceSync.setVisibility(View.VISIBLE);
                                } else
                                    deviceSync.setVisibility(View.GONE);
                                //进行版本升级
                                checkDeviceVersion(robotDevice);
                            }
                        }
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case RemoteState.STATE_ENTER_SYNC_MODE_SUCCESS://笔记同步成功
                deviceSync.setVisibility(View.GONE);
                Toast.makeText(BleConnectActivity.this, "笔记同步成功", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onPenServiceError(String s) {

    }

    static class MyScanCallback extends RobotScanCallback {
        BleConnectActivity act;

        public MyScanCallback(BleConnectActivity a) {
            act = new WeakReference<>(a).get();
        }

        @Override
        public void onResult(BluetoothDevice bluetoothDevice, int i, boolean b) {
            act.addRobotDevice2list(bluetoothDevice);
        }

        @Override
        public void onFailed(int i) {

        }
    }
}
