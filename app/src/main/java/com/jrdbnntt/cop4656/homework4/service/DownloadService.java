package com.jrdbnntt.cop4656.homework4.service;

import android.app.DownloadManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.jrdbnntt.cop4656.homework4.R;
import com.jrdbnntt.cop4656.homework4.activity.MainActivity;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class DownloadService extends IntentService {

    private static final String MP3_URL = "https://files.freemusicarchive.org/music%2Fno_curator%2FRolemusic%2FThe_Pirate_And_The_Dancer%2FRolemusic_-_04_-_The_Pirate_And_The_Dancer.mp3";
    private static final String ACTION_DOWNLOAD_MP3 = "com.jrdbnntt.cop4656.homework4.action.DOWNLOAD_MP3";

    private static final int NOTIFICATION_ID_DOWNLOADING = 1;
    private static final int NOTIFICATION_ID_DOWNLOADED = 2;

    private static boolean isDownloading = false;

    private static DownloadManager dm;
    private BroadcastReceiver downloadReciever;

    public DownloadService() {
        super("DownloadService");
    }


    public static void startActionDownloadMp3(Context context) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(ACTION_DOWNLOAD_MP3);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_DOWNLOAD_MP3.equals(action)) {
                handleActionDownloadMp3();
            }
        }
    }

    /**
     * Handles the download action.
     * 1 Notification when started
     * 1 Notification when completed (click launches activity)
     */
    private void handleActionDownloadMp3() {
        if (!isDownloading) {
            isDownloading = true;
            downloadMp3();

        }
    }


    private void downloadMp3() {
        sendDownloadStartedNotification();

        dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(MP3_URL));
        dm.enqueue(request);
        downloadReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                sendDownloadCompleteNotification(dm.getUriForDownloadedFile(id));
                isDownloading = false;

            }
        };
        registerReceiver(downloadReciever, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        // Wait for the download to complete before closing the service
        while (isDownloading) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        unregisterReceiver(downloadReciever);
    }



    private void sendDownloadStartedNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_app_icon)
                .setAutoCancel(true)
                .setContentTitle("Music Player")
                .setContentText("MP3 Download Started");
        notificationManager.notify(NOTIFICATION_ID_DOWNLOADING, builder.build());
    }

    private void sendDownloadCompleteNotification(Uri mp3Path) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_app_icon)
                .setAutoCancel(true)
                .setContentTitle("Music Player")
                .setContentText("MP3 Download Complete");
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.putExtra(MainActivity.INTENT_PARAM_MP3_URI, mp3Path);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);
        notificationManager.cancel(NOTIFICATION_ID_DOWNLOADING);
        notificationManager.notify(NOTIFICATION_ID_DOWNLOADED, builder.build());
    }
}
