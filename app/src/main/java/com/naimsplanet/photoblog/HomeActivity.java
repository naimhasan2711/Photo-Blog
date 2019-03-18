package com.naimsplanet.photoblog;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.FractionRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import static com.naimsplanet.photoblog.ConnectivityReceiver.IS_NETWORK_AVAILABLE;


public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Toolbar main_toolbar;
    private FloatingActionButton mAddBlogpostButton;
    private FirebaseFirestore mFirebaseFirestore;
    String current_user_id;
    private BottomNavigationView mBottomNavigationView;
    private HomeFragment homeFragment;
    private AccountFragment accountFragment;
    private NotificationFragment notificationFragment;
    IntentFilter intentFilter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        intentFilter = new IntentFilter(ConnectivityReceiver.NETWORK_AVAILABLE_ACTION);

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean isNetworkAvailable = intent.getBooleanExtra(IS_NETWORK_AVAILABLE, false);
                String networkStatus = isNetworkAvailable ? "connected" : "disconnected";

                if (networkStatus.equals("disconnected")) {
                    //startActivity(new Intent(HomeActivity.this,NetworkActivity.class));
                    Intent networkIntent = new Intent(HomeActivity.this, NetworkActivity.class);
                    startActivity(networkIntent);
                    finish();
                } else {
                    Snackbar.make(findViewById(R.id.activity_home), "Network Status: " + networkStatus, Snackbar.LENGTH_LONG).show();
                }
            }
        }, intentFilter);

        mAuth = FirebaseAuth.getInstance();
        mFirebaseFirestore = FirebaseFirestore.getInstance();

        main_toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(main_toolbar);
        getSupportActionBar().setTitle("Photo Blog");


        if (mAuth.getCurrentUser() != null) {

            mBottomNavigationView = findViewById(R.id.main_bottom_navigation_view);
            //fragments

            homeFragment = new HomeFragment();
            accountFragment = new AccountFragment();
            notificationFragment = new NotificationFragment();

            initializeFragment();

            mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.main_container);
                    switch (item.getItemId()) {
                        case R.id.bottom_home:
                            replaceFragment(homeFragment, currentFragment);
                            return true;
                        case R.id.bottom_account:
                            replaceFragment(accountFragment, currentFragment);
                            return true;
                        default:
                            return false;
                    }
                }
            });
        }

        mAddBlogpostButton = findViewById(R.id.add_post_button);
        addBlogPost();

    }



    private void addBlogPost() {
        mAddBlogpostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newActivityIntent = new Intent(HomeActivity.this, NewpostActivity.class);
                startActivity(newActivityIntent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            sentToLogin();
        } else {
            current_user_id = mAuth.getCurrentUser().getUid();
            mFirebaseFirestore.collection("Users").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        if (!task.getResult().exists()) {
                            sentToSetup();
                            finish();
                        }
                    } else {
                        String error_message = task.getException().getMessage();
                        Toast.makeText(HomeActivity.this, "Error : " + error_message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean isNetworkAvailable = intent.getBooleanExtra(IS_NETWORK_AVAILABLE, false);
                String networkStatus = isNetworkAvailable ? "connected" : "disconnected";

                if (networkStatus.equals("disconnected")) {
                    //startActivity(new Intent(HomeActivity.this,NetworkActivity.class));
                    Intent networkIntent = new Intent(HomeActivity.this, NetworkActivity.class);
                    startActivity(networkIntent);
                    finish();
                } else {
                    Snackbar.make(findViewById(R.id.activity_home), "Network Status: " + networkStatus, Snackbar.LENGTH_LONG).show();
                }
            }
        }, intentFilter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toolbar_logout:
                logout();
                return true;


        }
        return false;
    }

    private void logout() {
        mAuth.signOut();
        startActivity(new Intent(HomeActivity.this, LoginActivity.class));
        finish();
    }

    private void sentToLogin() {
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void sentToSetting() {
        Intent settingIntent = new Intent(HomeActivity.this, SetupActivity.class);
        startActivity(settingIntent);
    }

    private void sentToSetup() {
        Intent setupIntent = new Intent(HomeActivity.this, SetupActivity.class);
        startActivity(setupIntent);
    }


    private void initializeFragment() {

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        fragmentTransaction.add(R.id.main_container, homeFragment);
        fragmentTransaction.add(R.id.main_container, notificationFragment);
        fragmentTransaction.add(R.id.main_container, accountFragment);

        fragmentTransaction.hide(notificationFragment);
        fragmentTransaction.hide(accountFragment);

        fragmentTransaction.commit();

    }

    private void replaceFragment(Fragment fragment, Fragment currentFragment) {

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (fragment == homeFragment) {

            fragmentTransaction.hide(accountFragment);
            fragmentTransaction.hide(notificationFragment);

        }

        if (fragment == accountFragment) {

            fragmentTransaction.hide(homeFragment);
            fragmentTransaction.hide(notificationFragment);

        }

        if (fragment == notificationFragment) {

            fragmentTransaction.hide(homeFragment);
            fragmentTransaction.hide(accountFragment);

        }
        fragmentTransaction.show(fragment);

        //fragmentTransaction.replace(R.id.main_container, fragment);
        fragmentTransaction.commit();

    }

    private void sentToCommnet() {
        startActivity(new Intent(HomeActivity.this, CommentActivity.class));
    }


}
