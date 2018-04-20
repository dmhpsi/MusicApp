package com.dmhpsi.musicapp;

enum PlayerStates {
    PLAYING, PAUSED, STOPPED
}

enum RepeatStates {
    REPEAT_ALL, REPEAT_ONE, REPEAT_NONE
}

enum ShuffleStates {
    SHUFFLE_ON, SHUFFLE_OFF
}

public class Constants {
    public interface URL {
        static final String GET_INFO = "http://darkha.pythonanywhere.com/getinfo/";
        static final String GET_MP3 = "http://darkha.pythonanywhere.com/getmp3/?id=";
    }

    public interface PLAYER {
        static final String START_SERVICE = "Start music player service";
        static final String STOP_SERVICE = "Stop music player service";
        static final String PLAY_INTENT = "Play intent";
        static final String RESUME_APP = "Resume app";
    }

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }
}
