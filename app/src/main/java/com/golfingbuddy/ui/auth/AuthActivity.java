package com.golfingbuddy.ui.auth;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.golfingbuddy.R;
import com.golfingbuddy.SkStaticData;
import com.golfingbuddy.core.SkApplication;
import com.golfingbuddy.core.SkServiceCallbackListener;
import com.golfingbuddy.model.base.BaseRestCommand;
import com.golfingbuddy.model.base.classes.SkSite;
import com.golfingbuddy.model.base.classes.SkUser;
import com.golfingbuddy.ui.base.MainFragmentActivity;
import com.golfingbuddy.ui.base.StatusActivity;
import com.golfingbuddy.ui.base.email_verification.EmailVerification;
import com.golfingbuddy.ui.fbconnect.FacebookLoginStepManager;
import com.golfingbuddy.ui.fbconnect.service.FacebookConnectService;
import com.golfingbuddy.ui.join.view.JoinForm;
import com.golfingbuddy.ui.search.fragments.EditFormFragment;
import com.golfingbuddy.ui.search.fragments.SearchFormFragment;
import com.golfingbuddy.ui.search.service.QuestionService;
import com.golfingbuddy.utils.SkApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

/**
 * Created by sardar on 4/30/14.
 */
public class AuthActivity extends BaseAuthActivity {

    private EditText mEmailView;
    private EditText mPasswordView;
    private Boolean blockButton = false;

    private ProgressBar progress;
    private TextView siteName;

    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.auth_auth_activity);
        super.onCreate(savedInstanceState);

        mEmailView = (EditText) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        progress = (ProgressBar) findViewById(R.id.progressBar);
        siteName = (TextView) findViewById(R.id.siteName);

        Button signIn = (Button) findViewById(R.id.sign_in);
        Button signUp = (Button) findViewById(R.id.sign_up);

        showChoose();

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthActivity.this.showSignIn();
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AuthActivity.this, JoinForm.class));
            }
        });

        TextView backToChoose = (TextView) findViewById(R.id.backToChoose);
        backToChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AuthActivity.this.showChoose();
            }
        });


        TextView changeSite = (TextView) findViewById(R.id.changeSite);
        changeSite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AuthActivity.this, UrlActivity.class));
            }
        });

        if (SkStaticData.SITE_URL != null) {
            changeSite.setVisibility(View.GONE);
        }

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        final Button mEmailSignInButton = (Button) findViewById(R.id.sign_in_button);

        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        LoginManager.getInstance().logOut();
        callbackManager = CallbackManager.Factory.create();
        final LoginButton authButton = (LoginButton) findViewById(R.id.authButton);
        authButton.setReadPermissions("email");
        authButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken accessToken = AccessToken.getCurrentAccessToken();

                if ( accessToken != null )
                {
                    GraphRequest request = GraphRequest.newMeRequest(accessToken,new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted( final JSONObject object, GraphResponse response ) {
                            try
                            {
                                FacebookConnectService service = new FacebookConnectService(AuthActivity.this.getApp());
                                service.tryLogin(object.getString("id"), new SkServiceCallbackListener() {
                                    @Override
                                    public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
                                        JsonObject json = SkApi.processResult(data);

                                        if (json != null && json.has("loggedIn") && json.get("loggedIn").getAsBoolean() == true) {
                                            SkApplication.saveOrUpdateSiteInfo(data, getApp());
                                            successSignInAction(AuthActivity.this);
                                            show();
                                        } else {
                                            startFBConnect(object);
                                        }
                                    }
                                });
                            }
                            catch (JSONException e)
                            {
                                LoginManager.getInstance().logOut();
                                Toast.makeText(AuthActivity.this, "Something is wrong", Toast.LENGTH_LONG).show();
                                Log.d("FB connect error:", e.getMessage());
                            }
                        }
                    });
                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "id,name,email,gender");
                    request.setParameters(parameters);
                    request.executeAsync();
                }

                hide();
            }

            @Override
            public void onCancel() {
                show();
            }

            @Override
            public void onError(FacebookException e) {
                Toast.makeText(AuthActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                show();
            }
        });

        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.skadatexapp", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }

        getBaseHelper().runRestRequest(BaseRestCommand.ACTION_TYPE.SITE_INFO, new SkServiceCallbackListener() {
            @Override
            public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
                getApp().saveOrUpdateSiteInfo(data, getApp());
                JsonObject dataObject = SkApi.processResult(data);

                if( SkApi.propExistsAndNotNull(dataObject, "isUserAuthenticated") ){
                    if( dataObject.get("isUserAuthenticated").getAsBoolean() ){
                        startActivity(new Intent(AuthActivity.this, MainFragmentActivity.class));
                    }
                }

                siteName.setText(SkApplication.getSiteInfo().getSiteName());
                showChoose();

                AuthActivity.this.findViewById(R.id.auth_progress_bar).setVisibility(View.GONE);
                AuthActivity.this.findViewById(R.id.initialPBar).setVisibility(View.GONE);

                SkSite siteInfo = SkApplication.getSiteInfo();

                if ( siteInfo != null && siteInfo.getFacebookPluginActive() != null && siteInfo.getFacebookAppId() != null )
                {
                    authButton.setVisibility(View.VISIBLE);
                    FacebookSdk.setApplicationId(siteInfo.getFacebookAppId());
                }
                else
                {
                    authButton.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void attemptLogin() {
        if (blockButton) {
            return;
        }

        blockButton = true;
        progress.setVisibility(View.VISIBLE);

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            blockButton = false;
            progress.setVisibility(View.GONE);

        } else {

            HashMap<String, String> params = new HashMap();
            params.put("username", email);
            params.put("password", password);

            //need to remove auth token
            SkApplication.setAuthToken(null);
            baseHelper.runRestRequest(BaseRestCommand.ACTION_TYPE.AUTHENTICATE, params, new SkServiceCallbackListener() {
                @Override
                public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
                    JsonObject resultData = SkApi.processResult(data);
                    if (resultData != null) {
                        if (SkApi.propExistsAndNotNull(resultData, "token")) {
                            SkApplication.saveOrUpdateSiteInfo(data, getApp());
                            successSignInAction(AuthActivity.this);
                        }
                    } else {
                        String result = data.getString("data");
                        if (result != null) {
                            resultData = new Gson().fromJson(result, JsonObject.class);
                            if( SkApi.propExistsAndNotNull(resultData, "data") ){
                                resultData = resultData.getAsJsonObject("data");
                            if (resultData.has("exception")) {
                                if (SkApi.propExistsAndNotNull(resultData, "userData")) {
                                    JsonObject temp = resultData.getAsJsonObject("userData");

                                    if (SkApi.propExistsAndNotNull(temp, "message")) {
                                        Toast.makeText(AuthActivity.this, temp.get("message").getAsString(), Toast.LENGTH_LONG).show();
                                    }
                                }
                                }
                            }
                        }
                    }

                    blockButton = false;
                    progress.setVisibility(View.GONE);
                }
            });
        }
    }

    public static void successSignInAction( Activity activity ) {
        SkUser user = SkApplication.getUserInfo();
        SkSite site = SkApplication.getSiteInfo();

        StatusActivity.STATUS_LIST specStatus = null;

        // clear search and edit form data
        QuestionService.getInstance().removeSharedPreferences(activity.getApplicationContext(), EditFormFragment.QUESTION_PREFIX);
        QuestionService.getInstance().removeSharedPreferences(activity.getApplicationContext(), SearchFormFragment.QUESTION_PREFIX);
        QuestionService.getInstance().removeSharedPreferences(activity.getApplicationContext(), JoinForm.PREFIX);


        // check status
        if( site.getMaintenance() ){
            specStatus = StatusActivity.STATUS_LIST.MAINTENANCE;
        }
        else if (user.getIsSuspended()) {
            specStatus = StatusActivity.STATUS_LIST.SUSPENDED;
        }
        else if (!user.getIsEmailVerified() && site.getConfirmEmail()) {
            Intent in = new Intent(activity, EmailVerification.class);
            in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(in);
            return;
        }
        else if (!user.getIsApproved() && site.getUserApprove()) {
            specStatus = StatusActivity.STATUS_LIST.NOT_APPROVED;
        }

        if (specStatus != null) {
            Intent in = new Intent(activity, StatusActivity.class);
            in.putExtra("type", specStatus);
            activity.startActivity(in);
        }
        else{
            Intent in = new Intent(activity, MainFragmentActivity.class);
            in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            in.putExtra("showHotList", true);
            activity.startActivity(in);
        }

        LocalBroadcastManager.getInstance(activity.getApplicationContext()).sendBroadcast(
                new Intent("base.user_sign_in")
        );
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return true;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 3;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        switch (resultCode) {
            case 500:
                LoginManager.getInstance().logOut();
                show();
                break;
            case 200:
                SkApplication.saveOrUpdateSiteInfo(data.getBundleExtra("userData"), getApp());
                successSignInAction(this);
                show();
                break;
        }
    }

    private void showSignIn() {
        findViewById(R.id.choose_layout).setVisibility(View.INVISIBLE);
        findViewById(R.id.backToChoose).setVisibility(View.VISIBLE);
        findViewById(R.id.password).setVisibility(View.VISIBLE);
        findViewById(R.id.email).setVisibility(View.VISIBLE);
        findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);

        if (SkStaticData.SITE_URL == null) {
            findViewById(R.id.changeSite).setVisibility(View.INVISIBLE);
        }
    }

    private void showChoose() {
        findViewById(R.id.choose_layout).setVisibility(View.VISIBLE);
        findViewById(R.id.backToChoose).setVisibility(View.INVISIBLE);
        findViewById(R.id.password).setVisibility(View.INVISIBLE);
        findViewById(R.id.email).setVisibility(View.INVISIBLE);
        findViewById(R.id.sign_in_button).setVisibility(View.GONE);

        if (SkStaticData.SITE_URL == null) {
            findViewById(R.id.changeSite).setVisibility(View.VISIBLE);
        }
    }


    private void hide() {
        findViewById(R.id.auth_progress_bar).setVisibility(View.VISIBLE);
        findViewById(R.id.auth_container).setVisibility(View.INVISIBLE);
    }

    private void show() {
        findViewById(R.id.auth_container).setVisibility(View.VISIBLE);
        findViewById(R.id.auth_progress_bar).setVisibility(View.INVISIBLE);
    }

    private void startFBConnect(JSONObject graphUser) {
        Intent intent = new Intent(AuthActivity.this, FacebookLoginStepManager.class);

        try
        {
            intent.putExtra("facebookId", graphUser.getString("id"))
                .putExtra("name", graphUser.getString("name"))
                .putExtra("gender", graphUser.has("gender") && !graphUser.isNull("gender") ? graphUser.getString("gender") : null)
                .putExtra("email", graphUser.has("email") && !graphUser.isNull("email") ? graphUser.getString("email") : null);

            startActivityForResult(intent, 200);
        }
        catch (JSONException e)
        {
            LoginManager.getInstance().logOut();
            Toast.makeText(this, "Something is wrong", Toast.LENGTH_LONG).show();
            Log.d("FB connect error:", e.getMessage());
        }
    }
}
