package com.example.frewa814.livekrubb.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import com.example.frewa814.livekrubb.R;
import com.example.frewa814.livekrubb.login.LoginActivity;

/**
 * An activity that will display an loading screen between activity shifts.
 * This is used when a user logging and when a user log out.
 * This is for the user to se that something is happening.
 */
public class LoadingScreenActivity extends Activity {
    //Introduce an delay
    private final int WAIT_TIME = 1500;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_screen);
        findViewById(R.id.mainSpinner1).setVisibility(View.VISIBLE);

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                Intent mainIntent = null;

                // Get which activity to start.
                Bundle extras = getIntent().getExtras();
                String activity = extras.getString("START_ACTIVITY");

                if (activity.equals("MainActivity")){
                    mainIntent = new Intent(LoadingScreenActivity.this, MainActivity.class);
                }
                if (activity.equals("LoginActivity")){
                    mainIntent = new Intent(LoadingScreenActivity.this, LoginActivity.class);
                }

                // Start the new activity when the delay is done.
                if (mainIntent != null){
                    LoadingScreenActivity.this.startActivity(mainIntent);
                    LoadingScreenActivity.this.finish();
                }
            }
        }, WAIT_TIME);
    }
}


