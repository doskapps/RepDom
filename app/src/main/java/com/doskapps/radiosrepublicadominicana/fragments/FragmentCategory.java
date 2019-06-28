package com.doskapps.radiosrepublicadominicana.fragments;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.doskapps.radiosrepublicadominicana.Config;
import com.doskapps.radiosrepublicadominicana.R;
import com.doskapps.radiosrepublicadominicana.activities.ActivityCategoryDetails;
import com.doskapps.radiosrepublicadominicana.adapters.AdapterCategory;
import com.doskapps.radiosrepublicadominicana.callbacks.CallbackCategory;
import com.doskapps.radiosrepublicadominicana.models.Category;
import com.doskapps.radiosrepublicadominicana.rests.ApiInterface;
import com.doskapps.radiosrepublicadominicana.rests.RestAdapter;
import com.doskapps.radiosrepublicadominicana.utilities.Constant;
import com.doskapps.radiosrepublicadominicana.utilities.GDPR;
import com.doskapps.radiosrepublicadominicana.utilities.GridSpacingItemDecoration;
import com.doskapps.radiosrepublicadominicana.utilities.NetworkCheck;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentCategory extends Fragment {

    private View root_view, parent_view;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private AdapterCategory adapterCategory;
    public static final String EXTRA_OBJC = "key.EXTRA_OBJC";
    private Call<CallbackCategory> callbackCall = null;
    private InterstitialAd interstitialAd;
    int counter = 1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_category, null);
        parent_view = getActivity().findViewById(R.id.main_content);

        loadInterstitialAd();

        swipeRefreshLayout = root_view.findViewById(R.id.swipe_refresh_layout_category);
        swipeRefreshLayout.setColorSchemeResources(R.color.orange, R.color.green, R.color.blue);

        recyclerView = root_view.findViewById(R.id.recyclerViewCategory);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), Config.CATEGORY_COLUMN_COUNT));
        //recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(Config.CATEGORY_COLUMN_COUNT,12, true));

        //set data and list adapter
        adapterCategory = new AdapterCategory(getActivity(), new ArrayList<Category>());
        recyclerView.setAdapter(adapterCategory);

        if (Config.ENABLE_RTL_MODE) {
            recyclerView.setRotationY(180);
        }

        // on item list clicked
        adapterCategory.setOnItemClickListener(new AdapterCategory.OnItemClickListener() {
            @Override
            public void onItemClick(View v, Category obj, int position) {
                Intent intent = new Intent(getActivity(), ActivityCategoryDetails.class);
                intent.putExtra(EXTRA_OBJC, obj);
                startActivity(intent);

                showInterstitialAd();
            }
        });

        // on swipe list
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapterCategory.resetListData();
                requestAction();
            }
        });

        requestAction();

        return root_view;
    }

    private void displayApiResult(final List<Category> categories) {
        adapterCategory.setListData(categories);
        swipeProgress(false);
        if (categories.size() == 0) {
            showNoItemView(true);
        }
    }

    private void requestCategoriesApi() {
        ApiInterface apiInterface = RestAdapter.createAPI();
        callbackCall = apiInterface.getAllCategories(Constant.LOCALE);
        Log.d("Log", "GET TODAS LAS CATEGORIAS");
        callbackCall.enqueue(new Callback<CallbackCategory>() {
            @Override
            public void onResponse(Call<CallbackCategory> call, Response<CallbackCategory> response) {
                CallbackCategory resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    Log.d("Log", "GET TODAS LAS CATEGORIAS OK");
                    displayApiResult(resp.categories);
                } else {
                    Log.d("Log", "GET TODAS LAS CATEGORIAS RESP " + resp);
                    Log.d("Log", "GET TODAS LAS CATEGORIAS STATUS " + resp.status);
                    onFailRequest();
                }
            }

            @Override
            public void onFailure(Call<CallbackCategory> call, Throwable t) {
                if (!call.isCanceled()) onFailRequest();
            }

        });
    }

    private void onFailRequest() {
        swipeProgress(false);
        if (NetworkCheck.isConnect(getActivity())) {
            showFailedView(true, getString(R.string.failed_text));
        } else {
            showFailedView(true, getString(R.string.failed_text));
        }
    }

    private void requestAction() {
        showFailedView(false, "");
        swipeProgress(true);
        showNoItemView(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                requestCategoriesApi();
            }
        }, 250);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        swipeProgress(false);
        if(callbackCall != null && callbackCall.isExecuted()){
            callbackCall.cancel();
        }
    }

    private void showFailedView(boolean flag, String message) {
        View lyt_failed = root_view.findViewById(R.id.lyt_failed_category);
        ((TextView) root_view.findViewById(R.id.failed_message)).setText(message);
        if (flag) {
            recyclerView.setVisibility(View.GONE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
        root_view.findViewById(R.id.failed_retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestAction();
            }
        });
    }

    private void showNoItemView(boolean show) {
        View lyt_no_item = root_view.findViewById(R.id.lyt_no_item_category);
        ((TextView) root_view.findViewById(R.id.no_item_message)).setText(R.string.no_category_found);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_no_item.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_no_item.setVisibility(View.GONE);
        }
    }

    private void swipeProgress(final boolean show) {
        if (!show) {
            swipeRefreshLayout.setRefreshing(show);
            return;
        }
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(show);
            }
        });
    }

    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    private void loadInterstitialAd() {
        if (Config.ENABLE_ADMOB_INTERSTITIAL_ADS_ON_CATEGORY) {
            interstitialAd = new InterstitialAd(getActivity());
            interstitialAd.setAdUnitId(getResources().getString(R.string.admob_interstitial_unit_id));
            AdRequest adRequest = new AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter.class, GDPR.getBundleAd(getActivity())).build();
            interstitialAd.loadAd(adRequest);
            interstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    interstitialAd.loadAd(new AdRequest.Builder().build());
                }
            });
        } else {
            Log.d("AdMob", "AdMob Interstitial is Disabled");
        }
    }

    private void showInterstitialAd() {
        if (Config.ENABLE_ADMOB_INTERSTITIAL_ADS_ON_CATEGORY) {
            if (interstitialAd != null && interstitialAd.isLoaded()) {

                if (counter == Config.ADMOB_INTERSTITIAL_ADS_INTERVAL) {
                    interstitialAd.show();
                    counter = 1;
                } else {
                    counter++;
                }
            }
        } else {
            Log.d("AdMob", "AdMob Interstitial is Disabled");
        }
    }

}
