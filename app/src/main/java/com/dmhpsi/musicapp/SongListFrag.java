package com.dmhpsi.musicapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class SongListFrag extends Fragment {
    SongList songList;
    private SongAdapter songAdapter;
    Player player;
    int page, totalpg;
    String path;

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
            final SwipeRefreshLayout rf = v.findViewById(R.id.refresh);
            try {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rf.setRefreshing(true);
                    }
                });
                URL url = new URL(Constants.URL.GET_INFO + page + "&pgsize=" + Constants.URL.PAGE_SIZE + "&" + objs[0]);
                urlConn = url.openConnection();
                bufferedReader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));

                StringBuilder stringBuffer = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuffer.append(line);
                }

                return new Wrapper(stringBuffer.toString(), v);
            } catch (Exception ex) {
                ex.printStackTrace();
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
                totalpg = (response.json.getInt("total") - 1) / Constants.URL.PAGE_SIZE;
                if (count > 0) {
                    JSONArray data = response.json.getJSONArray("data");
                    songList.clear();
                    for (int i = 0; i < count; i++) {
                        songList.add(new SongItem(data.getJSONObject(i)));
                    }
                    rerender();
                    Button pv = response.view.findViewById(R.id.page);
                    pv.setText("" + (page + 1) + "/" + (totalpg + 1));
                } else {
                    if (page < 0) {
                        page = 0;
                    } else {
                        page = (totalpg - 1) / Constants.URL.PAGE_SIZE;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
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
        page = 0;
        path = "";
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
        GetDataTask g = new GetDataTask();

        SwipeRefreshLayout refreshLayout =  v.findViewById(R.id.refresh);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                GetDataTask g = new GetDataTask();
                g.execute(path, v);
            }
        });

        g.execute(path, v);

        SearchView searchView = v.findViewById(R.id.search);
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null && !b) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                GetDataTask g = new GetDataTask();
                try {
                    page = 0;
                    path = "query=" + URLEncoder.encode(newText, "utf-8");
                    g.execute(path, v);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                return false;
            }
        });

        v.findViewById(R.id.prev_page).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                page--;
                GetDataTask g = new GetDataTask();
                g.execute(path, v);
            }
        });
        v.findViewById(R.id.next_page).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                page++;
                GetDataTask g = new GetDataTask();
                g.execute(path, v);
            }
        });
        v.findViewById(R.id.page).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final NumberPicker numberPicker = new NumberPicker(getContext());
                numberPicker.setMinValue(1);
                numberPicker.setMaxValue(totalpg + 1);
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Select page")
                        .setView(numberPicker)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                page = numberPicker.getValue() - 1;
                                GetDataTask g = new GetDataTask();
                                g.execute(path, v);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create().show();
            }
        });
        return v;
    }
}
