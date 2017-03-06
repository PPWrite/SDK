package cn.robotpenDemo.point;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import cn.robotpen.pen.adapter.OnPenConnectListener;
import cn.robotpen.pen.adapter.RobotPenAdapter;
import cn.robotpenDemo.point.connect.BleConnectTwoActivity;

/**
 * Created by wang on 2017/3/3.
 */

public class BaseTwoActivity extends AppCompatActivity implements OnPenConnectListener<String>,Handler.Callback {

    public RobotPenAdapter<BaseTwoActivity, String> adapter;
    private Handler mHandler ;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler(this);
        progressDialog=new ProgressDialog(BaseTwoActivity.this);
        progressDialog.setMessage("正在初始化");
        progressDialog.show();
        try {
            adapter = new RobotPenAdapter<BaseTwoActivity, String>(this, this) {
                @Override
                protected String convert(byte[] bytes) {
                    return new String(bytes);
                }
            };
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        boolean result = adapter.init();
        if(!result){
            Toast.makeText(BaseTwoActivity.this,"初始化失败",Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public boolean handleMessage(Message msg) {
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(progressDialog!=null){
            progressDialog.dismiss();
            progressDialog=null;
        }
        adapter.release();
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;

    }


    @Override
    public void onPenServiceStarted() {
        if(progressDialog!=null){
            progressDialog.dismiss();
            progressDialog=null;
        }
    }

    @Override
    public void onConnected(int i) {

    }

    @Override
    public void onConnectFailed(int i) {

    }

    @Override
    public void onReceiveDot(long l, int i, int i1, int i2, int i3) {

    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onMemoryFillLevel(int i) {

    }

    @Override
    public void onRemainBattery(int i) {

    }

    @Override
    public void onOfflineDataReceived(String s, boolean b) {

    }
}
