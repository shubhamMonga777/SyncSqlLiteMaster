package com.example.syncmaster.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class SyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static SyncAdaptor syncAdaptor = null;

    @Override
    public void onCreate() {
        Log.d("MovieSyncService", "onCreate - MovieSyncService");
        synchronized (sSyncAdapterLock) {
            if (syncAdaptor == null) {
                syncAdaptor = new SyncAdaptor(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return syncAdaptor.getSyncAdapterBinder();
    }
}


