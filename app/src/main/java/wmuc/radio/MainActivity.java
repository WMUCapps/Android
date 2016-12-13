package wmuc.radio;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

public class MainActivity extends Activity implements OnClickListener {
    private final Uri fmURI = Uri.parse("http://wmuc.umd.edu:8000/wmuc-hq");
    private final Uri digURI = Uri.parse("http://wmuc.umd.edu:8000/wmuc2-high");
    private static final String TAG = "WMUC";
    private Uri currchan;
    private ImageButton playButton;
    private ImageButton DIGButton;
    private ImageButton FMButton;
    private boolean playing = false;
    private Animation justslideLeft;
    private Animation justslideRightCenter;
    private Animation justShrinkLeft;
    private Animation justShrinkRight;
    private boolean digHit = false;
    private boolean fmHit = false;
    private Animation slideRight;
    private Animation shrinkRight;
    private Animation slideLeft;
    private Animation shrinkLeft;
    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;
    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;
    private final int notificationID = (int) System.currentTimeMillis();
    private RemoteViews remoteViews;
    private Context context;
    private boolean mAudioFocusGranted = false;
    private AudioManager.OnAudioFocusChangeListener afChangeListener;
    private Notification note;
    private boolean ongoing;
    private final IntentFilter headphoneFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private final MusicIntentReceiver hRemoval = new MusicIntentReceiver();


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    public void showNotification() {
        remoteViews.setImageViewResource(R.id.img, R.drawable.wmuc);
        remoteViews.setTextViewText(R.id.infotext,"HELLO MOM");

        Intent playIntent = new Intent("play_clicked");
        Intent fmIntent = new Intent("fm_clicked");
        Intent digIntent = new Intent("dig_clicked");

        fmIntent.putExtra("id", notificationID);
        digIntent.putExtra("id", notificationID);
        playIntent.putExtra("id", notificationID);

        PendingIntent p_button_intent = PendingIntent.getBroadcast(context, 123, playIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.playnotif, p_button_intent);

        PendingIntent fm_button_intent = PendingIntent.getBroadcast(context, 123, fmIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.fmnotif, fm_button_intent);

        PendingIntent dig_button_intent = PendingIntent.getBroadcast(context, 123, digIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.dignotif, dig_button_intent);

        IntentFilter dig = new IntentFilter();
        dig.addAction("dig_clicked");
        registerReceiver(this.dig_listener, dig);

        IntentFilter fm = new IntentFilter();
        fm.addAction("fm_clicked");
        registerReceiver(this.fm_listener, fm);

        IntentFilter play = new IntentFilter();
        play.addAction("play_clicked");
        registerReceiver(this.play_listener, play);

        Intent nIntent = new Intent(context, MainActivity.class);
        nIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        nIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, nIntent, 0);
        builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.wmuc);
        builder.setAutoCancel(false);
        builder.setContentIntent(pendingIntent);
        builder.setPriority(Notification.PRIORITY_MAX);
        builder.setOngoing(ongoing);
        builder.setStyle(new NotificationCompat.MediaStyle());
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        note = builder.build();
        note.bigContentView = remoteViews;
        note.contentView = remoteViews;
        notificationManager.notify(notificationID, note);
    }

    public void onClick(View v) {
        Log.d("THE BUCK STOPS HERE", "hopefully " + playing);
        if (v == DIGButton && !digHit) {
            Log.d("Digital", "hit the button");

            if (!fmHit) {
                v.startAnimation(justslideLeft);
                FMButton.startAnimation(justShrinkLeft);
            } else {
                v.startAnimation(slideLeft);
                FMButton.startAnimation(shrinkLeft);
            }

            currchan = digURI;
            digHit = true;
            fmHit = false;
            if (playing) {
                stopService(new Intent(getBaseContext(), StreamingService.class));
                Log.d("This one is for", " visibility");
            }

            if (playing) {
                remoteViews = new RemoteViews(getPackageName(), R.layout.dig_play);
                startService(new Intent("", currchan, getBaseContext(), StreamingService.class));
                showNotification();
            }
        } else if (v == FMButton && !fmHit) {
            if (!digHit) {
                v.startAnimation(justslideRightCenter);
                DIGButton.startAnimation(justShrinkRight);
            } else {
                v.startAnimation(slideRight);
                DIGButton.startAnimation(shrinkRight);
            }


            if (playing) {
                stopService(new Intent(getBaseContext(), StreamingService.class));
                Log.d("This one is for", " visibility");
            }
            currchan = fmURI;
            digHit = false;
            fmHit = true;
            if (playing) {
                remoteViews = new RemoteViews(getPackageName(), R.layout.fm_play);
                startService(new Intent("", currchan, getBaseContext(), StreamingService.class));
                showNotification();
            }
        } else if (v == playButton) {
            Log.d("FM: " + fmHit + " DIG: " + digHit + " playing: " + playing, " In case you were curious");
            if (fmHit && playing) {
                stopService(new Intent(getBaseContext(), StreamingService.class));
                playing = false;
                playButton.setImageResource(R.drawable.play1);
                abandonAudioFocus();
                ongoing = false;
                remoteViews = new RemoteViews(getPackageName(), R.layout.fm_pause);
                showNotification();

            } else if (digHit && playing) {
                stopService(new Intent(getBaseContext(), StreamingService.class));
                playing = false;
                playButton.setImageResource(R.drawable.play1);
                abandonAudioFocus();
                ongoing = false;
                remoteViews = new RemoteViews(getPackageName(), R.layout.dig_pause);
                showNotification();

            } else if (!fmHit && !digHit && !playing) {
                Context context = getApplicationContext();
                CharSequence text = "Please select a station.";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                playing = false;

            } else {
                if (!playing) {
                    requestAudioFocus();
                    startService(new Intent("", currchan, getBaseContext(), StreamingService.class));
                    playing = true;
                    playButton.setImageResource(R.drawable.pause1);
                    ongoing = true;
                    if (fmHit)
                        remoteViews = new RemoteViews(getPackageName(), R.layout.fm_play);

                    else
                        remoteViews = new RemoteViews(getPackageName(), R.layout.dig_play);
                    showNotification();

                }
            }
        }

    }

    private void initializeUIElements() {

        currchan = fmURI;
        //       playSeekBar = (ProgressBar) findViewById(R.id.progressBar);
        //       playSeekBar.setMax(100);
        //       playSeekBar.setVisibility(View.INVISIBLE);
        playButton = (ImageButton) findViewById(R.id.Play);
        playButton.setOnClickListener(this);
        DIGButton = (ImageButton) findViewById(R.id.DIG);
        DIGButton.setOnClickListener(this);
        FMButton = (ImageButton) findViewById(R.id.FM);
        FMButton.setOnClickListener(this);

    }

    private void requestAudioFocus() {
        if (!mAudioFocusGranted) {
            AudioManager am = (AudioManager) context
                    .getSystemService(Context.AUDIO_SERVICE);
            // Request audio focus for play back
            int result = am.requestAudioFocus(afChangeListener,
                    // Use the music stream.
                    AudioManager.STREAM_MUSIC,
                    // Request permanent focus.
                    AudioManager.AUDIOFOCUS_GAIN);

            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                mAudioFocusGranted = true;
                Log.d("Focus", "Gained");
            } else {
                // FAILED
                Log.e(TAG,
                        ">>>>>>>>>>>>> FAILED TO GET AUDIO FOCUS <<<<<<<<<<<<<<<<<<<<<<<<");
            }
        }
    }

    private void abandonAudioFocus() {
        Log.d("ABANDONED", ": Audio Focus");
        AudioManager am = (AudioManager) context
                .getSystemService(Context.AUDIO_SERVICE);
        int result = am.abandonAudioFocus(afChangeListener);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mAudioFocusGranted = false;
        } else {
            // FAILED
            Log.e(TAG,
                    ">>>>>>>>>>>>> FAILED TO ABANDON AUDIO FOCUS <<<<<<<<<<<<<<<<<<<<<<<<");
        }
        afChangeListener = null;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("Creating", "does the log even work");
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        justslideRightCenter = AnimationUtils.loadAnimation(this, R.anim.anim_slide_right);
        justShrinkRight = AnimationUtils.loadAnimation(this, R.anim.just_shrink_right);
        justslideLeft = AnimationUtils.loadAnimation(this, R.anim.anim_slide_left);
        justShrinkLeft = AnimationUtils.loadAnimation(this, R.anim.just_shrink_left);
        slideRight = AnimationUtils.loadAnimation(this, R.anim.anim_slide_dig_right);
        shrinkRight = AnimationUtils.loadAnimation(this, R.anim.shrink_fm_right);
        slideLeft = AnimationUtils.loadAnimation(this, R.anim.anim_slide_fm_left);
        shrinkLeft = AnimationUtils.loadAnimation(this, R.anim.shrink_dg_left);
        setContentView(R.layout.activity_main);
        initializeUIElements();
        mDrawerList = (ListView) findViewById(R.id.navList);
        Log.d("LIST", " " + mDrawerList);
        addDrawerItems();

        context = this;
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        registerReceiver(hRemoval, headphoneFilter);

        afChangeListener =
                new AudioManager.OnAudioFocusChangeListener() {
                    public void onAudioFocusChange(int focusChange) {
                        Log.d("focus change: ", focusChange + " ");
                        if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                            Log.d("Transient", "AudioFocus");
                            onClick(playButton);
                        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                            Log.d("Gained", "Audio Focus");
                            onClick(playButton);
                        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                            Log.wtf("Lost Audio Focus", "");
                            stopService(new Intent(context, StreamingService.class));
                            playing = false;
                            playButton.setImageResource(R.drawable.play1);
                            abandonAudioFocus();
                            ongoing = false;
                            remoteViews = new RemoteViews(getPackageName(), R.layout.dig_pause);
                            showNotification();
                        }
                    }
                };

    }

    private void addDrawerItems() {
        String[] osArray = {"Schedule", "Settings"};
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, osArray);
        mDrawerList.setAdapter(mAdapter);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("TEST", "ID " + id);
                if (id == 1) {
                    startActivity(new Intent(getApplicationContext(), settings.class));
                } else if (id == 0) {
                    startActivity(new Intent(getApplicationContext(), Schedule.class));
                }

            }
        });
    }

    /*
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();

            //noinspection SimplifiableIfStatement
            if (id == R.id.action_settings) {
                startActivity(new Intent(getApplicationContext(), settings.class));
                return true;
            }
            else if (id == R.id.action_schedule) {
                startActivity(new Intent(getApplicationContext(), Schedule.class));
                return true;
            }

            return super.onOptionsItemSelected(item);
        }
    */
    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://wmuc.radio/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://wmuc.radio/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    @Override
    protected void onDestroy() {
        //before destroying the app
        this.unregisterReceiver(play_listener);
        this.unregisterReceiver(fm_listener);
        this.unregisterReceiver(dig_listener);
        this.unregisterReceiver(hRemoval);
        Log.wtf("Bye Bye now", "");
        notificationManager.cancel(notificationID);
        super.onDestroy();


    }

    private final BroadcastReceiver dig_listener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("this one's for dig", " digital that is");
            onClick(DIGButton);
        }
    };
    private final BroadcastReceiver fm_listener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("this one's for fm", " frequency modulation that is");
            onClick(FMButton);
        }
    };
    private final BroadcastReceiver play_listener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("play", "or is it pause hm");
            onClick(playButton);

        }
    };

    private class MusicIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context cntx, Intent intent) {
            String action = intent.getAction();

            if (action.compareTo(AudioManager.ACTION_AUDIO_BECOMING_NOISY) == 0) {
                stopService(new Intent(getBaseContext(), StreamingService.class));
                playing = false;
                playButton.setImageResource(R.drawable.play1);
                abandonAudioFocus();
                ongoing = false;
                remoteViews = new RemoteViews(getPackageName(), R.layout.dig_pause);
                showNotification();
            }
        }
    }

    ;  /* end HeadsetIntentReceiver  */
}
