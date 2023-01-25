package com.golfingbuddy.core;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.golfingbuddy.R;
import com.golfingbuddy.model.base.classes.SkSite;
import com.golfingbuddy.model.base.classes.SkUser;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kairat on 10/12/15.
 */
public class SkAds {
    private static boolean isInitialized = false;
    private static Map<String, AdsView> adsViews = new HashMap<>();

    private SkAds() {

    }

    public static void init(SkApplication application) {
        if (isAppConfigured()) {
            isInitialized = true;
        } else {
            LocalBroadcastManager.getInstance(application).registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (isInitialized) {
                        return;
                    }

                    isInitialized = isAppConfigured();
                }
            }, new IntentFilter("base.user_sign_in"));
        }
    }

    private static boolean isAppConfigured() {
        SkSite site = SkApplication.getSiteInfo();

        return site != null && !TextUtils.isEmpty(site.getAdsApiKey());
    }

    private static String getActivityName(Activity activity) {
        return activity.getClass().getSimpleName();
    }

    public static void initView(Activity activity) {
        if (!isAppConfigured()) {
            return;
        }

        final String activityName = getActivityName(activity);

        if (adsViews.containsKey(activityName)) {
            return;
        }

        AdsView adsView = null;

        switch (activityName) {
            case "ProfileViewActivity":
                adsView = new AdsProfileView(activity);
                break;
            case "MainFragmentActivity":
            case "FilterActivity":
                adsView = new AdsMainFragmentActivity(activity);
                break;
            case "EditProfileActivity":
                adsView = new AdsEditProfileActivity(activity);
                break;
            case "AlbumListViewActivity":
                adsView = new AdsAlbumListViewActivity(activity);
                break;
            case "PhotoListManageActivity":
                adsView = new AdsPhotoListManageActivity(activity);
                break;
            case "ComposeActivity":
                adsView = new AdsComposeActivity(activity);
                break;
            case "MailActivity":
                adsView = new AdsMailActivity(activity);
                break;
            case "ReplyActivity":
                adsView = new AdsReplyActivity(activity);
                break;
            case "ChatActivity":
                adsView = new AdsChatActivity(activity);
                break;
            case "AlbumPhotoListViewActivity":
                adsView = new AdsAlbumPhotoListViewActivity(activity);
                break;
            case "MembershipAndCreditsActivity":
            case "MembershipsActivity":
            case "CreditsActivity":
                adsView = new AdsMembershipActivity(activity);
                break;
            case "MembershipDetailsActivity":
                adsView = new AdsMembershipDetailsActivity(activity);
                break;
            case "CreditActionsActivity":
                adsView = new AdsCreditActionsActivity(activity);
                break;
            case "PhotoViewActivity":
                adsView = new AdsPhotoViewActivity(activity);
                break;
        }

        if (adsView != null) {
            adsView.addAds(activity);
            adsView.build();
            adsViews.put(activityName, adsView);
        }
    }

    public static void onPause(Activity activity) {
        final String activityName = getActivityName(activity);

        if (adsViews.containsKey(activityName)) {
            adsViews.get(activityName).pause();
        }
    }

    public static void onResume(Activity activity) {
        final String activityName = getActivityName(activity);

        if (adsViews.containsKey(activityName)) {
            adsViews.get(activityName).resume();
        }
    }

    public static void onDestroy(Activity activity) {
        final String activityName = getActivityName(activity);

        if (adsViews.containsKey(activityName)) {
            adsViews.get(activityName).destroy();
            adsViews.remove(activityName);
        }
    }

    private abstract static class AdsView {
        protected AdView adView;

        public AdsView(Context context) {
            adView = new AdView(context);
            adView.setAdSize(AdSize.SMART_BANNER);
            adView.setAdUnitId(SkApplication.getSiteInfo().getAdsApiKey().trim());

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            adView.setLayoutParams(layoutParams);
        }

        public void pause() {
            adView.pause();
        }

        public void resume() {
            adView.resume();
        }

        public void destroy() {
            adView.destroy();
        }

        public void addAds(Activity activity) {
            adView.setId(getId());
            ((ViewGroup)activity.findViewById(getParentId())).addView(adView);
            ((RelativeLayout.LayoutParams)activity.findViewById(getTopSiblingId()).getLayoutParams()).
                    addRule(RelativeLayout.ABOVE, adView.getId());
        }

        public void build() {
            SkUser user = SkApplication.getUserInfo();
            AdRequest.Builder request = new AdRequest.Builder();

            if (user != null) {
                if (user.hasBirthday()) {
                    final Map<String, Integer> birthday = user.getBirthday();
                    request.setBirthday(new GregorianCalendar(
                            birthday.get("year").intValue(),
                            birthday.get("month").intValue(),
                            birthday.get("day").intValue()
                    ).getTime());
                }

                if (user.getSex() == 1) {
                    request.setGender(AdRequest.GENDER_MALE);
                } else if (user.getSex() == 2) {
                    request.setGender(AdRequest.GENDER_FEMALE);
                }
            }

            adView.loadAd(request.build());
        }

        public abstract int getId();

        public abstract int getParentId();

        public abstract int getTopSiblingId();
    }

    private static class AdsProfileView extends AdsView {
        public AdsProfileView(Context context) {
            super(context);
        }

        @Override
        public int getId() {
            return R.id.ads_profile_view;
        }

        @Override
        public int getParentId() {
            return R.id.content_layout;
        }

        @Override
        public int getTopSiblingId() {
            return R.id.root_layout;
        }
    }

    private static class AdsMainFragmentActivity extends AdsView {
        public AdsMainFragmentActivity(Context context) {
            super(context);
        }

        @Override
        public int getId() {
            return R.id.ads_main_fragment_view;
        }

        @Override
        public int getParentId() {
            return R.id.content;
        }

        @Override
        public int getTopSiblingId() {
            return R.id.content_frame;
        }
    }

    private static class AdsEditProfileActivity extends AdsView {
        public AdsEditProfileActivity(Context context) {
            super(context);
        }

        @Override
        public int getId() {
            return R.id.ads_edit_profile_view;
        }

        @Override
        public int getParentId() {
            return R.id.root;
        }

        @Override
        public int getTopSiblingId() {
            return R.id.filter;
        }
    }

    private static class AdsAlbumListViewActivity extends AdsView {
        public AdsAlbumListViewActivity(Context context) {
            super(context);
        }

        @Override
        public int getId() {
            return R.id.ads_album_list_view;
        }

        @Override
        public int getParentId() {
            return R.id.root;
        }

        @Override
        public int getTopSiblingId() {
            return R.id.gridView;
        }
    }

    private static class AdsPhotoListManageActivity extends AdsView {
        public AdsPhotoListManageActivity(Context context) {
            super(context);
        }

        @Override
        public int getId() {
            return R.id.ads_album_list_view;
        }

        @Override
        public int getParentId() {
            return R.id.photomanage_root;
        }

        @Override
        public int getTopSiblingId() {
            return R.id.gridView;
        }
    }

    private static class AdsComposeActivity extends AdsView {
        public AdsComposeActivity(Context context) {
            super(context);
        }

        @Override
        public int getId() {
            return R.id.ads_compose_view;
        }

        @Override
        public int getParentId() {
            return R.id.root;
        }

        @Override
        public int getTopSiblingId() {
            return R.id.mailbox_compose_content;
        }
    }

    private static class AdsMailActivity extends AdsView {
        public AdsMailActivity(Context context) {
            super(context);
        }

        @Override
        public int getId() {
            return R.id.ads_mail_view;
        }

        @Override
        public int getParentId() {
            return R.id.mailbox_mail_root;
        }

        @Override
        public int getTopSiblingId() {
            return R.id.mailbox_mail_content;
        }
    }

    private static class AdsReplyActivity extends AdsView {
        public AdsReplyActivity(Context context) {
            super(context);
        }

        @Override
        public int getId() {
            return R.id.ads_reply_view;
        }

        @Override
        public int getParentId() {
            return R.id.mailbox_reply_root;
        }

        @Override
        public int getTopSiblingId() {
            return R.id.mailbox_reply_content;
        }
    }

    private static class AdsChatActivity extends AdsView {
        public AdsChatActivity(Context context) {
            super(context);
        }

        @Override
        public int getId() {
            return R.id.ads_chat_view;
        }

        @Override
        public int getParentId() {
            return R.id.mailbox_chat_root;
        }

        @Override
        public int getTopSiblingId() {
            return R.id.mailbox_chat_content;
        }
    }

    private static class AdsAlbumPhotoListViewActivity extends AdsView {
        public AdsAlbumPhotoListViewActivity(Context context) {
            super(context);
        }

        @Override
        public int getId() {
            return R.id.ads_album_photo_list_view;
        }

        @Override
        public int getParentId() {
            return R.id.photo_view_root;
        }

        @Override
        public int getTopSiblingId() {
            return R.id.gridViewParent;
        }
    }

    private static class AdsMembershipActivity extends AdsView {
        public AdsMembershipActivity(Context context) {
            super(context);
        }

        @Override
        public int getId() {
            return R.id.ads_album_photo_list_view;
        }

        @Override
        public int getParentId() {
            return R.id.root_layout;
        }

        @Override
        public int getTopSiblingId() {
            return R.id.pager;
        }
    }

    private static class AdsMembershipDetailsActivity extends AdsView {
        public AdsMembershipDetailsActivity(Context context) {
            super(context);
        }

        @Override
        public int getId() {
            return R.id.ads_membership_details_view;
        }

        @Override
        public int getParentId() {
            return R.id.membership_details_root;
        }

        @Override
        public int getTopSiblingId() {
            return R.id.benefits;
        }
    }

    private static class AdsCreditActionsActivity extends AdsView {
        public AdsCreditActionsActivity(Context context) {
            super(context);
        }

        @Override
        public int getId() {
            return R.id.ads_credit_actions_view;
        }

        @Override
        public int getParentId() {
            return R.id.credit_actions_root;
        }

        @Override
        public int getTopSiblingId() {
            return R.id.credit_actions_content;
        }
    }

    private static class AdsPhotoViewActivity extends AdsView {
        public AdsPhotoViewActivity(Context context) {
            super(context);
        }

        @Override
        public int getId() {
            return R.id.ads_photo_view;
        }

        @Override
        public int getParentId() {
            return R.id.photo_view_root;
        }

        @Override
        public int getTopSiblingId() {
            return R.id.pager;
        }
    }
}
