package com.presisco.shared.service;

import android.app.Service;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public abstract class BaseMonitorService extends Service {
    private LocalBroadcastManager mBroadcastManager;
    private Vibrator mVibrator;
    private RingtoneManager mRingtoneManager;
    private boolean is_sending_alarm = false;
    private Executor executor = Executors.newSingleThreadExecutor();

    public BaseMonitorService() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mBroadcastManager = LocalBroadcastManager.getInstance(this);
        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        mRingtoneManager = new RingtoneManager(this);
    }

    protected LocalBroadcastManager getLocalBroadcastManager() {
        return mBroadcastManager;
    }

    protected boolean isSendingAlarm() {
        return is_sending_alarm;
    }

    protected void vibrate(int repeat, long... pattern) {
        mVibrator.vibrate(pattern, repeat);
    }

    protected void scream(Uri uri_ringtone, int duration) {
        if (!is_sending_alarm) {
            Ringtone ringtone = mRingtoneManager.getRingtone(mRingtoneManager.getRingtonePosition(uri_ringtone));
            ringtone.play();
            new Timer().executeOnExecutor(executor, duration);
            is_sending_alarm = true;
        }
    }

    protected void stopScream() {
        if (is_sending_alarm) {
            mRingtoneManager.stopPreviousRingtone();
        }
    }

    protected void call(String number) {
        Intent call_intent = new Intent(Intent.ACTION_CALL);
        call_intent.setData(Uri.parse("tel:" + number));
        try {
            startActivity(call_intent);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {

    }

    private class Timer extends AsyncTask<Integer, Void, Void> {
        @Override
        protected void onPostExecute(Void aVoid) {
            mRingtoneManager.stopPreviousRingtone();
            is_sending_alarm = false;
        }

        @Override
        protected Void doInBackground(Integer... params) {
            try {
                Thread.sleep(params[0]);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
