package cn.robotpenDemo.point;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by wang on 2017/3/3.
 */

public class SelectMainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    List<String> itemList;
    @BindView(R.id.mainactivity_listview)
    ListView mainActivityListview;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_main);
        ButterKnife.bind(this);
        itemList = new ArrayList<String>();
        itemList.add("集成SDK方式1：onStateChanged onPenServiceError onPenPositionChanged接口demo");
        itemList.add("集成SDK方式2：init connect disconnect onPenServiceStarted onReceiveDot等接口demo");
        ListAdapter itemAdapter = new ArrayAdapter(this,R.layout.support_simple_spinner_dropdown_item,itemList);
        mainActivityListview.setAdapter(itemAdapter);
        mainActivityListview.setOnItemClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent;
        switch (position){
            case 0:
                intent = new Intent(SelectMainActivity.this, MainActivity.class);
                startActivity(intent);
                break;
            case 1:
                intent = new Intent(SelectMainActivity.this, MainTwoActivity.class);
                startActivity(intent);
                break;
        }
    }
}
