package com.doskapps.radiosrepublicadominicana.tab;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.doskapps.radiosrepublicadominicana.Config;
import com.doskapps.radiosrepublicadominicana.R;
import com.doskapps.radiosrepublicadominicana.activities.MainActivity;
import com.doskapps.radiosrepublicadominicana.fragments.FragmentCategory;
import com.doskapps.radiosrepublicadominicana.fragments.FragmentRadio;
import com.doskapps.radiosrepublicadominicana.utilities.AppBarLayoutBehavior;
import com.doskapps.radiosrepublicadominicana.utilities.Constant;


public class FragmentTabHome extends Fragment {

    private MainActivity mainActivity;
    private Toolbar toolbar;
    public static TabLayout tabLayout;
    public static ViewPager viewPager;
    public static int tab_count = 2;

    public FragmentTabHome() {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_layout, container, false);

        AppBarLayout appBarLayout = view.findViewById(R.id.tab_appbar_layout);
        ((CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams()).setBehavior(new AppBarLayoutBehavior());

        tabLayout = view.findViewById(R.id.tabs);
        viewPager = view.findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(tab_count);

        toolbar = view.findViewById(R.id.toolbar);
        setupToolbar();

        viewPager.setAdapter(new MyAdapter(getChildFragmentManager()));

        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                tabLayout.setupWithViewPager(viewPager);
            }
        });

        if (Config.ENABLE_RTL_MODE) {
            viewPager.setRotationY(180);
        }

        return view;

    }

    class MyAdapter extends FragmentPagerAdapter {

        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 1:
                    return new FragmentRadio();
                case 0:
                    return new FragmentCategory();
            }
            return null;
        }

        @Override
        public int getCount() {
            return tab_count;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            switch (position) {
                case 1:
                    return getResources().getString(R.string.tab_recent);
                case 0:
                    return getResources().getString(R.string.tab_category);
            }

            return null;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainActivity.setupNavigationDrawer(toolbar);
    }

    private void setupToolbar() {
        toolbar.setTitle(getString(R.string.app_name) + " " + Constant.PAIS);
        mainActivity.setSupportActionBar(toolbar);
    }

}