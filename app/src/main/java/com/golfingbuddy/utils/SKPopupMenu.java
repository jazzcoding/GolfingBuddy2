package com.golfingbuddy.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.golfingbuddy.R;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import bolts.Bolts;

/**
 * Created by kairat on 1/6/15.
 */
public class SKPopupMenu {
    public static class MenuItem {
        private static AtomicInteger idCounter = new AtomicInteger();
        private int mId;
        private String mKey;
        private PRESENTATION mPresentation;
        private int mGroup;
        private Boolean mIsClickable;
        private String mLabel;
        private int mLabelColor;
        private int mIcon;
        private int mDividerColor;
        private View mView;

        public MenuItem(String key) {
            mId = idCounter.getAndIncrement();
            mKey = key;
            mPresentation = PRESENTATION.SIMPLE;
            mIsClickable = true;
        }

        public int getId() {
            return mId;
        }

        public String getKey() {
            return mKey;
        }

        public void setKey(String key) {
            mKey = key;
        }

        public PRESENTATION getPresentation() {
            return mPresentation;
        }

        public void setPresentation(PRESENTATION presentation) {
            this.mPresentation = presentation;
        }

        public int getGroup() {
            return mGroup;
        }

        public void setGroup(int mGroup) {
            this.mGroup = mGroup;
        }

        public Boolean getIsClickable() {
            return mIsClickable;
        }

        public void setIsClickable(Boolean mIsClickable) {
            this.mIsClickable = mIsClickable;
        }

        public String getLabel() {
            return mLabel;
        }

        public void setLabel(String label) {
            this.mLabel = label;
        }

        public int getLabelColor() {
            return mLabelColor;
        }

        public void setLabelColor(int mLabelColor) {
            this.mLabelColor = mLabelColor;
        }

        public int getIcon() {
            return mIcon;
        }

        public void setIcon(int mIcon) {
            this.mIcon = mIcon;
        }

        public int getDividerColor() {
            return mDividerColor;
        }

        public void setDividerColor(int mDividerColor) {
            this.mDividerColor = mDividerColor;
        }

        public View getView() {
            return mView;
        }

        public void setView(View mView) {
            this.mView = mView;
        }
    }

    public interface MenuItemClickListener {
        Boolean onClick(MenuItem menuItem, int position, long id, ArrayList<MenuItem> list);
    }

    public static class MenuItemClickListenerImpl implements MenuItemClickListener {
        public Boolean onClick(MenuItem menuItem, int position, long id, ArrayList<MenuItem> list) {
            return true;
        }
    }

    public static class MenuParams {
        protected Context mContext;
        protected ArrayList<MenuItem> mMenuItems;

        protected int mWidth;
        protected int mHeight;
        protected Boolean mOutsideTouchable;
        protected Boolean mFocusable;
        protected int mBackgroundColor;
        protected View mAnchor;
        protected int mXoff;
        protected int mYoff;
        protected Float mAlpha;
        protected Object mTag;

        public MenuParams(Context context) {
            mContext = context;
            mWidth = mHeight = ViewGroup.LayoutParams.WRAP_CONTENT;
            mOutsideTouchable = mFocusable = true;
            mBackgroundColor = Color.WHITE;
            mTag = mAnchor = null;
            mXoff = mYoff = 0;
            mAlpha = 1f;
        }

        public Context getContext() {
            return mContext;
        }

        public void setContext(Context mContext) {
            this.mContext = mContext;
        }

        public ArrayList<MenuItem> getMenuItems() {
            return mMenuItems;
        }

        public void setMenuItems(ArrayList<MenuItem> mData) {
            mMenuItems = mData;
        }

        public int getWidth() {
            return mWidth;
        }

        public void setWidth(int mWidth) {
            this.mWidth = mWidth;
        }

        public int getHeight() {
            return mHeight;
        }

        public void setHeight(int mHeight) {
            this.mHeight = mHeight;
        }

        public Boolean getOutsideTouchable() {
            return mOutsideTouchable;
        }

        public void setOutsideTouchable(Boolean mOutsideTouchable) {
            this.mOutsideTouchable = mOutsideTouchable;
        }

        public Boolean getFocusable() {
            return mFocusable;
        }

        public void setFocusable(Boolean mFocusable) {
            this.mFocusable = mFocusable;
        }

        public int getBackgroundColor() {
            return mBackgroundColor;
        }

        public void setBackgroundColor(int mBackgroundColor) {
            this.mBackgroundColor = mBackgroundColor;
        }

        public View getAnchor() {
            return mAnchor;
        }

        public void setAnchor(View anchor) {
            mAnchor = anchor;
        }

        public int getXoff() {
            return mXoff;
        }

        public void setXoff(int mXoff) {
            this.mXoff = mXoff;
        }

        public int getYoff() {
            return mYoff;
        }

        public void setYoff(int mYoff) {
            this.mYoff = mYoff;
        }

        public Float getAlpha() {
            return mAlpha;
        }

        public void setAlpha(Float alpha) {
            if ( alpha != null ) {
                this.mAlpha = alpha;
            }
        }

        public Object getTag() {
            return mTag;
        }

        public void setTag(Object tag) {
            mTag = tag;
        }
    }

    public static enum PRESENTATION {
        SIMPLE, DIVIDER
    }

    protected Boolean mIsInit;
    protected MenuParams mParams;
    protected View mView;

    protected PopupWindow mPopupWindow;
    protected ListView mListView;
    protected ListArrayAdapter mListArrayAdapter;
    protected MenuItemClickListener mListener;

    public SKPopupMenu(MenuParams params) {
        mIsInit = false;
        mParams = params;
        mPopupWindow = new PopupWindow(mParams.getContext());
    }

    public PopupWindow getPopupWindow() {
        return mPopupWindow;
    }

    public void setPopupWindow(PopupWindow mPopupWindow) {
        this.mPopupWindow = mPopupWindow;
    }

    public MenuItemClickListener getMenuItemClickListener() {
        return mListener;
    }

    public void setMenuItemClickListener(MenuItemClickListener mListener) {
        this.mListener = mListener;
    }

    public MenuParams getParams() {
        return mParams;
    }

    public void setParams(MenuParams mParams) {
        this.mParams = mParams;
    }

    public void renderMenuItem(MenuItem menuItem) {
        if (menuItem == null || menuItem.getView() == null) {
            return;
        }

        View view = menuItem.getView();

        switch (menuItem.getPresentation()) {
            case SIMPLE:
                if (!menuItem.getIsClickable()) {
                    view.setEnabled(false);
                    view.setOnClickListener(null);
                }

                ((TextView) view.findViewById(R.id.base_popup_window_label)).setText(menuItem.getLabel());

                if (menuItem.getLabelColor() != 0) {
                    ((TextView) view.findViewById(R.id.base_popup_window_label)).setTextColor(menuItem.getLabelColor());
                } else {
                    ((TextView) view.findViewById(R.id.base_popup_window_label)).setTextColor(
                            mParams.getContext().getResources().getColor(R.color.base_popup_window_label_color)
                    );
                }

                ((ImageView) view.findViewById(R.id.base_popup_window_icon)).setImageResource(menuItem.getIcon());
                break;
            case DIVIDER:
            default:
                view.setEnabled(false);
                view.setOnClickListener(null);
                view.findViewById(R.id.base_popup_window_divider).setBackgroundColor(menuItem.getDividerColor());
                break;
        }
    }

    public void show() {
        if (mParams.getAnchor() == null) {
            throw new IllegalArgumentException("Anchor is required attribute.");
        }

        if (mIsInit != true) {
            mView = LayoutInflater.from(mParams.getContext()).inflate(R.layout.base_popup_window, null);
            mView.setBackgroundColor(mParams.getBackgroundColor());
            mView.setAlpha(mParams.getAlpha());

            mPopupWindow.setContentView(mView);
            mPopupWindow.setHeight(mParams.getHeight());
            mPopupWindow.setWidth(mParams.getWidth());
            mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            mPopupWindow.setFocusable(mParams.getFocusable());

            ListView listView = (ListView) mView.findViewById(R.id.base_popup_window_list);
            ListArrayAdapter adapter = new ListArrayAdapter(mParams.getContext(), R.layout.base_popup_window_list_item);
            adapter.addAll(mParams.getMenuItems());
            listView.setAdapter(adapter);

            drawList();
            mIsInit = true;
        }

        mPopupWindow.showAsDropDown(mParams.getAnchor(), mParams.getXoff(), mParams.getYoff());
    }

    public void show(Boolean reInit) {
        if (reInit != null) {
            mIsInit = reInit;
        }

        show();
    }

    protected void drawList() {
        if (mView.getId() != R.id.base_popup_window_context) {
            return;
        }

        mListView = (ListView) mView.findViewById(R.id.base_popup_window_list);
        mListArrayAdapter = new ListArrayAdapter(mParams.getContext(), R.layout.base_popup_window_list_item);
        mListView.setAdapter(mListArrayAdapter);
        mListArrayAdapter.addAll(mParams.getMenuItems());
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if ( mListener != null ) {
                    MenuItem menuItem = mParams.getMenuItems().get(position);
                    menuItem.setView(view);

                    Boolean result = mListener.onClick(menuItem, position, id, mParams.getMenuItems());

                    if ( menuItem.getGroup() != 0 && menuItem.getIcon() != 0 ) {
                        for ( SKPopupMenu.MenuItem menu : mParams.getMenuItems() ) {
                            if ( menu.getGroup() == menuItem.getGroup() && menu.getId() != menuItem.getId() ) {
                                menu.setIcon(0);
                                renderMenuItem(menu);
                            }
                        }
                    }

                    renderMenuItem(menuItem);

                    if ( result == true ) {
                        mPopupWindow.dismiss();
                    } else {
                        mListArrayAdapter.clear();
                        mListArrayAdapter.addAll(mParams.getMenuItems());
                    }
                } else {
                    mPopupWindow.dismiss();
                }
            }
        });
    }

    private class ListArrayAdapter extends ArrayAdapter<MenuItem> {
        public ListArrayAdapter(Context context, int resource) {
            super(context, resource);
        }

        @Override
        public View getView(int position, View row, ViewGroup parent) {
            MenuItem menuItem = getItem(position);
            LayoutInflater inflater = LayoutInflater.from(mParams.getContext());

            switch (menuItem.getPresentation()) {
                case SIMPLE:
                    row = inflater.inflate(R.layout.base_popup_window_list_item, parent, false);
                    break;
                case DIVIDER:
                default:
                    row = inflater.inflate(R.layout.base_popup_window_divider, parent, false);
                    break;
            }

            menuItem.setView(row);
            renderMenuItem(menuItem);

            return row;
        }
    }
}