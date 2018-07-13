package com.android.launcher2;

import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * added by chenxin
 * ҳ��������
 */
public class ViewPagerAdapter extends PagerAdapter {


	private Context mContext;
	
	
	// ���Դ,������JSONARRAY
	private JSONArray mJsonArray = null;

    public ArrayList<AppWidgetProviderInfo> mWidgetArray =null;
	
	private int mTotal;
	
	public static ViewPager mPagedViewEditScreen;
	
	//Hashmap����ҳ��λ���Լ�ItemView.
	private HashMap<Integer, ViewPagerItemView> mHashMap;
 
        private int mLoadingType = 0;
	
	public ViewPagerAdapter(Context context,JSONArray arrays,int totalPages) {
		mPagedViewEditScreen = null;
		this.mContext = context;
		this.mJsonArray = arrays;
		this.mTotal = totalPages;
		mHashMap = new HashMap<Integer, ViewPagerItemView>();
	}

        public ViewPagerAdapter(Context context,ArrayList<AppWidgetProviderInfo> arrays,int totalPages){
        mPagedViewEditScreen = null;
		this.mContext = context;
		this.mWidgetArray = arrays;
		this.mTotal = totalPages;
		mHashMap = new HashMap<Integer, ViewPagerItemView>();
	}
	
        public void setLoadingType(int type){
		this.mLoadingType = type;
	}
	
	private int getLoadingType(){
		return this.mLoadingType;
	}

	//������л��գ����������һ�����ʱ�򣬻�����ڵ�ͼƬ���յ�.
	@Override
	public void destroyItem(View container, int position, Object object) {
		if (ScreenEditUtil.editMenuChoose == ScreenEditUtil.MENU_ID_WIDGET
				|| ScreenEditUtil.editMenuChoose == ScreenEditUtil.MENU_ID_WALLPAPER){
			return;
		}
		
		//M: Not need to recycle bitmap
		//ViewPagerItemView itemView = (ViewPagerItemView)object;
		//itemView.recycle();
	}
	
	@Override
	public void finishUpdate(View view) {

	}

	//�����ܵ�ҳ��
	@Override
	public int getCount() {
//		return mJsonArray.length();
		return mTotal;
	}

	//��ʼ��ViewPagerItemView.���ViewPagerItemView�Ѿ�����,
	//����reload  ������newһ������������.
	@Override
	public Object instantiateItem(View container, int position) {	
		ViewPagerItemView itemView;
		if(mHashMap.containsKey(position)){
			itemView = mHashMap.get(position);
			itemView.reload(mJsonArray,position);
		}else{
			itemView = new ViewPagerItemView(mContext);
			try {
                                if (null != mJsonArray){
				JSONObject dataObj = (JSONObject) mJsonArray.get(position);
				//itemView.setData(dataObj,position);
				itemView.setData(mJsonArray, position, getLoadingType(), mJsonArray.length(),mWidgetArray);
                                }else{
                                    if (null != mWidgetArray)
                                    itemView.setData(null, position, 3, mWidgetArray.size(),mWidgetArray);
                                }
			} catch (JSONException e) {
				e.printStackTrace();
			}
			mHashMap.put(position, itemView);
			((ViewPager) container).addView(itemView);
			mPagedViewEditScreen = (ViewPager)container;
		}
		
		return itemView;
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}

	@Override
	public void restoreState(Parcelable arg0, ClassLoader arg1) {
		
	}

	@Override
	public Parcelable saveState() {
		return null;
	}

	@Override
	public void startUpdate(View view) {

	}
	
}
