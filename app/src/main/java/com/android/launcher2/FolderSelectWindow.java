package com.android.launcher2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.launcher.R;

import java.util.List;

public class FolderSelectWindow extends PopupWindow {

	public static final int ITEM_WIDTH = 130;
	public static final int ITEM_HEIGHT = 40;

	private List<FolderInfo> mFolderList;
	private Context mContext;
	private ListView mListView;

	private LayoutInflater mInflater;
	LinearLayout mLayout;

	private float mDensity;

	private int mIndex;

	private AppsArrangeView mAppsArrangeView;

	public FolderSelectWindow(Context c) {
		super(c);
		mContext = c;
		mInflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mLayout = (LinearLayout) mInflater.inflate(
				R.layout.folder_select_dialog, null);
		setContentView(mLayout);

		mDensity = mContext.getResources().getDisplayMetrics().density;

		setFocusable(true); // 设置PopupWindow可获得焦�?
		setTouchable(true); // 设置PopupWindow可触�?
		setOutsideTouchable(true); // 设置非PopupWindow区域可触�?
	}

	public FolderSelectWindow(Context c, AppsArrangeView aav,
			List<FolderInfo> folders) {
		this(c);

		mAppsArrangeView = aav;
		mFolderList = addDesktopToFolderList(folders);
		mListView = (ListView) (mLayout.findViewById(R.id.folder_list));
		mListView.setAdapter(new FolderAdapter());
		setWinSize();

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position != mFolderList.size() - 1) {
					long cid = mFolderList.get(position).id;
					if (!mAppsArrangeView.isFull(cid)
							|| cid == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
						// mAppsArrangeView.setContainer(mIndex, cid);
						mAppsArrangeView.moveApplication(mIndex, cid);
					} else {
						Toast.makeText(
								mContext,
								mContext.getResources().getString(
										R.string.folder_full_tip),
								Toast.LENGTH_SHORT).show();
					}
				} else {
					/*
					 * Launcher launcher = (Launcher)mContext; int[] cell =
					 * launcher.getVacantCell(); CellLayout cellLayout =
					 * (CellLayout)launcher.getWorkspace().getChildAt(cell[2]);
					 * launcher.addFolder(cellLayout,
					 * LauncherSettings.Favorites.CONTAINER_DESKTOP, cell[2],
					 * cell[0], cell[1]);
					 */
					final Launcher launcher = (Launcher) mContext;
					final int[] cell = launcher.getVacantCell();
					final CellLayout cellLayout = (CellLayout) launcher
							.getWorkspace().getChildAt(cell[2]);

					final EditText et = new EditText(mContext);
					et.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
							8) });

					new AlertDialog.Builder(mContext)
							.setTitle(
									mContext.getString(R.string.str_please_input))
							.setIcon(android.R.drawable.ic_dialog_info)
							.setView(et)
							.setPositiveButton(
									mContext.getString(R.string.str_confirm),
									new OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											String title = et.getText()
													.toString();

											if (title.equals("")) {
												title = mContext
														.getResources()
														.getString(
																R.string.folder_name);
											}

											FolderIcon fi = launcher
													.addFolder(
															title,
															cellLayout,
															LauncherSettings.Favorites.CONTAINER_DESKTOP,
															cell[2], cell[0],
															cell[1]);
										}
									})
							.setNegativeButton(
									mContext.getString(R.string.str_cancel),
									null).show();

				}
				FolderSelectWindow.this.dismiss();
			}
		});
	}

	public void setIndex(int i) {
		mIndex = i;
	}

	// set the size of window
	private void setWinSize() {
		int width = (int) (mDensity * ITEM_WIDTH);
		int height = (int) (mDensity * ITEM_HEIGHT) * mFolderList.size();
		setWidth(width);
		setHeight(height + 15);
	}

	// besides the FolderInfo map, we also add desktop and "create new folder"
	// to list
	private List<FolderInfo> addDesktopToFolderList(List<FolderInfo> list) {
		// List<FolderInfo> list = getFolderListFromMap(map);

		// add desktop folder to list
		FolderInfo desktopFolder = new FolderInfo();
		desktopFolder.setTitle(mContext.getResources().getString(
				R.string.container_desktop));
		desktopFolder.id = LauncherSettings.Favorites.CONTAINER_DESKTOP;
		list.add(0, desktopFolder);

		// add button of create folder to list
		FolderInfo addFolder = new FolderInfo();
		addFolder.setTitle(mContext.getResources().getString(
				R.string.add_folder));
		addFolder.id = -1;
		list.add(list.size(), addFolder);

		return list;
	}

	class FolderAdapter extends BaseAdapter {

		private LayoutInflater inflater;

		public FolderAdapter() {
			inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return mFolderList.size();
		}

		@Override
		public Object getItem(int arg0) {
			return mFolderList.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			LinearLayout layout = new LinearLayout(mContext);
			layout.setOrientation(LinearLayout.VERTICAL);

			TextView tv = new TextView(mContext);
			tv.setTextColor(Color.BLACK);
			tv.setText(mFolderList.get(arg0).getTitle() != null ? mFolderList
					.get(arg0).getTitle() : mContext.getResources().getString(
					R.string.default_folder_name));
			tv.setGravity(Gravity.CENTER);
			tv.setWidth((int) (ITEM_WIDTH * mDensity));
			tv.setHeight((int) (ITEM_HEIGHT * mDensity));

			layout.addView(tv);
			return layout;
		}
	}
}
