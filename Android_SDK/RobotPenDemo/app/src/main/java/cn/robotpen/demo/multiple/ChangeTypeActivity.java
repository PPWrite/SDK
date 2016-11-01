package cn.robotpen.demo.multiple;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import cn.robotpen.core.services.PenService;
import cn.robotpen.core.views.MultipleCanvasView;
import cn.robotpen.demo.R;
import cn.robotpen.demo.RobotPenApplication;
import cn.robotpen.demo.bluetooth.BleListActivity;
import cn.robotpen.demo.utils.ResUtils;
import cn.robotpen.file.services.FileManageService;
import cn.robotpen.model.interfaces.Listeners;
import cn.robotpen.model.symbol.ConnectState;
import cn.robotpen.model.symbol.Keys;
import cn.robotpen.model.symbol.SceneType;

public class ChangeTypeActivity extends Activity implements Switch.OnCheckedChangeListener,View.OnClickListener,MultipleCanvasView.CanvasManageInterface{

    PageItem mPageItem;
    private PenService mPenService;
    private String mTag;
    private boolean isPenSvrBinding;
    private ProgressDialog mProgressDialog;
    private Handler mHandler = new Handler();
    private MultipleCanvasView mPenCanvasView;
    private String mNoteKey = "0";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_type);
        mPageItem = new PageItem();
        mTag = Keys.APP_USB_SERVICE_NAME;//默认为USB连接形式
        // 启动USB服务
        RobotPenApplication.getInstance().bindPenService(mTag);
        Intent intent = getIntent();
        String tempNote = intent.getStringExtra(Keys.KEY_TARGET);
        if(tempNote!=null&&!tempNote.equals("")){
            mNoteKey = tempNote;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPenService == null) {
            mProgressDialog = ProgressDialog.show(this, "", getString(R.string.service_usb_start), true);
            // 启动笔服务
            initPenService();
        }
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        if (mPenService != null) {
            RobotPenApplication.getInstance().unBindPenService();//断开笔服务
        }
    }
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked){
            mPageItem.connectDevice.setEnabled(true);
            mPageItem.deviceType.setText("蓝牙");
            mTag = Keys.APP_PEN_SERVICE_NAME;//切换为蓝牙
        }else{
            mPageItem.connectDevice.setEnabled(false);
            mPageItem.deviceType.setText("USB");
            mTag = Keys.APP_USB_SERVICE_NAME;//切换为蓝牙
        }
        changePenService(mTag);
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.connectDevice){
           Intent intent = new Intent(this,BleListActivity.class);
            this.startActivity(intent);
        }else if(v.getId()==R.id.deviceType){
            Toast.makeText(ChangeTypeActivity.this,"当前设定的笔服务为:"+mTag,Toast.LENGTH_SHORT).show();
            String tag = RobotPenApplication.getInstance().getConnectDeviceType();
            Toast.makeText(ChangeTypeActivity.this,"当前连接的设备类型为:"+tag,Toast.LENGTH_SHORT).show();
        }

    }



    /**
     * 初始化笔服务
     */
    public void initPenService() {
        //如果正在执行，那么退出
        if(isPenSvrBinding) return;
        mPenService = RobotPenApplication.getInstance().getPenService();
        if (mPenService == null)
            RobotPenApplication.getInstance().bindPenService(mTag);

        isPenServiceReady(mTag);
    }
    /**
     * 判断笔服务是否已启动完毕,如果未启动则一直确认到启动为止
     */
    private void isPenServiceReady(String mTag) {
        isPenSvrBinding = true;
        mPenService = RobotPenApplication.getInstance().getPenService();
        if (mPenService != null) {
            dismissProgressDialog();
            isPenSvrBinding = false;
            penSvrInitComplete();
            String tag = RobotPenApplication.getInstance().getConnectDeviceType();
            Toast.makeText(ChangeTypeActivity.this,"已成功启动笔服务"+tag,Toast.LENGTH_SHORT).show();
        } else {
            final String currentTag = mTag;
            if(mTag.equals(Keys.APP_USB_SERVICE_NAME)){
                showProgressDialog("正在加载USB服务……", true);
            }else {
                showProgressDialog("正在加载蓝牙服务……", true);
            }
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isPenServiceReady(currentTag);
                }
            }, 500);
        }
    }
    /**
     * 切换为指定的笔服务
     */
    private void changePenService(String tag) {
        if (!mPenService.getSvrTag().equals(tag)) {
            //解除绑定
            RobotPenApplication.getInstance().unBindPenService();
            isUnBindPenService(tag);
        }
    }
    /**
     * 判断服务是否已解绑完成，解绑完成后根据tag绑定新的服务
     */
    private void isUnBindPenService(final String bindTag){
        if(mPenService == null || !mPenService.getIsBind()){
            RobotPenApplication.getInstance().setConnectDeviceType(bindTag);
            initPenService();
        }else{
            if(mTag.equals(Keys.APP_USB_SERVICE_NAME)){
                showProgressDialog("正在绑定USB服务……", true);
            }else {
                showProgressDialog("正在绑定蓝牙服务……", true);
            }
           mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isUnBindPenService(bindTag);
                }
            }, 500);
        }
    }
    /**
     * 笔服务成功后,操作……此处可以自定义
     */
    private void  penSvrInitComplete(){
        if (mTag.equals(Keys.APP_USB_SERVICE_NAME)){
            mPenService.setSceneType(SceneType.INCH_101);// 设置场景值，用于坐标转化
            mPenService.setIsPressure(true);//允许获取压感值
            mPenService.setOnConnectStateListener(onConnectStateListener);// 如果要弹出确认则必须设置连接监听
        }else if(mTag.equals(Keys.APP_PEN_SERVICE_NAME)){
            mPenService.setSceneType(SceneType.INCH_101_BLE);// 设置场景值，用于坐标转化
            mPenService.setIsPressure(true);//允许获取压感值
        }
        if(mPenCanvasView==null){
            mPenCanvasView = new MultipleCanvasView(ChangeTypeActivity.this, this);//画布只能通过new的方式创建
            mPenCanvasView.setDataSaveDir(ResUtils.getSavePath(ResUtils.DIR_NAME_DATA));
            mPageItem.lineWindow.addView(mPenCanvasView);
            mPenCanvasView.setPenIcon(R.drawable.ic_pen);
            mPenCanvasView.refresh();
        }else {
            mPenCanvasView.refresh(); //这里一定要重新刷新一下画布
        }
    }

    /**
	 * 弹出授权
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

    @Override
    public PenService getPenService() {
        return mPenService;
    }

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
        return ImageView.ScaleType.CENTER;
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

        Switch switchButton;
        Button connectDevice;
        TextView deviceType;
        RelativeLayout lineWindow;

        public PageItem(){
            switchButton = (Switch) findViewById(R.id.switch1);
            switchButton.setOnCheckedChangeListener(ChangeTypeActivity.this);
            connectDevice= (Button) findViewById(R.id.connectDevice);
            connectDevice.setOnClickListener(ChangeTypeActivity.this);
            deviceType = (TextView) findViewById(R.id.deviceType);
            deviceType.setOnClickListener(ChangeTypeActivity.this);
            lineWindow = (RelativeLayout) findViewById(R.id.lineWindow);
        }
    }

    /**
    *显示对话框
     **/
    public boolean showProgressDialog(String msg,boolean isNew) {
        boolean isCreate = true;
        if (isNew) {
            if (mProgressDialog != null) dismissProgressDialog();
            mProgressDialog = ProgressDialog.show(this, "", msg, true);
        } else {
            if (mProgressDialog == null) {
                mProgressDialog = ProgressDialog.show(this, "", msg, true);
            }else{
                isCreate = false;
            }
        }
        return isCreate;
    }
    /**
     *关闭对话框
     **/
    public void dismissProgressDialog(){
        if(mProgressDialog != null){
            if(mProgressDialog.isShowing())
                mProgressDialog.dismiss();

            mProgressDialog = null;
        }
    }
}
