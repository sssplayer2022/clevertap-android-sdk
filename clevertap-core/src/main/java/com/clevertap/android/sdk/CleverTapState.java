package com.clevertap.android.sdk;

import android.content.Context;
import com.clevertap.android.sdk.db.BaseDatabaseManager;
import com.clevertap.android.sdk.network.BaseNetworkManager;

abstract class CleverTapState {

    protected final Context context;

    CleverTapState(final Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    abstract BaseDatabaseManager getDatabaseManager();

    abstract void setDatabaseManager(final BaseDatabaseManager databaseManager);

    abstract BaseNetworkManager getNetworkManager();

    abstract void setNetworkManager(final BaseNetworkManager networkManager);

    abstract BaseLocationManager getLocationManager();

    abstract void setLocationManager(BaseLocationManager baseLocationManager);

}
