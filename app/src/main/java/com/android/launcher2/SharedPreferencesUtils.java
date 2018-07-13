package com.android.launcher2;


import android.content.Context;
import android.content.SharedPreferences;

import com.android.launcher.R;

/**
 * EYO�? * 用于保存workspace相关信息的工具，如屏幕数量，默认屏等
 * @author leesidong
 *
 */
public class SharedPreferencesUtils {
	
	//static final String WORKSPACE_PREFERENCES = "launcher.preferences.workspace";
	static final String WORKSPACE_PREFERENCES = "com.android.launcher_preferences";
	static final String WROKSPACE_PREFERENCES_GUEST_MODE = "com.android.launcher_preferences_guest_mode"; // jeffliu eton add
	
	static final String DESKTOP_SCREENS = "desktopScreens";
	static final String DEFAULT_SCREEN = "defaultScreen";
	static final String IS_SHOW_APPS_TYPE_GRID = "is_show_apps_grid";
	static final String WORKSACE_EFFECT = "pref_workspace_effect";
	static final String ALL_APPLICATIONS_EFFECT = "pref_all_app_effect";
	//added by chenxin
	static final String UI_WHICH_SCREEN_POSITION = "pref_screen_edit_screen";
	static final String UI_INSCREEN_POSITION = "pref_screen_edit_posion_inscreen";
	
	//add by huryjiang
	static final String FAVORITES_SCREENS_NUM = "favorites_screens_num";
	//end by huryjiang
	
	/// M: Added by liudekuan
	static final String DISPLAY_MODE = "display_mode";
	static final String MODE_CHANGED = "mode_changed";
	/// M: End
	
	public static int getDesktopScreens(Context context) {
		// jeffliu eton change
		// SharedPreferences sp = context.getSharedPreferences(WORKSPACE_PREFERENCES, Context.MODE_PRIVATE);
		SharedPreferences sp = getSharedPreferences(context);
		int screens = sp.getInt(DESKTOP_SCREENS, context.getResources().getInteger(R.integer.config_desktopScreens));
		return screens;
	}
	
	public static int getDefaultScreen(Context context) {
		// jeffliu eton change
		// SharedPreferences sp = context.getSharedPreferences(WORKSPACE_PREFERENCES, Context.MODE_PRIVATE);
		SharedPreferences sp = getSharedPreferences(context);
		int def_screen = sp.getInt(DEFAULT_SCREEN, context.getResources().getInteger(R.integer.config_defaultScreen));
		return def_screen;
	}
	
	public static void setDesktopScreens(Context context,int screens) {
		// jeffliu eton change
		//SharedPreferences sp = context.getSharedPreferences(WORKSPACE_PREFERENCES, Context.MODE_PRIVATE);
	   	SharedPreferences sp = getSharedPreferences(context);
	    SharedPreferences.Editor editor = sp.edit();
		editor.putInt(DESKTOP_SCREENS, screens);
	    editor.commit();
	}
	
	public static void setDefaultScreen(Context context,int screens) {
		// jeffliu eton change
		// SharedPreferences sp = context.getSharedPreferences(WORKSPACE_PREFERENCES, Context.MODE_PRIVATE);
	    SharedPreferences sp = getSharedPreferences(context);
		SharedPreferences.Editor editor = sp.edit();
		editor.putInt(DEFAULT_SCREEN, screens);
	    editor.commit();
	    Workspace.updateDefaultScreenParam(screens);
	}
	
	public static void setIsAppsTypeGrid(Context context, boolean isTypeGrid) {
		// jeffliu eton change
		//SharedPreferences sp = context.getSharedPreferences(WORKSPACE_PREFERENCES, Context.MODE_PRIVATE);
	    SharedPreferences sp = getSharedPreferences(context);
		SharedPreferences.Editor editor = sp.edit();
		editor.putBoolean(IS_SHOW_APPS_TYPE_GRID, isTypeGrid);
	    editor.commit();
	}
	
	public static boolean getIsAppsTypeGrid (Context context) {
		// jeffliu eton change
		// SharedPreferences sp = context.getSharedPreferences(WORKSPACE_PREFERENCES, Context.MODE_PRIVATE);
		SharedPreferences sp = getSharedPreferences(context);
		boolean isAppsTypeGrid = sp.getBoolean(IS_SHOW_APPS_TYPE_GRID, context.getResources().getBoolean(R.bool.config_default_is_apps_grid));
		return isAppsTypeGrid;
	}
	
	public static int getWorkspaceEffect(Context context) {
		// jeffliu eton change
		// SharedPreferences sp = context.getSharedPreferences(WORKSPACE_PREFERENCES, Context.MODE_PRIVATE);
		SharedPreferences sp = getSharedPreferences(context);
//		return Integer.parseInt(sp.getString(WORKSACE_EFFECT, context.getResources().getString(R.string.pref_workspace_transition_effect_default)));
		return sp.getInt(WORKSACE_EFFECT, -2);
	}
	
	public static void setWorkspaceEffect(Context context, int effectIndex) {
		SharedPreferences sp = context.getSharedPreferences(WORKSPACE_PREFERENCES, Context.MODE_PRIVATE);
	    SharedPreferences.Editor editor = sp.edit();
		editor.putInt(WORKSACE_EFFECT, effectIndex);
	    editor.commit();
	}
	
	//added by chenxin
	public static void setEffectInScreenUIPosition(Context context, int whichScreen, int position){
		SharedPreferences sp = context.getSharedPreferences(WORKSPACE_PREFERENCES, Context.MODE_PRIVATE);
	    SharedPreferences.Editor editor = sp.edit();
	    editor.putInt(UI_WHICH_SCREEN_POSITION, whichScreen);
	    editor.putInt(UI_INSCREEN_POSITION, position);
	    editor.commit();
	}
	
	public static int getEffectWhichScreen(Context context){
		SharedPreferences sp = getSharedPreferences(context);
		return sp.getInt(UI_WHICH_SCREEN_POSITION, 0);
	}
	
	public static int getPositionInScreen(Context context){
		SharedPreferences sp = getSharedPreferences(context);
		return sp.getInt(UI_INSCREEN_POSITION, 0);
	}
	//added end
	
	public static int getAllAppEffect(Context context) {
		// jeffliu eton change
		// SharedPreferences sp = context.getSharedPreferences(WORKSPACE_PREFERENCES, Context.MODE_PRIVATE);
		SharedPreferences sp = getSharedPreferences(context);
		return Integer.parseInt(sp.getString(ALL_APPLICATIONS_EFFECT, context.getResources().getString(R.string.pref_allapp_transition_effect_default)));
	}
	
	/*public static void setAllAppEffect(Context context, int effectIndex) {
		SharedPreferences sp = context.getSharedPreferences(WORKSPACE_PREFERENCES, Context.MODE_PRIVATE);
	    SharedPreferences.Editor editor = sp.edit();
		editor.putInt(ALL_APPLICATIONS_EFFECT, effectIndex);
	    editor.commit();
	}*/
	
	public static SharedPreferences getSharedPreferences(Context context){
		//modified by ETON guolinan
		 return context.getSharedPreferences(WORKSPACE_PREFERENCES, Context.MODE_PRIVATE);
//		return context.getSharedPreferences(getPreferencesFileName(context), Context.MODE_PRIVATE);
	}
	/*// jeffliu eton add
	public static String getPreferencesFileName(Context context) {
		boolean  isNormal = ((LauncherApplication) context.getApplicationContext()).isCurrentModeNormal();
		return isNormal ? WORKSPACE_PREFERENCES : WROKSPACE_PREFERENCES_GUEST_MODE;
	}*/
	
	public static int getDisplayMode (Context c) {
		SharedPreferences sp = getSharedPreferences(c);
		return sp.getInt(DISPLAY_MODE, -1);
	}
	
	public static void setDisplayMode (Context c, int mode) {
		SharedPreferences sp = getSharedPreferences(c);
	    SharedPreferences.Editor editor = sp.edit();
		editor.putInt(DISPLAY_MODE, mode);
	    editor.commit();
	}
	
	public static boolean isModeChanged (Context c) {
		SharedPreferences sp = getSharedPreferences(c);
		return sp.getBoolean(MODE_CHANGED, false);
	}
	
	public static void setDisplayMode (Context c, boolean changed) {
		SharedPreferences sp = getSharedPreferences(c);
		SharedPreferences.Editor editor = sp.edit();
		editor.putBoolean(MODE_CHANGED, changed);
		editor.commit();
	}
}
