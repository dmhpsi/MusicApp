package com.dmhpsi.musicapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;

public class PlaylistFrag extends Fragment {
    SongList playlists;
    SongAdapter adapter;
    PlaylistManager playlistManager;
    Player player;

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void rerender() {
        playlistManager = PlaylistManager.getInstance(getContext());
        playlists.clear();
        for (int i = 0; i < playlistManager.getPlCount(); i++) {
            Playlist pl = playlistManager.getPlaylist(i);
            playlists.add(new SongItem(pl.getName(),
                    "" + pl.count() + " songs",
                    "",
                    pl.getId()));
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.playlist_frag, container, false);
        playlists = new SongList();
        adapter = new SongAdapter(getActivity(), playlists.getList(), ListPurpose.PLAYLIST);
        ListView playlistsView = view.findViewById(R.id.playlists);
        playlistsView.setAdapter(adapter);
        playlistsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    SongItem songItem = (SongItem) view.getTag();
                    Playlist playlist = PlaylistManager.getInstance(getContext()).getPlaylist(songItem.id);
                    if (playlist.count() != 0) {
                        player.playPlaylist(playlist, "");
                        rerender();
                        Intent startIntent = new Intent(getContext(), Player.class);
                        startIntent.setAction(Constants.PLAYER.START_SERVICE);
                        getActivity().startService(startIntent);
                        ViewPager viewPager = getActivity().findViewById(R.id.pager);
                        viewPager.setCurrentItem(1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            rerender();
        } catch (Exception e) {
            //
        }

        PlaylistManager.getInstance(getContext()).setOnDataSetChangedListener(new OnDataSetChangedListener() {
            @Override
            public void onEvent() {
                rerender();
            }
        });

        FloatingActionButton button = view.findViewById(R.id.add_playlist_btn);
        button.bringToFront();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.add_playlist, null);
                builder.setView(dialogView)
                        .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                EditText editText = dialogView.findViewById(R.id.pl_name);
                                String input = editText.getText().toString();
                                if (!input.matches("")) {
                                    Playlist pl = new Playlist(input, new JSONArray());
                                    if (playlistManager.addPlaylist(pl, getContext()) == -1) {
                                        Toast.makeText(getContext(),
                                                "Playlist add failed! Duplicate playlist name found",
                                                Toast.LENGTH_LONG)
                                                .show();
                                    } else {
                                        rerender();
                                        Toast.makeText(getContext(),
                                                "Playlist added successfully!",
                                                Toast.LENGTH_LONG)
                                                .show();
                                    }
                                }
                            }
                        }).setNegativeButton("Cancel", null);
                builder.create().show();
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }
}
