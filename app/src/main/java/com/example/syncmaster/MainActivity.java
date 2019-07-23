package com.example.syncmaster;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.CursorIndexOutOfBoundsException;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.syncmaster.sync.SyncAdaptor;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView mTime, mCount, mnewsStatus;
    private RecyclerView mrecyclerView;
    private List<NewsModel> newsModels;
    private DatabaseHandler db;
    private Adaptor adaptor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SyncAdaptor.initializeSyncAdapter(this);
        db = new DatabaseHandler(this);
        newsModels = new ArrayList<>();

        initViews();
        initAdaptor();
        initBroadcaster();

        getNews();
        setData();
    }

    @SuppressLint("SetTextI18n")
    private void setData() {

        if (newsModels.size() == 0)
            mnewsStatus.setVisibility(View.VISIBLE);
        else
            mnewsStatus.setVisibility(View.GONE);

        try {

            mCount.setText("Total News - " + db.getNewsCount());
            mTime.setText("Last Updated time - " + db.getLastUpdatedNewsTime());

        } catch (CursorIndexOutOfBoundsException e) {
            e.printStackTrace();
        }

    }

    private void initBroadcaster() {

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("UpdateNews"));
    }

    private void getNews() {

        newsModels = db.getAllNews();


        if (newsModels.size() == 0) {
            mnewsStatus.setVisibility(View.VISIBLE);
        } else {
            mnewsStatus.setVisibility(View.GONE);
            adaptor = new Adaptor(this, newsModels);
            mrecyclerView.setAdapter(adaptor);
        }


    }

    private void initAdaptor() {
        mrecyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    private void initViews() {
        mnewsStatus = findViewById(R.id.newsStatus);
        mTime = findViewById(R.id.tvtime);
        mCount = findViewById(R.id.tvcount);
        mrecyclerView = findViewById(R.id.recycler_view);

    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
            Log.d("receiver", "Got message: " + message);
            setData();
            getNews();
        }
    };

    @Override
    protected void onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }
}
