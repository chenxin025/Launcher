package com.android.launcher2;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.launcher.R;

public class DeleteZone extends FrameLayout implements DropTarget, DragController.DragListener{
	Context mContext;
	AnimatorSet mDeleteZoneFadeInAnimator;
	AnimatorSet mDeleteZoneFadeOutAnimator;
	ImageView mTopBar;
	//TransitionDrawable mTopBarDrawable;
	
	ImageView mTrash;
	AnimationDrawable mTrashFadeInDrawable;
	AnimationDrawable mTrashFadeOutDrawable;
	private static int DELETE_ANIMATION_DURATION = 285;
	
	Launcher mLauncher;
	float mTopBarHeight;
	boolean isvisiable = false;
	private static final int sTransitionInDuration = 200;
	private static final int sTransitionOutDuration = 150;
	
	//public static boolean mDragEnter = false;
	
	public DeleteZone(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}
	public DeleteZone(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}
	public DeleteZone(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		init(context);
	}
	private void init(Context context){
		mContext = context;
	}
	public void setup(Launcher launcher, DragController dragController) {
		mLauncher = launcher;
		dragController.addDragListener(this);
		dragController.addDropTarget(this);
		
		topMarGin();
	}
	
	private int statusBarHeight =0;
	
	private int  topMargin = 0;
	private void statusBarHeight()
	{
	     Rect frame = new Rect();  
         mLauncher.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);  
         statusBarHeight = frame.top;
	}
	private void topMarGin()
	{
	    LayoutParams   lp = (LayoutParams) mLauncher.getWorkspace().getLayoutParams();
        topMargin = lp.topMargin;
	}
	@Override
	public void onFinishInflate(){
		super.onFinishInflate();
		mTopBar = (ImageView)findViewById(R.id.topBar);
		//mTopBarDrawable = (TransitionDrawable)mTopBar.getDrawable();
		mTrash =(ImageView)findViewById(R.id.trash);
		mTopBarHeight = 20;  
		mDeleteZoneFadeInAnimator = new AnimatorSet();
		mDeleteZoneFadeOutAnimator = new AnimatorSet();
		ObjectAnimator fadeInAlphaAnimator = ObjectAnimator.ofFloat(this,"alpha",0.0f,1.0f);
		fadeInAlphaAnimator.setInterpolator(new DecelerateInterpolator());
		AnimatorSet.Builder fadeInAnimator = mDeleteZoneFadeInAnimator.play(fadeInAlphaAnimator);
		ObjectAnimator fadeInTranslationYAnimator = ObjectAnimator.ofFloat(this,"translationY",-mTopBarHeight,0.0f);
		fadeInAnimator.with(fadeInTranslationYAnimator);
		mDeleteZoneFadeInAnimator.setDuration(sTransitionInDuration);
		mDeleteZoneFadeInAnimator.addListener(new AnimatorListenerAdapter() {
	        @Override
	        public void onAnimationStart(Animator animation) {
	            
	            statusBarHeight();
	            mLauncher.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
	            LayoutParams   lp = (LayoutParams) mLauncher.getWorkspace().getLayoutParams();
	            lp.topMargin = topMargin + statusBarHeight;
	            setVisibility(View.VISIBLE);
	        }
		});
		
		ObjectAnimator fadeOutAlphaAnimator = ObjectAnimator.ofFloat(this,"alpha",0.0f,0.0f);
		fadeOutAlphaAnimator.setInterpolator(new DecelerateInterpolator());
		AnimatorSet.Builder fadeOutAnimator = mDeleteZoneFadeOutAnimator.play(fadeInAlphaAnimator);
		ObjectAnimator fadeOutTranslationYAnimator = ObjectAnimator.ofFloat(this,"translationY",-0.0f,-mTopBarHeight);
		fadeOutAnimator.with(fadeOutTranslationYAnimator);
		mDeleteZoneFadeOutAnimator.setDuration(sTransitionOutDuration);
		mDeleteZoneFadeOutAnimator.addListener(new AnimatorListenerAdapter() {
		    
		    @Override
		    public void onAnimationStart(Animator animation)
		    {
               
		        mLauncher.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                LayoutParams   lp = (LayoutParams) mLauncher.getWorkspace().getLayoutParams();
                lp.topMargin = topMargin;
		        super.onAnimationStart(animation);
		      
		    }
		    
		    @Override
            public void onAnimationEnd(Animator animation) {
		        
		    
                setLayerType(View.LAYER_TYPE_NONE, null);
                
                Workspace workSpace = mLauncher.getWorkspace();
               // workSpace.mDragEndPage = mLauncher.getCurrentPage();

                if(workSpace.mDragEndPage != workSpace.mDragStartPage && 
                    (workSpace.mDragEndPage >= 0) &&(workSpace.mDragStartPage >= 0) && !workSpace.getScreenEditState())
                {
                    mLauncher.removeGaussBitmap(workSpace.mDragStartPage);
                    mLauncher.getGaussBitmapByThread();  
                }
                else if(!workSpace.getScreenEditState())
                {
                    if(mLauncher.mAllowGauss)
                    {
                        mLauncher.getGaussBitmapByThread();
                    }
                    else
                    {
                        mLauncher.mAllowGauss = true;
                        mLauncher.removeGaussBitmap(workSpace.mDragStartPage);
                    }
                }
                else
                {
                    mLauncher.removeGaussBitmap(workSpace.mDragStartPage);
                    mLauncher.removeGaussBitmap(workSpace.mDragEndPage);
                }

               // mDragEnter = false;
                workSpace.mDragEndPage = -1;
                workSpace.mDragStartPage = -1;
            }
		});
		mTrashFadeInDrawable = mTrashFadeOutDrawable = (AnimationDrawable)mTrash.getDrawable();
	}
	@Override
	public void onDragStart(DragSource source, Object info, int dragAction) {
		// TODO Auto-generated method stub
			isvisiable = false;
		 if (isAllAppsWidget(source, info)) {
	        setVisibility(GONE);
			 return;
	        }
		 if (isAppsApplicationOrShortcut(source,info) 
				 ||isWorkspaceOrFolderWidget(source,info)
				 	||isFolder(source, info)){
			isvisiable = true;
			setLayerType(View.LAYER_TYPE_HARDWARE, null);
			buildLayer();
			mDeleteZoneFadeOutAnimator.cancel();
			mDeleteZoneFadeInAnimator.start();
			//mTopBarDrawable.resetTransition();
		 }
	}
	@Override
	public void onDragEnd() {
		// TODO Auto-generated method stub
		if(isvisiable){
			mDeleteZoneFadeInAnimator.cancel();
			mDeleteZoneFadeOutAnimator.start();
			setVisibility(View.GONE);
		}
	}
	public void hideDeleteZone()
	{
            mDeleteZoneFadeInAnimator.cancel();
            mDeleteZoneFadeOutAnimator.start();
            setVisibility(View.GONE);
	}
	@Override
	public boolean isDropEnabled() {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	public void onDrop(DragObject dragObject) {
		// TODO Auto-generated method stub
		animateToTrashAndCompleteDrop(dragObject);
	}
	@Override
	public void onDragEnter(DragObject dragObject) {
		// TODO Auto-generated method stub
		//mTopBarDrawable.startTransition(150);
		mTrashFadeOutDrawable.stop();
		mTrash.setImageResource(R.anim.open_trash) ;
		mTrashFadeInDrawable = (AnimationDrawable)mTrash.getDrawable();
		mTrashFadeInDrawable.start();
		
		//mDragEnter = true;
	}
	@Override
	public void onDragOver(DragObject dragObject) {
		// TODO Auto-generated method stub
       
	}
	@Override
	public void onDragExit(DragObject dragObject) {
		// TODO Auto-generated method stub
		//mTopBarDrawable.reverseTransition(100);
		mTrashFadeInDrawable.stop();
		mTrash.setImageResource(R.anim.close_trash);
		mTrashFadeOutDrawable = (AnimationDrawable)mTrash.getDrawable();
		mTrashFadeOutDrawable.start();
	}
	@Override
	public DropTarget getDropTargetDelegate(DragObject dragObject) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public boolean acceptDrop(DragObject dragObject) {
		// TODO Auto-generated method stub
		if(dragObject.dragInfo instanceof FolderInfo){
			if(((FolderInfo)dragObject.dragInfo).contents.isEmpty()){
				return true;
			}else {
			    int messageId = R.string.delete_a_folder;
	            Toast.makeText(mLauncher, messageId, Toast.LENGTH_SHORT).show();
			}
		}
		if((dragObject.dragInfo instanceof ShortcutInfo && !(dragObject.dragInfo instanceof ApplicationInfo)) || dragObject.dragInfo instanceof LauncherAppWidgetInfo ){
			return true;
		}
		if(dragObject.dragInfo instanceof ApplicationInfo){
		
			ApplicationInfo appInfo = (ApplicationInfo)dragObject.dragInfo;
			   if ((appInfo.flags & ApplicationInfo.DOWNLOADED_FLAG) == 0) {
				   int messageId = R.string.uninstall_system_app_text;
		            Toast.makeText(mLauncher, messageId, Toast.LENGTH_SHORT).show();
		            dragObject.cancelled = true;
		            return false;
			   }else {
				   return true;
			   }
		}
		return false;
	}
	@Override
	public void getLocationInDragLayer(int[] loc) {
		// TODO Auto-generated method stub
		 mLauncher.getDragLayer().getLocationInDragLayer(this, loc);
	}
	@Override
	public void onFlingToDelete(DragObject dragObject, int x, int y, PointF vec) {
	
		
	}
    public boolean isEnterIn(int x,int y){
    	Rect r = new Rect();
    	getHitRect(r);
    	if(r.contains(x,y)){
    		return true;
    	}
    	return false;
    }
    private boolean isAllAppsWidget(DragSource source, Object info) {
        if (source instanceof AppsCustomizePagedView) {
            if (info instanceof PendingAddItemInfo) {
                PendingAddItemInfo addInfo = (PendingAddItemInfo) info;
                switch (addInfo.itemType) {
                    case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                    case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
                        return true;
                }
            }
        }
        return false;
    }
    private boolean isAppsApplicationOrShortcut(DragSource source, Object info) {
        return ((source instanceof Workspace)||(source instanceof Folder)) 
        		&& ((info instanceof ApplicationInfo)||(info instanceof ShortcutInfo));
    }
    
    private boolean isWorkspaceOrFolderWidget(DragSource source, Object info) {
        return ((source instanceof Workspace)||(source instanceof Folder)) 
        		&& (info instanceof LauncherAppWidgetInfo);
    } 
    private boolean isFolder(DragSource source, Object info) {
        return ((source instanceof Workspace)&&(info instanceof FolderInfo));
    } 
    private boolean isDragSourceWorkspaceOrFolder(DragObject d) {
        return (d.dragSource instanceof Workspace) || (d.dragSource instanceof Folder);
    }
    private boolean isWorkspaceOrFolderApplication(DragObject d) {
        return isDragSourceWorkspaceOrFolder(d) 
        		&&((d.dragInfo instanceof ShortcutInfo)||(d.dragInfo instanceof ApplicationInfo));
    }
    private boolean isWorkspaceOrFolderWidget(DragObject d) {
        return isDragSourceWorkspaceOrFolder(d) && (d.dragInfo instanceof LauncherAppWidgetInfo);
    }    
        
    private void animateToTrashAndCompleteDrop(final DragObject d) {
        DragLayer dragLayer = mLauncher.getDragLayer();
        Rect from = new Rect();
        dragLayer.getViewRectRelativeToSelf(d.dragView, from);
        Rect to = new Rect(10, 10, 20, 20);
        float scale = (float) to.width() / from.width();

        // added by liudekuan
        // reason: move an app from a folder to deleteZone, the app icon will be removed
        // from the folder though we do not uninstall this app. The following code solved 
        // this bug.
        ShortcutInfo si = null;
        if (d.dragInfo instanceof ApplicationInfo) {
        	si = (ShortcutInfo) d.dragInfo;
        	
        	// the following code must be inside, or else exception will be 
        	// caused when empty folder is removed.
        	if (si.container != LauncherSettings.Favorites.CONTAINER_DESKTOP 
            		&& si.container != LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
            	FolderInfo folder = LauncherModel.sBgFolders.get(si.container);
            	if (folder != null) {
            		folder.add(si);
            	}
            }
        }
        
        // end
        
        Runnable onAnimationEndRunnable = new Runnable() {
            @Override
            public void run() {
                onDragEnd();
                completeDrop(d);
            }
        };
        dragLayer.animateView(d.dragView, from, to, scale, 1f, 1f, 0.1f, 0.1f,
                DELETE_ANIMATION_DURATION, new DecelerateInterpolator(2),
                new LinearInterpolator(), onAnimationEndRunnable,
                DragLayer.ANIMATION_END_DISAPPEAR, null);
    }
    
    private void completeDrop(DragObject dragObject) {
    	ItemInfo item = (ItemInfo) dragObject.dragInfo;
        if (LauncherLog.DEBUG) {
            LauncherLog.d(DragController.TAG, "DeleteDropTarget completeDrop: item = " + item + ",d = " + dragObject);
        }
        switch(item.itemType){
        //[eton begin]: added by liudekuan
        case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
        	 LauncherModel.deleteItemFromDatabase(mLauncher, item);
        	 break;
        	 //[eton end]
 	        case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
 	        	dragObject.cancelled = true;
 	        	
 	        	mLauncher.startApplicationUninstallActivity((ApplicationInfo) item);
 	        	break;
 	        case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
 	        	LauncherModel.deleteItemFromDatabase(mLauncher, item);
 	        	// added by liudekuan
 	        	// absolutely, we need break
 	        	break;
             // Remove the widget from the workspace
 	        case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:	 
 	            mLauncher.removeAppWidget((LauncherAppWidgetInfo) item);
 	            LauncherModel.deleteItemFromDatabase(mLauncher, item);
 	
 	            final LauncherAppWidgetInfo launcherAppWidgetInfo = (LauncherAppWidgetInfo) item;
 	            final LauncherAppWidgetHost appWidgetHost = mLauncher.getAppWidgetHost();
 	            if (appWidgetHost != null) {
 	                // Deleting an app widget ID is a void call but writes to disk before returning
 	                // to the caller...
 	                new Thread("deleteAppWidgetId") {
 	                    public void run() {
 	                        appWidgetHost.deleteAppWidgetId(launcherAppWidgetInfo.appWidgetId);
 	                    }
 	                }.start();
 	            }
         }
    	
    }
    
}
