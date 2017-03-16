package appinventor.ai_bengg.WMUC_Radio;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecSelector;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;

/**
 * Created by Joseph on 11/14/2016.
 */
public class StreamingService extends Service{

    private ExoPlayer exp;
    private MediaCodecAudioTrackRenderer audioRenderer ;
    private final String TAG = "wmuc";

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent!=null) {
            initializeExoPlayer(intent.getData());
            exp.setPlayWhenReady(true);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        exp.setPlayWhenReady(false);

        super.onDestroy();

    }

    private void initializeExoPlayer(Uri channel){
        Log.wtf("Hello?","Is it me you're coding for?");
        exp = ExoPlayer.Factory.newInstance(1,1000,0); // new Exoplayer instance, only one render, as we're playing only audio, in case of video
        // we need two renders, one for audio and one for video
        DefaultUriDataSource dataSource = new DefaultUriDataSource(this, TAG); // this instance is reqd to pass data to exoplayer
        ExtractorSampleSource extractorSampleSource = new ExtractorSampleSource(channel, dataSource, new DefaultAllocator(64 * 1024), 64 * 1024 * 256);
        //ExtractorSampleSource is used for mp3 or mp4, uri is passed, datasource is passed, a DefaultAllocator instance is also passed)
        // to know more about this go on exoplayers (see description for links)
        audioRenderer = new MediaCodecAudioTrackRenderer(extractorSampleSource, MediaCodecSelector.DEFAULT);
        //here we prepare audioRenderer by passing extractorSampleSource and MediaCodecSelector
        exp.prepare(audioRenderer);

    }
}
