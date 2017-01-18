package com.ifthenelse.ejmoore2.agenda;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by edward on 1/17/17.
 */

public class ConfigManager {

    private Context context;
    private int widgetId;

    public ConfigManager(Context context, int widgetId) {
        this.context = context;
        this.widgetId = widgetId;
    }

    public long getLong(int keyId, long defaultValue) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String keyString = generateKeyString(keyId);

        return prefs.getLong(keyString, defaultValue);
    }

    public boolean setLong(int keyId, long value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String keyString = generateKeyString(keyId);

        // Return true if this is a new key, or the value differs from the current value.
        boolean wasChanged = true;
        if (prefs.contains(keyString)) {
            wasChanged = prefs.getLong(keyString, 0) != value;
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(keyString, value);
        editor.apply();
        return wasChanged;
    }

    private String generateKeyString(int keyId) {
        String configKey = context.getString(keyId);
        return configKey + "-" + widgetId;
    }
}
