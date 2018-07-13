package com.android.launcher2;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.launcher.R;
import com.android.launcher2.PagedView.PageSwitchListener;

public class ScrollingIndicator extends LinearLayout implements PageSwitchListener{
	Context mContext;
	private int mPageCount;
	private int mCurrentPage;
	
	// added by liukaibang {{ 
	private LayoutParams mIndicatorLayoutParams;
	// end }}
	
	
	public ScrollingIndicator(Context context) {
		this(context,null);
		init(context);
	}
	
	public ScrollingIndicator(Context context, AttributeSet attrs) {
		this(context, attrs,0);
		init(context);
	}
	
	public ScrollingIndicator(Context context, AttributeSet attrs, int defStyle) {
	    super(context, attrs, defStyle);
	    init(context);
	}
	 
	private void init(Context context){
	    mContext = context;
	    
	    // added by liukaibang {{ 
	    int margin = (int) getResources().getDimension(R.dimen.indicator_margin);
	    mIndicatorLayoutParams = new LinearLayout.LayoutParams(
	    		LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	    mIndicatorLayoutParams.setMargins(margin, 0, 0, 0);
	    // end }}
	}
	
	public void setPageCount(int count){
		 mPageCount = count;
	}
	
	public void setCurrentPage(int page){
	     mCurrentPage = page;
	}

	 public void addPageIndex(int index,int count){ 
		 removeAllViews();
		 for (int i = 0 ; i < count ; i++) {
			 ImageView view = new ImageView(mContext);

			 // Modified by liukaibang {{
			 /* Old Code
			 if (i == index) {
				 mCurrentPage = i;
				 view.setImageResource(R.drawable.ic_pageindicator_focused);
			 } else {
				 view.setImageResource(R.drawable.ic_pageindicator_nomal);
			 }
			 addView(view);*/
			 
			 // New Code
			 if (i == index) mCurrentPage = i;
			 view.setImageResource(LauncherApplication
					 .getDisplayFactory(mContext).getPageIndicatorId(i == index));
			 addView(view, mIndicatorLayoutParams);
			 // End }} 
		 }
	 }
	 
	 @Override
	 public void onPageSwitch(View newPage, int newPageIndex) {
		 if (mCurrentPage == newPageIndex && mPageCount == ((PagedView)newPage).getChildCount())
			 return;
		
		 if (mPageCount == ((PagedView)newPage).getChildCount()){
		 	 ImageView view = (ImageView)getChildAt(mCurrentPage);
		 	 
			 // view.setImageResource(R.drawable.ic_pageindicator_nomal);
			 view.setImageResource(LauncherApplication.
			 		getDisplayFactory(mContext).getPageIndicatorId(false));
			 
			 mCurrentPage = newPageIndex;
			 view = (ImageView)getChildAt(mCurrentPage);
			
			 // Modified by liukaibang {{
			 // view.setImageResource(R.drawable.ic_pageindicator_focused);
			 view.setImageResource(LauncherApplication.
					getDisplayFactory(mContext).getPageIndicatorId(true));
			 // End }}
		 } else {
			 mPageCount = ((PagedView)newPage).getChildCount();
			 addPageIndex(newPageIndex, mPageCount);
		 }
	 }
		
}
