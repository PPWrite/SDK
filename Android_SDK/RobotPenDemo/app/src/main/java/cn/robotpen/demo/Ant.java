package cn.robotpen.demo;

import java.util.HashMap;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;
import cn.robotpen.core.services.PenService;
import cn.robotpen.demo.R;
import cn.robotpen.demo.RobotPenApplication;
import cn.robotpen.demo.usb.GetAxesActivity;
import cn.robotpen.model.DeviceObject;
import cn.robotpen.model.interfaces.Listeners.OnConnectStateListener;
import cn.robotpen.model.interfaces.Listeners.OnScanDeviceListener;
import cn.robotpen.model.symbol.ConnectState;
import cn.robotpen.model.symbol.SceneType;

public abstract class Ant extends Activity{
	public static final String TAG = Ant.class.getSimpleName();
	//笔服务
	private PenService mPenService;
	private boolean isPenSvrBinding;
	private ProgressDialog mProgressDialog;

	/**
	 * 是否自动绑定服务 <br />
	 * 设置为true后onStart会自动启动,onStop会自动解绑服务; <br />
	 * 如果设置为false,那么需要手动执行initPenService,退出后如果不在需要笔服务还需要执行
	 */
	protected boolean isAutoBindSvr = true;
	protected Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			onHandleMessage(msg);
			super.handleMessage(msg);
		}
	};

	/**
	 * 获取Handler
	 * @return 输出Handler
	 */
	public Handler getHandler(){
		return mHandler;
	}

	/**
	 * 当笔服务绑定成功后调用
	 */
	public abstract void penSvrInitComplete();


	@Override
	public void onStart() {
		super.onStart();

		if(isAutoBindSvr)
			initPenService();
	}

	@Override
	public void onStop() {
		super.onStop();

		//离开后断开
		//if (isAutoBindSvr)
		//	cleanPenService();
	}

	/**
	 * 显示绑定服务提示
	 * @param tag 绑定服务的TAG标签
	 */
	public void showBindSvrDialog(String tag) {
		if (tag.equals(cn.robotpen.model.symbol.Keys.APP_PEN_SERVICE_NAME)) {
			showProgressDialog("正在绑定蓝牙设备,请稍候……", false);
		} else {
			showProgressDialog("正在绑定USB设备,请稍候……", false);
		}
	}

	/**
	 * 获取笔服务
	 * @return
	 */
	public PenService getPenService(){
		mPenService = RobotPenApplication.getInstance().getPenService();
		return mPenService;
	}

	/**
	 * 清除笔服务资源
	 */
	public void cleanPenService(){
		if (mPenService != null) {
			mPenService.setOnConnectStateListener(null);
			mPenService.setOnUploadFirmwareCallback(null);
			mPenService.setOnReceiveDataListener(null);
			mPenService.setOnPointChangeListener(null);
			mPenService.setOnTrailsClientConnectListener(null);
		}
		RobotPenApplication.getInstance().unBindPenService();
	}

	/**
	 * 初始化笔服务
	 */
	public void initPenService() {
		//如果正在执行，那么退出
		if(isPenSvrBinding)return;

		getPenService();
		if (mPenService == null)
			RobotPenApplication.getInstance().bindPenService();

		isPenServiceReady();
	}

	private void isPenServiceReady() {
		isPenSvrBinding = true;
		mPenService = RobotPenApplication.getInstance().getPenService();
		if (mPenService != null) {
			dismissProgressDialog();
			isPenSvrBinding = false;
			penSvrInitComplete();
		} else {
			String tag = RobotPenApplication.getInstance().getConnectDeviceType();
			showBindSvrDialog(tag);
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					isPenServiceReady();
				}
			}, 500);
		}
	}

	/**
	 * 处理HandleMessage
	 * @param msg
	 */
	public void onHandleMessage(Message msg){

	}

	/**释放progressDialog**/
	public void dismissProgressDialog(){
		if(mProgressDialog != null){
			if(mProgressDialog.isShowing())
				mProgressDialog.dismiss();

			mProgressDialog = null;
		}
	}
	/**
	 * 显示ProgressDialog
	 * @param msg
	 */
	public void showProgressDialog(String msg){
		showProgressDialog(msg,true);
	}

	/**
	 * 显示ProgressDialog
	 * @param msg
	 * @param isNew 如果mProgressDialog存在，是否关闭新建
	 * @return 返回是否有新创建
	 */
	public boolean showProgressDialog(String msg,boolean isNew) {
		boolean isCreate = true;
		if (isNew) {
			if (mProgressDialog != null) dismissProgressDialog();
			mProgressDialog = ProgressDialog.show(this, "", msg, true);
		} else {
			if (mProgressDialog == null) {
				mProgressDialog = ProgressDialog.show(this, "", msg, true);
			}else{
				isCreate = false;
			}
		}
		return isCreate;
	}
}
