/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.launcher2;


import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * M: An icon on a PagedView, specifically for items in the launcher's paged view (with compound
 * drawables on the top).
 */
public class TipsArchive extends FrameLayout{
	private static String TAG = "TipsArchive";
	public float mXDown = 0;
	public float mYDown = 0;
    public TipsArchive(final Context context) {
        super(context);
    }

    public TipsArchive(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public TipsArchive(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }
    
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		if (LauncherLog.DEBUG_MOTION) {
            LauncherLog.d(TAG, "dispatchTouchEvent: ev = " + event + ", mScrollX = " + getScrollX());
        }
		   switch (event.getAction()) {
	        case MotionEvent.ACTION_DOWN:
	            mXDown = event.getX();
	            mYDown = event.getY();
	            break;
	        case MotionEvent.ACTION_POINTER_UP:
	        case MotionEvent.ACTION_UP:
	        		if( getParent() != null && Math.abs(mXDown - event.getX()) > 20){
	        			((ViewGroup)getParent()).removeView(TipsArchive.this);
	        		}
	        }
//		   return true;
		   return super.dispatchTouchEvent(event);
	}   
@Override
public boolean onInterceptTouchEvent(MotionEvent event) {
	// TODO 自动生成的方法存根
	if (LauncherLog.DEBUG_MOTION) {
        LauncherLog.d(TAG, "onInterceptTouchEvent: ev = " + event + ", mScrollX = " + getScrollX());
    }
//	return true;
	return super.onInterceptTouchEvent(event);
}
	
@Override
public boolean onTouchEvent(MotionEvent event) {
	// TODO 自动生成的方法存根
	if (LauncherLog.DEBUG_MOTION) {
        LauncherLog.d(TAG, "onTouchEvent: ev = " + event + ", mScrollX = " + getScrollX());
    }
	return true;
}

}
