package cn.robotpen.demo.bluetooth;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import cn.robotpen.db.TrailDB;
import cn.robotpen.demo.R;
import cn.robotpen.demo.bluetooth.adapter.TrailAdapter;
import cn.robotpen.model.TrailsObject;
import cn.robotpen.model.symbol.Keys;
import cn.robotpen.utils.FileUtils;

public class TrailManageActivity extends Activity {
    private TrailAdapter mPenAdapter;
    private PageItem mPageItem;
    private ArrayList<TrailsObject> mGetNoteList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trail_manage);
        mPageItem = new PageItem();
        initList();
    }


    public  void initList(){
        mGetNoteList = TrailDB.getNoteList(TrailManageActivity.this,Long.MAX_VALUE);
        if(mGetNoteList!=null&&mGetNoteList.size()>0){
            mPenAdapter.clearItems();
            mPenAdapter.notifyDataSetChanged();
            mPageItem.emptyText.setVisibility(View.GONE);
            for (TrailsObject noteInfo:mGetNoteList){
                if(noteInfo.Title==null){
                    noteInfo.Title = "android_tmp";
                }
                mPenAdapter.addItem(noteInfo);
                mPenAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * 显示新建笔记窗口
     */
    private void showAddNoteAlert() {
        final String name = FileUtils.getDateFormatName("yyyyMMdd_HHmmss");
        AlertDialog.Builder alert = new AlertDialog.Builder(TrailManageActivity.this);
        alert.setTitle("添加笔记:"+name);
        alert.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                TrailsObject trailsObject = new TrailsObject();
                trailsObject.Title = name;
                trailsObject.NoteKey = TrailsObject.newNoteKey();
                TrailDB.createNote(TrailManageActivity.this, trailsObject);
                //刷新列表
                initList();
            }
        });
        alert.setNegativeButton("取消", null);
        alert.show();
    }

    class  PageItem{
        Button addTrail;
        ListView trailList;
        TextView emptyText;
        public PageItem(){
            emptyText = (TextView) findViewById(R.id.emptyText);
            addTrail = (Button) findViewById(R.id.addTrail);
            addTrail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAddNoteAlert();
                }
            });
            trailList = (ListView) findViewById(R.id.trailList);
            mPenAdapter = new TrailAdapter(TrailManageActivity.this);
            trailList.setAdapter(mPenAdapter);
            trailList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
                    TrailsObject note = mPenAdapter.getItem(arg2);
                    //停止搜索
                    Intent intent = new Intent(TrailManageActivity.this, NoteWithTrailActivity.class);
                    intent.putExtra(Keys.KEY_TARGET, note.NoteKey);
                    startActivity(intent);

                }
            });

        }

    }
}
