package com.eton.launcher.displaymode;

import android.content.Context;
import android.content.res.Resources;

import com.android.launcher.R;

public class NormalMode extends DisplayFactory {

	public NormalMode (Context c) {
		setCellCountXY(4, 4);
		setFolderCellCountXY(3, 4);
        setFolderMaxNumItems(12);
        setHotseatCellCountX(4);
		mDatabaseName = "launcher.db";
		mWorkspaceId = R.xml.default_workspace;
		
		final Resources res = c.getResources();
		mIconTitleGap = res.getInteger(R.integer.workSpace_cellItem_drawablePadding_4_4);
        mWorkspaceTextSize = res.getInteger(R.integer.workSpace_cellItem_textSize_4_4);
		mScrollingIndicatorHeight =  res.getInteger(R.integer.scrollingIndicator_4_4);
		mHotseatHeight = res.getInteger(R.integer.hotseat_layout_height_4_4);
		mCellLayoutParams = res.getIntArray(R.array.celllayout_params_4_4);
		mFolderParams = res.getIntArray(R.array.folder_params_4_4);
		mHotseatParams = res.getIntArray(R.array.hotseat_params_4_4);
		mScrollingIndicatorHeight =  res.getInteger(R.integer.scrollingIndicator_4_4);
		mHotseatMaxCount = 4;
		
		mPostfixMsg = "";
		
		// added by liukaibang {{
		mCoverDrawableId = R.drawable.ic_cover_white;
		mAppBackGroundDrawableId = R.drawable.app_bg_0;
		mFolderIconBackGroundDrawableId = R.drawable.portal_ring_inner_holo;
		mPageIndicatorFocused = R.drawable.ic_pageindicator_focused;
		mPageIndicatorNormal = R.drawable.ic_pageindicator_nomal;
		mFolderIconItemTransX = res.getDimension(R.dimen.folder_icon_transx);
		mFolderContentPaddingLeft = res.getDimension(R.dimen.folder_content_paddingleft);
	    // end }}
		
		setDefaultScreen(c, 0);
	}
}
