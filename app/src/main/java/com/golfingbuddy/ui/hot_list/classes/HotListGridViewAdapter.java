package com.golfingbuddy.ui.hot_list.classes;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.golfingbuddy.R;
import com.golfingbuddy.ui.profile.ProfileViewActivity;
import com.golfingbuddy.utils.SKImageView;

import java.util.ArrayList;

/**
 * Created by kairat on 2/5/15.
 */
public class HotListGridViewAdapter extends ArrayAdapter {
    private ArrayList<HotListItem> mList;

    public HotListGridViewAdapter(Context context) {
        super(context, R.layout.hotlist_item);
        mList = new ArrayList<>();
    }

    public HotListGridViewAdapter(Context context, ArrayList<HotListItem> list) throws HotListAdapterException {
        super(context, R.layout.hotlist_item);

        if ( list == null || list.isEmpty() ) {
            throw new HotListAdapterException();
        }

        mList = list;
        addAll(mList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HotListItem item = (HotListItem) getItem(position);
        View hotItem;

        if ( convertView == null ) {
            LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            hotItem = inflater.inflate(R.layout.hotlist_item, null);
        } else {
            hotItem = convertView;
        }

        SKImageView hotItemAvatar = (SKImageView) hotItem.findViewById(R.id.hotlist_avatar);
        hotItemAvatar.setImageUrl(item.getAvatar());
        hotItem.setTag(item);

        hotItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HotListItem item = (HotListItem) v.getTag();
                Intent intent = new Intent(getContext(), ProfileViewActivity.class);
                intent.putExtra("userId", item.getUserId());
                getContext().startActivity(intent);
            }
        });

        return hotItem;
    }

    public ArrayList<HotListItem> getList() {
        return mList;
    }

    public void addItem(HotListItem item) {
        mList.add(0, item);
        clear();
        addAll(mList);
    }

    public void removeItem(int userId) throws HotListAdapterException {
        for ( HotListItem item : mList ) {
            if ( item.getUserId() == userId ) {
                mList.remove(item);

                break;
            }
        }

        if ( mList.isEmpty() ) {
            throw new HotListAdapterException();
        }

        clear();
        addAll(mList);
    }

    public class HotListAdapterException extends Exception { }
}
