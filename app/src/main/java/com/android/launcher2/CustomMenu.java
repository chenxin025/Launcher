package com.android.launcher2;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.android.launcher.R;

public final class CustomMenu {
	
	private static final String TAG="CustomMenu";	
	private static PopupWindow mPopWindow=null;
	private final Activity activity;
	private ListView mListView;
	public String[] mListItems;
	private TextView mTextView;
	public CustomMenu(Activity activity) {
		// TODO Auto-generated constructor stub
		this.activity = activity;
	}
	                
	public  PopupWindow getMenu(OnTouchListener touchListener,OnKeyListener keyListener,final Context context1) {

		
		View view = activity.getLayoutInflater().inflate(R.layout.custom_menu, null);  // layout_custom_menu菜单的布局文件
		mListView = (ListView)view.findViewById(R.id.menu_list);
		mTextView = (TextView)view.findViewById(R.id.menu_text);
		mTextView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (null != mPopWindow && mPopWindow.isShowing()){
					mPopWindow.dismiss();
				}
			}
		});
		
		mListItems = context1.getResources().getStringArray(
				R.array.custom_menu_list);
		
		mListView.setAdapter(new MenuBodyAdapter(context1,mListItems));
		mPopWindow = new PopupWindow(view,ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT,true);          
		//pop.setAnimationStyle(R.style.pop_anim_style);
		mPopWindow.setBackgroundDrawable(activity.getResources().getDrawable(android.R.color.transparent));// 这句是关键，响应返回键必须的语句
		mPopWindow.setFocusable(true);
		mPopWindow.setTouchable(true);
		mPopWindow.setOutsideTouchable(true);
		view.setFocusableInTouchMode(true); 
		//pop.setTouchInterceptor(touchListener);
		view.setOnKeyListener( keyListener);
		
	//	Log.i(TAG, pop.toString());
		return mPopWindow;
	
	}
	
	
	static public class MenuBodyAdapter extends BaseAdapter{

		public final Context mContext;
		public String[] mStrings;
		public LayoutInflater mLayoutInflater;
		
		public MenuBodyAdapter(Context context,String[] strs){
			this.mContext = context;
			mStrings = strs;
			this.mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			Log.d("vvv", "55555555555555");
			return mStrings.length;
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public View getView(final int postion, View arg1, ViewGroup arg2) {
			// TODO Auto-generated method stub
			//Log.d("vvv", "arg0="+postion);
			ViewHolder holder;
			if (null == arg1){
				holder = new ViewHolder();
				arg1 = mLayoutInflater.inflate(R.layout.menu_item, null);
				holder.text = (TextView) arg1.findViewById(R.id.label);
				
				arg1.setTag(holder);
			}else{
				holder = (ViewHolder)arg1.getTag();
			}
			
			holder.text.setText(mStrings[postion]);
			
			arg1.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					Log.d("vvv", "arg0="+postion);
					int customMenuId = -1;
					switch (postion) {
					case 0:
						customMenuId = Launcher.CUSTOM_MENU_WALLPAPER;
						break;
					case 1:
						customMenuId = Launcher.CUSTOM_MENU_EFFECT;
						break;
					case 2:
						customMenuId = Launcher.CUSTOM_MENU_EDIT;
						break;
					case 3:
						customMenuId = Launcher.CUSTOM_MENU_SETTINGS;
						break;
					case 4:
						customMenuId = Launcher.MENU_TEST;
						break;

					default:
						break;
					}
					((Launcher)mContext).handleCustomMenuOptions(customMenuId);
					if (null != mPopWindow && mPopWindow.isShowing()){
						mPopWindow.dismiss();
					}
				}
			});
			
//			holder.text = (TextView)arg1.findViewById(R.id.menuitem);
//			holder.text.setText("kjkjhkj");
			return arg1;
		}
		
		static class ViewHolder {
			TextView text;
		}
		
	}
	
	/*protected static class PickAdapter extends BaseAdapter {
		   private final LayoutInflater mInflater;
		   private final Context mContext;

	       private ViewHolder1 holder;
	        *//**
	         * Create an adapter for the given items.
	         *//*
	        public PickAdapter(Context context) {
	            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            mContext = context;
	          
	        }
		   
			   
			@Override
			public int getCount() {
				   return 5;
			}

			@Override
			public Object getItem(int position) {
				return  position;
			}

			@Override
			public long getItemId(int position) {
				return position;
			}

			@Override
			  public View getView(final int position, View convertView, ViewGroup parent) {
				
					holder = new ViewHolder1();
					convertView = mInflater.inflate(R.layout.alert_picker, null);
					holder.title = (TextView) convertView.findViewById(R.id.label);
					holder.title.setText("ABCCCC");
					//holder.icon = (ImageView) convertView.findViewById(R.id.icon);
					
				
//				AppWidgetProviderInfo widgetProviderInfo = mWidgets.get(position);
//				holder.title.setText(widgetProviderInfo.label);
//				final ComponentName cn = widgetProviderInfo.provider;
//				Drawable d = mPackageManager.getDrawable(cn.getPackageName(), widgetProviderInfo.icon, null);
//				holder.icon.setImageDrawable(d);
				
				convertView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
//						AppWidgetHost appWidgetHost = new AppWidgetHost(mContext, 1024);
//						int mAppWidgetId = appWidgetHost.allocateAppWidgetId();
//						AppWidgetManager mAppWidgetManager = AppWidgetManager.getInstance(mContext);
//						mAppWidgetManager.bindAppWidgetId(mAppWidgetId, cn);
//						
//						Intent data = new Intent();
//						data.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,mAppWidgetId);
//						((WidgetAlertActivity)mContext).setResult(RESULT_OK, data);
//						((WidgetAlertActivity)mContext).finish();
					}

				});
				return convertView;
			}
		}*/
}

final class ViewHolder1 {
	public ImageView icon;
	public TextView title;
}
