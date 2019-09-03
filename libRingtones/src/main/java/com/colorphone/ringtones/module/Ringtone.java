package com.colorphone.ringtones.module;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.colorphone.ringtones.RingtoneConfig;
import com.colorphone.ringtones.bean.RingtoneBean;
import com.colorphone.ringtones.download2.Downloader;

import java.io.Serializable;

public class Ringtone implements Serializable {
    private String title;
    private String imgUrl;
    private String singer;
    private String playTimes;
    private String ringtoneId;
    private String ringtoneUrl;

    private String filePath;

    private int durationSeconds;


    private boolean isPlaying = false;
    private String mColumnSource;

    public static Ringtone valueOf(RingtoneBean bean) {
        Ringtone ringtone = new Ringtone();
        ringtone.title = bean.getTitle();
        ringtone.imgUrl = bean.getImgurl();
        ringtone.singer = bean.getSinger();
        ringtone.playTimes = bean.getListencount();
        ringtone.ringtoneId = bean.getId();
        ringtone.ringtoneUrl = bean.getAudiourl();
        ringtone.durationSeconds = formatToInteger(bean.getDuration());
        return ringtone;
    }

    private static int formatToInteger(String duration) {
        if (TextUtils.isDigitsOnly(duration)) {
            return Integer.valueOf(duration);
        }
        return 0;
    }

    public static String generateFilePath(Ringtone ringtone) {
        return Downloader.getDownloadPath(
                RingtoneConfig.getInstance().getRingtoneFileDir() ,ringtone.ringtoneUrl);
    }

    public String getTitle() {
        return title;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getSinger() {
        return singer;
    }

    public String getPlayTimes() {
        return playTimes;
    }

    public String getRingtoneId() {
        return ringtoneId;
    }

    public String getRingtoneUrl() {
        return ringtoneUrl;
    }

    public String getFilePath() {
        if (filePath == null) {
            filePath = generateFilePath(this);
        }
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    @NonNull
    @Override
    public String toString() {
        return getClass().getSimpleName() + ": Name=" + title + ", Url="+ getRingtoneUrl();
    }

    public String getColumnSource() {
        return mColumnSource;
    }

    public void setColumnSource(String columnSource) {
        mColumnSource = columnSource;
    }
}
