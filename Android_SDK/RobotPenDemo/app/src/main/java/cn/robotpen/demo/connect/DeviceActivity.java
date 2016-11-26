package cn.robotpen.demo.connect;

import android.app.ActivityGroup;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabWidget;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.robotpen.core.services.SmartPenService;
import cn.robotpen.core.services.UsbPenService;
import cn.robotpen.demo.R;

public class DeviceActivity extends ActivityGroup {

    @BindView(android.R.id.tabs)
    TabWidget tabs;
    @BindView(R.id.linearLayout)
    LinearLayout linearLayout;
    @BindView(R.id.linearLayout2)
    LinearLayout linearLayout2;
    @BindView(android.R.id.tabcontent)
    FrameLayout tabcontent;

    @BindView(R.id.tabHost)
    TabHost mTabHost;
    @BindView(R.id.container)
    ViewPager mViewPager;

    FragmentPagerAdapter pagerAdapter;
    List<Fragment> pageData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_device);
        ButterKnife.bind(this);

        mTabHost.setup();
        mTabHost.setup(this.getLocalActivityManager());

        mTabHost.addTab(mTabHost.newTabSpec(UsbPenService.TAG)
                .setIndicator("USB连接").setContent(R.id.linearLayout));
        mTabHost.addTab(mTabHost.newTabSpec(SmartPenService.TAG)
                .setIndicator("蓝牙连接").setContent(R.id.linearLayout2));

        pagerAdapter.setPageData(pagerData);
//        mViewPager.setAdapter(pagerAdapter);
    }


}
