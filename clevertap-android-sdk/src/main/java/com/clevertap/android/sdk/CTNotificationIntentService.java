package com.clevertap.android.sdk;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;

import static com.clevertap.android.sdk.CTPushNotificationReceiver.DEEPLINK_ACTIVITY;

public class CTNotificationIntentService extends IntentService {

    public final static String MAIN_ACTION = "com.clevertap.PUSH_EVENT";
    public final static String TYPE_BUTTON_CLICK = "com.clevertap.ACTION_BUTTON_CLICK";

    public CTNotificationIntentService() {
        super("CTNotificationIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras == null) return;

        String type = extras.getString("ct_type");
        if (TYPE_BUTTON_CLICK.equals(type)) {
            Logger.v("CTNotificationIntentService handling " + TYPE_BUTTON_CLICK);
            handleActionButtonClick(extras);
        } else {
            Logger.v("CTNotificationIntentService: unhandled intent "+intent.getAction());
        }
    }

    private void handleActionButtonClick(Bundle extras) {
        try {
            boolean autoCancel = extras.getBoolean("autoCancel", false);
            int notificationId = extras.getInt("notificationId", -1);
            String dl = extras.getString("dl");

            Context context = getApplicationContext();
            Intent launchIntent;
            if (dl != null) {
                try {
                    Class<?> webLinkActivity = Class.forName(DEEPLINK_ACTIVITY);
                    if (webLinkActivity == null) {
                        return;
                    }
                    launchIntent = new Intent(context, webLinkActivity);
                    launchIntent.setData(Uri.parse(dl));
                } catch (Exception e) {
                    return;
                }
//                launchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(dl));
            } else {
                launchIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            }

            if (launchIntent == null) {
                Logger.v("CTNotificationService: create launch intent.");
               return;
            }

            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            launchIntent.putExtra(CTPushNotificationReceiver.FROM_CLEVERTAP, CTPushNotificationReceiver.CLEVERTAP_NOTIFICATION_CLICKED);
            launchIntent.putExtras(extras);
            launchIntent.removeExtra("dl");

            if (autoCancel && notificationId > -1) {
                NotificationManager notificationManager =
                        (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null) {
                    notificationManager.cancel(notificationId);
                }

            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)); // close the notification drawer
            }
            startActivity(launchIntent);
        } catch (Throwable t) {
            Logger.v("CTNotificationService: unable to process action button click:  "+ t.getLocalizedMessage());
        }
    }
}
