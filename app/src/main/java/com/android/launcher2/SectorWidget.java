package com.android.launcher2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.RotateDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.launcher.R;

import java.util.ArrayList;
import java.util.List;

public class SectorWidget extends Activity
{
	private static final String LOG_TAG = "SectorWidget";
	public static final String FIRST_Step = "1";
	
	ImageView popImageView;
	
    ImageView centerImageView;
    RelativeLayout centerLayout;
    
    ImageView middleFrameworkImageView,pointerAnimaImageView,pointerActualImageView;
    TextView middle_Part0_TextView,middle_Part1_TextView,middle_Part2_TextView;

    RelativeLayout outerRelativeLayout;
    RelativeLayout iconRelativeLayout0;
    RelativeLayout iconRelativeLayout1;
    RelativeLayout iconRelativeLayout2;
    RelativeLayout iconRelativeLayout0_bg;
    RelativeLayout iconRelativeLayout1_bg;
    RelativeLayout iconRelativeLayout2_bg;
    ImageView  iconlayer0_bg_img;
    ImageView  iconlayer1_bg_img;
    ImageView  iconlayer2_bg_img;
	List<View> layer0List;
	List<View> layer1List;
	List<View> layer2List;
	RelativeLayout SectorareSize;
	boolean hasMeasured = false;
    
    RelativeLayout rootLayout;
    RelativeLayout sectorRelativeLayout;
    RelativeLayout middleRelativeLayout;
    
    SectorAnimation mSectorAnimation;
    SectorWholeArea mWholeArea;
    
    public static int SYS_SCREEN_WIDTH;
    public static int SYS_SCREEN_HIGHT;   
    
    public static int SYS_SECTOR_WIDTH;
    public static int SYS_SECTOR_HIGHT;   
    
    public static final int  ANIMATION_UNPOP_FINISH = 1;
    
    public static boolean CURRENT_ACTIVITY_EXIST = false;
    
    
    Handler unpopHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ANIMATION_UNPOP_FINISH :				
				SectorWidget.this.finish();
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}	
    };

    public final static int MSG_SECTORSIZE = 1;
	Handler initViewHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_SECTORSIZE:
	             loadData();
	             mSectorAnimation.runPopAnimation();
				break;

			default:
				break;
			}
		}
    };

    
    
   public static int WidgetCurrentLocation = SectorWidgetConfig.DIRECTION_LEFT_BOTTOM;
//    public static int WidgetCurrentLocation = WidgetConfig.DIRECTION_LEFT_TOP; 
// public static int WidgetCurrentLocation = WidgetConfig.DIRECTION_RIGHT_BOTTOM; 
    
	GestureDetector flingDetector = new GestureDetector(new WidgetGestureDetector()); 
	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); Log.i(LOG_TAG, "onCreate()");
		setContentView(R.layout.sectorview);
		CURRENT_ACTIVITY_EXIST = true;
		
		try {
			Intent intent = getIntent();
			Bundle bundle = intent.getExtras();
			WidgetCurrentLocation = bundle.getInt("direction", SectorWidgetConfig.DIRECTION_LEFT_TOP);
		} catch (Exception e) {
			WidgetCurrentLocation = SectorWidgetConfig.DIRECTION_RIGHT_BOTTOM;
		}

		initScreenData();
		getSectorAreaSize();
		createView();
		InitView(WidgetCurrentLocation);
			
	}
	
	@Override
	protected void onDestroy() {
		CURRENT_ACTIVITY_EXIST = false;
		super.onDestroy();
	}





	private void createView(){
		//���ڲ���������
		centerImageView = (ImageView) findViewById(R.id.lancher_center_img);				
		centerLayout = (RelativeLayout) findViewById(R.id.lancher_center_area);		

		//�������
		outerRelativeLayout = (RelativeLayout) findViewById(R.id.lancher_outer_area);
		iconRelativeLayout0 = (RelativeLayout) findViewById(R.id.lancher_outer_layer0);
		iconRelativeLayout1 = (RelativeLayout) findViewById(R.id.lancher_outer_layer1);
		iconRelativeLayout2 = (RelativeLayout) findViewById(R.id.lancher_outer_layer2);
		iconRelativeLayout0_bg = (RelativeLayout) findViewById(R.id.lancher_outer_layer0_bg);
		iconRelativeLayout0_bg.setVisibility(View.VISIBLE);
		iconRelativeLayout1_bg = (RelativeLayout) findViewById(R.id.lancher_outer_layer1_bg);
		iconRelativeLayout2_bg = (RelativeLayout) findViewById(R.id.lancher_outer_layer2_bg);
		iconlayer0_bg_img = (ImageView) findViewById(R.id.lancher_outer_layer0_bg_img);
		iconlayer1_bg_img = (ImageView) findViewById(R.id.lancher_outer_layer1_bg_img);
		iconlayer2_bg_img = (ImageView) findViewById(R.id.lancher_outer_layer2_bg_img);
		
		//�м�����
		middleFrameworkImageView = (ImageView) findViewById(R.id.lancher_mid_framework_img);
//		middleFrameworkImageView.setImageResource(R.drawable.cometonlaunch_quick_access_section_indicator0);
//		middleFrameworkImageView.setOnTouchListener(new RotateButtonListener());
		pointerActualImageView = (ImageView) findViewById(R.id.lancher_pointer_Actual_img);
//		pointerActualImageView.setImageResource(R.drawable.cometonlaunch_quick_access_section_indicator0);
//		pointerActualImageView.setTag(Integer.valueOf(R.drawable.cometonlaunch_quick_access_section_indicator0));
		pointerAnimaImageView = (ImageView) findViewById(R.id.lancher_pointer_Anima_img);
		pointerAnimaImageView.setVisibility(View.GONE);
		middleRelativeLayout = (RelativeLayout) findViewById(R.id.lancher_middle_area);
		middle_Part0_TextView = (TextView) findViewById(R.id.lancher_pointer_txt_part0);
		middle_Part1_TextView = (TextView) findViewById(R.id.lancher_pointer_txt_part1);
		middle_Part2_TextView = (TextView) findViewById(R.id.lancher_pointer_txt_part2);

		//ȫ�ְ������ڲ����м䣬�ⲿ������
		sectorRelativeLayout = (RelativeLayout) findViewById(R.id.lancher_sector_area);
		sectorRelativeLayout.setVisibility(View.GONE);
	
	
		//��ʼ�� ����������Ӧ�����
		int layer0_icons[] = {R.id.lancher_outer_layer01,R.id.lancher_outer_layer02,R.id.lancher_outer_layer03,
				R.id.lancher_outer_layer04,R.id.lancher_outer_layer05,R.id.lancher_outer_layer06,
				R.id.lancher_outer_layer07,R.id.lancher_outer_layer08,R.id.lancher_outer_layer09};
		layer0List = new ArrayList<View>();
		for (int i = 0; i < layer0_icons.length; i++) {
			ImageView iconIV =(ImageView) findViewById(layer0_icons[i]);
			layer0List.add(iconIV);
		}
		
		int layer1_icons[] = {R.id.lancher_outer_layer11,R.id.lancher_outer_layer12,R.id.lancher_outer_layer13,
				R.id.lancher_outer_layer14,R.id.lancher_outer_layer15,R.id.lancher_outer_layer16,
				R.id.lancher_outer_layer17,R.id.lancher_outer_layer18,R.id.lancher_outer_layer19};
		layer1List = new ArrayList<View>();
//		for (int i = 0; i < layer1_icons.length; i++) {
			for (int i = 0; i < 4; i++) {
			ImageView iconIV =(ImageView) findViewById(layer1_icons[i]);
			layer1List.add(iconIV);
		}
		
		int layer2_icons[] = {R.id.lancher_outer_layer21,R.id.lancher_outer_layer22,R.id.lancher_outer_layer23,
				R.id.lancher_outer_layer24,R.id.lancher_outer_layer25,R.id.lancher_outer_layer26,
				R.id.lancher_outer_layer27,R.id.lancher_outer_layer28,R.id.lancher_outer_layer29};
		layer2List = new ArrayList<View>();
//		for (int i = 0; i < layer2_icons.length; i++) {
			for (int i = 0; i < 6; i++) {
			ImageView iconIV =(ImageView) findViewById(layer2_icons[i]);
			layer2List.add(iconIV);
		}
		
		centerImageView.setOnClickListener(new OnClickListener() {	
			@Override
			public void onClick(View v) {
				mSectorAnimation.runUnPopAnimation();
			}
		});
		

	}
	
	private void initScreenData(){
		
		WindowManager wm = (WindowManager) SectorWidget.this.getSystemService(SectorWidget.this.WINDOW_SERVICE);
		SYS_SCREEN_WIDTH = wm.getDefaultDisplay().getWidth();//��Ļ���
		SYS_SCREEN_HIGHT = wm.getDefaultDisplay().getHeight();//��Ļ�߶�	
	}
	


	private void getSectorAreaSize(){
		
		SectorareSize = (RelativeLayout) findViewById(R.id.lancher_outer_area_size);
        
        ViewTreeObserver vto2 = SectorareSize.getViewTreeObserver();  
        vto2.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {  
            @Override  
            public void onGlobalLayout() {  
            	SectorareSize.getViewTreeObserver().removeGlobalOnLayoutListener(this);  
            	SYS_SECTOR_HIGHT = SectorareSize.getMeasuredHeight();
                SYS_SECTOR_WIDTH = SectorareSize.getMeasuredWidth();
            	SectorTestAPI.PrintCommon("screenSize", "3listener��ʽ��ȡ-���������ȣ�"+SYS_SECTOR_WIDTH+",��������߶ȣ�"+SYS_SECTOR_HIGHT);
                Message msg = Message.obtain();
                msg.what = MSG_SECTORSIZE;
                initViewHandler.sendMessageDelayed(msg, 200);
            }  
        });  
	}
	
	
	private void loadData(){
		
		SectorCenterArea centerArea = new SectorCenterArea(centerImageView,centerLayout);
		SectorMiddleArea middleArea = new SectorMiddleArea(middleFrameworkImageView, pointerAnimaImageView, pointerActualImageView, middleRelativeLayout
				,middle_Part0_TextView,middle_Part1_TextView,middle_Part2_TextView);
		SectorOuterArea outerArea = new SectorOuterArea(outerRelativeLayout,
				iconRelativeLayout0_bg,iconRelativeLayout1_bg,iconRelativeLayout2_bg, 
				iconlayer0_bg_img,iconlayer1_bg_img,iconlayer2_bg_img,
				iconRelativeLayout0,iconRelativeLayout1, iconRelativeLayout2,
				layer0List,layer1List,layer2List);
		mWholeArea = new SectorWholeArea(popImageView, sectorRelativeLayout, centerArea, middleArea, outerArea);
		mSectorAnimation = new SectorAnimation(SectorWidget.this,unpopHandler,mWholeArea,SYS_SECTOR_WIDTH,SYS_SECTOR_HIGHT);	
	}	
	
	
	
	
	private class RotateButtonListener implements OnTouchListener
	{
		@Override
		public boolean onTouch(View view, MotionEvent event) {
			if (MotionEvent.ACTION_DOWN ==event.getAction() ) {
				
				return mSectorAnimation.runPointerClickAnimation(event.getX(), event.getY(), view.getWidth());
			}
			return true;
		}
	}

	class WidgetGestureDetector extends SimpleOnGestureListener 
	{     
	   @Override
		public boolean onSingleTapUp(MotionEvent e) {
		   //������ؼ������⣬�ؼ���ʧ֮�߼�
		   if (SectorUtils.clickForUnpop(e.getX(), e.getY(), SYS_SECTOR_WIDTH)) {
			   mSectorAnimation.runUnPopAnimation();
			   return true;
		}
			return super.onSingleTapUp(e);
		}

	@Override     
	   public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) 
	   {  
			if (e1 == null || e2 == null) return false;  
			           
			SectorVector vector =  SectorUtils.getVectorForGestures(e1.getX(), e1.getY(), e2.getX(), e2.getY());
			
			if (null == vector){
				return false;
			}else if (!vector.lengthIsValid()) {
				return true;
			}
			if (SectorWidgetConfig.DIRECTION_LEFT_TOP == SectorWidget.WidgetCurrentLocation) {
				if (vector.isSecondQuadrant()) {
//					 Toast.makeText(SectorWidget.this, "�ڶ���������", Toast.LENGTH_SHORT).show();
					 mSectorAnimation.runPointerMoveAnimation(SectorPointerAnimation.CLOCKWISE_ROTATE);
				}else if (vector.isFourthQuadrant()) {
//					 Toast.makeText(SectorWidget.this, "������������", Toast.LENGTH_SHORT).show();
					 mSectorAnimation.runPointerMoveAnimation(SectorPointerAnimation.COUNTER_CLOCKWISE_ROTATE);
				}

			}else if (SectorWidgetConfig.DIRECTION_LEFT_BOTTOM == SectorWidget.WidgetCurrentLocation) {
				if (vector.isFirstQuadrant()) {
//					 Toast.makeText(SectorWidget.this, "��һ��������", Toast.LENGTH_SHORT).show();
					 mSectorAnimation.runPointerMoveAnimation(SectorPointerAnimation.CLOCKWISE_ROTATE);
				}else if (vector.isThirdQuadrant()) {
//					 Toast.makeText(SectorWidget.this, "������������", Toast.LENGTH_SHORT).show();
					 mSectorAnimation.runPointerMoveAnimation(SectorPointerAnimation.COUNTER_CLOCKWISE_ROTATE);
				} 
			}
			else if (SectorWidgetConfig.DIRECTION_RIGHT_BOTTOM == SectorWidget.WidgetCurrentLocation) {
				if (vector.isSecondQuadrant()) {
//					 Toast.makeText(SectorWidget.this, "�ڶ���������", Toast.LENGTH_SHORT).show();
					 mSectorAnimation.runPointerMoveAnimation(SectorPointerAnimation.COUNTER_CLOCKWISE_ROTATE);
				}else if (vector.isFourthQuadrant()) {
//					 Toast.makeText(SectorWidget.this, "������������", Toast.LENGTH_SHORT).show();
					 mSectorAnimation.runPointerMoveAnimation(SectorPointerAnimation.CLOCKWISE_ROTATE);
				}

			}else {
				if (vector.isSecondQuadrant()) {
//					 Toast.makeText(SectorWidget.this, "�ڶ���������", Toast.LENGTH_SHORT).show();
					 mSectorAnimation.runPointerMoveAnimation(SectorPointerAnimation.CLOCKWISE_ROTATE);
				}else if (vector.isFourthQuadrant()) {
//					 Toast.makeText(SectorWidget.this, "������������", Toast.LENGTH_SHORT).show();
					 mSectorAnimation.runPointerMoveAnimation(SectorPointerAnimation.COUNTER_CLOCKWISE_ROTATE);
				}
			}
         
		   return true;     
	   }
	} 
	
	  	@Override 
	   public boolean onTouchEvent(MotionEvent event) {     
		   if (flingDetector.onTouchEvent(event)){         
			   return true;     
		   }
		   return super.onTouchEvent(event); 
		} 
	  	
	  	
	  	
	  	
	  void	InitView(int imgDirection){
		 
		 int alignType1 = RelativeLayout.ALIGN_PARENT_LEFT;
		 int alignType2 = RelativeLayout.ALIGN_PARENT_TOP;
		 
		switch (imgDirection) {
		case SectorWidgetConfig.DIRECTION_LEFT_TOP :
			alignType1 = RelativeLayout.ALIGN_PARENT_LEFT;
			alignType2 = RelativeLayout.ALIGN_PARENT_TOP;
			break;
		case SectorWidgetConfig.DIRECTION_LEFT_BOTTOM :
			alignType1 = RelativeLayout.ALIGN_PARENT_LEFT;
			alignType2 = RelativeLayout.ALIGN_PARENT_BOTTOM;
			break;
		case SectorWidgetConfig.DIRECTION_RIGHT_BOTTOM :
			alignType1 = RelativeLayout.ALIGN_PARENT_RIGHT;
			alignType2 = RelativeLayout.ALIGN_PARENT_BOTTOM;
			break;		
		default:
			break;
		}
		
		
		 SectorUtils.modifyLayoutAlignType(sectorRelativeLayout, alignType1,alignType2);
		 		 
		 modifyImageDirection(centerImageView,R.drawable.cometonlaunch_center,imgDirection);
		 SectorUtils.modifyLayoutAlignType(centerLayout, alignType1,alignType2);
		 
		 SectorUtils.modifyLayoutAlignType(middleRelativeLayout, alignType1,alignType2);
		 SectorUtils.modifyLayoutAlignType(middleFrameworkImageView, alignType1,alignType2);
		 modifyImageDirection(middleFrameworkImageView,R.drawable.cometonlaunch_middle_framework,imgDirection);
		 middleFrameworkImageView.setOnTouchListener(new RotateButtonListener());
		 
		 
		 SectorUtils.modifyLayoutAlignType(pointerActualImageView, alignType1,alignType2);
		 modifyImageDirection(pointerActualImageView,R.drawable.cometonlaunch_quick_access_section_indicator0,imgDirection);
		 
		 SectorUtils.modifyLayoutAlignType(pointerAnimaImageView, alignType1,alignType2);
		 modifyImageDirection(pointerAnimaImageView,R.drawable.cometonlaunch_quick_access_section_indicator0,imgDirection);

		 SectorUtils.modifyLayoutAlignType(outerRelativeLayout, alignType1,alignType2);

		 SectorUtils.modifyLayoutAlignType(iconRelativeLayout0, alignType1,alignType2);
		 SectorUtils.modifyLayoutAlignType(iconRelativeLayout1, alignType1,alignType2);
		 SectorUtils.modifyLayoutAlignType(iconRelativeLayout2, alignType1,alignType2);
		 SectorUtils.modifyLayoutAlignType(iconRelativeLayout0_bg, alignType1,alignType2);
		 SectorUtils.modifyLayoutAlignType(iconRelativeLayout1_bg, alignType1,alignType2);
		 SectorUtils.modifyLayoutAlignType(iconRelativeLayout2_bg, alignType1,alignType2);
		 
		 SectorUtils.modifyLayoutAlignType(iconlayer0_bg_img, alignType1,alignType2);
		 if ( layer0List.size()<5) {
			 modifyImageDirection(iconlayer0_bg_img,R.drawable.cometonlaunch_outer_small_framework,imgDirection);
		}else {
			modifyImageDirection(iconlayer0_bg_img,R.drawable.cometonlaunch_outer_big_framework,imgDirection);
		}
		 
		 SectorUtils.modifyLayoutAlignType(iconlayer1_bg_img, alignType1,alignType2);
		 if ( layer1List.size()<5) {
			 modifyImageDirection(iconlayer1_bg_img,R.drawable.cometonlaunch_outer_small_framework,imgDirection);
		}else {
			modifyImageDirection(iconlayer1_bg_img,R.drawable.cometonlaunch_outer_big_framework,imgDirection);
		}
		 
		 SectorUtils.modifyLayoutAlignType(iconlayer2_bg_img, alignType1,alignType2);
		 
		 if ( layer2List.size()<5) {
			 modifyImageDirection(iconlayer2_bg_img,R.drawable.cometonlaunch_outer_small_framework,imgDirection);
		}else {
			modifyImageDirection(iconlayer2_bg_img,R.drawable.cometonlaunch_outer_big_framework,imgDirection);
		}
	  }
	  
	 public void modifyImageDirection(ImageView imgView,int imgDrawableID,int direction){
		    imgView.setImageDrawable(getResources().getDrawable(imgDrawableID));
			RotateDrawable rotateDrawable =(RotateDrawable)imgView.getDrawable();  
			rotateDrawable.setLevel(direction);
			imgView.setTag(Integer.valueOf(imgDrawableID));
			imgView.refreshDrawableState();
	 }
	

	
	
}


