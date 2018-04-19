package com.dmhpsi.musicapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class SongListFrag extends Fragment {
    SongList songList;
    private SongAdapter songAdapter;

    class SongList {
        private ArrayList<SongItem> list = new ArrayList<>();
        void add(SongItem song) {
            list.add(song);
        }
        void clear() {
            list.clear();
        }
        SongItem get(int pos) {
            return list.get(pos);
        }
        ArrayList<SongItem> getList() {
            return list;
        }
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
        protected void onPreExecute() {
        }

        @Override
        protected Wrapper doInBackground(Object... objs) {
            URLConnection urlConn;
            BufferedReader bufferedReader = null;

            View v = (View)objs[1];
            SwipeRefreshLayout rf = v.findViewById(R.id.refresh);
            rf.setRefreshing(true);
            try {
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
        Toast.makeText(getContext(), "Network error!", Toast.LENGTH_SHORT).show();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.song_list_frag, container, false);
        songList = new SongList();
        songAdapter = new SongAdapter(getActivity(), songList.getList());
        ListView songListView = v.findViewById(R.id.song_list);
        songListView.setAdapter(songAdapter);
        songListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                SongItem song = songList.get(i);
                try {
                    Player.getInstance().setSongName(song.songName);
                    Player.getInstance().playAudio(
                            "http://darkha.pythonanywhere.com/getmp3/?id=" + song.id);
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
                g.execute("http://darkha.pythonanywhere.com/getinfo/", v);
            }
        });

        g.execute("http://darkha.pythonanywhere.com/getinfo/", v);
        return v;
    }
}