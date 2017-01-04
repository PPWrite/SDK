package cn.robotpen.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.robotpen.core.PenManage;
import cn.robotpen.core.services.UsbPenService;
import cn.robotpen.demo.connect.DeviceActivity;
import cn.robotpen.demo.show.StartActivity;
import cn.robotpen.model.entity.DeviceEntity;

public class MainActivity extends Activity {


    @BindView(R.id.main_connect_status)
    TextView mainConnectStatus;
    @BindView(R.id.main_connect_device)
    TextView mainConnectDevice;
    @BindView(R.id.main_connect_usb)
    TextView mainConnectUsb;
    @BindView(R.id.main_linear_device)
    LinearLayout mainLinearDevice;
    @BindView(R.id.main_show)
    TextView mainShow;
    @BindView(R.id.main_linear_show)
    LinearLayout mainLinearShow;
    @BindView(R.id.connect_device)
    LinearLayout connectDevice;
    @BindView(R.id.activity_main)
    LinearLayout activityMain;
    PenManage mPenManage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mainLinearDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DeviceActivity.class);
                startActivity(intent);
            }
        });

        mainLinearShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, StartActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        CheckDevice();
    }

    /*
    * 检测设备连接
    */
    private void CheckDevice(){
        if (null == mPenManage) {
            mPenManage = new PenManage(this, UsbPenService.TAG); //这样新建服务会记住连接方式
        }
        DeviceEntity device =  mPenManage.getLastDevice(MainActivity.this);
        if(device!=null){
            mainConnectStatus.setText("上次连接设备："+device.getName()+"。 ");
            mainConnectDevice.setText("类型为："+device.getDeviceType().name());
        }else {
            mainConnectStatus.setText("未连接设备！");
            mainConnectDevice.setText("");
        }
    }

}
