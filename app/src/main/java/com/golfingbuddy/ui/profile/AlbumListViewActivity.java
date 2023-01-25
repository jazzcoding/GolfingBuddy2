package com.golfingbuddy.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.golfingbuddy.R;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.golfingbuddy.core.SkServiceCallbackListener;
import com.golfingbuddy.ui.base.SkBaseInnerActivity;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class AlbumListViewActivity extends SkBaseInnerActivity{

    protected static int COLUMNS_COUNT = 2;

    GridView gridView;
    ProgressBar progressBar;
    private int columnWidth;
    private JsonArray jsonData;
    private Integer userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.album_list_view_activity);
        setEmulateBackButton(true);

        userId = getIntent().getIntExtra("userId", 1);

        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        gridView = (GridView)findViewById(R.id.gridView);

        initGridLayout();

        baseHelper.getUserAlbumList(userId, new SkServiceCallbackListener() {
            @Override
            public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
                String result = data.getString("data");
                if (result != null) {
                    JsonObject a = new Gson().fromJson(result, JsonObject.class);
                    if (a.has("data")) {
                        JsonObject mData = a.getAsJsonObject("data");
                        jsonData = mData.get("list").getAsJsonArray();

                        gridView.setAdapter(new GridViewAdapter(AlbumListViewActivity.this, R.layout.album_list_view_activity_row));
                        gridView.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                    }
                    else {

                    }
                }
            }
        });

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void initGridLayout() {
        Resources r = getResources();
        float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r.getDisplayMetrics());

        Display display = AlbumListViewActivity.this.getWindowManager().getDefaultDisplay();
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

    class GridViewAdapter extends ArrayAdapter {
        private int layoutResourceId;

        public GridViewAdapter(Context context, int layoutResourceId) {
            super(context, layoutResourceId);
            this.layoutResourceId = layoutResourceId;
        }

        @Override
        public int getCount() {
            return jsonData.size();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View row = convertView;
            ViewHolder holder = null;

            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(layoutResourceId, parent, false);
                row.getLayoutParams().height = columnWidth;
                holder = new ViewHolder();
                holder.image = (ImageView) row.findViewById(R.id.image);
                holder.name = (TextView) row.findViewById(R.id.name);
                holder.count = (TextView) row.findViewById(R.id.count);
                holder.progressBar = (ProgressBar) row.findViewById(R.id.progressBar);
                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            final JsonObject item = jsonData.get(position).getAsJsonObject();
            holder.name.setText(item.get("name").getAsString());
            holder.count.setText(item.get("photoCount").getAsString());
            Picasso.with(getContext()).load(item.get("url").getAsString()).fit().centerInside().into(holder.image);

            final ViewHolder finalHolder = holder;

            Picasso.with(getApplication()).load(item.get("url").getAsString()).into(finalHolder.image, new Callback(){
                @Override
                public void onSuccess() {
                    finalHolder.image.setVisibility(View.VISIBLE);
                    finalHolder.name.setVisibility(View.VISIBLE);
                    finalHolder.count.setVisibility(View.VISIBLE);
                    finalHolder.progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onError() {

                }
            });

            holder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(AlbumListViewActivity.this, AlbumPhotoListViewActivity.class);
                    i.putExtra("albumId", item.get("id").getAsInt());
                    i.putExtra("name", item.get("name").getAsString());
                    i.putExtra("userId", userId);
                    AlbumListViewActivity.this.startActivity(i);
                }
            });
            return row;
        }

        class ViewHolder {
            ImageView image;
            TextView name;
            TextView count;
            ProgressBar progressBar;
        }
    }
}