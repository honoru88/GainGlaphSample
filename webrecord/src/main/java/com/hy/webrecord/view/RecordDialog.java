package com.hy.webrecord.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.ImageButton;
import android.widget.TextView;

import com.hy.webrecord.R;
import com.hy.webrecord.sound.SamplePlayer;
import com.hy.webrecord.sound.SoundFile;

import java.io.File;

/**
 * Created by lim2621 on 2015-05-29.
 */


public class RecordDialog extends Dialog {
    private File mFile;
    private String mFilename;
    private String mArtist;
    private String mTitle;
    private Context mContext;
    private long mLoadingLastUpdateTime;
    private boolean mLoadingKeepGoing;
    private long mRecordingLastUpdateTime;
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

    private String mInfoContent;
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
    private String mCaption = "";

    private ImageButton mPlayButton;
    private ImageButton mRewindButton;
    private ImageButton mFfwdButton;

    public RecordDialog(Context context) {
        super(context);
        mContext = context;
    }


    private void recordAudio() {
        mFile = null;
        mTitle = null;
        mArtist = null;

        mRecordingLastUpdateTime = getCurrentTime();
        mRecordingKeepGoing = true;
        mFinishActivity = false;
        AlertDialog.Builder adBuilder = new AlertDialog.Builder(mContext);
        adBuilder.setTitle(mContext.getResources().getText(R.string.progress_dialog_recording));
        adBuilder.setCancelable(true);
        adBuilder.setNegativeButton(
                mContext.getResources().getText(R.string.progress_dialog_cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mRecordingKeepGoing = false;
                        mFinishActivity = true;
                    }
                });
        adBuilder.setPositiveButton(
                mContext.getResources().getText(R.string.progress_dialog_stop),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mRecordingKeepGoing = false;
                    }
                });
        // TODO(nfaralli): try to use a FrameLayout and pass it to the following inflate call.
        // Using null, android:layout_width etc. may not work (hence text is at the top of view).
        // On the other hand, if the text is big enough, this is good enough.
        //adBuilder.setView(this.inflate(R.layout.record_audio, null));
        mAlertDialog = adBuilder.show();
        mTimerTextView = (TextView) mAlertDialog.findViewById(R.id.record_audio_timer);

        final SoundFile.ProgressListener listener =
                new SoundFile.ProgressListener() {
                    public boolean reportProgress(double elapsedTime) {

                        ExampleAsyncTask aa = new ExampleAsyncTask();

                        aa.execute("a");


                        return mRecordingKeepGoing;
                    }
                };

        // Record the audio stream in a background thread
        mRecordAudioThread = new Thread() {
            public void run() {
                //  try {
                mSoundFile = SoundFile.record(listener, mWidth);
               /*     if (mSoundFile == null) {
                        mAlertDialog.dismiss();
                        Runnable runnable = new Runnable() {
                            public void run() {
                                showFinalAlert(
                                        new Exception(),
                                        mContext.getResources().getText(R.string.record_error)
                                );
                            }
                        };
                        mHandler.post(runnable);*/
                // return;
                // }
                mPlayer = new SamplePlayer(mSoundFile);
                //  } catch (final Exception e) {
                  /*  mAlertDialog.dismiss();
                    e.printStackTrace();
                    mInfoContent = e.toString();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            mInfo.setText(mInfoContent);
                        }
                    });*/

                   /* Runnable runnable = new Runnable() {
                        public void run() {
                            showFinalAlert(e, mContext.getResources().getText(R.string.record_error));
                        }
                    };
                    mHandler.post(runnable);*/
                  /*  return;
                }*/
                mAlertDialog.dismiss();
                if (mFinishActivity) {
                    //MainActivity.this.finish();
                } else {
                    Runnable runnable = new Runnable() {
                        public void run() {
                            finishOpeningSoundFile();
                        }
                    };
                    mHandler.post(runnable);
                }
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

        mCaption = "체널:" + mSoundFile.getChannels() + "," +
                "프레임:" + mSoundFile.getFrameGains().length + "," +
                "샘플프래임:" + mSoundFile.getSamplesPerFrame() + "," +
                mSoundFile.getFiletype() + ", " +
                mSoundFile.getSampleRate() + " Hz, " +
                mSoundFile.getAvgBitrateKbps() + " kbps, " +
                formatTime(mMaxPos) + " " +
                mContext.getResources().getString(R.string.time_seconds);
        mInfo.setText(mCaption);

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
        mWaveformView.invalidate();

       /* mStartMarker.setContentDescription(
                getResources().getText(R.string.start_marker) + " " +
                        formatTime(mStartPos));
        mEndMarker.setContentDescription(
                getResources().getText(R.string.end_marker) + " " +
                        formatTime(mEndPos));

        int startX = mStartPos - mOffset - mMarkerLeftInset;
        if (startX + mStartMarker.getWidth() >= 0) {
            if (!mStartVisible) {
                // Delay this to avoid flicker
                mHandler.postDelayed(new Runnable() {
                    public void run() {
                        mStartVisible = true;
                        mStartMarker.setAlpha(1f);
                    }
                }, 0);
            }
        } else {
            if (mStartVisible) {
                mStartMarker.setAlpha(0f);
                mStartVisible = false;
            }
            startX = 0;
        }

        int endX = mEndPos - mOffset - mEndMarker.getWidth() + mMarkerRightInset;
        if (endX + mEndMarker.getWidth() >= 0) {
            if (!mEndVisible) {
                // Delay this to avoid flicker
                mHandler.postDelayed(new Runnable() {
                    public void run() {
                        mEndVisible = true;
                        mEndMarker.setAlpha(1f);
                    }
                }, 0);
            }
        } else {
            if (mEndVisible) {
                mEndMarker.setAlpha(0f);
                mEndVisible = false;
            }
            endX = 0;
        }

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(
                startX,
                mMarkerTopOffset,
                -mStartMarker.getWidth(),
                -mStartMarker.getHeight());
        mStartMarker.setLayoutParams(params);//왼쪽 마커 위치 이동

        params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(
                endX,
                mWaveformView.getMeasuredHeight() - mEndMarker.getHeight() - mMarkerBottomOffset,
                -mStartMarker.getWidth(),
                -mStartMarker.getHeight());
        mEndMarker.setLayoutParams(params);//오른쪽 마커 위치이동
        mEndMarker.setVisibility(View.INVISIBLE);
        mStartMarker.setVisibility(View.INVISIBLE);*/


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
     * 일시정지
     */
    private synchronized void handlePause() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.pause();
        }
        mWaveformView.setPlayback(-1);
        mIsPlaying = false;
        enableDisableButtons();
    }

    private void enableDisableButtons() {
        if (mIsPlaying) {
            mPlayButton.setImageResource(android.R.drawable.ic_media_pause);
            mPlayButton.setContentDescription(mContext.getResources().getText(R.string.stop));
        } else {
            mPlayButton.setImageResource(android.R.drawable.ic_media_play);
            mPlayButton.setContentDescription(mContext.getResources().getText(R.string.play));
        }
    }

    class ExampleAsyncTask extends AsyncTask<String, Integer, Long> {

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Long result) {

            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {

            super.onProgressUpdate(values);
        }

        @Override
        protected Long doInBackground(String... params) {
            long result = 0;

            return result;
        }
    }
}
