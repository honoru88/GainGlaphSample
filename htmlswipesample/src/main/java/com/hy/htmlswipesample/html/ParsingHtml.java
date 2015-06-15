package com.hy.htmlswipesample.html;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by lim2621 on 2015-06-01.
 */
public class ParsingHtml {
    private Context mContext;

    public ParsingHtml(Context mContext) {
        this.mContext = mContext;

    }


    /**
     * url내용을 String 으로
     *
     * @param urlToRead
     * @return
     */
    public String getHTML(String urlToRead) throws IOException {

        InputStream is = mContext.getAssets().open("test2.html");
        int size = is.available();

        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();

        String str = new String(buffer);


        return str;
    }

    /**
     * 인터넷 html 가져오기
     * @param urlToRead
     * @return
     */
    public String getUrlHTML(String urlToRead) {
        URL url; // The URL to read
        HttpURLConnection conn; // The actual connection to the web page
        BufferedReader rd; // Used to read results from the web page
        String line; // An individual line of the web page HTML
        String result = ""; // A long string containing all the HTML
        try {
            url = new URL(urlToRead);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line = rd.readLine()) != null) {
                result += line;
            }
            rd.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * json load
     *
     * @return
     */
    public JSONArray getJsonObj() {
        final JSONArray jArray = new JSONArray();


        JSONObject jObject = new JSONObject();
        try {
            jObject.put("id", "a1");
            jObject.put("type", "bold");
            jObject.put("value", "1.Humpty Dumpty sat on a wall.");

            jArray.put(jObject);

            jObject = new JSONObject();

            jObject.put("id", "a2");
            jObject.put("type", "img");
            jObject.put("value", "http://file.yoons.com//YoonsCom_test2/test/img/my_001.png");
            jArray.put(jObject);

            jObject = new JSONObject();

            jObject.put("id", "b1");
            jObject.put("type", "bold");
            jObject.put("value", "2.Humpty Dumpty had a great fall..");

            jArray.put(jObject);

            jObject = new JSONObject();

            jObject.put("id", "b2");
            jObject.put("type", "img");
            jObject.put("value", "http://file.yoons.com//YoonsCom_test2/test/img/my_002.png");

            jArray.put(jObject);

            jObject = new JSONObject();

            jObject.put("id", "c1");
            jObject.put("type", "bold");
            jObject.put("value", "3.Humpty Dumpty was lazy.");

            jArray.put(jObject);

            jObject = new JSONObject();

            jObject.put("id", "c2");
            jObject.put("type", "img");
            jObject.put("value", "http://file.yoons.com//YoonsCom_test2/test/img/my_003.png");

            jArray.put(jObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jArray;
    }

    /**
     * html 내용을 json내용하고 비교해서 변경
     *
     * @param strHtml
     * @param jsonArray
     * @return
     * @throws JSONException
     */
    public String exChangeHtml(String strHtml, JSONArray jsonArray) throws JSONException {


        String value = null;
        String id = null;


        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String str_tag = null;
            if ("img".equals(jsonObject.getString("type"))) {
                str_tag = "<img src=" + jsonObject.get("value").toString() + " style= 'width: 50%; padding-top: 10px;'>";
            } else if ("bold".equals(jsonObject.getString("type"))) {
                str_tag = "<b>" + jsonObject.get("value").toString() + "</b>";
            } else if ("txt".equals(jsonObject.getString("type"))) {
                str_tag = "<input type value=" + jsonObject.get("value").toString() + " style= 'width: 150px;'>";
            }
            id = "*" + jsonObject.get("id").toString() + "*";

            strHtml = strHtml.replace(id, str_tag);
        }


        return strHtml;

    }

    public String test(String strHtml) throws JSONException, IOException {
        String value = exChangeHtml(getHTML(strHtml), getJsonObj());

        Log.i("리턴", value);
        return value;
    }


}
