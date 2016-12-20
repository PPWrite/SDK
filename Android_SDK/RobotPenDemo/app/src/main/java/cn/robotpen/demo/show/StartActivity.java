package cn.robotpen.demo.show;

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
    private String[] group = {"点坐标演示","单画布演示", "多画布+离线笔记演示"};
            //, "综合演示示例"};
            private String[] point = {"> 点坐标"};
    private String[] single = {"> 单画布", "> 画布的常用功能"};
    private String[] multi = {"> 多画布", "> 离线笔记功能", "> 多画布常用功能"};
   // private String[] together = {"> 画布综合演示"};
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
        List<String> item0 = new ArrayList<String>();
        for (int i = 0; i < point.length; i++) {
            item0.add(point[i]);
        }
        List<String> item1 = new ArrayList<String>();
        for (int i = 0; i < single.length; i++) {
            item1.add(single[i]);
        }
        List<String> item2 = new ArrayList<String>();
        for (int i = 0; i < multi.length; i++) {
            item2.add(multi[i]);
        }
//        List<String> item3 = new ArrayList<String>();
//        for (int i = 0; i < together.length; i++) {
//            item3.add(together[i]);
//        }
        itemList.add(item0);
        itemList.add(item1);
        itemList.add(item2);
       // itemList.add(item3);
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
        text.setTextSize(22);
        text.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        text.setPadding(110, 0, 0, 0);
        text.setText(name);
        text.setOnClickListener(new View.OnClickListener() {
            Intent intent;

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                switch (g) {
                    case 0:// 点坐标
                        switch (c) {
                            case 0:// 点坐标
                                intent = new Intent(mContext, ShowPointActivity.class);
                                mContext.startActivity(intent);
                                break;
                        }
                        break;
                    case 1:// 单画布
                        switch (c) {
                            case 0:// 单画布
                                intent = new Intent(mContext, SingleCanvasActivity.class);
                                mContext.startActivity(intent);
                                break;
                            case 1:// 画布的常用功能
                                intent = new Intent(mContext, SingleWithMethodActivity.class);
                                mContext.startActivity(intent);
                                break;
                            default:
                                break;
                        }
                        break;
                    case 2:// 多画布
                        switch (c) {
                            case 0:// 多画布
                                intent = new Intent(mContext, MulityCanvasActivity.class);
                                mContext.startActivity(intent);
                                break;
                            case 1:// 离线笔记功能
                                intent = new Intent(mContext, NoteActivity.class);
                                mContext.startActivity(intent);
                                break;
                            case 2:// 多画布常用功能
                                intent = new Intent(mContext, MulityWithMethodActivity.class);
                                mContext.startActivity(intent);
                                break;
                            default:
                                break;
                        }
                        break;
                    case 3:// 综合
                        switch (c) {
                            case 0:// 蓝牙/usb切换
//                                intent = new Intent(mContext, ChangeTypeActivity.class);
//                                mContext.startActivity(intent);
                                break;
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
