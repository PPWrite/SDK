package cn.robotpen.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.ListView;

public class DemoListActivity extends Activity {

	private ListView demoListView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.demolist);
		demoListView = (ListView) findViewById(R.id.demoListView);
		privatestaticfinal String[] strs = new String[] {
			    "first", "second", "third", "fourth", "fifth"
			    };
		
		demoListView.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, strs));

    }
	}
}
