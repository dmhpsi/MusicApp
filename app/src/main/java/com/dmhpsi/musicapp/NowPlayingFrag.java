package com.dmhpsi.musicapp;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

public class NowPlayingFrag extends Fragment {
    class Timer {
        private boolean started;
        private Context context;
        Timer(Context context) {
             this.context = context;
         }

        void start() {
            if (!started) {
                started = true;
                Runnable runnable = new Runnable() {
                    public void run() {
                    while (true) {
                        updateTime(context);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            break;
                        }
                        if (!started) {
                            break;
                        }
                    }
                    }
                };
                new Thread(runnable).start();
            }
        }

        void stop() {
            started = false;
        }
    }

    private boolean seekerTouching;
    Timer timer;
    Player player;

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void updateSongName(final Context context) {
        try {
            ((Activity)context).runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        TextView playing_song_name = ((Activity)context).findViewById(R.id.playing_song_name);
                        playing_song_name.setText(player.getSongName());
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateTime(final Context context) {
        try {
            ((Activity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        TextView time = ((Activity)context).findViewById(R.id.time);
                        int newTime = player.getCurrentPosition();
                        int t = Math.round(newTime / 1000f);
                        String x = String.format("%02d:%02d", t / 60, t % 60);
                        time.setText(x);
                        if (!seekerTouching) {
                            SeekBar seekBar = ((Activity)context).findViewById(R.id.seek_bar);
                            seekBar.setProgress(Math.round(newTime * 100.0f / player.getCurrentSongDuration()));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateBufferProgress(final Context context) {
        try {
            ((Activity)context).runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        SeekBar seekBar = ((Activity)context).findViewById(R.id.seek_bar);
                        seekBar.setSecondaryProgress(player.getBufferProgress());
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

    private void setBtn(ImageButton btn, @DrawableRes int drawableId, @ColorRes int colorId) {
        if (drawableId != -1)
            btn.setImageResource(drawableId);
        if (colorId != -1)
            btn.setColorFilter(ContextCompat.getColor(getContext(), colorId));
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        timer = new Timer(context);
        timer.start();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        timer.stop();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.now_playing_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        updateSongName(getContext());
        player.setOnBufferUpdateEventListener(new OnBufferUpdateEventListener() {
            @Override
            public void onEvent() {
                updateBufferProgress(getContext());
            }
        });

        player.setOnSongChangeEventListener(new OnSongChangeEventListener() {
            @Override
            public void onEvent() {
                updateSongName(getContext());
            }
        });

        final ImageButton playBtn = view.findViewById(R.id.play_btn);
        TextView playing_song_name = view.findViewById(R.id.playing_song_name);
        playing_song_name.setText(player.getSongName());

        seekerTouching = false;
        timer.start();

        player.setOnStateChangeEventListener(new OnStateChangeEventListener() {
            @Override
            public void onEvent(PlayerStates state) {
                if (state == PlayerStates.PLAYING) {
                    setBtn(playBtn, R.drawable.ma_ic_pause, -1);
                } else {
                    setBtn(playBtn, R.drawable.ma_ic_play, -1);
                }
            }
        });

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (player.isPlaying()) {
                    player.pause();
                }
                else {
                    player.start();
                }
            }
        });

        if (player.isPlaying()) {
            setBtn(playBtn, R.drawable.ma_ic_pause, -1);
        } else {
            setBtn(playBtn, R.drawable.ma_ic_play, -1);
        }

        ImageButton shuffleBtn, repeatBtn;

        repeatBtn = view.findViewById(R.id.repeat_btn);

        RepeatStates repeatState = player.getRepeatState();
        if (repeatState == RepeatStates.REPEAT_NONE) {
            setBtn(repeatBtn, R.drawable.ma_ic_repeat, R.color.colorAccent);
        } else if (repeatState == RepeatStates.REPEAT_ALL) {
            setBtn(repeatBtn, R.drawable.ma_ic_repeat, R.color.colorPrimary);
        } else {
            setBtn(repeatBtn, R.drawable.ma_ic_repeat_one, R.color.colorPrimary);
        }
        repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RepeatStates repeatState = player.getRepeatState();
                ImageButton btn = (ImageButton)view;
                if (repeatState == RepeatStates.REPEAT_NONE) {
                    player.setRepeatState(RepeatStates.REPEAT_ALL);
                    setBtn(btn, R.drawable.ma_ic_repeat, R.color.colorPrimary);
                } else if (repeatState == RepeatStates.REPEAT_ALL) {
                    player.setRepeatState(RepeatStates.REPEAT_ONE);
                    setBtn(btn, R.drawable.ma_ic_repeat_one, R.color.colorPrimary);
                } else {
                    player.setRepeatState(RepeatStates.REPEAT_NONE);
                    setBtn(btn, R.drawable.ma_ic_repeat, R.color.colorAccent);
                }
            }
        });

        shuffleBtn = view.findViewById(R.id.shuffle_btn);
        ShuffleStates shuffleState = player.getShuffleState();
        if (shuffleState == ShuffleStates.SHUFFLE_ON) {
            setBtn(shuffleBtn, -1, R.color.colorPrimary);
        } else {
            setBtn(shuffleBtn, -1, R.color.colorAccent);
        }
        shuffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShuffleStates shuffleState = player.getShuffleState();
                ImageButton btn = (ImageButton)view;
                if (shuffleState == ShuffleStates.SHUFFLE_ON) {
                    player.setShuffleState(ShuffleStates.SHUFFLE_OFF);
                    setBtn(btn, -1, R.color.colorAccent);
                } else {
                    player.setShuffleState(ShuffleStates.SHUFFLE_ON);
                    setBtn(btn, -1, R.color.colorPrimary);
                }
            }
        });

        SeekBar seekBar = view.findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                player.seekTo(seekBar.getProgress() * player.getCurrentSongDuration() / 100);
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
    }

    @Override
    public void onPause() {
        super.onPause();
        timer.stop();
    }

    @Override
    public void onResume() {
        super.onResume();
        timer.start();
    }
}
