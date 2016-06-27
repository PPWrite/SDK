package cn.robotpen.demo;

import cn.robotpen.core.PenApplication;
import cn.robotpen.remote.Aliyun.AliyunConfig;

/**
 * 
 * @author Xiaoz
 * @date 2015年6月12日 上午11:39:48
 *
 * Description
 */
public class RobotPenApplication extends PenApplication{
    private static RobotPenApplication instance = null;
    
	 @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        
        AliyunConfig.ACCESS_KEY = "ACCESS_KEY";
        AliyunConfig.SECRET_KEY = "SECRET_KEY";
        AliyunConfig.TRAIL_CONSUMER_ID = "TRAIL_CONSUMER_ID";
        
    }

    public static RobotPenApplication getInstance() {
        return instance;
    }
}
