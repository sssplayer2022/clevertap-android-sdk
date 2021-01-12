package com.clevertap.android.sdk.extras;

import android.os.AsyncTask;

import static com.clevertap.android.sdk.extras.CleverTapExecutors.io;

public class CTAsyncTask extends AsyncTask<Void, Void, Void> {

    private final Runnable run;

    public CTAsyncTask(Runnable run) {
        this.run = run;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        if (run != null) {
            run.run();
        }
        return null;
    }

    public void interrupt() {
        cancel(true);
    }

    public void start() {
        executeOnExecutor(io());
    }
}
