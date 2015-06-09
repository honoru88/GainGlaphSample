package com.hy.htmlswipesample.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.hy.htmlswipesample.R;
import com.hy.htmlswipesample.sound.SamplePlayer;
import com.hy.htmlswipesample.sound.SoundFile;

import java.io.File;
import java.io.IOException;

/**
 * Created by lim2621 on 2015-06-09.
 */
public class ViewDialog extends Dialog implements View.OnClickListener, MediaPlayer.OnCompletionListener {
    private static final String ACTION_KEY_TYPE = "ActionKeyType";
    private static final String ACTION_KEY_VALUE = "ActionKeyValue";

    private static final int ACTION_TYPE_SETTEXT = 0;
    private static final int ACTION_TYPE_SETSCROLL = 1;

    private Button btn_recrod;
    private Button btn_play;
    private Button btn_stop;

    private File mFile;


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


    private WaveformView mWaveFormNavtive;
    private WaveformView mWaveFormMe;

    private SoundFile mNavtiveSoundFile;
    private SoundFile mMeSoundFile;


    private String mFilename;

    private Button btn_cancle;

    private Button btn_native_play;
    private Button btn_me_play;

    private MediaPlayer player;


    public ViewDialog(Context context, String filename) {
        super(context);
        mContext = context;
        mHandler = new Handler();
        mFilename = filename;


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_dialog);

        mWaveFormNavtive = (WaveformView) findViewById(R.id.waveform_native);
        mWaveFormNavtive.setCheck(0);
        mWaveFormMe = (WaveformView) findViewById(R.id.waveform_me);
        mWaveFormMe.setCheck(1);


        btn_cancle = (Button) findViewById(R.id.btn_cancle);
        btn_native_play = (Button) findViewById(R.id.btn_native_play);
        btn_me_play = (Button) findViewById(R.id.btn_me_play);

        btn_cancle.setOnClickListener(this);
        btn_native_play.setOnClickListener(this);
        btn_me_play.setOnClickListener(this);


    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        try {
            AssetManager assetMgr = mContext.getAssets();

            // mNavtiveSoundFile =  SoundFile.create(Environment.getExternalStorageDirectory()+"/my_001.mp4",getWindow().getDecorView().getWidth());
            mNavtiveSoundFile = new SoundFile(Environment.getExternalStorageDirectory() + "/" + mFilename + ".mp3", mWaveFormNavtive.getWidth());
            mMeSoundFile = new SoundFile(Environment.getExternalStorageDirectory() + "/" + mFilename + ".amr", mWaveFormMe.getWidth());

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SoundFile.InvalidInputException e) {
            e.printStackTrace();
        }
        mWaveFormNavtive.setSoundFile(mNavtiveSoundFile);
        mWaveFormMe.setSoundFile(mMeSoundFile);
        mWaveFormNavtive.invalidate();
        mWaveFormMe.invalidate();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_cancle:
                this.dismiss();
                break;
            case R.id.btn_native_play:


                btn_native_play.setSelected(!btn_native_play.isSelected());
                if (btn_native_play.isSelected()) {
                    Uri uri = Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/" + mFilename + ".mp3"));
                    if (player == null) {
                        player = MediaPlayer.create(mContext, uri);
                        player.setOnCompletionListener(this);
                        player.start();
                        btn_me_play.setClickable(false);
                    }
                } else {
                    player.stop();
                    player = null;
                    btn_me_play.setClickable(true);
                }

                break;

            case R.id.btn_me_play:
                btn_me_play.setSelected(!btn_me_play.isSelected());
                if (btn_me_play.isSelected()) {
                    Uri uri = Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/" + mFilename + ".amr"));
                    if (player == null) {
                        player = MediaPlayer.create(mContext, uri);
                        player.setOnCompletionListener(this);
                        player.start();
                        btn_native_play.setClickable(false);
                    } else {
                        player.stop();
                        player = null;
                        btn_native_play.setClickable(true);
                    }

                    break;
                }
        }
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        mp=null;
        btn_native_play.setSelected(false);
        btn_native_play.setClickable(true);
        btn_me_play.setSelected(false);
        btn_me_play.setClickable(true);


    }
}