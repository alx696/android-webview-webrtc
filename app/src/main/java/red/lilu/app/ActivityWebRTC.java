package red.lilu.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import red.lilu.app.databinding.ActivityWebrtcBinding;

public class ActivityWebRTC extends AppCompatActivity {

    private static final String T = "调试";
    private static final int REQUEST_CODE_PERMISSION = 1;
    private ActivityWebrtcBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityWebrtcBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        setSupportActionBar(b.toolbar);

        init();
    }

    @Override
    protected void onPause() {
        super.onPause();

        try {
            //
        } catch (Exception e) {
            Log.w(T, e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                init();
            } else {
                Toast.makeText(getApplicationContext(), "没有权限!", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    void init() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_PERMISSION
            );
            return;
        }

        try {
            /*
            除了RECORD_AUDIO和CAMERA权限, 要想录音还要声明MODIFY_AUDIO_SETTINGS权限.
             */
            WebView webView = b.web;
            WebSettings webViewSettings = webView.getSettings();
            webViewSettings.setSafeBrowsingEnabled(false);
            webViewSettings.setJavaScriptEnabled(true);
            webViewSettings.setDomStorageEnabled(true);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                    // 忽略证书错误
                    handler.proceed();
                }
            });
            webView.setWebChromeClient(new WebChromeClient() {
                @Override
                public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                    Log.d(T, String.format("%s:%d %s", consoleMessage.sourceId(), consoleMessage.lineNumber(), consoleMessage.message()));
                    return true;
                }

                @Override
                public void onPermissionRequest(PermissionRequest request) {
                    // 参考 https://github.com/googlearchive/chromium-webview-samples/blob/master/webrtc-example/app/src/main/java/com/google/chrome/android/webrtcsample/MainActivity.java#L144
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (request.getOrigin().toString().equals("https://27.17.7.86:81/")) {
                                request.grant(request.getResources());
                            } else {
                                request.deny();
                            }
                        }
                    });
                }
            });
            webView.loadUrl("https://27.17.7.86:81/demo/rtc/");
            Log.d(T, "加载网页");
        } catch (Exception e) {
            Log.w(T, e);
        }
    }
}
