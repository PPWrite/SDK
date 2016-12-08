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
import cn.robotpen.demo.connect.DeviceActivity;
import cn.robotpen.demo.show.StartActivity;

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
        if(null==mPenManage){
            mPenManage = new PenManage(MainActivity.this);
        }
        if(null!=mPenManage.getConnectDevice()){
            mainConnectStatus.setText("已连接设备：");
            mainConnectDevice.setText(mPenManage.getConnectDevice().getName());
        }else {
            mainConnectStatus.setText("未连接设备！");
            mainConnectDevice.setText("暂无设备连接");
        }

    }
}
