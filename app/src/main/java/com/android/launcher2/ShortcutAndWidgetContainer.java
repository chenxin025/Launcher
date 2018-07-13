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

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ShortcutAndWidgetContainer extends ViewGroup {
    static final String TAG = "CellLayoutChildren";

    // These are temporary variables to prevent having to allocate a new object just to
    // return an (x, y) value from helper functions. Do NOT use them to maintain other state.
    private final int[] mTmpCellXY = new int[2];

    private final WallpaperManager mWallpaperManager;

    private int mCellWidth;
    private int mCellHeight;

    private int mWidthGap;
    private int mHeightGap;

    private Context mcontenxt;
    public ShortcutAndWidgetContainer(Context context) {
        super(context);
        mcontenxt = context;
        mWallpaperManager = WallpaperManager.getInstance(context);
    }
    /**
     * 标志是否进入Hoseat区域
     */
    private boolean  isEnterHoseat = false;
    
    public boolean isEnterHoseat()
    {
        return isEnterHoseat;
    }

    public void setEnterHoseat(boolean isEnterHoseat)
    {
        this.isEnterHoseat = isEnterHoseat;
    }

    private View dragView;

    public View getDragView()
    {
        return dragView;
    }

    public void setDragView(View dragView)
    {
        this.dragView = dragView;
    }

    /**
     * 拖拽到的cellx 坐标
     */
    private int targetX = -1;
    
    public int getTargetX()
    {
        return targetX;
    }

    public void setTargetX(int targetX)
    {
        this.targetX = targetX;
    }

    public static int TYPE_OF_HOTSET = 1;
    /**
     * 1 标示 �?Hotseat
     */
    private int typeOfHoseat = 0;
    
    public int getTypeOfHoseat()
    {
        return typeOfHoseat;
    }

    public void setTypeOfHoseat(int typeOfHoseat)
    {
        this.typeOfHoseat = typeOfHoseat;
    }

    private int width ;
    

    public void setWidth(int width)
    {
    	/// M: Modified by liudekuan
//        this.width = width-2*mWidthGap;
        this.width = width;
        /// M: End
    }

    public void setCellDimensions(int cellWidth, int cellHeight, int widthGap, int heightGap ) {
        mCellWidth = cellWidth;
        mCellHeight = cellHeight;
        mWidthGap = widthGap;
        mHeightGap = heightGap;
    }

    public View getChildAt(int x, int y) {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();

            if ((lp.cellX <= x) && (x < lp.cellX + lp.cellHSpan) &&
                    (lp.cellY <= y) && (y < lp.cellY + lp.cellVSpan)) {
                return child;
            }
        }
        return null;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        @SuppressWarnings("all") // suppress dead code warning
        final boolean debug = false;
        if (debug) {
            // Debug drawing for hit space
            Paint p = new Paint();
            p.setColor(0x6600FF00);
            for (int i = getChildCount() - 1; i >= 0; i--) {
                final View child = getChildAt(i);
                final CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();

                canvas.drawRect(lp.x, lp.y, lp.x + lp.width, lp.y + lp.height, p);
            }
        }
        super.dispatchDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            measureChild(child);
        }
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecSize =  MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(widthSpecSize, heightSpecSize);
    }
    
    /**
     * 
     * 移动图标�? 计算出最新的indext下标
     * 返回�?表示   修改过的下标总数  例如 如果只有一个图标位置变�?就返�? 
     * @return
     */
    public int  sortIndext()
    {
        int count = getChildCount();
        int total = 0;
        if(count == 4)
        {
            // add by tl 修改位置信息不对，正对快速拖动到底部
            isContainer(dragView);
            int size = lastHotseatView.size();
            Collections.sort(lastHotseatView, new MyComparator(0));
            for (int j = 0; j < size; j++)
            {
                View child = lastHotseatView.get(j);
                CellLayout.LayoutParams lp =  ( CellLayout.LayoutParams)child.getLayoutParams();
                lp.indext = j;
                child.setLayoutParams(lp);
            }
        }
        else
        {
            for (int i = 0; i < count; i++)
            {
                View child = getChildAt(i);
                CellLayout.LayoutParams lp =  ( CellLayout.LayoutParams)child.getLayoutParams();
                ItemInfo item = (ItemInfo) child.getTag();
                // 从外部添加进来的View  lp.indext= -1 的对象要考虑 当前插入在那个位�?                  
                // 4�?个情况不考虑 
                   if(lp.indext == -1)
                   {
                       total++;
                       switch (isChange)
                      {
                          case LEFT:
                              lp.indext =0;
                              break;
                          case RIGHT:
                              lp.indext =count -1;
                              break;
                          case MIDDLE:
                              lp.indext =count/2;
                              break;
                          case NONE:
                              lp.indext =i;
                              break;
                          case LEFT_MIDDLE:
                              lp.indext = (i==0) ? i:i+1;
                               break;
                          case RIGHT_MIDDLE:
                              lp.indext = (i==(count -1)) ? i+1:i;
                              break;
                          default:
                              break;
                      }
                   }
               child.setLayoutParams(lp);
            }
        }
       
       
        return total;
    }
    private int isChange = NONE;
    /**
     * 
     */
    private void hotseatCenter()
    {
      if(typeOfHoseat == TYPE_OF_HOTSET)
      {
          if(dragView == null) 
          {
              
              int count = getChildCount();
              int total = sortIndext();
              isContainer(dragView);
              int size = lastHotseatView.size();
              Collections.sort(lastHotseatView, new MyComparator(0));
              for (int j = 0; j < size; j++)
              {
                  View child =lastHotseatView.get(j);
                  CellLayout.LayoutParams lp =  ( CellLayout.LayoutParams)child.getLayoutParams();
                  ItemInfo item = (ItemInfo) child.getTag();
                  measureX(count,j,lp);
                  child.setLayoutParams(lp);
              }
             
              
            
              
          }
          else 
          {
                  dargingPreHoseatView();
                  int size = lastHotseatView.size();
                  for (int j = 0; j < size; j++)
                  {
                       View child = lastHotseatView.get(j);
                       CellLayout.LayoutParams lp =  ( CellLayout.LayoutParams)child.getLayoutParams();
                       ItemInfo item = (ItemInfo) child.getTag();
                       if(isEnterHoseat)
                       {
                           measureX(Math.min(4,size+1),lp.indext,lp);
                       }
                       else
                       {
                           measureX(size,lp.indext,lp);
                           
                       }
//                       
//                       Log.v("1111", 
//                           "-- dragView != null = " +  isEnterHoseat
//                           +"--  lp.indext-   = " +   lp.indext
//                           +"--  lp.cellX   = " +   lp.cellX
//                           +"--  item.getTitle()---   = " +   item.getTitle()
//                               );
                  }
               
                  
          }
          
      }
    }
    
    private void dargingPreHoseatView()
    {
        int count = getChildCount();
        int dragViewIndext = isContainer(dragView) ;
        Collections.sort(lastHotseatView, new MyComparator(0));
        int size = lastHotseatView.size();
        isChange = measureX(size);
        for (int i = 0; i < size; i++)
        {
            
            View child = lastHotseatView.get(i);
            CellLayout.LayoutParams lp =  ( CellLayout.LayoutParams)child.getLayoutParams();
            ItemInfo item = (ItemInfo) child.getTag();
            int indext =0;
            
            if(!isEnterHoseat)
            {
                indext = i;
            }
            else
            {
                switch (isChange)
                {
                     // 左区�?                
                    case LEFT:
                       indext = i+1; 
                       break;
                       // 右区�?                 
                       case RIGHT:
                      indext = i;                  
                       break;
                  case MIDDLE:
                      //中间区域
                      //包含 此拖拽图�?                     
                	  if(dragViewIndext != -1)
                      {
                          indext = (i==0) ? i:count-1;
                      }
                      else
                      {
                          indext = (i==0) ? i:size;
                      }
                       break;
                  case LEFT_MIDDLE:
                      indext = (i==0) ? i:i+1;
                       break;
                  case RIGHT_MIDDLE:
                      indext = (i==(size -1)) ? i+1:i;
                      break;

                   default:
                       break;
                }
            }
            if(size != 4)
            { 
                lp.indext = indext;
            }
//            Log.v("1111", 
//                "-- ************* = " +  isEnterHoseat
//                +"--  lp.indext-   = " +   lp.indext
//                +"--  lp.cellX   = " +   lp.cellX
//                +"--  item.getTitle()---   = " +   item.getTitle()
//                    );
            child.setLayoutParams(lp);
        }
    }
    private static final int NONE =-1;
    private static final int LEFT =0;
    private static final int RIGHT =1;
    private static final int MIDDLE =2;
    private static final int LEFT_MIDDLE =3;
    private static final int RIGHT_MIDDLE =4;
    /**
     * 上一次的状�?     */
    private int current =NONE;
    
  
    private int measureX(int count)
    {
        int x =NONE;
        if(typeOfHoseat == TYPE_OF_HOTSET)
        {
            
            int[] disx = new int[2];
            
            disx[0] =  width / (2 *2) + mCellWidth /2;
            disx[1] =  width / (2 *2) - mCellWidth /2  + (width/2);
            
//            disx[0] =  ((mCellWidth + mWidthGap));
//            disx[1] = (int) 3*((mCellWidth + mWidthGap));
            
            
            if(targetX <  disx[0])
            {
                x =LEFT;
                current =LEFT;
            }
            else if(targetX >  disx[1])
            {
                x =RIGHT;
                current =RIGHT;
            }
            
            if(count ==  1)
            {
                
                if(targetX >  disx[0] && targetX <  disx[1])
                {
                    x =current;
                }
            }
            else  if(count == 2)
            {
                
               if(targetX >  disx[0] && targetX <  disx[1])
                {
                    x =MIDDLE;
                    current =MIDDLE;
                }
            }
            else if(count == 3)
            {
                int[] dd = new int[3];
                dd[0] = (int) ((mCellWidth + mWidthGap));
                dd[1] = (int) 2*((mCellWidth + mWidthGap));
                dd[2] = (int) 3*((mCellWidth + mWidthGap));
                if(targetX <  dd[0])
                {
                    x =LEFT;
                }
                else if(targetX >  dd[2])
                {
                    x =RIGHT;
                }
                else if(targetX >=  dd[0] && targetX <=  dd[1])
                {
                     x =LEFT_MIDDLE;
                }
                else  if(targetX >  dd[1] && targetX <=  dd[2])
                {
                    x =RIGHT_MIDDLE;
                }
            }
           
        }
        return x;
    }
    private ArrayList<View> hotseatView = new ArrayList<View>();
    /**
     * 留下最后的能够显示的View并且安装　cellx　排序
     */
    private ArrayList<View> lastHotseatView = new ArrayList<View>();
    /**
     * 
     * 获取一个当前容器中留下的可见的View 集合  lastHotseatView
     * @param view
     * @return
     */
    private int isContainer(View view)
    {
        int indext =-1;
        lastHotseatView.clear();
        int count = getChildCount();
        for (int i = 0; i < count; i++)
        {
            View child = getChildAt(i);
            
            if(view == child)
            {
                CellLayout.LayoutParams lp =  ( CellLayout.LayoutParams)view.getLayoutParams();
                lp.indext = -1;
                child.setLayoutParams(lp);
                indext = i;
            }
            else
            {
                lastHotseatView.add(child);
            }
        }
        return indext;
    }
    /**
     * @param count 总数
     * @param index 排列的下�?     * @param lp 个体参数
     * @param i 补充�?     * @return
     */
    private int measureX(int count,int index,  CellLayout.LayoutParams lp)
    {
         int x =lp.x;
         /// M: Modified by liudekuan
//         if(typeOfHoseat == TYPE_OF_HOTSET)
//        {
//            x = (width/(count))*(index +1) - ((mCellWidth + 2*mWidthGap) / 2);
//            if(count == 4)
//            {
//                x = (int) (index * (mCellWidth + mWidthGap));
//            }
//        }
         LauncherLog.d(TAG, "width = " + width);
         LauncherLog.d(TAG, "count = " + count);
         LauncherLog.d(TAG, "mCellWidth = " + mCellWidth);
         
         double gap = (width - (count * mCellWidth)) / (count + 1.0);
         x = (int) (gap * (index + 1) + (index * mCellWidth));
         /// M: End
         
        return lp.x =x;
    }
  
    private class MyComparator implements Comparator
    {
        
        /**
         *  1 标示 �?indext 排序
         *  0 标示按cellx 排序
         */
        private int type = 0 ;
         public MyComparator(int type)
        {
             this.type = type;
        }
        
        @Override
        public int compare(Object arg0, Object arg1)
        {
          
               if(type == 0)
               {
                   if( ((CellLayout.LayoutParams)((View)arg0).getLayoutParams()).cellX  > ((CellLayout.LayoutParams)((View)arg1).getLayoutParams()).cellX )
                   {
                       return  1;
                   }
                   else
                   {
                       return -1;
                   } 
               }
               else if(type == 1)
               {
                   if( ((CellLayout.LayoutParams)((View)arg0).getLayoutParams()).indext  > ((CellLayout.LayoutParams)((View)arg1).getLayoutParams()).indext )
                   {
                       return  1;
                   }
                   else
                   {
                       return -1;
                   } 
               }
               return 1;
              
        }
        
    };
    public void setupLp(CellLayout.LayoutParams lp) {
        lp.setup(mCellWidth, mCellHeight, mWidthGap, mHeightGap,"animateChildToPosition");
    }

    public void measureChild(View child) {
        final int cellWidth = mCellWidth;
        final int cellHeight = mCellHeight;
        CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
        ItemInfo item = (ItemInfo) child.getTag();
        {
            lp.setup(cellWidth, cellHeight, mWidthGap, mHeightGap,item.getTitle());
        }
        int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY);
        int childheightMeasureSpec = MeasureSpec.makeMeasureSpec(lp.height,
                MeasureSpec.EXACTLY);
        child.measure(childWidthMeasureSpec, childheightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if(typeOfHoseat == TYPE_OF_HOTSET)
        {
            hotseatCenter();
        }
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
                int childLeft = lp.x;
                int childTop = lp.y;
                child.layout(childLeft, childTop, childLeft + lp.width, childTop + lp.height);

                if (lp.dropped) {
                    lp.dropped = false;
                    final int[] cellXY = mTmpCellXY;
                    getLocationOnScreen(cellXY);
                    mWallpaperManager.sendWallpaperCommand(getWindowToken(),
                            WallpaperManager.COMMAND_DROP,
                            cellXY[0] + childLeft + lp.width / 2,
                            cellXY[1] + childTop + lp.height / 2, 0, null);
                }
            }
        }
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    @Override
    public void requestChildFocus(View child, View focused) {
        super.requestChildFocus(child, focused);
        if (child != null) {
            Rect r = new Rect();
            child.getDrawingRect(r);
            requestRectangleOnScreen(r);
        }
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();

        // Cancel long press for all children
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            child.cancelLongPress();
        }
    }

    @Override
    protected void setChildrenDrawingCacheEnabled(boolean enabled) {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View view = getChildAt(i);
            view.setDrawingCacheEnabled(enabled);
            // Update the drawing caches
            if (!view.isHardwareAccelerated() && enabled) {
                view.buildDrawingCache(true);
            }
        }
    }

    @Override
    protected void setChildrenDrawnWithCacheEnabled(boolean enabled) {
        super.setChildrenDrawnWithCacheEnabled(enabled);
    }

}
