package cn.robotpen.demo.connect;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.robotpen.demo.R;

public class BleConnectActivity extends Activity {

    @BindView(R.id.statusText)
    TextView statusText;
    @BindView(R.id.deviceDetail)
    Button deviceDetail;
    @BindView(R.id.listview)
    ListView listview;
    @BindView(R.id.scanBut)
    Button scanBut;
    @BindView(R.id.disconnectBut)
    Button disconnectBut;
    @BindView(R.id.listFrame)
    LinearLayout listFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_connect);
        ButterKnife.bind(this);
    }

}