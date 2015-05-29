package com.hy.webviewsample.event;

import android.app.Activity;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lim2621 on 2015-05-29.
 */
public class WebViewInterface {

    private WebView mAppView;
    private Activity mContext;

    /**
     * 생성자.
     *
     * @param activity : context
     * @param view     : 적용될 웹뷰
     */
    public WebViewInterface(Activity activity, WebView view) {
        mAppView = view;
        mContext = activity;
    }

    /**
     * 안드로이드 토스트를 출력한다. Time Long.
     *
     * @param message : 메시지
     */
    @JavascriptInterface
    public void toastLong(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
    }

    /**
     * 안드로이드 토스트를 출력한다. Time Short.
     */
    @JavascriptInterface
    public void toastShort(String strJsonObj) { // Show toast for a short time

        try {

            JSONObject jsonObj = new JSONObject(strJsonObj);

            final String name = jsonObj.get("name").toString();
            final String play = jsonObj.get("play").toString();
            mContext.runOnUiThread(new Runnable() {

                public void run() {
                    Toast.makeText(mContext, "이름:" + name + ",하는일:" + play, Toast.LENGTH_SHORT).show();
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * Json데이터 load
     */
    @JavascriptInterface
    public void dataLoad() {
        String myJSONData = "place your data here";
        JSONObject objJSON = null;
        String webData = null;
        try {
            objJSON = new JSONObject(myJSONData);

            JSONObject subObj = objJSON.getJSONObject("MenuContent");
            webData = subObj.getString("menuPageContent");
            mAppView.loadData(webData, "text/html; charset=UTF-8", null);


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @JavascriptInterface
    public void getValueJson() throws JSONException {
        final JSONArray jArray = new JSONArray();


        JSONObject jObject = new JSONObject();

        jObject.put("Name", "Jackson");
        jObject.put("Age", "24");
        jArray.put(jObject);

        jObject = new JSONObject();
        jObject.put("Name", "Jenny");
        jObject.put("Age", "23");
        jArray.put(jObject);

        jObject = new JSONObject();
        jObject.put("Name", "Fenny");
        jObject.put("Age", "28");
        jArray.put(jObject);

        final JSONArray result = jArray;


        mContext.runOnUiThread(new Runnable() {

            public void run() {
                mAppView.loadUrl("javascript:setJson(" + result + ")");
            }
        });


    }


}