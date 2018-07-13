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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import com.android.launcher.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * Various utilities shared amongst the Launcher's classes.
 */
public   class Utilities {
	@SuppressWarnings("unused")
	private static final String TAG = "Launcher.Utilities";

	private static int sIconWidth = -1;
	private static int sIconHeight = -1;
	private static int sIconTextureWidth = -1;
	private static int sIconTextureHeight = -1;

	private static final Paint sBlurPaint = new Paint();
	private static final Paint sGlowColorPressedPaint = new Paint();
	private static final Paint sGlowColorFocusedPaint = new Paint();
	private static final Paint sDisabledPaint = new Paint();
	private static final Rect sOldBounds = new Rect();
	private static final Canvas sCanvas = new Canvas();

	private static int msIconPadding = 0;
	private static int msIconMaxPadding = 0;
	private static int msIconIncreaseSize = 0;
	private static int msIconNotRectZoomSize = 0;

	static {
		sCanvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG,
				Paint.FILTER_BITMAP_FLAG));
	}
	static int sColors[] = { 0xffff0000, 0xff00ff00, 0xff0000ff };
	static int sColorIndex = 0;

	/**
	 * Returns a bitmap suitable for the all apps view. Used to convert pre-ICS
	 * icon bitmaps that are stored in the database (which were 74x74 pixels at
	 * hdpi size) to the proper size (48dp)
	 */
 public	static Bitmap createIconBitmap(Bitmap icon, Context context) {
		int textureWidth = sIconTextureWidth;
		int textureHeight = sIconTextureHeight;
		int sourceWidth = icon.getWidth();
		int sourceHeight = icon.getHeight();
		if (sourceWidth > textureWidth && sourceHeight > textureHeight) {
			// Icon is bigger than it should be; clip it (solves the GB->ICS
			// migration case)
			return Bitmap.createBitmap(icon, (sourceWidth - textureWidth) / 2,
					(sourceHeight - textureHeight) / 2, textureWidth,
					textureHeight);
		} else if (sourceWidth == textureWidth && sourceHeight == textureHeight) {
			// Icon is the right size, no need to change it
			return icon;
		} else {
			// Icon is too small, render to a larger bitmap
			final Resources resources = context.getResources();
			return createIconBitmap(new BitmapDrawable(resources, icon),
					context);
		}
	}

	// added by ETON guolinan
	public static Bitmap drawableToBitmap(Drawable drawable) {
		int width = drawable.getIntrinsicWidth();
		int height = drawable.getIntrinsicHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height, drawable
				.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, width, height);
		drawable.draw(canvas);
		return bitmap;

	}

	// added by ETON xyg
	public static Bitmap drawableToBitmap(Context context, Drawable drawable) {
		// Modified by liukaibang {{
		// Bitmap cover = drawableToBitmap(context.getResources().getDrawable(
		//		R.drawable.ic_cover_white));
		int coverDrawableId = LauncherApplication.getDisplayFactory(context).getCoverDrawableId();
		Bitmap cover = drawableToBitmap(context.getResources().getDrawable(coverDrawableId));
		// end }}
		
//		int width = drawable.getIntrinsicWidth();
//		int height = drawable.getIntrinsicHeight();
		Bitmap bitmap = Bitmap.createBitmap(cover.getWidth(), cover.getHeight(), drawable
				.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, cover.getWidth(), cover.getHeight());
		drawable.draw(canvas);
		return bitmap;
	}
	// end xyg

	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, Context context) {
		// Modified by liukaibang {{
		// Bitmap cover = drawableToBitmap(context.getResources().getDrawable(
		//		R.drawable.ic_cover_white));
		int coverDrawableId = LauncherApplication.getDisplayFactory(context).getCoverDrawableId();
		Bitmap cover = drawableToBitmap(context.getResources().getDrawable(coverDrawableId/*R.drawable.ic_cover_white*/));
		// end }}
		
		int w = cover.getWidth();
		int h = cover.getHeight();
		bitmap = zoomBitmap(bitmap, w, h);
		int[] pixels = new int[w * h];
		int[] newPixels = new int[w * h];
		bitmap.getPixels(pixels, 0, w, 0, 0, w, h);
		cover.getPixels(newPixels, 0, w, 0, 0, w, h);

		for (int i = 0; i < w * h; i++) {
			if (newPixels[i] != 0) {
				newPixels[i] = pixels[i] & newPixels[i];
			}
		}
		cover = Bitmap.createBitmap(newPixels, 0, w, w, h,
				Bitmap.Config.ARGB_8888);
		return cover;
	}

	public static Bitmap zoomBitmap(Bitmap bitmap, int w, int h) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		Matrix matrix = new Matrix();
		float scaleWidht = ((float) w / width);
		float scaleHeight = ((float) h / height);
		matrix.postScale(scaleWidht, scaleHeight);
		Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height,
				matrix, true);
		return newbmp;
	}

	static Bitmap createIconBitmap(Drawable icon, Context context) {
		if (icon == null)
			return null;
		synchronized (sCanvas) {
			if (sIconWidth == -1) {
				initStatics(context);
			}

			int width = sIconWidth;
			int height = sIconHeight;
			if (icon instanceof PaintDrawable) {
				PaintDrawable painter = (PaintDrawable) icon;
				painter.setIntrinsicWidth(width);
				painter.setIntrinsicHeight(height);
			} else if (icon instanceof BitmapDrawable) {
				// Ensure the bitmap has a density.
				BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
				Bitmap bitmap = bitmapDrawable.getBitmap();
				if (bitmap.getDensity() == Bitmap.DENSITY_NONE) {
					bitmapDrawable.setTargetDensity(context.getResources()
							.getDisplayMetrics());
				}
			}

			// Modified by liukaibang {{
			// Old code
			// final Bitmap bitmap_out = drawableToBitmap(context.getResources()
			//		.getDrawable(R.drawable.app_bg_0));
			
			// New Code
			int coverDrawableId = LauncherApplication.getDisplayFactory(context)
					.getAppBackGroundDrawableId();
			final Bitmap bitmap_out = drawableToBitmap(context.getResources()
					.getDrawable(coverDrawableId));
			// end }}

			Bitmap bitmap = drawableToBitmap(icon);
			Canvas canvas = new Canvas(bitmap_out);
			int widthPadding = 0;
			int heightPadding = 0;
			msIconPadding = (int) context.getResources().getDimension(
					R.dimen.icon_bitmap_padding);
			msIconMaxPadding = (int) context.getResources().getDimension(
					R.dimen.icon_bitmap_max_padding);
			msIconIncreaseSize = (int) context.getResources().getDimension(
					R.dimen.icon_bitmap_increase_size);
			msIconNotRectZoomSize = (int) context.getResources().getDimension(
					R.dimen.icon_bitmap_not_rect_size);

			if (isRect(bitmap, context)) {
				bitmap = getRoundedCornerBitmap(bitmap, context);
			} else {
				widthPadding = (bitmap_out.getWidth() - bitmap.getWidth()) / 2;
				heightPadding = (bitmap_out.getHeight() - bitmap.getHeight()) / 2;
				if (widthPadding < 0 || heightPadding < 0) {
					bitmap = zoomBitmap(bitmap, msIconNotRectZoomSize,
							msIconNotRectZoomSize);
					widthPadding = (bitmap_out.getWidth() - bitmap.getWidth()) / 2;
					heightPadding = (bitmap_out.getHeight() - bitmap
							.getHeight()) / 2;
				}
			}
			BitmapDrawable bd = new BitmapDrawable(bitmap);
			bd.setBounds(0 + widthPadding, 0 + heightPadding, bitmap.getWidth()
					+ widthPadding, bitmap.getWidth() + heightPadding);
			bd.draw(canvas);
			return bitmap_out;
		}
	}

	static boolean isRect(Bitmap bitmap, Context context) {
		boolean isRect = true;
		int w = bitmap.getWidth(), width = bitmap.getWidth();
		int h = bitmap.getHeight(), height = bitmap.getHeight();

		// Modified by liukaibang {{
		// Old code
		/*
		// modefied by huryjiang if icon width or height > bg0 ,can be
		// recognized rect.
		Bitmap bitmap_out = drawableToBitmap(context.getResources()
				.getDrawable(R.drawable.app_bg_0));
		 */
		
		// New Code
		int coverDrawableId = LauncherApplication.getDisplayFactory(context)
				.getAppBackGroundDrawableId();
		final Bitmap bitmap_out = drawableToBitmap(context.getResources()
				.getDrawable(coverDrawableId));
		// end }}
					
		int bgWidth = bitmap_out.getWidth();
		int bgHeight = bitmap_out.getHeight();
		if (width > bgWidth && height > bgHeight) {
			return isRect;
		}

		Bitmap isoBitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(isoBitmap);

		Rect rect = new Rect();
		// rect.set(0+12, 0+12, width-12, height-12);
		rect.set(0 + msIconPadding, 0 + msIconPadding, width - msIconPadding,
				height - msIconPadding);
		Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setStyle(Style.STROKE);
		canvas.drawRect(rect, paint);

		int[] pixels = new int[w * h];
		int[] isoBitmapPixels = new int[w * h];
		boolean isIndenting = true;

		// this is important
		int indentation = msIconPadding;
		while (isIndenting && indentation <= msIconMaxPadding) {
			bitmap.getPixels(pixels, 0, w, 0, 0, w, h);
			isoBitmap.getPixels(isoBitmapPixels, 0, w, 0, 0, w, h);
			for (int i = 0; i < w * h; i++) {
				if (isoBitmapPixels[i] != 0) {
					if (pixels[i] != 0) {
						isIndenting = false;
						break;
					}
				}
			}
			if (isIndenting) {
				indentation += msIconIncreaseSize;
				rect.set(0 + indentation, 0 + indentation, width - indentation,
						height - indentation);
				canvas.drawRect(rect, paint);
			}
		}

		bitmap.getPixels(pixels, 0, w, 0, 0, w, h);
		isoBitmap.getPixels(isoBitmapPixels, 0, w, 0, 0, w, h);
		for (int i = 0; i < w * h; i++) {
			if (isoBitmapPixels[i] != 0) {
				if (pixels[i] == 0) {
					isRect = false;
					break;
				}
			}
		}
		return isRect;
	}

	// end
	/**
	 * Returns a bitmap suitable for the all apps view.
	 */
	/*
	 * static Bitmap createIconBitmap(Drawable icon, Context context) {
	 * synchronized (sCanvas) { // we share the statics :-( if (sIconWidth ==
	 * -1) { initStatics(context); }
	 * 
	 * int width = sIconWidth; int height = sIconHeight;
	 * 
	 * if (icon instanceof PaintDrawable) { PaintDrawable painter =
	 * (PaintDrawable) icon; painter.setIntrinsicWidth(width);
	 * painter.setIntrinsicHeight(height); } else if (icon instanceof
	 * BitmapDrawable) { // Ensure the bitmap has a density. BitmapDrawable
	 * bitmapDrawable = (BitmapDrawable) icon; Bitmap bitmap =
	 * bitmapDrawable.getBitmap(); if (bitmap.getDensity() ==
	 * Bitmap.DENSITY_NONE) {
	 * bitmapDrawable.setTargetDensity(context.getResources
	 * ().getDisplayMetrics()); } } int sourceWidth = icon.getIntrinsicWidth();
	 * int sourceHeight = icon.getIntrinsicHeight(); if (sourceWidth > 0 &&
	 * sourceHeight > 0) { // There are intrinsic sizes. if (width < sourceWidth
	 * || height < sourceHeight) { // It's too big, scale it down. final float
	 * ratio = (float) sourceWidth / sourceHeight; if (sourceWidth >
	 * sourceHeight) { height = (int) (width / ratio); } else if (sourceHeight >
	 * sourceWidth) { width = (int) (height * ratio); } } else if (sourceWidth <
	 * width && sourceHeight < height) { // Don't scale up the icon width =
	 * sourceWidth; height = sourceHeight; } }
	 * 
	 * // no intrinsic size --> use default size int textureWidth =
	 * sIconTextureWidth; int textureHeight = sIconTextureHeight;
	 * 
	 * final Bitmap bitmap = Bitmap.createBitmap(textureWidth, textureHeight,
	 * Bitmap.Config.ARGB_8888); final Canvas canvas = sCanvas;
	 * canvas.setBitmap(bitmap);
	 * 
	 * final int left = (textureWidth-width) / 2; final int top =
	 * (textureHeight-height) / 2;
	 * 
	 * Random rdm = new Random(); int random = rdm.nextInt(6); int res =
	 * R.drawable.app_bg1; switch (random){ case 0: res =
	 * R.drawable.app_bg1;break; case 1: res = R.drawable.app_bg2;break; case 2:
	 * res = R.drawable.app_bg3;break; case 3: res = R.drawable.app_bg4;break;
	 * case 4: res = R.drawable.app_bg5;break; case 5: res =
	 * R.drawable.app_bg6;break; } Bitmap bg =
	 * BitmapFactory.decodeResource(context.getResources(), res);
	 * 
	 * Rect r = new Rect(0, 0, bg.getWidth(), bg.getHeight());
	 * canvas.drawBitmap(bg, r, canvas.getClipBounds(), new Paint());
	 * 
	 * @SuppressWarnings("all") // suppress dead code warning final boolean
	 * debug = false; if (debug) { // draw a big box for the icon for debugging
	 * canvas.drawColor(sColors[sColorIndex]); if (++sColorIndex >=
	 * sColors.length) sColorIndex = 0; Paint debugPaint = new Paint();
	 * debugPaint.setColor(0xffcccc00); canvas.drawRect(left+1, top+1,
	 * left+width, top+height, debugPaint); } sOldBounds.set(icon.getBounds());
	 * icon.setBounds(left+10, top+10, left+width, top+height);
	 * icon.draw(canvas); icon.setBounds(sOldBounds); canvas.setBitmap(null);
	 * 
	 * return bitmap; } }
	 */

	static void drawSelectedAllAppsBitmap(Canvas dest, int destWidth,
			int destHeight, boolean pressed, Bitmap src) {
		synchronized (sCanvas) { // we share the statics :-(
			if (sIconWidth == -1) {
				// We can't have gotten to here without src being initialized,
				// which
				// comes from this file already. So just assert.
				// initStatics(context);
				throw new RuntimeException(
						"Assertion failed: Utilities not initialized");
			}

			dest.drawColor(0, PorterDuff.Mode.CLEAR);

			int[] xy = new int[2];
			Bitmap mask = src.extractAlpha(sBlurPaint, xy);

			float px = (destWidth - src.getWidth()) / 2;
			float py = (destHeight - src.getHeight()) / 2;
			dest.drawBitmap(mask, px + xy[0], py + xy[1],
					pressed ? sGlowColorPressedPaint : sGlowColorFocusedPaint);

			mask.recycle();
		}
	}

	/**
	 * Returns a Bitmap representing the thumbnail of the specified Bitmap. The
	 * size of the thumbnail is defined by the dimension
	 * android.R.dimen.launcher_application_icon_size.
	 * 
	 * @param bitmap
	 *            The bitmap to get a thumbnail of.
	 * @param context
	 *            The application's context.
	 * 
	 * @return A thumbnail for the specified bitmap or the bitmap itself if the
	 *         thumbnail could not be created.
	 */
	static Bitmap resampleIconBitmap(Bitmap bitmap, Context context) {
		synchronized (sCanvas) { // we share the statics :-(
			if (sIconWidth == -1) {
				initStatics(context);
			}

			if (bitmap.getWidth() == sIconWidth
					&& bitmap.getHeight() == sIconHeight) {
				return bitmap;
			} else {
				final Resources resources = context.getResources();
				return createIconBitmap(new BitmapDrawable(resources, bitmap),
						context);
			}
		}
	}

	static Bitmap drawDisabledBitmap(Bitmap bitmap, Context context) {
		synchronized (sCanvas) { // we share the statics :-(
			if (sIconWidth == -1) {
				initStatics(context);
			}
			final Bitmap disabled = Bitmap.createBitmap(bitmap.getWidth(),
					bitmap.getHeight(), Bitmap.Config.ARGB_8888);
			final Canvas canvas = sCanvas;
			canvas.setBitmap(disabled);

			canvas.drawBitmap(bitmap, 0.0f, 0.0f, sDisabledPaint);

			canvas.setBitmap(null);

			return disabled;
		}
	}

	private static void initStatics(Context context) {
		final Resources resources = context.getResources();
		final DisplayMetrics metrics = resources.getDisplayMetrics();
		final float density = metrics.density;
		
		
		// Modified by liukaibang {{
		// Old code
		// // change by eton lisidong:
		// sIconWidth = sIconHeight = BitmapFactory.decodeResource(
		//		context.getResources(), R.drawable.app_bg_0).getWidth();
		
		// New Code
		int coverDrawableId = LauncherApplication.getDisplayFactory(context)
				.getAppBackGroundDrawableId();
		sIconWidth = sIconHeight = BitmapFactory.decodeResource(
				context.getResources(), coverDrawableId).getWidth();
		// end }}
					
		// sIconWidth = sIconHeight = (int)
		// resources.getDimension(R.dimen.app_icon_size);
		sIconTextureWidth = sIconTextureHeight = sIconWidth;

		sBlurPaint.setMaskFilter(new BlurMaskFilter(5 * density,
				BlurMaskFilter.Blur.NORMAL));
		sGlowColorPressedPaint.setColor(0xffffc300);
		sGlowColorFocusedPaint.setColor(0xffff8e00);

		ColorMatrix cm = new ColorMatrix();
		cm.setSaturation(0.2f);
		sDisabledPaint.setColorFilter(new ColorMatrixColorFilter(cm));
		sDisabledPaint.setAlpha(0x88);
	}

	/** Only works for positive numbers. */
	static int roundToPow2(int n) {
		int orig = n;
		n >>= 1;
		int mask = 0x8000000;
		while (mask != 0 && (n & mask) == 0) {
			mask >>= 1;
		}
		while (mask != 0) {
			n |= mask;
			mask >>= 1;
		}
		n += 1;
		if (n != orig) {
			n <<= 1;
		}
		return n;
	}

	static int generateRandomId() {
		return new Random(System.currentTimeMillis()).nextInt(1 << 24);
	}

	/**
	 * M: Check whether the given component name is enabled.
	 * 
	 * @param context
	 * @param cmpName
	 * @return true if the component is in default or enable state, and the
	 *         application is also in default or enable state, false if in
	 *         disable or disable user state.
	 */
	static boolean isComponentEnabled(final Context context,
			final ComponentName cmpName) {
		final String pkgName = cmpName.getPackageName();
		final PackageManager pm = context.getPackageManager();
		// Check whether the package has been uninstalled.
		PackageInfo pInfo = null;
		try {
			pInfo = pm.getPackageInfo(pkgName, 0);
		} catch (NameNotFoundException e) {
			LauncherLog.i(TAG,
					"isComponentEnabled NameNotFoundException: pkgName = "
							+ pkgName);
		}

		if (pInfo == null) {
			LauncherLog.d(TAG,
					"isComponentEnabled return false because package "
							+ pkgName + " has been uninstalled!");
			return false;
		}

		final int pkgEnableState = pm.getApplicationEnabledSetting(pkgName);
		if (LauncherLog.DEBUG) {
			LauncherLog.d(TAG, "isComponentEnabled: cmpName = " + cmpName
					+ ",pkgEnableState = " + pkgEnableState);
		}
		if (pkgEnableState == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
				|| pkgEnableState == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
			final int cmpEnableState = pm.getComponentEnabledSetting(cmpName);
			if (LauncherLog.DEBUG) {
				LauncherLog.d(TAG, "isComponentEnabled: cmpEnableState = "
						+ cmpEnableState);
			}
			if (cmpEnableState == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
					|| cmpEnableState == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
				return true;
			}
		}

		return false;
	}
	
	
	
	public static File saveBitmap(Bitmap bitmap, String fileAbslutlyPath) {
		if (bitmap==null || TextUtils.isEmpty(fileAbslutlyPath)) {
		return null;
		}

		        File file = new File(fileAbslutlyPath);
		        File dir = null;
		        FileOutputStream out = null;
		        try {
		        	// 若父路径不存在,先创建父路径
		        	dir = new File(file.getParent());
		        	if (!dir.exists()) {
		        	 dir.mkdirs();
		        	}
		        	
		        	// 若文件不存在,则创建
		        	if (!file.exists()) {
		            	file.createNewFile();
		            }
		            out = new FileOutputStream(file);
		            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
		            return file;
		        } catch (IOException e) {
		        	Log.e("", e.toString());
		        	return null;
		        } finally {
		        	try {
		        	 if(null!=out) {
		        	 out.close();
		        	 }
		} catch (IOException e) {
		Log.e("", e.toString());
		}
		        }
		    }
	
	
	
	
}
