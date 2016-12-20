package cn.robotpen.demo.show;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import cn.robotpen.demo.R;
import cn.robotpen.model.entity.NoteEntity;

public class NoteAdapter extends BaseAdapter {


    private Context mContext;
    private LayoutInflater inflater;
    private ArrayList<NoteEntity> noteData;

    public NoteAdapter(Context context) {
        this.mContext = context;
        inflater = LayoutInflater.from(context);
        noteData = new ArrayList<NoteEntity>();
    }

    @Override
    public int getCount() {
        return noteData.size();
    }

    @Override
    public NoteEntity getItem(int i) {
        return noteData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public void addItem(NoteEntity item){
        noteData.add(item);
    }

    /**
     * 清除集合内容
     */
    public void clearItems(){
        noteData.clear();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        PageItem pageItem;
        if(convertView != null){
            pageItem = (PageItem)convertView.getTag();
        }else{
            convertView = inflater.inflate(R.layout.pen_adapter_item,null);
            pageItem = new NoteAdapter.PageItem(convertView);
            convertView.setTag(pageItem);
        }

        NoteEntity noteObject = getItem(position);

        pageItem.deviceName.setText(noteObject.getTitle());
        pageItem.deviceAddress.setText(noteObject.getNoteKey());
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
