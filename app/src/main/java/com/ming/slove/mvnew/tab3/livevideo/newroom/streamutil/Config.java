package com.ming.slove.mvnew.tab3.livevideo.newroom.streamutil;

import android.content.pm.ActivityInfo;

import com.ming.slove.mvnew.BuildConfig;
import com.qiniu.pili.droid.streaming.StreamingProfile;

/**
 * 配置信息
 */
public class Config {
    public static final boolean DEBUG_MODE = false;
    public static final boolean FILTER_ENABLED = false;
    public static final int ENCODING_LEVEL = StreamingProfile.VIDEO_ENCODING_HEIGHT_480;
    public static final int SCREEN_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

    public static final String VERSION_HINT = BuildConfig.VERSION_NAME;

    public static final String EXTRA_PUBLISH_URL_PREFIX = "URL:";
    public static final String EXTRA_PUBLISH_JSON_PREFIX = "JSON:";

    public static final String EXTRA_KEY_PUB_URL = "pub_url";
    public static final String EXTRA_KEY_ROOM_ID = "room_id";
    public static final String EXTRA_KEY_SHARE_INFO = "share_info";

    public static final String HINT_ENCODING_ORIENTATION_CHANGED = "直播录制方向改变,已暂停，请手动启动开始录制。";
}
