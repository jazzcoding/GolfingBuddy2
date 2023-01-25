package com.golfingbuddy.ui.profile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.golfingbuddy.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.golfingbuddy.core.SkApplication;
import com.golfingbuddy.core.SkRestInterface;
import com.golfingbuddy.core.SkServiceCallbackListener;
import com.golfingbuddy.model.base.BaseServiceHelper;
import com.golfingbuddy.ui.base.SkBaseInnerActivity;
import com.golfingbuddy.ui.base.classes.ExpandableHeightGridView;
import com.golfingbuddy.ui.memberships.SubscribeOrBuyActivity;
import com.golfingbuddy.ui.search.classes.AuthObject;
import com.golfingbuddy.utils.SkApi;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import retrofit.mime.TypedFile;
import retrofit.mime.TypedString;


/**
 * Created by sardar on 10/7/14.
 */
public class PhotoViewFragment extends Fragment {

    int SUBSCRIBE_ACTIVITY_RESULT_CODE = 2341;

    public enum ENTITY_TYPE {
        USER,
        ALBUM
    }

    private ENTITY_TYPE entityType;
    private int entityId;
    private ArrayList<PhotoItem> photoList = new ArrayList();
    private BaseServiceHelper helper;
    private int columnWidth;
    private ExpandableHeightGridView gridView;
    private SkBaseInnerActivity activity;
    private Boolean photoNA = false;
    private Dialog progressDialog;
    private ProgressBar pdProgressBar;
    private TextView pdText;
    private Button viewAlbums;
    private LinearLayout viewAlbumsParent;
    private Boolean viewAlbumsParentShow = false;
    private ProgressBar progressBar;
    private LinearLayout noPhotoLayout;
    private LinearLayout ownerNoPhotoLayout;
    private LinearLayout noPremissionLayout;
    private LinearLayout subscribeLayout;
    private PhotoAuth auth;

    private Boolean isOwner;
    private String authPhotoUpload;
    private String authPhotoUploadMessage;
    private Boolean authPhotoView;

    private View fragmentView;

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<Integer> deletedItems = intent.getIntegerArrayListExtra("idList");

            ArrayList<PhotoItem> tmp = new ArrayList<PhotoItem>();
            for( PhotoItem item:photoList )
            {
                if (deletedItems.contains(item.getId())) {
                    tmp.add(item);
                }
            }
            photoList.removeAll(tmp);

            if ( photoList == null || photoList.size() == 0 )
            {
                showEmptyPage();
            }
            else
            {
                hideEmptyPage();
            }

            gridView.setAdapter(new GridViewAdapter(getActivity(), R.layout.photoview_fragment_view_row, photoList));
            updateActionBar();
        }
    };

    protected static int COLUMNS_COUNT = 3;

    public static PhotoViewFragment newInstance(ENTITY_TYPE type, int entityId, Boolean isOwner, String authPhotoUpload, Boolean authPhotoView) {
        PhotoViewFragment fr = new PhotoViewFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("entityType", type);
        bundle.putInt("entityId", entityId);
        bundle.putBoolean("isOwner", isOwner);
        bundle.putString("authPhotoUpload", authPhotoUpload);
        bundle.putBoolean("authPhotoView", authPhotoView);
        fr.setArguments(bundle);
        return fr;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        entityType = (ENTITY_TYPE) bundle.getSerializable("entityType");
        entityId = bundle.getInt("entityId");
        isOwner = bundle.getBoolean("isOwner");
        authPhotoUpload = bundle.getString("authPhotoUpload");
        authPhotoView = bundle.getBoolean("authPhotoView");
        activity = (SkBaseInnerActivity) getActivity();
        helper = activity.getBaseHelper();

        // check status list
        if (!activity.getApp().isPluginActive(SkApplication.PLUGINS.PHOTO)) {
            photoNA = true;
            //return;
        }

        setHasOptionsMenu(true);
        LocalBroadcastManager.getInstance(activity).registerReceiver(mMessageReceiver, new IntentFilter("photo.manage_list_delete"));

        // progress dialog
        progressDialog = new Dialog(activity);
        progressDialog.setContentView(R.layout.photoupload_dialog_view);
        progressDialog.setTitle(R.string.photoupload_dialog_title);
        progressDialog.setCancelable(false);
        pdProgressBar = (ProgressBar) progressDialog.findViewById(R.id.progressBar);
        pdText = (TextView) progressDialog.findViewById(R.id.counter);
        pdProgressBar.setMax(100);
    }

    public class PhotoAuth {
        public AuthObject photo_view;
        public AuthObject photo_upload;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.photoview_fragment_view, container, false);

        reloadData(fragmentView);

        return fragmentView;
    }

    protected void reloadData( View fragmentView ) {
        gridView = (ExpandableHeightGridView) fragmentView.findViewById(R.id.gridView);
        gridView.setExpanded(true);


        viewAlbumsParent = (LinearLayout) fragmentView.findViewById(R.id.view_albums_parent);

        if (entityType == ENTITY_TYPE.USER) {
            viewAlbums = (Button) fragmentView.findViewById(R.id.view_albums);
            viewAlbumsParentShow = true;
            viewAlbums.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent in = new Intent(activity, AlbumListViewActivity.class);
                    in.putExtra("userId", entityId);
                    activity.startActivity(in);
                }
            });
        }

        noPhotoLayout = (LinearLayout) fragmentView.findViewById(R.id.no_photo);
        ownerNoPhotoLayout = (LinearLayout) fragmentView.findViewById(R.id.owner_no_photo);
        noPremissionLayout = (LinearLayout) fragmentView.findViewById(R.id.permissions);
        subscribeLayout = (LinearLayout) fragmentView.findViewById(R.id.subscribe);

        ImageView subscribeImg = (ImageView) subscribeLayout.findViewById(R.id.subscribe_img);
        subscribeImg.setClickable(true);
        subscribeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SubscribeOrBuyActivity.class);
                intent.putExtra("pluginKey", "photo");
                intent.putExtra("actionKey", "view");

                startActivityForResult(intent, SUBSCRIBE_ACTIVITY_RESULT_CODE);
            }
        });

        progressBar = (ProgressBar) fragmentView.findViewById(R.id.progressBar);

        if (photoNA) {
            (fragmentView.findViewById(R.id.permissions)).setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            return;
        }

        initGridLayout();

        SkServiceCallbackListener listener = new SkServiceCallbackListener() {
            @Override
            public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
                if (SkApi.checkResult(data) == SkApi.API_RESULT.SUCCESS) {
                    JsonObject a = SkApi.processResult(data);
                    if (a != null) {
                        JsonArray list = a.getAsJsonArray("list");
                        photoList = new ArrayList<>();
                        JsonObject currentObject;
                        PhotoItem photo;

                        for (int i = 0; i < list.size(); i++) {
                            currentObject = list.get(i).getAsJsonObject();

                            if (currentObject != null && currentObject.has("thumbUrl")) {
                                photo = new PhotoItem();
                                photo.setId(currentObject.get("id").getAsInt());
                                photo.setUrl(currentObject.get("thumbUrl").getAsString());
                                photo.setIsApproved(currentObject.get("approved").getAsBoolean());
                                photoList.add(photo);
                            }
                        }

                        if( a.has("auth") )
                        {
                            Gson g = new GsonBuilder().create();
                            auth = g.fromJson(a.getAsJsonObject("auth"), PhotoAuth.class);
                        }
                    }
                }
                progressBar.setVisibility(View.GONE);
                gridView.setAdapter(new GridViewAdapter(getActivity(), R.layout.photoview_fragment_view_row, photoList));

                if ( auth != null  )
                {
                    if( auth.photo_view != null )
                    {
                        if ( !(AuthObject.STATUS_AVAILABLE.equals(auth.photo_view.status)) && !isOwner ) {

                            if (  AuthObject.STATUS_PROMOTED.equals(auth.photo_view.status) ) {
                                showPromotionPage(auth.photo_view.msg);
                            }
                            else {
                                showPermissionPage(auth.photo_view.msg);
                            }

                            return;
                        }
                    }

                    if( auth.photo_upload != null )
                    {
                        if ( isOwner ) {

                            if (  AuthObject.STATUS_PROMOTED.equals(auth.photo_upload.status) ) {
                                authPhotoUpload = auth.photo_upload.status;
                            }
                            else if ( AuthObject.STATUS_AVAILABLE.equals(auth.photo_upload.status) ) {
                                authPhotoUpload = auth.photo_upload.status;
                            }
                            else
                            {
                                authPhotoUpload = AuthObject.STATUS_DISABLED;
                                if ( getActivity() != null ) {
                                    View menu = getActivity().findViewById(R.id.add_btn);
                                    if ( menu != null) {
                                        getActivity().findViewById(R.id.add_btn).setVisibility(View.GONE);
                                    }
                                }
                            }

                            authPhotoUploadMessage = auth.photo_upload.msg;
                        }
                    }
                }

                if (photoList.size() > 0) {
                    showGridView();
                    if (viewAlbumsParentShow) {
                        viewAlbumsParent.setVisibility(View.VISIBLE);
                    }
                } else {
                    showEmptyPage();
                }

                updateActionBar();
            }
        };

        switch (entityType) {
            case USER:
                progressBar.setVisibility(View.VISIBLE);
                helper.getUserPhotoList(entityId, listener);
                break;

            case ALBUM:
                progressBar.setVisibility(View.VISIBLE);
                helper.getAlbumPhotoList(entityId, listener);
                break;
        }
    }
    private void showGridView()
    {
        gridView.setVisibility(View.VISIBLE);
        viewAlbumsParent.setVisibility(View.GONE);
        noPhotoLayout.setVisibility(View.GONE);
        noPremissionLayout.setVisibility(View.GONE);
        ownerNoPhotoLayout.setVisibility(View.GONE);
        subscribeLayout.setVisibility(View.GONE);

        progressBar.setVisibility(View.GONE);
    }

    private void showProgressbar()
    {
        gridView.setVisibility(View.GONE);
        viewAlbumsParent.setVisibility(View.GONE);
        noPhotoLayout.setVisibility(View.GONE);
        noPremissionLayout.setVisibility(View.GONE);
        ownerNoPhotoLayout.setVisibility(View.GONE);
        subscribeLayout.setVisibility(View.GONE);

        progressBar.setVisibility(View.VISIBLE);
    }

    private void showPromotionPage(String message)
    {
        gridView.setVisibility(View.GONE);
        viewAlbumsParent.setVisibility(View.GONE);
        noPhotoLayout.setVisibility(View.GONE);
        noPremissionLayout.setVisibility(View.GONE);
        ownerNoPhotoLayout.setVisibility(View.GONE);
        subscribeLayout.setVisibility(View.VISIBLE);

        if ( message != null && !message.isEmpty() ) {
            TextView subscribeText = (TextView) subscribeLayout.findViewById(R.id.subscribe_text);
            subscribeText.setText(message);
        }

        progressBar.setVisibility(View.GONE);
    }

    private void showPermissionPage(String message)
    {
        gridView.setVisibility(View.GONE);
        viewAlbumsParent.setVisibility(View.GONE);
        noPhotoLayout.setVisibility(View.GONE);
        noPremissionLayout.setVisibility(View.VISIBLE);
        ownerNoPhotoLayout.setVisibility(View.GONE);
        subscribeLayout.setVisibility(View.GONE);

        if ( message != null && !message.isEmpty() ) {
            TextView textView = (TextView) noPremissionLayout.findViewById(R.id.permissions_text);
            textView.setText(message);
        }

        progressBar.setVisibility(View.GONE);
    }

    private void showEmptyPage()
    {
        gridView.setVisibility(View.GONE);
        viewAlbumsParent.setVisibility(View.GONE);
        noPremissionLayout.setVisibility(View.GONE);
        subscribeLayout.setVisibility(View.GONE);

        noPhotoLayout.setVisibility(View.VISIBLE);
        ownerNoPhotoLayout.setVisibility(View.GONE);

        if ( isOwner ) {
            noPhotoLayout.setVisibility(View.GONE);
            ownerNoPhotoLayout.setVisibility(View.VISIBLE);
        }

        progressBar.setVisibility(View.GONE);
    }

    private void hideEmptyPage()
    {
        gridView.setVisibility(View.VISIBLE);
        if (viewAlbumsParentShow) {
            viewAlbumsParent.setVisibility(View.VISIBLE);
        }
        noPhotoLayout.setVisibility(View.GONE);
        ownerNoPhotoLayout.setVisibility(View.GONE);
        noPremissionLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        subscribeLayout.setVisibility(View.GONE);
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

        viewAlbumsParent.setPadding((int) padding, 0, (int) padding, (int) padding);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menuInflater = inflater;
        this.menu = menu;
        if (!photoNA && isOwner) {
            inflater.inflate(R.menu.photo_list_view_fragment, menu);

            if ( !AuthObject.STATUS_PROMOTED.equals(authPhotoUpload) && !AuthObject.STATUS_AVAILABLE.equals(authPhotoUpload) ) {
                menu.findItem(R.id.add_btn).setVisible(false);
            }
        }

        super.onCreateOptionsMenu(menu, inflater);
        updateActionBar();
    }

    private MenuInflater menuInflater;
    private Menu menu;

    private void updateActionBar(){
        if( gridView.getAdapter() != null && menu != null && menuInflater != null && menu.findItem(R.id.del_btn) != null ){
            menu.findItem(R.id.del_btn).setVisible(gridView.getAdapter().getCount() != 0);
            getActivity().invalidateOptionsMenu();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!photoNA && isOwner) {
            if (item.getItemId() == R.id.add_btn) {
                if ( AuthObject.STATUS_PROMOTED.equals(authPhotoUpload) && activity != null ) {
                    Intent intent = new Intent(activity, SubscribePhotoAdd.class);
                    intent.putExtra("pluginKey", "photo");
                    intent.putExtra("actionKey", "upload");

                    if ( authPhotoUploadMessage != null && !authPhotoUploadMessage.isEmpty() ) {
                        intent.putExtra("message", authPhotoUploadMessage);
                    }

                    startActivityForResult(intent, SUBSCRIBE_ACTIVITY_RESULT_CODE);
                }

                if ( AuthObject.STATUS_AVAILABLE.equals(authPhotoUpload) && activity != null ) {
                    startDialog();
                }
            }

            if (item.getItemId() == R.id.del_btn) {
                Intent i = new Intent(activity, PhotoListManageActivity.class);
                i.putExtra("entityType", entityType);
                i.putExtra("entityId", entityId);
                activity.startActivity(i);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        if (!photoNA && isOwner) {
            LocalBroadcastManager.getInstance(activity).unregisterReceiver(mMessageReceiver);
        }
        super.onDestroyView();
    }

    class GridViewAdapter extends ArrayAdapter {
        private Context context;
        private int layoutResourceId;
        private ArrayList data = new ArrayList();

        public GridViewAdapter(Context context, int layoutResourceId, ArrayList data) {
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
                holder.pending = (ImageView) row.findViewById(R.id.pending_iamge);
                holder.progressBar = (ProgressBar) row.findViewById(R.id.progressBar);

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            String imageUrl = ((PhotoItem) data.get(position)).getUrl();
            final ViewHolder finalHolder = holder;

            Picasso.with(getContext()).load(imageUrl).into(holder.image, new Callback() {
                @Override
                public void onSuccess() {
                    finalHolder.image.setVisibility(View.VISIBLE);
                    finalHolder.progressBar.setVisibility(View.GONE);

                    Boolean isApproved = ((PhotoItem)data.get(position)).getIsApproved();

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
                    Intent i = new Intent(getActivity(), PhotoViewActivity.class);
                    i.putExtra("position", position);
                    i.putExtra("type", entityType);
                    i.putExtra("entityId", entityId);
                    activity.startActivity(i);
                }
            });
            return row;
        }

        class ViewHolder {
            ImageView image;
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

        public Boolean getIsApproved() {
            return isApproved;
        }

        public void setIsApproved(Boolean isApproved) {
            this.isApproved = (isApproved != null && isApproved);
        }
    }


    protected static final int CAMERA_REQUEST = 0;
    protected static final int GALLERY_PICTURE = 1;
    private Intent pictureActionIntent = null;
    Uri imageUri;
    //Bitmap bitmap;
    //String selectedImagePath;

    private void startDialog() {
        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(activity);
        myAlertDialog.setTitle(getResources().getString(R.string.photoupload_upload_options));
        myAlertDialog.setMessage(getResources().getString(R.string.photoupload_options_qst));

        myAlertDialog.setPositiveButton(getResources().getString(R.string.photoupload_gallery_label),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        pictureActionIntent = new Intent(Intent.ACTION_GET_CONTENT, null);
                        pictureActionIntent.setType("image/*");
                        pictureActionIntent.putExtra("return-data", true);
                        startActivityForResult(pictureActionIntent, GALLERY_PICTURE);
                    }
                });

        myAlertDialog.setNegativeButton(getResources().getString(R.string.photoupload_camera_label),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        imageUri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
                        pictureActionIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        pictureActionIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                        startActivityForResult(pictureActionIntent, CAMERA_REQUEST);
                    }
                });
        myAlertDialog.show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String resultFilePath = null;
        Bitmap resultBitmap = null;

        if ( requestCode == SUBSCRIBE_ACTIVITY_RESULT_CODE )
        {
            this.showProgressbar();
            this.reloadData(fragmentView);
        }

        if (requestCode == GALLERY_PICTURE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    Cursor cursor = activity.getContentResolver().query(data.getData(), null, null, null, null);
                    if (cursor == null) {
                        resultFilePath = data.getData().getPath();
                    } else {
                        InputStream is = null;

                        try {
                            is = activity.getContentResolver().openInputStream(data.getData());
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                        try {
                            resultBitmap = BitmapFactory.decodeStream(is);
                        }
                        catch( OutOfMemoryError ex ) {
                            Toast.makeText(activity, getResources().getString(R.string.photoupload_photo_is_too_big), Toast.LENGTH_SHORT).show();
                        }

                        try {
                            is.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                } else {
                    Toast.makeText(activity, getResources().getString(R.string.photoupload_cancelled_label), Toast.LENGTH_SHORT).show();
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(activity, getResources().getString(R.string.photoupload_cancelled_label), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == CAMERA_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                    String[] proj = { MediaStore.Images.Media.DATA };
                    Cursor cursor = getActivity().managedQuery(imageUri, proj, null, null, null);
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();
                    resultFilePath = cursor.getString(column_index);

            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(activity, getResources().getString(R.string.photoupload_cancelled_label), Toast.LENGTH_SHORT).show();
            }
        }

        if (resultBitmap != null) {
            try {
                File outputFile = new File(activity.getCacheDir(), "tempMediaFile.jpg");
                outputFile.createNewFile();

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                resultBitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
                resultBitmap.recycle();
                byte[] bitmapdata = bos.toByteArray();
                bos.close();

                FileOutputStream fos = new FileOutputStream(outputFile);
                fos.write(bitmapdata);
                fos.close();

                new SendFileTask(outputFile).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if (resultFilePath != null) {
            new SendFileTask(new File(resultFilePath)).execute();
        }
    }

    interface ProgressListener {
        void transferred(long num);
    }

    class CountingTypedFile extends TypedFile {

        private static final int BUFFER_SIZE = 4096;

        private final ProgressListener listener;

        public CountingTypedFile(String mimeType, File file, ProgressListener listener) {
            super(mimeType, file);
            this.listener = listener;
        }

        @Override
        public void writeTo(OutputStream out) throws IOException {
            byte[] buffer = new byte[BUFFER_SIZE];
            FileInputStream in = new FileInputStream(super.file());
            long total = 0;
            try {
                int read;
                while ((read = in.read(buffer)) != -1) {
                    total += read;
                    this.listener.transferred(total);
                    out.write(buffer, 0, read);
                }
            } finally {
                in.close();
            }
        }
    }

    class SendFileTask extends AsyncTask<String, Integer, Integer> {
        private ProgressListener listener;
        private File file;

        public SendFileTask(File file) {
            this.file = file;
            progressDialog.show();
        }

        @Override
        protected Integer doInBackground(String... params) {
            //File file = new File(filePath);
            final long totalSize = file.length();
            listener = new ProgressListener() {
                @Override
                public void transferred(long num) {
                    publishProgress((int) ((num / (float) totalSize) * 100));
                }
            };

            try {
                CountingTypedFile fff = new CountingTypedFile("image/*", file, listener);
                SkRestInterface restAdapter = SkApplication.getRestAdapter();
                //TODO if user mode add default album instead of 0
                Integer albumId = entityType.equals(ENTITY_TYPE.ALBUM) ? entityId : 0;
                JsonObject result = restAdapter.uploadPhoto(fff, new TypedString(albumId.toString()));

                if (result != null && result.has("data")) {
                    JsonArray items = result.get("data").getAsJsonObject().get("uploaded").getAsJsonArray();


                    //temp hardcode for single photo
                    JsonObject item = items.get(1).getAsJsonArray().get(0).getAsJsonObject();



                    final PhotoItem newPhotoItem = new PhotoItem();
                    newPhotoItem.setIsApproved(item.get("approved").getAsBoolean());
                    newPhotoItem.setUrl(item.get("thumbUrl").getAsString());
                    newPhotoItem.setId(item.get("id").getAsInt());


                    //                for( int i=items.size()-1; i>=0; i-- ){
                    //                    JsonObject item = items.get(i).getAsJsonObject();
                    //                    newPhotoItem = new PhotoItem();
                    //                    newPhotoItem.setUrl(item.get("thumbUrl").getAsString());
                    //                    newPhotoItem.setId(item.get("id").getAsInt());
                    //
                    //                    photoList.add(0, newPhotoItem);
                    //                }

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pdProgressBar.setProgress(100);
                            pdText.setText(100 + "%");
                        }
                    });

                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            photoList.add(0, newPhotoItem);
                            gridView.setAdapter(new GridViewAdapter(getActivity(), R.layout.photoview_fragment_view_row, photoList));
                            progressDialog.dismiss();
                            hideEmptyPage();
                            updateActionBar();
                        }
                    });

                }
            }
            catch( Exception ex )
            {
                Log.e(" upload photo  ", ex.getMessage(), ex);
            }

            return 1;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            Log.e("FU", String.format("progress[%d]", values[0]));
            //do something with values[0], its the percentage so you can easily do

            int value = values[0];
            if ( values[0] > 95 )
            {
                value = 95;
            }

            pdProgressBar.setProgress(value);
            pdText.setText(value + "%");
        }
    }


}
