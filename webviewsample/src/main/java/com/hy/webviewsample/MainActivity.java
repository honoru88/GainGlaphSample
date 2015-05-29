package com.hy.webviewsample;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.hy.webviewsample.event.WebViewInterface;


public class MainActivity extends ActionBarActivity {

    private WebView mWebView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setLayout();

        // 웹뷰에서 자바스크립트실행가능
        mWebView.getSettings().setJavaScriptEnabled(true);
        // 구글홈페이지 지정
        // mWebView.loadUrl("http://www.google.com");

/*        AssetManager am = getResources().getAssets();
        try {
            InputStream is = am.open("test.html");
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        mWebView.loadUrl("file:///android_asset/test.html");
        // WebViewClient 지정


        mWebView.setWebViewClient(new WebViewClientClass());
        mWebView.addJavascriptInterface(new WebViewInterface(this,mWebView), "Android"); // eventDetail : 클라이언트에서 사용할 이름

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.KITKAT){
            mWebView.setWebContentsDebuggingEnabled(true);
        }


    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private class WebViewClientClass extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    /*
     * Layout
     */
    private void setLayout() {
        mWebView = (WebView) findViewById(R.id.webview);
    }


    private class EventDetailJavascriptInterface {

        public void partEventAlert(String str) {

            Log.i("view", "click");

        }

    }


}
