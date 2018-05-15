package com.dmhpsi.musicapp;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.util.Objects;

interface OnBufferUpdateEventListener {
    void onEvent();
}

interface OnSongChangeEventListener {
    void onEvent();
}

interface OnStateChangeEventListener {
    void onEvent(PlayerStates state);
}

interface OnLoadingStateChangeListener {
    void onEvent(boolean loaded);
}

public class Player extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private Binder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        Player getService() {
            return Player.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Objects.equals(intent.getAction(), Constants.PLAYER.START_SERVICE)) {
            Intent notificationIntent = new Intent(this, main.class);
            notificationIntent.setAction(Constants.PLAYER.START_SERVICE);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0);
//
//            Intent previousIntent = new Intent(this, Player.class);
//            previousIntent.setAction(Constants.ACTION.PREV_ACTION);
//            PendingIntent ppreviousIntent = PendingIntent.getService(this, 0,
//                    previousIntent, 0);
//
            Intent playIntent = new Intent(this, Player.class);
            playIntent.setAction(Constants.PLAYER.PLAY_INTENT);
            PendingIntent pplayIntent = PendingIntent.getService(this, 0,
                    playIntent, 0);


            Intent stopIntent = new Intent(this, Player.class);
            stopIntent.setAction(Constants.PLAYER.STOP_SERVICE);
            PendingIntent pstopIntent = PendingIntent.getService(this, 0,
                    stopIntent, 0);

//
//            Intent nextIntent = new Intent(this, ForegroundService.class);
//            nextIntent.setAction(Constants.ACTION.NEXT_ACTION);
//            PendingIntent pnextIntent = PendingIntent.getService(this, 0,
//                    nextIntent, 0);

//            Bitmap icon = BitmapFactory.decodeResource(getResources(),
//                    R.drawable.ma_ic_more_im);

            Notification notification = new Notification.Builder(this)
                    .setContentTitle(currentSong.songName)
                    .setTicker(currentSong.songName)
                    .setContentText(currentSong.artist)
                    .setSmallIcon(R.drawable.ma_ic_noti_im)
//                    .setLargeIcon(
//                            Bitmap.createScaledBitmap(icon, 128, 128, false))
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
//                    .addAction(android.R.drawable.ic_media_previous,
//                            "Previous", ppreviousIntent)
                    .addAction(R.drawable.ma_ic_play_im, "Play",
                            pplayIntent)
                    .addAction(R.drawable.ma_ic_stop_im, "Stop",
                            pstopIntent)
//                    .addAction(android.R.drawable.ic_media_next, "Next",
//                            pnextIntent).build();
                    .build();
            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                    notification);
        } else if (Objects.equals(intent.getAction(), Constants.PLAYER.PLAY_INTENT)) {
            if (isPlaying()) {
                pause();
            } else {
                start();
            }
        }/* else if (intent.getAction().equals(Constants.ACTION.PLAY_ACTION)) {
            Log.i(LOG_TAG, "Clicked Play");
        } else if (intent.getAction().equals(Constants.ACTION.NEXT_ACTION)) {
            Log.i(LOG_TAG, "Clicked Next");
        } */ else if (Objects.equals(intent.getAction(), Constants.PLAYER.STOP_SERVICE)) {
            Log.e("Servicce", "killed");
            stop();
            stopForeground(true);
            stopSelf();
        } else {
            Log.e("service", intent.getAction());
        }
        return START_STICKY;
    }

    private MediaPlayer mediaPlayer;
    private SongItem currentSong;
    private int currentSongDuration, bufferProgress;
    private RepeatStates repeatState;
    private ShuffleStates shuffleState;

    private OnBufferUpdateEventListener onBufferUpdateEventListener;
    public void setOnBufferUpdateEventListener(OnBufferUpdateEventListener eventListener) {
        onBufferUpdateEventListener = eventListener;
    }

    @Nullable
    private OnSongChangeEventListener onSongChangeEventListener;
    public void setOnSongChangeEventListener(@Nullable OnSongChangeEventListener eventListener) {
        onSongChangeEventListener = eventListener;
    }

    private OnStateChangeEventListener onStateChangeEventListener;
    public void setOnStateChangeEventListener(OnStateChangeEventListener eventListener) {
        onStateChangeEventListener = eventListener;
    }

    private interface OnNextPrevEventListener {
        void onEvent(boolean isNext);
    }

    private OnNextPrevEventListener onNextPrevEventListener;
    private void setOnNextPrevEventListener(OnNextPrevEventListener onNextPrevEventListener) {
        this.onNextPrevEventListener = onNextPrevEventListener;
    }

    private OnLoadingStateChangeListener onLoadingStateChangeListener;

    public void setOnLoadingStateChangeListener(OnLoadingStateChangeListener onLoadingStateChangeListener) {
        this.onLoadingStateChangeListener = onLoadingStateChangeListener;
    }

    public void setRepeatState(RepeatStates repeatState) {
        this.repeatState = repeatState;
        PlaylistManager.getInstance(getApplicationContext())
                .setRepeatState(repeatState, getApplicationContext());
    }

    public RepeatStates getRepeatState() {
        return repeatState;
    }

    public void setShuffleState(ShuffleStates shuffleState) {
        this.shuffleState = shuffleState;
        PlaylistManager.getInstance(getApplicationContext())
                .setShuffleState(shuffleState, getApplicationContext());
    }

    public ShuffleStates getShuffleState() {
        return shuffleState;
    }

    public String getSongName() {
        return currentSong.songName;
    }

    public int getCurrentSongDuration() {
        return currentSongDuration;
    }

    public SongItem getCurrentSong() {
        return currentSong;
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

    public boolean start() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
            onStateChangeEventListener.onEvent(PlayerStates.PLAYING);
            return true;
        }
        return false;
    }

    public void stop() {
        mediaPlayer.pause();
        mediaPlayer.seekTo(0);
        onStateChangeEventListener.onEvent(PlayerStates.STOPPED);
    }

    private void playAudio(SongItem song) {
        onLoadingStateChangeListener.onEvent(false);
        killMediaPlayer();
        currentSong = new SongItem(song);
        if (mediaPlayer == null)
            mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(Constants.URL.GET_MP3 + song.id);
        } catch (IOException e) {
            e.printStackTrace();
        }
        bufferProgress = 0;
        if (onSongChangeEventListener != null) {
            onSongChangeEventListener.onEvent();
        }
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
                onLoadingStateChangeListener.onEvent(true);
            }
        });
        PlaylistManager.getInstance(getApplicationContext()).setLastSong(song, getApplicationContext());
    }

    private void killMediaPlayer() {
        if (mediaPlayer != null) {
            onStateChangeEventListener.onEvent(PlayerStates.STOPPED);
            try {
//                mediaPlayer.start();
                mediaPlayer.reset();
//                mediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void playPlaylist(@NonNull Playlist playlist, String startId) {
        PlaylistManager pm = PlaylistManager.getInstance(getApplicationContext());
        pm.defineLastPlaylist(new Playlist(playlist), getApplicationContext());
        playAudio(pm.getPrevSongItem(pm.getNextSongItem(startId, false).id, false));
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (repeatState == RepeatStates.REPEAT_ONE) {
                    onStateChangeEventListener.onEvent(PlayerStates.PLAYING);
                    mp.seekTo(0);
                    mp.start();
                } else if (repeatState == RepeatStates.REPEAT_NONE) {
                    onStateChangeEventListener.onEvent(PlayerStates.STOPPED);
                    mp.seekTo(0);
                } else {
                    onStateChangeEventListener.onEvent(PlayerStates.PLAYING);
                    playAudio(PlaylistManager.getInstance(getApplicationContext())
                            .getNextSongItem(currentSong.id, shuffleState == ShuffleStates.SHUFFLE_ON));
                }
            }
        });
        setOnNextPrevEventListener(new OnNextPrevEventListener() {
            @Override
            public void onEvent(boolean isNext) {
                if (repeatState == RepeatStates.REPEAT_ONE) {
                    onStateChangeEventListener.onEvent(PlayerStates.PLAYING);
                    mediaPlayer.seekTo(0);
                    mediaPlayer.start();
                    return;
                }
                if (isNext) {
                    onStateChangeEventListener.onEvent(PlayerStates.PLAYING);
                    playAudio(PlaylistManager.getInstance(getApplicationContext())
                            .getNextSongItem(currentSong.id, shuffleState == ShuffleStates.SHUFFLE_ON));
                } else {
                    onStateChangeEventListener.onEvent(PlayerStates.PLAYING);
                    playAudio(PlaylistManager.getInstance(getApplicationContext())
                            .getPrevSongItem(currentSong.id, shuffleState == ShuffleStates.SHUFFLE_ON));
                }
            }
        });
    }

    public void playSong(@NonNull final SongItem song) {
        Playlist pl = new Playlist("", song);
        PlaylistManager.getInstance(getApplicationContext())
                .defineLastPlaylist(pl, getApplicationContext());
        playAudio(song);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (repeatState == RepeatStates.REPEAT_NONE || repeatState == RepeatStates.REPEAT_ALL) {
                    onStateChangeEventListener.onEvent(PlayerStates.STOPPED);
                    mp.seekTo(0);
                } else {
                    onStateChangeEventListener.onEvent(PlayerStates.PLAYING);
                    PlaylistManager.getInstance(getApplicationContext())
                            .defineLastPlaylist(null, getApplicationContext());
                    playAudio(song);
                }
            }
        });

        setOnNextPrevEventListener(new OnNextPrevEventListener() {
            @Override
            public void onEvent(boolean isNext) {
                mediaPlayer.seekTo(0);
            }
        });
    }

    public int next() {
        if (onNextPrevEventListener != null) {
            onNextPrevEventListener.onEvent(true);
            return 1;
        } else {
            return -1;
        }
    }

    public int prev() {
        if (onNextPrevEventListener != null) {
            onNextPrevEventListener.onEvent(false);
            return 1;
        } else {
            return -1;
        }
    }

    public void seekTo(int msec) {
        if (msec * 100.0 / currentSongDuration < bufferProgress) {
            mediaPlayer.seekTo(msec);
        }
    }
}
