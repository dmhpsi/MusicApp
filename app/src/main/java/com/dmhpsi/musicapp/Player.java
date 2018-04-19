package com.dmhpsi.musicapp;

import android.media.MediaPlayer;

enum PlayerStates {
    PLAYING, PAUSED, STOPPED
}
enum RepeatStates {
    REPEAT_ALL, REPEAT_ONE, REPEAT_NONE
}
enum ShuffleStates {
    SHUFFLE_ON, SHUFFLE_OFF
}

interface OnBufferUpdateEventListener {
    void onEvent();
}

interface OnSongChangeEventListener {
    void onEvent();
}

interface OnStateChangeEventListener {
    void onEvent(PlayerStates state);
}

class Player {
    private static Player instance;

    static Player getInstance() {
        if (instance == null) {
            instance = new Player();
        }
        return instance;
    }

    private Player() {
        repeatState = RepeatStates.REPEAT_NONE;
        shuffleState = ShuffleStates.SHUFFLE_OFF;
    }

    private MediaPlayer mediaPlayer;
    private String songName;
    private int currentSongDuration, bufferProgress;
    private RepeatStates repeatState;
    private ShuffleStates shuffleState;

    private OnBufferUpdateEventListener onBufferUpdateEventListener;
    public void setOnBufferUpdateEventListener(OnBufferUpdateEventListener eventListener) {
        onBufferUpdateEventListener = eventListener;
    }

    private OnSongChangeEventListener onSongChangeEventListener;
    public void setOnSongChangeEventListener(OnSongChangeEventListener eventListener) {
        onSongChangeEventListener = eventListener;
    }

    private OnStateChangeEventListener onStateChangeEventListener;
    public void setOnStateChangeEventListener(OnStateChangeEventListener eventListener) {
        onStateChangeEventListener = eventListener;
    }

    public void setRepeatState(RepeatStates repeatState) {
        this.repeatState = repeatState;
    }

    public RepeatStates getRepeatState() {
        return repeatState;
    }

    public void setShuffleState(ShuffleStates shuffleState) {
        this.shuffleState = shuffleState;
    }

    public ShuffleStates getShuffleState() {
        return shuffleState;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public int getCurrentSongDuration() {
        return currentSongDuration;
    }

    public int getCurrentPosition() {
        try {
            return mediaPlayer.getCurrentPosition();
        }
        catch (Exception e) {
            return 0;
        }
    }

    public int getBufferProgress() {
        return bufferProgress;
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public void pause() {
        mediaPlayer.pause();
        onStateChangeEventListener.onEvent(PlayerStates.PAUSED);
    }

    public void start() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
            onStateChangeEventListener.onEvent(PlayerStates.PLAYING);
        }
    }

    public void playAudio(String url) throws Exception
    {
        killMediaPlayer();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setDataSource(url);
        bufferProgress = 0;
        onSongChangeEventListener.onEvent();
        onBufferUpdateEventListener.onEvent();
        mediaPlayer.prepareAsync();

        mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
                bufferProgress = i;
                onBufferUpdateEventListener.onEvent();
            }
        });
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                currentSongDuration = mp.getDuration();
                onStateChangeEventListener.onEvent(PlayerStates.PLAYING);
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (repeatState == RepeatStates.REPEAT_NONE) {
                    onStateChangeEventListener.onEvent(PlayerStates.STOPPED);
                    mp.seekTo(0);
                } else if (repeatState == RepeatStates.REPEAT_ONE) {
                    onStateChangeEventListener.onEvent(PlayerStates.PLAYING);
                    mp.seekTo(0);
                    mp.start();
                } /*else {
                    onStateChangeEventListener.onEvent(PlayerStates.PLAYING);
                    *** next in playlist ***
                }*/
            }
        });
    }

    private void killMediaPlayer() {
        if(mediaPlayer != null) {
            onStateChangeEventListener.onEvent(PlayerStates.STOPPED);
            try {
                mediaPlayer.start();
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void seekTo(int msec) {
        if (msec * 100.0 / currentSongDuration < bufferProgress) {
            mediaPlayer.seekTo(msec);
        }
    }
}
