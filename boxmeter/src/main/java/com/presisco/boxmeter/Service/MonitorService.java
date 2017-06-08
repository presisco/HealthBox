package com.presisco.boxmeter.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.presisco.shared.service.BaseMonitorService;

/**
 * Created by presisco on 2017/5/9.
 */

public class MonitorService extends BaseMonitorService implements SharedPreferences.OnSharedPreferenceChangeListener {
    private boolean use_vibrator;
    private boolean use_ringtone;
    private Uri uri_ringtone;
    private String emergency_number;

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(this);
        use_vibrator = preferences.getBoolean("preference_use_vibration", true);
        use_ringtone = preferences.getBoolean("preference_use_sound", true);
        if (use_ringtone) {
            uri_ringtone = Uri.parse(preferences.getString("preference_ringtone",
                    RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE)
                            .toString()));
        }
        emergency_number = preferences.getString("preference_emergency_number", "120");
        getLocalBroadcastManager().registerReceiver(new SignsReceiver(), new IntentFilter(HubService.ACTION_SPO2H));
    }

    /**
     * Called when a shared preference is changed, added, or removed. This
     * may be called even if a preference is set to its existing value.
     * <p>
     * <p>This callback will be run on your main thread.
     *
     * @param sharedPreferences The {@link SharedPreferences} that received
     *                          the change.
     * @param key               The key of the preference that was changed, added, or
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case "preference_use_vibration":
                use_vibrator = sharedPreferences.getBoolean(key, true);
                break;
            case "preference_use_sound":
                use_ringtone = sharedPreferences.getBoolean(key, true);
                break;
            case "preference_ringtone":
                uri_ringtone = Uri.parse(sharedPreferences.getString(key,
                        RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE)
                                .toString()));
            case "preference_emergency_number":
                emergency_number = sharedPreferences.getString("preference_emergency_number", "120");
                break;
        }
    }

    private class SignsReceiver extends BroadcastReceiver {
        private static final int EMERGENCY_THRESHOLD = 5;
        private int emergency_counter = 0;

        @Override
        public void onReceive(Context context, Intent intent) {
            int spo2h = intent.getIntExtra(HubService.KEY_DATA, 90);
            if (spo2h < 40) {
                emergency_counter++;
            }
            if (emergency_counter > EMERGENCY_THRESHOLD) {
                if (use_vibrator) {
                    vibrate(-1, 750, 500, 750, 500);
                }
                if (use_ringtone) {
                    scream(uri_ringtone, 3000);
                }
                call(emergency_number);
                emergency_counter = 0;
            }
        }
    }
}
