package com.android.launcher2;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.launcher.R;

public class MTKShortcut extends RelativeLayout {
    private static final String TAG = "MTKShortcut";
    BubbleTextView mFavorite;
    TextView mUnread;
    ShortcutInfo mInfo;

    public MTKShortcut(final Context context) {
        super(context);
        init(context);
    }

    public MTKShortcut(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MTKShortcut(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(final Context context) {

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();  

        /*
         * If use the default id, can get the view, but if not, may return null,
         * so be careful when create the shortcut icon from different layout,
         * make it the same id is very important, like application and
         * boxed_application.
         */
        mFavorite = (BubbleTextView)findViewById(R.id.app_icon_title);
        
        //added by shanlijuan
        /// R: Set text size and the gap between icon and title
        int iconTitleGap = LauncherApplication.getDisplayFactory(getContext()).getIconTitleGap();
        mFavorite.setCompoundDrawablePadding(iconTitleGap);
        int workspaceTextSize = LauncherApplication.getDisplayFactory(getContext()).getWorkspaceTextSize();
        mFavorite.setTextSize((float)workspaceTextSize);
        //end
        
        mUnread = (TextView)findViewById(R.id.app_unread); 
    }

    @Override
    public void setTag(Object tag) {
        super.setTag(tag);
        mFavorite.setTag(tag);
        mUnread.setTag(tag);
        mInfo = (ShortcutInfo)tag;
    }

    /**
     * Set favorite icon and tag, then update current unread number of the shortcut.
     * 
     * @param info
     * @param iconCache
     */
    public void applyFromShortcutInfo(ShortcutInfo info, IconCache iconCache) {
        mFavorite.applyFromShortcutInfo(info, iconCache);
        setTag(info);
        updateShortcutUnreadNum();
    }

    /**
     * Set the icon image of the favorite.
     * 
     * @param paramDrawable
     */
    public void setIcon(Drawable paramDrawable) {
        mFavorite.setCompoundDrawablesWithIntrinsicBounds(null, paramDrawable, null, null);
    }

    /**
     * Set the content text of the favorite text view.
     * 
     * @param title
     */
    public void setTitle(CharSequence title) {
        mFavorite.setText(title);
    }

    /**
     * Get favorite text.
     * 
     * @return
     */
    public CharSequence getTitle() {
        return mFavorite.getText();
    }

    /**
     * Get the top compound drawable in textview.
     * 
     * @return
     */
    public Drawable getFavoriteCompoundDrawable() {
        return mFavorite.getCompoundDrawables()[1];
    }

    
    /**
     * Update unread message of the shortcut, the number of unread information comes from
     * the list. 
     */
    public void updateShortcutUnreadNum() {
        if (LauncherLog.DEBUG_UNREAD) {
            LauncherLog.d(TAG, "updateShortcutUnreadNum: mInfo = " + mInfo + ",this = " + this);
        }
        updateShortcutUnreadNum(mInfo.unreadNum);
    }
    
    /**
     * Update the unread message of the shortcut with the given information.
     * 
     * @param unreadNum the number of the unread message.
     */
    public void updateShortcutUnreadNum(int unreadNum) {
        if (LauncherLog.DEBUG_UNREAD) {
            LauncherLog.d(TAG, "updateShortcutUnreadNum: unreadNum = " + unreadNum + ",mInfo = "
                    + mInfo + ",this = " + this);
        }
        // added by liudekuan for cleaning
        // removing redundant corner mark on email icon
        mUnread.setVisibility(View.GONE);
        // end
        
        if (unreadNum <= 0 && mInfo.isNew != LauncherSettings.Favorites.ITEM_IS_NEW) {
            mInfo.unreadNum = 0;
            mUnread.setVisibility(View.GONE);
        } else {
            mInfo.unreadNum = unreadNum;
            mUnread.setVisibility(View.VISIBLE);
            mUnread.setBackgroundResource(R.drawable.ic_newevents_numberindication);
            if (unreadNum > 99) {
                mUnread.setText(MTKUnreadLoader.getExceedText());
            } else {
            	if (mInfo.isNew == LauncherSettings.Favorites.ITEM_IS_NEW) {
            		mUnread.setBackgroundResource(R.drawable.icon_app_new);
            		/*mUnread.setHeight(mContext.getResources().getDrawable(R.drawable.icon_app_new).getIntrinsicHeight());*/
            	} else {
            		mUnread.setText(String.valueOf(mInfo.unreadNum));            		
            	}
            }
        }        
        setTag(mInfo);
    }

    /**
     * Get the unread text of shortcut.
     * 
     * @return
     */   
    public CharSequence getUnreadText() { 
        if (mUnread == null || mUnread.getVisibility() != View.VISIBLE) {
            return "0";
        } else {
            return mUnread.getText();
        }        
    }

    /**
     * Get the visibility of the shortcut unread text.
     * 
     * @return
     */
    public int getUnreadVisibility() {
        if (mUnread != null) {
            return mUnread.getVisibility();
        }

        return View.GONE;
    }

    /**
     * Set the margin right of unread text view, used for user folder in hotseat
     * only.
     * 
     * @param marginRight
     */
    void setShortcutUnreadMarginRight(int marginRight) {
        MarginLayoutParams params = (MarginLayoutParams) mUnread.getLayoutParams();
        params.setMargins(params.leftMargin, params.topMargin, marginRight, params.bottomMargin);
        if (LauncherLog.DEBUG_UNREAD) {
            LauncherLog.d(TAG, "Set shortcut margin right (" + marginRight + ") of shortcut " + mInfo);
        }
        mUnread.setLayoutParams(params);
        mUnread.requestLayout();
    }
}
