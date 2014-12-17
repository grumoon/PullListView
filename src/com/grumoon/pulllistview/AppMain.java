package com.grumoon.pulllistview;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ArrayAdapter;

import com.grumoon.pulllistview.PullListView.OnGetMoreListener;
import com.grumoon.pulllistview.PullListView.OnRefreshListener;

public class AppMain extends Activity {

	private PullListView plvMain;
	private ArrayAdapter<String> adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.app_main);
		initView();
	}

	private void initView() {
		plvMain = (PullListView) findViewById(R.id.plv_main);

		plvMain.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				getData(true);

			}
		});

		plvMain.setOnGetMoreListener(new OnGetMoreListener() {

			@Override
			public void onGetMore() {
				getData(false);

			}
		});

		adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
		plvMain.setAdapter(adapter);

		plvMain.performRefresh();

	}

	private void getData(final boolean isRefresh) {

		//延迟加载数据，模拟耗时操作
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				if (isRefresh) {
					adapter.clear();
				}
				int currentCount = adapter.getCount();
				for (int i = currentCount; i < currentCount + 20; i++) {
					adapter.add("第" + (i + 1) + "条");
				}

				adapter.notifyDataSetChanged();
				plvMain.refreshComplete();
				plvMain.getMoreComplete();
			}
		}, 2000);
	}
}
