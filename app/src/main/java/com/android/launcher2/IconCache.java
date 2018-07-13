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

import java.util.HashMap;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * Cache of application icons. Icons can be made from any thread.
 */
public class IconCache {
	@SuppressWarnings("unused")
	private static final String TAG = "IconCache";

	private static final int INITIAL_ICON_CACHE_CAPACITY = 16 * 3;

	private static class CacheEntry {
		public Bitmap icon;
		public String title;
	}

	private final Bitmap mDefaultIcon;
	private final LauncherApplication mContext;
	private final PackageManager mPackageManager;
	private final HashMap<ComponentName, CacheEntry> mCache = new HashMap<ComponentName, CacheEntry>(
			INITIAL_ICON_CACHE_CAPACITY);
	private int mIconDpi;

	public IconCache(LauncherApplication context) {
		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);

		mContext = context;
		mPackageManager = context.getPackageManager();
		mIconDpi = activityManager.getLauncherLargeIconDensity();

		// need to set mIconDpi before getting default icon
		mDefaultIcon = makeDefaultIcon();
	}

	public Drawable getFullResDefaultActivityIcon() {
		return getFullResIcon(Resources.getSystem(),
				android.R.mipmap.sym_def_app_icon);
	}

	public Drawable getFullResIcon(Resources resources, int iconId) {
		Drawable d;
		try {
			d = resources.getDrawableForDensity(iconId, mIconDpi);
		} catch (Resources.NotFoundException e) {
			d = null;
		}

		return (d != null) ? d : getFullResDefaultActivityIcon();
	}

	public Drawable getFullResIcon(String packageName, int iconId) {
		Resources resources;
		try {
			resources = mPackageManager.getResourcesForApplication(packageName);
		} catch (PackageManager.NameNotFoundException e) {
			resources = null;
		}
		if (resources != null) {
			if (iconId != 0) {
				return getFullResIcon(resources, iconId);
			}
		}
		return getFullResDefaultActivityIcon();
	}

	public Drawable getFullResIcon(ResolveInfo info) {
		return getFullResIcon(info.activityInfo);
	}

	public Drawable getFullResIcon(ActivityInfo info) {

		Resources resources;
		try {
			resources = mPackageManager
					.getResourcesForApplication(info.applicationInfo);
		} catch (PackageManager.NameNotFoundException e) {
			resources = null;
		}
		if (resources != null) {
			int iconId = info.getIconResource();
			if (iconId != 0) {
				return getFullResIcon(resources, iconId);
			}
		}
		return getFullResDefaultActivityIcon();
	}

	private Bitmap makeDefaultIcon() {
		Drawable d = getFullResDefaultActivityIcon();
		Bitmap b = Bitmap.createBitmap(Math.max(d.getIntrinsicWidth(), 1),
				Math.max(d.getIntrinsicHeight(), 1), Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(b);
		d.setBounds(0, 0, b.getWidth(), b.getHeight());
		d.draw(c);
		c.setBitmap(null);
		return b;
	}

	/**
	 * Remove any records for the supplied ComponentName.
	 */
	public void remove(ComponentName componentName) {
		synchronized (mCache) {
			mCache.remove(componentName);
		}
	}

	/**
	 * Empty out the cache.
	 */
	public void flush() {
		synchronized (mCache) {
			// / M: Cause GC free memory
			for (ComponentName cn : mCache.keySet()) {
				CacheEntry e = mCache.get(cn);
				e.icon = null;
				e.title = null;
				e = null;
			}

			mCache.clear();
		}

		if (LauncherLog.DEBUG) {
			LauncherLog.d(TAG, "Flush icon cache here.");
		}
	}

	/**
	 * Fill in "application" with the icon and label for "info."
	 */
	public void getTitleAndIcon(ApplicationInfo application, ResolveInfo info,
			HashMap<Object, CharSequence> labelCache) {
		synchronized (mCache) {
			CacheEntry entry = cacheLocked(application.componentName, info,
					labelCache);

			// M: Added by liudekuan
			// R: For instance, we have update an application which has different component name
			ComponentName cn = new ComponentName(info.activityInfo.packageName,
					info.activityInfo.name);
			if (!application.componentName.equals(cn)
					&& application.title.equals(entry.title)) {
				application.componentName = cn;

				final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
				mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
				mainIntent.setComponent(cn);
				mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				application.intent = mainIntent;

				LauncherModel.updateItemInDatabase(mContext, application);
			}
			// M: End

			application.title = entry.title;
			application.iconBitmap = entry.icon;
		}
	}

	public Bitmap getIcon(Intent intent) {
		synchronized (mCache) {
			final ResolveInfo resolveInfo = mPackageManager.resolveActivity(
					intent, 0);
			ComponentName component = intent.getComponent();

			if (resolveInfo == null || component == null) {
				return mDefaultIcon;
			}

			CacheEntry entry = cacheLocked(component, resolveInfo, null);
			return entry.icon;
		}
	}

	public Bitmap getIcon(ComponentName component, ResolveInfo resolveInfo,
			HashMap<Object, CharSequence> labelCache) {
		synchronized (mCache) {
			if (resolveInfo == null || component == null) {
				return null;
			}

			CacheEntry entry = cacheLocked(component, resolveInfo, labelCache);
			return entry.icon;
		}
	}

	public boolean isDefaultIcon(Bitmap icon) {
		return mDefaultIcon == icon;
	}

	private CacheEntry cacheLocked(ComponentName componentName,
			ResolveInfo info, HashMap<Object, CharSequence> labelCache) {
		if (LauncherLog.DEBUG_LAYOUT) {
			LauncherLog.d(TAG, "cacheLocked: componentName = " + componentName
					+ ", info = " + info
					+ ", HashMap<Object, CharSequence>:size = "
					+ ((labelCache == null) ? "null" : labelCache.size()));
		}

		// M: Added by liudekuan
		ComponentName cn = new ComponentName(info.activityInfo.packageName,
				info.activityInfo.name);
		if (!componentName.equals(cn)) {
			componentName = cn;
		}
		// M: End

		CacheEntry entry = mCache.get(componentName);
		if (entry == null) {
			entry = new CacheEntry();
			// if (mCache.size() < INITIAL_ICON_CACHE_CAPACITY) {
			mCache.put(componentName, entry);
			// }

			ComponentName key = LauncherModel
					.getComponentNameFromResolveInfo(info);
			if (labelCache != null && labelCache.containsKey(key)) {
				entry.title = labelCache.get(key).toString();
				if (LauncherModel.DEBUG_LOADERS) {
					LauncherLog.d(TAG,
							"CacheLocked get title from cache: title = "
									+ entry.title);
				}
			} else {
				entry.title = info.loadLabel(mPackageManager).toString();
				if (LauncherModel.DEBUG_LOADERS) {
					LauncherLog.d(TAG,
							"CacheLocked get title from pms: title = "
									+ entry.title);
				}
				if (labelCache != null) {
					labelCache.put(key, entry.title);
				}
			}

			if (entry.title == null) {
				entry.title = info.activityInfo.name;
				if (LauncherModel.DEBUG_LOADERS) {
					LauncherLog.d(TAG,
							"CacheLocked get title from activity information: entry.title = "
									+ entry.title);
				}
			}
			Drawable resouseDrawable = null;
			// M: by chenxin
			String componentNameString = componentName.getPackageName() + "/"
					+ componentName.getClassName();

			String title = null;
			if (null != LauncherProvider.mInitAppNames) {
				title = LauncherProvider.mInitAppNames.get(componentNameString
						.toString());
			}

			if (title != null) {
				if (LauncherModel.DEBUG_LOADERS) {
					Log.v("title=" + componentName.getClassName(),
							"perfecticon");
					Log.v("title=" + title, "perfecticon2");
				}
				// M: by chenxin
				// If not try NotFoundException exception, app not get appself
				// icon
				// resouseDrawable =
				// mContext.getResources().getDrawable(LauncherProvider.getResourdIdByResourdName(mContext,title));
				try {
					resouseDrawable = mContext.getResources().getDrawable(
							LauncherProvider.getResourdIdByResourdName(
									mContext, title));
				} catch (NotFoundException e) {
					// TODO: handle exception
					e.printStackTrace();
				}

				if (null != resouseDrawable) {
					// modify by xyg
					entry.icon = Utilities.drawableToBitmap(resouseDrawable);
					// entry.icon = Utilities.drawableToBitmap(mContext,
					//		resouseDrawable);
					// end
				} else {
					resouseDrawable = getFullResIcon(info);
				}
			} else {
				resouseDrawable = getFullResIcon(info);
			}
			if (entry.icon == null) {
				entry.icon = Utilities.createIconBitmap(resouseDrawable,
						mContext);
			}
		}
		return entry;
	}

	public HashMap<ComponentName, Bitmap> getAllIcons() {
		synchronized (mCache) {
			HashMap<ComponentName, Bitmap> set = new HashMap<ComponentName, Bitmap>();
			for (ComponentName cn : mCache.keySet()) {
				final CacheEntry e = mCache.get(cn);
				set.put(cn, e.icon);
			}
			return set;
		}
	}
}