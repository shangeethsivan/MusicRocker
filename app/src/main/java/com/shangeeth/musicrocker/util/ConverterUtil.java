package com.shangeeth.musicrocker.util;

import android.content.Intent;
import android.util.Log;

/**
 * Created by Shangeeth Sivan on 06/05/17.
 */

public class ConverterUtil {

    private static final String TAG = "ConverterUtil";

    public static String convertToString(int pSongDurationInMs) {
        pSongDurationInMs = convertToSeconds(pSongDurationInMs);

        int lSeconds = pSongDurationInMs % 60;
        int lMinutes = pSongDurationInMs / 60;

        return String.format("%02d:%02d", lMinutes, lSeconds);
    }

    public static int convertToSeconds(int pDurationInMs){
        return (int)Math.round(pDurationInMs/1000.0);
    }
}
