package com.android.launcher2;


import android.graphics.drawable.RotateDrawable;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.android.launcher.R;

public class SectorPointerAnimation {
//��ʼλ��Ϊ��һͼ��ʱ���Ƕȷ���
	public static final float  ROTATE_STEP0 = 0.0f;
	public static final float  ROTATE_STEP1 = 30.0f;
	public static final float  ROTATE_STEP2 = 60.0f;
	public static final float  ROTATE_STEP3 = 90.0f;
	

	
	//��ʾ��һͼ��ΪĬ��ͼ��ʱ�������ã�
//	public static final float  MODE_OFFSET = 0.0f;
//	public static final int POINTER_LOCATION_DEFAULT = R.drawable.cometonlaunch_quick_access_section_indicator0;
//	public static final int LAYER_DISAPLAY_LOCATION_DEFAULT = R.drawable.cometonlaunch_quick_access_section_indicator0;
//	public static final int LAYER_0_DISPLAY_MODE_DEFAULT = View.VISIBLE;
//	public static final int LAYER_1_DISPLAY_MODE_DEFAULT = View.GONE;
//	public static final int LAYER_2_DISPLAY_MODE_DEFAULT = View.GONE;
	
	
	
	//��ʾ�м�ͼ��ΪĬ��ͼ��ʱ����Ҫ��ԭ4�߼����ϼӴ�ƫ�����������ã�
	public static final float  MODE_OFFSET = 30.0f;
	public static final int POINTER_LOCATION_DEFAULT = R.drawable.cometonlaunch_quick_access_section_indicator1;
	public static final int LAYER_DISAPLAY_LOCATION_DEFAULT = R.drawable.cometonlaunch_quick_access_section_indicator1;
	public static final int LAYER_0_DISPLAY_MODE_DEFAULT = View.GONE;
	public static final int LAYER_1_DISPLAY_MODE_DEFAULT = View.VISIBLE;
	public static final int LAYER_2_DISPLAY_MODE_DEFAULT = View.GONE;

	public static final float  ROTATE_360 = 360.0f;
	//unit :ms
	public static final int ANIMATION_DURATION = 400;
	public static final int COUNTER_CLOCKWISE_ROTATE = 1;
	public static final int CLOCKWISE_ROTATE = 2;
	public static final int ERROR_DRAWABLE_ID = 0;
	
	public boolean MoveAminationRunningFlag = false;
	
	
	AnimationSet mAnimationSet;
	float mStartAngle;
	float mEndAngle;
	
	View mPointerAnimationImageView;
	View mPointerActualImageView;
	
	View mIconLayout0;
	View mIconLayout1;
	View mIconLayout2;
	View mIconLayoutStart;
	View mIconLayoutEnd;
	
	PointerAnimationListener pointerAnimationListener;
	IconAnimationListener iconAnimationListener;
	
	
	public SectorPointerAnimation(float startAngle,float endAngle,View animationImageView,View actualImageView
			,View iconLayout0,View iconLayout1,View iconLayout2)
	{
		mStartAngle = startAngle;
		mEndAngle = endAngle;
		mPointerAnimationImageView = animationImageView;
		mPointerActualImageView = actualImageView;
		mIconLayout0 = iconLayout0;
		mIconLayout1 = iconLayout1;
		mIconLayout2 = iconLayout2;
		pointerAnimationListener = new PointerAnimationListener(); 
		iconAnimationListener = new IconAnimationListener();
	}
	
	
	public AnimationSet getmAnimationSet(float startAngle,float endAngle,int durationTime) {
		mAnimationSet = new AnimationSet(true);
		
		RotateAnimation rotateAnimation = null;
		switch (SectorView.WidgetCurrentLocation) {
		case SectorWidgetConfig.DIRECTION_LEFT_TOP:
			 rotateAnimation = new RotateAnimation(startAngle, endAngle, 
					 Animation.RELATIVE_TO_SELF, 0f,Animation.RELATIVE_TO_SELF, 0f);
			break;
		case SectorWidgetConfig.DIRECTION_LEFT_BOTTOM:
			 rotateAnimation = new RotateAnimation(startAngle, endAngle, 
					 Animation.RELATIVE_TO_SELF, 0f,Animation.RELATIVE_TO_SELF, 1f);
			break;
		case SectorWidgetConfig.DIRECTION_RIGHT_BOTTOM:
			 rotateAnimation = new RotateAnimation(startAngle, endAngle, 
					 Animation.RELATIVE_TO_SELF, 1f,Animation.RELATIVE_TO_SELF, 1f);
			break;			
		default:
			break;
		}
///>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>		
//		RotateAnimation rotateAnimation = 
//			new RotateAnimation(startAngle, endAngle, Animation.RELATIVE_TO_SELF, 0f, 
//										Animation.RELATIVE_TO_SELF, 0f);
//<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
		
	
		rotateAnimation.setDuration(durationTime);
		mAnimationSet.addAnimation(rotateAnimation);
		return mAnimationSet;
	}
	
	public void loadPointerAnimation(float startAngle,float endAngle) {
		
		AnimationSet pointerAnimationSet = getmAnimationSet(startAngle,endAngle,ANIMATION_DURATION);
//		pointerAnimationSet.setAnimationListener(new PointerAnimationListener());
		pointerAnimationSet.setAnimationListener(pointerAnimationListener);
		
		mPointerAnimationImageView.setVisibility(View.VISIBLE);
		mPointerActualImageView.setVisibility(View.GONE);
		mPointerAnimationImageView.startAnimation(pointerAnimationSet);	
	}
	
	public void loadIconAnimation(int rotateType,View iconLayoutStart,View iconLayoutEnd) {
		
		AnimationSet iconAnimationSetStart;
		AnimationSet iconAnimationSetEnd;
		if ( CLOCKWISE_ROTATE == rotateType) {
			
			iconAnimationSetStart = getmAnimationSet(0.0f,90.0f,ANIMATION_DURATION);
			iconAnimationSetEnd = getmAnimationSet(-90.0f,0.0f,ANIMATION_DURATION);

		}else if ( COUNTER_CLOCKWISE_ROTATE == rotateType) {
			
			iconAnimationSetStart = getmAnimationSet(0.0f,-90.0f,ANIMATION_DURATION);
			iconAnimationSetEnd = getmAnimationSet(90.0f,0.0f,ANIMATION_DURATION);

		}else {
			return;
		}
//		iconAnimationSetStart.setAnimationListener(new IconAnimationListener());
//		iconAnimationSetEnd.setAnimationListener(new IconAnimationListener());
		iconAnimationSetStart.setAnimationListener(iconAnimationListener);
		iconAnimationSetEnd.setAnimationListener(iconAnimationListener);
		
		
		iconLayoutStart.startAnimation(iconAnimationSetStart);
		iconLayoutEnd.startAnimation(iconAnimationSetEnd);
		iconLayoutEnd.setVisibility(View.VISIBLE);
	}
	
	
	public void showAnimationForMove(int  rotateType)
	{
		if (MoveAminationRunningFlag) {
			return;
		}
		float startAngle = SectorPointerAnimation.ROTATE_STEP0;
		float endAngle = SectorPointerAnimation.ROTATE_STEP1;
		if (rotateType == CLOCKWISE_ROTATE) {

			if (((Integer)(mPointerActualImageView.getTag())).intValue() 
					   == SectorWidgetConfig.DRAWABLE_POINTER_0PART_ID){
				   
				   mPointerActualImageView.setTag(Integer.valueOf(SectorWidgetConfig.DRAWABLE_POINTER_1PART_ID));
				   startAngle = SectorPointerAnimation.ROTATE_STEP0 - MODE_OFFSET;
				   endAngle =  SectorPointerAnimation.ROTATE_STEP1 - MODE_OFFSET;
				   
				   mIconLayoutStart = mIconLayout0;
				   mIconLayoutEnd = mIconLayout1;
				   
			}else if (((Integer)(mPointerActualImageView.getTag())).intValue() 
					   == SectorWidgetConfig.DRAWABLE_POINTER_1PART_ID){
				
				   mPointerActualImageView.setTag(Integer.valueOf(SectorWidgetConfig.DRAWABLE_POINTER_2PART_ID));
				   startAngle = SectorPointerAnimation.ROTATE_STEP1 - MODE_OFFSET;
				   endAngle =  SectorPointerAnimation.ROTATE_STEP2 - MODE_OFFSET;
				   
				   mIconLayoutStart = mIconLayout1;
				   mIconLayoutEnd = mIconLayout2;
				   
			}else if (((Integer)(mPointerActualImageView.getTag())).intValue() 
					   == SectorWidgetConfig.DRAWABLE_POINTER_2PART_ID){
				
				   mPointerActualImageView.setTag(Integer.valueOf(SectorWidgetConfig.DRAWABLE_POINTER_0PART_ID));
				   startAngle = SectorPointerAnimation.ROTATE_STEP2 - MODE_OFFSET;
				   endAngle =  SectorPointerAnimation.ROTATE_STEP0+ROTATE_360 - MODE_OFFSET;
				   
				   mIconLayoutStart = mIconLayout2;
				   mIconLayoutEnd = mIconLayout0;
			}
			   

		}else if (rotateType == COUNTER_CLOCKWISE_ROTATE) {
			
			if (((Integer)(mPointerActualImageView.getTag())).intValue() 
					   == SectorWidgetConfig.DRAWABLE_POINTER_0PART_ID){
				   
				   mPointerActualImageView.setTag(Integer.valueOf(SectorWidgetConfig.DRAWABLE_POINTER_2PART_ID));
				   startAngle = SectorPointerAnimation.ROTATE_STEP0 - MODE_OFFSET;
				   endAngle =  SectorPointerAnimation.ROTATE_STEP2-ROTATE_360 - MODE_OFFSET;
				   
				   mIconLayoutStart = mIconLayout0;
				   mIconLayoutEnd = mIconLayout2;
				   
			}else if (((Integer)(mPointerActualImageView.getTag())).intValue() 
					   == SectorWidgetConfig.DRAWABLE_POINTER_1PART_ID){
				
				   mPointerActualImageView.setTag(Integer.valueOf(SectorWidgetConfig.DRAWABLE_POINTER_0PART_ID));
				   startAngle = SectorPointerAnimation.ROTATE_STEP1  - MODE_OFFSET;
				   endAngle =  SectorPointerAnimation.ROTATE_STEP0  - MODE_OFFSET;
				   
				   mIconLayoutStart = mIconLayout1;
				   mIconLayoutEnd = mIconLayout0;
				   
			}else if (((Integer)(mPointerActualImageView.getTag())).intValue() 
					   == SectorWidgetConfig.DRAWABLE_POINTER_2PART_ID){
				
				   mPointerActualImageView.setTag(Integer.valueOf(SectorWidgetConfig.DRAWABLE_POINTER_1PART_ID));
				   startAngle = SectorPointerAnimation.ROTATE_STEP2  - MODE_OFFSET;
				   endAngle =  SectorPointerAnimation.ROTATE_STEP1  - MODE_OFFSET;
				   
				   mIconLayoutStart = mIconLayout2;
				   mIconLayoutEnd = mIconLayout1;
			}
		}
		loadPointerAnimation(startAngle,endAngle);
		loadIconAnimation(rotateType,mIconLayoutStart,mIconLayoutEnd);
	}
	
	
	public boolean showAnimationForClick(float evnet_x, float evnet_y,int radius )
	{
		
		if (MoveAminationRunningFlag) {
			return false;
		}
		float startAngle = SectorPointerAnimation.ROTATE_STEP0;
		float endAngle = SectorPointerAnimation.ROTATE_STEP1;
		int  rotateType = CLOCKWISE_ROTATE;
		int targetDrawableID = ERROR_DRAWABLE_ID;
		int currentDrawableID = ((Integer)(mPointerActualImageView.getTag())).intValue();
		   
		if (!SectorUtils.clickIsValib(evnet_x, evnet_y, radius)) {
			return false;
		}
		  
//		double degress = Math.toDegrees(Math.atan(evnet_y/evnet_x));
//	  
//		if (degress <= PointerAnimation.ROTATE_STEP1) {
//			 targetDrawableID = WidgetConfig.DRAWABLE_POINTER_0PART_ID;
//		}else if (degress <= PointerAnimation.ROTATE_STEP2) {
//			 targetDrawableID = WidgetConfig.DRAWABLE_POINTER_1PART_ID;
//		}else if (degress <= PointerAnimation.ROTATE_STEP3) {
//			 targetDrawableID = WidgetConfig.DRAWABLE_POINTER_2PART_ID;
//		}  
		
		targetDrawableID = getTargetDrawableByDegress( evnet_x,evnet_y,radius);


		if (ERROR_DRAWABLE_ID == targetDrawableID||currentDrawableID == targetDrawableID) 	return false;
	   
	   if (SectorWidgetConfig.DRAWABLE_POINTER_0PART_ID == currentDrawableID) {
			   if (SectorWidgetConfig.DRAWABLE_POINTER_1PART_ID == targetDrawableID) {
				   startAngle = SectorPointerAnimation.ROTATE_STEP0 - MODE_OFFSET;
				   endAngle =  SectorPointerAnimation.ROTATE_STEP1 - MODE_OFFSET;
				   
				   mIconLayoutStart = mIconLayout0;
				   mIconLayoutEnd = mIconLayout1;
				   rotateType = CLOCKWISE_ROTATE;
				   
			   }else if (SectorWidgetConfig.DRAWABLE_POINTER_2PART_ID == targetDrawableID) {
				   startAngle = SectorPointerAnimation.ROTATE_STEP0 - MODE_OFFSET;
				   endAngle =  SectorPointerAnimation.ROTATE_STEP2 - MODE_OFFSET;
				   
				   mIconLayoutStart = mIconLayout0;
				   mIconLayoutEnd = mIconLayout2;
				   rotateType = CLOCKWISE_ROTATE;
			   }else {
				   return false;
			   }
		}else if (SectorWidgetConfig.DRAWABLE_POINTER_1PART_ID == currentDrawableID) {
			
			   if (SectorWidgetConfig.DRAWABLE_POINTER_0PART_ID == targetDrawableID) {
				   startAngle = SectorPointerAnimation.ROTATE_STEP1 - MODE_OFFSET;
				   endAngle =  SectorPointerAnimation.ROTATE_STEP0 - MODE_OFFSET;
				   
				   mIconLayoutStart = mIconLayout1;
				   mIconLayoutEnd = mIconLayout0;
				   rotateType = COUNTER_CLOCKWISE_ROTATE;
				   
			   }else if (SectorWidgetConfig.DRAWABLE_POINTER_2PART_ID == targetDrawableID) {
				   startAngle = SectorPointerAnimation.ROTATE_STEP1- MODE_OFFSET;
				   endAngle =  SectorPointerAnimation.ROTATE_STEP2- MODE_OFFSET;
				   
				   mIconLayoutStart = mIconLayout1;
				   mIconLayoutEnd = mIconLayout2;
				   rotateType = CLOCKWISE_ROTATE;
			   }else {
				   return false;
			   }
		}else if (SectorWidgetConfig.DRAWABLE_POINTER_2PART_ID == currentDrawableID) {
			
			   if (SectorWidgetConfig.DRAWABLE_POINTER_0PART_ID == targetDrawableID) {
				   startAngle = SectorPointerAnimation.ROTATE_STEP2- MODE_OFFSET;
				   endAngle =  SectorPointerAnimation.ROTATE_STEP0- MODE_OFFSET;
				   
				   mIconLayoutStart = mIconLayout2;
				   mIconLayoutEnd = mIconLayout0;
				   rotateType = COUNTER_CLOCKWISE_ROTATE;
				   
			   }else if (SectorWidgetConfig.DRAWABLE_POINTER_1PART_ID == targetDrawableID) {
				   startAngle = SectorPointerAnimation.ROTATE_STEP2 - MODE_OFFSET;
				   endAngle =  SectorPointerAnimation.ROTATE_STEP1 - MODE_OFFSET;
				   
				   mIconLayoutStart = mIconLayout2;
				   mIconLayoutEnd = mIconLayout1;
				   rotateType = COUNTER_CLOCKWISE_ROTATE;
				   
			   }else {
				   return false;
			   }
		}else {
			return false;
		}
		mPointerActualImageView.setTag(targetDrawableID);
		loadPointerAnimation(startAngle,endAngle);
		loadIconAnimation(rotateType,mIconLayoutStart,mIconLayoutEnd);
		return true;
	}
	
	
//	public boolean clickIsValib(float x,float y,int  radius)
//	{
//		TestAPI.PrintCommon(null, "��Ļ��Ϊ��"+SectorView.SYS_SCREEN_HIGHT);
//		TestAPI.PrintCommon(null, "���£����X��꣺"+x);
//		TestAPI.PrintCommon(null, "���£����Y��꣺"+y);
//		if (WidgetConfig.DIRECTION_LEFT_TOP == SectorView.WidgetCurrentLocation) {
//			
//			  if (Math.pow(x, 2)+ Math.pow(y, 2) > Math.pow(radius,2)) {
//				  return false;
//			};
//			  if (Math.pow(x, 2)+ Math.pow(y, 2) < Math.pow(radius/2,2)){ 
//				  return false;
//			};
//		}else if (WidgetConfig.DIRECTION_LEFT_BOTTOM == SectorView.WidgetCurrentLocation) {
//			
//			  if (Math.pow(x, 2)+ Math.pow(radius -y, 2) > Math.pow(radius,2)) {
//				  return false;
//			};
//			  if (Math.pow(x, 2)+ Math.pow(radius -y, 2) < Math.pow(radius/2,2)){ 
//				  return false;
//			};
//		}else if (WidgetConfig.DIRECTION_RIGHT_BOTTOM == SectorView.WidgetCurrentLocation) {
//			
//			  if (Math.pow(radius - x, 2)+ Math.pow(radius -y, 2) > Math.pow(radius,2)) {
//				  return false;
//			};
//			  if (Math.pow(radius - x, 2)+ Math.pow(radius -y, 2) < Math.pow(radius/2,2)){ 
//				  return false;
//			};
//		}
//		return true;
//	}
	
	 int getTargetDrawableByDegress(float evnet_x,float evnet_y,int radius){
		double degress = 0;
		  int targetDrawableID = ERROR_DRAWABLE_ID;
		//�������� �Ƕ� 
		if (SectorWidgetConfig.DIRECTION_LEFT_TOP == SectorView.WidgetCurrentLocation) {
				degress = Math.toDegrees(Math.atan(evnet_y/evnet_x));
		}else if (SectorWidgetConfig.DIRECTION_LEFT_BOTTOM == SectorView.WidgetCurrentLocation) {
			   degress = Math.toDegrees(Math.atan(evnet_x/(radius - evnet_y)));
		}else if (SectorWidgetConfig.DIRECTION_RIGHT_BOTTOM == SectorView.WidgetCurrentLocation){
			 degress = Math.toDegrees(Math.atan((radius -evnet_y)/(radius -evnet_x)));
		}
		// ͨ��Ƕȣ������Ӧ��drawable��Դ�� 
		if (degress <= SectorPointerAnimation.ROTATE_STEP1) {
			 targetDrawableID = SectorWidgetConfig.DRAWABLE_POINTER_0PART_ID;
		}else if (degress <= SectorPointerAnimation.ROTATE_STEP2) {
			 targetDrawableID = SectorWidgetConfig.DRAWABLE_POINTER_1PART_ID;
		}else if (degress <= SectorPointerAnimation.ROTATE_STEP3) {
			 targetDrawableID = SectorWidgetConfig.DRAWABLE_POINTER_2PART_ID;
		} 
		return targetDrawableID;
	}
	
	
	
	
	
	
	class PointerAnimationListener implements AnimationListener {
		@Override
		public void onAnimationStart(Animation animation) {
			MoveAminationRunningFlag = true;
			
		}
		
		@Override
		public void onAnimationRepeat(Animation animation) {
		}
		
		@Override
		public void onAnimationEnd(Animation animation) {
			
			((ImageView)mPointerActualImageView).setImageResource(((Integer)(mPointerActualImageView.getTag())).intValue());
			RotateDrawable rotateDrawable =(RotateDrawable)((ImageView)mPointerActualImageView).getDrawable();  
			rotateDrawable.setLevel(SectorView.WidgetCurrentLocation);
			
			mPointerAnimationImageView.setVisibility(View.GONE);
			mPointerActualImageView.setVisibility(View.VISIBLE);
			MoveAminationRunningFlag = false;
		}
	}
	
	
	class IconAnimationListener implements AnimationListener {
		@Override
		public void onAnimationStart(Animation animation) {
		}
		
		@Override
		public void onAnimationRepeat(Animation animation) {
		}
		
		@Override
		public void onAnimationEnd(Animation animation) {
			mIconLayoutStart.setVisibility(View.GONE);
			mIconLayoutEnd.setVisibility(View.VISIBLE);
		}
	}
	
	
	
	
}
