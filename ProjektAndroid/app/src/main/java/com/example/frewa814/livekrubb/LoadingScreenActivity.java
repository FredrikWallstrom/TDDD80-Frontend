package com.example.frewa814.livekrubb;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;



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
                Intent mainIntent = new Intent(LoadingScreenActivity.this,MainActivity.class);
                LoadingScreenActivity.this.startActivity(mainIntent);
                LoadingScreenActivity.this.finish();
            }
        }, WAIT_TIME);
    }
}


