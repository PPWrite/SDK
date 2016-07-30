package cn.robotpen.demo.usb;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import cn.robotpen.core.services.PenService;
import cn.robotpen.demo.R;
import cn.robotpen.demo.RobotPenApplication;

public class ConnectActivity extends Activity{


    private PenService mPenService;
    private ProgressDialog mProgressDialog;
    private boolean isReady;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPenService = RobotPenApplication.getInstance().getPenService();
        Log.v("DD","getSvrTag:"+mPenService.getSvrTag());
    }

    @Override
    public void onResume() {
        super.onResume();
        isReady = true;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        //停止搜索
        PenService service = RobotPenApplication.getInstance().getPenService();
        if(service != null){
            service.stopScanDevice();
        }

        super.onDestroy();
    }



    private void changePenService(String tag){
        mPenService = RobotPenApplication.getInstance().getPenService();
        if(mPenService != null) {
            if(!mPenService.getSvrTag().equals(tag)){
                //解除绑定
            	RobotPenApplication.getInstance().unBindPenService();
                isUnBindPenService(tag);
            }
        }
    }

    private void isUnBindPenService(final String bindTag){
        mPenService = RobotPenApplication.getInstance().getPenService();
        if(mPenService == null || !mPenService.getIsBind()){
            //启动tag服务
        	RobotPenApplication.getInstance().bindPenService(bindTag);
            isPenServiceReady(bindTag);
        }else{
            showBindDialog(bindTag);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isUnBindPenService(bindTag);
                }
            }, 500);
        }
    }

    private void isPenServiceReady(final String bindTag){
        mPenService = RobotPenApplication.getInstance().getPenService();
        if(mPenService != null){
            dismissProgressDialog();
            RobotPenApplication.getInstance().setConnectDeviceType(mPenService.getSvrTag());
        }else{
            showBindDialog(bindTag);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isPenServiceReady(null);
                }
            }, 500);
        }
    }

  

    private void showBindDialog(String tag){
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(this, "", getString(R.string.service_usb_start), true);
        }
    }

    /**
     * 显示ProgressDialog
     * @param msg
     */
    public void showProgressDialog(String msg){
        if(mProgressDialog != null)dismissProgressDialog();
        mProgressDialog = ProgressDialog.show(this, "",msg, true);
    }

    /**释放progressDialog**/
    public void dismissProgressDialog(){
        if(mProgressDialog != null){
            if(mProgressDialog.isShowing())
                mProgressDialog.dismiss();

            mProgressDialog = null;
        }
    }


}
