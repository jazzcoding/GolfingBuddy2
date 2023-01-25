package com.golfingbuddy.utils.form;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.golfingbuddy.R;
import com.golfingbuddy.ui.search.service.QuestionService;
import com.soundcloud.android.crop.Crop;

/**
 * Created by jk on 2/8/16.
 */
public class AvatarField extends Preference implements FormField {
    protected ImageView avatar;
    protected OnClickListener dialogClickListener;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AvatarField(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public AvatarField(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public AvatarField(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AvatarField(Context context) {
        super(context);
        init();
    }

    protected void init() {
        QuestionService.getInstance().preparePreference(this);

        setLayoutResource(R.layout.join_avatar_preference_layout);
        setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                startAvatarUploadDialog();

                return false;
            }
        });
    }

    protected View errorIcon;
    protected TextView error;
    protected String errorMessage;

    @Override
    protected View onCreateView(ViewGroup parent) {
        View view = super.onCreateView(parent);
        errorIcon = view.findViewById(R.id.error_icon);
        error = (TextView)view.findViewById(R.id.error);
        avatar = (ImageView)view.findViewById(R.id.avatar);

        showAvatar();
        displayError();

        return view;
    }

    protected void displayError() {
        if ( errorMessage != null )
        {
            if ( error != null ) {
                error.setText(errorMessage);
                error.setVisibility(View.VISIBLE);
            }

            if ( errorIcon != null )
            {
                errorIcon.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            if ( error != null ) {
                error.setText("");
                error.setVisibility(View.INVISIBLE);
            }

            if ( errorIcon != null )
            {
                errorIcon.setVisibility(View.INVISIBLE);
            }
        }
        notifyChanged();
    }

    @Override
    public void setError(String value) {
        errorMessage = value;
        displayError();
    }

    @Override
    public void setValue(String value) {
        persistString(value);
        showAvatar();
    }

    protected void showAvatar()
    {
        if ( avatar != null  )
        {
            Uri uri = null;
            String value = getValue();
            if ( value != null )
            {
                try {
                    uri = Uri.parse(value);
                }
                catch(NullPointerException ex ) {

                }

            }
            else
            {
                avatar.setImageResource(R.drawable.join_default_avatar);
            }

            if ( uri != null ) {
                // hack to redraw image
                avatar.setImageDrawable(null);

                avatar.setImageURI(uri);
            }
        }
    }

    @Override
    public String getValue() {
        return getPersistedString(null);
    }

    public void setOnDialogButtonClickListener( OnClickListener listener ) {
        dialogClickListener = listener;
    }

    protected void startAvatarUploadDialog() {
        final Resources resorces = getContext().getResources();
        
        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(getContext());
        myAlertDialog.setTitle(resorces.getString(R.string.upload_avatar_dialog_title));
        myAlertDialog.setMessage(resorces.getString(R.string.upload_avatar_dialog_message));

        myAlertDialog.setPositiveButton(resorces.getString(R.string.photoupload_gallery_label),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        if (dialogClickListener != null) {
                            dialogClickListener.onGallaryClick(arg0, arg1);
                        }
                    }
                });

        myAlertDialog.setNegativeButton(resorces.getString(R.string.photoupload_camera_label),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        if (dialogClickListener != null) {
                            dialogClickListener.onCameraClick(arg0, arg1);
                        }
                    }
                });
        myAlertDialog.show();
    }

    public static interface OnClickListener {
        public void onCameraClick(DialogInterface arg0, int arg1);

        public void onGallaryClick(DialogInterface arg0, int arg1);
    }
}
