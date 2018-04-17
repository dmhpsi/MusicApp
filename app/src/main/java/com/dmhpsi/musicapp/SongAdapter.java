package com.dmhpsi.musicapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class SongAdapter extends ArrayAdapter <SongItem> {

    public SongAdapter(Context context, ArrayList<SongItem> songList) {
        super(context, 0, songList);
    }

    @Override
    public SongItem getItem(int pos) {
        return SongList.getInstance().get(pos);
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
        // Lookup view for data population
        TextView songName = convertView.findViewById(R.id.song_name);
        TextView songArtist = convertView.findViewById(R.id.song_artist);
        // Populate the data into the template view using the data object
        assert song != null;
        songName.setText(song.songName);
        songArtist.setText(song.artist);

        RelativeLayout top_view = convertView.findViewById(R.id.top_view);
        if (convertView.findViewById(R.id.ALT) == null) {
            RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            param.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            ImageButton btn = new ImageButton(convertView.getContext(), null, android.R.attr.borderlessButtonStyle);
            btn.setId(R.id.ALT);
            btn.setImageResource(R.drawable.ma_ic_more_im);
            btn.setMinimumWidth(0);
            btn.setLayoutParams(param);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i("btn", song.songName);
                }
            });
            top_view.addView(btn);
        }

        // Return the completed view to render on screen
        return convertView;
    }
}
