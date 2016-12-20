package cn.robotpen.demo;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import cn.robotpen.model.db.DBConfig;
import cn.robotpen.model.db.DaoMaster;
import cn.robotpen.model.db.DaoSession;

/**
 * 
 * @author Xiaoz
 * @date 2015年6月12日 上午11:39:48
 *
 * Description
 */
public class RobotPenApplication extends Application{
    private static RobotPenApplication instance = null;
    private static DaoSession daoSession = null;
	 @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static RobotPenApplication getInstance() {
        return instance;
    }

    public DaoSession getDaoSession() {
        if(null==daoSession){
            SQLiteDatabase db = new DaoMaster.DevOpenHelper(instance, DBConfig.DB_NAME).getWritableDatabase();
            this.daoSession = new DaoMaster(db).newSession();
        }
        return daoSession;
    }

//    //引入so文件
//    static{
//    	 System.loadLibrary("avutil-54");
//         System.loadLibrary("swresample-1");
//         System.loadLibrary("swscale-3");
//         System.loadLibrary("postproc-53");
//         System.loadLibrary("avcodec-56");
//         System.loadLibrary("avformat-56");
//         System.loadLibrary("avfilter-5");
//         System.loadLibrary("avdevice-56");
//         System.loadLibrary("RecordImageUtil");
//    }
}
