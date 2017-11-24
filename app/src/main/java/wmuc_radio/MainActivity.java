package wmuc_radio;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends Activity implements OnClickListener {
    private final Uri fmURI = Uri.parse("http://wmuc.umd.edu:8000/wmuc-hq");
    private final Uri digURI = Uri.parse("http://wmuc.umd.edu:8000/wmuc2-high");
    private static final String TAG = "wmuc";
    private TextView currShow;
    private TextView currHost;
    private Uri currchan;
    private ImageButton playButton;
    private ImageButton schedButton;
    private ImageButton DIGButton;
    private ImageButton FMButton;
    private ImageButton favoriteButton;
    private boolean playing = false;
    private Animation justShrinkLeft;
    private Animation justShrinkRight;
    private boolean digHit = false;
    private boolean fmHit = false;
    private Animation shrinkRight;
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
    private boolean favorited = false;
    float swipeX1,swipeY1,swipeX2,swipeY2;
    DisplayMetrics dm = new DisplayMetrics();
    int xDest;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    private void moveToCenter(View v){
        this.getWindowManager().getDefaultDisplay().getMetrics( dm );
        xDest = dm.widthPixels/2;
        Log.wtf("Display Metrics", " "+ dm);
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
        slideAnim.setInterpolator(new DecelerateInterpolator());

        ScaleAnimation scaleAnim;

        if(fmHit||digHit) {
            scaleAnim = new ScaleAnimation(.7f, 1.5f, .7f, 1.5f, 0, 200f);
        }
        else
            scaleAnim = new ScaleAnimation(1f, 1.5f, 1f, 1.5f, 0, 200f);

        scaleAnim.setDuration(400);
        scaleAnim.setFillAfter( true );
        scaleAnim.setInterpolator(new DecelerateInterpolator());

        a.addAnimation(scaleAnim);
        a.addAnimation(slideAnim);
        a.setFillAfter(true);
        v.startAnimation(a);
    }

    private void shrinkToSide(View v){
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics( dm );


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

        Intent homescreenintent = new Intent(this, MainActivity.class);
        homescreenintent.setAction(Intent.ACTION_MAIN);
        homescreenintent.addCategory(Intent.CATEGORY_LAUNCHER);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                homescreenintent, PendingIntent.FLAG_UPDATE_CURRENT);

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
            builder.addAction(0, "play", playIntent);

        builder.addAction(0, "fm", fmIntent);
        builder.addAction(0, "digital", digIntent);

        builder.setContentIntent(contentIntent);
        notificationManager.notify(notificationID, builder.build());
    }

    public void onClick(View v) {
        if(v == schedButton) {

            Intent intent = new Intent(getApplicationContext(), Schedule.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        Log.d("THE BUCK STOPS HERE", "hopefully " + playing);
        if (v == DIGButton && !digHit) {
            channel = "WMUC: Digital";
            showNotification();

            Log.d("digital", "hit the button");

            if (!fmHit) {
                moveToCenter(v);
                FMButton.startAnimation(justShrinkLeft);
            } else {
                moveToCenter(v);
                FMButton.startAnimation(shrinkLeft);
            }

            if (playing) {
                stopService(new Intent(getBaseContext(), StreamingService.class));
                Log.wtf("Service", "Service stopped");
            }


            currchan = digURI;
            getCurrShow(Schedule.DIGITAL);
            digHit = true;
            fmHit = false;

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
                Log.wtf("Service", "Service stopped");
            }
            currchan = fmURI;
            getCurrShow(Schedule.FM);

            digHit = false;
            fmHit = true;
            if (playing) {
                startService(new Intent("", currchan, getBaseContext(), StreamingService.class));
            }
        } else {
            if (v == favoriteButton) {
                String string = (String) currShow.getText();
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = prefs.edit();

                Set<String> shows = prefs.getStringSet("favorite_shows",null);
                Set<String> newList = new HashSet<String>();

                if (shows != null) {
                    for (String each : shows) {
                        newList.add(each);
                    }
                }

                if (favorited) {
                    favoriteButton.setImageResource(R.drawable.nofavorite);
                    favorited = false;

                    if (shows != null){
                        if (newList.contains(string)){
                            newList.remove(string);
                            editor.putStringSet("favorite_shows", newList);
                            Boolean removed = editor.commit();
                            System.out.println("**** ^ shows " + newList.toString());
                            System.out.println("**** ^ did it commit " + removed.toString());
                        }
                    }


                } else {
                    favoriteButton.setImageResource(R.drawable.favorited);
                    favorited = true;

                    newList.add(string);
                    editor.putStringSet("favorite_shows", newList);
                    Boolean removed = editor.commit();
                    System.out.println("******* > shows " + newList.toString());
                    System.out.println("******* > did it commit " + removed.toString());

                }
            } else if (v == playButton) {

                Log.d("fm: " + fmHit + " DIG: " + digHit + " playing: " + playing, " In case you were curious");
                if (fmHit && playing) {
                    stopService(new Intent(getBaseContext(), StreamingService.class));
                    playing = false;
                    playButton.setImageResource(R.drawable.play);
                    abandonAudioFocus();
                    ongoing = false;
                    showNotification();

                } else if (digHit && playing) {
                    stopService(new Intent(getBaseContext(), StreamingService.class));
                    playing = false;
                    playButton.setImageResource(R.drawable.play);
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
                        playButton.setImageResource(R.drawable.pause);
                        ongoing = true;
                        if (fmHit)
                            channel = "WMUC: FM";

                        else channel = "WMUC: Digital";
                        showNotification();

                    }
                }
            }
        }

    }

    private void initializeUIElements() {

        currchan = fmURI;
        //       playSeekBar = (ProgressBar) findViewById(R.id.progressBar);
        //       playSeekBar.setMax(100);
        //       playSeekBar.setVisibility(View.INVISIBLE);
        currShow = (TextView) findViewById(R.id.currShow);
        currShow.setText("");
        currHost = (TextView) findViewById(R.id.currHost);
        currHost.setText("");
        playButton = (ImageButton) findViewById(R.id.Play);
        playButton.setOnClickListener(this);
        schedButton = (ImageButton) findViewById(R.id.sched);
        schedButton.setOnClickListener(this);
        DIGButton = (ImageButton) findViewById(R.id.DIG);
        DIGButton.setOnClickListener(this);
        FMButton = (ImageButton) findViewById(R.id.FM);
        FMButton.setOnClickListener(this);
        DIGButton.setOnTouchListener(new TouchHandler());
        FMButton.setOnTouchListener(new TouchHandler());
        favoriteButton = (ImageButton) findViewById(R.id.favorite);
        favoriteButton.setOnClickListener(this);

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
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        justShrinkRight = AnimationUtils.loadAnimation(this, R.anim.just_shrink_right);
        justShrinkLeft = AnimationUtils.loadAnimation(this, R.anim.just_shrink_left);
        shrinkRight = AnimationUtils.loadAnimation(this, R.anim.shrink_fm_right);
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
                            playButton.setImageResource(R.drawable.play);
                            abandonAudioFocus();
                            ongoing = false;
                            showNotification();
                        }
                    }
                };



    }

    private void addDrawerItems() {
        String[] osArray = {"schedule", "Settings"};
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
                startActivity(new Intent(getApplicationContext(), schedule.class));
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
                Uri.parse("android-app://wmuc_radio.radio/http/host/path")
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
                Uri.parse("android-app://wmuc_radio.radio/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    @Override
    protected void onDestroy() {
        //before destroying the app
        this.unregisterReceiver(hRemoval);
        this.unregisterReceiver(NBR);

        notificationManager.cancel(notificationID);
        super.onDestroy();


    }

    public class MusicIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context cntx, Intent intent) {
            String action = intent.getAction();

            if (action.compareTo(AudioManager.ACTION_AUDIO_BECOMING_NOISY) == 0) {
                stopService(new Intent(getBaseContext(), StreamingService.class));
                playing = false;
                playButton.setImageResource(R.drawable.play);
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
    private int _xDelta;
    private int _yDelta;

    /*@TODO: add swiping correctly*/
    private class TouchHandler implements View.OnTouchListener {
        public boolean onTouch(View view, MotionEvent touchevent) {
            Log.d("X value:" , " " + touchevent.getX());
            final int X = (int) touchevent.getRawX();
            final int Y = (int) touchevent.getRawY();
            switch (touchevent.getAction()) {
                // when user first touches the screen we get x and y coordinate
                case MotionEvent.ACTION_DOWN: {
 /*                   RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                    _xDelta = X - lParams.leftMargin;
                    _yDelta = Y - lParams.topMargin;*/
                    swipeX1 = touchevent.getX();
                    swipeY1 = touchevent.getY();
                    Log.d("Just checking if ", " this runs constantly");
                    break;
                }
               /* case MotionEvent.ACTION_MOVE: {
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                    if(view == FMButton)
                        layoutParams.leftMargin = Math.min(X - _xDelta, xDest);
                    else
                        layoutParams.leftMargin = Math.max(X - _xDelta, xDest);
                    view.setLayoutParams(layoutParams);
                    break;
                }*/
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

    private Schedule.Show getCurrShow(int channel) {
        Calendar c = Calendar.getInstance();
        int hourOfDay = c.get(Calendar.HOUR_OF_DAY);
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

        Schedule.Show show = null;

        if(currchan == digURI) {
            if (dayOfWeek == Calendar.SUNDAY) {
                show = Splash.digSched[0][hourOfDay];
            } else if (dayOfWeek == Calendar.MONDAY) {
                show = Splash.digSched[1][hourOfDay];
            } else if (dayOfWeek == Calendar.TUESDAY) {
                show = Splash.digSched[2][hourOfDay];
            } else if (dayOfWeek == Calendar.WEDNESDAY) {
                show = Splash.digSched[3][hourOfDay];
            } else if (dayOfWeek == Calendar.THURSDAY) {
                show = Splash.digSched[4][hourOfDay];
            } else if (dayOfWeek == Calendar.FRIDAY) {
                show = Splash.digSched[5][hourOfDay];
            } else if (dayOfWeek == Calendar.SATURDAY) {
                show = Splash.digSched[6][hourOfDay];
            }
        } else if(currchan == fmURI) {
            if (dayOfWeek == Calendar.SUNDAY) {
                show = Splash.fmSched[0][hourOfDay];
            } else if (dayOfWeek == Calendar.MONDAY) {
                show = Splash.fmSched[1][hourOfDay];
            } else if (dayOfWeek == Calendar.TUESDAY) {
                show = Splash.fmSched[2][hourOfDay];
            } else if (dayOfWeek == Calendar.WEDNESDAY) {
                show = Splash.fmSched[3][hourOfDay];
            } else if (dayOfWeek == Calendar.THURSDAY) {
                show = Splash.fmSched[4][hourOfDay];
            } else if (dayOfWeek == Calendar.FRIDAY) {
                show = Splash.fmSched[5][hourOfDay];
            } else if (dayOfWeek == Calendar.SATURDAY) {
                show = Splash.fmSched[6][hourOfDay];
            }
        }
        currShow.setText(show.sName);
        currHost.setText(show.host);
        favoriteButton.setVisibility(View.VISIBLE);
        favoriteButton.setImageResource(R.drawable.nofavorite);
        favorited = false;
        //check here if show is favorited or not
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();


        Set<String> shows = prefs.getStringSet("favorite_shows",null);
        if (shows != null) {
            System.out.println("**** ^ shows " + shows.toString());
            if (shows.contains(show.sName)) {
                favoriteButton.setImageResource(R.drawable.favorited);
                favorited = true;
            }
        }

        return show;
    }

}