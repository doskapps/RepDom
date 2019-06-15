package com.doskapps.interradio.activities;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.doskapps.interradio.Config;
import com.doskapps.interradio.R;
import com.doskapps.interradio.adapters.AdapterRadio;
import com.doskapps.interradio.callbacks.CallbackCategoryDetails;
import com.doskapps.interradio.models.Category;
import com.doskapps.interradio.models.Radio;
import com.doskapps.interradio.rests.ApiInterface;
import com.doskapps.interradio.rests.RestAdapter;
import com.doskapps.interradio.services.RadioPlayerService;
import com.doskapps.interradio.utilities.AppBarLayoutBehavior;
import com.doskapps.interradio.utilities.Constant;
import com.doskapps.interradio.utilities.DatabaseHandler;
import com.doskapps.interradio.utilities.GDPR;
import com.doskapps.interradio.utilities.NetworkCheck;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityCategoryDetails extends AppCompatActivity implements View.OnClickListener {

    public static final String EXTRA_OBJC = "key.EXTRA_OBJC";
    private RecyclerView recyclerView;
    private AdapterRadio adapterRecent;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Call<CallbackCategoryDetails> callbackCall = null;
    private int post_total = 0;
    private int failed_page = 0;
    private Category category;
    View view;
    TextView txt_radio_name;
    ImageView img_logo;
    ImageButton btn_pause, btn_play, btn_close;
    LinearLayout relativeLayout;
    private DatabaseHandler databaseHandler;
    private CharSequence charSequence = null;
    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_details);
        view = findViewById(android.R.id.content);

        if (Config.ENABLE_RTL_MODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
        }

        AppBarLayout appBarLayout = findViewById(R.id.tab_appbar_layout);
        ((CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams()).setBehavior(new AppBarLayoutBehavior());

        btn_pause = findViewById(R.id.main_pause);
        btn_play = findViewById(R.id.main_play);
        btn_close = findViewById(R.id.mainClose);
        relativeLayout = findViewById(R.id.main_bar);

        category = (Category) getIntent().getSerializableExtra(EXTRA_OBJC);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.orange, R.color.green, R.color.blue);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        //set data and list adapter
        adapterRecent = new AdapterRadio(this, recyclerView, new ArrayList<Radio>());
        recyclerView.setAdapter(adapterRecent);

        // on item list clicked
        adapterRecent.setOnItemClickListener(new AdapterRadio.OnItemClickListener() {
            @Override
            public void onItemClick(View v, Radio obj, int position) {
                if (RadioPlayerService.getInstance().isPlaying()) {
                    Radio play = RadioPlayerService.getInstance().getPlayingRadioStation();
                    String playingRadioName = play.radio_name;

                    if (obj.radio_name.equals(playingRadioName)) {
                        ((ActivityCategoryDetails.this)).play(false);
                        Constant.IS_PLAYING = "1";
                    } else {
                        ((ActivityCategoryDetails.this)).play(false);
                        RadioPlayerService.initialize(ActivityCategoryDetails.this, obj, 2);
                        ((ActivityCategoryDetails.this)).play(true);
                        Constant.IS_PLAYING = "0";

                        // Guardar la radio en recientes
                        if (databaseHandler == null) databaseHandler = new DatabaseHandler(getApplicationContext());

                        databaseHandler.AddtoRecent(obj);
                    }
                } else {
                    RadioPlayerService.initialize(ActivityCategoryDetails.this, obj, 2);
                    ((ActivityCategoryDetails.this)).play(true);
                    Constant.IS_PLAYING = "0";

                    // Guardar la radio en recientes
                    if (databaseHandler == null) databaseHandler = new DatabaseHandler(getApplicationContext());

                    databaseHandler.AddtoRecent(obj);
                }
            }
        });

        adapterRecent.setOnItemOverflowClickListener(new AdapterRadio.OnItemClickListener() {
            @Override
            public void onItemClick(View v, final Radio obj, int position) {
                PopupMenu popup = new PopupMenu(getApplicationContext(), v);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.menu_popup, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_context_favorite:

                                if (charSequence.equals(getString(R.string.option_set_favorite))) {
                                    databaseHandler.AddtoFavorite(new Radio(obj.radio_id, obj.radio_name, obj.genere_name,obj.category_name, obj.radio_image, obj.radio_url));
                                    Toast.makeText(getApplicationContext(), getString(R.string.favorite_added), Toast.LENGTH_SHORT).show();

                                } else if (charSequence.equals(getString(R.string.option_unset_favorite))) {
                                    databaseHandler.RemoveFav(new Radio(obj.radio_id));
                                    Toast.makeText(getApplicationContext(), getString(R.string.favorite_removed), Toast.LENGTH_SHORT).show();
                                }
                                return true;

                            case R.id.menu_context_share:

                                String share_title = android.text.Html.fromHtml(obj.radio_name).toString();
                                String share_content = android.text.Html.fromHtml(getResources().getString(R.string.share_content)).toString();
                                Intent sendIntent = new Intent();
                                sendIntent.setAction(Intent.ACTION_SEND);
                                sendIntent.putExtra(Intent.EXTRA_TEXT, share_title + "\n\n" + share_content + "\n\n" + "https://play.google.com/store/apps/details?id=" + getPackageName());
                                sendIntent.setType("text/plain");
                                startActivity(sendIntent);
                                return true;

                            default:
                        }
                        return false;
                    }
                });
                popup.show();

                if (databaseHandler == null) databaseHandler = new DatabaseHandler(getApplicationContext());

                List<Radio> data = databaseHandler.getFavRow(obj.radio_id);
                if (data.size() == 0) {
                    popup.getMenu().findItem(R.id.menu_context_favorite).setTitle(R.string.option_set_favorite);
                    charSequence = popup.getMenu().findItem(R.id.menu_context_favorite).getTitle();
                } else {
                    if (data.get(0).getRadio_id().equals(obj.radio_id)) {
                        popup.getMenu().findItem(R.id.menu_context_favorite).setTitle(R.string.option_unset_favorite);
                        charSequence = popup.getMenu().findItem(R.id.menu_context_favorite).getTitle();
                    }
                }

            }
        });

        // detect when scroll reach bottom
        adapterRecent.setOnLoadMoreListener(new AdapterRadio.OnLoadMoreListener() {
            @Override
            public void onLoadMore(int current_page) {
                if (post_total > adapterRecent.getItemCount() && current_page != 0) {
                    int next_page = current_page + 1;
                    requestAction(next_page);
                } else {
                    adapterRecent.setLoaded();
                }
            }
        });

        // on swipe list
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (callbackCall != null && callbackCall.isExecuted()) {
                    callbackCall.cancel();
                }
                adapterRecent.resetListData();
                requestAction(1);
            }
        });

        requestAction(1);

        setupToolbar();

        loadBannerAd();

    }

    public void setupToolbar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle(category.category_name);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.search:
                Intent intent = new Intent(getApplicationContext(), ActivitySearch.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    private void displayApiResult(final List<Radio> videos) {
        adapterRecent.insertData(videos);
        swipeProgress(false);
        if (videos.size() == 0) {
            showNoItemView(true);
        }
    }

    private void requestPostApi(final int page_no) {
        ApiInterface apiInterface = RestAdapter.createAPI();
        Log.d("Log", "DETALLE DE CATEGORIAS");
        callbackCall = apiInterface.getCategoryDetailsByPage(category.cid, page_no, Config.LOAD_MORE, Constant.LOCALE);
        Log.d("Log", "DETALLE DE CATEGORIAS!!!");
        callbackCall.enqueue(new Callback<CallbackCategoryDetails>() {
            @Override
            public void onResponse(Call<CallbackCategoryDetails> call, Response<CallbackCategoryDetails> response) {
                CallbackCategoryDetails resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    post_total = resp.count_total;
                    Log.d("Log", "DETALLE DE CATEGORIAS TOTAL " + post_total);
                    displayApiResult(resp.posts);
                } else {
                    Log.d("Log", "DETALLE DE CATEGORIAS RESP " + resp);
                    Log.d("Log", "DETALLE DE CATEGORIAS STATUS " + resp.status);
                    onFailRequest(page_no);
                }
            }

            @Override
            public void onFailure(Call<CallbackCategoryDetails> call, Throwable t) {
                if (!call.isCanceled()) onFailRequest(page_no);
            }

        });
    }

    private void onFailRequest(int page_no) {
        failed_page = page_no;
        adapterRecent.setLoaded();
        swipeProgress(false);
        if (NetworkCheck.isConnect(getApplicationContext())) {
            showFailedView(true, getString(R.string.failed_text));
        } else {
            showFailedView(true, getString(R.string.failed_text));
        }
    }

    private void requestAction(final int page_no) {
        showFailedView(false, "");
        showNoItemView(false);
        if (page_no == 1) {
            swipeProgress(true);
        } else {
            adapterRecent.setLoading();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                requestPostApi(page_no);
            }
        }, 250);
    }

    private void showFailedView(boolean show, String message) {
        View view = findViewById(R.id.lyt_failed);
        ((TextView) findViewById(R.id.failed_message)).setText(message);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            view.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            view.setVisibility(View.GONE);
        }
        findViewById(R.id.failed_retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestAction(failed_page);
            }
        });
    }

    private void showNoItemView(boolean show) {
        View view = findViewById(R.id.lyt_no_item);
        ((TextView) findViewById(R.id.no_item_message)).setText(R.string.no_post_found);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            view.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            view.setVisibility(View.GONE);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        swipeProgress(false);
        if (callbackCall != null && callbackCall.isExecuted()) {
            callbackCall.cancel();
        }
    }

    public void notifyShowBar() {

        Radio station = RadioPlayerService.getInstance().getPlayingRadioStation();

        img_logo = findViewById(R.id.main_bar_logo);
        txt_radio_name = findViewById(R.id.main_bar_station);
        btn_pause.setOnClickListener(this);
        btn_play.setOnClickListener(this);
        btn_close.setOnClickListener(this);

        Picasso
                .with(this)
                .load(Config.ADMIN_PANEL_URL + "/upload/" + Constant.LOCALE + "/" + station.radio_image)
                .placeholder(R.mipmap.ic_launcher)
                .into(img_logo);

        txt_radio_name.setText(station.radio_name);
        relativeLayout.setVisibility(View.VISIBLE);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_pause:
                play(false);
                //RadioPlayerService.instance(ActivityCategoryDetails.this, 2);
                btn_pause.setVisibility(View.GONE);
                btn_play.setVisibility(View.VISIBLE);
                break;

            case R.id.main_play:
                play(true);
                RadioPlayerService.instance(ActivityCategoryDetails.this, 2);
                btn_pause.setVisibility(View.VISIBLE);
                btn_play.setVisibility(View.GONE);
                break;

            case R.id.mainClose:
                play(false);
                relativeLayout.setVisibility(View.GONE);
                break;

            default:
                break;
        }
    }

    public void play(boolean toPlay) {

        if (!toPlay) {
            stopService(new Intent(ActivityCategoryDetails.this, RadioPlayerService.class));
            relativeLayout.setVisibility(View.VISIBLE);
            btn_pause.setVisibility(View.GONE);
            btn_play.setVisibility(View.VISIBLE);

        } else {
            startService(new Intent(ActivityCategoryDetails.this, RadioPlayerService.class));
            btn_pause.setVisibility(View.VISIBLE);
            btn_play.setVisibility(View.GONE);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        RadioPlayerService.getInstance().onStop();
        super.onStop();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (RadioPlayerService.getInstance().isPlaying()) {
            notifyShowBar();
        } else {
            relativeLayout.setVisibility(View.GONE);
        }
    }

    public void loadBannerAd() {
        if (Config.ENABLE_ADMOB_BANNER_ADS) {
            adView = findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter.class, GDPR.getBundleAd(ActivityCategoryDetails.this)).build();
            adView.loadAd(adRequest);
            adView.setAdListener(new AdListener() {

                @Override
                public void onAdClosed() {
                }

                @Override
                public void onAdFailedToLoad(int error) {
                    adView.setVisibility(View.GONE);
                }

                @Override
                public void onAdLeftApplication() {
                }

                @Override
                public void onAdOpened() {
                }

                @Override
                public void onAdLoaded() {
                    adView.setVisibility(View.VISIBLE);
                }
            });
        } else {
            Log.d("Log", "Banner Ad is Disabled!");
        }
    }

}
