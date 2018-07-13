package com.android.launcher2;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SectorAnimation {

	public SectorWholeArea mSectorWholeArea;
	private SectorPointerAnimation mPointerAnimation;
	private SectorPopAnimation mPopAnimation;
	Context mContext;
	Handler mHandler;

	
	public SectorAnimation(Context context ,Handler handler,SectorWholeArea mSectorWholeArea,int widgetWidth,int widgetHight) {
		super();
		mContext = context;
		mHandler = handler;
		this.mSectorWholeArea = mSectorWholeArea;
		createPointerAnimation();
		createPopAnimation();
		initMiddleIconView(widgetWidth,widgetHight);
		initMiddleTxtView(widgetWidth,widgetHight);
	}
	
	void createPointerAnimation()
	{
		if (null == mPointerAnimation) {
			mPointerAnimation = new SectorPointerAnimation( 0.0f, 0.0f, 
					mSectorWholeArea.mMiddleArea.mPointerAnimaImageView, 
					mSectorWholeArea.mMiddleArea.mPointerActualImageView, 
					mSectorWholeArea.mOuterArea.mIconRelativeLayout0_bg,
					mSectorWholeArea.mOuterArea.mIconRelativeLayout1_bg, 
					mSectorWholeArea.mOuterArea.mIconRelativeLayout2_bg);
		}
	}
	
	void createPopAnimation()
	{
		if (null == mPopAnimation) {
			mPopAnimation = new SectorPopAnimation() ;
		}
	}
	
	public void runUnPopAnimation()
	{
		View mPointerActualImageView = mSectorWholeArea.mMiddleArea.mPointerActualImageView;
		View currentIconRelativeLayout = null;
		
		int partValues = SectorUtils.getCurrentLocation(mPointerActualImageView);;
		switch (partValues) {
		case SectorUtils.PART0:
			currentIconRelativeLayout = mSectorWholeArea.mOuterArea.mIconRelativeLayout0;
			break;
		case SectorUtils.PART1:
			currentIconRelativeLayout = mSectorWholeArea.mOuterArea.mIconRelativeLayout1;
			break;
		case SectorUtils.PART2:
			currentIconRelativeLayout = mSectorWholeArea.mOuterArea.mIconRelativeLayout2;
			break;
		default:
			return;
		}

		mPopAnimation.setPopAnimation(mContext,mSectorWholeArea.mSectorBackGroundArea,
				currentIconRelativeLayout,mSectorWholeArea.mRocketButton,mHandler);
		mPopAnimation.unpopAnimation();	
	}
	
	public void runPopAnimation()
	{
		View mPointerActualImageView = mSectorWholeArea.mMiddleArea.mPointerActualImageView;
		View currentIconRelativeLayout = null;
		
		int partValues = SectorUtils.getCurrentLocation(mPointerActualImageView);;
		switch (partValues) {
		case SectorUtils.PART0:
			currentIconRelativeLayout = mSectorWholeArea.mOuterArea.mIconRelativeLayout0;
			break;
		case SectorUtils.PART1:
			currentIconRelativeLayout = mSectorWholeArea.mOuterArea.mIconRelativeLayout1;
			break;
		case SectorUtils.PART2:
			currentIconRelativeLayout = mSectorWholeArea.mOuterArea.mIconRelativeLayout2;
			break;
		default:
			return;
		}

		mPopAnimation.setPopAnimation(mContext,mSectorWholeArea.mSectorBackGroundArea,
				currentIconRelativeLayout,mSectorWholeArea.mRocketButton,mHandler);
		mPopAnimation.popAnimation();
	}

	public void runPopAppendAnimation()
	{
		View mPointerActualImageView = mSectorWholeArea.mMiddleArea.mPointerActualImageView;
		View currentIconRelativeLayout = null;
		
		int partValues = SectorUtils.getCurrentLocation(mPointerActualImageView);;
		switch (partValues) {
		case SectorUtils.PART0:
			currentIconRelativeLayout = mSectorWholeArea.mOuterArea.mIconRelativeLayout0;
			break;
		case SectorUtils.PART1:
			currentIconRelativeLayout = mSectorWholeArea.mOuterArea.mIconRelativeLayout1;
			break;
		case SectorUtils.PART2:
			currentIconRelativeLayout = mSectorWholeArea.mOuterArea.mIconRelativeLayout2;
			break;
		default:
			return;
		}

		mPopAnimation.setPopAnimation(mContext,mSectorWholeArea.mSectorBackGroundArea,
				currentIconRelativeLayout,mSectorWholeArea.mRocketButton,mHandler);
		mPopAnimation.popAppendmAnimationSet(150);
	}
	
	
	
////////////////////////////////////////////////////////////////////////////	
	public void runPointerMoveAnimation(int rotateType)
	{		
		mPointerAnimation.showAnimationForMove(rotateType);
	}
	
	public boolean runPointerClickAnimation(float evnet_x, float evnet_y,int radius)
	{
		return mPointerAnimation.showAnimationForClick(evnet_x, evnet_y, radius);
	}
	
	private void initMiddleTxtView(int width,int hight){
		
		List<SectorIconInfo> txtInfos = SectorUtils.getCoordinateForText(width);
		View txtPart0 = mSectorWholeArea.mMiddleArea.mMiddleTxtPart0;
		View txtPart1 = mSectorWholeArea.mMiddleArea.mMiddleTxtPart1;
		View txtPart2 = mSectorWholeArea.mMiddleArea.mMiddleTxtPart2;
		List<View> txtParts = new ArrayList<View>();
		txtParts.add(txtPart0);
		txtParts.add(txtPart1);
		txtParts.add(txtPart2);
		
		for (int i = 0; i < txtInfos.size(); i++) {
			TextView textView = (TextView) txtParts.get(i);
			RelativeLayout.LayoutParams txtParams = (RelativeLayout.LayoutParams)textView.getLayoutParams();
			txtParams.setMargins(txtInfos.get(i).coordinates_x, txtInfos.get(i).coordinates_y, width, hight);
			textView.setLayoutParams(txtParams);
		}
	}
	
	public void initMiddleIconView(int width,int hight){
		
		List<SectorIconInfo> iconInfos = SectorUtils.getCoordinateForIcons(width);
		
		for (int n = 0; n < 3; n++) {
			List<View> layer = null;
			View layer_bg = null;
			switch (n) {
			case 0:
				layer = mSectorWholeArea.mOuterArea.outerLayer0_IconViews;
				layer_bg = mSectorWholeArea.mOuterArea.mIconRelativeLayout0_bg_img;
				break;
			case 1:
				layer = mSectorWholeArea.mOuterArea.outerLayer1_IconViews;
				layer_bg = mSectorWholeArea.mOuterArea.mIconRelativeLayout1_bg_img;
				break;
			case 2:
				layer = mSectorWholeArea.mOuterArea.outerLayer2_IconViews;
				layer_bg = mSectorWholeArea.mOuterArea.mIconRelativeLayout2_bg_img;
				break;
			
			default:
				break;
			}			
			if (null == layer) {
				return;
			}
			
			
				for (int i = 0; i < layer.size(); i++) {
					CellLayout iconIV =(CellLayout) layer.get(i);
					
					int x = iconInfos.get(i).coordinates_x;
					int y = iconInfos.get(i).coordinates_y;
					int viewWidth = iconIV.getWidth();
					int viewHight = iconIV.getHeight();
					
					
					RelativeLayout.LayoutParams iconParams = (RelativeLayout.LayoutParams)iconIV.getLayoutParams();
			

					iconParams.setMargins(iconInfos.get(i).coordinates_x,iconInfos.get(i).coordinates_y,SectorView.SYS_SCREEN_WIDTH,SectorView.SYS_SCREEN_HIGHT);
//					iconParams.setMargins(iconInfos.get(i).coordinates_x,iconInfos.get(i).coordinates_y,width,hight);
//					iconParams.setMargins(iconInfos.get(i).coordinates_x,iconInfos.get(i).coordinates_y,x+100,y+100);
					
					SectorTestAPI.PrintCommon("coordinatesTest", i+":("+iconInfos.get(i).coordinates_x+","+iconInfos.get(i).coordinates_y+")");
					iconIV.setLayoutParams(iconParams);
			//		iconIV.setOnClickListener(new IconOnclickListener());
				}
			
		
//			for (int i = 0; i < layer.size(); i++) {
//				ImageView iconIV =(ImageView) layer.get(i);
//				iconIV.setImageResource(R.drawable.cometonlaunch_quick_launch_empty);
//				iconIV.setTag(null);
//				int x = iconInfos.get(i).coordinates_x;
//				int y = iconInfos.get(i).coordinates_y;
//				int viewWidth = iconIV.getWidth();
//				int viewHight = iconIV.getHeight();
//				
//				
//				RelativeLayout.LayoutParams iconParams = (RelativeLayout.LayoutParams)iconIV.getLayoutParams();
//		
//
//				iconParams.setMargins(iconInfos.get(i).coordinates_x,iconInfos.get(i).coordinates_y,SectorView.SYS_SCREEN_WIDTH,SectorView.SYS_SCREEN_HIGHT);
////				iconParams.setMargins(iconInfos.get(i).coordinates_x,iconInfos.get(i).coordinates_y,width,hight);
////				iconParams.setMargins(iconInfos.get(i).coordinates_x,iconInfos.get(i).coordinates_y,x+100,y+100);
//				
//				SectorTestAPI.PrintCommon("coordinatesTest", i+":("+iconInfos.get(i).coordinates_x+","+iconInfos.get(i).coordinates_y+")");
//				iconIV.setLayoutParams(iconParams);
//				iconIV.setOnClickListener(new IconOnclickListener());
//			}
		}

	}
	

	 
	class IconOnclickListener implements OnClickListener {
		int i = 0;

		@Override
		public void onClick(View v) {	
			SectorTestAPI.PrintCommon(null, "view-width:"+v.getWidth()+" view-hight:"+v.getHeight());
			if (i%2 == 0) {
				v.setBackgroundColor(Color.RED);
			}else {
				v.setBackgroundColor(0);
			}
		   i++;
		}
	}

}
