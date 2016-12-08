package robotpent.robotpen_sampledemo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.robotpen.core.PenManage;
import cn.robotpen.core.services.PenService;
import cn.robotpen.core.services.UsbPenService;
import cn.robotpen.model.PointObject;
import cn.robotpen.model.entity.SettingEntity;
import cn.robotpen.model.symbol.ConnectState;
import cn.robotpen.model.symbol.SceneType;

public class UsbActivity extends Activity {
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
    private PenManage mPenManage;
    private Handler mHandler;
    private SettingEntity settingEntity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb);
        ButterKnife.bind(this);
        settingEntity = new SettingEntity(this);
        //设置压感
        settingEntity.setPressure(true);

        mPenManage = new PenManage(this, UsbPenService.TAG); //这样新建服务会记住连接方式
        mPenManage.setScanTime(2000);
        mPenManage.setSceneObject(SceneType.P1); //如果是横屏使用则设置为P1_H
        mPenManage.setOnConnectStateListener(onConnectStateListener);
        mPenManage.setOnPointChangeListener(onPointChangeListener);
        mHandler = new Handler();
    }

    @Override
    protected void onResume() {
        super.onResume();
       if(null==mPenManage){
           mPenManage = new PenManage(this, UsbPenService.TAG); //这样新建服务会记住连接方式
           mPenManage.setScanTime(2000);//此种方式创建的PenManage对象将记住服务类型，USB连接推荐此种创建方式
           mPenManage.setSceneObject(SceneType.P1); //如果是横屏使用则设置为P1_H
           //mPenManage.setOnConnectStateListener(onConnectStateListener);//set过一次后不需要再设置 ,不set就会出现授权框
           mPenManage.scanDevice(null);
           mPenManage.setOnPointChangeListener(onPointChangeListener);
       }else{
           mPenManage.scanDevice(null);
           mPenManage.setOnPointChangeListener(onPointChangeListener);
       }
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

    /*
            * 此处监听是为了弹出授权
            */
    private PenService.OnConnectStateListener onConnectStateListener = new PenService.OnConnectStateListener() {
        @Override
        public void stateChange(String arg0, ConnectState arg1) {
            if (arg1 == ConnectState.CONNECTED) {
                Toast.makeText(UsbActivity.this, "设备已连接且连接成功！", Toast.LENGTH_SHORT).show();
                Toast.makeText(UsbActivity.this, "是否设置了压感！" + settingEntity.isPressure(), Toast.LENGTH_SHORT).show();
                connectDeviceType.setText(mPenManage.getConnectDeviceType().name());
            } else if (arg1 == ConnectState.DISCONNECTED) {
                Toast.makeText(UsbActivity.this, "设备已断开", Toast.LENGTH_SHORT).show();
            }
        }
    };
    /*
        * 通过监听方式完成业务处理 这里接收笔的信息建议通过监听，通过广播方式也是可以的，但是广播方式效率较低
        */
    private PenService.OnPointChangeListener onPointChangeListener = new PenService.OnPointChangeListener() {

        @Override
        public void change(final PointObject point) {
            // post子线程，查看坐标中的各个字段
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    connectSenceType.setText(point.getSceneType().name());
                    connectDeviceSize.setText(point.getWidth() + "/" + point.getHeight());
                    penBattery.setText(String.valueOf(point.battery.getValue()));
                    penIsRoute.setText(String.valueOf(point.isRoute));
                    penWeight.setText(String.valueOf(point.weight));
                    penColor.setText(String.valueOf(point.color));
                    penPress.setText(point.pressure + "/" + point.pressureValue);
                    penOriginal.setText(point.originalX + "/" + point.originalY);
                    connectOffest.setText(point.getOffsetX() + "/" + point.getOffsetY());
                }
            });
        }

        @Override
        public void onButClick(int i) {

        }
    };

}
