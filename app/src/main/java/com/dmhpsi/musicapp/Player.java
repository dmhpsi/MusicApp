package com.dmhpsi.musicapp;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

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

public class Player extends Service {
    public Player() {
        repeatState = RepeatStates.REPEAT_NONE;
        shuffleState = ShuffleStates.SHUFFLE_OFF;
    }

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
//
//            Intent nextIntent = new Intent(this, ForegroundService.class);
//            nextIntent.setAction(Constants.ACTION.NEXT_ACTION);
//            PendingIntent pnextIntent = PendingIntent.getService(this, 0,
//                    nextIntent, 0);

            Bitmap icon = BitmapFactory.decodeResource(getResources(),
                    R.drawable.ma_ic_more_im);

            Notification notification = new Notification.Builder(this)
                    .setContentTitle(songName)
                    .setTicker(songName)
                    .setContentText(songArtist)
                    .setSmallIcon(R.drawable.ma_ic_more_im)
                    .setLargeIcon(
                            Bitmap.createScaledBitmap(icon, 128, 128, false))
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
//                    .addAction(android.R.drawable.ic_media_previous,
//                            "Previous", ppreviousIntent)
                    .addAction(R.drawable.ma_ic_more_im, "Play",
                            pplayIntent)
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
        } else if (intent.getAction().equals(
                Constants.ACTION.STOPFOREGROUND_ACTION)) {
            Log.i(LOG_TAG, "Received Stop Foreground Intent");
            stopForeground(true);
            stopSelf();
        }*/
        return START_STICKY;
    }

    private MediaPlayer mediaPlayer;
    private String songName, songArtist;
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

    public void setSongArtist(String songArtist) {
        this.songArtist = songArtist;
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
