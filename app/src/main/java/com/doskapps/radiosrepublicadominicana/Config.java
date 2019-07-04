package com.doskapps.radiosrepublicadominicana;

public class Config {

    //put your admin panel url
    //public static final String ADMIN_PANEL_URL = "http://192.168.1.23/radios/mexico";
    //public static final String ADMIN_PANEL_URL = "http://192.168.1.19/radios/mexico";
    //public static final String ADMIN_PANEL_URL = "http://lithe-bow.000webhostapp.com/radios/internacional";
    public static final String ADMIN_PANEL_URL = "https://doskapps.com/apps/radios/interradio";

    //put your api key which obtained from admin panel
    public static final String API_KEY = "cda11lHY0ZafN2nrti4U5QAKMDhTw7Czm1xoSsyVLduvRegkqE";
    //public static final String API_KEY = "cda11pcHeVtxJ9wUMfoIY05PjNniDrKgChd2B3Fa8lkAmZ6QOz";

    //Ads Configuration
    //set true to enable or set false to disable
    public static final boolean ENABLE_ADMOB_BANNER_ADS = true;
    public static final boolean ENABLE_ADMOB_INTERSTITIAL_ADS_ON_DRAWER_MENU = false;
    public static final boolean ENABLE_ADMOB_INTERSTITIAL_ADS_ON_CATEGORY = true;
    public static final int ADMOB_INTERSTITIAL_ADS_INTERVAL = 2;

    //number of columns in a row category
    public static final int CATEGORY_COLUMN_COUNT = 3;

    //if you use RTL Language e.g : Arabic Language or other, set true
    public static final boolean ENABLE_RTL_MODE = false;

    //volume bar
    public static final boolean ENABLE_VOLUME_BAR = false;
    //set default volume to listening radio streaming, volume range 0 - 15
    public static final int DEFAULT_VOLUME = 10;

    //load more for the next radio list
    public static final int LOAD_MORE = 20;

    //social link configuration
    public static final boolean ENABLE_MENU_WEBSITE = true;
    public static final boolean ENABLE_MENU_FACEBOOK = true;
    public static final boolean ENABLE_MENU_TWITTER = true;
    public static final boolean ENABLE_MENU_INSTAGRAM = true;
    public static final boolean ENABLE_MENU_EMAIL = true;

}