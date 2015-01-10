package com.grumoon.pulllistview.sample;

import android.os.Handler;
import android.widget.ArrayAdapter;

import com.grumoon.pulllistview.PullListView;

/**
 * Created by grumoon on 15/1/10.
 */
public class DataUtil {

    public static void getData(final boolean isRefresh, final ArrayAdapter<String> aa, final PullListView plv) {

        //延迟加载数据，模拟耗时操作
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if (isRefresh) {
                    aa.clear();
                }
                int currentCount = aa.getCount();
                for (int i = currentCount; i < currentCount + 20; i++) {
                    aa.add("第" + (i + 1) + "条");
                }

                aa.notifyDataSetChanged();
                plv.refreshComplete();
                plv.getMoreComplete();
            }
        }, 2000);
    }
}
