package cn.robotpen.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.robotpen.demo.connect.DeviceActivity;

public class MainActivity extends Activity {

    @BindView(R.id.gotoConnect)
    Button gotoConnect;
    @BindView(R.id.gotoStart)
    Button gotoStart;
    @BindView(R.id.activity_main)
    LinearLayout activityMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        gotoConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,DeviceActivity.class);
                startActivity(intent);
            }
        });

        gotoStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,StartActivity.class);
                startActivity(intent);
            }
        });
    }
}
