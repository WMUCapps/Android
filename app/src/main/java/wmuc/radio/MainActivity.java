package wmuc.radio;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecSelector;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;
import com.google.android.exoplayer.util.PlayerControl;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
public class MainActivity extends Activity implements OnClickListener {

    private ExoPlayer exp;
    public final String FMurl = "http://wmuc.umd.edu:8000/wmuc-hq";
    public final String Digitalurl = "http://wmuc.umd.edu:8000/wmuc2-high";
    private static final String TAG = "WMUC";
    private String currchan;
    private ImageButton playButton;
    private ImageButton DIGButton;
    private ImageButton FMButton;
    private ProgressBar playSeekBar;
    private boolean enableButton = true;
    private boolean playing = false;
    private Animation justslideLeft;
    private Animation justslideRightCenter;
    private Animation justShrinkLeft;
    private Animation justShrinkRight;
    private boolean digHit = false;
    private  boolean fmHit = false;
    private Animation slideRight;
    private Animation shrinkRight;
    private Animation slideLeft;
    private Animation shrinkLeft;
    private Animation pause_fm;
    private Animation dig_grow_to_pause;
    private Animation pause_dig;
    private Animation fm_grow_to_pause;
    private MediaCodecAudioTrackRenderer audioRenderer ;
    private PlayerControl playerControl;
    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
/*
    private void moveViewToScreenCenter( View view )
    {
        view.setPivotX(50);
        view.setPivotY(50);
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics( dm );
        int originalPos[] = new int[2];
        view.getLocationOnScreen(originalPos);
        float xDest = dm.widthPixels/2;
        xDest -= (view.getWidth()/2);
        AnimationSet anim = new AnimationSet (true);
        float heightAdj = (float)1.5 * view.getHeight()/2;

        TranslateAnimation move = new TranslateAnimation(0, xDest - originalPos[0], 0, heightAdj);
        ScaleAnimation grow = new ScaleAnimation(1f,1.5f,1f,1.5f);
        anim.addAnimation(move);
        anim.addAnimation(grow);
        anim.setDuration(400);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.setFillAfter( true );
        view.startAnimation(anim);
    }

*/
    public void onClick(View v) {
        playing = playerControl.isPlaying();
        Log.d("THE BUCK STOPS HERE", "hopefully " + playing);

        if (enableButton) {
            if (v == DIGButton && !digHit) {

                if (!fmHit){
                v.startAnimation(justslideRightCenter);
                FMButton.startAnimation(justShrinkRight);
            }
                else{
                    v.startAnimation(slideRight);
                    FMButton.startAnimation(shrinkRight);
                }

                digHit = true;
                fmHit = false;
                if (playing)
                    stopPlaying();

                currchan = Digitalurl;
                initializeExoPlayer();

                if (playing)
                    startPlaying();
            }
            else if (v == FMButton && !fmHit) {

                if (!digHit) {
                v.startAnimation(justslideLeft);
                    DIGButton.startAnimation(justShrinkLeft);
                }
                else{
                    v.startAnimation(slideLeft);
                    DIGButton.startAnimation(shrinkLeft);
                }


                if (playing)
                    stopPlaying();
                currchan = FMurl;
                initializeExoPlayer();
                digHit = false;
                fmHit = true;
                if (playing)
                    startPlaying();
            }
            else if (v == playButton) {
                if (fmHit && playing) {
                    FMButton.startAnimation(pause_fm);
                    stopPlaying();
                    DIGButton.startAnimation(dig_grow_to_pause);
                    fmHit = false;
                } else if (digHit && playing) {
                    stopPlaying();
                    DIGButton.startAnimation(pause_dig);
                    FMButton.startAnimation(fm_grow_to_pause);
                    digHit = false;
                } else if (!fmHit && !digHit && !playing) {
                    Context context = getApplicationContext();
                    CharSequence text = "Please select a station.";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();

                } else {
                    if (!playing) {
                        startPlaying();
                    }
                }
            }
        }
    }

    private void startPlaying() {
        Log.d("We over here now", "YES");
        playButton.setImageResource(R.drawable.pause1);
        exp.setPlayWhenReady(true);
    }

    private void stopPlaying() {
        Log.d("HELLO", "I'm tryna pause");
        playButton.setImageResource(R.drawable.play1);
        playerControl.pause();
        exp.release();
    }

    private void initializeUIElements() {

        currchan = FMurl;
        playSeekBar = (ProgressBar) findViewById(R.id.progressBar);
        playSeekBar.setMax(100);
        playSeekBar.setVisibility(View.INVISIBLE);
        playButton = (ImageButton) findViewById(R.id.Play);
        playButton.setOnClickListener(this);

        DIGButton = (ImageButton) findViewById(R.id.DIG);
        DIGButton.setOnClickListener(this);
        FMButton = (ImageButton) findViewById(R.id.FM);
        FMButton.setOnClickListener(this);
        initializeExoPlayer();


    }

    private void initializeExoPlayer(){
        Log.wtf("Hello?","Is it me you're coding for?");
        Uri FM_uri = Uri.parse(currchan); //Convert that to uri
        exp = ExoPlayer.Factory.newInstance(1); // new Exoplayer instance, only one render, as we're playing only audio, in case of video
        // we need two renders, one for audio and one for video
        DataSource dataSource = new DefaultUriDataSource(this, TAG); // this instance is reqd to pass data to exoplayer
        ExtractorSampleSource extractorSampleSource = new ExtractorSampleSource(FM_uri, dataSource, new DefaultAllocator(64 * 1024), 64 * 1024 * 256);
        //ExtractorSampleSource is used for mp3 or mp4, uri is passed, datasource is passed, a DefaultAllocator instance is also passed)
        // to know more about this go on exoplayers (see description for links)
        audioRenderer = new MediaCodecAudioTrackRenderer(extractorSampleSource, MediaCodecSelector.DEFAULT);
        //here we prepare audioRenderer by passing extractorSampleSource and MediaCodecSelector
        exp.prepare(audioRenderer);
        // finally we prepare player
        playerControl = new PlayerControl(exp);
        Log.d("Created: ", currchan);

    }
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
        pause_fm = AnimationUtils.loadAnimation(this, R.anim.pause_fm);
        dig_grow_to_pause = AnimationUtils.loadAnimation(this, R.anim.dig_grow_to_pause);
        pause_dig = AnimationUtils.loadAnimation(this, R.anim.pause_dig);
        fm_grow_to_pause = AnimationUtils.loadAnimation(this, R.anim.fm_grow_to_pause);
        setContentView(R.layout.activity_main);
        initializeUIElements();

        mDrawerList = (ListView)findViewById(R.id.navList);
        Log.d("LIST", " " + mDrawerList);
        addDrawerItems();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

    }

    private void addDrawerItems() {
        String[] osArray = {"Schedule", "Settings" };
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, osArray);
        mDrawerList.setAdapter(mAdapter);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("TEST", "ID " + id);
                if (id == 1) {
                    startActivity(new Intent(getApplicationContext(), settings.class));
                }
                else if (id == 0) {
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
        super.onDestroy();
        exp.release(); // important otherwise song will play even after app has closed.
    }
}
