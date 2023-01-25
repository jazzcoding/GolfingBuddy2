package com.golfingbuddy.ui.hot_list;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridView;

import com.google.gson.GsonBuilder;
import com.golfingbuddy.R;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.golfingbuddy.core.SkApplication;
import com.golfingbuddy.core.SkServiceCallbackListener;
import com.golfingbuddy.model.base.BaseRestCommand;
import com.golfingbuddy.model.base.BaseServiceHelper;
import com.golfingbuddy.model.base.classes.SkUser;
import com.golfingbuddy.ui.hot_list.classes.HotListGridViewAdapter;
import com.golfingbuddy.ui.hot_list.classes.HotListItem;
import com.golfingbuddy.ui.hot_list.classes.HotListRestRequestCommand;
import com.golfingbuddy.ui.hot_list.service.HotListService;
import com.golfingbuddy.ui.memberships.SubscribeOrBuyActivity;
import com.golfingbuddy.ui.search.classes.AuthObject;
import com.golfingbuddy.utils.SKDimensions;
import com.golfingbuddy.utils.SKPopupMenu;
import com.golfingbuddy.utils.SkApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by kairat on 2/5/15.
 */
public class HotListView extends Activity implements View.OnClickListener {
    private HotListService mService;
    private SkServiceCallbackListener mListener;
    private HotListGridViewAdapter mAdapter;
    private SKPopupMenu mPopupMenu;
    private ProgressDialog mProgressDialog;
    private AlertDialog.Builder mConfirmDialog;

    private View mAddAction;
    private View mNoItem;
    private GridView mGridView;

    private Boolean mUserAdded;
    private Boolean mAuthorized;
    private Boolean mPromoted;
    private Boolean mIsCreditsEnable;
    private Boolean mIsSubsribeEnable;
    private String mAuthorizeMsg;

    public final static int SUBSCRIBE_ACTIVITY_RESULT_CODE = 5672341;
    private final static String SUBSCRIBE_PLUGIN_KEY = "hotlist";
    private final static String SUBSCRIBE_ADD_TO_LIST_ACTION = "add_to_list";

    public static HotListView newInstance() {
        return new HotListView();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hotlist_view);

        mService = new HotListService(getApplication());
        mListener = new SkServiceCallbackListener() {
            @Override
            public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
                JsonObject dataJson = SkApi.processResult(data);
                HotListRestRequestCommand.COMMAND command = (HotListRestRequestCommand.COMMAND)data.getSerializable("command");

                switch ( command ) {
                    case LOAD_LIST:
                        onLoadList(dataJson);
                        break;
                    case ADD:
                        onAdd(dataJson);
                        break;
                    case REMOVE:
                        onRemove(dataJson);
                        break;
                }
            }
        };
        mProgressDialog = new ProgressDialog(this, ProgressDialog.THEME_HOLO_DARK);
        mProgressDialog.setMessage(getResources().getString(R.string.hotlist_please_wait));
        mProgressDialog.setCancelable(false);

        mConfirmDialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_DARK);

        Resources resources = getResources();

        SKPopupMenu.MenuParams params = new SKPopupMenu.MenuParams(this);
        params.setWidth(530);
        params.setXoff(-470);
        params.setBackgroundColor(resources.getColor(R.color.base_popup_window_label_color));
        params.setAlpha(0.7f);

        SKPopupMenu.MenuItem menuItem = new SKPopupMenu.MenuItem("remove");
        menuItem.setLabel(resources.getString(R.string.hotlist_remove));
        menuItem.setLabelColor(resources.getColor(R.color.matches_white_text_color));
        params.setMenuItems(new ArrayList<>(Arrays.asList(menuItem)));

        mPopupMenu = new SKPopupMenu(params);
        mPopupMenu.setMenuItemClickListener(new SKPopupMenu.MenuItemClickListener() {
            @Override
            public Boolean onClick(SKPopupMenu.MenuItem menuItem, int position, long id, ArrayList<SKPopupMenu.MenuItem> list) {
                mConfirmDialog.setMessage(R.string.hotlist_remove_confirm_title);
                mConfirmDialog.setPositiveButton(R.string.hotlist_btn_remove, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeFromHotList();
                    }
                });
                mConfirmDialog.setNegativeButton(R.string.hotlist_btn_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                mConfirmDialog.show();

                return true;
            }
        });

        View anchor = findViewById(R.id.hotlist_anchor);
        mPopupMenu.getParams().setAnchor(anchor);

        anchor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupMenu.show();
            }
        });

        findViewById(R.id.hotlist_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mAddAction = findViewById(R.id.hotlist_add_action);
        mAddAction.setOnClickListener(this);

        mNoItem = findViewById(R.id.hotlist_no_item);
        mNoItem.findViewById(R.id.hotlist_no_item_icon).setOnClickListener(this);

        mService.getHotList(mListener);
    }

    @Override
    public void onClick(View v) {
        if (mAuthorized) {
            mConfirmDialog.setMessage(R.string.hotlist_add_confirm_message);
            mConfirmDialog.setPositiveButton(R.string.hotlist_btn_add, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    addToHotList();
                }
            });
            mConfirmDialog.setNegativeButton(R.string.hotlist_btn_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            mConfirmDialog.show();

        } else if (mPromoted) {
            mConfirmDialog.setMessage(mAuthorizeMsg);
            mConfirmDialog.setPositiveButton(R.string.hotlist_btn_buy, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(HotListView.this, SubscribeOrBuyActivity.class);
                    intent.putExtra("pluginKey", SUBSCRIBE_PLUGIN_KEY);
                    intent.putExtra("actionKey", SUBSCRIBE_ADD_TO_LIST_ACTION);

                    startActivityForResult(intent, SUBSCRIBE_ACTIVITY_RESULT_CODE);
                }
            });
            mConfirmDialog.setNegativeButton(R.string.hotlist_btn_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            mConfirmDialog.show();
        }
    }

    private void getAuthorizeInfo(JsonObject object) {
        if (object == null) {
            return;
        }

        mUserAdded = object.get("userAdded").getAsJsonPrimitive().getAsBoolean();
        mAuthorized = object.get("authorized").getAsJsonPrimitive().getAsBoolean();
        mPromoted = object.get("promoted").getAsJsonPrimitive().getAsBoolean();
        mIsCreditsEnable = object.has("isCreditsEnable") && object.getAsJsonPrimitive("isCreditsEnable").getAsBoolean();
        mIsSubsribeEnable = object.has("isSubscribeEnable") && object.getAsJsonPrimitive("isSubscribeEnable").getAsBoolean();
        mAuthorizeMsg = object.has("authorizeMsg") ? object.get("authorizeMsg").getAsString() : "";
    }

    private void onLoadList(JsonObject dataJson) {
        ArrayList<HotListItem> hotList = new ArrayList<>();
        mGridView = (GridView) findViewById(R.id.hotlist_view);

        if ( dataJson != null && dataJson.has("list") ) {
            Gson gson = new Gson();

            for ( JsonElement jsonElement : dataJson.getAsJsonArray("list") ) {
                hotList.add(gson.fromJson(jsonElement, HotListItem.class));
            }
        }

        try {
            getAuthorizeInfo(dataJson);

            showAddAction();

            mAdapter = new HotListGridViewAdapter(this, hotList);
            mGridView.setAdapter(mAdapter);
            mNoItem.setVisibility(View.GONE);
        } catch ( HotListGridViewAdapter.HotListAdapterException e ) {
            mNoItem.setVisibility(View.VISIBLE);
        }

        hideProgressBar();
    }

    private void onAdd(JsonObject dataJson) {
        mProgressDialog.dismiss();
        getAuthorizeInfo(dataJson);

        Boolean result = dataJson.get("result").getAsJsonPrimitive().getAsBoolean();

        if ( !result ) {
            mConfirmDialog.setMessage(R.string.hotlist_add_confirm_buy);
            mConfirmDialog.setPositiveButton(R.string.hotlist_btn_buy, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(HotListView.this, SubscribeOrBuyActivity.class);
                    intent.putExtra("pluginKey", "base");
                    intent.putExtra("actionKey", "view_profile");

                    startActivityForResult(intent, SUBSCRIBE_ACTIVITY_RESULT_CODE);
                }
            });
            mConfirmDialog.setNegativeButton(R.string.hotlist_btn_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            mConfirmDialog.show();
        } else {
            SkUser user = SkApplication.getUserInfo();
            HotListItem item = new HotListItem(user.getUserId(), user.getBigAvatarUrl() != null ? user.getBigAvatarUrl() : user.getAvatarUrl());

            if ( mAdapter == null ) {
                mAdapter = new HotListGridViewAdapter(this);
                mGridView.setAdapter(mAdapter);
            }

            mAdapter.addItem(item);

            mNoItem.setVisibility(View.GONE);
            mGridView.setPadding(0, SKDimensions.convertDpToPixel(48, this), 0, 0);
            findViewById(R.id.hotlist_anchor).setVisibility(View.VISIBLE);
            mAddAction.setVisibility(View.GONE);
        }
    }

    private void onRemove(JsonObject dataJson) {
        mProgressDialog.dismiss();
        getAuthorizeInfo(dataJson);
        SkUser user = SkApplication.getUserInfo();

        try {
            mAdapter.removeItem(user.getUserId());
            mNoItem.setVisibility(View.GONE);
        } catch ( HotListGridViewAdapter.HotListAdapterException e ) {
            mNoItem.setVisibility(View.VISIBLE);
        }

        mGridView.setPadding(0, SKDimensions.convertDpToPixel(48, this), 0, SKDimensions.convertDpToPixel(49, this));
        findViewById(R.id.hotlist_anchor).setVisibility(View.GONE);
        mAddAction.findViewById(R.id.hotlist_add_action).setVisibility(View.VISIBLE);
    }

    private void addToHotList() {
        mProgressDialog.show();
        mService.addToHotList(mListener);
    }

    private void removeFromHotList() {
        mProgressDialog.show();
        mService.removeFromHotList(mListener);
    }

    private void hideProgressBar() {
        findViewById(R.id.hotlist_progress_bar).setVisibility(View.GONE);
    }

    private void showAddAction() {
        if ( mUserAdded ) {
            mAddAction.setVisibility(View.GONE);
            mGridView.setPadding(0, SKDimensions.convertDpToPixel(48, this), 0, 0);
            findViewById(R.id.hotlist_anchor).setVisibility(View.VISIBLE);
        } else {
            if ( mAuthorized || (!mAuthorized && mPromoted) ) {
                mAddAction.setVisibility(View.VISIBLE);
                mGridView.setPadding(0, SKDimensions.convertDpToPixel(48, this), 0, SKDimensions.convertDpToPixel(49, this));
            } else {
                mAddAction.setVisibility(View.GONE);
                mNoItem.findViewById(R.id.hotlist_no_item_icon).setOnClickListener(null);
                mGridView.setPadding(0, SKDimensions.convertDpToPixel(48, this), 0, 0);
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ( requestCode == SUBSCRIBE_ACTIVITY_RESULT_CODE && this != null ) {
            checkAuthStatus();
        }
    }

    private void checkAuthStatus()
    {
        BaseServiceHelper helper = BaseServiceHelper.getInstance(this.getApplication());
        HashMap<String,String> params = new HashMap<String,String>();
        params.put(SUBSCRIBE_PLUGIN_KEY, SUBSCRIBE_ADD_TO_LIST_ACTION);

        helper.runRestRequest(BaseRestCommand.ACTION_TYPE.GET_AUTHORIZATION_ACTION_STATUS, params, new SkServiceCallbackListener() {
            @Override
            public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle bundle) {

                HashMap<String, Object> user = new HashMap<String, Object>();

                AutorizationActionStatus srd = null;

                try {
                    String result = bundle.getString("data");

                    if (result != null) {
                        JsonObject dataObject = new Gson().fromJson(result, JsonObject.class);
                        JsonElement listData = dataObject.get("data");
                        JsonElement type = dataObject.get("type");

                        if (listData != null && listData.isJsonObject() && type != null && "success".equals(type.getAsString())) {
                            JsonObject data = listData.getAsJsonObject();

                            Gson g = new GsonBuilder().create();
                            srd = g.fromJson(listData, AutorizationActionStatus.class);
                        }
                    }
                } catch (Exception ex) {
                    Log.i(" build search form  ", ex.getMessage(), ex);
                } finally {
                    if (srd != null && srd.auth.hotlist_add_to_list != null) {
                        if (srd.auth.hotlist_add_to_list.status != null) {
                            if (AuthObject.STATUS_AVAILABLE.equals(srd.auth.hotlist_add_to_list.status)) {
                                mAuthorized = true;
                                mPromoted = false;
                            } else if (AuthObject.STATUS_PROMOTED.equals(srd.auth.hotlist_add_to_list.status)) {
                                mAuthorized = false;
                                mPromoted = true;
                            } else if (AuthObject.STATUS_DISABLED.equals(srd.auth.hotlist_add_to_list.status)) {
                                mAuthorized = false;
                                mPromoted = false;
                            }

                            showAddAction();
                        }
                    }
                }
            }
        });
    }

    private class Auth {
        public AuthObject hotlist_add_to_list;
    }

    private class AutorizationActionStatus {

        public Auth getAuth() {
            return auth;
        }

        public void setAuth(Auth auth) {
            this.auth = auth;
        }

        private Auth auth;
    }
}
