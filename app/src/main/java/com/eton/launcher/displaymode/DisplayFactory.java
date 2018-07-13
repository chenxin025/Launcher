package com.eton.launcher.displaymode;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.launcher2.SharedPreferencesUtils;

public abstract class DisplayFactory {
    
    private static final String TAG = "DisplayMode";
    
    public static final int MODE_4_4 = 0;
    public static final int MODE_3_3 = 1;
    public static final int MODE_4_3 = 2;
    public static final int MODE_5_4 = 3;
    
    protected static int mCurrentMode = -1;

    // state defined in LauncherModel
    protected int mCellCountX;
    protected int mCellCountY;
    // db name corresponding different mode
    protected String mDatabaseName;
    // the resource id of customized screens
    protected int mWorkspaceId;
    // represent whether the mode has changed
    protected boolean mChanged;
    
    protected String MODE_CHANGE = "MODE_CHANGE";
    // postfix of customize icons
    protected String mPostfixMsg;
    
    // the state whether db is inited.
    protected boolean mDatabaseInited = false;
    
    // View params of workspace
    protected int[] mCellLayoutParams;
    // view params of folder
    protected int[] mFolderParams;
    // view params of hotseat
    protected int[] mHotseatParams;
    
    protected int mFolderMaxCountX;
    protected int mFolderMaxCountY;
    protected int mFolderMaxNumItems;
    
    protected int mHotseatCellCountX;
    protected int mHotseatHeight;
    protected int mScrollingIndicatorHeight;
    protected int mHotseatMaxCount;
    protected int mIconTitleGap;
    protected int mWorkspaceTextSize;
    protected int mWorkspaceCellWidth;
    protected int mWorkspaceCellHeight;

    public int getWorkspaceTextSize() {
        return mWorkspaceTextSize;
    }

    public int getIconTitleGap() {
        return mIconTitleGap;
    }
    
    // added by liukaibang {{
    protected int mCoverDrawableId;
    protected int mAppBackGroundDrawableId;
    protected int mFolderIconBackGroundDrawableId;
    protected int mPageIndicatorNormal;
    protected int mPageIndicatorFocused;
    protected float mFolderIconItemTransX;
    protected float mFolderContentPaddingLeft;
    // end }}
    
    public int getScrollingIndicatorHeight() {
        return mScrollingIndicatorHeight;
    }
    
    public boolean isDatabaseInited() {
        return mDatabaseInited;
    }

    public void setDatabaseInited(boolean flag) {
        this.mDatabaseInited = flag;
    }

    public String getmPostfixMsg() {
        return mPostfixMsg;
    }

    public int getFolderMaxCountX() {
        return mFolderMaxCountX;
    }

    public int getFolderMaxCountY() {
        return mFolderMaxCountY;
    }

    public int getFolderMaxNumItems() {
        return mFolderMaxNumItems;
    }

    public void setFolderMaxNumItems(int mFolderMaxNumItems) {
        this.mFolderMaxNumItems = mFolderMaxNumItems;
    }

    public int getHotseatHeight() {
        return mHotseatHeight;
    }

    public int getHotseatCellCountX() {
        return mHotseatCellCountX;
    }

    public void setHotseatCellCountX(int mHotseatCellCountX) {
        this.mHotseatCellCountX = mHotseatCellCountX;
    }
    
    public int getHotseatMaxCount () {
        return mHotseatMaxCount;
    }
    
    public int getCellCountX() {
        return mCellCountX;
    }

    public int getCellCountY() {
        return mCellCountY;
    }

    public String getDatabaseName() {
        return mDatabaseName;
    }

    public int getWorkspaceId() {
        return mWorkspaceId;
    }

    public boolean isChanged(Context c) {
        return SharedPreferencesUtils.getSharedPreferences(c).getBoolean(MODE_CHANGE, false);
    }
    
    public void setChanged (Context c, boolean flag) {
        SharedPreferences sp = SharedPreferencesUtils.getSharedPreferences(c);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(MODE_CHANGE, flag);
        editor.commit();
    }

    public int[] getCellLayoutParams() {
        return mCellLayoutParams;
    }

    public int[] getFolderParams() {
        return mFolderParams;
    }
    
    public int[] getHotseatParams() {
        return mHotseatParams;
    }
    
    public void setCellCountXY (int x, int y) {
        mCellCountX = x;
        mCellCountY = y;
    }
    
    public void setFolderCellCountXY (int x, int y) {
        mFolderMaxCountX = x;
        mFolderMaxCountY = y;
    }
    
    public static int getCurrentMode (Context c) {
        if (mCurrentMode == -1) {
            mCurrentMode = SharedPreferencesUtils.getDisplayMode(c);
            if (mCurrentMode == -1) {
                /// set the mode according the font size
                mCurrentMode = 0;
            }
        }
        return mCurrentMode;
    }
    
    public static DisplayFactory produce (Context c) {
        int mode = getCurrentMode(c);
        if (mode == MODE_3_3) {
            return new LargeFont_3_3(c);
        } else if (mode == MODE_4_4) {
            return new NormalMode(c);
        }
        return new NormalMode(c);
    }

    /*******************************************************************************************/
    /*The states saved on preference*/
    /*******************************************************************************************/
    public static final String DEFAULT_SCREEN = "DEFAULT_SCREEN";
    public static final String SCREEN_COUNT = "SCREEN_COUNT";
    
    public int getDefaultScreen (Context c) {
        SharedPreferences sp = SharedPreferencesUtils.getSharedPreferences(c);
        return sp.getInt(DEFAULT_SCREEN, 0);
    }
    
    public void setDefaultScreen (Context c, int index) {
        SharedPreferences sp = SharedPreferencesUtils.getSharedPreferences(c);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(DEFAULT_SCREEN, index);
        editor.commit();
    }
    
    public int getScreenCount (Context c) {
        SharedPreferences sp = SharedPreferencesUtils.getSharedPreferences(c);
        return sp.getInt(SCREEN_COUNT, 0);
    }
    
    public void setScreenCount (Context c, int count) {
        SharedPreferences sp = SharedPreferencesUtils.getSharedPreferences(c);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(SCREEN_COUNT, count);
        editor.commit();
    }
    
    // added by liukaibang {{
    public int getCoverDrawableId() {
        return mCoverDrawableId;
    }
    
    public int getAppBackGroundDrawableId() {
        return mAppBackGroundDrawableId;
    }
    
    public int getFoldIconBackGroundDrawableId() {
    	return mFolderIconBackGroundDrawableId;
    }
    
    /**
     * Get drawing folder icon item transx dimentions.
     * @author liukaibang
     */
    public float getFolderIconItemTransX () {
    	return mFolderIconItemTransX;
    }
    
    public float getFolderContentPaddingLeft () {
    	return mFolderContentPaddingLeft;
    }
    
    public int getPageIndicatorId (boolean focused) {
    	return focused ? mPageIndicatorFocused : mPageIndicatorNormal;
    }
    // end }}
    
    public int getWorkspaceCellHeight () {
        return mWorkspaceCellHeight;
    }

    public int getWorkspaceCellWidth() {
        return mWorkspaceCellWidth;
    }
    
    public void calculateCellSize (Context c, int paddingTop,int paddingBottom,int statusBarHeight, int xGap, int yGap) {
        int screenWidth = c.getResources().getDisplayMetrics().widthPixels;
        int screenHeight = c.getResources().getDisplayMetrics().heightPixels;
        mWorkspaceCellHeight = (screenHeight
                - (statusBarHeight + paddingTop + paddingBottom + mHotseatHeight + mScrollingIndicatorHeight)
                - (yGap * (mCellCountY - 1))) / mCellCountY;
        
        mWorkspaceCellWidth = (screenWidth - (xGap*(mCellCountX-1)))/mCellCountX;
    }
}
