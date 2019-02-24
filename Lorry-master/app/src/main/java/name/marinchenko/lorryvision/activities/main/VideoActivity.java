package name.marinchenko.lorryvision.activities.main;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.util.ArrayList;

import name.marinchenko.lorryvision.R;
import name.marinchenko.lorryvision.activities.ToolbarAppCompatActivity;
import name.marinchenko.lorryvision.util.Initializer;
import name.marinchenko.lorryvision.util.net.WifiAgent;

import static name.marinchenko.lorryvision.services.NetScanService.MSG_DISCONNECTED;
import static name.marinchenko.lorryvision.services.NetScanService.MSG_RETURN_TO_MAIN;



public class VideoActivity extends ToolbarAppCompatActivity
                 {

    private String mMediaUrl = "http://192.168.1.104:8080/qwe";


    private SurfaceView mVideoSurface = null;
    private LibVLC mLibVLC = null;
    private MediaPlayer mMediaPlayer = null;

    public static final int JUMP_DELAY = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        this.messenger = new Messenger(new VideoIncomingHandler(this));

        Initializer.Video.init(this);

        //initWeb();

        mVideoSurface = (SurfaceView) findViewById(R.id.surface);
        ArrayList<String> options = new ArrayList<>();
          //  options.add("--aout=none");
            options.add("--swscale-mode=0");
        mLibVLC = new LibVLC(getApplicationContext(), options);
        mMediaPlayer =  new MediaPlayer(mLibVLC);



    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }


    void initLibVLC(){
        // LibVLC init

     /*   final ArrayList<String> options = new ArrayList<>();
            options.add("--aout=none");
            options.add("--swscale-mode=0");
        mLibVLC = new LibVLC(this, options);
        mMediaPlayer = new MediaPlayer(mLibVLC);

        //mVideoSurfaceFrame = (FrameLayout) findViewById(R.id.video_surface_frame);
        if (USE_SURFACE_VIEW) {
          //  mSurface = (SurfaceView) findViewById(R.id.surface);
            mVideoSurface = (SurfaceView) findViewById(R.id.surface);
            mVideoView = mVideoSurface;
            holder = mVideoSurface.getHolder();

          //  Log.d("size_test",String.format("surfaceView.width = %d",mVideoSurface.getWidth()));

        }
        */
    }



    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaPlayer.release();
        mLibVLC.release();
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (prefs.getBoolean(SettingsFragment.PREF_KEY_CURRENT_PROTOCOL,true))
            mMediaUrl = prefs.getString(SettingsFragment.PREF_KEY_HTTP_STR, getString(R.string.pref_http_default_value));
        else mMediaUrl = prefs.getString(SettingsFragment.PREF_KEY_RTSP_STR, getString(R.string.pref_rtsp_default_value));

     //   mMediaUrl = "rtsp://user:user@192.168.1.199:554/0";

        IVLCVout vout = mMediaPlayer.getVLCVout();
        vout.setVideoView(mVideoSurface);
        vout.attachViews();

        Media media = new Media(mLibVLC, Uri.parse(mMediaUrl));
        media.setHWDecoderEnabled(true, false);
        media.addOption(":network-caching=50");
        media.addOption(":clock-jitter=0");
        media.addOption(":clock-synchro=0");

        mMediaPlayer.setMedia(media);

        vout.addCallback(new IVLCVout.Callback() {
            @Override
            public void onSurfacesCreated(IVLCVout vlcVout) {
                int sw = getWindow().getDecorView().getWidth();
                int sh = getWindow().getDecorView().getHeight();

                if (sw * sh == 0) {
                    Log.e("VLC_size", "Invalid surface size");
                    return;
                }

              //  View bDisconnect = findViewById(R.id.activity_video_button_disconnect);
              //  View videoToolbar = findViewById(R.id.activity_video_toolbar);
               // sh = sh-bDisconnect.getHeight()-videoToolbar.getHeight();
              //  sw = sw - bDisconnect.getWidth();
                mMediaPlayer.getVLCVout().setWindowSize(sw, sh);
                mMediaPlayer.setAspectRatio("16:9");
                mMediaPlayer.setScale(0f);
            }

            @Override
            public void onSurfacesDestroyed(IVLCVout vlcVout) {

            }
        });

        mMediaPlayer.play();

    }

    @Override
    protected void onStop() {
        super.onStop();
        mMediaPlayer.stop();
        mMediaPlayer.getVLCVout().detachViews();
        //mMediaPlayer.getVLCVout().removeCallback(this);
    }


    private void stopPlaying(){

    }

    private void startPlaying(){
      /*  SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        String videoURL;

        if (prefs.getBoolean(SettingsFragment.PREF_KEY_CURRENT_PROTOCOL,true))
            videoURL = prefs.getString(SettingsFragment.PREF_KEY_HTTP_STR, getString(R.string.pref_http_default_value));
            else videoURL = prefs.getString(SettingsFragment.PREF_KEY_RTSP_STR, getString(R.string.pref_rtsp_default_value));

        videoURL = "http://192.168.1.100:8080/qwe";

        try {
            Media media = new Media(mLibVLC, Uri.parse(videoURL));

            // media.setEventListener();

            media.setHWDecoderEnabled(true,false);
            media.addOption(":network-caching=50");
            media.addOption(":clock-jitter=0");
            media.addOption(":clock-synchro=0");

            mMediaPlayer.setMedia(media);
            //  media.release();
            mMediaPlayer.play();
        } catch (Exception e){
            Toast toast = Toast.makeText(getApplicationContext(),
                    e.toString(), Toast.LENGTH_LONG);
            toast.show();
        }
        */
    }




    ///  From prototype

    /*private void initWeb(){
        final String targetURL = "https://www.youtube.com/embed/qyEzsAy4qeU";

        final WebView mWebView = findViewById(R.id.activity_video_webView_translation);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.getSettings().setAllowFileAccessFromFileURLs(true);
        mWebView.loadUrl(targetURL);
        mWebView.reload();

        Handler handler = new Handler();
        handler.postDelayed( new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl(
                        "javascript:(function() { document.getElementsByClassName('ytp-large-play-button ytp-button')[0].click(); })()"
                );

            }
        }, 5000);
    }

*/
    @Override
    public void onBackPressed() {
        WifiAgent.notifyDisconnectedManual(this);
        super.onBackPressed();
    }

    public void onButtonExitClick(View view) {
        onBackPressed();

    }


    protected static class VideoIncomingHandler extends ToolbarAppCompatActivity.IncomingHandler {

        VideoIncomingHandler(ToolbarAppCompatActivity activity) { super(activity); }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_RETURN_TO_MAIN:
                    this.activity.onBackPressed();
                    break;

                case MSG_DISCONNECTED:
                    this.activity.onBackPressed();
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    }
}
