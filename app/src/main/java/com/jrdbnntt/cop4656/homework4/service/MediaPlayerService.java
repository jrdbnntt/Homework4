package com.jrdbnntt.cop4656.homework4.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.jrdbnntt.cop4656.homework4.R;
import com.jrdbnntt.cop4656.homework4.activity.MainActivity;

public class MediaPlayerService extends Service {
    public static final int MSG_PLAY = 1;
    public static final int MSG_PAUSE = 2;
    public static final int MSG_STOP = 3;

    private static final int NOTIFICATION_ID_PLAYING = 10;

    /**
     * Handler of incoming messages from clients
     */
    static class IncomingHandler extends Handler {
        MediaPlayerService service;
        IncomingHandler(MediaPlayerService service) {
            super();
            this.service = service;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_PLAY:
                    service.player.start();
                    service.sendDownloadCompleteNotification();
                    break;
                case MSG_PAUSE:
                    if (service.player.isPlaying()) {
                        service.player.pause();
                    }
                    service.removeNotification();
                    break;
                case MSG_STOP:
                    if (service.player.isPlaying()) {
                        service.player.pause();
                        service.player.seekTo(0);
                    }
                    service.removeNotification();
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler(this));
    static private MediaPlayer player;

    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service
     */
    @Override
    public IBinder onBind(Intent intent) {

        Uri mp3Path = (Uri) intent.getExtras().get(MainActivity.INTENT_PARAM_MP3_URI);

        if (player == null) {
            player = MediaPlayer.create(getApplicationContext(), mp3Path);
        }


        return mMessenger.getBinder();
    }


    @Override
    public void onDestroy() {
        player.stop();
        player.release();
    }


    private void sendDownloadCompleteNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_app_icon)
                .setContentTitle("Music Player")
                .setContentText("MP3 is playing.")
                .setOngoing(true);
        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_NO_CREATE);
        builder.setContentIntent(resultPendingIntent);
        notificationManager.notify(NOTIFICATION_ID_PLAYING, builder.build());
    }

    private void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID_PLAYING);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        removeNotification();
        return super.onUnbind(intent);
    }
}