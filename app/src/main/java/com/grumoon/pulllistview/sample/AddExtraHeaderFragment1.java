package com.grumoon.pulllistview.sample;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.grumoon.pulllistview.PullListView;

public class AddExtraHeaderFragment1 extends Fragment {

    private PullListView plvData;
    private ArrayAdapter<String> adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_add_extra_header1, container, false);
        initView(v);
        return v;
    }


    private void initView(View v) {

        plvData = (PullListView) v.findViewById(R.id.plv_data);

        TextView header = new TextView(getActivity());

        header.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150));

        header.setGravity(Gravity.CENTER);

        header.setBackgroundColor(Color.parseColor("#4db6ac"));

        header.setText("我是额外的Header，我在下拉刷新Header上面");

        plvData.addHeaderView(header);

        plvData.addPullHeaderView();


        plvData.setOnRefreshListener(new PullListView.OnRefreshListener() {

            @Override
            public void onRefresh() {
                DataUtil.getData(true, adapter, plvData);

            }
        });

        plvData.setOnGetMoreListener(new PullListView.OnGetMoreListener() {

            @Override
            public void onGetMore() {
                DataUtil.getData(false, adapter, plvData);

            }
        });

        adapter = new ArrayAdapter<>(getActivity(), R.layout.list_item);
        plvData.setAdapter(adapter);

        plvData.performRefresh();

    }


}
