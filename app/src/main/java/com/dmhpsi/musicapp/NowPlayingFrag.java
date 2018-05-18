package com.dmhpsi.musicapp;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

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
                        updateUI(context);
                        try {
                            Thread.sleep(200);
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
    SongList playlist;
    SongAdapter adapter;

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void updateUI(final Context context) {
        try {
            ((Activity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        TextView time = ((Activity) context).findViewById(R.id.totaltime);
                        int newTime = player.getCurrentSongDuration();
                        int t = Math.round(newTime / 1000f);
                        String x = String.format("%02d:%02d", t / 60, t % 60);
                        time.setText(x);

                        time = ((Activity) context).findViewById(R.id.time);
                        newTime = player.getCurrentPosition();
                        t = Math.round(newTime / 1000f);
                        x = String.format("%02d:%02d", t / 60, t % 60);
                        time.setText(x);
                        if (!seekerTouching) {
                            SeekBar seekBar = ((Activity) context).findViewById(R.id.seek_bar);
                            seekBar.setProgress(Math.round(newTime * 100.0f / player.getCurrentSongDuration()));
                        }
//                        adapter.setSpecialItem(player.getCurrentSong());
//                        adapter.notifyDataSetChanged();
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
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        updateSongName(getContext());
        player.setOnBufferUpdateEventListener(new OnBufferUpdateEventListener() {
            @Override
            public void onEvent() {
                updateBufferProgress(getContext());
            }
        });

        final ImageButton playBtn = view.findViewById(R.id.play_btn);

        seekerTouching = false;
        timer.start();

        playlist = new SongList();
        adapter = new SongAdapter(getActivity(), playlist.getList(), ListPurpose.PLAYLIST_SONG);
        ListView playlistsView = view.findViewById(R.id.curr_pl_view);
        playlistsView.setAdapter(adapter);
        playlistsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                SongItem song = playlist.get(i);
                try {
                    Intent startIntent = new Intent(getContext(), Player.class);
                    startIntent.setAction(Constants.PLAYER.START_SERVICE);
                    getActivity().startService(startIntent);
                    player.playPlaylist(PlaylistManager.getInstance(getContext()).getLastPlaylist(),
                            song.id);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        playlist.clear();

        Playlist lastPl = PlaylistManager.getInstance(getContext()).getLastPlaylist();
        if (lastPl != null) {
            playlist.add(lastPl.getSongs());
        } else {
            playlist.add(PlaylistManager.getInstance(getContext()).getLastSong());
        }
        adapter.setSpecialItem(player.getCurrentSong());
        adapter.notifyDataSetChanged();

        player.setOnSongChangeEventListener(new OnSongChangeEventListener() {
            @Override
            public void onEvent() {
                adapter.setSpecialItem(player.getCurrentSong());
                adapter.notifyDataSetChanged();
                Intent startIntent = new Intent(getContext(), Player.class);
                startIntent.setAction(Constants.PLAYER.START_SERVICE);
                getActivity().startService(startIntent);
            }
        });

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

        player.setOnLoadingStateChangeListener(new OnLoadingStateChangeListener() {
            @Override
            public void onEvent(boolean loaded) {
                view.findViewById(R.id.play_btn).setClickable(loaded);
                view.findViewById(R.id.next_btn).setClickable(loaded);
                view.findViewById(R.id.prev_btn).setClickable(loaded);
            }
        });

        PlaylistManager.getInstance(getContext()).setOnLastPlaylistChangedListener(new OnLastPlaylistChangedListener() {
            @Override
            public void onEvent() {
                Playlist lastPl = PlaylistManager.getInstance(getContext()).getLastPlaylist();
                playlist.clear();
                if (lastPl != null) {
                    playlist.add(lastPl.getSongs());
                } else {
                    playlist.add(player.getCurrentSong());
                }
                adapter.notifyDataSetChanged();
            }
        });

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (player.isPlaying()) {
                    player.pause();
                }
                else {
                    Intent startIntent = new Intent(getContext(), Player.class);
                    if (player.start()) {
                        startIntent.setAction(Constants.PLAYER.START_SERVICE);
                        getActivity().startService(startIntent);
                    } else {
                        Playlist pl = PlaylistManager.getInstance(getContext()).getLastPlaylist();
                        if (pl != null) {
                            if (pl.getSongs().size() > 0) {
                                startIntent.setAction(Constants.PLAYER.START_SERVICE);
                                getActivity().startService(startIntent);
                                player.playPlaylist(pl, "");
                            }
                        }
                    }
                }
            }
        });

        view.findViewById(R.id.next_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (player.next() != -1) {
                    Intent startIntent = new Intent(getContext(), Player.class);
                    startIntent.setAction(Constants.PLAYER.START_SERVICE);
                    getActivity().startService(startIntent);
                }
            }
        });

        view.findViewById(R.id.prev_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (player.prev() != -1) {
                    Intent startIntent = new Intent(getContext(), Player.class);
                    startIntent.setAction(Constants.PLAYER.START_SERVICE);
                    getActivity().startService(startIntent);
                }
            }
        });

        view.findViewById(R.id.save_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Save playlist as ...");
                LayoutInflater inflater = getActivity().getLayoutInflater();
                final View dialog = inflater.inflate(R.layout.add_playlist, null);
                builder.setView(dialog)
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                TextView tv = dialog.findViewById(R.id.pl_name);
                                String input = tv.getText().toString();
                                if (!input.matches("")) {
                                    Playlist pl = new Playlist(
                                            PlaylistManager.getInstance(
                                                    getContext()).getLastPlaylist());
                                    pl.setName(input);
                                    if (PlaylistManager.getInstance(getContext()).addPlaylist(
                                            new Playlist(pl), getContext()) == -1) {
                                        Toast.makeText(getContext(),
                                                "Playlist add failed! Duplicated playlist name found",
                                                Toast.LENGTH_LONG)
                                                .show();
                                    } else {
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

        if (player.isPlaying()) {
            setBtn(playBtn, R.drawable.ma_ic_pause, -1);
        } else {
            setBtn(playBtn, R.drawable.ma_ic_play, -1);
        }

        ImageButton shuffleBtn, repeatBtn;

        repeatBtn = view.findViewById(R.id.repeat_btn);

        RepeatStates repeatState = player.getRepeatState();
        if (repeatState == RepeatStates.REPEAT_NONE) {
            setBtn(repeatBtn, R.drawable.ma_ic_repeat, R.color.colorPrimaryLight);
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
                    setBtn(btn, R.drawable.ma_ic_repeat, R.color.colorPrimaryLight);
                }
            }
        });

        shuffleBtn = view.findViewById(R.id.shuffle_btn);
        ShuffleStates shuffleState = player.getShuffleState();
        if (shuffleState == ShuffleStates.SHUFFLE_ON) {
            setBtn(shuffleBtn, -1, R.color.colorPrimary);
        } else {
            setBtn(shuffleBtn, -1, R.color.colorPrimaryLight);
        }
        shuffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShuffleStates shuffleState = player.getShuffleState();
                ImageButton btn = (ImageButton)view;
                if (shuffleState == ShuffleStates.SHUFFLE_ON) {
                    player.setShuffleState(ShuffleStates.SHUFFLE_OFF);
                    setBtn(btn, -1, R.color.colorPrimaryLight);
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
