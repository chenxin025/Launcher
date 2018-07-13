package com.android.launcher2;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Environment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;

import com.android.launcher.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ScreenEditUtil {
	
	private final static String TAG = "ScreenEditUtil";
	
	public  static int SCREEN_EDIT_HEIGHT = -1;  
	public  static int CELLLAYOUT_Y_TRANSLATION = -1;
	
	public  static int editMenuChoose = -1;
	public final static int MENU_ID_WALLPAPER = 0;
	public final static int MENU_ID_THEME     = 1;
	public final static int MENU_ID_EFFECT    = 2;
	public final static int MENU_ID_WIDGET    = 3;
	public final static int ADD_WIDGET_INVALID_SCREEN = -1;
	public static int mLocalWallpaperID;
	public static int mWallpaperAddId;
	
 	private static ViewPager mGridView;

 	private static int mAddWidgetScreen = -1;
	private Context mContext;
	private AppWidgetManager mAppWidgetManager;
	private AppsCustomizePagedView mWidgetsView;
	private Workspace mWorkspace;
	private static final String SDCARD_ROOT_PATH = Environment
			.getExternalStorageDirectory() + "/";
	
	private ArrayList<Integer> mThumbs;
    private ArrayList<Integer> mImages;
	
    
    public ScreenEditUtil(LauncherApplication context) {
    	findWallpapers(context);
	}
    
	public  void setGridViewObject(ViewPager gridview,Context context){
		mGridView = gridview;
		mContext = context;
	}

  public void setWidgetsView(AppsCustomizePagedView view){
   
      mWidgetsView = view;
  }
	
  public void setWorkSpaceView(Workspace workspace){
      mWorkspace = workspace;
  }

	public  void handleOnMenuClick(int menuId){
		int id = menuId;

		switch (id) {
		case MENU_ID_WALLPAPER:
			editMenuChoose = MENU_ID_WALLPAPER;
			
			updateMenuWallpaper();
			break;
		case MENU_ID_THEME:
			editMenuChoose = MENU_ID_THEME;
			updateMenuTheme();
			break;
		case MENU_ID_EFFECT:
			editMenuChoose = MENU_ID_EFFECT;
			
			updateMenuEffect();
			break;
		case MENU_ID_WIDGET:
			editMenuChoose = MENU_ID_WIDGET;
			initWidgetData();
			break;
		default:
			break;
		}
	}
	
      private void initWidgetData(){
    	
    	ArrayList<AppWidgetProviderInfo> mThirdAppWidget = getThirdWidgetProviderInfo();
    	int screens = 0;
    	if (mThirdAppWidget != null && mThirdAppWidget.size()!=0){
	    	screens = mThirdAppWidget.size()/4 + 1;
	    	if (mThirdAppWidget.size()%4 == 0){
	    		screens = mThirdAppWidget.size()/4;
	    	}
    	}
        ViewPagerItemView.mEtonWidgetView = mWidgetsView;
    	mViewPagerAdapter = new ViewPagerAdapter(mContext, mThirdAppWidget,screens);
    	mGridView.setAdapter(mViewPagerAdapter);
    }

  public List<String> getFileDir(String path) {
		ArrayList<String> allfilePath = new ArrayList<String>();
		File[] files = null;

		File f = new File(path);
		if (null != f) {
			files = f.listFiles();
		}
        if (files != null){
			for (int i = 0; i < files.length; i++) {

				if (isPicture(files[i].getAbsolutePath())){
					allfilePath.add(files[i].getAbsolutePath());
				}
			}
        }
		return allfilePath;
	}
  
	public boolean isPicture(String file) {
		boolean result = false;
		if (file.endsWith(".jpg") || file.endsWith(".JPG")
				|| file.endsWith(".png") || file.endsWith(".PNG")) {
			File fl = new File(file);
			long count = fl.length();
			if (count == 0){
				result = false;
			}else{
				result = true;
			}
			return result;
		}
		
		return result;
	}
	
	public boolean checkDirExists(String filepath) {

		File file = new File(SDCARD_ROOT_PATH + filepath);

		if (null != file) {
			return file.exists();
		}
		return false;

	}

	private File mkdirWallpaper() {
		File dir = new File(SDCARD_ROOT_PATH + ".eton_launcher/wallpaper/");
		//File dir = new File(SDCARD_ROOT_PATH + "aaaaaaaaa/");
		if (dir != null) {
			dir.mkdirs();
		}
		return dir;
	}
	
	
	public void updateMenuWallpaper(){
		if (null == ViewPagerItemView.mEtonWorkSpace){
			ViewPagerItemView.mEtonWorkSpace = mWorkspace;
		}
        mJsonArray = new JSONArray();
    	if (!checkDirExists(".eton_launcher/wallpaper")){
    		if (mkdirWallpaper() == null){
    		//	return;
    		}
    		//return;
    	}
    	List<String> array = getFileDir(SDCARD_ROOT_PATH+".eton_launcher/wallpaper");
    	//if dir is exist,read data
    	
        try{
    	JSONObject object1 = new JSONObject();
        //object1.put("path","local_wallpaper");
    	mLocalWallpaperID = R.drawable.local_wallpaper;
        object1.put("resid", mLocalWallpaperID);
        mJsonArray.put(object1);
        }catch (JSONException e) {
             e.printStackTrace();      
        }

        if (array != null){
    	for (int i = 0; i < mImages.size(); i++){
       //for (int i = 0; i < array.size(); i++){	
    		JSONObject object = new JSONObject();
    		
    		try {
				/*object.put("path", array.get(i));
				mJsonArray.put(object);*/
				
				object.put("resid",mImages.get(i));
//                //object.put("name",effectArray[i]);
                mJsonArray.put(object);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
       }	
        
        //add + res 
        try{
        	JSONObject object1 = new JSONObject();
            //object1.put("path","local_wallpaper");
        	mWallpaperAddId = R.drawable.wallpaper_add;
            object1.put("resid", mWallpaperAddId);
            mJsonArray.put(object1);
            }catch (JSONException e) {
                 e.printStackTrace();      
            }
        
    	int screens = 0;
    	if (null != mJsonArray && mJsonArray.length() != 0){
    		screens = mJsonArray.length()/4 + 1;
    		if (mJsonArray.length()%4 == 0){
    			screens =  mJsonArray.length()/4;
    	  }
    	}
    	mViewPagerAdapter = new ViewPagerAdapter(mContext, mJsonArray,screens);
    	mViewPagerAdapter.setLoadingType(0);
    	ViewPagerItemView.mViewPager = mGridView;
    	//ViewPagerItemView.setPageChangeListen();
    	mGridView.setAdapter(mViewPagerAdapter); 
	}
  
       private JSONArray mJsonArray;
       private ViewPagerAdapter mViewPagerAdapter;
       private static final int ICONS_COUNT[] = {R.drawable.effect_radam,R.drawable.effect_standard,R.drawable.effect_tablet,
                                                 R.drawable.effect_zoomin,R.drawable.effect_zoomout,R.drawable.effect_rotateup,
                                                 R.drawable.effect_rotatedown,R.drawable.effect_cubein,R.drawable.effect_cubeout,R.drawable.effect_stack};

	public void updateMenuEffect() {

		//M: modify from menu goto special effect, not setting current default effect about ui.
		ViewPagerItemView.mPositionEffectOfPage = SharedPreferencesUtils
				.getEffectWhichScreen(mContext);
		ViewPagerItemView.mPositionInPage = SharedPreferencesUtils
				.getPositionInScreen(mContext);

		mJsonArray = new JSONArray();
		String[] effectArray;
		effectArray = mContext.getResources().getStringArray(
				R.array.pref_workspace_transition_effect_entries);
		for (int i = 0; i < effectArray.length; i++) {
			JSONObject object = new JSONObject();
			try {
				object.put("resid", ICONS_COUNT[i]);
				object.put("name", effectArray[i]);
				mJsonArray.put(object);
			} catch (JSONException e) {
			}
		}
		mViewPagerAdapter = new ViewPagerAdapter(mContext, mJsonArray, 3);
		mViewPagerAdapter.setLoadingType(2);

		if (ViewPagerItemView.mEtonWorkSpace == null) {
			ViewPagerItemView.mEtonWorkSpace = mWorkspace;
		}
		ViewPagerItemView.mViewPager = mGridView;
		mGridView.setAdapter(mViewPagerAdapter);
	}

	public void defaultUI(){
		ArrayList<HashMap<String,Object>> items = new ArrayList<HashMap<String,Object>>();
		for(int i =0; i < 15; i++){
			HashMap<String,Object> map = new HashMap<String, Object>();
			map.put("icon",R.drawable.ic_menu_edit);
			map.put("text", i);
			items.add(map);
		}
		//mGridView.setAdapter(new ScreenEditGridAdapter(mContext, items));
	}
	public void updateMenuTheme(){
		if (null == ViewPagerItemView.mEtonWorkSpace) {
			ViewPagerItemView.mEtonWorkSpace = mWorkspace;
		}
		if (null == ViewPagerItemView.mViewPager){
			ViewPagerItemView.mViewPager = mGridView;
		}
		mJsonArray = null;
		mViewPagerAdapter = new ViewPagerAdapter(mContext, mJsonArray, 1);
		mGridView.setAdapter(mViewPagerAdapter);
		/*
		 * ArrayList<HashMap<String,Object>> items = new
		 * ArrayList<HashMap<String,Object>>(); for(int i =0; i < 15; i++){
		 * HashMap<String,Object> map = new HashMap<String, Object>();
		 * map.put("icon",R.drawable.ic_menu_effect); map.put("text", i);
		 * items.add(map); }
		 */
		// mGridView.setAdapter(new ScreenEditGridAdapter(mContext, items));
	}
	
	public ScreenEditTextView mTempView = null;
    public  void setScreenEditEvent(ScreenEditTextView tv){
    	tv.setClickable(true);
    	tv.setFocusable(true);
    	tv.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (null != mTempView){
					mTempView.setSelected(false);
				}
				
				v.setSelected(true);
				mTempView = (ScreenEditTextView)v;
				handleOnMenuClick((Integer)v.getTag());
			}
		});
    }
    
    public static void setAddWidgetScreen(int which){
    	mAddWidgetScreen = which;
    }
    
    public static int getAddWidgetScreen(){
    	return mAddWidgetScreen;
    }
	
    public void notifyEditMenuUpdate(){
    	mViewPagerAdapter.notifyDataSetChanged();
    }
    
    public ArrayList<AppWidgetProviderInfo> getThirdWidgetProviderInfo(){
    	ArrayList<AppWidgetProviderInfo> mThirdAppWidget = new ArrayList<AppWidgetProviderInfo>();
    	mThirdAppWidget.clear();
    	
    	mAppWidgetManager = AppWidgetManager.getInstance(mContext);
    	List<AppWidgetProviderInfo> mAppinfoList = mAppWidgetManager.getInstalledProviders(); 
    	
    	AppWidgetProviderInfo systemwidget= new AppWidgetProviderInfo();
        systemwidget.label = "eton_system_widget";
        mThirdAppWidget.add(systemwidget);

    	final PackageManager pm = mContext.getPackageManager();
    	if (mAppinfoList != null){
    		for (int i=0; i < mAppinfoList.size(); i++){
    		 ComponentName provider = mAppinfoList.get(i).provider; 
    		 try {
				int appFlags = pm.getApplicationInfo(provider.getPackageName(), 0).flags;
				int flags = 0;
				if ((appFlags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0) {
	                flags |= 0x01;
				}
				if (flags == 1){
					//三方应用 需要专门显示
					if (mAppinfoList.get(i).minHeight != 0 && mAppinfoList.get(i).minWidth != 0 
							&& !mAppinfoList.get(i).provider.getPackageName().equals("com.dianxinos.optimizer.channel")
							&&!mAppinfoList.get(i).provider.getPackageName().equals("com.ting.mp3.oemc.android")
							&&!mAppinfoList.get(i).provider.getPackageName().equals("com.sohu.sohuvideo")
							&&!mAppinfoList.get(i).provider.getPackageName().equals("com.sina.weibo")){
						mThirdAppWidget.add(mAppinfoList.get(i));
					}
				}
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		}
    	}

    	return mThirdAppWidget;
    	
    	
    }
    
    //M: fixed issue when uninstall application,need to refresh third widget data under in the edit mode and in widget
    // menu option
    public void refreshEditMenuData(){
    	ArrayList<AppWidgetProviderInfo> mThirdAppWidget = getThirdWidgetProviderInfo();
    	
    	int screens = 0;
    	if (mThirdAppWidget != null && mThirdAppWidget.size()!=0){
	    	screens = mThirdAppWidget.size()/4 + 1;
	    	if (mThirdAppWidget.size()%4 == 0){
	    		screens = mThirdAppWidget.size()/4;
	    	}
    	}
    	mViewPagerAdapter.mWidgetArray = mThirdAppWidget;
    	int currentPage = -1;
    	currentPage = mGridView.getCurrentItem();
    	mViewPagerAdapter = new ViewPagerAdapter(mContext, mThirdAppWidget,screens);
    	
    	if (currentPage > screens){
    		currentPage = screens;
    	}
    	mGridView.setAdapter(mViewPagerAdapter);
    	mGridView.setCurrentItem(currentPage);
    }
    
    private void findWallpapers(LauncherApplication context) {
        mThumbs = new ArrayList<Integer>(24);
        mImages = new ArrayList<Integer>(24);

        final Resources resources = context.getResources();
        // Context.getPackageName() may return the "original" package name,
        // com.android.launcher2; Resources needs the real package name,
        // com.android.launcher. So we ask Resources for what it thinks the
        // package name should be.
        final String packageName = resources.getResourcePackageName(R.array.wallpapers);

        addWallpapers(resources, packageName, R.array.wallpapers);
        //addWallpapers(resources, packageName, R.array.extra_wallpapers);
    }

    private void addWallpapers(Resources resources, String packageName, int list) {
        final String[] extras = resources.getStringArray(list);
        for (String extra : extras) {
            int res = resources.getIdentifier(extra, "drawable", packageName);
            if (res != 0) {
                final int thumbRes = resources.getIdentifier(extra + "_small",
                        "drawable", packageName);

                if (thumbRes != 0) {
                    mThumbs.add(thumbRes);
                    mImages.add(res);
                    // Log.d(TAG, "add: [" + packageName + "]: " + extra + " (" + res + ")");
                }
            }
        }
    }
}
