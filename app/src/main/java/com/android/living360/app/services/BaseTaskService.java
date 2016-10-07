package com.android.living360.app.services;

import android.app.Service;
import android.util.Log;

/**
 * Created by Jose on 10/7/2016.
 */

public abstract class BaseTaskService extends Service {

    private static final String TAG = "BaseTaskService";
    private int numTasks = 0;

    public void taskStarted() {
        changeNumberOfTasks(1);
    }

    public void taskCompleted() {
        changeNumberOfTasks(-1);
    }

    private synchronized void changeNumberOfTasks(int delta) {
        Log.d(TAG, "changeNumberOfTasks:" + numTasks + ":");
        numTasks += delta;

        if (numTasks <= 0) {
            Log.d(TAG, "stopping");
            stopSelf();
        }
    }
}
