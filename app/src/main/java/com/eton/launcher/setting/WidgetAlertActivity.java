package com.eton.launcher.setting;

import android.app.Activity;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
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

import java.util.ArrayList;
import java.util.List;

public class WidgetAlertActivity extends Activity {
	
	private static PackageManager mPackageManager;
	private ArrayList<AppWidgetProviderInfo> systemWidgets;
	private ListView listView ;
	private TextView mTitleTextView;
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.wallpaper);
        listView = (ListView) findViewById(R.id.wallpaper_list);
        mTitleTextView = (TextView)findViewById(R.id.dialog_title);
        
        mPackageManager = getPackageManager(); 
        List<AppWidgetProviderInfo> mAppinfoList = AppWidgetManager.getInstance(this).getInstalledProviders(); 
    	systemWidgets = new ArrayList<AppWidgetProviderInfo>();
    	systemWidgets.clear();
    	
    	if (mAppinfoList != null){
    		for (int i=0; i < mAppinfoList.size(); i++){
	    		 ComponentName provider = mAppinfoList.get(i).provider; 
	    		 if (provider.getPackageName().equals("com.android.protips")
	    				 ||provider.getPackageName().equals("com.mediatek.videofavorites")
	    				 || provider.getPackageName().equals("com.android.contacts") 
	    				 || provider.getClassName().equals("com.baidu.searchbox.widget.QuickSearchWidgetProvider")){
	    			 continue;
	    		 }
	    		 try {
					int appFlags = mPackageManager.getApplicationInfo(provider.getPackageName(), 0).flags;
					if ((appFlags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 1){
						systemWidgets.add(mAppinfoList.get(i));
					}
				} catch (NameNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    	}
        /*final AlertController.AlertParams p = mAlertParams;
        PickAdapter pickAdapter = new PickAdapter(this,systemWidgets);
        p.mLabelColumn = MediaStore.Audio.Media.TITLE;
        p.mIsSingleChoice = true;
        p.mAdapter = pickAdapter;
        if (p.mTitle == null) {
            p.mTitle = getString(R.string.system_widgets_title);
        }
        setupAlert();*/
        
    	mTitleTextView.setText(getString(R.string.system_widgets_title));
        PickAdapter pickAdapter = new PickAdapter(this,systemWidgets);
        listView.setAdapter(pickAdapter);
    }
	protected static class PickAdapter extends BaseAdapter {
	   private final LayoutInflater mInflater;
	   private final Context mContext;
       private final List<AppWidgetProviderInfo> mWidgets;
       private ViewHolder1 holder;
        /**
         * Create an adapter for the given items.
         */
        public PickAdapter(Context context ,List<AppWidgetProviderInfo> widgets) {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mContext = context;
            mWidgets = widgets;
        }
	   
		   
		@Override
		public int getCount() {
			   return mWidgets.size();
		}

		@Override
		public Object getItem(int position) {
			return  mWidgets.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		  public View getView(final int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				holder = new ViewHolder1();
				convertView = mInflater.inflate(R.layout.alert_picker, null);
				holder.title = (TextView) convertView.findViewById(R.id.label);
				holder.icon = (ImageView) convertView.findViewById(R.id.icon);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder1) convertView.getTag();
			}
			AppWidgetProviderInfo widgetProviderInfo = mWidgets.get(position);
			holder.title.setText(widgetProviderInfo.label);
			final ComponentName cn = widgetProviderInfo.provider;
			Drawable d = mPackageManager.getDrawable(cn.getPackageName(), widgetProviderInfo.icon, null);
			holder.icon.setImageDrawable(d);
			
			convertView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					AppWidgetHost appWidgetHost = new AppWidgetHost(mContext, 1024);
					int mAppWidgetId = appWidgetHost.allocateAppWidgetId();
					AppWidgetManager mAppWidgetManager = AppWidgetManager.getInstance(mContext);
					mAppWidgetManager.bindAppWidgetIdIfAllowed(mAppWidgetId, cn);
					
					Intent data = new Intent();
					data.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,mAppWidgetId);
					((WidgetAlertActivity)mContext).setResult(RESULT_OK, data);
					((WidgetAlertActivity)mContext).finish();
				}

			});
			return convertView;
		}
	}
}
final class ViewHolder1 {
	public ImageView icon;
	public TextView title;
}