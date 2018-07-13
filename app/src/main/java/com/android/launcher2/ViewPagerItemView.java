package com.android.launcher2;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.launcher.R;

/**
 * added by chenxin Launcher С����ģʽ�£��Զ������ѡ��view
 */
public class ViewPagerItemView extends FrameLayout {

	public static AppsCustomizePagedView mEtonWidgetView;
	public static Workspace mEtonWorkSpace;
	private PackageManager mPackageManager;
	public static ViewPager mViewPager;
	public static int mPositionEffectOfPage = -1;
	public static int mPositionInPage = -1;
	// �����ѡ�������
	private int mType;

	// ÿҳ��view ��4��ImageView
	private ImageView mAlbumImageView1;
	private ImageView mAlbumImageView2;
	private ImageView mAlbumImageView3;
	private ImageView mAlbumImageView4;

	private ImageView mCheck1;
	private ImageView mCheck2;
	private ImageView mCheck3;
	private ImageView mCheck4;
	
	// ÿҳ��view ImageView �·���Ӧ��TextView
	private TextView mALbumNameTextView1;
	private TextView mALbumNameTextView2;
	private TextView mALbumNameTextView3;
	private TextView mALbumNameTextView4;

	// ͼƬBitmap
	private Bitmap mBitmap1 = null;
	private Bitmap mBitmap2 = null;
	private Bitmap mBitmap3 = null;
	private Bitmap mBitmap4 = null;

	// TextView����
	private String mTitle1 = null;
	private String mTitle2 = null;
	private String mTitle3 = null;
	private String mTitle4 = null;

	// widget dims
	private TextView mWidgetDimText1;
	private String mDim1 = null;

	// Ҫ��ʾͼƬ��JSONOBject��.
	private JSONObject mObject;
	private ArrayList<AppWidgetProviderInfo> mList;
	private JSONArray mJSONArray;

	private Context mContext;

	private int mTotalSize;
	private static int mResType;

	// ����ÿҳImageView����
	private final static int ITEMNUMS_PER_PAGE = 4;
	private int mPosition;

	private DisplayMetrics mDm;

	private boolean mIsLocalWallpaper = false;
	public static boolean mIsAddCompleted = true;
	
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			// TODO ����ͼƬUI��ʾ
			if (mBitmap1 != null) {
				// updateUI(mAlbumImageView1,mALbumNameTextView1,mBitmap1,mTitle1);
				if (mPosition == 0 && (mResType == 0 || mResType == 3)) {
					if (mResType == 0) {
						// para = new
						// ViewGroup.LayoutParams(mAlbumImageView1.getWidth(),
						// 130);
						// mAlbumImageView1.setLayoutParams(para);
						// mAlbumImageView1.setLayoutParams(new
						// Gallery.LayoutParams(mAlbumImageView1.getWidth(),
						// 130));
						// mAlbumImageView1.setLayoutParams(new
						// ImageSwitcher.LayoutParams(mAlbumImageView1.getWidth(),
						// 130));
					}
					updateUI(mAlbumImageView1, mALbumNameTextView1, mBitmap1,
							mTitle1,mCheck1,mPosition,0,mResType);
					/*if (mResType == 0) {
						para = mAlbumImageView1.getLayoutParams();
						para.height = 48;
						para.width = mAlbumImageView1.getWidth();
						mAlbumImageView1.setLayoutParams(para);
						mAlbumImageView1.setScaleType(ImageView.ScaleType.FIT_XY );

					}*/
				} else {
					updateUI(mAlbumImageView1, mALbumNameTextView1, mBitmap1,
							mTitle1,mCheck1,mPosition,0,mResType);

				}
			}

			if (mBitmap2 != null) {
				updateUI(mAlbumImageView2, mALbumNameTextView2, mBitmap2,
						mTitle2,mCheck2,mPosition,1,mResType);
			}

			if (mBitmap3 != null) {
				updateUI(mAlbumImageView3, mALbumNameTextView3, mBitmap3,
						mTitle3,mCheck3,mPosition,2,mResType);
			}

			if (mBitmap4 != null) {
				updateUI(mAlbumImageView4, mALbumNameTextView4, mBitmap4,
						mTitle4,mCheck4,mPosition,3,mResType);
			}

		}
	};

	private void updateUI(ImageView imageview, TextView tv, Bitmap bitmap,
			String str,ImageView checkImage,int which, int postion,int type) {
		
		if (bitmap != null) {
			if (!bitmap.isRecycled()){
				imageview.setImageBitmap(bitmap);
				imageview.setVisibility(VISIBLE);
				//checkImage.setVisibility(VISIBLE);
			}
		}

		if (str != null) {
			tv.setText(str);
		}
		
		if (type == 2 ){
			if (mPositionEffectOfPage == which && mPositionInPage == postion){
				if (null != checkImage){
					checkImage.setVisibility(VISIBLE);
				}
			}
		}
	}

	public ViewPagerItemView(Context context) {
		super(context);
		setupViews(context);
		setPageChangeListen();
	}

	public ViewPagerItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setupViews(context);
		setPageChangeListen();
	}

	// ��ʼ��View
	private void setupViews(Context context) {
		mContext = context;
		mPackageManager = mContext.getPackageManager();
		LayoutInflater inflater = LayoutInflater.from(getContext());
		View view = inflater.inflate(R.layout.viewpager_itemview, null);

		mAlbumImageView1 = (ImageView) view.findViewById(R.id.album_imgview1);
		mAlbumImageView2 = (ImageView) view.findViewById(R.id.album_imgview2);
		mAlbumImageView3 = (ImageView) view.findViewById(R.id.album_imgview3);
		mAlbumImageView4 = (ImageView) view.findViewById(R.id.album_imgview4);

		mCheck1 = (ImageView) view.findViewById(R.id.check1);
		mCheck2 = (ImageView) view.findViewById(R.id.check2);
		mCheck3 = (ImageView) view.findViewById(R.id.check3);
		mCheck4 = (ImageView) view.findViewById(R.id.check4);
		
		mALbumNameTextView1 = (TextView) view.findViewById(R.id.album_name1);
		mALbumNameTextView2 = (TextView) view.findViewById(R.id.album_name2);
		mALbumNameTextView3 = (TextView) view.findViewById(R.id.album_name3);
		mALbumNameTextView4 = (TextView) view.findViewById(R.id.album_name4);

		addView(view);
		mDm = mContext.getResources().getDisplayMetrics();
	}

	android.view.ViewGroup.LayoutParams para;

	private void getBitmapByPath(JSONArray object, int position, int totalSize) {
		// TODO
		JSONObject data1 = null;
		JSONObject data2 = null;
		JSONObject data3 = null;
		JSONObject data4 = null;

		if (position * 4 < totalSize) {
			try {
				data1 = (JSONObject) object.get(position * 4);
				mBitmap1 = makeBitmapFromPath(data1.getString("path"), false);
				if (data1.getString("path").equals("local_wallpaper")) {
					// para = mAlbumImageView1.getLayoutParams();
					// para.height = 130;
					// para.width = mAlbumImageView1.getWidth();
					mTitle1 = mContext.getString(R.string.local_wallpaper);
				}
				// mTitle1 = data1.getString("name");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if ((position * 4 + 1) < totalSize) {
			try {
				data2 = (JSONObject) object.get(position * 4 + 1);
				mBitmap2 = makeBitmapFromPath(data2.getString("path"), false);
				// mTitle2 = data2.getString("name");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if ((position * 4 + 2) < totalSize) {
			try {
				data3 = (JSONObject) object.get(position * 4 + 2);
				mBitmap3 = makeBitmapFromPath(data3.getString("path"), false);
				// mTitle3 = data3.getString("name");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if ((position * 4 + 3) < totalSize) {
			try {
				data4 = (JSONObject) object.get(position * 4 + 3);
				// mTitle4 = data4.getString("name");
				mBitmap4 = makeBitmapFromPath(data4.getString("path"), false);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public Bitmap makeBitmapFromPath(String filename, boolean isFull) {
		if (filename.equals("local_wallpaper")) {
			Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(),
					R.drawable.local_wallpaper);// local_wallpaper);
			return bmp;
		}
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filename, options);

		int reqWidth, reqHeight;

		if (!isFull) {
			reqWidth = 50;
			reqHeight = 50;
		} else {
			reqWidth = mDm.widthPixels;
			reqHeight = mDm.heightPixels;
		}
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(filename, options);

		/*
		 * FileInputStream stream = null; try { stream = new FileInputStream(new
		 * File(path)); } catch (FileNotFoundException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); }
		 * BitmapFactory.Options opts = new BitmapFactory.Options();
		 * if(!isFull){ opts.inJustDecodeBounds = false; opts.inSampleSize = 10;
		 * } Bitmap bitmap = BitmapFactory.decodeStream(stream , null, opts);
		 * return bitmap;
		 */
	}

	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			if (width > height) {
				inSampleSize = Math.round((float) height / (float) reqHeight);
			} else {
				inSampleSize = Math.round((float) width / (float) reqWidth);
			}

			// This offers some additional logic in case the image has a strange
			// aspect ratio. For example, a panorama may have a much larger
			// width than height. In these cases the total pixels might still
			// end up being too large to fit comfortably in memory, so we should
			// be more aggressive with sample down the image (=larger
			// inSampleSize).

			final float totalPixels = width * height;

			// Anything more than 2x the requested pixels we'll sample down
			// further.
			final float totalReqPixelsCap = reqWidth * reqHeight * 2;

			while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
				inSampleSize++;
			}
		}
		return inSampleSize;
	}

	private Bitmap makeResIdToBitmap(int id) {
		InputStream is = mContext.getResources().openRawResource(id);
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeStream(is, null, options);
	}

	private String getWidgetDims(Context context, AppWidgetProviderInfo info) {
		int[] spanXY = Launcher.getSpanForWidget(context, info);
		int hSpan = Math.min(spanXY[0], LauncherModel.getCellCountX());
		int vSpan = Math.min(spanXY[1], LauncherModel.getCellCountY());
		final Resources r = mContext.getResources();
		mDim1 = r.getString(R.string.widget_dims_format);
		mDim1 = String.format(mDim1, hSpan, vSpan);
		return mDim1;

	}

	private void getWidgetBitmap(ArrayList<AppWidgetProviderInfo> list,
			int position, int totalSize) {
		AppWidgetProviderInfo info1 = null;
		if (position * 4 < totalSize) {
			if (list.get(position * 4).label.equals("eton_system_widget")) {
				mBitmap1 = BitmapFactory.decodeResource(
						mContext.getResources(),
						R.drawable.ic_menu_system_setting);// system_widget);
				mTitle1 = mContext.getString(R.string.system_widget);

			} else {

				AppWidgetProviderInfo info = list.get(position * 4);
				Drawable drawable = mPackageManager.getDrawable(
						info.provider.getPackageName(), info.icon, null);
				BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
				mBitmap1 = bitmapDrawable.getBitmap();

				mTitle1 = getWidgetDims(mContext, info);
				mTitle1 += info.label;
			}

		}

		if ((position * 4 + 1) < totalSize) {
			AppWidgetProviderInfo info = list.get(position * 4 + 1);
			Drawable drawable = mPackageManager.getDrawable(
					info.provider.getPackageName(), info.icon, null);
			BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
			mBitmap2 = bitmapDrawable.getBitmap();
			mTitle2 = getWidgetDims(mContext, info);
			mTitle2 += info.label;
		}

		if ((position * 4 + 2) < totalSize) {
			AppWidgetProviderInfo info = list.get(position * 4 + 2);
			Drawable drawable = mPackageManager.getDrawable(
					info.provider.getPackageName(), info.icon, null);
			BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
			mBitmap3 = bitmapDrawable.getBitmap();
			mTitle3 = getWidgetDims(mContext, info);
			mTitle3 += info.label;
		}

		if ((position * 4 + 3) < totalSize) {
			AppWidgetProviderInfo info = list.get(position * 4 + 3);
			Drawable drawable = mPackageManager.getDrawable(
					info.provider.getPackageName(), info.icon, null);
			BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
			mBitmap4 = bitmapDrawable.getBitmap();
			mTitle4 = getWidgetDims(mContext, info);
			mTitle4 += info.label;
		}
		/*
		 * for (int i = position*4; i < (position*4+4); i++){ if (position*4 <
		 * totalSize){
		 * 
		 * } }
		 */
	}

	private void getWallpaperBitmap(JSONArray object, int position, int totalSize){

		JSONObject data1 = null;
		JSONObject data2 = null;
		JSONObject data3 = null;
		JSONObject data4 = null;
		LauncherApplication app = (LauncherApplication)((Launcher)mContext).getApplication();
		if (position * 4 < totalSize) {
			try {
				data1 = (JSONObject) object.get(position * 4);
				//mBitmap1 = makeResIdToBitmap(data1.getInt("resid"));
				mBitmap1 = app.mWallpaperCache.getBitmap(data1.getInt("resid"), mContext);
				//app.mWallpaperCache.getBitmap(data1.getInt("resid"), mContext);
				
				if (data1.getInt("resid") == ScreenEditUtil.mLocalWallpaperID){
					mTitle1 = mContext.getString(R.string.local_wallpaper);
				}
				if (mResType != 0)
				mTitle1 = data1.getString("name");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if ((position * 4 + 1) < totalSize) {
			try {
				data2 = (JSONObject) object.get(position * 4 + 1);
				mBitmap2 = app.mWallpaperCache.getBitmap(data2.getInt("resid"), mContext);
				if (mResType != 0)
				mTitle2 = data2.getString("name");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if ((position * 4 + 2) < totalSize) {
			try {
				data3 = (JSONObject) object.get(position * 4 + 2);
				mBitmap3 = app.mWallpaperCache.getBitmap(data3.getInt("resid"), mContext);
				if (mResType != 0)
				mTitle3 = data3.getString("name");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if ((position * 4 + 3) < totalSize) {
			try {
				data4 = (JSONObject) object.get(position * 4 + 3);
				if (mResType != 0)
				mTitle4 = data4.getString("name");
				
				mBitmap4 = app.mWallpaperCache.getBitmap(data4.getInt("resid"), mContext);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	
	}
	
	private void getBitmapByResID(JSONArray object, int position, int totalSize) {

		JSONObject data1 = null;
		JSONObject data2 = null;
		JSONObject data3 = null;
		JSONObject data4 = null;

		if (position * 4 < totalSize) {
			try {
				data1 = (JSONObject) object.get(position * 4);
				mBitmap1 = makeResIdToBitmap(data1.getInt("resid"));
				
				if (data1.getInt("resid") == ScreenEditUtil.mLocalWallpaperID){
					mTitle1 = mContext.getString(R.string.local_wallpaper);
				}
				if (mResType != 0)
				mTitle1 = data1.getString("name");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if ((position * 4 + 1) < totalSize) {
			try {
				data2 = (JSONObject) object.get(position * 4 + 1);
				mBitmap2 = makeResIdToBitmap(data2.getInt("resid"));
				if (mResType != 0)
				mTitle2 = data2.getString("name");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if ((position * 4 + 2) < totalSize) {
			try {
				data3 = (JSONObject) object.get(position * 4 + 2);
				mBitmap3 = makeResIdToBitmap(data3.getInt("resid"));
				if (mResType != 0)
				mTitle3 = data3.getString("name");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if ((position * 4 + 3) < totalSize) {
			try {
				data4 = (JSONObject) object.get(position * 4 + 3);
				if (mResType != 0)
				mTitle4 = data4.getString("name");
				
				mBitmap4 = makeResIdToBitmap(data4.getInt("resid"));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private void clearCheckFlag(){
		mCheck1.setVisibility(INVISIBLE);
		mCheck2.setVisibility(INVISIBLE);
		mCheck3.setVisibility(INVISIBLE);
		mCheck4.setVisibility(INVISIBLE);
		
	}
	/**
	 * �����ݣ����ⲿ����.
	 * 
	 * @param object
	 */
	public void setData(final JSONArray object, final int position,
			final int resType, final int totalSize,
			ArrayList<AppWidgetProviderInfo> WidgetArray) {
		final int count = position;
		mTotalSize = totalSize;
		mResType = resType;
		mList = WidgetArray;
		mType = resType;
		mJSONArray = object;
		mPosition = position;
		// this.mObject = object;

		// ���ͼƬ��� ���̴߳���
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (resType == 1 || resType == 2) {
					// ������ԴresID�ķ�ʽ ת��Ϊbitmap
					getBitmapByResID(object, position, totalSize);
					// Message message = new Message();
					// mHandler.sendMessage(message);

				} else if (resType == 0) {
					//getBitmapByResID(object, position, totalSize);
					getWallpaperBitmap(object, position, totalSize);
					//getBitmapByPath(object, position, totalSize);
				} else if (resType == 3) {
					if (null != mList) {
						getWidgetBitmap(mList, position, totalSize);
					}
				}

				Message message = new Message();
				mHandler.sendMessage(message);
			}

		}).start();

		
		setImageViewOnTouchEvent(mAlbumImageView1);

		mAlbumImageView1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// Toast.makeText(mContext,
				// Integer.toString(count*ITEMNUMS_PER_PAGE),Toast.LENGTH_SHORT).show();
				handleDiffType(mType, count * ITEMNUMS_PER_PAGE);
				if (!isHandleScreenEditClickType()){
					return;
				}
				mPositionEffectOfPage  = count;
				mPositionInPage = 0;
				
				SharedPreferencesUtils.setEffectInScreenUIPosition(mContext, count, 0);
				mCheck1.setVisibility(VISIBLE);
				
				mCheck4.setVisibility(INVISIBLE);
				mCheck2.setVisibility(INVISIBLE);
				mCheck3.setVisibility(INVISIBLE);
			}
		});

		setImageViewOnTouchEvent(mAlbumImageView2);
		
		
		mAlbumImageView2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				handleDiffType(mType, count * ITEMNUMS_PER_PAGE + 1);
				if (!isHandleScreenEditClickType()){
					return;
				}
				
				mPositionEffectOfPage  = count;
				mPositionInPage = 1;
				SharedPreferencesUtils.setEffectInScreenUIPosition(mContext, count, 1);
				// Toast.makeText(mContext,
				// Integer.toString(count*ITEMNUMS_PER_PAGE+1),Toast.LENGTH_SHORT).show();
				mCheck2.setVisibility(VISIBLE);
				
				mCheck1.setVisibility(INVISIBLE);
				mCheck4.setVisibility(INVISIBLE);
				mCheck3.setVisibility(INVISIBLE);
			}
		});

		
		setImageViewOnTouchEvent(mAlbumImageView3);
		mAlbumImageView3.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				handleDiffType(mType, count * ITEMNUMS_PER_PAGE + 2);
				if (!isHandleScreenEditClickType()){
					return;
				}
				
				mPositionEffectOfPage  = count;
				mPositionInPage = 2;
				SharedPreferencesUtils.setEffectInScreenUIPosition(mContext, count, 2);
				// Toast.makeText(mContext,
				// Integer.toString(count*ITEMNUMS_PER_PAGE+2),Toast.LENGTH_SHORT).show();
				mCheck3.setVisibility(VISIBLE);
				
				mCheck1.setVisibility(INVISIBLE);
				mCheck2.setVisibility(INVISIBLE);
				mCheck4.setVisibility(INVISIBLE);
			}
		});

		
		setImageViewOnTouchEvent(mAlbumImageView4);
		mAlbumImageView4.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				handleDiffType(mType, count * ITEMNUMS_PER_PAGE + 3);
				if (!isHandleScreenEditClickType()){
					return;
				}
				mPositionEffectOfPage  = count;
				mPositionInPage = 3;
				SharedPreferencesUtils.setEffectInScreenUIPosition(mContext, count, 3);
				// Toast.makeText(mContext,
				// Integer.toString(count*ITEMNUMS_PER_PAGE+3),Toast.LENGTH_SHORT).show();
				
				mCheck4.setVisibility(VISIBLE);
				mCheck1.setVisibility(INVISIBLE);
				mCheck2.setVisibility(INVISIBLE);
				mCheck3.setVisibility(INVISIBLE);
				
			}
		});

	}
	private void setImageViewOnTouchEvent(ImageView imageView){
		imageView.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(final View v, MotionEvent event) {
				// TODO Auto-generated method stub

	            // Open shortcut
	            if (event.getAction() == MotionEvent.ACTION_DOWN){
	            	ValueAnimator animation = ValueAnimator.ofFloat(0f, 1f);
	            	animation.setDuration(400);
	            	animation.addUpdateListener(new AnimatorUpdateListener() {
	            	    @Override
	            	    public void onAnimationUpdate(ValueAnimator animation) {
	            	        float animatedValue = (Float) animation.getAnimatedValue();
	            	        v.setAlpha(1 - 0.3f * animatedValue);
	            	        v.setScaleX(1 - 0.1f * animatedValue);
	            	        v.setScaleY(1 - 0.1f * animatedValue);
	            	    }
	            	});
	            	animation.setInterpolator(new Workspace.ZoomInInterpolator());
	            	animation.start();
	    	}else if(event.getAction() == MotionEvent.ACTION_UP ||event.getAction() == MotionEvent.ACTION_CANCEL){
	    		final boolean isUp = (event.getAction() == MotionEvent.ACTION_UP);
	    		ValueAnimator animation = ValueAnimator.ofFloat(0f, 1f);
	        	animation.setDuration(400);
	        	animation.addUpdateListener(new AnimatorUpdateListener() {
	        	    @Override
	        	    public void onAnimationUpdate(ValueAnimator animation) {
	        	        float animatedValue = (Float) animation.getAnimatedValue();
	        	        v.setAlpha(v.getAlpha() + (1 - v.getAlpha()) * animatedValue);
						v.setScaleX(v.getScaleX() + (1 - v.getScaleX()) * animatedValue);
						v.setScaleY(v.getScaleY() + (1 - v.getScaleY()) * animatedValue);
	        	    }
	        	});
	        	animation.setInterpolator(new Workspace.ZoomInInterpolator());
	        	animation.addListener(new AnimatorListener() {
					
					@Override
					public void onAnimationStart(Animator arg0) {
						// TODO �Զ���ɵķ������
						
					}
					
					@Override
					public void onAnimationRepeat(Animator arg0) {
						// TODO �Զ���ɵķ������
						
					}
					
					@Override
					public void onAnimationEnd(Animator arg0) {/*
						// TODO �Զ���ɵķ������
						if (isUp){ 
							if(tag instanceof ShortcutInfo){
						        final Intent intent = ((ShortcutInfo) tag).intent;
					            int[] pos = new int[2];
					            v.getLocationOnScreen(pos);
					            intent.setSourceBounds(new Rect(pos[0], pos[1],
					                    pos[0] + v.getWidth(), pos[1] + v.getHeight()));
								boolean success = startActivitySafely(v, intent, tag);
			
					            if (success && v instanceof BubbleTextView) {
					                mWaitingForResume = (BubbleTextView) v;
					                mWaitingForResume.setStayPressed(true);
					            }
							}else if((tag instanceof FolderInfo && v instanceof FolderIcon)){
								  FolderIcon fi = (FolderIcon) v;
					              handleFolderClick(fi);
							}
					}
					*/}
					@Override
					public void onAnimationCancel(Animator arg0) {
						// TODO �Զ���ɵķ������
						
					}
				});
	        	animation.start();
	    	 }
	    	
				return false;
			}
		});
	}
	
	public boolean isHandleScreenEditClickType(){
		if (ScreenEditUtil.editMenuChoose == ScreenEditUtil.MENU_ID_WIDGET
				|| ScreenEditUtil.editMenuChoose == ScreenEditUtil.MENU_ID_THEME
				|| ScreenEditUtil.editMenuChoose == ScreenEditUtil.MENU_ID_WALLPAPER){
			return false;
		}
		return true;
	}
	
	/**
	 * �ڴ����?�ⲿ����
	 */
	public void recycle() {
		mAlbumImageView1.setImageBitmap(null);
		mAlbumImageView2.setImageBitmap(null);
		mAlbumImageView3.setImageBitmap(null);
		mAlbumImageView4.setImageBitmap(null);

		if (mBitmap1 != null) {
			if (!mBitmap1.isRecycled()) {
				mBitmap1.recycle();
			}
			mBitmap1 = null;
		}

		if (mBitmap2 != null) {
			if (!mBitmap2.isRecycled()) {
				mBitmap2.recycle();
			}
			mBitmap2 = null;
		}

		if (mBitmap3 != null) {
			if (!mBitmap3.isRecycled()) {
				mBitmap3.recycle();
			}
			mBitmap3 = null;
		}

		if (mBitmap4 != null) {
			if (!mBitmap4.isRecycled()) {
				mBitmap4.recycle();
			}
			mBitmap4 = null;
		}
		System.gc();
	}

	/**
	 * ���¼��� �ⲿ����
	 */
	public void reload(final JSONArray object, final int position) {
		// try {
		/*
		 * JSONObject data1 = (JSONObject)object.get(position*4);
		 * mAlbumImageView1.setImageResource(data1.getInt("resid")); JSONObject
		 * data2 = (JSONObject)object.get(position*4+1);
		 * mAlbumImageView2.setImageResource(data2.getInt("resid")); JSONObject
		 * data3 = (JSONObject)object.get(position*4+2);
		 * mAlbumImageView3.setImageResource(data3.getInt("resid")); JSONObject
		 * data4 = (JSONObject)object.get(position*4+3);
		 * mAlbumImageView4.setImageResource(data4.getInt("resid"));
		 */

		// ���ͼƬ������̴߳���
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (mResType == 1 || mResType == 2) {
					// ������ԴresID�ķ�ʽ ת��Ϊbitmap
					getBitmapByResID(object, position, mTotalSize);

				} else if (mResType == 0) {
					getWallpaperBitmap(object, position, mTotalSize);
					//getBitmapByResID(object, position, mTotalSize);
					//getBitmapByPath(object, position, mTotalSize);
				} else if (mResType == 3) {
					if (mList != null) {
						getWidgetBitmap(mList, position, mTotalSize);
					}
				}

				Message message = new Message();
				mHandler.sendMessage(message);
			}

		}).start();
		// }catch (JSONException e) {
		// e.printStackTrace();
		// }
	}

	private void startLocalWallpaper() {
		Intent intent = new Intent();
		intent.setClassName(mContext,
				"com.eton.launcher.setting.WallpaperAlertActivity");
		mContext.startActivity(intent, null);
	}

	private void enterGrallery(){
		String packageName = "com.android.gallery3d";
		String activityName = "com.android.gallery3d.app.Wallpaper";
		Intent intent = new Intent(Intent.ACTION_SET_WALLPAPER);
        intent.setComponent(new ComponentName(
                  packageName, activityName));
        mContext.startActivity(intent);
	}
	
	private boolean isHandelWallpaperCmd(int resid){
		if (resid == ScreenEditUtil.mLocalWallpaperID) {
			// TODO
			startLocalWallpaper();
			return true;
		}
		
		if (resid == ScreenEditUtil.mWallpaperAddId){
			//TODO enter gallery to choice wallpaper
			enterGrallery();
			return true;
		}
		return false;
	}
	
	private Object obj = new  Object();
	
	private void handleDiffType(int choice, int postion) {
		switch (choice) {
		case ScreenEditUtil.MENU_ID_WALLPAPER:
			// TODO ��ֽ����
			try {
				JSONObject data = (JSONObject) mJSONArray.get(postion);

				
				if (isHandelWallpaperCmd(Integer.parseInt(data.getString("resid")))){
					return;
				}

				/*if (data.getString("path").equals("local_wallpaper")) {
					// TODO
					startLocalWallpaper();
					return;
				}*/
				
				//use WallpaperTask to set wallpaper, avoid ANR problem
				if (!mSetWallpaperFlag){
					//new WallpaperTask().execute(data.getString("path"));
					new WallpaperTask().execute(data.getString("resid"));
				}
				
			} catch (JSONException e) {
			}
			break;
		case ScreenEditUtil.MENU_ID_THEME:
			// TODO ���⴦��
			break;
		case ScreenEditUtil.MENU_ID_EFFECT:
			// TODO ��Ч����
			mEtonWorkSpace.setTransitionEffect(postion - 1);
			SharedPreferencesUtils.setWorkspaceEffect(mContext, postion - 1);
			break;
		case ScreenEditUtil.MENU_ID_WIDGET:
			// TODO С���ߴ���
			//Modified
			if (postion >= mList.size()){
				return;
			}
			
			//Fixed when in the add screen, not allow to add shortcut and widget
			if (((mEtonWorkSpace.getCurrentPage()+ 1) ==  mEtonWorkSpace.getChildCount()) || (mEtonWorkSpace.getCurrentPage() == 0)){
				Toast.makeText(mContext,
						mContext.getString(R.string.screen_not_allow_add),Toast.LENGTH_SHORT).show();
				return;
			}
			
			if (mList.get(postion).label.equals("eton_system_widget")) {
				mEtonWidgetView.startSystemWidget();
				return;
			}
			
			/*if (!mIsAddCompleted){
				mIsAddCompleted = false;
				return;
			}*/
			
			mIsAddCompleted = false;
			PendingAddItemInfo createItemInfo = null;
			createItemInfo = new PendingAddWidgetInfo(mList.get(postion), null,
					null);
			int[] spanXY = Launcher.getSpanForWidget(mContext,
					mList.get(postion));
			createItemInfo.spanX = spanXY[0];
			createItemInfo.spanY = spanXY[1];
			int[] minSpanXY = Launcher.getMinSpanForWidget(mContext,
					mList.get(postion));
			createItemInfo.minSpanX = minSpanXY[0];
			createItemInfo.minSpanY = minSpanXY[1];
			
			ScreenEditUtil.setAddWidgetScreen(mEtonWorkSpace.getCurrentPage());
			synchronized (obj)
            {
			    mEtonWidgetView
                .preloadWidget((PendingAddWidgetInfo) createItemInfo);
        
            }
		
			break;
		default:
			break;

		}
	}
	
	public  void setPageChangeListen(){
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int arg0) {
				// TODO Auto-generated method stub
				if (ScreenEditUtil.editMenuChoose == ScreenEditUtil.MENU_ID_WIDGET
						|| ScreenEditUtil.editMenuChoose == ScreenEditUtil.MENU_ID_THEME
						|| ScreenEditUtil.editMenuChoose == ScreenEditUtil.MENU_ID_WALLPAPER){
					return;
				}
				
				clearCheckFlag();
				ViewPagerItemView view = (ViewPagerItemView)ViewPagerAdapter.mPagedViewEditScreen.getChildAt(arg0);
				if (arg0 == mPositionEffectOfPage){
					
					view.mCheck1.setVisibility(INVISIBLE);
					view.mCheck2.setVisibility(INVISIBLE);
					view.mCheck3.setVisibility(INVISIBLE);
					view.mCheck4.setVisibility(INVISIBLE);
					if (mPositionInPage == 0){
						view.mCheck1.setVisibility(VISIBLE);
					}else if (mPositionInPage == 1){
						view.mCheck2.setVisibility(VISIBLE);
					}else if (mPositionInPage == 2){
						view.mCheck3.setVisibility(VISIBLE);
					}else if (mPositionInPage == 3){
						view.mCheck4.setVisibility(VISIBLE);
					}
						
				}else{
					view.mCheck1.setVisibility(INVISIBLE);
					view.mCheck2.setVisibility(INVISIBLE);
					view.mCheck3.setVisibility(INVISIBLE);
					view.mCheck4.setVisibility(INVISIBLE);
				}
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
			    if (ScreenEditUtil.editMenuChoose == ScreenEditUtil.MENU_ID_WIDGET
                    || ScreenEditUtil.editMenuChoose == ScreenEditUtil.MENU_ID_THEME
                    || ScreenEditUtil.editMenuChoose == ScreenEditUtil.MENU_ID_WALLPAPER){
                return;
             }
            
               clearCheckFlag();
			    
               int total =  ViewPagerAdapter.mPagedViewEditScreen.getChildCount();
               
               for (int i = 0; i < total; i++)
              {
                   ViewPagerItemView view = (ViewPagerItemView)ViewPagerAdapter.mPagedViewEditScreen.getChildAt(i);
                   
                   if (i == mPositionEffectOfPage){
                       
                       view.mCheck1.setVisibility(INVISIBLE);
                       view.mCheck2.setVisibility(INVISIBLE);
                       view.mCheck3.setVisibility(INVISIBLE);
                       view.mCheck4.setVisibility(INVISIBLE);
                       if (mPositionInPage == 0){
                           view.mCheck1.setVisibility(VISIBLE);
                       }else if (mPositionInPage == 1){
                           view.mCheck2.setVisibility(VISIBLE);
                       }else if (mPositionInPage == 2){
                           view.mCheck3.setVisibility(VISIBLE);
                       }else if (mPositionInPage == 3){
                           view.mCheck4.setVisibility(VISIBLE);
                       }
                   }
                   else
                   {
                       view.mCheck1.setVisibility(INVISIBLE);
                       view.mCheck2.setVisibility(INVISIBLE);
                       view.mCheck3.setVisibility(INVISIBLE);
                       view.mCheck4.setVisibility(INVISIBLE);
                   }
                   
             }
				
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	
	private Bitmap mBitmap = null;
	private int resultOfwallpaper = -1;
	private boolean mSetWallpaperFlag = false;
	private static final int ERROR_NOT_EXIST = 0;
	private static final int ERROR_EXCEPTION = 1;
	private ProgressDialog mProgressDialog = null;
	
    class WallpaperTask extends AsyncTask<String, Void, Bitmap> {
        BitmapFactory.Options mOptions;

        WallpaperTask() {
            mOptions = new BitmapFactory.Options();
            mOptions.inDither = false;
            mOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;            
        }
        
        @Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			//Show Dialog progress
			mSetWallpaperFlag = true;
			if(null == mProgressDialog){
				mProgressDialog = new ProgressDialog(mContext);
				mProgressDialog.setMessage(mContext.getResources().getString(R.string.set_wallpaper_progress));
				mProgressDialog.setIndeterminate(false);
				mProgressDialog.setCancelable(false);
				mProgressDialog.show();
			}
		}

		protected Bitmap doInBackground(String... params) {
            if (isCancelled()) return null;
            
            //Bitmap bp = makeBitmapFromPath(params[0], true);
            Bitmap bp = makeResIdToBitmap(Integer.parseInt(params[0]));
			if (null == bp) {
				// TODO deal with file is not exist
				resultOfwallpaper = ERROR_NOT_EXIST;
				return null;
			}
			
			WallpaperManager wpm = (WallpaperManager) mContext
					.getSystemService(Context.WALLPAPER_SERVICE);
			try {
				//wpm.setBitmap(bp);
				wpm.setResource(Integer.parseInt(params[0]));
			} catch (IOException e) {
				resultOfwallpaper = ERROR_EXCEPTION;
				return null;
			}
            
           return bp;
        }

        @Override
        protected void onPostExecute(Bitmap b) {
        	
        	if(null != mProgressDialog){
        		mProgressDialog.dismiss();
        		mProgressDialog = null;
        	}
            if (b == null) {
            	if (resultOfwallpaper == ERROR_NOT_EXIST){
            		Toast.makeText(mContext, R.string.wallpaper_not_exist,
							Toast.LENGTH_SHORT).show();
            	}else if (resultOfwallpaper == ERROR_EXCEPTION){
            		
            	}
            	resultOfwallpaper = -1;
            	mSetWallpaperFlag = false;
            	return;
            }

            if (!isCancelled() && !mOptions.mCancel) {
                // Help the GC
                if (mBitmap != null) {
                    mBitmap.recycle();
                }
                mBitmap = b;
            } else {
               b.recycle(); 
            }
            resultOfwallpaper = -1;
            mSetWallpaperFlag = false;
        }

        void cancel() {
            mOptions.requestCancelDecode();
            super.cancel(true);
        }
    }
}
