package com.hy.htmlswipesample.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.hy.htmlswipesample.R;
import com.hy.htmlswipesample.sound.SamplePlayer;
import com.hy.htmlswipesample.sound.SoundFile;

import java.io.File;

/**
 * Created by lim2621 on 2015-06-09.
 */
public class ViewDialog extends Dialog implements View.OnClickListener {
    private static final String ACTION_KEY_TYPE = "ActionKeyType";
    private static final String ACTION_KEY_VALUE = "ActionKeyValue";

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

    private ImageButton mPlayButton;
    private ImageButton mRewindButton;

    public ViewDialog(Context context) {
        super(context);
        mContext = context;
        mHandler = new Handler();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_dialog);

        mWaveformView = (WaveformView) findViewById(R.id.waveform_native);
        //mWaveformView.setListener(this);
      /*  btn_recrod = (Button) findViewById(R.id.btn_rec);
        btn_recrod.setOnClickListener(this);
        btn_play = (Button) findViewById(R.id.btn_play);
        btn_play.setOnClickListener(this);*/

       /* DisplayMetrics metrics = new DisplayMetrics();
        mContext.getgetDefaultDisplay().getMetrics(metrics);*/
        DisplayMetrics metrics = new DisplayMetrics();
        mDensity = metrics.density;

        //recordAudio();
    }

    private void recordAudio() {
        mFile = null;
        mTitle = null;
        mArtist = null;

        mRecordingLastUpdateTime = getCurrentTime();
        mRecordingKeepGoing = true;
        mFinishActivity = false;
        final SoundFile.ProgressListener listener =
                new SoundFile.ProgressListener() {
                    public boolean reportProgress(double elapsedTime) {

                       /* ExampleAsyncTask aa = new ExampleAsyncTask();

                        aa.execute("a");
*/

                        return mRecordingKeepGoing;
                    }
                };

        // Record the audio stream in a background thread
        mRecordAudioThread = new Thread() {
            public void run() {
                mSoundFile = SoundFile.record(listener, getWindow().getDecorView().getWidth());
                mPlayer = new SamplePlayer(mSoundFile);
                finishOpeningSoundFile();
            }
        };
        mRecordAudioThread.start();
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
        resetPositions();
        if (mEndPos > mMaxPos)
            mEndPos = mMaxPos;

       /* mCaption = "체널:" + mSoundFile.getChannels() + "," +
                "프레임:" + mSoundFile.getFrameGains().length + "," +
                "샘플프래임:" + mSoundFile.getSamplesPerFrame() + "," +
                mSoundFile.getFiletype() + ", " +
                mSoundFile.getSampleRate() + " Hz, " +
                mSoundFile.getAvgBitrateKbps() + " kbps, " +
                formatTime(mMaxPos) + " " +
                mContext.getResources().getString(R.string.time_seconds);*/
        //  mInfo.setText(mCaption);

        updateDisplay();
    }
    /*private void showFinalAlert(Exception e, int messageResourceId) {
        showFinalAlert(e, mContext.getResources().getText(messageResourceId));
    }*/

    /**
     * 현재시간
     *
     * @return
     */
    private long getCurrentTime() {
        return System.nanoTime() / 1000000;
    }

    private synchronized void updateDisplay() {
        if (mIsPlaying) {
            int now = mPlayer.getCurrentPosition();
            int frames = mWaveformView.millisecsToPixels(now);
            mWaveformView.setPlayback(frames);//재생할때 노란색
            setOffsetGoalNoUpdate(frames - mWidth / 2);
            if (now >= mPlayEndMsec) {
                handlePause();
            }
        }

        if (!mTouchDragging) {
            int offsetDelta;

            if (mFlingVelocity != 0) {
                offsetDelta = mFlingVelocity / 30;
                if (mFlingVelocity > 80) {
                    mFlingVelocity -= 80;
                } else if (mFlingVelocity < -80) {
                    mFlingVelocity += 80;
                } else {
                    mFlingVelocity = 0;
                }

                mOffset += offsetDelta;

                if (mOffset + mWidth / 2 > mMaxPos) {
                    mOffset = mMaxPos - mWidth / 2;
                    mFlingVelocity = 0;
                }
                if (mOffset < 0) {
                    mOffset = 0;
                    mFlingVelocity = 0;
                }
                mOffsetGoal = mOffset;
            } else {
                offsetDelta = mOffsetGoal - mOffset;

                if (offsetDelta > 10)
                    offsetDelta = offsetDelta / 10;
                else if (offsetDelta > 0)
                    offsetDelta = 1;
                else if (offsetDelta < -10)
                    offsetDelta = offsetDelta / 10;
                else if (offsetDelta < 0)
                    offsetDelta = -1;
                else
                    offsetDelta = 0;

                mOffset += offsetDelta;
            }
        }

        mWaveformView.setParameters(mStartPos, mEndPos, mOffset);//움직이는 거 반영
        sendActionMsg(ACTION_TYPE_SETTEXT, "테스트");
    }

    private void setOffsetGoalNoUpdate(int offset) {
        if (mTouchDragging) {
            return;
        }

        mOffsetGoal = offset;
        if (mOffsetGoal + mWidth / 2 > mMaxPos)
            mOffsetGoal = mMaxPos - mWidth / 2;
        if (mOffsetGoal < 0)
            mOffsetGoal = 0;
    }

    /**
     * 위치초기화
     */
    private void resetPositions() {
        mStartPos = mWaveformView.secondsToPixels(0.0);
        mEndPos = mWaveformView.secondsToPixels(15.0);
    }

    /**
     * 시간
     *
     * @param pixels
     * @return
     */
    private String formatTime(int pixels) {
        if (mWaveformView != null && mWaveformView.isInitialized()) {
            return formatDecimal(mWaveformView.pixelsToSeconds(pixels));
        } else {
            return "";
        }
    }

    private String formatDecimal(double x) {
        int xWhole = (int) x;
        int xFrac = (int) (100 * (x - xWhole) + 0.5);

        if (xFrac >= 100) {
            xWhole++; //Round up
            xFrac -= 100; //Now we need the remainder after the round up
            if (xFrac < 10) {
                xFrac *= 10; //we need a fraction that is 2 digits long
            }
        }

        if (xFrac < 10)
            return xWhole + ".0" + xFrac;
        else
            return xWhole + "." + xFrac;
    }

    /**
     * 일시정지(안씀)
     */
    private synchronized void handlePause() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.pause();
        }
        mWaveformView.setPlayback(-1);
        mIsPlaying = false;
        enableDisableButtons();
    }

    /**
     * 버튼상태 변경(안씀)
     */
    private void enableDisableButtons() {
        if (mIsPlaying) {
            mPlayButton.setImageResource(android.R.drawable.ic_media_pause);
            mPlayButton.setContentDescription(mContext.getResources().getText(R.string.stop));
        } else {
            mPlayButton.setImageResource(android.R.drawable.ic_media_play);
            mPlayButton.setContentDescription(mContext.getResources().getText(R.string.play));
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
        /*    case R.id.btn_rec:
                btn_recrod.setSelected(!btn_recrod.isSelected());
                if (btn_recrod.isSelected()) {
                    recordAudio();
                } else {
                    mRecordingKeepGoing = false;
                }
                break;
            case R.id.btn_play:
                mPlayer.start();
                break;*/

        }
    }

    //핸들러 호출 함수
    private void sendActionMsg(int action, String value) {
        Message msg = mActionHandler.obtainMessage();

        Bundle bundle = new Bundle();
        bundle.putInt(ACTION_KEY_TYPE, action);
        bundle.putString(ACTION_KEY_VALUE, value);

        msg.setData(bundle);
        mActionHandler.sendMessage(msg);
    }

    private void sendActionMsg(int action, int value) {
        Message msg = mActionHandler.obtainMessage();

        Bundle bundle = new Bundle();
        bundle.putInt(ACTION_KEY_TYPE, action);
        bundle.putInt(ACTION_KEY_VALUE, value);

        msg.setData(bundle);
        mActionHandler.sendMessage(msg);
    }

    //핸들러
    public Handler mActionHandler = new Handler() {
        public void handleMessage(Message msg) {
            mWaveformView.invalidate();
         /*   Bundle data = msg.getData();
*/
           /* switch(data.getInt(ACTION_KEY_TYPE)) {
                case ACTION_TYPE_SETTEXT:
                    String strvalue = data.getString(ACTION_KEY_VALUE);
                    mTextView.setText(strvalue);

                    break;

                case ACTION_TYPE_SETSCROLL:
                    int intvalue = data.getInt(ACTION_KEY_VALUE);
                    mLayout.scrollTo(0, intvalue);

                    break;
            }*/
        }
    };

}