package cn.robotpen.demo.show;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.robotpen.core.PenManage;
import cn.robotpen.core.module.NoteManageModule;
import cn.robotpen.core.services.PenService;
import cn.robotpen.core.services.SmartPenService;
import cn.robotpen.demo.R;
import cn.robotpen.demo.RobotPenApplication;
import cn.robotpen.model.entity.DeviceEntity;
import cn.robotpen.model.entity.NoteEntity;
import cn.robotpen.model.symbol.ConnectState;
import cn.robotpen.model.symbol.DeviceType;
import cn.robotpen.model.symbol.Keys;
import cn.robotpen.model.symbol.SceneType;
import cn.robotpen.utils.FileUtils;
import cn.robotpen.utils.StringUtil;

public class NoteActivity extends Activity {

    @BindView(R.id.noteListView)
    ListView noteListView;
    @BindView(R.id.butFrame)
    LinearLayout butFrame;
    @BindView(R.id.addNoteBut)
    Button addNoteBut;
    NoteManageModule mNoteManageModule;
    private NoteAdapter mNoteAdapter;
    private ArrayList<NoteEntity> mGetNoteList;
    PenManage mPenManage;
    final static String KEY_NOTEKEY = "NoteKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        ButterKnife.bind(this);
        mNoteManageModule = new NoteManageModule(this, RobotPenApplication.getInstance().getDaoSession());
        mNoteAdapter = new NoteAdapter(NoteActivity.this);
        mPenManage = new PenManage(this);
        noteListView.setAdapter(mNoteAdapter);
        noteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NoteEntity note = mNoteAdapter.getItem(position);
                Intent intent = new Intent(NoteActivity.this, MulityWithMethodActivity.class);
                intent.putExtra(KEY_NOTEKEY, note.getNoteKey());
                startActivity(intent);
            }
        });
        noteListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final NoteEntity note = mNoteAdapter.getItem(position);
                new AlertDialog.Builder(NoteActivity.this).setTitle("删除笔记")//设置对话框标题
                        .setMessage("请确认是否删除笔记？")//设置显示的内容
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加确定按钮
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(mNoteManageModule.deleteNote(note.getNoteKey())){//判断是否删除成功
                                    dialog.dismiss();
                                    initList();
                                }
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();//在按键响应事件中显示此对话框
                return true;
            }
        });

        addNoteBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddNoteAlert();
            }
        });
        initList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkDeviceConnStatus(); //检查设备连接状态
    }
    /*
     * 检测设备连接
     */
    private void checkDeviceConnStatus(){
        DeviceEntity deviceEntity = mPenManage.getConnectDevice();
        if (null == deviceEntity) {
            //判断蓝牙还是USB服务
            if (SmartPenService.TAG.equals(mPenManage.getSvrTag())) {
                //检查以前是否有连接过设备
                DeviceEntity lastDevice = PenManage.getLastDevice(NoteActivity.this);
                if (lastDevice == null || TextUtils.isEmpty(lastDevice.getAddress())) {
                } else {
                    mPenManage.scanDevice(onScanDeviceListener);
                }
            } else {
                mPenManage.scanDevice(null);
            }
        }else{ //已成功连接设备
            Toast.makeText(NoteActivity.this,"设备连接成功",Toast.LENGTH_LONG).show();
        }
    }

    public void initList() {
        mGetNoteList = (ArrayList<NoteEntity>) mNoteManageModule.getAllNotes();
        if (mGetNoteList != null && mGetNoteList.size() > 0) {
            mNoteAdapter.clearItems();
            mNoteAdapter.notifyDataSetChanged();
            for (NoteEntity noteInfo : mGetNoteList) {
                mNoteAdapter.addItem(noteInfo);
                mNoteAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * 显示新建笔记窗口
     */
    private void showAddNoteAlert() {
        final String time = FileUtils.getDateFormatName("yyyyMMdd_HHmmss");
        AlertDialog.Builder alert = new AlertDialog.Builder(NoteActivity.this);
        alert.setTitle("添加笔记:" + time);
        alert.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(null==mPenManage){
                    mPenManage = new PenManage(NoteActivity.this);
                }
                DeviceType deviceType = DeviceType.P1;
                if(null != mPenManage.getConnectDeviceType()) {
                    deviceType = mPenManage.getConnectDeviceType();
                    NoteEntity noteEntity = new NoteEntity();
                    noteEntity.setTitle(deviceType.name()+"笔记");
                    noteEntity.setNoteKey(deviceType.name()+"_"+time);
                    noteEntity.setDeviceType(deviceType.getValue());
                    mNoteManageModule.createNote(noteEntity);
                    //刷新列表
                    initList();
                }
            }
        });
        alert.setNegativeButton("取消", null);
        alert.show();
    }

    /*
   *扫描监听
    */
    PenService.OnScanDeviceListener onScanDeviceListener = new PenService.OnScanDeviceListener() {
        @Override
        public void find(DeviceEntity deviceObject) {
            DeviceEntity lastDevice = mPenManage.getLastDevice(NoteActivity.this);
            if (!StringUtil.isEmpty(lastDevice.getAddress())) {
                if (deviceObject.getAddress().equals(lastDevice.getAddress())) {
                    mPenManage.stopScanDevice();
                    mPenManage.connectDevice(onConnectStateListener, lastDevice.getAddress());
                }
            }
        }

        @Override
        public void complete(HashMap<String, DeviceEntity> hashMap) {
            if (!mPenManage.getIsStartConnect()) {
                Toast.makeText(NoteActivity.this, "暂未发现设备", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void status(int i) {
            switch (i) {
                case Keys.REQUEST_ENABLE_BT:
                    Toast.makeText(NoteActivity.this, "蓝牙未打开", Toast.LENGTH_SHORT).show();
                    Intent req_ble = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(req_ble, Keys.REQUEST_ENABLE_BT);
                    break;
                case Keys.BT_ENABLE_ERROR:
                    Toast.makeText(NoteActivity.this, "设备不支持BLE协议", Toast.LENGTH_SHORT).show();
                    break;
            }

        }
    };
    /*
   * 此处监听是为了弹出授权
   */
    private PenService.OnConnectStateListener onConnectStateListener = new PenService.OnConnectStateListener() {
        @Override
        public void stateChange(String arg0, ConnectState arg1) {
            if (arg1 == ConnectState.CONNECTED) {
                Toast.makeText(NoteActivity.this, "设备已连接且连接成功！", Toast.LENGTH_SHORT).show();
                mPenManage.setSceneObject(SceneType.getSceneType(false,mPenManage.getConnectDeviceType()));
            } else if (arg1 == ConnectState.DISCONNECTED) {
                Toast.makeText(NoteActivity.this, "设备已断开", Toast.LENGTH_SHORT).show();
            }
        }
    };
}
