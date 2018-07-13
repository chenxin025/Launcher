package com.eton.launcher.setting;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.launcher.R;

public class WallpaperAlertActivity extends Activity{
	List<ResolveInfo> wallpaperList;
	
	private ListView listView ;
	private TextView mTitleTextView;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        
        setContentView(R.layout.wallpaper);
        
        listView = (ListView) findViewById(R.id.wallpaper_list);
        mTitleTextView = (TextView)findViewById(R.id.dialog_title);
        Intent intent = new Intent(Intent.ACTION_SET_WALLPAPER);
        wallpaperList = getPackageManager().queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        // Display the fragment as the main content.
//        final AlertController.AlertParams p = null;
        
       
        PickAdapter pickAdapter = new PickAdapter(this,wallpaperList);
//        p.mLabelColumn = MediaStore.Audio.Media.TITLE;
//        p.mIsSingleChoice = true;
//        p.mNegativeButtonText = getString(com.android.internal.R.string.cancel);
//        p.mAdapter = pickAdapter;
//        if (p.mTitle == null) {
//            p.mTitle = getString(R.string.chooser_wallpaper);
//        }
//        setupAlert();
        mTitleTextView.setText(getString(R.string.chooser_wallpaper));
        
        listView.setAdapter(pickAdapter);
 
    }
	protected static class PickAdapter extends BaseAdapter {
	   private final LayoutInflater mInflater;
	   private final Context mContext;
       private final List<ResolveInfo> mItems;
       private WallpaperViewHolder holder;
        /**
         * Create an adapter for the given items.
         */
        public PickAdapter(Context context ,List<ResolveInfo> items) {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mContext = context;
            mItems = items;
        }
        
		@Override
		public int getCount() {
			// TODO 鑷姩鐢熸垚鐨勬柟娉曞瓨鏍�
			   return mItems.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO 鑷姩鐢熸垚鐨勬柟娉曞瓨鏍�
			return  mItems.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO 鑷姩鐢熸垚鐨勬柟娉曞瓨鏍�
			return position;
		}

		@Override
		  public View getView(int position, View convertView, ViewGroup parent) {
			// TODO 鑷姩鐢熸垚鐨勬柟娉曞瓨鏍�
			if (convertView == null) {
				holder = new WallpaperViewHolder();
				convertView = mInflater.inflate(R.layout.alert_picker, null);
				holder.title = (TextView) convertView.findViewById(R.id.label);
				holder.icon = (ImageView) convertView.findViewById(R.id.icon);
				convertView.setTag(holder);
			} else {
				holder = (WallpaperViewHolder) convertView.getTag();
			}
			
			final ResolveInfo info = mItems.get(position);
			holder.title.setText(info.loadLabel(mContext.getPackageManager()));
			
			Drawable d = null;
            d = ((WallpaperAlertActivity)mContext).getFullResIcon(mContext,info);
			holder.icon.setImageDrawable(d);
			convertView.setOnClickListener(new View.OnClickListener() {
			
				@Override
				public void onClick(View arg0) {
					// TODO 鑷姩鐢熸垚鐨勬柟娉曞瓨鏍�
					 Intent intent = new Intent(Intent.ACTION_SET_WALLPAPER);
			         intent.setComponent(new ComponentName(
			                    info.activityInfo.packageName, info.activityInfo.name));
			         mContext.startActivity(intent);
			         ((WallpaperAlertActivity)mContext).finish();
				}

			});
			return convertView;
		}
	}
	 public Drawable getFullResIcon(Context mContext , ResolveInfo info) {
		 Drawable d = null;
		 Resources resources;
		 final ActivityInfo activityInfo = info.activityInfo;
         	ActivityManager activityManager =
		                (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
		        int mIconDpi = activityManager.getLauncherLargeIconDensity();
		        try {
		            resources = getPackageManager().getResourcesForApplication(
		            		activityInfo.applicationInfo);
		        } catch (PackageManager.NameNotFoundException e) {
		            resources = null;
		        }
		        if (resources != null) {
		            int iconId = info.getIconResource();
		            if (iconId != 0) {
		                 try {
		                     d = resources.getDrawableForDensity(iconId, mIconDpi);
		                 } catch (Resources.NotFoundException e) {
		                     d = null;
		                 }
		            }
		        }
		 return d;
	 }
}
final class WallpaperViewHolder {
	public ImageView icon;
	public TextView title;
}