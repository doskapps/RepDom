package com.doskapps.radiosrepublicadominicana.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;

import com.doskapps.radiosrepublicadominicana.R;

public class ActivitySplash extends AppCompatActivity {

    private ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        new CountDownTimer(2500, 1000) {

            @Override
            public void onFinish() {
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(intent);
                finish();

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onTick(long millisUntilFinished) {

            }
        }.start();

    }
}