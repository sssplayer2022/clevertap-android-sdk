package com.clevertap.android.sdk;

import static com.clevertap.android.sdk.Constants.AUTH;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.core.content.ContextCompat;
import com.clevertap.android.sdk.task.CTExecutorFactory;
import com.clevertap.android.sdk.task.Task;
import com.google.firebase.messaging.RemoteMessage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;
import javax.net.ssl.HttpsURLConnection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class Utils {

    public static boolean haveVideoPlayerSupport;

    public static boolean haveDeprecatedFirebaseInstanceId;

    public static boolean containsIgnoreCase(Collection<String> collection, String key) {
        if (collection == null || key == null) {
            return false;
        }
        for (String entry : collection) {
            if (key.equalsIgnoreCase(entry)) {
                return true;
            }
        }
        return false;
    }

    public static HashMap<String, Object> convertBundleObjectToHashMap(@NonNull Bundle b) {
        final HashMap<String, Object> map = new HashMap<>();
        for (String s : b.keySet()) {
            final Object o = b.get(s);

            if (o instanceof Bundle) {
                map.putAll(convertBundleObjectToHashMap((Bundle) o));
            } else {
                map.put(s, b.get(s));
            }
        }
        return map;
    }

    public static ArrayList<HashMap<String, Object>> convertJSONArrayOfJSONObjectsToArrayListOfHashMaps(
            JSONArray jsonArray) {
        final ArrayList<HashMap<String, Object>> hashMapArrayList = new ArrayList<>();
        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    hashMapArrayList.add(convertJSONObjectToHashMap(jsonArray.getJSONObject(i)));
                } catch (JSONException e) {
                    Logger.v("Could not convert JSONArray of JSONObjects to ArrayList of HashMaps - " + e
                            .getMessage());
                }
            }
        }
        return hashMapArrayList;
    }

    public static ArrayList<String> convertJSONArrayToArrayList(JSONArray array) {
        ArrayList<String> listdata = new ArrayList<>();
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                try {
                    listdata.add(array.getString(i));
                } catch (JSONException e) {
                    Logger.v("Could not convert JSONArray to ArrayList - " + e.getMessage());
                }
            }
        }
        return listdata;
    }

    public static HashMap<String, Object> convertJSONObjectToHashMap(JSONObject b) {
        final HashMap<String, Object> map = new HashMap<>();
        final Iterator<String> keys = b.keys();

        while (keys.hasNext()) {
            try {
                final String s = keys.next();
                final Object o = b.get(s);
                if (o instanceof JSONObject) {
                    map.putAll(convertJSONObjectToHashMap((JSONObject) o));
                } else {
                    map.put(s, b.get(s));
                }
            } catch (Throwable ignored) {
                // Ignore
            }
        }

        return map;
    }

    public static String convertToTitleCase(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        StringBuilder converted = new StringBuilder();

        boolean convertNext = true;
        for (char ch : text.toCharArray()) {
            if (Character.isSpaceChar(ch)) {
                convertNext = true;
            } else if (convertNext) {
                ch = Character.toTitleCase(ch);
                convertNext = false;
            } else {
                ch = Character.toLowerCase(ch);
            }
            converted.append(ch);
        }

        return converted.toString();
    }

    public static Bitmap getBitmapFromURL(@NonNull String srcUrl) {
        // Safe bet, won't have more than three /s . url must not be null since we are not handling null pointer exception that would cause otherwise
        srcUrl = srcUrl.replace("///", "/");
        srcUrl = srcUrl.replace("//", "/");
        srcUrl = srcUrl.replace("http:/", "http://");
        srcUrl = srcUrl.replace("https:/", "https://");
        HttpURLConnection connection = null;
        try {
            URL url = new URL(srcUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {

            Logger.v("Couldn't download the notification icon. URL was: " + srcUrl);
            e.printStackTrace();
            return null;
            //todo catch other exceptions?
        } finally {
            try {
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (Throwable t) {
                Logger.v("Couldn't close connection!", t);
            }
        }
    }

    public static Bitmap getBitmapFromURLWithSizeConstraint(String srcUrl, int size) {
        // Safe bet, won't have more than three /s
        srcUrl = srcUrl.replace("///", "/");
        srcUrl = srcUrl.replace("//", "/");
        srcUrl = srcUrl.replace("http:/", "http://");
        srcUrl = srcUrl.replace("https:/", "https://");
        HttpURLConnection connection = null;
        try {
            URL url = new URL(srcUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setUseCaches(true);
            connection.addRequestProperty("Accept-Encoding", "gzip, deflate");
            connection.connect();
            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Logger.d("File not loaded completely not going forward. URL was: " + srcUrl);
                return null;
            }

            // might be -1: server did not report the length
            long fileLength = connection.getContentLength();
            boolean isGZipEncoded = (connection.getContentEncoding() != null &&
                    connection.getContentEncoding().contains("gzip"));

            // download the file
            InputStream input = connection.getInputStream();

            byte[] buffer = new byte[16384];
            ByteArrayOutputStream finalData = new ByteArrayOutputStream();

            Logger.v("Downloading " + srcUrl + "....");
            long total = 0;
            int count;
            while ((count = input.read(buffer)) != -1) {
                total += count;
                finalData.write(buffer, 0, count);
                if (total > size) {
                    Logger.v("Image size is larger than " + size + " bytes. Cancelling download!");
                    return null;
                }
                Logger.v("Downloaded " + total + " bytes");
            }

            byte[] tmpByteArray = new byte[16384];
            long totalDownloaded = total;

            Logger.v("Total download size for bitmap = " + totalDownloaded);

            if (isGZipEncoded) {
                InputStream is = new ByteArrayInputStream(finalData.toByteArray());
                ByteArrayOutputStream decompressedFile = new ByteArrayOutputStream();
                GZIPInputStream gzipInputStream = new GZIPInputStream(is);
                total = 0;
                int counter;
                while ((counter = gzipInputStream.read(tmpByteArray)) != -1) {
                    total += counter;
                    decompressedFile.write(tmpByteArray, 0, counter);
                }
                Logger.v("Total decompressed download size for bitmap = " + total);
                if (fileLength != -1 && fileLength != totalDownloaded) {
                    Logger.d("File not loaded completely not going forward. URL was: " + srcUrl);
                    return null;
                }
                return BitmapFactory.decodeByteArray(decompressedFile.toByteArray(), 0, (int) total);
            }

            if (fileLength != -1 && fileLength != totalDownloaded) {
                Logger.d("File not loaded completely not going forward. URL was: " + srcUrl);
                return null;
            }
            return BitmapFactory.decodeByteArray(finalData.toByteArray(), 0, (int) totalDownloaded);
        } catch (IOException e) {
            e.printStackTrace();
            Logger.v("Couldn't download the file. URL was: " + srcUrl);
            return null;
        } finally {
            try {
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (Throwable t) {
                Logger.v("Couldn't close connection!", t);
            }
        }
    }

    public static byte[] getByteArrayFromImageURL(String srcUrl) {
        srcUrl = srcUrl.replace("///", "/");
        srcUrl = srcUrl.replace("//", "/");
        srcUrl = srcUrl.replace("http:/", "http://");
        srcUrl = srcUrl.replace("https:/", "https://");
        HttpsURLConnection connection = null;
        try {
            URL url = new URL(srcUrl);
            connection = (HttpsURLConnection) url.openConnection();
            InputStream is = connection.getInputStream();
            byte[] buffer = new byte[8192];
            int bytesRead;
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while ((bytesRead = is.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            Logger.v("Error processing image bytes from url: " + srcUrl);
            return null;
        } finally {
            try {
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (Throwable t) {
                Logger.v("Couldn't close connection!", t);
            }
        }
    }

    @SuppressLint("MissingPermission")
    public static String getCurrentNetworkType(final Context context) {
        try {
            // First attempt to check for WiFi connectivity
            ConnectivityManager connManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connManager == null) {
                return "Unavailable";
            }
            NetworkInfo mWifi = connManager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if (mWifi != null && mWifi.isConnected()) {
                return "WiFi";
            }

            return getDeviceNetworkType(context);


        } catch (Throwable t) {
            return "Unavailable";
        }
    }

    @SuppressLint("MissingPermission")
    public static String getDeviceNetworkType(@NonNull  final Context context) {
        // Fall back to network type
        TelephonyManager teleMan = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (teleMan == null) {
            return "Unavailable";
        }

        int networkType = TelephonyManager.NETWORK_TYPE_UNKNOWN;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (hasPermission(context, Manifest.permission.READ_PHONE_STATE)) {
                try {
                    networkType = teleMan.getDataNetworkType();
                } catch (SecurityException se) {
                    Logger.d("Security Exception caught while fetch network type" + se.getMessage());
                }
            } else {
                Logger.d("READ_PHONE_STATE permission not asked by the app or not granted by the user");
            }
        } else {
            networkType = teleMan.getNetworkType();
        }

        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "2G";
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "3G";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "4G";
            case TelephonyManager.NETWORK_TYPE_NR:
                return "5G";
            default:
                return "Unknown";
        }
    }

//    @RestrictTo(Scope.LIBRARY)
//    public static String getFcmTokenUsingManifestMetaEntry(Context context, CleverTapInstanceConfig config) {
//        String token = null;
//        try {
//            String senderID = ManifestInfo.getInstance(context).getFCMSenderId();
//            if (senderID != null) {
//                config.getLogger().verbose(config.getAccountId(),
//                        "Requesting an FCM token with Manifest SenderId - " + senderID);
//                token = FirebaseInstanceId.getInstance().getToken(senderID, FirebaseMessaging.INSTANCE_ID_SCOPE);
//            }
//            config.getLogger().info(config.getAccountId(), "FCM token using Manifest SenderId: " + token);
//        } catch (Throwable t) {
//            config.getLogger().verbose(config.getAccountId(), "Error requesting FCM token with Manifest SenderId", t);
//        }
//        return token;
//    }

    public static long getMemoryConsumption() {
        long free = Runtime.getRuntime().freeMemory();
        long total = Runtime.getRuntime().totalMemory();
        return total - free;
    }

    public static Bitmap getNotificationBitmap(String icoPath, boolean fallbackToAppIcon, final Context context)
            throws NullPointerException {
        return getNotificationBitmapWithSizeConstraints(icoPath,fallbackToAppIcon,context,-1);
    }

    public static Bitmap getNotificationBitmapWithSizeConstraints(String icoPath, boolean fallbackToAppIcon,
            final Context context, int size)
            throws NullPointerException {
        // If the icon path is not specified
        if (icoPath == null || icoPath.equals("")) {
            return fallbackToAppIcon ? getAppIcon(context) : null;
        }
        // Simply stream the bitmap
        if (!icoPath.startsWith("http")) {
            icoPath = Constants.ICON_BASE_URL + "/" + icoPath;
        }
        Bitmap ic;
        if (size == -1) {
            ic = getBitmapFromURL(icoPath);
        } else {
            ic = getBitmapFromURLWithSizeConstraint(icoPath, size);
        }
        return (ic != null) ? ic : ((fallbackToAppIcon) ? getAppIcon(context) : null);
    }

    /**
     * get bitmap from url within defined timeoutMillis bound and sizeBytes bound or else return null or app icon
     * based on fallbackToAppIcon param
     */
    public static Bitmap getNotificationBitmapWithTimeoutAndSize(String icoPath, boolean fallbackToAppIcon,
            final Context context, final CleverTapInstanceConfig config, long timeoutMillis, int sizeBytes)
            throws NullPointerException {
        Task<Bitmap> task = CTExecutorFactory.executors(config).ioTask();
        return task.submitAndGetResult("getNotificationBitmap",
                () -> getNotificationBitmapWithSizeConstraints(icoPath, fallbackToAppIcon, context, sizeBytes)
                , timeoutMillis);
    }

    public static int getNow() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    public static int getThumbnailImage(Context context, String image) {
        if (context != null) {
            return context.getResources().getIdentifier(image, "drawable", context.getPackageName());
        } else {
            return -1;
        }
    }

    /**
     * Checks whether a particular permission is available or not.
     *
     * @param context    The Android {@link Context}
     * @param permission The fully qualified Android permission name
     */
    public static boolean hasPermission(@NonNull final Context context,@NonNull String permission) {
        try {
            return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(context, permission);
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean isActivityDead(Activity activity) {
        if (activity == null) {
            return true;
        }
        boolean isActivityDead = activity.isFinishing();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            isActivityDead = isActivityDead || activity.isDestroyed();
        }
        return isActivityDead;
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public static boolean isServiceAvailable(@NonNull Context context, Class clazz) {
        if (clazz == null) {
            return false;
        }

        PackageManager pm = context.getPackageManager();
        String packageName = context.getPackageName();

        PackageInfo packageInfo;
        try {
            packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SERVICES);
            ServiceInfo[] services = packageInfo.services;
            for (ServiceInfo serviceInfo : services) {
                if (serviceInfo.name.equals(clazz.getName())) {
                    Logger.v("Service " + serviceInfo.name + " found");
                    return true;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            Logger.d("Intent Service name not found exception - " + e.getLocalizedMessage());
        }
        return false;
    }

    public static String optionalStringKey(JSONObject o, String k)
            throws JSONException {
        if (o.has(k) && !o.isNull(k)) {
            return o.getString(k);
        }

        return null;
    }

    /**
     * Handy method to post any runnable to run on the main thread.
     *
     * @param runnable - task to be run
     */
    public static void runOnUiThread(Runnable runnable) {
        if (runnable != null) {
            //run if already on the UI thread
            if (Looper.myLooper() == Looper.getMainLooper()) {
                runnable.run();
            } else {
                //post on UI thread if called from Non-UI thread.
                Handler mainHandler = new Handler(Looper.getMainLooper());
                mainHandler.post(runnable);
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static void setPackageNameFromResolveInfoList(Context context, Intent launchIntent) {
        List<ResolveInfo> resolveInfoList = context.getPackageManager().queryIntentActivities(launchIntent, 0);
        if (resolveInfoList != null) {
            String appPackageName = context.getPackageName();
            for (ResolveInfo resolveInfo : resolveInfoList) {
                if (appPackageName.equals(resolveInfo.activityInfo.packageName)) {
                    launchIntent.setPackage(appPackageName);
                    break;
                }
            }
        }
    }

    /**
     * @param content String which contains bundle information
     * @return Bundle to be passed to createNotification(Context context, Bundle extras)
     */
    @SuppressWarnings("rawtypes")
    public static Bundle stringToBundle(String content) throws JSONException {

        Bundle bundle = new Bundle();

        if (!TextUtils.isEmpty(content)) {
            JSONObject jsonObject = new JSONObject(content);
            Iterator iter = jsonObject.keys();
            while (iter.hasNext()) {
                String key = (String) iter.next();
                String value = jsonObject.getString(key);
                bundle.putString(key, value);
            }
        }

        return bundle;
    }

    public static boolean validateCTID(String cleverTapID) {
        if (cleverTapID == null) {
            Logger.i(
                    "CLEVERTAP_USE_CUSTOM_ID has been set as 1 in AndroidManifest.xml but custom CleverTap ID passed is NULL.");
            return false;
        }
        if (cleverTapID.isEmpty()) {
            Logger.i(
                    "CLEVERTAP_USE_CUSTOM_ID has been set as 1 in AndroidManifest.xml but custom CleverTap ID passed is empty.");
            return false;
        }
        if (cleverTapID.length() > 64) {
            Logger.i("Custom CleverTap ID passed is greater than 64 characters. ");
            return false;
        }
        if (!cleverTapID.matches("[=|<>;+.A-Za-z0-9()!:$@_-]*")) {
            Logger.i("Custom CleverTap ID cannot contain special characters apart from : =,(,),_,!,@,$,|<,>,;,+,. and - ");
            return false;
        }
        return true;
    }

    static Bitmap drawableToBitmap(@NonNull Drawable drawable)
            throws NullPointerException {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    /**
     * Method to check whether app has ExoPlayer dependencies
     *
     * @return boolean - true/false depending on app's availability of ExoPlayer dependencies
     */
    private static boolean checkForExoPlayer() {
        boolean exoPlayerPresent = false;
        Class className = null;
        try {
            className = Class.forName("com.google.android.exoplayer2.ExoPlayer");
            className = Class.forName("com.google.android.exoplayer2.source.hls.HlsMediaSource");
            className = Class.forName("com.google.android.exoplayer2.ui.StyledPlayerView");

            Logger.d("ExoPlayer is present");
            exoPlayerPresent = true;
        } catch (Throwable t) {
            Logger.d("ExoPlayer library files are missing!!!");
            Logger.d(
                    "Please add ExoPlayer dependencies to render InApp or Inbox messages playing video. For more information checkout CleverTap documentation.");
            if (className != null) {
                Logger.d("ExoPlayer classes not found " + className.getName());
            } else {
                Logger.d("ExoPlayer classes not found");
            }
        }
        return exoPlayerPresent;
    }

    private static Bitmap getAppIcon(final Context context) throws NullPointerException {
        // Try to get the app logo first
        try {
            Drawable logo = context.getPackageManager().getApplicationLogo(context.getApplicationInfo());
            if (logo == null) {
                throw new Exception("Logo is null");
            }
            return drawableToBitmap(logo);
        } catch (Exception e) {
            e.printStackTrace();
            // Try to get the app icon now
            // No error handling here - handle upstream
            return drawableToBitmap(context.getPackageManager().getApplicationIcon(context.getApplicationInfo()));
        }
    }

    public static String getSCDomain(String domain) {
        String[] parts = domain.split("\\.", 2);
        return parts[0] + "." + AUTH + "." + parts[1];
    }

    public static boolean isRenderFallback(RemoteMessage remoteMessage, Context context) {
        boolean renderRateKillSwitch = Boolean
                .parseBoolean(remoteMessage.getData().get(Constants.WZRK_TSR_FB));//tsrfb
        boolean renderRateFallback = Boolean
                .parseBoolean(remoteMessage.getData().get(Constants.NOTIFICATION_RENDER_FALLBACK));

        return !renderRateKillSwitch && renderRateFallback;

    }

    public static void navigateToAndroidSettingsForNotifications(Context context){
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("app_package", context.getPackageName());
            intent.putExtra("app_uid", context.getApplicationInfo().uid);
        } else {
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
        }
        context.startActivity(intent);
    }

    static {
        haveVideoPlayerSupport = checkForExoPlayer();
    }
}