package com.android.launcher2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.RemoteViews;

import com.android.launcher.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class UpdateManager
{

    private Context mContext;

    public static boolean isDownloading = false;
   

    private Dialog noticeDialog;

    private Dialog downloadDialog;
    /* 下载包安装路�?*/
    private static final String savePath = "/sdcard/updatedemo/";

    /* 进度条与通知ui刷新的handler和msg常量 */
//    private ProgressBar mProgress;

    private static final int DOWN_UPDATE = 1;

    private static final int DOWN_OVER = 2;

    private int progress;

    private Thread downLoadThread;

    public boolean interceptFlag = false;
    
    
    /**
     * 手机开机后 间隔多长时间  查询新版本 
     */
    public static  int alarmTime = 2*60*1000;


    public static String check_update = "check_update";

    public static String check_broadcast ="com.android.launcher2.check_broadcast";
    
    public static String cancle_Download ="com.android.launcher2.cancle_Download";
    
    public static String update_broadcast ="com.android.launcher2.update_broadcast";
    
    // 返回的安装包url
    private String apkUrl = "";
    
    public static String apkUpdateUrl ="apkUpdateUrl";
    
   
    private String content;
    public static String apkContentText ="apkcontentText";
    
    
    public static String apkSaveFileName ="apkSaveFileName";
    private   String saveFileName = "";
    
    
    public static String apkVersionName ="apkVersionName";
    private   String versionName = "";
    
    public static String apkNoticeTotalNmber ="apkNoticeTotalNmber";
    private   int noticeTotalNmber =0;
    
    
    public static String arealyNoticeNmber ="arealyNoticeNmber";
    
    
    SharedPreferences mSharedPrefs ;
    
    public UpdateManager(Context context)
    {
        this.mContext = context;
        showDoalog_flag = true;
         mSharedPrefs = mContext.getSharedPreferences(LauncherApplication.getSharedPreferencesKey(),
            Context.MODE_PRIVATE);
         in =new Info();
    }
    private Handler mHandler = new Handler()
    {

        public void handleMessage(Message msg)
        {
            
            switch (msg.what)
            {
                case DOWN_UPDATE:
                    
                    if(flag)
                    {
                        String title = "";
                        
                        if(TextUtils.isEmpty(saveFileName))
                        {
                            title = in.appName;
                        }
                        else
                        {
                            title = saveFileName; 
                        }
                        
                        PendingIntent pendingintent = PendingIntent.getActivity(mContext, 0, new Intent("ddadasd"), PendingIntent.FLAG_CANCEL_CURRENT);
                        
                        mNotification.contentView.setTextViewText(R.id.textView2, "  " +title);
                        mNotification.contentIntent=pendingintent;
                        
                        mNotification.contentView.setProgressBar(R.id.progress, 100, progress, false);
                        mNotification.contentView.setTextViewText(R.id.textView1, "  " + progress + "%");
                        manager.notify(NOTIFICATION_ID, mNotification);
                    }
                    break;
                case DOWN_OVER:

                    Log.v("check", "-------------DOWN_OVER---------");
                    if(downloadDialog != null)
                    {
                        downloadDialog.dismiss();
                    }
                    installApk();
                    if(flag)
                    cancelNotyFication(apkUrl,NOTIFICATION_ID);   
                    break;
                    // 正常下载
                 case 3:
                    
                     
                     String currentVersion =  mSharedPrefs.getString(apkVersionName, "null");
                     Log.v("check", "-------------currentVersion---------" + currentVersion  +  "---versionName---" + versionName);
                     if(currentVersion.equalsIgnoreCase(versionName))
                     {
                         int a = mSharedPrefs.getInt(arealyNoticeNmber, 0);
                         Log.v("check", "-------------arealyNoticeNmber---------" + a);
                         Log.v("check", "------------noticeTotalNmber---------" + noticeTotalNmber);
                         if(a > noticeTotalNmber)
                         {
                            return;
                         }
                     }
                        
                     showUpDateNOTY();
                     saveState();
                  
//                    showNoticeDialog(content,1);
                    break;
                    //当前版本�?��
//                case 4:
//                    showNoticeDialog(mContext.getResources().getString(R.string.version_is_new),0);
//                    break;
                    // 非正常下�?服务器提供相关提示的 
                case 5:
                  showNoticeDialog(content,0);
                  
                  break;
                case 6:
                    // 下载失败情况
                    cancelNotyFication(apkUrl,NOTIFICATION_ID);  
                    if(downloadDialog != null)
                    {
                        downloadDialog.dismiss();
                    }
                    break;
                default:
                    break;
            }
        };
    };


    private void showUpDateNOTY()
    {
        
        Notification.Builder builder = new Notification.Builder(mContext);
        builder.setContentTitle(mContext.getResources().getString(R.string.application_name)+" " + mContext.getResources().getString(R.string.apk_version_update));
        builder.setAutoCancel(true);
        builder.setSmallIcon(R.mipmap.ic_launcher_home);
        builder.setOngoing(true);
        Intent intent = new Intent();
        intent.setAction(UpdateManager.update_broadcast);
        builder.setContentIntent(PendingIntent.getBroadcast(mContext, 
            0, intent , PendingIntent.FLAG_UPDATE_CURRENT));
        Notification notification = builder.getNotification();
        NotificationManager manager=(NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify("12", 1, notification);
        
        
    }
    
    public void getState()
    {
         apkUrl = mSharedPrefs.getString(apkUpdateUrl, apkUrl);
         content = mSharedPrefs.getString(apkContentText, content);
         saveFileName = mSharedPrefs.getString(apkSaveFileName, saveFileName);
         versionName = mSharedPrefs.getString(apkVersionName, versionName);
         noticeTotalNmber =  mSharedPrefs.getInt(apkNoticeTotalNmber, noticeTotalNmber);
         
           
    }
    private void saveState()
    {
           
            int a = mSharedPrefs.getInt(arealyNoticeNmber, 0);
            mSharedPrefs.edit()
           .putInt(arealyNoticeNmber, a+1)
           .commit();
           
        
           mSharedPrefs.edit()
          .putString(apkUpdateUrl, apkUrl)
          .commit();
        
           mSharedPrefs.edit()
          .putString(apkContentText, content)
          .commit();
           
           
           mSharedPrefs.edit()
           .putString(apkSaveFileName, saveFileName)
           .commit();
           
           
            mSharedPrefs.edit()
           .putInt(apkNoticeTotalNmber, noticeTotalNmber)
           .commit();
            
            
            mSharedPrefs.edit()
            .putString(apkVersionName, versionName)
            .commit();
    }
    
    
    
    private Info in;
    private String convertStreamToString(InputStream in) throws IOException
    {
        int rCount = 0;
        byte[] ret = new byte[0];
        byte[] buff = new byte[1024];
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        try
        {
            while ((rCount = in.read(buff, 0, 1024)) > 0)
            {
                swapStream.write(buff, 0, rCount);
            }
            ret = swapStream.toByteArray();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return new String(ret);
    }
    

    // 外部接口让主Activity调用
    public void checkUpdateInfo()
    {
        
        if(isDownloading)
        {
            
            return;
        }
        
        if(isNetWorkAvailable(mContext))
        {
//            showProgressDialog(this.mContext.getResources().getString(R.string.check_update_ing));
            new CheckVersionThead().start(); 
        }
        
    }

    private class CheckVersionThead extends Thread
    {
        @Override
        public void run()
        {
            HttpResponse httpResponse  = checkUpdateVersion();
            
         
            dismissPrgsDialog();
            if(httpResponse != null)
            {
                   try
                {
                  
                    String date = convertStreamToString(httpResponse.getEntity().getContent());
                    Log.v("tianlei", "---httpResponse.getEntity().getContent()----" + date);
                    Log.v("check", date);
                    JSONObject jo = new JSONObject(date);
                  
                    if(jo.getInt("status") == 1000)
                    {
                         content =jo.getString("content");
                         apkUrl = (jo.getString("url"));
                     
                         saveFileName =jo.getString("appname");
                         
                         versionName = jo.getString("appver");
                         
                         saveFileName =saveFileName+"_"+versionName;
                         
                         noticeTotalNmber =jo.getInt("noticenum");
              
                         
                         mHandler.sendEmptyMessage(3);
                    }
                    else
                    {
                        saveFileName =jo.getString("appname");
                        content =jo.getString("content");
//                        mHandler.sendEmptyMessage(5);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
//                    mHandler.sendEmptyMessage(4);
                }
            
            }
            else
            {
//                mHandler.sendEmptyMessage(4);
            }
           
        }
    }
    
    private class Info
    {
        
        /**
         * 手机分辨�?
         */
        private String translation ;
        /**
         * Android4.0.0_SDK-15
         */
        private String androidSdk;
        
        /**
         * 机型版本�?
         */
        private String phoneMode;
        private String phoneVersion;
        
        private String appName;
        
        private String appVersion;
        
        private String imei;
        
        
        private String platform ;
        
        
        
        /**
         * 版本代码code
         */
        private String appVersionCode;
        
        /**
         * 运用的启动类名称
         */
        private String appMainClassName;
        
        /**
         * 运用的包�?
         */
        private String appPackageName;
        
        
        public Info(){}
        /*{
            TelephonyManager teleMgr = (TelephonyManager) mContext
                .getSystemService(Context.TELEPHONY_SERVICE);
            imei = teleMgr.getDeviceId();
            phoneMode = android.os.Build.MODEL;
            phoneVersion = SystemProperties.get("ro.build.display.id");
            
            platform = SystemProperties.get("ro.board.platform");
            Log.v("check", "-------------platform---------" + platform);
            androidSdk = 
                  "Android-"+
                  (android.os.Build.VERSION.RELEASE)
                  + "SDK-"
                  +
                  (android.os.Build.VERSION.SDK);
            DisplayMetrics dm = new DisplayMetrics();   
            ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);   
            translation=dm.widthPixels+"*"+dm.heightPixels;   
            
         // 获取packagemanager的实�?
            PackageManager packageManager = (mContext).getPackageManager();
            // getPackageName()是你当前类的包名�?代表是获取版本信�?
            
            appPackageName = mContext.getPackageName();
            
            PackageInfo packInfo;
            try
            {
                packInfo = packageManager.getPackageInfo(mContext.getPackageName(),0);
                appVersion = packInfo.versionName;
                appVersionCode = packInfo.versionCode+"";
                
            }
            catch (NameNotFoundException e)
            {
                e.printStackTrace();
            }
          
            appName = mContext.getResources().getString(R.string.application_name);
            
            
            appMainClassName = findActivitiesForPackage(mContext,appPackageName);
            
            
        }*/
        
    }
    private String  findActivitiesForPackage(Context context, String packageName) {
         PackageManager packageManager = context.getPackageManager();

        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mainIntent.setPackage(packageName);

         List<ResolveInfo> apps = packageManager.queryIntentActivities(mainIntent, 0);
        
        if(apps!= null && apps.size() >0)
        {
            
            return  apps.get(0).activityInfo.name;
        }
        
        return "";
    }

    private HttpResponse checkUpdateVersion()
    {
        
        String uri = this.mContext.getResources().getString(R.string.check_version);
        if(!TextUtils.isEmpty(uri))
        {
            HttpResponse response = null;
             try
            {
           
                 HttpClient httpClient = new DefaultHttpClient();
                 HttpParams params = new BasicHttpParams();
                 HttpConnectionParams.setConnectionTimeout(params, 5 * 1000);
                 HttpConnectionParams.setSoTimeout(params, 20 * 1000);
                 HttpPost httpPost = new HttpPost(uri);
                 httpPost.setParams(params);
                 
               
                 List<BasicNameValuePair> lsitBasicNameValuePairs = new ArrayList<BasicNameValuePair>();
                 BasicNameValuePair pav = new  BasicNameValuePair("translation", in.translation);
                 lsitBasicNameValuePairs.add(pav);
                 
                 BasicNameValuePair pav2 = new  BasicNameValuePair("androidSdk", in.androidSdk);
                 lsitBasicNameValuePairs.add(pav2);
                 
                 BasicNameValuePair pav3 = new  BasicNameValuePair("phoneMode", in.phoneMode);
                 lsitBasicNameValuePairs.add(pav3);
                 
                 BasicNameValuePair pav4 = new  BasicNameValuePair("phoneVersion", in.phoneVersion);
                 lsitBasicNameValuePairs.add(pav4);
                 
                 BasicNameValuePair pav5 = new  BasicNameValuePair("appName", in.appName);
                 lsitBasicNameValuePairs.add(pav5);
                 
                 BasicNameValuePair pav6 = new  BasicNameValuePair("appVersion", in.appVersion);
                 lsitBasicNameValuePairs.add(pav6);
                 
                 BasicNameValuePair pav7 = new  BasicNameValuePair("imei", in.imei);
                 lsitBasicNameValuePairs.add(pav7);
                 
                 
                 BasicNameValuePair platform = new  BasicNameValuePair("platform", in.platform);
                 lsitBasicNameValuePairs.add(platform);
                 
                 
                 
                 //----------
                 BasicNameValuePair appVersionCode = new  BasicNameValuePair("appVersionCode", in.appVersionCode);
                 lsitBasicNameValuePairs.add(appVersionCode);
                 
                 
                 
                 BasicNameValuePair appMainClassName = new  BasicNameValuePair("appMainClassName", in.appMainClassName);
                 lsitBasicNameValuePairs.add(appMainClassName);

                 
                 
                 BasicNameValuePair appPackageName = new  BasicNameValuePair("appPackageName", in.appPackageName);
                 lsitBasicNameValuePairs.add(appPackageName);

                 
                 
                 //--------
                 
                
                 UrlEncodedFormEntity enty = new  UrlEncodedFormEntity(lsitBasicNameValuePairs,"utf-8");
                 
                 Log.v("tianlei", "uri)----" + uri);
                 httpPost.setEntity(enty);
                 response = httpClient.execute(httpPost);
                 
                 return response;
                 
            }
            catch (Exception e)
            {
                Log.v("tianlei", "Exception------------" + e.toString());
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }
             
        }
        return null;
    }
    private static ProgressDialog mPrgsDialog = null;
    private void showProgressDialog(String meg)
    {
        mPrgsDialog = new ProgressDialog(mContext);// 必须获得父类的对话框
        mPrgsDialog.setMessage(meg);
        mPrgsDialog.setIndeterminate(false);
        mPrgsDialog.setCancelable(true);
        mPrgsDialog.setCanceledOnTouchOutside(false); 
        mPrgsDialog.show(); 
    }
    public static void dismissPrgsDialog()
    {
        if (mPrgsDialog != null && mPrgsDialog.isShowing()
                && mPrgsDialog.getContext() != null)
        {
            try
            {
                mPrgsDialog.dismiss();
            } catch (IllegalArgumentException e)
            {
                e.printStackTrace();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    
    public static boolean showDoalog_flag = true;
    /**
     * 版本提示 信息 
     * @param updateMsg
     * @param type 0 标示无新版本�?有最新版�?
     */
    public void showNoticeDialog(String updateMsg,int type)
    {
        
        if(showDoalog_flag)
        {
            AlertDialog.Builder builder = new Builder(mContext);
            builder.setTitle(mContext.getResources().getString(R.string.apk_version_update_title));
            builder.setMessage(updateMsg);
            if(type ==1)
            {
                builder.setPositiveButton(mContext.getResources().getString(R.string.apk_version_update_now),
                    new OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
//                            showDownloadDialog();
                            Log.v("tianlei", "--saveFileName-----------------" + saveFileName);
                            if(TextUtils.isEmpty(saveFileName))
                            {
                                addNotyFication(1,NOTIFICATION_ID,apkUrl,in.appName);
                            }
                            else
                            {
                                addNotyFication(1,NOTIFICATION_ID,apkUrl,saveFileName);
                            }
                            flag =true;
                            downloadApk();
                           isDownloading = true;
                        }
                    });
                builder.setNegativeButton(mContext.getResources().getString(R.string.apk_version_update_later),
                    new OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                            isDownloading = false;
                        }
                    });
            }
            else
            {
                builder.setNegativeButton(mContext.getResources().getString(R.string.str_ok),
                    new OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                        }
                    });
            }
            noticeDialog = builder.create();
            noticeDialog.show();
        }
       
    }

    private boolean flag =false;
    private void showDownloadDialog()
    {
//        flag = false;
//        AlertDialog.Builder builder = new Builder(mContext);
//        builder.setTitle(mContext.getResources().getString(R.string.apk_version_update_title));
//        builder.setCancelable(false);
//        final LayoutInflater inflater = LayoutInflater.from(mContext);
//        View v = inflater.inflate(R.layout.progress, null);
//        mProgress = (ProgressBar) v.findViewById(R.id.progress);
//
//        builder.setView(v);
//        builder.setNegativeButton(mContext.getResources().getString(R.string.str_cancel), new OnClickListener()
//        {
//            @Override
//            public void onClick(DialogInterface dialog, int which)
//            {
//                dialog.dismiss();
//                interceptFlag = true;
//                flag = false;
//            }
//        });
//        builder.setPositiveButton(mContext.getResources().getString(R.string.apk_version_update_bk),
//            new OnClickListener()
//            {
//                @Override
//                public void onClick(DialogInterface dialog, int which)
//                {
//                    dialog.dismiss();
//                    if(TextUtils.isEmpty(saveFileName))
//                    {
//                        addNotyFication(0,0,apkUrl,in.appName);
//                    }
//                    else
//                    {
//                        addNotyFication(0,0,apkUrl,saveFileName);
//                    }
//                    flag =true;
////                    downloadApk();
//                }
//            });
//        downloadDialog = builder.create();
//        downloadDialog.show();
//
//        downloadApk();
    }

    private void cancelNotyFication(String tag,int type)
    {
        NotificationManager manager=(NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        
        manager.cancel(type);
    }
    
    private Notification  mNotification;
    private NotificationManager manager;
    private int NOTIFICATION_ID =1111;
    private void addNotyFication(int percent,int type,String tag,String title)
    {
        
          mNotification = new Notification(R.drawable.download_icon, title,System.currentTimeMillis());
        
          mNotification.icon = R.drawable.download_icon;
          PendingIntent pendingintent = PendingIntent.getActivity(mContext, 0, new Intent("ddadasd"), PendingIntent.FLAG_CANCEL_CURRENT);
          
        // 自定义布局的 通知
          mNotification.contentView =  new RemoteViews(mContext.getPackageName(), R.layout.progress);;
//          
          mNotification.contentView.setTextViewText(R.id.textView1, "  " + percent + "%");
          mNotification.contentView.setProgressBar(R.id.progress, 100, percent, false);
          mNotification.contentView.setOnClickPendingIntent(R.id.button1, PendingIntent.getBroadcast(mContext, 0, new Intent(UpdateManager.cancle_Download), PendingIntent.FLAG_CANCEL_CURRENT));
          
          mNotification.flags = Notification.FLAG_AUTO_CANCEL;
       
          mNotification.contentView.setTextViewText(R.id.textView2, "  " +title);
        
          mNotification.contentIntent=pendingintent;

          
          
          manager=(NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
          manager.notify(type, mNotification);
    }
    private Runnable mdownApkRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            try
            {
                File file = new File(savePath);
                if (!file.exists())
                {
                    file.mkdir();
                }
                String apkFiletm = savePath+saveFileName+".apk";
                File apkFiletmf = new File(apkFiletm);
                if(apkFiletmf.exists())
                {
                    mHandler.sendEmptyMessage(DOWN_OVER);
                    isDownloading = false;
                    return;
                }
                
                String apkFile = savePath+saveFileName+".apk.tmep";
                File ApkFile = new File(apkFile);
                
                long currentSize = getFileSize(apkFile);
                if (currentSize < 0)
                {
                    currentSize = 0;
                }
                if(TextUtils.isEmpty(apkUrl)) return;
                URL url = new URL(apkUrl);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                if(currentSize > 0)
                {
                    conn.setRequestProperty("range", "bytes="+currentSize+"-");
                }
                conn.connect();
                int length = (int) (conn.getContentLength() + currentSize);
                InputStream is = conn.getInputStream();

               FileOutputStream fos = new FileOutputStream(ApkFile,true);

                int count = (int) currentSize;
                byte buf[] = new byte[1024];

                int prePrpgress =  1;
                int numread = 0;
                while((numread =is.read(buf))>0)
                {
                    count+=numread;
                    
                    progress = (int) (((float) count / length) * 100);
                    
                    
                    Log.v("check", "-------------progress---------" + progress);
                    
                    fos.write(buf, 0, numread);
                    
                    if(progress >= 100)
                    { 
                        // 更新进度
                        mHandler.sendEmptyMessage(DOWN_UPDATE);
                    }
                    else if(progress > prePrpgress)
                    {
                        mHandler.sendEmptyMessage(DOWN_UPDATE);
                    }
                    if(progress > 1)
                    {
                        prePrpgress = progress;  
                    }
                    if(interceptFlag)
                    {
                        isDownloading = false;
                        cancelNotyFication(apkUrl,NOTIFICATION_ID);   
                        break;
                    }
                }
                if(!interceptFlag)
                {
                    reName(apkFiletm,ApkFile);
                    // 下载完成通知安装
                    mHandler.sendEmptyMessage(DOWN_OVER);
                }
                else
                {
                    interceptFlag = false;
                }
//             
                Log.v("check", "-------------end---------");
                fos.close();
                is.close();
                isDownloading = false;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                isDownloading = false;
                mHandler.sendEmptyMessage(6);
                
            }

        }
    };
    /**
     * 下载完成后重命名
     * 
     * @param newName 名称
     * @param file 正式名称
     */
    private void reName(String newName, File file)
    {
        File reNameFile;
        reNameFile = new File(newName);
        file.renameTo(reNameFile);
    }
    /**
     * 下载apk
     * 
     * @param url
     */

    private void downloadApk()
    {
        downLoadThread = new Thread(mdownApkRunnable);
        downLoadThread.start();
    }

    /**
     * 安装apk
     * 
     * @param url
     */
    private void installApk()
    {
        File apkfile = new File(savePath+saveFileName+".apk");
        if (!apkfile.exists())
        {
            return;
        }
        Uri uri = Uri.fromFile(apkfile);
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(uri, "application/vnd.android.package-archive");
        mContext.startActivity(i);

    }
    public static boolean isNetWorkAvailable(Context context)
    {
        boolean ret = false;
        if (context == null)
        {
            return ret;
        }
        try
        {
            ConnectivityManager connetManager =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connetManager == null)
            {
                return ret;
            }
            NetworkInfo[] infos = connetManager.getAllNetworkInfo();
            if (infos == null)
            {
                return ret;
            }
            for (int i = 0; i < infos.length && infos[i] != null; i++)
            {
                if (infos[i].isConnected() && infos[i].isAvailable())
                {
                    ret = true;
                    break;
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return ret;
    }
    public static long getFileSize(String path)
    {
        File file = new File(path);
        if (!file.isFile())
        {
            return -1;
        }
        if (!file.exists())
        {
            return -1;
        }
        return file.length();
    }
}
