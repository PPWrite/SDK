package cn.robotpen.demo;

import java.util.ArrayList;

import cn.robotpen.model.DeviceObject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * 
 * @author Xiaoz
 * @date 2015年6月12日 上午11:58:28
 *
 * Description
 */
public class PenAdapter extends BaseAdapter{
	public final static String TAG = PenAdapter.class.getSimpleName();

	private Context mContext;
	private LayoutInflater inflater;
	private ArrayList<DeviceObject> mPenDevices;

	public PenAdapter(Context context) {
		this.mContext = context;
		inflater = LayoutInflater.from(context);
		mPenDevices = new ArrayList<DeviceObject>();
	}

	@Override
	public int getCount() {
		return mPenDevices.size();
	}

	@Override
	public DeviceObject getItem(int i) {
		return mPenDevices.get(i);
	}

	@Override
	public long getItemId(int i) {
		return i;
	}
	
	public void addItem(DeviceObject item){
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
		
		DeviceObject deviceObject = getItem(position);
		
		pageItem.deviceName.setText(deviceObject.name);
		pageItem.deviceAddress.setText(deviceObject.address);
		
		
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
