package com.hy.htmlswipesample;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import com.hy.htmlswipesample.html.ParsingHtml;
import com.hy.htmlswipesample.sound.SamplePlayer;
import com.hy.htmlswipesample.sound.SoundFile;
import com.hy.htmlswipesample.view.ViewDialog;
import com.hy.htmlswipesample.view.WaveformView;

import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class MainActivity extends Activity implements MediaPlayer.OnCompletionListener {

    private WebView mWebView;

    final private static String ACTION_KEY_TYPE = "ActionKeyType";
    final private static String ACTION_KEY_VALUE = "ActionKeyValue";
    final private static File SD_PATH = Environment.getExternalStorageDirectory();

    private static final int ACTION_TYPE_SETTEXT = 0;
    private static final int ACTION_TYPE_SETSCROLL = 1;

    private Button btn_recrod;
    private Button btn_play;
    private Button btn_stop;

    private File mFile;

    private String mFilename;
    private String mArtist;
    private String mTitle;
    private String mCaption = "";
    private String mInfoContent;


    private Context mContext;

    private long mLoadingLastUpdateTime;
    private long mRecordingLastUpdateTime;

    private boolean mLoadingKeepGoing;
    private boolean mRecordingKeepGoing;

    private double mRecordingTime;

    private Thread mLoadSoundFileThread;
    private Thread mRecordAudioThread;
    private Thread mSaveSoundFileThread;

    private AlertDialog mAlertDialog;

    private ProgressDialog mProgressDialog;

    private TextView mTimerTextView;

    private SoundFile mSoundFile;

    private Handler mHandler;

    private SamplePlayer mPlayer;

    private TextView mInfo;

    private WaveformView mWaveformView;

    private boolean mIsPlaying;
    private boolean mTouchDragging;
    private boolean mFinishActivity;

    private float mDensity;

    private int mWidth;
    private int mMaxPos;
    private int mStartPos;
    private int mEndPos;
    private int mLastDisplayedStartPos;
    private int mLastDisplayedEndPos;
    private int mOffset;
    private int mOffsetGoal;
    private int mFlingVelocity;
    private int mPlayStartMsec;
    private int mPlayEndMsec;

    private MediaRecorder mediaRecorder;
    private SharedPreferences pref;
    private MediaPlayer player;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        setLayout();
        mHandler = new Handler();

        // 웹뷰에서 자바스크립트실행가능
        mWebView.getSettings().setJavaScriptEnabled(true);

        mWebView.setWebViewClient(new WebViewClientClass());
        mWebView.addJavascriptInterface(this, "Android"); // eventDetail : 클라이언트에서 사용할 이름

        //mWebView.addJavascriptInterface(new WebViewInterface(), "Android"); // eventDetail : 클라이언트에서 사용할 이름
        ParsingHtml parsingHtml = new ParsingHtml(this);
        try {
            mWebView.loadDataWithBaseURL("file:///android_asset/", parsingHtml.test("file:///android_asset/test2.html"), "text/html", "UTF-8", "");
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //mWebView.loadUrl("file:///android_asset/test2.html");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mWebView.setWebContentsDebuggingEnabled(true);
        }


        pref = getSharedPreferences("pref", MODE_PRIVATE);
        if (!pref.getBoolean("isFirst", false)) {

            CopyAssets();
            SharedPreferences.Editor edit = pref.edit();
            edit.putBoolean("isFirst", true);
            edit.commit();
        }

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


    private void finishOpeningSoundFile() {
        mWaveformView.setSoundFile(mSoundFile);
        mWaveformView.recomputeHeights(mDensity);

        mMaxPos = mWaveformView.maxPos();
        mLastDisplayedStartPos = -1;
        mLastDisplayedEndPos = -1;

        mTouchDragging = false;

        mOffset = 0;
        mOffsetGoal = 0;
        mFlingVelocity = 0;
        if (mEndPos > mMaxPos)
            mEndPos = mMaxPos;


    }

    @JavascriptInterface
    public void recode(String str) {
        Log.i("메롱", "녹음" + SD_PATH.getAbsolutePath());
        if(mediaRecorder==null) {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            mediaRecorder.setOutputFile(SD_PATH.getAbsolutePath() + "/" + str + ".amr");
        }
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @JavascriptInterface
    public void rStop() {

        if (mediaRecorder == null)
            return;

        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
        Log.i("메롱", "멈춤");

    }

    @JavascriptInterface
    public void pStop() {

        if (mediaRecorder == null)
            return;

        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
        Log.i("메롱", "멈춤");

    }


    @JavascriptInterface
    public void play(String str) {
        Log.i("메롱", "플레이");

        Uri uri = Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/" + str + ".mp3"));
        if (player == null) {
            player = MediaPlayer.create(MainActivity.this, uri);
            player.setOnCompletionListener(this);

        }
        player.start();
    }

    @JavascriptInterface
    public void popup(String str) {
        Log.i("메롱", "팝업");
        ViewDialog dialog = new ViewDialog(mContext, str);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

    }

    @JavascriptInterface
    public void pause() {
        Log.i("메롱", "팝업");
        if (player != null) {
            player.pause();
        }
    }

    @JavascriptInterface
    public void nextpage() {
        Log.i("메롱", "nextpage");
        btnInit();
    }

    @JavascriptInterface
    public void prevpage() {
        Log.i("메롱", "prevpage");
        btnInit();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    /*private File makeFile(File dir, String file_path) {
        File file = null;
        boolean isSuccess = false;
        if (dir.isDirectory()) {
            file = new File(file_path);
            if (file != null && !file.exists()) {
                Log.i("메롱", "!file.exists");
                try {
                    isSuccess = file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    Log.i("메롱", "파일생성 여부 = " + isSuccess);
                }
            } else {
                Log.i("메롱", "file.exists");
            }
        }
        return file;
    }*/


    private void CopyAssets() {
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list("mp3");

            //이미지만 가져올때 files = assetManager.list("image");

        } catch (IOException e) {
            Log.e("tag", e.getMessage());
        }
        for (int i = 0; i < files.length; i++) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open("mp3/" + files[i]);

                out = new FileOutputStream(Environment.getExternalStorageDirectory() + "/" + files[i]);
                copyFile(in, out);
                in.close();
                in = null;
                out.flush();
                out.close();
                out = null;
            } catch (Exception e) {
                Log.e("tag", e.getMessage());
            }
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }

    }

    /**
     * 버튼 초기화
     */
    private void btnInit() {
        if (player != null) {
            if (player.isPlaying())
                player.stop();
            player = null;
        }
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl("javascript:btnInit()");
            }
        });

    }

    /**
     * 플레이어 리스너
     *
     * @param mp
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl("javascript:pStop()");
            }
        });

    }
}
