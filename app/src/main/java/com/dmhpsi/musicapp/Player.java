package com.dmhpsi.musicapp;

import android.media.MediaPlayer;

class Player {
    private static final Player ourInstance = new Player();

    static Player getInstance() {
        return ourInstance;
    }

    private MediaPlayer mediaPlayer;
    private String songName;
    private int currentSongDuration, bufferProgress;

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

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public void pause() {
        mediaPlayer.pause();
    }

    public void start() {
        mediaPlayer.start();
    }

    public void playAudio(String url) throws Exception
    {
        killMediaPlayer();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setDataSource(url);
        mediaPlayer.prepareAsync();
        NowPlayingFrac.getInstance().setLoadingProgress(0);
        NowPlayingFrac.getInstance().forceUpdate();

        mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
                bufferProgress = i;
                NowPlayingFrac.getInstance().setLoadingProgress(bufferProgress);
            }
        });
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                currentSongDuration = mp.getDuration();
                NowPlayingFrac.getInstance().timeUpdater.start();
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.seekTo(0);
            }
        });
    }

    private void killMediaPlayer() {
        if(mediaPlayer!=null) {
            try {
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Player() {
    }

    public void seekTo(int msec) {
        if (msec * 100.0 / currentSongDuration < bufferProgress)
            mediaPlayer.seekTo(msec);
    }
}
