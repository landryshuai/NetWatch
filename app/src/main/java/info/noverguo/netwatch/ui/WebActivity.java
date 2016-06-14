package info.noverguo.netwatch.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import info.noverguo.netwatch.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import info.noverguo.netwatch.ui.view.ProgressWebView;

/**
 * Created by noverguo on 2016/5/29.
 */

public class WebActivity extends AppCompatActivity {
    private static final String KEY_URL = "KEY_URL";
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.webView)
    ProgressWebView webview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        String url = getIntent().getStringExtra(KEY_URL);
        if (TextUtils.isEmpty(url)) {
            Toast.makeText(getApplicationContext(), "url不能为空", Toast.LENGTH_SHORT).show();
            finish();
        }
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setSupportZoom(true);
        webview.getSettings().setBuiltInZoomControls(true);
        Log.i("MA", "---------------===================loadUrl: " + url);
//        webview.loadUrl(url);
        webview.setWebViewClient(new WebViewClient(){

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        webview.loadUrl(url);
    }

    public static void showUrl(Context context, String url) {
        context.startActivity(new Intent(context, WebActivity.class).putExtra(KEY_URL, url));
    }

}
