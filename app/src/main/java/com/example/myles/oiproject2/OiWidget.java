package com.example.myles.oiproject2;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.LinearLayoutCompat;
import android.telephony.SmsManager;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link OiWidgetConfigureActivity OiWidgetConfigureActivity}
 */
public class OiWidget extends AppWidgetProvider {

    private static SmsManager smsMgr;
    private TextView test;
    private static String info;
    private int mCount = 0;
    private static ArrayList<String> names = new ArrayList<String>();
    private static ArrayList<String> numbers = new ArrayList<String>();
    private static String message;

    public static String TEST_FUNCTION = "Test Function";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        String widgetText = OiWidgetConfigureActivity.loadTitlePref(context, appWidgetId);



                // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.oi_widget);

        if(!widgetText.equals("EXAMPLE")) {
            String[] content = widgetText.split(",");
            String group = "";
            for (int i = 0; i < content.length - 1; i += 2) {
                if (!names.contains(content[i])) {
                    names.add(content[i]);
                }
                if (!numbers.contains(content[i + 1])) {
                    numbers.add(content[i + 1]);
                }
                group += content[i] + ",";
            }
            message = content[content.length - 1];
            group = group.substring(0, group.length() - 1);


            info = widgetText;

            group += " Message: " + message;
            views.setTextViewText(R.id.appwidget_text, group);

            Intent intent = new Intent(context, OiWidget.class);

            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            int[] ids = {appWidgetId};
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);

            PendingIntent pi = PendingIntent.getBroadcast(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            views.setOnClickPendingIntent(R.id.appWidgetLayout, pi);
        }

        // Instruct the widget manager to update the widget
        
        
        appWidgetManager.updateAppWidget(appWidgetId, views);

    }

    protected PendingIntent getPendingSelfIntent(Context context, String action){
       Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    private static void sendText(String widgetText){
        smsMgr = SmsManager.getDefault();
        String number = widgetText.split(",")[0];
        String message = widgetText.split(",")[2];
        smsMgr.sendTextMessage(number, null,"OI: " + message, null, null);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        /*RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.oi_widget);

        ComponentName oiWidget = new ComponentName(context, OiWidget.class);

        remoteViews.setOnClickPendingIntent(R.id.appWidgetLayout, getPendingSelfIntent(context, TEST_FUNCTION));

        appWidgetManager.updateAppWidget(oiWidget, remoteViews);*/
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            OiWidgetConfigureActivity.deleteTitlePref(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.oi_widget);

        ComponentName oiWidget = new ComponentName(context, OiWidget.class);

        if(intent.getAction() == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            for (int i = 0; i < numbers.size(); i++) {
                String number = numbers.get(i);
                smsMgr = SmsManager.getDefault();
                smsMgr.sendTextMessage(number, null, message, null, null);
            }
        }
        appWidgetManager.updateAppWidget(oiWidget, remoteViews);
    }
}

