package cn.robotpen.demo.bluetooth;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.robotpen.core.services.PenService;
import cn.robotpen.core.services.SmartPenService;
import cn.robotpen.core.views.MultipleCanvasView;
import cn.robotpen.demo.Ant;
import cn.robotpen.demo.R;
import cn.robotpen.demo.RobotPenApplication;
import cn.robotpen.demo.utils.ResUtils;
import cn.robotpen.file.services.FileManageService;
import cn.robotpen.model.DeviceObject;
import cn.robotpen.model.symbol.Keys;
import cn.robotpen.model.symbol.SceneType;
import cn.robotpen.utils.LogUtil;

public class NoteWithTrailActivity extends Ant implements View.OnClickListener,MultipleCanvasView.CanvasManageInterface{
    private PenService mPenService ;
    private RobotPenApplication mApplication;
    private PageItem mPageItem;
    private MultipleCanvasView mPenCanvasView;

    private Long mUserId = 0L;//不要设置为null
    private ImageView.ScaleType mScaleType;
    private String mNoteKey;
    private PenServiceReceiver mPenServiceReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_with_trail);
        mApplication = RobotPenApplication.getInstance();
        mPageItem = new PageItem();
        Intent intent = getIntent();
        String tempNote = intent.getStringExtra(Keys.KEY_TARGET);
        if(tempNote!=null&&!tempNote.equals("")){
            mNoteKey = tempNote;
        }
        mPenServiceReceiver = new PenServiceReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Keys.ACTION_SERVICE_SEND_SYNC_NOTE_COMPLETE);
        registerReceiver(mPenServiceReceiver, intentFilter);
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
            mPenService.setSceneType(SceneType.INCH_101_BLE);// 设置场景值，用于坐标转化
            mPenService.setIsPressure(true);//允许获取压感值
            DeviceObject device = mPenService.getConnectDevice();
            if(device!=null&&!device.address.equals("")){
                mPageItem.deviceStatus.setText("已连接:");
                mPageItem.deviceName.setVisibility(View.VISIBLE);
                mPageItem.deviceName.setText(device.name);
                mPageItem.deviceType.setVisibility(View.VISIBLE);
                mPageItem.deviceType.setText(String.valueOf(device.type.getValue()));
//                DeviceType deviceType = mPenService.getConnectDeviceType();
//                int type = deviceType.getValue();
                //mPageItem.deviceVersion.setVisibility(View.VISIBLE);
              //  mPageItem.deviceVersion.setText(device.verMajor+"/"+device.verMinor);
                if(device.name.equals("Elite")){
                    mPageItem.synTrail.setEnabled(true);
                }
            }else{
                Toast.makeText(this,"请先连接蓝牙设备",Toast.LENGTH_SHORT).show();
            }
            // 如果要弹出确认则必须设置连接监听
            mPenCanvasView = new MultipleCanvasView(NoteWithTrailActivity.this, this);//画布只能通过new的方式创建
            mPenCanvasView.setDataSaveDir(ResUtils.getSavePath(ResUtils.DIR_NAME_DATA));
            mPageItem.lineWindow.addView(mPenCanvasView);
            mPenCanvasView.setPenIcon(R.drawable.ic_pen);
            mPenCanvasView.refresh();
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
    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()){
            case R.id.connectDevice:
                intent = new Intent(this,BleListActivity.class);
                this.startActivity(intent);
                break;
            case R.id.synTrail:
                //先判断是否连上设备
                if (mPenService != null) {

                    if (mPenService.getConnectDevice() == null) {
                        Toast.makeText(this,"请先连接蓝牙设备",Toast.LENGTH_SHORT).show();
                    } else {
                        int num = mPenService.getStorageNoteNum();
                        LogUtil.show(TAG, "StorageNoteNum:" + num);
                        if (num > 0) {
                            AlertDialog.Builder alert = new AlertDialog.Builder(this);
                            alert.setTitle("存在可同步的笔记");
                            alert.setMessage(String.format("您的设备上存在可以同步的笔记", num));
                            alert.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    showProgressDialog("正在同步,请稍后");
                                    ((SmartPenService) mPenService).startSyncNote();
                                }
                            });

                            alert.setNegativeButton("取消", null);
                            alert.show();
                        }
                    }
                }
            case R.id.manageTrail:
                intent = new Intent(this,TrailManageActivity.class);
                this.startActivity(intent);
                break;
            default:
                break;
        }
    }

    private class PenServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Keys.ACTION_SERVICE_SEND_SYNC_NOTE_COMPLETE)) {
                dismissProgressDialog();
                Toast.makeText(NoteWithTrailActivity.this,"同步成功",Toast.LENGTH_SHORT).show();
            }
        }
    }
//    private OnReceiveDataHandler onReceiveDataHandler = new OnReceiveDataHandler(){
//        @Override
//        public void version(int hw, int sw) {
//            //hw为硬件版本  sw为固件版本
//           mPageItem.deviceVersion.setText(PenDataUtil.getFormatHardwareVersion(hw)+"/"+PenDataUtil.getFormatFirmwareVersion(sw));
//        }
//    };
    @Override
    public MultipleCanvasView.PenModel getPenModel() {
        return MultipleCanvasView.PenModel.WaterPen;
    }

    @Override
    public float getPenWeight() {
        return 2;
    }

    @Override
    public int getPenColor() {
        return 0xFFFFFFFF;
    }

    @Override
    public int getBgColor() {
        return 0xFF37474F;
    }

    @Override
    public Uri getBgPhoto() {
        return null;
    }

    @Override
    public int getBgResId() {
        return 0;
    }

    @Override
    public ImageView.ScaleType getBgScaleType() {
        return mScaleType;
    }

    @Override
    public void onCanvasSizeChanged(int i, int i1, SceneType sceneType) {


    }

    @Override
    public float getIsRubber() {
        return 0.0f;
    }

    @Override
    public void penRouteStatus(boolean b) {

    }

    @Override
    public FileManageService getFileService() {
        return null;
    }

    @Override
    public long getCurrUserId() {
        return 0;
    }


    @Override
    public String getNoteKey() {
        return mNoteKey;
    }


    class PageItem{
        Button connectDevice;
        Button synTrail;
        Button manageTrail;
        TextView deviceStatus;
        TextView deviceName;
        TextView deviceType;
        TextView deviceVersion;
        RelativeLayout lineWindow;

        public PageItem() {
            connectDevice = (Button) findViewById(R.id.connectDevice);
            connectDevice.setOnClickListener(NoteWithTrailActivity.this);
            synTrail = (Button) findViewById(R.id.synTrail);
            synTrail.setOnClickListener(NoteWithTrailActivity.this);
            manageTrail = (Button) findViewById(R.id.manageTrail);
            manageTrail.setOnClickListener(NoteWithTrailActivity.this);
            lineWindow = (RelativeLayout) findViewById(R.id.lineWindow);
            deviceStatus = (TextView) findViewById(R.id.deviceStatus);
            deviceName = (TextView) findViewById(R.id.deviceName);
            deviceType = (TextView) findViewById(R.id.deviceType);
            deviceVersion = (TextView) findViewById(R.id.deviceVersion);
        }

    }
}
