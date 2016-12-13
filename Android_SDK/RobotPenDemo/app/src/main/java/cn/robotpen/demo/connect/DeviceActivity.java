package cn.robotpen.demo.connect;

import android.app.ActivityGroup;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.robotpen.demo.R;

public class DeviceActivity extends ActivityGroup {

//    @BindView(android.R.id.tabs)
//    TabWidget tabs;
//    @BindView(R.id.linearLayout)
//    LinearLayout linearLayout;
//    @BindView(R.id.linearLayout2)
//    LinearLayout linearLayout2;
//    @BindView(android.R.id.tabcontent)
//    FrameLayout tabcontent;
//
//    @BindView(R.id.tabHost)
//    TabHost mTabHost;
//    @BindView(R.id.container)
//    ViewPager mViewPager;

    FragmentPagerAdapter pagerAdapter;
    List<Fragment> pageData;
    @BindView(R.id.devicesStatus)
    TextView devicesStatus;
    @BindView(R.id.gotoUsb)
    Button gotoUsb;
    @BindView(R.id.gotoBle)
    Button gotoBle;

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


}
