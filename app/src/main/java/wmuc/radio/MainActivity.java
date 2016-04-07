package wmuc.radio;


import android.app.ActionBar;
import android.content.Intent;
import android.media.AudioManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.Button;
import android.view.View;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements OnClickListener {


    public final String FMurl = "http://wmuc.umd.edu:8000/wmuc-hq";
    public final String Digitalurl = "http://wmuc.umd.edu:8000/wmuc2-high";
    private String currchan;
    private ImageView image;
    private TextView buff;
    private TextView np;
    private Button playButton;
    private Button DIGButton;
    private Button FMButton;
    private ProgressBar playSeekBar;
    private MediaPlayer player;
    private boolean enableButton = true;


    public void onClick(View v) {
        if(enableButton) {
            if(playButton.getText().equals("Play")) {
                if (v == DIGButton) {
                    currchan = Digitalurl;
                    np.setText("Digital");
                    initializeMediaPlayer();
                }
                if (v == FMButton) {
                    currchan = FMurl;
                    np.setText("FM");
                    initializeMediaPlayer();
                }
            }
            if (v == playButton) {
                if (playButton.getText().equals("Play")) {
                    startPlaying();
                } else if (playButton.getText().equals("Stop")) {
                    stopPlaying();
                }
            }
        }

    }

    private void startPlaying() {

        enableButton = false;

        playSeekBar.setVisibility(View.VISIBLE);
        buff.setVisibility(View.VISIBLE);

        player.prepareAsync();

        player.setOnPreparedListener(new OnPreparedListener() {

            public void onPrepared(MediaPlayer mp) {
                player.start();
                playButton.setText("Stop");
                image.setImageResource(R.drawable.ps);
                playSeekBar.setVisibility(View.INVISIBLE);
                buff.setVisibility(View.INVISIBLE);
                enableButton = true;

            }
        });

    }

    private void stopPlaying() {
        playButton.setText("Play");
        image.setImageResource(R.drawable.os);
        playSeekBar.setVisibility(View.INVISIBLE);
        buff.setVisibility(View.INVISIBLE);
        if (player.isPlaying()) {
            player.stop();
            player.release();
            player = null;
            initializeMediaPlayer();
        }
    }

    private void initializeMediaPlayer() {
        player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            player.setDataSource(currchan);
        } catch (Exception e) {
            e.printStackTrace();
        }

        player.setOnBufferingUpdateListener(new OnBufferingUpdateListener() {

            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                //playSeekBar.setSecondaryProgress(percent);
                playSeekBar.setProgress(percent);

            }
        });
    }

    private void initializeUIElements() {

        currchan = FMurl;


        playSeekBar = (ProgressBar) findViewById(R.id.progressBar1);
        playSeekBar.setMax(100);
        playSeekBar.setVisibility(View.INVISIBLE);

        playButton = (Button) findViewById(R.id.Play);
        playButton.setOnClickListener(this);

        DIGButton = (Button) findViewById(R.id.DIG);
        DIGButton.setOnClickListener(this);
        FMButton = (Button) findViewById(R.id.FM);
        FMButton.setOnClickListener(this);

        image = (ImageView) findViewById(R.id.bg);

        buff = (TextView) findViewById(R.id.buff);
        np = (TextView) findViewById(R.id.np);
        buff.setVisibility(View.INVISIBLE);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeUIElements();

        initializeMediaPlayer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(getApplicationContext(), settings.class));
            return  true;
        }
        if (id == R.id.action_schedule) {
            startActivity(new Intent(getApplicationContext(), Schedule.class));
            return  true;
        }

        return super.onOptionsItemSelected(item);
    }
}
