package cn.robotpen.demo;

import cn.robotpen.core.PenApplication;
import cn.robotpen.file.qiniu.QiniuConfig;
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
        QiniuConfig.ACCESS_KEY = "i2hg6Yvl2mo86rEdgdgifL8eKAjHTVeWbXp9ZAa4";
        QiniuConfig.SECRET_KEY = "ZQ6H3fFzcRPNeLwQIkVLyZCFYxhuRW4Ct5E1Zjld";
        AliyunConfig.ACCESS_KEY = "J38gvBvILH6wsgTZ";
        AliyunConfig.SECRET_KEY = "Qkz9X8ZwtRkWAihbgTlDJ8jNb5vnKr";
    }

    public static RobotPenApplication getInstance() {
        return instance;
    }
    //引入so文件
    static{
    	System.loadLibrary("avutil-54");
        System.loadLibrary("swresample-1");
        System.loadLibrary("swscale-3");
        System.loadLibrary("postproc-53");
        System.loadLibrary("avcodec-56");
        System.loadLibrary("avformat-56");
        System.loadLibrary("avfilter-5");
        System.loadLibrary("avdevice-56");
        System.loadLibrary("RecordImageUtil");
    }
    
    
}
