package com.shangeeth.musicrocker.util;

import android.content.Intent;
import android.util.Log;

/**
 * Created by Shangeeth Sivan on 06/05/17.
 */

public class ConverterUtil {

    private static final String TAG = "ConverterUtil";

    /**
     * Converts the given duration in ms to String format of min (eg: 01:07) for showing in the media player
     *
     * @param pSongDurationInMs
     * @return
     */
    public static String convertToString(int pSongDurationInMs) {
        pSongDurationInMs = (int) Math.round(pSongDurationInMs / 1000.0);

        int lSeconds = pSongDurationInMs % 60;
        int lMinutes = pSongDurationInMs / 60;

        return String.format("%02d:%02d", lMinutes, lSeconds);
    }

}
