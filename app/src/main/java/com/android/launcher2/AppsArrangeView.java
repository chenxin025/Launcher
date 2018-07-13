package com.android.launcher2;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.launcher.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class AppsArrangeView extends RelativeLayout {

	public static final int APP_ENTITY_HEIGHT = 50;

	public static final String TAG = "EtonLauncher";
	private Context mContext;
	private Workspace mWorkspace;
	// we obtain apps info from LauncherModel
	// LauncherModel is the abstrct desc of Launcher, we get it from
	// LaucherApplication
	private LauncherModel mModel;

	private HashMap<Long, FolderInfo> mFolders;
	// the list saved the values of mFolders
	private List<ApplicationInfo> mApps;

	private ListView mListView;
	// save container ids of mApps
	private long[] mContainers;

	private AppsAdapter mAdapter;

	int mCurrPos;

	int mMaxNumItems;

	int mDrawableWidth;
	int mScreenWidth;
	int mScreenHeight;

	public AppsArrangeView(Context context) {
		super(context);
	}

	public AppsArrangeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;

		mModel = ((LauncherApplication) ((Launcher) mContext).getApplication())
				.getModel();
		// init workspace
		mWorkspace = ((Launcher) mContext).getWorkspace();
		// obtain app list from LauncherModel
		mApps = (List<ApplicationInfo>) mModel.getAllAppsList().data;
		mApps = appsFilter(mApps);
		// obtain folder info from LauncherModel
		// we use mModel.sBgFolders directly, because sBgFolders will be updated
		// when some folder
		// be created or removed, then we dont need update it by ourselfves.
		mFolders = (HashMap<Long, FolderInfo>) LauncherModel.sBgFolders;
		// init mContainers
		mContainers = new long[mApps.size()];
		for (int i = 0; i < mContainers.length; i++) {
			mContainers[i] = mApps.get(i).getContainer();
		}

		mMaxNumItems = mContext.getResources().getInteger(
				R.integer.folder_max_num_items);

		DisplayMetrics dm = new DisplayMetrics();
		dm = getResources().getDisplayMetrics();
		mScreenWidth = dm.widthPixels;
		mScreenHeight = dm.heightPixels;
		Drawable drawable = getResources().getDrawable(
				R.drawable.folder_arrage_xuxian);
		mDrawableWidth = drawable.getIntrinsicWidth();
	}

	/**
	 * filter the system apps
	 * 
	 * @param list
	 * @return
	 */
	private List<ApplicationInfo> appsFilter(List<ApplicationInfo> list) {
		if (null == list) {
			return null;
		}

		List<ApplicationInfo> r = new ArrayList<ApplicationInfo>();

		for (int i = 0; i < list.size(); i++) {
			ApplicationInfo info = list.get(i);
			if (!((info.flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) <= 0)) {
				r.add(list.get(i));
			}
		}
		return r;
	}
	
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		super.onFinishInflate();
		// init applist view, and set adapter
		mListView = (ListView) findViewById(R.id.applications_list);
		mAdapter = new AppsAdapter(mContext);
		mListView.setAdapter(mAdapter);
	}
	
	// added by xyg
	public void updateData(){
		mApps = (List<ApplicationInfo>) mModel.getAllAppsList().data;
		mContainers = new long[mApps.size()];
		for (int i = 0; i < mContainers.length; i++) {
			mContainers[i] = mApps.get(i).getContainer();
		}
		mAdapter.notifyDataSetChanged();
	}
	// end
	
	private List<FolderInfo> getFolderList(HashMap<Long, FolderInfo> map) {
		List<FolderInfo> list = new ArrayList<FolderInfo>();
		if (map != null) {
			Iterator<Entry<Long, FolderInfo>> iter = map.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<Long, FolderInfo> entry = (Map.Entry<Long, FolderInfo>) iter
						.next();
				FolderInfo val = (FolderInfo) entry.getValue();
				list.add(val);
			}
		}
		return list;
	}

	public void setContainer(int index, long containerId) {
		mContainers[index] = containerId;
		mAdapter.notifyDataSetChanged();
	}

	static AppsArrangeView fromXml(Context context) {
		return (AppsArrangeView) LayoutInflater.from(context).inflate(
				R.layout.apps_arrange_view, null);
	}

	public void moveApplication(int index, long container) {

		Resources r = ((Launcher) mContext).getResources();

		ApplicationInfo ai = mApps.get(index);

		if (null == ai) {
			if (LauncherLog.DEBUG) {
				LauncherLog.d(TAG, "moveApplication: ai = null");
			}
			return;
		}

		long oriContainer = ai.container;

		if (ai.container == container) {
			Toast.makeText(mContext,
					r.getString(R.string.move_application_failed),
					Toast.LENGTH_SHORT).show();
			return;
		}

		if (container != LauncherSettings.Favorites.CONTAINER_DESKTOP
				&& container != LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
			FolderInfo folderInfo = mFolders.get(container);
			if (folderInfo != null) {
				removeOriIcon(oriContainer, ai);
				folderInfo.add(ai);

				setContainer(index, container);
				Toast.makeText(mContext,
						r.getString(R.string.move_application_successful),
						Toast.LENGTH_SHORT).show();
				return;
			} else {
				Toast.makeText(mContext,
						r.getString(R.string.move_application_failed),
						Toast.LENGTH_SHORT).show();
				return;
			}
		}

		if (container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {

			int[] cell = ((Launcher) mContext).getVacantCell();
			// update db

			if (null == cell) {
				Toast.makeText(mContext,
						r.getString(R.string.move_application_failed),
						Toast.LENGTH_SHORT).show();
				return;
			}

			LauncherModel.moveItemInDatabase(mContext, ai,
					LauncherSettings.Favorites.CONTAINER_DESKTOP, cell[2],
					cell[0], cell[1]);

			removeOriIcon(oriContainer, ai);

			// add view to desktop
			View shortcut = ((Launcher) mContext).createShortcut(ai);
			mWorkspace.addInScreen(shortcut,
					LauncherSettings.Favorites.CONTAINER_DESKTOP, cell[2],
					cell[0], cell[1], 1, 1);

			setContainer(index, container);
			// mContext.sendBroadcast(new
			// Intent(Launcher.ACTION_MOVE_APPLICATION_END));
			Toast.makeText(mContext,
					r.getString(R.string.move_application_successful),
					Toast.LENGTH_SHORT).show();
			return;
		}
	}

	private void removeOriIcon(long container, ItemInfo ii) {
		int actualScreen = LauncherModel.getActualScreen(ii.screen,
				ii.container);
		View child = null;
		if (container != LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
			child = getViewForInfo(container, actualScreen, ii.cellX, ii.cellY);
		} else {
			child = getViewForInfoInHotseat(ii);
		}
		CellLayout cellLayout = null;
		if (container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
			cellLayout = (CellLayout) mWorkspace.getChildAt(actualScreen);
			cellLayout.removeView(child);
		} else if (container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
			cellLayout = ((Launcher) mContext).getHotseat().getLayout();
			cellLayout.removeView(child);
		} else {
			FolderInfo folderInfo = mFolders.get(container);
			if (folderInfo != null && (ii instanceof ShortcutInfo)) {
				folderInfo.remove((ShortcutInfo) ii);
			}
		}
	}

	private View getViewForInfoInHotseat(ItemInfo itemInfo) {
		ShortcutAndWidgetContainer sawc = ((Launcher) mContext).getHotseat()
				.getLayout().getShortcutsAndWidgets();
		for (int i = 0; i < sawc.getChildCount(); i++) {
			View child = sawc.getChildAt(i);
			if (((ItemInfo) child.getTag()) == itemInfo) {
				return child;
			}
		}
		return null;
	}

	private View getViewForInfo(int screen, int cellX, int cellY) {
		CellLayout layout = (CellLayout) mWorkspace.getChildAt(screen);
		View v = layout.getChildAt(cellX, cellY);
		return v;
	}

	private View getViewForInfo(long container, int screen, int cellX, int cellY) {
		if (container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
			return getViewForInfo(screen, cellX, cellY);
		} else if (container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
			// return ((Launcher)mContext).getHotseat().getLayout()
			// .getShortcutsAndWidgets().getChildAt(cellX, cellY);
			ShortcutAndWidgetContainer sawc = ((Launcher) mContext)
					.getHotseat().getLayout().getShortcutsAndWidgets();
			return sawc.getChildAt(cellX);
		} else {
			return null;
		}
	}

	private String getContainerTitle(long id) {
		if (id == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
			return mContext.getString(R.string.container_desktop);
		}
		if (id == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
			return mContext.getString(R.string.container_hotseat);
		}
		if (mFolders.containsKey(id)) {
			String title = mFolders.get(id).getTitle();
			if (title != null) {
				return title;
			} else {
				return ((Launcher) mContext).getResources().getString(
						R.string.default_folder_name);
			}
		}

		return mContext.getString(R.string.container_desktop);
	}

	@Override
	public boolean onTouchEvent(MotionEvent arg0) {
		return true;
	}

	private FolderSelectWindow mPrePupWindow;

	class AppsAdapter extends BaseAdapter {

		LayoutInflater mInfater = null;

		public AppsAdapter(Context context) {
			mInfater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return mApps.size();
		}

		@Override
		public Object getItem(int position) {
			return mApps.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertview, ViewGroup arg2) {

			ViewHolder holder = null;
			if (null == convertview) {
				holder = new ViewHolder();
				convertview = mInfater.inflate(R.layout.app_entity, null);
				holder.appIcon = (ImageView) convertview
						.findViewById(R.id.folder_arrage_app_icon);
				holder.title = (TextView) (convertview
						.findViewById(R.id.app_name));
				holder.dottedLayout = (LinearLayout) convertview
						.findViewById(R.id.dotted_layout);
				holder.containerLayout = (LinearLayout) convertview
						.findViewById(R.id.container_layout);
				holder.containerTitle = (TextView) convertview
						.findViewById(R.id.container);
				convertview.setTag(holder);
			} else {
				holder = (ViewHolder) convertview.getTag();
			}

			ApplicationInfo app = (ApplicationInfo) getItem(position);

			IconCache iconCache = mModel.getIconCache();

			holder.appIcon.setImageBitmap(app.getIcon(iconCache));

			holder.title.setText(app.getTitle() != null ? app.getTitle() : "");
			// holder.containerTitle.setText(getContainerTitle(app.getContainer()));
			holder.containerTitle
					.setText(getContainerTitle(mContainers[position]));

			holder.containerLayout.setTag(position);

			holder.containerLayout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					if (mPrePupWindow != null && mPrePupWindow.isShowing()) {
						mPrePupWindow.dismiss();
					}
					List<FolderInfo> list = getFolderList(mFolders);
					FolderSelectWindow fsw = new FolderSelectWindow(mContext,
							AppsArrangeView.this, list);
					mPrePupWindow = fsw;
					fsw.setIndex((Integer) arg0.getTag());
					int[] loc = new int[2];
					arg0.getLocationOnScreen(loc);
					float desity = mContext.getResources().getDisplayMetrics().density;
					;
					int fswItemHeight = (int) (FolderSelectWindow.ITEM_HEIGHT * desity);
					int fswHeight = fswItemHeight * 5;
					if (loc[1] > mScreenHeight / 2) {
						Drawable bg = getResources().getDrawable(
								R.drawable.menu_dropdown_panel_holo_light_down);
						fsw.setBackgroundDrawable(bg);
						fsw.setHeight(fswHeight);
						fsw.showAtLocation(arg0,
								Gravity.RIGHT | Gravity.BOTTOM, mScreenWidth
										- 2 * loc[0] - 30, mScreenHeight
										- loc[1] - arg0.getHeight() / 2 + 20);
					} else {
						Drawable bg = getResources().getDrawable(
								R.drawable.menu_dropdown_panel_holo_light_up);
						fsw.setBackgroundDrawable(bg);
						fsw.setHeight(fswHeight);
						fsw.showAtLocation(arg0, Gravity.RIGHT | Gravity.TOP,
								mScreenWidth - 2 * loc[0] - 30,
								loc[1] + arg0.getHeight() / 2 + 20);
					}
				}
			});

			int w = View.MeasureSpec.makeMeasureSpec(0,
					View.MeasureSpec.UNSPECIFIED);
			int h = View.MeasureSpec.makeMeasureSpec(0,
					View.MeasureSpec.UNSPECIFIED);
			holder.appIcon.measure(w, h);
			int appIconWidth = holder.appIcon.getMeasuredWidth();
			holder.containerTitle.measure(w, h);
			int appNameWidth = holder.title.getMeasuredWidth();
			holder.containerTitle.measure(w, h);
			int rightWidth = holder.containerTitle.getMeasuredWidth();
			int leftSpace = mScreenWidth - appIconWidth - appNameWidth
					- rightWidth - 60;
			int count = leftSpace / mDrawableWidth;
			// if(holder.dottedLayout.getChildCount() < count){
			holder.dottedLayout.removeAllViews();
			for (int i = 0; i < count; i++) {
				ImageView imageView = new ImageView(mContext);
				imageView.setImageResource(R.drawable.folder_arrage_xuxian);
				holder.dottedLayout.addView(imageView);
			}
			// }
			return convertview;
		}
	}

	class ViewHolder {
		ImageView appIcon;
		TextView title;
		LinearLayout dottedLayout;
		LinearLayout containerLayout;
		TextView containerTitle;
	}

	public boolean isFull(long id) {

		int vacantCount = 0;

		FolderInfo folderInfo = mFolders.get(id);
		if (folderInfo != null) {
			if (folderInfo.contents != null) {
				vacantCount = mMaxNumItems - folderInfo.contents.size();
			} else {
				return true;
			}
		} else {
			if (LauncherLog.DEBUG) {
				LauncherLog.d(TAG, "folderInfo == null");
			}
			return false;
		}

		int num = 0;
		for (int i = 0; i < mContainers.length; i++) {
			if (mContainers[i] == id
					&& !folderInfo.contents.contains(mApps.get(i))) {
				num++;
			}
		}

		if (num < vacantCount) {
			return false;
		}
		return true;
	}
}
