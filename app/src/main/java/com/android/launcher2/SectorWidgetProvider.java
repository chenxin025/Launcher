package com.android.launcher2;


import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

public class SectorWidgetProvider extends AppWidgetProvider {
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
	// TODO Auto-generated method stub
	super.onDeleted(context, appWidgetIds);
	}
	@Override
	public void onDisabled(Context context) {
	// TODO Auto-generated method stub
	super.onDisabled(context);
	}
	@Override
	public void onEnabled(Context context) {
	// TODO Auto-generated method stub
	super.onEnabled(context);
	}
	@Override
	public void onReceive(Context context, Intent intent) {
	// TODO Auto-generated method stub
	super.onReceive(context, intent);
	}
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
	int[] appWidgetIds) {
//		RemoteViews views = new RemoteViews(context.getPackageName(),R.layout.sectorwidgetlayout);
//		Intent intent = new Intent();
//		intent.setAction(SectorView.ACTION_SECTORVIEW_SHOW);
//		intent.putExtra(SectorWidgetConfig.MODE, SectorWidgetConfig.DIRECTION_LEFT_BOTTOM);
//		PendingIntent pending = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		
		/*
		Intent intent1 = new Intent();
		intent1.setClass(context, SectorView.class);
		intent1.setAction(String.valueOf(System.currentTimeMillis()));
        Bundle bundle1 = new Bundle();
        bundle1.putInt("direction", SectorWidgetConfig.DIRECTION_LEFT_BOTTOM);
        intent1.putExtras(bundle1);
//		intent.putExtra("direction", WidgetConfig.DIRECTION_LEFT_BOTTOM);
		PendingIntent pending1 = PendingIntent.getActivity(context, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
		
		Intent intent2 = new Intent();
		intent2.setClass(context, SectorView.class);
		intent2.setAction(String.valueOf(System.currentTimeMillis()));
        Bundle bundle2 = new Bundle();
        bundle2.putInt("direction", SectorWidgetConfig.DIRECTION_RIGHT_BOTTOM);
        intent2.putExtras(bundle2);
//		intent.putExtra("direction", WidgetConfig.DIRECTION_RIGHT_BOTTOM);
		PendingIntent pending2 = PendingIntent.getActivity(context, 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
*/

//		views.setOnClickPendingIntent(R.id.widget_rocketbutton, pending);
/*		
		views.setOnClickPendingIntent(R.id.widget_rocketbutton1, pending1);
		views.setOnClickPendingIntent(R.id.widget_rocketbutton2, pending2);
*/
//		appWidgetManager.updateAppWidget(appWidgetIds,views);
		
//		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}
}