package com.hy.websubtitle;

import android.app.Activity;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;


public class MainActivity extends Activity {
    private WebView webview;
    private Button btn_refresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webview = (WebView) this.findViewById(R.id.webview);
        btn_refresh= (Button)findViewById(R.id.btn_refresh);
        btn_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                webview.loadUrl("file:///android_asset/test2/test.html");

            }
        });
        WebSettings settings = webview.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccessFromFileURLs(true); //Maybe you don't need this rule
        settings.setAllowUniversalAccessFromFileURLs(true);
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.KITKAT){
            webview.setWebContentsDebuggingEnabled(true);
        }

        webview.loadUrl("file:///android_asset/test2/test.html");
    }

}
