package name.marinchenko.lorryvision.activities.main;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.text.format.Formatter;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import name.marinchenko.lorryvision.R;
import name.marinchenko.lorryvision.activities.ToolbarAppCompatActivity;
import name.marinchenko.lorryvision.util.Initializer;
import name.marinchenko.lorryvision.util.net.WifiAgent;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;



import static name.marinchenko.lorryvision.services.NetScanService.MSG_DISCONNECTED;
import static name.marinchenko.lorryvision.services.NetScanService.MSG_RETURN_TO_MAIN;


public class VideoActivity extends ToolbarAppCompatActivity {


    private static final boolean USE_SURFACE_VIEW = true;
    private static final boolean ENABLE_SUBTITLES = true;
    private static final String TAG = "JavaActivity";
    private static final int SURFACE_BEST_FIT = 0;
    private static final int SURFACE_FIT_SCREEN = 1;
    private static final int SURFACE_FILL = 2;
    private static final int SURFACE_16_9 = 3;
    private static final int SURFACE_4_3 = 4;
    private static final int SURFACE_ORIGINAL = 5;
    private static int CURRENT_SIZE = SURFACE_BEST_FIT;

    private FrameLayout mVideoSurfaceFrame = null;
    private SurfaceView mVideoSurface = null;
    private SurfaceView mSubtitlesSurface = null;
    private TextureView mVideoTexture = null;
    private View mVideoView = null;

    private View.OnLayoutChangeListener mOnLayoutChangeListener = null;

    private LibVLC mLibVLC = null;
    private MediaPlayer mMediaPlayer = null;
    private int mVideoHeight = 0;
    private int mVideoWidth = 0;
    private int mVideoVisibleHeight = 0;
    private int mVideoVisibleWidth = 0;
    private int mVideoSarNum = 0;
    private int mVideoSarDen = 0;


    public static final int JUMP_DELAY = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        this.messenger = new Messenger(new VideoIncomingHandler(this));

        Initializer.Video.init(this);

        initWeb();
    }

    @Override
    public void onBackPressed() {
        WifiAgent.notifyDisconnectedManual(this);
        super.onBackPressed();
    }

    private void initWeb(){
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

    public void onButtonExitClick(View view) { onBackPressed(); }


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
