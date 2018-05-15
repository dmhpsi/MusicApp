package com.dmhpsi.musicapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class EditPlaylistActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist_manage);
        Intent intent = getIntent();
        final String plId = intent.getStringExtra(Constants.ACTIVITY_MESSAGE.PLAYLIST_TO_EDIT);
        ListView plView = findViewById(R.id.playlist_song);
        final Context context = EditPlaylistActivity.this;
        final SongList songList = new SongList();
        final SongAdapter adapter = new SongAdapter(context, songList.getList(), null);
        plView.setAdapter(adapter);
        Playlist pl = PlaylistManager.getInstance(context).getPlaylist(plId);
//        Log.e("plplplpl", pl.toString());
        songList.add(pl.getSongs());
        adapter.notifyDataSetChanged();

        plView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override

            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Confirm remove")
                        .setMessage("Do you want to remove this song from playlist?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                try {
                                    SongItem song = songList.get(position);
                                    PlaylistManager.getInstance(context)
                                            .removeSong(plId, song, context);
                                    songList.clear();
                                    Playlist pl = PlaylistManager.getInstance(context).getPlaylist(plId);
                                    songList.add(pl.getSongs());
                                    adapter.notifyDataSetChanged();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).setNegativeButton("No", null);
                builder.create().show();
            }
        });
        Toolbar toolbar = findViewById(R.id.manage_toolbar);
        toolbar.setTitle(pl.getName());
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditPlaylistActivity.this.finish();
                Intent intent = new Intent(context, main.class);
                context.startActivity(intent);
            }
        });
    }

}
