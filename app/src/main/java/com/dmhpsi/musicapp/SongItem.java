package com.dmhpsi.musicapp;

import org.json.JSONException;
import org.json.JSONObject;

public class SongItem {
    String songName = "", artist = "", duration = "", id = "";
    SongItem(String songName, String artist, String duration) {
        this.songName = songName;
        this.artist = artist;
        this.duration = duration;
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
}
