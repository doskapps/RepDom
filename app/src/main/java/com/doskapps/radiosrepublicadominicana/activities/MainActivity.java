package com.doskapps.radiosrepublicadominicana.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.doskapps.radiosrepublicadominicana.BuildConfig;
import com.doskapps.radiosrepublicadominicana.Config;
import com.doskapps.radiosrepublicadominicana.R;
import com.doskapps.radiosrepublicadominicana.adapters.AdapterPaises;
import com.doskapps.radiosrepublicadominicana.fragments.FragmentAbout;
import com.doskapps.radiosrepublicadominicana.models.Pais;
import com.doskapps.radiosrepublicadominicana.models.Radio;
import com.doskapps.radiosrepublicadominicana.services.RadioPlayerService;
import com.doskapps.radiosrepublicadominicana.tab.FragmentTabFavorite;
import com.doskapps.radiosrepublicadominicana.tab.FragmentTabHome;
import com.doskapps.radiosrepublicadominicana.utilities.Constant;
import com.doskapps.radiosrepublicadominicana.utilities.DatabaseHandler;
import com.doskapps.radiosrepublicadominicana.utilities.GDPR;
import com.doskapps.radiosrepublicadominicana.utilities.HttpTask;
import com.doskapps.radiosrepublicadominicana.utilities.NetworkCheck;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.squareup.picasso.Picasso;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;

import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, AdapterView.OnItemSelectedListener {

    private final static String COLLAPSING_TOOLBAR_FRAGMENT_TAG = "collapsing_toolbar";
    private final static String SELECTED_TAG = "selected_index";
    private String pais = "";
    private static int selectedIndex;
    private final static int COLLAPSING_TOOLBAR = 0;
    ActionBarDrawerToggle actionBarDrawerToggle;
    private DrawerLayout drawerLayout;
    NavigationView navigationView;
    TextView txt_radio_name;
    ImageView img_logo;
    ImageButton btn_pause, btn_play, btn_close, btn_favorite, btn_no_favorite;
    LinearLayout relativeLayout;
    private AdView adView;
    private InterstitialAd interstitialAd;
    View view;
    SeekBar seekbar;
    private ImageView img_volume;
    AudioManager audioManager;
    RelativeLayout lyt_volumeBar;
    SharedPreferences preferences;
    Context context;

    private AdapterPaises adapterPaises;
    private ArrayList<String> listPaises = null;
    private Pais paisSeleccionado = null;
    private DatabaseHandler databaseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        view = findViewById(android.R.id.content);

        if (Config.ENABLE_RTL_MODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
        }

        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);
        drawerLayout = findViewById(R.id.drawer_layout);
        lyt_volumeBar = findViewById(R.id.lyt_volumeBar);

        loadInterstitialAd();
        loadBannerAd();

        btn_pause = findViewById(R.id.main_pause);
        btn_play = findViewById(R.id.main_play);
        btn_favorite = findViewById(R.id.main_favorite);
        btn_no_favorite = findViewById(R.id.main_no_favorite);
        btn_close = findViewById(R.id.mainClose);
        relativeLayout = findViewById(R.id.main_bar);

        if (savedInstanceState != null) {
            navigationView.getMenu().getItem(savedInstanceState.getInt(SELECTED_TAG)).setChecked(true);
            return;
        }

        selectedIndex = COLLAPSING_TOOLBAR;
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, new FragmentTabHome(), COLLAPSING_TOOLBAR_FRAGMENT_TAG)
                .commit();

        if (Config.ENABLE_VOLUME_BAR) {
            volumeBar();
        } else {
            lyt_volumeBar.setVisibility(View.GONE);
        }

        sendRegistrationIdToBackend();

        Intent intent = getIntent();
        final String link = intent.getStringExtra("link");

        if (link != null) {
            if (!link.equals("")) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(link));
                startActivity(i);
            }
        }

        GDPR.updateConsentStatus(this);

        if (!NetworkCheck.isConnect(this)) {
                //Create an alert dialog
                AlertDialog.Builder Checkbuilder = new  AlertDialog.Builder(this);
                Checkbuilder.setTitle(getString(R.string.failed_title_text));
                Checkbuilder.setCancelable(false);
                Checkbuilder.setMessage(getString(R.string.failed_text));
                //Builder Retry Button

                Checkbuilder.setPositiveButton(getString(R.string.retry), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //Restart The Activity
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);

                    }
                });

                Checkbuilder.setNegativeButton(getString(R.string.quit), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }) ;

                AlertDialog alert=Checkbuilder.create();
                alert.show();

        } else {

            // Se obtienen los paises disponibles
            adapterPaises = new AdapterPaises();

            // Se obtiene le pais de la red. Puede no funcionar en dispositivos sin tarjeta SIM
            //TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
            pais = "DO"; // tm.getNetworkCountryIso().toUpperCase();

            if (pais == null || pais.trim().length() == 0) {
                // Se obtiene el pais de la configuracion regional del dispositivo
                pais = getCurrentLocale(this).getCountry().toUpperCase();
            }

            // Verificar en tabla local de pais seleccionado si anteriormente se selecciono uno
            // Si existe la tabla y el pais seleccionado es distinto al pais detectado, preguntar si se
            // desea utlizar el pais que se selecciono
            databaseHandler = new DatabaseHandler(this);
            final List<Pais> lPais = databaseHandler.getCountry();
            if (lPais.size() > 0 && (lPais.get(0).getLocale().compareToIgnoreCase(pais) != 0)) {
                Constant.LOCALE = lPais.get(0).getLocale();
                Constant.PAIS = lPais.get(0).getNombre();

                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle(R.string.alert_country_title);
                alertDialog.setMessage(String.format(getString(R.string.alert_country_message), lPais.get(0).getNombre()));
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.alert_country_ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Constant.LOCALE = lPais.get(0).getLocale();
                                Constant.PAIS = lPais.get(0).getNombre();

                                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentTabHome(), COLLAPSING_TOOLBAR_FRAGMENT_TAG).commit();

                                dialog.dismiss();
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.alert_country_cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Constant.LOCALE = pais;
                                Constant.PAIS = getCountryName(pais);

                                Pais p = new Pais();

                                p.setNombre(Constant.PAIS);
                                p.setLocale(Constant.LOCALE);

                                databaseHandler.AddtoCountry(p);

                                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentTabHome(), COLLAPSING_TOOLBAR_FRAGMENT_TAG).commit();

                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            } else {
                Constant.LOCALE = pais;
                Constant.PAIS = getCountryName(pais);
            }
        }
    }

    // Obtiene el pais de la Configuracion Regional
    // Android N (Api level 24) update
    private Locale getCurrentLocale(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            return context.getResources().getConfiguration().getLocales().get(0);
        } else{
            //noinspection deprecation
            return context.getResources().getConfiguration().locale;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_TAG, selectedIndex);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.search:
                Intent intent = new Intent(getApplicationContext(), ActivitySearch.class);
                startActivity(intent);
                break;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        switch (menuItem.getItemId()) {
            case R.id.drawer_recent:
                if (!menuItem.isChecked()) {
                    menuItem.setChecked(true);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentTabHome(), COLLAPSING_TOOLBAR_FRAGMENT_TAG).commit();
                    showInterstitialAd();
                }
                drawerLayout.closeDrawer(GravityCompat.START);

                return true;

            case R.id.drawer_favorite:

                if (!menuItem.isChecked()) {
                    menuItem.setChecked(true);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentTabFavorite(), COLLAPSING_TOOLBAR_FRAGMENT_TAG).commit();
                    showInterstitialAd();
                }
                drawerLayout.closeDrawer(GravityCompat.START);

                return true;

            /*
            case R.id.drawer_locale:

                navigationView.getMenu().findItem(R.id.drawer_recent).setChecked(true);

                LayoutInflater inflater = getLayoutInflater();
                View alertLayout = inflater.inflate(R.layout.layout_paises, null);

                final Spinner spinPaises = alertLayout.findViewById(R.id.spinner);

                listPaises = adapterPaises.getNombrePaises();
                ArrayAdapter<String> comboAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_selected, listPaises);
                comboAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

                //Cargo el spinner con los datos
                spinPaises.setAdapter(comboAdapter);
                spinPaises.setOnItemSelectedListener(this);

                // Selecciona el pais actual
                List<Pais> lPaises = adapterPaises.getPaises();
                for (Pais p: lPaises) {
                    if (p.getLocale().compareToIgnoreCase(Constant.LOCALE) == 0) {
                        int pos = ((ArrayAdapter<String>) spinPaises.getAdapter()).getPosition(p.getNombre());
                        spinPaises.setSelection(pos);
                        break;
                    }
                }

                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle(R.string.select_country_title);
                // this is set the view from XML inside AlertDialog
                alert.setView(alertLayout);
                // disallow cancel of AlertDialog on click of back button and outside touch
                alert.setCancelable(false);
                alert.setNegativeButton(R.string.select_country_cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentTabHome(), COLLAPSING_TOOLBAR_FRAGMENT_TAG).commit();
                    }
                });

                alert.setPositiveButton(R.string.select_country_ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Guardar en base de datos local el nuevo pais
                        Pais p = new Pais();

                        p.setNombre(paisSeleccionado.getNombre());
                        p.setLocale(paisSeleccionado.getLocale());

                        if (Constant.LOCALE.compareToIgnoreCase(paisSeleccionado.getLocale()) == 0) {
                            return;
                        }

                        databaseHandler.AddtoCountry(p);

                        Constant.LOCALE = paisSeleccionado.getLocale();
                        Constant.PAIS = getCountryName(Constant.LOCALE);

                        Toast.makeText(getBaseContext(), String.format(getString(R.string.select_country_success), paisSeleccionado.getNombre()), Toast.LENGTH_SHORT).show();

                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentTabHome(), COLLAPSING_TOOLBAR_FRAGMENT_TAG).commit();
                    }
                });
                AlertDialog dialog = alert.create();
                dialog.show();

                drawerLayout.closeDrawer(GravityCompat.START);
                return false;
            */
            case R.id.drawer_rate:

                final String appName = getPackageName();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appName)));
                }
                showInterstitialAd();

                return true;

            case R.id.drawer_more:

                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.play_more_apps))));
                showInterstitialAd();

                return true;

            case R.id.drawer_about:
                if (!menuItem.isChecked()) {
                    menuItem.setChecked(true);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new FragmentAbout(), COLLAPSING_TOOLBAR_FRAGMENT_TAG).commit();

                    showInterstitialAd();
                }
                drawerLayout.closeDrawer(GravityCompat.START);

                return true;

        }
        return false;
    }

    public void setupNavigationDrawer(Toolbar toolbar) {
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    public void volumeBar() {

        seekbar = findViewById(R.id.seekBar1);

        lyt_volumeBar.setVisibility(View.VISIBLE);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, Config.DEFAULT_VOLUME, 0);

        seekbar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        seekbar.setProgress(Config.DEFAULT_VOLUME);
        seekbar.setMax(15);

        img_volume = findViewById(R.id.ic_volume);
        img_volume.setImageResource(R.drawable.ic_volume);

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {

                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);

                if (progress == 0) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.volume_off), Toast.LENGTH_SHORT).show();
                    img_volume.setImageResource(R.drawable.ic_volume_off);
                } else if (progress == 15) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.volume_max), Toast.LENGTH_SHORT).show();
                    img_volume.setImageResource(R.drawable.ic_volume);
                } else {
                    img_volume.setImageResource(R.drawable.ic_volume);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    public void notifyShowBar() {

        Radio station = RadioPlayerService.getInstance().getPlayingRadioStation();

        img_logo = findViewById(R.id.main_bar_logo);
        txt_radio_name = findViewById(R.id.main_bar_station);
        btn_pause.setOnClickListener(this);
        btn_play.setOnClickListener(this);
        btn_close.setOnClickListener(this);
        btn_favorite.setOnClickListener(this);
        btn_no_favorite.setOnClickListener(this);

        Picasso
                .with(this)
                .load(Config.ADMIN_PANEL_URL + "/upload/" + Constant.LOCALE + "/" + station.radio_image)
                .placeholder(R.mipmap.ic_launcher)
                .into(img_logo);

        txt_radio_name.setText(station.radio_name);
        relativeLayout.setVisibility(View.VISIBLE);

        // Vuelvo a actualizar el estado de los botones
        btn_pause.setVisibility(View.VISIBLE);
        btn_play.setVisibility(View.GONE);

        if (databaseHandler == null) databaseHandler = new DatabaseHandler(getApplicationContext());

        if (databaseHandler.getFavRow(station.radio_id) != null && databaseHandler.getFavRow(station.radio_id).size() != 0) {
            btn_no_favorite.setVisibility(View.GONE);
            btn_favorite.setVisibility(View.VISIBLE);
        } else {
            btn_favorite.setVisibility(View.GONE);
            btn_no_favorite.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        Radio obj = RadioPlayerService.getInstance().getPlayingRadioStation();

        switch (v.getId()) {
            case R.id.main_pause:
                play(false);
                btn_pause.setVisibility(View.GONE);
                btn_play.setVisibility(View.VISIBLE);
                break;

            case R.id.main_play:
                play(true);
                RadioPlayerService.instance(MainActivity.this, 0);
                btn_pause.setVisibility(View.VISIBLE);
                btn_play.setVisibility(View.GONE);

                break;

            case R.id.mainClose:
                play(false);
                relativeLayout.setVisibility(View.GONE);
                break;

            case R.id.main_no_favorite:
                databaseHandler.AddtoFavorite(new Radio(obj.radio_id, obj.radio_name, obj.genere_name, obj.category_name, obj.radio_image, obj.radio_url));
                Toast.makeText(getApplicationContext(), getString(R.string.favorite_added), Toast.LENGTH_SHORT).show();

                btn_no_favorite.setVisibility(View.GONE);
                btn_favorite.setVisibility(View.VISIBLE);
                break;

            case R.id.main_favorite:
                databaseHandler.RemoveFav(new Radio(obj.radio_id));
                Toast.makeText(getApplicationContext(), getString(R.string.favorite_removed), Toast.LENGTH_SHORT).show();

                btn_favorite.setVisibility(View.GONE);
                btn_no_favorite.setVisibility(View.VISIBLE);

                break;

            default:
                break;
        }
    }

    public void play(boolean toPlay) {
        if (!toPlay) {
            stopService(new Intent(MainActivity.this, RadioPlayerService.class));
            btn_pause.setVisibility(View.GONE);
            btn_play.setVisibility(View.VISIBLE);

        } else {
            startService(new Intent(MainActivity.this, RadioPlayerService.class));
            btn_pause.setVisibility(View.VISIBLE);
            btn_play.setVisibility(View.GONE);

            // Verifico si la radio es favorita
            Radio radio = RadioPlayerService.getInstance().getPlayingRadioStation();

            if (databaseHandler.getFavRow(radio.radio_id) != null && databaseHandler.getFavRow(radio.radio_id).size() != 0) {
                btn_no_favorite.setVisibility(View.GONE);
                btn_favorite.setVisibility(View.VISIBLE);
            } else {
                btn_favorite.setVisibility(View.GONE);
                btn_no_favorite.setVisibility(View.VISIBLE);
            }

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            exitDialog();
        }
    }

    public void exitDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setIcon(R.mipmap.ic_launcher);
        dialog.setTitle(R.string.app_name);
        dialog.setMessage(getResources().getString(R.string.message));
        dialog.setPositiveButton(getResources().getString(R.string.quit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.this.finish();
                stopService(new Intent(MainActivity.this, RadioPlayerService.class));
            }
        });

        dialog.setNegativeButton(getResources().getString(R.string.minimize), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                minimizeApp();
            }
        });

        dialog.setNeutralButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        dialog.show();
    }

    public void minimizeApp() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void loadBannerAd() {
        if (Config.ENABLE_ADMOB_BANNER_ADS) {
            adView = findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter.class, GDPR.getBundleAd(MainActivity.this)).build();
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

    private void loadInterstitialAd() {
        Log.d("TAG", "showAd");
        interstitialAd = new InterstitialAd(MainActivity.this);
        interstitialAd.setAdUnitId(getResources().getString(R.string.admob_interstitial_unit_id));
        AdRequest adRequest = new AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter.class, GDPR.getBundleAd(MainActivity.this)).build();
        interstitialAd.loadAd(adRequest);
    }


    private void showInterstitialAd() {
        if (Config.ENABLE_ADMOB_INTERSTITIAL_ADS_ON_DRAWER_MENU) {
            if (interstitialAd.isLoaded()) {
                interstitialAd.show();
            }
        } else {
            Log.d("Log", "Interstitial Ad is Disabled!");
        }
    }

    private void sendRegistrationIdToBackend() {

        Log.d("INFO", "Start update data to server...");

        String token = preferences.getString("fcm_token", null);
        String appVersion = BuildConfig.VERSION_CODE + " (" + BuildConfig.VERSION_NAME + ")";
        String osVersion = currentVersion() + " " + Build.VERSION.RELEASE;
        String model = android.os.Build.MODEL;
        String manufacturer = android.os.Build.MANUFACTURER;

        // Register FCM Token ID to server
        if (token != null) {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
            nameValuePairs.add(new BasicNameValuePair("user_android_token", token));
            nameValuePairs.add(new BasicNameValuePair("user_app_version", appVersion));
            nameValuePairs.add(new BasicNameValuePair("user_os_version", osVersion));
            nameValuePairs.add(new BasicNameValuePair("user_device_model", model));
            nameValuePairs.add(new BasicNameValuePair("user_device_manufacturer", manufacturer));
            new HttpTask(null, MainActivity.this, Config.ADMIN_PANEL_URL + "/register.php", nameValuePairs, false).execute();
        }

    }

    public static String currentVersion() {
        double release = Double.parseDouble(Build.VERSION.RELEASE.replaceAll("(\\d+[.]\\d+)(.*)", "$1"));
        String codeName = "Unsupported";//below Jelly bean OR above Oreo
        if (release >= 4.1 && release < 4.4) codeName = "Jelly Bean";
        else if (release < 5) codeName = "Kit Kat";
        else if (release < 6) codeName = "Lollipop";
        else if (release < 7) codeName = "Marshmallow";
        else if (release < 8) codeName = "Nougat";
        else if (release < 9) codeName = "Oreo";
        return codeName;
    }

    private String getCountryName(String code) {
        Locale loc = new Locale("", code);

        return loc.getDisplayCountry();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //Almaceno el nombre del pais seleccionado
        paisSeleccionado = adapterPaises.getPaises().get(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

}
