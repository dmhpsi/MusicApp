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

enum ListPurpose {
    ALL_SONG, PLAYLIST, PLAYLIST_SONG
}

public class Constants {
    public interface URL {
        String HOST = "darkha.pythonanywhere.com";
        String GET_INFO = "http://" + HOST + "/getinfo/";
        String GET_MP3 = "http://" + HOST + "/getmp3/?id=";
    }

    public interface PLAYER {
        String NOT_IN_PLAYLIST = "notpl";
        String START_SERVICE = "Start music player service";
        String STOP_SERVICE = "Stop music player service";
        String PLAY_INTENT = "Play intent";
        String RESUME_APP = "Resume app";
    }

    public interface NOTIFICATION_ID {
        int FOREGROUND_SERVICE = 101;
    }

    public interface ACTIVITY_MESSAGE {
        String PLAYLIST_TO_EDIT = "pl to edit";
    }
}
