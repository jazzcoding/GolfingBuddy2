package com.golfingbuddy.ui.profile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.golfingbuddy.R;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.golfingbuddy.core.SkServiceCallbackListener;
import com.golfingbuddy.model.base.BaseRestCommand;
import com.golfingbuddy.model.base.BaseServiceHelper;
import com.golfingbuddy.ui.base.SkBaseInnerActivity;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class PhotoManageFragment extends Fragment {

    private ArrayList<Integer> itemsToDelete = new ArrayList<>();
    private PhotoViewFragment.ENTITY_TYPE entityType;
    private int entityId;
    private ArrayList<PhotoItem> photoList = new ArrayList();
    private BaseServiceHelper helper;
    private int columnWidth;
    GridView gridView;
    private GridViewAdapter mAdapter;
    private SkBaseInnerActivity activity;
    protected static int COLUMNS_COUNT = 3;

    public static PhotoManageFragment newInstance(PhotoViewFragment.ENTITY_TYPE type, int entityId) {
        PhotoManageFragment fr = new PhotoManageFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("entityType", type);
        bundle.putInt("entityId", entityId);
        fr.setArguments(bundle);
        return fr;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = (SkBaseInnerActivity) getActivity();
        Bundle bundle = getArguments();
        entityType = (PhotoViewFragment.ENTITY_TYPE) bundle.getSerializable("entityType");
        entityId = bundle.getInt("entityId");
        helper = activity.getBaseHelper();
        setHasOptionsMenu(true);
        activity.setActionBarCheckCounterEnable(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View fragmentView = inflater.inflate(R.layout.photomanage_fragment_view, container, false);
        gridView = (GridView) fragmentView.findViewById(R.id.gridView);
        initGridLayout();
        //adapter = new GridViewAdapter(getActivity(), R.layout.photomanage_fragment_view_row, photoList);
        final ProgressBar bar = (ProgressBar) fragmentView.findViewById(R.id.progressBar);


        final SkServiceCallbackListener listener = new SkServiceCallbackListener() {
            @Override
            public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
                String result = data.getString("data");

                if (result != null) {
                    JsonObject a = new Gson().fromJson(result, JsonObject.class);
                    if (a.has("data")) {
                        a = a.getAsJsonObject("data");
                        JsonArray list = a.getAsJsonArray("list");
                        photoList = new ArrayList<>();
                        JsonObject currentObject;
                        PhotoItem item;

                        for (int i = 0; i < list.size(); i++) {
                            currentObject = list.get(i).getAsJsonObject();

                            if (currentObject != null && currentObject.has("thumbUrl")) {
                                item = new PhotoItem();
                                item.setUrl(currentObject.get("thumbUrl").getAsString());
                                item.setId(currentObject.get("id").getAsInt());
                                if ( currentObject.has("approved") && currentObject.get("approved") != null )
                                {
                                    String approved = currentObject.get("approved").getAsString();
                                    if ( approved != null )
                                    {
                                        item.setApproved(Boolean.parseBoolean(approved));
                                    }
                                }

                                photoList.add(item);
                            }
                        }
                    }
                }

                mAdapter = new GridViewAdapter(getActivity(), R.layout.photomanage_fragment_view_row, photoList);
                gridView.setAdapter(mAdapter);
                gridView.setVisibility(View.VISIBLE);
                bar.setVisibility(View.GONE);
            }
        };

        switch (entityType) {
            case USER:
                helper.getUserPhotoList(entityId, listener);
                break;

            case ALBUM:
                helper.getAlbumPhotoList(entityId, listener);
                break;
        }

        //ProgressDialog pd = ProgressDialog.show(getActivity(), "", "Loading. Please wait...", true);


        return fragmentView;
    }

    private void initGridLayout() {
        Resources r = getResources();
        float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r.getDisplayMetrics());

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        columnWidth = (int) ((size.x - ((COLUMNS_COUNT + 1) * padding)) / COLUMNS_COUNT);

        gridView.setNumColumns(COLUMNS_COUNT);
        gridView.setColumnWidth(columnWidth);
        gridView.setStretchMode(GridView.NO_STRETCH);
        gridView.setPadding((int) padding, (int) padding, (int) padding, (int) padding);
        gridView.setHorizontalSpacing((int) padding);
        gridView.setVerticalSpacing((int) padding);
    }

    private void deletePhotos() {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.photomanage_delete_confirm_title)
                .setMessage(R.string.photomanage_delete_confirm_msg)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        ArrayList<PhotoItem> tmp = new ArrayList<PhotoItem>();
                        for( PhotoItem item:photoList )
                        {
                            if (itemsToDelete.contains(item.getId())) {
                                tmp.add(item);
                            }
                        }
                        photoList.removeAll(tmp);

                        HashMap<String, String> params = new HashMap<>();
                        params.put("idList", TextUtils.join(",", itemsToDelete));
                        helper.runRestRequest(BaseRestCommand.ACTION_TYPE.PHOTO_DELETE, params);

                        mAdapter.notifyDataSetChanged();

                        //gridView.setAdapter(new GridViewAdapter(getActivity(), R.layout.photomanage_fragment_view_row, photoList));

                        //notify parent activity about list changes
                        Intent intent = new Intent("photo.manage_list_delete");
                        intent.putIntegerArrayListExtra("idList", itemsToDelete);
                        LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);

                        itemsToDelete = new ArrayList<Integer>();
                        activity.setCheckedItemsCount(itemsToDelete.size());
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.photomanage_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.delete_btn) {
            if (itemsToDelete.size() == 0) {
                Toast.makeText(activity, getResources().getString(R.string.photomanage_delete_empty_list_msg), Toast.LENGTH_LONG).show();
            } else {
                deletePhotos();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    class GridViewAdapter extends ArrayAdapter {
        private Context context;
        private int layoutResourceId;
        public ArrayList<PhotoItem> data = new ArrayList();

        public GridViewAdapter(Context context, int layoutResourceId, ArrayList<PhotoItem> data) {
            super(context, layoutResourceId, data);
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            this.data = data;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View row = convertView;
            ViewHolder holder = null;

            if (row == null) {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                row = inflater.inflate(layoutResourceId, parent, false);
                row.getLayoutParams().height = columnWidth;
                holder = new ViewHolder();
                holder.image = (ImageView) row.findViewById(R.id.image);
                holder.overlay = (TextView) row.findViewById(R.id.overlay);
                holder.check = (ImageView) row.findViewById(R.id.check);
                holder.pending = (ImageView) row.findViewById(R.id.pending_iamge);
                holder.progressBar = (ProgressBar) row.findViewById(R.id.progressBar);

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            final PhotoItem item = data.get(position);

            if (!itemsToDelete.contains(item.getId())) {
                holder.overlay.setVisibility(View.INVISIBLE);
                holder.check.setVisibility(View.INVISIBLE);

            } else {
                holder.overlay.setVisibility(View.VISIBLE);
                holder.check.setVisibility(View.VISIBLE);
            }

            String imageUrl = item.getUrl();
            Picasso.with(getContext()).load(imageUrl).fit().into(holder.image);
            final ViewHolder finalHolder = holder;

            Picasso.with(getContext()).load(imageUrl).into(finalHolder.image, new Callback(){
                @Override
                public void onSuccess() {
                    finalHolder.image.setVisibility(View.VISIBLE);
                    finalHolder.progressBar.setVisibility(View.GONE);

                    Boolean isApproved = item.isApproved();

                    if ( isApproved != null && !isApproved ) {
                        finalHolder.pending.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onError() {

                }
            });

            holder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemsToDelete.contains(item.getId())) {
                        itemsToDelete.remove(new Integer(item.getId()));
                        finalHolder.overlay.setVisibility(View.INVISIBLE);
                        finalHolder.check.setVisibility(View.INVISIBLE);
                    } else {
                        itemsToDelete.add(item.getId());
                        finalHolder.overlay.setVisibility(View.VISIBLE);
                        finalHolder.check.setVisibility(View.VISIBLE);
                    }

                    activity.setCheckedItemsCount(itemsToDelete.size());
                }
            });
            return row;
        }

        class ViewHolder {
            ImageView image;
            TextView overlay;
            ImageView check;
            ImageView pending;
            ProgressBar progressBar;
        }
    }

    class PhotoItem {
        private String url;
        private Integer id;
        private Boolean isApproved;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public Boolean isApproved() {
            return this.isApproved;
        }

        public void setApproved(Boolean approved) {
            this.isApproved = approved;
        }

    }

}
