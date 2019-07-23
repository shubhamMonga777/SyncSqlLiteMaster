package com.example.syncmaster.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateFormat;
import android.util.Log;

import com.example.syncmaster.DatabaseHandler;
import com.example.syncmaster.NewsModel;
import com.example.syncmaster.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static android.support.constraint.Constraints.TAG;

public class SyncAdaptor extends AbstractThreadedSyncAdapter {

    public final String LOG_TAG = SyncAdaptor.class.getSimpleName();

    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;

    ContentResolver mContentResolver;

    private Context context;

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    public SyncAdaptor(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        this.context = context;
        mContentResolver = context.getContentResolver();

    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");
        BufferedReader reader = null;

        HttpURLConnection urlConnection = null;

        // Will contain the raw JSON response as a string.
        String newsJsonStr = null;

        try {

            final String News_URL =
                    "https://newsapi.org/v2/everything?q=bitcoin&from=2019-06-23&sortBy=publishedAt&apiKey=973945f3066e4f569dfb1ad2c1d9bed5";

            Uri builtUri = Uri.parse(News_URL).buildUpon()
                    .build();


            URL url = new URL(builtUri.toString());

            Log.d(LOG_TAG, "The URL link is  " + url);

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(1000000 /* milliseconds */);
            urlConnection.setConnectTimeout(1500000/* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();

            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return;
            }

            newsJsonStr = buffer.toString();
            getNewsData(newsJsonStr);

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error passing data ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        return;
    }

    private void getNewsData(String newsJsonStr)
            throws JSONException {


        final String KEY_SOURCE = "source";
        final String KEY_SOURCENAME = "name";
        final String KEY_TITLE = "title";
        final String KEY_DESCRIPTION = "description";
        final String KEY_IMAGE = "urlToImage";
        final String KEY_ARTICLES = "articles";

        String time = getReminingTime();

        DatabaseHandler db = new DatabaseHandler(context);


        try {
            JSONObject newsJson = new JSONObject(newsJsonStr);


            JSONArray articlesArray = newsJson.getJSONArray(KEY_ARTICLES);


            for (int i = 0; i < articlesArray.length(); i++) {

                String sourceName;
                String title;
                String description;
                String imageUrl;

                JSONObject newsDetails = articlesArray.getJSONObject(i);

                title = newsDetails.getString(KEY_TITLE);
                description = newsDetails.getString(KEY_DESCRIPTION);
                imageUrl = newsDetails.getString(KEY_IMAGE);
                if (imageUrl == null)
                    imageUrl = "";

                JSONObject sourceDetails = newsDetails.getJSONObject(KEY_SOURCE);

                sourceName = sourceDetails.getString(KEY_SOURCENAME);

                NewsModel newsModel = new NewsModel(sourceName, title, imageUrl, description, time);
                db.addNews(newsModel);


                Log.d(LOG_TAG, "Inserted Successfully " + articlesArray.toString());
            }

            Log.d(LOG_TAG, "Inserted Successfully " + articlesArray.length());
            sendMessage();

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

    }

    private String getReminingTime() {
        String delegate = "hh:mm aaa";
        return (String) DateFormat.format(delegate, Calendar.getInstance().getTime());
    }

    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {

            /*
             * Add the account and account type, no password or user data
             * If successful, return the Account object, otherwise report an error.
             */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */


        SyncAdaptor.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
//        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {


        getSyncAccount(context);


    }

    private void sendMessage() {
        Log.d(LOG_TAG, "Broadcasting message");
        Intent intent = new Intent("UpdateNews");
        // You can also include some extra data.
        intent.putExtra("message", "Adding morenews");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }


}
