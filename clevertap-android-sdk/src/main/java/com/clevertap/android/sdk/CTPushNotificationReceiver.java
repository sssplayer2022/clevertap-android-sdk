package com.clevertap.android.sdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;


public class CTPushNotificationReceiver extends BroadcastReceiver {
    public static final String FROM_CLEVERTAP = "from_cleverTap";
    public static final String CLEVERTAP_NOTIFICATION_CLICKED = "clever_tap_notification_clicked";
    public final static String DEEPLINK_ACTIVITY = "com.mxtech.videoplayer.ad.online.mxexo.WebLinksRouterActivity";

    @Override
    public void onReceive(Context context, Intent intent) {

        try {
            Intent launchIntent;

            Bundle extras = intent.getExtras();
            if (extras == null) {
                return;
            }

            if (extras.containsKey(Constants.DEEP_LINK_KEY)){
                try {
                    Class<?> webLinkActivity = Class.forName(DEEPLINK_ACTIVITY);
                    if (webLinkActivity == null) {
                        return;
                    }
                    launchIntent = new Intent(context, webLinkActivity);
                    launchIntent.setData(Uri.parse(intent.getStringExtra(Constants.DEEP_LINK_KEY)));
                } catch (Exception e) {
                    return;
                }
            } else {
                launchIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
                if (launchIntent == null) {
                    return;
                }
            }

            CleverTapAPI.handleNotificationClicked(context,extras);

            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            launchIntent.putExtra(FROM_CLEVERTAP, CLEVERTAP_NOTIFICATION_CLICKED);
            launchIntent.putExtras(extras);

            //to prevent calling of pushNotificationClickedEvent(extras) in ActivityLifecycleCallback
            launchIntent.putExtra(Constants.WZRK_FROM_KEY, Constants.WZRK_FROM);

            context.startActivity(launchIntent);

            Logger.d("CTPushNotificationReceiver: handled notification: " + extras.toString());
        } catch (Throwable t) {
            Logger.v("CTPushNotificationReceiver: error handling notification", t);
        }
    }
}
