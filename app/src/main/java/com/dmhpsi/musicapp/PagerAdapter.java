package com.dmhpsi.musicapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class PagerAdapter extends FragmentStatePagerAdapter {
    private int mNumOfTabs;
    private Player player;

    PagerAdapter(FragmentManager fm, int NumOfTabs, Player player) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
        this.player = player;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                SongListFrag songListFrag = new SongListFrag();
                songListFrag.setPlayer(player);
                return songListFrag;
            case 1:
                NowPlayingFrag nowPlayingFrag = new NowPlayingFrag();
                nowPlayingFrag.setPlayer(player);
                return nowPlayingFrag;
            case 2:
                return new PlaylistFrag();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
