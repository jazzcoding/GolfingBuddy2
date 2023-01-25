package com.golfingbuddy.ui.matches.fragments;

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
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.golfingbuddy.R;
import com.golfingbuddy.core.SkApplication;
import com.golfingbuddy.core.SkServiceCallbackListener;
import com.golfingbuddy.model.base.BaseRestCommand;
import com.golfingbuddy.model.base.BaseServiceHelper;
import com.golfingbuddy.ui.base.SkBaseInnerActivity;
import com.golfingbuddy.ui.mailbox.send.Send;
import com.golfingbuddy.ui.matches.classes.SlideData;
import com.golfingbuddy.ui.matches.classes.SlidePhoto;
import com.golfingbuddy.ui.profile.ProfileViewActivity;
import com.golfingbuddy.ui.search.classes.AuthObject;
import com.golfingbuddy.utils.SKColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by kairat on 12/2/14.
 */
public class UserSlide extends Fragment {
    public static final int COUNT = 20;
    public static final int LIMIT = 5;

    private int mCount;
    private Boolean mIsCompleted;
    private int mLimit;
    private int mCurrentIterator;
    private ArrayList<SlideData> mSlideData;
    private Boolean mDrawPhotoIndex;
    private String mEmptyLabel;

    private TextView mMatchesIndex;
    private TextView mDisplayNameAndAges;
    private TextView mRole;
    private TextView mLocation;
    private TextView mCompatibility;

    private ImageButton mPrev;
    private ImageButton mNext;
    private ImageButton mBookmark;
    private ImageButton mChat;
    private ImageButton mProfile;
    private LinearLayout mNoItem;
    private View.OnClickListener mActionListener;

    private ViewPager mPager;
    private ScreenSlidePagerAdapter mPagerAdapter;
    private UserSlideListener mUserSlideListener;
    private UserSlideBroadcastReceiver mReceiver = new UserSlideBroadcastReceiver();
    private UserSubscrobeBroadcastReceiver mSubscribeReceiver = new UserSubscrobeBroadcastReceiver();


    private enum MOVE {
        NEXT, PREV
    }

    public UserSlide() {
        mCount = COUNT;
        mIsCompleted = false;
        mLimit = LIMIT;
        mCurrentIterator = 0;
        mSlideData = new ArrayList<>();
        mDrawPhotoIndex = false;
        mUserSlideListener = new UserSlideListener();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mEmptyLabel = getResources().getString(R.string.speedmatches_no_items);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActionListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() != R.id.matches_bookmark) {
                    v.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.user_slide_button_anim));
                }

                switch (v.getId()) {
                    case R.id.matches_prev:
                        prev();
                        break;
                    case R.id.matches_next:
                        next();
                        break;
                    case R.id.matches_bookmark:
                        bookmark();
                        break;
                    case R.id.matches_chat:
                        chat();
                        break;
                    case R.id.matches_profile:
                        profile();
                        break;
                    default:
                        return;
                }
            }
        };

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, new IntentFilter("base.user_block_change_status"));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mSubscribeReceiver, new IntentFilter("base.user_subscribe_status_changed"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.user_slide, container, false);

        mPager = (ViewPager) view.findViewById(R.id.slide_pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getFragmentManager());

        mPager.setAdapter(mPagerAdapter);
        mPager.setOffscreenPageLimit(6);
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
                mUserSlideListener.onPageScrolled(i, v, i2);
            }

            @Override
            public void onPageSelected(int i) {
                SlideData user = getCurrentItem();
                int count = user.getIsLimited() ? 2 : user.getPhotos().isEmpty() ? 1 : user.getPhotos().size();

                mMatchesIndex.setText(
                        String.format(getResources().getString(R.string.matches_index), i + 1, mPagerAdapter.getCount())
                );
                animateIndex();
                mUserSlideListener.onPageSelected(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                mUserSlideListener.onPageScrollStateChanged(i);
            }
        });

        init(view);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
    }

    private void init(ViewGroup view) {
        mMatchesIndex = (TextView) view.findViewById(R.id.slide_index);
        mDisplayNameAndAges = (TextView) view.findViewById(R.id.slide_displayname_age);
        mRole = (TextView) view.findViewById(R.id.slide_role);
        mLocation = (TextView) view.findViewById(R.id.slide_location);
        mCompatibility = (TextView) view.findViewById(R.id.slide_compatibility);

        mPrev = (ImageButton) view.findViewById(R.id.matches_prev);
        mNext = (ImageButton) view.findViewById(R.id.matches_next);
        mBookmark = (ImageButton) view.findViewById(R.id.matches_bookmark);
        mChat = (ImageButton) view.findViewById(R.id.matches_chat);
        mProfile = (ImageButton) view.findViewById(R.id.matches_profile);
        mNoItem = (LinearLayout) view.findViewById(R.id.user_slide_no_items);

        mPrev.setOnClickListener(mActionListener);
        mNext.setOnClickListener(mActionListener);
        mBookmark.setOnClickListener(mActionListener);
        mChat.setOnClickListener(mActionListener);
        mProfile.setOnClickListener(mActionListener);

        if (!SkApplication.isPluginActive(SkApplication.PLUGINS.BOOKMARKS)) {
            mBookmark.setVisibility(View.GONE);
        }

        if (!SkApplication.isPluginActive(SkApplication.PLUGINS.MAILBOX)) {
            mChat.setVisibility(View.GONE);
        }
    }

    public UserSlideListener getUserSlideListener() {
        return mUserSlideListener;
    }

    public void setUserSlideListener(UserSlideListener userSlideListener) {
        this.mUserSlideListener = userSlideListener;
    }

    public void addSlideData(SlideData data) {
        mSlideData.add(data);
    }

    public void addSlideData(ArrayList<SlideData> data) {
        if ( data != null ) {
            mSlideData.addAll(data);
        }
    }

    public SlideData getData(int index) {
        try {
            return mSlideData.get(index);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public ArrayList<SlideData> getDataList() {
        return mSlideData;
    }

    public int getCount() {
        return mCount;
    }

    public void setCount(int mCount) {
        this.mCount = mCount;
    }

    public Boolean getIsCompleted() {
        return mIsCompleted;
    }

    public void setIsCompleted(Boolean mIsCompleted) {
        this.mIsCompleted = mIsCompleted;
    }

    public int getLimit() {
        return mLimit;
    }

    public void setLimit(int mLimit) {
        this.mLimit = mLimit;
    }

    public int getCurrentIterator() {
        return mCurrentIterator;
    }

    public Boolean getDrawPhotoIndex() {
        return mDrawPhotoIndex;
    }

    public void setDrawPhotoIndex(Boolean mDrawPhotoIndex) {
        this.mDrawPhotoIndex = mDrawPhotoIndex;
    }

    public String getEmptyLabel() {
        return mEmptyLabel;
    }

    public void setEmptyLabel(String mEmptyLabel) {
        this.mEmptyLabel = mEmptyLabel;
    }


    public Boolean isEmpty() {
        return mSlideData.isEmpty();
    }

    public Boolean hasPrev() {
        return !isEmpty() && mCurrentIterator > 0;
    }

    public Boolean hasNext() {
        return !isEmpty() && mCurrentIterator < mSlideData.size() - 1;
    }

    public SlideData getPreviousItem() {
        if (hasPrev()) {
            return mSlideData.get(mCurrentIterator - 1);
        }

        return null;
    }

    public SlideData getCurrentItem() {
        return isEmpty() ? null : mSlideData.get(mCurrentIterator);
    }

    public SlideData getNextItem() {
        if (hasNext()) {
            return mSlideData.get(mCurrentIterator + 1);
        }

        return null;
    }

    public void setSlideData(ArrayList<SlideData> slideData) {
        Reset();

        if (slideData == null || slideData.isEmpty()) {
            hide(false);
            mNoItem.setVisibility(View.VISIBLE);

            return;
        }

        if (slideData.size() < mCount) {
            mIsCompleted = true;
        }

        prepareData(slideData);

        mSlideData.addAll(slideData);

        triggerUserChange();
        show();
    }

    private void Reset() {
        mCurrentIterator = 0;
        mIsCompleted = false;
        mSlideData.clear();
    }

    private void prepareData(ArrayList<SlideData> data) {
        for (SlideData slide : data) {
            if (!TextUtils.isEmpty(slide.getAvatar())) {
                slide.getPhotos().add(0, slide.getAvatar());
            }
        }
    }

    public void next() {
        if (hasNext() && mUserSlideListener.onMoveNext(getPreviousItem(), getCurrentItem(), getNextItem())) {
            move(MOVE.NEXT);
        }
    }

    public void prev() {
        if (hasPrev() && mUserSlideListener.onMovePrevious(getPreviousItem(), getCurrentItem(), getNextItem())) {
            move(MOVE.PREV);
        }
    }

    private void move(MOVE duration) {
        switch (duration) {
            case NEXT:
                if (hasNext()) {
                    mCurrentIterator++;
                }
                break;
            case PREV:
                if (hasPrev()) {
                    mCurrentIterator--;
                }
                break;
            default:
                return;
        }

        triggerUserChange();
    }

    private void bookmark() {
        SlideData slideData = getCurrentItem();

        if (mUserSlideListener.onBookmark(slideData)) {
            slideData.setBookmarked(!slideData.isBookmarked());
            mBookmark.setImageResource(slideData.isBookmarked() ? R.drawable.bookmark_prssed : R.drawable.bookmark);
            ((SkBaseInnerActivity)getActivity()).getBaseHelper().bookmark(slideData.getUserId(), slideData.isBookmarked(), null);

            Toast.makeText(getActivity(), getString(slideData.isBookmarked() ? R.string.profile_view_bookmark_on : R.string.profile_view_bookmark_off), Toast.LENGTH_SHORT).show();

            Intent intent = new Intent("base.bookmark_change_status");
            intent.putExtra("userId", slideData.getUserId());
            intent.putExtra("status", slideData.isBookmarked());
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
        }
    }

    private void chat() {
        if (mUserSlideListener.onChat(getCurrentItem())) {
            new Send(getActivity(), getCurrentItem().getUserId());
        }
    }

    private void profile() {
        if (mUserSlideListener.onProfile(getCurrentItem())) {
            Intent intent = new Intent(getActivity(), ProfileViewActivity.class);
            intent.putExtra("userId", getCurrentItem().getUserId());
            startActivity(intent);
        }
    }

    private void triggerUserChange() {
        hideUI();

        if (hasPrev()) {
            mPrev.setClickable(true);
            mPrev.setAlpha(1f);
        } else {
            mPrev.setAlpha(0.5f);
            mPrev.setClickable(false);
        }

        if (hasNext()) {
            mNext.setClickable(true);
            mNext.setAlpha(1f);
        } else {
            mNext.setAlpha(0.5f);
            mNext.setClickable(false);
        }

        if (!isEmpty()) {
            assignSlideData(getCurrentItem());
            mPagerAdapter.notifyDataSetChanged();
            mPager.setCurrentItem(0, false);
        }

        showUI();

        if ( (mPager.getCurrentItem() == 0 && getCurrentItem().getAvatar() != null) || getCurrentItem().getIsAuthorized() ) {
            int color = getResources().getColor(R.color.matches_white_text_color);

            mDisplayNameAndAges.setTextColor(color);
            mDisplayNameAndAges.setShadowLayer(10, 1, 1, R.color.matches_remark);
            mLocation.setTextColor(color);
            mLocation.setShadowLayer(10, 1, 1, R.color.matches_remark);
            mCompatibility.setTextColor(color);
            mCompatibility.setShadowLayer(10, 1, 1, R.color.matches_remark);
        } else {
            int color = getResources().getColor(R.color.matches_black_text_color);

            mDisplayNameAndAges.setTextColor(color);
            mDisplayNameAndAges.setShadowLayer(0, 0, 0, 0);
            mLocation.setTextColor(getResources().getColor(R.color.matches_remark));
            mLocation.setShadowLayer(0, 0, 0, 0);
            mCompatibility.setTextColor(color);
            mCompatibility.setShadowLayer(0, 0, 0, 0);
        }

        if (!mIsCompleted && mCurrentIterator >= mSlideData.size() - mLimit) {
            ArrayList<SlideData> list = new ArrayList(mSlideData.subList(mCurrentIterator, mSlideData.size()));

            mUserSlideListener.loadMore(mCurrentIterator, list);
        }

        animateIndex();

        mUserSlideListener.onAfterMove(getCurrentItem());
    }

    private void hideUI() {
        //hideTextView(mMatchesIndex);
        hideTextView(mDisplayNameAndAges);
        hideTextView(mRole);
        hideTextView(mLocation);
        hideTextView(mCompatibility);
    }

    private void showUI() {
        showTextView(mDisplayNameAndAges);
        showTextView(mLocation);
    }

    private void hideTextView(TextView textView) {
        textView.setVisibility(View.GONE);
        textView.setAlpha(0f);
    }

    private void showTextView(TextView textView) {
        int duration = getResources().getInteger(R.integer.matches_duration);
        textView.setVisibility(View.VISIBLE);
        textView.animate().alpha(1f).setDuration(duration).setListener(null);
    }

    private void assignSlideData(SlideData slideData) {
        if (slideData == null) {
            return;
        }

        Resources resources = getResources();

        mDisplayNameAndAges.setText(
                String.format(resources.getString(
                        R.string.matches_display_name_and_age), slideData.getDisplayName(), slideData.getAges())
        );

        if (slideData.getLabel() != null && slideData.getLabelColor() != null) {
            mRole.setText(slideData.getLabel());
            GradientDrawable bgShape = (GradientDrawable) mRole.getBackground();
            bgShape.setColor(SKColor.RGBtoColor(slideData.getLabelColor()));
            showTextView(mRole);
        }

        if (slideData.getRealCompatibility() != null) {
            mCompatibility.setText(
                    String.format(resources.getString(
                            R.string.matches_compatibility), slideData.getCompatibility())
            );
            showTextView(mCompatibility);
            mCompatibility.setVisibility(View.VISIBLE);
        }

        mLocation.setText(slideData.getLocation());

        mMatchesIndex.setText(
                String.format(resources.getString(
                        R.string.matches_index), 1, mPagerAdapter.getCount())
        );

        mBookmark.setImageResource(slideData.isBookmarked() ? R.drawable.bookmark_prssed : R.drawable.bookmark);

        mUserSlideListener.onAssignSlideData(slideData);
    }

    public void show() {
        View view = getView();

        if (view == null) {
            return;
        }

        int duration = getResources().getInteger(R.integer.matches_duration);

        FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.matches_view);
        frameLayout.setAlpha(0f);
        frameLayout.setVisibility(View.VISIBLE);
        frameLayout.animate()
                .alpha(1f)
                .setDuration(duration)
                .setListener(null);

        final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.matches_progress_bar);
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
        hide(true);
    }

    public void hide(Boolean showProgressBar) {
        View view = getView();

        if (view == null) {
            return;
        }

        int duration = getResources().getInteger(R.integer.matches_duration);

        final FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.matches_view);
        frameLayout.animate()
                .alpha(0f)
                .setDuration(duration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        frameLayout.setVisibility(View.GONE);
                    }
                });


        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.matches_progress_bar);
        progressBar.setVisibility(showProgressBar ? View.VISIBLE : View.INVISIBLE);
        progressBar.animate()
                .alpha(1f)
                .setDuration(duration)
                .setListener(null);

        mNoItem.setVisibility(View.INVISIBLE);
    }

    private void animateIndex() {
        if (!mDrawPhotoIndex) {
            hideTextView(mMatchesIndex);

            return;
        }

        int duration = getResources().getInteger(R.integer.matches_duration);
        mMatchesIndex.animate().cancel();
        mMatchesIndex.setVisibility(View.VISIBLE);
        mMatchesIndex.animate().alpha(1f).setStartDelay(0).setDuration(duration).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                mMatchesIndex.animate().setStartDelay(2000).setDuration(500).alpha(0f).setListener(null);
            }
        });
    }

    public void onClick(Fragment fragment, View view) {
        animateIndex();
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            SlideData user = getCurrentItem();

            if (user == null) {
                return SlideViewFragment.create(null, 0);
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

            return SlideViewFragment.create(params, UserSlide.this.getId());
        }

        @Override
        public int getCount() {
            if (isEmpty()) {
                return 0;
            }

            SlideData user = getCurrentItem();

            if (!user.getIsAuthorized()) {
                return 2;
            }

            if (user.getPhotos().isEmpty()) {
                return 1;
            }

            return getCurrentItem().getPhotos().size();
        }

        @Override
        public int getItemPosition(Object o) {
            return POSITION_NONE;
        }
    }

    public static class UserSlideListener {
        public Boolean onPageScrolled(int i, float v, int i2) {
            return true;
        }

        public Boolean onPageSelected(int i) {
            return true;
        }

        public Boolean onPageScrollStateChanged(int i) {
            return true;
        }

        public Boolean onMovePrevious(SlideData previous, SlideData current, SlideData next) {
            return true;
        }

        public Boolean onMoveNext(SlideData previous, SlideData current, SlideData next) {
            return true;
        }

        public Boolean onBookmark(SlideData data) {
            return true;
        }

        public Boolean onChat(SlideData data) {
            return true;
        }

        public Boolean onProfile(SlideData data) {
            return true;
        }

        public Boolean onAssignSlideData(SlideData data) {
            return true;
        }

        public Boolean onAfterMove(SlideData data) {
            return true;
        }

        public Boolean loadMore(final int position, final ArrayList<SlideData> list) {
            return true;
        }
    }

    private class UserSlideBroadcastReceiver extends BroadcastReceiver {
        private HashMap<String, SlideData> mList;

        public UserSlideBroadcastReceiver() {
            mList = new HashMap<>();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            int userId = intent.getIntExtra("userId", 0);
            Boolean status = intent.getBooleanExtra("status", false);

            if (userId != 0 && !mSlideData.isEmpty()) {
                if (status) {
                    for (int i = 0, j = mSlideData.size(); i < j; i++) {
                        if (mSlideData.get(i).getUserId() == userId) {
                            mList.put(Integer.toString(i), mSlideData.get(i));

                            break;
                        }
                    }
                } else {
                    Set<String> keys = mList.keySet();

                    for (String index : keys) {
                        if (mList.get(index).getUserId() == userId) {
                            mSlideData.add(Integer.valueOf(index), mList.get(index));

                            if (hasPrev()) {
                                move(MOVE.PREV);
                            } else if (hasNext()) {
                                move(MOVE.NEXT);
                            }

                            break;
                        }
                    }
                }
            }
        }
    }

    private class UserSubscrobeBroadcastReceiver extends BroadcastReceiver {
        private HashMap<String, SlideData> mList;

        public UserSubscrobeBroadcastReceiver() {
            mList = new HashMap<>();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if( getActivity() == null )
            {
                return;
            }

            BaseServiceHelper helper = BaseServiceHelper.getInstance(getActivity().getApplication());
            HashMap<String,String> params = new HashMap<String,String>();
            params.put("photo", "view");

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
                        if (getActivity() != null && srd != null && srd.auth.photo_view != null) {
                            if ( srd.auth.photo_view.status != null )
                            {
                                for ( SlideData slide: mSlideData)
                                {
                                    if (AuthObject.STATUS_AVAILABLE.equals(srd.auth.photo_view.status)) {
                                        slide.setIsSubscribe(false);
                                        slide.setIsAuthorized(true);
                                    }
                                    else if (AuthObject.STATUS_PROMOTED.equals(srd.auth.photo_view.status))
                                    {
                                        slide.setIsSubscribe(true);
                                        slide.setIsAuthorized(false);
                                    }
                                    else if (AuthObject.STATUS_DISABLED.equals(srd.auth.photo_view.status))
                                    {
                                        slide.setIsSubscribe(false);
                                        slide.setIsAuthorized(false);
                                    }
                                }

                                mPagerAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
            });
        }
    }

    private class Auth {

        public AuthObject getPhoto_view() {
            return photo_view;
        }

        public void setPhoto_view(AuthObject photo_view) {
            this.photo_view = photo_view;
        }

        private AuthObject photo_view;
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
