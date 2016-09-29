package cn.robotpen.demo.bluetooth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import cn.robotpen.core.services.PenService;
import cn.robotpen.demo.Ant;
import cn.robotpen.demo.R;
import cn.robotpen.demo.RobotPenApplication;
import cn.robotpen.model.DeviceObject;
import cn.robotpen.model.PointObject;
import cn.robotpen.model.interfaces.Listeners;
import cn.robotpen.model.symbol.ConnectState;
import cn.robotpen.model.symbol.Keys;
import cn.robotpen.model.symbol.SceneType;

public class GetdataActivity extends Ant implements View.OnClickListener{
    private String[] mItems = { "蓝牙版10.1寸竖屏", "蓝牙版10.1寸横屏" };
    private PenService mPenService ;
    private RobotPenApplication mApplication;
    private PageItem mPageItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_getaxes);
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
            //Toast.makeText(this,"蓝牙服务已准备就绪",Toast.LENGTH_SHORT).show();
            DeviceObject device = mPenService.getConnectDevice();
            if(device!=null&&!device.address.equals("")){
                //Toast.makeText(this,device.name,Toast.LENGTH_SHORT).show();
                mPageItem.deviceStatus.setText("已连接设备: "+device.name);
                mPageItem.deviceStatus.setVisibility(View.VISIBLE);
                mPenService.setSceneType(SceneType.INCH_101_BLE);// 设置场景值，用于坐标转化
                mPenService.setIsPressure(true);//允许获取压感值
                mPenService.setOnPointChangeListener(onPointChangeListener);

            }else{
                Toast.makeText(this,"请先连接蓝牙设备",Toast.LENGTH_SHORT).show();
            }
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
    /*
	 * 通过监听方式完成业务处理 这里接收笔的信息建议通过监听，通过广播方式也是可以的，但是广播方式效率较低
	 */
    private Listeners.OnPointChangeListener onPointChangeListener = new Listeners.OnPointChangeListener() {
        @Override
        public void change(PointObject point) {
            // TODO Auto-generated method stub
            // 设置看坐标中的各个字段
            mPageItem.originalX.setText(String.valueOf(point.originalX));
            mPageItem.originalY.setText(String.valueOf(point.originalY));
            mPageItem.isRoute.setText(String.valueOf(point.isRoute));
            mPageItem.pressure.setText(String.valueOf(point.pressure) + "(" + String.valueOf(point.pressureValue) + ")");

            mPageItem.sceneType1.setText(String.valueOf(point.getSceneType()));
            mPageItem.sceneWidth.setText(String.valueOf(point.getWidth(mPenService.getSceneType())));
            mPageItem.sceneHeight.setText(String.valueOf(point.getHeight(mPenService.getSceneType())));
            mPageItem.sceneOffsetX.setText(String.valueOf(point.getOffsetX()));
            mPageItem.sceneOffsetY.setText(String.valueOf(point.getOffsetY()));
        }

        @Override
        public void change(List<PointObject> arg0) {
            // TODO Auto-generated method stub

        }
    };
    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()){
            case R.id.deviceBut:
                intent = new Intent(this,BleListActivity.class);
                this.startActivity(intent);
                break;
            default:
                break;
        }
    }

    class PageItem{
        Button deviceBut;
        TextView deviceStatus;
        private Spinner mSceneType;
        private TextView isRoute; // 是否写入状态
        private TextView pressure; // 压感
        private TextView originalX;
        private TextView originalY;

        private TextView sceneType1;
        private TextView sceneWidth;
        private TextView sceneHeight;
        private TextView sceneOffsetX;
        private TextView sceneOffsetY;
        public PageItem() {
            deviceBut = (Button) findViewById(R.id.deviceBut);
            deviceBut.setText("连接蓝牙设备");
            deviceBut.setOnClickListener(GetdataActivity.this);
            deviceStatus = (TextView) findViewById(R.id.deviceStatus);

            mSceneType = (Spinner) findViewById(R.id.sceneType);
            // 笔的信息
            isRoute = (TextView) findViewById(R.id.isRoute);
            pressure = (TextView) findViewById(R.id.pressure);
            originalX = (TextView) findViewById(R.id.originalX);
            originalY = (TextView) findViewById(R.id.originalY);
            // 设备板信息
            sceneType1 = (TextView) findViewById(R.id.sceneType1);
            sceneWidth = (TextView) findViewById(R.id.sceneWidth);
            sceneHeight = (TextView) findViewById(R.id.sceneHeight);
            sceneOffsetX = (TextView) findViewById(R.id.sceneOffsetX);
            sceneOffsetY = (TextView) findViewById(R.id.sceneOffsetY);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(GetdataActivity.this, android.R.layout.simple_spinner_item, mItems);
            mSceneType.setAdapter(adapter);
            mSceneType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // TODO Auto-generated method stub
                    switch (position) {
                        case 0:
                            if (null != mPenService){
                                mPenService.setSceneType(SceneType.INCH_101_BLE);
                                mPenService.setIsPressure(true);
                            }
                            break;
                        case 1:
                            if (null != mPenService)
                                mPenService.setSceneType(SceneType.INCH_101_BLE_horizontal);
                            break;
                        default:
                            break;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // TODO Auto-generated method stub

                }
            });

        }

    }

}
