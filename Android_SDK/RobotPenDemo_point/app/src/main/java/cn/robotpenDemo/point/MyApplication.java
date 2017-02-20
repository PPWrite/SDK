package cn.robotpenDemo.point;

import android.Manifest;
import android.app.Application;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

import cn.robotpen.pen.RobotPenService;
import cn.robotpen.pen.RobotPenServiceImpl;

/**
 * Created by dadou on 2017/1/20.
 */

public class MyApplication extends Application {

    public RobotPenService robotPenService;
    private static MyApplication instance = null;

    public static MyApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        robotPenService = new RobotPenServiceImpl(this.getBaseContext());
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //权限处理
            return;
        }
        robotPenService.startRobotPenService(this.getBaseContext(), true);//true为在通知栏显示通知 false将不在通知栏显示
    }

    /**
     * 获取笔服务
     *
     * @return
     */
    public RobotPenService getRobotPenService() {
        return this.robotPenService;
    }

    @Override
    public void onTerminate() {
        // 程序终止的时候执行
        super.onTerminate();
    }

}
