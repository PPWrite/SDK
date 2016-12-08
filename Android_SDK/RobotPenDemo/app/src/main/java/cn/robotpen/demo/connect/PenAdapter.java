package cn.robotpen.demo.connect;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import cn.robotpen.demo.R;
import cn.robotpen.model.entity.DeviceEntity;

public class PenAdapter extends BaseAdapter {
	public final static String TAG = PenAdapter.class.getSimpleName();

	private Context mContext;
	private LayoutInflater inflater;
	private ArrayList<DeviceEntity> mPenDevices;

	public PenAdapter(Context context) {
		this.mContext = context;
		inflater = LayoutInflater.from(context);
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
	
	public void addItem(DeviceEntity item){
		mPenDevices.add(item);
	}
	
	/**
	 * 清除集合内容
	 */
	public void clearItems(){
		mPenDevices.clear();
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		PageItem pageItem;
		if(convertView != null){
			pageItem = (PageItem)convertView.getTag();
		}else{
			convertView = inflater.inflate(R.layout.pen_adapter_item,null);
			pageItem = new PageItem(convertView);
			convertView.setTag(pageItem);
		}

		DeviceEntity deviceObject = getItem(position);
		
		pageItem.deviceName.setText(deviceObject.getName());
		pageItem.deviceAddress.setText(deviceObject.getAddress());
		
		
		return convertView;
	}
	
	private class PageItem{
		public TextView deviceName;
		public TextView deviceAddress;
		public PageItem(View view){
			deviceName = (TextView) view.findViewById(R.id.deviceName);
			deviceAddress = (TextView) view.findViewById(R.id.deviceAddress);
		}
	}
}
