package com.example.frewa814.livekrubb.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.example.frewa814.livekrubb.R;
import com.example.frewa814.livekrubb.login.LoginActivity;


/**
 * Created by Fredrik on 2015-04-22.
 */
public class LoadingScreenActivity extends Activity {
    //Introduce an delay
    private final int WAIT_TIME = 1500;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_screen);
        findViewById(R.id.mainSpinner1).setVisibility(View.VISIBLE);

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                Intent mainIntent = null;

                // Get wich activity to start.
                Bundle extras = getIntent().getExtras();
                String activity = extras.getString("START_ACTIVITY");

                if (activity.equals("MainActivity")){
                    mainIntent = new Intent(LoadingScreenActivity.this, MainActivity.class);
                }
                if (activity.equals("LoginActivity")){
                    mainIntent = new Intent(LoadingScreenActivity.this, LoginActivity.class);
                }


                if (mainIntent != null){
                    LoadingScreenActivity.this.startActivity(mainIntent);
                    LoadingScreenActivity.this.finish();
                }
            }
        }, WAIT_TIME);
    }
}


