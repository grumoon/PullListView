package com.grumoon.pulllistview.sample;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;


public class AppMain extends ActionBarActivity {


    private Toolbar toolbar;
    private DrawerLayout dlMain;
    private ActionBarDrawerToggle drawerToggle;

    private Button btnAutoGetMore;
    private Button btnClickGetMore;
    private Button btnAddExtraHeader1;
    private Button btnAddExtraHeader2;


    private Fragment autoGetMoreFragment;
    private Fragment clickGetMoreFragment;
    private Fragment addExtraHeaderFragment1;
    private Fragment addExtraHeaderFragment2;


    private FragmentManager fm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_main);
        initView();
    }

    private void initView() {

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        dlMain = (DrawerLayout) findViewById(R.id.dl_main);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        drawerToggle = new ActionBarDrawerToggle(this, dlMain, toolbar, 0, 0);
        drawerToggle.syncState();
        dlMain.setDrawerListener(drawerToggle);

        btnAutoGetMore = (Button) findViewById(R.id.btn_auto_get_more);
        btnClickGetMore = (Button) findViewById(R.id.btn_click_get_more);
        btnAddExtraHeader1 = (Button) findViewById(R.id.btn_add_extra_header1);
        btnAddExtraHeader2 = (Button) findViewById(R.id.btn_add_extra_header2);


        autoGetMoreFragment = new AutoGetMoreFragment();
        clickGetMoreFragment = new ClickGetMoreFragment();
        addExtraHeaderFragment1 = new AddExtraHeaderFragment1();
        addExtraHeaderFragment2 = new AddExtraHeaderFragment2();

        fm = getSupportFragmentManager();
        fm.beginTransaction().add(R.id.dl_container, autoGetMoreFragment).commit();

        btnAutoGetMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fm.beginTransaction().replace(R.id.dl_container, autoGetMoreFragment).commit();
                dlMain.closeDrawers();
            }
        });

        btnClickGetMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fm.beginTransaction().replace(R.id.dl_container, clickGetMoreFragment).commit();
                dlMain.closeDrawers();
            }
        });

        btnAddExtraHeader1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fm.beginTransaction().replace(R.id.dl_container, addExtraHeaderFragment1).commit();
                dlMain.closeDrawers();
            }
        });

        btnAddExtraHeader2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fm.beginTransaction().replace(R.id.dl_container, addExtraHeaderFragment2).commit();
                dlMain.closeDrawers();
            }
        });


    }


}
