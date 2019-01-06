package name.marinchenko.lorryvision.activities.main;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.text.format.Formatter;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import name.marinchenko.lorryvision.R;
import name.marinchenko.lorryvision.activities.ToolbarAppCompatActivity;
import name.marinchenko.lorryvision.util.Initializer;
import name.marinchenko.lorryvision.util.net.WifiAgent;

import static name.marinchenko.lorryvision.services.NetScanService.MSG_DISCONNECTED;
import static name.marinchenko.lorryvision.services.NetScanService.MSG_RETURN_TO_MAIN;


public class VideoActivity extends ToolbarAppCompatActivity {

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
