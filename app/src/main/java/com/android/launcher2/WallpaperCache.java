package com.android.launcher2;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.android.launcher.R;

import java.util.HashMap;

public class WallpaperCache {
	
	private static final String TAG = "WallpaperCache";
	private HashMap<Integer,CacheEntry> mWallpaperCache = null;
	
	
	private static class CacheEntry {
        public Bitmap wallpapericon;
    }
	public WallpaperCache(){
//		mWallpaperCache = new HashMap<Integer, SoftReference<Bitmap>>();
		mWallpaperCache = new HashMap<Integer, CacheEntry>();
	}
	
	public Bitmap getBitmap(int strId, Context context){
		if (mWallpaperCache.containsKey(strId)){
			CacheEntry reference = mWallpaperCache.get(strId);
			Bitmap bitmap = reference.wallpapericon;
			if (null != bitmap){
				//LauncherLog.d(TAG, "null != bitmap");
				return bitmap;
			}else{
				//TODO
				//LauncherLog.d(TAG, "null == bitmap");
				Bitmap bitmaptemp = makeResIdToBitmap(strId,context);
				//mWallpaperCache.put(strId, new SoftReference<Bitmap>(bitmaptemp));
				return bitmaptemp;
			}
		}else{
			return null;
		}
	}
	
	
	public void getAllWallpaperBitmaps(final Resources resources,final Context context){
		final String[] extras = resources.getStringArray(R.array.wallpapers);
		new Thread(){
			public void run(){
				mWallpaperCache.clear();
				Bitmap bitmap = makeResIdToBitmap(R.drawable.local_wallpaper,context);
				CacheEntry entry = new CacheEntry();
				entry.wallpapericon = bitmap;
				mWallpaperCache.put(R.drawable.local_wallpaper, entry);
				for (String extra : extras){
					int res = resources.getIdentifier(extra, "drawable", resources.getResourcePackageName(R.array.wallpapers));
					if (res != 0){
						Bitmap bitmap1 = makeResIdToBitmap(res,context);
						CacheEntry entry1 = new CacheEntry();
						entry1.wallpapericon = bitmap1;
						//Log.d("GGA", "bitmap1="+bitmap1);
						mWallpaperCache.put(res, entry1);
					}
				}
				Bitmap bitmap1 = makeResIdToBitmap(R.drawable.wallpaper_add,context);
				CacheEntry entry2 = new CacheEntry();
				entry2.wallpapericon = bitmap1;
				mWallpaperCache.put(R.drawable.wallpaper_add, entry2);
			}
		}.start();
				
	}
	
	private Bitmap makeResIdToBitmap(int id,Context context) {
//		InputStream is = context.getResources().openRawResource(id);
//		BitmapFactory.Options options = new BitmapFactory.Options();
//		options.inJustDecodeBounds = false;
		
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(context.getResources(), id, options);
		
		options.inSampleSize = ViewPagerItemView.calculateInSampleSize(options, 60,
				60);
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(context.getResources(), id, options);
		//return BitmapFactory.decodeStream(is, null, options);
	}
}
