package com.android.launcher2;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout.LayoutParams;

import java.util.ArrayList;
import java.util.List;

public class SectorUtils {
	//���η�ָ��
	public static final int  Square = 2;
	
	private static boolean isAppCountDBUpdate = false;
	public static final int ERROR_ID = -1;
	
	public static final int  PART_ERROR = -1;
	public static final int  PART0 = 0;
	public static final int  PART1 = 1;
	public static final int  PART2 = 2;
	
	
	
// for T920 Screen arguments(I6) 720 X 1080 xhdpi
	public static final float  I6_ICON_INNER_RADIUS_PERCENT_LEFTTOP = 0.68f;
	public static final float  I6_ICON_OUTER_RADIUS_PERCENT_LEFTTOP = 0.45f;
	public static final float  I6_ICON_INNER_RADIUS_PERCENT_LEFTBOTTOM = 0.75f;
	public static final float  I6_ICON_OUTER_RADIUS_PERCENT_LEFTBOTTOM = 0.53f;
	public static final float  I6_ICON_INNER_RADIUS_PERCENT_RIGHTBOTTOM = 0.80f;
	public static final float  I6_ICON_OUTER_RADIUS_PERCENT_RIGHTBOTTOM = 0.58f;


	public static final float  I6_TXT_MIDDLE_RADIUS_PERCENT_LEFTTOP = 0.27f;	
	public static final int  I6_ICON_OUTER_ANGLE_LEFTTOP[]= {5,25,45,65,85};
	public static final int  I6_ICON_INNER_ANGLE_LEFTTOP[]= {6,32,57,83};
	public static final int  I6_TXT_INNER_ANGLE_LEFTTOP[]= {80,45,0};
	
	public static final float  I6_TXT_MIDDLE_RADIUS_PERCENT_LEFTBOTTOM = 0.43f;
	public static final int  I6_ICON_OUTER_ANGLE_LEFTBOTTOM[]= {7,25,43,61,79};
	public static final int  I6_ICON_INNER_ANGLE_LEFTBOTTOM[]= {9,32,55,77};
	public static final int  I6_TXT_INNER_ANGLE_LEFTBOTTOM[]= {90,50,18};
	
	public static final float  I6_TXT_MIDDLE_RADIUS_PERCENT_RIGHTBOTTOM = 0.44f;
	public static final int  I6_ICON_OUTER_ANGLE_RIGHTBOTTOM[]= {13,31,49,67,85};
	public static final int  I6_ICON_INNER_ANGLE_RIGHTBOTTOM[]= {15,38,60,83};
	public static final int  I6_TXT_INNER_ANGLE_RIGHTBOTTOM[]= {21,42,65};
//	public static final int  I6_TXT_INNER_ANGLE_RIGHTBOTTOM[]= {20,42,65};
// for T920  720 X 1080 xhdpi  end
	
	
/*	
	//for T890 arguments
	public static final float  T890_ICON_INNER_RADIUS_PERCENT_LEFTTOP = 0.68f;
	public static final float  T890_ICON_OUTER_RADIUS_PERCENT_LEFTTOP = 0.45f;
	public static final float  T890_ICON_INNER_RADIUS_PERCENT_LEFTBOTTOM = 0.70f;
	public static final float  T890_ICON_OUTER_RADIUS_PERCENT_LEFTBOTTOM = 0.48f;
	public static final float  T890_ICON_INNER_RADIUS_PERCENT_RIGHTBOTTOM = 0.76f;
	public static final float  T890_ICON_OUTER_RADIUS_PERCENT_RIGHTBOTTOM = 0.50f;


	public static final float  T890_TXT_MIDDLE_RADIUS_PERCENT_LEFTTOP = 0.27f;	
	public static final int  T890_ICON_OUTER_ANGLE_LEFTTOP[]= {5,25,45,65,85};
	public static final int  T890_ICON_INNER_ANGLE_LEFTTOP[]= {6,32,57,83};
	public static final int  T890_TXT_INNER_ANGLE_LEFTTOP[]= {80,45,0};
	
	public static final float  T890_TXT_MIDDLE_RADIUS_PERCENT_LEFTBOTTOM = 0.43f;
	public static final int  T890_ICON_OUTER_ANGLE_LEFTBOTTOM[]= {9,27,45,63,81};
	public static final int  T890_ICON_INNER_ANGLE_LEFTBOTTOM[]= {9,33,57,81};
	public static final int  T890_TXT_INNER_ANGLE_LEFTBOTTOM[]= {90,50,18};
	
	public static final float  T890_TXT_MIDDLE_RADIUS_PERCENT_RIGHTBOTTOM = 0.42f;
	public static final int  T890_ICON_OUTER_ANGLE_RIGHTBOTTOM[]= {13,31,49,67,85};
	public static final int  T890_ICON_INNER_ANGLE_RIGHTBOTTOM[]= {14,38,62,86};
	public static final int  T890_TXT_INNER_ANGLE_RIGHTBOTTOM[]= {18,40,65};	
	//For T890 end
*/
	
	//for P1 hdpi 480*800 arguments
	public static final float  P1_ICON_INNER_RADIUS_PERCENT_LEFTTOP = 0.56f;
	public static final float  P1_ICON_OUTER_RADIUS_PERCENT_LEFTTOP = 0.36f;
	public static final float  P1_ICON_INNER_RADIUS_PERCENT_LEFTBOTTOM = 0.60f;
	public static final float  P1_ICON_OUTER_RADIUS_PERCENT_LEFTBOTTOM = 0.42f;
	public static final float  P1_ICON_INNER_RADIUS_PERCENT_RIGHTBOTTOM = 0.64f;
	public static final float  P1_ICON_OUTER_RADIUS_PERCENT_RIGHTBOTTOM = 0.44f;


	public static final float  P1_TXT_MIDDLE_RADIUS_PERCENT_LEFTTOP = 0.23f;	
	public static final int  P1_ICON_OUTER_ANGLE_LEFTTOP[]= {5,25,45,65,85};
	public static final int  P1_ICON_INNER_ANGLE_LEFTTOP[]= {6,32,57,83};
	public static final int  P1_TXT_INNER_ANGLE_LEFTTOP[]= {80,45,0};
	
	public static final float  P1_TXT_MIDDLE_RADIUS_PERCENT_LEFTBOTTOM = 0.40f;
	public static final int  P1_ICON_OUTER_ANGLE_LEFTBOTTOM[]= {9,27,45,63,81};
	public static final int  P1_ICON_INNER_ANGLE_LEFTBOTTOM[]= {9,33,57,81};
	public static final int  P1_TXT_INNER_ANGLE_LEFTBOTTOM[]= {90,48,18};
	
	public static final float  P1_TXT_MIDDLE_RADIUS_PERCENT_RIGHTBOTTOM = 0.40f;
	public static final int  P1_ICON_OUTER_ANGLE_RIGHTBOTTOM[]= {13,31,49,67,85};
	public static final int  P1_ICON_INNER_ANGLE_RIGHTBOTTOM[]= {14,38,62,86};
	public static final int  P1_TXT_INNER_ANGLE_RIGHTBOTTOM[]= {20,42,65};	
	//For P1 hdpi 480*800 end
	
	
	
	//for  arguments
	public static  float  ICON_INNER_RADIUS_PERCENT_LEFTTOP = 0.56f;
	public static  float  ICON_OUTER_RADIUS_PERCENT_LEFTTOP = 0.36f;
	public static  float  ICON_INNER_RADIUS_PERCENT_LEFTBOTTOM = 0.60f;
	public static  float  ICON_OUTER_RADIUS_PERCENT_LEFTBOTTOM = 0.42f;
	public static  float  ICON_INNER_RADIUS_PERCENT_RIGHTBOTTOM = 0.64f;
	public static  float  ICON_OUTER_RADIUS_PERCENT_RIGHTBOTTOM = 0.44f;


	public static  float  TXT_MIDDLE_RADIUS_PERCENT_LEFTTOP = 0.23f;	
	public static  int  ICON_OUTER_ANGLE_LEFTTOP[];//;= {5,25,45,65,85};
	public static  int  ICON_INNER_ANGLE_LEFTTOP[]= {6,32,57,83};
	public static  int  TXT_INNER_ANGLE_LEFTTOP[]= {80,45,0};
	
	public static  float  TXT_MIDDLE_RADIUS_PERCENT_LEFTBOTTOM = 0.40f;
	public static  int  ICON_OUTER_ANGLE_LEFTBOTTOM[]= {9,27,45,63,81};
	public static  int  ICON_INNER_ANGLE_LEFTBOTTOM[]= {9,33,57,81};
	public static  int  TXT_INNER_ANGLE_LEFTBOTTOM[]= {90,48,18};
	
	public static  float  TXT_MIDDLE_RADIUS_PERCENT_RIGHTBOTTOM = 0.40f;
	public static  int  ICON_OUTER_ANGLE_RIGHTBOTTOM[]= {13,31,49,67,85};
	public static  int  ICON_INNER_ANGLE_RIGHTBOTTOM[]= {14,38,62,86};
	public static  int  TXT_INNER_ANGLE_RIGHTBOTTOM[]= {18,40,65};	
	//For  end
	

	
	public static void initAgruments(int sysDensity){
		
		if (SectorView.XHDPI_DENSITY == sysDensity ) {
			ICON_INNER_RADIUS_PERCENT_LEFTTOP = I6_ICON_INNER_RADIUS_PERCENT_LEFTTOP;
			ICON_OUTER_RADIUS_PERCENT_LEFTTOP = I6_ICON_OUTER_RADIUS_PERCENT_LEFTTOP;
			ICON_INNER_RADIUS_PERCENT_LEFTBOTTOM = I6_ICON_INNER_RADIUS_PERCENT_LEFTBOTTOM;
			ICON_OUTER_RADIUS_PERCENT_LEFTBOTTOM = I6_ICON_OUTER_RADIUS_PERCENT_LEFTBOTTOM;
			ICON_INNER_RADIUS_PERCENT_RIGHTBOTTOM = I6_ICON_INNER_RADIUS_PERCENT_RIGHTBOTTOM;
			ICON_OUTER_RADIUS_PERCENT_RIGHTBOTTOM = I6_ICON_OUTER_RADIUS_PERCENT_RIGHTBOTTOM;


			TXT_MIDDLE_RADIUS_PERCENT_LEFTTOP = I6_TXT_MIDDLE_RADIUS_PERCENT_LEFTTOP;	
			ICON_OUTER_ANGLE_LEFTTOP = I6_ICON_OUTER_ANGLE_LEFTTOP;
			ICON_INNER_ANGLE_LEFTTOP= I6_ICON_INNER_ANGLE_LEFTTOP;
			TXT_INNER_ANGLE_LEFTTOP= I6_TXT_INNER_ANGLE_LEFTTOP;
			
			TXT_MIDDLE_RADIUS_PERCENT_LEFTBOTTOM = I6_TXT_MIDDLE_RADIUS_PERCENT_LEFTBOTTOM;
			ICON_OUTER_ANGLE_LEFTBOTTOM = I6_ICON_OUTER_ANGLE_LEFTBOTTOM ;
			ICON_INNER_ANGLE_LEFTBOTTOM = I6_ICON_INNER_ANGLE_LEFTBOTTOM ;
			TXT_INNER_ANGLE_LEFTBOTTOM = I6_TXT_INNER_ANGLE_LEFTBOTTOM ;
			
			TXT_MIDDLE_RADIUS_PERCENT_RIGHTBOTTOM = I6_TXT_MIDDLE_RADIUS_PERCENT_RIGHTBOTTOM;
			ICON_OUTER_ANGLE_RIGHTBOTTOM = I6_ICON_OUTER_ANGLE_RIGHTBOTTOM;
			ICON_INNER_ANGLE_RIGHTBOTTOM = I6_ICON_INNER_ANGLE_RIGHTBOTTOM;
			TXT_INNER_ANGLE_RIGHTBOTTOM = I6_TXT_INNER_ANGLE_RIGHTBOTTOM;	
			
			
		}else if (SectorView.HDPI_DENSITY == sysDensity) {
			
			ICON_INNER_RADIUS_PERCENT_LEFTTOP = P1_ICON_INNER_RADIUS_PERCENT_LEFTTOP;
			ICON_OUTER_RADIUS_PERCENT_LEFTTOP = P1_ICON_OUTER_RADIUS_PERCENT_LEFTTOP;
			ICON_INNER_RADIUS_PERCENT_LEFTBOTTOM = P1_ICON_INNER_RADIUS_PERCENT_LEFTBOTTOM;
			ICON_OUTER_RADIUS_PERCENT_LEFTBOTTOM = P1_ICON_OUTER_RADIUS_PERCENT_LEFTBOTTOM;
			ICON_INNER_RADIUS_PERCENT_RIGHTBOTTOM = P1_ICON_INNER_RADIUS_PERCENT_RIGHTBOTTOM;
			ICON_OUTER_RADIUS_PERCENT_RIGHTBOTTOM = P1_ICON_OUTER_RADIUS_PERCENT_RIGHTBOTTOM;


			TXT_MIDDLE_RADIUS_PERCENT_LEFTTOP = P1_TXT_MIDDLE_RADIUS_PERCENT_LEFTTOP;	
			ICON_OUTER_ANGLE_LEFTTOP = P1_ICON_OUTER_ANGLE_LEFTTOP;
			ICON_INNER_ANGLE_LEFTTOP= P1_ICON_INNER_ANGLE_LEFTTOP;
			TXT_INNER_ANGLE_LEFTTOP= P1_TXT_INNER_ANGLE_LEFTTOP;
			
			TXT_MIDDLE_RADIUS_PERCENT_LEFTBOTTOM = P1_TXT_MIDDLE_RADIUS_PERCENT_LEFTBOTTOM;
			ICON_OUTER_ANGLE_LEFTBOTTOM = P1_ICON_OUTER_ANGLE_LEFTBOTTOM ;
			ICON_INNER_ANGLE_LEFTBOTTOM = P1_ICON_INNER_ANGLE_LEFTBOTTOM ;
			TXT_INNER_ANGLE_LEFTBOTTOM = P1_TXT_INNER_ANGLE_LEFTBOTTOM ;
			
			TXT_MIDDLE_RADIUS_PERCENT_RIGHTBOTTOM = P1_TXT_MIDDLE_RADIUS_PERCENT_RIGHTBOTTOM;
			ICON_OUTER_ANGLE_RIGHTBOTTOM = P1_ICON_OUTER_ANGLE_RIGHTBOTTOM;
			ICON_INNER_ANGLE_RIGHTBOTTOM = P1_ICON_INNER_ANGLE_RIGHTBOTTOM;
			TXT_INNER_ANGLE_RIGHTBOTTOM = P1_TXT_INNER_ANGLE_RIGHTBOTTOM;	
			
			
		}
		
		
		
		
		
	}
	
	
	
	
	
	
	

	
	public static SectorVector getVectorForGestures(float point1_x,float point1_y,float point2_x,float point2_y)
	{
		SectorVector vector = null;
		if (point2_y >= point1_y && point2_x >= point1_x) {
			SectorTestAPI.PrintCommon(null, "��һ����");
			   
			double degress = Math.toDegrees(Math.atan((point2_y - point1_y)/(point2_x - point1_x)));
			double length = Math.sqrt(Math.pow((point2_y - point1_y), Square)+Math.pow((point2_x - point1_x),Square));
			vector = new SectorVector(length, degress);

		}else if (point2_y > point1_y && point2_x < point1_x) {
			SectorTestAPI.PrintCommon(null, "�ڶ�����");
			
			double degress = Math.toDegrees(Math.atan((point2_y - point1_y)/(point2_x - point1_x)));
			double length = Math.sqrt(Math.pow((point2_y - point1_y), Square)+Math.pow((point2_x - point1_x),Square));
			vector = new SectorVector(length, degress+180.0);

		}else if (point2_y <= point1_y && point2_x <= point1_x) {
			SectorTestAPI.PrintCommon(null, "��������");
			
			double degress = Math.toDegrees(Math.atan((point2_y - point1_y)/(point2_x - point1_x)));
			double length = Math.sqrt(Math.pow((point2_y - point1_y), Square)+Math.pow((point2_x - point1_x),Square));
			vector = new SectorVector(length, degress+180.0);
		
		}else if (point2_y < point1_y && point2_x > point1_x) {
			SectorTestAPI.PrintCommon(null, "��������");
			
			double degress = Math.toDegrees(Math.atan((point2_y - point1_y)/(point2_x - point1_x)));
			double length = Math.sqrt(Math.pow((point2_y - point1_y), Square)+Math.pow((point2_x - point1_x),Square));
			vector = new SectorVector(length, degress+360.0);
		}
		SectorTestAPI.PrintCommon(null, "ʸ���ȣ�"+vector.length);
		SectorTestAPI.PrintCommon(null, "ʸ�Ƕȣ�"+vector.angle);
		return vector ;
	}
	
	
	public static double getTowPointDistance(float point1_x,float point1_y,float point2_x,float point2_y)
	{
		return  Math.sqrt(Math.pow((point2_y - point1_y), Square)+Math.pow((point2_x - point1_x),Square));
	}
	
	
	public static List<SectorIconInfo> getCoordinateForIcons(int circleRadius)
	{
		List<SectorIconInfo> list = new ArrayList<SectorIconInfo>();
		//Ӧ��ͼ�� ��Բ�α����ı�Ե���ڲ�
		int iconOuterRadius = (int) (circleRadius * ICON_INNER_RADIUS_PERCENT_LEFTTOP);
		int iconInnerRadius = (int) (circleRadius * ICON_OUTER_RADIUS_PERCENT_LEFTTOP);
		
		
		if (SectorWidgetConfig.DIRECTION_LEFT_TOP == SectorView.WidgetCurrentLocation) {
			iconOuterRadius = (int) (circleRadius * ICON_INNER_RADIUS_PERCENT_LEFTTOP);
			iconInnerRadius = (int) (circleRadius * ICON_OUTER_RADIUS_PERCENT_LEFTTOP);
			
			for (int i = 0; i < ICON_INNER_ANGLE_LEFTTOP.length; i++) {
				
				float angre_sin = (float) Math.sin(((float)Math.PI/180.0f) *ICON_INNER_ANGLE_LEFTTOP[i]);
				float angre_cos = (float) Math.cos(((float)Math.PI/180.0f) *ICON_INNER_ANGLE_LEFTTOP[i]);
				int button_y = (int) (angre_sin * iconInnerRadius);
				int button_x = (int) (angre_cos * iconInnerRadius);
				
				list.add(new SectorIconInfo(button_x, button_y));
			}
			for (int i = 0; i < ICON_OUTER_ANGLE_LEFTTOP.length; i++) {
				
				float angre_sin = (float) Math.sin(((float)Math.PI/180.0f) *ICON_OUTER_ANGLE_LEFTTOP[i]);
				float angre_cos = (float) Math.cos(((float)Math.PI/180.0f) *ICON_OUTER_ANGLE_LEFTTOP[i]);
				int button_y = (int) (angre_sin * iconOuterRadius);
				int button_x = (int) (angre_cos * iconOuterRadius);

				list.add(new SectorIconInfo(button_x, button_y));
			}

		}else if (SectorWidgetConfig.DIRECTION_LEFT_BOTTOM == SectorView.WidgetCurrentLocation) {
			iconOuterRadius = (int) (circleRadius * ICON_INNER_RADIUS_PERCENT_LEFTBOTTOM);
			iconInnerRadius = (int) (circleRadius * ICON_OUTER_RADIUS_PERCENT_LEFTBOTTOM);
			for (int i = 0; i < ICON_INNER_ANGLE_LEFTBOTTOM.length; i++) {
				
				float angre_sin = (float) Math.sin(((float)Math.PI/180.0f) *ICON_INNER_ANGLE_LEFTBOTTOM[i]);
				float angre_cos = (float) Math.cos(((float)Math.PI/180.0f) *ICON_INNER_ANGLE_LEFTBOTTOM[i]);
				int button_x = (int) (angre_sin * iconInnerRadius);
				int button_y = (int) (angre_cos * iconInnerRadius);
				int offset = (int) (65*1.6)+40;

				button_y = SectorView.SYS_SCREEN_HIGHT - button_y - offset;
				button_x = button_x - 30;
				list.add(new SectorIconInfo(button_x, button_y));
			}
			for (int i = 0; i < ICON_OUTER_ANGLE_LEFTBOTTOM.length; i++) {
				
				float angre_sin = (float) Math.sin(((float)Math.PI/180.0f) *ICON_OUTER_ANGLE_LEFTBOTTOM[i]);
				float angre_cos = (float) Math.cos(((float)Math.PI/180.0f) *ICON_OUTER_ANGLE_LEFTBOTTOM[i]);
				int button_x = (int) (angre_sin * iconOuterRadius);
				int button_y = (int) (angre_cos * iconOuterRadius);
				int offset = (int) (65*1.6)+50;
				
				button_y = SectorView.SYS_SCREEN_HIGHT - button_y - offset;
				button_x = button_x - 30;
				list.add(new SectorIconInfo(button_x, button_y));
			}
		}
		else if (SectorWidgetConfig.DIRECTION_RIGHT_BOTTOM == SectorView.WidgetCurrentLocation) {
				iconOuterRadius = (int) (circleRadius * ICON_INNER_RADIUS_PERCENT_RIGHTBOTTOM);
				iconInnerRadius = (int) (circleRadius * ICON_OUTER_RADIUS_PERCENT_RIGHTBOTTOM);
				
			//���£�Ӧ������Ϊ���µ��ϣ��ָ�Ϊ���ϵ����4���ʾ�����ϣ����·��һ��
			for (int i = ICON_INNER_ANGLE_RIGHTBOTTOM.length -1; i >= 0; i--) {
//			for (int i = 0; i < ICON_INNER_ANGLE_RIGHTBOTTOM.length; i++) {
				float angre_sin = (float) Math.sin(((float)Math.PI/180.0f) *ICON_INNER_ANGLE_RIGHTBOTTOM[i]);
				float angre_cos = (float) Math.cos(((float)Math.PI/180.0f) *ICON_INNER_ANGLE_RIGHTBOTTOM[i]);
				int button_y = (int) (angre_sin * (iconInnerRadius));
				int button_x = (int) (angre_cos * (iconInnerRadius));
				int offset = (int) (65*1.6)+ i*5;
				
				list.add(new SectorIconInfo(SectorView.SYS_SCREEN_WIDTH - button_x-offset, SectorView.SYS_SCREEN_HIGHT -button_y-offset));

			}
			//���£�Ӧ������Ϊ���µ��ϣ��ָ�Ϊ���ϵ����4���ʾ�����ϣ����·��һ��
			for (int i = ICON_OUTER_ANGLE_RIGHTBOTTOM.length -1; i >= 0; i--) {
//			for (int i = 0; i < ICON_OUTER_ANGLE_RIGHTBOTTOM.length; i++) {				
				float angre_sin = (float) Math.sin(((float)Math.PI/180.0f) *ICON_OUTER_ANGLE_RIGHTBOTTOM[i]);
				float angre_cos = (float) Math.cos(((float)Math.PI/180.0f) *ICON_OUTER_ANGLE_RIGHTBOTTOM[i]);
				int button_y = (int) (angre_sin * (iconOuterRadius));
				int button_x = (int) (angre_cos * (iconOuterRadius));
				int offset = (int) (65*1.6);

				list.add(new SectorIconInfo(SectorView.SYS_SCREEN_WIDTH - button_x-offset, SectorView.SYS_SCREEN_HIGHT -button_y-offset));
			}
		}
		
		for (int i = 0; i < list.size(); i++) {

			SectorTestAPI.PrintCommon(null, "���"+i+":("+list.get(i).coordinates_x+","+list.get(i).coordinates_y+")");
		}
		return list;
	}
	
	public static List<SectorIconInfo> getCoordinateForText(int circleRadius)
	{
		List<SectorIconInfo> list = new ArrayList<SectorIconInfo>();
		//Ӧ��ͼ�� ��Բ�α����ı�Ե���ڲ�
		int txtRadius = (int) (circleRadius * TXT_MIDDLE_RADIUS_PERCENT_LEFTTOP);

		if (SectorWidgetConfig.DIRECTION_LEFT_TOP == SectorView.WidgetCurrentLocation) {
			txtRadius = (int) (circleRadius * TXT_MIDDLE_RADIUS_PERCENT_LEFTTOP);
			
			for (int i = 0; i < TXT_INNER_ANGLE_LEFTTOP.length; i++) {
				float angre_sin = (float) Math.sin(((float)Math.PI/180.0f) *TXT_INNER_ANGLE_LEFTTOP[i]);
				float angre_cos = (float) Math.cos(((float)Math.PI/180.0f) *TXT_INNER_ANGLE_LEFTTOP[i]);
				int button_x1 = (int) (angre_sin * txtRadius);
				int button_y1 = (int) (angre_cos * txtRadius);
				list.add(new SectorIconInfo(button_x1, button_y1));
			}

		}else if (SectorWidgetConfig.DIRECTION_LEFT_BOTTOM == SectorView.WidgetCurrentLocation) {
			txtRadius = (int) (circleRadius * TXT_MIDDLE_RADIUS_PERCENT_LEFTBOTTOM);
			
			for (int i = 0; i < TXT_INNER_ANGLE_LEFTBOTTOM.length; i++) {	
				float angre_sin = (float) Math.sin(((float)Math.PI/180.0f) *TXT_INNER_ANGLE_LEFTBOTTOM[i]);
				float angre_cos = (float) Math.cos(((float)Math.PI/180.0f) *TXT_INNER_ANGLE_LEFTBOTTOM[i]);
				int button_y1 = (int) (angre_sin * txtRadius);
				int button_x1 = (int) (angre_cos * txtRadius);
//				list.add(new IconInfo(button_x1-8*i, SectorView.SYS_SCREEN_HIGHT - button_y1 +15*i));
				list.add(new SectorIconInfo(button_x1-50*i, SectorView.SYS_SCREEN_HIGHT - button_y1 -15*i));
			}
		}
		else if (SectorWidgetConfig.DIRECTION_RIGHT_BOTTOM == SectorView.WidgetCurrentLocation) {
			txtRadius = (int) (circleRadius * TXT_MIDDLE_RADIUS_PERCENT_RIGHTBOTTOM);
			for (int i = 0; i < TXT_INNER_ANGLE_RIGHTBOTTOM.length; i++) {
				float angre_sin = (float) Math.sin(((float)Math.PI/180.0f) *TXT_INNER_ANGLE_RIGHTBOTTOM[i]);
				float angre_cos = (float) Math.cos(((float)Math.PI/180.0f) *TXT_INNER_ANGLE_RIGHTBOTTOM[i]);
				int button_x1 = (int) (angre_sin * txtRadius);
				int button_y1 = (int) (angre_cos * txtRadius);
				list.add(new SectorIconInfo(SectorView.SYS_SCREEN_WIDTH - button_x1, SectorView.SYS_SCREEN_HIGHT - button_y1));
			}
		}
		
		for (int i = 0; i < list.size(); i++) {

			SectorTestAPI.PrintCommon(null, "txt���"+i+":("+list.get(i).coordinates_x+","+list.get(i).coordinates_y+")");
		}
		return list;
	}
	
	
	
	
	public static int getCurrentLocation(int drawableID)
	{
		if (drawableID == SectorWidgetConfig.DRAWABLE_POINTER_0PART_ID) {
			return PART0;
		}else if (drawableID == SectorWidgetConfig.DRAWABLE_POINTER_1PART_ID) {
			return PART1;
		}else if (drawableID == SectorWidgetConfig.DRAWABLE_POINTER_2PART_ID) {
			return PART2;
		}
		return PART_ERROR;
	}
	
	public static int getCurrentLocation(View view)
	{
		int drawableID = ((Integer)(view.getTag())).intValue();
		if (drawableID == SectorWidgetConfig.DRAWABLE_POINTER_0PART_ID) {
			return PART0;
		}else if (drawableID == SectorWidgetConfig.DRAWABLE_POINTER_1PART_ID) {
			return PART1;
		}else if (drawableID == SectorWidgetConfig.DRAWABLE_POINTER_2PART_ID) {
			return PART2;
		}
		return PART_ERROR;
	}
		
	public static void modifyLayoutAlignType(View view,int alignType1,int alignType2)
	{
		LayoutParams params = (LayoutParams) view.getLayoutParams();
		params.addRule(alignType1);
		params.addRule(alignType2);
		view.setLayoutParams(params);
	}

	
	
	public static boolean clickIsValib(float x,float y,int  radius)
	{
		SectorTestAPI.PrintCommon(null, "��Ļ��Ϊ��"+SectorView.SYS_SCREEN_HIGHT);
		SectorTestAPI.PrintCommon(null, "���£����X��꣺"+x);
		SectorTestAPI.PrintCommon(null, "���£����Y��꣺"+y);
		if (SectorWidgetConfig.DIRECTION_LEFT_TOP == SectorView.WidgetCurrentLocation) {
			
			  if (Math.pow(x, 2)+ Math.pow(y, 2) > Math.pow(radius,2)) {
				  return false;
			};
			//  if (Math.pow(x, 2)+ Math.pow(y, 2) < Math.pow(radius/2,2)){ 
			  if (Math.pow(x, 2)+ Math.pow(y, 2) < Math.pow(radius/(SectorView.MIDDLE_AREA_RADIUS/SectorView.CENTER_AREA_RADIUS),2)){ 
				  return false;
			};
		}else if (SectorWidgetConfig.DIRECTION_LEFT_BOTTOM == SectorView.WidgetCurrentLocation) {
			
			  if (Math.pow(x, 2)+ Math.pow(radius -y, 2) > Math.pow(radius,2)) {
				  return false;
			};
			 // if (Math.pow(x, 2)+ Math.pow(radius -y, 2) < Math.pow(radius/2,2)){ 
			  if (Math.pow(x, 2)+ Math.pow(radius -y, 2) < Math.pow(radius/(SectorView.MIDDLE_AREA_RADIUS/SectorView.CENTER_AREA_RADIUS),2)){ 
				  return false;
			};
		}else if (SectorWidgetConfig.DIRECTION_RIGHT_BOTTOM == SectorView.WidgetCurrentLocation) {
			
			  if (Math.pow(radius - x, 2)+ Math.pow(radius -y, 2) > Math.pow(radius,2)) {
				  return false;
			};
			  //if (Math.pow(radius - x, 2)+ Math.pow(radius -y, 2) < Math.pow(radius/2,2)){ 
			  if (Math.pow(radius - x, 2)+ Math.pow(radius -y, 2) < Math.pow(radius/(SectorView.MIDDLE_AREA_RADIUS/SectorView.CENTER_AREA_RADIUS),2)){ 
				  return false;
			};
		}
		return true;
	}
	
	
	public static boolean clickForUnpop(float x,float y,int  radius)
	{
		SectorTestAPI.PrintCommon(null, "��Ļ��Ϊ��"+SectorView.SYS_SCREEN_HIGHT);
		SectorTestAPI.PrintCommon(null, "���£����X��꣺"+x);
		SectorTestAPI.PrintCommon(null, "���£����Y��꣺"+y);
		if (SectorWidgetConfig.DIRECTION_LEFT_TOP == SectorView.WidgetCurrentLocation) {
			  if (Math.pow(x, 2)+ Math.pow(y, 2) > Math.pow(radius,2)) {
				  return true;
			};
			  
		}else if (SectorWidgetConfig.DIRECTION_LEFT_BOTTOM == SectorView.WidgetCurrentLocation) {
			  if (Math.pow(x, 2)+ Math.pow(SectorView.SYS_SCREEN_HIGHT -y, 2) > Math.pow(radius,2)) {
				  return true;
			};
			  
		}else if (SectorWidgetConfig.DIRECTION_RIGHT_BOTTOM == SectorView.WidgetCurrentLocation) {
			  if (Math.pow(SectorView.SYS_SCREEN_WIDTH - x, 2)+ Math.pow(SectorView.SYS_SCREEN_HIGHT -y, 2) > Math.pow(radius,2)) {
				  return true;
			};
		}
		return false;
	}
	
	public static void updateAppCountToDatabase(Context context,long id ){
        if (ERROR_ID != id) {
        	setAppCountUpdating(true);
        	writeAppCountToDatabase(context,id);
		}
	}
	
/*	public static void updateAppCountToDatabase(Context context,Intent intent, List<ApplicationInfo> mApps){
		long appID = getShortcutInfoIDByIntent(intent,mApps);
        if (ERROR_ID != appID) {
        	setAppCountUpdating(true);
        	writeAppCountToDatabase(context,appID);
		}
	}
*/	
	public static boolean isAppCountUpdating(){
		return isAppCountDBUpdate;
	}
	
	public static void setAppCountUpdating(boolean appCountUpdate){
		isAppCountDBUpdate = appCountUpdate;
		
	}
//	
	private static long getShortcutInfoIDByIntent(Intent intent, List<ApplicationInfo> mApps){
		if(null == mApps|| null == intent)return ERROR_ID;
		
		for (int i = 0; i < mApps.size(); i++) {
			Intent modeIntent = mApps.get(i).intent;
			if (null != modeIntent && modeIntent.getComponent().getPackageName()
										.equals(intent.getComponent().getPackageName())
				&& null != modeIntent && modeIntent.getComponent().getClassName()
										.equals(intent.getComponent().getClassName())) {
				
				ShortcutInfo info =  mApps.get(i);
				SectorTestAPI.PrintCommon("1212", ""+modeIntent.getComponent().getPackageName()+"-ID:"+info.getId());
				return info.getId();
			}
		}
		return ERROR_ID;
	}
	
	private static void writeAppCountToDatabase (Context context,long shortcutInfoID){
        final ContentResolver contentResolver = context.getContentResolver();
        String [] projection = {LauncherSettings.Favorites._ID,
        						LauncherSettings.Favorites.APP_LAUNCH_COUNT,
        						LauncherSettings.Favorites.ITEM_TYPE};
        synchronized (LauncherModel.sBgLock) {
        
	        final Cursor c = contentResolver.query(LauncherSettings.Favorites.CONTENT_URI,
	        		projection, LauncherSettings.Favorites._ID+"= '" + shortcutInfoID + "'", null, null);
	        try{
	        	final int idIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites._ID);
		        final int countIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.APP_LAUNCH_COUNT);
		        final int itemTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ITEM_TYPE);
		        while (c.moveToNext()) {
		        	try {
			                int itemType = c.getInt(itemTypeIndex);
			                switch (itemType) {
			                case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
			                case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
	
		                    int count =  c.getInt(countIndex);
		                    count++;
		            
		                    ContentValues values = new ContentValues();
		                    Uri uri = Uri.withAppendedPath(LauncherSettings.Favorites.CONTENT_URI, c.getString(idIndex));
		                    values.put(LauncherSettings.Favorites.APP_LAUNCH_COUNT, count);
		                    contentResolver.update(uri, values, null, null);
		                    break;
		                }
		        	} catch (Exception e) {
		                e.printStackTrace();
		            }finally {
		            	if (null != c) {
		                c.close();
		            	}
		            }
		        	
		        }
			}finally {
				if (null != c) {
					c.close();
				}
	            
	        }
        }
	}
	
	
	
	
	public static Bitmap blurImageAmeliorate(Bitmap bmp) 
    { 
        long start = System.currentTimeMillis(); 
        // 高斯矩阵 
        int[] gauss = new int[] { 1, 2, 1, 2, 4, 2, 1, 2, 1 }; 
         
        int width = bmp.getWidth(); 
        int height = bmp.getHeight(); 
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565); 
         
        int pixR = 0; 
        int pixG = 0; 
        int pixB = 0; 
         
        int pixColor = 0; 
         
        int newR = 0; 
        int newG = 0; 
        int newB = 0; 
         
        int delta = 8; // 值越小图片会越亮，越大则越暗 
         
        int idx = 0; 
        int[] pixels = new int[width * height]; 
        bmp.getPixels(pixels, 0, width, 0, 0, width, height); 
        for (int i = 1, length = height - 1; i < length; i++) 
        { 
            for (int k = 1, len = width - 1; k < len; k++) 
            { 
                idx = 0; 
                for (int m = -1; m <= 1; m++) 
                { 
                    for (int n = -1; n <= 1; n++) 
                    { 
                        pixColor = pixels[(i + m) * width + k + n]; 
                        pixR = Color.red(pixColor); 
                        pixG = Color.green(pixColor); 
                        pixB = Color.blue(pixColor); 
                         
                        newR = newR + (int) (pixR * gauss[idx]); 
                        newG = newG + (int) (pixG * gauss[idx]); 
                        newB = newB + (int) (pixB * gauss[idx]); 
                        idx++; 
                    } 
                } 
                 
                newR /= delta; 
                newG /= delta; 
                newB /= delta; 
                 
                newR = Math.min(255, Math.max(0, newR)); 
                newG = Math.min(255, Math.max(0, newG)); 
                newB = Math.min(255, Math.max(0, newB)); 
                 
                pixels[i * width + k] = Color.argb(255, newR, newG, newB); 
                 
                newR = 0; 
                newG = 0; 
                newB = 0; 
            } 
        } 
         
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height); 
        long end = System.currentTimeMillis(); 
        Log.d("may", "used time="+(end - start)); 
        return bitmap; 
    } 
	
	
	public static Bitmap blurImage(Bitmap bmp)  
    {  
        int width = bmp.getWidth();  
        int height = bmp.getHeight();  
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);  
          
        int pixColor = 0;  
          
        int newR = 0;  
        int newG = 0;  
        int newB = 0;  
          
        int newColor = 0;  
          
        int[][] colors = new int[9][3];  
        for (int i = 1, length = width - 1; i < length; i++)  
        {  
            for (int k = 1, len = height - 1; k < len; k++)  
            {  
                for (int m = 0; m < 9; m++)  
                {  
                    int s = 0;  
                    int p = 0;  
                    switch(m)  
                    {  
                    case 0:  
                        s = i - 1;  
                        p = k - 1;  
                        break;  
                    case 1:  
                        s = i;  
                        p = k - 1;  
                        break;  
                    case 2:  
                        s = i + 1;  
                        p = k - 1;  
                        break;  
                    case 3:  
                        s = i + 1;  
                        p = k;  
                        break;  
                    case 4:  
                        s = i + 1;  
                        p = k + 1;  
                        break;  
                    case 5:  
                        s = i;  
                        p = k + 1;  
                        break;  
                    case 6:  
                        s = i - 1;  
                        p = k + 1;  
                        break;  
                    case 7:  
                        s = i - 1;  
                        p = k;  
                        break;  
                    case 8:  
                        s = i;  
                        p = k;  
                    }  
                    pixColor = bmp.getPixel(s, p);  
                    colors[m][0] = Color.red(pixColor);  
                    colors[m][1] = Color.green(pixColor);  
                    colors[m][2] = Color.blue(pixColor);  
                }  
                  
                for (int m = 0; m < 9; m++)  
                {  
                    newR += colors[m][0];  
                    newG += colors[m][1];  
                    newB += colors[m][2];  
                }  
                  
                newR = (int) (newR / 9F);  
                newG = (int) (newG / 9F);  
                newB = (int) (newB / 9F);  
                  
                newR = Math.min(255, Math.max(0, newR));  
                newG = Math.min(255, Math.max(0, newG));  
                newB = Math.min(255, Math.max(0, newB));  
                  
                newColor = Color.argb(255, newR, newG, newB);  
                bitmap.setPixel(i, k, newColor);  
                  
                newR = 0;  
                newG = 0;  
                newB = 0;  
            }  
        }  
          
        return bitmap;  
    }  
	
	
	
	/** 水平方向模糊度 */
	private static float hRadius = 3;//5;//10;
	/** 竖直方向模糊度 */
	private static float vRadius = 3;//5;//10;
	/** 模糊迭代度 */
	private static int iterations = 3;//3;//7;

	/*** 高斯模糊
	     
	    public static Drawable BoxBlurFilter(Bitmap bmp) {
	        int width = bmp.getWidth();
	        int height = bmp.getHeight();
	        int[] inPixels = new int[width * height];
	        int[] outPixels = new int[width * height];
	        Bitmap bitmap = Bitmap.createBitmap(width, height,Bitmap.Config.ARGB_8888);
	        bmp.getPixels(inPixels, 0, width, 0, 0, width, height);
	        for (int i = 0; i < iterations; i++) {
	            blur(inPixels, outPixels, width, height, hRadius);
	            blur(outPixels, inPixels, height, width, vRadius);
	        }
	        blurFractional(inPixels, outPixels, width, height, hRadius);
	        blurFractional(outPixels, inPixels, height, width, vRadius);
	        bitmap.setPixels(inPixels, 0, width, 0, 0, width, height);
	        Drawable drawable = new BitmapDrawable(bitmap);
	        return drawable;
	    }
	*/    
	    
	    public static Bitmap BoxBlurFilter2(Bitmap bmp) {
	        int width = bmp.getWidth();
	        int height = bmp.getHeight();
	        int[] inPixels = new int[width * height];
	        int[] outPixels = new int[width * height];
	        Bitmap bitmap = Bitmap.createBitmap(width, height,Bitmap.Config.ARGB_8888);
	        bmp.getPixels(inPixels, 0, width, 0, 0, width, height);
	        for (int i = 0; i < iterations; i++) {
	            blur(inPixels, outPixels, width, height, hRadius);
	            blur(outPixels, inPixels, height, width, vRadius);
	        }
//	        blurFractional(inPixels, outPixels, width, height, hRadius);
//	        blurFractional(outPixels, inPixels, height, width, vRadius);
	        bitmap.setPixels(inPixels, 0, width, 0, 0, width, height);
	        
	        return bitmap;
	    }


	public static void blur(int[] in, int[] out, int width, int height,
	            float radius) {
	        int widthMinus1 = width - 1;
	        int r = (int) radius;
	        int tableSize = 2 * r + 1;
	        int divide[] = new int[256 * tableSize];
	 
	        for (int i = 0; i < 256 * tableSize; i++)
	            divide[i] = i / tableSize;
	 
	        int inIndex = 0;
	 
	        for (int y = 0; y < height; y++) {
	            int outIndex = y;
	            int ta = 0, tr = 0, tg = 0, tb = 0;
	 
	            for (int i = -r; i <= r; i++) {
	                int rgb = in[inIndex + clamp(i, 0, width - 1)];
	                ta += (rgb >> 24) & 0xff;
	                tr += (rgb >> 16) & 0xff;
	                tg += (rgb >> 8) & 0xff;
	                tb += rgb & 0xff;
	            }
	 
	            for (int x = 0; x < width; x++) {
	                out[outIndex] = (divide[ta] << 24) | (divide[tr] << 16)
	                        | (divide[tg] << 8) | divide[tb];
	 
	                int i1 = x + r + 1;
	                if (i1 > widthMinus1)
	                    i1 = widthMinus1;
	                int i2 = x - r;
	                if (i2 < 0)
	                    i2 = 0;
	                int rgb1 = in[inIndex + i1];
	                int rgb2 = in[inIndex + i2];
	 
	                ta += ((rgb1 >> 24) & 0xff) - ((rgb2 >> 24) & 0xff);
	                tr += ((rgb1 & 0xff0000) - (rgb2 & 0xff0000)) >> 16;
	                tg += ((rgb1 & 0xff00) - (rgb2 & 0xff00)) >> 8;
	                tb += (rgb1 & 0xff) - (rgb2 & 0xff);
	                outIndex += height;
	            }
	            inIndex += width;
	        }
	    }
	 
	    public static void blurFractional(int[] in, int[] out, int width,
	            int height, float radius) {
	        radius -= (int) radius;
	        float f = 1.0f / (1 + 2 * radius);
	        int inIndex = 0;
	 
	        for (int y = 0; y < height; y++) {
	            int outIndex = y;
	 
	            out[outIndex] = in[0];
	            outIndex += height;
	            for (int x = 1; x < width - 1; x++) {
	                int i = inIndex + x;
	                int rgb1 = in[i - 1];
	                int rgb2 = in[i];
	                int rgb3 = in[i + 1];
	 
	                int a1 = (rgb1 >> 24) & 0xff;
	                int r1 = (rgb1 >> 16) & 0xff;
	                int g1 = (rgb1 >> 8) & 0xff;
	                int b1 = rgb1 & 0xff;
	                int a2 = (rgb2 >> 24) & 0xff;
	                int r2 = (rgb2 >> 16) & 0xff;
	                int g2 = (rgb2 >> 8) & 0xff;
	                int b2 = rgb2 & 0xff;
	                int a3 = (rgb3 >> 24) & 0xff;
	                int r3 = (rgb3 >> 16) & 0xff;
	                int g3 = (rgb3 >> 8) & 0xff;
	                int b3 = rgb3 & 0xff;
	                a1 = a2 + (int) ((a1 + a3) * radius);
	                r1 = r2 + (int) ((r1 + r3) * radius);
	                g1 = g2 + (int) ((g1 + g3) * radius);
	                b1 = b2 + (int) ((b1 + b3) * radius);
	                a1 *= f;
	                r1 *= f;
	                g1 *= f;
	                b1 *= f;
	                out[outIndex] = (a1 << 24) | (r1 << 16) | (g1 << 8) | b1;
	                outIndex += height;
	            }
	            out[outIndex] = in[width - 1];
	            inIndex += width;
	        }
	    }
	 
	    public static int clamp(int x, int a, int b) {
	        return (x < a) ? a : (x > b) ? b : x;
	    }
	
	
}
