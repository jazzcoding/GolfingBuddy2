package com.golfingbuddy.utils.form;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.TextView;

import com.golfingbuddy.R;

/**
 * Created by jk on 2/3/16.
 */
public class PasswordField extends TextField {

    protected TextView passwordLabel;
    protected TextView repeatPasswordLabel;
    protected TextView repeatPasswordError;
    protected EditText repeatPassword;
    protected String mErrorMessage;
    private TextWatcher mTextWatcher;

    public PasswordField(Context context) {
        super(context);
        init(context, null);
    }

    public PasswordField(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context,attrs);
    }

    public PasswordField(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public PasswordField(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs)
    {
        mTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Dialog dialog = PasswordField.this.getDialog();

                if ( dialog == null )
                {
                    return;
                }

                boolean value = PasswordField.this.valiadte();

                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setClickable(value);
                PasswordField.this.showDialogError(value);
            }
        };

        repeatPassword = new EditText(context, attrs);
        repeatPassword.setId(R.id.password);
        repeatPassword.setEnabled(true);
        repeatPassword.addTextChangedListener(mTextWatcher);

        getEditText().addTextChangedListener(mTextWatcher);

        passwordLabel = new TextView(context, attrs);
        passwordLabel.setId(R.id.password_label);
        passwordLabel.setText(R.string.password_label);

        repeatPasswordLabel = new TextView(context, attrs);
        repeatPasswordLabel.setId(R.id.repeat_password_label);
        repeatPasswordLabel.setText(R.string.repeat_password_label);


        repeatPasswordError = new TextView(context, attrs);
        repeatPasswordError.setId(R.id.password_error);
        repeatPasswordError.setTextColor(context.getResources().getColor(R.color.question_error_message_color));
        repeatPasswordError.setTextAppearance(context, R.style.PasswordMessage);
        repeatPasswordError.setVisibility(View.GONE);
        repeatPasswordError.setText("");

        setDialogLayoutResource(R.layout.password_dialog);
    }

    protected View onCreateView(ViewGroup parent) {
        View view = super.onCreateView(parent);

        TextView summary = (TextView)view.findViewById(android.R.id.summary);

        if ( summary != null)
        {
            summary.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }

        return view;
    }

    @Override
    protected void onBindDialogView(View view) {

        TextView passwordLabel = this.passwordLabel;
        TextView repeatPasswordLabel = this.repeatPasswordLabel;
        TextView repeatPasswordError = this.repeatPasswordError;

        ViewParent oldParent = passwordLabel.getParent();
        if (oldParent != view) {
            if (oldParent != null) {
                ((ViewGroup) oldParent).removeView(passwordLabel);
            }
            onAddTextViewToDialogView(view, passwordLabel);
        }

        super.onBindDialogView(view);

        EditText repeatPassword = this.repeatPassword;

        oldParent = repeatPasswordLabel.getParent();
        if (oldParent != view) {
            if (oldParent != null) {
                ((ViewGroup) oldParent).removeView(repeatPasswordLabel);
            }
            onAddTextViewToDialogView(view, repeatPasswordLabel);
        }

        oldParent = repeatPassword.getParent();
        if (oldParent != view) {
            if (oldParent != null) {
                ((ViewGroup) oldParent).removeView(repeatPassword);
            }
            onAddEditTextToDialogView(view, repeatPassword);
        }

        oldParent = repeatPasswordError.getParent();
        if (oldParent != view) {
            if (oldParent != null) {
                ((ViewGroup) oldParent).removeView(repeatPasswordError);
            }
            onAddTextViewToDialogView(view, repeatPasswordError);
        }


    }

    protected void onAddTextViewToDialogView(View dialogView, TextView editText) {
        ViewGroup container = (ViewGroup) dialogView
                .findViewById(R.id.edittext_container);
        if (container != null) {
            container.addView(editText, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    protected void onAddEditTextToDialogView(View dialogView, EditText editText) {
        editText.setText("");
        editText.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD);

        ViewGroup container = (ViewGroup) dialogView
                .findViewById(R.id.edittext_container);
        if (container != null) {
            container.addView(editText, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    protected void showDialog(Bundle state) {
        super.showDialog(state);
        AlertDialog d = (AlertDialog) getDialog();

        d.getButton(AlertDialog.BUTTON_POSITIVE).setClickable(false);
    }

    protected void showDialogError(boolean isValid) {
        if ( isValid )
        {
            repeatPasswordError.setText("");
            repeatPasswordError.setVisibility(View.GONE);
        }
        else
        {
            repeatPasswordError.setText(mErrorMessage);
            repeatPasswordError.setVisibility(View.VISIBLE);
        }
    }

    protected boolean valiadte() {
        EditText editText = getEditText();
        Editable value = editText.getText();

        if ( value == null || "".equals(value.toString()) || value.length() < 4  )
        {
            if ( getContext() != null ) {
                mErrorMessage = getContext().getResources().getString(R.string.empty_password_error_message);
            }
            return false;
        }

        Editable repeatValue = repeatPassword.getText();

        if ( value != null  )
        {
            if( repeatValue != null && !value.toString().equals(repeatValue.toString()) ) {
                if (getContext() != null) {
                    mErrorMessage = getContext().getResources().getString(R.string.not_equal_passwords_error_message);
                }
                return false;
            }
        }

        mErrorMessage = "";
        return true;
    }
}
