/*
 * Copyright (C) 2015 The CyanogenMod Project
 *               2017-2018 The LineageOS Project
 *               2020-2022 Paranoid Android
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pixys.glyph.Services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.IBinder;
import android.util.Log;

import com.pixys.glyph.Constants.Constants;
import com.pixys.glyph.Manager.AnimationManager;
import com.pixys.glyph.Manager.SettingsManager;
import com.pixys.glyph.Manager.StatusManager;
import com.pixys.glyph.Sensors.FlipToGlyphSensor;
import com.pixys.glyph.Utils.FileUtils;

public class FlipToGlyphService extends Service {

    private static final String TAG = "FlipToGlyphService";
    private static final boolean DEBUG = true;

    private boolean isFlipped;
    private int ringerMode;

    private AnimationManager mAnimationManager;
    private AudioManager mAudioManager;
    private FlipToGlyphSensor mFlipToGlyphSensor;

    @Override
    public void onCreate() {
        if (DEBUG) Log.d(TAG, "Creating service");

        mFlipToGlyphSensor = new FlipToGlyphSensor(this, this::onFlip);

        mAnimationManager = new AnimationManager(this);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        IntentFilter screenStateFilter = new IntentFilter();
        screenStateFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mScreenStateReceiver, screenStateFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (DEBUG) Log.d(TAG, "Starting service");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (DEBUG) Log.d(TAG, "Destroying service");
        unregisterReceiver(mScreenStateReceiver);
        mFlipToGlyphSensor.disable();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void onFlip(boolean flipped) {
        if (flipped == isFlipped) return;
        if (DEBUG) Log.d(TAG, "Flipped: " + Boolean.toString(flipped));
        if (flipped) {
            mAnimationManager.playCsv("flip");
            ringerMode = mAudioManager.getRingerModeInternal();
            mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_SILENT);
        } else {
            mAudioManager.setRingerModeInternal(ringerMode);
        }
        isFlipped = flipped;
    }

    private BroadcastReceiver mScreenStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                mFlipToGlyphSensor.disable();
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                mFlipToGlyphSensor.enable();
            }
        }
    };
}