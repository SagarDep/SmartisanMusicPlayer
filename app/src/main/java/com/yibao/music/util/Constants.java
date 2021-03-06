package com.yibao.music.util;

import android.os.Environment;

/**
 * 作者：Stran on 2017/3/23 15:26
 * 描述：${常量类}
 * 邮箱：strangermy@outlook.com
 *
 * @author Stran
 */
public class Constants {

    public static final int NUMBER_ZOER = 0;
    public static final int NUMBER_ONE = 1;
    public static final int NUMBER_TWO = 2;
    public static final int NUMBER_THRRE = 3;
    public static final int NUMBER_FOUR = 4;
    public static final int NUMBER_FIEV = 5;
    public static final int NUMBER_EIGHT = 8;
    public static final int NUMBER_NINE = 9;
    public static final int NUMBER_TEN = 10;
    public static final char LETTER_A = 'A';
    public static final char LETTER_Z = 'Z';
    public static final char LETTER_HASH = '#';
    static final int MODE_KEY = 0;
    static final String MUSIC_LOAD = "music_load";

    static final String MUSIC_LOAD_FLAG = "play_load_flag";

    static final String MUSIC_MODE = "music_mode";
    static final String PLAY_MODE_KEY = "play_mode";

    static final String DETAIL_FLAG = "detail_flag";
    static final String DETAIL_FLAG_KEY = "detail_flag_key";

    static final String MUSIC_DATA_FLAG = "music_data_flag";
    static final String MUSIC_DATA_LIST_FLAG = "music_data_list_flag";

    static final String MUSIC_POSITION = "music_position";
    static final String MUSIC_ITEM_POSITION = "music_item_position";

    static final String MUSIC_PLAY_STATE = "music_play_state";
    static final String MUSIC_PLAY_STATE_KEY = "music_play_state_key";

    static final String MUSIC_CONFIG = "music_config";
    static final String MUSIC_REMENBER_FLAG = "music_remenber_flag";
    public static final String FRAGMENT_PLAYLIST = "PlayListFragment";
    public static final String FRAGMENT_ARTIST = "ArtistanListFragment";
    public static final String FRAGMENT_ALBUM = "AlbumFragment";

    public static final String FAVORITE_FILE = Environment.getExternalStorageDirectory().getAbsolutePath() + "/smartisan/music/favorite.txt/";
}
