package com.doskapps.interradio.activities;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ProgressBar;

import com.doskapps.interradio.Config;
import com.doskapps.interradio.R;
import com.doskapps.interradio.models.Settings;
import com.doskapps.interradio.rests.ApiInterface;
import com.doskapps.interradio.rests.RestAdapter;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityPrivacyPolicy extends AppCompatActivity {

    WebView wv_privacy_policy;
    ProgressBar progressBar;
    Button btn_failed_retry;
    View lyt_failed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);
        setupToolbar();

        if (Config.ENABLE_RTL_MODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
        }

        wv_privacy_policy = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        btn_failed_retry = findViewById(R.id.failed_retry);
        lyt_failed = findViewById(R.id.lyt_failed);

        displayData();

        btn_failed_retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lyt_failed.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                displayData();
            }
        });
    }

    public void setupToolbar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle(R.string.privacy_policy);
        }
    }

    public void displayData() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadData();
            }
        }, 1000);
    }

    public void loadData() {
        ApiInterface apiInterface = RestAdapter.createAPI();
        Call<Settings> call = apiInterface.getPrivacyPolicy();
        call.enqueue(new Callback<Settings>() {
            @Override
            public void onResponse(Call<Settings> call, Response<Settings> response) {
                String privacy_policy = response.body().getPrivacy_policy();
                try {

                    if (Config.ENABLE_RTL_MODE) {
                        wv_privacy_policy.setBackgroundColor(Color.parseColor("#ffffff"));
                        wv_privacy_policy.setFocusableInTouchMode(false);
                        wv_privacy_policy.setFocusable(false);
                        wv_privacy_policy.getSettings().setDefaultTextEncodingName("UTF-8");

                        WebSettings webSettings = wv_privacy_policy.getSettings();
                        Resources res = getResources();
                        int fontSize = res.getInteger(R.integer.font_size);
                        webSettings.setDefaultFontSize(fontSize);

                        String mimeType = "text/html; charset=UTF-8";
                        String encoding = "utf-8";
                        String htmlText = privacy_policy;

                        String text = "<html dir='rtl'><head>"
                                + "<style type=\"text/css\">body{color: #525252;}"
                                + "</style></head>"
                                + "<body>"
                                + htmlText
                                + "</body></html>";

                        wv_privacy_policy.loadData(text, mimeType, encoding);

                        progressBar.setVisibility(View.GONE);
                        lyt_failed.setVisibility(View.GONE);
                    } else {
                        wv_privacy_policy.setBackgroundColor(Color.parseColor("#ffffff"));
                        wv_privacy_policy.setFocusableInTouchMode(false);
                        wv_privacy_policy.setFocusable(false);
                        wv_privacy_policy.getSettings().setDefaultTextEncodingName("UTF-8");

                        WebSettings webSettings = wv_privacy_policy.getSettings();
                        Resources res = getResources();
                        int fontSize = res.getInteger(R.integer.font_size);
                        webSettings.setDefaultFontSize(fontSize);

                        String mimeType = "text/html; charset=UTF-8";
                        String encoding = "utf-8";
                        String htmlText = privacy_policy;

                        String text = "<html><head>"
                                + "<style type=\"text/css\">body{color: #525252;}"
                                + "</style></head>"
                                + "<body>"
                                + htmlText
                                + "</body></html>";

                        wv_privacy_policy.loadData(text, mimeType, encoding);

                        progressBar.setVisibility(View.GONE);
                        lyt_failed.setVisibility(View.GONE);
                    }

                } catch (Exception e) {
                    Log.d("onResponse", "There is an error");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Settings> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                lyt_failed.setVisibility(View.VISIBLE);
            }

        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

}
