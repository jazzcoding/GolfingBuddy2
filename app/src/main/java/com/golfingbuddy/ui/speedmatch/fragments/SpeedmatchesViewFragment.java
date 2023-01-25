package com.golfingbuddy.ui.speedmatch.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.golfingbuddy.R;
import com.golfingbuddy.ui.matches.classes.SlidePhoto;
import com.golfingbuddy.ui.memberships.SubscribeOrBuyActivity;
import com.golfingbuddy.utils.SKImageView;

public class SpeedmatchesViewFragment extends Fragment implements View.OnClickListener {

    private static final int SUBSCRIBE_ACTIVITY_RESULT_CODE = 7436241;
    private static final String userPhoto = "userPhoto";
    private SlidePhoto mPhoto;
    private int id;
    private ClickListener listener;
    private boolean mEnableSubscribeClick = false;

    public static SpeedmatchesViewFragment create(SlidePhoto photo, int id) {
        SpeedmatchesViewFragment fragment = new SpeedmatchesViewFragment();

        Bundle args = new Bundle();
        args.putParcelable(userPhoto, photo);
        args.putInt("id", id);
        fragment.setArguments(args);

        return  fragment;
    }

    public SpeedmatchesViewFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPhoto = getArguments().getParcelable(userPhoto);
        id = getArguments().getInt("id");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.slide_view_fragment, container, false);
        SKImageView imageView = (SKImageView) root.findViewById(R.id.slide_view_avatar);

        if (mPhoto.getIsAuthorized()) {
            showProgressBar(root);

            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageUrl(mPhoto.getPhoto() + "", new Callback(root));
        } else {
            TextView textView = (TextView) root.findViewById(R.id.slide_label);
            textView.setVisibility(View.VISIBLE);

            if (mPhoto.getIsSubscribe()) {
                imageView.setImageResource(R.drawable.lock_enable);
                textView.setText(mPhoto.getAuthorizeMsg());
                mEnableSubscribeClick = true;
            } else {
                imageView.setImageResource(R.drawable.lock_disable);
                textView.setTextColor(getResources().getColor(R.color.matches_remark));
                textView.setText(mPhoto.getAuthorizeMsg());
            }

            LinearLayout linearLayout = (LinearLayout) root.findViewById(R.id.slide_content);
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
        }
        imageView.setOnClickListener(this);

        return root;
    }

    private void showProgressBar(View view) {
        view.findViewById(R.id.slide_progress_bar).setVisibility(View.VISIBLE);
    }

    private class Callback implements com.squareup.picasso.Callback {
        private View view;

        public Callback(View view) {
            this.view = view;
        }

        @Override
        public void onSuccess() {
            view.findViewById(R.id.slide_progress_bar).setVisibility(View.GONE);
        }

        @Override
        public void onError() {
            ((SKImageView) view.findViewById(R.id.slide_view_avatar)).setImageResource(R.drawable.no_avatar);
            ((ImageView) view.findViewById(R.id.slide_view_avatar)).setScaleType(ImageView.ScaleType.CENTER);
            view.findViewById(R.id.slide_progress_bar).setVisibility(View.GONE);
        }
    }

    public ClickListener getListener() {
        return listener;
    }

    public void setListener(ClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View v) {
        Fragment fragment = getFragmentManager().findFragmentById(id);

        if (fragment == null) {
            return;
        }

        if (mPhoto.getIsSubscribe() && mEnableSubscribeClick) {
//            if ( mPhoto.getSubscribeActionKey() != null && mPhoto.getSubscribePluginKey() != null ) {
            Intent intent = new Intent(getActivity(), SubscribeOrBuyActivity.class);
//                intent.putExtra("pluginKey", mPhoto.getSubscribePluginKey());
//                intent.putExtra("actionKey", mPhoto.getSubscribeActionKey());
            intent.putExtra("pluginKey", "photo");
            intent.putExtra("actionKey", "view");

            startActivityForResult(intent, SUBSCRIBE_ACTIVITY_RESULT_CODE);
//            }
        }
    }

    public static abstract class ClickListener {
        public abstract void onClick(Fragment fragment, View view);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ( requestCode == SUBSCRIBE_ACTIVITY_RESULT_CODE && getActivity() != null ) {

            Intent intent = new Intent("base.user_subscribe_status_changed");
            LocalBroadcastManager.getInstance(getActivity().getApplication()).sendBroadcast(intent);
        }
    }
}
