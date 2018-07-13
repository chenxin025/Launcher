package com.eton.launcher.displaymode;

import android.content.Context;
import android.content.res.Resources;

import com.android.launcher.R;

public class LargeFont_3_3 extends DisplayFactory {

	public LargeFont_3_3 (Context c) {
		setCellCountXY(3, 3);
		setFolderCellCountXY(3, 3);
		setFolderMaxNumItems(9);
		mDatabaseName = "launcher_3_3.db";
        setHotseatCellCountX(3);
        
        final Resources res = c.getResources();
        mIconTitleGap = res.getInteger(R.integer.workSpace_cellItem_drawablePadding_3_3);
        mWorkspaceTextSize = res.getInteger(R.integer.workSpace_cellItem_textSize_3_3);
        mScrollingIndicatorHeight =  res.getInteger(R.integer.scrollingIndicator_3_3);
        mHotseatHeight = res.getInteger(R.integer.hotseat_layout_height_3_3);
		mWorkspaceId = R.xml.customize_workspace_3_3;
		mCellLayoutParams = res.getIntArray(R.array.celllayout_params_3_3);
		mFolderParams = res.getIntArray(R.array.folder_params_3_3);
		mHotseatParams = res.getIntArray(R.array.hotseat_params_3_3);
		mScrollingIndicatorHeight =  res.getInteger(R.integer.scrollingIndicator_3_3);
		mHotseatMaxCount = 3;
		
		mPostfixMsg = "_3_3";
		
		// added by liukaibang {{
		mCoverDrawableId = R.drawable.ic_cover_white_3_3;
		mAppBackGroundDrawableId = R.drawable.app_bg_0_3_3;
		mFolderIconBackGroundDrawableId = R.drawable.portal_ring_inner_holo_3_3;
		mPageIndicatorFocused = R.drawable.ic_pageindicator_focused_3_3;
		mPageIndicatorNormal = R.drawable.ic_pageindicator_nomal_3_3;
		mFolderIconItemTransX = res.getDimension(R.dimen.folder_icon_transx_3_3);
		mFolderContentPaddingLeft = res.getDimension(R.dimen.folder_content_paddingleft_3_3);
	    // end }}
		
		setDefaultScreen(c, 0);
	}
}
