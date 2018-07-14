package com.sample.kontaktio;


import android.app.Application;

import com.kontakt.sdk.android.common.KontaktSDK;
import com.kontakt.sdk.android.common.log.LogLevel;

public class BeaconAPP extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        KontaktSDK.initialize(this)
                  .setDebugLoggingEnabled(BuildConfig.DEBUG)
                  .setLogLevelEnabled(LogLevel.DEBUG, true);
    }
}