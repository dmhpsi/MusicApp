package com.dmhpsi.musicapp;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

public class NowPlayingFrac extends Fragment {
    private static NowPlayingFrac instance;
    public NowPlayingFrac() {}
    static NowPlayingFrac getInstance() {
        if(instance == null) {
            instance = new NowPlayingFrac();
        }
        return instance;
    }


    class TimeUpdater {
        void start() {
            Runnable runnable = new Runnable() {
                public void run() {
                    while (true) {
                        setTime();
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
            };
            new Thread(runnable).start();
        }

    }

    TimeUpdater timeUpdater;
    boolean seekerTouching;

    public void setSongName(final String newName) {
        try {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    TextView playing_song_name = getActivity().findViewById(R.id.playing_song_name);
                    playing_song_name.setText(newName);
                }
            });
        }
        catch (Exception e) {
            //
        }
    }

    private void setTime() {
        try {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        TextView time = getActivity().findViewById(R.id.time);
                        int newTime = Player.getInstance().getCurrentPosition();
                        int t = Math.round(newTime / 1000f);
                        String x = String.format("%02d:%02d", t / 60, t % 60);
                        time.setText(x);
                        if (!seekerTouching) {
                            SeekBar seekBar = getActivity().findViewById(R.id.seek_bar);
                            seekBar.setProgress(Math.round(newTime * 100.0f / Player.getInstance().getCurrentSongDuration()));
                        }
                    } catch (Exception e) {
                        //
                    }
                }
            });
        }
        catch (Exception e) {
            //
        }
    }

    public void setLoadingProgress(final int progress) {
        try {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        ProgressBar buffer = getActivity().findViewById(R.id.progress_bar);
                        buffer.setProgress(progress);
                    } catch (Exception e) {
                        //
                    }
                }
            });
        }
        catch (Exception e) {
            //
        }
    }

    public void forceUpdate() {
        setSongName(Player.getInstance().getSongName());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        seekerTouching = false;
        timeUpdater = new TimeUpdater();
        timeUpdater.start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.now_playing_frac, container, false);
        Button playBtn = v.findViewById(R.id.play_btn);
        TextView playing_song_name = v.findViewById(R.id.playing_song_name);
        playing_song_name.setText(Player.getInstance().getSongName());

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Player.getInstance().isPlaying()) {
                    Player.getInstance().pause();
                }
                else {
                    Player.getInstance().start();
                    timeUpdater.start();
                }
            }
        });

        SeekBar seekBar = v.findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Player.getInstance().seekTo(seekBar.getProgress() * Player.getInstance().getCurrentSongDuration() / 100);
                seekerTouching = false;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekerTouching = true;
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int arg1, boolean fromUser) {
                seekBar.setProgress(arg1);
            }
        });

        return v;
    }


}
