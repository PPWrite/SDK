package cn.robotpen.demo.connect;

import android.app.ActivityGroup;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.robotpen.core.PenManage;
import cn.robotpen.core.services.UsbPenService;
import cn.robotpen.demo.R;
import cn.robotpen.model.entity.DeviceEntity;

public class DeviceActivity extends ActivityGroup {

    @BindView(R.id.devicesStatus)
    TextView devicesStatus;
    @BindView(R.id.gotoUsb)
    Button gotoUsb;
    @BindView(R.id.gotoBle)
    Button gotoBle;

    PenManage mPenManage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_device);
        ButterKnife.bind(this);
        gotoUsb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Intent intent = new Intent(DeviceActivity.this, USBConnectActivity.class);
                startActivity(intent);
            }
        });
        gotoBle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DeviceActivity.this, BleConnectActivity.class);
                startActivity(intent);
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

    /*
     * 检测设备连接
     */
    private void CheckDevice(){
        if (null == mPenManage) {
            mPenManage = new PenManage(this, UsbPenService.TAG); //这样新建服务会记住连接方式
        }
       DeviceEntity device =  mPenManage.getLastDevice(DeviceActivity.this);
        if(device!=null){
            devicesStatus.setText("已连接："+device.getName()+"。 "+"类型为："+device.getDeviceType().name());
        }else{
            devicesStatus.setText("未连接设备！");
        }
    }


}
