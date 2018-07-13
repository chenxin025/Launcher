package com.eton.launcher.setting;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import com.android.launcher.R;
import com.android.launcher2.SharedPreferencesUtils;

import java.util.ArrayList;
import java.util.List;

public class EffectAlertActivity extends Activity{
	
	private static int mIndex;
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //存储在sharedpreference特效索引mindex为-1 ~8，对应的position为0~9； 
        /*mIndex = SharedPreferencesUtils.getWorkspaceEffect(this);
        if(mIndex < -1){
        	mIndex = -1; 
        	SharedPreferencesUtils.setWorkspaceEffect(this, mIndex);
        }
        // Display the fragment as the main content.
        final AlertController.AlertParams p = mAlertParams;
        List<String[]> mlist = new ArrayList<String[]>(); 
        mlist.add(getResources().getStringArray(R.array.pref_workspace_transition_effect_entries));
        PickAdapter pickAdapter = new PickAdapter(this,mlist);
        p.mLabelColumn = MediaStore.Audio.Media.TITLE;
        p.mIsSingleChoice = true;
        p.mNegativeButtonText = getString(com.android.internal.R.string.cancel);
        p.mAdapter = pickAdapter;
        if (p.mTitle == null) {
            p.mTitle = getString(R.string.pref_workspace_transition_effect_title);
        }
        setupAlert();*/
    }
	protected static class PickAdapter extends BaseAdapter {
	   private final LayoutInflater mInflater;
	   private final Context mContext;
       private final List<String[]> mItems;
       private ViewHolder holder;
        /**
         * Create an adapter for the given items.
         */
        public PickAdapter(Context context ,List<String[]> items) {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mContext = context;
            mItems = items;
        }
	   
		   
		@Override
		public int getCount() {
			// TODO 自动生成的方法存根
			   return ((String[])mItems.get(0)).length;
		}

		@Override
		public Object getItem(int position) {
			// TODO 自动生成的方法存根
			return  ((String[])mItems.get(0))[position];
		}

		@Override
		public long getItemId(int position) {
			// TODO 自动生成的方法存根
			return position;
		}

		@Override
		  public View getView(int position, View convertView, ViewGroup parent) {
			// TODO 自动生成的方法存根
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.effect_picker, null);
				holder.title = (TextView) convertView.findViewById(R.id.label);
				holder.cbx = (RadioButton) convertView.findViewById(R.id.cbx);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			final int mPosition = position;
			holder.cbx.setChecked(mIndex + 1 == position);
			holder.title.setText(((String[])mItems.get(0))[position]);
			holder.cbx.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					SharedPreferencesUtils.setWorkspaceEffect(mContext, mPosition - 1);
					if (SharedPreferencesUtils.getWorkspaceEffect(mContext) >= -1)
					((Activity) mContext).finish();
				}
			});
			return convertView;
		}
	}
}
final class ViewHolder {
	public TextView title;
	public RadioButton cbx;
}