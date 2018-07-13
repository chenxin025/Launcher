package com.android.launcher2;


import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;

public class SectorPopAnimation {
	//unit :ms
	public static final int ANIMATION_DURATION = 300;
	
	AnimationSet mAnimationSet;
	View mSectorLayout;
	View mIconLayout;
	View mRocketButton;
	Handler mHandler;
	Context mContext;
	
	public boolean AminationRunningFlag = false;

	
	public void setPopAnimation(Context context,View sectorLayout,View iconLayout,View rocketButton,Handler handler)
	{
		mContext = context;
		mSectorLayout = sectorLayout;
		mIconLayout = iconLayout;
		mRocketButton = rocketButton;
		mHandler = handler;
	}
	
	
	public AnimationSet getPopAnimationSet(int durationTime) {
		mAnimationSet = new AnimationSet(true);
		Animation mScaleAnimation = null;
		
		if (SectorWidgetConfig.DIRECTION_LEFT_TOP == SectorView.WidgetCurrentLocation) {
		 mScaleAnimation = new ScaleAnimation(0.2f, 1.0f, 0.2f,
	                1.0f,// �����Ļ��0.0��1.0�Ĵ�С//���
	                Animation.RELATIVE_TO_SELF, 0.0f,
	                Animation.RELATIVE_TO_SELF, 0.0f);

		}else if (SectorWidgetConfig.DIRECTION_LEFT_BOTTOM == SectorView.WidgetCurrentLocation) {
			 mScaleAnimation = new ScaleAnimation(0.2f, 1.0f, 0.2f,
		                1.0f,// �����Ļ��0.0��1.0�Ĵ�С//���
		                Animation.RELATIVE_TO_SELF, 0.0f,
		                Animation.RELATIVE_TO_SELF, 1.0f);
		}
		else if (SectorWidgetConfig.DIRECTION_RIGHT_BOTTOM == SectorView.WidgetCurrentLocation) {
			
			 mScaleAnimation = new ScaleAnimation(0.2f, 1.0f, 0.2f,
		                1.0f,// �����Ļ��0.0��1.0�Ĵ�С//���
		                Animation.RELATIVE_TO_SELF, 1.0f,
		                Animation.RELATIVE_TO_SELF, 1.0f);
		}else {
			 mScaleAnimation = new ScaleAnimation(0.2f, 1.0f, 0.2f,
		                1.0f,// �����Ļ��0.0��1.0�Ĵ�С//���
		                Animation.RELATIVE_TO_SELF, 0.0f,
		                Animation.RELATIVE_TO_SELF, 0.0f);
		}
        mScaleAnimation.setDuration(durationTime);
        mScaleAnimation.setFillAfter(true);
        mAnimationSet.addAnimation(mScaleAnimation);
		return mAnimationSet;
	}
	
	public void popAnimation() {
		if (AminationRunningFlag) {
			return;
		}
		mSectorLayout.setVisibility(View.VISIBLE);
//		mRocketButton.setVisibility(View.GONE);
		
		AnimationSet animationSet = getPopAnimationSet(ANIMATION_DURATION);
		animationSet.setAnimationListener(new PopAnimationListener());
		mSectorLayout.startAnimation(animationSet);	
	}
	
	public void popAppendmAnimationSet(int durationTime) {
		mAnimationSet = new AnimationSet(true);
		Animation mScaleAnimation = null;

		
		if (SectorWidgetConfig.DIRECTION_LEFT_TOP == SectorView.WidgetCurrentLocation) {
			mScaleAnimation = new ScaleAnimation(1.2f, 0.9f, 1.2f,
	                0.9f,// �����Ļ��0.0��1.0�Ĵ�С//���
	                Animation.RELATIVE_TO_SELF, 0.0f,
	                Animation.RELATIVE_TO_SELF, 0.0f);

			}else if (SectorWidgetConfig.DIRECTION_LEFT_BOTTOM == SectorView.WidgetCurrentLocation) {
				mScaleAnimation = new ScaleAnimation(1.2f, 0.9f, 1.2f,
		                0.9f,// �����Ļ��0.0��1.0�Ĵ�С//���
		                Animation.RELATIVE_TO_SELF, 0.0f,
		                Animation.RELATIVE_TO_SELF, 1.0f);
			}
			else if (SectorWidgetConfig.DIRECTION_RIGHT_BOTTOM == SectorView.WidgetCurrentLocation) {
				
				mScaleAnimation = new ScaleAnimation(1.2f, 0.9f, 1.2f,
		                0.9f,// �����Ļ��0.0��1.0�Ĵ�С//���
		                Animation.RELATIVE_TO_SELF, 1.0f,
		                Animation.RELATIVE_TO_SELF, 1.0f);
			}else {
				mScaleAnimation = new ScaleAnimation(1.2f, 0.9f, 1.2f,
		                0.9f,// �����Ļ��0.0��1.0�Ĵ�С//���
		                Animation.RELATIVE_TO_SELF, 0.0f,
		                Animation.RELATIVE_TO_SELF, 0.0f);
			}
		
        mScaleAnimation.setDuration(durationTime);
        mScaleAnimation.setFillAfter(true);
        mScaleAnimation.setAnimationListener(new PopAppendAnimationListener());
        mAnimationSet.addAnimation(mScaleAnimation);

        mIconLayout.startAnimation(mAnimationSet);
	}
	
	
	class PopAnimationListener implements AnimationListener {
		@Override
		public void onAnimationStart(Animation animation) {
			AminationRunningFlag = true;
		}
		
		@Override
		public void onAnimationRepeat(Animation animation) {
		}
		
		@Override
		public void onAnimationEnd(Animation animation) {
			
			popAppendmAnimationSet(ANIMATION_DURATION/2);
			AminationRunningFlag = false;
			
			if(SectorView.mDragFalg)
			{
			    ((Launcher) mContext).getGaussBitmapByThread();
			}
			
			SectorView.mDragFalg = false;
		}
	}
	
	class PopAppendAnimationListener implements AnimationListener {
		@Override
		public void onAnimationStart(Animation animation) {
			AminationRunningFlag = true;
		}
		
		@Override
		public void onAnimationRepeat(Animation animation) {
		}
		
		@Override
		public void onAnimationEnd(Animation animation) {
			AminationRunningFlag = false;
		}
	}
	
/////////////////////////////////////////////////////////////////////////////////////	
	
	public AnimationSet getUnpopAnimationSet(int durationTime) {
		mAnimationSet = new AnimationSet(true);

		Animation mScaleAnimation = null;
		
		if (SectorWidgetConfig.DIRECTION_LEFT_TOP == SectorView.WidgetCurrentLocation) {
			mScaleAnimation = new ScaleAnimation(1.0f, 0.2f, 1.0f,
	                0.2f,// �����Ļ��0.0��1.0�Ĵ�С//���
	                Animation.RELATIVE_TO_SELF, 0.0f,
	                Animation.RELATIVE_TO_SELF, 0.0f);

		}else if (SectorWidgetConfig.DIRECTION_LEFT_BOTTOM == SectorView.WidgetCurrentLocation) {
			mScaleAnimation = new ScaleAnimation(1.0f, 0.2f, 1.0f,
	                0.2f,// �����Ļ��0.0��1.0�Ĵ�С//���
	                Animation.RELATIVE_TO_SELF, 0.0f,
	                Animation.RELATIVE_TO_SELF, 1.0f);
		}
		else if (SectorWidgetConfig.DIRECTION_RIGHT_BOTTOM == SectorView.WidgetCurrentLocation) {
			mScaleAnimation = new ScaleAnimation(1.0f, 0.2f, 1.0f,
	                0.2f,// �����Ļ��0.0��1.0�Ĵ�С//���
	                Animation.RELATIVE_TO_SELF, 1.0f,
	                Animation.RELATIVE_TO_SELF, 1.0f);

		}else {
			mScaleAnimation = new ScaleAnimation(1.0f, 0.2f, 1.0f,
	                0.2f,// �����Ļ��0.0��1.0�Ĵ�С//���
	                Animation.RELATIVE_TO_SELF, 0.0f,
	                Animation.RELATIVE_TO_SELF, 0.0f);
		}
        mScaleAnimation.setDuration(durationTime);
        mScaleAnimation.setFillAfter(true);
        mAnimationSet.addAnimation(mScaleAnimation);
		return mAnimationSet;
	}
	
	
	
	public void unpopAnimation() {
		if (AminationRunningFlag) {
			return;
		}
		mAnimationSet = new AnimationSet(true);

		Animation mScaleAnimation = null;
		if (SectorWidgetConfig.DIRECTION_LEFT_TOP == SectorView.WidgetCurrentLocation) {
			mScaleAnimation = new ScaleAnimation(1.2f, 1.0f, 1.2f,
	                1.0f,// �����Ļ��0.0��1.0�Ĵ�С//���
	                Animation.RELATIVE_TO_SELF, 0.0f,
	                Animation.RELATIVE_TO_SELF, 0.0f);

		}else if (SectorWidgetConfig.DIRECTION_LEFT_BOTTOM == SectorView.WidgetCurrentLocation) {
			mScaleAnimation = new ScaleAnimation(1.2f, 1.0f, 1.2f,
	                1.0f,// �����Ļ��0.0��1.0�Ĵ�С//���
	                Animation.RELATIVE_TO_SELF, 0.0f,
	                Animation.RELATIVE_TO_SELF, 1.0f);
		}
		else if (SectorWidgetConfig.DIRECTION_RIGHT_BOTTOM == SectorView.WidgetCurrentLocation) {
			mScaleAnimation = new ScaleAnimation(1.2f, 1.0f, 1.2f,
	                1.0f,// �����Ļ��0.0��1.0�Ĵ�С//���
	                Animation.RELATIVE_TO_SELF, 1.0f,
	                Animation.RELATIVE_TO_SELF, 1.0f);

		}else {
			mScaleAnimation = new ScaleAnimation(1.2f, 1.0f, 1.2f,
	                1.0f,// �����Ļ��0.0��1.0�Ĵ�С//���
	                Animation.RELATIVE_TO_SELF, 0.0f,
	                Animation.RELATIVE_TO_SELF, 0.0f);
		}
		
        mScaleAnimation.setDuration(ANIMATION_DURATION/2);
        mScaleAnimation.setFillAfter(true);
        mAnimationSet.addAnimation(mScaleAnimation);
        mAnimationSet.setAnimationListener(new UnpopAnimationListener());
        mIconLayout.startAnimation(mAnimationSet);
        
	
	}
	
	public void unpopAppendmAnimationSet(int durationTime) {
    	AnimationSet animationSet = getUnpopAnimationSet(durationTime);
        mAnimationSet.setAnimationListener(new UnpopFinishedAnimationListener());
		mSectorLayout.startAnimation(animationSet);	
	}
	
	
	class UnpopAnimationListener implements AnimationListener {
		@Override
		public void onAnimationStart(Animation animation) {
			AminationRunningFlag = true;
		}
		
		@Override
		public void onAnimationRepeat(Animation animation) {
		}
		
		@Override
		public void onAnimationEnd(Animation animation) {
			
			unpopAppendmAnimationSet(ANIMATION_DURATION);
			AminationRunningFlag = false;
		}
	}
	
	class UnpopFinishedAnimationListener implements AnimationListener {
		@Override
		public void onAnimationStart(Animation animation) {
			AminationRunningFlag = true;
		}
		
		@Override
		public void onAnimationRepeat(Animation animation) {
		}
		
		@Override
		public void onAnimationEnd(Animation animation) {
			
			mSectorLayout.setVisibility(View.GONE);
//			mRocketButton.setVisibility(View.VISIBLE);
			AminationRunningFlag = false;
			Message msg = Message.obtain();
			msg.what = SectorView.ANIMATION_UNPOP_FINISH;
			mHandler.sendMessage(msg);
			
		}
	}
	
	
}
