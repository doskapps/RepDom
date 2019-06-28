package com.doskapps.radiosrepublicadominicana.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.doskapps.radiosrepublicadominicana.Config;
import com.doskapps.radiosrepublicadominicana.R;
import com.doskapps.radiosrepublicadominicana.activities.ActivityPrivacyPolicy;
import com.doskapps.radiosrepublicadominicana.activities.MainActivity;
import com.balysv.materialripple.MaterialRippleLayout;

public class FragmentAbout extends Fragment {

    private MainActivity mainActivity;
    private Toolbar toolbar;
    View root_view, parent_view;
    MaterialRippleLayout privacy_policy, website, facebook, twitter, instagram, email, rate, more;

    public FragmentAbout() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_about, null);
        parent_view = getActivity().findViewById(R.id.main_content);
        setHasOptionsMenu(true);

        toolbar = root_view.findViewById(R.id.toolbar);
        setupToolbar();

        privacy_policy = root_view.findViewById(R.id.row_privacy_policy);
        privacy_policy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ActivityPrivacyPolicy.class);
                startActivity(intent);
            }
        });

        website = root_view.findViewById(R.id.row_website);
        website.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.website_url)));
                startActivity(intent);
            }
        });

        facebook = root_view.findViewById(R.id.row_facebook);
        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.facebook_url)));
                startActivity(intent);
            }
        });

        twitter = root_view.findViewById(R.id.row_twitter);
        twitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.twitter_url)));
                startActivity(intent);
            }
        });

        instagram = root_view.findViewById(R.id.row_instagram);
        instagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.instagram_url)));
                startActivity(intent);
            }
        });

        email = root_view.findViewById(R.id.row_email);
        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto: " + getResources().getString(R.string.email_address)));
                startActivity(Intent.createChooser(emailIntent, "Contact Us"));
            }
        });

        rate = root_view.findViewById(R.id.row_rate_us);
        rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String appName = getActivity().getPackageName();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appName)));
                }
            }
        });

        more = root_view.findViewById(R.id.row_more_apps);
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.play_more_apps)));
                startActivity(intent);
            }
        });

        hideShowMenu();

        return root_view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainActivity.setupNavigationDrawer(toolbar);
    }

    private void setupToolbar() {
        toolbar.setTitle(getString(R.string.app_name));
        toolbar.setSubtitle(getString(R.string.drawer_about));
        mainActivity.setSupportActionBar(toolbar);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);
    }

    public void hideShowMenu() {
        if (!Config.ENABLE_MENU_WEBSITE) {
            website.setVisibility(View.GONE);
        }
        if (!Config.ENABLE_MENU_FACEBOOK) {
            facebook.setVisibility(View.GONE);
        }
        if (!Config.ENABLE_MENU_TWITTER) {
            twitter.setVisibility(View.GONE);
        }
        if (!Config.ENABLE_MENU_INSTAGRAM) {
            instagram.setVisibility(View.GONE);
        }
        if (!Config.ENABLE_MENU_EMAIL) {
            email.setVisibility(View.GONE);
        }
    }

    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = getActivity().getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }

}