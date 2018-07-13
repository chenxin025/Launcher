package com.android.launcher2;

import android.app.Activity;
import android.app.ActivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.util.AttributeSet;

import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.view.Gravity;
import android.view.animation.AlphaAnimation;
import android.util.DisplayMetrics;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RotateDrawable;
import android.widget.Toast;
import android.widget.Button;
import android.util.Log;
import android.widget.TextView;

import com.android.launcher.R;
import com.android.launcher2.DropTarget.DragObject;

import java.util.Set;




public class SectorView extends RelativeLayout implements  View.OnClickListener,
		View.OnLongClickListener, DragSource {

	public static final String TAG = "SectorView";

	public static final String FIRST_Step = "1";
	
    public static int WidgetCurrentLocation = SectorWidgetConfig.DIRECTION_LEFT_BOTTOM;
    private static final int LAYER_APP_MAX_CAPACITY = 9;
    private static final int NUM_BUTTONS = 16;
    private static final int MAX_RECENT_TASKS = NUM_BUTTONS * 2;    // allow for some discards
    private static final boolean DBG_FORCE_EMPTY_LIST = false;

    public static final String ACTION_SECTORVIEW_REMOVE = "com.android.launcher2.ACTION_SECTORVIEW_REMOVE";
    public static final String ACTION_SECTORVIEW_SHOW = "com.android.launcher2.ACTION_SECTORVIEW_SHOW";
    public static final String ACTION_SECTORVIEW_UPDATE = "com.android.launcher2.ACTION_SECTORVIEW_UPDATE";
    public static final String ACTION_SECTORVIEW_REMOVE_WITH_UNPOPANIMATION = "com.android.launcher2.ACTION_SECTORVIEW_REMOVE_WITH_UNPOPANIMATION";
    
    public static int SYS_SCREEN_WIDTH;
    public static int SYS_SCREEN_HIGHT;   
    
    public static int SYS_SECTOR_WIDTH;
    public static int SYS_SECTOR_HIGHT;   
    
    public static int SECTOR_OUTER_SMALL_IMG_WIDTH;
    public static int SECTOR_OUTER_SMALL_IMG_HIGHT;
    
    public static final int  ANIMATION_UNPOP_FINISH = 1;
    public static boolean CURRENT_ACTIVITY_EXIST = false;
    
    public static final float CENTER_AREA_RADIUS = 104.0f;
    public static final float MIDDLE_AREA_RADIUS = 217.0f;
    public static final float OUTER_AREA_SMALL_RADIUS = 380.0f;
    public static final float OUTER_AREA_BIG_RADIUS = 480.0f;
    
    public static final int XHDPI_DENSITY = 320;
    public static final int HDPI_DENSITY = 240;
    public static final int MDPI_DENSITY = 160;
    public static int  CURRENT_SYS_DENSITY = HDPI_DENSITY;
    
    
 
	ImageView popImageView;
    ImageView centerImageView;
    RelativeLayout centerLayout;
    
    ImageView middleFrameworkImageView,pointerAnimaImageView,pointerActualImageView;
    TextView middle_Part0_TextView,middle_Part1_TextView,middle_Part2_TextView;

    RelativeLayout outerRelativeLayout;
    RelativeLayout iconRelativeLayout0;
    RelativeLayout iconRelativeLayout1;
    RelativeLayout iconRelativeLayout2;
    RelativeLayout iconRelativeLayout0_bg;
    RelativeLayout iconRelativeLayout1_bg;
    RelativeLayout iconRelativeLayout2_bg;
    ImageView  iconlayer0_bg_img;
    ImageView  iconlayer1_bg_img;
    ImageView  iconlayer2_bg_img;
	List<View> layer0List = new ArrayList<View>();
	List<View> layer1List = new ArrayList<View>();
	List<View> layer2List = new ArrayList<View>();
	RelativeLayout SectorareSize;
	boolean hasMeasured = false;
    
    RelativeLayout rootLayout;
    RelativeLayout sectorRelativeLayout;
    RelativeLayout middleRelativeLayout;
    
    SectorAnimation mSectorAnimation;
    SectorWholeArea mWholeArea;
    
    GestureDetector flingDetector;

	private Context mContext;
	private LauncherModel mModel;
	private List<ApplicationInfo> mAppsClone;
	private List<ApplicationInfo> mApps;
	private List<ApplicationInfo> mAllAppList;
	private List<ApplicationInfo> mNewInstalledApps;
	private List<ApplicationInfo> mShortcutsOfRecentTasks;
	private List<ApplicationInfo> mFrequentUsedApps;
	
	private List<AppPackageNameAndClassName> recentTasksList;
	private List<String> recentTasksPackageNameList;
	private List<AppCounter> freqAppCounters;
	
    private final LayoutInflater mInflater;
    private final IconCache mIconCache;
//    protected CellLayout mContent;
    
    protected Launcher mLauncher;
    private Drawable mIconDrawable;
    private ApplicationInfo mCurrentDragInfo;
    private View mCurrentDragView;
    private int[] mEmptyCell = new int[2];
    private Workspace mWorkspace;
//    private ItemInfo movedItemInfo;
    private ApplicationInfo movedItemInfo;

    int movedItemOrgScreen ;
    int movedItemOrgCellX ;
    int movedItemOrgCellY;
    private HashMap<Long, FolderInfo> mFolders;

	public static final String CONTACTS_PACKAGENAME = "com.android.contacts";
	public static final String PHONE_PACKAGENAME = "com.android.phone";
	public static final String MMS_PACKAGENAME = "com.android.mms";
	public static final String BROWSER_PACKAGENAME = "com.android.browser";
    private static List<String> undisplay_apps = new ArrayList<String>();
	
    static {
        undisplay_apps.add(CONTACTS_PACKAGENAME);
        undisplay_apps.add(PHONE_PACKAGENAME);
        undisplay_apps.add(MMS_PACKAGENAME);
        undisplay_apps.add(BROWSER_PACKAGENAME);
    }
	
    //grallery and camera packageName is same(only main class isnot same);In recent task,need Special handle;
    public static final String GALLERY_AND_CAMERA_PACKAGENAME = "com.android.gallery3d";
	public static final String SETTINGS_PACKAGENAME = "com.android.settings";
    
	public static boolean mDragFalg = false;
	
    Handler unpopHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ANIMATION_UNPOP_FINISH :				
				SectorView.this.setVisibility(View.INVISIBLE);
//				mContext.unregisterReceiver(mSectorReceive);
				SectorView.this.getContext().sendBroadcast(new Intent(SectorView.ACTION_SECTORVIEW_REMOVE));
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}	
    };

    public final static int MSG_SECTORSIZE = 1;
	Handler initViewHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_SECTORSIZE:
	             loadData();
	             mSectorAnimation.runPopAnimation();
				break;

			default:
				break;
			}
		}
    };

	public SectorView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
//		Log.i("1222", "SectorView-->init Strat");
		mContext = context;
		mLauncher = (Launcher) context;
		
    	flingDetector = new GestureDetector(new WidgetGestureDetector()); 
    	
        mInflater = LayoutInflater.from(context);
        mModel = ((LauncherApplication) (mLauncher).getApplication()).getModel();
		
		mIconCache = ((LauncherApplication)context.getApplicationContext()).getIconCache();
        mWorkspace = ((Launcher) context).getWorkspace();
        mFolders = (HashMap<Long, FolderInfo>) mModel.sBgFolders;

        int screenDensity = getSYSScreenDensityDpi(mContext);
        if (screenDensity > 0) {
        	CURRENT_SYS_DENSITY = screenDensity;
		}
        SectorUtils.initAgruments(screenDensity);
	}
	
	private static void getScreenSize(Context context)
    {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            ((Activity) context).getWindowManager().getDefaultDisplay()
                            .getMetrics(displayMetrics);
//   CURRENT_SCREEN_WIDTH = displayMetrics.widthPixels;
//   CURRENT_SCREEN_HEIGHT = displayMetrics.heightPixels;
//   CURRENT_DENSITY = displayMetrics.densityDpi;
//   DENSITY_RATIO = STANDARD_DENSITY / CURRENT_DENSITY;
            Toast.makeText(context, "widthPixels:"+displayMetrics.widthPixels
            						+";heightPixels"+displayMetrics.heightPixels
            						+";densityDpi"+displayMetrics.densityDpi
            						, Toast.LENGTH_LONG).show();
    }
	
	private static int getSYSScreenDensityDpi(Context context)
    {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            ((Activity) context).getWindowManager().getDefaultDisplay()
                            .getMetrics(displayMetrics);
            return displayMetrics.densityDpi;
 
    }


    protected void onFinishInflate() {
        super.onFinishInflate();
        
      //1.ȡ����Ӧ������б?
        //2.�Ƴ�ָ��Ϊ����ʾ�ġ�����Ӧ�����Ӧ����ݣ�����Ϣ��jϵ�ˣ��绰�������ȣ�����spec����ȷҪ��ģ�
        //3.�Ƴ�ϵͳӦ��ֻ�����ص�Ӧ�ã�
        //4.ϵͳshortcutinfo�б?��ϵͳ��װʱ�����������Ա���ʾ��
//        Log.i("1222", "SectorView-->newInstall Strat");
        mAllAppList = (List<ApplicationInfo>) mModel.getAllAppsList().data;
        
        mNewInstalledApps = new ArrayList<ApplicationInfo>();
        //>>add by eton wanghenan;Use ApplicationInfo in Map,but no from clone list;
        for (ApplicationInfo applicationInfo : mAllAppList) {
        	ApplicationInfo info = (ApplicationInfo) LauncherModel.sBgItemsIdMap.get(applicationInfo.id);
        	if (null != info) {
        		mNewInstalledApps.add(info);
			}
		}
//      mNewInstalledApps = (List<ApplicationInfo>) mModel.getAllAppsList().data.clone();
        //<< added end
        
        removeNoNeedDisplayApps(mNewInstalledApps);
        removeSystemApps(mNewInstalledApps);
        Collections.sort(mNewInstalledApps, new ComparatorInstallTime());
//        Log.i("1222", "SectorView-->newInstall end");
        
        //1.ȡ����Ӧ������б?�Ҵ��б�Ҫ���������������ù�ͬʹ�ã����°�װ�����Լ���Ӧ���б?��Ҫ����ú��°�װ����Ҫ���������޷�����ͬһ��list��
        //2.�Ƴ�ָ��Ϊ����ʾ�ġ�����Ӧ�����Ӧ����ݣ�
//        mAppsClone = (List<ApplicationInfo>) mModel.getAllAppsList().data;
        
        //>>add by eton wanghenan;Use ApplicationInfo in Map,but no from clone list;
        mApps = new ArrayList<ApplicationInfo>();
        for (ApplicationInfo applicationInfo : mAllAppList) {
        	ApplicationInfo info = (ApplicationInfo) LauncherModel.sBgItemsIdMap.get(applicationInfo.id);
        	if (null != info) {
                mApps.add(info);
			}
		}
       // mApps = (List<ApplicationInfo>) mModel.getAllAppsList().data.clone();
       //<<add end
        
        removeNoNeedDisplayApps(mApps);
        
        //��ʼ�����������б�
        //1.��ȡ��������;2.����ý��������Ӧ��ShortcutInfo
//        Log.i("1222", "SectorView-->RecentTasks Strat");
        mShortcutsOfRecentTasks = new ArrayList<ApplicationInfo>();
        recentTasksList = new ArrayList<AppPackageNameAndClassName>();
        //Prevent "system Settings APP" display two seconds
        recentTasksPackageNameList = new ArrayList<String>();
        //end
        reloadRecentTask();
//        Log.i("1222", "reloadRecentTask end");
//        Log.i("1222", "getShortcutInfosForRecentTasks(); start");
        getApplicationinfoForRecentTasks();
//        Log.i("1222", "SectorView-->RecentTasks end");
        
        //��ʼ������б�
        //���ϵͳapp count��ֵ�����ø�ֵȥ��ö�Ӧ��Shortcutinfo
//        Log.i("1222", "SectorView-->AppCounters Strat");
        freqAppCounters = new ArrayList<AppCounter>();
        mFrequentUsedApps = new ArrayList<ApplicationInfo>(); 
        loadDataBaseAppsOpenCounts();
       // Collections.sort(freqAppCounters, new ComparatorCounter());
       // getShortcutInfosByCount();
        getApplicationInfosByID();
        removeNoNeedDisplayApps(mFrequentUsedApps);
//        Log.i("1222", "SectorView-->AppCounters end");
        
       
 //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>.       
//        if (WidgetCurrentLocation == SectorWidgetConfig.DIRECTION_LEFT_BOTTOM) {
//        	  DBCheckAPI.createErrorDataToDatabaseForTest(mContext );
//		}
      
        
//      DataCheckAPI.loadFavoritesContainer(mContext);
//      Log.i("3222", "isOKcheakScreenFromFavoriteToMapping-->Strat");
      DBCheckAPI.isOkCheakDB(mContext,
  			DBCheckAPI.CHECK_FAVORITES_POSTION | DBCheckAPI.CHECK_MAPPING_SCREEN | DBCheckAPI.CHECK_SAME_APP_ITEM,
      		"SectorView");
//      Log.i("3222", "isOkCheakFavoritesPostion-->end");
      
//      Log.i("1222", "SectorView-->init end");
//    mNewInstalledApps = new ArrayList<ApplicationInfo>();
  //    mFrequentUsedApps = new ArrayList<ApplicationInfo>();
      DBCheckAPI.checkAllapplistAndAppMap( mContext,mModel);
 //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
      
      
    	CURRENT_ACTIVITY_EXIST = true;
		initScreenData();
		getSectorAreaSize();
		createView();
		initView(WidgetCurrentLocation);
    }

	public static SectorView fromXml(Context context,int mode) {
		if (mode != SectorWidgetConfig.DIRECTION_LEFT_TOP 
			&&mode != SectorWidgetConfig.DIRECTION_LEFT_BOTTOM
			&&mode != SectorWidgetConfig.DIRECTION_RIGHT_BOTTOM) {
			WidgetCurrentLocation = SectorWidgetConfig.DIRECTION_LEFT_TOP;
		}else {
			WidgetCurrentLocation = mode; 
		}
        return (SectorView) LayoutInflater.from(context).inflate(R.layout.sectorview, null);
    }
	
	
	public static final class ComparatorInstallTime implements Comparator<ApplicationInfo>{
        @Override
        public int compare(ApplicationInfo object1, ApplicationInfo object2) {
//            long m1=((ApplicationInfo)object1).appInstallTime;
//            long m2=((ApplicationInfo)object2).appInstallTime;
            long m1=((ApplicationInfo)object1).firstInstallTime;
            long m2=((ApplicationInfo)object2).firstInstallTime;
            int result=0;
            //��Ϊ���򣬵ݼ����У���� if(m1>m2){result=1;}if(m1<m2){result=-1;}��Ϊ����������У�
            //�˴�Ϊ�˱���㣬�����°�װ�����ڵ�һλ���4εݼ�
            if( m1 > m2){
                result= -1;
            }else if( m1 < m2){
                result = 1;
            }
            return result;
        }
    }
	
	public static final class ComparatorCounter implements Comparator<AppCounter>{
        @Override
        public int compare(AppCounter object1, AppCounter object2) {
            int m1=((AppCounter)object1).usedCounter;
            int m2=((AppCounter)object2).usedCounter;
            int result=0;
            //��Ϊ���򣬵ݼ����У���� if(m1>m2){result=1;}if(m1<m2){result=-1;}��Ϊ����������У�
            //�˴�Ϊ�˱���㣬�����°�װ�����ڵ�һλ���4εݼ�
            if( m1 > m2){
                result= -1;
            }else if( m1 < m2){
                result = 1;
            }
            return result;
        }
    }
	private void initScreenData(){
		WindowManager wm = (WindowManager) mContext.getSystemService(mContext.WINDOW_SERVICE);
		SYS_SCREEN_WIDTH = wm.getDefaultDisplay().getWidth();//��Ļ���
		SYS_SCREEN_HIGHT = wm.getDefaultDisplay().getHeight();//��Ļ�߶�	
	}
	
	private void getSectorAreaSize(){
		SectorareSize = (RelativeLayout) findViewById(R.id.lancher_outer_area_size);
        ViewTreeObserver vto2 = SectorareSize.getViewTreeObserver();  
        vto2.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override  
            public void onGlobalLayout() {  
            	SectorareSize.getViewTreeObserver().removeGlobalOnLayoutListener(this);  
            	SYS_SECTOR_HIGHT = SectorareSize.getMeasuredHeight();
                SYS_SECTOR_WIDTH = SectorareSize.getMeasuredWidth();
            	SectorTestAPI.PrintCommon("screenSize", "3listener��ʽ��ȡ-���������ȣ�"+SYS_SECTOR_WIDTH+",��������߶ȣ�"+SYS_SECTOR_HIGHT);
                Message msg = Message.obtain();
                msg.what = MSG_SECTORSIZE;
                initViewHandler.sendMessageDelayed(msg, 50);
            }
        });  
	}
	
	private void createView(){
		//���ڲ���������
		centerImageView = (ImageView) findViewById(R.id.lancher_center_img);				
		centerLayout = (RelativeLayout) findViewById(R.id.lancher_center_area);		

		//�������
		outerRelativeLayout = (RelativeLayout) findViewById(R.id.lancher_outer_area);
		iconRelativeLayout0 = (RelativeLayout) findViewById(R.id.lancher_outer_layer0);
		iconRelativeLayout1 = (RelativeLayout) findViewById(R.id.lancher_outer_layer1);
		iconRelativeLayout2 = (RelativeLayout) findViewById(R.id.lancher_outer_layer2);
		iconRelativeLayout0_bg = (RelativeLayout) findViewById(R.id.lancher_outer_layer0_bg);
		iconRelativeLayout1_bg = (RelativeLayout) findViewById(R.id.lancher_outer_layer1_bg);
		iconRelativeLayout2_bg = (RelativeLayout) findViewById(R.id.lancher_outer_layer2_bg);
		iconlayer0_bg_img = (ImageView) findViewById(R.id.lancher_outer_layer0_bg_img);
		iconlayer1_bg_img = (ImageView) findViewById(R.id.lancher_outer_layer1_bg_img);
		iconlayer2_bg_img = (ImageView) findViewById(R.id.lancher_outer_layer2_bg_img);
		
		//�м�����
		middleFrameworkImageView = (ImageView) findViewById(R.id.lancher_mid_framework_img);
//		middleFrameworkImageView.setImageResource(R.drawable.cometonlaunch_quick_access_section_indicator0);
//		middleFrameworkImageView.setOnTouchListener(new RotateButtonListener());
		pointerActualImageView = (ImageView) findViewById(R.id.lancher_pointer_Actual_img);
//		pointerActualImageView.setImageResource(R.drawable.cometonlaunch_quick_access_section_indicator0);
//		pointerActualImageView.setTag(Integer.valueOf(R.drawable.cometonlaunch_quick_access_section_indicator0));
		pointerAnimaImageView = (ImageView) findViewById(R.id.lancher_pointer_Anima_img);
		pointerAnimaImageView.setVisibility(View.GONE);
		middleRelativeLayout = (RelativeLayout) findViewById(R.id.lancher_middle_area);
		middle_Part0_TextView = (TextView) findViewById(R.id.lancher_pointer_txt_part0);
		middle_Part1_TextView = (TextView) findViewById(R.id.lancher_pointer_txt_part1);
		middle_Part2_TextView = (TextView) findViewById(R.id.lancher_pointer_txt_part2);

		//ȫ�ְ�(���ڲ����м䣬�ⲿ������
		sectorRelativeLayout = (RelativeLayout) findViewById(R.id.lancher_sector_area);
		sectorRelativeLayout.setVisibility(View.GONE);
	
	
//		//��ʼ�� ����������Ӧ�����
//	 int[] layer0_icons = {R.id.lancher_outer_layer01,R.id.lancher_outer_layer02,R.id.lancher_outer_layer03,
//			R.id.lancher_outer_layer04,R.id.lancher_outer_layer05,R.id.lancher_outer_layer06,
//			R.id.lancher_outer_layer07,R.id.lancher_outer_layer08,R.id.lancher_outer_layer09};
//		layer0List = new ArrayList<View>();
//		for (int i = 0; i < mNewInstalledApps.size()&& i < LAYER_APP_MAX_CAPACITY; i++) {
//			CellLayout cell =(CellLayout) findViewById(layer0_icons[i]);
//			cell.setGridSize(0, 0);
//			cell.getShortcutsAndWidgets().setMotionEventSplittingEnabled(false);
//			createAndAddShortcut(cell,(ApplicationInfo)mNewInstalledApps.get(i));
//			layer0List.add(cell);
//		}
//		
//		layer1List = new ArrayList<View>();
////		for (int i = 0; i < 6; i++) {
//		for (int i = 0; i < mFrequentUsedApps.size()&& i < LAYER_APP_MAX_CAPACITY; i++) {
//			CellLayout cell =(CellLayout) findViewById(SectorWidgetConfig.layer1_icons[i]);
//			cell.setGridSize(0, 0);
//			cell.getShortcutsAndWidgets().setMotionEventSplittingEnabled(false);
//			createAndAddShortcut(cell,(ApplicationInfo)mFrequentUsedApps.get(i));
//			layer1List.add(cell);
//		}	
//
//		layer2List = new ArrayList<View>();
//		for (int i = 0; i < mShortcutsOfRecentTasks.size()&& i < LAYER_APP_MAX_CAPACITY; i++) {
//			CellLayout cell =(CellLayout) findViewById(SectorWidgetConfig.layer2_icons[i]);
//			cell.setGridSize(0, 0);
//			cell.getShortcutsAndWidgets().setMotionEventSplittingEnabled(false);
//			createAndAddShortcut(cell,(ApplicationInfo)mShortcutsOfRecentTasks.get(i));
//			layer2List.add(cell);
//		}
		
		//��ʼ�����ͼ��Ĭ�ϵ���ʾ��ʽ����ʾ������ʾ
		iconRelativeLayout0_bg.setVisibility(SectorPointerAnimation.LAYER_0_DISPLAY_MODE_DEFAULT);
		iconRelativeLayout1_bg.setVisibility(SectorPointerAnimation.LAYER_1_DISPLAY_MODE_DEFAULT);
		iconRelativeLayout2_bg.setVisibility(SectorPointerAnimation.LAYER_2_DISPLAY_MODE_DEFAULT);
		//�����ڲ�ͬλ���£��������ͼ�����ʾ���ݵķ�����ֻ��ı����ͼ�����Դ���ü���
		//�����Ҫ��
		//������ʾʱ�����ϵ��£��ֱ���ʾ ����ʹ�á�������á������°�װ����
		//������ʾʱ�����ϵ��£��ֱ���ʾ ����ʹ�á�������á������°�װ����
		//������ʾʱ�����ϵ��£��ֱ���ʾ ����ʹ�á�������á������°�װ����
		if (SectorWidgetConfig.DIRECTION_LEFT_TOP == WidgetCurrentLocation) {
			
			initLayer(SectorWidgetConfig.layer0_icons,layer0List,mShortcutsOfRecentTasks);
			initLayer(SectorWidgetConfig.layer1_icons,layer1List,mFrequentUsedApps);
			initLayer(SectorWidgetConfig.layer2_icons,layer2List,mNewInstalledApps);
		}else if (SectorWidgetConfig.DIRECTION_LEFT_BOTTOM == WidgetCurrentLocation) {
			
			initLayer(SectorWidgetConfig.layer0_icons,layer0List,mShortcutsOfRecentTasks);
			initLayer(SectorWidgetConfig.layer1_icons,layer1List,mFrequentUsedApps);
			initLayer(SectorWidgetConfig.layer2_icons,layer2List,mNewInstalledApps);
		}else if (SectorWidgetConfig.DIRECTION_RIGHT_BOTTOM == WidgetCurrentLocation) {
			
			initLayer(SectorWidgetConfig.layer0_icons,layer0List,mNewInstalledApps);
			initLayer(SectorWidgetConfig.layer1_icons,layer1List,mFrequentUsedApps);
			initLayer(SectorWidgetConfig.layer2_icons,layer2List,mShortcutsOfRecentTasks);
		}
		//��ʼ�� ����������Ӧ�����

			
		centerImageView.setOnClickListener(new OnClickListener() {	
			@Override
			public void onClick(View v) {
				mSectorAnimation.runUnPopAnimation();
			//	mContext.sendBroadcast(new Intent(SectorView.ACTION_SECTORVIEW_UPDATE));
			}
		});
		
		//measure sectorView get fling Event ,no this,workspace will take it;
		outerRelativeLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SectorTestAPI.PrintCommon(null, "sectorView outer is onClicked");
			}
		});
	}
	
	private void initLayer(final int[] layerViewIDs,List<View> displayedViews,List<ApplicationInfo> dataSource){
//		displayedViews = new ArrayList<View>();
		for (int i = 0; i < dataSource.size()&& i < LAYER_APP_MAX_CAPACITY; i++) {
			CellLayout cell =(CellLayout) findViewById(layerViewIDs[i]);
			cell.setGridSize(0, 0);
			cell.getShortcutsAndWidgets().setMotionEventSplittingEnabled(false);
			//>>added by wanghenan ;Prevent Exception CheckIteminfo;
			ApplicationInfo applicationinfo = (ApplicationInfo)dataSource.get(i);
			ItemInfo itemInfo = LauncherModel.sBgItemsIdMap.get(applicationinfo.id);
			if (null == itemInfo) {
				continue;
			}
			//<< end
		//	createAndAddShortcut(cell,(ApplicationInfo)dataSource.get(i));
			createAndAddShortcut(cell,(ApplicationInfo)itemInfo);
			displayedViews.add(cell);
		}	
	}

	
	
	private void loadData(){
		
		SectorCenterArea centerArea = new SectorCenterArea(centerImageView,centerLayout);
		SectorMiddleArea middleArea = new SectorMiddleArea(middleFrameworkImageView, pointerAnimaImageView, pointerActualImageView, middleRelativeLayout
				,middle_Part0_TextView,middle_Part1_TextView,middle_Part2_TextView);
		SectorOuterArea outerArea = new SectorOuterArea(outerRelativeLayout,
				iconRelativeLayout0_bg,iconRelativeLayout1_bg,iconRelativeLayout2_bg, 
				iconlayer0_bg_img,iconlayer1_bg_img,iconlayer2_bg_img,
				iconRelativeLayout0,iconRelativeLayout1, iconRelativeLayout2,
				layer0List,layer1List,layer2List);
		mWholeArea = new SectorWholeArea(popImageView, sectorRelativeLayout, centerArea, middleArea, outerArea);
		mSectorAnimation = new SectorAnimation(mContext,unpopHandler,mWholeArea,SYS_SECTOR_WIDTH,SYS_SECTOR_HIGHT);	
	}	
	
	
	
	
	private class RotateButtonListener implements OnTouchListener
	{
		@Override
		public boolean onTouch(View view, MotionEvent event) {
			if (MotionEvent.ACTION_DOWN ==event.getAction() ) {
				
				return mSectorAnimation.runPointerClickAnimation(event.getX(), event.getY(), view.getWidth());
			}
			return true;
		}
	}

	class WidgetGestureDetector extends SimpleOnGestureListener 
	{     
	   @Override
		public boolean onSingleTapUp(MotionEvent e) {
		   //�����ؼ������⣬�ؼ���ʧ֮�߼�
			SectorTestAPI.PrintCommon(null, "SectorView onTouchEvent3-1 !");
		   if (SectorUtils.clickForUnpop(e.getX(), e.getY(), SYS_SECTOR_WIDTH)) {
			   mSectorAnimation.runUnPopAnimation();
			   return true;
		   }else  {
			   boolean unPopflag = false;
			   if (View.VISIBLE == iconRelativeLayout0_bg.getVisibility()) {
					if (((Integer)iconlayer0_bg_img.getTag()).intValue()== R.drawable.cometonlaunch_outer_small_framework) {
						unPopflag = true;
					};
					
				}else if (View.VISIBLE == iconRelativeLayout1_bg.getVisibility()) {
					if (((Integer)iconlayer1_bg_img.getTag()).intValue()== R.drawable.cometonlaunch_outer_small_framework) {
						unPopflag = true;
					};
				}else if (View.VISIBLE == iconRelativeLayout2_bg.getVisibility()) {
					if (((Integer)iconlayer2_bg_img.getTag()).intValue()== R.drawable.cometonlaunch_outer_small_framework) {
						unPopflag = true;
					};
				}
			   
			   if(unPopflag){
					if (SectorUtils.clickForUnpop(e.getX(), e.getY(), (int)(SYS_SECTOR_WIDTH * (OUTER_AREA_SMALL_RADIUS/OUTER_AREA_BIG_RADIUS))) ){
						   mSectorAnimation.runUnPopAnimation();
						   return true;
					   }
			   }
			
		}
			return super.onSingleTapUp(e);
		}

	   @Override     
	   public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) 
	   {  
		SectorTestAPI.PrintCommon(null, "SectorView onTouchEvent3-2 !");
			if (e1 == null || e2 == null) return false;  
			           
			SectorVector vector =  SectorUtils.getVectorForGestures(e1.getX(), e1.getY(), e2.getX(), e2.getY());
			
			if (null == vector){
				return false;
			}else if (!vector.lengthIsValid()) {
				return true;
			}
			if (SectorWidgetConfig.DIRECTION_LEFT_TOP == SectorView.WidgetCurrentLocation) {
				if (vector.isSecondQuadrant()) {
//					 Toast.makeText(SectorView.this, "�ڶ���������", Toast.LENGTH_SHORT).show();
					 mSectorAnimation.runPointerMoveAnimation(SectorPointerAnimation.CLOCKWISE_ROTATE);
				}else if (vector.isFourthQuadrant()) {
//					 Toast.makeText(SectorView.this, "������������", Toast.LENGTH_SHORT).show();
					 mSectorAnimation.runPointerMoveAnimation(SectorPointerAnimation.COUNTER_CLOCKWISE_ROTATE);
				}else if (vector.isThirdQuadrant()) {
					mSectorAnimation.runUnPopAnimation();
				}else if (vector.isFirstQuadrant()) {
					mSectorAnimation.runPopAppendAnimation();
				}

			}else if (SectorWidgetConfig.DIRECTION_LEFT_BOTTOM == SectorView.WidgetCurrentLocation) {
				if (vector.isFirstQuadrant()) {
//					 Toast.makeText(SectorView.this, "��һ��������", Toast.LENGTH_SHORT).show();
					 mSectorAnimation.runPointerMoveAnimation(SectorPointerAnimation.CLOCKWISE_ROTATE);
				}else if (vector.isThirdQuadrant()) {
//					 Toast.makeText(SectorView.this, "������������", Toast.LENGTH_SHORT).show();
					 mSectorAnimation.runPointerMoveAnimation(SectorPointerAnimation.COUNTER_CLOCKWISE_ROTATE);
				} else if (vector.isSecondQuadrant()) {
					mSectorAnimation.runUnPopAnimation();
				}else if (vector.isFourthQuadrant()) {
					mSectorAnimation.runPopAppendAnimation();
				}
			}
			else if (SectorWidgetConfig.DIRECTION_RIGHT_BOTTOM == SectorView.WidgetCurrentLocation) {
				if (vector.isSecondQuadrant()) {
//					 Toast.makeText(SectorView.this, "�ڶ���������", Toast.LENGTH_SHORT).show();
					 mSectorAnimation.runPointerMoveAnimation(SectorPointerAnimation.COUNTER_CLOCKWISE_ROTATE);
				}else if (vector.isFourthQuadrant()) {
//					 Toast.makeText(SectorView.this, "������������", Toast.LENGTH_SHORT).show();
					 mSectorAnimation.runPointerMoveAnimation(SectorPointerAnimation.CLOCKWISE_ROTATE);
				}else if (vector.isFirstQuadrant()) {
					mSectorAnimation.runUnPopAnimation();
				}else if (vector.isThirdQuadrant()) {
					mSectorAnimation.runPopAppendAnimation();
				}
			}
		   return true;     
	   }
	} 
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		SectorTestAPI.PrintCommon(null, "SectorView onTouchEvent2!");
		   if (flingDetector.onTouchEvent(event)){         
		   return true;     
	   }
	    SectorTestAPI.PrintCommon(null, "SectorView onTouchEvent2-2 !");
		return super.onInterceptTouchEvent(event);
	}
	  	

  void	initView(int imgDirection){
		 
		 int alignType1 = RelativeLayout.ALIGN_PARENT_LEFT;
		 int alignType2 = RelativeLayout.ALIGN_PARENT_TOP;
		 
		switch (imgDirection) {
		case SectorWidgetConfig.DIRECTION_LEFT_TOP :
			alignType1 = RelativeLayout.ALIGN_PARENT_LEFT;
			alignType2 = RelativeLayout.ALIGN_PARENT_TOP;
			break;
		case SectorWidgetConfig.DIRECTION_LEFT_BOTTOM :
			alignType1 = RelativeLayout.ALIGN_PARENT_LEFT;
			alignType2 = RelativeLayout.ALIGN_PARENT_BOTTOM;
			break;
		case SectorWidgetConfig.DIRECTION_RIGHT_BOTTOM :
			alignType1 = RelativeLayout.ALIGN_PARENT_RIGHT;
			alignType2 = RelativeLayout.ALIGN_PARENT_BOTTOM;
			break;		
		default:
			break;
		}
		
		 SectorUtils.modifyLayoutAlignType(sectorRelativeLayout, alignType1,alignType2);
		 		 
		 modifyImageDirection(centerImageView,R.drawable.cometonlaunch_center,imgDirection);
		 SectorUtils.modifyLayoutAlignType(centerLayout, alignType1,alignType2);
		 
		 SectorUtils.modifyLayoutAlignType(middleRelativeLayout, alignType1,alignType2);
		 SectorUtils.modifyLayoutAlignType(middleFrameworkImageView, alignType1,alignType2);
		 modifyImageDirection(middleFrameworkImageView,R.drawable.cometonlaunch_middle_framework,imgDirection);
		 middleFrameworkImageView.setOnTouchListener(new RotateButtonListener());
		 
		 
		 SectorUtils.modifyLayoutAlignType(pointerActualImageView, alignType1,alignType2);
//		 modifyImageDirection(pointerActualImageView,R.drawable.cometonlaunch_quick_access_section_indicator0,imgDirection);
		 modifyImageDirection(pointerActualImageView,SectorPointerAnimation.LAYER_DISAPLAY_LOCATION_DEFAULT,imgDirection);
		 
		 SectorUtils.modifyLayoutAlignType(pointerAnimaImageView, alignType1,alignType2);
//		 modifyImageDirection(pointerAnimaImageView,R.drawable.cometonlaunch_quick_access_section_indicator0,imgDirection);
		 modifyImageDirection(pointerAnimaImageView,SectorPointerAnimation.POINTER_LOCATION_DEFAULT,imgDirection);

		 SectorUtils.modifyLayoutAlignType(outerRelativeLayout, alignType1,alignType2);

		 SectorUtils.modifyLayoutAlignType(iconRelativeLayout0, alignType1,alignType2);
		 SectorUtils.modifyLayoutAlignType(iconRelativeLayout1, alignType1,alignType2);
		 SectorUtils.modifyLayoutAlignType(iconRelativeLayout2, alignType1,alignType2);
		 SectorUtils.modifyLayoutAlignType(iconRelativeLayout0_bg, alignType1,alignType2);
		 SectorUtils.modifyLayoutAlignType(iconRelativeLayout1_bg, alignType1,alignType2);
		 SectorUtils.modifyLayoutAlignType(iconRelativeLayout2_bg, alignType1,alignType2);
		 
		 SectorUtils.modifyLayoutAlignType(iconlayer0_bg_img, alignType1,alignType2);
		 if ( layer0List.size()<5) {
			 modifyImageDirection(iconlayer0_bg_img,R.drawable.cometonlaunch_outer_small_framework,imgDirection);
		}else {
			modifyImageDirection(iconlayer0_bg_img,R.drawable.cometonlaunch_outer_big_framework,imgDirection);
		}
		 
		 SectorUtils.modifyLayoutAlignType(iconlayer1_bg_img, alignType1,alignType2);
		 if ( layer1List.size()<5) {
			 modifyImageDirection(iconlayer1_bg_img,R.drawable.cometonlaunch_outer_small_framework,imgDirection);
		}else {
			modifyImageDirection(iconlayer1_bg_img,R.drawable.cometonlaunch_outer_big_framework,imgDirection);
		}
		 
		 SectorUtils.modifyLayoutAlignType(iconlayer2_bg_img, alignType1,alignType2);
		 
		 if ( layer2List.size()<5) {
			 modifyImageDirection(iconlayer2_bg_img,R.drawable.cometonlaunch_outer_small_framework,imgDirection);
		}else {
			modifyImageDirection(iconlayer2_bg_img,R.drawable.cometonlaunch_outer_big_framework,imgDirection);
		}
	  }
	  
	public void modifyImageDirection(ImageView imgView,int imgDrawableID,int direction){
		    imgView.setImageDrawable(getResources().getDrawable(imgDrawableID));
			RotateDrawable rotateDrawable =(RotateDrawable)imgView.getDrawable();  
			rotateDrawable.setLevel(direction);
			imgView.setTag(Integer.valueOf(imgDrawableID));
			imgView.refreshDrawableState();
			
	 }

	   protected boolean createAndAddShortcut(CellLayout cell,ApplicationInfo itemTemp) {
	        /// M: modified for Unread feature, the icon is playced in a RelativeLayout,
	        /// so we should set click/longClick listener for the icon, not the RealtiveLayout.
		   
	        final MTKShortcut shortcut =
//	            (MTKShortcut) mInflater.inflate(R.layout.mtk_application, this, false);       
	            (MTKShortcut) mInflater.inflate(R.layout.sector_cell, this, false);  
	 
	        ApplicationInfo item =  (ApplicationInfo) LauncherModel.sBgItemsIdMap.get(itemTemp.id);
	        if (null == item || null == shortcut) {
				return false;
			}
	        
	        shortcut.setIcon(new FastBitmapDrawable(item.getIcon(mIconCache)));
	        shortcut.setTitle(item.title);

			shortcut.setTag(item);
	        
	    //    shortcut.setShortcutUnreadMarginRight(mUnreadMarginRight);
	        shortcut.updateShortcutUnreadNum(item.unreadNum);
	    //    mFolderIcon.updateFolderUnreadNum(item.intent.getComponent(), item.unreadNum);

	        shortcut.mFavorite.setOnClickListener(SectorView.this);
	        shortcut.mFavorite.setOnLongClickListener(this);

	        // We need to check here to verify that the given item's location isn't already occupied
	        // by another item.
	        if (cell.getChildAt(item.cellX, item.cellY) != null || item.cellX < 0 || item.cellY < 0
	                || item.cellX >= cell.getCountX() || item.cellY >= cell.getCountY()) {
	            // This shouldn't happen, log it. 
	            Log.e(TAG, "Folder order not properly persisted during bind");
//	            if (!findAndSetEmptyCells(item)) {
//	                return false;
//	            }
	        }

//	        CellLayout.LayoutParams lp =
//	            new CellLayout.LayoutParams(item.cellX, item.cellY, item.spanX, item.spanY);
	        CellLayout.LayoutParams lp =
	            new CellLayout.LayoutParams(0, 0, 1, 1);
//	        boolean insert = false;
	        boolean insert = true;
//	        shortcut.setOnKeyListener(new FolderKeyEventListener());
//	        mContent.addViewToCellLayout(shortcut, insert ? 0 : -1, (int)item.id, lp, true);
	        
	        cell.addView(shortcut, lp);
	        cell.setScaleX((float)0.7);
	        cell.setScaleY((float)0.7);
	        cell.setVisibility(View.VISIBLE);
	       
	        return true;
	    }

	   public void onClick(View v) {
	        Object tag = v.getTag();
	        if (LauncherLog.DEBUG) {
	            LauncherLog.d(TAG, "onClick: v = " + v + ", tag = " + tag);
	        }

	        if (tag instanceof ApplicationInfo) {
	            // refactor this code from Folder
	           ApplicationInfo item = (ApplicationInfo) tag;
//	            int[] pos = new int[2];
//	            v.getLocationOnScreen(pos);
//	            item.intent.setSourceBounds(new Rect(pos[0], pos[1],
//	                    pos[0] + v.getWidth(), pos[1] + v.getHeight()));

	            mLauncher.startActivitySafely(v, item.intent, item);    
	        } 
	    }
	   
	    public boolean onLongClick(View v) {
	        // Return if global dragging is not enabled
	        if (!mLauncher.isDraggingEnabled()) {
	            return true;
	        }

	        Object tag = v.getTag();
	        
	        if (LauncherLog.DEBUG) {
	            LauncherLog.d(TAG, "onLongClick: v = " + v + ", tag = " + tag);
	        }

	        if (tag instanceof ApplicationInfo) {
	        	ApplicationInfo item = (ApplicationInfo) tag;
	           
	            if (!v.isInTouchMode()) {
	                return false;
	            }

	            movedItemInfo = item;

	            SectorView.this.setVisibility(View.INVISIBLE);
	            
	            mLauncher.dismissFolderCling(null);
	            mLauncher.getWorkspace().sectorViewStartDrg(v);
	            mLauncher.getWorkspace().onDragStartedWithItem(v);
	            mLauncher.getWorkspace().beginDragShared(v, SectorView.this);
	            mIconDrawable = ((TextView) v).getCompoundDrawables()[1];

	            mCurrentDragInfo = item;
	            mEmptyCell[0] = item.cellX;
	            mEmptyCell[1] = item.cellY;
	            /// M: modified for unread feature, the icon is playced in a RelativeLayout.
	          //  mCurrentDragView = (View)v.getParent();
	         //   mContent.removeView(mCurrentDragView);
	         //  ((CellLayout)( v.getParent().getParent())).removeView(mCurrentDragView);
	            removeAppIcon(movedItemInfo, 22);
	        }
	        return true;
	    }

		@Override
		public boolean supportsFlingToDelete() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void onFlingToDeleteCompleted() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onDropCompleted(View target, DragObject d,
				boolean isFlingToDelete, boolean success) {
			
			if (target instanceof DeleteZone) {
				d.dragView.setVisibility(View.GONE);
				restoreAppIcon(movedItemInfo, 22);
				SectorView.this.getContext().sendBroadcast(new Intent(SectorView.ACTION_SECTORVIEW_REMOVE));
				return;
			}
			
			if(success && mWorkspace.containerIsHotseat(d)){
			    mLauncher.removeAllGaussBitmap();
			}

			if (!success) {
				d.dragView.setVisibility(View.GONE);
//				SectorView.this.getContext().sendBroadcast(new Intent(SectorView.ACTION_SECTORVIEW_REMOVE));
				restoreAppIcon(movedItemInfo, 22);
			}
			
			mDragFalg = true;
		   	SectorView.this.setVisibility(View.VISIBLE);
			mSectorAnimation.runPopAnimation();

		}
		
		   /**
	     * note: we must pay attention on the use of HashMap.
	     *       when we call mFolders.get(long key), we cant cast key to int, which will change the address
	     *       of index key, then the null will be returned. 
	     */
	    private void removeAppIcon(ItemInfo app, long destDir) {

	        ApplicationInfo si = (ApplicationInfo) app;
	        long originalDir = si.getContainer();
//	        int screen = si.screen;
	        int screen = LauncherModel.getActualScreen(si.screen,originalDir);

	        int cellX = si.cellX;
	        int cellY = si.cellY;	        


//	        if (originalDir == destDir) {
//	            return;
//	        }

//	        // first, we add new icon to dest location
//	        switch ((int)destDir) {
//	        case LauncherSettings.Favorites.CONTAINER_DESKTOP:
//	            //int[] cell = mModel.getVacantCell();
//	            int[] cell = ((Launcher)mContext).getVacantCell();
//	            // update db
//	            LauncherModel.moveItemInDatabase(mContext, si,
//	            LauncherSettings.Favorites.CONTAINER_DESKTOP, cell[2], cell[0], cell[1]);
//
//	            // add view to desktop
//	            View shortcut = ((Launcher) mContext).createShortcut(si);
//	            mWorkspace.addInScreen(shortcut, LauncherSettings.Favorites.CONTAINER_DESKTOP, cell[2],
//						cell[0], cell[1], 1, 1);
//	            break;
//	        case LauncherSettings.Favorites.CONTAINER_HOTSEAT:
//	            break;
//	        default:
//	            FolderInfo fi = mFolders.get(destDir);
//
//	            if (fi.contents.size() >= mMaxNumItems) {
//	                return;
//	            }
//
//	            fi.add(si);
//	            break;
//	        }

	        // then, we remove original icon
	        switch ((int) originalDir) {
	        case LauncherSettings.Favorites.CONTAINER_DESKTOP:
	            CellLayout layout = (CellLayout) mWorkspace.getChildAt(screen);
	            if (null == layout) {
					return;
				}
	            View child = layout.getChildAt(cellX, cellY);
	            if (child != null) {
	                layout.removeView(child);
	            }
	            break;
	        case LauncherSettings.Favorites.CONTAINER_HOTSEAT:
	        	CellLayout hotseatlayout = mLauncher.getHotseat().getLayout();
	        	if (null == hotseatlayout) {
					return;
				}
	        	View hotseatChild = hotseatlayout.getChildAt(cellX,cellY);
	        	if (hotseatChild != null) {
	        		hotseatlayout.removeView(hotseatChild);
				}	        	
	            break;
	        default:
	         //   FolderInfo fi = getFolderInfo(originalDir);
	            FolderInfo fi = mFolders.get(originalDir);
	            if (null != fi) {
	            	if (LauncherLog.DEBUG) Log.d(TAG, "fi not null!");
	            	
	                fi.remove(si);
	            } else {
	            	if (LauncherLog.DEBUG) Log.d(TAG, "fi is null!");
	            }
	            break;
	        }
	    }
	    
	    
	    private void restoreAppIcon(ItemInfo app, long destDir) {

	        ApplicationInfo si = (ApplicationInfo) app;
	        long originalDir = si.getContainer();
	        int screen = si.screen;
	        int cellX = si.cellX;
	        int cellY = si.cellY;	        

//	        if (originalDir == destDir) {
//	            return;
//	        }

	        // first, we add new icon to dest location
	        switch ((int)originalDir) {
	        case LauncherSettings.Favorites.CONTAINER_DESKTOP:
	            //add view to desktop
	            View shortcut = ((Launcher) mContext).createShortcut(si);
	            mWorkspace.addInScreen(shortcut, LauncherSettings.Favorites.CONTAINER_DESKTOP, LauncherModel.getActualScreen(app.screen,LauncherSettings.Favorites.CONTAINER_DESKTOP),
						app.cellX, app.cellY, app.spanX, app.spanY);
	            break;
	        case LauncherSettings.Favorites.CONTAINER_HOTSEAT:
	        	//add view to hotseat
	            View hotseatShortcut = ((Launcher) mContext).createShortcut(si);
	            mWorkspace.addInScreen(hotseatShortcut, LauncherSettings.Favorites.CONTAINER_HOTSEAT, LauncherModel.getActualScreen(app.screen,LauncherSettings.Favorites.CONTAINER_HOTSEAT),
						app.cellX, app.cellY, app.spanX, app.spanY);
	            break;
	        default:
	            FolderInfo fi = mFolders.get(originalDir);

//	            if (fi.contents.size() >= mMaxNumItems) {
//	                return;
//	            }

	            fi.add(si);
	            break;
	        }

	    }
	    
	    private View getViewFromDesktop (int screen, int cellX, int cellY) {
	    	CellLayout layout = (CellLayout) mWorkspace.getChildAt(screen);
	    	View v = layout.getChildAt(cellX, cellY);
	    	return v;
	    }
	    
	    private View getViewFromHotseat (int cellX, int cellY) {
	    	CellLayout hotseatlayout = mLauncher.getHotseat().getLayout();
        	View hotseatChild = hotseatlayout.getChildAt(cellX,cellY);
	    	return hotseatChild;
	    }

	    private View getViewForInfo(CellLayout layout, ApplicationInfo item) {
	        for (int j = 0; j < layout.getCountY(); j++) {
	            for (int i = 0; i < layout.getCountX(); i++) {
	                View v = layout.getChildAt(i, j);
	                if (v.getTag() == item) {
	                    return v;
	                }
	            }
	        }
	        return null;
	    }
		
	    /**
	     * Reload the 6 buttons with recent activities
	     */
	    private void reloadRecentTask() {

	        final Context context = getContext();
	        final PackageManager pm = context.getPackageManager();
	        final ActivityManager am = (ActivityManager)
	                context.getSystemService(Context.ACTIVITY_SERVICE);
	        final List<ActivityManager.RecentTaskInfo> recentTasks =
	                am.getRecentTasks(MAX_RECENT_TASKS, ActivityManager.RECENT_IGNORE_UNAVAILABLE);

	        ActivityInfo homeInfo = 
	            new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
	                    .resolveActivityInfo(pm, 0);

//	        IconUtilities iconUtilities = new IconUtilities(getContext());

	        // Performance note:  Our android performance guide says to prefer Iterator when
	        // using a List class, but because we know that getRecentTasks() always returns
	        // an ArrayList<>, we'll use a simple index instead.
	        int index = 0;
	        int numTasks = recentTasks.size();
	        
	        //ԭ��ȡindex < LAYER_APP_MAX_CAPACITY �����ڵ�һ���Ϊ��launcher����ᱻ���Ե�
	        //Ӧ��ȡLAYER_APP_MAX_CAPACITY�� Ϊ 9����Ϊ������ʱ����ȥlauncher�������Ҳֻ��ȡ9-1=8��;
	        //��˴˴��ӡ�1����
	        for (int i = 0; i < numTasks && (index < LAYER_APP_MAX_CAPACITY+undisplay_apps.size()+1); ++i) {
//	        for (int i = 0; i < numTasks && (index < LAYER_APP_MAX_CAPACITY); ++i) {

	            final ActivityManager.RecentTaskInfo info = recentTasks.get(i);

	            // for debug purposes only, disallow first result to create empty lists
	            if (DBG_FORCE_EMPTY_LIST && (i == 0)) continue;

	            Intent intent = new Intent(info.baseIntent);
	            if (info.origActivity != null) {
	                intent.setComponent(info.origActivity);
	            }

	            // Skip the current home activity.
	            if (homeInfo != null) {
	                if (homeInfo.packageName.equals(
	                        intent.getComponent().getPackageName())
	                        && homeInfo.name.equals(
	                                intent.getComponent().getClassName())) {
	                    continue;
	                }
	            }

	            intent.setFlags((intent.getFlags()&~Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
	                    | Intent.FLAG_ACTIVITY_NEW_TASK 
//	                    | Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY
	                    );
	            final ResolveInfo resolveInfo = pm.resolveActivity(intent, 0);
	            if (resolveInfo != null) {
	                final ActivityInfo activityInfo = resolveInfo.activityInfo;
//	                final android.content.pm.ApplicationInfo sysApplicationInfo = activityInfo.applicationInfo;
	                final String title = activityInfo.loadLabel(pm).toString();
	                Drawable icon = activityInfo.loadIcon(pm);
	                	
	                	
	                	
	                if (title != null && title.length() > 0 && icon != null) {
	                	String packageName = intent.getComponent().getPackageName();
	                	String className = intent.getComponent().getClassName();
//	                	String applicationName = getApplicationName(sysApplicationInfo);
	                	String applicationName = title;
	                	/*
	                	 * 1.Prevent "system Settings APP" display two seconds;
	                	*if the packagename had been added in list,then we donot added it again;
	                	* settings may be showed two seconds in recent task list;
	                	* 
	                	*  2.Package name of gallery and camera is same;
	                	*  but both of they need display in the same time;
	                	*/
//	                	if (recentTasksPackageNameList.contains(packageName) 
//	                			&& !GALLERY_AND_CAMERA_PACKAGENAME.equalsIgnoreCase(packageName)
//	                		) {
//	                		continue;
//	                	}	
//	                	recentTasksPackageNameList.add(packageName);
	                	
	                	if (SETTINGS_PACKAGENAME.equalsIgnoreCase(packageName)) 
	                	{
	                		if (!recentTasksPackageNameList.contains(SETTINGS_PACKAGENAME)) {
	                			recentTasksPackageNameList.add(packageName);
							}else {
								continue;
							}
	                	}
	                	//Prevent "system Settings APP" -->end
	                		
                		AppPackageNameAndClassName appAndClassName = new AppPackageNameAndClassName(packageName, className, applicationName);
                		recentTasksList.add(appAndClassName);
                		SectorTestAPI.PrintCommon("9999", packageName +"+"+applicationName+"\n\n");
                		 ++index;
						
	                   
	                }
	            }
	        }

	    }
		
	    private void getApplicationinfoForRecentTasks(){
	    	
	        for (int i = 0; i < recentTasksList.size(); i++) {
	        	for (int j = 0; j < mApps.size(); j++) {
	        		ApplicationInfo info2 = (ApplicationInfo)mApps.get(j);
	        		if (null == info2) continue;
	        		ApplicationInfo info =  (ApplicationInfo) LauncherModel.sBgItemsIdMap.get(info2.id);
	        		if (null == info) {
	        			continue;
					}
	        		
	        		String intentPackageName = info.intent.getComponent().getPackageName();
	        		String intentClassName =  info.intent.getComponent().getClassName();
	        		String applicationName = info.title.toString();
	        		SectorTestAPI.PrintCommon("9999", intentPackageName+"+"+applicationName);
	        		//if (null != intentContent && intentContent.contains(recentTasksList.get(i))) 
	            	if (null != intentPackageName && intentPackageName.equalsIgnoreCase(recentTasksList.get(i).packageName)
	            		&&( 
	            			   (null != intentClassName && intentClassName.equalsIgnoreCase(recentTasksList.get(i).className))
	            			|| (null != applicationName && applicationName.equalsIgnoreCase(recentTasksList.get(i).applicationName))
	            		  )
	            	   )
	            	{
	        			mShortcutsOfRecentTasks.add((ApplicationInfo)info);
	        			continue;
					}
				}	
	        	SectorTestAPI.PrintCommon("9999", "\n\n\n");
			}
	    }
	    
	    public String getApplicationName(android.content.pm.ApplicationInfo applicationInfo) { 
	    	PackageManager packageManager = null; 
	    	packageManager = mContext.getPackageManager(); 
	    	
	    	String applicationName = 
	    	(String) packageManager.getApplicationLabel(applicationInfo); 
	    	return applicationName; 
	    	} 
		
		private class AppPackageNameAndClassName{
			public String packageName;
			public String className;
			public String applicationName;
			public AppPackageNameAndClassName(String packageName,String className,String applicationName) {
				super();
				this.packageName = packageName;
				this.className = className;
				this.applicationName = applicationName;
			}
		}
		
		private class AppCounter{
			public long id;
			public String packageName;
			public String className;
			public int usedCounter;
			
			public AppCounter(long id,String packageName, String className,int usedCounter) {
				super();
				this.id = id;
				this.packageName = packageName;
				this.className = className;
				this.usedCounter = usedCounter;
			}
		}
		
		private void loadDataBaseAppsOpenCounts(){
			
	        final ContentResolver contentResolver = mContext.getContentResolver();
//	        String [] projection = {LauncherSettings.Favorites.INTENT,
//	        						LauncherSettings.Favorites.APP_LAUNCH_COUNT,
//	        						LauncherSettings.Favorites.ITEM_TYPE};
//	        
//	        final Cursor c = contentResolver.query(LauncherSettings.Favorites.CONTENT_URI,
//	        		projection, "scene = '" + Launcher.getCurrentScene() + "'", null, null);
	        
	        String [] projection = {LauncherSettings.Favorites._ID,
					        		LauncherSettings.Favorites.INTENT,
									LauncherSettings.Favorites.APP_LAUNCH_COUNT,
									LauncherSettings.Favorites.ITEM_TYPE};
	        
	        String orderMode = LauncherSettings.Favorites.APP_LAUNCH_COUNT+" DESC " ;
	        String dataNumberLimited = " LIMIT " + (LAYER_APP_MAX_CAPACITY + undisplay_apps.size());
	        
	        synchronized (LauncherModel.sBgLock) {
		        //>>modify new install application ,have no scene default values;
	//			final Cursor c = contentResolver.query(LauncherSettings.Favorites.CONTENT_URI,
	//				projection, "scene = '" + Launcher.getCurrentScene() + "'", null ,orderMode + dataNumberLimited);
				final Cursor c = contentResolver.query(LauncherSettings.Favorites.CONTENT_URI,
						projection, LauncherSettings.Favorites.ITEM_TYPE +" = '" + LauncherSettings.Favorites.ITEM_TYPE_APPLICATION + "'", 
						null ,orderMode + dataNumberLimited);
		        //<< end
				
		        try{
		        	final int idIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites._ID);
			        final int intentIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.INTENT);
			        final int countIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.APP_LAUNCH_COUNT);
			        final int itemTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ITEM_TYPE);
			        String intentDescription;
			        Intent intent;
			        while (c.moveToNext()) {
			        	try {
				                int itemType = c.getInt(itemTypeIndex);
				                switch (itemType) {
				                case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
				               // case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
			                    intentDescription = c.getString(intentIndex);
			                    try {
			                        intent = Intent.parseUri(intentDescription, 0);
			                    } catch (URISyntaxException e) {
			                        continue;
			                    }
			                    int count =  c.getInt(countIndex);
			                    //����4û�д򿪹������ʾ�б�-���
			                    if (0 == count) {
									continue;
								}
			                    long id = c.getLong(idIndex);
			                    
			                    AppCounter appCounter = new AppCounter(id , intent.getComponent().getPackageName(), 
			                    		intent.getComponent().getClassName(), count);
			                    
			                    freqAppCounters.add(appCounter);
			                    break;
			                }
			        	} catch (Exception e) {
			                Log.w(TAG, "Sector items loading interrupted:", e);
			            }
			        }
				}finally {
					if (null != c) {
						c.close();
					}
		        }
	        }
		}
		
		private void  getApplicationInfosByCount(){
			int index = 0;
	        for (int i = 0; i < freqAppCounters.size()&& index < LAYER_APP_MAX_CAPACITY; i++) {
	        	for (int j = 0; j < mApps.size(); j++) {
	        		ApplicationInfo info = mApps.get(j);
	   
	        		String packageName = info.intent.getComponent().getPackageName();
	        		String  className = info.intent.getComponent().getClassName();
	        		if (null != packageName && packageName.equalsIgnoreCase(freqAppCounters.get(i).packageName)
	        			&&null != className && className.equalsIgnoreCase(freqAppCounters.get(i).className)) {
	        			
        				mFrequentUsedApps.add(info);
	        			index++;
	        			continue;
	        		
					}
	        	}
			}
		}

		private void  getApplicationInfosByID(){
	        for (int i = 0; i < freqAppCounters.size(); i++) {
	        	long applicationID = freqAppCounters.get(i).id;
	        	ApplicationInfo info = (ApplicationInfo) LauncherModel.sBgItemsIdMap.get(applicationID);
	        	if (null != info) {
	        		mFrequentUsedApps.add(info);
				}
			}
		}
		
		
		
	    private class SectorAppChangeReceive extends BroadcastReceiver {
	        @Override
	        public void onReceive(Context context, Intent intent){
	        	 final String action = intent.getAction();
	        	 if (Intent.ACTION_PACKAGE_CHANGED.equals(action)
	        		||Intent.ACTION_PACKAGE_REMOVED.equals(action)
	        		||Intent.ACTION_PACKAGE_ADDED.equals(action)) {
	        		 
	        		 context.sendBroadcast(new Intent(SectorView.ACTION_SECTORVIEW_UPDATE));	
	        		 Toast.makeText(context, "Sector-update0", Toast.LENGTH_SHORT).show();
				}
//        		if (Intent.ACTION_PACKAGE_CHANGED.equals(action)) {
//        			
//	    		}else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
//	    		
//	    		}else if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
//	    			
//	    		}
	        }
	    }
	    
	    public void unpopSectorAndRemove(){
	    	if (null == mSectorAnimation) {
				return;
			}
	    	mSectorAnimation.runUnPopAnimation();
	    }
	    
	    
		private void removeNoNeedDisplayApps(List<ApplicationInfo> allApps){
			//�Ƴ� ��ָ����Ҫ��ʾ��Ӧ�ã���Ϣ��jϵ�ˣ��绰�������ȣ�����spec��Ҫ��
		       for (int i = 0; i < allApps.size(); i++) {
		        	ApplicationInfo info = allApps.get(i);
		        	String packageName = info.intent.getComponent().getPackageName();
					if (undisplay_apps.contains(packageName)) {
						allApps.remove(i);
						i--;
					}
				}
		}
		
		private void removeSystemApps(List<ApplicationInfo> allApps){
		//�Ƴ�ϵͳӦ��ֻ���������Ӧ�ã�
		       for (int i = 0; i < allApps.size(); i++) {
		        	ApplicationInfo info = allApps.get(i);
		        	if (ApplicationInfo.DOWNLOADED_FLAG != info.flags) {
		        		allApps.remove(i);
		        		//list���ȱ仯��i+1���ǰ���u�i��λ�ã����i���Լ����©��ԭ���ĵ�i+1��
		        		//��ֹ©�
		        		i--;
					}
				}
		}	
		

}
