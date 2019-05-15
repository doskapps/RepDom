package com.doskapps.interradio.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.doskapps.interradio.Config;
import com.doskapps.interradio.R;
import com.doskapps.interradio.activities.ActivityCategoryDetails;
import com.doskapps.interradio.activities.ActivitySearch;
import com.doskapps.interradio.activities.MainActivity;
import com.doskapps.interradio.models.Radio;
import com.doskapps.interradio.utilities.Constant;
import com.doskapps.interradio.utilities.OkHttpDataSource;
import com.devbrackets.android.exomedia.EMAudioPlayer;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecSelector;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.upstream.Allocator;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;
import com.google.android.exoplayer.util.Util;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import okhttp3.CacheControl;
import okhttp3.OkHttpClient;

public class RadioPlayerService extends Service {

    public static final String NOTIFICATION_CHANNEL_ID = "radio_channel";
    private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
    private static final int BUFFER_SEGMENT_COUNT = 256;
    private static final int NOTIFICATION_ID = 1;
    private static RadioPlayerService service;
    private static Context context;
    private static ExoPlayer exoPlayer;
    private static EMAudioPlayer emAudioPlayer;
    private static Radio station;
    static public int list;
    private WifiLock wifiLock;
    static ProgressDialog dialog;
    static ProgressTask task;
    private static int inwhich;
    private TelephonyManager telephonyManager;
    private boolean onGoingCall = false;

    static public void initialize(Context context, Radio station, int inwhich) {
        RadioPlayerService.context = context;
        RadioPlayerService.station = station;
        RadioPlayerService.inwhich = inwhich;
        Log.e("inwhich", "" + inwhich);
    }

    static public void instance(Context context, int inwhich) {
        RadioPlayerService.context = context;
        RadioPlayerService.inwhich = inwhich;
    }

    static public RadioPlayerService getInstance() {
        if (service == null) {
            service = new RadioPlayerService();
        }
        return service;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private PhoneStateListener phoneStateListener = new PhoneStateListener() {

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {

            if (state == TelephonyManager.CALL_STATE_OFFHOOK || state == TelephonyManager.CALL_STATE_RINGING) {
                if (!isPlaying()) return;
                onGoingCall = true;
                onStopPlayer();

            } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                if (!onGoingCall) return;
                onGoingCall = false;
                onPlayPlayer();
            }
        }

    };

    @Override
    public void onCreate() {
        exoPlayer = ExoPlayer.Factory.newInstance(1);
        emAudioPlayer = new EMAudioPlayer(context);
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        task = new ProgressTask();
        task.execute();
        return START_NOT_STICKY;
    }

    public void onDestroy() {
        stop();
        if (telephonyManager != null)
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        Log.e("Destroyed", "Called");
    }

    public void onStop() {
        if (dialog != null && dialog.isShowing()) {
            task.cancel(true);
        }
    }

    public void stop() {
        if (exoPlayer != null && exoPlayer.getPlayWhenReady()) {
            exoPlayer.stop();
            exoPlayer.seekTo(0);
            exoPlayer.release();
            exoPlayer = null;
            wifiLock.release();
            stopForeground(true);
        } else if (emAudioPlayer != null && emAudioPlayer.isPlaying()) {
            emAudioPlayer.stopPlayback();
            emAudioPlayer.seekTo(0);
            emAudioPlayer = null;
            wifiLock.release();
            stopForeground(true);
        }
    }

    public static void onStopPlayer() {
        if (station.radio_url.endsWith(".m3u8")) {
            emAudioPlayer.pause();
        } else {
            exoPlayer.setPlayWhenReady(false);
        }
    }

    public static void onPlayPlayer() {
        if (station.radio_url.endsWith(".m3u8")) {
            emAudioPlayer.start();
        } else {
            exoPlayer.setPlayWhenReady(true);
        }
    }

    public boolean isPlaying() {
        return (exoPlayer != null && exoPlayer.getPlayWhenReady() || emAudioPlayer != null && emAudioPlayer.isPlaying());
    }

    private class ProgressTask extends AsyncTask<String, Void, Boolean> {

        public ProgressTask() {
            dialog = new ProgressDialog(context);
        }

        protected void onPreExecute() {
            dialog.setMessage(context.getString(R.string.radio_connection) + " " + station.radio_name + "...");
            dialog.setCancelable(false);
            dialog.show();
            dialog.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    RadioPlayerService.ProgressTask.this.cancel(true);
                    RadioPlayerService.this.stopSelf();
                }
            });
        }

        protected Boolean doInBackground(final String... args) {

            try {

                if (station.radio_url.endsWith(".m3u8")) {

                    String url = station.radio_url;
                    emAudioPlayer.setDataSource(context, Uri.parse(url));
                    emAudioPlayer.prepareAsync();

                } else {

                    Uri uri;
                    uri = Uri.parse(station.radio_url);

                    Allocator allocator = new DefaultAllocator(BUFFER_SEGMENT_SIZE);
                    String userAgent = Util.getUserAgent(context, "ExoPlayerDemo");
                    OkHttpClient okHttpClient = new OkHttpClient();

                    DataSource dataSource = new DefaultUriDataSource(context, null,
                            new OkHttpDataSource(okHttpClient, userAgent, null, null, CacheControl.FORCE_NETWORK));

                    ExtractorSampleSource sampleSource = new ExtractorSampleSource(uri, dataSource, allocator,
                            BUFFER_SEGMENT_COUNT * BUFFER_SEGMENT_SIZE);

                    MediaCodecAudioTrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource,
                            MediaCodecSelector.DEFAULT, null, true, null, null,
                            AudioCapabilities.getCapabilities(context), AudioManager.STREAM_MUSIC);

                    exoPlayer.prepare(audioRenderer);

                }
                return true;

            } catch (IllegalArgumentException e1) {
            } catch (SecurityException e1) {
            } catch (IllegalStateException e1) {
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {

            dialog.dismiss();
            if (success) {
                wifiLock = ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, "RadiophonyLock");
                wifiLock.acquire();

                if (station.radio_url.endsWith(".m3u8")) {
                    emAudioPlayer.start();
                } else {
                    exoPlayer.setPlayWhenReady(true);
                }

                if (RadioPlayerService.inwhich == 2) {
                    ((ActivityCategoryDetails) context).notifyShowBar();
                } else if (RadioPlayerService.inwhich == 3) {
                    ((ActivitySearch) context).notifyShowBar();
                } else {
                    ((MainActivity) context).notifyShowBar();
                }

                startNotify();

            } else {
                Toast.makeText(context, context.getString(R.string.internet_disabled), Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void startNotify() {

        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            /* Create or update. */
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_LOW);
            channel.enableVibration(false);
            notificationManager.createNotificationChannel(channel);
        }

        Bundle bundle = new Bundle();
        bundle.putInt("list", list);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(getApplicationContext(), MainActivity.class).putExtras(bundle),
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);

        builder.setContentTitle(getResources().getString(R.string.app_name)).setContentText(station.radio_name)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_radio_notif)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVibrate(new long[]{0L})
                .setWhen(System.currentTimeMillis())
                .setOngoing(true).setContentIntent(pendingIntent).build();

        Picasso.with(context)
                .load(Config.ADMIN_PANEL_URL + "/upload/" + Constant.LOCALE + "/" + station.radio_image)
                .resize(250, 250)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(final Bitmap bitmap, final Picasso.LoadedFrom from) {
                        builder.setLargeIcon(bitmap);
                        notificationManager.notify(NOTIFICATION_ID, builder.build());
                    }

                    @Override
                    public void onBitmapFailed(final Drawable errorDrawable) {
                        // Do nothing
                    }

                    @Override
                    public void onPrepareLoad(final Drawable placeHolderDrawable) {
                        // Do nothing
                    }
                });

        final Notification notification = builder.build();
        startForeground(NOTIFICATION_ID, notification);

    }

    public Radio getPlayingRadioStation() {
        return station;
    }
}
