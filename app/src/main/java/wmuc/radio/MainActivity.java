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
import android.support.v4.animation.AnimatorCompatHelper;
import android.support.v7.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
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
    private Context context;
    private boolean mAudioFocusGranted = false;
    private AudioManager.OnAudioFocusChangeListener afChangeListener;
    private boolean ongoing;
    private final IntentFilter headphoneFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private final MusicIntentReceiver hRemoval = new MusicIntentReceiver();
    private final IntentFilter playfFilter = new IntentFilter("PLAY_BUTTON_ACTION");
    private final IntentFilter fmFilter = new IntentFilter("FM_BUTTON_ACTION");
    private final IntentFilter digFilter = new IntentFilter("DIG_BUTTON_ACTION");
    private final NotificationBroadcastReciver NBR = new NotificationBroadcastReciver();
    private String channel;
    float swipeX1,swipeY1,swipeX2,swipeY2;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    private void moveToCenter(View v){
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics( dm );
        Log.wtf("Display Metrics", " "+ dm);
        int xDest = dm.widthPixels/2;
        xDest -= (v.getMeasuredWidth()*1.5/2);
        int originalPos[] = new int[2];
        Log.wtf("x Destination", " "+ xDest);
        v.getLocationOnScreen( originalPos );
        Log.wtf("x origin", " "+ originalPos[0]);

        AnimationSet a = new AnimationSet(true);
        TranslateAnimation slideAnim;

        if(fmHit) {
            slideAnim = new TranslateAnimation(115, xDest - originalPos[0], 0, 0);
            Log.d("Dig hit", "HIT");
        }else
            slideAnim = new TranslateAnimation(-25, xDest-originalPos[0],0,0);
        slideAnim.setDuration(400);
        slideAnim.setFillAfter(true);
        ScaleAnimation scaleAnim;


        if(fmHit||digHit) {
            scaleAnim = new ScaleAnimation(.7f, 1.5f, .7f, 1.5f, 0, 200f);
        }
        else
            scaleAnim = new ScaleAnimation(1f, 1.5f, 1f, 1.5f, 0, 200f);

        scaleAnim.setDuration(400);
        scaleAnim.setFillAfter( true );

        a.addAnimation(scaleAnim);
        a.addAnimation(slideAnim);
        a.setFillAfter(true);
        v.startAnimation(a);
    }

    public void showNotification() {
        builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.wmuc);
        builder.setAutoCancel(false);
        builder.setPriority(Notification.PRIORITY_MAX);
        builder.setOngoing(ongoing);
        builder.setContentTitle(channel);
        builder.setPriority(Notification.PRIORITY_MAX);
        Intent play = new Intent();
        play.putExtra("message", "play");
        play.setAction("PLAY_BUTTON_ACTION");
        PendingIntent playIntent = PendingIntent.getBroadcast(this, 123, play, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent fm = new Intent();
        fm.putExtra("message", "fm");
        fm.setAction("FM_BUTTON_ACTION");
        PendingIntent fmIntent = PendingIntent.getBroadcast(this, 123, fm, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent dig = new Intent();
        dig.putExtra("message", "fm");
        dig.setAction("DIG_BUTTON_ACTION");
        PendingIntent digIntent = PendingIntent.getBroadcast(this, 123, dig, PendingIntent.FLAG_UPDATE_CURRENT);

        if(ongoing)
            builder.addAction(0, "Pause", playIntent);
        else
            builder.addAction(0, "Play", playIntent);

        builder.addAction(0, "FM", fmIntent);
        builder.addAction(0, "Digital", digIntent);

        builder.setContentIntent(playIntent);
        notificationManager.notify(notificationID, builder.build());
    }

    public void onClick(View v) {
        Log.d("THE BUCK STOPS HERE", "hopefully " + playing);
        if (v == DIGButton && !digHit) {
            channel = "WMUC: Digital";
            showNotification();

            Log.d("Digital", "hit the button");

            if (!fmHit) {
                moveToCenter(v);
                FMButton.startAnimation(justShrinkLeft);
            } else {
                moveToCenter(v);
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
                startService(new Intent("", currchan, getBaseContext(), StreamingService.class));
            }
        } else if (v == FMButton && !fmHit) {
            channel = "WMUC: FM";
            showNotification();

            if (!digHit) {
                moveToCenter(v);
                DIGButton.startAnimation(justShrinkRight);
            } else {
                moveToCenter(v);
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
                startService(new Intent("", currchan, getBaseContext(), StreamingService.class));
            }
        } else if (v == playButton) {

            Log.d("FM: " + fmHit + " DIG: " + digHit + " playing: " + playing, " In case you were curious");
            if (fmHit && playing) {
                stopService(new Intent(getBaseContext(), StreamingService.class));
                playing = false;
                playButton.setImageResource(R.drawable.play1);
                abandonAudioFocus();
                ongoing = false;
                showNotification();

            } else if (digHit && playing) {
                stopService(new Intent(getBaseContext(), StreamingService.class));
                playing = false;
                playButton.setImageResource(R.drawable.play1);
                abandonAudioFocus();
                ongoing = false;
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
                    if(fmHit)
                        channel = "WMUC: FM";

                    else channel = "WMUC: Digital";
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
        DIGButton.setOnTouchListener(new TouchHandler());
        FMButton.setOnTouchListener(new TouchHandler());

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
        registerReceiver(NBR,playfFilter);
        registerReceiver(NBR,fmFilter);
        registerReceiver(NBR,digFilter);

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
        this.unregisterReceiver(hRemoval);
        this.unregisterReceiver(NBR);

        Log.wtf("Bye Bye now", "");
        notificationManager.cancel(notificationID);
        super.onDestroy();


    }

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
                showNotification();
            }
        }
    }
    private class NotificationBroadcastReciver extends BroadcastReceiver{
        public void onReceive(Context context, Intent intent) {
            Log.d("HELP", "Me, the sun is rising again");
            if(intent.getAction().equals("PLAY_BUTTON_ACTION"))
                onClick(playButton);
            else if (intent.getAction().equals("FM_BUTTON_ACTION"))
                onClick(FMButton);
            else if (intent.getAction().equals("DIG_BUTTON_ACTION"))
                onClick(DIGButton);
        }
    }

    private class TouchHandler implements View.OnTouchListener {
        public boolean onTouch(View view, MotionEvent touchevent) {
            switch (touchevent.getAction()) {
                // when user first touches the screen we get x and y coordinate
                case MotionEvent.ACTION_DOWN: {
                    swipeX1 = touchevent.getX();
                    swipeY1 = touchevent.getY();
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    swipeX2 = touchevent.getX();
                    swipeY2 = touchevent.getY();

                    // if left to right sweep event on screen
                    if (swipeX1 < swipeX2) {
                        onClick(view);

                    }

                    // if right to left sweep event on screen
                    if (swipeX1 > swipeX2) {
                        onClick(view);

                    }

                    // if UP to Down sweep event on screen
                    if (swipeY1 < swipeY2) {
                    //do nothing.
                    }

                    // if Down to UP sweep event on screen
                    if (swipeY1 > swipeY2) {
                    //do nothing.
                    }
                    break;
                }
            }
            return false;
        }
    }




}
