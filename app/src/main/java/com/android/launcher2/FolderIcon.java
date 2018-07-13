/*
 * Copyright (C) 2008 The Android Open Source Project
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

import java.util.ArrayList;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.launcher.R;
import com.android.launcher2.DropTarget.DragObject;
import com.android.launcher2.FolderInfo.FolderListener;
import com.eton.launcher.displaymode.DisplayFactory;

/**
 * An icon that can appear on in the workspace representing an {@link UserFolder}.
 */
public class FolderIcon extends LinearLayout implements FolderListener {
    private static final String TAG = "FolderIcon";

    private Launcher mLauncher;
    private Folder mFolder;
    private FolderInfo mInfo;
    private static boolean sStaticValuesDirty = true;

    // added by liudekuan
    private boolean mHidePreviewContent = false;

    private ArrayList<ShortcutInfo> mInfoBackup;
    // end

    private CheckLongPressHelper mLongPressHelper;

    // The number of icons to display in the
    private static final int NUM_ITEMS_IN_PREVIEW = 4;
    private static final int CONSUMPTION_ANIMATION_DURATION = 100;
    private static final int DROP_IN_ANIMATION_DURATION = 400;
    private static final int INITIAL_ITEM_ANIMATION_DURATION = 350;
    private static final int FINAL_ITEM_ANIMATION_DURATION = 200;

    // The degree to which the inner ring grows when accepting drop
    private static final float INNER_RING_GROWTH_FACTOR = 0.15f;

    // The degree to which the outer ring is scaled in its natural state
    private static final float OUTER_RING_GROWTH_FACTOR = 0.3f;

    // The amount of vertical spread between items in the stack [0...1]
    private static final float PERSPECTIVE_SHIFT_FACTOR = 0.24f;

    // The degree to which the item in the back of the stack is scaled [0...1]
    // (0 means it's not scaled at all, 1 means it's scaled to nothing)
    private static final float PERSPECTIVE_SCALE_FACTOR = 0.35f;

    public static Drawable sSharedFolderLeaveBehind = null;

    private ImageView mPreviewBackground;
    private BubbleTextView mFolderName;

    FolderRingAnimator mFolderRingAnimator = null;

    // These variables are all associated with the drawing of the preview; they are stored
    // as member variables for shared usage and to avoid computation on each frame
    private int mIntrinsicIconSize;
    private float mBaselineIconScale;
    private int mBaselineIconSize;
    private int mAvailableSpaceInPreview;
    private int mTotalWidth = -1;
    private int mPreviewOffsetX;
    private int mPreviewOffsetY;
    private float mMaxPerspectiveShift;
    boolean mAnimating = false;

    private PreviewItemDrawingParams mParams = new PreviewItemDrawingParams(0, 0, 0, 0);
    private PreviewItemDrawingParams mAnimParams = new PreviewItemDrawingParams(0, 0, 0, 0);
    private ArrayList<ShortcutInfo> mHiddenItems = new ArrayList<ShortcutInfo>();

    //add by eton lisidong
    private static final int ICON_COUNT = 4;  //����ʾ������ͼ��
    private static final int NUM_COL = 2;    // ÿ����ʾ�ĸ���
    private static final int PADDING = 1;    //�ڱ߾�
    private static final int MARGIN = 10;     //��߾�?    private static final int MARGINLEFT = 8; 
    private static final int MARGINTOP = 20; 
    
    //end

    // added by LiuDekuan
    private int mPreviewWidth;       
    private int mPreviewHeight;
    //
    
    // add by xyg
    private static int mCustomDuration;
    // end
    
    public FolderIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FolderIcon(Context context) {
        super(context);
        init();
    }

    private void init() {
        mLongPressHelper = new CheckLongPressHelper(this);
        // add by xyg
        mCustomDuration = getResources().getInteger(R.integer.anim_duration_integer);
        // end
    }

    public boolean isDropEnabled() {
        final ViewGroup cellLayoutChildren = (ViewGroup) getParent();
        final ViewGroup cellLayout = (ViewGroup) cellLayoutChildren.getParent();
        final Workspace workspace = (Workspace) cellLayout.getParent();
        return !workspace.isSmall();
    }

    static FolderIcon fromXml(int resId, Launcher launcher, ViewGroup group,
            FolderInfo folderInfo, IconCache iconCache) {
        @SuppressWarnings("all") // suppress dead code warning
        final boolean error = INITIAL_ITEM_ANIMATION_DURATION >= DROP_IN_ANIMATION_DURATION;
        if (error) {
            throw new IllegalStateException("DROP_IN_ANIMATION_DURATION must be greater than " +
                    "INITIAL_ITEM_ANIMATION_DURATION, as sequencing of adding first two items " +
                    "is dependent on this");
        }

        FolderIcon icon = (FolderIcon) LayoutInflater.from(launcher).inflate(resId, group, false);

        icon.mFolderName = (BubbleTextView) icon.findViewById(R.id.folder_icon_name);
        icon.mFolderName.setText(folderInfo.title);
        icon.mPreviewBackground = (ImageView) icon.findViewById(R.id.preview_background);
        
        // added by liukaibang, for folder background in defferent mode. {{
        final DisplayFactory df = LauncherApplication.getDisplayFactory(launcher);
        final LinearLayout.LayoutParams p = (LinearLayout.LayoutParams)(icon.mFolderName.getLayoutParams());
        p.topMargin = df.getIconTitleGap();
        icon.mFolderName.setLayoutParams(p);
        
        icon.mPreviewBackground.setImageResource(df.getFoldIconBackGroundDrawableId());
        icon.mFolderName.setTextSize(df.getWorkspaceTextSize());
        // end }}
        
        icon.setTag(folderInfo);
        icon.setOnClickListener(launcher);
        icon.setOnTouchListener(launcher);
        icon.mInfo = folderInfo;
        icon.mLauncher = launcher;
        icon.setContentDescription(String.format(launcher.getString(R.string.folder_name_format),
                folderInfo.title));
        Folder folder = Folder.fromXml(launcher);
        folder.setDragController(launcher.getDragController());
        folder.setFolderIcon(icon);
        folder.bind(folderInfo);
        icon.mFolder = folder;

        icon.mFolderRingAnimator = new FolderRingAnimator(launcher, icon);
        folderInfo.addListener(icon);

        return icon;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        sStaticValuesDirty = true;
        return super.onSaveInstanceState();
    }

    public static class FolderRingAnimator {
        public int mCellX;
        public int mCellY;
        private CellLayout mCellLayout;
        public float mOuterRingSize;
        public float mInnerRingSize;
        public FolderIcon mFolderIcon = null;
        public Drawable mOuterRingDrawable = null;
        public Drawable mInnerRingDrawable = null;
        public static Drawable sSharedOuterRingDrawable = null;
        public static Drawable sSharedInnerRingDrawable = null;
        public static int sPreviewSize = -1;
        public static int sPreviewPadding = -1;

        private ValueAnimator mAcceptAnimator;
        private ValueAnimator mNeutralAnimator;

        public FolderRingAnimator(Launcher launcher, FolderIcon folderIcon) {
            mFolderIcon = folderIcon;
            Resources res = launcher.getResources();
            mOuterRingDrawable = res.getDrawable(R.drawable.portal_ring_outer_holo);
            mInnerRingDrawable = res.getDrawable(R.drawable.portal_ring_inner_holo);

            // We need to reload the static values when configuration changes in case they are
            // different in another configuration
            if (sStaticValuesDirty) {
            	// modify by xyg
                sPreviewSize = res.getDimensionPixelSize(R.dimen.folder_preview_scale_size);
                // end
                sPreviewPadding = res.getDimensionPixelSize(R.dimen.folder_preview_padding);
                sSharedOuterRingDrawable = res.getDrawable(R.drawable.portal_ring_outer_holo);
                sSharedInnerRingDrawable = res.getDrawable(R.drawable.portal_ring_inner_holo);
                sSharedFolderLeaveBehind = res.getDrawable(R.drawable.portal_ring_rest);
                sStaticValuesDirty = false;
            }
        }

        public void animateToAcceptState() {
            if (mNeutralAnimator != null) {
                mNeutralAnimator.cancel();
            }
            mAcceptAnimator = LauncherAnimUtils.ofFloat(0f, 1f);
            mAcceptAnimator.setDuration(mCustomDuration);//CONSUMPTION_ANIMATION_DURATION);

            final int previewSize = sPreviewSize;
            mAcceptAnimator.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float percent = (Float) animation.getAnimatedValue();
                    mOuterRingSize = (1 + percent * OUTER_RING_GROWTH_FACTOR) * previewSize;
                    mInnerRingSize = (1 + percent * INNER_RING_GROWTH_FACTOR) * previewSize;
                    if (mCellLayout != null) {
                        mCellLayout.invalidate();
                    }
                }
            });
            mAcceptAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (mFolderIcon != null) {
                        mFolderIcon.mPreviewBackground.setVisibility(INVISIBLE);
                    }
                }
            });
            mAcceptAnimator.start();
        }

        public void animateToNaturalState() {
            if (mAcceptAnimator != null) {
                mAcceptAnimator.cancel();
            }
            mNeutralAnimator = LauncherAnimUtils.ofFloat(0f, 1f);
            // modify by xyg
            mNeutralAnimator.setDuration(mCustomDuration);//CONSUMPTION_ANIMATION_DURATION);
            // end

            final int previewSize = sPreviewSize;
            mNeutralAnimator.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float percent = (Float) animation.getAnimatedValue();
                    mOuterRingSize = (1 + (1 - percent) * OUTER_RING_GROWTH_FACTOR) * previewSize;
                    mInnerRingSize = (1 + (1 - percent) * INNER_RING_GROWTH_FACTOR) * previewSize;
                    if (mCellLayout != null) {
                        mCellLayout.invalidate();
                    }
                }
            });
            mNeutralAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                	// del by xyg
//                    if (mFolderIcon != null) {
//                        mFolderIcon.mPreviewBackground.setVisibility(VISIBLE);
//                    }
                	// end
                }
                
                // add by xyg
                @Override
                public void onAnimationStart(Animator animation) {
                    if (mFolderIcon != null) {
                    	mFolderIcon.mPreviewBackground.setVisibility(VISIBLE);
                    }
                }// end
            });
            mNeutralAnimator.start();
            /// M: Call the fun 'hideFolderAccept' outside neutral animation, avoid to 
            /// draw folder outer ring at wrong position due to not remove folder outer ring promptly.
            if (mCellLayout != null) {
                mCellLayout.hideFolderAccept(FolderRingAnimator.this);
            }
        }

        // Location is expressed in window coordinates
        public void getCell(int[] loc) {
            loc[0] = mCellX;
            loc[1] = mCellY;
        }

        // Location is expressed in window coordinates
        public void setCell(int x, int y) {
            mCellX = x;
            mCellY = y;
        }

        public void setCellLayout(CellLayout layout) {
            mCellLayout = layout;
        }

        public float getOuterRingSize() {
            return mOuterRingSize;
        }

        public float getInnerRingSize() {
            return mInnerRingSize;
        }
    }

    Folder getFolder() {
        return mFolder;
    }

    FolderInfo getFolderInfo() {
        return mInfo;
    }

    private boolean willAcceptItem(ItemInfo item) {
        final int itemType = item.itemType;
        return ((itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION ||
                itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT) &&
                !mFolder.isFull() && item != mInfo && !mInfo.opened);
    }

    public boolean acceptDrop(Object dragInfo) {
        final ItemInfo item = (ItemInfo) dragInfo;
        /// M: Modified by liudekuan on 2014-01-20
//        return !mFolder.isDestroyed() && willAcceptItem(item);
        return !mFolder.isDestroyed() && willAcceptItem(item) && !mRemoved;
        /// M: End
    }

    public void addItem(ShortcutInfo item) {
        mInfo.add(item);
    }

    public void onDragEnter(Object dragInfo) {
        if (mFolder.isDestroyed() || !willAcceptItem((ItemInfo) dragInfo)) return;
        CellLayout.LayoutParams lp = (CellLayout.LayoutParams) getLayoutParams();
        CellLayout layout = (CellLayout) getParent().getParent();
        mFolderRingAnimator.setCell(lp.cellX, lp.cellY);
        mFolderRingAnimator.setCellLayout(layout);
        mFolderRingAnimator.animateToAcceptState();
        layout.showFolderAccept(mFolderRingAnimator);
    }

    public void onDragOver(Object dragInfo) {
    }

    public void performCreateAnimation(final ShortcutInfo destInfo, final View destView,
            final ShortcutInfo srcInfo, final DragView srcView, Rect dstRect,
            float scaleRelativeToDragLayer, Runnable postAnimationRunnable) {
        // These correspond two the drawable and view that the icon was dropped _onto_
        Drawable animateDrawable = ((MTKShortcut) destView).getFavoriteCompoundDrawable();
        computePreviewDrawingParams(animateDrawable.getIntrinsicWidth(), destView.getMeasuredWidth());

        // This will animate the first item from it's position as an icon into its
        // position as the first item in the preview
        animateFirstItem(animateDrawable, INITIAL_ITEM_ANIMATION_DURATION, false, null);
        addItem(destInfo);

        // This will animate the dragView (srcView) into the new folder
        onDrop(srcInfo, srcView, dstRect, scaleRelativeToDragLayer, 1, postAnimationRunnable, null);
    }

    public void performDestroyAnimation(final View finalView, Runnable onCompleteRunnable) {
        Drawable animateDrawable = ((MTKShortcut) finalView).getFavoriteCompoundDrawable();
        computePreviewDrawingParams(animateDrawable.getIntrinsicWidth(), 
                finalView.getMeasuredWidth());

        // This will animate the first item from it's position as an icon into its
        // position as the first item in the preview
        animateFirstItem(animateDrawable, FINAL_ITEM_ANIMATION_DURATION, true,
                onCompleteRunnable);
    }

    public void onDragExit(Object dragInfo) {
        onDragExit();
    }

    public void onDragExit() {
        mFolderRingAnimator.animateToNaturalState();
    }

    private void onDrop(final ShortcutInfo item, DragView animateView, Rect finalRect,
            float scaleRelativeToDragLayer, int index, Runnable postAnimationRunnable,
            DragObject d) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "onDrop: item = " + item + ", animateView = "
                    + animateView + ", finalRect = " + finalRect + ", scaleRelativeToDragLayer = "
                    + scaleRelativeToDragLayer + ", index = " + index + ", d = " + d);
        }

        item.cellX = -1;
        item.cellY = -1;

        // Typically, the animateView corresponds to the DragView; however, if this is being done
        // after a configuration activity (ie. for a Shortcut being dragged from AllApps) we
        // will not have a view to animate
        if (animateView != null) {
            DragLayer dragLayer = mLauncher.getDragLayer();
            Rect from = new Rect();
            dragLayer.getViewRectRelativeToSelf(animateView, from);
            Rect to = finalRect;
            if (to == null) {
                to = new Rect();
                Workspace workspace = mLauncher.getWorkspace();
                // Set cellLayout and this to it's final state to compute final animation locations
                workspace.setFinalTransitionTransform((CellLayout) getParent().getParent());
                float scaleX = getScaleX();
                float scaleY = getScaleY();
                setScaleX(1.0f);
                setScaleY(1.0f);
                scaleRelativeToDragLayer = dragLayer.getDescendantRectRelativeToSelf(this, to);
                // Finished computing final animation locations, restore current state
                setScaleX(scaleX);
                setScaleY(scaleY);
                workspace.resetTransitionTransform((CellLayout) getParent().getParent());
            }

            int[] center = new int[2];
            float scale = getLocalCenterForIndex(index, center);
            center[0] = (int) Math.round(scaleRelativeToDragLayer * center[0]);
            center[1] = (int) Math.round(scaleRelativeToDragLayer * center[1]);

            to.offset(center[0] - animateView.getMeasuredWidth() / 2,
                    center[1] - animateView.getMeasuredHeight() / 2);

            float finalAlpha = index < NUM_ITEMS_IN_PREVIEW ? 0.5f : 0f;

            float finalScale = scale * scaleRelativeToDragLayer;
            dragLayer.animateView(animateView, from, to, finalAlpha,
                    1, 1, finalScale, finalScale, DROP_IN_ANIMATION_DURATION,
                    new DecelerateInterpolator(2), new AccelerateInterpolator(2),
                    postAnimationRunnable, DragLayer.ANIMATION_END_DISAPPEAR, null);
            addItem(item);
            mHiddenItems.add(item);
            postDelayed(new Runnable() {
                public void run() {
                    mHiddenItems.remove(item);
                    invalidate();
                }
            }, DROP_IN_ANIMATION_DURATION);
        } else {
            addItem(item);
        }
    }

    public void onDrop(DragObject d) {
    	if(LauncherLog.DEBUG) {
    	    LauncherLog.d(TAG, "onDrop: DragObject = " + d);
    	}

        ShortcutInfo item = (ShortcutInfo) d.dragInfo; 
        //deleted by ETON guolinan
       /* if (d.dragInfo instanceof ApplicationInfo) {
            // Came from all apps -- make a copy
            item = ((ApplicationInfo) d.dragInfo).makeShortcut();
        } else {
            item = (ShortcutInfo) d.dragInfo;
        }*/
        //end
        mFolder.notifyDrop();
        onDrop(item, d.dragView, null, 1.0f, mInfo.contents.size(), d.postAnimationRunnable, d);
    }

    public DropTarget getDropTargetDelegate(DragObject d) {
        return null;
    }

    private void computePreviewDrawingParams(int drawableSize, int totalSize) {
        if (mIntrinsicIconSize != drawableSize || mTotalWidth != totalSize) {
            mIntrinsicIconSize = drawableSize;
            mTotalWidth = totalSize;

            // modify by xyg
            final int previewSize = this.getResources().getDimensionPixelSize(R.dimen.folder_preview_size);//FolderRingAnimator.sPreviewSize;
            // end
            final int previewPadding = FolderRingAnimator.sPreviewPadding;

            mAvailableSpaceInPreview = (previewSize - 2 * previewPadding);
            // cos(45) = 0.707  + ~= 0.1) = 0.8f
            int adjustedAvailableSpace = (int) ((mAvailableSpaceInPreview / 2) * (1 + 0.8f));

            int unscaledHeight = (int) (mIntrinsicIconSize * (1 + PERSPECTIVE_SHIFT_FACTOR));
            mBaselineIconScale = (1.0f * adjustedAvailableSpace / unscaledHeight);

            mBaselineIconSize = (int) (mIntrinsicIconSize * mBaselineIconScale);
            mMaxPerspectiveShift = mBaselineIconSize * PERSPECTIVE_SHIFT_FACTOR;

            mPreviewOffsetX = (mTotalWidth - mAvailableSpaceInPreview) / 2;
            mPreviewOffsetY = previewPadding;
        }
    }

    private void computePreviewDrawingParams(Drawable d) {
        computePreviewDrawingParams(d.getIntrinsicWidth(), getMeasuredWidth());
    }

    class PreviewItemDrawingParams {
        PreviewItemDrawingParams(float transX, float transY, float scale, int overlayAlpha) {
            this.transX = transX;
            this.transY = transY;
            this.scale = scale;
            this.overlayAlpha = overlayAlpha;
        }
        float transX;
        float transY;
        float scale;
        int overlayAlpha;
        Drawable drawable;
    }

    private float getLocalCenterForIndex(int index, int[] center) {
        mParams = computePreviewItemDrawingParams(Math.min(NUM_ITEMS_IN_PREVIEW, index), mParams);

        mParams.transX += mPreviewOffsetX;
        mParams.transY += mPreviewOffsetY;
        float offsetX = mParams.transX + (mParams.scale * mIntrinsicIconSize) / 2;
        float offsetY = mParams.transY + (mParams.scale * mIntrinsicIconSize) / 2;

        center[0] = (int) Math.round(offsetX);
        center[1] = (int) Math.round(offsetY);
        return mParams.scale;
    }

    private PreviewItemDrawingParams computePreviewItemDrawingParams(int index,
            PreviewItemDrawingParams params) {
    	
      /*  index = NUM_ITEMS_IN_PREVIEW - index - 1;
        float r = (index * 1.0f) / (NUM_ITEMS_IN_PREVIEW - 1);
        float scale = (1 - PERSPECTIVE_SCALE_FACTOR * (1 - r));

        float offset = (1 - r) * mMaxPerspectiveShift;
        float scaledSize = scale * mBaselineIconSize;
        float scaleOffsetCorrection = (1 - scale) * mBaselineIconSize;

        // We want to imagine our coordinates from the bottom left, growing up and to the
        // right. This is natural for the x-axis, but for the y-axis, we have to invert things.
        float transY = mAvailableSpaceInPreview - (offset + scaledSize + scaleOffsetCorrection);
        float transX = offset + scaleOffsetCorrection;
        float totalScale = mBaselineIconScale * scale;
        final int overlayAlpha = (int) (80 * (1 - r));*/
    	//add by eton lisidong: calculate the icon's location and scale,so it look like the IPhone's style
    			int iconWidth = mPreviewBackground.getWidth();
    	        float scaleWidth = (float) ((iconWidth - MARGIN * 2) / NUM_COL - 5 * PADDING);  //计算缩略图的�?高与宽相�?  
    	        float scale = (scaleWidth / iconWidth); // 计算缩放比例 
    	        final int overlayAlpha = 0;
    	        // modified by liudekuan
//    	        float transX = MARGIN + PADDING * (2 * (index % NUM_COL) + 1) + scaleWidth  
//    	                * (index % NUM_COL);
//    	        float transY = MARGIN + mPreviewBackground.getPaddingTop() / 2 + PADDING * (2 * (index / NUM_COL) + 1) + scaleWidth  
//    	                * (index / NUM_COL);
    	        
    	        // Modified by liukaibang
    	        float arrX = LauncherApplication.getDisplayFactory(mLauncher).getFolderIconItemTransX();
    	        
    	        float arrY = (float)this.getResources().getDimension(R.dimen.folder_icon_transy);
    	        float transX = MARGIN + PADDING * (2 * (index % NUM_COL) + 1) + scaleWidth  
    	                * (index % NUM_COL) - arrX;
    	        // modify by xyg:调整文件夹内部item布局
    	        float transY = MARGIN + mPreviewBackground.getPaddingTop() / 2 + PADDING * (2 * (index / NUM_COL) + 1) + scaleWidth  
    	                * (index / NUM_COL) - arrY;
    	        // end
    	        // end
    	        //end
        if (params == null) {
            params = new PreviewItemDrawingParams(transX, transY, scale, overlayAlpha);
        } else {
            params.transX = transX;
            params.transY = transY;
            params.scale = scale;
            params.overlayAlpha = overlayAlpha;
        }

        // added by liudekuan
        mPreviewWidth = mPreviewBackground.getWidth() - MARGIN * 2 - 2 * PADDING;
        // end

        return params;
    }

    private void drawPreviewItem(Canvas canvas, PreviewItemDrawingParams params) {
        canvas.save();
        canvas.translate(params.transX + mPreviewOffsetX, params.transY + mPreviewOffsetY);
        canvas.scale(params.scale, params.scale);
        Drawable d = params.drawable;

        if (d != null) {
            d.setBounds(0, 0, mIntrinsicIconSize, mIntrinsicIconSize);
            d.setFilterBitmap(true);
            d.setColorFilter(Color.argb(params.overlayAlpha, 0, 0, 0), PorterDuff.Mode.SRC_ATOP);
            d.draw(canvas);
            d.clearColorFilter();
            d.setFilterBitmap(false);
        }
        canvas.restore();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        // added by liudekuan
        mPreviewWidth = mPreviewBackground.getWidth() - MARGIN * 2;
        mPreviewHeight = mPreviewWidth;

        if (mHidePreviewContent) return; 
        // end
        if (mFolder == null) return;
        if (mFolder.getItemCount() == 0 && !mAnimating) return;

        ArrayList<View> items = mFolder.getItemsInReadingOrder(false);
        Drawable d;
        TextView v;

        // Update our drawing parameters if necessary
        if (mAnimating) {
            computePreviewDrawingParams(mAnimParams.drawable);
        } else {
        	//modified by ETON guolinan
         /*   v = (TextView) items.get(0);
            d = v.getCompoundDrawables()[1];*/
        	 d = ((MTKShortcut)items.get(0)).getFavoriteCompoundDrawable();
        	 //end
        	 computePreviewDrawingParams(d);
        }

        int nItemsInPreview = Math.min(items.size(), NUM_ITEMS_IN_PREVIEW);
        if (!mAnimating) {
            for (int i = nItemsInPreview - 1; i >= 0; i--) {
            	 
                if (!mHiddenItems.contains(((MTKShortcut)items.get(i)).getTag())) {
                	//modified by ETON guolinan
                	d = ((MTKShortcut)items.get(i)).getFavoriteCompoundDrawable();
//                    d = v.getCompoundDrawables()[1];
                	//end
                    mParams = computePreviewItemDrawingParams(i, mParams);
                    mParams.drawable = d;
                    drawPreviewItem(canvas, mParams);
                }
            }
        } else {
            drawPreviewItem(canvas, mAnimParams);
        }

        // M: Removed by liudekuan
        // R: The code will cause corner mark's incorrect-dispalying on FolderIcon. 
        /// M: Draw unread event number.
//        MTKUnreadLoader.drawUnreadEventIfNeed(canvas, this);
        // M: End
    }

    private void animateFirstItem(final Drawable d, int duration, final boolean reverse,
            final Runnable onCompleteRunnable) {
        final PreviewItemDrawingParams finalParams = computePreviewItemDrawingParams(0, null);

        final float scale0 = 1.0f;
        final float transX0 = (mAvailableSpaceInPreview - d.getIntrinsicWidth()) / 2;
        final float transY0 = (mAvailableSpaceInPreview - d.getIntrinsicHeight()) / 2;
        mAnimParams.drawable = d;

        ValueAnimator va = LauncherAnimUtils.ofFloat(0f, 1.0f);
        va.addUpdateListener(new AnimatorUpdateListener(){
            public void onAnimationUpdate(ValueAnimator animation) {
                float progress = (Float) animation.getAnimatedValue();
                if (reverse) {
                    progress = 1 - progress;
                    mPreviewBackground.setAlpha(progress);
                }

                mAnimParams.transX = transX0 + progress * (finalParams.transX - transX0);
                mAnimParams.transY = transY0 + progress * (finalParams.transY - transY0);
                mAnimParams.scale = scale0 + progress * (finalParams.scale - scale0);
                invalidate();
            }
        });
        va.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mAnimating = true;
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimating = false;
                if (onCompleteRunnable != null) {
                    onCompleteRunnable.run();
                }
            }
        });
        va.setDuration(duration);
        va.start();
    }

    public void setTextVisible(boolean visible) {
        if (visible) {
            mFolderName.setVisibility(VISIBLE);
        } else {
            mFolderName.setVisibility(INVISIBLE);
        }
    }

    public boolean getTextVisible() {
        return mFolderName.getVisibility() == VISIBLE;
    }

    public void onItemsChanged() {
        invalidate();
        requestLayout();
    }

    public void onAdd(ShortcutInfo item) {
    	if (LauncherLog.DEBUG) {
    		LauncherLog.d(TAG, "onAdd item = " + item);
    	}

        /// M: added for unread feature, when add a item to a folder, we need to update
        /// the unread num of the folder.
        final ComponentName componentName = item.intent.getComponent();
        updateFolderUnreadNum(componentName, item.unreadNum);
        invalidate();
        requestLayout();
    }

    public void onRemove(ShortcutInfo item) {
    	if (LauncherLog.DEBUG) {
    		LauncherLog.d(TAG, "onRemove item = " + item);
    	}

        /// M: added for Unread feature, when remove a item from a folder, we need to update
        /// the unread num of the folder
        final ComponentName componentName = item.intent.getComponent();
        updateFolderUnreadNum(componentName, item.unreadNum);
        invalidate();
        requestLayout();
    }

    public void onTitleChanged(CharSequence title) {
        mFolderName.setText(title.toString());
        setContentDescription(String.format(getContext().getString(R.string.folder_name_format),
                title));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Call the superclass onTouchEvent first, because sometimes it changes the state to
        // isPressed() on an ACTION_UP
        boolean result = super.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLongPressHelper.postCheckForLongPress();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mLongPressHelper.cancelLongPress();
                break;
        }
        return result;
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();

        mLongPressHelper.cancelLongPress();
    }

    /**
     * M: Update the unread message number of the shortcut with the given value.
     * 
     * @param unreadNum the number of the unread message.
     */
    public void setFolderUnreadNum(int unreadNum) {
        if (LauncherLog.DEBUG_UNREAD) {
            LauncherLog.d(TAG, "setFolderUnreadNum: unreadNum = " + unreadNum + ", mInfo = " + mInfo
                    + ", this = " + this);
        }
        
        if (unreadNum <= 0) {
            mInfo.unreadNum = 0;
        } else {
            mInfo.unreadNum = unreadNum;
        }
    }

    /**
     * M: Update unread number of the folder, the number is the total unread number
     * of all shortcuts in folder, duplicate shortcut will be only count once.
     */
    public void updateFolderUnreadNum() {
        final ArrayList<ShortcutInfo> contents = mInfo.contents;
        final int contentsCount = contents.size();
        int unreadNumTotal = 0;
        final ArrayList<ComponentName> components = new ArrayList<ComponentName>();
        ShortcutInfo shortcutInfo = null;
        ComponentName componentName = null;
        int unreadNum = 0;
        for (int i = 0; i < contentsCount; i++) {
            shortcutInfo = contents.get(i);
            componentName = shortcutInfo.intent.getComponent();
            unreadNum = MTKUnreadLoader.getUnreadNumberOfComponent(componentName);
            if (unreadNum > 0) {
                shortcutInfo.unreadNum = unreadNum;
                int j = 0;
                for (j = 0; j < components.size(); j++) {
                    if (componentName != null && componentName.equals(components.get(j))) {
                        break;
                    }
                }
                if (LauncherLog.DEBUG_UNREAD) {
                    LauncherLog.d(TAG, "updateFolderUnreadNum: unreadNumTotal = " + unreadNumTotal
                            + ", j = " + j + ", components.size() = " + components.size());
                }
                if (j >= components.size()) {
                    components.add(componentName);
                    unreadNumTotal += unreadNum;
                }
            }
        }
        if (LauncherLog.DEBUG_UNREAD) {
            LauncherLog.d(TAG, "updateFolderUnreadNum 1 end: unreadNumTotal = " + unreadNumTotal);
        }     
        setFolderUnreadNum(unreadNumTotal);
    }

    /**
     * M: Update the unread message of the shortcut with the given information.
     * 
     * @param unreadNum the number of the unread message.
     */
    public void updateFolderUnreadNum(ComponentName component, int unreadNum) {
        final ArrayList<ShortcutInfo> contents = mInfo.contents;
        final int contentsCount = contents.size();
        int unreadNumTotal = 0;
        ShortcutInfo appInfo = null;
        ComponentName name = null;
        final ArrayList<ComponentName> components = new ArrayList<ComponentName>();
        for (int i = 0; i < contentsCount; i++) {
            appInfo = contents.get(i);
            name = appInfo.intent.getComponent();
            if (name != null && name.equals(component)) {
                appInfo.unreadNum = unreadNum;
            }
            if (appInfo.unreadNum > 0) {
                int j = 0;
                for (j = 0; j < components.size(); j++) {
                    if (name != null && name.equals(components.get(j))) {
                        break;
                    }
                }
                if (LauncherLog.DEBUG_UNREAD) {
                    LauncherLog.d(TAG, "updateFolderUnreadNum: unreadNumTotal = " + unreadNumTotal
                            + ", j = " + j + ", components.size() = " + components.size());
                }    
                if (j >= components.size()) {
                    components.add(name);
                    unreadNumTotal += appInfo.unreadNum;
                }
            }
        }
        if (LauncherLog.DEBUG_UNREAD) {
            LauncherLog.d(TAG, "updateFolderUnreadNum 2 end: unreadNumTotal = " + unreadNumTotal);
        }
        setFolderUnreadNum(unreadNumTotal);
    }

    /**
     * M: Reset the value of the variable of sStaticValuesDirty.
     */
    public static void resetValuesDirty() {
        sStaticValuesDirty = true;
    }
    
    /**
     * added by LiuDekuan
     */
    public void hidePreviewContent () {
        //mPreviewBackground.setVisibility(INVISIBLE);
        //mInfoBackup = mInfo.contents;
        //mInfo.contents = null;
        //invalidate();
        //requestLayout();       
        //ArrayList<ShortcutInfo> infos = mInfo.contents;
        //for (int i = infos.size() - 1; i >= 0; i --) {
            //mInfoBackup.add(mInfo.remove(infos));
          //  mInfoBackup.add(infos.get(i));
           // mInfo.remove(infos.get(i));
        //}
        mHidePreviewContent = true; 
        invalidate();
	requestLayout();
    }

    /**
     * added by LiuDekuan
     */	
    public void displayPreviewContent () {
        //mPreviewBackground.setVisibility(VISIBLE);
        mHidePreviewContent = false;
        invalidate();
	requestLayout();
    }

    /**
     * added by liudekuan
     * @return
     */
    public int getPreviewWidth () {
        return mPreviewWidth;
    }
    
    /**
     * added by liudekuan
     */
    public boolean isFull () {
    	return mFolder.isFull();
    }
    
  /// M: Added by liudekuan on 2014-01-20
    /// R: mRemoved will be set to true when the last app is removed from folder
    ///    the folderIcon is bound, then the foldericon wont accept any dropping.
    private boolean mRemoved = false;
    public void setRemoved (boolean flag) {
    	mRemoved = flag;
    }
    
    public boolean isRemoved () {
    	return mRemoved;
    }
    /// M: End
}
