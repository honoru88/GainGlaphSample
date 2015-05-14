/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hy.gainglaphsample.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.ImageView;

/*드래그 시작 또는 끝 마커를 나타냅니다.

        대부분의 이벤트를 이용하여 클라이언트 클래스로 다시 전달
        청취자 인터페이스입니다.

        이 클래스는 직접하지만, 자신의 속도를 추적
        사용자로 가속하면 왼쪽 또는 오른쪽 화살표를 아래로 보유
        동안이 컨트롤이 초점을 맞추고 있습니다.*/
public class MarkerView extends ImageView {

    public interface MarkerListener {
        public void markerTouchStart(MarkerView marker, float pos);
        public void markerTouchMove(MarkerView marker, float pos);
        public void markerTouchEnd(MarkerView marker);
        public void markerFocus(MarkerView marker);
        public void markerLeft(MarkerView marker, int velocity);
        public void markerRight(MarkerView marker, int velocity);
        public void markerEnter(MarkerView marker);
        public void markerKeyUp();
        public void markerDraw();
    };

    private int mVelocity;
    private MarkerListener mListener;



    public MarkerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Make sure we get keys
        setFocusable(true);

        mVelocity = 0;
        mListener = null;
    }

    public void setListener(MarkerListener listener) {
        mListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            requestFocus();
            // We use raw x because this window itself is going to
            // move, which will screw up the "local" coordinates
            mListener.markerTouchStart(this, event.getRawX());
            break;
        case MotionEvent.ACTION_MOVE:
            // We use raw x because this window itself is going to
            // move, which will screw up the "local" coordinates
            mListener.markerTouchMove(this, event.getRawX());
            break;
        case MotionEvent.ACTION_UP:
            mListener.markerTouchEnd(this);
            break;
        }
        return true;
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction,
                                  Rect previouslyFocusedRect) {
        if (gainFocus && mListener != null)
            mListener.markerFocus(this);
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mListener != null)
            mListener.markerDraw();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        mVelocity++;
        int v = (int)Math.sqrt(1 + mVelocity / 2);
        if (mListener != null) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                mListener.markerLeft(this, v);
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                mListener.markerRight(this, v);
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                mListener.markerEnter(this);
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        mVelocity = 0;
        if (mListener != null)
            mListener.markerKeyUp();
        return super.onKeyDown(keyCode, event);
    }
}
