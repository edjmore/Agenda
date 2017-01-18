package com.ifthenelse.ejmoore2.agenda;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Map;

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

    public boolean getBoolean(int keyId, boolean defaultValue) {
        long defaultLongValue = defaultValue ? 1 : 0;
        long actualLongValue = getLong(keyId, defaultLongValue);

        return actualLongValue != 0;
    }

    public long getLong(int keyId, long defaultValue) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String keyString = generateKeyString(keyId);

        return prefs.getLong(keyString, defaultValue);
    }

    public boolean setBoolean(int keyId, boolean value) {
        return setLong(keyId, value ? 1 : 0);
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

    public static void removeAllConfigsForWidget(Context context, int widgetId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        Map<String, ?> configs = prefs.getAll();

        /* Remove any keys where the widget ID matches our widget ID. */
        for (String key : configs.keySet()) {
            String[] tokens = key.split("-");

            if (tokens.length > 0) {
                int keyWidgetId = Integer.parseInt(tokens[tokens.length - 1]);
                if (keyWidgetId == widgetId) {
                    editor.remove(key);
                }
            }
        }

        editor.apply();
    }

    private String generateKeyString(int keyId) {
        String configKey = context.getString(keyId);
        return configKey + "-" + widgetId;
    }
}
