package com.dmhpsi.musicapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

public class main extends AppCompatActivity {

    Player player;
    boolean isBound = false;
    private ServiceConnection myConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Player.LocalBinder binder = (Player.LocalBinder) service;
            player = binder.getService();
            isBound = true;
            player.setShuffleState(
                    PlaylistManager.getInstance(getApplicationContext()).getShuffleState());
            player.setRepeatState(
                    PlaylistManager.getInstance(getApplicationContext()).getRepeatState());

            TabLayout tabLayout = findViewById(R.id.tab_layout);
            ViewPager viewPager = findViewById(R.id.pager);
            PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(),
                    tabLayout.getTabCount(),
                    player);
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(
                    PlaylistManager.getInstance(getApplicationContext()).getLastPageIdx());
            viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, Player.class);
        bindService(intent, myConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isBound) {
            unbindService(myConnection);
            isBound = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Toolbar myToolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(myToolbar);
//        myToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                Log.i("Toolbar", "Menu item clicked!");
//                return false;
//            }
//        });
        final TabLayout tabLayout = findViewById(R.id.tab_layout);
        final ViewPager viewPager = findViewById(R.id.pager);

        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ma_ic_songlist));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ma_ic_musicnote));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ma_ic_playlist));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                PlaylistManager.getInstance(
                        getApplicationContext()).setLastPageIdx(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
}
