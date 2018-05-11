package com.dmhpsi.musicapp;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

interface OnDataSetChangedListener {
    void onEvent();
}

interface OnLastPlaylistChangedListener {
    void onEvent();
}

class Playlist {
    private String name, id;
    int count;
    private JSONArray songs;

    int addSong(SongItem song) {
        for (int i = 0; i < count; i++) {
            try {
                if (songs.getJSONObject(i).getString("id").equals(song.id)) {
                    return -1;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        count++;
        songs.put(song.toJSONObject());
        return 0;
    }

    void removeSong(String songId) {
        try {
            for (int i = 0; i < count; i++) {
                JSONObject song = songs.getJSONObject(i);
                if (song.getString("id").equals(songId)) {
                    songs.remove(i);
                    count--;
                    return;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    ArrayList<SongItem> getSongs() {
        ArrayList<SongItem> songItems = new ArrayList<>();
        try {
            for (int i = 0; i < count; i++) {
                JSONObject song = songs.getJSONObject(i);
                songItems.add(new SongItem(song));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return songItems;
    }

    SongItem getSong(String songId) {
        try {
            for (int i = 0; i < count; i++) {
                JSONObject song = songs.getJSONObject(i);
                if (song.getString("id").equals(songId)) {
                    return new SongItem(song);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    SongItem getSong(int id) {
        if (songs.isNull(id)) {
            return null;
        } else {
            try {
                return new SongItem(songs.getJSONObject(id));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    SongItem getNextSongOf(String songId) {
        try {
            for (int i = 0; i < count; i++) {
                JSONObject song = songs.getJSONObject(i);
                if (song.getString("id").equals(songId)) {
                    if (i < count - 1) {
                        return new SongItem(songs.getJSONObject(i + 1));
                    } else {
                        return new SongItem(songs.getJSONObject(0));
                    }
                }
            }
            return new SongItem(songs.getJSONObject(0));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public SongItem getPrevSongOf(String songId) {
        try {
            for (int i = count - 1; i >= 0; i--) {
                JSONObject song = songs.getJSONObject(i);
                if (song.getString("id").equals(songId)) {
                    if (i > 0) {
                        return new SongItem(songs.getJSONObject(i - 1));
                    } else {
                        return new SongItem(songs.getJSONObject(count - 1));
                    }
                }
            }
            return new SongItem(songs.getJSONObject(count - 1));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    Playlist(JSONObject object) {
        try {
            songs = new JSONArray();
            name = object.getString("name");
            count = object.getInt("songCount");
            id = object.getString("id");
            songs = object.getJSONArray("songs");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    Playlist(Playlist pl) {
        if (pl != null) {
            this.id = pl.id;
            this.name = pl.name;
            this.count = pl.count;
            this.songs = pl.songs;
        }
    }

    Playlist(String name, int count, JSONArray songs) {
        this.name = name;
        this.count = count;
        this.songs = songs;
    }

    Playlist(String name, SongItem song) {
        this.name = name;
        this.count = 1;
        this.songs = new JSONArray();
        this.songs.put(song.toJSONObject());
    }

    @Override
    public String toString() {
        return "{\"name\": \"" + name
                + "\", \"songCount\": " + count
                + ", \"id\": \"" + id
                + "\", \"songs\": " + songs.toString() + "}";
    }
}

public class PlaylistManager {
    private final String key = "pl";
    private final String LASTPLID = "lastplid";
    private int plCount;
    private ArrayList<Playlist> playlists;
    private SongItem lastSong;
    private Playlist lastPl;

    private OnDataSetChangedListener onDataSetChangedListener;

    void setOnDataSetChangedListener(OnDataSetChangedListener listener) {
        onDataSetChangedListener = listener;
    }

    private OnLastPlaylistChangedListener onLastPlaylistChangedListener;

    void setOnLastPlaylistChangedListener(OnLastPlaylistChangedListener listener) {
        onLastPlaylistChangedListener = listener;
    }

    private static PlaylistManager INSTANCE = null;

    public static PlaylistManager getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new PlaylistManager(context);
        }
        return (INSTANCE);
    }

    private PlaylistManager(Context context) {
        FileInputStream inputStream;
        playlists = new ArrayList<>();
        try {
            inputStream = context.openFileInput("mafile_" + key);
            int length = inputStream.available();
            byte bytes[] = new byte[length];
            inputStream.read(bytes);
            JSONObject data = new JSONObject(new String(bytes));
            Log.e("File", data.toString(4));
            plCount = data.getInt("count");
            if (data.isNull("lastPl")) {
                lastPl = null;
            } else {
                lastPl = new Playlist(data.getJSONObject("lastPl"));
            }
            if (data.isNull("lastSong")) {
                lastSong = null;
            } else {
                lastSong = new SongItem(data.getJSONObject("lastSong"));
            }
            JSONArray array = data.getJSONArray("data");
            for (int i = 0; i < plCount; i++) {
                playlists.add(new Playlist(array.getJSONObject(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Playlist manager", "File not found");
            plCount = 0;
            save(context);
        }
    }

    private void save(Context context) {
        FileOutputStream outputStream;
        try {
            outputStream = context.openFileOutput("mafile_" + key, Context.MODE_PRIVATE);
            StringBuilder string = new StringBuilder("{\"count\": " + plCount
                    + ", \"lastPl\": " + lastPl
                    + ", \"lastSong\": " + lastSong
                    + ", \"data\": [");
            for (int i = 0; i < plCount; i++) {
                string.append(playlists.get(i).toString());
                if (i < plCount - 1) {
                    string.append(",");
                }
            }
            string.append("]}");
//            Log.e("Save", new JSONObject(string.toString()).toString(4));
            outputStream.write(string.toString().getBytes());
            outputStream.close();
            if (onDataSetChangedListener != null) {
                onDataSetChangedListener.onEvent();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int addPlaylist(Playlist playlist, Context context) {
        Playlist __pl = new Playlist(playlist);
        for (Playlist pl : playlists) {
            if (Objects.equals(__pl.getName(), pl.getName())) {
                return -1;
            }
        }
        plCount++;
        __pl.setId(String.valueOf(new Date().getTime()));
        playlists.add(__pl);
        save(context);
        return 1;
    }

    public void removePlaylist(String plId, Context context) {
        for (int i = 0; i < plCount; i++) {
            if (playlists.get(i).getId().equals(plId)) {
                playlists.remove(i);
                plCount--;
                save(context);
                return;
            }
        }
    }

    public String[] getPlNames() {
        ArrayList<String> nl = new ArrayList<>();
        nl.add("Now playing");
        for (Playlist pl : playlists) {
            nl.add(pl.getName());
        }
        String[] ar = new String[plCount];
        return nl.toArray(ar);
    }

    public int getPlCount() {
        return plCount;
    }

    public ArrayList<Playlist> getPlaylists() {
        return playlists;
    }

    public Playlist getPlaylist(int index) {
        if (index < 0) {
            return new Playlist(lastPl);
        } else {
            return new Playlist(playlists.get(index));
        }
    }

    public SongItem getLastSong() {
        return new SongItem(lastSong);
    }

    public void setLastSong(SongItem lastSong, Context context) {
        this.lastSong = new SongItem(lastSong);
        save(context);
    }

    public void removeSongFromLastPlaylist(String songId, Context context) {
        lastPl.removeSong(songId);
        save(context);
    }

    public Playlist getLastPlaylist() {
        return new Playlist(lastPl);
    }

    public void defineLastPlaylist(Playlist lastPl, Context context) {
        if (lastPl == null) {
            this.lastPl = new Playlist("", 0, new JSONArray());
        } else {
            this.lastPl = new Playlist(lastPl);
        }
        this.lastPl.setId(LASTPLID);
        if (onLastPlaylistChangedListener != null) {
            onLastPlaylistChangedListener.onEvent();
        }
        save(context);
    }

    public Playlist getPlaylist(String id) {
        for (Playlist pl : playlists) {
            if (pl.getId().equals(id)) {
                return new Playlist(pl);
            }
        }
        return null;
    }

    public SongItem getNextSongItem(String songId) {
        return lastPl.getNextSongOf(songId);
    }

    public SongItem getPrevSongItem(String songId) {
        return lastPl.getPrevSongOf(songId);
    }

    public SongItem getSongItemById(String plId, String songId) {
        return getPlaylist(plId).getSong(songId);
    }

    public void addSong(String playlistid, SongItem song, Context context) {
        Playlist target = null;
        if (Objects.equals(playlistid, LASTPLID)) {
            target = lastPl;
        } else {
            for (Playlist playlist : playlists) {
                if (playlist.getId().equals(playlistid)) {
                    target = playlist;
                    break;
                }
            }
        }
        if (target == null || target.addSong(new SongItem(song)) == -1) {
            Toast.makeText(context, "Song add failed! Song already added!", Toast.LENGTH_SHORT).show();
        } else {
            Log.e("targetid", target.getId());
            save(context);
            Toast.makeText(context, "Song added successfully!", Toast.LENGTH_SHORT).show();
        }
        if (onLastPlaylistChangedListener != null) {
            onLastPlaylistChangedListener.onEvent();
        }
    }
}
