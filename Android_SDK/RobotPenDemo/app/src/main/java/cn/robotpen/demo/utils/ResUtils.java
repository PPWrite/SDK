package cn.robotpen.demo.utils;


import java.io.File;
import java.text.SimpleDateFormat;

import cn.robotpen.demo.R;

/**
 * 资源管理工具
 * Created by Xiaoz on 15/10/20.
 */
public class ResUtils {
    public static final String DIR_NAME_BUFFER = "buffer";
    public static final String DIR_NAME_VIDEO = "video";
    public static final String DIR_NAME_PHOTO = "photo";
    public static final String DIR_NAME_DATA = "data";

    public static boolean isDirectory(String dir){
        String path = getSavePath(dir);
        File file = new File(path);
        file.mkdirs();
        return file.isDirectory();
    }

    public static String getSavePath(String dir){
        String storagePath = cn.robotpen.utils.FileUtils.getExternalSdCardPath();
        String path = storagePath + "/"+ getPackagePath() +"/";

        if(dir != null && !dir.isEmpty())
            path += dir + "/";
        return path;
    }

    public static String getDateFormatName(){
        return getDateFormatName("yyyyMMddHHmmss");
    }
    public static String getDateFormatName(String pattern){
        SimpleDateFormat date = new SimpleDateFormat(pattern);
        String filename = String.valueOf(date.format(System.currentTimeMillis()));
        return filename;
    }

    public static String getPackagePath(){
        String _class = R.class.getCanonicalName();
        String result = _class.replace(".R", "");
        return result;
    }

    /**
     * 删除缓存文件
     * @param fileName
     * @return
     */
    public static boolean delBufferFile(String fileName){
        String filePath = getSavePath(DIR_NAME_BUFFER) + fileName;
        File file = new File(filePath);
        return file.delete();
    }
}
