package cn.robotpenDemo.board.show;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.robotpen.core.widget.WhiteBoardView;
import cn.robotpen.model.DevicePoint;
import cn.robotpen.model.entity.note.NoteEntity;
import cn.robotpen.model.symbol.DeviceType;
import cn.robotpen.pen.callback.PenPositionAndEventCallback;
import cn.robotpen.pen.callback.RobotPenActivity;
import cn.robotpen.pen.model.RemoteState;
import cn.robotpen.pen.model.RobotDevice;
import cn.robotpenDemo.board.MyApplication;
import cn.robotpenDemo.board.R;
import cn.robotpenDemo.board.common.BaseConnectPenServiceActivity;
import cn.robotpenDemo.board.common.ResUtils;

public class WhiteBoardWithMethodActivity extends RobotPenActivity
        implements WhiteBoardView.CanvasManageInterface {

    DeviceType mDeDeviceType = DeviceType.P1;//默认连接设备为P1 当与连接设备有冲突时则需要进行切换
    float isRubber = 0;//是否是橡皮
    ProgressDialog mProgressDialog;
    Handler mHandler;
    float mPenWeight = 2;//默认笔宽度是2像素
    int mPenColor = Color.BLUE;//默认为蓝色
    String mNoteKey = NoteEntity.KEY_NOTEKEY_TMP;//默认为临时笔记
    static final int SELECT_PICTURE = 1001;
    static final int SELECT_BG = 1002;
    Uri mInsertPhotoUri = null;
    Uri mBgUri = null;

    @BindView(R.id.whiteBoardView_m)
    WhiteBoardView whiteBoardView;
    @BindView(R.id.viewWindow)
    RelativeLayout viewWindow;
    @BindView(R.id.changePenBut)
    Button changePenBut;
    @BindView(R.id.changePenColorBut)
    Button changePenColorBut;
    @BindView(R.id.cleanLineBut)
    Button cleanLineBut;
    @BindView(R.id.cleanPhotoBut)
    Button cleanPhotoBut;
    @BindView(R.id.cleanScreenBut)
    Button cleanScreenBut;
    @BindView(R.id.innerPhotoBut)
    Button innerPhotoBut;
    @BindView(R.id.photoScaleTypeBut)
    Button photoScaleTypeBut;
    @BindView(R.id.removePhotoBut)
    Button removePhotoBut;
    @BindView(R.id.innerbgBut)
    Button innerbgBut;
    @BindView(R.id.bgScaleTypeBut)
    Button bgScaleTypeBut;
    @BindView(R.id.removeBgBut)
    Button removeBgBut;
    @BindView(R.id.delPageBut)
    Button delPageBut;
    @BindView(R.id.gotoProBut)
    Button gotoProBut;
    @BindView(R.id.gotoNextBut)
    Button gotoNextBut;
    @BindView(R.id.saveScreenBut)
    Button saveScreenBut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_white_board_with_method);
        ButterKnife.bind(this);
        mHandler = new Handler();
        whiteBoardView.setIsTouchWrite(true);//允许在屏幕上直接绘制
        whiteBoardView.setDaoSession(MyApplication.getInstance().getDaoSession());
    }

    @Override
    protected void onResume() {
        super.onResume();
        whiteBoardView.initDrawArea();
        checkIntentInsertPhoto();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (whiteBoardView != null) {
            whiteBoardView.dispose();
            whiteBoardView = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            mInsertPhotoUri = null;
            mBgUri = null;
            if (requestCode == SELECT_PICTURE && data != null) {
                mInsertPhotoUri = data.getData();
            }
            if (requestCode == SELECT_BG && data != null) {
                mBgUri = data.getData();
            }
        }
    }
    /**
     * 当服务服务连接成功后进行
     *
     * @param name
     * @param service
     */
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        super.onServiceConnected(name, service);
        checkDeviceConn();
    }

    public void checkDeviceConn() {
        if (robotService != null) {
            try {
                RobotDevice device = robotService.getConnectedDevice();
                if (device != null) {
                    DeviceType type = DeviceType.toDeviceType(device.getDeviceVersion());
                    //判断当前设备与笔记设备是否一致
                    if (whiteBoardView.getFrameSizeObject().getDeviceType() != type) {
                        mDeDeviceType = type;
                        mNoteKey = NoteEntity.KEY_NOTEKEY_TMP + "_" + mDeDeviceType.name();
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        //都需要刷新白板
        whiteBoardView.initDrawArea();
    }

    /**
     * 检查是否有Intent传入需要插入的图片
     */
    public void checkIntentInsertPhoto() {
        //检查是否有需要插入的图片uri
        if (null != mInsertPhotoUri) {
            whiteBoardView.insertPhoto(mInsertPhotoUri);
            whiteBoardView.startPhotoEdit(true); //插入图片后，设置图片可以编辑状态
            mInsertPhotoUri = null;
        }
        if(null != mBgUri){
            whiteBoardView.setBgPhoto(mBgUri);
            mBgUri = null;
        }
    }

    @OnClick({R.id.changePenBut, R.id.changePenColorBut
            , R.id.cleanLineBut, R.id.cleanPhotoBut, R.id.cleanScreenBut
            , R.id.innerPhotoBut, R.id.photoScaleTypeBut, R.id.removePhotoBut
            , R.id.saveScreenBut
            , R.id.innerbgBut, R.id.bgScaleTypeBut, R.id.removeBgBut
            , R.id.delPageBut, R.id.gotoProBut, R.id.gotoNextBut})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.changePenBut: //更改笔粗细
                final String[] penWeightItems = {"2个像素", "3个像素", "10个像素", "50个像素"};
                new AlertDialog.Builder(WhiteBoardWithMethodActivity.this).setTitle("修改笔粗细")
                        .setItems(penWeightItems, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                switch (which) {
                                    case 0:
                                        mPenWeight = 2;
                                        break;
                                    case 1:
                                        mPenWeight = 3;
                                        break;
                                    case 2:
                                        mPenWeight = 10;
                                        break;
                                    case 3:
                                        mPenWeight = 50;
                                        break;
                                }
                            }
                        }).show();

                break;
            case R.id.changePenColorBut:
                final String[] penColorItems = {"红色", "绿色", "蓝色", "黑色"};
                new AlertDialog.Builder(WhiteBoardWithMethodActivity.this).setTitle("修改笔颜色")
                        .setItems(penColorItems, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        mPenColor = Color.RED;
                                        break;
                                    case 1:
                                        mPenColor = Color.GREEN;
                                        break;
                                    case 2:
                                        mPenColor = Color.BLUE;
                                        break;
                                    case 3:
                                        mPenColor = Color.BLACK;
                                        break;
                                }
                            }
                        }).show();
                break;
            case R.id.cleanLineBut:
                whiteBoardView.cleanTrail();
                break;
            case R.id.cleanPhotoBut:
                whiteBoardView.cleanPhoto();
                break;
            case R.id.cleanScreenBut:
                whiteBoardView.cleanScreen();
                break;
            case R.id.innerPhotoBut:
                whiteBoardView.setDataSaveDir(ResUtils.getSavePath(ResUtils.DIR_NAME_DATA));
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(Intent.createChooser(intent, "选择图片"), SELECT_PICTURE);
                //支持多个图片的插入 所以插入图片成功后需要改变序号
                break;
            case R.id.photoScaleTypeBut:
                whiteBoardView.setPhotoScaleType(ImageView.ScaleType.CENTER_CROP);
                break;
            case R.id.removePhotoBut:
                whiteBoardView.delCurrEditPhoto();
                break;
            case R.id.innerbgBut:
                whiteBoardView.setDataSaveDir(ResUtils.getSavePath(ResUtils.DIR_NAME_DATA));
                Intent intent2 = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(Intent.createChooser(intent2, "选择背景"), SELECT_BG);
                break;
            case R.id.bgScaleTypeBut:
                whiteBoardView.setBgScaleType(ImageView.ScaleType.CENTER_CROP);
                break;
            case R.id.removeBgBut:
                mBgUri = null;
                whiteBoardView.setBgPhoto(null);
                break;
            case R.id.saveScreenBut:
                whiteBoardView.setSaveSnapshotDir(ResUtils.getSavePath(ResUtils.DIR_NAME_PHOTO));//设置存储路径
                whiteBoardView.saveSnapshot();
                break;
            case R.id.delPageBut:
                whiteBoardView.delCurrBlock();
                break;
            case R.id.gotoProBut:
                whiteBoardView.frontBlock();
                break;
            case R.id.gotoNextBut:
                whiteBoardView.nextBlock();
                break;
        }
    }

    @Override
    public DeviceType getDeviceType() {
        return mDeDeviceType;
    }

    @Override
    public float getPenWeight() {
        return mPenWeight;
    }

    @Override
    public int getPenColor() {
        return mPenColor;
    }

    @Override
    public float getIsRubber() {
        return isRubber;
    } //非0时即视为橡皮擦

    @Override
    public boolean getIsPressure() {
        return false;
    }

    @Override
    public boolean getIsHorizontal() {
        return false;
    }

    @Override
    public long getCurrUserId() {
        return 0;
    }

    @Override
    public String getNoteKey() {
        return mNoteKey;
    }

    @Override
    public String getNewNoteName() {
        return null;
    }

    @Override
    public boolean onEvent(WhiteBoardView.BoardEvent boardEvent, Object o) {
        switch (boardEvent) {
            case BOARD_AREA_COMPLETE: //白板区域加载完成
                whiteBoardView.beginBlock();
                break;
            case ERROR_DEVICE_TYPE: //检测到连接设备更换
                checkDeviceConn();
                break;
            case ERROR_SCENE_TYPE: //横竖屏更换
                break;
        }
        return true;
    }

    @Override
    public boolean onMessage(String s, Object o) {
        return false;
    }

    @Override
    public void onStateChanged(int i, String s) {
        switch (i) {
            case RemoteState.STATE_CONNECTED:
                break;
            case RemoteState.STATE_DEVICE_INFO: //当出现设备切换时获取到新设备信息后执行的
                //whiteBoardView.initDrawArea();
                checkDeviceConn();
                break;
            case RemoteState.STATE_DISCONNECTED://设备断开
                break;
        }
    }

    @Override
    public void onPenServiceError(String s) {

    }

    @Override
    public void onPenPositionChanged(int deviceType, int x, int y, int presure, byte state) {
        super.onPenPositionChanged(deviceType, x, y, presure, state);
        DevicePoint p = DevicePoint.obtain(deviceType, x, y, presure, state);
        whiteBoardView.drawLine(p);//白板的绘制必须手动执行
    }
}
