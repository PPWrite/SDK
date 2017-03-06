package cn.robotpenDemo.board.common;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import cn.robotpen.pen.IRemoteRobotService;
import cn.robotpen.pen.IRemoteRobotServiceCallback;
import cn.robotpen.pen.RobotPenService;
import cn.robotpenDemo.board.MyApplication;

/**
 * Created by 王强 on 2016/12/17.
 * 简介：连接到RobotPen 服务
 */

public abstract class BaseConnectPenServiceActivity<T extends IRemoteRobotServiceCallback.Stub>
        extends Activity implements ServiceConnection {
    protected IRemoteRobotService robotService;
    protected T penServiceCallback;
    private RobotPenService robotPenService;

    final IBinder.DeathRecipient deathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            if (ActivityCompat.checkSelfPermission(BaseConnectPenServiceActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(BaseConnectPenServiceActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(BaseConnectPenServiceActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                return;
            }
            robotPenService.bindRobotPenService(
                    BaseConnectPenServiceActivity.this, BaseConnectPenServiceActivity.this);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        penServiceCallback = initPenServiceCallback(); // 链接状态改变 位置改变绘图
        robotPenService = MyApplication.getInstance().getRobotPenService();// 接口 robotPenService 服务内部类
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            return;
        }
        robotPenService.bindRobotPenService(this, this);
    }

    protected abstract T initPenServiceCallback();

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        robotService = IRemoteRobotService.Stub.asInterface(service);
        try {
            robotService.registCallback(penServiceCallback);
            service.linkToDeath(deathRecipient, 0);
        } catch (RemoteException e) {
            e.printStackTrace();
            onServiceConnectError(e.getMessage());
        }
    }

    /**
     * 无法连接到笔服务
     *
     * @param msg
     */
    public void onServiceConnectError(String msg) {

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (robotService != null) {
            try {
                byte model = robotService.getCurrentMode();
                //检查当前模式
                if (model == 0x0A) {
                    //退出同步模式
                    robotService.exitSyncMode();
                } else if (model == 0x06) {
                    //退出OTA模式
                    robotService.exitOTA();
                }
                //取消回调
                robotService.unRegistCallback(penServiceCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        try {
            robotService.asBinder().unlinkToDeath(deathRecipient, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        robotPenService.unBindRobotPenService(this, this);
    }
}
