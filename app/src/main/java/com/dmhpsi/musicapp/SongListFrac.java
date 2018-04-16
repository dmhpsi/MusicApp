package com.dmhpsi.musicapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

class SongList {
    private static SongList instance = null;
    private ArrayList<SongItem> list = new ArrayList<>();
    private SongList() {
    }
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
    static SongList getInstance() {
        if(instance == null) {
            instance = new SongList();
        }
        return instance;
    }
}

class Wrapper {
    View view;
    JSONObject json;

    Wrapper(String json, View view) {
        this.view = view;
        try {
            this.json = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
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
            return null;
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
            SongList.getInstance().clear();
            for (int i = 0; i < count; i++) {
                SongList.getInstance().add(new SongItem(data.getJSONObject(i)));
            }
            SongListFrac.getInstance().rerender();
            SwipeRefreshLayout rf = response.view.findViewById(R.id.refresh);
            rf.setRefreshing(false);
        } catch (JSONException e) {
            Log.e("Async", "Json error!");
        }
    }
}

public class SongListFrac extends Fragment {
    private static SongListFrac instance;
    private SongAdapter songAdapter;
    public SongListFrac() {}
    public void rerender() {
        songAdapter.notifyDataSetChanged();
    }
    static SongListFrac getInstance() {
        if(instance == null) {
            instance = new SongListFrac();
        }
        return instance;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.song_list_frac, container, false);
        songAdapter = new SongAdapter(getActivity(), SongList.getInstance().getList());
        ListView songListView = v.findViewById(R.id.song_list);
        songListView.setAdapter(songAdapter);
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
