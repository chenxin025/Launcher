package com.android.launcher2;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.widget.Toast;

import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver{
private static long CLOCKTIME = 3 * 24 * 60 * 60 * 1000L;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
    	SharedPreferences sp = SharedPreferencesUtils.getSharedPreferences(context); 
    	SharedPreferences.Editor editor = sp.edit();
    	boolean isTimeOver = sp.getBoolean("istimeover", true);
    	//褰撹缃椂闂磋秴杩囧畾鏃剁殑鏃堕棿锛屽畾鏃跺櫒浼氬搷闂广�
        if(!isTimeOver && Intent.ACTION_TIME_CHANGED.equals(intent.getAction()) ||Intent.ACTION_TIMEZONE_CHANGED.equals(intent.getAction())){
        	 long currentTimeMillis = System.currentTimeMillis();
        	 long elapsedTime = sp.getLong("elapsedtime", 0);
        	 long cumulativeTime =  sp.getLong("cumulativetime", 0);
        	 cumulativeTime += SystemClock.elapsedRealtime() - elapsedTime;
        	 elapsedTime = SystemClock.elapsedRealtime();
        	 long leftTime = CLOCKTIME - cumulativeTime;        	
        	 
        	 if(leftTime > 0){
	        	 Intent intent1 =new Intent("com.android.launcher2.ALARM");
	     	     PendingIntent sender=
	     	        PendingIntent.getBroadcast(context, 0, intent1, 0);
	     	     AlarmManager alarm =(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
	     	     alarm.cancel(sender);
	     	     alarm.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + leftTime, sender);
	     		 editor.putBoolean("istimeover",false);
        	 }else {
	    		Toast.makeText(context, "Time exceeds!", 
	                     Toast.LENGTH_LONG).show();
	    		currentTimeMillis = 0;
	    		elapsedTime = 0;
	    		cumulativeTime = 0;
	    		editor.putBoolean("istimeover",true);
        	 }
    		 editor.putLong("systemrealtime",currentTimeMillis);
    		 editor.putLong("elapsedtime",elapsedTime);
    		 editor.putLong("cumulativetime", cumulativeTime);
    	     editor.commit(); 
        	 
        }
        else if(!isTimeOver && "android.intent.action.BOOT_COMPLETED".equals(intent.getAction())){
           /* Toast.makeText(context, "BOOT_COMPLETED", 
                  Toast.LENGTH_LONG).show();*/
            
         long currentTimeMillis = System.currentTimeMillis();
       	 long lastRealTime = sp.getLong("systemrealtime", currentTimeMillis);
       	 long newElalsedTime = currentTimeMillis - lastRealTime;
       	 long cumulativeTime = sp.getLong("cumulativetime", 0) + newElalsedTime;
       	 long leftTime = 0;
       	 long elapsedTime = 0;
       	 //鏈埌瀹氭椂鏃堕棿
       	 if( cumulativeTime < CLOCKTIME){
       		 leftTime = CLOCKTIME - cumulativeTime;
   	    
            Intent intent1 =new Intent("com.android.launcher2.ALARM");
                	    PendingIntent sender=
    	        PendingIntent.getBroadcast(context, 0, intent1, 0);
    	    AlarmManager alarm = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    	    alarm.cancel(sender);
    	    alarm.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis() + leftTime, sender);
    		editor.putBoolean("istimeover",false);
    	    
    	 //瀹氭椂鏃堕棿鍒颁簡锛屾竻闆�  
       	 }else{
       		currentTimeMillis = 0;
       		cumulativeTime = 0;
       		editor.putBoolean("istimeover",true);
       	 }
       	 
		 editor.putLong("systemrealtime",currentTimeMillis);
		 editor.putLong("elapsedtime",elapsedTime);
		 editor.putLong("cumulativetime", cumulativeTime);
	     editor.commit();  
        }else if("com.android.launcher2.ALARM".equals(intent.getAction())){
       		editor.putBoolean("istimeover",true);
       	    editor.commit();
        	/*Toast.makeText(context, intent.getAction(), 
                      Toast.LENGTH_LONG).show();*/
        
        }
        
        else if("android.intent.action.BOOT_COMPLETED".equals(intent.getAction()))
        {
//            UpdateManager updateManager = new  UpdateManager(context);
            
//            updateManager.checkUpdateInfo();
             SharedPreferences mSharedPrefs = context.getSharedPreferences(LauncherApplication.getSharedPreferencesKey(),
                Context.MODE_PRIVATE);
             mSharedPrefs.edit()
            .putBoolean(UpdateManager.check_update, true)
            .commit();
             
             Intent it = new Intent();
             it.setAction(UpdateManager.check_broadcast);
             AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
             PendingIntent operation = PendingIntent.getBroadcast(context, 0, it, PendingIntent.FLAG_CANCEL_CURRENT);
             alarmMgr.cancel(operation);
             alarmMgr.set(AlarmManager.RTC, Calendar.getInstance().getTimeInMillis()+UpdateManager.alarmTime, operation);
            
            
        }
        
        
        
        
        
        
    }
}