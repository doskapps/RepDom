package com.doskapps.radiosrepublicadominicana.utilities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.doskapps.radiosrepublicadominicana.Config;
import com.doskapps.radiosrepublicadominicana.models.Pais;
import com.doskapps.radiosrepublicadominicana.models.Radio;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "id9142442_id33455223_interradio";
    private static final String TABLE_NAME = "tbl_radio_favorite";
    private static final String TABLE_NAME_RECENT = "tbl_radio_recent";
    private static final String TABLE_NAME_COUNTRY = "tbl_country_selected";
    private static final String KEY_ID = "id";
    private static final String KEY_RADIOID = "radio_id";
    private static final String KEY_RADIO_NAME = "radio_name";
    private static final String KEY_RADIO_GENERE_NAME = "genere_name";
    private static final String KEY_RADIO_CATEGORY_NAME = "category_name";
    private static final String KEY_RADIO_IMAGE = "radio_image";
    private static final String KEY_RADIO_URL = "radio_url";
    private static final String KEY_COUNTRY_NAME = "name";
    private static final String KEY_LOCALE = "locale";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_RADIOID + " TEXT,"
                + KEY_RADIO_NAME + " TEXT,"
                + KEY_RADIO_CATEGORY_NAME + " TEXT,"
                + KEY_RADIO_IMAGE + " TEXT,"
                + KEY_RADIO_URL + " TEXT,"
                + KEY_RADIO_GENERE_NAME+ " TEXT,"
                + KEY_LOCALE + " TEXT"
                + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);

        String CREATE_TABLE_RECENT = "CREATE TABLE " + TABLE_NAME_RECENT + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_RADIOID + " TEXT,"
                + KEY_RADIO_NAME + " TEXT,"
                + KEY_RADIO_CATEGORY_NAME + " TEXT,"
                + KEY_RADIO_IMAGE + " TEXT,"
                + KEY_RADIO_URL + " TEXT,"
                + KEY_RADIO_GENERE_NAME+ " TEXT,"
                + KEY_LOCALE + " TEXT"
                + ")";
        db.execSQL(CREATE_TABLE_RECENT);

        String CREATE_TABLE_COUNTRY = "CREATE TABLE " + TABLE_NAME_COUNTRY + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_COUNTRY_NAME + " TEXT,"
                + KEY_LOCALE + " TEXT"
                + ")";
        db.execSQL(CREATE_TABLE_COUNTRY);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_RECENT);

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_COUNTRY);

        // Create tables again
        onCreate(db);
    }

    //Adding Record in Database

    public void AddtoFavorite(Radio pj) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_RADIOID, pj.getRadio_id());
        values.put(KEY_RADIO_NAME, pj.getRadio_name());
        values.put(KEY_RADIO_CATEGORY_NAME, pj.getCategory_name());
        values.put(KEY_RADIO_IMAGE, pj.getRadio_image());
        values.put(KEY_RADIO_URL, pj.getRadio_url());
        values.put(KEY_RADIO_GENERE_NAME, pj.getGenere_name());
        values.put(KEY_LOCALE, Constant.LOCALE);
        // Inserting Row
        db.insert(TABLE_NAME, null, values);
        db.close(); // Closing database connection

    }

    public void AddtoRecent(Radio pj) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_RADIOID, pj.getRadio_id());
        values.put(KEY_RADIO_NAME, pj.getRadio_name());
        values.put(KEY_RADIO_CATEGORY_NAME, pj.getCategory_name());
        values.put(KEY_RADIO_IMAGE, pj.getRadio_image());
        values.put(KEY_RADIO_URL, pj.getRadio_url());
        values.put(KEY_RADIO_GENERE_NAME, pj.getGenere_name());
        values.put(KEY_LOCALE, Constant.LOCALE);
        // Elimina repetidos
        db.delete(TABLE_NAME_RECENT, KEY_RADIOID+"="+pj.getRadio_id(), null);
        // Inserting Row
        db.insert(TABLE_NAME_RECENT, null, values);
        db.close(); // Closing database connection

    }

    public void AddtoCountry(Pais pj) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, 1);
        values.put(KEY_COUNTRY_NAME, pj.getNombre());
        values.put(KEY_LOCALE, pj.getLocale());
        // Inserting Row
        db.replace(TABLE_NAME_COUNTRY, null, values);
        db.close(); // Closing database connection

    }

    // Getting All Data
    public List<Radio> getAllData() {
        List<Radio> dataList = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE locale='"+ Constant.LOCALE +"' ORDER BY id DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Radio contact = new Radio();
                contact.setId(Integer.parseInt(cursor.getString(0)));
                contact.setRadio_id(cursor.getString(1));
                contact.setRadio_name(cursor.getString(2));
                contact.setCategory_name(cursor.getString(3));
                contact.setRadio_image(cursor.getString(4));
                contact.setRadio_url(cursor.getString(5));
                contact.setGenere_name(cursor.getString(6));

                // Adding contact to list
                dataList.add(contact);
            } while (cursor.moveToNext());
            cursor.close();
        }

        // return contact list
        db.close();
        return dataList;
    }

    // Getting All Data
    public List<Radio> getAllDataRecent() {
        List<Radio> dataList = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT DISTINCT "+KEY_RADIOID+ ", " +
                                                KEY_RADIO_NAME+ ", " +
                                                KEY_RADIO_CATEGORY_NAME+ ", " +
                                                KEY_RADIO_IMAGE+ ", " +
                                                KEY_RADIO_URL+ ", " +
                                                KEY_RADIO_GENERE_NAME+ ", " +
                                                KEY_ID+ " " +
                             " FROM " + TABLE_NAME_RECENT + " WHERE locale='"+ Constant.LOCALE +"' ORDER BY id DESC LIMIT " + Config.LOAD_MORE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Radio radio = new Radio();
                radio.setId(Integer.parseInt(cursor.getString(6)));
                radio.setRadio_id(cursor.getString(0));
                radio.setRadio_name(cursor.getString(1));
                radio.setCategory_name(cursor.getString(2));
                radio.setRadio_image(cursor.getString(3));
                radio.setRadio_url(cursor.getString(4));
                radio.setGenere_name(cursor.getString(5));

                // Adding contact to list
                dataList.add(radio);
            } while (cursor.moveToNext());
            cursor.close();
        }

        // return radio recent list
        db.close();
        return dataList;
    }

    //getting single row
    public List<Radio> getFavRow(String id) {
        List<Radio> dataList = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE locale='" + Constant.LOCALE + "' AND radio_id=" + id;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Radio contact = new Radio();

                contact.setId(Integer.parseInt(cursor.getString(0)));
                contact.setRadio_id(cursor.getString(1));
                contact.setRadio_name(cursor.getString(2));
                contact.setCategory_name(cursor.getString(3));
                contact.setRadio_image(cursor.getString(4));
                contact.setRadio_url(cursor.getString(5));
                contact.setGenere_name(cursor.getString(6));

                // Adding contact to list
                dataList.add(contact);
            } while (cursor.moveToNext());
            cursor.close();
        }
        // return contact list
        db.close();
        return dataList;
    }

    //getting single row
    public List<Pais> getCountry() {
        List<Pais> dataList = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_NAME_COUNTRY + "";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Pais pais = new Pais();

                pais.setNombre(cursor.getString(1));
                pais.setLocale(cursor.getString(2));

                // Adding contact to list
                dataList.add(pais);
            } while (cursor.moveToNext());
            cursor.close();
        }
        // return country list
        db.close();
        return dataList;
    }

    //for remove favorite

    public void RemoveFav(Radio contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, KEY_RADIOID + " = ?",
                new String[]{String.valueOf(contact.getRadio_id())});
        db.close();
    }

    public enum DatabaseManager {
        INSTANCE;
        private SQLiteDatabase db;
        private boolean isDbClosed = true;
        DatabaseHandler dbHelper;

        public void init(Context context) {
            dbHelper = new DatabaseHandler(context);
            if (isDbClosed) {
                isDbClosed = false;
                this.db = dbHelper.getWritableDatabase();
            }

        }

        public boolean isDatabaseClosed() {
            return isDbClosed;
        }

        public void closeDatabase() {
            if (!isDbClosed && db != null) {
                isDbClosed = true;
                db.close();
                dbHelper.close();
            }
        }
    }
}
