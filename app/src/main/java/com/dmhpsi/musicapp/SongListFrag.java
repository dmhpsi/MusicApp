package com.dmhpsi.musicapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class SongListFrag extends Fragment {
    SongList songList;
    private SongAdapter songAdapter;
    Player player;

    public void setPlayer(Player player) {
        this.player = player;
    }

    class Wrapper {
        View view;
        JSONObject json;

        Wrapper(String json, View view) {
            this.view = view;
            try {
                this.json = new JSONObject(json);
            } catch (Exception e) {
                this.json = null;
            }
        }
    }

    class GetDataTask extends AsyncTask<Object, Void, Wrapper> {
        @Override
        protected Wrapper doInBackground(Object... objs) {
            URLConnection urlConn;
            BufferedReader bufferedReader = null;

            View v = (View)objs[1];
            SwipeRefreshLayout rf = v.findViewById(R.id.refresh);
            try {
                rf.setRefreshing(true);
                URL url = new URL((String)objs[0]);
                urlConn = url.openConnection();
                bufferedReader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));

                StringBuilder stringBuffer = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuffer.append(line);
                }

                return new Wrapper(stringBuffer.toString(), v);
            } catch (Exception ex) {
                return new Wrapper(null, v);
            } finally {
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }


        @Override
        protected void onPostExecute(Wrapper response) {
            try {
                int count = response.json.getInt("count");
                JSONArray data = response.json.getJSONArray("data");
                songList.clear();
                for (int i = 0; i < count; i++) {
                    songList.add(new SongItem(data.getJSONObject(i)));
                }
                rerender();
            } catch (Exception e) {
                displayError();
            }
            SwipeRefreshLayout rf = response.view.findViewById(R.id.refresh);
            rf.setRefreshing(false);
        }
    }


    public void rerender() {
        songAdapter.notifyDataSetChanged();
    }
    public void displayError() {
        //Toast.makeText(getContext(), "Network error!", Toast.LENGTH_SHORT).show();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.song_list_frag, container, false);
        songList = new SongList();
        songAdapter = new SongAdapter(getActivity(), songList.getList(), ListPurpose.ALL_SONG);
        ListView songListView = v.findViewById(R.id.song_list);
        songListView.setAdapter(songAdapter);
        songListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                SongItem song = songList.get(i);
                try {
                    Intent startIntent = new Intent(getContext(), Player.class);
                    startIntent.setAction(Constants.PLAYER.START_SERVICE);
                    getActivity().startService(startIntent);
                    player.playSong(song);
                    ViewPager viewPager = getActivity().findViewById(R.id.pager);
                    viewPager.setCurrentItem(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        final GetDataTask g = new GetDataTask();

        SwipeRefreshLayout refreshLayout =  v.findViewById(R.id.refresh);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                GetDataTask g = new GetDataTask();
                g.execute(Constants.URL.GET_INFO, v);
            }
        });

        g.execute(Constants.URL.GET_INFO, v);
        return v;
    }
}
