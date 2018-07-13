package com.android.launcher2;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;



public class DBCheckAPI {	
	public static final String SWITCH_TAG = "DataCheckAPI";
	/* 数据库表监测功能，功能已经实现，需要常开    */
	private static final boolean  DEBUG_CHECK_CONTAINER_FROM_DB = true;
	
	private static final boolean   DEBUG_TOAST_SHOW = false;
//	private static final boolean   DEBUG_TOAST_SHOW = true;
	
	
	/*  关闭下列check功能    */
	public static final boolean  DEBUG_ALLTIME_BUTTON_DB = false;
	private static final boolean  DEBUG_CHECK_POSTION_FROM_DB = false;
	private static final boolean  DEBUG_CHECK_MODEL_ALLAPPLIST_AND_MAP_MATCH = false;
	private static final boolean  DEBUG_CHECK_APP_ITEM_SAME_FROM_DB = false;
	/* for check item info button */
	public static final boolean  DEBUG_CHECK_ITEMINFO = false;
	
	/*   开启下列check功能    */
//	public static final boolean  DEBUG_ALLTIME_BUTTON_DB = true;
//	private static final boolean  DEBUG_CHECK_POSTION_FROM_DB = true;
//	private static final boolean  DEBUG_CHECK_MODEL_ALLAPPLIST_AND_MAP_MATCH = true;
//	private static final boolean  DEBUG_CHECK_APP_ITEM_SAME_FROM_DB = true;
//	/* for check item info button;  */
//	public static final boolean  DEBUG_CHECK_ITEMINFO = true;
	

	
	private static Context mContext;
	
    public static final int CHECK_NULL = 0x00000000;
    public static final int CHECK_MAPPING_SCREEN 	= 0x00000001;
    public static final int CHECK_FAVORITES_POSTION = 0x00000010;
    public static final int CHECK_SAME_APP_ITEM 	= 0x00000100;
    public static int ITEM_ID  = 500;

    
    public static Handler DBCheckHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {

			super.handleMessage(msg);
			String callLocationDescript = (String) msg.obj;
			
			if ((msg.what & CHECK_MAPPING_SCREEN) == CHECK_MAPPING_SCREEN) {
				isOKcheakScreenFromFavoriteToMapping(mContext,callLocationDescript);
			}
			if ((msg.what & CHECK_FAVORITES_POSTION) == CHECK_FAVORITES_POSTION) {
				isOkCheakFavoritesPostion(mContext,callLocationDescript);
			}
			if ((msg.what & CHECK_SAME_APP_ITEM) == CHECK_SAME_APP_ITEM) {
				checkDBAppItemCoincide(mContext,callLocationDescript);
			}

//			Log.d(SWITCH_TAG, "CHECK_DB_END ");
		}
    	
    };
    //<<end


    
//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>CHECK MAPPING AND FAVOEITE  Container>>>>>>>>>>>>>>>>>>>>>//
  
	private static ArrayList<String> loadFavoritesContainer(Context context){
		
        final ContentResolver contentResolver = context.getContentResolver();
        ArrayList<String> favoriteContainerList = new ArrayList<String>();
        
        String [] projection = {
        						" DISTINCT "+LauncherSettings.Favorites.CONTAINER,
								LauncherSettings.Favorites.SCREEN,
								};
        
        String orderMode = LauncherSettings.Favorites.SCREEN+" DESC " ;
        String dataNumberLimited = "";//
        
		final Cursor c = contentResolver.query(LauncherSettings.Favorites.CONTENT_URI,
			projection, "container = '" + LauncherSettings.Favorites.CONTAINER_DESKTOP + "'", null ,orderMode + dataNumberLimited);
        
        try{
	        final int countIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SCREEN);

	        while (c.moveToNext()) {
	        	try {
	                    int count =  c.getInt(countIndex);
	                    favoriteContainerList.add(""+count);
	                
	        	} catch (Exception e) {
	                Log.w(SWITCH_TAG, "Sector items loading interrupted:", e);
	            }
	        }
		}finally {
			if (null != c) {
				c.close();
			}
        }
		
		return  favoriteContainerList;
		
	}
	
	
	private static ArrayList<String> loadMappContainer(Context context){
		
        final ContentResolver contentResolver = context.getContentResolver();
        ArrayList<String> mappingContainerList = new ArrayList<String>();
        
        String [] projection = {
        						LauncherSettings.ScreenMapping.LOGIC_SCREEN,
								};
        
        String orderMode = LauncherSettings.ScreenMapping.LOGIC_SCREEN+" DESC " ;
        String dataNumberLimited = "";//
        
		final Cursor c = contentResolver.query(LauncherSettings.ScreenMapping.CONTENT_URI,
			projection, "", null ,orderMode + dataNumberLimited);
        
        try{
	        final int countIndex = c.getColumnIndexOrThrow(LauncherSettings.ScreenMapping.LOGIC_SCREEN);
	        while (c.moveToNext()) {
	        	try {
	                    int count =  c.getInt(countIndex);
	                    mappingContainerList.add(""+count);
	                
	        	} catch (Exception e) {
	                Log.w(SWITCH_TAG, "Sector items loading interrupted:", e);
	            }
	        }
		}finally {
			if (null != c) {
				c.close();
			}
        }
		
		return  mappingContainerList;
		
	}
	
	public static boolean isOKcheakScreenFromFavoriteToMapping(Context context,String locationDescrip){
		

		if (!(DEBUG_CHECK_CONTAINER_FROM_DB)) {
			return true;
		}
		ArrayList<String> favoriteContainerList = loadFavoritesContainer(context);
		ArrayList<String> mappingContainerList = loadMappContainer(context);
		
		for (String favoriteContainer : favoriteContainerList) {
			if (!mappingContainerList.contains(favoriteContainer)) {
				if (DEBUG_ALLTIME_BUTTON_DB) {
					Toast.makeText(context, "mapping Not matched! \n favor Screen:"+favoriteContainer + "\n;from "+locationDescrip, Toast.LENGTH_LONG).show();
					Log.d(SWITCH_TAG, "mapping Not matched! \n favor Screen:"+favoriteContainer + "\n;from "+locationDescrip);
				}
				
				return false;
			}
		}
		Log.d(SWITCH_TAG, "mapping and favorites is OK!");
		return true;
	}
//<<<<<<<<<<<<<<<<<<<<<<<<CHECK MAPPING AND FAVOEITE  Container<<<<<<<<<<<<<<<<<<<<<<//	
	
	
	
	
//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>CHECK Favorites  Postion >>>>>>>>>>>>>>>>>>>>>//	
	
	private static ArrayList<String> loadFavoritesPostion(Context context){
		
        final ContentResolver contentResolver = context.getContentResolver();
        ArrayList<String> favoritePostionList = new ArrayList<String>();
        
        String [] projection = {
        						LauncherSettings.Favorites.CONTAINER,
								LauncherSettings.Favorites.SCREEN,
								LauncherSettings.Favorites.CELLX,
								LauncherSettings.Favorites.CELLY,
								};
        
        String orderMode = LauncherSettings.Favorites.SCREEN+" DESC " ;
        String dataNumberLimited = "";//
        
		final Cursor c = contentResolver.query(LauncherSettings.Favorites.CONTENT_URI,
			projection, "container = '" + LauncherSettings.Favorites.CONTAINER_DESKTOP + "'", null ,orderMode + dataNumberLimited);
        
        try{
	        final int containerIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CONTAINER);
	        final int screenIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SCREEN);
	        final int cellXIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLX);
	        final int cellYIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLY);
	     
	        while (c.moveToNext()) {
	        	try {
	                    int container =  c.getInt(containerIndex);
	                    int screen =  c.getInt(screenIndex);
	                    int cellX =  c.getInt(cellXIndex);
	                    int cellY =  c.getInt(cellYIndex);
	                    
	                    favoritePostionList.add(" "+container+" "+screen+" "+cellX+" "+cellY+" ");
	                
	        	} catch (Exception e) {
	                Log.w(SWITCH_TAG, "Sector items loading interrupted:", e);
	            }
	        }
		}finally {
			if (null != c) {
				c.close();
			}
        }
		return  favoritePostionList;
		
	}
	
	private static boolean isOkCheakFavoritesPostion(Context context,String locationDescript){
		if (!(DEBUG_CHECK_POSTION_FROM_DB && DEBUG_ALLTIME_BUTTON_DB)) {
			return true;
		}
		ArrayList<String> favoritesPostionList = loadFavoritesPostion(context);
		
		while (favoritesPostionList.size() > 1) {
			String Temp = favoritesPostionList.get(0);
			favoritesPostionList.remove(0);
			if (favoritesPostionList.contains(Temp)) {
				Toast.makeText(context, "DB Postion Same! \n Postion:"+Temp+ "\n;from "+locationDescript, Toast.LENGTH_LONG).show();
				Log.d(SWITCH_TAG, "DB Postion Same! \n Postion:"+Temp+ "\n;from "+locationDescript);
				return false;
			}
		}
		Log.d(SWITCH_TAG, "check DB Postion is OK!");
		return true;
	}
//<<<<<<<<<<<<<<<<<<<<<<<<<<<<<CHECK Favorites  Postion <<<<<<<<<<<<<<<<<<<<<<<<<<<<//	

	
	
	
	
//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>CHECK DB App Item Coincide >>>>>>>>>>>>>>>>>>>>>//		
	public static ArrayList<String> loadDBAppItemCoincide(Context context ){
		
		final ContentResolver contentResolver = context.getContentResolver();
        ArrayList<String> favoriteItemList = new ArrayList<String>();
        
        String [] projection = {
        						LauncherSettings.Favorites.TITLE,
								LauncherSettings.Favorites.INTENT,
								LauncherSettings.Favorites.ITEM_TYPE,
								};
        
        String orderMode = "";//LauncherSettings.Favorites.SCREEN+" DESC " ;
        String dataNumberLimited = "";//
        
		final Cursor c = contentResolver.query(LauncherSettings.Favorites.CONTENT_URI,
			projection, LauncherSettings.Favorites.ITEM_TYPE+" = '" + LauncherSettings.Favorites.ITEM_TYPE_APPLICATION + "'", null ,orderMode + dataNumberLimited);
        
        try{
	        final int titleIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.TITLE);
	        final int intentIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.INTENT);
	        final int itemTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ITEM_TYPE);

	     
	        while (c.moveToNext()) {
	        	try {
	        			String title =  c.getString(titleIndex);
	                    String intent = c.getString(intentIndex);;
	                    int itemType =  c.getInt(itemTypeIndex);
	                    
	                    favoriteItemList.add(title+" "+intent + " "+itemType+" ");
	                
	        	} catch (Exception e) {
	                Log.w(SWITCH_TAG, "Sector items loading interrupted:", e);
	            }
	        }
		}finally {
			if (null != c) {
				c.close();
			}
        }
		return  favoriteItemList;

	}
	
	
	private static void checkDBAppItemCoincide(Context context ,String logDescription){
		
		if (!(DEBUG_CHECK_APP_ITEM_SAME_FROM_DB && DEBUG_ALLTIME_BUTTON_DB)) {
			return ;
		}
		
		 ArrayList<String> favoriteItemList = loadDBAppItemCoincide(context);
		 
		while (favoriteItemList.size() > 1) {
			
			String Temp = favoriteItemList.get(0);
			favoriteItemList.remove(0);
			
			if (favoriteItemList.contains(Temp)) {
				String[] infos = Temp.split(" ");
				String title = infos[0];
				Log.d(SWITCH_TAG, "DB Apps same !!name:" + title +";from "+ logDescription);
				Toast.makeText(context,"DB Apps same !!name:" + title +";from "+ logDescription,Toast.LENGTH_LONG).show();
			}
		}
		Log.d(SWITCH_TAG, "check DB AppItem Coincide END ");
	}
	
//<<<<<<<<<<<<<<<<<<<<<<<<<CHECK DB App Item Coincide <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<//		
	
	
	
	
//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>CHECK LauncherModel map and allapplist is match? >>>>>>>>>>>>>>>>>>>>>//	
	public static void checkAllapplistAndAppMap(Context context,LauncherModel model){
		
		if (!(DEBUG_CHECK_MODEL_ALLAPPLIST_AND_MAP_MATCH && DEBUG_ALLTIME_BUTTON_DB)) {
			return ;
		}
		List<ApplicationInfo> mApps;
		mContext = context;
//		LauncherModel mModel;
//      mModel = ((LauncherApplication) (mLauncher).getApplication()).getModel();
//      mApps = (List<ApplicationInfo>) mModel.getAllAppsList().data;
		
		mApps = (List<ApplicationInfo>) model.getAllAppsList().data;
        boolean isNotMatch = false;
        int count = 0;
        
        for (ApplicationInfo applicationInfo : mApps) {
        	if (null == applicationInfo) {
        		 Log.d(SWITCH_TAG, "mModel.getAllAppsList().data  exist null");
        		 return;
			}
        	
        	ItemInfo tempApplicationInfo =  LauncherModel.sBgItemsIdMap.get(applicationInfo.id);
        	if (null == tempApplicationInfo) {
       		 Log.d(SWITCH_TAG, "sBgItemsIdMap can not exist it:"+applicationInfo.toString());
       		 	return;
			}
        	
			if (!applicationInfo.equals(tempApplicationInfo)) {
				Log.d(SWITCH_TAG, "sBgItemsIdMap---AllAppsList is  Unmatch!!! " 
					+"AllAppsList-item-hashCode:"+applicationInfo.hashCode()
					+"sBgItemsIdMap-item-hashCode:"+tempApplicationInfo.hashCode()
				);
				isNotMatch = true;
				count++;
			}
		}
        if (isNotMatch) {
        	 Log.d(SWITCH_TAG, "sBgItemsIdMap---AllAppsList is Unmatch!!!total:"+count);
        	Toast.makeText(context, "sBgItemsIdMap---AllAppsList is Unmatch!!!total:"+count, Toast.LENGTH_LONG).show();
		}else {
			Log.d(SWITCH_TAG, "sBgItemsIdMap---AllAppsList is Match!!!");
		//	Toast.makeText(context, "sBgItemsIdMap---AllAppsList is Match!!!", Toast.LENGTH_LONG).show();
		}
	}
	
	public static void checkAllapplistAndAppMap(Context context){
		if (!(DEBUG_CHECK_MODEL_ALLAPPLIST_AND_MAP_MATCH  && DEBUG_ALLTIME_BUTTON_DB)) {
			return ;
		}
		
		List<ApplicationInfo> mApps;
		mContext = context;
		Launcher mLauncher = (Launcher) context;
		
		LauncherModel mModel;
        mModel = ((LauncherApplication) (mLauncher).getApplication()).getModel();
        mApps = (List<ApplicationInfo>) mModel.getAllAppsList().data;
		
	
        boolean isNotMatch = false;
        int count = 0;
        
        for (ApplicationInfo applicationInfo : mApps) {
        	if (null == applicationInfo) {
        		 Log.d(SWITCH_TAG, "mModel.getAllAppsList().data  exist null");
        		 return;
			}
        	
        	ItemInfo tempApplicationInfo =  LauncherModel.sBgItemsIdMap.get(applicationInfo.id);
        	if (null == tempApplicationInfo) {
       		 Log.d(SWITCH_TAG, "sBgItemsIdMap can not exist it:"+applicationInfo.toString());
       		 	return;
			}
        	
			if (!applicationInfo.equals(tempApplicationInfo)) {
				Log.d(SWITCH_TAG, "sBgItemsIdMap---AllAppsList is  Unmatch!!! " 
					+"AllAppsList-item-hashCode:"+applicationInfo.hashCode()
					+"sBgItemsIdMap-item-hashCode:"+tempApplicationInfo.hashCode()
				);
				isNotMatch = true;
				count++;
			}
		}
        if (isNotMatch) {
        	Log.d(SWITCH_TAG, "sBgItemsIdMap---AllAppsList is Unmatch!!!total:"+count);
			Toast.makeText(context, "sBgItemsIdMap---AllAppsList is Unmatch!!!total:"+count, Toast.LENGTH_LONG).show();
		}else {
			Log.d(SWITCH_TAG, "sBgItemsIdMap---AllAppsList is Match!!!");
	//		Toast.makeText(context, "sBgItemsIdMap---AllAppsList is Match!!!", Toast.LENGTH_LONG).show();
		}
	}
	
//<<<<<<<<<<<<<<<<<<<<<<<<<CHECK LauncherModel map and allapplist is match? <<<<<<<<<<<<<<<<<<<<<<//		
	
	
	
	
	
	public static void isOkCheakDB(Context context,int CheckType,String callLoactionDescription){
		
		if (!(DEBUG_ALLTIME_BUTTON_DB)) {
		return ;
	}
		mContext = context.getApplicationContext();
		
		Message msg = Message.obtain();
		msg.what = CheckType;
		msg.obj = callLoactionDescription;
		
//		Log.d(SWITCH_TAG, "CHECK_MESSAGE_SENDED ");
		DBCheckHandler.sendMessageDelayed(msg, 200);
		
	}
	
	
	public static void createErrorDataToDatabaseForTest(Context context ) {
		if (!(DEBUG_ALLTIME_BUTTON_DB)) {
			return ;
		} 
		
		 final ContentValues values = new ContentValues();
	        final ContentResolver cr = context.getContentResolver();
	        //app same check
	        values.put(LauncherSettings.BaseLauncherColumns.TITLE, "软件商店");
	        values.put(LauncherSettings.BaseLauncherColumns.INTENT, "#Intent;action=android.intent.action.MAIN;category=android.intent.category.LAUNCHER;launchFlags=0x10200000;component=com.uucun105381.android.cms/com.uucun.android.cms.activity.MarketLoginAndRegisterActivity;end");
	        values.put(LauncherSettings.BaseLauncherColumns.ITEM_TYPE, 0);
	        
	        //mapping check
	        values.put(LauncherSettings.Favorites.SCREEN, -3);
	        //postion check 
	        values.put(LauncherSettings.Favorites.CELLX, 0);
	        values.put(LauncherSettings.Favorites.CELLY, 0);
	        
	        values.put(LauncherSettings.Favorites.SPANX, 1);
	        values.put(LauncherSettings.Favorites.SPANY, 1);
	        
	        values.put(LauncherSettings.Favorites.CONTAINER, -100);
	        values.put(LauncherSettings.Favorites.APPWIDGET_ID, -1);
	        
	        long id = getDBMaxID(context);
	        values.put(LauncherSettings.Favorites._ID, id);
	        
	        cr.insert(false ? LauncherSettings.Favorites.CONTENT_URI :
                LauncherSettings.Favorites.CONTENT_URI_NO_NOTIFICATION, values);
	 }
	
	private static long getDBMaxID(Context context){
		
        final ContentResolver contentResolver = context.getContentResolver();
        long container = 345;
        
        String [] projection = {
        						LauncherSettings.Favorites._ID,
								};
        
        String orderMode = LauncherSettings.Favorites._ID+" DESC " ;
        String dataNumberLimited = " LIMIT " + 1;//
        
		final Cursor c = contentResolver.query(LauncherSettings.Favorites.CONTENT_URI,
			projection, null, null ,orderMode + dataNumberLimited);
        
        try{
	        final int IDIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites._ID);
	        
	        	try {
	        			c.moveToNext();
	                    container =  c.getLong(IDIndex);
	        	} catch (Exception e) {
	                Log.w(SWITCH_TAG, "Sector items loading interrupted:", e);
	            }
	        	
		}finally {
			if (null != c) {
				c.close();
			}
        }
		return  container+1;
	}
	
	
	public static void showToast(Context context ,String txtDescript){
		if (!(DEBUG_TOAST_SHOW&&DEBUG_ALLTIME_BUTTON_DB)) {
			return;
		}
		Log.d(SWITCH_TAG, txtDescript);
		Toast.makeText(context, txtDescript, Toast.LENGTH_LONG).show();
		
	}
	
	
	
	public static final String LAUNCHER_HOME = "/data/data/com.android.launcher/";
	private static final String LAUNCHER_CACHE = "/data/data/com.android.launcher/cache";
	private static final String LAUNCHER_DATABASE = "/data/data/com.android.launcher/databases";
	private static final String LAUNCHER_FILES = "/data/data/com.android.launcher/files";
	private static final String LAUNCHER_SHARE = "/data/data/com.android.launcher/shared_prefs";
	
	public static void deleteByPath(String path) {
    	File mFile = new File(path);
    	if(mFile.exists()){
    		ArrayList<File> listDirList = new ArrayList<File>();
    		File[] mList = mFile.listFiles();
            for (File f : mList) {
            	if(f.getName().equals("lib")){
            		continue;
            	}
                if(f.isDirectory()){
                	listDirList.add(f);
                	deleteByPath(f.getPath());
                } else {
                	f.delete();
                }
            }
            
            for(File item: listDirList){
            	item.delete();
            }
    	}
    }

	
	
}
