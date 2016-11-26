package cn.robotpen.demo;

import android.app.ExpandableListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.robotpen.demo.usb.NoteActivity;
import cn.robotpen.demo.utils.ResUtils;

/**
 * @author Xiaoz
 * @date 2015年9月30日 下午4:38:45
 * <p>
 * Description
 */
public class StartActivity extends ExpandableListActivity {
    public static final String TAG = StartActivity.class.getSimpleName();
    private BaseExpandableListAdapter sAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setListAdapter(new IdeasExpandableListAdapter(StartActivity.this));
        if (ResUtils.isDirectory(ResUtils.DIR_NAME_BUFFER)
                && ResUtils.isDirectory(ResUtils.DIR_NAME_PHOTO)
                && ResUtils.isDirectory(ResUtils.DIR_NAME_VIDEO)
                && ResUtils.isDirectory(ResUtils.DIR_NAME_DATA)) {
            //创建文件夹
        }
    }
}

class IdeasExpandableListAdapter extends BaseExpandableListAdapter {

    private Context mContext = null;
    private String[] group = {"蓝牙演示", "USB演示", "综合演示示例"};
    private String[] blue = {"> 获取笔记坐标演示", "> 单画布+离线笔迹"};
    private String[] usb = {"> 获取笔记坐标演示", "> 单画布演示", "> 画布常用功能演示", "> 画布综合演示"};
    private String[] multiple = {"> 蓝牙/USB切换"};
    //,"> P2P交互功能演示","> 多画布功能演示","> 综合示例演示"};
    private List<String> groupList = null;
    private List<List<String>> itemList = null;

    public IdeasExpandableListAdapter(Context context) {
        this.mContext = context;
        groupList = new ArrayList<String>();
        itemList = new ArrayList<List<String>>();
        initData();//数据初始化
    }

    private void initData() {
        for (int i = 0; i < group.length; i++) {
            groupList.add(group[i]);
        }
        List<String> item1 = new ArrayList<String>();
        for (int i = 0; i < blue.length; i++) {
            item1.add(blue[i]);
        }
        List<String> item2 = new ArrayList<String>();
        for (int i = 0; i < usb.length; i++) {
            item2.add(usb[i]);
        }
        List<String> item3 = new ArrayList<String>();
        for (int i = 0; i < multiple.length; i++) {
            item3.add(multiple[i]);
        }
        itemList.add(item1);
        itemList.add(item2);
        itemList.add(item3);
    }

    public boolean areAllItemsEnabled() {
        return false;
    }

    public Object getChild(int groupPosition, int childPosition) {
        return itemList.get(groupPosition).get(childPosition);
    }

    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
                             ViewGroup parent) {
        TextView text = null;
        if (convertView == null) {
            text = new TextView(mContext);
        } else {
            text = (TextView) convertView;
        }
        // 获取子节点要显示的名称
        String name = (String) itemList.get(groupPosition).get(childPosition);
        final int g = groupPosition;
        final int c = childPosition;
        // 设置文本视图的相关属性
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 100);
        text.setLayoutParams(lp);
        text.setTextSize(20);
        text.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        text.setPadding(100, 0, 0, 0);
        text.setText(name);
        text.setOnClickListener(new View.OnClickListener() {
            Intent intent;

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                switch (g) {
                    case 0:// 蓝牙
                        switch (c) {
//                            case 0:// 笔迹
//                                intent = new Intent(mContext, GetdataActivity.class);
//                                mContext.startActivity(intent);
//                                break;
//                            case 1:// 画布
//                                intent = new Intent(mContext, NoteWithTrailActivity.class);
//                                mContext.startActivity(intent);
//                                break;
                            default:
                                break;
                        }
                        break;
                    case 1:// USB
                        switch (c) {
//                            case 0:// 获取数据信息
//                                intent = new Intent(mContext, GetAxesActivity.class);
//                                mContext.startActivity(intent);
//                                break;
                            case 1:// 单画布
                                intent = new Intent(mContext, NoteActivity.class);
                                mContext.startActivity(intent);
                                break;
//                            case 2:// 画布功能展示
//                                intent = new Intent(mContext, NoteWithActivity.class);
//                                mContext.startActivity(intent);
//                                break;
//                            case 3:// 画布综合展示
//                                intent = new Intent(mContext, GeneralNoteActivity.class);
//                                mContext.startActivity(intent);
//                                break;
                            default:
                                break;
                        }

                        break;
                    case 2:// 综合
                        switch (c) {
//                            case 0:// 蓝牙/usb切换
//                                intent = new Intent(mContext, ChangeTypeActivity.class);
//                                mContext.startActivity(intent);
//                                break;
                            default:
                                break;
                        }

                        break;
                    default:

                        break;
                }
            }
        });
        return text;
    }

    public int getChildrenCount(int groupPosition) {
        return itemList.get(groupPosition).size();
    }

    public Object getGroup(int groupPosition) {
        return groupList.get(groupPosition);
    }

    public int getGroupCount() {
        return groupList.size();
    }

    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        TextView text = null;
        if (convertView == null) {
            text = new TextView(mContext);
        } else {
            text = (TextView) convertView;
        }
        String name = (String) groupList.get(groupPosition);
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 120);
        text.setLayoutParams(lp);
        text.setBackgroundColor(Color.GRAY);
        text.setTextSize(24);
        text.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        text.setPadding(120, 0, 0, 0);
        text.setText(name);
        return text;
    }

    public boolean isEmpty() {
        return false;
    }

    public void onGroupCollapsed(int groupPosition) {
    }

    public void onGroupExpanded(int groupPosition) {
    }

    public boolean hasStableIds() {
        return false;
    }

    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

}
