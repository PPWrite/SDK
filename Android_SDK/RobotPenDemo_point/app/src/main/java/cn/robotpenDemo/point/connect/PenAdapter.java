package cn.robotpenDemo.point.connect;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import cn.robotpen.model.entity.DeviceEntity;
import cn.robotpenDemo.point.R;

public class PenAdapter extends BaseAdapter {
    private ArrayList<DeviceEntity> mPenDevices;

    HashMap<String, DeviceEntity> dataCache = new HashMap<>();

    public PenAdapter(Context context) {
        mPenDevices = new ArrayList<DeviceEntity>();
    }

    @Override
    public int getCount() {
        return mPenDevices.size();
    }

    @Override
    public DeviceEntity getItem(int i) {
        return mPenDevices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public void addItem(DeviceEntity item) {
        String macAddr = item.getAddress();
        if (dataCache.get(macAddr) != null) {
            mPenDevices.add(item);
            notifyDataSetChanged();
        }
    }

    /**
     * 清除集合内容
     */
    public void clearItems() {
        mPenDevices.clear();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        PageItem pageItem;
        if (convertView != null) {
            pageItem = (PageItem) convertView.getTag();
        } else {
            convertView = LayoutInflater.from(convertView.getContext()).inflate(R.layout.pen_adapter_item, null);
            pageItem = new PageItem(convertView);
            convertView.setTag(pageItem);
        }

        DeviceEntity deviceObject = getItem(position);

        pageItem.deviceName.setText(deviceObject.getName());
        pageItem.deviceAddress.setText(deviceObject.getAddress());


        return convertView;
    }

    private class PageItem {
        public TextView deviceName;
        public TextView deviceAddress;

        public PageItem(View view) {
            deviceName = (TextView) view.findViewById(R.id.deviceName);
            deviceAddress = (TextView) view.findViewById(R.id.deviceAddress);
        }
    }
}
