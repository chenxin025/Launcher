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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;

import com.android.launcher.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores the list of all applications for the all apps view.
 */
class AllAppsList {
    private static final String TAG = "AllAppsList";

    /// M: add for top packages.
    private static final String TAG_TOPPACKAGES = "toppackages";
    private static final String WIFI_SETTINGPKGNAME = "com.android.settings";
    private static final String WIFI_SETTINGCLASSNAME = "com.android.settings.Settings$WifiSettingsActivity";

    /// M: add for Wifi Settings {@
    private boolean mRemovedWifiSettings = false;
    /// M: @}

    private static final boolean DEBUG_LOADERS_REORDER = false;
    public static final int DEFAULT_APPLICATIONS_NUMBER = 42;

    /** The list off all apps. */
    public ArrayList<ApplicationInfo> data = new ArrayList<ApplicationInfo>(
            DEFAULT_APPLICATIONS_NUMBER);

    /** The list of apps that have been added since the last notify() call. */
    public ArrayList<ApplicationInfo> added = new ArrayList<ApplicationInfo>(
            DEFAULT_APPLICATIONS_NUMBER);
    /** The list of apps that have been removed since the last notify() call. */
    public ArrayList<ApplicationInfo> removed = new ArrayList<ApplicationInfo>();
    /** The list of apps that have been modified since the last notify() call. */
    public ArrayList<ApplicationInfo> modified = new ArrayList<ApplicationInfo>();

    /**
     * M: The list of appWidget that have been removed since the last notify()
     * call.
     */
    public ArrayList<String> appwidgetRemoved = new ArrayList<String>();

    private IconCache mIconCache;

    /// M: add for top packages.
    static ArrayList<TopPackage> sTopPackages = null;
    
    // added by liudekuan
    private LauncherModel mModel;
    // we get vacant cell using LauncherModel, so model has to been obtained
    public void setModel (LauncherModel model) {
    	if (null == mModel) {
    		mModel = model;
    	}
    }
    // end
    static class TopPackage {
        public TopPackage(String pkgName, String clsName, int index) {
            packageName = pkgName;
            className = clsName;
            order = index;
        }

        String packageName;
        String className;
        int order;
    }

    /**
     * Boring constructor.
     */
    public AllAppsList(IconCache iconCache) {
        mIconCache = iconCache;
    }

    /**
     * Add the supplied ApplicationInfo objects to the list, and enqueue it into the
     * list to broadcast when notify() is called.
     *
     * If the app is already in the list, doesn't add it.
     */
    public void add(ApplicationInfo info) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "Add application in app list: app = " + info.componentName
                    + ", title = " + info.title);
        }

        /// M: Modified by liudekuan
//        if (findActivity(data, info.componentName)) {
//            return;
//        }
        removeItemInfoByComponentName(data, info.componentName);
        removeItemInfoByComponentName(added, info.componentName);
        /// M: End
        data.add(info);
        added.add(info);
    }

    public void clear() {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "clear all data in app list: app size = " + data.size());
        }

        data.clear();
        // TODO: do we clear these too?
        added.clear();
        removed.clear();
        modified.clear();
        /// M: clear appWidgetRemoved.
        appwidgetRemoved.clear();

        /// M: remove extra icons
        mRemovedWifiSettings = false;
    }

    public int size() {
        return data.size();
    }

    public ApplicationInfo get(int index) {
        return data.get(index);
    }

    /**
     * Add the icons for the supplied apk called packageName.
     */
    public void addPackage(Context context, String packageName) {
        final List<ResolveInfo> matches = findActivitiesForPackage(context, packageName);

        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "addPackage: packageName = " + packageName + ", matches = " + matches.size());
        }

        if (matches.size() > 0) {
            for (ResolveInfo info : matches) {
            	
            	/// M: Added by liudekuan
            	ApplicationInfo applicationInfo = findApplicationInfoLocked(context, info);
            	
            	if (applicationInfo != null) {
            		if (LauncherLog.DEBUG) {
                        LauncherLog.d(TAG, "addPackage: application != null ");
                    }
            		continue;
            	}
            	/// M: End
            	
            	ApplicationInfo appInfo = new ApplicationInfo(context.getPackageManager(), info, mIconCache, null);
            	
            	ApplicationInfo ai = hasInstalled(LauncherModel.mExternalApps, appInfo);
            	
            	appInfo.container = (ai != null ? ai.container : LauncherSettings.Favorites.CONTAINER_DESKTOP);
            	appInfo.isNew = (ai != null ? ai.isNew : LauncherSettings.Favorites.ITEM_IS_NEW);
            	appInfo.calledNum = (ai != null ? ai.calledNum : 0);
            	
				if (LauncherLog.DEBUG) {
					LauncherLog.d(
							TAG,
							"addPackage: title = " + appInfo.title
									+ ", package = "
									+ appInfo.componentName.getPackageName()
									+ ", class = "
									+ appInfo.componentName.getClassName());
				}
            	
            	/// M: Added by liudekuan
            	/// R: If during SD is unmounted, the empty folder some SD apps has stored is removed, we need 
            	///    find new positions for them and set them on desktop.
            	boolean flag = false;
            	if (LauncherModel.beInFolder(appInfo) && (null == LauncherModel.sBgFolders.get(appInfo.container))) {
            		
            		if (LauncherLog.DEBUG) {
            			LauncherLog.d(TAG, "LauncherModel.beInFolder(appInfo) " +
            					"&& (null == LauncherModel.sBgFolders.get(appInfo.container))");
            		}
            		
            		appInfo.container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
            		flag = true;
            	}
            	/// M: End
            	
            	if (LauncherLog.DEBUG) {
        			LauncherLog.d(TAG, "flag = " + flag + ", ai = null: " + (ai == null));
        		}
            	
            	if (ai != null && !isOcuppied(ai) && !flag) {
            		
            		if (LauncherLog.DEBUG) {
                        LauncherLog.d(TAG, "addPackage: ai != null && !isOcuppied(ai)");
                    }
            		
            		appInfo.screen = LauncherModel.getActualScreen(ai.screen, ai.container);
            		appInfo.cellX = ai.cellX;
            		appInfo.cellY = ai.cellY;
            	} else {
            		// M: Added by liudekuan
            		int[] p = mModel.findVacantCell(context);
                    if (null == p)  {
                      	return;
                    }
                    /// M: Added on 2014-02-13
                    /// R: If appInfo.containter is some folder, reset it be desktop.
                    appInfo.container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
                    /// M: End
                    appInfo.screen = p[0];
                    appInfo.cellX = p[1];
                    appInfo.cellY = p[2];
            	}
            	
             	LauncherModel.addItemToDatabase(context, appInfo, appInfo.container,
            			appInfo.screen,appInfo.cellX, appInfo.cellY, false);
             	
                add(appInfo);
                Log.v(appInfo.intent+"","agoodfew");
            }
        }else{
        	LauncherModel.setAppAdding(false);
        }
    }
    
    /**
     * M: Added by liudekuan
     * @author Administrator liudekuan
     */
    public ApplicationInfo hasInstalled (List<ApplicationInfo> list, ApplicationInfo info) {
    	
    	if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "hasInstalled: " + LauncherModel.mExternalApps.size());
        }
    	
    	if (null == list || list.size() == 0) {
    		return null;
    	}
    	
    	for (int i = list.size() - 1; i >= 0; i --) {
    		ApplicationInfo ai = list.get(i);
    		/// M: Modified by liudekuan
//    		if (ai.intent.getComponent().getPackageName().equals(info.componentName.getPackageName())
//    				&& ai.intent.getComponent().getClassName().equals(info.componentName.getClassName())) {
//    			if (LauncherLog.DEBUG) {
//    	            LauncherLog.d(TAG, "hasInstalled: " + list.get(i).intent.getComponent().getPackageName());
//    	        }
//    			return list.remove(i);
//    		}
    		if (ai.intent.getComponent().getPackageName().equals(info.componentName.getPackageName())) {
    			if (LauncherLog.DEBUG) {
    	            LauncherLog.d(TAG, "hasInstalled: " + list.get(i).intent.getComponent().getPackageName());
    	        }
    			return list.remove(i);
    		}
    		/// M: End
    	}
    	return null;
    }
    
    /**
     * 
     * @author Administrator liudekuan
     */
    public boolean isOcuppied (ApplicationInfo info) {
    	
    	/// M: If we remove screens before updating external apps, we set apps in this screen as ocuppied.
    	if (LauncherModel.getActualScreen(info.screen, info.container) == Launcher.ERROR) {
    		if (LauncherLog.DEBUG) {
				LauncherLog.d(TAG,
						"LauncherModel.getActualScreen(info.screen, info.container) == Launcher.ERROR");
    		}
    		return true;
    	}
    	
    	if (info.container != LauncherSettings.Favorites.CONTAINER_DESKTOP
    			&& info.container != LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
    		
    		if (LauncherLog.DEBUG) {
	            LauncherLog.d(TAG, "isOcuppied: container is folder, return false");
	        }
    		
    		/// M: Added on 2014-02-13
    		/// R: If the folder has been full, return true.
    		if (mModel.isFolderFull(info.container)) {
    			return true;
    		}
    		/// M: End
    		
    		return false;
    	}
    	
    	if (LauncherModel.sBgWorkspaceItems != null && LauncherModel.sBgWorkspaceItems.size() > 0) {
    		for (ItemInfo ii : LauncherModel.sBgWorkspaceItems) {
    			if (ii.screen != info.screen || ii.container != info.container) {
    				continue;
    			}
    			if (ii.cellX == info.cellX && ii.cellY == info.cellY) {
    				if (LauncherLog.DEBUG) {
    		            LauncherLog.d(TAG, "isOcuppied: is occupied by app!");
    		            LauncherLog.d(TAG, " ii.screen = " + ii.screen + ", ii.container = " + ii.container + ", " +
    		            		"ii.cellX = " + ii.cellX + ", ii.cellY = " + ii.cellY + "ii.title = " + ii.title);
    		            LauncherLog.d(TAG, "info.screen = " + info.screen + ", info.container = " + info.container + ", " +
    		            		"ii.cellX = " + info.cellX + ", info.cellY = " + info.cellY + ", info.title = " + info.title);
    		        }
    				return true;
    			}
    		}
    	}
    	
    	if (LauncherModel.sBgAppWidgets != null && LauncherModel.sBgAppWidgets.size() > 0) {
    		for (ItemInfo ii : LauncherModel.sBgAppWidgets) {
    			if (ii.screen != info.screen || ii.container != info.container) {
    				continue;
    			}
    			if ((info.cellX >= ii.cellX && (info.cellX < ii.cellX + ii.spanX)) 
    					&& info.cellY >= ii.cellY && (info.cellY < ii.cellY + ii.spanY)) {
    				if (LauncherLog.DEBUG) {
    		            LauncherLog.d(TAG, "isOcuppied: is occupied by widget!");
    		        }
    				return true;
    			}
    		}
    	}
    	if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "isOcuppied: return false!");
        }
    	return false;
    }
    
    /**
     * Remove the apps for the given apk identified by packageName.
     */
    public void removePackage(String packageName,Context context) {
        final List<ApplicationInfo> data = this.data;
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "removePackage: packageName = " + packageName + ", data size = " + data.size());
        }
        
        for (int i = data.size() - 1; i >= 0; i--) {
            ApplicationInfo info = data.get(i);
            Log.v(info.intent+"","agoodfew");
            final ComponentName component = info.intent.getComponent();
            if (packageName.equals(component.getPackageName())) {
            	
            	// M: Added by liudekuan
                if (LauncherModel.needSaveAppsRemoved()) {
                	LauncherModel.mExternalApps.add(info);
                	if (LauncherLog.DEBUG) {
						LauncherLog.d(TAG,
								"removePackage: add to mExternalApps: "
										+ info.componentName.getPackageName());
                	}
                }
                // M: End
            	
        		//modified by ETON guolinan
            	LauncherModel.deleteItemFromDatabase(context, info);
        		//end
                removed.add(info);
                data.remove(i);
            }
        }
        // This is more aggressive than it needs to be.
        mIconCache.flush();
    }

    /**
     * Add and remove icons for this package which has been updated.
     */
    public void updatePackage(Context context, String packageName) {
        final List<ResolveInfo> matches = findActivitiesForPackage(context, packageName);
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "updatePackage: packageName = " + packageName + ", matches = " + matches.size());
        }
        
        for (ResolveInfo ri : matches) {
        	if (LauncherLog.DEBUG) {
                LauncherLog.d(TAG, "updatePackage: ri.packageName = " + ri.activityInfo.packageName);
                LauncherLog.d(TAG, "updatePackage: ri.name = " + ri.activityInfo.name);
            }
        }

        if (matches.size() > 0) {
            // Find disabled/removed activities and remove them from data and add them
            // to the removed list.
        	
        	// M: Removed by liudekuan
//            for (int i = data.size() - 1; i >= 0; i--) {
//                final ApplicationInfo applicationInfo = data.get(i);
//                final ComponentName component = applicationInfo.intent.getComponent();
//                
//                if (LauncherLog.DEBUG) {
//                    LauncherLog.d(TAG, "updatePackage: component = " + component);
//                }
//                if (packageName.equals(component.getPackageName())) {
//                    if (!findActivity(matches, component)) {
//                    	if (LauncherLog.DEBUG) {
//                            LauncherLog.d(TAG, "updatePackage: findActivity = ");
//                        }
//                        removed.add(applicationInfo);
//                        mIconCache.remove(component);
//                        data.remove(i);
//                    }
//                }
//            }
        	// M: End

            // Find enabled activities and add them to the adapter
            // Also updates existing activities with new labels/icons
            int count = matches.size();
            
            for (int i = 0; i < count; i++) {
                final ResolveInfo info = matches.get(i);
                final String pkgName = info.activityInfo.applicationInfo.packageName;
                final String className = info.activityInfo.name;
                // M: Added by liudekuan
                PackageManager pm = context.getPackageManager();
                final String applicationName = info.loadLabel(pm).toString();
                // M: End
                
                if (LauncherLog.DEBUG) {
                    LauncherLog.d(TAG, "updatePackage: pkgName = " + pkgName + ", className = " + className);
                }
                
                // Modified by ygxing
                /*if ((LauncherExtPlugin.getAllAppsListExt(context) == null || !LauncherExtPlugin.getAllAppsListExt(context).isShowWifiSettings())
                        && WIFI_SETTINGPKGNAME.equals(pkgName)
                        && WIFI_SETTINGCLASSNAME.equals(className)) {
                	if (LauncherLog.DEBUG) {
                		LauncherLog.d(TAG, "updatePackage: LauncherExtPlugin.getAllAppsListExt(context) == null");
                	}
                    continue;
                }*/
                // end
                
                // M: Modified by liudekuan
                //ApplicationInfo applicationInfo = findApplicationInfoLocked(pkgName, className);
                ApplicationInfo applicationInfo = findApplicationInfoLocked(pkgName, className, applicationName);
                // M: End
                if (LauncherLog.DEBUG) {
                    LauncherLog.d(TAG, "updatePackage: applicationInfo == null " + (applicationInfo == null));
                }
                if (applicationInfo == null) {
                	ApplicationInfo appInfo = new ApplicationInfo(context.getPackageManager(), info, mIconCache, null);
                	
                	ApplicationInfo ai = hasInstalled(LauncherModel.mExternalApps, appInfo);
                	
                	appInfo.container = (ai != null ? ai.container : LauncherSettings.Favorites.CONTAINER_DESKTOP);
//                	appInfo.isNew = (ai != null ? ai.isNew : LauncherSettings.Favorites.ITEM_NOT_NEW);
                	appInfo.isNew = LauncherSettings.Favorites.ITEM_NOT_NEW;
                	appInfo.calledNum = 1;
                	
                	/// M: Added by liudekuan
                	/// R: If during SD is unmounted, the empty folder some SD apps has stored is removed, we need 
                	///    find new positions for them and set them on desktop.
                	boolean flag = false;
                	if (LauncherModel.beInFolder(appInfo) && (null == LauncherModel.sBgFolders.get(appInfo.container))) {
                		
                		if (LauncherLog.DEBUG) {
                			LauncherLog.d(TAG, "LauncherModel.beInFolder(appInfo) " +
                					"&& (null == LauncherModel.sBgFolders.get(appInfo.container))");
                		}
                		
                		appInfo.container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
                		flag = true;
                	}
                	/// M: End
                	
                	if (LauncherLog.DEBUG) {
            			LauncherLog.d(TAG, "flag = " + flag + ", ai = null: " + (ai == null));
            		}
                	
                	if (ai != null && !isOcuppied(ai) && !flag) {
                		
                		if (LauncherLog.DEBUG) {
                            LauncherLog.d(TAG, "updatePackage: ai != null && !isOcuppied(ai)");
                        }
                		
                		appInfo.screen = LauncherModel.getActualScreen(ai.screen, ai.container);
                		appInfo.cellX = ai.cellX;
                		appInfo.cellY = ai.cellY;
                	} else {
                		// M: Added by liudekuan
                		int[] p = mModel.findVacantCell(context);
                        if (null == p)  {
                          	return;
                        }
                        appInfo.screen = p[0];
                        appInfo.cellX = p[1];
                        appInfo.cellY = p[2];
                	}
                	
                	if (LauncherLog.DEBUG) {
                        LauncherLog.d(TAG, "updatePackage: appInfo.container = " + appInfo.container);
                    }
                	
                 	LauncherModel.addItemToDatabase(context, appInfo, appInfo.container,
                			appInfo.screen, appInfo.cellX, appInfo.cellY, false);
                 	
                 	add(appInfo);
                } else {
                    mIconCache.remove(applicationInfo.componentName);
                    mIconCache.getTitleAndIcon(applicationInfo, info, null);
                    modified.add(applicationInfo);
                }
            }
        } else {
            // Remove all data for this package.
            for (int i = data.size() - 1; i >= 0; i--) {
                final ApplicationInfo applicationInfo = data.get(i);
                final ComponentName component = applicationInfo.intent.getComponent();
                if (packageName.equals(component.getPackageName())) {
                    if (LauncherLog.DEBUG) {
                        LauncherLog.d(TAG, "Remove application from launcher: component = " + component);
                    }
                    removed.add(applicationInfo);
                    mIconCache.remove(component);
                    data.remove(i);
                }
                /// M: only appWidget, if removed ,place in appWidgetRemoved.
                if (removed.size() == 0) {
                    appwidgetRemoved.add(packageName);
                }
            }
        }
    }

    /**
     * Query the package manager for MAIN/LAUNCHER activities in the supplied package.
     */
    private static List<ResolveInfo> findActivitiesForPackage(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();

        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mainIntent.setPackage(packageName);

        final List<ResolveInfo> apps = packageManager.queryIntentActivities(mainIntent, 0);
        return apps != null ? apps : new ArrayList<ResolveInfo>();
    }

    /**
     * Returns whether <em>apps</em> contains <em>component</em>.
     */
    private static boolean findActivity(List<ResolveInfo> apps, ComponentName component) {
        final String className = component.getClassName();
        for (ResolveInfo info : apps) {
            final ActivityInfo activityInfo = info.activityInfo;
            if (activityInfo.name.equals(className)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether <em>apps</em> contains <em>component</em>.
     */
    private static boolean findActivity(ArrayList<ApplicationInfo> apps, ComponentName component) {
        final int N = apps.size();
        for (int i = 0; i < N; i++) {
            final ApplicationInfo info = apps.get(i);
            if (info.componentName.equals(component)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Before adding new data to data, we should first remove old same data.
     * @param apps
     * @author Administrator liudekuan
     */
    private static void removeItemInfoByComponentName (ArrayList<ApplicationInfo> apps, ComponentName component) {
    	final int N = apps.size();
        for (int i = N - 1; i >= 0; i--) {
            final ApplicationInfo info = apps.get(i);
            if (info.componentName.equals(component)) {
                apps.remove(info);
            }
        }
    }

    /**
     * Find an ApplicationInfo object for the given packageName and className.
     */
    private ApplicationInfo findApplicationInfoLocked(String packageName, String className) {
        for (ApplicationInfo info : data) {
            final ComponentName component = info.intent.getComponent();
            
            // M: Added by liudekuan
            // R: locate app just by packagename
//            if (packageName.equals(component.getPackageName())
//                    && className.equals(component.getClassName())) {
//                return info;
//            }
            
            if (packageName.equals(component.getPackageName())) {
                return info;
            }
            // M: End
        }
        return null;
    }
    
    /**
     * Find an ApplicationInfo object for the given packageName and title, or given packageName and className
     * @author Administrator liudekuan
     */
    private ApplicationInfo findApplicationInfoLocked(String packageName, String className, String title) {
    	
		synchronized (LauncherModel.sBgLock) {
			for (ApplicationInfo info : data) {
				final ComponentName component = info.intent.getComponent();
				// Note: if two apps have same packageName and className, or
				// have same packageName and title,
				// we consider they are the same app.
				if (LauncherModel
						.beSameApp(info, packageName, className, title)) {
					if (LauncherLog.DEBUG) {
						LauncherLog.d(TAG, "info.title=" + info.title
								+ "; title=" + title);
					}
					return info;	
				}
			}
			if (LauncherLog.DEBUG) {
				LauncherLog.d(TAG, "cant find same activity");
			}
			return null;
		}
    }
    
    /// M: Added by liudekuan
    private ApplicationInfo findApplicationInfoLocked(Context c, ResolveInfo info) {
		return findApplicationInfoLocked(
				info.activityInfo.applicationInfo.packageName,
				info.activityInfo.applicationInfo.className,
				info.loadLabel(c.getPackageManager()).toString());
    }
    /// M: End
    
    /**
     * M: Load the default set of default top packages from an xml file.
     *
     * @param context
     * @return true if load successful.
     */
    static boolean loadTopPackage(final Context context) {
        boolean bRet = false;
        if (sTopPackages != null) {
            return bRet;
        }

        sTopPackages = new ArrayList<TopPackage>();

        try {
            XmlResourceParser parser = context.getResources().getXml(R.xml.default_toppackage);
            AttributeSet attrs = Xml.asAttributeSet(parser);
            //XmlUtils.beginDocument(parser, TAG_TOPPACKAGES);

            final int depth = parser.getDepth();

            int type = -1;
            while (((type = parser.next()) != XmlPullParser.END_TAG || parser.getDepth() > depth)
                    && type != XmlPullParser.END_DOCUMENT) {

                if (type != XmlPullParser.START_TAG) {
                    continue;
                }

                TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TopPackage);

                sTopPackages.add(new TopPackage(a.getString(R.styleable.TopPackage_topPackageName),
                        a.getString(R.styleable.TopPackage_topClassName), a.getInt(
                                R.styleable.TopPackage_topOrder, 0)));

                LauncherLog.d(TAG, "loadTopPackage: packageName = "
                        + a.getString(R.styleable.TopPackage_topPackageName)
                        + ", className = "
                        + a.getString(R.styleable.TopPackage_topClassName));

                a.recycle();
            }
        } catch (XmlPullParserException e) {
            LauncherLog.w(TAG, "Got XmlPullParserException while parsing toppackage.", e);
        } catch (IOException e) {
            LauncherLog.w(TAG, "Got IOException while parsing toppackage.", e);
        }

        return bRet;
    }

    /**
     * M: Get the index for the given appInfo in the top packages.
     * 
     * @param appInfo
     * @return the index of the given appInfo.
     */
    static int getTopPackageIndex(final ApplicationInfo appInfo) {
        int retIndex = -1;
        if (sTopPackages == null || sTopPackages.isEmpty() || appInfo == null) {
            return retIndex;
        }

        for (TopPackage tp : sTopPackages) {
            if (appInfo.componentName.getPackageName().equals(tp.packageName)
                    && appInfo.componentName.getClassName().equals(tp.className)) {
                retIndex = tp.order;
                break;
            }
        }

        return retIndex;
    }

    /**
     * M: Reorder all apps index according to TopPackages.
     */
    void reorderApplist() {
        final long sortTime = DEBUG_LOADERS_REORDER ? SystemClock.uptimeMillis() : 0;

        if (sTopPackages == null || sTopPackages.isEmpty()) {
            return;
        }
        ensureTopPackageOrdered();

        final ArrayList<ApplicationInfo> dataReorder = new ArrayList<ApplicationInfo>(
                DEFAULT_APPLICATIONS_NUMBER);

        for (TopPackage tp : sTopPackages) {
            int loop = 0;
            for (ApplicationInfo ai : added) {
                if (DEBUG_LOADERS_REORDER) {
                    LauncherLog.d(TAG, "reorderApplist: remove loop = " + loop);
                }

                if (ai.componentName.getPackageName().equals(tp.packageName)
                        && ai.componentName.getClassName().equals(tp.className)) {
                    if (DEBUG_LOADERS_REORDER) {
                        LauncherLog.d(TAG, "reorderApplist: remove packageName = "
                                + ai.componentName.getPackageName());
                    }
                    data.remove(ai);
                    dataReorder.add(ai);
                    dumpData();
                    break;
                }
                loop++;
            }
        }

        for (TopPackage tp : sTopPackages) {
            int loop = 0;
            int newIndex = 0;
            for (ApplicationInfo ai : dataReorder) {
                if (DEBUG_LOADERS_REORDER) {
                    LauncherLog.d(TAG, "reorderApplist: added loop = " + loop + ", packageName = "
                            + ai.componentName.getPackageName());
                }

                if (ai.componentName.getPackageName().equals(tp.packageName)
                        && ai.componentName.getClassName().equals(tp.className)) {
                    newIndex = Math.min(Math.max(tp.order, 0), added.size());
                    if (DEBUG_LOADERS_REORDER) {
                        LauncherLog.d(TAG, "reorderApplist: added newIndex = " + newIndex);
                    }
                    /// M: make sure the array list not out of bound
                    if (newIndex < data.size()) {
                        data.add(newIndex, ai);
                    } else {
                        data.add(ai);
                    }
                    dumpData();
                    break;
                }
                loop++;
            }
        }

        if (added.size() == data.size()) {
            added = (ArrayList<ApplicationInfo>) data.clone();
            LauncherLog.d(TAG, "reorderApplist added.size() == data.size()");
        }

        if (DEBUG_LOADERS_REORDER) {
            LauncherLog.d(TAG, "sort and reorder took " + (SystemClock.uptimeMillis() - sortTime) + "ms");
        }
    }

    /**
     * Dump application informations in data.
     */
    void dumpData() {
        int loop2 = 0;
        for (ApplicationInfo ai : data) {
            if (DEBUG_LOADERS_REORDER) {
                LauncherLog.d(TAG, "reorderApplist data loop2 = " + loop2);
                LauncherLog.d(TAG, "reorderApplist data packageName = "
                        + ai.componentName.getPackageName());
            }
            loop2++;
        }
    }

    /**
     * M: Remove wifisettings in apps list.
     */
    void removeWifiSettings() {
        if (!mRemovedWifiSettings) {
            mRemovedWifiSettings = removeSpecificApp(WIFI_SETTINGPKGNAME, WIFI_SETTINGCLASSNAME);
        }
    }

    private boolean removeSpecificApp(final String packageName, final String className) {
        ApplicationInfo appInfo = null;
        for (ApplicationInfo ai : added) {
            if (ai.componentName.getPackageName().equalsIgnoreCase(packageName)
                    && ai.componentName.getClassName().equalsIgnoreCase(className)) {
                appInfo = ai;
                break;
            }
        }

        if (appInfo != null) {
            data.remove(appInfo);
            added.remove(appInfo);
            LauncherLog.d(TAG, "Success to remove from app list: " + className);
            return true;
        }
        LauncherLog.d(TAG, "Fail to remove from app list: " + className);
        return false;
    }

    /*
     * M: ensure the items from top_package.xml is in order, 
     * for some special case of top_package.xml will make the arraylist out of bound.
     */

    static void ensureTopPackageOrdered() {
        ArrayList<TopPackage> tpOrderList = new ArrayList<TopPackage>(DEFAULT_APPLICATIONS_NUMBER);
        boolean bFirst = true;
        for (TopPackage tp : sTopPackages) {
            if (bFirst) {
                tpOrderList.add(tp);
                bFirst = false;
            } else {
                for (int i = tpOrderList.size() - 1; i >= 0; i--) {
                    TopPackage tpItor = tpOrderList.get(i);
                    if (0 == i) {
                        if (tp.order < tpOrderList.get(0).order) {
                            tpOrderList.add(0, tp);
                        } else {
                            tpOrderList.add(1, tp);
                        }
                        break;
                    }
                    
                    if ((tp.order < tpOrderList.get(i).order)
                        && (tp.order >= tpOrderList.get(i - 1).order)) {
                        tpOrderList.add(i, tp);
                        break;
                    } else if (tp.order > tpOrderList.get(i).order) {
                        tpOrderList.add(i + 1, tp);
                        break;
                    }
                }
            }
        }

        if (sTopPackages.size() == tpOrderList.size()) {
        	sTopPackages = (ArrayList<TopPackage>) tpOrderList.clone();
        	tpOrderList = null;
            LauncherLog.d(TAG, "ensureTopPackageOrdered done");
        } else {
        	LauncherLog.d(TAG, "some mistake may occur when ensureTopPackageOrdered");
        }
    }
}
