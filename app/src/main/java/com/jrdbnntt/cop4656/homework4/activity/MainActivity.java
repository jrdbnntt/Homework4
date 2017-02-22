package com.jrdbnntt.cop4656.homework4.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jrdbnntt.cop4656.homework4.R;
import com.jrdbnntt.cop4656.homework4.service.DownloadService;
import com.jrdbnntt.cop4656.homework4.service.MediaPlayerService;

public class MainActivity extends AppCompatActivity {
    /** Messenger for communicating with the service. */
    private Messenger mService = null;

    /** Flag indicating whether we have called bind on the service. */
    private boolean mBound;


    private ServiceConnection mConnection;


    public static final String INTENT_PARAM_MP3_URI = "mp3Uri";

    public enum ToggleButtonType {
        PLAY, PAUSE
    }

    TextView tvArtist;
    TextView tvSong;
    ImageButton btnToggle;
    ImageButton btnStop;
    ImageView ivAlbumArt;
    ToggleButtonType toggleValue = ToggleButtonType.PLAY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivAlbumArt = (ImageView) findViewById(R.id.ivAlbumArt);
        tvArtist = (TextView) findViewById(R.id.tvArtistName);
        tvSong = (TextView) findViewById(R.id.tvSongTitle);
        btnToggle = (ImageButton) findViewById(R.id.btnToggle);
        btnStop = (ImageButton) findViewById(R.id.btnStop);

        btnToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBound) {
                    toggleMusic();
                }
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBound) {
                    stopMusic();
                }
            }
        });


        if (mBound) {
            return;
        }

        // From downloader notification?
        Intent intent = getIntent();
        if (intent.hasExtra(INTENT_PARAM_MP3_URI) ) {
            Bundle bundle = intent.getExtras();
            Uri mp3Uri = (Uri) bundle.get(INTENT_PARAM_MP3_URI);

            // Initialize player
            Intent bIntent = new Intent(getApplicationContext(), MediaPlayerService.class);
            bIntent.putExtra(INTENT_PARAM_MP3_URI, mp3Uri);


            mConnection = new ServiceConnection() {
                public void onServiceConnected(ComponentName className, IBinder service) {
                    mService = new Messenger(service);
                    mBound = true;

                    // Refresh display
                    setSongDisplayInfo();
                }

                public void onServiceDisconnected(ComponentName className) {
                    mService = null;
                    mBound = false;
                }
            };
            bindService(bIntent, mConnection, Context.BIND_AUTO_CREATE);

        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.music_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.download:
                if (!mBound) {
                    downloadMp3();
                } else {
                    Toast.makeText(this, "MP3 Already downloaded", Toast.LENGTH_SHORT).show();
                }

                return true;
            case R.id.exit:
                exitGracefully();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void downloadMp3() {
        DownloadService.startActionDownloadMp3(this);
    }


    /**
     * Exit app and stop MediaPlayerService, but do not stop DownloadService
     */
    private void exitGracefully() {
        if (mBound) {
            stopMusic();
            unbindService(mConnection);
            mBound = false;
        }
        System.exit(0);
    }


    private void setSongDisplayInfo() {
        tvSong.setText(getString(R.string.song_title_actual));
        tvArtist.setText(getString(R.string.artist_name_actual));
        ivAlbumArt.setImageResource(R.mipmap.album_art_downloaded);
    }



    private void toggleMusic() {
        switch (toggleValue) {
            case PLAY:
                // Play it
                try {
                    mService.send(Message.obtain(null, MediaPlayerService.MSG_PLAY, 0, 0));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                // Swap button
                toggleValue = ToggleButtonType.PAUSE;
                btnToggle.setImageResource(R.drawable.ic_buttons_pause_50dp);
                break;
            case PAUSE:
            default:
                // Pause it
                try {
                    mService.send(Message.obtain(null, MediaPlayerService.MSG_PAUSE, 0, 0));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                // Swap button
                toggleValue = ToggleButtonType.PLAY;
                btnToggle.setImageResource(R.drawable.ic_buttons_play_50dp);
                break;
        }
    }

    private void stopMusic() {
        try {
            mService.send(Message.obtain(null, MediaPlayerService.MSG_STOP, 0, 0));

        } catch (RemoteException e) {
            e.printStackTrace();
        }
        toggleValue = ToggleButtonType.PLAY;
        btnToggle.setImageResource(R.drawable.ic_buttons_play_50dp);
    }

    @Override
    protected void onDestroy() {
        if (mBound) {
            stopMusic();
            unbindService(mConnection);
            mBound = false;
        }
        super.onDestroy();
    }
}
