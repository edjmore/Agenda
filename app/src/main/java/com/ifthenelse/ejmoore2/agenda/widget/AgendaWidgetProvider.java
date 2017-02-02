package com.ifthenelse.ejmoore2.agenda.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.CalendarContract;
import android.widget.RemoteViews;

import com.ifthenelse.ejmoore2.agenda.R;
import com.ifthenelse.ejmoore2.agenda.UpdateService;
import com.ifthenelse.ejmoore2.agenda.model.Instance;
import com.ifthenelse.ejmoore2.agenda.util.ConfigManager;
import com.ifthenelse.ejmoore2.agenda.view.ConfigActivity;

/**
 * Created by ejmoore2 on 1/15/17.
 */

public class AgendaWidgetProvider extends AppWidgetProvider {

    private static final String PACKAGE = "com.droid.mooresoft.agenda.";

    public static final String ACTION_VIEW_DATE = PACKAGE + "ACTION_VIEW_DATE";
    public static final String ACTION_AGENDA_UPDATE = PACKAGE + "ACTION_AGENDA_UPDATE";
    public static final String ACTION_EVENT_CLICK = PACKAGE + "ACTION_EVENT_CLICK";
    public static final String ACTION_EDIT_NEW_EVENT = PACKAGE + "ACTION_EDIT_NEW_EVENT";
    public static final String ACTION_OPEN_CONFIG = PACKAGE + "ACTION_OPEN_CONFIG";

    public static final String EXTRA_ACTION = "extra_action";
    public static final String EXTRA_DATE = "extra_date";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        setupWidgets(context, appWidgetIds);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // Remove any saved user preferences for the given widgets.
        for (int widgetId : appWidgetIds) {
            ConfigManager.removeAllConfigsForWidget(context, widgetId);
        }
        super.onDeleted(context, appWidgetIds);

        // Stop the widget data update service if there are no active widgets left.
        if (!isAnyWidgetActive(context)) {
            Intent updateService = new Intent(context, UpdateService.class);
            context.stopService(updateService);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Intent activityIntent = null;

        // Read intent data and respond accordingly, potentially by starting a new activity.
        if (ACTION_AGENDA_UPDATE.equals(action)) {
            //Log.i("AgendaWidgetProvider", "Update alarm fired: refreshing all widgets");

            refreshAllWidgets(context);

        } else if (ACTION_EVENT_CLICK.equals(intent.getStringExtra(EXTRA_ACTION))) {
            // Open calendar to view event instance.
            activityIntent = buildViewEventIntent(intent);

        } else if (ACTION_VIEW_DATE.equals(action)) {
            // Open calendar to view given date.
            activityIntent = buildViewDateIntent(context, intent);

        } else if (ACTION_EDIT_NEW_EVENT.equals(action)) {
            // Open calendar to edit and insert a new event.
            activityIntent = buildEditNewEventIntent();

        } else if (ACTION_OPEN_CONFIG.equals(action)) {
            // Open the configuration activity to edit widget settings.
            activityIntent = buildOpenConfigIntent(context, intent);

        } else {
            super.onReceive(context, intent);
        }

        if (activityIntent != null) {
            // Try to start a new activity with the given intent.
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (activityIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(activityIntent);
            }
        }
    }

    /* BEGIN helper methods for launching activities. */

    private Intent buildViewDateIntent(Context context, Intent data) {
        long time = data.getLongExtra(EXTRA_DATE, System.currentTimeMillis());

        Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
        builder.appendPath("time");
        ContentUris.appendId(builder, time);

        return new Intent(Intent.ACTION_VIEW)
                .setData(builder.build());
    }

    private Intent buildViewEventIntent(Intent data) {
        String encodedInstance = data.getAction();
        long[] decodedInstance = Instance.decodeInstance(encodedInstance);

        Uri uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, decodedInstance[2]);
        return new Intent(Intent.ACTION_VIEW)
                .setData(uri)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, decodedInstance[0])
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, decodedInstance[1]);
    }

    private Intent buildEditNewEventIntent() {
        return new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI);
    }

    private Intent buildOpenConfigIntent(Context context, Intent data) {
        int widgetId =
                data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                        AppWidgetManager.INVALID_APPWIDGET_ID);

        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            return null;
        } else {
            return new Intent(context, ConfigActivity.class)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        }
    }

    /* END helper methods for launching activities. *
     * BEGIN public utility methods.                */

    /**
     * Notifies the widget manager that the data for each widget may have changed.
     */
    public static void refreshAllWidgets(Context context) {
        int[] widgetIds = getAllWidgetIds(context);
        AppWidgetManager.getInstance(context)
                .notifyAppWidgetViewDataChanged(widgetIds, R.id.listview_days);
    }

    /**
     * Returns an array of all active agenda widget IDs.
     */
    public static int[] getAllWidgetIds(Context context) {
        ComponentName widgetName = new ComponentName(context, AgendaWidgetProvider.class);
        return AppWidgetManager.getInstance(context)
                .getAppWidgetIds(widgetName);
    }

    /**
     * Returns true iff there is at least one active agenda widget instance.
     */
    public static boolean isAnyWidgetActive(Context context) {
        int[] widgetIds = getAllWidgetIds(context);
        return widgetIds != null && widgetIds.length > 0;
    }

    /* END utility methods.        *
     * BEGIN widget setup methods. */

    public static void setupWidgets(Context context, int[] widgetIds) {
        for (int widgetId : widgetIds) {
            setupWidget(context, widgetId);
        }
        UpdateService.start(context);
    }

    private static void setupWidget(Context context, int widgetId) {
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget);
        rv.setEmptyView(R.id.listview_days, R.id.empty_view);


        // Link remote service to provide widget data.
        Intent intent = new Intent(context, AgendaWidgetService.class)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))); // Need this line so we get a distinct
        rv.setRemoteAdapter(R.id.listview_days, intent);                   // factory for each widget.

        // Setup widget button actions (new event and config activity).
        rv.setOnClickPendingIntent(
                R.id.button_new_event, getNewEventBroadcast(context, widgetId));
        rv.setOnClickPendingIntent(
                R.id.button_open_config, getOpenConfigBroadcast(context, widgetId));

        // Set colors based on theme (white or black).
        ConfigManager configManager = new ConfigManager(context, widgetId);
        boolean useWhiteTheme = configManager.getBoolean(R.string.config_text_color_key, true);
        rv.setTextColor(R.id.empty_view,
                context.getResources().getColor(
                        useWhiteTheme ?
                                R.color.text_subtitle :
                                R.color.text_subtitle_dark));
        rv.setImageViewResource(R.id.button_new_event,
                useWhiteTheme ? R.drawable.ic_add_white_24dp : R.drawable.ic_add_black_24dp);
        rv.setImageViewResource(R.id.button_open_config,
                useWhiteTheme ? R.drawable.ic_settings_white_24dp : R.drawable.ic_settings_black_24dp);

        // Date click opens given calendar day.
        rv.setPendingIntentTemplate(
                R.id.listview_days, getViewDateBroadcastTemplate(context, widgetId));

        AppWidgetManager.getInstance(context)
                .updateAppWidget(widgetId, rv);
    }

    private static PendingIntent getOpenConfigBroadcast(Context context, int widgetId) {
        Intent openConfigIntent = new Intent(context, AgendaWidgetProvider.class)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                .setAction(ACTION_OPEN_CONFIG);
        return PendingIntent.getBroadcast(
                context, widgetId, openConfigIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static PendingIntent getNewEventBroadcast(Context context, int widgetId) {
        Intent newEventIntent = new Intent(context, AgendaWidgetProvider.class)
                .setAction(ACTION_EDIT_NEW_EVENT);
        return PendingIntent.getBroadcast(
                context, widgetId, newEventIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static PendingIntent getViewDateBroadcastTemplate(Context context, int widgetId) {
        Intent viewDateIntent = new Intent(context, AgendaWidgetProvider.class)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                .setAction(ACTION_VIEW_DATE);
        return PendingIntent.getBroadcast(
                context, widgetId, viewDateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /* END widget setup methods. */
}
