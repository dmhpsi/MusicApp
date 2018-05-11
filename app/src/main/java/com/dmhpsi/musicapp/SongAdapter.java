package com.dmhpsi.musicapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

class SongItem {
    String songName = "", artist = "", duration = "", id = "";

    SongItem(String songName, String artist, String duration, String id) {
        this.songName = songName;
        this.artist = artist;
        this.duration = duration;
        this.id = id;
    }

    SongItem(JSONObject obj) {
        try {
            this.songName = obj.getString("name");
            this.artist = obj.getString("artist");
            this.duration = obj.getString("duration");
            this.id = obj.getString("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    SongItem(SongItem item) {
        if (item != null) {
            this.songName = item.songName;
            this.artist = item.artist;
            this.duration = item.duration;
            this.id = item.id;
        }
    }

    JSONObject toJSONObject() {
        JSONObject jo = new JSONObject();
        try {
            jo.put("name", songName);
            jo.put("artist", artist);
            jo.put("duration", duration);
            jo.put("id", id);
            return jo;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean equals(SongItem item) {
        return item != null && (Objects.equals(this.id, item.id));
    }

    @Override
    public String toString() {
        return toJSONObject().toString();
    }
}

class SongList {
    private ArrayList<SongItem> list = new ArrayList<>();

    void add(SongItem song) {
        list.add(song);
    }

    void add(ArrayList<SongItem> songs) {
        list.addAll(songs);
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

public class SongAdapter extends ArrayAdapter <SongItem> {
    private ListPurpose listPurpose;
    private SongItem specialItem;

    SongAdapter(Context context, ArrayList<SongItem> songList, ListPurpose listPurpose) {
        super(context, 0, songList);
        this.listPurpose = listPurpose;
    }

    public void setSpecialItem(SongItem specialItem) {
        this.specialItem = new SongItem(specialItem);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // Get the data item for this position
        final SongItem song = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.song_item, parent, false);
        }
        if (song != null) {
            if (specialItem != null && song.id.equals(specialItem.id)) {
                convertView.setBackgroundResource(R.color.colorHighLightTrans);
            } else {
                convertView.setBackgroundResource(R.color.colorTrans);
            }
        }
        // Lookup view for data population
        TextView songName = convertView.findViewById(R.id.song_name);
        TextView songArtist = convertView.findViewById(R.id.song_artist);
        // Populate the data into the template view using the data object
        if (song != null) {
            songName.setText(song.songName);
            songArtist.setText(song.artist);
            convertView.setTag(song);
        }

        ImageButton btn = convertView.findViewById(R.id.more_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listPurpose == ListPurpose.ALL_SONG) {
                    PopupMenu menu = new PopupMenu(getContext(), view, Gravity.END);
                    menu.getMenuInflater().inflate(R.menu.song_list_menu, menu.getMenu());
                    menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            if (menuItem.getItemId() == R.id.details) {
                                builder.setTitle("Details");
                            } else {
                                builder.setTitle("Add song to ...")
                                        .setItems(PlaylistManager.getInstance(getContext()).getPlNames(), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                Playlist pl = PlaylistManager.getInstance(getContext()).getPlaylist(i - 1);
                                                Log.e("pl", pl.toString());
                                                PlaylistManager.getInstance(getContext()).addSong(pl.getId(), song, getContext());
                                            }
                                        });
                            }
                            builder.create().show();
                            return true;
                        }
                    });
                    menu.show();
                } else if (listPurpose == ListPurpose.PLAYLIST) {
                    PopupMenu menu = new PopupMenu(getContext(), view, Gravity.END);
                    menu.getMenuInflater().inflate(R.menu.playlist_menu, menu.getMenu());
                    menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            if (item.getItemId() == R.id.manage) {

                            } else {
                                builder.setTitle("Confirm delete")
                                        .setMessage("Do you want to delete this playlist?")
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                if (song != null) {
                                                    PlaylistManager.getInstance(getContext())
                                                            .removePlaylist(Objects.requireNonNull(song).id, getContext());
                                                    remove(song);
                                                    notifyDataSetChanged();
                                                }
                                            }
                                        }).setNegativeButton("No", null);
                                builder.create().show();
                            }
                            return true;
                        }
                    });
                    menu.show();
                } else if (listPurpose.equals(ListPurpose.PLAYLIST_SONG)) {
                    PopupMenu menu = new PopupMenu(getContext(), view, Gravity.END);
                    menu.getMenuInflater().inflate(R.menu.pl_song_menu, menu.getMenu());
                    menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle("Confirm remove")
                                    .setMessage("Do you want to remove this song from Now Playing?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if (song != null) {
                                                remove(song);
                                                PlaylistManager.getInstance(getContext())
                                                        .removeSongFromLastPlaylist(song.id, getContext());
                                                notifyDataSetChanged();
                                            }
                                        }
                                    }).setNegativeButton("No", null);
                            builder.create().show();
                            return true;
                        }
                    });
                    menu.show();
                }

            }
        });
        return convertView;
    }
}
