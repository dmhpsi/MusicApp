package com.dmhpsi.musicapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;
import java.util.Random;

interface OnDataSetChangedListener {
    void onEvent();
}

interface OnLastPlaylistChangedListener {
    void onEvent();
}

class Playlist {
    Playlist(JSONObject object) {
        try {
            songs = new JSONArray();
            name = object.getString("name");
            id = object.getString("id");
            songs = object.getJSONArray("songs");
            sortSongs();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private String name, id;
    private JSONArray songs;
    private ArrayList<Integer> shuffleList;

    Playlist(Playlist pl) {
        if (pl != null) {
            this.id = pl.id;
            this.name = pl.name;
            try {
                this.songs = new JSONArray(pl.songs.toString());
                sortSongs();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            this.shuffle();
        }
    }

    public int count() {
        if (songs != null)
            return songs.length();
        else
            return 0;
    }

    Playlist(String name, JSONArray songs) {
        this.name = name;
        try {
            this.songs = new JSONArray(songs.toString());
            sortSongs();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        this.shuffle();
    }

    Playlist(String name, @Nullable SongItem song) {
        this.name = name;
        this.songs = new JSONArray();
        if (song != null) {
            this.songs.put(song.toJSONObject());
        }
        this.shuffle();
    }

    private void sortSongs() {
        int c = count();
        ArrayList<JSONObject> s = new ArrayList<>();
        for (int i = 0; i < c; i++) {
            try {
                s.add(songs.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Collections.sort(s, new SongJsonSort());
        songs = new JSONArray();
        for (int i = 0; i < c; i++) {
            songs.put(s.get(i));
        }
    }

    private void shuffle() {
        shuffleList = new ArrayList<>();
        if (count() > 0) {
            Random random = new Random();
            random.nextInt();
            for (int i = 0; i < count(); i++) {
                shuffleList.add(i);
            }
            Collections.shuffle(shuffleList);
            shuffleList.add(shuffleList.get(0));
        }
    }

    int addSong(SongItem song) {
        for (int i = 0; i < count(); i++) {
            try {
                if (songs.getJSONObject(i).getString("id").equals(song.id)) {
                    return -1;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        songs.put(song.toJSONObject());
        shuffle();
        return 0;
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

    void removeSong(SongItem song) {
        for (int i = 0; i < count(); i++) {
            try {
                if (songs.getJSONObject(i).getString("id").equals(song.id)) {
                    songs.remove(i);
                    sortSongs();
                    shuffle();
                    return;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public SongItem getPrevSongOf(String songId, boolean random) {
        try {
            int count = songs.length();
            for (int i = count - 1; i >= 0; i--) {
                JSONObject song = songs.getJSONObject(i);
                if (song.getString("id").equals(songId)) {
                    if (random) {
                        return new SongItem(songs.getJSONObject(shuffleList.get(shuffleList.lastIndexOf(i) - 1)));
                    }
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

    void removeSong(String songId) {
        try {
            for (int i = 0; i < count(); i++) {
                JSONObject song = songs.getJSONObject(i);
                if (song.getString("id").equals(songId)) {
                    songs.remove(i);
                    sortSongs();
                    shuffle();
                    return;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    ArrayList<SongItem> getSongs() {
        ArrayList<SongItem> songItems = new ArrayList<>();
        try {
            for (int i = 0; i < count(); i++) {
                JSONObject song = songs.getJSONObject(i);
                songItems.add(new SongItem(song));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return songItems;
    }

    SongItem getNextSongOf(String songId, boolean random) {
        try {
            for (int i = 0; i < count(); i++) {
                JSONObject song = songs.getJSONObject(i);
                if (song.getString("id").equals(songId)) {
                    if (random) {
                        return new SongItem(songs.getJSONObject(shuffleList.get(shuffleList.indexOf(i) + 1)));
                    }
                    if (i < count() - 1) {
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

    class SongJsonSort implements Comparator<JSONObject> {
        @Override
        public int compare(JSONObject object, JSONObject t1) {
            try {
                return object.getString("name").compareToIgnoreCase(t1.getString("name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return 0;
        }
    }

    @Override
    public String toString() {
        return "{\"name\": \"" + name + "\""
//                + ", \"songCount\": " + count
                + ", \"id\": \"" + id
                + "\", \"songs\": " + songs.toString() + "}";
    }
}

public class PlaylistManager {
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
            Collections.sort(playlists, new PlaylistCompare());
            if (data.isNull("repeatState")) {
                repeatState = RepeatStates.REPEAT_ALL;
            } else {
                repeatState = RepeatStates.valueOf(data.getString("repeatState"));
            }
            if (data.isNull("shuffleState")) {
                shuffleState = ShuffleStates.SHUFFLE_OFF;
            } else {
                shuffleState = ShuffleStates.valueOf(data.getString("shuffleState"));
            }
            Log.e("state", "" + shuffleState + " " + repeatState);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Playlist manager", "File not found");
            plCount = 0;
            repeatState = RepeatStates.REPEAT_ALL;
            shuffleState = ShuffleStates.SHUFFLE_OFF;
            lastPl = null;
            lastSong = null;
            save(context);
        }
    }

    private final String key = "pl";
    private final String LASTPLID = "lastplid";
    private int plCount;
    private ArrayList<Playlist> playlists;
    private SongItem lastSong;
    private Playlist lastPl;
    private RepeatStates repeatState;
    private ShuffleStates shuffleState;
    private int lastPageIdx = 1;

    public void setLastPageIdx(int lastPageIdx) {
        this.lastPageIdx = lastPageIdx;
    }

    public int getLastPageIdx() {
        return lastPageIdx;
    }

    public void setRepeatState(RepeatStates repeatState, Context context) {
        this.repeatState = repeatState;
        save(context);
    }

    public RepeatStates getRepeatState() {
        return repeatState;
    }

    public void setShuffleState(ShuffleStates shuffleState, Context context) {
        this.shuffleState = shuffleState;
        save(context);
    }

    public ShuffleStates getShuffleState() {
        return shuffleState;
    }

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
        Collections.sort(playlists, new PlaylistCompare());
        save(context);
        return 1;
    }

    private void save(Context context) {
        FileOutputStream outputStream;
        try {
            outputStream = context.openFileOutput("mafile_" + key, Context.MODE_PRIVATE);
            StringBuilder string = new StringBuilder("{\"count\": " + plCount
                    + ", \"lastPl\": " + lastPl
                    + ", \"lastSong\": " + lastSong
                    + ", \"repeatState\": " + repeatState
                    + ", \"shuffleState\": " + shuffleState
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

    public void removePlaylist(String plId, Context context) {
        for (int i = 0; i < plCount; i++) {
            if (playlists.get(i).getId().equals(plId)) {
                playlists.remove(i);
                plCount--;
                Collections.sort(playlists, new PlaylistCompare());
                save(context);
                return;
            }
        }
    }

    public Playlist getPlaylist(int index) {
        if (index < 0) {
            if (lastPl == null) {
                lastPl = new Playlist("", (SongItem) null);
                lastPl.setId(LASTPLID);
            }
            return new Playlist(lastPl);
        } else {
            return new Playlist(playlists.get(index));
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

    public void addSong(String playlistid, SongItem song, Context context) {
        if (Objects.equals(playlistid, LASTPLID)) {
            if (lastPl == null) {
                lastPl = new Playlist("", song);
            } else {
                lastPl.addSong(song);
            }
            if (onLastPlaylistChangedListener != null) {
                onLastPlaylistChangedListener.onEvent();
            }
            save(context);
            Toast.makeText(context, "Song added successfully!", Toast.LENGTH_SHORT).show();
        } else {
            for (Playlist playlist : playlists) {
                if (playlist.getId().equals(playlistid)) {
                    if (playlist.addSong(new SongItem(song)) == -1) {
                        Toast.makeText(context, "Song add failed! Song already added!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Song added successfully!", Toast.LENGTH_SHORT).show();
                        save(context);
                    }
                    break;
                }
            }
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
            this.lastPl = new Playlist("", new JSONArray());
        } else {
            this.lastPl = new Playlist(lastPl);
        }
        this.lastPl.setId(LASTPLID);
        if (onLastPlaylistChangedListener != null) {
            onLastPlaylistChangedListener.onEvent();
        }
        save(context);
    }

    @Nullable
    public Playlist getPlaylist(String id) {
        for (Playlist pl : playlists) {
            if (pl.getId().equals(id)) {
                return new Playlist(pl);
            }
        }
        return null;
    }

    public SongItem getNextSongItem(String songId, boolean random) {
        return lastPl.getNextSongOf(songId, random);
    }

    public SongItem getPrevSongItem(String songId, boolean random) {
        return lastPl.getPrevSongOf(songId, random);
    }

    public void renamePlaylist(String plId, String newName, Context context) {
        for (Playlist pl : playlists) {
            if (pl.getId().equals(plId)) {
                pl.setName(newName);
                save(context);
                return;
            }
        }
    }

    public void removeSong(String playlistid, SongItem song, Context context) {
        for (Playlist pl : playlists) {
            if (pl.getId().equals(playlistid)) {
                pl.removeSong(song);
                save(context);
                return;
            }
        }
    }

    class PlaylistCompare implements Comparator<Playlist> {
        @Override
        public int compare(Playlist playlist, Playlist t1) {
            return playlist.getName().compareToIgnoreCase(t1.getName());
        }
    }
}
