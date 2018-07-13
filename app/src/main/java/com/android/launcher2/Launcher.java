
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

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.os.SystemClock;
import android.os.Trace;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.v4.view.ViewPager;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Advanceable;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.launcher.R;
import com.android.launcher2.DropTarget.DragObject;
import com.eton.launcher.displaymode.DisplayFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
/**
 * Default launcher application.
 */
public final class Launcher extends Activity
        implements View.OnClickListener, OnLongClickListener, LauncherModel.Callbacks,
                   View.OnTouchListener, MTKUnreadLoader.UnreadCallbacks {
    private static final String TAG = "Launcher";
    static final String TAG_SURFACEWIDGET = "MTKWidgetView";
    static final boolean LOGD = false;

    static final boolean PROFILE_STARTUP = false;
    static final boolean DEBUG_WIDGETS = true;
    static final boolean DEBUG_STRICT_MODE = false;
    
    private static final int HOME_MAX_SCREENS = 9;
    //added by ETON guolinan
    private static final int CLOSTING_OPENING_DURATION = 500;
    //end
    private static final int MENU_GROUP_WALLPAPER = 1;
    private static final int MENU_GROUP_EDIT_WORKSPACE = MENU_GROUP_WALLPAPER + 1;
   
    private static final int MENU_WALLPAPER_SETTINGS = Menu.FIRST + 1;
    //add by ETON guolinan
    private static final int MENU_EDIT = MENU_WALLPAPER_SETTINGS + 1;
    //end
    
    private static final int MENU_MANAGE_APPS = MENU_EDIT + 1;
    private static final int MENU_SYSTEM_SETTINGS = MENU_MANAGE_APPS + 1;
    private static final int MENU_HELP = MENU_SYSTEM_SETTINGS + 1;
    private static final int MENU_EDIT_EFFECT = MENU_HELP + 1; 
    private static final int MENU_THEME_SETTINGS = MENU_EDIT_EFFECT + 1;
    
    public static final int CUSTOM_MENU_WALLPAPER = Menu.FIRST + 1;
    public static final int CUSTOM_MENU_EFFECT = CUSTOM_MENU_WALLPAPER + 1;
    public static final int CUSTOM_MENU_EDIT = CUSTOM_MENU_EFFECT + 1;
    public static final int CUSTOM_MENU_SETTINGS = CUSTOM_MENU_EDIT + 1;
    
    //>>added by eton wanghenan for Test
    public static final int MENU_TEST = MENU_EDIT_EFFECT + 1;
    //<<end
    
    private static final int REQUEST_CREATE_SHORTCUT = 1;
    private static final int REQUEST_CREATE_APPWIDGET = 5;
    private static final int REQUEST_PICK_APPLICATION = 6;
    private static final int REQUEST_PICK_SHORTCUT = 7;
    private static final int REQUEST_PICK_APPWIDGET = 9;
    private static final int REQUEST_PICK_WALLPAPER = 10;

    private static final int REQUEST_BIND_APPWIDGET = 11;

    static final String EXTRA_SHORTCUT_DUPLICATE = "duplicate";

    static final int SCREEN_COUNT = 4;
    static final int DEFAULT_SCREEN = 1;

    private static final String PREFERENCES = "launcher.preferences";
    // To turn on these properties, type
    // adb shell setprop log.tag.PROPERTY_NAME [VERBOSE | SUPPRESS]
    static final String FORCE_ENABLE_ROTATION_PROPERTY = "launcher_force_rotate";
    static final String DUMP_STATE_PROPERTY = "launcher_dump_state";

    // The Intent extra that defines whether to ignore the launch animation
    static final String INTENT_EXTRA_IGNORE_LAUNCH_ANIMATION =
            "com.android.launcher.intent.extra.shortcut.INGORE_LAUNCH_ANIMATION";

    // Type: int
    private static final String RUNTIME_STATE_CURRENT_SCREEN = "launcher.current_screen";
    // Type: int
    private static final String RUNTIME_STATE = "launcher.state";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_CONTAINER = "launcher.add_container";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_SCREEN = "launcher.add_screen";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_CELL_X = "launcher.add_cell_x";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_CELL_Y = "launcher.add_cell_y";
    // Type: boolean
    private static final String RUNTIME_STATE_PENDING_FOLDER_RENAME = "launcher.rename_folder";
    // Type: long
    private static final String RUNTIME_STATE_PENDING_FOLDER_RENAME_ID = "launcher.rename_folder_id";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_SPAN_X = "launcher.add_span_x";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_SPAN_Y = "launcher.add_span_y";
    // Type: parcelable
    private static final String RUNTIME_STATE_PENDING_ADD_WIDGET_INFO = "launcher.add_widget_info";

    private static final String TOOLBAR_ICON_METADATA_NAME = "com.android.launcher.toolbar_icon";
    //deleted by ETON guolinan
/*    private static final String TOOLBAR_SEARCH_ICON_METADATA_NAME =
            "com.android.launcher.toolbar_search_icon";
    private static final String TOOLBAR_VOICE_SEARCH_ICON_METADATA_NAME =
            "com.android.launcher.toolbar_voice_search_icon";*/
    //end
    /** The different states that Launcher can be in. */
    private enum State { NONE, WORKSPACE, APPS_CUSTOMIZE, APPS_CUSTOMIZE_SPRING_LOADED };
    private State mState = State.WORKSPACE;
    private AnimatorSet mStateAnimation;
    //deleted by ETON guolinan
//    private AnimatorSet mDividerAnimator;
    //end
    
    //added by ETON guolinan
  /*  public  enum Fonts { Medium, Big, Huge };
    public static Fonts mFont = Fonts.Huge;*/
    //end
    
    static final int APPWIDGET_HOST_ID = 1024;
    private static final int EXIT_SPRINGLOADED_MODE_SHORT_TIMEOUT = 300;
    private static final int EXIT_SPRINGLOADED_MODE_LONG_TIMEOUT = 600;
    private static final int SHOW_CLING_DURATION = 550;
    private static final int DISMISS_CLING_DURATION = 250;

    private static final Object sLock = new Object();
    private static int sScreen = DEFAULT_SCREEN;

    // How long to wait before the new-shortcut animation automatically pans the workspace
    private static int NEW_APPS_ANIMATION_INACTIVE_TIMEOUT_SECONDS = 10;

    private final BroadcastReceiver mCloseSystemDialogsReceiver
            = new CloseSystemDialogsIntentReceiver();

    private final ContentObserver mWidgetObserver = new AppWidgetResetObserver();
    
    //>>added by tianlei
    private final BroadcastReceiver checkBroadcastReceiver= new CheckBroadcast();
    //<<added end

    private LayoutInflater mInflater;

    private Workspace mWorkspace;
    //deleted by ETON guolinan
   /* private View mQsbDivider;
    private View mDockDivider;*/
  //added by ETON guolinan
    private DeleteZone mDeleteZone;
	//added by chenxin
    private LinearLayout mDeleteZoneTest;
    private ScrollingIndicator mScrollingIndicator; 
	private ScrollingIndicator mAppsScrollingIndicator;
	private PopupWindow mPopWindow;
    //end
    
    //end
    private View mLauncherView;
    private DragLayer mDragLayer;
    private DragController mDragController;

    private AppWidgetManager mAppWidgetManager;
    private LauncherAppWidgetHost mAppWidgetHost;

    private ItemInfo mPendingAddInfo = new ItemInfo();
    private AppWidgetProviderInfo mPendingAddWidgetInfo;

    private int[] mTmpAddItemCellCoordinates = new int[2];

    private FolderInfo mFolderInfo;

    private Hotseat mHotseat;
    //deleted by ETON guolinan
//    private View mAllAppsButton;
//end
    
    private SearchDropTargetBar mSearchDropTargetBar;
    private AppsCustomizeTabHost mAppsCustomizeTabHost;
    private AppsCustomizePagedView mAppsCustomizeContent;
    private boolean mAutoAdvanceRunning = false;

    private Bundle mSavedState;
    // We set the state in both onCreate and then onNewIntent in some cases, which causes both
    // scroll issues (because the workspace may not have been measured yet) and extra work.
    // Instead, just save the state that we need to restore Launcher to, and commit it in onResume.
    private State mOnResumeState = State.NONE;

    private SpannableStringBuilder mDefaultKeySsb = null;

    private boolean mWorkspaceLoading = true;

    private boolean mPaused = true;
    private boolean mRestoring;
    private boolean mWaitingForResult;
    private boolean mOnResumeNeedsLoad;

    // Keep track of whether the user has left launcher
    private static boolean sPausedFromUserAction = false;

    private Bundle mSavedInstanceState;

    private LauncherModel mModel;
    private IconCache mIconCache;
    private ScreenEditUtil mScreenEditUtil;
    private boolean mUserPresent = true;
    private boolean mVisible = false;
    private boolean mAttached = false;

    private static LocaleConfiguration sLocaleConfiguration = null;

    private static HashMap<Long, FolderInfo> sFolders = new HashMap<Long, FolderInfo>();

    private Intent mAppMarketIntent = null;

    // Related to the auto-advancing of widgets
    private final int ADVANCE_MSG = 1;
    
    // added by liudekuan
    public boolean mIsScreenOff = false;
    // end
    
    private final int mAdvanceInterval = 20000;
    private final int mAdvanceStagger = 250;
    private long mAutoAdvanceSentTime;
    private long mAutoAdvanceTimeLeft = -1;
    private HashMap<View, AppWidgetProviderInfo> mWidgetsToAdvance =
        new HashMap<View, AppWidgetProviderInfo>();

    // Determines how long to wait after a rotation before restoring the screen orientation to
    // match the sensor state.
    private final int mRestoreScreenOrientationDelay = 500;
    //deleted by ETON guolinan
   /* // External icons saved in case of resource changes, orientation, etc.
    private static Drawable.ConstantState[] sGlobalSearchIcon = new Drawable.ConstantState[2];
    private static Drawable.ConstantState[] sVoiceSearchIcon = new Drawable.ConstantState[2];*/
    //end
    private static Drawable.ConstantState[] sAppMarketIcon = new Drawable.ConstantState[2];

    private Drawable mWorkspaceBackgroundDrawable;
    private Drawable mBlackBackgroundDrawable;

    private final ArrayList<Integer> mSynchronouslyBoundPages = new ArrayList<Integer>();

    static final ArrayList<String> sDumpLogs = new ArrayList<String>();

    // We only want to get the SharedPreferences once since it does an FS stat each time we get
    // it from the context.
    private SharedPreferences mSharedPrefs;

    // Holds the page that we need to animate to, and the icon views that we need to animate up
    // when we scroll to that page on resume.
    private int mNewShortcutAnimatePage = -1;
    private ArrayList<View> mNewShortcutAnimateViews = new ArrayList<View>();
    private ImageView mFolderIconImageView;
    private Bitmap mFolderIconBitmap;
    private Canvas mFolderIconCanvas;
    private Rect mRectForFolderAnimation = new Rect();

    private BubbleTextView mWaitingForResume;
    
    
    //add by eton lisidong:
//    private QuickViewWorkspace mQuickViewWorkspace;
    private OnSharedPreferenceChangeListener mSharedPreferenceChangeListener;
    //end

    //added by ETON guolinan
    static final String DIALOG_CUSTOM = "CustomDialogFragment";
    //end
    
    //added by ETON wanghenan
    SectorView mSector = null;
    private final BroadcastReceiver mSectorReceive = new SectorReceive();
    
	public static final String ACTION_DATA_ISLOADING = "action_progressdialog_show";
	public static final String ACTION_DATA_LOAD_FINISHED = "action_progressdialog_dismiss";
	private ProgressDialog mDataDialog = null;
    private final BroadcastReceiver mProgressDialogReceive = new DataLoadingReceiver();

    //end
    
    // added by liudekuan
    public static final int ERROR = -1;
    public static final int SUCCESS = 0;
    
    private ProgressDialog mAppsUpdateDialog = null;
    private ProgressDialog mAppsAddDialog = null;
    public static final String ACTION_APPS_UPDATING = "ACTION_APPS_UPDATING";
    public static final String ACTION_APPS_UPDATING_FINISHED = "ACTION_APPS_UPDATING_FINISHED";
    public static final String ACTION_APPS_ARRANGE_UPDATING = "ACTION_APPS_ARRANGE_UPDATING";
    // end
    
    //added by chenxin
    public static boolean mIsAddSystemWidget = false;
    public static boolean mIsShowCustommenu = true;
    //end
    
     
    //add by yangailin
    private  WallpaperReceiver mWallReceiver;
    public Bitmap mWallGaussBitmap;
    public View mWorkSpaceView;
    public Bitmap mGaussViewBG;
   // private int[] mColors;  
    public static int mWidth;
    public static int mHeight;
    public static int mStatusBarHeight;
    public Bitmap mCopyBitmap;
    public Bitmap mScreenBitmap;
    public Bitmap mStatusGaussBitmap;
    public static String GAUSS_BITMAP = "gaussBitmp";
    public static HashMap<String, Bitmap> mGaussBitmap = new HashMap<String, Bitmap>();
    public GetGaussBitmapThread mGaussBitmapThread;
    public boolean mGaussOver = true;
    public boolean mAllowGauss = true;

    public long mGaussBeginTime = -1;
    public int mGaussScreenIndex = -1;
    
    public int  topMargin = -1;

    public static final long CLICK_INTERVAL_TIME = 1000;
    //end
    
    private HideFromAccessibilityHelper mHideFromAccessibilityHelper
        = new HideFromAccessibilityHelper();

    private Runnable mBuildLayersRunnable = new Runnable() {
        public void run() {
            if (mWorkspace != null) {
                mWorkspace.buildPageHardwareLayers();
            }
        }
    };

    private static ArrayList<PendingAddArguments> sPendingAddList
            = new ArrayList<PendingAddArguments>();

    private static boolean sForceEnableRotation = isPropertyEnabled(FORCE_ENABLE_ROTATION_PROPERTY);

    private static class PendingAddArguments {
        int requestCode;
        Intent intent;
        long container;
        int screen;
        int cellX;
        int cellY;
    }

    private static boolean isPropertyEnabled(String propertyName) {
        return Log.isLoggable(propertyName, Log.VERBOSE);
    }

    /// M: Static variable to record whether locale has been changed.
    private static boolean sLocaleChanged = false;

    /// M: Add for launch specified applications in landscape. @{
    private static final int ORIENTATION_0 = 0;
    private static final int ORIENTATION_90 = 90;
    private static final int ORIENTATION_180 = 180;
    private static final int ORIENTATION_270 = 270;

    private OrientationEventListener mOrientationListener;
    private int mLastOrientation = ORIENTATION_0;
    /// @}
    private static String sCurrentScene = "default";
    private static String sCurrentWallpaper = "default_wallpaper";
    static final HashMap<String, String> SCENE_WALLPAPER = new HashMap<String, String>();
    /// M: Add for launcher unread shortcut feature. @{
    static final int MAX_UNREAD_COUNT = 99;
    private MTKUnreadLoader mUnreadLoader;

    private boolean mUnreadLoadCompleted = false;
    private boolean mBindingWorkspaceFinished = false;
    private boolean mBindingAppsFinished = false;
    /// @}

    /// M: Save current CellLayout bounds before workspace.changeState(CellLayout will be scaled).
    private Rect mCurrentBounds = new Rect();
    
    /// M: Used to popup long press widget to add message.
    private Toast mLongPressWidgetToAddToast;

    /// M: Used to force reload when loading workspace
    private boolean mIsLoadingWorkspace;

    /// M: flag to indicate whether the orientation has changed.
    private boolean mOrientationChanged;

    /// M: flag to indicate whether the pages in app customized pane were recreated.
    private boolean mPagesWereRecreated;
    
    
    public static final int ADD_SCREEN_LEFT_POSITON = 1;
    
    private static boolean mIsCompleteDeteleScreen = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG_STRICT_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()   // or .detectAll() for all detectable problems
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
        }

        super.onCreate(savedInstanceState);
        
        //added by shanlijuan
        mStatusBarHeight = getSysStatusBarHeight(Launcher.this);
        //end
        LauncherApplication app = ((LauncherApplication)getApplication());
        mModel = app.setLauncher(this);
        mModel.initScreenCount(this);
        mSharedPrefs = getSharedPreferences(LauncherApplication.getSharedPreferencesKey(),
                Context.MODE_PRIVATE);
        
        // M: Added by liudekuan on 20131024
        mModel.restoreFromEditModeIfNecessary(this);
        // M: End
        /// M: added for unread feature, load and bind unread info.
        /*if (FeatureOption.MTK_LAUNCHER_UNREAD_SUPPORT) {
            mUnreadLoader = app.getUnreadLoader();
            mUnreadLoader.loadAndInitUnreadShortcuts();    
        }*/
        mIconCache = app.getIconCache();
		//added by chenxin
        mScreenEditUtil = app.getScreenEditUtil();
        mIsCompleteDeteleScreen = true;
        
        mDragController = new DragController(this);
        mInflater = getLayoutInflater();

        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "(Launcher)onCreate: savedInstanceState = " + savedInstanceState
                    + ", mModel = " + mModel + ", mIconCache = " + mIconCache + ", this = " + this
                    + ", sLocaleChanged = " + sLocaleChanged);
        }
        mAppWidgetManager = AppWidgetManager.getInstance(this);
        mAppWidgetHost = new LauncherAppWidgetHost(this, APPWIDGET_HOST_ID);
        mAppWidgetHost.startListening();

        // If we are getting an onCreate, we can actually preempt onResume and unset mPaused here,
        // this also ensures that any synchronous binding below doesn't re-trigger another
        // LauncherModel load.
        mPaused = false;

        if (PROFILE_STARTUP) {
            android.os.Debug.startMethodTracing(
                    Environment.getExternalStorageDirectory() + "/launcher");
        }
        
        checkForLocaleChange();
        setContentView(R.layout.launcher);
        setupViews();
        //deleted by ETON guolinan
//        showFirstRunWorkspaceCling();
        //end
        registerContentObservers();

        lockAllApps();

        mSavedState = savedInstanceState;
        restoreState(mSavedState);

        // Update customization drawer _after_ restoring the states
        if (mAppsCustomizeContent != null) {
        	mAppsCustomizeContent.onPackagesUpdated();
        /*	 mAppsCustomizeContent.setDataIsReady();
        	//added by ETON guolinan
        	mAppsCustomizeTabHost.selectWidgetsTab();*/
        	//end
        }

        if (PROFILE_STARTUP) {
            android.os.Debug.stopMethodTracing();
        }

        if (!mRestoring) {
            /// M: Reset load state if locale changed before.
            if (sLocaleChanged) {
                mModel.resetLoadedState(true, true);
                sLocaleChanged = false;
            }
            mIsLoadingWorkspace = true;
            
	        //>>add by eton wanghenan
	        IntentFilter sectorFilter = new IntentFilter();
	        sectorFilter.addAction(SectorView.ACTION_SECTORVIEW_SHOW);
	        sectorFilter.addAction(SectorView.ACTION_SECTORVIEW_REMOVE);
	        sectorFilter.addAction(SectorView.ACTION_SECTORVIEW_UPDATE);
	        sectorFilter.addAction(SectorView.ACTION_SECTORVIEW_REMOVE_WITH_UNPOPANIMATION);
	        registerReceiver(mSectorReceive, sectorFilter);
	        
	        IntentFilter progressDialogDisplayFilter = new IntentFilter();
	        progressDialogDisplayFilter.addAction(Launcher.ACTION_DATA_ISLOADING);
	        progressDialogDisplayFilter.addAction(Launcher.ACTION_DATA_LOAD_FINISHED);
	        
	        // M: Added by liudekuan
	        progressDialogDisplayFilter.addAction(Launcher.ACTION_APPS_UPDATING);
	        progressDialogDisplayFilter.addAction(Launcher.ACTION_APPS_UPDATING_FINISHED);
	        // M: End
	        progressDialogDisplayFilter.addAction(Launcher.ACTION_APPS_ARRANGE_UPDATING);
	        
	        registerReceiver(mProgressDialogReceive, progressDialogDisplayFilter);
	        
	        this.sendBroadcast(new Intent(Launcher.ACTION_DATA_ISLOADING));
	        
	        //<<end
            
            
            if (sPausedFromUserAction) {
                // If the user leaves launcher, then we should just load items asynchronously when
                // they return.
                mModel.startLoader(true, -1);
            } else {
                // We only load the page synchronously if the user rotates (or triggers a
                // configuration change) while launcher is in the foreground
                mModel.startLoader(true, mWorkspace.getCurrentPage());
            }
        }
        //added by ETON guolinan
      /*  if (!mModel.isAllAppsLoaded()) {
            ViewGroup appsCustomizeContentParent = (ViewGroup) mAppsCustomizeContent.getParent();
            mInflater.inflate(R.layout.apps_customize_progressbar, appsCustomizeContentParent);
        }*/
        //end
        // For handling default keys
        mDefaultKeySsb = new SpannableStringBuilder();
        Selection.setSelection(mDefaultKeySsb, 0);

        IntentFilter filter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mCloseSystemDialogsReceiver, filter);
        
 
        
        //deleted by ETON guolinan
//        updateGlobalIcons();
        //end
        // On large interfaces, we want the screen to auto-rotate based on the current orientation
        unlockScreenOrientation(true);
        
        if (mIsShowCustommenu){
        	setListMenuParams();
        }
        // M: Register orientation listener.
        registerOrientationListener();
        registerSharedPreferenceListener();
        
        
        
        
        Log.v("tianlei", "-------------oncreate--------------");
        
        IntentFilter filter2 =  new  IntentFilter();
        filter2.addAction(UpdateManager.check_broadcast);
        filter2.addAction(UpdateManager.update_broadcast);
        filter2.addAction(UpdateManager.cancle_Download);
        
//        this.registerReceiver(new CheckBroadcast(), filter2);
        this.registerReceiver(checkBroadcastReceiver, filter2);
       
        mWallReceiver = new WallpaperReceiver();   
        IntentFilter filterWall = new IntentFilter(Intent.ACTION_WALLPAPER_CHANGED);  
        registerReceiver(mWallReceiver, filterWall);  
        // 测试 --------
//        SharedPreferences mSharedPrefs = getSharedPreferences(LauncherApplication.getSharedPreferencesKey(),
//            Context.MODE_PRIVATE);
//         mSharedPrefs.edit()
//        .putBoolean(UpdateManager.check_update, true)
//        .commit();
         //-------测试 end
//         check_Update();
    }

    //added by shanlijuan
    /**
     * get SysStatusBarHeight
     * @param activity
     * @return SysStatusBarHeight
     * 
     */
    private int getSysStatusBarHeight(Activity activity){
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            Field field = clazz.getField("status_bar_height");
            int dpHeight = Integer.parseInt(field.get(object).toString());
            int pxHeight = activity.getResources().getDimensionPixelSize(dpHeight);
            return pxHeight;
        } catch (Exception e1) {
            e1.printStackTrace();
            return 0;
        } 
    }
    //end
    
    private class CheckBroadcast extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
          if(intent.getAction().equals(UpdateManager.check_broadcast))
          {
              check_Update();
          }
          else if(intent.getAction().equals(UpdateManager.update_broadcast))
          {
              
              check_AndDownloadUpdate();
              
          }
          else if(intent.getAction().equals(UpdateManager.cancle_Download))
          {
              
            if(updateManager != null)
            {
                updateManager.interceptFlag= true;
            }
              
          }
        }
    }
   private UpdateManager updateManager;
    private void check_AndDownloadUpdate()
    {
        
        String content =  mSharedPrefs.getString(UpdateManager.apkContentText, "");
        if(content.length() > 0)
        {
             updateManager = new  UpdateManager(this);
            updateManager.getState();
            updateManager.showNoticeDialog(content,1);
        }
        
    }
    private void  check_Update()
    {
      
        if(mSharedPrefs.getBoolean(UpdateManager.check_update, false))
        {
            mSharedPrefs.edit()
                        .putBoolean(UpdateManager.check_update, false)
                        .commit();
            
            UpdateManager updateManager = new  UpdateManager(this);
            updateManager.checkUpdateInfo();
            
        }
    }
    
    
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        sPausedFromUserAction = true;
    }
    //deleted by ETON guolinan
   /* private void updateGlobalIcons() {
        boolean searchVisible = false;
        boolean voiceVisible = false;
        // If we have a saved version of these external icons, we load them up immediately
        int coi = getCurrentOrientationIndexForGlobalIcons();
        if (sGlobalSearchIcon[coi] == null || sVoiceSearchIcon[coi] == null ||
                sAppMarketIcon[coi] == null) {
            updateAppMarketIcon();
            searchVisible = updateGlobalSearchIcon();
            voiceVisible = updateVoiceSearchIcon(searchVisible);
        }
        if (sGlobalSearchIcon[coi] != null) {
             updateGlobalSearchIcon(sGlobalSearchIcon[coi]);
             searchVisible = true;
        }
        if (sVoiceSearchIcon[coi] != null) {
            updateVoiceSearchIcon(sVoiceSearchIcon[coi]);
            voiceVisible = true;
        }
        if (sAppMarketIcon[coi] != null) {
            updateAppMarketIcon(sAppMarketIcon[coi]);
        }
        if (mSearchDropTargetBar != null) {
            mSearchDropTargetBar.onSearchPackagesChanged(searchVisible, voiceVisible);
        }
    }*/
    //end
    private void checkForLocaleChange() {
        if (sLocaleConfiguration == null) {
            new AsyncTask<Void, Void, LocaleConfiguration>() {
                @Override
                protected LocaleConfiguration doInBackground(Void... unused) {
                    LocaleConfiguration localeConfiguration = new LocaleConfiguration();
                    readConfiguration(Launcher.this, localeConfiguration);
                    return localeConfiguration;
                }

                @Override
                protected void onPostExecute(LocaleConfiguration result) {
                    sLocaleConfiguration = result;
                    checkForLocaleChange();  // recursive, but now with a locale configuration
                }
            }.execute();
            return;
        }

        final Configuration configuration = getResources().getConfiguration();

        final String previousLocale = sLocaleConfiguration.locale;
        final String locale = configuration.locale.toString();

        final int previousMcc = sLocaleConfiguration.mcc;
        final int mcc = configuration.mcc;

        final int previousMnc = sLocaleConfiguration.mnc;
        final int mnc = configuration.mnc;

        boolean localeChanged = !locale.equals(previousLocale) || mcc != previousMcc || mnc != previousMnc;

        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "checkForLocaleChange: previousLocale = " + previousLocale
                    + ", locale = " + locale + ", previousMcc = " + previousMcc + ", mcc = " + mcc
                    + ", previousMnc = " + previousMnc + ", mnc = " + mnc + ", localeChanged = "
                    + localeChanged + ", this = " + this);
        }

        if (localeChanged) {
            sLocaleConfiguration.locale = locale;
            sLocaleConfiguration.mcc = mcc;
            sLocaleConfiguration.mnc = mnc;

            /// M: When locale changed, reset collator and flush caches.
            sLocaleChanged = localeChanged;
            mModel.setFlushCache();
            mIconCache.flush();

            final LocaleConfiguration localeConfiguration = sLocaleConfiguration;
            new Thread("WriteLocaleConfiguration") {
                @Override
                public void run() {
                    writeConfiguration(Launcher.this, localeConfiguration);
                }
            }.start();
        }
    }

    private static class LocaleConfiguration {
        public String locale;
        public int mcc = -1;
        public int mnc = -1;
    }

    private static void readConfiguration(Context context, LocaleConfiguration configuration) {
        DataInputStream in = null;
        try {
            in = new DataInputStream(context.openFileInput(PREFERENCES));
            configuration.locale = in.readUTF();
            configuration.mcc = in.readInt();
            configuration.mnc = in.readInt();
        } catch (FileNotFoundException e) {
            LauncherLog.d(TAG, "FileNotFoundException when read configuration.");
        } catch (IOException e) {
            LauncherLog.d(TAG, "IOException when read configuration.");
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    LauncherLog.d(TAG, "IOException when close file.");
                }
            }
        }
    }

    private static void writeConfiguration(Context context, LocaleConfiguration configuration) {
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(context.openFileOutput(PREFERENCES, MODE_PRIVATE));
            out.writeUTF(configuration.locale);
            out.writeInt(configuration.mcc);
            out.writeInt(configuration.mnc);
            out.flush();
        } catch (FileNotFoundException e) {
            LauncherLog.d(TAG, "FileNotFoundException when write configuration.");
        } catch (IOException e) {
            //noinspection ResultOfMethodCallIgnored
            context.getFileStreamPath(PREFERENCES).delete();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    LauncherLog.d(TAG, "IOException when close file.");
                }
            }
        }
    }

    public DragLayer getDragLayer() {
        return mDragLayer;
    }

    boolean isDraggingEnabled() {
        // We prevent dragging when we are loading the workspace as it is possible to pick up a view
        // that is subsequently removed from the workspace in startBinding().
        return !mModel.isLoadingWorkspace();
    }

    static int getScreen() {
        synchronized (sLock) {
            return sScreen;
        }
    }

    static void setScreen(int screen) {
        synchronized (sLock) {
            sScreen = screen;
        }
    }

    /**
     * Returns whether we should delay spring loaded mode -- for shortcuts and widgets that have
     * a configuration step, this allows the proper animations to run after other transitions.
     */
    private boolean completeAdd(PendingAddArguments args) {
        boolean result = false;
        switch (args.requestCode) {
            case REQUEST_PICK_APPLICATION:
                completeAddApplication(args.intent, args.container, args.screen, args.cellX,
                        args.cellY);
                break;
            case REQUEST_PICK_SHORTCUT:
                processShortcut(args.intent);
                break;
            case REQUEST_CREATE_SHORTCUT:
                completeAddShortcut(args.intent, args.container, args.screen, args.cellX,
                        args.cellY);
                result = true;
                break;
            case REQUEST_CREATE_APPWIDGET:
                int appWidgetId = args.intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
                completeAddAppWidget(appWidgetId, args.container, args.screen, null, null);
                result = true;
                break;
            case REQUEST_PICK_WALLPAPER:
                // We just wanted the activity result here so we can clear mWaitingForResult
                break;
        }
        // Before adding this resetAddInfo(), after a shortcut was added to a workspace screen,
        // if you turned the screen off and then back while in All Apps, Launcher would not
        // return to the workspace. Clearing mAddInfo.container here fixes this issue
        resetAddInfo();
        return result;
    }

    @Override
    protected void onActivityResult(
            final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == REQUEST_BIND_APPWIDGET) {
            int appWidgetId = data != null ?
                    data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) : -1;
            //Modified by chenxin
            mWaitingForResult = false;
            
            if (resultCode == RESULT_CANCELED) {
                completeTwoStageWidgetDrop(RESULT_CANCELED, appWidgetId);
            } else if (resultCode == RESULT_OK) {
            	//M: by chenxin
            	mIsAddSystemWidget = true;
            	
                addAppWidgetImpl(appWidgetId, mPendingAddInfo, null, mPendingAddWidgetInfo);
            }
            return;
        }
        boolean delayExitSpringLoadedMode = false;
        boolean isWidgetDrop = (requestCode == REQUEST_PICK_APPWIDGET ||
                requestCode == REQUEST_CREATE_APPWIDGET);
        mWaitingForResult = false;

        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "onActivityResult: requestCode = " + requestCode 
                    + ", resultCode = " + resultCode + ", data = " + data 
                    + ", mPendingAddInfo = " + mPendingAddInfo);
        }

        // We have special handling for widgets
        if (isWidgetDrop) {
            int appWidgetId = data != null ?
                    data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) : -1;
            if (appWidgetId < 0) {
                Log.e(TAG, "Error: appWidgetId (EXTRA_APPWIDGET_ID) was not returned from the \\" +
                        "widget configuration activity.");
                completeTwoStageWidgetDrop(RESULT_CANCELED, appWidgetId);
            } else {
//                completeTwoStageWidgetDrop(resultCode, appWidgetId);
            	//Modified by chenxin
            	//Fixed when in the add screen, not allow to add shortcut and widget
            	if ((getCurrentWorkspaceScreen()+1) == mWorkspace.getChildCount()){
            		Toast.makeText(this,
    						getString(R.string.screen_not_allow_add),Toast.LENGTH_SHORT).show();
            		return;
            	}
            		
                addSystemWidget(resultCode, appWidgetId);
            }
            return;
        }

        // The pattern used here is that a user PICKs a specific application,
        // which, depending on the target, might need to CREATE the actual target.

        // For example, the user would PICK_SHORTCUT for "Music playlist", and we
        // launch over to the Music app to actually CREATE_SHORTCUT.
        if (resultCode == RESULT_OK && mPendingAddInfo.container != ItemInfo.NO_ID) {
            final PendingAddArguments args = new PendingAddArguments();
            args.requestCode = requestCode;
            args.intent = data;
            args.container = mPendingAddInfo.container;
            args.screen = mPendingAddInfo.screen;
            args.cellX = mPendingAddInfo.cellX;
            args.cellY = mPendingAddInfo.cellY;
            if (isWorkspaceLocked()) {
                sPendingAddList.add(args);
            } else {
                delayExitSpringLoadedMode = completeAdd(args);
            }
        }
        mDragLayer.clearAnimatedView();
        // Exit spring loaded mode if necessary after cancelling the configuration of a widget
        exitSpringLoadedDragModeDelayed((resultCode != RESULT_CANCELED), delayExitSpringLoadedMode,
                null);
    }
    
    @Override 
    protected void onStart() {
        super.onStart();
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "(Launcher)onStart: this = " + this);
        }

        if (isAllAppsVisible() && mAppsCustomizeTabHost.getVisibility() == View.VISIBLE
                && mAppsCustomizeTabHost.getContentVisibility() == View.GONE) {
            mAppsCustomizeTabHost.setContentVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "(Launcher)onStop: this = " + this);
        }
    }
    //added by ETON guolinan
    private void addSystemWidget(final int resultCode, final int appWidgetId) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "addSystemWidget resultCode = " + resultCode + ", appWidgetId = " + appWidgetId
                    + ", mPendingAddInfo.screen = " + mPendingAddInfo.screen);
        }
        AppWidgetProviderInfo info = AppWidgetManager.getInstance(this).getAppWidgetInfo(appWidgetId);
        if (info == null) return;
        PendingAddWidgetInfo createItemInfo = new PendingAddWidgetInfo(info, null, null);

        // Determine the widget spans and min resize spans.
        int[] spanXY = Launcher.getSpanForWidget(this, info);
        createItemInfo.spanX = spanXY[0];
        createItemInfo.spanY = spanXY[1];
        int[] minSpanXY = Launcher.getMinSpanForWidget(this, info);
        createItemInfo.minSpanX = minSpanXY[0];
        createItemInfo.minSpanY = minSpanXY[1];
       
        final long container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
        int screen = mWorkspace.getCurrentPage();
        int[] loc = new int[]{0,0};
        int[] cell = new int[]{-1,-1};
        int[] span = new int[]{createItemInfo.minSpanX,createItemInfo.minSpanY};
        
        resetAddInfo();
        mPendingAddInfo.container = createItemInfo.container = container;
        mPendingAddInfo.screen = createItemInfo.screen = screen;
        mPendingAddInfo.dropPos = loc;
        mPendingAddInfo.minSpanX = createItemInfo.minSpanX;
        mPendingAddInfo.minSpanY = createItemInfo.minSpanY;
        mPendingAddWidgetInfo = info;
        CellLayout cellLayout = (CellLayout) mWorkspace.getChildAt(mPendingAddInfo.screen);
        
        //added by chenxin
        //if findNearestArea,cause screen mOccupied not correct
        /*cell = cellLayout.findNearestArea(0, 0, span[0], span[1], cell);
        if (cell != null) {
            mPendingAddInfo.cellX = cell[0];
            mPendingAddInfo.cellY = cell[1];
        }
        if (span != null) {
            mPendingAddInfo.spanX = span[0];
            mPendingAddInfo.spanY = span[1];
        }
        
        int temp = 0;
        int y =0; 
        boolean foundcell = false;
        for(y = 0; y < 4;y++){
        
        	for(int x = 0; x < 4;x++)
        	{
        		if(cellLayout.getChildAt(x, y)!= null){
        			temp = 0;
        			break;
        		}
        		if(x == 3)
        		temp ++;
        	}
        	if(temp == mPendingAddInfo.spanY){
        		foundcell = true;
        		break;
        	}
        }
        if(!foundcell){
        	showOutOfSpaceMessage(false);
        	return;
        }else {
        	if ((y = y - mPendingAddInfo.spanY + 1) >= 0){
        		mPendingAddInfo.cellX = 0;
        		mPendingAddInfo.cellY = y;
        	}else {
        		showOutOfSpaceMessage(false);
        		return;
        	}
        }*/

        Runnable onCompleteRunnable = null;
        int animationType = 0;

        AppWidgetHostView boundWidget = null;
        if (resultCode == RESULT_OK) {
            animationType = Workspace.COMPLETE_TWO_STAGE_WIDGET_DROP_ANIMATION;
            final AppWidgetHostView layout = mAppWidgetHost.createView(this, appWidgetId,
                    mPendingAddWidgetInfo);
            boundWidget = layout;
            onCompleteRunnable = new Runnable() {
                @Override
                public void run() {
                	//M:by chenxin
                	mIsAddSystemWidget = true;
                	
                    completeAddAppWidget(appWidgetId, mPendingAddInfo.container,
                            mPendingAddInfo.screen, layout, null);
                    exitSpringLoadedDragModeDelayed((resultCode != RESULT_CANCELED), false,
                            null);
                }
            };
        } else if (resultCode == RESULT_CANCELED) {
            animationType = Workspace.CANCEL_TWO_STAGE_WIDGET_DROP_ANIMATION;
            onCompleteRunnable = new Runnable() {
                @Override
                public void run() {
                    exitSpringLoadedDragModeDelayed((resultCode != RESULT_CANCELED), false,
                            null);
                }
            };
        }
        if (mDragLayer.getAnimatedView() != null) {
            mWorkspace.animateWidgetDrop(mPendingAddInfo, cellLayout,
                    (DragView) mDragLayer.getAnimatedView(), onCompleteRunnable,
                    animationType, boundWidget, true);
        } else {
            // The animated view may be null in the case of a rotation during widget configuration
            onCompleteRunnable.run();
        }
    }
    //end
    private void completeTwoStageWidgetDrop(final int resultCode, final int appWidgetId) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "completeTwoStageWidgetDrop resultCode = " + resultCode + ", appWidgetId = " + appWidgetId
                    + ", mPendingAddInfo.screen = " + mPendingAddInfo.screen);
        }

        CellLayout cellLayout = (CellLayout) mWorkspace.getChildAt(mPendingAddInfo.screen);
        Runnable onCompleteRunnable = null;
        int animationType = 0;

        AppWidgetHostView boundWidget = null;
        if (resultCode == RESULT_OK) {
            animationType = Workspace.COMPLETE_TWO_STAGE_WIDGET_DROP_ANIMATION;
            final AppWidgetHostView layout = mAppWidgetHost.createView(this, appWidgetId,
                    mPendingAddWidgetInfo);
            boundWidget = layout;
            onCompleteRunnable = new Runnable() {
                @Override
                public void run() {
                    completeAddAppWidget(appWidgetId, mPendingAddInfo.container,
                            mPendingAddInfo.screen, layout, null);
                    exitSpringLoadedDragModeDelayed((resultCode != RESULT_CANCELED), false,
                            null);
                }
            };
        } else if (resultCode == RESULT_CANCELED) {
            animationType = Workspace.CANCEL_TWO_STAGE_WIDGET_DROP_ANIMATION;
            onCompleteRunnable = new Runnable() {
                @Override
                public void run() {
                    exitSpringLoadedDragModeDelayed((resultCode != RESULT_CANCELED), false,
                            null);
                }
            };
        }
        if (mDragLayer.getAnimatedView() != null) {
            mWorkspace.animateWidgetDrop(mPendingAddInfo, cellLayout,
                    (DragView) mDragLayer.getAnimatedView(), onCompleteRunnable,
                    animationType, boundWidget, true);
        } else {
            // The animated view may be null in the case of a rotation during widget configuration
            onCompleteRunnable.run();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "(Launcher)onResume: mRestoring = " + mRestoring
                    + ", mOnResumeNeedsLoad = " + mOnResumeNeedsLoad + ",mOrientationChanged = "
                    + mOrientationChanged + ",mPagesAreRecreated = " + mPagesWereRecreated
                    + ", this = " + this);
        }

        /// M: if the orientation changed and remove views happened during Launcher
        /// activity paused, we need to re-sync all apps pages, because the views
        /// recreated after view removed would use landscape resources.
        //>> remove by eton wanghenan
//        if (mOrientationChanged && mPagesWereRecreated) {
//            LauncherLog.d(TAG, "(Launcher)onResume: mOrientationChanged && mPagesWereRecreated");
//            mAppsCustomizeContent.invalidateAppPages(mAppsCustomizeContent.getCurrentPage(), true);
//        }
        //<< remove end
        resetReSyncFlags();

        /// M: Call the appropriate callback for the IMtkWidget on the current page when we resume Launcher.
        /*mWorkspace.onResumeWhenShown(mWorkspace.getCurrentPage());*/
        
        // Restore the previous launcher state
        if (mOnResumeState == State.WORKSPACE) {
            showWorkspace(false);
        } else if (mOnResumeState == State.APPS_CUSTOMIZE) {
            showAllApps(false);
        }
        mOnResumeState = State.NONE;

        // Background was set to gradient in onPause(), restore to black if in all apps.
        setWorkspaceBackground(mState == State.WORKSPACE);

        // Process any items that were added while Launcher was away
        InstallShortcutReceiver.flushInstallQueue(this);

        mPaused = false;
        sPausedFromUserAction = false;
        if (mRestoring || mOnResumeNeedsLoad) {
            //>>modify by eton wanghenan;
//            mWorkspaceLoading = true;
//            mIsLoadingWorkspace = true;
            // mModel.startLoader(true, -1);
            //<<end
            mRestoring = false;
            mOnResumeNeedsLoad = false;
        }
        // Reset the pressed state of icons that were locked in the press state while activities
        // were launching
        if (mWaitingForResume != null) {
            // Resets the previous workspace icon press state
            mWaitingForResume.setStayPressed(false);
        }
        if (mAppsCustomizeContent != null) {
            // Resets the previous all apps icon press state
            mAppsCustomizeContent.resetDrawableState();
        }
        // It is possible that widgets can receive updates while launcher is not in the foreground.
        // Consequently, the widgets will be inflated in the orientation of the foreground activity
        // (framework issue). On resuming, we ensure that any widgets are inflated for the current
        // orientation.
        getWorkspace().reinflateWidgetsIfNecessary();

        // Again, as with the above scenario, it's possible that one or more of the global icons
        // were updated in the wrong orientation.
        //deleted by ETON guolinan
//        updateGlobalIcons();
        //end
        /// M: Enable orientation listener when we resume Launcher.
        enableOrientationListener();

    }

    @Override
    protected void onPause() {
        // NOTE: We want all transitions from launcher to act as if the wallpaper were enabled
        // to be consistent.  So re-enable the flag here, and we will re-disable it as necessary
        // when Launcher resumes and we are still in AllApps.
        updateWallpaperVisibility(true);

        super.onPause();
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "(Launcher)onPause: this = " + this);
        }

        /// M: Call the appropriate callback for the IMtkWidget on the current page when we pause Launcher.
        /*mWorkspace.onPauseWhenShown(mWorkspace.getCurrentPage());*/
        resetReSyncFlags();

        mPaused = true;
        mDragController.cancelDrag();
        mDragController.resetLastGestureUpTime();

        /// M: Disable the orientation listener when we pause Launcher.
        disableOrientationListener();
        
        //>>add by eton wanghenan
        sendBroadcast(new Intent(SectorView.ACTION_SECTORVIEW_REMOVE));
        //<<end
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "onRetainNonConfigurationInstance: mSavedState = "
                    + mSavedState + ", mSavedInstanceState = " + mSavedInstanceState);
        }

        // Flag the loader to stop early before switching
        mModel.stopLoader();
        if (mAppsCustomizeContent != null) {
            mAppsCustomizeContent.surrender();
        }
        return Boolean.TRUE;
    }

    // We can't hide the IME if it was forced open.  So don't bother
    /*
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            final InputMethodManager inputManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            inputManager.hideSoftInputFromWindow(lp.token, 0, new android.os.ResultReceiver(new
                        android.os.Handler()) {
                        protected void onReceiveResult(int resultCode, Bundle resultData) {
                            Log.d(TAG, "ResultReceiver got resultCode=" + resultCode);
                        }
                    });
            Log.d(TAG, "called hideSoftInputFromWindow from onWindowFocusChanged");
        }
    }
    */

    private boolean acceptFilter() {
        final InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        return !inputManager.isFullscreenMode();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	
    	if (keyCode == KeyEvent.KEYCODE_MENU) {
			if (null != mPopWindow && mPopWindow.isShowing()) {
				
				Log.d("TestB", "isShowing");
				mPopWindow.dismiss();
			} else if (null != mPopWindow){	
				Log.d("TestB", "is not Showing");
				
				mPopWindow.showAtLocation(findViewById(R.id.drag_layer),Gravity.BOTTOM, 0, 0);

			}
		}else if (keyCode == KeyEvent.KEYCODE_BACK){
			if (null != mPopWindow && mPopWindow.isShowing()){
				mPopWindow.dismiss();
				
			}
		}
    	
        final int uniChar = event.getUnicodeChar();
        final boolean handled = super.onKeyDown(keyCode, event);
        final boolean isKeyNotWhitespace = uniChar > 0 && !Character.isWhitespace(uniChar);
        if (LauncherLog.DEBUG_KEY) {
            LauncherLog.d(TAG, " onKeyDown: KeyCode = " + keyCode + ", KeyEvent = " + event
                    + ", uniChar = " + uniChar + ", handled = " + handled + ", isKeyNotWhitespace = "
                    + isKeyNotWhitespace);
        }

        //>>added by eton wanghenan
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU ) {
        	sendBroadcast(new Intent(SectorView.ACTION_SECTORVIEW_REMOVE_WITH_UNPOPANIMATION));
        }
        //<<end
        
        
        if (!handled && acceptFilter() && isKeyNotWhitespace) {
            boolean gotKey = TextKeyListener.getInstance().onKeyDown(mWorkspace, mDefaultKeySsb,
                    keyCode, event);
            if (gotKey && mDefaultKeySsb != null && mDefaultKeySsb.length() > 0) {
                // something usable has been typed - start a search
                // the typed text will be retrieved and cleared by
                // showSearchDialog()
                // If there are multiple keystrokes before the search dialog takes focus,
                // onSearchRequested() will be called for every keystroke,
                // but it is idempotent, so it's fine.
                return onSearchRequested();
            }
        }

        // Eat the long press event so the keyboard doesn't come up.
        if (keyCode == KeyEvent.KEYCODE_MENU && event.isLongPress()) {
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
            /// M: invalidate the option menu before menu pop ups. Since the
            // menu item count differs between workspace and app list, if we
            // press menu key in workspace, and then do it in app list,the
            // menu window will animate because window size changed. We add this
            // step to force re-create menu decor view, this would lower the
            // time duration of option menu pop ups. Also we could do it only
            // when the menu pop switch between workspace and app list.
            invalidateOptionsMenu();
        }
        

        

        return handled;
    }

    private String getTypedText() {
        return mDefaultKeySsb.toString();
    }

    private void clearTypedText() {
        mDefaultKeySsb.clear();
        mDefaultKeySsb.clearSpans();
        Selection.setSelection(mDefaultKeySsb, 0);
    }

    /**
     * Given the integer (ordinal) value of a State enum instance, convert it to a variable of type
     * State
     */
    private static State intToState(int stateOrdinal) {
        State state = State.WORKSPACE;
        final State[] stateValues = State.values();
        for (int i = 0; i < stateValues.length; i++) {
            if (stateValues[i].ordinal() == stateOrdinal) {
                state = stateValues[i];
                break;
            }
        }
        return state;
    }

    /**
     * Restores the previous state, if it exists.
     *
     * @param savedState The previous state.
     */
    private void restoreState(Bundle savedState) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "restoreState: savedState = " + savedState);
        }

        if (savedState == null) {
            return;
        }

        State state = intToState(savedState.getInt(RUNTIME_STATE, State.WORKSPACE.ordinal()));
        if (state == State.APPS_CUSTOMIZE) {
            mOnResumeState = State.APPS_CUSTOMIZE;
        }

        int currentScreen = savedState.getInt(RUNTIME_STATE_CURRENT_SCREEN, -1);
        if (currentScreen > -1) {
            mWorkspace.setCurrentPage(currentScreen);
        }

        final long pendingAddContainer = savedState.getLong(RUNTIME_STATE_PENDING_ADD_CONTAINER, -1);
        final int pendingAddScreen = savedState.getInt(RUNTIME_STATE_PENDING_ADD_SCREEN, -1);

        if (pendingAddContainer != ItemInfo.NO_ID && pendingAddScreen > -1) {
            mPendingAddInfo.container = pendingAddContainer;
            mPendingAddInfo.screen = pendingAddScreen;
            mPendingAddInfo.cellX = savedState.getInt(RUNTIME_STATE_PENDING_ADD_CELL_X);
            mPendingAddInfo.cellY = savedState.getInt(RUNTIME_STATE_PENDING_ADD_CELL_Y);
            mPendingAddInfo.spanX = savedState.getInt(RUNTIME_STATE_PENDING_ADD_SPAN_X);
            mPendingAddInfo.spanY = savedState.getInt(RUNTIME_STATE_PENDING_ADD_SPAN_Y);
            mPendingAddWidgetInfo = savedState.getParcelable(RUNTIME_STATE_PENDING_ADD_WIDGET_INFO);
            mWaitingForResult = true;
            mRestoring = true;
        }


        boolean renameFolder = savedState.getBoolean(RUNTIME_STATE_PENDING_FOLDER_RENAME, false);
        if (renameFolder) {
            long id = savedState.getLong(RUNTIME_STATE_PENDING_FOLDER_RENAME_ID);
            mFolderInfo = mModel.getFolderById(this, sFolders, id);
            mRestoring = true;
        }

        // Restore the AppsCustomize tab
        if (mAppsCustomizeTabHost != null) {
            String curTab = savedState.getString("apps_customize_currentTab");
            if (curTab != null) {
                mAppsCustomizeTabHost.setContentTypeImmediate(
                        mAppsCustomizeTabHost.getContentTypeForTabTag(curTab));
                mAppsCustomizeContent.loadAssociatedPages(
                        mAppsCustomizeContent.getCurrentPage());
            }

            int currentIndex = savedState.getInt("apps_customize_currentIndex");
            mAppsCustomizeContent.restorePageForIndex(currentIndex);
        }
    }

   //added by chenxin
   private SimpleAdapter mGridViewadapter;
   private GridView mGridView;
     private ViewPager mScreenEditGridView;
   //private Gallery mScreenEditGridView;
   private ScreenEditTextView mMenuWallpaper;
   //private ScreenEditTextView mMenuTheme;
   private ScreenEditTextView mMenuEffect;
   private ScreenEditTextView mMenuWidget;
    /**
     * Finds all the views we need and configure them properly.
     */
    private void setupViews() {
        mDeleteZoneTest = (LinearLayout)findViewById(R.id.screen_edit);
        //mGridView = (GridView)mDeleteZoneTest.findViewById(R.id.griditem);        
        //mScreenEditGridView = (ScreenEditGridView)mDeleteZoneTest.findViewById(R.id.griditem);        
        mScreenEditGridView = (ViewPager)mDeleteZoneTest.findViewById(R.id.viewpager);        
       // mScreenEditGridView = (Gallery)mDeleteZoneTest.findViewById(R.id.griditem);        
        mMenuWallpaper = (ScreenEditTextView)mDeleteZoneTest.findViewById(R.id.menu_wallpaper);
        mMenuWallpaper.setSelected(true);
        mScreenEditUtil.mTempView = mMenuWallpaper;
        
        //mMenuTheme = (ScreenEditTextView)mDeleteZoneTest.findViewById(R.id.menu_theme);
        mMenuEffect = (ScreenEditTextView)mDeleteZoneTest.findViewById(R.id.menu_effect);
        mMenuWidget = (ScreenEditTextView)mDeleteZoneTest.findViewById(R.id.menu_widget);
        
        mMenuWallpaper.setTag(ScreenEditUtil.MENU_ID_WALLPAPER);
        //mMenuTheme.setTag(ScreenEditUtil.MENU_ID_THEME);
        mMenuEffect.setTag(ScreenEditUtil.MENU_ID_EFFECT);
        mMenuWidget.setTag(ScreenEditUtil.MENU_ID_WIDGET);

        mScreenEditUtil.setScreenEditEvent(mMenuWallpaper);
        //mScreenEditUtil.setScreenEditEvent(mMenuTheme);
        mScreenEditUtil.setScreenEditEvent(mMenuEffect);
        mScreenEditUtil.setScreenEditEvent(mMenuWidget);
       
        //mGridView.setNumColumns(15);
        //mScreenEditUtil.setGridViewObject(mGridView,this);
        mScreenEditUtil.setGridViewObject(mScreenEditGridView,this);
        //mScreenEditUtil.defaultUI();
      
        final DragController dragController = mDragController;

        mLauncherView = getLayoutInflater().inflate(R.layout.launcher,null);
        mDragLayer = (DragLayer) findViewById(R.id.drag_layer);
        
        //added by shanlijuan
        View workSpaceView = mDragLayer.findViewById(R.id.workspace);
        int mHotseatHeight = LauncherApplication.getDisplayFactory(Launcher.this).getHotseatHeight();
        int mScrollIndicatorHeight = LauncherApplication.getDisplayFactory(Launcher.this).getScrollingIndicatorHeight();
        workSpaceView.setPadding(0, 0, 0, (mHotseatHeight + mScrollIndicatorHeight));
        //end
        
        mWorkspace = (Workspace) workSpaceView;
        
        
        //end
        //deleted by ETON guolinan
        /*mQsbDivider = (ImageView) findViewById(R.id.qsb_divider);
        mDockDivider = (ImageView) findViewById(R.id.dock_divider);*/
        //end
        mLauncherView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        mWorkspaceBackgroundDrawable = getResources().getDrawable(R.drawable.workspace_bg);
        mBlackBackgroundDrawable = new ColorDrawable(Color.BLACK);

        // Setup the drag layer
        mDragLayer.setup(this, dragController);

        // Setup the hotseat
        mHotseat = (Hotseat) findViewById(R.id.hotseat);
        if (mHotseat != null) {
            mWidth = this.getWindowManager().getDefaultDisplay().getWidth();
            mHotseat.setup(this);
            mHotseat.getLayout().mShortcutsAndWidgets.setWidth(mWidth);
        }

        // Setup the workspace
        mWorkspace.setHapticFeedbackEnabled(false);
        mWorkspace.setOnLongClickListener(this);
        mWorkspace.setup(dragController);
        dragController.addDragListener(mWorkspace);

        // Get the search/delete bar
        mSearchDropTargetBar = (SearchDropTargetBar) mDragLayer.findViewById(R.id.qsb_bar);

        // Setup AppsCustomize
        mAppsCustomizeTabHost = (AppsCustomizeTabHost) findViewById(R.id.apps_customize_pane);
        mAppsCustomizeContent = (AppsCustomizePagedView)
                mAppsCustomizeTabHost.findViewById(R.id.apps_customize_pane_content);
        mAppsCustomizeContent.setup(this, dragController);

        // Setup the drag controller (drop targets have to be added in reverse order in priority)
        dragController.setDragScoller(mWorkspace);
        dragController.setScrollView(mDragLayer);
        dragController.setMoveTarget(mWorkspace);
        dragController.addDropTarget(mWorkspace);
        if (mSearchDropTargetBar != null) {
//            mSearchDropTargetBar.setup(this, dragController);
        }
        //added by ETON guolinan
        mDeleteZone = (DeleteZone)findViewById(R.id.delete_zone);
        if(mDeleteZone != null){
        	mDeleteZone.setup(this, mDragController);
        }
        //end
        
    	mScrollingIndicator =  mWorkspace.getScrollingIndicator();
		mScrollingIndicator.setPageCount(mWorkspace.getChildCount());
		mScrollingIndicator.setCurrentPage(mWorkspace.getCurrentPage());
		mScrollingIndicator.addPageIndex(mWorkspace.getCurrentPage(),mWorkspace.getChildCount());
		mWorkspace.setPageSwitchListener(mScrollingIndicator);
		
		mAppsScrollingIndicator = (ScrollingIndicator)mAppsCustomizeTabHost.findViewById(R.id.scrollingIndicator_1);
		mAppsScrollingIndicator.setPageCount(mAppsCustomizeContent.getChildCount());
		mAppsScrollingIndicator.setCurrentPage(mAppsCustomizeContent.getCurrentPage());
		mAppsScrollingIndicator.addPageIndex(mAppsCustomizeContent.getCurrentPage(),mAppsCustomizeContent.getChildCount());
		mAppsCustomizeContent.setPageSwitchListener(mAppsScrollingIndicator);
		
    }
    
    View createShortcut(ShortcutInfo info) {
        return createShortcut(R.layout.mtk_application,
                (ViewGroup) mWorkspace.getChildAt(mWorkspace.getCurrentPage()), info);
    }

    /**
     * Creates a view representing a shortcut inflated from the specified resource.
     *
     * @param layoutResId The id of the XML layout used to create the shortcut.
     * @param parent The group the shortcut belongs to.
     * @param info The data structure describing the shortcut.
     *
     * @return A View inflated from layoutResId.
     */
    View createShortcut(int layoutResId, ViewGroup parent, ShortcutInfo info) {
        MTKShortcut shortcut = (MTKShortcut)mInflater.inflate(layoutResId, parent, false);
        shortcut.applyFromShortcutInfo(info, mIconCache);
        //shortcut.setOnClickListener(this);
        shortcut.mFavorite.setOnClickListener(this);
        shortcut.mFavorite.setOnTouchListener(this);
        shortcut.mFavorite.setOnLongClickListener(this);
        return shortcut;
    }


    /**
     * Add an application shortcut to the workspace.
     *
     * @param data The intent describing the application.
     * @param cellInfo The position on screen where to create the shortcut.
     */
    void completeAddApplication(Intent data, long container, int screen, int cellX, int cellY) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "completeAddApplication: Intent = " + data
                    + ", container = " + container + ", screen = " + screen + ", cellX = " + cellX
                    + ", cellY = " + cellY);
        }    

        final int[] cellXY = mTmpAddItemCellCoordinates;
        final CellLayout layout = getCellLayout(container, screen);

        // First we check if we already know the exact location where we want to add this item.
        if (cellX >= 0 && cellY >= 0) {
            cellXY[0] = cellX;
            cellXY[1] = cellY;
        } else if (!layout.findCellForSpan(cellXY, 1, 1)) {
            showOutOfSpaceMessage(isHotseatLayout(layout));
            return;
        }

        final ShortcutInfo info = mModel.getShortcutInfo(getPackageManager(), data, this);

        if (info != null) {
            info.setActivity(data.getComponent(), Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            info.container = ItemInfo.NO_ID;
            mWorkspace.addApplicationShortcut(info, layout, container, screen, cellXY[0], cellXY[1],
                    isWorkspaceLocked(), cellX, cellY);
        } else {
            Log.e(TAG, "Couldn't find ActivityInfo for selected application: " + data);
        }
    }

    /**
     * Add a shortcut to the workspace.
     *
     * @param data The intent describing the shortcut.
     * @param cellInfo The position on screen where to create the shortcut.
     */
    private void completeAddShortcut(Intent data, long container, int screen, int cellX,
            int cellY) {
        int[] cellXY = mTmpAddItemCellCoordinates;
        int[] touchXY = mPendingAddInfo.dropPos;
        CellLayout layout = getCellLayout(container, screen);

        boolean foundCellSpan = false;

        ShortcutInfo info = mModel.infoFromShortcutIntent(this, data, null);
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "completeAddShortcut: info = " + info + ", data = " + data
                    + ", container = " + container + ", screen = " + screen + ", cellX = "
                    + cellX + ", cellY = " + cellY);
        }

        if (info == null) {
            return;
        }
        final View view = createShortcut(info);

        // First we check if we already know the exact location where we want to add this item.
        if (cellX >= 0 && cellY >= 0) {
            cellXY[0] = cellX;
            cellXY[1] = cellY;
            foundCellSpan = true;

            // If appropriate, either create a folder or add to an existing folder
            if (mWorkspace.createUserFolderIfNecessary(view, container, layout, cellXY, 0,
                    true, null,null)) {
                return;
            }
            DragObject dragObject = new DragObject();
            dragObject.dragInfo = info;
            if (mWorkspace.addToExistingFolderIfNecessary(view, layout, cellXY, 0, dragObject,
                    true)) {
                return;
            }
        } else if (touchXY != null) {
            // when dragging and dropping, just find the closest free spot
            int[] result = layout.findNearestVacantArea(touchXY[0], touchXY[1], 1, 1, cellXY);
            foundCellSpan = (result != null);
        } else {
            foundCellSpan = layout.findCellForSpan(cellXY, 1, 1);
        }

        if (!foundCellSpan) {
            showOutOfSpaceMessage(isHotseatLayout(layout));
            return;
        }

        LauncherModel.addItemToDatabase(this, info, container, screen, cellXY[0], cellXY[1], false);

        if (mIsLoadingWorkspace) {
        	mModel.forceReload();
        }
        
        if (!mRestoring) {
            mWorkspace.addInScreen(view, container, screen, cellXY[0], cellXY[1], 1, 1,
                    isWorkspaceLocked());
        }
    }

    static int[] getSpanForWidget(Context context, ComponentName component, int minWidth,
            int minHeight) {
        Rect padding = AppWidgetHostView.getDefaultPaddingForWidget(context, component, null);
        // We want to account for the extra amount of padding that we are adding to the widget
        // to ensure that it gets the full amount of space that it has requested
        int requiredWidth = minWidth + padding.left + padding.right;
        int requiredHeight = minHeight + padding.top + padding.bottom;
        return CellLayout.rectToCell(context.getResources(), requiredWidth, requiredHeight, null);
    }

    static int[] getSpanForWidget(Context context, AppWidgetProviderInfo info) {
        return getSpanForWidget(context, info.provider, info.minWidth, info.minHeight);
    }

    static int[] getMinSpanForWidget(Context context, AppWidgetProviderInfo info) {
        return getSpanForWidget(context, info.provider, info.minResizeWidth, info.minResizeHeight);
    }

    static int[] getSpanForWidget(Context context, PendingAddWidgetInfo info) {
        return getSpanForWidget(context, info.componentName, info.minWidth, info.minHeight);
    }

    static int[] getMinSpanForWidget(Context context, PendingAddWidgetInfo info) {
        return getSpanForWidget(context, info.componentName, info.minResizeWidth,
                info.minResizeHeight);
    }

    //added by xin.chen get appwidgetId 
    public int getEtonAppWidgetId(){
    	int appWidgetId = Launcher.this.mAppWidgetHost.allocateAppWidgetId();
    	return appWidgetId;
    }
    
    public void completeAddAppWidget(final int appWidgetId, CellLayout.CellInfo cellInfo){
    	 AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
    	 //CellLayout layout = (CellLayout) mWorkspace.getChildAt(cellInfo.screen);
    	 CellLayout layout = (CellLayout) mWorkspace.getChildAt(getCurrentWorkspaceScreen());
         if (cellInfo != null)
         Log.d("added","layout ="+layout + "         cellInfo.screen="+cellInfo.screen+"     appWidgetInfo="+appWidgetInfo+"  id="+appWidgetId);    	 
    	 //added by chenxin
		 //int[] spans = layout.rectToCell(appWidgetInfo.minWidth, appWidgetInfo.minHeight,null);
         int[] spans = getMinSpanForWidget(this, appWidgetInfo);
         
         //added by shanlijuan
         if (spans[0]>LauncherModel.getCellCountX()){
             spans[0] = LauncherModel.getCellCountX();
         }
         if (spans[1] > LauncherModel.getCellCountY()){
             spans[1] = LauncherModel.getCellCountY();
         }
         //end
         
    	 final int[] xy = mTmpAddItemCellCoordinates;
//    	 if (!findSlot(cellInfo, xy, spans[0], spans[1])) {
//             if (appWidgetId != -1) mAppWidgetHost.deleteAppWidgetId(appWidgetId);
//             return;
//         }
    	 
    	 if (!layout.findCellForSpan(xy, spans[0], spans[1])){
    		 //TODO show space is not valid
    		 if (appWidgetId != -1){
    			 mAppWidgetHost.deleteAppWidgetId(appWidgetId);
    		 }
    		 showOutOfSpaceMessage(false);
    		 return;
    	 }
    	 
    	 LauncherAppWidgetInfo launcherInfo = new LauncherAppWidgetInfo(appWidgetId,appWidgetInfo.provider);
    	 launcherInfo.spanX = spans[0];
         launcherInfo.spanY = spans[1];
         
         LauncherModel.addItemToDatabase(this, launcherInfo,
                 LauncherSettings.Favorites.CONTAINER_DESKTOP,
                 getCurrentWorkspaceScreen(), xy[0], xy[1], false);
         if (!mRestoring) {
        	// Perform actual inflation because we're live
             launcherInfo.hostView = mAppWidgetHost.createView(this, appWidgetId, appWidgetInfo);
             launcherInfo.hostView.setAppWidget(appWidgetId, appWidgetInfo);
             launcherInfo.hostView.setTag(launcherInfo);
             /*mWorkspace.addInScreen(launcherInfo.hostView,1, xy[0], xy[1],
                     launcherInfo.spanX, launcherInfo.spanY, isWorkspaceLocked());*/
             mWorkspace.addInScreen(launcherInfo.hostView, LauncherSettings.Favorites.CONTAINER_DESKTOP, getCurrentWorkspaceScreen(),xy[0], xy[1],
                     launcherInfo.spanX, launcherInfo.spanY, isWorkspaceLocked());
         }
         
    	 
    }

    /**
     * Add a widget to the workspace.
     *
     * @param appWidgetId The app widget id
     * @param cellInfo The position on screen where to create the widget.
     */
    public void completeAddAppWidget(final int appWidgetId, long container, int screen,
            AppWidgetHostView hostView, AppWidgetProviderInfo appWidgetInfo) {
    	//M:by chenxin
    	//Fixed issue when add system widget can't get getAddWidgetScreen
		if (((mWorkspace.getCurrentPage()+ 1) ==  mWorkspace.getChildCount()) || (mWorkspace.getCurrentPage() == 0)){
			Toast.makeText(this,
					getString(R.string.screen_not_allow_add),Toast.LENGTH_SHORT).show();
			return;
		}
		
    	if (mIsAddSystemWidget){
    		ScreenEditUtil.setAddWidgetScreen(screen);
    		mIsAddSystemWidget = false;
    	}
    	
        if (appWidgetInfo == null) {
            appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        }
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "completeAddAppWidget: appWidgetId = " + appWidgetId
                    + ", container = " + container + ", screen = " + screen);
        }

        //Modified by chenxin
        //Fixed under edit screen,remove the delete icon, in order to add 4*4 widget
        //move one screen to other screen quckily,avoid add widget to screen which is not corrent problem
        int addwidgetScreen = ScreenEditUtil.getAddWidgetScreen();
       
        
        
        //M: by chenxin
        
        if (addwidgetScreen != ScreenEditUtil.ADD_WIDGET_INVALID_SCREEN){
        	screen = addwidgetScreen;
        	// M: removed by ChenXin
        	// R: multi-click widget causing error screen index
        	//ScreenEditUtil.setAddWidgetScreen(ScreenEditUtil.ADD_WIDGET_INVALID_SCREEN);
        	// M: End
        }
        
        // Calculate the grid spans needed to fit this widget
        CellLayout layout = getCellLayout(container, screen);
        
        /// M: If screen is -1, layout will be null, replaced with currentDropLayout.
        if (layout == null) {
            layout = mWorkspace.getCurrentDropLayout();
        }

        int[] minSpanXY = getMinSpanForWidget(this, appWidgetInfo);
        int[] spanXY = getSpanForWidget(this, appWidgetInfo);
        
        //Modified by chenxin
        //Fixed issue when widget size > 4*4
        
        //added by shanlijuan
        int countX = LauncherModel.getCellCountX();
        int countY = LauncherModel.getCellCountY();
        if (spanXY[0]>countX){
        	spanXY[0] = countX;
        }
        if (spanXY[1] > countY){
        	spanXY[1] = countY;
        }
        
        if (minSpanXY[0] > countX){
        	minSpanXY[0] = countX;
        }
        if (minSpanXY[1] > countY){
        	minSpanXY[1] = countY;
        }
        //end
        
        // Try finding open space on Launcher screen
        // We have saved the position to which the widget was dragged-- this really only matters
        // if we are placing widgets on a "spring-loaded" screen
        int[] cellXY = mTmpAddItemCellCoordinates;
        mPendingAddInfo.dropPos = null;
        int[] touchXY = mPendingAddInfo.dropPos;
        int[] finalSpan = new int[2];
        
        boolean foundCellSpan = false;
        
        //M: by chenxin  when fitst add widget not contain config info 
        // then add widget contain config info, cause  foundCellSpan == ture, need reset mPendingAddInfo.cellX value
        mPendingAddInfo.cellX = -1;
        
        if (mPendingAddInfo.cellX >= 0 && mPendingAddInfo.cellY >= 0) {
            cellXY[0] = mPendingAddInfo.cellX;
            cellXY[1] = mPendingAddInfo.cellY;
            spanXY[0] = mPendingAddInfo.spanX;
            spanXY[1] = mPendingAddInfo.spanY;
            foundCellSpan = true;
        } else if (touchXY != null) {
            // when dragging and dropping, just find the closest free spot
            int[] result = layout.findNearestVacantArea(
                    touchXY[0], touchXY[1], minSpanXY[0], minSpanXY[1], spanXY[0],
                    spanXY[1], cellXY, finalSpan);
            spanXY[0] = finalSpan[0];
            spanXY[1] = finalSpan[1];
			//Modified by chenxin
            //foundCellSpan = (result != null);
            foundCellSpan = false;
        } else {
            
            LauncherAppWidgetInfo launcherInfo = new LauncherAppWidgetInfo(appWidgetId,
                    appWidgetInfo.provider);
            launcherInfo.spanX = spanXY[0];
            launcherInfo.spanY = spanXY[1];
            launcherInfo.minSpanX = mPendingAddInfo.minSpanX;
            launcherInfo.minSpanY = mPendingAddInfo.minSpanY;
            launcherInfo.container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
            
            mModel.addAppWidget(this, launcherInfo, screen);
            return;
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                LauncherLog.d(TAG, "ACTION_SCREEN_OFF: mPendingAddInfo = " + mPendingAddInfo
                        + ", mAppsCustomizeTabHost = " + mAppsCustomizeTabHost + ", this = " + this);
                // M: Added by liudekuan
                mIsScreenOff = true;
                // M: End
                
                mUserPresent = false;
                mDragLayer.clearAllResizeFrames();
                updateRunning();

                // Reset AllApps to its initial state only if we are not in the middle of
                // processing a multi-step drop
                if (mAppsCustomizeTabHost != null && mPendingAddInfo.container == ItemInfo.NO_ID) {
                    mAppsCustomizeTabHost.reset();
                    showWorkspace(false);
                }
            } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
                mUserPresent = true;
                updateRunning();
            } 
            // M: Added by liudekuan
            else if (Intent.ACTION_SCREEN_ON.equals(action)) {
            	mIsScreenOff = false;
            }
            else if (LauncherModel.ACTION_ADD_SCREEN.equals(action)) {
            	int index = ((Bundle)intent.getExtras()).getInt(LauncherSettings.Favorites.SCREEN);
            	mWorkspace.addScreen(index);
            	CellLayout layout = (CellLayout)mWorkspace.getChildAt(index);
            	if (mWorkspace.getScreenEditState()) {
            		layout.setTranslationX(0);
            		layout.setTranslationY(-ScreenEditUtil.CELLLAYOUT_Y_TRANSLATION);
            		layout.setScaleX(0.85f);
            		layout.setScaleY(0.85f);
            		layout.setBackgroundAlpha(1.0f);
            		layout.setBackgroundAlphaMultiplier(1.0f);
            		layout.setAlpha(1.0f);
            	}
            	//mWorkspace.setCurrentPage(index);
        		mWorkspace.mForceScreenScrolled = true;
            }
            // M: End
        }
    };

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "onAttachedToWindow.");
        }

        // Listen for broadcasts related to user-presence
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        // M: Added by liudekuan
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(LauncherModel.ACTION_ADD_SCREEN);
        // M: End
        registerReceiver(mReceiver, filter);

        mAttached = true;
        mVisible = true;
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "onDetachedFromWindow.");
        }

        mVisible = false;

        if (mAttached) {
            unregisterReceiver(mReceiver);
            mAttached = false;
        }
        updateRunning();
    }

    public void onWindowVisibilityChanged(int visibility) {
        mVisible = visibility == View.VISIBLE;
        updateRunning();
        // The following code used to be in onResume, but it turns out onResume is called when
        // you're in All Apps and click home to go to the workspace. onWindowVisibilityChanged
        // is a more appropriate event to handle
        if (mVisible) {
            mAppsCustomizeTabHost.onWindowVisible();
            if (!mWorkspaceLoading) {
                final ViewTreeObserver observer = mWorkspace.getViewTreeObserver();
                // We want to let Launcher draw itself at least once before we force it to build
                // layers on all the workspace pages, so that transitioning to Launcher from other
                // apps is nice and speedy. Usually the first call to preDraw doesn't correspond to
                // a true draw so we wait until the second preDraw call to be safe
                observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    public boolean onPreDraw() {
                        // We delay the layer building a bit in order to give
                        // other message processing a time to run.  In particular
                        // this avoids a delay in hiding the IME if it was
                        // currently shown, because doing that may involve
                        // some communication back with the app.
                        mWorkspace.postDelayed(mBuildLayersRunnable, 500);

                        observer.removeOnPreDrawListener(this);
                        return true;
                    }
                });
            }
            // When Launcher comes back to foreground, a different Activity might be responsible for
            // the app market intent, so refresh the icon
            updateAppMarketIcon();
            clearTypedText();
        }
    }

    private void sendAdvanceMessage(long delay) {
        mHandler.removeMessages(ADVANCE_MSG);
        Message msg = mHandler.obtainMessage(ADVANCE_MSG);
        mHandler.sendMessageDelayed(msg, delay);
        mAutoAdvanceSentTime = System.currentTimeMillis();
    }

    private void updateRunning() {
        boolean autoAdvanceRunning = mVisible && mUserPresent && !mWidgetsToAdvance.isEmpty();
        if (autoAdvanceRunning != mAutoAdvanceRunning) {
            mAutoAdvanceRunning = autoAdvanceRunning;
            if (autoAdvanceRunning) {
                long delay = mAutoAdvanceTimeLeft == -1 ? mAdvanceInterval : mAutoAdvanceTimeLeft;
                sendAdvanceMessage(delay);
            } else {
                if (!mWidgetsToAdvance.isEmpty()) {
                    mAutoAdvanceTimeLeft = Math.max(0, mAdvanceInterval -
                            (System.currentTimeMillis() - mAutoAdvanceSentTime));
                }
                mHandler.removeMessages(ADVANCE_MSG);
                mHandler.removeMessages(0); // Remove messages sent using postDelayed()
            }
        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == ADVANCE_MSG) {
                int i = 0;
                for (View key: mWidgetsToAdvance.keySet()) {
                    final View v = key.findViewById(mWidgetsToAdvance.get(key).autoAdvanceViewId);
                    final int delay = mAdvanceStagger * i;
                    if (v instanceof Advanceable) {
                       postDelayed(new Runnable() {
                           public void run() {
                               ((Advanceable) v).advance();
                           }
                       }, delay);
                    }
                    i++;
                }
                sendAdvanceMessage(mAdvanceInterval);
            }
        }
    };

    void addWidgetToAutoAdvanceIfNeeded(View hostView, AppWidgetProviderInfo appWidgetInfo) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "addWidgetToAutoAdvanceIfNeeded hostView = " + hostView + ", appWidgetInfo = "
                    + appWidgetInfo);
        }

        if (appWidgetInfo == null || appWidgetInfo.autoAdvanceViewId == -1) {
            return;
        }
        View v = hostView.findViewById(appWidgetInfo.autoAdvanceViewId);
        if (v instanceof Advanceable) {
            mWidgetsToAdvance.put(hostView, appWidgetInfo);
            ((Advanceable) v).fyiWillBeAdvancedByHostKThx();
            updateRunning();
        }
    }

    void removeWidgetToAutoAdvance(View hostView) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "removeWidgetToAutoAdvance hostView = " + hostView);
        }

        if (mWidgetsToAdvance.containsKey(hostView)) {
            mWidgetsToAdvance.remove(hostView);
            updateRunning();
        }
    }

    public void removeAppWidget(LauncherAppWidgetInfo launcherInfo) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "removeAppWidget launcherInfo = " + launcherInfo);
        }
        //modified by ETON guolinan
        ShortcutAndWidgetContainer saw =(ShortcutAndWidgetContainer)launcherInfo.hostView.getParent(); 
        saw.removeView(launcherInfo.hostView);
        ((CellLayout)saw.getParent()).addButtonDrawable(
        		R.drawable.homescreen_delete_screen_selector, mWorkspace.DELETE_BUTTON_ID, this, this);
        //end
        removeWidgetToAutoAdvance(launcherInfo.hostView);
        launcherInfo.hostView = null;
    }

    void showOutOfSpaceMessage(boolean isHotseatLayout) {
        int strId = (isHotseatLayout ? R.string.hotseat_out_of_space : R.string.out_of_space);
        Toast.makeText(this, getString(strId), Toast.LENGTH_SHORT).show();
    }

    /**
     * M: Pop up message allows to you add only one IMtkWidget for the given AppWidgetInfo.
     *
     * @param info The information of the IMtkWidget.
     */
    void showOnlyOneWidgetMessage(PendingAddWidgetInfo info) {
        try {
            PackageManager pm = getPackageManager();
            String label = pm.getApplicationLabel(pm.getApplicationInfo(info.componentName.getPackageName(), 0)).toString();
            Toast.makeText(this, getString(R.string.one_video_widget, label), Toast.LENGTH_SHORT).show();
        } catch (PackageManager.NameNotFoundException e) {
            LauncherLog.e(TAG, "Got NameNotFounceException when showOnlyOneWidgetMessage.", e);
        }
        // Exit spring loaded mode if necessary after adding the widget.
        exitSpringLoadedDragModeDelayed(false, false, null);
    }

    public LauncherAppWidgetHost getAppWidgetHost() {
        return mAppWidgetHost;
    }

    public LauncherModel getModel() {
        return mModel;
    }

    void closeSystemDialogs() {
        getWindow().closeAllPanels();

        // Whatever we were doing is hereby canceled.
        mWaitingForResult = false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "onNewIntent: intent = " + intent);
        }

        // Close the menu
        if (Intent.ACTION_MAIN.equals(intent.getAction())) {
            // also will cancel mWaitingForResult.
            closeSystemDialogs();

            final boolean alreadyOnHome =
                    ((intent.getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
                        != Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

            Runnable processIntent = new Runnable() {
                public void run() {
                    if (mWorkspace == null) {
                        // Can be cases where mWorkspace is null, this prevents a NPE
                        return;
                    }
                    Folder openFolder = mWorkspace.getOpenFolder();
                    // In all these cases, only animate if we're already on home
                    mWorkspace.exitWidgetResizeMode();
                    if (alreadyOnHome && mState == State.WORKSPACE && !mWorkspace.isTouchActive() &&
                            openFolder == null) {
                        /// M: Call the appropriate callback for the IMtkWidget on the current page
                        /// when press "Home" key move to default screen.
                        /*mWorkspace.moveOutAppWidget(mWorkspace.getCurrentPage());*/
                    	
                    	//M: added by chenxin
                    	//Under in screen edit mode,Press Home is need to normal mode
                    	if (mWorkspace.getScreenEditState()){
                    		if (!isHasDefaultStartInfo()){
                            	return;
                            }
                    		if (null!= mPopWindow && mPopWindow.isShowing()){
                    			mPopWindow.dismiss();
                    		}
                    		startBackMenu();
                    	}else{
                    		if (null!= mPopWindow && mPopWindow.isShowing()){
                    			mPopWindow.dismiss();
                    		}
                    		
                    		mWorkspace.moveToDefaultScreen(true);
                    	}
                    }

                    closeFolder();
                    //deleted by ETON guolinan
//                    exitSpringLoadedDragMode();
                    //end

                    // If we are already on home, then just animate back to the workspace,
                    // otherwise, just wait until onResume to set the state back to Workspace
                    if (alreadyOnHome) {
                        showWorkspace(true);
                    } else {
                        mOnResumeState = State.WORKSPACE;
                    }

                    final View v = getWindow().peekDecorView();
                    if (v != null && v.getWindowToken() != null) {
                        InputMethodManager imm = (InputMethodManager)getSystemService(
                                INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }

                    // Reset AllApps to its initial state
                    if (!alreadyOnHome && mAppsCustomizeTabHost != null) {
                        mAppsCustomizeTabHost.reset();
                    }
                }
            };

            if (alreadyOnHome && !mWorkspace.hasWindowFocus()) {
                // Delay processing of the intent to allow the status bar animation to finish
                // first in order to avoid janky animations.
                mWorkspace.postDelayed(processIntent, 350);
            } else {
                // Process the intent immediately.
                processIntent.run();
            }

        }
    }

    @Override
    public void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "onRestoreInstanceState: state = " + state
                    + ", mSavedInstanceState = " + mSavedInstanceState);
        }
        
        for (int page: mSynchronouslyBoundPages) {
            mWorkspace.restoreInstanceStateForChild(page);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(RUNTIME_STATE_CURRENT_SCREEN, mWorkspace.getNextPage());
        super.onSaveInstanceState(outState);

        outState.putInt(RUNTIME_STATE, mState.ordinal());
        // We close any open folder since it will not be re-opened, and we need to make sure
        // this state is reflected.
        closeFolder();

        if (mPendingAddInfo.container != ItemInfo.NO_ID && mPendingAddInfo.screen > -1 &&
                mWaitingForResult) {
            outState.putLong(RUNTIME_STATE_PENDING_ADD_CONTAINER, mPendingAddInfo.container);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_SCREEN, mPendingAddInfo.screen);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_CELL_X, mPendingAddInfo.cellX);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_CELL_Y, mPendingAddInfo.cellY);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_SPAN_X, mPendingAddInfo.spanX);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_SPAN_Y, mPendingAddInfo.spanY);
            outState.putParcelable(RUNTIME_STATE_PENDING_ADD_WIDGET_INFO, mPendingAddWidgetInfo);
        }

        if (mFolderInfo != null && mWaitingForResult) {
            outState.putBoolean(RUNTIME_STATE_PENDING_FOLDER_RENAME, true);
            outState.putLong(RUNTIME_STATE_PENDING_FOLDER_RENAME_ID, mFolderInfo.id);
        }

        // Save the current AppsCustomize tab
        if (mAppsCustomizeTabHost != null) {
            String currentTabTag = mAppsCustomizeTabHost.getCurrentTabTag();
            if (currentTabTag != null) {
                outState.putString("apps_customize_currentTab", currentTabTag);
            }
            int currentIndex = mAppsCustomizeContent.getSaveInstanceStateIndex();
            outState.putInt("apps_customize_currentIndex", currentIndex);
        }
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, " onSaveInstanceState: outState = " + outState);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "(Launcher)onDestroy: this = " + this);
        }

        // Remove all pending runnables
        mHandler.removeMessages(ADVANCE_MSG);
        mHandler.removeMessages(0);
        mWorkspace.removeCallbacks(mBuildLayersRunnable);

        // Stop callbacks from LauncherModel
        LauncherApplication app = ((LauncherApplication) getApplication());
        mModel.stopLoader();
        app.setLauncher(null);

        try {
            mAppWidgetHost.stopListening();
        } catch (NullPointerException ex) {
            Log.w(TAG, "problem while stopping AppWidgetHost during Launcher destruction", ex);
        }
        mAppWidgetHost = null;

        mWidgetsToAdvance.clear();

        TextKeyListener.getInstance().release();

        // Disconnect any of the callbacks and drawables associated with ItemInfos on the workspace
        // to prevent leaking Launcher activities on orientation change.
        if (mModel != null) {
            mModel.unbindItemInfosAndClearQueuedBindRunnables();
        }

        getContentResolver().unregisterContentObserver(mWidgetObserver);
        unregisterReceiver(mCloseSystemDialogsReceiver);
        
        //>>add by eton wanghenan
        if (null != mSectorReceive) {
        	 unregisterReceiver(mSectorReceive);
		}
       if (null != mProgressDialogReceive) {
    	   unregisterReceiver(mProgressDialogReceive);
        }
        if(mWallReceiver != null)
        {
            unregisterReceiver(mWallReceiver);
        }
       
        mWallReceiver = null;
        //<<end
       
       //>>added by tianlei
       if (null != checkBroadcastReceiver) {
    	   unregisterReceiver(checkBroadcastReceiver);
       }
       //<<added end

        mDragLayer.clearAllResizeFrames();
        ((ViewGroup) mWorkspace.getParent()).removeAllViews();
        mWorkspace.removeAllViews();
        mWorkspace = null;
        mDragController = null;

        LauncherAnimUtils.onDestroyActivity();
        
        /// M: Disable orientation listener when launcher is destroyed.
        disableOrientationListener();
    }

    public DragController getDragController() {
        return mDragController;
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        if (requestCode >= 0) {
            mWaitingForResult = true;
        }
        super.startActivityForResult(intent, requestCode);
    }

    /**
     * Indicates that we want global search for this activity by setting the globalSearch
     * argument for {@link #startSearch} to true.
     */
    //deleted by ETON guolinan
   /* @Override
    public void startSearch(String initialQuery, boolean selectInitialQuery,
            Bundle appSearchData, boolean globalSearch) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "startSearch.");
        }
        showWorkspace(true);

        if (initialQuery == null) {
            // Use any text typed in the launcher as the initial query
            initialQuery = getTypedText();
        }
        if (appSearchData == null) {
            appSearchData = new Bundle();
            appSearchData.putString(Search.SOURCE, "launcher-search");
        }
        Rect sourceBounds = new Rect();
        if (mSearchDropTargetBar != null) {
            sourceBounds = mSearchDropTargetBar.getSearchBarBounds();
        }

        startGlobalSearch(initialQuery, selectInitialQuery,
            appSearchData, sourceBounds);
    }
*/
    //end
    /**
     * Starts the global search activity. This code is a copied from SearchManager
     */
    public void startGlobalSearch(String initialQuery,
            boolean selectInitialQuery, Bundle appSearchData, Rect sourceBounds) {
        final SearchManager searchManager =
            (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        ComponentName globalSearchActivity = searchManager.getGlobalSearchActivity();
        if (globalSearchActivity == null) {
            Log.w(TAG, "No global search activity found.");
            return;
        }
        Intent intent = new Intent(SearchManager.INTENT_ACTION_GLOBAL_SEARCH);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(globalSearchActivity);
        // Make sure that we have a Bundle to put source in
        if (appSearchData == null) {
            appSearchData = new Bundle();
        } else {
            appSearchData = new Bundle(appSearchData);
        }
        // Set source to package name of app that starts global search, if not set already.
        if (!appSearchData.containsKey("source")) {
            appSearchData.putString("source", getPackageName());
        }
        intent.putExtra(SearchManager.APP_DATA, appSearchData);
        if (!TextUtils.isEmpty(initialQuery)) {
            intent.putExtra(SearchManager.QUERY, initialQuery);
        }
        if (selectInitialQuery) {
            intent.putExtra(SearchManager.EXTRA_SELECT_QUERY, selectInitialQuery);
        }
        intent.setSourceBounds(sourceBounds);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Log.e(TAG, "Global search activity not found: " + globalSearchActivity);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	
    	if (mIsShowCustommenu){
    		menu.add("menu");
    		return super.onCreateOptionsMenu(menu);
    	}

        if (isWorkspaceLocked()) {
            return false;
        }
        
      
        
        super.onCreateOptionsMenu(menu);

        Intent manageApps = new Intent(android.provider.Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS);
        manageApps.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        Intent settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
        settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        String helpUrl = getString(R.string.help_url);
        Intent help = new Intent(Intent.ACTION_VIEW, Uri.parse(helpUrl));
        help.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
      
        //menu.add(0, MENU_THEME_SETTINGS, 0, R.string.menu_theme);
        
        menu.add(MENU_GROUP_WALLPAPER, MENU_WALLPAPER_SETTINGS, 0, R.string.menu_wallpaper)
            .setIcon(R.drawable.ic_menu_gallery)
            .setAlphabeticShortcut('W');
        
        menu.add(0, MENU_EDIT_EFFECT, 0, R.string.menu_effect_setting)
    	.setIcon(R.drawable.ic_menu_effect);
        //add by eton lisidong
        
        menu.add(MENU_GROUP_EDIT_WORKSPACE, MENU_EDIT, 0, R.string.menu_edit)
        	.setIcon(R.drawable.ic_menu_edit)
        	.setAlphabeticShortcut('E');
        
        //>>add by eton wanghenan for Test
 //       menu.add(0, MENU_TEST, 0, R.string.menu_test);
        //<<end
        //modified by chenxin
        
        
	    
        //end
        
        /*menu.add(0, MENU_MANAGE_APPS, 0, R.string.menu_manage_apps)
            .setIcon(R.drawable.ic_menu_app_manager)
            .setIntent(manageApps)
            .setAlphabeticShortcut('M');*/
        menu.add(0, MENU_SYSTEM_SETTINGS, 0, R.string.menu_settings)
            .setIcon(R.drawable.ic_menu_system_setting)
            .setIntent(settings)
            .setAlphabeticShortcut('P');
        if (!helpUrl.isEmpty()) {
            menu.add(0, MENU_HELP, 0, R.string.menu_help)
                .setIcon(android.R.drawable.ic_menu_help)
                .setIntent(help)
                .setAlphabeticShortcut('H');
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (mAppsCustomizeTabHost.isTransitioning()) {
            return false;
        }
        boolean allAppsVisible = (mAppsCustomizeTabHost.getVisibility() == View.VISIBLE);
        menu.setGroupVisible(MENU_GROUP_WALLPAPER, !allAppsVisible);
        return true;
    }
    
    /// M: Added by liudekuan
    private void closeTipsArchive () {
    	if (mDragLayer != null) {
    		int count = mDragLayer.getChildCount();
    		for (int i = 0; i < count; i ++) {
    			View v = mDragLayer.getChildAt(i);
    			if (v instanceof TipsArchive) {
    				mDragLayer.removeView(v);
    			}
    		}
    	}
    }




	/*public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("menu");
		return super.onCreateOptionsMenu(menu);
	}*/
	
	public boolean onMenuOpened(int featureId, Menu menu) {
		if (mPopWindow != null) {
			if (mPopWindow.isShowing()){
				mPopWindow.dismiss();
			}
			else {
				mPopWindow.showAtLocation(findViewById(R.id.workspace),
						Gravity.LEFT|Gravity.BOTTOM, 0, 0);
			}
		}
		
		// Return true is show system menu
		if (mIsShowCustommenu){
			return false;
		}else {
			return true;
		}
		
	}

	private void setListMenuParams(){
		CustomMenu cm = new CustomMenu(this);
		mPopWindow = cm.getMenu(null,  keyListener,this);

	}
    
	
	private OnKeyListener keyListener = new OnKeyListener() {

		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if ((event.getAction() == KeyEvent.ACTION_DOWN
					&& keyCode == KeyEvent.KEYCODE_MENU) ||
					(event.getAction() == KeyEvent.ACTION_DOWN
					&& keyCode == KeyEvent.KEYCODE_BACK)) {
				
				
				if (mPopWindow != null && mPopWindow.isShowing()){
					
					mPopWindow.dismiss();
				}
				return true;
			}
			return false;
		}
	};
	
	
	/**
	 * Init display mode choosing dialog and show it
	 * @author liukaibang
	 */
	private void changeDisplayMode () {
		final LauncherApplication app = ((LauncherApplication)getApplication());
		final int currentMode = DisplayFactory.getCurrentMode(app);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
		
		// These two resources are defined in valuse-zh-rCN temporary.
		builder.setTitle(R.string.displaymode_dialog_title);
		builder.setSingleChoiceItems(R.array.display_mode_list, currentMode,
			new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				int newMode = currentMode;
				switch (arg1) {
                case 0:
                	newMode = DisplayFactory.MODE_4_4;
                    break;
                case 1:
                	newMode = DisplayFactory.MODE_3_3;
                    break;
                case 2:
                	// newMode = DisplayFactory.MODE_4_3;
                    break;
                case 3:
                	// newMode = DisplayFactory.MODE_5_4;
                    break;
                default: 
                	break;
                }
				
				// Swithing to new mode and saving the new mode 
				// to preferences when a different mode is choosed.
				if (newMode != currentMode) {
					SharedPreferencesUtils.setDisplayMode(app, newMode);
					app.restart();
				}
			}
		});
		
		builder.show();
	}

	
	public void handleCustomMenuOptions(int choice) {
		int choiceId = choice;
		switch (choiceId) {
		case CUSTOM_MENU_WALLPAPER:
			mModel.enterScreenEditMode(this, CUSTOM_MENU_WALLPAPER);
			break;
		case CUSTOM_MENU_EFFECT:
			mModel.enterScreenEditMode(this, CUSTOM_MENU_EFFECT);
			break;
		case CUSTOM_MENU_EDIT:
			mModel.enterScreenEditMode(this, CUSTOM_MENU_EDIT);
			break;
		case CUSTOM_MENU_SETTINGS:
			Intent settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
	        settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
	                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
	        startActivity(settings);
			break;
		case MENU_TEST:
			/// Modified by liukaibang begin {{
			// Old Code
			// This will be handled by dialog list item(ex: 4x4, 3x3 ect).
			/*
			LauncherApplication app = ((LauncherApplication)getApplication());
			int mode = LauncherApplication.getCurrentDisplayMode(app);
			if (mode == DisplayFactory.MODE_3_3) {
				SharedPreferencesUtils.setDisplayMode(app, DisplayFactory.MODE_4_4);
			} else {
				SharedPreferencesUtils.setDisplayMode(app, DisplayFactory.MODE_3_3);
			}
			app.restart(); 
			*/
			
			
			// New Code
			changeDisplayMode();
			// end }}
			
			break;
		default:
			break;

		}
	}
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	/// M: Added by liudekuan
    	/// R: Cannot tidy apps when in edit mode.
    	closeTipsArchive();
    	/// M: End
        switch (item.getItemId()) {
        case MENU_THEME_SETTINGS:
        	mModel.enterScreenEditMode(this, MENU_THEME_SETTINGS);
        	break;
        case MENU_WALLPAPER_SETTINGS:
        	mModel.enterScreenEditMode(this, CUSTOM_MENU_WALLPAPER);
            return true;
        case MENU_EDIT:
            mModel.enterScreenEditMode(this, CUSTOM_MENU_EDIT);
        	break;
        case MENU_EDIT_EFFECT:
        	mModel.enterScreenEditMode(this, CUSTOM_MENU_EFFECT);
        	break;
        //>>add by eton wanghenan for Test
//        case MENU_TEST:
//        	this.sendBroadcast(new Intent(SectorView.ACTION_SECTORVIEW_SHOW));
//            break;
        //<<end            
        }

        return super.onOptionsItemSelected(item);
    }

    public void startSystemWidget(){
    	Intent intent = new Intent();
    	intent.setClassName(this, "com.eton.launcher.setting.WidgetAlertActivity");
    	startActivityForResult(intent , REQUEST_BIND_APPWIDGET) ;
    }
    
    @Override
    public boolean onSearchRequested() {
        startSearch(null, false, null, true);
        // Use a custom animation for launching search
        return true;
    }

    public boolean isWorkspaceLocked() {
        return mWorkspaceLoading || mWaitingForResult;
    }

    private void resetAddInfo() {
        mPendingAddInfo.container = ItemInfo.NO_ID;
        mPendingAddInfo.screen = -1;
        mPendingAddInfo.cellX = mPendingAddInfo.cellY = -1;
        mPendingAddInfo.spanX = mPendingAddInfo.spanY = -1;
        mPendingAddInfo.minSpanX = mPendingAddInfo.minSpanY = -1;
        mPendingAddInfo.dropPos = null;
    }

    void addAppWidgetImpl(final int appWidgetId, ItemInfo info, AppWidgetHostView boundWidget,
            AppWidgetProviderInfo appWidgetInfo) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "addAppWidgetImpl: appWidgetId = " + appWidgetId
                    + ", info = " + info + ", boundWidget = " + boundWidget 
                    + ", appWidgetInfo = " + appWidgetInfo);
        }
        //added by chenxin
    	appWidgetInfo =  mAppWidgetManager.getAppWidgetInfo(appWidgetId);
    	//added end

        if (appWidgetInfo.configure != null) {
            mPendingAddWidgetInfo = appWidgetInfo;

            // Launch over to configure widget, if needed
            Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
            intent.setComponent(appWidgetInfo.configure);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            startActivityForResultSafely(intent, REQUEST_CREATE_APPWIDGET);
        } else {
            // Otherwise just add it
        	//Modified by chenxin
        	//Fixed add widget in desktop
        	info.container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
            completeAddAppWidget(appWidgetId, info.container, getCurrentWorkspaceScreen(), boundWidget,
                    appWidgetInfo);
            // Exit spring loaded mode if necessary after adding the widget
            exitSpringLoadedDragModeDelayed(true, false, null);
        }
    }

    /**
     * Process a shortcut drop.
     *
     * @param componentName The name of the component
     * @param screen The screen where it should be added
     * @param cell The cell it should be added to, optional
     * @param position The location on the screen where it was dropped, optional
     */
    void processShortcutFromDrop(ComponentName componentName, long container, int screen,
            int[] cell, int[] loc) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "processShortcutFromDrop componentName = " + componentName + ", container = " + container
                    + ", screen = " + screen);
        }

        resetAddInfo();
        mPendingAddInfo.container = container;
        mPendingAddInfo.screen = screen;
        mPendingAddInfo.dropPos = loc;

        if (cell != null) {
            mPendingAddInfo.cellX = cell[0];
            mPendingAddInfo.cellY = cell[1];
        }

        Intent createShortcutIntent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
        createShortcutIntent.setComponent(componentName);
        processShortcut(createShortcutIntent);
    }

    /**
     * Process a widget drop.
     *
     * @param info The PendingAppWidgetInfo of the widget being added.
     * @param screen The screen where it should be added
     * @param cell The cell it should be added to, optional
     * @param position The location on the screen where it was dropped, optional
     */
    void addAppWidgetFromDrop(PendingAddWidgetInfo info, long container, int screen,
            int[] cell, int[] span, int[] loc) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "addAppWidgetFromDrop: info = " + info + ", container = " + container + ", screen = "
                    + screen);
        }

        resetAddInfo();
        mPendingAddInfo.container = info.container = container;
        mPendingAddInfo.screen = info.screen = screen;
        mPendingAddInfo.dropPos = loc;
        mPendingAddInfo.minSpanX = info.minSpanX;
        mPendingAddInfo.minSpanY = info.minSpanY;

        if (cell != null) {
            mPendingAddInfo.cellX = cell[0];
            mPendingAddInfo.cellY = cell[1];
        }
        if (span != null) {
            mPendingAddInfo.spanX = span[0];
            mPendingAddInfo.spanY = span[1];
        }

        AppWidgetHostView hostView = info.boundWidget;
        int appWidgetId;
        if (hostView != null) {
            appWidgetId = hostView.getAppWidgetId();
            addAppWidgetImpl(appWidgetId, info, hostView, info.info);
        } else {
            // In this case, we either need to start an activity to get permission to bind
            // the widget, or we need to start an activity to configure the widget, or both.
            appWidgetId = getAppWidgetHost().allocateAppWidgetId();
            Bundle options = info.bindOptions;

            boolean success = false;
            if (options != null) {
                success = mAppWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId,
                        info.componentName, options);
            } else {
                success = mAppWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId,
                        info.componentName);
            }
            if (success) {
                addAppWidgetImpl(appWidgetId, info, null, info.info);
            } else {
                mPendingAddWidgetInfo = info.info;
                Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_BIND);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, info.componentName);
                // TODO: we need to make sure that this accounts for the options bundle.
                // intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_OPTIONS, options);
                startActivityForResult(intent, REQUEST_BIND_APPWIDGET);
            }
        }
    }

    void processShortcut(Intent intent) {
        // Handle case where user selected "Applications"
        String applicationName = getResources().getString(R.string.group_applications);
        String shortcutName = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);

        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "processShortcut: applicationName = " + applicationName
                    + ", shortcutName = " + shortcutName + ", intent = " + intent);
        }

        if (applicationName != null && applicationName.equals(shortcutName)) {
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
            pickIntent.putExtra(Intent.EXTRA_INTENT, mainIntent);
            pickIntent.putExtra(Intent.EXTRA_TITLE, getText(R.string.title_select_application));
            startActivityForResultSafely(pickIntent, REQUEST_PICK_APPLICATION);
        } else {
            startActivityForResultSafely(intent, REQUEST_CREATE_SHORTCUT);
        }
    }

    void processWallpaper(Intent intent) {
        startActivityForResult(intent, REQUEST_PICK_WALLPAPER);
    }

    FolderIcon addFolder(CellLayout layout, long container, final int screen, int cellX,
            int cellY) {
        final FolderInfo folderInfo = new FolderInfo();
        folderInfo.title = getText(R.string.folder_name);

        // Update the model
        LauncherModel.addItemToDatabase(Launcher.this, folderInfo, container, screen, cellX, cellY,
                false);
        sFolders.put(folderInfo.id, folderInfo);

        // Create the view
        FolderIcon newFolder =
            FolderIcon.fromXml(R.layout.folder_icon, this, layout, folderInfo, mIconCache);
        mWorkspace.addInScreen(newFolder, container, screen, cellX, cellY, 1, 1,
                isWorkspaceLocked());
        return newFolder;
    }

    /**
     * added by LiuDekuan on 2013-07-18
     */
    FolderIcon addFolder(String folderTitle, CellLayout layout, long container, final int screen, int cellX,
            int cellY) {
        final FolderInfo folderInfo = new FolderInfo();
        folderInfo.title = folderTitle;

        // Update the model
        LauncherModel.addItemToDatabase(Launcher.this, folderInfo, container, screen, cellX, cellY,
                false);
        sFolders.put(folderInfo.id, folderInfo);

        // Create the view
        FolderIcon newFolder =
            FolderIcon.fromXml(R.layout.folder_icon, this, layout, folderInfo, mIconCache);
        mWorkspace.addInScreen(newFolder, container, screen, cellX, cellY, 1, 1,
                isWorkspaceLocked());
        return newFolder;
    }

    void removeFolder(FolderInfo folder) {
        sFolders.remove(folder.id);
    }
//deleted by ETON guolinan
  /*  private void startWallpaper() {
        showWorkspace(true);
        final Intent pickWallpaper = new Intent(Intent.ACTION_SET_WALLPAPER);
        Intent chooser = Intent.createChooser(pickWallpaper,
                getText(R.string.chooser_wallpaper));
        // NOTE: Adds a configure option to the chooser if the wallpaper supports it
        //       Removed in Eclair MR1
//        WallpaperManager wm = (WallpaperManager)
//                getSystemService(Context.WALLPAPER_SERVICE);
//        WallpaperInfo wi = wm.getWallpaperInfo();
//        if (wi != null && wi.getSettingsActivity() != null) {
//            LabeledIntent li = new LabeledIntent(getPackageName(),
//                    R.string.configure_wallpaper, 0);
//            li.setClassName(wi.getPackageName(), wi.getSettingsActivity());
//            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { li });
//        }
        startActivityForResult(chooser, REQUEST_PICK_WALLPAPER);
    }*/
//end
    /**
     * Registers various content observers. The current implementation registers
     * only a favorites observer to keep track of the favorites applications.
     */
    private void registerContentObservers() {
        ContentResolver resolver = getContentResolver();
        resolver.registerContentObserver(LauncherProvider.CONTENT_APPWIDGET_RESET_URI,
                true, mWidgetObserver);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (LauncherLog.DEBUG_KEY) {
            LauncherLog.d(TAG, "dispatchKeyEvent: keyEvent = " + event);
        }

        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_HOME:
                    return true;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    if (isPropertyEnabled(DUMP_STATE_PROPERTY)) {
                        dumpState();
                        return true;
                    }
                    break;
            }
        } else if (event.getAction() == KeyEvent.ACTION_UP) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_HOME:
                    return true;
            }
        }

        return super.dispatchKeyEvent(event);
    }
    
    /**
     * added by liudekuan
     * close AppsArrangeView if opend when back button is pressed. 
     */
    private void closeAavIfNecessary () {
    	
    	if (mDragLayer == null) {
    		return;
    	}
    	
    	for (int i = 0; i < mDragLayer.getChildCount(); i ++) {
    		View v = mDragLayer.getChildAt(i);
    		if (v instanceof AppsArrangeView) {
    			mDragLayer.removeView(v);
    		}
    	}
    }
    
    
    private void startBackMenu(){
    	if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "Back key pressed, mState = " + mState + ", mOnResumeState = " + mOnResumeState);
        }
        
        // added by liudekuan
        closeAavIfNecessary();
        // end
        
    	if (isAllAppsVisible()) {
    		showWorkspace(true);
        } else if (mWorkspace.getOpenFolder() != null) {
            Folder openFolder = mWorkspace.getOpenFolder();
            if (openFolder.isEditingName()) {
                openFolder.dismissEditingName();
            } else {
                closeFolder(); 
            }
        //added by ETON guolinan
        } else if(mWorkspace.getScreenEditState()){
        	/// M: Added by Liu-Dekuan
        	mModel.exitScreenEditMode(this);
        	/// M: End
        } else {
            mWorkspace.exitWidgetResizeMode();

            // Back button is a no-op here, but give at least some feedback for the button press
            mWorkspace.showOutlinesTemporarily();
        }
        /// M: Cancel long press widget to add message.
        cancelLongPressWidgetToAddMessage();
    }
    
    @Override
    public void onBackPressed() {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "Back key pressed, mState = " + mState + ", mOnResumeState = " + mOnResumeState);
        }
        
        // added by liudekuan
        closeAavIfNecessary();
        // end
        
        closeTipsArchive();
        
    	if (isAllAppsVisible()) {
    		showWorkspace(true);
        } else if (mWorkspace.getOpenFolder() != null) {
            Folder openFolder = mWorkspace.getOpenFolder();
            /// M: Added by liudekuan
            if (openFolder.isAnimating()) {
            	return;
            }
            /// M: End
            if (openFolder.isEditingName()) {
                openFolder.dismissEditingName();
            } else {
                closeFolder(); 
            }
        //added by ETON guolinan
        } else if(mWorkspace.getScreenEditState()){
        	/// M: Added by Liu-Dekuan
        	mModel.exitScreenEditMode(this);
        	/// M: End
        } else {
            mWorkspace.exitWidgetResizeMode();

            // Back button is a no-op here, but give at least some feedback for the button press
            mWorkspace.showOutlinesTemporarily();
        }
        /// M: Cancel long press widget to add message.
        cancelLongPressWidgetToAddMessage();
    }

    /**
     * Re-listen when widgets are reset.
     */
    private void onAppWidgetReset() {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "onAppWidgetReset.");
        }

        if (mAppWidgetHost != null) {
            mAppWidgetHost.startListening();
        }
    }

    /**
     * Launches the intent referred by the clicked shortcut.
     *
     * @param v The view representing the clicked shortcut.
     */
    public void onClick(View v) {
        // Make sure that rogue clicks don't get through while allapps is launching, or after the
        // view has detached (it's possible for this to happen if the view is removed mid touch).

        /// M: add systrace to analyze application launche time.
        
        long time = System.currentTimeMillis();
        
        if(!getWorkspace().getScreenEditState() && !mGaussOver && (time-mGaussBeginTime <CLICK_INTERVAL_TIME))
        {
            Log.i("Launcher_test", "Launcher-gonClick: (time = " +time+") - (mGaussBeginTime ="+mGaussBeginTime+")="+(time-mGaussBeginTime)+"  <1s");
            return;
        }
    
        if(!getWorkspace().getScreenEditState() && !mGaussOver)
        {
            mGaussOver = true;
        }
        


        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "Click on view " + v);
        }

        //added by ETON guolinan
        if (v.getId() == R.id.tips_button){
        	mDragLayer.removeView(mDragLayer.findViewById(R.id.tips_archive));
        	AppsArrangeView aav = AppsArrangeView.fromXml(this);
            if (aav.getParent() == null) {
                getDragLayer().addView(aav);
            }
            aav.buildLayer();
        }
        //end
        
        if (v.getWindowToken() == null) {
            LauncherLog.d(TAG, "Click on a view with no window token, directly return.");
            return;
        }

        if (!mWorkspace.isFinishedSwitchingState()) {
            LauncherLog.d(TAG, "The workspace is in switching state when clicking on view, directly return.");
            return;
        }
                
        Object tag = v.getTag();
		//Modified by chenxin
        if (tag instanceof ShortcutInfo /*&& mState != State.APPS_CUSTOMIZE_SPRING_LOADED*/) {
            // Open shortcut
            final Intent intent = ((ShortcutInfo) tag).intent;
            int[] pos = new int[2];
            v.getLocationOnScreen(pos);
            intent.setSourceBounds(new Rect(pos[0], pos[1],
                    pos[0] + v.getWidth(), pos[1] + v.getHeight()));

            boolean success = startActivitySafely(v, intent, tag);

            if (success && v instanceof BubbleTextView) {
                mWaitingForResume = (BubbleTextView) v;
                mWaitingForResume.setStayPressed(true);
            }
        } else if (tag instanceof FolderInfo) {
            if (v instanceof FolderIcon) {
                FolderIcon fi = (FolderIcon) v;
                handleFolderClick(fi);
            }
            //deleted by ETON guolinan
       /* } else if (v == mAllAppsButton) {
            if (isAllAppsVisible()) {
                showWorkspace(true);
            } else {
                onClickAllAppsButton(v);
            }*/
            //end
        } else if (v.getId() == Workspace.ADD_BUTTON_ID) {
            
            if (LauncherLog.DEBUG) {
                LauncherLog.d(TAG, "ADD_BUTTON_ID: isAppAdding = " + LauncherModel.isAppAdding());
            }
        	
        	if (LauncherModel.isAppAdding()) {
        	    if (LauncherLog.DEBUG) {
        	        LauncherLog.d(TAG, "Workspace.ADD_BUTTON_ID: isAppAdding=true");
        	    }
        		return;
        	}
        	
        	//set the max screens limist
        	//remove the limist
        	/*if (mWorkspace.getChildCount() > HOME_MAX_SCREENS){
    			Toast.makeText(this, getString(R.string.launcher_max_screens), Toast.LENGTH_SHORT).show();
    			return;
    		}*/
        	
        	if(mWorkspace.indexOfChild((CellLayout)v.getParent()) == mWorkspace.getChildCount() - 1){
		    	 mWorkspace.addScreenInEditMode(this,mWorkspace.getChildCount() - 1,false);
             }else if (mWorkspace.indexOfChild((CellLayout)v.getParent()) == 0){
            	 mWorkspace.addScreenInEditMode(this,ADD_SCREEN_LEFT_POSITON,false);
             }
        }
        else if (v.getId() == Workspace.DELETE_BUTTON_ID) {
            if (LauncherLog.DEBUG) {
                LauncherLog.d(TAG, "DELETE_BUTTON_ID: isAppAdding = " + LauncherModel.isAppAdding());
            }
            
            //M: by chenxin
            //Under edit secreen, device's screen contain delete button and current celllyout,click delete button not should to do 
            boolean isCurrentView = false;
            if ((v.getParent()).equals(mWorkspace.getPageAt(getCurrentPage()))){
            	isCurrentView = true;
            }
            //end
            
        	if (LauncherModel.isAppAdding() || !isCurrentView) {
        	    if (LauncherLog.DEBUG) {
                    LauncherLog.d(TAG, "Workspace.DELETE_BUTTON_ID: isAppAdding=true");
                }
        		return;
        	}
        	
        	if (isDeleteScreenCompleted()){
        		mIsCompleteDeteleScreen = false;
        		mModel.deleteScreen(this, getCurrentWorkspaceScreen());
        	}
        	
        }
    }
    
    //Added by chenxin 
    //get state about delte screen,void multiple delete operate cause screen disappear 
    private boolean isDeleteScreenCompleted(){
    	return mIsCompleteDeteleScreen;
    }
    
  
    //modified by ETON guolinan
    /*public boolean onTouch(View v, MotionEvent event) {
        // this is an intercepted event being forwarded from mWorkspace;
        // clicking anywhere on the workspace causes the customization drawer to slide down
    	showWorkspace(true);
        return false;
    }*/
    public boolean onTouch(final View v, final MotionEvent event) {
        // this is an intercepted event being forwarded from mWorkspace;
        // clicking anywhere on the workspace causes the customization drawer to slide down
        final Object tag = v.getTag();
    	if (tag instanceof ShortcutInfo || (tag instanceof FolderInfo && v instanceof FolderIcon)) {
            // Open shortcut
            if (event.getAction() == MotionEvent.ACTION_DOWN){
            	ValueAnimator animation = ValueAnimator.ofFloat(0f, 1f);
            	animation.setDuration(400);
            	animation.addUpdateListener(new AnimatorUpdateListener() {
            	    @Override
            	    public void onAnimationUpdate(ValueAnimator animation) {
            	        float animatedValue = (Float) animation.getAnimatedValue();
            	        v.setAlpha(1 - 0.3f * animatedValue);
            	        v.setScaleX(1 - 0.1f * animatedValue);
            	        v.setScaleY(1 - 0.1f * animatedValue);
            	    }
            	});
            	animation.setInterpolator(new Workspace.ZoomInInterpolator());
            	animation.start();
    	}else if(event.getAction() == MotionEvent.ACTION_UP ||event.getAction() == MotionEvent.ACTION_CANCEL){
    		final boolean isUp = (event.getAction() == MotionEvent.ACTION_UP);
    		ValueAnimator animation = ValueAnimator.ofFloat(0f, 1f);
        	animation.setDuration(400);
        	animation.addUpdateListener(new AnimatorUpdateListener() {
        	    @Override
        	    public void onAnimationUpdate(ValueAnimator animation) {
        	        float animatedValue = (Float) animation.getAnimatedValue();
        	        v.setAlpha(v.getAlpha() + (1 - v.getAlpha()) * animatedValue);
					v.setScaleX(v.getScaleX() + (1 - v.getScaleX()) * animatedValue);
					v.setScaleY(v.getScaleY() + (1 - v.getScaleY()) * animatedValue);
        	    }
        	});
        	animation.setInterpolator(new Workspace.ZoomInInterpolator());
        	animation.addListener(new AnimatorListener() {
				
				@Override
				public void onAnimationStart(Animator arg0) {
					// TODO �Զ���ɵķ������
					
				}
				
				@Override
				public void onAnimationRepeat(Animator arg0) {
					// TODO �Զ���ɵķ������
					
				}
				
				@Override
				public void onAnimationEnd(Animator arg0) {/*
					// TODO �Զ���ɵķ������
					if (isUp){ 
						if(tag instanceof ShortcutInfo){
					        final Intent intent = ((ShortcutInfo) tag).intent;
				            int[] pos = new int[2];
				            v.getLocationOnScreen(pos);
				            intent.setSourceBounds(new Rect(pos[0], pos[1],
				                    pos[0] + v.getWidth(), pos[1] + v.getHeight()));
							boolean success = startActivitySafely(v, intent, tag);
		
				            if (success && v instanceof BubbleTextView) {
				                mWaitingForResume = (BubbleTextView) v;
				                mWaitingForResume.setStayPressed(true);
				            }
						}else if((tag instanceof FolderInfo && v instanceof FolderIcon)){
							  FolderIcon fi = (FolderIcon) v;
				              handleFolderClick(fi);
						}
				}
				*/}
				@Override
				public void onAnimationCancel(Animator arg0) {
					// TODO �Զ���ɵķ������
					
				}
			});
        	animation.start();
    	 }
    	}
        return false;
    }

    /**
     * Event handler for the search button
     *
     * @param v The view that was clicked.
     */
    public void onClickSearchButton(View v) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "onClickSearchButton v = " + v);
        }

        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        onSearchRequested();
    }

    /**
     * Event handler for the voice button
     *
     * @param v The view that was clicked.
     */
    public void onClickVoiceButton(View v) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "onClickVoiceButton v = " + v);
        }

        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        try {
            final SearchManager searchManager =
                    (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            ComponentName activityName = searchManager.getGlobalSearchActivity();
            Intent intent = new Intent(RecognizerIntent.ACTION_WEB_SEARCH);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (activityName != null) {
                intent.setPackage(activityName.getPackageName());
            }
            startActivity(null, intent, "onClickVoiceButton");
            overridePendingTransition(R.anim.fade_in_fast, R.anim.fade_out_fast);
        } catch (ActivityNotFoundException e) {
            Intent intent = new Intent(RecognizerIntent.ACTION_WEB_SEARCH);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivitySafely(null, intent, "onClickVoiceButton");
        }
    }

    /**
     * Event handler for the "grid" button that appears on the home screen, which
     * enters all apps mode.
     *
     * @param v The view that was clicked.
     */
    public void onClickAllAppsButton(View v) {
        showAllApps(true);
    }

    public void onTouchDownAllAppsButton(View v) {
        // Provide the same haptic feedback that the system offers for virtual keys.
        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
    }

    public void onClickAppMarketButton(View v) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "onClickAppMarketButton v = " + v + ", mAppMarketIntent = " + mAppMarketIntent);
        }

        if (mAppMarketIntent != null) {
            startActivitySafely(v, mAppMarketIntent, "app market");
        } else {
            Log.e(TAG, "Invalid app market intent.");
        }
    }

    void startApplicationDetailsActivity(ComponentName componentName) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "startApplicationDetailsActivity: componentName = " + componentName);
        }

        String packageName = componentName.getPackageName();
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", packageName, null));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivitySafely(null, intent, "startApplicationDetailsActivity");
    }

    void startApplicationUninstallActivity(ApplicationInfo appInfo) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "startApplicationUninstallActivity: appInfo = " + appInfo);
        }

        if ((appInfo.flags & ApplicationInfo.DOWNLOADED_FLAG) == 0) {
            // System applications cannot be installed. For now, show a toast explaining that.
            // We may give them the option of disabling apps this way.
            int messageId = R.string.uninstall_system_app_text;
            Toast.makeText(this, messageId, Toast.LENGTH_SHORT).show();
        } else {
            String packageName = appInfo.componentName.getPackageName();
            String className = appInfo.componentName.getClassName();
            Intent intent = new Intent(
                    Intent.ACTION_DELETE, Uri.fromParts("package", packageName, className));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(intent);
        }
    }

    boolean startActivity(View v, Intent intent, Object tag) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "startActivity v = " + v + ", intent = " + intent + ", tag = " + tag);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            // Only launch using the new animation if the shortcut has not opted out (this is a
            // private contract between launcher and may be ignored in the future).
            boolean useLaunchAnimation = (v != null) &&
                    !intent.hasExtra(INTENT_EXTRA_IGNORE_LAUNCH_ANIMATION);

            /// M: add systrace to analyze application launche time.

            if (useLaunchAnimation) {
                ActivityOptions opts = ActivityOptions.makeScaleUpAnimation(v, 0, 0,
                        v.getMeasuredWidth(), v.getMeasuredHeight());

                startActivity(intent, opts.toBundle());
            } else {
                startActivity(intent);
            }

            /// M: add systrace to analyze application launche time.

            return true;
        } catch (SecurityException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Launcher does not have the permission to launch " + intent +
                    ". Make sure to create a MAIN intent-filter for the corresponding activity " +
                    "or use the exported attribute for this activity. "
                    + "tag=" + tag + " intent=" + intent, e);
        }
        return false;
    }

    boolean startActivitySafely(View v, Intent intent, Object tag) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "startActivitySafely v = " + v + ", intent = " + intent + ", tag = " + tag);
        }
      //add by ETON guolinan
        if (tag != null && tag instanceof ItemInfo) {
        	
        	/// M: Modified by liudekuan
        	/// R: Sometimes apps in sBgItemsIdMap donot match to mBgAllAppsList, 
        	///    which causes corner-mark wont disappear
        	final ItemInfo ii = LauncherModel.sBgItemsIdMap.get(((ItemInfo)tag).id);
        	if (ii != null) {
        		new Thread() {
					public void run() {
						LauncherModel.updateApplicationInfoNotNew(Launcher.this, ii.id);
		            }
				}.run();
        		ii.isNew = LauncherSettings.Favorites.ITEM_NOT_NEW;
        		bindComponentUnreadChanged(intent.getComponent(), ((ItemInfo) tag).unreadNum);
        	}
        	
//    		final List<ApplicationInfo> appInfos = mModel.getAllAppsList().data;
//    		for (ApplicationInfo appInfo : appInfos) {
//    			
//    			if (LauncherLog.DEBUG) {
//    	            LauncherLog.d(TAG, "appInfo = " + appInfo.title + ", " + appInfo.intent);
//    	        }
//    			
//    			if (appInfo.isNew == LauncherSettings.Favorites.ITEM_IS_NEW 
//    						&& intent.getComponent() != null
//    						&& appInfo.intent.getComponent().flattenToString().equals(
//    								intent.getComponent().flattenToString())){
//    				
//    				if (LauncherLog.DEBUG) {
//        	            LauncherLog.d(TAG, "find matching appInfo: appInfo.id = " + appInfo.id + ", tag.id = " + ((ItemInfo)tag).id);
//        	        }
//    				
//    				// modified by liudekuan
////    				LauncherModel.updateApplicationInfoNotNew(this, intent);
//    				final ItemInfo ii = (ItemInfo) tag;
//    				new Thread() {
//    					public void run() {
//    						LauncherModel.updateApplicationInfoNotNew(Launcher.this, ii.id);
//    		            }
//    				}.run();
//    				// end
//    				appInfo.isNew = LauncherSettings.Favorites.ITEM_NOT_NEW;
//    				bindComponentUnreadChanged(intent.getComponent(), ((ItemInfo) tag).unreadNum);
//    				break;
//    			}
//    		}
        	/// M: End
        }
        //end
        boolean success = false;
        try {
            success = startActivity(v, intent, tag);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Unable to launch. tag=" + tag + " intent=" + intent, e);
        }
        //>>add by eton wanghenan
        if (tag != null && tag instanceof ItemInfo) {
        	ItemInfo itemInfo = (ItemInfo) tag;
        	SectorUtils.updateAppCountToDatabase(this,itemInfo.id);
        }
//        SectorUtils.updateAppCountToDatabase(this, intent, (List<ApplicationInfo>)(mModel.getAllAppsList().data.clone()));
        //<<end
        return success;
    }

    void startActivityForResultSafely(Intent intent, int requestCode) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "startActivityForResultSafely: intent = " + intent
                    + ", requestCode = " + requestCode);
        }

        try {
            startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Launcher does not have the permission to launch " + intent +
                    ". Make sure to create a MAIN intent-filter for the corresponding activity " +
                    "or use the exported attribute for this activity.", e);
        }
    }

    private void handleFolderClick(FolderIcon folderIcon) {
        final FolderInfo info = folderIcon.getFolderInfo();
        Folder openFolder = mWorkspace.getFolderForTag(info);

        // If the folder info reports that the associated folder is open, then verify that
        // it is actually opened. There have been a few instances where this gets out of sync.
        if (info.opened && openFolder == null) {
            Log.d(TAG, "Folder info marked as open, but associated folder is not open. Screen: "
                    + LauncherModel.getActualScreen(info.screen, info.container) + " (" + info.cellX + ", " + info.cellY + ")");
            info.opened = false;
        }

        if (!info.opened && !folderIcon.getFolder().isDestroyed()) {
            // Close any open folder
            closeFolder();
            // Open the requested folder
            openFolder(folderIcon);
        } else {
            // Find the open folder...
            int folderScreen;
            if (openFolder != null) {
                folderScreen = mWorkspace.getPageForView(openFolder);
                // .. and close it
                closeFolder(openFolder);
                if (folderScreen != mWorkspace.getCurrentPage()) {
                    // Close any folder open on the current screen
                    closeFolder();
                    // Pull the folder onto this screen
                    openFolder(folderIcon);
                }
            }
        }
    }

    /**
     * This method draws the FolderIcon to an ImageView and then adds and positions that ImageView
     * in the DragLayer in the exact absolute location of the original FolderIcon.
     */
    private void copyFolderIconToImage(FolderIcon fi) {
        final int width = fi.getMeasuredWidth();
        final int height = fi.getMeasuredHeight();

        // Lazy load ImageView, Bitmap and Canvas
        if (mFolderIconImageView == null) {
            mFolderIconImageView = new ImageView(this);
        }
        if (mFolderIconBitmap == null || mFolderIconBitmap.getWidth() != width ||
                mFolderIconBitmap.getHeight() != height) {
            mFolderIconBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            mFolderIconCanvas = new Canvas(mFolderIconBitmap);
        }

        DragLayer.LayoutParams lp;
        if (mFolderIconImageView.getLayoutParams() instanceof DragLayer.LayoutParams) {
            lp = (DragLayer.LayoutParams) mFolderIconImageView.getLayoutParams();
        } else {
            lp = new DragLayer.LayoutParams(width, height);
        }

        // The layout from which the folder is being opened may be scaled, adjust the starting
        // view size by this scale factor.
        float scale = mDragLayer.getDescendantRectRelativeToSelf(fi, mRectForFolderAnimation);
        lp.customPosition = true;
        
        lp.x = mRectForFolderAnimation.left;
        lp.y = mRectForFolderAnimation.top;
        lp.width = (int) (scale * width);
        lp.height = (int) (scale * height);

        mFolderIconCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        fi.draw(mFolderIconCanvas);
        mFolderIconImageView.setImageBitmap(mFolderIconBitmap);
        if (fi.getFolder() != null) {
            mFolderIconImageView.setPivotX(fi.getFolder().getPivotXForIconAnimation());
            mFolderIconImageView.setPivotY(fi.getFolder().getPivotYForIconAnimation());
        }
        // Just in case this image view is still in the drag layer from a previous animation,
        // we remove it and re-add it.
        if (mDragLayer.indexOfChild(mFolderIconImageView) != -1) {
            mDragLayer.removeView(mFolderIconImageView);
        }
        mDragLayer.addView(mFolderIconImageView, lp);
        if (fi.getFolder() != null) {
            fi.getFolder().bringToFront();
        }
    }

    private void growAndFadeOutFolderIcon(FolderIcon fi) {
        if (fi == null) {
            return;
        }
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 0);
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 1.5f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 1.5f);

        FolderInfo info = (FolderInfo) fi.getTag();
        if (info.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
            CellLayout cl = (CellLayout) fi.getParent().getParent();
            CellLayout.LayoutParams lp = (CellLayout.LayoutParams) fi.getLayoutParams();
            cl.setFolderLeaveBehindCell(lp.cellX, lp.cellY);
        }

        // Push an ImageView copy of the FolderIcon into the DragLayer and hide the original
        copyFolderIconToImage(fi);
        fi.setVisibility(View.INVISIBLE);

        ObjectAnimator oa = LauncherAnimUtils.ofPropertyValuesHolder(mFolderIconImageView, alpha,
                scaleX, scaleY);
        oa.setDuration(getResources().getInteger(R.integer.config_folderAnimDuration));
        oa.start();
    }

    private void shrinkAndFadeInFolderIcon(final FolderIcon fi) {
        if (fi == null) return;
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 1.0f);
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 1.0f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 1.0f);

        final CellLayout cl = (CellLayout) fi.getParent().getParent();

        // We remove and re-draw the FolderIcon in-case it has changed
        mDragLayer.removeView(mFolderIconImageView);
        copyFolderIconToImage(fi);
        ObjectAnimator oa = LauncherAnimUtils.ofPropertyValuesHolder(mFolderIconImageView, alpha,
                scaleX, scaleY);
        oa.setDuration(getResources().getInteger(R.integer.config_folderAnimDuration));
        oa.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (cl != null) {
                    cl.clearFolderLeaveBehind();
                    // Remove the ImageView copy of the FolderIcon and make the original visible.
                    mDragLayer.removeView(mFolderIconImageView);
                    fi.setVisibility(View.VISIBLE);
                }
            }
        });
        oa.start();
    }

/*
    public void sendStatusBarBroadcast()
    {
        Intent intent = new Intent();
        intent.setAction("android.intent.Launcher.openfload");
        
        Bundle bundle = new Bundle();
        bundle.putParcelable("bitmap", mStatusGaussBitmap);
        intent.putExtra("bundle", bundle);

        sendBroadcast(intent);        
    }
*/    
    /**
     * Opens the user folder described by the specified tag. The opening of the folder
     * is animated relative to the specified View. If the View is null, no animation
     * is played.
     *
     * @param folderInfo The FolderInfo describing the folder to open.
     */
    public void openFolder(FolderIcon folderIcon) {
        Folder folder = folderIcon.getFolder();
        FolderInfo info = folder.mInfo;

        info.opened = true;

        // Just verify that the folder hasn't already been added to the DragLayer.
        // There was a one-off crash where the folder had a parent already.
        if (folder.getParent() == null) {
        	
            mDragLayer.addView(folder);
            DragLayer.LayoutParams flp = (DragLayer.LayoutParams) folder.getLayoutParams();
            mDragController.addDropTarget((DropTarget) folder);
        } else {
            Log.w(TAG, "Opening folder (" + folder + ") which already has a parent (" +
                    folder.getParent() + ").");
        }
        
        if(!mWorkspace.getScreenEditState())
        {
            int page = getCurrentPage();
            if(getGaussBitmap(page) == null)
            {
                mCopyBitmap = Bitmap.createBitmap(mGaussViewBG);
                Bitmap b = getEndGaussBitmap(mWorkSpaceView);
                if( b!= null)
                {
                    addGaussBitmap(b, page);
                }
                
                mGaussOver = true;
            }
            
        }

        //sendStatusBarBroadcast();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        
            FrameLayout.LayoutParams   lp = (FrameLayout.LayoutParams) getWorkspace().getLayoutParams();
            if(topMargin <= -1){
                topMargin = lp.topMargin;
            }
            lp.topMargin = topMargin + mStatusBarHeight;
        
        folder.animateOpen();
        // removed by liudekuan
//        growAndFadeOutFolderIcon(folderIcon);
        // end
    }

    public void closeFolder() {
        Folder folder = mWorkspace.getOpenFolder();
        if (folder != null) {
            if (folder.isEditingName()) {
                folder.dismissEditingName();
            }
            closeFolder(folder);

            // Dismiss the folder cling
            dismissFolderCling(null);
        }
    }

    void closeFolder(Folder folder) {
        folder.getInfo().opened = false;

        ViewGroup parent = (ViewGroup) folder.getParent().getParent();
        if (parent != null) {
            FolderIcon fi = (FolderIcon) mWorkspace.getViewForTag(folder.mInfo);
            shrinkAndFadeInFolderIcon(fi);
        }
        folder.animateClosed();
    }
    private boolean mBoolScreenEdit = false;

    public boolean onLongClick(View v) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "onLongClick: View = " + v + ", v.getTag() = " + v.getTag()
                    + ", mState = " + mState);
        }

        if (!isDraggingEnabled()) {
            LauncherLog.d(TAG, "onLongClick: isDraggingEnabled() = " + isDraggingEnabled());
            return false;
        }

        if (isWorkspaceLocked()) {
            LauncherLog.d(TAG, "onLongClick: isWorkspaceLocked() mWorkspaceLoading " + mWorkspaceLoading
                    + ", mWaitingForResult = " + mWaitingForResult);
            return false;
        }

        if (mState != State.WORKSPACE) {
            LauncherLog.d(TAG, "onLongClick: mState != State.WORKSPACE: = " + mState);
            return false;
        }

        /// M: modidfied for Unread feature, to find CellLayout through while loop.
        while (!(v instanceof CellLayout)) {
            v = (View) v.getParent();
        }

        resetAddInfo();
        CellLayout.CellInfo longClickCellInfo = (CellLayout.CellInfo) v.getTag();
        // This happens when long clicking an item with the dpad/trackball
        if (longClickCellInfo == null) {
            return true;
        }

        // The hotseat touch handling does not go through Workspace, and we always allow long press
        // on hotseat items.
        final View itemUnderLongClick = longClickCellInfo.cell;
        boolean allowLongPress = isHotseatLayout(v) || mWorkspace.allowLongPress();
        Log.d("chx","allwoLong="+allowLongPress + " isDrag="+mDragController.isDragging());
        if (allowLongPress && !mDragController.isDragging()) {
			if (itemUnderLongClick == null) {
				// User long pressed on empty space
				// Modified by chenxin
				if (!mWorkspace.getScreenEditState()) {
					mModel.enterScreenEditMode(this, CUSTOM_MENU_EDIT);
				}

				/*
				 * mWorkspace.performHapticFeedback(HapticFeedbackConstants.
				 * LONG_PRESS,
				 * HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING); //
				 * startWallpaper(); CustomDialogFragment cdf = new
				 * CustomDialogFragment(); cdf.show(getFragmentManager(),
				 * DIALOG_CUSTOM);
				 */

			} else {
                if (!(itemUnderLongClick instanceof Folder)) {
                    /// M: Call the appropriate callback for the IMtkWidget on the current page
                    /// when long click and begin to drag IMtkWidget.
                    /*mWorkspace.startDragAppWidget(mWorkspace.getCurrentPage());*/
                    // User long pressed on an item
                    mWorkspace.startDrag(longClickCellInfo);
                }
            }
        }
        return true;
    }

    /**
     * M: Added by liudekuan
     */
    private void setScreenEditState (int type) {
    	
    	mMenuWallpaper.setSelected(false);
    	mMenuEffect.setSelected(false);
    	mMenuWidget.setSelected(false);
    	
    	switch(type) {
    	case CUSTOM_MENU_SETTINGS:
    		ScreenEditUtil.editMenuChoose = ScreenEditUtil.MENU_ID_THEME;
    		mScreenEditUtil.updateMenuTheme();
    		break;
    	case CUSTOM_MENU_WALLPAPER:
    	case CUSTOM_MENU_EDIT:
    		mMenuWallpaper.setSelected(true);
    		ScreenEditUtil.editMenuChoose = ScreenEditUtil.MENU_ID_WALLPAPER;
    		mScreenEditUtil.mTempView = mMenuWallpaper;
    		mScreenEditUtil.updateMenuWallpaper();
    		break;
    	case CUSTOM_MENU_EFFECT:
    		mMenuEffect.setSelected(true);
    		ScreenEditUtil.editMenuChoose = ScreenEditUtil.MENU_ID_EFFECT;
    		mScreenEditUtil.mTempView = mMenuEffect;
    		mScreenEditUtil.updateMenuEffect();
    		break;
    	default:
    		if (LauncherLog.DEBUG) {
    			LauncherLog.d(TAG, "setScreenEditState: invalid type = " + type);
    		}
    	}
    }
    
	//added end
    
    boolean isHotseatLayout(View layout) {
        return mHotseat != null && layout != null &&
                (layout instanceof CellLayout) && (layout == mHotseat.getLayout());
    }
    
    Hotseat getHotseat() {
        return mHotseat;
    }
    
    SearchDropTargetBar getSearchBar() {
        return mSearchDropTargetBar;
    }

    /**
     * Returns the CellLayout of the specified container at the specified screen.
     */
    CellLayout getCellLayout(long container, int screen) {
        if (container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
            if (mHotseat != null) {
                return mHotseat.getLayout();
            } else {
                return null;
            }
        } else {
            return (CellLayout) mWorkspace.getChildAt(screen);
        }
    }

    Workspace getWorkspace() {
        return mWorkspace;
    }

    // Now a part of LauncherModel.Callbacks. Used to reorder loading steps.
    @Override
    public boolean isAllAppsVisible() {
        return (mState == State.APPS_CUSTOMIZE) || (mOnResumeState == State.APPS_CUSTOMIZE);
    }
//deleted by guolinan
   public boolean isAllAppsButtonRank(int rank) {
       /*  return mHotseat.isAllAppsButtonRank(rank);*/
	   return false;
    }

    /**
     * Helper method for the cameraZoomIn/cameraZoomOut animations
     * @param view The view being animated
     * @param scaleFactor The scale factor used for the zoom
     */
    private void setPivotsForZoom(View view, float scaleFactor) {
        view.setPivotX(view.getWidth() / 2.0f);
        view.setPivotY(view.getHeight() / 2.0f);
    }

    void disableWallpaperIfInAllApps() {
        // Only disable it if we are in all apps
        if (isAllAppsVisible()) {
            if (mAppsCustomizeTabHost != null &&
                    !mAppsCustomizeTabHost.isTransitioning()) {
                //modified by ETON guolinan
            	updateWallpaperVisibility(true);
//            	updateWallpaperVisibility(false);
            	//end
            }
        }
    }

    private void setWorkspaceBackground(boolean workspace) {
        mLauncherView.setBackground(workspace ?
                mWorkspaceBackgroundDrawable : mBlackBackgroundDrawable);
    }

    public void updateWallpaperVisibility(boolean visible) {
        int wpflags = visible ? WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER : 0;
        int curflags = getWindow().getAttributes().flags
                & WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER;
        if (wpflags != curflags) {
            getWindow().setFlags(wpflags, WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
        }
        setWorkspaceBackground(visible);
    }

    private void dispatchOnLauncherTransitionPrepare(View v, boolean animated, boolean toWorkspace) {
        if (v instanceof LauncherTransitionable) {
            ((LauncherTransitionable) v).onLauncherTransitionPrepare(this, animated, toWorkspace);
        }
    }

    private void dispatchOnLauncherTransitionStart(View v, boolean animated, boolean toWorkspace) {
        if (v instanceof LauncherTransitionable) {
            ((LauncherTransitionable) v).onLauncherTransitionStart(this, animated, toWorkspace);
        }

        // Update the workspace transition step as well
        dispatchOnLauncherTransitionStep(v, 0f);
    }

    private void dispatchOnLauncherTransitionStep(View v, float t) {
        if (v instanceof LauncherTransitionable) {
            ((LauncherTransitionable) v).onLauncherTransitionStep(this, t);
        }
    }

    private void dispatchOnLauncherTransitionEnd(View v, boolean animated, boolean toWorkspace) {
        if (v instanceof LauncherTransitionable) {
            ((LauncherTransitionable) v).onLauncherTransitionEnd(this, animated, toWorkspace);
        }

        // Update the workspace transition step as well
        dispatchOnLauncherTransitionStep(v, 1f);
    }

    /**
     * Things to test when changing the following seven functions.
     *   - Home from workspace
     *          - from center screen
     *          - from other screens
     *   - Home from all apps
     *          - from center screen
     *          - from other screens
     *   - Back from all apps
     *          - from center screen
     *          - from other screens
     *   - Launch app from workspace and quit
     *          - with back
     *          - with home
     *   - Launch app from all apps and quit
     *          - with back
     *          - with home
     *   - Go to a screen that's not the default, then all
     *     apps, and launch and app, and go back
     *          - with back
     *          -with home
     *   - On workspace, long press power and go back
     *          - with back
     *          - with home
     *   - On all apps, long press power and go back
     *          - with back
     *          - with home
     *   - On workspace, power off
     *   - On all apps, power off
     *   - Launch an app and turn off the screen while in that app
     *          - Go back with home key
     *          - Go back with back key  TODO: make this not go to workspace
     *          - From all apps
     *          - From workspace
     *   - Enter and exit car mode (becuase it causes an extra configuration changed)
     *          - From all apps
     *          - From the center workspace
     *          - From another workspace
     */

    /**
     * Zoom the camera out from the workspace to reveal 'toView'.
     * Assumes that the view to show is anchored at either the very top or very bottom
     * of the screen.
     */
    private void showAppsCustomizeHelper(final boolean animated, final boolean springLoaded) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "showAppsCustomizeHelper animated = " + animated + ", springLoaded = " + springLoaded);
        }

        if (mStateAnimation != null) {
            mStateAnimation.cancel();
            mStateAnimation = null;
        }
        final Resources res = getResources();

        final int duration = res.getInteger(R.integer.config_appsCustomizeZoomInTime);
        final int fadeDuration = res.getInteger(R.integer.config_appsCustomizeFadeInTime);
        
        
        final float scale = (float) res.getInteger(R.integer.config_appsCustomizeZoomScaleFactor);
        final View fromView = mWorkspace;
        final AppsCustomizeTabHost toView = mAppsCustomizeTabHost;
        final int startDelay =
                res.getInteger(R.integer.config_workspaceAppsCustomizeAnimationStagger);

        setPivotsForZoom(toView, scale);
        hideHotseat(animated);
        // Shrink workspaces away if going to AppsCustomize from workspace
        Animator workspaceAnim =
                mWorkspace.getChangeStateAnimation(Workspace.State.SMALL, animated);

        if (animated) {
            toView.setScaleX(scale);
            toView.setScaleY(scale);
            final LauncherViewPropertyAnimator scaleAnim = new LauncherViewPropertyAnimator(toView);
            scaleAnim.
                scaleX(1f).scaleY(1f).
                setDuration(duration).
                setInterpolator(new Workspace.ZoomOutInterpolator());

            toView.setVisibility(View.VISIBLE);
            toView.setAlpha(0f);
            final ObjectAnimator alphaAnim = ObjectAnimator
                .ofFloat(toView, "alpha", 0f, 1f)
                .setDuration(fadeDuration);
            alphaAnim.setInterpolator(new DecelerateInterpolator(1.5f));
            alphaAnim.addUpdateListener(new AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (animation == null) {
                        throw new RuntimeException("animation is null");
                    }
                    float t = (Float) animation.getAnimatedValue();
                    dispatchOnLauncherTransitionStep(fromView, t);
                    dispatchOnLauncherTransitionStep(toView, t);
                }
            });

            // toView should appear right at the end of the workspace shrink
            // animation
            mStateAnimation = LauncherAnimUtils.createAnimatorSet();
            mStateAnimation.play(scaleAnim).after(startDelay);
            mStateAnimation.play(alphaAnim).after(startDelay);

            mStateAnimation.addListener(new AnimatorListenerAdapter() {
                boolean animationCancelled = false;

                @Override
                public void onAnimationStart(Animator animation) {
                    updateWallpaperVisibility(true);
                    // Prepare the position
                    toView.setTranslationX(0.0f);
                    toView.setTranslationY(0.0f);
                    toView.setVisibility(View.VISIBLE);
                    toView.bringToFront();
                }
                @Override
                public void onAnimationEnd(Animator animation) {
                    dispatchOnLauncherTransitionEnd(fromView, animated, false);
                    dispatchOnLauncherTransitionEnd(toView, animated, false);

                    if (mWorkspace != null && !springLoaded && !LauncherApplication.isScreenLarge()) {
                        // Hide the workspace scrollbar
                        mWorkspace.hideScrollingIndicator(true);
                        //deleted by ETON guolinan
//                        hideDockDivider();
                        //end
                    }
                    if (!animationCancelled) {
//                        updateWallpaperVisibility(false);
                    }

                    // Hide the search bar
                    //deleted by ETON guolinan
                    /*if (mSearchDropTargetBar != null) {
                        mSearchDropTargetBar.hideSearchBar(false);
                    }*/
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    animationCancelled = true;
                }
            });

            if (workspaceAnim != null) {
                mStateAnimation.play(workspaceAnim);
            }

            boolean delayAnim = false;
            final ViewTreeObserver observer;

            dispatchOnLauncherTransitionPrepare(fromView, animated, false);
            dispatchOnLauncherTransitionPrepare(toView, animated, false);

            // If any of the objects being animated haven't been measured/laid out
            // yet, delay the animation until we get a layout pass
            if ((((LauncherTransitionable) toView).getContent().getMeasuredWidth() == 0) ||
                    (mWorkspace.getMeasuredWidth() == 0) ||
                    (toView.getMeasuredWidth() == 0)) {
                observer = mWorkspace.getViewTreeObserver();
                delayAnim = true;
            } else {
                observer = null;
            }

            final AnimatorSet stateAnimation = mStateAnimation;
            final Runnable startAnimRunnable = new Runnable() {
                public void run() {
                    // Check that mStateAnimation hasn't changed while
                    // we waited for a layout/draw pass
                    if (mStateAnimation != stateAnimation)
                        return;
                    setPivotsForZoom(toView, scale);
                    dispatchOnLauncherTransitionStart(fromView, animated, false);
                    dispatchOnLauncherTransitionStart(toView, animated, false);
                    toView.post(new Runnable() {
                        public void run() {
                            // Check that mStateAnimation hasn't changed while
                            // we waited for a layout/draw pass
                            if (mStateAnimation != stateAnimation)
                                return;
                            mStateAnimation.start();
                        }
                    });
                }
            };
            if (delayAnim) {
                final OnGlobalLayoutListener delayedStart = new OnGlobalLayoutListener() {
                    public void onGlobalLayout() {
                        toView.post(startAnimRunnable);
                        observer.removeOnGlobalLayoutListener(this);
                    }
                };
                observer.addOnGlobalLayoutListener(delayedStart);
            } else {
                startAnimRunnable.run();
            }
        } else {
            toView.setTranslationX(0.0f);
            toView.setTranslationY(0.0f);
            toView.setScaleX(1.0f);
            toView.setScaleY(1.0f);
            toView.setVisibility(View.VISIBLE);
            toView.bringToFront();

            if (!springLoaded && !LauncherApplication.isScreenLarge()) {
                // Hide the workspace scrollbar
                mWorkspace.hideScrollingIndicator(true);
                //deleted by ETON guolinan
//                hideDockDivider();
                //end
                // Hide the search bar
               //deleted by ETON guolinan
                /* if (mSearchDropTargetBar != null) {
                    mSearchDropTargetBar.hideSearchBar(false);
                }*/
            }
            dispatchOnLauncherTransitionPrepare(fromView, animated, false);
            dispatchOnLauncherTransitionStart(fromView, animated, false);
            dispatchOnLauncherTransitionEnd(fromView, animated, false);
            dispatchOnLauncherTransitionPrepare(toView, animated, false);
            dispatchOnLauncherTransitionStart(toView, animated, false);
            dispatchOnLauncherTransitionEnd(toView, animated, false);
            //deleted by ETON guolinan
//            updateWallpaperVisibility(false);
            //end
        }
    }

    /**
     * Zoom the camera back into the workspace, hiding 'fromView'.
     * This is the opposite of showAppsCustomizeHelper.
     * @param animated If true, the transition will be animated.
     */
    private void hideAppsCustomizeHelper(State toState, final boolean animated,
            final boolean springLoaded, final Runnable onCompleteRunnable) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "hideAppsCustomzieHelper toState = " + toState + ", animated = " + animated
                    + ", springLoaded = " + springLoaded);
        }

        if (mStateAnimation != null) {
            mStateAnimation.cancel();
            mStateAnimation = null;
        }
        Resources res = getResources();

        final int duration = res.getInteger(R.integer.config_appsCustomizeZoomOutTime);
        final int fadeOutDuration =
                res.getInteger(R.integer.config_appsCustomizeFadeOutTime);
        final float scaleFactor = (float)
                res.getInteger(R.integer.config_appsCustomizeZoomScaleFactor);
        final View fromView = mAppsCustomizeTabHost;
        final View toView = mWorkspace;
        Animator workspaceAnim = null;

        if (toState == State.WORKSPACE) {
            int stagger = res.getInteger(R.integer.config_appsCustomizeWorkspaceAnimationStagger);
            workspaceAnim = mWorkspace.getChangeStateAnimation(
                    Workspace.State.NORMAL, animated, stagger);
        } else if (toState == State.APPS_CUSTOMIZE_SPRING_LOADED) {
            workspaceAnim = mWorkspace.getChangeStateAnimation(
                    Workspace.State.SPRING_LOADED, animated);
        }

        setPivotsForZoom(fromView, scaleFactor);
        updateWallpaperVisibility(true);
        showHotseat(animated);
       if (animated) {
            final LauncherViewPropertyAnimator scaleAnim =
                    new LauncherViewPropertyAnimator(fromView);
            scaleAnim.
                scaleX(scaleFactor).scaleY(scaleFactor).
                setDuration(duration).
                setInterpolator(new Workspace.ZoomInInterpolator());

            final ObjectAnimator alphaAnim = ObjectAnimator
                .ofFloat(fromView, "alpha", 1f, 0f)
                .setDuration(fadeOutDuration);
            alphaAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            alphaAnim.addUpdateListener(new AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float t = 1f - (Float) animation.getAnimatedValue();
                    dispatchOnLauncherTransitionStep(fromView, t);
                    dispatchOnLauncherTransitionStep(toView, t);
                }
            });

            mStateAnimation = LauncherAnimUtils.createAnimatorSet();
            dispatchOnLauncherTransitionPrepare(fromView, animated, true);
            dispatchOnLauncherTransitionPrepare(toView, animated, true);
            mAppsCustomizeContent.pauseScrolling();
           
            mStateAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    updateWallpaperVisibility(true);
                    fromView.setVisibility(View.GONE);
                    dispatchOnLauncherTransitionEnd(fromView, animated, true);
                    dispatchOnLauncherTransitionEnd(toView, animated, true);
                    if (mWorkspace != null) {
                        mWorkspace.hideScrollingIndicator(false);
                    }
                    if (onCompleteRunnable != null) {
                        onCompleteRunnable.run();
                    }
                    mAppsCustomizeContent.updateCurrentPageScroll();
                    mAppsCustomizeContent.resumeScrolling();
                }
            });
            	mStateAnimation.playTogether(scaleAnim, alphaAnim);
            if (workspaceAnim != null) {
                mStateAnimation.play(workspaceAnim);
            }
            dispatchOnLauncherTransitionStart(fromView, animated, true);
            dispatchOnLauncherTransitionStart(toView, animated, true);
            final Animator stateAnimation = mStateAnimation;
            mWorkspace.post(new Runnable() {
                public void run() {
                    if (stateAnimation != mStateAnimation)
                        return;
                    mStateAnimation.start();
                }
            });
        } else {
            fromView.setVisibility(View.GONE);
            dispatchOnLauncherTransitionPrepare(fromView, animated, true);
            dispatchOnLauncherTransitionStart(fromView, animated, true);
            dispatchOnLauncherTransitionEnd(fromView, animated, true);
            dispatchOnLauncherTransitionPrepare(toView, animated, true);
            dispatchOnLauncherTransitionStart(toView, animated, true);
            dispatchOnLauncherTransitionEnd(toView, animated, true);
            mWorkspace.hideScrollingIndicator(false);
        }
      
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "onTrimMemory: level = " + level);
        }

       /* if (level >= ComponentCallbacks2.TRIM_MEMORY_COMPLETE) {
            volunteerFreeMemory();
        }*/
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (!hasFocus) {
            // When another window occludes launcher (like the notification shade, or recents),
            // ensure that we enable the wallpaper flag so that transitions are done correctly.
            updateWallpaperVisibility(true);
        } else {
            // When launcher has focus again, disable the wallpaper if we are in AllApps
            mWorkspace.postDelayed(new Runnable() {
                @Override
                public void run() {
                    disableWallpaperIfInAllApps();
                }
            }, 500);
        }
    }

    void showWorkspace(boolean animated) {
        /// M: Call the appropriate callback for the IMtkWidget on the current page when leave all apps list back to
        /// workspace.
        /*mWorkspace.stopCovered(mWorkspace.getCurrentPage());*/
        showWorkspace(animated, null);
    }

    void showWorkspace(boolean animated, Runnable onCompleteRunnable) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "showWorkspace: animated = " + animated + ", mState = " + mState);
        }

        boolean wasInSpringLoadedMode = (mState == State.APPS_CUSTOMIZE_SPRING_LOADED);
        if (mState != State.WORKSPACE) {
            mWorkspace.setVisibility(View.VISIBLE);
            hideAppsCustomizeHelper(State.WORKSPACE, animated, false, onCompleteRunnable);

            // Show the search bar (only animate if we were showing the drop target bar in spring
            // loaded mode)
            //deleted by ETON guolinan
            /*if (mSearchDropTargetBar != null) {
                mSearchDropTargetBar.showSearchBar(wasInSpringLoadedMode);
            }*/
            //deleted by ETON guolinan
            // We only need to animate in the dock divider if we're going from spring loaded mode
//            showDockDivider(animated && wasInSpringLoadedMode);
            //end
            // Set focus to the AppsCustomize button
           //deleted by ETON guolinan
            /* if (mAllAppsButton != null) {
                mAllAppsButton.requestFocus();
            }*/
        }
        mWorkspace.flashScrollingIndicator(animated);

        // Change the state *after* we've called all the transition code
        mState = State.WORKSPACE;

        // Resume the auto-advance of widgets
        mUserPresent = true;
        updateRunning();

        // Send an accessibility event to announce the context change
        getWindow().getDecorView()
                .sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
    }

    void showAllApps(boolean animated) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "showAllApps: animated = " + animated + ", mState = " + mState
                    + ", mCurrentBounds = " + mCurrentBounds);
        }
        if (mState != State.WORKSPACE) return;
        
        /// M: Recorder current bounds of current cellLayout.
        if (mWorkspace != null) {
            mDragLayer.getDescendantRectRelativeToSelf(mWorkspace.getCurrentDropLayout(), mCurrentBounds);
        }

        /// M: Call the appropriate callback for the IMtkWidget on the current page when enter all apps list.
//        mWorkspace.startCovered(mWorkspace.getCurrentPage());
        showAppsCustomizeHelper(animated, false);
        mAppsCustomizeTabHost.requestFocus();
        // Change the state *after* we've called all the transition code
        mState = State.APPS_CUSTOMIZE;

        // Pause the auto-advance of widgets until we are out of AllApps
        mUserPresent = false;
        updateRunning();
        closeFolder();

        // Send an accessibility event to announce the context change
        getWindow().getDecorView()
                .sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
    }

    //added by ETON guolinan
    void enterSpringLoadedMode() {
            mState = State.APPS_CUSTOMIZE_SPRING_LOADED;
    }
    void exitSpringLoadedMode() {
            mState = State.WORKSPACE;
    }
    //end
    
    void enterSpringLoadedDragMode() {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "enterSpringLoadedDragMode mState = " + mState + ", mOnResumeState = " + mOnResumeState);
        }
        
    	if (isAllAppsVisible()) {
            hideAppsCustomizeHelper(State.APPS_CUSTOMIZE_SPRING_LOADED, true, true, null);
           //deleted by ETON guolinan
//            hideDockDivider();
            //end
            mState = State.APPS_CUSTOMIZE_SPRING_LOADED;
        }
    }

    void exitSpringLoadedDragModeDelayed(final boolean successfulDrop, boolean extendedDelay,
            final Runnable onCompleteRunnable) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "exitSpringLoadedDragModeDelayed successfulDrop = " + successfulDrop + ", extendedDelay = "
                    + extendedDelay + ", mState = " + mState);
        }

        if (mState != State.APPS_CUSTOMIZE_SPRING_LOADED) {
            return;
        }

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (successfulDrop) {
                    // Before we show workspace, hide all apps again because
                    // exitSpringLoadedDragMode made it visible. This is a bit hacky; we should
                    // clean up our state transition functions
                    mAppsCustomizeTabHost.setVisibility(View.GONE);
                    showWorkspace(true, onCompleteRunnable);
                } else {
                    exitSpringLoadedDragMode();
                }
            }
        }, (extendedDelay ?
                EXIT_SPRINGLOADED_MODE_LONG_TIMEOUT :
                EXIT_SPRINGLOADED_MODE_SHORT_TIMEOUT));
    }

    void exitSpringLoadedDragMode() {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "exitSpringLoadedDragMode mState = " + mState);
        }

        if (mState == State.APPS_CUSTOMIZE_SPRING_LOADED) {
            final boolean animated = true;
            final boolean springLoaded = true;
            showAppsCustomizeHelper(animated, springLoaded);
            mState = State.APPS_CUSTOMIZE;
        }
        // Otherwise, we are not in spring loaded mode, so don't do anything.
    }
//deleted by ETON guolinan, we do not need anything related to dock divider. 
 /*   void hideDockDivider() {
        if (mQsbDivider != null && mDockDivider != null) {
            mQsbDivider.setVisibility(View.INVISIBLE);
            mDockDivider.setVisibility(View.INVISIBLE);
        }
    }

    void showDockDivider(boolean animated) {
        if (mQsbDivider != null && mDockDivider != null) {
            mQsbDivider.setVisibility(View.VISIBLE);
            mDockDivider.setVisibility(View.VISIBLE);
            if (mDividerAnimator != null) {
                mDividerAnimator.cancel();
                mQsbDivider.setAlpha(1f);
                mDockDivider.setAlpha(1f);
                mDividerAnimator = null;
            }
            if (animated) {
                mDividerAnimator = LauncherAnimUtils.createAnimatorSet();
                mDividerAnimator.playTogether(LauncherAnimUtils.ofFloat(mQsbDivider, "alpha", 1f),
                        LauncherAnimUtils.ofFloat(mDockDivider, "alpha", 1f));
                int duration = 0;
                if (mSearchDropTargetBar != null) {
                    duration = mSearchDropTargetBar.getTransitionInDuration();
                }
                mDividerAnimator.setDuration(duration);
                mDividerAnimator.start();
            }
        }
    }
*/
    void lockAllApps() {
        // TODO
    }

    void unlockAllApps() {
        // TODO
    }

    /**
     * Shows the hotseat area.
     */
    void showHotseat(boolean animated) {
        if (!LauncherApplication.isScreenLarge()) {
            if (animated) {
                if (mHotseat.getAlpha() != 1f) {
                    int duration = 0;
                    if (mSearchDropTargetBar != null) {
                        duration = mSearchDropTargetBar.getTransitionInDuration();
                    }
                    mHotseat.animate().alpha(1f).setDuration(duration);
                }
            } else {
                mHotseat.setAlpha(1f);
            }
        }
    }

    /**
     * Hides the hotseat area.
     */
    void hideHotseat(boolean animated) {
        if (!LauncherApplication.isScreenLarge()) {
            if (animated) {
                if (mHotseat.getAlpha() != 0f) {
                    int duration = 0;
                    if (mSearchDropTargetBar != null) {
                        duration = mSearchDropTargetBar.getTransitionOutDuration();
                    }
                    mHotseat.animate().alpha(0f).setDuration(duration);
                }
            } else {
                mHotseat.setAlpha(0f);
            }
        }
    }

    /**
     * Add an item from all apps or customize onto the given workspace screen.
     * If layout is null, add to the current screen.
     */
    void addExternalItemToScreen(ItemInfo itemInfo, final CellLayout layout) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "addExternalItemToScreen itemInfo = " + itemInfo + ", layout = " + layout);
        }

        if (!mWorkspace.addExternalItemToScreen(itemInfo, layout)) {
            showOutOfSpaceMessage(isHotseatLayout(layout));
        }
    }

    /** Maps the current orientation to an index for referencing orientation correct global icons */
    private int getCurrentOrientationIndexForGlobalIcons() {
        // default - 0, landscape - 1
        switch (getResources().getConfiguration().orientation) {
        case Configuration.ORIENTATION_LANDSCAPE:
            return 1;
        default:
            return 0;
        }
    }

    private Drawable getExternalPackageToolbarIcon(ComponentName activityName, String resourceName) {
        try {
            PackageManager packageManager = getPackageManager();
            // Look for the toolbar icon specified in the activity meta-data
            Bundle metaData = packageManager.getActivityInfo(
                    activityName, PackageManager.GET_META_DATA).metaData;
            if (metaData != null) {
                int iconResId = metaData.getInt(resourceName);
                if (iconResId != 0) {
                    Resources res = packageManager.getResourcesForActivity(activityName);
                    return res.getDrawable(iconResId);
                }
            }
        } catch (NameNotFoundException e) {
            // This can happen if the activity defines an invalid drawable
            Log.w(TAG, "Failed to load toolbar icon; " + activityName.flattenToShortString() +
                    " not found", e);
        } catch (Resources.NotFoundException nfe) {
            // This can happen if the activity defines an invalid drawable
            Log.w(TAG, "Failed to load toolbar icon from " + activityName.flattenToShortString(),
                    nfe);
        }
        return null;
    }

    // if successful in getting icon, return it; otherwise, set button to use default drawable
    private Drawable.ConstantState updateTextButtonWithIconFromExternalActivity(
            int buttonId, ComponentName activityName, int fallbackDrawableId,
            String toolbarResourceName) {
        Drawable toolbarIcon = getExternalPackageToolbarIcon(activityName, toolbarResourceName);
        Resources r = getResources();
        int w = r.getDimensionPixelSize(R.dimen.toolbar_external_icon_width);
        int h = r.getDimensionPixelSize(R.dimen.toolbar_external_icon_height);

        TextView button = (TextView) findViewById(buttonId);
        // If we were unable to find the icon via the meta-data, use a generic one
        if (toolbarIcon == null) {
            toolbarIcon = r.getDrawable(fallbackDrawableId);
            toolbarIcon.setBounds(0, 0, w, h);
            if (button != null) {
                button.setCompoundDrawables(toolbarIcon, null, null, null);
            }
            return null;
        } else {
            toolbarIcon.setBounds(0, 0, w, h);
            if (button != null) {
                button.setCompoundDrawables(toolbarIcon, null, null, null);
            }
            return toolbarIcon.getConstantState();
        }
    }

    // if successful in getting icon, return it; otherwise, set button to use default drawable
    private Drawable.ConstantState updateButtonWithIconFromExternalActivity(
            int buttonId, ComponentName activityName, int fallbackDrawableId,
            String toolbarResourceName) {
        ImageView button = (ImageView) findViewById(buttonId);
        Drawable toolbarIcon = getExternalPackageToolbarIcon(activityName, toolbarResourceName);

        if (button != null) {
            // If we were unable to find the icon via the meta-data, use a
            // generic one
            if (toolbarIcon == null) {
                button.setImageResource(fallbackDrawableId);
            } else {
                button.setImageDrawable(toolbarIcon);
            }
        }

        return toolbarIcon != null ? toolbarIcon.getConstantState() : null;

    }

    private void updateTextButtonWithDrawable(int buttonId, Drawable d) {
        TextView button = (TextView) findViewById(buttonId);
        button.setCompoundDrawables(d, null, null, null);
    }
    //deleted by ETON guolinan
   /* private void updateButtonWithDrawable(int buttonId, Drawable.ConstantState d) {
        ImageView button = (ImageView) findViewById(buttonId);
        button.setImageDrawable(d.newDrawable(getResources()));
    }

    private void invalidatePressedFocusedStates(View container, View button) {
        if (container instanceof HolographicLinearLayout) {
            HolographicLinearLayout layout = (HolographicLinearLayout) container;
            layout.invalidatePressedFocusedStates();
        } else if (button instanceof HolographicImageView) {
            HolographicImageView view = (HolographicImageView) button;
            view.invalidatePressedFocusedStates();
        }
    }*/
    //change by ETON guolinan: we do not need the search bar
    /*private boolean updateGlobalSearchIcon() {
        final View searchButtonContainer = findViewById(R.id.search_button_container);
        final ImageView searchButton = (ImageView) findViewById(R.id.search_button);
        final View voiceButtonContainer = findViewById(R.id.voice_button_container);
        final View voiceButton = findViewById(R.id.voice_button);
        final View voiceButtonProxy = findViewById(R.id.voice_button_proxy);

        final SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        ComponentName activityName = searchManager.getGlobalSearchActivity();
        if (activityName != null) {
            /// M: only show search engine name on non-OP01 projects.
            final boolean needUpdate = LauncherExtPlugin.getSearchButtonExt(this).needUpdateSearchButtonResource();
            if (LauncherLog.DEBUG) {
                LauncherLog.d(TAG, "updateGlobalSearchIcon: needUpdate = " + needUpdate + ",activityName = " + activityName);
            }
            if (needUpdate) {
            int coi = getCurrentOrientationIndexForGlobalIcons();
            sGlobalSearchIcon[coi] = updateButtonWithIconFromExternalActivity(
                    R.id.search_button, activityName, R.drawable.ic_home_search_normal_holo,
                    TOOLBAR_SEARCH_ICON_METADATA_NAME);
                if (sGlobalSearchIcon[coi] == null) {
                    sGlobalSearchIcon[coi] = updateButtonWithIconFromExternalActivity(
                            R.id.search_button, activityName, R.drawable.ic_home_search_normal_holo,
                            TOOLBAR_ICON_METADATA_NAME);
                }
            } else {
                searchButton.setImageResource(R.drawable.ic_home_search_normal_holo);
            }

            if (searchButtonContainer != null) searchButtonContainer.setVisibility(View.VISIBLE);
            searchButton.setVisibility(View.VISIBLE);
            invalidatePressedFocusedStates(searchButtonContainer, searchButton);
            return true;
        } else {
            // We disable both search and voice search when there is no global search provider
            if (searchButtonContainer != null) searchButtonContainer.setVisibility(View.GONE);
            if (voiceButtonContainer != null) voiceButtonContainer.setVisibility(View.GONE);
            searchButton.setVisibility(View.GONE);
            voiceButton.setVisibility(View.GONE);
            if (voiceButtonProxy != null) {
                voiceButtonProxy.setVisibility(View.GONE);
            }
            return false;
        }
    }

    private void updateGlobalSearchIcon(Drawable.ConstantState d) {
        final View searchButtonContainer = findViewById(R.id.search_button_container);
        final View searchButton = (ImageView) findViewById(R.id.search_button);
        updateButtonWithDrawable(R.id.search_button, d);
        invalidatePressedFocusedStates(searchButtonContainer, searchButton);
    }

    private boolean updateVoiceSearchIcon(boolean searchVisible) {
        final View voiceButtonContainer = findViewById(R.id.voice_button_container);
        final View voiceButton = findViewById(R.id.voice_button);
        final View voiceButtonProxy = findViewById(R.id.voice_button_proxy);

        // We only show/update the voice search icon if the search icon is enabled as well
        final SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        ComponentName globalSearchActivity = searchManager.getGlobalSearchActivity();

        ComponentName activityName = null;
        if (globalSearchActivity != null) {
            // Check if the global search activity handles voice search
            Intent intent = new Intent(RecognizerIntent.ACTION_WEB_SEARCH);
            intent.setPackage(globalSearchActivity.getPackageName());
            activityName = intent.resolveActivity(getPackageManager());
        }

        if (activityName == null) {
            // Fallback: check if an activity other than the global search activity
            // resolves this
            Intent intent = new Intent(RecognizerIntent.ACTION_WEB_SEARCH);
            activityName = intent.resolveActivity(getPackageManager());
        }
        if (searchVisible && activityName != null) {
            int coi = getCurrentOrientationIndexForGlobalIcons();
            sVoiceSearchIcon[coi] = updateButtonWithIconFromExternalActivity(
                    R.id.voice_button, activityName, R.drawable.ic_home_voice_search_holo,
                    TOOLBAR_VOICE_SEARCH_ICON_METADATA_NAME);
            if (sVoiceSearchIcon[coi] == null) {
                sVoiceSearchIcon[coi] = updateButtonWithIconFromExternalActivity(
                        R.id.voice_button, activityName, R.drawable.ic_home_voice_search_holo,
                        TOOLBAR_ICON_METADATA_NAME);
            }
            if (voiceButtonContainer != null) voiceButtonContainer.setVisibility(View.VISIBLE);
            voiceButton.setVisibility(View.VISIBLE);
            if (voiceButtonProxy != null) {
                voiceButtonProxy.setVisibility(View.VISIBLE);
            }
            invalidatePressedFocusedStates(voiceButtonContainer, voiceButton);
            return true;
        } else {
            if (voiceButtonContainer != null) voiceButtonContainer.setVisibility(View.GONE);
            voiceButton.setVisibility(View.GONE);
            if (voiceButtonProxy != null) {
                voiceButtonProxy.setVisibility(View.GONE);
            }
            return false;
        }
    }

    private void updateVoiceSearchIcon(Drawable.ConstantState d) {
        final View voiceButtonContainer = findViewById(R.id.voice_button_container);
        final View voiceButton = findViewById(R.id.voice_button);
        updateButtonWithDrawable(R.id.voice_button, d);
        invalidatePressedFocusedStates(voiceButtonContainer, voiceButton);
    }*/

    /**
     * Sets the app market icon.
     */
    private void updateAppMarketIcon() {
        final View marketButton = findViewById(R.id.market_button);
        Intent intent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MARKET);
        // Find the app market activity by resolving an intent.
        // (If multiple app markets are installed, it will return the ResolverActivity.)
        ComponentName activityName = intent.resolveActivity(getPackageManager());
        if (activityName != null) {
            int coi = getCurrentOrientationIndexForGlobalIcons();
            mAppMarketIntent = intent;
            sAppMarketIcon[coi] = updateTextButtonWithIconFromExternalActivity(
                    R.id.market_button, activityName, R.drawable.ic_launcher_market_holo,
                    TOOLBAR_ICON_METADATA_NAME);
            marketButton.setVisibility(View.VISIBLE);
        } else {
            // We should hide and disable the view so that we don't try and restore the visibility
            // of it when we swap between drag & normal states from IconDropTarget subclasses.
            marketButton.setVisibility(View.GONE);
            marketButton.setEnabled(false);
        }
    }

    private void updateAppMarketIcon(Drawable.ConstantState d) {
        // Ensure that the new drawable we are creating has the approprate toolbar icon bounds
        Resources r = getResources();
        Drawable marketIconDrawable = d.newDrawable(r);
        int w = r.getDimensionPixelSize(R.dimen.toolbar_external_icon_width);
        int h = r.getDimensionPixelSize(R.dimen.toolbar_external_icon_height);
        marketIconDrawable.setBounds(0, 0, w, h);

        updateTextButtonWithDrawable(R.id.market_button, marketIconDrawable);
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        final boolean result = super.dispatchPopulateAccessibilityEvent(event);
        final List<CharSequence> text = event.getText();
        text.clear();
        // Populate event with a fake title based on the current state.
        if (mState == State.APPS_CUSTOMIZE) {
            text.add(getString(R.string.all_apps_button_label));
        } else {
            text.add(getString(R.string.all_apps_home_button_label));
        }
        return result;
    }

    /**
     * Receives notifications when system dialogs are to be closed.
     */
    private class CloseSystemDialogsIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (LauncherLog.DEBUG) {
                LauncherLog.d(TAG, "Close system dialogs: intent = " + intent);
            }
            closeSystemDialogs();
        }
    }
    
    private int  getLaunhcerNums(){
//    	ArrayList<ResolveInfo> list = new ArrayList<ResolveInfo>();
    	Intent it =new Intent(Intent.ACTION_MAIN);
    	it.addCategory(Intent.CATEGORY_HOME);
    	List<ResolveInfo> lt = this.getPackageManager().queryIntentActivities(it, 0);
    	if (null != lt){
    		LauncherLog.d(TAG, "getLaunhcerNums activityInfo="+lt.get(0).activityInfo + "  lt.size()="+lt.size());
    		return lt.size();
    	}
    	return 0;
    }
    
	private boolean isHasDefaultStartInfo() {
		String temp = null;
		final IntentFilter filter = new IntentFilter(Intent.ACTION_VIEW);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		List<IntentFilter> filters = new ArrayList<IntentFilter>();
		filters.add(filter);
		List<ComponentName> activities = new ArrayList<ComponentName>();
		final PackageManager packageManager = (PackageManager) getPackageManager();
		packageManager.getPreferredActivities(filters, activities, null);
		for (ComponentName activity : activities) {
			temp = activity.getPackageName();
		}
		if (temp == null) {
			LauncherLog.d(TAG,"isHasDefaultStartInfo   false");
			if (getLaunhcerNums() == 1){
				return true;
			}
			return false;
		}
		LauncherLog.d(TAG,"isHasDefaultStartInfo   true");
		return true;
	}
    /**
     * Receives notifications whenever the appwidgets are reset.
     */
    private class AppWidgetResetObserver extends ContentObserver {
        public AppWidgetResetObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            onAppWidgetReset();
        }
    }

    /**
     * If the activity is currently paused, signal that we need to re-run the loader
     * in onResume.
     *
     * This needs to be called from incoming places where resources might have been loaded
     * while we are paused.  That is becaues the Configuration might be wrong
     * when we're not running, and if it comes back to what it was when we
     * were paused, we are not restarted.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     *
     * @return true if we are currently paused.  The caller might be able to
     * skip some work in that case since we will come back again.
     */
    public boolean setLoadOnResume() {
        if (mPaused) {
            LauncherLog.i(TAG, "setLoadOnResume: this = " + this);
            mOnResumeNeedsLoad = true;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public int getCurrentWorkspaceScreen() {
        if (mWorkspace != null) {
            return mWorkspace.getCurrentPage();
        } else {
            return SCREEN_COUNT / 2;
        }
    }

    /**
     * Refreshes the shortcuts shown on the workspace.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void startBinding() {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "startBinding: this = " + this);
        }

        /// M: Cancel Drag when reload to avoid dragview lost parent and JE @{
        if (mDragController != null) {
            mDragController.cancelDrag();
        }
        /// M: }@

        final Workspace workspace = mWorkspace;

        mNewShortcutAnimatePage = -1;
        mNewShortcutAnimateViews.clear();
        mWorkspace.clearDropTargets();
        int count = workspace.getChildCount();
        for (int i = 0; i < count; i++) {
            // Use removeAllViewsInLayout() to avoid an extra requestLayout() and invalidate().
            final CellLayout layoutParent = (CellLayout) workspace.getChildAt(i);
            layoutParent.removeAllViewsInLayout();
            layoutParent.requestChildLayout();  
        }
        workspace.invalidate();
        mWidgetsToAdvance.clear();
         if (mHotseat != null) {
            mHotseat.resetLayout();
        }
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "startBinding: mIsLoadingWorkspace = " + mIsLoadingWorkspace);
        }
        mIsLoadingWorkspace = false;
    }

    /**
     * Bind the items start-end from the list.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindItems(ArrayList<ItemInfo> shortcuts, int start, int end) {
    	//M: by chenxin, when change language, need to reset mEtonWorkSpace
    	if (null != mWorkspace){
    		ViewPagerItemView.mEtonWorkSpace = mWorkspace;
    	}
    	
        setLoadOnResume();
        // Get the list of added shortcuts and intersect them with the set of shortcuts here
        Set<String> newApps = new HashSet<String>();
        newApps = mSharedPrefs.getStringSet(InstallShortcutReceiver.NEW_APPS_LIST_KEY, newApps);
        Workspace workspace = mWorkspace;
        for (int i = start; i < end; i++) {
            final ItemInfo item = shortcuts.get(i);
            if (LauncherLog.DEBUG) {
                LauncherLog.d(TAG, "bindItems: start = " + start + ", end = " + end 
                        + "item = " + item + ",title="+item.title + ", this = " + this);
            }

            // Short circuit if we are loading dock items for a configuration which has no dock
            if (item.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT &&
                    mHotseat == null) {
                continue;
            }

            switch (item.itemType) {
                case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
                case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                    ShortcutInfo info = (ShortcutInfo) item;
                    String uri = info.intent.toUri(0).toString();
                    View shortcut = createShortcut(info);
                    if (LauncherLog.DEBUG) {
                    	if (item.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
                    		LauncherLog.d(TAG, "bindItems: logicScreen=" + item.screen + "; actualScreen=" + 
                    				LauncherModel.getActualScreen(item.screen, item.container));
                    	}
                    }
                    workspace.addInScreen(shortcut, item.container, LauncherModel.getActualScreen(item.screen, item.container), item.cellX,
                            item.cellY, 1, 1, false);
//                    workspace.addInScreen(shortcut, item.container, item.screen, item.cellX,
//                            item.cellY, 1, 1, false);
                    boolean animateIconUp = false;
                    synchronized (newApps) {
                        if (newApps.contains(uri)) {
                            animateIconUp = newApps.remove(uri);
                        }
                    }
                    if (animateIconUp) {
                        // Prepare the view to be animated up
                        shortcut.setAlpha(0f);
                        shortcut.setScaleX(0.5f);
                        shortcut.setScaleY(0.5f);
                        /// M: Modified by liudekuan
//                        mNewShortcutAnimatePage = item.screen;
                        mNewShortcutAnimatePage = LauncherModel.getActualScreen(item.screen, item.container);
                        /// M: End
                        if (!mNewShortcutAnimateViews.contains(shortcut)) {
                            mNewShortcutAnimateViews.add(shortcut);
                        }
                    }
                    break;
                case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
                    FolderIcon newFolder = FolderIcon.fromXml(R.layout.folder_icon, this,
                            (ViewGroup) workspace.getChildAt(workspace.getCurrentPage()),
                            (FolderInfo) item, mIconCache);
                    workspace.addInScreen(newFolder, item.container, LauncherModel.getActualScreen(item.screen, item.container), item.cellX,
                            item.cellY, 1, 1, false);
                    break;
            }
        }

        //workspace.requestLayout();
    }

    /**
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindFolders(HashMap<Long, FolderInfo> folders) {
        setLoadOnResume();
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "bindFolders: this = " + this);
        }
        sFolders.clear();
        sFolders.putAll(folders);
    }

    /**
     * Add the views for a widget to the workspace.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindAppWidget(LauncherAppWidgetInfo item) {
        setLoadOnResume();

        final long start = DEBUG_WIDGETS ? SystemClock.uptimeMillis() : 0;
        if (DEBUG_WIDGETS) {
            Log.d(TAG, "bindAppWidget: " + item);
        }
        final Workspace workspace = mWorkspace;

        final int appWidgetId = item.appWidgetId;
        final AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        if (DEBUG_WIDGETS) {
            Log.d(TAG, "bindAppWidget: id=" + item.appWidgetId + " belongs to component "
                    + (appWidgetInfo == null ? "" : appWidgetInfo.provider));
        }
        
        /// M: If appWidgetInfo is null, appWidgetHostView will be error view,
        /// don't add in homescreen.
        if (appWidgetInfo == null) {
            return;
        }

        item.hostView = mAppWidgetHost.createView(this, appWidgetId, appWidgetInfo);

        item.hostView.setTag(item);
        item.onBindAppWidget(this);

        /// M: Call the appropriate callback for the IMtkWidget will be bound to workspace.
        /*mWorkspace.setAppWidgetIdAndScreen(item.hostView, mWorkspace.getCurrentPage(), appWidgetId);*/

        workspace.addInScreen(item.hostView, item.container, LauncherModel.getActualScreen(item.screen, item.container), item.cellX,
                item.cellY, item.spanX, item.spanY, false);
        addWidgetToAutoAdvanceIfNeeded(item.hostView, appWidgetInfo);

        workspace.requestLayout();

        if (DEBUG_WIDGETS) {
            Log.d(TAG, "bound widget id=" + item.appWidgetId + " in " + (SystemClock.uptimeMillis() - start) + "ms");
        }
    }
    
    /**
     * Add the views for a widget to the workspace.
     * Implementation of the method from LauncherModel.Callbacks.
     * M: Added by liudekuan
     */
    public void bindAppWidgetAdded (LauncherAppWidgetInfo item, int resultCode) {
    	
    	if (null == item) {
    		if (LauncherLog.DEBUG) {
        		LauncherLog.d(TAG, "bindAppWidgetAdded: info = null");
        	}
    		return;
    	}
    	
    	if (LauncherLog.DEBUG) {
    		LauncherLog.d(TAG, "bindAppWidgetAdded: resultCode = " + resultCode);
    	}
    	
    	if (resultCode == Launcher.ERROR) {
            Toast.makeText(this, getString(R.string.out_of_space), Toast.LENGTH_SHORT).show();
            return;
    	}
    	
    	final int appWidgetId = item.appWidgetId;
        final AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        if (LauncherLog.DEBUG) {
            Log.d(TAG, "bindAppWidget: id=" + item.appWidgetId + " belongs to component "
                    + (appWidgetInfo == null ? "" : appWidgetInfo.provider));
        }
        
        /// M: If appWidgetInfo is null, appWidgetHostView will be error view,
        /// don't add in homescreen.
        if (appWidgetInfo == null) {
            return;
        }

        item.hostView = mAppWidgetHost.createView(this, appWidgetId, appWidgetInfo);

        item.hostView.setTag(item);
        item.onBindAppWidget(this);
        if (mWorkspace.getScreenEditState()){
        	mWorkspace.removeDeleteDrawable(LauncherModel.getActualScreen(item.screen, item.container));
        }
        mWorkspace.addInScreen(item.hostView, item.container, LauncherModel.getActualScreen(item.screen, item.container), item.cellX,
                item.cellY, item.spanX, item.spanY, false);
        addWidgetToAutoAdvanceIfNeeded(item.hostView, appWidgetInfo);

        mWorkspace.requestLayout();
        
        ViewPagerItemView.mIsAddCompleted = true;
        resetAddInfo();
    }

    public void onPageBoundSynchronously(int page) {
        mSynchronouslyBoundPages.add(page);
    }

    /**
     * Callback saying that there aren't any more items to bind.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void finishBindingItems() {
        setLoadOnResume();
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "finishBindingItems: mSavedState = " + mSavedState + ", mSavedInstanceState = "
                    + mSavedInstanceState + ", this = " + this);
        }

        if (mSavedState != null) {
            if (!mWorkspace.hasFocus()) {
                mWorkspace.getChildAt(mWorkspace.getCurrentPage()).requestFocus();
            }
            mSavedState = null;
        }
        //added by ETON guolinan
        /*CellLayout cellLayout = (CellLayout)mWorkspace.getChildAt(0);
        ShortcutAndWidgetContainer child = (ShortcutAndWidgetContainer)cellLayout.getChildAt(0);
        if(!(child.getChildAt(0)instanceof Button)){
        	mWorkspace.addScreen(0);
        	cellLayout = (CellLayout)mWorkspace.getChildAt(0);
            child = (ShortcutAndWidgetContainer)cellLayout.getChildAt(0);
            ShortcutAndWidgetContainer cellLayoutChildren = (ShortcutAndWidgetContainer)cellLayout. getChildAt(0);
    		cellLayoutChildren.addButtonDrawable(R.drawable.homescreen_add_screen_selector, Workspace.ADD_BUTTON_ID, this, this);
    		child.setVisibility(View.INVISIBLE);
        }*/
        //end
        mWorkspace.restoreInstanceStateForRemainingPages();
        
        // If we received the result of any pending adds while the loader was running (e.g. the
        // widget configuration forced an orientation change), process them now.
        for (int i = 0; i < sPendingAddList.size(); i++) {
            completeAdd(sPendingAddList.get(i));
        }
        sPendingAddList.clear();

        // Update the market app icon as necessary (the other icons will be managed in response to
        // package changes in bindSearchablesChanged()
        updateAppMarketIcon();

        // Animate up any icons as necessary
        if (mVisible || mWorkspaceLoading) {
            Runnable newAppsRunnable = new Runnable() {
                @Override
                public void run() {
                    runNewAppsAnimation(false);
                }
            };

            boolean willSnapPage = mNewShortcutAnimatePage > -1 &&
                    mNewShortcutAnimatePage != mWorkspace.getCurrentPage();
            if (canRunNewAppsAnimation()) {
                // If the user has not interacted recently, then either snap to the new page to show
                // the new-apps animation or just run them if they are to appear on the current page
                if (willSnapPage) {
                    mWorkspace.snapToPage(mNewShortcutAnimatePage, newAppsRunnable);
                } else {
                    runNewAppsAnimation(false);
                }
            } else {
                // If the user has interacted recently, then just add the items in place if they
                // are on another page (or just normally if they are added to the current page)
                runNewAppsAnimation(willSnapPage);
            }
        }

        mWorkspaceLoading = false;

        /// M: If unread information load completed, we need to bind it to workspace.        
        if (mUnreadLoadCompleted) {
            bindWorkspaceUnreadInfo();
        }
        mBindingWorkspaceFinished = true;
        
        // M: Added by liudekuan on 20131028
        // R: Load apps which installed in scard but have no info store in db.
//        mModel.loadDelayedApps(this);
        // M: End
        
      
        initDisplayWidthHeight();
        mWallGaussBitmap = getGaussWallBitmap(Launcher.this);
        initGaussViewBitmap();
        initWorkSpaceView();
    }

    private boolean canRunNewAppsAnimation() {
        long diff = System.currentTimeMillis() - mDragController.getLastGestureUpTime();
        return diff > (NEW_APPS_ANIMATION_INACTIVE_TIMEOUT_SECONDS * 1000);
    }

    /**
     * Runs a new animation that scales up icons that were added while Launcher was in the
     * background.
     *
     * @param immediate whether to run the animation or show the results immediately
     */
    private void runNewAppsAnimation(boolean immediate) {
        AnimatorSet anim = LauncherAnimUtils.createAnimatorSet();
        Collection<Animator> bounceAnims = new ArrayList<Animator>();

        // Order these new views spatially so that they animate in order
        Collections.sort(mNewShortcutAnimateViews, new Comparator<View>() {
            @Override
            public int compare(View a, View b) {
                CellLayout.LayoutParams alp = (CellLayout.LayoutParams) a.getLayoutParams();
                CellLayout.LayoutParams blp = (CellLayout.LayoutParams) b.getLayoutParams();
                int cellCountX = LauncherModel.getCellCountX();
                return (alp.cellY * cellCountX + alp.cellX) - (blp.cellY * cellCountX + blp.cellX);
            }
        });

        // Animate each of the views in place (or show them immediately if requested)
        if (immediate) {
            for (View v : mNewShortcutAnimateViews) {
                v.setAlpha(1f);
                v.setScaleX(1f);
                v.setScaleY(1f);
            }
        } else {
            for (int i = 0; i < mNewShortcutAnimateViews.size(); ++i) {
                View v = mNewShortcutAnimateViews.get(i);
                ValueAnimator bounceAnim = LauncherAnimUtils.ofPropertyValuesHolder(v,
                        PropertyValuesHolder.ofFloat("alpha", 1f),
                        PropertyValuesHolder.ofFloat("scaleX", 1f),
                        PropertyValuesHolder.ofFloat("scaleY", 1f));
                bounceAnim.setDuration(InstallShortcutReceiver.NEW_SHORTCUT_BOUNCE_DURATION);
                bounceAnim.setStartDelay(i * InstallShortcutReceiver.NEW_SHORTCUT_STAGGER_DELAY);
                bounceAnim.setInterpolator(new SmoothPagedView.OvershootInterpolator());
                bounceAnims.add(bounceAnim);
            }
            anim.playTogether(bounceAnims);
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mWorkspace != null) {
                        mWorkspace.postDelayed(mBuildLayersRunnable, 500);
                    }
                }
            });
            anim.start();
        }

        // Clean up
        mNewShortcutAnimatePage = -1;
        mNewShortcutAnimateViews.clear();
        new Thread("clearNewAppsThread") {
            public void run() {
                mSharedPrefs.edit()
                            .putInt(InstallShortcutReceiver.NEW_APPS_PAGE_KEY, -1)
                            .putStringSet(InstallShortcutReceiver.NEW_APPS_LIST_KEY, null)
                            .commit();
            }
        }.start();
    }
//deleted by ETON guolinan
  /*  @Override
    public void bindSearchablesChanged() {
    	
        boolean searchVisible = updateGlobalSearchIcon();
        boolean voiceVisible = updateVoiceSearchIcon(searchVisible);
       
        if (mSearchDropTargetBar != null) {
            mSearchDropTargetBar.onSearchPackagesChanged(searchVisible, voiceVisible);
        }
    }*/
 //end
    /**
     * Add the icons for all apps.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindAllApplications(final ArrayList<ApplicationInfo> apps) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "bindAllApplications: apps = " + apps);
        }
        
    	Runnable setAllAppsRunnable = new Runnable() {
            public void run() {
                if (mAppsCustomizeContent != null) {
                    mAppsCustomizeContent.setApps(apps);
                }
            }
        };

        /// M: If unread information load completed, we need to update information in app list.
        if (mUnreadLoadCompleted) {
            AppsCustomizePagedView.updateUnreadNumInAppInfo(apps);
        }
        // Remove the progress bar entirely; we could also make it GONE
        // but better to remove it since we know it's not going to be used
        View progressBar = mAppsCustomizeTabHost.
            findViewById(R.id.apps_customize_progress_bar);
        if (progressBar != null) {
            ((ViewGroup)progressBar.getParent()).removeView(progressBar);

            // We just post the call to setApps so the user sees the progress bar
            // disappear-- otherwise, it just looks like the progress bar froze
            // which doesn't look great
            mAppsCustomizeTabHost.post(setAllAppsRunnable);
        } else {
            // If we did not initialize the spinner in onCreate, then we can directly set the
            // list of applications without waiting for any progress bars views to be hidden.
            setAllAppsRunnable.run();
        }
        mBindingAppsFinished = true;
    }

    /**
     * A package was installed.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindAppsAdded(ArrayList<ApplicationInfo> apps) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "bindAppsAdded: apps = " + apps);
        }
        
        // M: Modified by liudekuan
  	    //mModel.startLoader(false, -1);
        //setLoadOnResume();
        for (int i = apps.size() - 1; i >= 0; i --) {
        	ApplicationInfo app = apps.get(i);
        	
        	// For apps in folder, we just add them to folder
        	if (app.container != LauncherSettings.Favorites.CONTAINER_DESKTOP
        			&& app.container != LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
        		FolderInfo fi = LauncherModel.sBgFolders.get(app.container);
        		if (fi != null) {
        			fi.add(app);
        			apps.remove(i);
        			continue;
        		}
        	}
        	// For apps in desktop, we should display them on desktop.
        	View shortcut = createShortcut(app);
        	mWorkspace.addInScreen(shortcut, app.container, LauncherModel.getActualScreen(app.screen, app.container), 
        			app.cellX, app.cellY, 1, 1, false);
        	
        	//M: added by chenxin
        	//not go to the added new screen when insatall app
        	final int actual = LauncherModel.getActualScreen(app.screen, app.container);
        	/// M: Added by liudekuan
        	if (actual >= mWorkspace.getChildCount()) {
        		return;
        	}
        	/// M: End
        	Runnable runnable = new Runnable(){

				@Override
				public void run() {
					// TODO Auto-generated method stub
					mWorkspace.snapToPage(actual);
		        	mWorkspace.getChildAt(actual).requestFocus();
				}
        		
        	};
        	mWorkspace.postDelayed(runnable, 350);
        	//end
        	if (mWorkspace.getScreenEditState()) {
        		if (LauncherLog.DEBUG) {
                    LauncherLog.d(TAG, "bindAppsAdded: screen = " + LauncherModel.getActualScreen(app.screen, app.container));
                }
        		removeDeleteDrawable(LauncherModel.getActualScreen(app.screen, app.container));
        	}
        }
        // M: End
        
        if (mAppsCustomizeContent != null) {
            mAppsCustomizeContent.addApps(apps);
        }
        
        removeOldGaussBitmap();

        LauncherModel.setAppAdding(false);
    }
    /**
     * M: Added by liudekuan 
     * @param whichPage
     */
    public void removeDeleteDrawable(int whichPage) {
		CellLayout cl = (CellLayout) mWorkspace.getChildAt(whichPage);
		ShortcutAndWidgetContainer clChildren = (ShortcutAndWidgetContainer) cl
				.getChildAt(0);
		if (clChildren.getChildCount() != 0) {
			cl.deleteButtonDrawable(Workspace.DELETE_BUTTON_ID);
		}
	}

    /**
     * A package was updated.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindAppsUpdated(ArrayList<ApplicationInfo> apps) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "bindAppsUpdated: apps = " + apps);
        }
        // M: Added by liudekuan
        if (null == apps || apps.size() == 0) {
        	return;
        }
        // M: End
        
        if (mWorkspace != null) {
//        	mWorkspace.updateShortcutsInFolder(apps);
            mWorkspace.updateShortcuts(apps);
//            mWorkspace.removeInvalidIcons();
        }

        if (mAppsCustomizeContent != null) {
            mAppsCustomizeContent.updateApps(apps);
        }
        
        removeOldGaussBitmap();
    }
    /**
     * A shortcut was added.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     * @author Administrator liudekuan
     */
    public void bindShortcutAdded(ShortcutInfo info) {
    	
    	if (null == info) {
    		return;
    	}
    	
    	if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "bindShortcutAdded: info = " + info.intent);
        }
    	
		int actualScreen = LauncherModel.getActualScreen(info.screen, info.container);

		CellLayout layout = (CellLayout) mWorkspace.getChildAt(actualScreen);
		if (layout != null) {
			if (!layout.isOccupied(info.cellX, info.cellY)) {
				MTKShortcut shortcut = (MTKShortcut) createShortcut(info);
				mWorkspace.addInScreen(shortcut, info.container, actualScreen,
						info.cellX, info.cellY, info.spanX, info.spanY);
			}
		}
		InstallShortcutHelper.setInstallingShortcut(false);
    }
    
    /**
     * A package was uninstalled.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindAppsRemoved(ArrayList<String> packageNames, boolean permanent) {
    	
    	//M: added by chenxin
    	LauncherApplication app = ((LauncherApplication)getApplication());
    	ScreenEditUtil se = app.getScreenEditUtil();
    	
    	if (mWorkspace.getScreenEditState() && (ScreenEditUtil.editMenuChoose == ScreenEditUtil.MENU_ID_WIDGET)){
    		se.refreshEditMenuData();
    	}
    	//end
    	
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "bindAppsRemoved: packageNames = " + packageNames + ", permanent = " + permanent);
        }     
   
        if (permanent) {
            mWorkspace.removeItems(packageNames);
        }

        if (mAppsCustomizeContent != null) {
            mAppsCustomizeContent.removeApps(packageNames);
        }

        // Notify the drag controller
        mDragController.onAppsRemoved(packageNames, this);
        
        removeOldGaussBitmap();
    }

    /**
     * A number of packages were updated.
     */
    public void bindPackagesUpdated() {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "bindPackagesUpdated.");
        }

        if (mAppsCustomizeContent != null) {
            mAppsCustomizeContent.onPackagesUpdated();
        }
    }

    private int mapConfigurationOriActivityInfoOri(int configOri) {
        final Display d = getWindowManager().getDefaultDisplay();
        int naturalOri = Configuration.ORIENTATION_LANDSCAPE;
        switch (d.getRotation()) {
        case Surface.ROTATION_0:
        case Surface.ROTATION_180:
            // We are currently in the same basic orientation as the natural orientation
            naturalOri = configOri;
            break;
        case Surface.ROTATION_90:
        case Surface.ROTATION_270:
            // We are currently in the other basic orientation to the natural orientation
            naturalOri = (configOri == Configuration.ORIENTATION_LANDSCAPE) ?
                    Configuration.ORIENTATION_PORTRAIT : Configuration.ORIENTATION_LANDSCAPE;
            break;
        }

        int[] oriMap = {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT,
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
        };
        // Since the map starts at portrait, we need to offset if this device's natural orientation
        // is landscape.
        int indexOffset = 0;
        if (naturalOri == Configuration.ORIENTATION_LANDSCAPE) {
            indexOffset = 1;
        }
        return oriMap[(d.getRotation() + indexOffset) % 4];
    }

    public boolean isRotationEnabled() {
        boolean enableRotation = sForceEnableRotation ||
                getResources().getBoolean(R.bool.allow_rotation);
        return enableRotation;
    }
    public void lockScreenOrientation() {
        if (isRotationEnabled()) {
            setRequestedOrientation(mapConfigurationOriActivityInfoOri(getResources()
                    .getConfiguration().orientation));
        }
    }
    public void unlockScreenOrientation(boolean immediate) {
        if (isRotationEnabled()) {
            if (immediate) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            } else {
                mHandler.postDelayed(new Runnable() {
                    public void run() {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    }
                }, mRestoreScreenOrientationDelay);
            }
        }
    }

    /* Cling related */
    private boolean isClingsEnabled() {
        // disable clings when running in a test harness
        if(ActivityManager.isRunningInTestHarness()) return false;

        return true;
    }

    private Cling initCling(int clingId, int[] positionData, boolean animate, int delay) {
        final Cling cling = (Cling) findViewById(clingId);
        if (cling != null) {
            cling.init(this, positionData);
            cling.setVisibility(View.VISIBLE);
            cling.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            if (animate) {
                cling.buildLayer();
                cling.setAlpha(0f);
                cling.animate()
                    .alpha(1f)
                    .setInterpolator(new AccelerateInterpolator())
                    .setDuration(SHOW_CLING_DURATION)
                    .setStartDelay(delay)
                    .start();
            } else {
                cling.setAlpha(1f);
            }
            cling.setFocusableInTouchMode(true);
            cling.post(new Runnable() {
                public void run() {
                    cling.setFocusable(true);
                    cling.requestFocus();
                }
            });
            mHideFromAccessibilityHelper.setImportantForAccessibilityToNo(
                    mDragLayer, false);
            //modified by ETON guolinan
//            mDragLayer, clingId == R.id.all_apps_cling);
            //end
        }
        return cling;
    }

    private void dismissCling(final Cling cling, final String flag, int duration) {
        // To catch cases where siblings of top-level views are made invisible, just check whether
        // the cling is directly set to GONE before dismissing it.
        if (cling != null && cling.getVisibility() != View.GONE) {
            ObjectAnimator anim = LauncherAnimUtils.ofFloat(cling, "alpha", 0f);
            anim.setDuration(duration);
            anim.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    cling.setVisibility(View.GONE);
                    cling.cleanup();
                    // We should update the shared preferences on a background thread
                    new Thread("dismissClingThread") {
                        public void run() {
                            SharedPreferences.Editor editor = mSharedPrefs.edit();
                            editor.putBoolean(flag, true);
                            editor.commit();
                        }
                    }.start();
                };
            });
            anim.start();
            mHideFromAccessibilityHelper.restoreImportantForAccessibility(mDragLayer);
        }
    }

    private void removeCling(int id) {
        final View cling = findViewById(id);
        if (cling != null) {
            final ViewGroup parent = (ViewGroup) cling.getParent();
            parent.post(new Runnable() {
                @Override
                public void run() {
                    parent.removeView(cling);
                }
            });
            mHideFromAccessibilityHelper.restoreImportantForAccessibility(mDragLayer);
        }
    }
//deleted by ETON guolinan
 /*   private boolean skipCustomClingIfNoAccounts() {
        Cling cling = (Cling) findViewById(R.id.workspace_cling);
        boolean customCling = cling.getDrawIdentifier().equals("workspace_custom");
        if (customCling) {
            AccountManager am = AccountManager.get(this);
            Account[] accounts = am.getAccountsByType("com.google");
            return accounts.length == 0;
        }
        return false;
    }

    public void showFirstRunWorkspaceCling() {
        // Enable the clings only if they have not been dismissed before
        if (isClingsEnabled() &&
                !mSharedPrefs.getBoolean(Cling.WORKSPACE_CLING_DISMISSED_KEY, false) &&
                !skipCustomClingIfNoAccounts()) {
            /// M: modified for theme feature, set different workspace cling color through different themes.
            Cling workspaceCling = initCling(R.id.workspace_cling, null, false, 0);
            setClingTitleWithThemeColor(workspaceCling, R.id.workspace_cling_title);
        } else {
            removeCling(R.id.workspace_cling);
        }
    }

    public void showFirstRunAllAppsCling(int[] position) {
        // Enable the clings only if they have not been dismissed before
        if (isClingsEnabled() &&
                !mSharedPrefs.getBoolean(Cling.ALLAPPS_CLING_DISMISSED_KEY, false)) {
            /// M: modified for theme feature, set different all apps cling color through different themes.
            Cling appsCling = initCling(R.id.all_apps_cling, position, true, 0);
            setClingTitleWithThemeColor(appsCling, R.id.all_apps_cling_title);
        } else {
            removeCling(R.id.all_apps_cling);
        }
    }*/
  //end
    public Cling showFirstRunFoldersCling() {
        // Enable the clings only if they have not been dismissed before
        /// M: modified for theme feature, set different folder cling color through different themes.
        Cling cling = null;
        if (isClingsEnabled() &&
                !mSharedPrefs.getBoolean(Cling.FOLDER_CLING_DISMISSED_KEY, false)) {
            cling = initCling(R.id.folder_cling, null, true, 0);
            setClingTitleWithThemeColor(cling, R.id.folder_cling_title);
        } else {
            removeCling(R.id.folder_cling);
        }
        return cling;
    }

    public boolean isFolderClingVisible() {
        Cling cling = (Cling) findViewById(R.id.folder_cling);
        if (cling != null) {
            return cling.getVisibility() == View.VISIBLE;
        }
        return false;
    }
  //deleted by ETON guolinan
    /* 
    public void dismissWorkspaceCling(View v) {
        Cling cling = (Cling) findViewById(R.id.workspace_cling);
        dismissCling(cling, Cling.WORKSPACE_CLING_DISMISSED_KEY, DISMISS_CLING_DURATION);
    }
    
  	public void dismissAllAppsCling(View v) {
        Cling cling = (Cling) findViewById(R.id.all_apps_cling);
        dismissCling(cling, Cling.ALLAPPS_CLING_DISMISSED_KEY, DISMISS_CLING_DURATION);
    }*/
//end
    public void dismissFolderCling(View v) {
        Cling cling = (Cling) findViewById(R.id.folder_cling);
        dismissCling(cling, Cling.FOLDER_CLING_DISMISSED_KEY, DISMISS_CLING_DURATION);
    }

    /**
     * Prints out out state for debugging.
     */
    public void dumpState() {
        Log.d(TAG, "BEGIN launcher2 dump state for launcher " + this);
        Log.d(TAG, "mSavedState=" + mSavedState);
        Log.d(TAG, "mWorkspaceLoading=" + mWorkspaceLoading);
        Log.d(TAG, "mRestoring=" + mRestoring);
        Log.d(TAG, "mWaitingForResult=" + mWaitingForResult);
        Log.d(TAG, "mSavedInstanceState=" + mSavedInstanceState);
        Log.d(TAG, "sFolders.size=" + sFolders.size());
        mModel.dumpState();

        if (mAppsCustomizeContent != null) {
            mAppsCustomizeContent.dumpState();
        }
        Log.d(TAG, "END launcher2 dump state");
    }

    @Override
    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        super.dump(prefix, fd, writer, args);
        writer.println(" ");
        writer.println("Debug logs: ");
        for (int i = 0; i < sDumpLogs.size(); i++) {
            writer.println("  " + sDumpLogs.get(i));
        }
    }

    public static void dumpDebugLogsToConsole() {
        Log.d(TAG, "");
        Log.d(TAG, "*********************");
        Log.d(TAG, "Launcher debug logs: ");
        for (int i = 0; i < sDumpLogs.size(); i++) {
            Log.d(TAG, "  " + sDumpLogs.get(i));
        }
        Log.d(TAG, "*********************");
        Log.d(TAG, "");
    }

    /**
     * M: Change cling color while theme changed.
     *
     * @param cling The cling will to be set color.
     * @param id The title of the cling.
     */
    private void setClingTitleWithThemeColor(final View cling, int id) {
        if (cling != null) {
            final TextView titleView = (TextView) cling.findViewById(id);
            if (titleView != null) {
                titleView.setTextColor(getThemeColor(getResources(), R.color.cling_title_text_color));
            }
        }
    }

    /**
     * M: Get theme main color.
     *
     * @param res resources object.
     * @param id the default color resource id.
     * @return theme main color, if non, return the default color from given id.
     */   
    public static int getThemeColor(Resources res, int id) {
        int color = 0;
        /*if (FeatureOption.MTK_THEMEMANAGER_APP) {
            color = res.getThemeMainColor();
        }*/
        if (color == 0) {
            color = res.getColor(id);
        }
        return color;
    }
    
    /**
     * M: Get current CellLayout bounds.
     * 
     * @return mCurrentBounds.
     */
    Rect getCurrentBounds() {
        return mCurrentBounds;
    }

    /**
     * M: Register OrientationListerner when onCreate.
     */
    private void registerOrientationListener() {
        mOrientationListener = new OrientationEventListener(Launcher.this) {
            @Override
            public void onOrientationChanged(int orientation) {
                orientation = roundOrientation(orientation);
                if (orientation != mLastOrientation) {
                    if (mLastOrientation == Launcher.ORIENTATION_0 || mLastOrientation == Launcher.ORIENTATION_180) {
                        if (orientation == Launcher.ORIENTATION_270 || orientation == Launcher.ORIENTATION_90) {
                            boolean isRotateEnabled = Settings.System.getInt(getContentResolver(),
                                    Settings.System.ACCELEROMETER_ROTATION, 1) != 0;
                            if (isRotateEnabled) {
                                String cmpName = null;
                                if (cmpName != null && !cmpName.equals("none")) {
                                    fireAppRotated(cmpName);
                                }
                            }
                        }
                    }
                    mLastOrientation = orientation;
                }
            }
        };
        final String cmpName =null;// Settings.System.getString(getContentResolver(), Settings.System.LANDSCAPE_LAUNCHER);
        if (cmpName != null && !cmpName.equals("none")) {
            mOrientationListener.enable();
        }
    }
    
    /**
     * M: Calculate orientation.
     *
     * @param orientation
     * @return
     */
    private int roundOrientation(int orientation) {
        return ((orientation + 45) / 90 * 90) % 360;
    }
    
    /**
     * M: Launch the specified app with name of "cmpName" and intent action is intent.ACTION_ROTATED_MAIN.
     *
     * @param cmpName
     */
    private void fireAppRotated(String cmpName) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "fireAppRotated: cmpName = " + cmpName);
        }

        String name[] = cmpName.split("/");
        /*Intent intent = new Intent(Intent.ACTION_ROTATED_MAIN);
        intent.setComponent(new ComponentName(name[0], name[1]));
        startActivitySafely(null, intent, null);*/
    }
    
    /**
     * M: Enable OrientationListener when onResume.
     */
    private void enableOrientationListener() {
        /*final String cmpName = Settings.System.getString(getContentResolver(), Settings.System.LANDSCAPE_LAUNCHER);
        if (cmpName != null && !cmpName.equals("none")) {
            if (mOrientationListener.canDetectOrientation()) {
                mOrientationListener.enable();
                mLastOrientation = Launcher.ORIENTATION_270;
            } else {
                Toast.makeText(this, R.string.orientation, Toast.LENGTH_SHORT).show();
            }
        }*/
    }

    /**
     * M: Disable OrientationListener when onPause/onDestory.
     */
    private void disableOrientationListener() {
        /*final String cmpName = Settings.System.getString(getContentResolver(), Settings.System.LANDSCAPE_LAUNCHER);
        if (cmpName != null && !cmpName.equals("none")) {
            mLastOrientation = Launcher.ORIENTATION_0;
            mOrientationListener.disable();
        }*/
    }

    /**
     * M: scene switched, reset views and states for UI update.

    /**
     * M: Get current scene.
     * 
     * @return the name of current scene.
     */
    public static String getCurrentScene() {
        return sCurrentScene;
    }
    /**
     * @param scene the name of the scene will be switched to.
     */
    public void switchScene() {
        for (int i = 0; i < SCREEN_COUNT; i++) {
            final CellLayout cellLayout = (CellLayout) mWorkspace.getChildAt(i);
            cellLayout.removeAllViews();
        }

        // wipe any previous widgets
        final Context context = getApplicationContext();
        AppWidgetHost appWidgetHost = new AppWidgetHost(context, Launcher.APPWIDGET_HOST_ID);
        appWidgetHost.deleteHost();
        final ContentResolver resolver = context.getContentResolver();
        resolver.notifyChange(LauncherProvider.CONTENT_APPWIDGET_RESET_URI, null);

        DragController dragController = mDragController;
        dragController.resetDropTarget();
        // The order here is bottom to top.
        dragController.addDropTarget(mWorkspace);
        if (mSearchDropTargetBar != null) {
            mSearchDropTargetBar.setup(this, dragController);
        }
    }

    /**
     * M: Switch scene to the given scene.
     * 
     * @param scene The name of the scene will be switched to.
     */
  /*  private void switchScene(String scene, int pos) {
        if (LauncherModel.exists(this, scene)) {
            LauncherModel.resetScene(this, scene);
        }
        LauncherProvider.DatabaseHelper dbHelper = LauncherProvider.getOpenHelper();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        dbHelper.loadFavorites(db, getSceneDefinedIdBySceneName(scene));
        dbHelper.loadScene(db, getSceneDefinedIdBySceneName(scene));
        dbHelper.close();

        if (!sCurrentScene.equals(scene)) {
            saveSceneSetting(this, scene, pos);
        }

        sCurrentWallpaper = getSceneWallpaper(sCurrentScene);
        mWorkspace.setWallpaper(sCurrentWallpaper);
        mModel.forceReload();
    }*/

    private String getSceneWallpaper(String scene) {
        return SCENE_WALLPAPER.get(scene);
    }

    /*private int getSceneDefinedIdBySceneName(String sceneName) {
        int loc = 0;
        int count = LauncherModel.SYSTEM_SCENE_NAMES.length;
        for (int i = 0; i < count; i++) {
            if (LauncherModel.SYSTEM_SCENE_NAMES[i].equals(sceneName)) {
                loc = i;
                break;
            }
        }
        return LauncherProvider.DatabaseHelper.PRESCENES[loc];
    }*/

    /**
     * M: Bind component unread information in workspace and all apps list.
     *
     * @param component the component name of the app.
     * @param unreadNum the number of the unread message.
     */
    public void bindComponentUnreadChanged(final ComponentName component, final int unreadNum) {
        if (LauncherLog.DEBUG_UNREAD) {
            LauncherLog.d(TAG, "bindComponentUnreadChanged: component = " + component
                    + ", unreadNum = " + unreadNum + ", this = " + this);
        }
        // Post to message queue to avoid possible ANR.
        mHandler.post(new Runnable() {
            public void run() {
                final long start = System.currentTimeMillis();
                if (LauncherLog.DEBUG_PERFORMANCE) {
                    LauncherLog.d(TAG, "bindComponentUnreadChanged begin: component = " + component
                            + ", unreadNum = " + unreadNum + ", start = " + start);
                }
                if (mWorkspace != null) {
                    mWorkspace.updateComponentUnreadChanged(component, unreadNum);
                }

                if (mAppsCustomizeContent != null) {
                    mAppsCustomizeContent.updateAppsUnreadChanged(component, unreadNum);
                }
                if (LauncherLog.DEBUG_PERFORMANCE) {
                    LauncherLog.d(TAG, "bindComponentUnreadChanged end: current time = "
                            + System.currentTimeMillis() + ", time used = "
                            + (System.currentTimeMillis() - start));
                }
            }
        });
    }
    
   /**
     * M: Bind shortcuts unread number if binding process has finished.
     */
    public void bindUnreadInfoIfNeeded() {
        if (LauncherLog.DEBUG_UNREAD) {
            LauncherLog.d(TAG, "bindUnreadInfoIfNeeded: mBindingWorkspaceFinished = "
                    + mBindingWorkspaceFinished + ", thread = " + Thread.currentThread());
        }
        
        // M: Removed by liudekuan
        // R: Following codes look like unuse, because unread info will be load 
        //    bind after finishing bindItems in finishBindItems
//        if (mBindingWorkspaceFinished) {
////            bindWorkspaceUnreadInfo();
//        }
//
//        if (mBindingAppsFinished) {
////            bindAppsUnreadInfo();
//        }
        // M: End
        
        // mUnreadLoadCompleted is important, if it is true, unread info could be bind
        mUnreadLoadCompleted = true;
    }
    
    /**
     * M: Bind unread number to shortcuts with data in MTKUnreadLoader.
     */
    private void bindWorkspaceUnreadInfo() {
        mHandler.post(new Runnable() {
            public void run() {
                final long start = System.currentTimeMillis();
                if (LauncherLog.DEBUG_PERFORMANCE) {
                    LauncherLog.d(TAG, "bindWorkspaceUnreadInfo begin: start = " + start);
                }
                if (mWorkspace != null) {
                    mWorkspace.updateShortcutsAndFoldersUnread();
                }
                if (LauncherLog.DEBUG_PERFORMANCE) {
                    LauncherLog.d(TAG, "bindWorkspaceUnreadInfo end: current time = "
                            + System.currentTimeMillis() + ",time used = "
                            + (System.currentTimeMillis() - start));
                }
            }
        });
    }
    
    /**
     * M: Bind unread number to shortcuts with data in MTKUnreadLoader.
     */
    private void bindAppsUnreadInfo() {
        mHandler.post(new Runnable() {
            public void run() {
                final long start = System.currentTimeMillis();
                if (LauncherLog.DEBUG_PERFORMANCE) {
                    LauncherLog.d(TAG, "bindAppsUnreadInfo begin: start = " + start);
                }
                if (mAppsCustomizeContent != null) {
                    mAppsCustomizeContent.updateAppsUnread();
                }
                if (LauncherLog.DEBUG_PERFORMANCE) {
                    LauncherLog.d(TAG, "bindAppsUnreadInfo end: current time = "
                            + System.currentTimeMillis() + ",time used = "
                            + (System.currentTimeMillis() - start));
                }
            }
        });
    }
    
    /**
     * M: Show long press widget to add message, avoid duplication of message.
     */
    public void showLongPressWidgetToAddMessage() {
        if (mLongPressWidgetToAddToast == null) {
            mLongPressWidgetToAddToast = Toast.makeText(getApplicationContext(), R.string.long_press_widget_to_add,
                    Toast.LENGTH_SHORT);
        } else {
            mLongPressWidgetToAddToast.setText(R.string.long_press_widget_to_add);
            mLongPressWidgetToAddToast.setDuration(Toast.LENGTH_SHORT);
        }
        mLongPressWidgetToAddToast.show();
    }

    /**
     * M: Cancel long press widget to add message when press back key.
     */
    private void cancelLongPressWidgetToAddMessage() {
        if (mLongPressWidgetToAddToast != null) {
            mLongPressWidgetToAddToast.cancel();
        }
    }
    
    /**
     * M: A widget was uninstalled/disabled.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindAppWidgetRemoved(ArrayList<String> appWidget, boolean permanent) {
    	
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "bindAppWidgetRemoved: appWidget = " + appWidget + ", permanent = " + permanent);
        }        
        if (permanent) {
            mWorkspace.removeItems(appWidget);
        }
    }
    
    /**
     * M: Add a screen to desktop, note: do not modify db.
     * Implementation of the method from LauncherModel.Callbacks
     * Added by liudekuan
     */
    public void bindScreenAdded (int screen,final boolean isAddApp) {
    	
    	if (LauncherLog.DEBUG) {
    	    LauncherLog.d(TAG, "isAppAdding = " + LauncherModel.isAppAdding());
    		LauncherLog.d(TAG, "bindScreenAdded: screen = " + screen);
    		int[] maps = LauncherModel.sBgScreenMap;
    		for (int i = 0; i < maps.length; i ++) {
    			LauncherLog.d(TAG, "bindScreenAdded: maps[" + i + "] = " + maps[i]);
    		}
    	}
    	
    	mWorkspace.addScreen(screen);
    	CellLayout layout = (CellLayout)mWorkspace.getChildAt(screen);
    	if (mWorkspace.getScreenEditState()) {
    		layout.setTranslationX(0);
    		layout.setTranslationY(-ScreenEditUtil.CELLLAYOUT_Y_TRANSLATION);
    		layout.setScaleX(0.85f);
    		layout.setScaleY(0.85f);
    		layout.setBackgroundAlpha(1.0f);
    		layout.setBackgroundAlphaMultiplier(1.0f);
    		layout.setAlpha(1.0f);
    		
    		if (screen == ADD_SCREEN_LEFT_POSITON) {
    			LauncherApplication.getDisplayFactory(this).setDefaultScreen(
    					this, 
    					LauncherApplication.getDisplayFactory(this).getDefaultScreen(this)+1);
    		}
			layout.addButtonDrawable(
					R.drawable.homescreen_delete_screen_selector,
					Workspace.DELETE_BUTTON_ID, this, this);
    	}
    	if(!isAddApp){
    		mWorkspace.setCurrentPage(screen);
    	}
		mWorkspace.mForceScreenScrolled = true;
    }
    
    /**
     * M: Remove a screen from desktop, note: do not modify db.
     * Implementation of the method from LauncherModel.Callbacks
     * Added by liudekuan
     */
    public void bindScreenRemoved (int screen) {
    	if (LauncherLog.DEBUG) {
    		LauncherLog.d(TAG, "bindScreenRemoved: screen = " + screen);
    		LauncherLog.d(TAG, "mAppAdding = " + LauncherModel.isAppAdding());
    	}
    	
    	CellLayout layout = (CellLayout) mWorkspace.getChildAt(screen);
    	mWorkspace.removeView(layout);
    	
    	int defaultScreen = LauncherApplication.getDisplayFactory(this).getDefaultScreen(this);
    	if ((screen - 1) < defaultScreen){
    		LauncherApplication.getDisplayFactory(this).setDefaultScreen(this, defaultScreen-1);
    	}else if ((screen - 1) == defaultScreen){
			LauncherApplication.getDisplayFactory(this).setDefaultScreen(this,
					getResources().getInteger(R.integer.config_defaultScreen));
    	}
    	
    	mIsCompleteDeteleScreen = true;
    	
		CellLayout lastCl = (CellLayout) mWorkspace
				.getChildAt(mWorkspace.mCurrentPage);
		lastCl.setRotationY(0);
		mWorkspace.setCurrentPage(getCurrentWorkspaceScreen());
    }
    
    /**
     * M: Enter edit mode in UI, that is to say: adding edit screens to desktop,
     * and set relavant states;
     * Implementation of the method from LauncherModel.Callbacks
     * Added by liudekuan
     */
    public void bindEditModeEntered (int type) {
    	
    	if (LauncherLog.DEBUG) {
    		LauncherLog.d(TAG, "bindEditModeEntered: type = " + type);
    	}
    	
    	if (!mWorkspace.getScreenEditState()) {
			closeAavIfNecessary();
			
			mScreenEditUtil.setWidgetsView(mAppsCustomizeContent);
			mScreenEditUtil.setWorkSpaceView(mWorkspace);
			mWorkspace.changeState();
			mDeleteZoneTest.setVisibility(View.VISIBLE);
			setScreenEditState(type);
			LauncherApplication.getDisplayFactory(this).setScreenCount(this, mWorkspace.getChildCount() - 2);
		}else{
			closeAavIfNecessary();
			setScreenEditState(type);
		}
    }
    
    /**
     * M: Exit edit mode in UI, that is to say: deleting edit screens from desktop,
     * and set relavant states;
     * Implementation of the method from LauncherModel.Callbacks
     * Added by liudekuan
     */
    public void bindEditModeExited () {
    	if (LauncherLog.DEBUG) {
    		LauncherLog.d(TAG, "bindEditModeExited");
    	}
    	if(mWorkspace.getScreenEditState()){
        	/// R: Remove edit screens 
            mWorkspace.removeViewAt(mWorkspace.getChildCount() - 1);
            mWorkspace.removeViewAt(0);
            exitSpringLoadedMode();
            //added by chenxin
            mWorkspace.setScreenEditState(false);
			//added end
			int stagger = getResources().getInteger(
					R.integer.config_appsCustomizeWorkspaceAnimationStagger);
            
            //M: Fixed the issue when at the right + screen, avoid beyond the limit of next screen value
            if ((getCurrentWorkspaceScreen()-1) != mWorkspace.getChildCount()){
            	mWorkspace.setNextPage(mWorkspace.getNextPage()-1);
            }else{
            	mWorkspace.setNextPage(getCurrentWorkspaceScreen() - 2);
            }
            
            if (getCurrentWorkspaceScreen() == mWorkspace.getChildCount()){
            	mWorkspace.setNextPage(getCurrentWorkspaceScreen() - 1);
            }
            
            mWorkspace.setCurrentPage(getCurrentWorkspaceScreen() - 1);
            mScrollingIndicator.setCurrentPage(getCurrentWorkspaceScreen());
            mWorkspace.getChangeStateAnimation(Workspace.State.NORMAL,false,stagger);
            
            //added by chenxin
            mWorkspace.startPageIndicatorAnimation(false);
            mWorkspace.hideOutlines();
            fadeInOutHotseat(true);
            fadeInOutScreenEdit(false);
            //end 
        }
    }
    
    /**
     * M: set orientation changed flag, this would make the apps customized pane
     * recreate views in certain condition.
     */
    public void notifyOrientationChanged() {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "notifyOrientationChanged: mOrientationChanged = "
                    + mOrientationChanged + ", mPaused = " + mPaused);
        }
        mOrientationChanged = true;
    }

    /**
     * M: tell Launcher that the pages in app customized pane were recreated.
     */
    void notifyPagesWereRecreated() {
        mPagesWereRecreated = true;
    }

    /**
     * M: reset re-sync apps pages flags.
     */
    private void resetReSyncFlags() {
        mOrientationChanged = false;
        mPagesWereRecreated = false;
    }

    /**
     * M: getVacantCell
     * cell[2]: screen; cell[0]: cellX; cell[1]: cellY 
     * added by liudekuan
     */
    public int[] getVacantCell() {
    	int count = mWorkspace.getChildCount();
        CellLayout layout = null;
        int[] cell = new int[3];
        // if there are one or more vacant cell, some one will be returned 
        for (int i = 0; i < count; i ++) {
            layout = (CellLayout) mWorkspace.getChildAt(i);
            if (layout.getVacantCell(cell, 1, 1)) {
                cell[2] = i;
                return cell;
            }
        }
        
        // if reached here, it means all screens are full, so we need create a new 
        // screen, add it to workspace, and return cell[0][0]
        long id = mModel.addScreenSync(this, count, LauncherSettings.ScreenMapping.NORMAL);
        
        if (id == Launcher.ERROR) {
        	return null;
        }
        
        mWorkspace.addScreen(count); 
        cell[0] = 0;
        cell[1] = 0;
        cell[2] = count;
        return cell;
    }
    
    //added by ETON guolinan
    public void closeQuickViewWorkspace(boolean isClose)
    {
    	if (!isClose)
    	{
    		mSearchDropTargetBar.findViewById(R.id.info_target_text).setVisibility(View.VISIBLE);
    		mWorkspace.showScrollingIndicator(true);
        	mWorkspace.setVisibility(View.VISIBLE);
    	}
    	else
    	{
    		mSearchDropTargetBar.findViewById(R.id.info_target_text).setVisibility(View.GONE);
    		mWorkspace.hideScrollingIndicator(true);
    		hideHotseat(true);
	    	mWorkspace.setVisibility(View.INVISIBLE);
	    	mWorkspace.resetChildProperty(true);
			mWorkspace.invalidate();
    	}
	}
    
    private void registerSharedPreferenceListener() {
    	mSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
			
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
					String key) {
				if (SharedPreferencesUtils.WORKSACE_EFFECT.equals(key)) {
					//M: by chenxin Not need setTransitionEffect,OnClick already set
					//mWorkspace.setTransitionEffect(SharedPreferencesUtils.getWorkspaceEffect(Launcher.this));
				} else if (SharedPreferencesUtils.ALL_APPLICATIONS_EFFECT.equals(key)) {
					final int effectIndex = SharedPreferencesUtils.getAllAppEffect(Launcher.this);
					mAppsCustomizeContent.setTransitionEffect(effectIndex);
//					mWidgetsCustomizeContent.setTransitionEffect(effectIndex);
				}
			}
		};
		SharedPreferencesUtils.getSharedPreferences(this).registerOnSharedPreferenceChangeListener(mSharedPreferenceChangeListener);
    }
    /**
     * add by eton lisidong:because showdialog() method is deprecated ,so use below
     */
	@SuppressLint("ValidFragment")
	public class CustomDialogFragment extends android.app.DialogFragment
			implements DialogInterface.OnClickListener {
		
		private AddAdapter mAdapter;

		public CustomDialogFragment() {
		}

		public Dialog onCreateDialog(Bundle savedInstanceState) {

			mAdapter = new AddAdapter(Launcher.this);

			final AlertDialog.Builder builder = new AlertDialog.Builder(
					Launcher.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
			builder.setTitle(getString(R.string.menu_item_add_item));
			builder.setAdapter(mAdapter, this);
			AlertDialog dialog = builder.create();

			return dialog;
		}

		@Override
		public void onCancel(DialogInterface dialog) {
			super.onCancel(dialog);
			mWaitingForResult = false;
			cleanup();
		}

		@Override
		public void onDismiss(DialogInterface dialog) {
			super.onDismiss(dialog);
			mWaitingForResult = false;
			cleanup();
		}

		private void cleanup() {
			try {
//				dismissDialog(DIALOG_CREATE_SHORTCUT);
			} catch (Exception e) {
				// An exception is thrown if the dialog is not visible, which is
				// fine
			}
		}

		/**
		 * Handle the action clicked in the "Add to home" dialog.
		 */
		public void onClick(DialogInterface dialog, int which) {
			cleanup();

			AddAdapter.ListItem item = (AddAdapter.ListItem) mAdapter
					.getItem(which);
			switch (item.actionTag) {
			case AddAdapter.ITEM_APPLICATION: {
				if (mAppsCustomizeTabHost != null) {
					mAppsCustomizeTabHost.selectAppsTab();
				}
				showAllApps(true);
				break;
			}
			case AddAdapter.ITEM_APPWIDGET: {
				if (mAppsCustomizeTabHost != null) {
					mAppsCustomizeTabHost.selectWidgetsTab();
				}
				showAllApps(true);
				break;
			}
		//deleted by ETON guolinan
		/*	case AddAdapter.ITEM_WALLPAPER: {
				startWallpaper();
				break;
			}*/
		//end			
			}
		}
		
		@Override
		public void onStart() {
			super.onStart();
			mWaitingForResult = true;
		}
	}
    
                //added by xin.chen 
                //mDeleteZoneTest.setVisibility(View.VISIBLE);
		public void fadeInOutScreenEdit(boolean in){
		  	 float fromAlpha = 0, toAlpha = 1;//透明ｿ  	 
		  	 float fromXDelta = 0, toXDelta = 0, fromYDelta=1, toYDelta = 1;//位移 
		  	 int   visibleHotseat =  View.VISIBLE; //Hotseat 的显示属性；
		  	 if (in){
	  			 fromAlpha = 0.0f;
	  			 toAlpha = 1.0f;
	  			 fromYDelta = mHotseat.getHeight();
	  			 toYDelta = 0;
	  			 visibleHotseat = View.VISIBLE;
		  	 }else {
		  			 fromAlpha = 1.0f;
		  			 toAlpha = 0.0f;
		  			 fromYDelta = 0;
		  			 toYDelta = mHotseat.getHeight();
		  			 visibleHotseat = View.INVISIBLE;
		  	 }
	  	 if(mHotseat != null){
		      	 AnimationSet mAnimationSet = new AnimationSet(true);
		      	 AlphaAnimation mAlphaAnimation = new AlphaAnimation(fromAlpha, toAlpha);
		      	 TranslateAnimation mTranslateAnimation =new TranslateAnimation(fromXDelta, toXDelta, fromYDelta, toYDelta); 	
		      	 mAnimationSet.addAnimation(mAlphaAnimation);
		      	 mAnimationSet.addAnimation(mTranslateAnimation);
		      	 mAnimationSet.setDuration(CLOSTING_OPENING_DURATION);
		      	  mAnimationSet.setAnimationListener(new AnimationListener(){

	                    @Override
	                    public void onAnimationEnd(Animation arg0)
	                    {
	                        // TODO Auto-generated method stub
	                        Workspace workspace = getWorkspace();
                            if(workspace.mDragEndPage != workspace.mDragStartPage && workspace.mDragEndPage != -1)
                            {
                                removeGaussBitmap(workspace.mDragStartPage);
                            }
	                        if(!mWorkspace.getScreenEditState())
	                        {
	                            getGaussBitmapByThread();
	                        }
	                        
	                    }

	                    @Override
	                    public void onAnimationRepeat(Animation arg0)
	                    {
	                        // TODO Auto-generated method stub
	                        
	                    }

	                    @Override
	                    public void onAnimationStart(Animation arg0)
	                    {
	                        // TODO Auto-generated method stub
	                        
	                    }
	                    
	                }
	                );
		      	 mDeleteZoneTest.setAlpha(1.0f);
		      	 mDeleteZoneTest.startAnimation(mAnimationSet);
		      	 mDeleteZoneTest.setVisibility(visibleHotseat);//设置显示属性可以解决hotseat的渐入后又闪动一下�?//		      	 launcher.getScrollingIndicator().setVisibility(visibleHotseat);
		  	 }
		 }
		//end
		
	//add by eton guolinan:Hotseat fading Out
		public void fadeInOutHotseat(boolean in){
		  	 float fromAlpha = 0, toAlpha = 1;//͸����
		  	 float fromXDelta = 0, toXDelta = 0, fromYDelta=1, toYDelta = 1;//λ�� 
		  	 int   visibleHotseat =  View.VISIBLE; //Hotseat ����ʾ���ԣ�
		  	 if (in){
	  			 fromAlpha = 0.0f;
	  			 toAlpha = 1.0f;
	  			 fromYDelta = mHotseat.getHeight();
	  			 toYDelta = 0;
	  			 visibleHotseat = View.VISIBLE;
		  	 }else {
		  			 fromAlpha = 1.0f;
		  			 toAlpha = 0.0f;
		  			 fromYDelta = 0;
		  			 toYDelta = mHotseat.getHeight();
		  			 visibleHotseat = View.INVISIBLE;
		  	 }
	  	 if(mHotseat != null){
		      	 AnimationSet mAnimationSet = new AnimationSet(true);
		      	 AlphaAnimation mAlphaAnimation = new AlphaAnimation(fromAlpha, toAlpha);
		      	 TranslateAnimation mTranslateAnimation =new TranslateAnimation(fromXDelta, toXDelta, fromYDelta, toYDelta); 	
		      	 mAnimationSet.addAnimation(mAlphaAnimation);
		      	 mAnimationSet.addAnimation(mTranslateAnimation);
		      	 mAnimationSet.setDuration(CLOSTING_OPENING_DURATION);            		
		      	 mHotseat.setAlpha(1.0f);
		      	 mHotseat.startAnimation(mAnimationSet);
		      	 mHotseat.setVisibility(visibleHotseat);//������ʾ���Կ��Խ��hotseat�Ľ����������һ�¡�?		      	 
		      	 
		      	 /// considering why? I don't know...
		      	 // launcher.getScrollingIndicator().setVisibility(visibleHotseat);
		  	 }
		 }
		//end
		
		
	//>>add by eton wanghenan		
	private int removeSectorView(){
        if (null != mSector) {
        	int mode = mSector.WidgetCurrentLocation;
            mDragLayer.removeView(mSector);
            mSector = null;
            return mode;
        }
        return SectorWidgetConfig.ERROR_MODE;
	}
	private void removeSectorViewAndShowAnimation(){
		if (null != mSector) {
			mSector.unpopSectorAndRemove();
		}
	}
	
	public boolean sectorIsExist(){
		if (null != mSector){
			return true;
		}else {
			return false;	
		}
		
	}
	
	
	private void addSectorView(int mode){
//		if (null != mSector)return;
		if (isUnavailableConditionForAddSectorView()) {
			return;
		}
		
    	mSector = SectorView.fromXml(this,mode);
        if (mSector.getParent() == null) {
            mDragLayer.addView(mSector);
        }
        mSector.buildLayer();
	}
	
	private boolean isUnavailableConditionForAddSectorView(){
//		if (null != mSector || mModel.loadDataing)return true;
		if (null != mSector ||mWorkspace.getScreenEditState())return true;
		return false;
	}
	
	
    private class SectorReceive extends BroadcastReceiver {
    	
        @Override
        public void onReceive(Context context, Intent intent){
        	 final String action = intent.getAction();
    		if (SectorView.ACTION_SECTORVIEW_SHOW.equals(action)) {
    			int mode = intent.getIntExtra(SectorWidgetConfig.MODE, SectorWidgetConfig.DIRECTION_LEFT_TOP);
    			addSectorView(mode);
//        			Toast.makeText(context, "Sector-add1", Toast.LENGTH_SHORT).show();
    		}else if (SectorView.ACTION_SECTORVIEW_REMOVE.equals(action)) {
    			removeSectorView();
//	    			Toast.makeText(context, "Sector-remove", Toast.LENGTH_SHORT).show();
    		}else if (SectorView.ACTION_SECTORVIEW_UPDATE.equals(action)) {
    				int mode = removeSectorView();
    				if (SectorWidgetConfig.ERROR_MODE == mode) return;
	    			addSectorView(mode);
//		    			Toast.makeText(context, "Sector-update1", Toast.LENGTH_SHORT).show();  				    		
    		}else if (SectorView.ACTION_SECTORVIEW_REMOVE_WITH_UNPOPANIMATION.equals(action)) {
    			removeSectorViewAndShowAnimation();
    		}
        }
    }
    
   private class DataLoadingReceiver extends BroadcastReceiver {
    	
        @Override
        public void onReceive(Context context, Intent intent){
        	 final String action = intent.getAction();
    		if (Launcher.ACTION_DATA_ISLOADING.equals(action)) {
    			if(null == mDataDialog)
    			{
    		        mDataDialog = new ProgressDialog(Launcher.this);
    		        mDataDialog.setMessage(Launcher.this.getResources().getString(R.string.launcher_data_loading));
    		        mDataDialog.setIndeterminate(false);
    		        mDataDialog.setCancelable(false);
    			}
    	        mDataDialog.show();
    	        
    		}else if (Launcher.ACTION_DATA_LOAD_FINISHED.equals(action)) {
				if(mDataDialog != null){
				   mDataDialog.dismiss();
				}		    		
    		}
    		// M: Added by liudekuan
    		else if (Launcher.ACTION_APPS_UPDATING.equals(action)) {
    			if (null == mAppsUpdateDialog) {
    				mAppsUpdateDialog = new ProgressDialog(Launcher.this);
    				mAppsUpdateDialog.setMessage(Launcher.this.getResources().getString(R.string.update_apps));
    				mAppsUpdateDialog.setIndeterminate(false);
    				mAppsUpdateDialog.setCancelable(false);
    			}
    			mAppsUpdateDialog.show();
    		} else if (Launcher.ACTION_APPS_UPDATING_FINISHED.equals(action)) {
    			if (mAppsUpdateDialog != null) {
    				mAppsUpdateDialog.dismiss();
    			}
    		} 
    		// M: End
    		// add by xyg
    		else if(Launcher.ACTION_APPS_ARRANGE_UPDATING.equals(action)){
    			for (int i = 0; i < mDragLayer.getChildCount(); i ++) {
    	    		View v = mDragLayer.getChildAt(i);
    	    		if (v instanceof AppsArrangeView) {
    	    			((AppsArrangeView)v).updateData();
    	    		}
    	    	}
    		}// end
        }
    }
    //<<end	by eton wanghenan	

    
    //added by ETON guolinan
    public boolean changeState(){
    	if (!mWorkspace.getScreenEditState()) {
			mBoolScreenEdit = true;
			mScreenEditUtil.setWidgetsView(mAppsCustomizeContent);
			mScreenEditUtil.setWorkSpaceView(mWorkspace);
			mWorkspace.changeState();
			mDeleteZoneTest.setVisibility(View.VISIBLE);
			mScreenEditUtil.updateMenuWallpaper();
			return true;
    	}else{
    		return false;
    	}
    }
    //end
    
	public void timingCleanWorkspace() {

		Intent intent =new Intent("com.android.launcher2.ALARM");
	    PendingIntent sender=
	        PendingIntent.getBroadcast(this, 0, intent, 0);
	    AlarmManager alarm=(AlarmManager)getSystemService(ALARM_SERVICE);
	    alarm.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000L, sender);
	    
	    SharedPreferences sp = SharedPreferencesUtils.getSharedPreferences(this);
	    SharedPreferences.Editor editor = sp.edit();
		editor.putLong("systemrealtime",System.currentTimeMillis());
		editor.putLong("elapsedtime",SystemClock.elapsedRealtime());
		editor.putLong("cumulativetime", 0);
   		editor.putBoolean("istimeover",false);
	    editor.commit();	    
//	    Toast.makeText(this, " start alarm 20s later", Toast.LENGTH_LONG).show();
		
	}
	
	/**
	 * Added by liudekuan
	 */
	public DeleteZone getDeleteZone () {
		return mDeleteZone;
	}
	
	public void cancelDraggingByPackageChanged() {
		mDragController.cancelDrag();
        mDragController.resetLastGestureUpTime();
	}
	
	/**
	 * M: Added by liudekuan
	 * The implementation of callbacks
	 */
	public void onAppsChanged (String action) {
		if (Intent.ACTION_PACKAGE_CHANGED.equals(action)
    			|| Intent.ACTION_PACKAGE_ADDED.equals(action)
    			|| Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE.equals(action)
    			|| Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
    		
			cancelDraggingByPackageChanged();
			closeAavIfNecessary();
			
			closeFolder();
    	}
	}
	
	 public Bitmap getWallpaperBitmap()
	    {
	        Bitmap bw = null;
	        long beginWallTime = System.currentTimeMillis();
	        WallpaperManager wallpaperManager = WallpaperManager.getInstance(Launcher.this);
	        
	        Drawable wallpaperDrawable = wallpaperManager.getDrawable();
	        if(wallpaperDrawable != null)
	        {
	            bw = ((BitmapDrawable)wallpaperDrawable).getBitmap();
	        }
	        long endWallTime = System.currentTimeMillis();
	        Log.i("Launcher_test", "==============  wallDrawable total spacetime =" +(endWallTime-beginWallTime));
	        
	        return bw;
	    }
	    
	    public Bitmap getGaussWallBitmap(Activity activity)
	    {
	        long beginTime = System.currentTimeMillis();
	        
	        if(mWidth <= 0 || mHeight <= 0 || mStatusBarHeight <= 0)
	        {
	            Log.e("Launcher_test", "========getWallpaperBitmap mWidth, mHeight or mStatusBarHeight <= 0"); 
                return null;
	        }
	        
	        Bitmap bw = getWallpaperBitmap();
	        if(bw == null)
	        {
	            Log.e("Launcher_test", "========getWallpaperBitmap return null"); 
	            return bw;
	        }
	        //ScreenShot.savePic(bw, "/sdcard/WW0.png");
	        int height = mHeight+mStatusBarHeight;	        
	        float scaleW =  0.25f;
	        float scaleH = 0.25f;
	        Matrix matrix = new Matrix();
	        matrix.postScale(scaleW, scaleH);
	        
	        Bitmap wallBitmap = Bitmap.createBitmap(bw, 0, 0, mWidth, height, matrix, true);
	        if(wallBitmap == null)
	        {
	            Log.w("Launcher_test", "========createBitmap wallBitmap return null"); 
	            return wallBitmap;
	        }
	      //ScreenShot.savePic(wallBitmap, "/sdcard/WW1.png");
	        
	        Bitmap smallWallGaosiBitmap = SectorUtils.BoxBlurFilter2(wallBitmap);
	        if(smallWallGaosiBitmap == null)
	        {
	            Log.w("Launcher_test", "========createBitmap smallWallGaosiBitmap return null"); 
	            return smallWallGaosiBitmap;
	        }
	       //ScreenShot.savePic(smallWallGaosiBitmap, "/sdcard/WW2.png");
	        
	        scaleW = (float)mWidth/(float)smallWallGaosiBitmap.getWidth();
	        scaleH = (float)bw.getHeight()/(float)smallWallGaosiBitmap.getHeight();
	        Matrix matrix2 = new Matrix();
	        matrix2.postScale(scaleW, scaleH);
	        
	        Bitmap wallGaussBitmapTemp = Bitmap.createBitmap(smallWallGaosiBitmap, 0, 0, smallWallGaosiBitmap.getWidth(), smallWallGaosiBitmap.getHeight(), matrix2, true);
	        mStatusGaussBitmap = Bitmap.createBitmap(wallGaussBitmapTemp, 0, 0, mWidth, mStatusBarHeight);
//	        Bitmap wallGaussBitmap = Bitmap.createBitmap(wallGaussBitmapTemp, 0, mStatusBarHeight, mWidth, wallGaussBitmapTemp.getHeight());
	        
//	        ScreenShot.savePic(wallGaussBitmapTemp, "/sdcard/WW3.png");
//	        ScreenShot.savePic(mStatusGaussBitmap, "/sdcard/WW4.png");
	        
	        long endTime = System.currentTimeMillis();
	        Log.i("Launcher_test", "=============getGaosiWallBitmap total spacetime =" +(endTime-beginTime));
	        return wallGaussBitmapTemp;
	    }
	    
	    private void initDisplayWidthHeight()
	    {
	        mWidth = Launcher.this.getWindowManager().getDefaultDisplay().getWidth();
            int height = Launcher.this.getWindowManager().getDefaultDisplay().getHeight();
            
            Rect frame = new Rect();
            Launcher.this.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
            mStatusBarHeight = frame.top;

            mHeight = height-mStatusBarHeight;
	    }
	    
	    private void initGaussViewBitmap()
	    {
	        int height = mHeight+mStatusBarHeight;
	        int[] colors= new int[mWidth*height];
	        for(int i = 0; i<height; i++){ 
	            for(int j = 0; j <mWidth; j++){
	                colors[i*mWidth+j]=0xFF000000; 
	            }
	        }
	        
	        mGaussViewBG = Bitmap.createBitmap(colors, mWidth, height, Bitmap.Config.RGB_565).copy(Bitmap.Config.RGB_565, true);
	        if(mGaussViewBG != null)
	        {
	            mCopyBitmap = Bitmap.createBitmap(mGaussViewBG);
	        }
	    }
	    
	    private void initWorkSpaceView()
	    {
	        FrameLayout frameLayout = (FrameLayout) this.getDragLayer().findViewById(R.id.main_content);
	        mWorkSpaceView = (View)frameLayout;
	    }


	    
	    public Bitmap getScreenBitmap(View view)
	    {
	        Bitmap b = null;
	        long beginTime = System.currentTimeMillis();
	       // View view = activity.getWindow().getDecorView();
	        try
	        {
	        view.setDrawingCacheEnabled(true);
	        view.buildDrawingCache();
	        
	        long beginGetBitmpaTime = System.currentTimeMillis();
	        Bitmap b1 = view.getDrawingCache();
	        if(b1 == null){
	            Log.e("Launcher_test", "getScreenBitmap: b1 is null");
	            return null;
	        }
	       
	        mGaussScreenIndex = getCurrentPage();
	       // ScreenShot.savePic(b1, "/sdcard/XX0.png");
	       
	        float scaleWidth = 0.25f;
	        float scaleHeight = 0.35f;
	        Matrix matrix = new Matrix();
	        matrix.postScale(scaleWidth, scaleHeight);
	        b = Bitmap.createBitmap(b1, 0, 0, b1.getWidth(), b1.getHeight(), matrix, true);
	        
	        view.destroyDrawingCache();
	        long endTime = System.currentTimeMillis();
	        Log.v("Launcher_test", "getScreenBitmap:  spaceTime ="+(endTime-beginTime)+" GetBitmpaTime="+(endTime-beginGetBitmpaTime));
	        }
	        catch(Exception e)
	        {
	            e.printStackTrace();
	        }
	        
	        return b;
	    }
	    
	    public Bitmap getEndGaussBitmap(View view)
	    {
	        Bitmap b = null;
	        long beginTime = System.currentTimeMillis();
	        mScreenBitmap = getScreenBitmap(view);
	        
	        if(mWallGaussBitmap != null && mCopyBitmap != null)
	        {             
	            //mFloadBitmap = ScreenShot.getGaussBitmap(Launcher.this, mCopyBitmap, mScreenBitmap);
	            b = ScreenShot.getGaussBitmap(Launcher.this, mCopyBitmap, mScreenBitmap);
	            long endTime = System.currentTimeMillis();
	            Log.i("Launcher_test", "=====getEndGaussBitmap: total spacetime =" +(endTime-beginTime));
	        }
	        
	        //mCopyBitmap = Bitmap.createBitmap(mGaussViewBG);
	        
	        return b;
	     }
	    
	    public int getCurrentPage()
	    {
	        return mWorkspace.getCurrentPage();
	    }
	    
	    public String getGaussBitmapKey(int screenPage)
	    {
	        return GAUSS_BITMAP+screenPage; //mWorkspace.getCurrentPage();
	    }
	    
	    public Bitmap getGaussBitmap(int screenPage)
	    {
	        String strKey = getGaussBitmapKey(screenPage);
	        
	        Bitmap b = mGaussBitmap.get(strKey);
        
	        return b;
	    }
	    
	    public void addGaussBitmap(Bitmap gaussBitmap, int screenPage)
	    {
	        removeGaussBitmap(screenPage);
	        String strKey = getGaussBitmapKey(screenPage);

	        mGaussBitmap.put(strKey, gaussBitmap);
	    }
	    
	    public void removeGaussBitmap(int screenPage)
	    {
	        String strKey = getGaussBitmapKey(screenPage);
	        
	        mGaussBitmap.remove(strKey);
	    }
	    
	    public void getGaussBitmapByThread()
	    {
	        mAllowGauss = true;
	       // mGaussBeginTime = System.currentTimeMillis();
            mGaussOver = false;
	        int page = getCurrentPage();
	        if(!hasFolder(page))
	        {
	            Log.i("Launcher_test", "getGaussBitmapByThread: no folder");
	            
	            if(getGaussBitmap(page) != null)
	            {
	                removeGaussBitmap(page);
	            }
	            
	            mGaussOver = true;
	            return;
	        }
	        
	        removeGaussBitmap(page);
	        if(getWorkspace().getScreenEditState())
            {
                Log.w("Launcher_test", "getGaussBitmapByThread: is Edit state");
                mGaussOver = true;
                return;
            }

	        //if(mGaussBitmapThread != null && mGaussBitmapThread.isAlive())
	        if(mGaussBitmapThread != null)
	        {
	            mGaussBitmapThread.interrupt();
	            mCopyBitmap = Bitmap.createBitmap(mGaussViewBG);
	        }
	        
	        mGaussBitmapThread = new GetGaussBitmapThread();
	        mGaussBitmapThread.start();
	    }
	    
	    private class GetGaussBitmapThread extends Thread
	    {

	        public GetGaussBitmapThread()
	        {
	        }
	        
	        public void run()
	        {
	           
	           long beginTime = System.currentTimeMillis();
	           Bitmap b = getEndGaussBitmap(mWorkSpaceView);
	           
	           if( b== null || mGaussScreenIndex == -1)
	           {
	              Log.w("Launcher_test", "GetGaussBitmapThread b is null, return");
	              mGaussOver = true;
	              return;
	           }
	           
	           addGaussBitmap(b, mGaussScreenIndex);     
               mGaussScreenIndex = -1;
	           mGaussOver = true;
	           long endTime = System.currentTimeMillis();
	           Log.i("Launcher_test", "====getGaussBitmapByThread: total spacetime =" +(endTime-mGaussBeginTime));
	           
	           //mCopyBitmap = Bitmap.createBitmap(mGaussViewBG);
	        }
	    }
	    
	    private boolean hasFolder (int screen) 
	    {  
	        boolean res = false;
	        if (LauncherLog.DEBUG) {
	            LauncherLog.d("Launcher_test", "hasFolder: screen = " + screen);
	        }
	        
	        HashMap<Long, FolderInfo> folders = LauncherModel.sBgFolders;
	        Iterator<Entry<Long, FolderInfo>> iter = folders.entrySet().iterator();
	        while (iter.hasNext()) {
	            Map.Entry<Long, FolderInfo> entry = (Map.Entry<Long, FolderInfo>) iter.next();
	            FolderInfo val = (FolderInfo) entry.getValue();
	            if (LauncherModel.getActualScreen(val.screen, val.container) == screen) {
	               res = true;
	            }
	        }
	        
	        Log.i("Launcher_test", "============== hasFolder return = " + res);
	        return res;
	    }
	    
	    public void removeOldGaussBitmap()
	    {
	        int size = LauncherModel.mModifiedApps.size();
	        if(size <= 0)
	        {
	            Log.i("Launcher_test", "removeOldGaussBitmap mModifiedApps is null");
	            return;
	        }
	        
	        for(int i =0; i<size; i++)
	        {
	            ApplicationInfo info = LauncherModel.mModifiedApps.get(i);   
	            int page = LauncherModel.getActualScreen(info.screen, info.container);
	            
	            removeGaussBitmap(page);   
	        } 
	        
	        LauncherModel.mModifiedApps.clear();
	        mCopyBitmap = Bitmap.createBitmap(mGaussViewBG);
	    }
	    
	    public void removeAllGaussBitmap()
	    {
	        int page = mWorkspace.getChildCount();
	        
	        for(int i=0; i<page; i++)
	        {
	            removeGaussBitmap(i);
	        }
	        mCopyBitmap = Bitmap.createBitmap(mGaussViewBG);
	    }
	    
	    private class WallpaperReceiver extends BroadcastReceiver
	    {
	
	        @Override
	        public void onReceive(Context arg0, Intent arg1)
	        {
	            // TODO Auto-generated method stub
	           mWallGaussBitmap = getGaussWallBitmap(Launcher.this);
	           
	           if(mGaussViewBG != null)
	           {
	               mCopyBitmap = Bitmap.createBitmap(mGaussViewBG);
	           }

	        }

	    }
}

interface LauncherTransitionable {
    View getContent();
    void onLauncherTransitionPrepare(Launcher l, boolean animated, boolean toWorkspace);
    void onLauncherTransitionStart(Launcher l, boolean animated, boolean toWorkspace);
    void onLauncherTransitionStep(Launcher l, float t);
    void onLauncherTransitionEnd(Launcher l, boolean animated, boolean toWorkspace);
}
