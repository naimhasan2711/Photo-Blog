package com.naimsplanet.photoblog;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import static com.naimsplanet.photoblog.ConnectivityReceiver.IS_NETWORK_AVAILABLE;

public class NetworkActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network);


        IntentFilter intentFilter = new IntentFilter(ConnectivityReceiver.NETWORK_AVAILABLE_ACTION);

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean isNetworkAvailable = intent.getBooleanExtra(IS_NETWORK_AVAILABLE, false);
                String networkStatus = isNetworkAvailable ? "connected" : "disconnected";

                if (networkStatus.equals("connected")) {
                    //startActivity(new Intent(HomeActivity.this,NetworkActivity.class));
                    Intent homeIntent = new Intent(NetworkActivity.this, HomeActivity.class);
                    startActivity(homeIntent);
                    finish();
                } else {
                    Snackbar.make(findViewById(R.id.activity_network), "Network Status: " + networkStatus, Snackbar.LENGTH_LONG).show();
                }
            }
        }, intentFilter);
    }
}
