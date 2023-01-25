package com.golfingbuddy.ui.speedmatch;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.golfingbuddy.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.golfingbuddy.core.SkServiceCallbackListener;
import com.golfingbuddy.ui.mailbox.send.Send;
import com.golfingbuddy.ui.matches.classes.SlidePhoto;
import com.golfingbuddy.ui.memberships.SubscribeOrBuyActivity;
import com.golfingbuddy.ui.speedmatch.classes.Speedmatches;
import com.golfingbuddy.ui.speedmatch.classes.SpeedmatchesFilter;
import com.golfingbuddy.ui.speedmatch.classes.SpeedmatchesRestRequestCommand;
import com.golfingbuddy.ui.speedmatch.classes.SpeedmatchesViewPager;
import com.golfingbuddy.ui.speedmatch.fragments.SpeedmatchesViewFragment;
import com.golfingbuddy.ui.speedmatch.service.SpeedmatchesService;
import com.golfingbuddy.utils.SKLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by kairat on 12/4/14.
 */
public class SpeedmatchesView extends Fragment {
    private static final int AUTHORIZE_RESULT = 100;

    private Boolean mIsCompleted = false;
    private Boolean extendMode;
    private SpeedmatchesFilter filterSettings;

    private TextView tvIndex;
    private ImageView ibOnlineStatus;
    private TextView tvDisplayNameAndAgesAndLocation;
    private ImageButton ibLike;
    private ImageButton ibInfo;
    private ImageButton ibSkip;
    private ListView lvInfo;
    private TextView tvCompatibility;

    private ArrayList<View> controls;

    private SpeedmatchesService service;
    private ArrayList<Speedmatches> list;
    private Boolean isAuthorized;
    private Boolean isPromoted;
    private String authrizeMsg;

    private SpeedmatchesViewPager mPager;
    private SpeedmatchesSlidePagerAdapter mPagerAdapter;
    private InfoArrayAdapter infoArrayAdapter;
    private SKLocation.SKGoogleLocation mGoogleLocation;
    private SKLocation.SKLocationInfo mLocationInfo;
    private SkServiceCallbackListener mListener;
    private SpeedmatchesBroadcastReceiver mReceiver = new SpeedmatchesBroadcastReceiver();

    public static SpeedmatchesView newInstance() {
        return new SpeedmatchesView();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().setTitle(getResources().getString(R.string.speedmatches_title));
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mListener = new SkServiceCallbackListener() {
            @Override
            public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
                SpeedmatchesRestRequestCommand.Command command = (SpeedmatchesRestRequestCommand.Command) data.getSerializable("command");

                String jsonString = data.getString("data");
                JsonParser jsonParser = new JsonParser();
                JsonObject json = jsonParser.parse(jsonString).getAsJsonObject().getAsJsonObject("data");

                switch (command) {
                    case LOAD_LIST:
                        isAuthorized = json.has("authorized") && json.getAsJsonPrimitive("authorized").getAsBoolean();
                        isPromoted = json.has("promoted") && json.getAsJsonPrimitive("promoted").getAsBoolean();
                        authrizeMsg = json.has("authorizeMsg") ? json.get("authorizeMsg").getAsString() : "";

                        if (!isAuthorized) {
                            View view = getView();
                            view.findViewById(R.id.speedmatches_progress_bar).setVisibility(View.GONE);
                            View content = view.findViewById(R.id.speedmatch_authorize);
                            content.setVisibility(View.VISIBLE);
                            ((TextView) view.findViewById(R.id.speedmatch_text)).setText(!authrizeMsg.isEmpty() ? authrizeMsg : getString(R.string.speedmatches_subscribe));

                            if (isPromoted) {
                                ImageView icon = (ImageView) view.findViewById(R.id.speedmatch_icon);
                                icon.setImageResource(R.drawable.lock_enable);
                                content.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(getActivity(), SubscribeOrBuyActivity.class);
                                        intent.putExtra("pluginKey", "base");
                                        intent.putExtra("actionKey", "view_profile");

                                        startActivityForResult(intent, AUTHORIZE_RESULT);
                                    }
                                });
                            }
                        } else {
                            loadListListener(json);
                        }
                        break;
                    case LIKE_USER:
                        likeUserListener(json);
                        break;
                    case SKIP_USER:
                        //onChangeMatch();
                        break;
                    case APPEND_LIST:
                        ArrayList<Speedmatches> speedmatchesArrayList = parse(json);
                        list.addAll(speedmatchesArrayList);

                        if (list.size() < service.COUNT) {
                            mIsCompleted = true;
                        }
                        break;
                }
            }
        };

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, new IntentFilter("base.user_block_change_status"));
    }

    @Override
    public void onDestroy() {
        mGoogleLocation.destroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);

        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.speedmatches_view, container, false);

        this.extendMode = false;
        this.tvIndex = (TextView) view.findViewById(R.id.speedmatches_index);
        this.ibOnlineStatus = (ImageView) view.findViewById(R.id.speedmatches_online_status);
        this.tvDisplayNameAndAgesAndLocation = (TextView) view.findViewById(R.id.speedmatches_displayname_and_ages_and_location);
        this.ibLike = (ImageButton) view.findViewById(R.id.speedmatches_like);
        this.ibInfo = (ImageButton) view.findViewById(R.id.speedmatches_info);
        this.ibSkip = (ImageButton) view.findViewById(R.id.speedmatches_skip);
        this.lvInfo = (ListView) view.findViewById(R.id.speedmatches_info_list);
        this.tvCompatibility = (TextView) view.findViewById(R.id.speedmatches_compatibility);

        this.controls = new ArrayList<>();
        this.controls.add(tvIndex);
        this.controls.add(ibOnlineStatus);
        this.controls.add(tvDisplayNameAndAgesAndLocation);

        this.service = new SpeedmatchesService(getActivity().getApplication());
        this.list = new ArrayList<>();

        this.mPager = (SpeedmatchesViewPager) view.findViewById(R.id.speedmatches_pager);
        this.mPagerAdapter = new SpeedmatchesSlidePagerAdapter(getFragmentManager());
        this.infoArrayAdapter = new InfoArrayAdapter(getActivity(), R.layout.speedmatches_info_item);

        mPager.setAdapter(mPagerAdapter);
        mPager.setOffscreenPageLimit(6);
        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int i) {
                Speedmatches speedmatches = getCurrentUser();

                if (speedmatches != null && !speedmatches.getPhotos().isEmpty()) {
                    tvIndex.setText(
                            String.format(getResources().getString(R.string.matches_index), i + 1, speedmatches.getPhotos().size())
                    );

                    animateIndex();
                }
            }
        });
        mPager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (extendMode == true) {
                    changeMode(false);
                }
            }
        });

        this.ibLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Speedmatches speedmatches = getCurrentUser();

                if (speedmatches != null) {
                    service.likeUser(speedmatches.getUserId(), mListener);
                }
            }
        });
        this.ibSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Speedmatches dto = shiftList();

                if (dto == null) {
                    return;
                }

                onChangeMatch();
                service.skipUser(dto.getUserId(), mListener);
            }
        });
        this.ibInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Boolean isExtend;
                Object tag = view.getTag();

                isExtend = (tag == null || (Boolean) tag == true);

                changeMode(isExtend);
                view.setTag(!isExtend);
            }
        });
        this.lvInfo.setAdapter(this.infoArrayAdapter);

        mGoogleLocation = new SKLocation.SKGoogleLocation(getActivity()) {
            @Override
            public void onConnected(Location lastLocation) {
                super.onConnected(lastLocation);

                Double longitude = null;
                Double latitude = null;

                if (lastLocation != null) {
                    longitude = lastLocation.getLongitude();
                    latitude = lastLocation.getLatitude();
                }

                SpeedmatchesView.this.start(longitude, latitude);
            }

            @Override
            public void onLocationInfoReady(SKLocation.SKLocationInfo skLocationInfo) {
                super.onLocationInfoReady(skLocationInfo);

                SpeedmatchesView.this.mLocationInfo = skLocationInfo;
            }
        };
        mGoogleLocation.connect();

        return view;
    }

    private Menu mMenu;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        getActivity().getMenuInflater().inflate(R.menu.speedmatches_menu, menu);
        mMenu = menu;

        if (this.filterSettings == null) {
            mMenu.findItem(R.id.speedmatches_filter_menu).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.speedmatches_filter_menu:
                service.cancel();
                Intent intent = new Intent(getActivity(), SpeedmatchesFilterActivity.class);
                HashMap<String, Object> filterSettings = getFilterSettings().getData();

                if (mLocationInfo != null && filterSettings.get("googlemapLocation") != null) {
                    ((HashMap<String, Object>) filterSettings.get("googlemapLocation")).put("address", mLocationInfo.getRealAddress());
                }

                intent.putExtra("settings", filterSettings);
                startActivityForResult(intent, 500);
                getActivity().overridePendingTransition(R.anim.speedmatches_slide_in_right, R.anim.speedmatches_slide_out_left);
                getView().findViewById(R.id.speedmatches_view_no_items).setVisibility(View.INVISIBLE);
                hide();
                list.clear();
                infoArrayAdapter.clear();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void start(Double longitude, Double latitude) {
        if (filterSettings == null) {
            filterSettings = service.getFilterSettings(getActivity());
        }

        filterSettings.getGooglemapLocation().put("longitude", longitude);
        filterSettings.getGooglemapLocation().put("latitude", latitude);

        this.service.getMatchesList(mListener, filterSettings);
    }

    private void loadListListener(JsonObject json) {
        ArrayList<Speedmatches> speedmatchesArrayList = parse(json);

        if (speedmatchesArrayList != null) {
            list.addAll(speedmatchesArrayList);
        }

        if (list.size() < SpeedmatchesService.COUNT) {
            mIsCompleted = true;
        }

        onChangeMatch();

        JsonElement filterElement = json.getAsJsonObject("filter");

        if (filterElement != null) {
            this.filterSettings = parseFilter(filterElement);
            mMenu.findItem(R.id.speedmatches_filter_menu).setVisible(true);
        }
    }

    private void likeUserListener(JsonObject json) {
        JsonElement isMutual = json.get("mutual");

        if (isMutual != null && isMutual.getAsBoolean()) {
            if (json.has("conversation")) {
                new Send(getActivity(), json.getAsJsonObject("conversation"));
            } else {
                new Send(getActivity(), getCurrentUser().getUserId(), "chat");
            }
        }

        shiftList();
        onChangeMatch();
    }

    public SpeedmatchesFilter getFilterSettings() {
        return filterSettings;
    }

    private void changeMode(Boolean toExtend) {
        changeMode(toExtend, null, null);
    }

    private void changeMode(Boolean toExtend, Float from, Float to) {
        this.extendMode = toExtend;
        Resources resources = getResources();

        final int changeModeDuration = resources.getInteger(R.integer.speedmatches_change_mode_duration);

        final LinearLayout pagerSide = (LinearLayout) getView().findViewById(R.id.speedmatches_pager_side);
        final LinearLayout infoSide = (LinearLayout) getView().findViewById(R.id.speedmatches_info_side);


        TypedValue bigType = new TypedValue();
        resources.getValue(R.dimen.speedmatches_expand_small_side, bigType, true);
        float bigSide = to != null ? to : bigType.getFloat();

        TypedValue smallType = new TypedValue();
        resources.getValue(R.dimen.speedmatches_collapse_small_side, smallType, true);
        float smallSide = from != null ? from : smallType.getFloat();

        if (toExtend == true) {
            hideView(tvIndex);

            if (getCurrentUser().getRealCompatibility() != null) {
                showView(tvCompatibility);
            }

            showView(lvInfo);

            if (getCurrentUser().getAvatar() == null && getCurrentUser().getPhotos().isEmpty()) {
                smallSide = 0.f;
                bigSide = 1.f;
            }

            Animation animation = new ChangeModeAnimation(pagerSide, infoSide, smallSide, bigSide);
            animation.setDuration(changeModeDuration);
            pagerSide.startAnimation(animation);
            final View divider =  getView().findViewById(R.id.speedmatches_divider);
            divider.setVisibility(View.VISIBLE);
            divider.setAlpha(1f);
            divider.animate().alpha(0).setDuration(changeModeDuration).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    divider.setVisibility(View.GONE);
                }
            });

            mPager.setTouchEnable(getCurrentUser().getPhotos().size() > 4);
        }
        else
        {
            hideView(tvCompatibility);
            hideView(lvInfo);

            animateIndex();

            Animation animation = new ChangeModeAnimation(pagerSide, infoSide, bigSide, smallSide);
            animation.setDuration(changeModeDuration);
            pagerSide.startAnimation(animation);
            View divider =  getView().findViewById(R.id.speedmatches_divider);
            divider.setVisibility(View.VISIBLE);
            divider.setAlpha(0);
            divider.animate().alpha(1f).setDuration(changeModeDuration).setListener(null);
            mPager.setTouchEnable(true);
        }

        ibInfo.setImageResource(toExtend == true ? R.drawable.speedmatches_info_on : R.drawable.speedmatches_info);
        mPagerAdapter.notifyDataSetChanged();
    }

    private class ChangeModeAnimation extends Animation {
        private View view1;
        private View view2;
        private final float mStartWeight;
        private final float mDeltaWeight;

        public ChangeModeAnimation(View view1, View view2, float startWeight, float endWeight) {
            this.view1 = view1;
            this.view2 = view2;
            mStartWeight = startWeight;
            mDeltaWeight = endWeight - startWeight;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view1.getLayoutParams();
            lp.weight = (mStartWeight + (mDeltaWeight * interpolatedTime));
            view1.setLayoutParams(lp);
            LinearLayout.LayoutParams lp1 = (LinearLayout.LayoutParams) view2.getLayoutParams();
            lp1.weight = 1 - lp.weight;
            view2.setLayoutParams(lp1);
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }
    }

    private Speedmatches getCurrentUser() {
        if (list.isEmpty()) {
            emptyAction();

            return null;
        }

        return list.get(0);
    }

    private Speedmatches shiftList() {
        if ( list.isEmpty() ) {
            emptyAction();

            return null;
        }

        Speedmatches dto = list.get(0);
        list.remove(0);

        return dto;
    }

    private ArrayList<Speedmatches> parse(JsonObject json) {
        if (json == null || !json.has("list")) {
            return null;
        }

        JsonArray jsonUserList = json.getAsJsonArray("list");

        if (jsonUserList == null) {
            return null;
        }

        ArrayList<Speedmatches> speedmatcheses = new ArrayList<>();
        Gson gson = new GsonBuilder().create();

        for (JsonElement jsonElement : jsonUserList) {
            speedmatcheses.add(gson.fromJson(jsonElement, Speedmatches.class));
        }

        return speedmatcheses;
    }

    private SpeedmatchesFilter parseFilter(JsonElement json) {
        Gson gson = new GsonBuilder().create();
        SpeedmatchesFilter filter = gson.fromJson(json, SpeedmatchesFilter.class);

        return filter;
    }

    private Boolean assignMatch(Speedmatches speedmatches) {
        if (speedmatches == null) {
            return  false;
        }

        Resources resources = getResources();

        tvDisplayNameAndAgesAndLocation.setText(
                Html.fromHtml(String.format(resources.getString(
                R.string.matches_display_name_and_age), speedmatches.getDisplayName(), speedmatches.getAges()) + " <font color=\"#b0b0b0\">" + speedmatches.getLocation() + "</font>")
        );

        if ( speedmatches.getPhotos().isEmpty() ) {
            hideView(tvIndex);
        } else {
            tvIndex.setText(
                    String.format(getResources().getString(R.string.matches_index), 1, speedmatches.getPhotos().size())
            );
        }

        ibOnlineStatus.setVisibility(speedmatches.isOnline() ? View.VISIBLE : View.GONE);
        infoArrayAdapter.clear();
        infoArrayAdapter.addAll(speedmatches.getInfo());

        if (speedmatches.getRealCompatibility() == null) {
            hideView(tvCompatibility);
        } else {
            tvCompatibility.setText(String.format(resources.getString(
                    R.string.matches_compatibility), speedmatches.getCompatibility()));
        }

        return true;
    }

    private Boolean assignMatch() {
        Speedmatches speedmatches = getCurrentUser();

        if (speedmatches != null) {
            return assignMatch(speedmatches);
        }

        return false;
    }

    private void onChangeMatch() {
        hideUI();

        if (assignMatch()) {
            mPagerAdapter.notifyDataSetChanged();
            mPager.setCurrentItem(0, false);

            if (extendMode)
                if (getCurrentUser().getPhotos().isEmpty()) {
                    changeMode(extendMode, 1f, 0.82f);
                } else {
                    changeMode(extendMode, 1f, 0.82f);
                }

            showUI();
            show();
            animateIndex();
        }

        if (mIsCompleted != true && this.list.size() <= SpeedmatchesService.CRITICALC_COUNT) {
            appendList();
        }
    }

    private void appendList() {
        ArrayList<String> excludeList = new ArrayList<>();

        for (Speedmatches dto: this.list) {
            excludeList.add(Integer.toString(dto.getUserId()));
        }

        this.service.getMatchesList(excludeList, mListener);
    }

    public void show() {
        int duration = getResources().getInteger(R.integer.matches_duration);

        FrameLayout frameLayout = (FrameLayout) getView().findViewById(R.id.speedmatches_view);
        frameLayout.setAlpha(0f);
        frameLayout.setVisibility(View.VISIBLE);
        frameLayout.animate()
                .alpha(1f)
                .setDuration(duration)
                .setListener(null);

        final ProgressBar progressBar = (ProgressBar) getView().findViewById(R.id.speedmatches_progress_bar);
        progressBar.animate()
                .alpha(0f)
                .setDuration(duration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    public void hide() {
        int duration = getResources().getInteger(R.integer.matches_duration);

        final ProgressBar progressBar = (ProgressBar) getView().findViewById(R.id.speedmatches_progress_bar);
        progressBar.setAlpha(0f);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.animate()
                .alpha(1f)
                .setDuration(duration)
                .setListener(null);

        if (list.isEmpty()) {
            return;
        }

        final FrameLayout frameLayout = (FrameLayout) getView().findViewById(R.id.speedmatches_view);
        frameLayout.setAlpha(1f);
        frameLayout.setVisibility(View.VISIBLE);
        frameLayout.animate()
                .alpha(0f)
                .setDuration(duration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        frameLayout.setVisibility(View.INVISIBLE);
                    }
                });
    }

    private void hideUI() {
//        if (list.isEmpty()) {
//            return;
//        }

        for (View view : this.controls) {
            hideView(view);
        }

        ibLike.setClickable(false);
        ibInfo.setClickable(false);
        ibSkip.setClickable(false);
    }

    private void showUI() {
        if (list.isEmpty()) {
            return;
        }

        for (View view : this.controls) {
            showView(view);
        }

        ibLike.setClickable(true);
        ibInfo.setClickable(true);
        ibSkip.setClickable(true);
    }

    private void hideView(final View view)
    {
        view.animate().alpha(0f).setDuration(getResources().getInteger(R.integer.matches_duration)).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
                view.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void showView(View view)
    {
        int duration = getResources().getInteger(R.integer.matches_duration);
        showView(view, duration);
    }

    private void showView(View view, int duration) {
        view.setVisibility(View.VISIBLE);
        view.animate().alpha(1f).setDuration(duration).setListener(null);
    }

    private class InfoArrayAdapter extends ArrayAdapter<Speedmatches.Info> {
        public InfoArrayAdapter(Context context, int resource) {
            super(context, resource);
        }

        @Override
        public View getView(int position, View row, ViewGroup parent) {
            Speedmatches.Info data = this.getItem(position);
            LayoutInflater inflater = ((Activity)this.getContext()).getLayoutInflater();
            row = inflater.inflate(R.layout.speedmatches_info_item, parent, false);

            ((TextView) row.findViewById(R.id.speedmatches_info_label)).setText(data.getLabel());
            ((TextView) row.findViewById(R.id.speedmatches_info_value)).setText(data.getValue());

            return row;
        }
    }

    private SpeedmatchesViewFragment.ClickListener listener = new SpeedmatchesViewFragment.ClickListener() {
        @Override
        public void onClick(Fragment fragment, View view) {
            animateIndex();
        }
    };

    private class SpeedmatchesSlidePagerAdapter extends FragmentStatePagerAdapter {
        public SpeedmatchesSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Speedmatches user = getCurrentUser();

            if (user == null) {
                return SpeedmatchesViewFragment.create(null, 0);
            }

            SlidePhoto params = new SlidePhoto();

            if (position == 0) {
                params.setIsAuthorized(true);

                if (TextUtils.isEmpty(user.getAvatar())) {
                    if (user.getIsAuthorized() && !user.getPhotos().isEmpty()) {
                        params.setPhoto(user.getPhoto(position));
                    } else {
                        params.setPhoto(null);
                    }
                } else {
                    params.setPhoto(user.getAvatar());
                }
            } else {
                params.setPhoto(user.getIsAuthorized() ? user.getPhoto(position) : null);
                params.setIsAuthorized(user.getIsAuthorized());
            }

            params.setIsSubscribe(user.getIsSubscribe());
            params.setAuthorizeMsg(user.getAuthorizeMsg());
            SpeedmatchesViewFragment fragment = SpeedmatchesViewFragment.create(params, SpeedmatchesView.this.getId());
            fragment.setListener(listener);

            return fragment;
        }

        @Override
        public int getCount() {
            if (list.isEmpty()) {
                return 0;
            }

            if (extendMode && !getCurrentUser().getIsAuthorized()) {
                return 1;
            }

            if (!getCurrentUser().getIsAuthorized()) {
                return 2;
            }

            if (getCurrentUser().getPhotos().isEmpty())
            {
                return 1;
            }

            return list.get(0).getPhotos().size();
        }

        @Override
        public int getItemPosition(Object o) {
            return POSITION_NONE;
        }

        @Override
        public float getPageWidth(int position) {
            if (extendMode == false) {
                return 1;
            }

            int count = this.getCount() - 1;

            if (position == 0 || position == count) {
                //return 0.9f;
            }

            return 0.25f;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AUTHORIZE_RESULT) {
            View view = getView();
            view.findViewById(R.id.speedmatches_progress_bar).setVisibility(View.VISIBLE);
            View content = view.findViewById(R.id.speedmatch_authorize);
            content.setVisibility(View.GONE);

            list.clear();
            mPagerAdapter.notifyDataSetChanged();
            this.service.getMatchesList(mListener, filterSettings);
        }
        else {
            getActivity().overridePendingTransition(R.anim.speedmatches_slide_in_left, R.anim.speedmatches_slide_out_right);
            filterSettings = service.mergeSettings(filterSettings, service.getFilterSettings(getActivity()));
            mPagerAdapter.notifyDataSetChanged();
            service.getMatchesList(mListener, filterSettings);

        }
    }

    private void emptyAction() {
        int duration = getResources().getInteger(R.integer.matches_duration);

        View view = getView();

        final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.speedmatches_progress_bar);
        progressBar.setVisibility(View.INVISIBLE);

        final FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.speedmatches_view);
        frameLayout.setVisibility(View.INVISIBLE);

        final LinearLayout noItemLayout = (LinearLayout) view.findViewById(R.id.speedmatches_view_no_items);
        noItemLayout.setAlpha(0f);
        noItemLayout.setVisibility(View.VISIBLE);
        noItemLayout.animate()
                .alpha(1f)
                .setDuration(duration)
                .setListener(null);
    }

    private void animateIndex() {
        if (extendMode || getCurrentUser().getPhotos().isEmpty()) {
            hideView(tvIndex);

            return;
        }

        int duration = getResources().getInteger(R.integer.matches_duration);
        tvIndex.animate().cancel();
        tvIndex.setVisibility(View.VISIBLE);
        tvIndex.animate().alpha(1f).setStartDelay(0).setDuration(duration).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                tvIndex.animate().setStartDelay(2000).setDuration(500).alpha(0f).setListener(null);
            }
        });
    }

    private class SpeedmatchesBroadcastReceiver extends BroadcastReceiver {
        private HashMap<String, Speedmatches> mList;

        public SpeedmatchesBroadcastReceiver() {
            mList = new HashMap<>();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            int userId = intent.getIntExtra("userId", 0);
            Boolean status = intent.getBooleanExtra("status", false);

            if (userId != 0 && !list.isEmpty()) {
                if (status) {
                    for (int i = 0, j = list.size(); i < j; i++) {
                        if (list.get(i).getUserId() == userId) {
                            mList.put(Integer.toString(i), list.get(i));

                            break;
                        }
                    }
                } else {
                    Set<String> keys = mList.keySet();

                    for (String index : keys) {
                        if (mList.get(index).getUserId() == userId) {
                            list.add(Integer.valueOf(index), mList.get(index));
                            Speedmatches dto = shiftList();

                            if (dto == null) {
                                return;
                            }

                            onChangeMatch();

                            break;
                        }
                    }
                }
            }
        }
    }
}
