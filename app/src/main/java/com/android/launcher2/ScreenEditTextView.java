package com.android.launcher2;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;


public class ScreenEditTextView extends TextView {

	

	public ScreenEditTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		this.setTextColor(Color.WHITE);
	}

	public ScreenEditTextView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		this.setTextColor(Color.WHITE);
	}
	
	public ScreenEditTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		this.setTextColor(Color.WHITE);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		
		if (event.getAction() == MotionEvent.ACTION_DOWN){
			//TODO set background and Text size or color
			this.setTextColor(Color.RED);
			
		}else if (event.getAction() == MotionEvent.ACTION_UP){
			this.setTextColor(Color.WHITE);
			
		}
		return super.onTouchEvent(event);
	}
	
	
	
}
