package com.clevertap.android.sdk.pushnotification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.android.sdk.Constants;
import com.clevertap.android.sdk.Logger;
import com.clevertap.android.sdk.Utils;


public class CTPushNotificationReceiver extends BroadcastReceiver {
    public static final String DEEPLINK_ACTIVITY = "com.mxtech.videoplayer.ad.online.mxexo.WebLinksRouterActivity";
    public static final String FROM_CLEVERTAP = "from_cleverTap";
    public static final String CLEVERTAP_NOTIFICATION_CLICKED = "clever_tap_notification_clicked";
    private static Class<?> extraDeepLinkClz = null;
    @Override
    public void onReceive(Context context, Intent intent) {

        try {
            Intent launchIntent = null;

            Bundle extras = intent.getExtras();
            if (extras == null) {
                return;
            }

            if (extras.containsKey(Constants.DEEP_LINK_KEY)) {
                try {
                    Class<?> webLinkActivity = extraDeepLinkClz;
                    try {
                        if (webLinkActivity == null) {
                            webLinkActivity = Class.forName(DEEPLINK_ACTIVITY);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (webLinkActivity != null) {
                        launchIntent = new Intent(context, webLinkActivity);
                        launchIntent.setData(Uri.parse(intent.getStringExtra(Constants.DEEP_LINK_KEY)));
                        Utils.setPackageNameFromResolveInfoList(context, launchIntent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (launchIntent == null) {
                launchIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            }
            if (launchIntent == null) {
                return;
            }

            CleverTapAPI.handleNotificationClicked(context, extras);

            launchIntent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
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

    public static void setExtraDeepLinkClz(Class<?> extraDeepLinkClz) {
        CTPushNotificationReceiver.extraDeepLinkClz = extraDeepLinkClz;
    }

    public static Class<?> getExtraDeepLinkClz() {
        return extraDeepLinkClz;
    }
}
