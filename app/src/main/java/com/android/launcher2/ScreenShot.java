package com.android.launcher2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ScreenShot
{ 
    // 获取指定Activity的截屏，保存到png文件
    public static Bitmap getGaussBitmap(Context context,  Bitmap copyBitmap, Bitmap screenBitmap) {
        long beginTime = System.currentTimeMillis();

        if(screenBitmap == null){
            Log.e("Launcher_test", "takeScreenShot: b is null");
            return null;
        }

        
        long beginBlurTime = System.currentTimeMillis();
        Bitmap blurBitmap = SectorUtils.BoxBlurFilter2(screenBitmap);
        long endBlurTime = System.currentTimeMillis();
        
        Log.v("Launcher_test", "getGaussBitmap: blur gaosi spaceTime = "+(endBlurTime-beginBlurTime));
       // savePic(blurBitmap, "/sdcard/XX2.png");
        
      //  int bpH = height- statusBarHeight;
        
        Launcher launcher = (Launcher) context;
        long beginbigBlurBitmap = System.currentTimeMillis();
        float scaleW =  (float)(launcher.mWidth*0.8f)/(float)blurBitmap.getWidth();
        float scaleH = (float)(launcher.mHeight*0.8f)/(float)blurBitmap.getHeight();

        Matrix matrix2 = new Matrix();
        matrix2.postScale(scaleW, scaleH);
        
        Bitmap bigBlurBitmap = Bitmap.createBitmap(blurBitmap, 0, 0, blurBitmap.getWidth(), blurBitmap.getHeight(), matrix2, true);
        
        long endBbigBlurBitmap = System.currentTimeMillis();
        Log.v("Launcher_test", "getGaussBitmap: bigBlurBitmap spaceTime = "+(endBbigBlurBitmap-beginbigBlurBitmap));
        //savePic(bigBlurBitmap, "/sdcard/XX3.png");
        
        long drawCnavasBitmap = System.currentTimeMillis();
        Canvas cv = new Canvas(copyBitmap);
        cv.drawBitmap(bigBlurBitmap, (launcher.mWidth-bigBlurBitmap.getWidth())/2, launcher.mStatusBarHeight+(launcher.mHeight-bigBlurBitmap.getHeight())/2, null );
        //cv.drawBitmap(bigBlurBitmap, (launcher.mWidth-bigBlurBitmap.getWidth())/2, launcher.mStatusBarHeight, null );
        cv.save(Canvas.ALL_SAVE_FLAG );
        cv.restore();
      
        long endTime = System.currentTimeMillis();
        Log.w("Launcher_test", "getGaussBitmap: total spaceTime = "+(endTime-beginTime)+"  drawCnavasBitmap time= "+(endTime-drawCnavasBitmap));
        
        //savePic(copyBitmap, "/sdcard/XXXXXX.png");
        //view.destroyDrawingCache();
        
       return copyBitmap;
        
    }
    
    // 保存到sdcard
    public static boolean savePic(Bitmap b, String strFileName) {
        boolean res = false;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(strFileName);
            if (null != fos) {
                res = b.compress(Bitmap.CompressFormat.PNG, 90, fos);
                fos.flush();
                fos.close();
            }
        } catch (FileNotFoundException e){
            res = false;
            e.printStackTrace();
        } catch (IOException e) {
            res = false;
            e.printStackTrace();
        }
        
        return res;
        
    }
}
